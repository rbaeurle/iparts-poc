/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit.DictMetaListContainer;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit.iPartsTransitMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper um aus einer iPartsDataDictMetaList mit neu erzeugten Texten eine Liste von iPartsDataDictTransJob zu erzeugen und zu speichern
 */
public class iPartsDictTransJobHelper {

    private static final String TITLE = "!!Übernahme neuer Texte in den Übersetzungsumfang";
    private static final int NOT_VALID_COUNT = -1;
    private static final Language[] VALID_SOURCE_LANGUAGES = { Language.DE, Language.EN };
    public static final String JOB_ID_PREFIX_CAR_AND_VAN = "P";
    public static final String JOB_ID_PREFIX_TRUCK_AND_BUS = "T";

    public static int storeTransJob(AbstractJavaViewerForm owner, DictMetaListContainer dataDictMetaList) {
        iPartsDictTransJobHelper helper = new iPartsDictTransJobHelper(owner.getProject());
        return helper.addNewTextsToTransJob(dataDictMetaList);
    }

    private final EtkProject project;

    public iPartsDictTransJobHelper(EtkProject project) {
        this.project = project;
    }

    public int addNewTextsToTransJob(final DictMetaListContainer dataDictMetaContainer) {
        if (!dataDictMetaContainer.isEmpty()) {
            final VarParam<Integer> result = new VarParam<>(0);
            // Log-Datei für die Logmeldungen
            final EtkMessageLogForm messageLogForm = new EtkMessageLogForm(TITLE, "!!Suche und verarbeite neue Texte...", null);
            String title = TranslationHandler.translateForLanguage(TITLE, iPartsConst.LOG_FILES_LANGUAGE);
            final DWFile logFile = iPartsJobsManager.getInstance().addDefaultLogFileToMessageLog(messageLogForm.getMessageLog(),
                                                                                                 title,
                                                                                                 iPartsPlugin.LOG_CHANNEL_DEBUG);
            messageLogForm.showModal(new FrameworkRunnable() {
                @Override
                public void run(FrameworkThread thread) {
                    boolean hasErrors = false;
                    try {
                        // Das ImportPlugin nicht aktiv ist, kann kein XML Export stattfinden -> Gar nicht erst Objekte anlegen
                        if (iPartsPlugin.isImportPluginActive()) {
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Starte Verarbeitung " +
                                                                                                    "von %1 gefundenen %2",
                                                                                                    String.valueOf(dataDictMetaContainer.size()),
                                                                                                    (dataDictMetaContainer.size() > 1) ? "Texten" : "Text"),
                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                            // Map mit Ausgangssprache und Textart auf einen JobContainer
                            Map<String, SourceLangAndTextKindGroup> jobContainerForLanguagesAndTextKind = new LinkedHashMap<>();
                            // Alle DataDictMetas durchgehen und für jede Ausgangssprache-Zielsprache-Textart-Kombination einen
                            // TransJob anlegen.
                            int storedJobsObjects = 0;
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Durchlaufe alle %1" +
                                                                                                    " gefundenen Texte und erzeuge" +
                                                                                                    " alle Ausgangssprache-Zielsprache-" +
                                                                                                    "Textart Kombinationen...", String.valueOf(dataDictMetaContainer.size())),
                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                            // Zähler für alle restlichen TransJobContainer, die am Ende abgearbeitet werden müssen
                            VarParam<Integer> remainingJobsCount = new VarParam<>(0);
                            // Archive nach Gültigkeitsbereichen trennen. Hier werden die Texte innerhalb ihrer Gültigkeiten weitergegeben
                            storedJobsObjects += handleDictMetas(dataDictMetaContainer.getCarAndVanTexts(), JOB_ID_PREFIX_CAR_AND_VAN, jobContainerForLanguagesAndTextKind, remainingJobsCount, 0, false);
                            storedJobsObjects += handleDictMetas(dataDictMetaContainer.getTruckAndBusTexts(), JOB_ID_PREFIX_TRUCK_AND_BUS, jobContainerForLanguagesAndTextKind, remainingJobsCount, dataDictMetaContainer.getCarAndVanTexts().size(), isTruckTranslationsActive());

                            messageLogForm.getMessageLog().hideProgress();
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Alle Ausgangssprache-Zielsprache-Textart Kombinationen für die gefundenen Texte erzeugt."),
                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Durchlaufe alle noch nicht verarbeiteten " +
                                                                                                    "Ausgangssprache-Zielsprache-Textart Kombinationen " +
                                                                                                    "und erzeuge die Übersetzungspakete...", String.valueOf(dataDictMetaContainer.size())),
                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                            // Noch nicht bearbeitete JobContainer hier abarbeiten
                            int jobCount = 0;
                            // Durchlaufe alle Ausgangssprache-Textart Gruppen, die übrig geblieben sind
                            groupLoop:
                            for (SourceLangAndTextKindGroup jobsContainerMap : jobContainerForLanguagesAndTextKind.values()) {
                                for (TranslationJobContainer jobsContainer : jobsContainerMap.getAllTransJobContainer()) {
                                    if (jobsContainer.getDictMetaCount() > 0) {
                                        // Wurde "-1" zurückgeliefert, dann ist das Plugin nicht aktiv -> rausspringen
                                        int tempJobs = startExport(jobsContainer, messageLogForm.getMessageLog());
                                        if (tempJobs == NOT_VALID_COUNT) {
                                            break groupLoop;
                                        }
                                        storedJobsObjects += tempJobs;
                                    }
                                    jobCount++;
                                    messageLogForm.getMessageLog().fireProgress(jobCount, remainingJobsCount.getValue(), "", true, true);
                                }
                            }
                            result.setValue(storedJobsObjects);
                            messageLogForm.getMessageLog().hideProgress();
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Alle Übersetzungspakete für die gefundenen Texte erzeugt."),
                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                            boolean moreThanOneStoredDictMeta = dataDictMetaContainer.size() > 1;
                            boolean moreThanOneJobEntries = storedJobsObjects > 1;
                            messageLogForm.getMessageLog().fireMessageWithSeparators(TranslationHandler.translate("!!Übernahme beendet. %1 %2 " +
                                                                                                                  "verarbeitet. %3 %4 für den Übersetzungsumfang" +
                                                                                                                  " erzeugt.", String.valueOf(dataDictMetaContainer.size()),
                                                                                                                  moreThanOneStoredDictMeta ? "Ausgangstexte" : "Ausgangstext",
                                                                                                                  String.valueOf(storedJobsObjects),
                                                                                                                  moreThanOneJobEntries ? "Einträge" : "Eintrag"),
                                                                                     MessageLogOption.TIME_STAMP);
                        } else {
                            // Hinweis, dass das Plugin nicht aktiv ist
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Erzeugung des Übersetzungsauftrags konnte nicht durchgeführt werden, weil das benötigte Import-Plugin nicht aktiv ist"));
                            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "Import Plugin must be active for starting the translation process!");
                        }
                    } catch (Exception e) {
                        Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                        hasErrors = true;
                    } finally {
                        finishLogFile(hasErrors);
                        clearExporter();
                    }
                }

                /**
                 * Verarbeitet die übergebenen Texte und sammelt alle {@link SourceLangAndTextKindGroup} auf
                 *
                 * @param dictMetaList
                 * @param jobIdPrefix
                 * @param jobContainerForLanguagesAndTextKind
                 * @param remainingJobsCount
                 * @param dictMetaCount
                 * @param isTruckObjectStoreTranslations
                 * @return
                 */
                private int handleDictMetas(iPartsDataDictMetaList dictMetaList, String jobIdPrefix,
                                            Map<String, SourceLangAndTextKindGroup> jobContainerForLanguagesAndTextKind,
                                            VarParam<Integer> remainingJobsCount, int dictMetaCount, boolean isTruckObjectStoreTranslations) {
                    int storedJobsObjects = 0;
                    for (iPartsDataDictMeta dataDictMeta : dictMetaList) {
                        // für jedes dataDictMeta die Einträge erzeugen
                        storedJobsObjects += handleTransJobFromDictMeta(dataDictMeta, jobIdPrefix,
                                                                        jobContainerForLanguagesAndTextKind,
                                                                        messageLogForm.getMessageLog(),
                                                                        remainingJobsCount, isTruckObjectStoreTranslations);
                        dictMetaCount++;
                        messageLogForm.getMessageLog().fireProgress(dictMetaCount, dataDictMetaContainer.size(), "", true, true);
                    }
                    return storedJobsObjects;
                }

                /**
                 * Verschiebt die Log-Datei abhängig vom Übernahmeergebnis.
                 * @param hadErrors
                 */
                private void finishLogFile(boolean hadErrors) {
                    if (logFile != null) {
                        if (hadErrors) {
                            iPartsJobsManager.getInstance().jobError(logFile);
                        } else {
                            iPartsJobsManager.getInstance().jobProcessed(logFile);
                        }
                    }
                }
            });
            return result.getValue();
        }
        return 0;
    }

    private void clearExporter() {
        if (iPartsPlugin.isImportPluginActive()) {
            de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation.TranslationsExportHelper.getInstance().clearJobId();
        }
    }

    /**
     * Für jede nicht vorhandene Sprache einen Eintrag in einem JobContainer erzeugen. Wenn die Obergrenzen an Datensätzen
     * pro Ausgangssprache-Textart-Zielsprache Kombination erreicht wird, wird der Prozess zum erzeugen der XML/ZIP Datei
     * und das speichern der {@link iPartsDataDictTransJob} Objekte gestartet.
     *
     * @param dataDictMeta
     * @param jobContainerForLanguagesAndTextKind
     * @param messageLog
     * @param remainingJobsCount
     * @param isTruckObjectStoreTranslations
     */
    private int handleTransJobFromDictMeta(iPartsDataDictMeta dataDictMeta, String jobIdPrefix,
                                           Map<String, SourceLangAndTextKindGroup> jobContainerForLanguagesAndTextKind,
                                           EtkMessageLog messageLog, VarParam<Integer> remainingJobsCount, boolean isTruckObjectStoreTranslations) {

        // alle bestehenden Sprachen bestimmen
        List<Language> existingLanguages = new DwList<>();
        for (iPartsDataDictLanguageMeta dataDictLanguageMeta : dataDictMeta.getLanguages()) {
            existingLanguages.add(Language.findLanguage(dataDictLanguageMeta.getAsId().getLanguage()));
        }
        // bei mehreren neu angelegten Sprachen die Quell-Sprache bestimmen
        Language sourceLang = existingLanguages.isEmpty() ? null : findSourceLang(dataDictMeta.getMultiLang());
        if (sourceLang == null) {
            messageLog.fireMessage(TranslationHandler.translate("!!Bei %1 sind nur leere Texte vorhanden! Wird übersprungen",
                                                                dataDictMeta.getAsId().toStringForLogMessages()));
            return 0;
        }
        // Log-Meldung
        String logMessage = TranslationHandler.translate("!!Für die Text-Id \"%1\" wurde als Ausgangssprache \"%2\" bestimmt.",
                                                         dataDictMeta.getTextId(), sourceLang.getCode());
        logMessage += getLogMessageAdditionalLanguages(existingLanguages, sourceLang);
        messageLog.fireMessage(logMessage, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        int storedJobsObjects = 0;
        // Liste der vorhandenen Sprachen holen CLM
        Map<String, Language> transitMappingMap = iPartsTransitMappingCache.getInstance(project).getIsoClmMapping();
        // Maximum, das via Admin-Option gesetzt wurde
        int maxEntriesPerFile = getMaxEntriesForFile(isTruckObjectStoreTranslations);
        if (maxEntriesPerFile > NOT_VALID_COUNT) {
            // Kenner, falls ein TransJobContainer der aktuellen Gruppe über die eingestellte max. Anzahl Einträge
            // gekommen ist
            boolean exportGroup = false;
            // Schlüssel aus Ausgangssprache, Textart und ggfs.Job-ID Präfix für einen JobContainer. Jede Gruppe erhält
            // später eine eigene JobId.
            String groupKey = sourceLang + "|" + dataDictMeta.getAsId().getTextKindId();
            if (StrUtils.isValid(jobIdPrefix)) {
                groupKey += "|" + jobIdPrefix;
            }
            // Durchlaufe alle CLM Sprachen und erzeuge für alle nicht vorhandenen Übersetzungen einen Eintrag im dazugehörigen
            // JobContainer
            for (Language destLang : transitMappingMap.values()) {
                if (!existingLanguages.contains(destLang)) {
                    SourceLangAndTextKindGroup transJobSourcLangAndTextkindGroup = jobContainerForLanguagesAndTextKind.get(groupKey);
                    if (transJobSourcLangAndTextkindGroup == null) {
                        // Job-Id pro Ausgangssprache und Textart erzeugen. Ist die Trennung der Texte nach Gültigkeit aktiv,
                        // bekommen IPARTS-TRUCK und IPARTS-MB Texte eigene Job-IDs
                        String jobId = getNextJobId(jobIdPrefix);
                        if (jobId == null) {
                            return NOT_VALID_COUNT;
                        }
                        transJobSourcLangAndTextkindGroup = new SourceLangAndTextKindGroup(groupKey, jobId);
                        jobContainerForLanguagesAndTextKind.put(groupKey, transJobSourcLangAndTextkindGroup);
                    }
                    TranslationJobContainer transJobContainer = transJobSourcLangAndTextkindGroup.getTransJobForDestLang(destLang);
                    if (transJobContainer == null) {
                        remainingJobsCount.setValue(remainingJobsCount.getValue() + 1);
                        iPartsDataDictTextKind textKind = DictTxtKindIdByMADId.getInstance(project).findDictTextKindByTextKindId(new iPartsDictTextKindId(dataDictMeta.getAsId().getTextKindId()),
                                                                                                                                 project);
                        transJobContainer = new TranslationJobContainer(sourceLang, destLang, textKind, isTruckObjectStoreTranslations);
                        transJobContainer.setJobId(transJobSourcLangAndTextkindGroup.getJobId());
                        transJobSourcLangAndTextkindGroup.addTransJobContainerForDestLang(transJobContainer, destLang);
                    }

                    // Füge das DictMeta Objekt hinzu, damit später der Job Datensatz erzeugt werden kann
                    transJobContainer.addDictMeta(dataDictMeta);
                    // Wurde das Maximum an Einträgen pro XML Datei erreicht, dann muss für jeden TransJobContainer in
                    // der aktuellen Gruppe ein XML Archive erzeugt werden.
                    // Im Exporter wird zwar auch geprüft, ob das Maximum erreicht wurde und falls ja auf mehrere
                    // Dateien verteilt, aber um den Speicher zu schonen, wird das aber hier auch schon getriggert.
                    if (transJobContainer.getDictMetaCount() >= maxEntriesPerFile) {
                        exportGroup = true;
                    }
                }
            }
            // Hat die aktuelle Gruppe die maximale Anzahl an Einträgen erreicht, werden die XML Dateien sofort gebaut
            // und in einem Archiv abgelegt
            if (exportGroup) {
                SourceLangAndTextKindGroup transJobSourceLangAndTextkindGroup = jobContainerForLanguagesAndTextKind.get(groupKey);
                if (transJobSourceLangAndTextkindGroup != null) {
                    for (TranslationJobContainer translationJobContainer : transJobSourceLangAndTextkindGroup.getAllTransJobContainer()) {
                        int tempJobs = startExport(translationJobContainer, messageLog);
                        if (tempJobs == NOT_VALID_COUNT) {
                            break;
                        }
                        storedJobsObjects += tempJobs;
                    }
                    // Zähler für die übrigen TransJobs anpassen
                    remainingJobsCount.setValue(remainingJobsCount.getValue() - transJobSourceLangAndTextkindGroup.getAllTransJobContainer().size());
                    // Abgearbeitete TransJobs entfernen sonst werden sie am Ende nochmals erzeugt
                    jobContainerForLanguagesAndTextKind.remove(groupKey);
                }
            }
        }

        return storedJobsObjects;
    }

    /**
     * Liefert die Logmeldung zu den existierenden Texten
     *
     * @param existingLanguages
     * @param sourceLang
     * @return
     */
    private String getLogMessageAdditionalLanguages(List<Language> existingLanguages, Language sourceLang) {
        if (existingLanguages.size() > 1) {
            StringBuilder additionalLanguages = new StringBuilder();
            for (Language language : existingLanguages) {
                if (language != sourceLang) {
                    if (additionalLanguages.length() != 0) {
                        additionalLanguages.append(", ");
                    }
                    additionalLanguages.append(language.getCode());
                }
            }
            return " " + TranslationHandler.translate("!!Zusätzlich wurden Einträge für folgende Sprachen gefunden: %1", additionalLanguages.toString());
        }
        return "";
    }

    /**
     * Liefert die nächste JOB-ID
     *
     * @param jobIdPrefix
     * @return
     */
    private String getNextJobId(String jobIdPrefix) {
        if (iPartsPlugin.isImportPluginActive()) {
            return de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation.TranslationsExportHelper.getInstance().getNextJobId(jobIdPrefix);
        }
        return null;
    }

    /**
     * Liefert die maximale Anzahl an Einträgen pro XML aus der dazugehörigen Admin-Option
     *
     * @param isTruckObjectStoreTranslations
     * @return
     */
    private int getMaxEntriesForFile(boolean isTruckObjectStoreTranslations) {
        if (iPartsPlugin.isImportPluginActive()) {
            return de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation.TranslationsHelper.getMaxEntriesPerFile(isTruckObjectStoreTranslations);
        }
        return NOT_VALID_COUNT;
    }

    private boolean isTruckTranslationsActive() {
        if (iPartsPlugin.isImportPluginActive()) {
            return de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation.TranslationsObjectStoreHelper.isTruckTranslationsActive();
        }
        return false;
    }

    /**
     * Startet den Export für die Einträge eim übergebenen {@link TranslationJobContainer}, sofern das Plugin aktiv ist.
     *
     * @param transJobContainer
     * @param messageLog
     * @return
     */
    private int startExport(TranslationJobContainer transJobContainer, EtkMessageLog messageLog) {
        if (iPartsPlugin.isImportPluginActive()) {
            return de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation.TranslationsExportHelper.getInstance().handleTranslationPackage(transJobContainer, messageLog);
        }
        return NOT_VALID_COUNT;
    }


    /**
     * Sucht die Ausgangssprache für den Übersetzungsumfang. Zur Ausgangssprache muss ein echter Text gehören. Es werden
     * alle validen Sprachkürzel Texte gesucht. Wird ein Text zu einem Kürzel gefunden, wird dieses Sprachkürzel zurück-
     * geliefert.
     *
     * @param multiLang
     * @return
     */
    public static Language findSourceLang(EtkMultiSprache multiLang) {
        // Alternativ:
//        for (Language validLanguage : VALID_SOURCE_LANGUAGES) {
//            if (multiLang.containsLanguage(validLanguage, true)) {
//                return validLanguage;
//            }
//        }
//        return null;
        return Arrays.stream(VALID_SOURCE_LANGUAGES)
                .filter(validLanguage -> multiLang.containsLanguage(validLanguage, true))
                .findFirst()
                .orElse(null);
    }

    public static String getSourceLanguagesAsString() {
        return Arrays.stream(VALID_SOURCE_LANGUAGES)
                .map(Language::getCode)
                .collect(Collectors.joining(", "));
    }

    public static Language[] getSourceLanguages() {
        return VALID_SOURCE_LANGUAGES;
    }

    /**
     * Hilfsklasse für alle Übersetzungen pro Ausgangssprache und Textart
     */
    public static class SourceLangAndTextKindGroup {

        private final String groupKey;
        private final String jobId;
        private final Map<String, TranslationJobContainer> translationJobContainerMap;

        public SourceLangAndTextKindGroup(String groupkey, String jobId) {
            this.groupKey = groupkey;
            this.jobId = jobId;
            this.translationJobContainerMap = new HashMap<>();
        }

        public String getGroupKey() {
            return groupKey;
        }

        public String getJobId() {
            return jobId;
        }

        public TranslationJobContainer getTransJobForDestLang(Language destLang) {
            return translationJobContainerMap.get(destLang.getCode());
        }

        public void addTransJobContainerForDestLang(TranslationJobContainer transJobContainer, Language destLang) {
            translationJobContainerMap.put(destLang.getCode(), transJobContainer);
        }

        public Collection<TranslationJobContainer> getAllTransJobContainer() {
            return translationJobContainerMap.values();
        }
    }
}
