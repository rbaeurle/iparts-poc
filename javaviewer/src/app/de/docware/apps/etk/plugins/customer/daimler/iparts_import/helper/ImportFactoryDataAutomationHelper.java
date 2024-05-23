/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEntrySourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterPartsEntries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterSwitchboard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.factoryDataAutomation.ColorTableFactoryDataAutomationChecker;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.factoryDataAutomation.PartListEntryFactoryDataAutomationChecker;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.framework.modules.db.serialization.SerializedDbDataObjectAsJSON;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst.*;
import static de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER;

/**
 * Helper-Klasse für automatische Tätigkeiten im Zusammenhang mit Importern, wie z.B. der automatischen Freigabe von
 * Werkseinsatzdaten, die das Filterergebnis nicht beeinflussen
 */
public class ImportFactoryDataAutomationHelper {

    public static String TITLE = "!!Automatische Freigabe von Werkseinsatzdaten";

    private iPartsFactories factoriesInstance;
    private EtkMessageLog messageLog;
    private VarParam<Boolean> cancelled;
    private EtkProject project;
    private Map<AssemblyId, AssemblyCacheData> assemblyCache;
    private int maxProgress;
    private int currentProgress;
    private List<EtkDataObject> releaseList;
    private List<List<EtkDataObject>> dateThresholdData; // Liste mit Werksdaten zu einem Werk und BCTE  oder Farbparameter Schlüssel
    private iPartsDataConfirmChangesList confirmChangesList;
    private Map<IdWithType, iPartsDataDIALOGChangeList> factoryDataDialogChanges;
    private iPartsFilter modelFilter;

    private static final Set<String> releasedOrNewState = new HashSet<>(2);

    static {
        releasedOrNewState.add(iPartsDataReleaseState.RELEASED.getDbValue());
        releasedOrNewState.add(iPartsDataReleaseState.NEW.getDbValue());
    }

    public static Comparator<EtkDataObject> createAdatAndSeqNoComparator(final String adatFieldName, final String seqNoFieldName) {
        return (o1, o2) -> {
            String o1AdatString = Utils.toSortString(o1.getFieldValue(adatFieldName));
            String o2AdatString = Utils.toSortString(o2.getFieldValue(adatFieldName));
            int result = o2AdatString.compareTo(o1AdatString);
            if (StrUtils.isValid(seqNoFieldName) && (result == 0)) {
                String o1SeqNoString = Utils.toSortString(o1.getFieldValue(seqNoFieldName));
                String o2SeqNoString = Utils.toSortString(o2.getFieldValue(seqNoFieldName));
                result = o2SeqNoString.compareTo(o1SeqNoString);
            }
            return result;
        };
    }

    public ImportFactoryDataAutomationHelper(EtkMessageLog messageLog, VarParam<Boolean> cancelledVarParam, EtkProject project) {
        this.messageLog = messageLog;
        this.cancelled = cancelledVarParam;
        this.project = project;
    }

    private void reinit() {
        factoriesInstance = iPartsFactories.getInstance(project);

        assemblyCache = new HashMap<>();
        releaseList = new DwList<>();
        dateThresholdData = new DwList<>();
        confirmChangesList = new iPartsDataConfirmChangesList();
        factoryDataDialogChanges = new HashMap<>();
        modelFilter = new iPartsFilter();
        final iPartsFilterSwitchboard filterForModelEvaluationSwitchboard = new iPartsFilterSwitchboard();
        filterForModelEvaluationSwitchboard.setMainSwitchActive(true);
        filterForModelEvaluationSwitchboard.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.MODEL, true);
        modelFilter.setSwitchboardState(filterForModelEvaluationSwitchboard);
    }

    public static void autoReleaseFactoryDataWithMessageLog(final EtkProject project) {
        final VarParam<Boolean> cancelledVarParam = new VarParam<>(false);
        final EtkMessageLogForm messageLogForm = new EtkMessageLogForm(TITLE, "!!Fortschritt", null) {
            @Override
            protected void cancel(Event event) {
                cancelledVarParam.setValue(true);
                messageLog.fireMessage(TranslationHandler.translate("!!Automatische Freigabe wird abgebrochen"));
                getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CANCEL).setVisible(false);
                getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CLOSE).setVisible(true);
            }
        };

        messageLogForm.showModal(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                project.executeWithoutActiveChangeSets(new Runnable() {
                    @Override
                    public void run() {
                        autoReleaseFactoryData(messageLogForm.getMessageLog(), cancelledVarParam, project);
                    }
                }, false);
            }
        });
    }

    public static void autoReleaseFactoryDataWithLogFile(final EtkProject project, final EtkMessageLog messageLog) {
        project.executeWithoutActiveChangeSets(new Runnable() {
            @Override
            public void run() {
                final VarParam<Boolean> cancelledVarParam = new VarParam<>(false);
                autoReleaseFactoryData(messageLog, cancelledVarParam, project);
            }
        }, false);
    }

    private static void autoReleaseFactoryData(EtkMessageLog messageLog, VarParam<Boolean> cancelledVarParam, EtkProject project) {
        ImportFactoryDataAutomationHelper helper = new ImportFactoryDataAutomationHelper(messageLog, cancelledVarParam, project);

        Map<IdWithType, Boolean> autoReleaseWasSuccessful = new HashMap<>();
        Map<IdWithType, List<EtkDataObject>> loadedFactoryData = helper.loadRelevantFactoryData();
        FactoryDataForAutoRelease factoryDataForLaterChecks = helper.handleSingleFactoryDataRelease(loadedFactoryData, autoReleaseWasSuccessful);

        while (factoryDataForLaterChecks.hasDataForFollowingChecks()) {
            String text = factoryDataForLaterChecks.getDataForFollowingChecks().entrySet()
                    .stream()
                    .map(entry -> {
                        String factories = String.join(", ", entry.getValue()
                                .stream()
                                .map(factoryData -> factoryData.getFieldValue(FIELD_DFD_FACTORY))
                                .collect(Collectors.toSet()));
                        return "(" + entry.getKey().toStringForLogMessages() + ") -> " + TranslationHandler.translate("!!Weitere Werke") + ": " + factories;
                    })
                    .collect(Collectors.joining("; "));
            helper.logMessageToLogChannel("Starting additional release cycle for factory data with different factories for the same ID: " + text);
            helper.logMessageToGui(TranslationHandler.translate("!!Starte zusätzliche Werksdatenfreigabe für unterschiedliche Werke zum gleichen Schlüssel: %1", text));
            factoryDataForLaterChecks = helper.handleSingleFactoryDataRelease(factoryDataForLaterChecks.getDataForFollowingChecks(), autoReleaseWasSuccessful);
        }

        helper.autoReleaseColorTableContentFactoryData();
        helper.addThresholdColorTableFactoryDataToRelease();
        helper.releaseColorTableFactoryData();
        helper.autoReleaseColorTableToPartFactoryData();
        helper.addThresholdColorTableFactoryDataToRelease();
        helper.releaseColorTableFactoryData();
    }

    private FactoryDataForAutoRelease handleSingleFactoryDataRelease(Map<IdWithType, List<EtkDataObject>> loadedFactoryDataGroupedById,
                                                                     Map<IdWithType, Boolean> autoReleaseWasSuccessful) {
        FactoryDataForAutoRelease factoryDataForLaterChecks = splitFactoryDataByFactory(loadedFactoryDataGroupedById);
        autoReleaseFactoryData(factoryDataForLaterChecks, autoReleaseWasSuccessful);
        addThresholdFactoryDataToRelease(FIELD_DFD_ADAT, FIELD_DFD_SEQ_NO);
        releaseFactoryData();
        return factoryDataForLaterChecks;

    }

    /**
     * Durchläuft alle neuen Datensätze die durch die vorherigen Prüfungen nicht auf "released" gesetzt werden konnten und
     * prüft, ob man sie aufgrund von Daten, die älter sind freigeben kann.
     *
     * @param fieldAdat
     * @param fieldSeqNo
     */
    private void addThresholdFactoryDataToRelease(String fieldAdat, String fieldSeqNo) {
        if (isCancelled() || !isThresholdCheckActive() || dateThresholdData.isEmpty()) {
            return;
        }
        for (List<EtkDataObject> dataPerFactory : dateThresholdData) {
            if (isCancelled()) {
                return;
            }
            if (dataPerFactory.isEmpty()) {
                continue;
            }
            // Aktuell soll keine Schleife implementiert werden, d.h. Wenn ich z.B. 5 "neue" Datensätze habe, dann soll
            // nur der älteste in diesem Durchlauf berücksichtigt werden
            if (dataPerFactory.size() > 1) {
                dataPerFactory.sort(createAdatAndSeqNoComparator(fieldAdat, fieldSeqNo));
            }

            FactoryThresholdData factoryThresholdData = new FactoryThresholdData(dataPerFactory.get(dataPerFactory.size() - 1));
            checkSingleFactoryData(factoryThresholdData);
        }
        // Wenn alle potentiellen Kandidaten durch sind, Liste leeren, weil die nachfolgenden Prüfungen
        // die Liste ebenfalls nutzen
        dateThresholdData.clear();
    }

    private void addThresholdColorTableFactoryDataToRelease() {
        addThresholdFactoryDataToRelease(FIELD_DCCF_ADAT, null);
    }

    /**
     * Überprüft, ob der neue Werksdatensatz im übergebene {@link FactoryThresholdData} Objekt bezüglich seiner Termin-
     * verschiebung zum freigegebenen Datensatz passt. PEM ab und PEM bis müssen gleich sein. Als Referenz wird der
     * Datensatz verwendet der am "nächsten" zum neuen ist (Vergangenheit).
     *
     * @param factoryThresholdData
     */
    private void checkSingleFactoryData(FactoryThresholdData factoryThresholdData) {
        // Wir haben keinen freigegebenen Referenzdatensatz -> Kann nicht freigegeben werden
        if (!factoryThresholdData.hasReleasedData()) {
            return;
        }
        // Wenn die PEM ab/bis Werte nicht übereinstimmen, dann kann der neue Datensatz nicht freigegeben werden
        if (!hasSamePEMs(factoryThresholdData)) {
            return;
        }
        // Prüfung, ob ein Termin unendlich ist und das Gegenstück nicht
        if (checkInvalidInfiniteDates(factoryThresholdData)) {
            return;
        }
        addFactoryDataIfWithinTimeSlice(factoryThresholdData);
    }

    /**
     * Fügt den neuen Werksdatensatz hinzu, wenn die Datumsangaben die Terminverschiebungsprüfung übersethen
     *
     * @param factoryThresholdData
     */
    private void addFactoryDataIfWithinTimeSlice(FactoryThresholdData factoryThresholdData) {
        EtkDataObject releasedFactoryData = factoryThresholdData.getCurrentReleasedData();
        EtkDataObject newFactoryData = factoryThresholdData.getNewFactoryData();
        // PEM Termin ab Prüfung
        if (!isWithinFromDateTimeSlice(releasedFactoryData, newFactoryData, factoryThresholdData)) {
            return;
        }
        // PEM Termin bis Prüfung
        if (!isWithinToDateTimeSlice(releasedFactoryData, newFactoryData, factoryThresholdData)) {
            return;
        }
        logMessageToLogChannel("PEM dates of new factory data is within the time slice of the released data object. " +
                               "Add new factory Data (" + newFactoryData.getAsId().toStringForLogMessages() + ") to release list; " +
                               "Released data object: " + releasedFactoryData.getAsId().toStringForLogMessages() + "; Offset in days: " + getDateThreshold());
        releaseList.add(newFactoryData);
    }

    /**
     * Prüfung, ob die Ab Termine übereinstimmen (inkl. Terminverschiebung)
     *
     * @param releasedFactoryData
     * @param newFactoryData
     * @param factoryThresholdData
     * @return
     */
    private boolean isWithinFromDateTimeSlice(EtkDataObject releasedFactoryData, EtkDataObject newFactoryData, FactoryThresholdData factoryThresholdData) {
        String pemFromDateReleased = releasedFactoryData.getFieldValue(factoryThresholdData.getPemtaField());
        String pemFromDateNew = newFactoryData.getFieldValue(factoryThresholdData.getPemtaField());
        // Beide Angaben sind unendlich und somit gleich
        if (StrUtils.isEmpty(pemFromDateReleased, pemFromDateNew)) {
            return true;
        }
        // Hier der eigentliche Check mit dem Intervall, dass via Admin-Option vorgebenen wurde
        // Das Vergleichsdatum vom freigegebenen Datensatz
        Calendar pemDateReleased = releasedFactoryData.getFieldValueAsDateTime(factoryThresholdData.getPemtaField());
        // Das Datum vom neuen Datensatz
        Calendar pemDateNew = newFactoryData.getFieldValueAsDate(factoryThresholdData.getPemtaField());
        return isWithinTimeSlice(pemDateReleased, pemDateNew);
    }

    /**
     * Prüfung, ob die Bis Termine übereinstimmen (inkl. Terminverschiebung)
     *
     * @param releasedFactoryData
     * @param newFactoryData
     * @param factoryThresholdData
     * @return
     */
    private boolean isWithinToDateTimeSlice(EtkDataObject releasedFactoryData, EtkDataObject newFactoryData, FactoryThresholdData factoryThresholdData) {
        String pemToDateReleased = releasedFactoryData.getFieldValue(factoryThresholdData.getPemtbField());
        String pemToDateNew = newFactoryData.getFieldValue(factoryThresholdData.getPemtbField());
        // Beide Angaben sind unendlich und somit gleich
        if (StrUtils.isEmpty(pemToDateReleased, pemToDateNew)) {
            return true;
        }
        // Hier der eigentliche Check mit dem Intervall, dass via Admin-Option vorgebenen wurde
        // Das Vergleichsdatum vom freigegebenen Datensatz
        Calendar pemDateReleased = releasedFactoryData.getFieldValueAsDateTime(factoryThresholdData.getPemtbField());
        // Das Datum vom neuen Datensatz
        Calendar pemDateNew = newFactoryData.getFieldValueAsDate(factoryThresholdData.getPemtbField());
        return isWithinTimeSlice(pemDateReleased, pemDateNew);
    }

    /**
     * Prüfung, ob die übergebenen Termine übereinstimmen (inkl. Terminverschiebung)
     *
     * @param pemDateReleased
     * @param pemDateNew
     * @return
     */
    private boolean isWithinTimeSlice(Calendar pemDateReleased, Calendar pemDateNew) {
        if (pemDateReleased == null) {
            logMessageToLogChannel("PEM date of released data must not be null for time slice check!");
            return false;
        }
        if (pemDateNew == null) {
            logMessageToLogChannel("PEM date of new data must not be null for time slice check!");
            return false;
        }
        LocalDateTime localDateTimeRelease = LocalDateTime.ofInstant(pemDateReleased.toInstant(), ZoneId.systemDefault());
        // Die Verschiebung in Tagen vor dem Zeitpunkt
        LocalDate releaseStart = localDateTimeRelease.toLocalDate().minusDays(getDateThreshold());
        // Die Verschiebung in Tagen nach dem Zeitpunkt
        LocalDate releaseEnd = localDateTimeRelease.toLocalDate().plusDays(getDateThreshold());

        LocalDateTime localDateTimeNew = LocalDateTime.ofInstant(pemDateNew.toInstant(), ZoneId.systemDefault());
        LocalDate newDate = localDateTimeNew.toLocalDate();
        // Wenn der Anfang des Intervall vor oder das Ende des Intervalls nach dem Datum des neuen Datensatzes ist,
        // dann liegt der Datensatz genau im vorgegebenen Intervall -> freigeben
        if (releaseStart.isEqual(newDate) || releaseEnd.isEqual(newDate) || (releaseStart.isBefore(newDate) && releaseEnd.isAfter(newDate))) {
            return true;
        }
        return false;
    }

    /**
     * Check, ob die PEM ab/bis werte übereinstimmen
     *
     * @param factoryThresholdData
     * @return
     */
    private boolean hasSamePEMs(FactoryThresholdData factoryThresholdData) {
        EtkDataObject releasedFactoryData = factoryThresholdData.getCurrentReleasedData();
        EtkDataObject newFactoryData = factoryThresholdData.getNewFactoryData();
        String pemFromReleased = releasedFactoryData.getFieldValue(factoryThresholdData.getPemaField());
        String pemToReleased = releasedFactoryData.getFieldValue(factoryThresholdData.getPembField());
        String pemFromNew = newFactoryData.getFieldValue(factoryThresholdData.getPemaField());
        String pemToNew = newFactoryData.getFieldValue(factoryThresholdData.getPembField());
        // PEM ab und PEM bis müssen gleich sein
        if (!pemFromNew.equals(pemFromReleased) || !pemToNew.equals(pemToReleased)) {
            return false;
        }
        return true;
    }

    /**
     * Check, ob ein Termin ab oder bis unendlich ist und das Gegenstück nicht
     *
     * @param factoryThresholdData
     * @return
     */
    private boolean checkInvalidInfiniteDates(FactoryThresholdData factoryThresholdData) {
        EtkDataObject releasedFactoryData = factoryThresholdData.getCurrentReleasedData();
        EtkDataObject newFactoryData = factoryThresholdData.getNewFactoryData();
        String pemFromDateReleased = releasedFactoryData.getFieldValue(factoryThresholdData.getPemtaField());
        String pemFromDateNew = newFactoryData.getFieldValue(factoryThresholdData.getPemtaField());
        // Ist ein Ab Datum für den Vergleich unendlich, dann muss der Autor sich das anschauen -> nicht freigeben
        if (pemFromDateReleased.isEmpty() && !pemFromDateNew.isEmpty()) {
            logMessageToLogChannel("PEM from date of released factory data (" + releasedFactoryData.getAsId().toStringForLogMessages() + ") " +
                                   "is empty while PEM from date of new factory Data (" + newFactoryData.getAsId().toStringForLogMessages() + ") is not.");
            return true;
        }
        if (!pemFromDateReleased.isEmpty() && pemFromDateNew.isEmpty()) {
            logMessageToLogChannel("PEM from date of new factory Data (" + newFactoryData.getAsId().toStringForLogMessages() + ") " +
                                   "is empty while PEM from date of released factory Data (" + releasedFactoryData.getAsId().toStringForLogMessages() + ") is not.");
            return true;
        }
        String pemToDateReleased = releasedFactoryData.getFieldValue(factoryThresholdData.getPemtbField());
        String pemToDateNew = newFactoryData.getFieldValue(factoryThresholdData.getPemtbField());
        // Ist ein Ab Datum für den Vergleich unendlich, dann muss der Autor sich das anschauen -> nicht freigeben
        if (pemToDateReleased.isEmpty() && !pemToDateNew.isEmpty()) {
            logMessageToLogChannel("PEM to date of released factory data (" + releasedFactoryData.getAsId().toStringForLogMessages() + ") " +
                                   "is empty while PEM to date of new factory Data (" + newFactoryData.getAsId().toStringForLogMessages() + ") is not.");
            return true;
        }
        if (!pemToDateReleased.isEmpty() && pemToDateNew.isEmpty()) {
            logMessageToLogChannel("PEM to date of new factory Data (" + newFactoryData.getAsId().toStringForLogMessages() + ") " +
                                   "is empty while PEM to date of released factory Data (" + releasedFactoryData.getAsId().toStringForLogMessages() + ") is not.");
            return true;
        }
        return false;
    }

    /**
     * Berechnung der Werkszeitscheibe für den Stücklisteneintrag und das Baumuster inkl. Korrektur auf die Baumusterzeitscheibe
     *
     * @param partListEntry
     * @param productId
     * @param modelNumber
     * @param project
     * @return
     */
    private static TimeSlice calculatePartlistentryTimeSlice(iPartsDataPartListEntry partListEntry, iPartsProductId productId,
                                                             String modelNumber, EtkProject project) {
        // Bestimmung analog zu iPartsFilter.FilterCachedModelData
        iPartsDataProductModels dataProductModels = null;
        if ((productId != null) && (modelNumber != null)) {
            //jetzt überprüfen, ob es eine Product-Models Beziehung gibt
            dataProductModels = iPartsProductModels.getInstance(project).getProductModelsByModelAndProduct(project, modelNumber,
                                                                                                           productId.getProductNumber());
        }

        iPartsModelId modelId = new iPartsModelId(modelNumber);
        long productModelsValidFrom = StrUtils.strToLongDef(iPartsProductModelHelper.getValidFromValue(project, dataProductModels,
                                                                                                       modelId), 0);
        long productModelsValidTo = StrUtils.strToLongDef(iPartsProductModelHelper.getValidToValue(project, dataProductModels,
                                                                                                   modelId), 0);
        if (productModelsValidTo == 0) { // Beim Enddatum muss mit Long.MAX_VALUE gerechnet werden für unendlich anstatt mit 0
            productModelsValidTo = Long.MAX_VALUE;
        }

        partListEntry.setTimeSliceDates(true, productModelsValidFrom, productModelsValidTo);
        long timeSliceDateFrom = partListEntry.getTimeSliceDateFrom(true);
        long timeSliceDateTo = partListEntry.getTimeSliceDateTo(true);
        return new TimeSlice(timeSliceDateFrom, timeSliceDateTo);
    }

    /**
     * Prüfe die Flags "PEM ab/bis auswerten", ob eine Automatisierung (evtl.) möglich ist.
     *
     * @param retailPartlistEntries
     * @param collectedDateFromForFactory Map von Werksnummer auf "PEM Datum ab", die in dieser Methode befüllt wird
     * @param collectedDateToForFactory   Map von Werksnummer auf "PEM Datum bis", die in dieser Methode befüllt wird
     * @return
     */
    private static PreconditionResult checkPreconditionPEMFlags(List<EtkDataPartListEntry> retailPartlistEntries,
                                                                Map<String, Long> collectedDateFromForFactory,
                                                                Map<String, Long> collectedDateToForFactory) {
        boolean evalPEMFromOrPEMTo = false;
        for (EtkDataPartListEntry partlistEntry : retailPartlistEntries) {
            boolean evalPEMFrom = partlistEntry.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_FROM_CALCULATED);
            boolean evalPEMTo = partlistEntry.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED);
            if (evalPEMFrom && evalPEMTo) {
                return PreconditionResult.NO_AUTOMATION_POSSIBLE;
            }
            if (evalPEMFrom || evalPEMTo) {
                evalPEMFromOrPEMTo = true;
            }
        }

        if (evalPEMFromOrPEMTo) {
            for (EtkDataPartListEntry partlistEntry : retailPartlistEntries) {
                if (partlistEntry instanceof iPartsDataPartListEntry) {
                    iPartsDataPartListEntry iPartsPLE = (iPartsDataPartListEntry)(partlistEntry);
                    boolean evalPEMFrom = iPartsPLE.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_FROM_CALCULATED);
                    boolean evalPEMTo = iPartsPLE.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED);
                    if (evalPEMFrom || evalPEMTo) {
                        iPartsFactoryData factoryDataForRetailUnfiltered = iPartsPLE.getFactoryDataForRetailUnfiltered();
                        if ((factoryDataForRetailUnfiltered == null) || !factoryDataForRetailUnfiltered.hasValidFactories()) {
                            continue;
                        }

                        for (Map.Entry<String, List<iPartsFactoryData.DataForFactory>> factoryData : factoryDataForRetailUnfiltered.getFactoryDataMap().entrySet()) {
                            // Minimalen PEM-Termin ab bzw. maximalen PEM-Termin bis für das Werk bestimmen
                            long minDateFrom = Long.MAX_VALUE;
                            long maxDateTo = Long.MIN_VALUE;
                            String factory = factoryData.getKey();
                            List<iPartsFactoryData.DataForFactory> dataForFactories = factoryData.getValue();
                            for (iPartsFactoryData.DataForFactory dataForFactory : dataForFactories) {
                                minDateFrom = Math.min(minDateFrom, dataForFactory.dateFrom);
                                maxDateTo = Math.max(maxDateTo, dataForFactory.getDateToWithInfinity());
                            }

                            if (evalPEMFrom) {
                                Long previousDateFrom = collectedDateFromForFactory.put(factory, minDateFrom);
                                if ((previousDateFrom != null) && (previousDateFrom.longValue() != minDateFrom)) {
                                    // die einzelnen Verwendungen haben unterschiedliche PEM ab Termine für das gleiche Werk
                                    // (kann eigentlich nicht sein, weil die Zuordnung von Werkseinsatzdaten zum Stücklisteneintrag
                                    // ja über den BCTE-Schlüssel geht)
                                    return PreconditionResult.NO_AUTOMATION_POSSIBLE;
                                }
                            }

                            if (evalPEMTo) {
                                Long previousDateTo = collectedDateToForFactory.put(factory, maxDateTo);
                                if ((previousDateTo != null) && (previousDateTo.longValue() != maxDateTo)) {
                                    // die einzelnen Verwendungen haben unterschiedliche PEM bis Termine für das gleiche Werk
                                    // (kann eigentlich nicht sein, weil die Zuordnung von Werkseinsatzdaten zum Stücklisteneintrag
                                    // ja über den BCTE-Schlüssel geht)
                                    return PreconditionResult.NO_AUTOMATION_POSSIBLE;
                                }
                            }
                        }
                    }
                }
            }

            // im nächsten Schritt prüfen ob sich die entsprechenden PEM Termine ändern
            return PreconditionResult.CHECK_PEM_DATES;
        } else {
            return PreconditionResult.AUTOMATION_POSSIBLE;
        }
    }

    /**
     * Prüfe, ob sich durch die neuen Werkseinsatzdaten die PEM ab/bis Termine ändern würden.
     *
     * @param newFactoryData
     * @param collectedDateFromForFactory Map von Werksnummer auf "PEM Datum ab" für die Stücklisteneinträge des zum Werkseinsatzdatensatz
     *                                    passenden BCTE-Schlüssels
     * @param collectedDateToForFactory   Map von Werksnummer auf "PEM Datum bis" für die Stücklisteneinträge des zum Werkseinsatzdatensatz
     *                                    passenden BCTE-Schlüssels
     * @return
     */
    private static PreconditionResult checkPreconditionPEMDatesChanged(iPartsDataFactoryData newFactoryData,
                                                                       Map<String, Long> collectedDateFromForFactory,
                                                                       Map<String, Long> collectedDateToForFactory) {
        // jetzt die gesammelten PEM ab/bis Termine mit den aufgesammelten Änderungen vergleichen
        String factory = newFactoryData.getAsId().getFactory();

        if (!collectedDateFromForFactory.isEmpty()) { // Leer, falls bei keinem Stücklisteneintrag das Flag "PEM ab auswerten" gesetzt ist
            String pemFrom = newFactoryData.getFieldValue(FIELD_DFD_PEMA);
            if (!pemFrom.isEmpty()) {
                long dateFrom = iPartsFactoryData.getFactoryDateFromDateString(newFactoryData.getFieldValue(FIELD_DFD_PEMTA),
                                                                               TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMTA));

                Long collectedDateFrom = collectedDateFromForFactory.get(factory);
                if ((collectedDateFrom != null) && (collectedDateFrom != dateFrom)) {
                    return PreconditionResult.NO_AUTOMATION_POSSIBLE;
                }
            }
        }

        if (!collectedDateToForFactory.isEmpty()) { // Leer, falls bei keinem Stücklisteneintrag das Flag "PEM bis auswerten" gesetzt ist
            String pemTo = newFactoryData.getFieldValue(FIELD_DFD_PEMB);
            if (!pemTo.isEmpty()) {
                long dateTo = iPartsFactoryData.getFactoryDateFromDateString(newFactoryData.getFieldValue(FIELD_DFD_PEMTB),
                                                                             TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMTB));
                if (dateTo == 0) { // Spezialbehandlung für unendlich
                    dateTo = Long.MAX_VALUE;
                }

                Long collectedDateTo = collectedDateToForFactory.get(factory);
                if ((collectedDateTo != null) && (collectedDateTo != dateTo)) {
                    return PreconditionResult.NO_AUTOMATION_POSSIBLE;
                }
            }
        }

        return PreconditionResult.AUTOMATION_POSSIBLE;
    }

    public void logMessageToGui(String key, String... placeHolderTexts) {
        String message = TranslationHandler.translate(key, placeHolderTexts);
        if (messageLog != null) {
            messageLog.fireMessage(message);
        }
    }

    public void logMessageToLogChannel(String message) {
        Logger.log(iPartsImportPlugin.LOG_CHANNEL_FACTORYDATA_AUTO_RELEASE, LogType.DEBUG, message);
    }

    public void logMessageToLogChannel(iPartsDataFactoryData factoryData, String message) {
        logMessageToLogChannel("[" + factoryData.getAsId().toStringForLogMessages() + "] " + message);
    }

    private void logMessageToLogChannel(iPartsDataFactoryData factoryData, iPartsChangeSetId changeSetId, PartListEntryId partListEntryId,
                                        String message) {
        logMessageToLogChannel("[" + factoryData.getAsId().toStringForLogMessages() + " in " + changeSetId.getGUID() + " for "
                               + partListEntryId.toStringForLogMessages() + "] " + message);
    }

    private void logMessageToLogChannel(iPartsDataColorTableFactory factoryData, String message) {
        logMessageToLogChannel("[" + factoryData.getAsId().toStringForLogMessages() + "] " + message);
    }

    private void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    private void increaseMaxProgress(int maxProgressIncrement) {
        this.maxProgress += maxProgressIncrement;
    }

    private void increaseProgress(int offset) {
        fireProgress(currentProgress + offset);
    }

    private void fireProgress(int progress) {
        if (messageLog != null) {
            currentProgress = progress;
            messageLog.fireProgress(currentProgress, maxProgress, "", false, true);
        }
    }

    private boolean isCancelled() {
        return this.cancelled.getValue();
    }

    /**
     * Automatische Freigabe von Teil zu Farbtabelle Werksdaten
     */
    private void autoReleaseColorTableToPartFactoryData() {
        reinit();

        logMessageToGui("!!Suche relevante Werkseinsatzdaten zu Farbtabelle zu Teil...");

        iPartsDataColorTableFactoryList colorTableToPartFactoryDataList = iPartsDataColorTableFactoryList.loadNewColorTableToPartFactoryData(project);

        int totalColorTableFactoryData = colorTableToPartFactoryDataList.size();
        logMessageToGui("!!Anzahl Werkseinsatzdaten zu Farbtabelle zu Teil mit Status \"neu\": %1", String.valueOf(totalColorTableFactoryData));
        logMessageToLogChannel("Starting colortable to part factory data auto release check for " + totalColorTableFactoryData + " datasets");
        increaseProgress(totalColorTableFactoryData);

        logMessageToGui("!!Überprüfe Baumustergültigkeiten über alle Baumuster aller Produkte pro gefundenen Datensatz");
        int currentProgress = this.currentProgress;
        int maxProgress = this.maxProgress;
        this.currentProgress = 0;
        setMaxProgress(totalColorTableFactoryData);
        ColorTableFactoryDataAutomationChecker checker = new ColorTableFactoryDataAutomationChecker(this);

        // Rückmeldedaten-Cache; wird verwendet, um relevante Rückmeldedaten den Werkseinsatzdaten zuzuordnen
        final iPartsResponseData responseData = iPartsResponseData.getInstance(project);
        // Ausreißer-Cache; wird verwendet, um relevante Ausreißer den Rückmeldedaten zuzuordnen
        final iPartsResponseSpikes responseSpikes = iPartsResponseSpikes.getInstance(project);

        for (iPartsDataColorTableFactory newColorTableToPartFactoryData : colorTableToPartFactoryDataList) {
            if (isCancelled()) {
                return;
            }
            Set<iPartsProductId> relevantProducts = new HashSet<>();
            PreconditionResult preconditionResult = handlePreColorTableFactoryCheckData(newColorTableToPartFactoryData, relevantProducts);
            if (preconditionResult == null) {
                continue;
            }

            // Durchlaufe alle relevanten Produkte und prüfe, ob sich das Filterergebnis mit dem neuen Datensatz für die
            // Baumuster des Produkts ändert
            for (iPartsProductId productId : relevantProducts) {
                boolean isSameForProduct = checkModelFilterResultForColorTableToPart(newColorTableToPartFactoryData, productId,
                                                                                     responseData, responseSpikes);
                if (!isSameForProduct) {
                    // Verändert der neue Datensatz in einem Produkt das Filterergebnis, dann darf er nicht freigegeben
                    // werden
                    preconditionResult = PreconditionResult.NO_AUTOMATION_POSSIBLE;
                    break;
                }
            }

            if (preconditionResult == PreconditionResult.NO_AUTOMATION_POSSIBLE) {
                // mit dem nächsten neuen Werkseinsatzdatensatz zu Farben weitermachen.
                logMessageToLogChannel(newColorTableToPartFactoryData, "Different model filter validity -> do threshold check");
                checker.addToDateThresholdCheck(newColorTableToPartFactoryData);
                continue;
            }

            // Der neue Datensatz hat bezüglich aller Baumuster aller Produkte das Filterergebnis nicht verändert
            // -> kann freigegeben werden
            releaseList.add(newColorTableToPartFactoryData);
            checker.addAlreadyReleasedDataKey(newColorTableToPartFactoryData);
        }
        checker.addDataForDateThresholdCheck(dateThresholdData);
        this.currentProgress = currentProgress;
        setMaxProgress(maxProgress);
        checker.logMessagesToGUI();
        checker.logMessagesToLogChannel();
    }

    private void autoReleaseColorTableContentFactoryData() {
        reinit();

        logMessageToGui("!!Suche relevante Werkseinsatzdaten zu Farbtabelleninhalt...");

        iPartsDataColorTableFactoryList colorTableContentFactoryDataList = iPartsDataColorTableFactoryList.loadNewColorTableContentFactoryData(project);

        int totalColorTableFactoryData = colorTableContentFactoryDataList.size();
        logMessageToGui("!!Anzahl Werkseinsatzdaten zu Farbtabelleninhalt mit Status \"neu\": %1", String.valueOf(totalColorTableFactoryData));
        logMessageToLogChannel("Starting colortable content factory data auto release check for " + totalColorTableFactoryData + " datasets");
        increaseProgress(totalColorTableFactoryData);

        ColorTableFactoryDataAutomationChecker checker = new ColorTableFactoryDataAutomationChecker(this);

        // Rückmeldedaten-Cache; wird verwendet, um relevante Rückmeldedaten den Werkseinsatzdaten zuzuordnen
        final iPartsResponseData responseData = iPartsResponseData.getInstance(project);
        // Ausreißer-Cache; wird verwendet, um relevante Ausreißer den Rückmeldedaten zuzuordnen
        final iPartsResponseSpikes responseSpikes = iPartsResponseSpikes.getInstance(project);

        for (iPartsDataColorTableFactory newColorTableContentFactoryData : colorTableContentFactoryDataList) {
            if (isCancelled()) {
                return;
            }
            Set<iPartsProductId> relevantProducts = new HashSet<>();
            PreconditionResult preconditionResult = handlePreColorTableFactoryCheckData(newColorTableContentFactoryData, relevantProducts);
            if (preconditionResult == null) {
                continue;
            }
            Map<iPartsProductId, iPartsColorTable> productToColorTableMap = new HashMap<>();
            for (iPartsProductId productId : relevantProducts) {
                boolean isSameForProduct = checkColorTableContentValidForProduct(newColorTableContentFactoryData, productId,
                                                                                 productToColorTableMap, responseData, responseSpikes);
                if (!isSameForProduct) {
                    preconditionResult = PreconditionResult.NO_AUTOMATION_POSSIBLE;
                    break;
                }
            }

            if (preconditionResult == PreconditionResult.NO_AUTOMATION_POSSIBLE) {
                // mit dem nächsten neuen Werkseinsatzdatensatz zu Farben weitermachen.
                logMessageToLogChannel(newColorTableContentFactoryData, "Different product validity -> do threshold check");
                checker.addToDateThresholdCheck(newColorTableContentFactoryData);
                continue;
            }

            // In productToColorTableMap sind nur die Produkte, für die die Farbe nach der Filterung potentiell gültig werden kann.
            for (Map.Entry<iPartsProductId, iPartsColorTable> productWithColorTable : productToColorTableMap.entrySet()) {
                iPartsProductId productId = productWithColorTable.getKey();
                iPartsColorTable colorTable = productWithColorTable.getValue();
                boolean isSameForAllProductModels = checkModelFilterResultForColor(newColorTableContentFactoryData, productId,
                                                                                   colorTable, responseData, responseSpikes);
                if (!isSameForAllProductModels) {
                    preconditionResult = PreconditionResult.NO_AUTOMATION_POSSIBLE;
                    break;
                }
            }

            if (preconditionResult == PreconditionResult.NO_AUTOMATION_POSSIBLE) {
                // mit dem nächsten neuen Werkseinsatzdatensatz zu Farben weitermachen.
                logMessageToLogChannel(newColorTableContentFactoryData, "Different model filter validity -> do threshold check");
                checker.addToDateThresholdCheck(newColorTableContentFactoryData);
                continue;
            }
            checker.addAlreadyReleasedDataKey(newColorTableContentFactoryData);
            releaseList.add(newColorTableContentFactoryData);
        }
        checker.addDataForDateThresholdCheck(dateThresholdData);
        checker.logMessagesToGUI();
        checker.logMessagesToLogChannel();
    }

    /**
     * Vorprüfung, ob der neue Datensatz überhaupt freigegeben werden darf und bestimmt alle relevanten Produkte.
     *
     * @param newColorTableContentFactoryData
     * @param relevantProducts
     * @return
     */
    private PreconditionResult handlePreColorTableFactoryCheckData(iPartsDataColorTableFactory newColorTableContentFactoryData, final Set<iPartsProductId> relevantProducts) {
        increaseProgress(1);
        if (isCancelled()) {
            return null;
        }

        // Primus Farbtabellen können nicht automatisiert werden
        if (newColorTableContentFactoryData.getSource() == iPartsImportDataOrigin.PRIMUS) {
            return null;
        }

        String dataObjectId = newColorTableContentFactoryData.getAsId().toDBString();
        iPartsDataDIALOGChangeList dialogChanges = iPartsDataDIALOGChangeList.loadDIALOGChangesForDataObject(dataObjectId, project);

        PreconditionResult preconditionResult = PreconditionResult.AUTOMATION_POSSIBLE;
        for (iPartsDataDIALOGChange dialogChange : dialogChanges) {
            if (!dialogChange.getFieldValue(FIELD_DDC_CHANGE_SET_GUID).isEmpty()) {
                // Der Datensatz wird schon von einem Autor bearbeitet --> Keine automatische Freigabe
                preconditionResult = PreconditionResult.NO_AUTOMATION_POSSIBLE;
            }
        }

        if (preconditionResult == PreconditionResult.NO_AUTOMATION_POSSIBLE) {
            logMessageToLogChannel(newColorTableContentFactoryData, "No DA_DIALOG_CHANGES entry -> no automation possible");
            return null;
        }

        factoryDataDialogChanges.put(newColorTableContentFactoryData.getAsId(), dialogChanges);

        // Zur aktuellen Farbe über die Farbtabelle zu Teil alle relevanten Produkte ermitteln
        // Dazu wird per Join über die Materialnummer in Katalogtabelle die entsprechende Modulnummer ermittelt, und dann
        // die gültigen Produkte geladen. Zusätzlich werden über den BCTE-Schlüssel und QFT-Nummer nur Datensätze mit
        // gleicher Baureihe berücksichtigt.
        final String colorTableSeriesNo = ColorTableHelper.extractSeriesNumberFromTableId(newColorTableContentFactoryData.getAsId().getTableId());
        EtkDataObjectList.FoundAttributesCallback callback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(attributes.getFieldValue(FIELD_K_SOURCE_GUID));
                if ((bctePrimaryKey == null) || !bctePrimaryKey.seriesNo.equals(colorTableSeriesNo)) {
                    // der Stücklisteneintrag kommt aus einer anderen Baureihe, als die der Farbtabelle.
                    // Das Produkt, dem er zugeordnet ist, muss nicht betrachtet werden
                    return false;
                }
                String productNo = attributes.getFieldValue(FIELD_DPM_PRODUCT_NO);
                Set<String> modelsOfProduct = iPartsProduct.getInstance(project, new iPartsProductId(productNo)).getModelNumbers(project);
                if (modelsOfProduct.isEmpty()) {
                    // das Produkt hat überhaupt keine zugehörigen Baumuster, also muss es nicht betrachtet werden.
                    return false;
                }
                relevantProducts.add(new iPartsProductId(productNo));
                return false;
            }
        };
        String[] whereFields = new String[]{ FIELD_DCTP_TABLE_ID };
        String[] whereValues = new String[]{ newColorTableContentFactoryData.getAsId().getTableId() };
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PRODUCT_NO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_KATALOG, FIELD_K_SOURCE_GUID, false, false));
        iPartsDataColorTableToPartList colorTableToPartList = new iPartsDataColorTableToPartList();
        colorTableToPartList.searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields,
                                                       whereFields, whereValues, false, null,
                                                       false, true, callback,
                                                       new EtkDataObjectList.JoinData(TABLE_KATALOG,
                                                                                      new String[]{ TableAndFieldName.make(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_PART) },
                                                                                      new String[]{ FIELD_K_MATNR },
                                                                                      false, false),
                                                       new EtkDataObjectList.JoinData(TABLE_DA_PRODUCT_MODULES,
                                                                                      new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI) },
                                                                                      new String[]{ FIELD_DPM_MODULE_NO },
                                                                                      false, false));

        logMessageToLogChannel(newColorTableContentFactoryData, "Found " + relevantProducts.size() + " relevant Products for this entry");
        return preconditionResult;
    }

    private Map<IdWithType, List<EtkDataObject>> loadRelevantFactoryData() {
        reinit();

        logMessageToGui("!!Suche relevante Werkseinsatzdaten...");

        // Lade alle Werkseinsatzdaten mit Status NEU zu denen es einen passenden Eintrag in der DA_DIALOG_CHANGES gibt
        // bei dem kein Changeset eingetragen ist
        iPartsDataFactoryDataList relevantFactoryData = new iPartsDataFactoryDataList();
        EtkDisplayFields selectFields = new EtkDisplayFields();
        // die selectFields sind die gleichen wie beim Join zum Laden der Werkseinsatzdaten für die Stückliste ...
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_GUID, false, false)); // notwendig für die ID
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_FACTORY, false, false)); // muss ein gültiges Werk sein
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_SPKZ, false, false)); // notwendig für die ID
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_ADAT, false, false)); // notwendig für Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_DATA_ID, false, false)); // notwendig für Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_SERIES_NO, false, false)); // notwendig für Retail-Filter
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMA, false, false)); // Handelt es sich um eine PEM ab?
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMB, false, false)); // Handelt es sich um eine PEM bis?
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMTA, false, false)); // Termin ab
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMTB, false, false)); // Termin bis
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_STCA, false, false)); // Steuercode ab
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_STCB, false, false)); // Steuercode bis
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_FN_ID, false, false)); // ELDAS Fußnoten-ID
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FACTORY_DATA, FIELD_DFD_STATUS, false, false)); // Der Freigabestatus

        // ... außerdem wird noch die DO_ID benötigt um zusätzliche, nicht relevante Einträge auszufiltern,
        // und zum Löschen der Einträge in DA_DIALOG_CHANGES noch der Hash und der Typ
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_DO_ID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_DO_TYPE, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_HASH, false, false));

        relevantFactoryData.searchSortAndFillWithJoin(project, null, selectFields, new String[]{ FIELD_DFD_GUID },
                                                      TABLE_DA_DIALOG_CHANGES,
                                                      new String[]{ FIELD_DDC_BCTE },
                                                      false, false,
                                                      new String[]{
                                                              TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_STATUS),
                                                              TableAndFieldName.make(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_DO_TYPE),
                                                              TableAndFieldName.make(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_CHANGE_SET_GUID) },
                                                      new String[]{
                                                              iPartsDataReleaseState.NEW.getDbValue(),
                                                              iPartsDataDIALOGChange.ChangeType.FACTORY_DATA.getDbKey(),
                                                              "" },
                                                      false, null, false);

//        // ======================= start Testcode =========================================================================
//        List<String> testKeys = new DwList<>();
        // Für Daimler-9735 nur den C204 einkommentieren und die wherTablesAndFields und whereValues anpassen
//        testKeys.add("C204|98|24|04|1000|0030|||FW|20110329103435");
//        testKeys.add("C213|08|04|20|2055|0140|||FW|20180518113736");
//        testKeys.add("C213|08|04|20|2110|0220|||FW|20180518113736");
//        iPartsDataFactoryDataList testList = new iPartsDataFactoryDataList();
//        selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_DIALOG_CHANGES));
//        testList.searchSortAndFillWithJoin(project, null, selectFields, new String[]{ FIELD_DFD_GUID },
//                                                      TABLE_DA_DIALOG_CHANGES,
//                                                      new String[]{ FIELD_DDC_BCTE },
//                                                      false, false,
//                                                      new String[]{
//                                                              TableAndFieldName.make(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_BCTE),
//                                                              TableAndFieldName.make(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_BCTE),
//                                                              TableAndFieldName.make(TABLE_DA_DIALOG_CHANGES, FIELD_DDC_BCTE)},
//                                                      new String[]{
//                                                              testKeys.get(0),
//                                                              testKeys.get(1),
//                                                              testKeys.get(2),},
//                                                      true, null, false);
//
//        for (iPartsDataFactoryData testData : testList) {
//            String status = testData.getFieldValue(FIELD_DFD_STATUS);
//            String type = testData.getFieldValue( FIELD_DDC_DO_TYPE);
//            String guid = testData.getFieldValue( FIELD_DDC_CHANGE_SET_GUID);
//
//            if( status.equals(iPartsDataReleaseState.NEW.getDbValue()) &&
//                type.equals(iPartsDataDIALOGChange.ChangeType.FACTORY_DATA.getDbKey()) &&
//                guid.isEmpty()){
//                relevantFactoryData.add(testData, DBActionOrigin.FROM_DB);
//            }
//        }
//        // ======================= ende Testcode =========================================================================

        int maxProgress = 0;
        Map<IdWithType, List<EtkDataObject>> factoryDataByGroupId = new TreeMap<>();
        // Durch den Join werden zu viele auch potentiell falsche Einträge gefunden, weil der Join nur über den BCTE Schlüssel
        // geht, aber eigentlich über die berechnete DO_ID gehen müsste.
        for (iPartsDataFactoryData factoryData : relevantFactoryData) {
            if (factoryData.getAsId().toDBString().equals(factoryData.getFieldValue(FIELD_DDC_DO_ID))) {
                addFactoryDataGroupedByFactoryToMap(factoryDataByGroupId, factoryData);
                maxProgress++;
            }
        }

        // Dreifacher maxProgress wegen Prüfung, späterer Freigabe und Commit (zunächst simple Annahme dass alle Datensätze
        // freigegeben werden können)
        setMaxProgress(3 * maxProgress);

        logMessageToGui("!!Anzahl Werkseinsatzdaten mit Status \"neu\": %1", String.valueOf(maxProgress));
        return factoryDataByGroupId;
    }

    private FactoryDataForAutoRelease splitFactoryDataByFactory(Map<IdWithType, List<EtkDataObject>> dataGroupedByFactoryId) {
        FactoryDataForAutoRelease factoryDataForAutoRelease = new FactoryDataForAutoRelease();
        // Werksdaten pro Gruppen Werksdaten-ID durchgehen(ohne ADAT, ohne Werk und ohne SeqNummer)
        for (Map.Entry<IdWithType, List<EtkDataObject>> factoryDataByFactoryEntry : dataGroupedByFactoryId.entrySet()) {
            IdWithType groupedFactoryDataId = factoryDataByFactoryEntry.getKey();
            List<EtkDataObject> dataForGroupKey = factoryDataByFactoryEntry.getValue();
            // Den ältesten Datensatz bestimmen
            Optional<EtkDataObject> highestAdatObjectOptional = dataForGroupKey.stream().max(createAdatAndSeqNoComparator(FIELD_DFD_ADAT, FIELD_DFD_SEQ_NO));
            // Nur die Werksdaten des Werks berücksichtigen, das den ältesten Werksdatensatz besitzt
            highestAdatObjectOptional.ifPresent(highestADATObject -> {
                String factory = highestADATObject.getFieldValue(FIELD_DFD_FACTORY);
                for (EtkDataObject factoryData : factoryDataByFactoryEntry.getValue()) {
                    String factoryFromData = factoryData.getFieldValue(FIELD_DFD_FACTORY);
                    // Werksdaten zum Werk mit dem ältesten Datensatz gemeinsam durchgeben. Werksdaten zu anderen
                    // Werken für später aufsammeln
                    if (factoryFromData.equals(factory)) {
                        factoryDataForAutoRelease.addFactoryDataForCurrentCheck(groupedFactoryDataId, factoryData);
                    } else {
                        factoryDataForAutoRelease.addFactoryDataForFollowingCheck(groupedFactoryDataId, factoryData);
                    }

                }
            });
        }
        return factoryDataForAutoRelease;
    }

    private void autoReleaseFactoryData(FactoryDataForAutoRelease factoryDataForAutoRelease, Map<IdWithType, Boolean> autoReleaseWasSuccessful) {

        logMessageToLogChannel("Starting factory data auto release check for " + maxProgress + " datasets");

        int noAutomationPossibleCount = 0;
        int notProcessedDataWithHigherSeqNo = 0;
        PartListEntryFactoryDataAutomationChecker checker = new PartListEntryFactoryDataAutomationChecker(this);

        for (Map.Entry<IdWithType, List<EtkDataObject>> changeEntry : factoryDataForAutoRelease.getDataForCurrentCheck().entrySet()) {
            if (isCancelled()) {
                return;
            }

            IdWithType key = changeEntry.getKey();
            String bcte = ((iPartsFactoryDataId)key).getGuid();
            if (!autoReleaseWasSuccessful.isEmpty() && (autoReleaseWasSuccessful.containsKey(key) && !autoReleaseWasSuccessful.get(key))) {
                noAutomationPossibleCount++;
                logMessageToLogChannel("Releasing factory data of part " + bcte + " was not successful in previous cycle. " +
                                       "Releasing factory data in following cycles is no more possible for this part.");
                continue;
            }

            List<EtkDataPartListEntry> retailPartlistEntriesAll
                    = EditConstructionToRetailHelper.getRetailSourceGuidPartListEntries(iPartsEntrySourceType.DIALOG,
                                                                                        bcte, null, project);
            // Hier werden die PSK Positionen aus der DB ausgefiltert, damit sie sie nicht in die Prüfung einspielen
            List<EtkDataPartListEntry> retailPartlistEntries = removePSKEntries(project, retailPartlistEntriesAll);

            Map<String, Long> collectedDateFromForFactory = new HashMap<>();
            Map<String, Long> collectedDateToForFactory = new HashMap<>();
            PreconditionResult preconditionPEMFlagsResult = checkPreconditionPEMFlags(retailPartlistEntries, collectedDateFromForFactory,
                                                                                      collectedDateToForFactory);
            checker.clearData();
            List<EtkDataObject> factoryDataList = changeEntry.getValue();
            // Neue Datensätze aus der DB nach ADAT und Sequenznummer sortieren. Pro ADAT soll nur der "älteste" neue
            // Datensatz berücksichtigt werden
            factoryDataList.sort(createAdatAndSeqNoComparator(FIELD_DFD_ADAT, FIELD_DFD_SEQ_NO).reversed());
            // Alle neuen Werkseinsatzdaten auf automatische Freigabe hin prüfen
            Set<iPartsFactoryDataId> alreadyCheckData = new HashSet<>();
            for (EtkDataObject newFactoryDataObject : factoryDataList) {
                if (isCancelled()) {
                    return;
                }
                if (newFactoryDataObject instanceof iPartsDataFactoryData) {
                    iPartsDataFactoryData newFactoryData = (iPartsDataFactoryData)newFactoryDataObject;
                    iPartsFactoryDataId id = newFactoryData.getAsId();
                    iPartsFactoryDataId idWithoutSeqNo = new iPartsFactoryDataId(id.getGuid(), id.getFactory(), id.getSplitAttribute(), id.getAdat(), id.getDataId());
                    if (!alreadyCheckData.add(idWithoutSeqNo)) {
                        notProcessedDataWithHigherSeqNo++;
                        logMessageToLogChannel("Skipping id " + id.toStringForLogMessages() + ". A dataset with the same id and a lower sequence number has already been processed");
                        continue;
                    }

                    // DA_DIALOG_CHANGES Eintrag bestimmen (Felder wurden beim Join mitgeladen)
                    String doType = newFactoryData.getFieldValue(FIELD_DDC_DO_TYPE);
                    String doId = newFactoryData.getFieldValue(FIELD_DDC_DO_ID);
                    String hash = newFactoryData.getFieldValue(FIELD_DDC_HASH);
                    iPartsDataDIALOGChange dialogChange = new iPartsDataDIALOGChange(project, new iPartsDialogChangesId(doType, doId, hash));
                    iPartsDataDIALOGChangeList dialogChangeAsList = new iPartsDataDIALOGChangeList();
                    dialogChangeAsList.add(dialogChange, DBActionOrigin.FROM_EDIT);
                    factoryDataDialogChanges.put(id, dialogChangeAsList);

                    PreconditionResult preconditionResult = preconditionPEMFlagsResult;
                    if (preconditionResult == PreconditionResult.CHECK_PEM_DATES) {
                        preconditionResult = checkPreconditionPEMDatesChanged(newFactoryData, collectedDateFromForFactory,
                                                                              collectedDateToForFactory);
                    }

                    if (preconditionResult == PreconditionResult.NO_AUTOMATION_POSSIBLE) {
                        checker.addToDateThresholdCheck(newFactoryData);
                        noAutomationPossibleCount++;
                        logMessageToLogChannel(newFactoryData, "Preconditions invalid -> do date threshold check");
                        autoReleaseWasSuccessful.put(changeEntry.getKey(), false);
                    } else if (preconditionResult == PreconditionResult.AUTOMATION_POSSIBLE) {
                        checker.doPartListEntryFactoryDataChecks(retailPartlistEntries, newFactoryData, releaseList);
                        autoReleaseWasSuccessful.put(changeEntry.getKey(), true);
                    }
                }
                increaseProgress(1);
            }
            // Jetzt die Daten aus dem Checker hier in die Liste einfügen
            checker.addDataForDateThresholdCheck(dateThresholdData);
        }

        logMessageToGui("!!Anzahl Datensätze, für die die Vorbedingungen nicht erfüllt wurden: %1", String.valueOf(noAutomationPossibleCount));
        checker.logMessagesToGUI();

        logMessageToLogChannel("Finished factory data auto release for " + maxProgress + " datasets");
        logMessageToLogChannel("Released factory data datasets: " + releaseList.size());
        logMessageToLogChannel("Datasets with invalid preconditions: " + noAutomationPossibleCount);
        logMessageToLogChannel("Datasets not processed due to higher sequence number: " + notProcessedDataWithHigherSeqNo);
        checker.logMessagesToLogChannel();

        if (!releaseList.isEmpty()) {
            checkAutoReleasedFactoryDataInActiveChangeSets(releaseList);
        }
    }

    /**
     * Entfernt Stücklistenpositionen, die in PSK Stücklisten vorkommen
     *
     * @param project
     * @param retailPartListEntriesAll
     * @return
     */
    private List<EtkDataPartListEntry> removePSKEntries(EtkProject project, List<EtkDataPartListEntry> retailPartListEntriesAll) {
        Map<AssemblyId, Boolean> assemblyIsPSKMap = new HashMap<>();
        return retailPartListEntriesAll.stream()
                .filter(entry -> !ASUsageHelper.isPSKAssembly(project, entry.getOwnerAssemblyId(), assemblyIsPSKMap))
                .collect(Collectors.toList());
    }

    private void checkAutoReleasedFactoryDataInActiveChangeSets(List<EtkDataObject> autoReleasedFactoryDataList) {
        setMaxProgress(autoReleasedFactoryDataList.size());
        logMessageToGui("!!Prüfe relevante Stücklisteneinträge in nicht freigegebenen Autoren-Aufträgen...");
        logMessageToLogChannel("Checking relevant parts list entries in not released author orders...");

        SerializedDbDataObjectAsJSON serializedDbDataObjectAsJSON = new SerializedDbDataObjectAsJSON(true);
        EnumSet<SerializedDBDataObjectState> validStatesForPLE = EnumSet.of(SerializedDBDataObjectState.NEW,
                                                                            SerializedDBDataObjectState.REPLACED,
                                                                            SerializedDBDataObjectState.MODIFIED);
        EnumSet<SerializedDBDataObjectState> validStatesForFactoryData = EnumSet.of(SerializedDBDataObjectState.NEW,
                                                                                    SerializedDBDataObjectState.REPLACED,
                                                                                    SerializedDBDataObjectState.MODIFIED,
                                                                                    SerializedDBDataObjectState.DELETED);
        final String[] selectFieldsForPLE = { FIELD_K_VARI, FIELD_K_VER, FIELD_K_LFDNR };
        for (EtkDataObject dataObject : autoReleasedFactoryDataList) {
            if (isCancelled()) {
                return;
            }

            if (dataObject instanceof iPartsDataFactoryData) {
                final iPartsDataFactoryData dataFactoryData = (iPartsDataFactoryData)dataObject;
                logMessageToLogChannel(dataFactoryData, "Searching relevant parts list entries in not released author orders...");
                iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dataFactoryData.getGUID());
                if (bctePrimaryKey != null) {
                    // DIALOG POS inkl. AA
                    String posKey = bctePrimaryKey.getPositionKeyWithoutAA() + DIALOG_GUID_DELIMITER + "*" + DIALOG_GUID_DELIMITER
                                    + bctePrimaryKey.getAA() + DIALOG_GUID_DELIMITER + "*";
                    iPartsDataChangeSetEntryList dataChangeSetEntries = iPartsRevisionsHelper.getActiveChangeSetEntriesContainingSourceGUID(PartListEntryId.TYPE,
                                                                                                                                            posKey,
                                                                                                                                            project);

                    // Alle betroffenen ChangeSets und darin die Stücklisteneinträge aufsammeln
                    Set<String> changeSetGUIDs = new HashSet<>();
                    final Map<String, Set<PartListEntryId>> changeSetsToPartListEntryIdsMap = new HashMap<>();
                    int numPartListEntries = 0;
                    for (iPartsDataChangeSetEntry dataChangeSetEntry : dataChangeSetEntries) {
                        if (isCancelled()) {
                            return;
                        }

                        SerializedDBDataObject serializedDBDataObject = serializedDbDataObjectAsJSON.getFromJSON(dataChangeSetEntry.getCurrentData());
                        if (!validStatesForPLE.contains(serializedDBDataObject.getState())) {
                            continue;
                        }

                        String sourceGUID = dataChangeSetEntry.getSourceGUID();
                        iPartsDialogBCTEPrimaryKey bctePrimaryKeyOfPLE = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(sourceGUID);
                        if (bctePrimaryKeyOfPLE != null) {
                            // Ausführungsart von den Werkseinsatzdaten und dem Stücklisteneintrag müssen übereinstimmen
                            // Wird eigentlich schon durch den posKey geprüft, aber die beiden *-Wildcards könnten theoretisch
                            // zu falschen Treffern führen
                            if (!bctePrimaryKeyOfPLE.getAA().equals(bctePrimaryKey.getAA())) {
                                continue;
                            }
                        } else { // Ungültig
                            continue;
                        }

                        String changeSetGUID = dataChangeSetEntry.getAsId().getGUID();
                        changeSetGUIDs.add(changeSetGUID);
                        // Damit die Stücklisteneinträge nach Modul sortiert sind für bessere Cache-Performance
                        Set<PartListEntryId> partListEntryIds = changeSetsToPartListEntryIdsMap.computeIfAbsent(changeSetGUID, k -> new TreeSet<>());

                        IdWithType id = dataChangeSetEntry.getAsId().getDataObjectIdWithType();
                        PartListEntryId partListEntryId = new PartListEntryId(id.toStringArrayWithoutType());
                        partListEntryIds.add(partListEntryId);
                        numPartListEntries++;
                    }

                    // Suche nach Verwendungen für neue/geänderte/gelöschte Werkseinsatzdaten in der DIALOG POS
                    dataChangeSetEntries = iPartsRevisionsHelper.getActiveChangeSetEntriesContainingSourceGUID(iPartsFactoryDataId.TYPE,
                                                                                                               posKey,
                                                                                                               project);
                    // Alle betroffenen ChangeSets mit den BCTE-Schlüsseln für die gefundenen Werkseinsatzdaten aufsammeln
                    final Map<String, Set<String>> changeSetsToBCTEKeysMap = new HashMap<>();
                    int numFactoryDataBCTEKeys = 0;
                    for (iPartsDataChangeSetEntry dataChangeSetEntry : dataChangeSetEntries) {
                        if (isCancelled()) {
                            return;
                        }

                        SerializedDBDataObject serializedDBDataObject = serializedDbDataObjectAsJSON.getFromJSON(dataChangeSetEntry.getCurrentData());
                        if (!validStatesForFactoryData.contains(serializedDBDataObject.getState())) {
                            continue;
                        }

                        IdWithType id = dataChangeSetEntry.getAsId().getDataObjectIdWithType();
                        iPartsFactoryDataId factoryDataId = new iPartsFactoryDataId(id.toStringArrayWithoutType());
                        String bcteKey = factoryDataId.getGuid();
                        iPartsDialogBCTEPrimaryKey bctePrimaryKeyOfFactoryData = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(bcteKey);
                        if (bctePrimaryKeyOfFactoryData != null) {
                            // Ausführungsart von den Werkseinsatzdaten und dem Stücklisteneintrag müssen übereinstimmen
                            // Wird eigentlich schon durch den posKey geprüft, aber die beiden *-Wildcards könnten theoretisch
                            // zu falschen Treffern führen
                            if (!bctePrimaryKeyOfFactoryData.getAA().equals(bctePrimaryKey.getAA())) {
                                continue;
                            }
                        } else { // Ungültig
                            continue;
                        }

                        String changeSetGUID = dataChangeSetEntry.getAsId().getGUID();
                        changeSetGUIDs.add(changeSetGUID);
                        Set<String> bcteKeys = changeSetsToBCTEKeysMap.computeIfAbsent(changeSetGUID, k -> new TreeSet<>());
                        // Sortierung nach BCTE-Schlüssel könnte bessere Cache-Performance bringen
                        bcteKeys.add(bcteKey);
                        numFactoryDataBCTEKeys++;
                    }

                    int numChangeSets = changeSetGUIDs.size();
                    if (numChangeSets == 0) { // Keine relevanten ChangeSets gefunden
                        logMessageToGui("!!%1: Keine relevanten nicht freigegebenen Autoren-Aufträge", dataFactoryData.getAsId().toStringForLogMessages());
                        logMessageToLogChannel(dataFactoryData, "No relevant not released author orders");
                        continue;
                    }

                    logMessageToGui("!!%1: Berechne die Ergebnisse für %2 relevante Stücklisteneinträge%3  und %4 relevante BCTE-Schlüssel aufgrund von neuen/veränderten/gelöschten Werkseinsatzdaten%3  in %5 nicht freigegebenen Autoren-Aufträgen...",
                                    dataFactoryData.getAsId().toStringForLogMessages(), String.valueOf(numPartListEntries),
                                    "\n", String.valueOf(numFactoryDataBCTEKeys), String.valueOf(numChangeSets));
                    logMessageToLogChannel(dataFactoryData, "Calculating results for " + numPartListEntries + " relevant parts list enries and "
                                                            + numFactoryDataBCTEKeys + " relevant BCTE keys because of new/modified/deleted factory data in "
                                                            + numChangeSets + " not released author orders");
                    increaseMaxProgress(numChangeSets);

                    // Jedes gefundene ChangeSet aktivieren und alle betroffenen Stücklisteneinträge durchrechnen
                    List<ChangeSetModificator.ChangeSetModificationTask> changeSetModificationTasks = new DwList<>();
                    for (final String changeSetGUID : changeSetGUIDs) {
                        if (isCancelled()) {
                            return;
                        }

                        ChangeSetModificator.ChangeSetModificationTask changeSetModificationTask = new ChangeSetModificator.ChangeSetModificationTask(changeSetGUID) {
                            @Override
                            public void modifyChangeSet(EtkProject projectForChangeSet, iPartsRevisionChangeSet authorOrderChangeSet,
                                                        GenericEtkDataObjectList dataObjectListForChangeSet) {
                                assemblyCache.clear();
                                Set<PartListEntryId> partListEntryIds = changeSetsToPartListEntryIdsMap.get(changeSetGUID);
                                Map<PartListEntryId, Boolean> partListEntryIdsToModelFilterMap = new TreeMap<>();
                                if (partListEntryIds != null) {
                                    for (PartListEntryId partListEntryId : partListEntryIds) {
                                        // Normale Prüfung ohne Zwangs-Prüfung für den Baumuster-Filter
                                        partListEntryIdsToModelFilterMap.put(partListEntryId, false);
                                    }
                                }

                                Set<EtkDataPartListEntry> partListEntriesToBeChecked = new LinkedHashSet<>();
                                iPartsChangeSetId changeSetId = new iPartsChangeSetId(changeSetGUID);
                                projectForChangeSet.startPseudoTransactionForActiveChangeSet(true);
                                try {
                                    // Relevante Stücklisteneinträge in vorhandenen AS-Stücklisten sowie im aktuellen ChangeSet
                                    // für alle relevanten BCTE-Schlüssel aufgrund von neuen/veränderten/gelöschten Werkseinsatzdaten
                                    // aufsammeln
                                    Set<String> bcteKeys = changeSetsToBCTEKeysMap.get(changeSetGUID);
                                    if (bcteKeys != null) {
                                        for (String bcteKey : bcteKeys) {
                                            if (isCancelled()) {
                                                return;
                                            }

                                            // Sucht über die Pseudo-Transaktion sowohl in AS-Stücklisten als auch im aktuellen ChangeSet
                                            DBDataObjectAttributesList attributesList = EditConstructionToRetailHelper.getRetailSourceGuidAttributeList(iPartsEntrySourceType.DIALOG,
                                                                                                                                                        bcteKey,
                                                                                                                                                        null,
                                                                                                                                                        selectFieldsForPLE,
                                                                                                                                                        projectForChangeSet);
                                            for (DBDataObjectAttributes dataAttributes : attributesList) {
                                                // Für diese Stücklisteneinträge muss der Baumuster-Filter (Prüfung 2) auf jeden Fall
                                                // durchgeführt werden, da Prüfung 1 bzgl. der Zeitscheiben ansonsten fälschlicherweise
                                                // bereits OK zurückliefern würde obwohl sich die Baumuster-Filterergebnisse aufgrund
                                                // der Werkseinsatzdaten von anderen Stücklisteneinträgen ändern würden
                                                partListEntryIdsToModelFilterMap.put(new PartListEntryId(dataAttributes.getFieldValue(FIELD_K_VARI),
                                                                                                         dataAttributes.getFieldValue(FIELD_K_VER),
                                                                                                         dataAttributes.getFieldValue(FIELD_K_LFDNR)), true);
                                            }
                                        }
                                    }

                                    // Jetzt die Ergebnisse für direkt gefundenen Stücklisteneinträge sowie die über den BCTE-Schlüssel
                                    // von neuen/veränderten/gelöschten Werkseinsatzdaten durchrechnen
                                    Map<AssemblyId, EtkDataAssembly> assemblyCache = new TreeMap<>();
                                    Map<EtkDataPartListEntry, Boolean> relevantPLEToModelFilterMap = new LinkedHashMap<>();

                                    Map<AssemblyId, Boolean> assemblyIsPSKMap = new HashMap<>();
                                    for (Map.Entry<PartListEntryId, Boolean> pleToModelFilterEntry : partListEntryIdsToModelFilterMap.entrySet()) {
                                        if (isCancelled()) {
                                            return;
                                        }

                                        PartListEntryId partListEntryId = pleToModelFilterEntry.getKey();
                                        AssemblyId assemblyId = partListEntryId.getOwnerAssemblyId();
                                        EtkDataAssembly assembly = assemblyCache.get(assemblyId);
                                        if (assembly == null) {
                                            assembly = EtkDataObjectFactory.createDataAssembly(projectForChangeSet, assemblyId);
                                            assemblyCache.put(assemblyId, assembly);
                                        }
                                        // Das funktioniert hier, weil das ChangeSet extra geladen wird
                                        if (ASUsageHelper.isPSKAssembly(projectForChangeSet, assemblyId, assemblyIsPSKMap)) {
                                            continue;
                                        }
                                        EtkDataPartListEntry partListEntry = assembly.getPartListEntryFromKLfdNrUnfiltered(partListEntryId.getKLfdnr());
                                        if (partListEntry != null) {
                                            relevantPLEToModelFilterMap.put(partListEntry, pleToModelFilterEntry.getValue());
                                            logMessageToLogChannel(dataFactoryData, changeSetId, partListEntryId,
                                                                   "Relevant parts list entry must be checked");
                                        } else {
                                            logMessageToGui("!!Stücklisteneintrag %1 mit aktivem Änderungsset \"%2\" existiert nicht",
                                                            partListEntryId.toStringForLogMessages(), changeSetId.getGUID());
                                            logMessageToLogChannel(dataFactoryData, changeSetId, partListEntryId,
                                                                   "Parts list entry does not exist with active change set");
                                        }
                                    }

                                    // Automatische Freigabe mit jedem relevanten Stücklisteneintrag im ChangeSet durchrechnen
                                    increaseMaxProgress(relevantPLEToModelFilterMap.size());
                                    for (Map.Entry<EtkDataPartListEntry, Boolean> relevantPLEToModelFilterEntry : relevantPLEToModelFilterMap.entrySet()) {
                                        EtkDataPartListEntry relevantPartlistEntry = relevantPLEToModelFilterEntry.getKey();
                                        if (!checkAutoReleasedFactoryDataInActiveChangeSet(dataFactoryData, changeSetId, relevantPartlistEntry,
                                                                                           relevantPLEToModelFilterEntry.getValue())) {
                                            partListEntriesToBeChecked.add(relevantPartlistEntry);
                                            logMessageToGui("!!Stücklisteneintrag %1 mit aktivem Änderungsset \"%2\" muss von einem Autor überprüft werden",
                                                            relevantPartlistEntry.getAsId().toStringForLogMessages(), changeSetId.getGUID());
                                            logMessageToLogChannel(dataFactoryData, changeSetId, relevantPartlistEntry.getAsId(),
                                                                   "Parts list entry must be confirmed by an author");
                                        }
                                        increaseProgress(1);
                                    }
                                } finally {
                                    projectForChangeSet.stopPseudoTransactionForActiveChangeSet();
                                    iPartsRevisionChangeSet.clearCachesForActiveChangeSets(projectForChangeSet);
                                }

                                // Datensätze für DA_CONFIRM_CHANGES anlegen und merken für die Bestätigung durch den Autor
                                // Hier muss das normale project verwendet werden zum Speichern und nicht projectForChangeSet
                                for (EtkDataPartListEntry partlistEntryToBeChecked : partListEntriesToBeChecked) {
                                    iPartsConfirmChangesId confirmChangesId = new iPartsConfirmChangesId(changeSetId.getGUID(),
                                                                                                         dataFactoryData.getAsId(),
                                                                                                         partlistEntryToBeChecked.getAsId());
                                    iPartsDataConfirmChanges dataConfirmChanges = new iPartsDataConfirmChanges(project, confirmChangesId);
                                    if (!dataConfirmChanges.existsInDB()) {
                                        dataConfirmChanges.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                                    } else {
                                        // Benutzer und Datum wieder leeren falls der Datensatz schon existiert, da offenbar erneut
                                        // eine Bestätigung für diesen Werkseinsatzdaten-Datensatz notwendig ist
                                        dataConfirmChanges.setFieldValue(FIELD_DCC_CONFIRMATION_USER, "", DBActionOrigin.FROM_EDIT);
                                        dataConfirmChanges.setFieldValue(FIELD_DCC_CONFIRMATION_DATE, "", DBActionOrigin.FROM_EDIT);
                                    }
                                    dataConfirmChanges.setFieldValue(FIELD_DCC_DO_SOURCE_GUID, dataFactoryData.getGUID(), DBActionOrigin.FROM_EDIT);
                                    confirmChangesList.add(dataConfirmChanges, DBActionOrigin.FROM_EDIT);
                                }

                                assemblyCache.clear();
                                increaseProgress(1);
                            }
                        };
                        changeSetModificationTasks.add(changeSetModificationTask);
                    }

                    ChangeSetModificator changeSetModificator = new ChangeSetModificator(iPartsImportPlugin.LOG_CHANNEL_FACTORYDATA_AUTO_RELEASE);
                    changeSetModificator.executeChangesInAllChangeSets(changeSetModificationTasks, true, TECHNICAL_USER_AUTO_RELEASE);
                }
            }
            increaseProgress(1);
        }
    }

    private boolean checkAutoReleasedFactoryDataInActiveChangeSet(iPartsDataFactoryData newFactoryData, iPartsChangeSetId changeSetId,
                                                                  EtkDataPartListEntry relevantPartlistEntry, boolean mustCheckModelFilter) {
        // Einzelnen Stücklisteneintrag überprüfen
        List<EtkDataPartListEntry> partListEntriesToBeChecked = new DwList<>(1);
        partListEntriesToBeChecked.add(relevantPartlistEntry);

        // Code analog zu autoReleaseFactoryData()
        Map<String, Long> collectedDateFromForFactory = new HashMap<>();
        Map<String, Long> collectedDateToForFactory = new HashMap<>();
        PreconditionResult preconditionResult = checkPreconditionPEMFlags(partListEntriesToBeChecked, collectedDateFromForFactory,
                                                                          collectedDateToForFactory);

        // Den übergebenen Stücklisteneintrag bzgl. des übergebenen neuen Werkseinsatzdaten-Datensatzes hin prüfen, ob
        // für diese ebenfalls eine automatische Freigabe möglich wäre
        if (preconditionResult == PreconditionResult.CHECK_PEM_DATES) {
            preconditionResult = checkPreconditionPEMDatesChanged(newFactoryData, collectedDateFromForFactory,
                                                                  collectedDateToForFactory);
        }

        if (preconditionResult == PreconditionResult.NO_AUTOMATION_POSSIBLE) {
            logMessageToLogChannel(newFactoryData, changeSetId, relevantPartlistEntry.getAsId(),
                                   "Preconditions invalid -> parts list entry must be confirmed by an author");
        } else if (preconditionResult == PreconditionResult.AUTOMATION_POSSIBLE) {
            // Prüfung 1: Maximale Werkszeitscheibe je Baumuster
            Map<PartListEntryId, ModelsForPartlistEntry> modelsAndPartlistEntriesWithChanges = checkMaxFactoryTimeSlice(newFactoryData,
                                                                                                                        partListEntriesToBeChecked);
            if (modelsAndPartlistEntriesWithChanges.isEmpty() && !mustCheckModelFilter) {
                // Keine Änderungen der Zeitscheiben: Daten können freigegeben werden
                logMessageToLogChannel(newFactoryData, changeSetId, relevantPartlistEntry.getAsId(),
                                       "Check 1: No change in timeslices -> parts list entry OK");
                return true;
            } else {
                // Es gibt Änderungen der Zeitscheiben oder der Baumuster-Filter MUSS geprüft werden: Weiter mit Prüfung 2
                if (modelsAndPartlistEntriesWithChanges.isEmpty()) {
                    logMessageToLogChannel(newFactoryData, changeSetId, relevantPartlistEntry.getAsId(),
                                           "Check 1: No change in timeslices -> executing check 2 because of modified factory data");
                } else {
                    logMessageToLogChannel(newFactoryData, changeSetId, relevantPartlistEntry.getAsId(),
                                           "Check 1: Timeslices changed -> executing check 2");
                }

                // Falls der Baumuster-Filter auf jeden Fall geprüft werden muss, alle Baumuster auch wirklich prüfen
                if (mustCheckModelFilter && (relevantPartlistEntry instanceof iPartsDataPartListEntry)) {
                    ModelsForPartlistEntry modelsForPartlistEntry = new ModelsForPartlistEntry((iPartsDataPartListEntry)relevantPartlistEntry);
                    AssemblyCacheData cacheData = getAssemblyCacheData(relevantPartlistEntry.getOwnerAssembly());
                    if (cacheData.getModelNumbers() != null) {
                        modelsForPartlistEntry.modelNumbers.addAll(cacheData.getModelNumbers());
                    }
                    modelsAndPartlistEntriesWithChanges.put(relevantPartlistEntry.getAsId(), modelsForPartlistEntry);
                }

                Collection<iPartsDataPartListEntry> pleWithDifferentModelFilterResult =
                        checkModelFilterResult(modelsAndPartlistEntriesWithChanges, newFactoryData);
                if (pleWithDifferentModelFilterResult.isEmpty()) {
                    // Keine Abweichung beim Baumusterfilter => freigeben
                    logMessageToLogChannel(newFactoryData, changeSetId, relevantPartlistEntry.getAsId(),
                                           "Check 2: No change in model filter results -> parts list entry OK");
                    return true;
                } else {
                    // DAIMLER-7921 TODO: Prüfung 3
                    if (Logger.getLogger().isChannelActive(iPartsImportPlugin.LOG_CHANNEL_FACTORYDATA_AUTO_RELEASE)) {
                        DwList<String> entriesForLog = new DwList<>();
                        for (iPartsDataPartListEntry entry : pleWithDifferentModelFilterResult) {
                            entriesForLog.add("(" + entry.getAsId().toStringForLogMessages() + ")");
                        }
                        logMessageToLogChannel(newFactoryData, changeSetId, relevantPartlistEntry.getAsId(),
                                               "Check 2: Model filter results changed -> executing check 3 for the following partListEntries: ["
                                               + StrUtils.stringListToString(entriesForLog, ", ") + "]");
                    }
                }
            }
        }

        return false;
    }

    private void releaseColorTableFactoryData() {
        Map<IdWithType, List<EtkDataObject>> releaseHistoryLists = new HashMap<>();
        for (EtkDataObject factoryData : releaseList) {
            addColorFactoryDataToMap(releaseHistoryLists, (iPartsDataColorTableFactory)factoryData);
        }
        releaseFactoryData(true, releaseHistoryLists, FIELD_DCCF_ADAT, FIELD_DCCF_STATUS);
    }

    private void releaseFactoryData() {
        Map<IdWithType, List<EtkDataObject>> releaseHistoryLists = new HashMap<>();
        // pro BCTE Schlüssel nur den neusten wirklich freigeben, alle anderen nicht relevant setzen
        for (EtkDataObject factoryData : releaseList) {
            addFactoryDataToMap(releaseHistoryLists, (iPartsDataFactoryData)factoryData);
        }
        releaseFactoryData(false, releaseHistoryLists, FIELD_DFD_ADAT, FIELD_DFD_STATUS);
    }

    private void releaseFactoryData(boolean isColorFactoryData, Map<IdWithType, List<EtkDataObject>> releaseHistoryLists,
                                    String adatField, String statusField) {
        logMessageToGui("!!Anzahl Datensätze, die automatisch freigegeben werden können: %1", String.valueOf(releaseList.size()));
        if (releaseList.isEmpty()) {
            logMessageToGui("!!Automatische Freigabe beendet. Es wurde kein Änderungsset angelegt.");
            fireProgress(maxProgress);
            return;
        }

        GenericEtkDataObjectList<EtkDataObject> dataObjectList = new GenericEtkDataObjectList<>();
        // Einträge aus DA_FACTORY_DATA freigeben und den passenden Eintrag dazu aus DA_DIALOG_CHANGES löschen
        setMaxProgress(currentProgress + (2 * releaseList.size())); // maximalen Fortschritt anpassen

        int countReleased = 0;
        int countNotReleased = 0;
        int countNotReleaseDueToADAT = 0;

        for (List<EtkDataObject> factoryDataGroup : releaseHistoryLists.values()) {
            if (isCancelled()) {
                return;
            }
            if (factoryDataGroup.size() > 0) {
                if (isColorFactoryData) {
                    // Werkseinsatzdaten zu Farben müssen nach Original-SDA absteigend sortiert werden
                    FactoryDataHelper.sortProductionColorTableFactoryData(factoryDataGroup);
                } else {
                    // Die Liste nach ADAT und SeqNo sortieren
                    Comparator<EtkDataObject> comparator = createAdatAndSeqNoComparator(adatField, FIELD_DFD_SEQ_NO);
                    Collections.sort(factoryDataGroup, comparator);
                }
                // der erste Eintrag ist der neuste; nur dieser soll freigegeben werden
                factoryDataGroup.get(0).setFieldValue(statusField, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);

                // Falls noch weitere, ältere Einträge existieren, diese auf "nicht relevant" setzen
                // Die Einträge haben alle den Status "neu", deshalb kann hier einfach überschrieben werden
                if (factoryDataGroup.size() > 1) {
                    for (int i = 1; i < factoryDataGroup.size(); i++) {
                        factoryDataGroup.get(i).setFieldValue(statusField, iPartsDataReleaseState.NOT_RELEVANT.getDbValue(), DBActionOrigin.FROM_EDIT);
                        countNotReleaseDueToADAT++;
                    }
                }
            }
        }

        for (EtkDataObject factoryData : releaseList) {
            if (isCancelled()) {
                return;
            }

            if (iPartsDataReleaseState.getTypeByDBValue(factoryData.getFieldValue(statusField)) == iPartsDataReleaseState.RELEASED) {
                countReleased++;
                // Alle Einträge aus der DB ermitteln, bei denen der Status auf "nicht relevant" gesetzt werden soll.
                // Konkret sind das hier nur Datensätze mit Status "freigegeben".
                // Datensätze mit Status "neu"" wurden durch den Join bereits geladen. Falls während der Laufzeit der automatischen Freigabe
                // weitere Datensätze mit Status "neu" angelegt werden, müssen diese hier ignoriert werden, weil sonst das Ergebnis
                // verfälscht werden könnte. Diese Datensätze werden dann beim nächsten Durchlauf korrekt behandelt.
                List<? extends EtkDataObject> factoryDataHistoryListFromDB = getFactoryDataHistoryList(factoryData, isColorFactoryData);
                for (EtkDataObject factoryDataFromDB : factoryDataHistoryListFromDB) {
                    if (!factoryDataFromDB.getAsId().equals(factoryData.getAsId())) {
                        iPartsDataReleaseState currentReleaseState = iPartsDataReleaseState.getTypeByDBValue(factoryDataFromDB.getFieldValue(statusField));
                        if (currentReleaseState == iPartsDataReleaseState.RELEASED) {
                            factoryDataFromDB.setFieldValue(statusField, iPartsDataReleaseState.NOT_RELEVANT.getDbValue(),
                                                            DBActionOrigin.FROM_EDIT);
                            countNotReleased++;
                            dataObjectList.add(factoryDataFromDB, DBActionOrigin.FROM_EDIT);
                            // Nachdem hier nur freigegebene Datensätze betrachtet werden, gehen wir davon aus, dass es keine
                            // DA_DIALOG_CHANGES-Einträge dazu gibt, die gelöscht werden müssten.
                        }
                    }
                }
            }

            iPartsDataDIALOGChangeList dialogChanges = factoryDataDialogChanges.get(factoryData.getAsId());
            for (iPartsDataDIALOGChange dialogChange : dialogChanges) {
                dataObjectList.delete(dialogChange, true, DBActionOrigin.FROM_EDIT);
            }

            // durch den Join beim Laden enthält das DA_FACTORY_DATA Objekt zusätzliche Attribute, die jetzt entfernt werden müssen
            factoryData.removeForeignTablesAttributes();
            dataObjectList.add(factoryData, DBActionOrigin.FROM_EDIT);
            increaseProgress(1);
        }

        iPartsRevisionChangeSet changeSet = iPartsRevisionChangeSet.createTempChangeSet(project, iPartsChangeSetSource.AUTO_RELEASE);
        changeSet.setExplicitUser(TECHNICAL_USER_AUTO_RELEASE);
        logMessageToGui("!!Automatisch freigegebene Datensätze werden gespeichert...");

        EtkDbObjectsLayer dbLayer = project.getDbLayer();
        dbLayer.startTransaction();
        dbLayer.startBatchStatement();
        try {
            changeSet.addDataObjectList(dataObjectList);
            if (isCancelled()) {
                dbLayer.cancelBatchStatement();
                dbLayer.rollback();
                return;
            }
            increaseProgress(releaseList.size());
            changeSet.commit();
            if (isCancelled()) {
                dbLayer.cancelBatchStatement();
                dbLayer.rollback();
                return;
            }
            if (!confirmChangesList.isEmpty()) {
                confirmChangesList.saveToDB(project);
            }
            dbLayer.endBatchStatement();
            dbLayer.commit();
        } catch (Exception e) {
            dbLayer.cancelBatchStatement();
            dbLayer.rollback();
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            if (messageLog != null) {
                messageLog.fireMessage(TranslationHandler.translate("!!Fehler beim Speichern der freigegebenen Datensätze: %1",
                                                                    Utils.exceptionToString(e)), MessageLogType.tmlError);
            }
        }

        logMessageToGui("!!Anzahl Datensätze die auf 'nicht relevant' gesetzt wurden weil es einen neueren, freigegebenen Datensatz gibt: %1", String.valueOf(countNotReleaseDueToADAT));
        logMessageToGui("!!Anzahl Datensätze aus der Datenbank die auf 'nicht relevant' gesetzt wurden: %1", String.valueOf(countNotReleased));
        logMessageToGui("!!Anzahl Datensätze die tatsächlich freigegeben wurden: %1", String.valueOf(countReleased));
        logMessageToGui("!!Automatische Freigabe beendet. Änderungsset-ID: %1", changeSet.getChangeSetId().getGUID());
        fireProgress(maxProgress);

        logMessageToLogChannel("Not released datasets due to other newer and released dataset: " + countNotReleaseDueToADAT);
        logMessageToLogChannel("Not released datasets: " + countNotReleased);
        logMessageToLogChannel("Released datasets: " + countReleased);
    }

    /**
     * Prüfung 1: Maximale Werkszeitscheibe je Baumuster
     * Dazu werden die Werkszeitscheiben der bisherigen, freigegebenen Werkseinsatzdaten berechnet und mit den Werkszeitscheiben
     * unter Berücksichtigung der neuen Werkseinsatzdaten verglichen. Ändern sich die Zeitscheiben nicht, können die Daten
     * freigegeben werden. Gibt es Abweichungen, dann wird weiter geprüft (Prüfung 2).
     *
     * @param newFactoryData        Die zu prüfenden neuen Werkseinsatzdaten für diesen BCTE Schlüssel
     * @param retailPartlistEntries Die Retail-Verwendungen zu den Werkseinsatzdaten (über den BCTE Schlüssel ermittelt)
     * @return Eine Map, die pro Stücklisteneintrag alle Baumuster enthält, bei denen Abweichungen gefunden wurden
     */
    public Map<PartListEntryId, ModelsForPartlistEntry> checkMaxFactoryTimeSlice(iPartsDataFactoryData newFactoryData,
                                                                                 Collection<EtkDataPartListEntry> retailPartlistEntries) {
        Map<PartListEntryId, ModelsForPartlistEntry> resultMap = new TreeMap<>();

        // Rückmeldedaten-Cache; wird verwendet, um relevante Rückmeldedaten den Werkseinsatzdaten zuzuordnen
        final iPartsResponseData responseData = iPartsResponseData.getInstance(project);
        // Ausreißer-Cache; wird verwendet, um relevante Ausreißer den Rückmeldedaten zuzuordnen
        final iPartsResponseSpikes responseSpikes = iPartsResponseSpikes.getInstance(project);

        for (EtkDataPartListEntry partlistEntry : retailPartlistEntries) {
            if (partlistEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsEntry = (iPartsDataPartListEntry)(partlistEntry);
                iPartsFactoryData originalFactoryData = iPartsEntry.getFactoryDataForRetailUnfiltered();

                // ermittle Produkt und Baumuster zu diesem Stücklisteneintrag
                AssemblyCacheData cachedData = getAssemblyCacheData(iPartsEntry.getOwnerAssembly());

                if ((cachedData.assembly != null) && (cachedData.getModelNumbers() != null)) {
                    Map<String, iPartsFactoryData> currentFactoryDataMap = new HashMap<>();
                    iPartsFactoryData factoryDataForRetail = null;
                    if (originalFactoryData != null) {
                        factoryDataForRetail = originalFactoryData.cloneMe();
                    }
                    iPartsEntry.setFactoryDataForRetailUnfiltered(factoryDataForRetail);
                    currentFactoryDataMap.put(iPartsEntry.getAsId().getKLfdnr(), factoryDataForRetail);

                    // Die Zeitscheibe für die bisher freigegebenen Werkseinsatzdaten pro Baumuster bestimmen und merken
                    Map<String, TimeSlice> originalTimeSlices = new TreeMap<>();
                    for (String model : cachedData.getModelNumbers()) {
                        originalTimeSlices.put(model, calculatePartlistentryTimeSlice(iPartsEntry, cachedData.productId,
                                                                                      model, project));
                    }

                    // Die neuen Werkseinsatzdaten einspielen und dabei auch den Status NEU berücksichtigen
                    DBDataObjectAttributes attributes = newFactoryData.getAttributes().cloneMe(DBActionOrigin.FROM_DB);
                    attributes.addFields(iPartsEntry.getAttributes(), DBActionOrigin.FROM_DB);

                    cachedData.assembly.loadFactoryDataCallback(attributes, factoriesInstance, cachedData.seriesIdMap,
                                                                releasedOrNewState, currentFactoryDataMap, responseData,
                                                                responseSpikes);

                    // Hier wird simuliert was passieren würde, wenn der aktuelle Datensatz freigegeben werden würde.
                    // D.h. es werden alle Datensätze aus der DB zum Werk auf nicht relevant gesetzt, unabhängig von ADAT oder Sequenz Nummer
                    // Die nicht relevanten Daten würden dann im Join nicht mit geladen, d.h. sie müssen hier gelöscht werden
                    simulateFactoryDataRelease(iPartsEntry, iPartsEntry.getFactoryDataForRetailUnfiltered(), newFactoryData.getAsId());

                    // Die Zeitscheibe mit den neuen Werkseinsatzdaten pro Baumuster bestimmen und prüfen, ob sich durch
                    // den neuen Datensatz etwas ändert
                    for (String model : cachedData.getModelNumbers()) {
                        TimeSlice newTimeSlice = null;
                        TimeSlice originalTimeSlice = originalTimeSlices.get(model);
                        if (originalTimeSlice == null) {
                            logMessageToGui("!!Bei Stücklisteneintrag \"%1\" gibt es für Baumuster \"%2\" keine Original-Werkszeitscheibe",
                                            iPartsEntry.getAsId().toStringForLogMessages(), model);
                        } else {
                            newTimeSlice = calculatePartlistentryTimeSlice(iPartsEntry, cachedData.productId, model, project);
                        }
                        if ((originalTimeSlice == null) || ((originalTimeSlice.start != newTimeSlice.start) || (originalTimeSlice.end != newTimeSlice.end))) {
                            ModelsForPartlistEntry modelsForPartlistEntry = resultMap.get(iPartsEntry.getAsId());
                            if (modelsForPartlistEntry == null) {
                                modelsForPartlistEntry = new ModelsForPartlistEntry(iPartsEntry);
                                resultMap.put(iPartsEntry.getAsId(), modelsForPartlistEntry);
                            }
                            modelsForPartlistEntry.addModel(model);
                        }
                    }

                    // Original-Werkseinsatzdaten am Stücklisteneintrag wiederherstellen
                    iPartsEntry.setFactoryDataForRetailUnfiltered(originalFactoryData);
                }
            }
        }
        return resultMap;
    }

    private AssemblyCacheData getAssemblyCacheData(EtkDataAssembly assembly) {
        AssemblyCacheData cachedData = assemblyCache.get(assembly.getAsId());
        if (cachedData == null) {
            cachedData = new AssemblyCacheData(assembly, project);
            assemblyCache.put(assembly.getAsId(), cachedData);
        }
        return cachedData;
    }

    /**
     * Prüfung 2: Vergleich vom Baumuster-Filter-Ergebnis
     * Es werden alle relevanten Positionsvarianten ermittelt und jeweils der Baumuster-Filter mit den bisherigen Werksdaten und
     * anschließend auch mit den neuen Werksdaten (Status freigegeben oder neu) durchgeführt. Wenn das Filterergebnis für alle
     * Einträge gleich bleibt, dann dürfen die Werksdaten freigegeben werden, sonst folgt Prüfung 3.
     *
     * @param modelsAndPartlistEntriesWithChanges Alle Stücklisteneinträge inkl. der Baumuster, bei denen sich in Prüfung 1
     *                                            Unterschiede ergeben haben
     * @param newFactoryData                      Die zu prüfenden neuen Werksdaten
     * @return Eine Liste von Stücklisteneinträgen, bei denen unterschiedliche Ergebnisse im Baumuster-Filter erzielt wurden
     */
    public Collection<iPartsDataPartListEntry> checkModelFilterResult(Map<PartListEntryId, ModelsForPartlistEntry> modelsAndPartlistEntriesWithChanges,
                                                                      iPartsDataFactoryData newFactoryData) {
        // Rückmeldedaten-Cache; wird verwendet, um relevante Rückmeldedaten den Werkseinsatzdaten zuzuordnen
        final iPartsResponseData responseData = iPartsResponseData.getInstance(project);
        // Ausreißer-Cache; wird verwendet, um relevante Ausreißer den Rückmeldedaten zuzuordnen
        final iPartsResponseSpikes responseSpikes = iPartsResponseSpikes.getInstance(project);

        String factoryDataBCTE = newFactoryData.getGUID();
        // Stücklisteneinträge mit Unterschieden merken (die ID als Key, damit keine doppelten Einträge entstehen)
        Map<PartListEntryId, iPartsDataPartListEntry> partlistEntriesWithDifferences = new HashMap<>();
        for (ModelsForPartlistEntry modelsForPartlistEntry : modelsAndPartlistEntriesWithChanges.values()) {
            iPartsDataPartListEntry partListEntry = modelsForPartlistEntry.partListEntry;

            // filterEntries sind alle Positionsvarianten, die auch der Baumuster-Filter berücksichtigen wird
            iPartsFilterPartsEntries filterEntries = iPartsFilterPartsEntries.getInstance(partListEntry);
            List<iPartsDataPartListEntry> filterEntriesAsSingleList = filterEntries.getAllPositionsVariantsAsSingleList();

            iPartsDataAssembly assembly = filterEntries.getPartListEntriesOwnerAssembly();
            if (assembly == null) {
                if (messageLog != null) {
                    messageLog.fireMessage(TranslationHandler.translate(
                            "!!Fehler bei der Anwendung des Baumuster-Filters für Stücklisteneintrag %1",
                            partListEntry.getAsId().toStringForLogMessages()), MessageLogType.tmlWarning);
                }
                continue;
            }

            // für alle relevanten Baumuster hier bereits die Datenkarten erzeugen
            List<AbstractDataCard> modelDatacards = new DwList<>(modelsForPartlistEntry.modelNumbers.size());
            for (String modelNumber : modelsForPartlistEntry.modelNumbers) {
                modelDatacards.add(AbstractDataCard.createModelDatacardByModelType(project, modelNumber));
            }

            // führe den Baumuster-Filter für jedes Baumuster und jeden relevanten Stücklisteneintrag durch und merke das Ergebnis
            Map<String, Boolean> modelFilterResult = new HashMap<>();
            for (AbstractDataCard dataCard : modelDatacards) {
                modelFilter.setCurrentDataCard(dataCard, project);
                for (iPartsDataPartListEntry entry : filterEntriesAsSingleList) {
                    String key = dataCard.getModelNo() + "|" + entry.getAsId().getKLfdnr();
                    modelFilterResult.put(key, modelFilter.checkFilter(entry));
                }
                // Werkseinsatzdaten wieder zurücksetzen weil diese im Baumuster Filter berechnet werden
                assembly.clearAllFactoryDataForRetailForPartList();
            }

            AssemblyCacheData cacheData = getAssemblyCacheData(assembly);

            // Alle Einträge aus den filterEntries heraussuchen, die den gleichen BCTE Schlüssel wie die neuen Werksdaten
            // haben, und nur für diese Einträge die Werksdaten inkl. Status NEU neu laden.
            // Hier werden die filterEntries und nicht die relevantEntries als Basis genommen, weil der Baumuster-Filter
            // die Einschränkung auf den gleichen Hotspot nicht macht.
            Map<PartListEntryId, iPartsFactoryData> allOriginalFactoryData = new HashMap<>();
            for (iPartsDataPartListEntry entry : filterEntriesAsSingleList) {
                String entryBCTE = entry.getFieldValue(FIELD_K_SOURCE_GUID);
                if (entryBCTE.equals(factoryDataBCTE)) {
                    // die originalen Werksdaten merken
                    iPartsFactoryData originalFactoryData = entry.getFactoryDataForRetailUnfiltered();
                    allOriginalFactoryData.put(entry.getAsId(), originalFactoryData);

                    iPartsFactoryData factoryDataForRetail = null;
                    if (originalFactoryData != null) {
                        // Werksdaten klonen und den Klon statt dem Original verwenden
                        factoryDataForRetail = originalFactoryData.cloneMe();
                    }

                    Map<String, iPartsFactoryData> currentFactoryDataMap = new HashMap<>();
                    currentFactoryDataMap.put(entry.getAsId().getKLfdnr(), factoryDataForRetail);

                    DBDataObjectAttributes attributes = newFactoryData.getAttributes().cloneMe(DBActionOrigin.FROM_DB);
                    attributes.addFields(entry.getAttributes(), DBActionOrigin.FROM_DB);

                    // für diesen Stücklisteneintrag Werksdaten neu laden inkl. Status NEU (Änderungen landen im Klon)
                    assembly.loadFactoryDataCallback(attributes, factoriesInstance, cacheData.seriesIdMap, releasedOrNewState,
                                                     currentFactoryDataMap, responseData, responseSpikes);

                    // Hier wird simuliert was passieren würde, wenn der aktuelle Datensatz freigegeben werden würde.
                    // D.h. es werden alle Datensätze aus der DB zum Werk auf nicht relevant gesetzt, unabhängig von ADAT oder Sequenz Nummer
                    // Die nicht relevanten Daten würden dann im Join nicht mit geladen, d.h. sie müssen hier gelöscht werden
                    simulateFactoryDataRelease(entry, currentFactoryDataMap.get(entry.getAsId().getKLfdnr()), newFactoryData.getAsId());
                }
            }

            // nochmal den Baumusterfilter für alle Baumuster und Stücklisteneinträge durchlaufen
            for (AbstractDataCard dataCard : modelDatacards) {
                // Immer den Filtercache leeren, da sonst evtl einfach das Ergebnis der vorigen Filterung mit den alten Daten verwendet wird
                modelFilter.clearCacheData();
                modelFilter.setCurrentDataCard(dataCard, project);
                for (iPartsDataPartListEntry visibleEntry : filterEntriesAsSingleList) {
                    boolean partListEntryValidForModel = modelFilter.checkFilter(visibleEntry);
                    String key = dataCard.getModelNo() + "|" + visibleEntry.getAsId().getKLfdnr();
                    Boolean oldFilterResult = modelFilterResult.get(key);
                    if ((oldFilterResult == null) || (partListEntryValidForModel != oldFilterResult)) {
                        // wenn sich das Filterergebnis unterscheidet den Stücklisteneintrag merken
                        partlistEntriesWithDifferences.put(visibleEntry.getAsId(), visibleEntry);
                        if (oldFilterResult == null) {
                            Logger.log(iPartsImportPlugin.LOG_CHANNEL_FACTORYDATA_AUTO_RELEASE, LogType.ERROR, "Error while executing model filter for partListEntry: "
                                                                                                               + visibleEntry.getAsId().toStringForLogMessages());
                        } else {
                            logMessageToLogChannel(newFactoryData, "Different model filter result for partListEntry ("
                                                                   + visibleEntry.getAsId().toStringForLogMessages() + "), model \""
                                                                   + dataCard.getModelNo() + "\"");
                        }
                    }
                }
                // Werkseinsatzdaten wieder zurücksetzen, weil diese im Baumuster-Filter berechnet werden
                assembly.clearAllFactoryDataForRetailForPartList();
            }

            // zum Schluss noch die originalen Werkseinsatzdaten wiederherstellen
            for (iPartsDataPartListEntry entry : filterEntriesAsSingleList) {
                iPartsFactoryData originalFactoryData = allOriginalFactoryData.get(entry.getAsId());
                entry.setFactoryDataForRetailUnfiltered(originalFactoryData);
            }
        }
        return partlistEntriesWithDifferences.values();
    }

    private void simulateFactoryDataRelease(iPartsDataPartListEntry entry, iPartsFactoryData currentFactoryData, iPartsFactoryDataId newFactoryDataId) {
        if (currentFactoryData == null) {
            return;
        }
        String factory = newFactoryDataId.getFactory();
        List<iPartsFactoryData.DataForFactory> dataForFactory = currentFactoryData.getDataForFactory(factory);
        if (dataForFactory != null) {
            dataForFactory.removeIf(dataForFactory1 -> !dataForFactory1.factoryDataId.equals(newFactoryDataId));

            if (dataForFactory.isEmpty()) {
                currentFactoryData.removeDataForFactory(factory);
            }
        }
        entry.setFactoryDataForRetailUnfiltered(currentFactoryData);
    }

    private void addColorFactoryDataToMap(Map<IdWithType, List<EtkDataObject>> dataMap, iPartsDataColorTableFactory colorFactoryData) {
        iPartsColorTableFactoryId id = colorFactoryData.getAsId();
        iPartsColorTableFactoryId colorTableFactoryIdWithoutAdat = new iPartsColorTableFactoryId(id.getTableId(), id.getPos(), id.getFactory(),
                                                                                                 "", id.getDataId(), id.getSdata());
        List<EtkDataObject> colorTableFactoryHistoryList = dataMap.get(colorTableFactoryIdWithoutAdat);
        if (colorTableFactoryHistoryList == null) {
            colorTableFactoryHistoryList = new DwList<>();
            dataMap.put(colorTableFactoryIdWithoutAdat, colorTableFactoryHistoryList);
        }
        colorTableFactoryHistoryList.add(colorFactoryData);
    }

    private void addFactoryDataToMap(Map<IdWithType, List<EtkDataObject>> dataMap, iPartsDataFactoryData factoryData) {
        if ((dataMap != null) && (factoryData != null)) {
            iPartsFactoryDataId id = factoryData.getAsId();
            iPartsFactoryDataId keyId = new iPartsFactoryDataId(id.getGuid(), id.getFactory(), id.getSplitAttribute(),
                                                                "", id.getDataId(), "");
            List<EtkDataObject> factoryDataList = dataMap.computeIfAbsent(keyId, k -> new DwList<>());
            factoryDataList.add(factoryData);
        }
    }

    private void addFactoryDataGroupedByFactoryToMap(Map<IdWithType, List<EtkDataObject>> dataMap, iPartsDataFactoryData factoryData) {
        if ((dataMap != null) && (factoryData != null)) {
            iPartsFactoryDataId id = factoryData.getAsId();
            iPartsFactoryDataId keyId = new iPartsFactoryDataId(id.getGuid(), "", id.getSplitAttribute(),
                                                                "", id.getDataId(), "");
            List<EtkDataObject> factoryDataForKey = dataMap.computeIfAbsent(keyId, f -> new DwList<>());
            factoryDataForKey.add(factoryData);

        }
    }

    private enum PreconditionResult {
        NO_AUTOMATION_POSSIBLE,
        AUTOMATION_POSSIBLE,
        CHECK_PEM_DATES
    }

    /**
     * Erzeugt eine Dummy-Stückliste mit einer Stücklistenposition um bei der Bestimmung von Teil zu Farbtabellen Werksdaten
     * die benötigten Stücklistenwerte zu haben.
     *
     * @param partNo
     * @param series
     * @return
     */
    private DBDataObjectList<EtkDataPartListEntry> createDummyPartList(String partNo, String series) {
        DBDataObjectList<EtkDataPartListEntry> partList = new DBDataObjectList<>();
        EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(project, new PartListEntryId("", "", "00001"));
        partListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        partListEntry.setFieldValue(FIELD_K_MATNR, partNo, DBActionOrigin.FROM_DB);

        HmMSmId dummyHmMSMId = new HmMSmId(series, "XX", "XX", "XX");
        String sourceContext = EditConstructionToRetailHelper.createSourceContext(iPartsEntrySourceType.DIALOG, dummyHmMSMId);
        partListEntry.setFieldValue(FIELD_K_SOURCE_CONTEXT, sourceContext, DBActionOrigin.FROM_DB);
        partListEntry.setFieldValue(FIELD_K_SOURCE_TYPE, iPartsEntrySourceType.DIALOG.getDbValue(), DBActionOrigin.FROM_DB);
        partList.add(partListEntry, DBActionOrigin.FROM_DB);
        return partList;
    }


    /**
     * Berechnet, ob die Farbe dieses Werkseinsatzdatensatzes für das übergebene Produkt mit und ohne den neuen Datensatz
     * potentiell gültig werden kann. Dann wird geprüft ob das Ergebnis mit und ohne den neuen Datensatz identisch ist.
     *
     * @param newColorTableContentFactoryData
     * @param productId
     * @param productToColorTableMap
     * @param responseData
     * @param responseSpikes
     * @return
     */
    private boolean checkColorTableContentValidForProduct(iPartsDataColorTableFactory newColorTableContentFactoryData, iPartsProductId productId,
                                                          Map<iPartsProductId, iPartsColorTable> productToColorTableMap,
                                                          iPartsResponseData responseData, iPartsResponseSpikes responseSpikes) {

        String colorTableId = newColorTableContentFactoryData.getAsId().getTableId();
        Set<String> colorTableIdAsSet = new HashSet<>();
        colorTableIdAsSet.add(colorTableId);

        iPartsDataAssembly dummyAssembly = getDummyAssemblyForProduct(productId);

        iPartsColorTableContentId colorTableContentId = new iPartsColorTableContentId(newColorTableContentFactoryData.getAsId());

        // Zustand mit bestehenden Daten
        iPartsColorTable oldColorTableForProduct = dummyAssembly.createGenericColorTableForRetail(true, colorTableIdAsSet);
        // Farbtabelle zur Farbtabellen ID für die bestehenden Daten
        iPartsColorTable.ColorTableContent oldColorTableContentForProduct = oldColorTableForProduct.getColorTableContent(colorTableId, colorTableContentId);

        // Falls colorTableContentForProduct null ist, ist die Farbe auch ohne Modelfilter für das aktuelle Produkt nicht
        // gültig für den Retail (z.B weil es keine für das Produkt gültigen Werksdaten gibt)
        boolean oldColorValidForProduct = (oldColorTableContentForProduct != null);

        // Zustand mit neuem Datensatz. Hierfür wird der bestehende Zustand erzeugt und der neue Datensatz manuell hinzugefügt
        iPartsColorTable newColorTableForProduct = dummyAssembly.createGenericColorTableForRetail(true, colorTableIdAsSet);
        // Neue Werksdatensatz hinzufügen
        dummyAssembly.loadColorTableContentDataCallback(newColorTableContentFactoryData.getAttributes(), true,
                                                        new HashMap<>(), newColorTableForProduct, releasedOrNewState,
                                                        responseData, responseSpikes);
        // Farbtabelle zur Farbtabellen ID für die bestehenden Daten plus neuen Datensatz
        iPartsColorTable.ColorTableContent newColorTableContentForProduct = newColorTableForProduct.getColorTableContent(colorTableId, colorTableContentId);

        boolean newColorValidForProduct = (newColorTableContentForProduct != null);

        if (oldColorValidForProduct != newColorValidForProduct) {
            logMessageToLogChannel(newColorTableContentFactoryData, "Different validity result for color ("
                                                                    + colorTableContentId.toStringForLogMessages()
                                                                    + "), product \"" + productId.getProductNumber() + "\"");
            return false;
        } else if (newColorValidForProduct) {
            // Beide Ergebnisse sind identisch und haben ergeben, dass die Farbe potentiell gültig für das Produkt sein kann
            productToColorTableMap.put(productId, oldColorTableForProduct);
            return true;
        }
        // Der verbleibende Fall ist folgender:
        // Ist eine Variantentabelle für ein Produkt bereits vor der Filterung mit den alten und neuen Werksdaten ungültig,
        // können die Werksdaten potentiell automatisiert werden, sofern die Prüfungen der anderen Produkte ebenfalls positiv sind.
        // Für dieses Produkt sind keine weiteren Prüfungen erforderlich.
        return true;
    }

    /**
     * Liefert zurück, ob sich das Filterergebnis mit dem neuen Datensatz von Filterergebnis ohne neuen Datensatz unterscheidet
     *
     * @param newColorTableToPartFactoryData
     * @param productId
     * @param responseData
     * @param responseSpikes
     * @return
     */
    private boolean checkModelFilterResultForColorTableToPart(iPartsDataColorTableFactory newColorTableToPartFactoryData,
                                                              iPartsProductId productId, iPartsResponseData responseData,
                                                              iPartsResponseSpikes responseSpikes) {
        String colorTableId = newColorTableToPartFactoryData.getAsId().getTableId();
        // Dummy Assembly, um die spezifischen Assembly-Methoden zu nutzen
        iPartsDataAssembly dummyAssembly = getDummyAssemblyForProduct(productId);
        // Map von laufender Nummer der Stückliste (nur für gefundene Farbvariantentabellen) auf Baureihe
        final Map<String, iPartsSeriesId> seriesIdMap = new HashMap<>();
        // Sets, die für den Methodenaufruf benötigt aber hier nicht verwendet werden
        final Set<String> allColorTableIds = new LinkedHashSet<>();
        final Set<String> partlistEntriesWithColoredPart = new HashSet<>();
        final Set<String> partlistEntriesWithUnfilteredColorTables = new HashSet<>();

        // Teilenummer für die Teil zu Farbtabellen-Beziehung
        String partNo = newColorTableToPartFactoryData.getFieldValue(FIELD_DCTP_PART);
        // Baureihe für den Baureihen-Check
        String series = ColorTableHelper.extractSeriesNumberFromTableId(colorTableId);
        // Dummy Stückliste um Stücklistenspezifische Methoden aufzurufen
        DBDataObjectList<EtkDataPartListEntry> partList = createDummyPartList(partNo, series);

        // Berechnete Werksdaten ohne neuen Eintrag
        final Map<String, iPartsColorTable> oldAllColorTableMap = dummyAssembly.getPartListEntryToColorTableMapForPartNo(partList, partNo, true, seriesIdMap, allColorTableIds,
                                                                                                                         partlistEntriesWithColoredPart, partlistEntriesWithUnfilteredColorTables);
        // Existiert ein Eintrag für die Farbtabelle
        boolean oldColorTableValidForProduct = colorTableToPartExists(oldAllColorTableMap, partList, colorTableId);

        // Berechnete Werksdaten mit neuem Eintrag
        final Map<String, iPartsColorTable> newAllColorTableMap = dummyAssembly.getPartListEntryToColorTableMapForPartNo(partList, partNo, true, seriesIdMap, allColorTableIds,
                                                                                                                         partlistEntriesWithColoredPart, partlistEntriesWithUnfilteredColorTables);

        // Prüfen, ob neuer Eintrag überhaupt ubernommen werden würde (Checks in der Stückliste selber)
        if (!dummyAssembly.checkDialogPartListForColorTableData(true, iPartsImportDataOrigin.DIALOG.getOrigin(), newColorTableToPartFactoryData.getAttributes(), new iPartsSeriesId(series))) {
            return true;
        }
        // Wenn die Stückliste den neuen Eintrag verarbeiten würde, dann muss der neue Eintrag für den späteren Vegrleich übernommen werden
        dummyAssembly.calcColorTableFactoryData(newAllColorTableMap, allColorTableIds, partList.get(0).getAsId().getKLfdnr(),
                                                newColorTableToPartFactoryData.getAttributes(), null, new iPartsSeriesId(series),
                                                true, responseData, responseSpikes);
        // Existiert ein Eintrag für die Farbtabelle nachdem der neue Eintrag in die Berechnung einbezogen wurde
        boolean newColorTableValidForProduct = colorTableToPartExists(newAllColorTableMap, partList, colorTableId);

        // Beide Zustände haben keine Werksdaten zur Teil zu Farbtabelle Beziehung -> Kann freigegeben werden
        if (!oldColorTableValidForProduct && !newColorTableValidForProduct) {
            return true;
        }

        // Ergebnis für den Zustand ohne neues Werksdatenobjekt berechnen
        boolean oldValidForProduct = isColorTableValidForProduct(oldAllColorTableMap, colorTableId, partList, dummyAssembly);
        // Ergebnis für den Zustand mit neuem Werksdatenobjekt berechnen
        boolean newValidForProduct = isColorTableValidForProduct(newAllColorTableMap, colorTableId, partList, dummyAssembly);

        // Sie beide Ergebnisse gleich, kann der Datensatz für dieses Produkt freigegeben werden
        return oldValidForProduct == newValidForProduct;


    }

    /**
     * Überprüft, ob die Werksdaten zu den Teil zu Farbtabellen Beziehungen in der übergebenen Farbtabelle bezüglich
     * ihrer Baumustergültigkeit valide sind
     *
     * @param allColorTableMap
     * @param colorTableId
     * @param partList
     * @param dummyAssembly
     * @return
     */
    private boolean isColorTableValidForProduct(Map<String, iPartsColorTable> allColorTableMap, String colorTableId, DBDataObjectList<EtkDataPartListEntry> partList, iPartsDataAssembly dummyAssembly) {
        Map<iPartsColorTableToPartId, iPartsColorTable.ColorTableToPart> oldColorTableToPart = getColorTableToPart(allColorTableMap, colorTableId, partList);
        if (oldColorTableToPart != null) {
            for (iPartsColorTable.ColorTableToPart colorTableToPart : oldColorTableToPart.values()) {
                if (iPartsFilter.isColorTableValidForModelTimeSlice(dummyAssembly, colorTableToPart, null)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Liefert alle Werksdaten zur Teil zu Farbtabellen Beziehung innerhalb der übergebenen Farbtabelle zurück
     *
     * @param allColorTableMap
     * @param colorTableId
     * @param partList
     * @return
     */
    private Map<iPartsColorTableToPartId, iPartsColorTable.ColorTableToPart> getColorTableToPart(Map<String, iPartsColorTable> allColorTableMap, String colorTableId, DBDataObjectList<EtkDataPartListEntry> partList) {
        if (!allColorTableMap.isEmpty()) {
            iPartsColorTable colorTable = allColorTableMap.get(partList.get(0).getAsId().getKLfdnr());
            if (colorTable != null) {
                if (colorTable.getColorTablesMap().containsKey(colorTableId)) {
                    return colorTable.getColorTablesMap().get(colorTableId).colorTableToPartsMap;
                }

            }
        }
        return null;
    }

    /**
     * Überprüft, ob zu übergebenen Farbtabellen-ID ein Eintrag existiert
     *
     * @param allColorTableMap
     * @param partList
     * @param colorTableId
     * @return
     */
    private boolean colorTableToPartExists(Map<String, iPartsColorTable> allColorTableMap, DBDataObjectList<EtkDataPartListEntry> partList, String colorTableId) {
        boolean colorTableFound = false;
        if (!allColorTableMap.isEmpty()) {
            iPartsColorTable colorTable = allColorTableMap.get(partList.get(0).getAsId().getKLfdnr());
            if (colorTable != null) {
                iPartsColorTable.ColorTable colorTableToPart = colorTable.getColorTablesMap().get(colorTableId);
                colorTableFound = colorTableToPart != null;
            }
        }
        return colorTableFound;
    }

    /**
     * Berechnet das Ergebnis der Baumuster-Filterung für die Farbe des übergebenen Werkseinsatzdatensatzes mit
     * und ohne diesen neuen Datensatz. Die Filterung wird für jedes Baumuster zum Produkt gemacht und geprüft,
     * ob das Ergebnis mit und ohne den neuen Datensatz identisch ist.
     *
     * @param newColorTableContentFactoryData
     * @param productId
     * @param colorTableForProduct
     * @param responseData
     * @param responseSpikes
     * @return {@code true}, falls das vorherige und das neue Ergebnis der Baumuster-Filterung übereinstimmen
     */
    private boolean checkModelFilterResultForColor(iPartsDataColorTableFactory newColorTableContentFactoryData, iPartsProductId productId,
                                                   iPartsColorTable colorTableForProduct, iPartsResponseData responseData,
                                                   iPartsResponseSpikes responseSpikes) {

        String colorTableId = newColorTableContentFactoryData.getAsId().getTableId();
        iPartsColorTableContentId colorTableContentId = new iPartsColorTableContentId(newColorTableContentFactoryData.getAsId());

        Set<String> modelsOfProduct = iPartsProduct.getInstance(project, productId).getModelNumbers(project);

        iPartsDataAssembly dummyAssembly = getDummyAssemblyForProduct(productId);

        for (String modelNumber : modelsOfProduct) {

            iPartsColorTable.ColorTableContent oldColorTableContentFiltered = modelFilter.checkModelFilterForColor(project, dummyAssembly,
                                                                                                                   colorTableForProduct.getColorTableContents(colorTableId),
                                                                                                                   modelNumber);
            boolean oldColorValidForModel = (oldColorTableContentFiltered != null);

            iPartsColorTable.ColorTableContent colorTableContent = colorTableForProduct.getColorTableContent(colorTableId, colorTableContentId);
            // die originalen Werksdaten merken
            iPartsColorFactoryDataForRetail originalFactoryData = colorTableContent.getFactoryData();
            // Werksdaten klonen und den Klon statt dem Original verwenden
            addNewColorTableContentFactoryData(newColorTableContentFactoryData, colorTableContent, dummyAssembly, responseData,
                                               responseSpikes);

            iPartsColorTable.ColorTableContent newColorTableContentFiltered = modelFilter.checkModelFilterForColor(project, dummyAssembly,
                                                                                                                   colorTableForProduct.getColorTableContents(colorTableId),
                                                                                                                   modelNumber);
            boolean newColorValidForModel = (newColorTableContentFiltered != null);

            // Ursprüngliche Daten wiederherstellen
            colorTableContent.setFactoryData(originalFactoryData);

            if (oldColorValidForModel != newColorValidForModel) {
                logMessageToLogChannel(newColorTableContentFactoryData, "Different model filter result for color ("
                                                                        + colorTableContentId.toStringForLogMessages()
                                                                        + "), model \"" + modelNumber + "\"");
                return false;
            }

            logMessageToLogChannel(newColorTableContentFactoryData, "Product: " + productId.getProductNumber()
                                                                    + " - Model: " + modelNumber + " Same Model filter result -> continue");

            if (oldColorValidForModel && newColorValidForModel) { // zweite Bedingung hier nochmal zur besseren Lesbarkeit mit aufgenommen
                if (!oldColorTableContentFiltered.isEvalPemFrom() && !oldColorTableContentFiltered.isEvalPemTo()
                    && !newColorTableContentFiltered.isEvalPemFrom() && !newColorTableContentFiltered.isEvalPemTo()) {
                    // Pem-Flags sind nicht gesetzt und haben sich auch nicht geändert
                    logMessageToLogChannel(newColorTableContentFactoryData, "Product: " + productId.getProductNumber()
                                                                            + " - Model: " + modelNumber + " All PEM flags false -> check passed");
                    continue;
                }
                Map<String, Long> oldCollectedDateFromForFactory = new HashMap<>();
                Map<String, Long> oldCollectedDateToForFactory = new HashMap<>();
                getFactoryDateFromAndTo(oldCollectedDateFromForFactory, oldCollectedDateToForFactory, oldColorTableContentFiltered);
                Map<String, Long> newCollectedDateFromForFactory = new HashMap<>();
                Map<String, Long> newCollectedDateToForFactory = new HashMap<>();
                getFactoryDateFromAndTo(newCollectedDateFromForFactory, newCollectedDateToForFactory, newColorTableContentFiltered);

                if (!oldCollectedDateFromForFactory.equals(newCollectedDateFromForFactory)
                    || !oldCollectedDateToForFactory.equals(newCollectedDateToForFactory)) {
                    logMessageToLogChannel(newColorTableContentFactoryData, "Product: " + productId.getProductNumber()
                                                                            + " - Model: " + modelNumber + "PEM flags modified and different date from/to -> check failed");
                    return false;
                }
            }
        }

        logMessageToLogChannel(newColorTableContentFactoryData, "Model filter and PEM flags match -> check passed");
        return true;
    }

    /**
     * Berechnet das AB-Datum und BIS-Datum der Werke für Werkseinsatzdaten von Farbtabelleninhalten.
     *
     * @param collectedDateFromForFactory
     * @param collectedDateToForFactory
     */
    private void getFactoryDateFromAndTo(Map<String, Long> collectedDateFromForFactory, Map<String, Long> collectedDateToForFactory,
                                         iPartsColorTable.ColorTableContent colorTableContent) {
        for (Map.Entry<String, List<iPartsColorFactoryDataForRetail.DataForFactory>> factoryData : colorTableContent.getFactoryData().getFactoryDataMap().entrySet()) {
            // Minimalen PEM-Termin ab bzw. maximalen PEM-Termin bis für das Werk bestimmen
            long minDateFrom = Long.MAX_VALUE;
            long maxDateTo = Long.MIN_VALUE;
            String factory = factoryData.getKey();
            List<iPartsColorFactoryDataForRetail.DataForFactory> dataForFactories = factoryData.getValue();
            for (iPartsColorFactoryDataForRetail.DataForFactory dataForFactory : dataForFactories) {
                minDateFrom = Math.min(minDateFrom, dataForFactory.dateFrom);
                maxDateTo = Math.max(maxDateTo, dataForFactory.getDateToWithInfinity());
            }

            if (colorTableContent.isEvalPemFrom()) {
                collectedDateFromForFactory.put(factory, minDateFrom);
            }

            if (colorTableContent.isEvalPemTo()) {
                collectedDateToForFactory.put(factory, maxDateTo);
            }
        }
    }

    private List<? extends EtkDataObject> getFactoryDataHistoryList(EtkDataObject factoryData, boolean isColorFactoryData) {
        if (!isColorFactoryData) {
            iPartsFactoryDataId id = ((iPartsDataFactoryData)factoryData).getAsId();
            return iPartsDataFactoryDataList.loadAfterSalesFactoryDataListWithHistoryData(project, id).getAsList();
        } else {
            iPartsColorTableFactoryId id = ((iPartsDataColorTableFactory)factoryData).getAsId();
            return iPartsDataColorTableFactoryList.loadAllColorTableFactoryForColorTableFactoryId(project, id).getAsList();
        }
    }

    private void addNewColorTableContentFactoryData(iPartsDataColorTableFactory newColorTableContentFactoryData, iPartsColorTable.ColorTableContent currentColorTableContent,
                                                    iPartsDataAssembly dummyAssembly, iPartsResponseData responseData, iPartsResponseSpikes responseSpikes) {
        currentColorTableContent.setFactoryData(currentColorTableContent.getFactoryData().cloneMe());
        // für diesen Farbtabelleninhalt die Werksdaten mit Status NEU dazumischen (Änderungen landen im Klon)
        currentColorTableContent.setFactoryData(dummyAssembly.getColorFactoryDataForRetail(currentColorTableContent.getFactoryData(),
                                                                                           newColorTableContentFactoryData.getAttributes(),
                                                                                           newColorTableContentFactoryData.getAsId().getTableId(),
                                                                                           null, null,
                                                                                           iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS,
                                                                                           responseData, responseSpikes));
    }

    private iPartsDataAssembly getDummyAssemblyForProduct(iPartsProductId productId) {
        iPartsDataAssembly dummyAssembly = (iPartsDataAssembly)EtkDataObjectFactory.createDataAssembly(project, new iPartsAssemblyId("", ""), false);
        // Dummy-Assembly muss nichts wissen AUSSER das Produkt das wir gerade anschauen, weil davon z.B. abhängt welche
        // Werke zum Produkt und damit für die Farben gültig sind.
        dummyAssembly.setProductIdFromModuleUsage(productId);
        // ... und der Dokumentationstyp muss DIALOG sein, da davon z.b. abhängt, wie viele Stände der Werksdaten geladen werden
        dummyAssembly.setDocumentationType(iPartsDocumentationType.DIALOG);
        return dummyAssembly;
    }

    /**
     * Holt die eingestellte Terminverschiebung in Tagen
     *
     * @return
     */
    private int getDateThreshold() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsImportPlugin.CONFIG_IMPORT_AUTO_RELEASE_DATE_THRESHOLD);
    }

    /**
     * Check, ob die Prüfung auf Basis der Terminverschiebung gemacht werden soll
     *
     * @return
     */
    public boolean isThresholdCheckActive() {
        return getDateThreshold() > 0;
    }

    private static class TimeSlice {

        private long start;
        private long end;

        TimeSlice(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

    private class FactoryThresholdData {

        private GenericEtkDataObjectList dataWithReleasedState;
        private String pemaField;
        private String pembField;
        private String pemtaField;
        private String pemtbField;
        private EtkDataObject newFactoryData;

        public FactoryThresholdData(EtkDataObject factoryData) {
            initObject(factoryData);
        }

        private void initObject(EtkDataObject factoryData) {
            this.newFactoryData = factoryData;
            dataWithReleasedState = new GenericEtkDataObjectList();
            if (factoryData instanceof iPartsDataFactoryData) {
                // Alle freigegebenen Werksdaten zum gleichen Schlüssel - außer ADAT und Sequenznummer - laden. Sortiert
                // werden die Daten von der DB nach ADAT und Sequenznummer
                this.dataWithReleasedState.addAll(iPartsDataFactoryDataList.loadReleasedFactoryData(project, ((iPartsDataFactoryData)factoryData).getAsId()), DBActionOrigin.FROM_DB);
                this.pemaField = FIELD_DFD_PEMA;
                this.pembField = FIELD_DFD_PEMB;
                this.pemtaField = FIELD_DFD_PEMTA;
                this.pemtbField = FIELD_DFD_PEMTB;
            } else if (factoryData instanceof iPartsDataColorTableFactory) {
                dataWithReleasedState.addAll(iPartsDataColorTableFactoryList.loadAllColorTableFactoryForColorTableFactoryId(project, ((iPartsDataColorTableFactory)factoryData).getAsId(), iPartsDataReleaseState.RELEASED), DBActionOrigin.FROM_DB);
                this.pemaField = FIELD_DCCF_PEMA;
                this.pembField = FIELD_DCCF_PEMB;
                this.pemtaField = FIELD_DCCF_PEMTA;
                this.pemtbField = FIELD_DCCF_PEMTB;
            }
        }

        public boolean hasReleasedData() {
            return !dataWithReleasedState.isEmpty();
        }

        public EtkDataObject getCurrentReleasedData() {
            return (EtkDataObject)dataWithReleasedState.getLast();
        }

        public String getPemaField() {
            return pemaField;
        }

        public String getPembField() {
            return pembField;
        }

        public String getPemtaField() {
            return pemtaField;
        }

        public String getPemtbField() {
            return pemtbField;
        }

        public EtkDataObject getNewFactoryData() {
            return newFactoryData;
        }
    }


    public static class ModelsForPartlistEntry {

        private Set<String> modelNumbers = new TreeSet<>();
        private iPartsDataPartListEntry partListEntry;

        private ModelsForPartlistEntry(iPartsDataPartListEntry partListEntry) {
            this.partListEntry = partListEntry;
        }

        private void addModel(String modelNumber) {
            modelNumbers.add(modelNumber);
        }

        public iPartsDataPartListEntry getPartListEntry() {
            return partListEntry;
        }

        public Set<String> getModelNumbers() {
            return modelNumbers;
        }
    }


    private static class AssemblyCacheData {

        private iPartsDataAssembly assembly;
        private EtkProject project;
        private iPartsProductId productId;
        private Set<String> modelNumbers;
        private Map<String, iPartsSeriesId> seriesIdMap = new HashMap<>();

        AssemblyCacheData(EtkDataAssembly assembly, EtkProject project) {
            this.project = project;
            EtkDataAssembly realAssembly = assembly.getLastHiddenSingleSubAssemblyOrThis(null);
            if (realAssembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)realAssembly;
                this.assembly = iPartsAssembly;
                productId = iPartsAssembly.getProductIdFromModuleUsage();
            }
        }

        public Set<String> getModelNumbers() {
            if ((modelNumbers == null) && (productId != null)) {
                modelNumbers = iPartsProduct.getInstance(project, productId).getModelNumbers(project);
            }
            return modelNumbers;
        }
    }

    private static class FactoryDataForAutoRelease {

        private final Map<IdWithType, List<EtkDataObject>> dataForFollowingChecks = new TreeMap<>();
        private final Map<IdWithType, List<EtkDataObject>> dataForCurrentCheck = new TreeMap<>();

        public Map<IdWithType, List<EtkDataObject>> getDataForFollowingChecks() {
            return dataForFollowingChecks;
        }

        public Map<IdWithType, List<EtkDataObject>> getDataForCurrentCheck() {
            return dataForCurrentCheck;
        }

        public void addFactoryDataForCurrentCheck(IdWithType groupedFactoryDataId, EtkDataObject factoryData) {
            List<EtkDataObject> dataForGroupedFactoryId = dataForCurrentCheck.computeIfAbsent(groupedFactoryDataId, f -> new DwList<>());
            dataForGroupedFactoryId.add(factoryData);
        }

        public void addFactoryDataForFollowingCheck(IdWithType groupedFactoryDataId, EtkDataObject factoryData) {
            List<EtkDataObject> dataForGroupedFactoryId = dataForFollowingChecks.computeIfAbsent(groupedFactoryDataId, f -> new DwList<>());
            dataForGroupedFactoryId.add(factoryData);
        }

        public boolean hasDataForFollowingChecks() {
            return !dataForFollowingChecks.isEmpty();
        }
    }


}
