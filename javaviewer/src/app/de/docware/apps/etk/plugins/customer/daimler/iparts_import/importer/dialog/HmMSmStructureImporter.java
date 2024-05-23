/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.iPartsDataHmMSm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.iPartsDataHmMSmDesc;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Importer für die HMMSM Struktur via XML Datei
 */
public class HmMSmStructureImporter extends AbstractDIALOGDataImporter implements iPartsConst {

    public static final String DIALOG_TABLENAME = "KGVZ";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;

    public static final String KGVZ_BRRA = "KGVZ_BRRA";
    public static final String KGVZ_SPS = "KGVZ_SPS";
    public static final String KGVZ_SDATA = "KGVZ_SDATA";
    public static final String KGVZ_SDATB = "KGVZ_SDATB";
    public static final String KGVZ_BEN = "KGVZ_BEN";
    public static final String KGVZ_VZWK = "KGVZ_VZWK";
    public static final String KGVZ_GHM = "KGVZ_GHM";
    public static final String KGVZ_GHS = "KGVZ_GHS";
    public static final String KGVZ_KGU = "KGVZ_KGU";
    public static final String KGVZ_PRI = "KGVZ_PRI";
    public static final String KGVZ_VERTRIEB_KZ = "KGVZ_VERTRIEB_KZ";

    private HashMap<String, String> mappingHMMSMDesc;
    private String[] primaryKeysHMMSMImportData;
    private Map<HmMSmId, iPartsDataHmMSm> hmMSmDatasets;
    private Map<HmMSmId, iPartsDataHmMSmDesc> hmMSmDescDatasets;

    private boolean importToDB = true;
    private boolean doBufferSave = true;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public HmMSmStructureImporter(EtkProject project) {
        super(project, "!!DIALOG-HM/M/SM Struktur (KGVZ)",
              new FilesImporterFileListType(TABLE_DA_HMMSMDESC, DD_HMMSM_STRUCTURE, false, false, true, new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysHMMSMImportData = new String[]{ KGVZ_BRRA };

        mappingHMMSMDesc = new HashMap<>();
        mappingHMMSMDesc.put(FIELD_DH_DATA, KGVZ_SDATA);
        mappingHMMSMDesc.put(FIELD_DH_DATB, KGVZ_SDATB);
        mappingHMMSMDesc.put(FIELD_DH_DESC, KGVZ_BEN);
        mappingHMMSMDesc.put(FIELD_DH_FACTORIES, KGVZ_VZWK);
        mappingHMMSMDesc.put(FIELD_DH_KGU, KGVZ_KGU);
        mappingHMMSMDesc.put(FIELD_DH_PRI, KGVZ_PRI);
        mappingHMMSMDesc.put(FIELD_DH_SALES_KZ, KGVZ_VERTRIEB_KZ);
        mappingHMMSMDesc.put(FIELD_DH_GHM, KGVZ_GHM);
        mappingHMMSMDesc.put(FIELD_DH_GHS, KGVZ_GHS);

    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysHMMSMImportData);
        importer.setMustHaveData(StrUtils.mergeArrays(primaryKeysHMMSMImportData, KGVZ_SPS, KGVZ_SDATA));

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(IMPORT_TABLENAME)
                   || importer.getTableNames().get(0).equals(DIALOG_TABLENAME);
        }
        return false;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        progressMessageType = ProgressMessageType.READING;
        hmMSmDatasets = new LinkedHashMap<>();
        hmMSmDescDatasets = new LinkedHashMap<>();
        setBufferedSave(doBufferSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        HMMSMImportHelper hmMSmDescHelper = new HMMSMImportHelper(getProject(), mappingHMMSMDesc, TABLE_DA_HMMSMDESC);
        iPartsDIALOGLanguageDefs langDef = iPartsDIALOGLanguageDefs.getType(hmMSmDescHelper.handleValueOfSpecialField(KGVZ_SPS, importRec));
        if (langDef == iPartsDIALOGLanguageDefs.DIALOG_UNKNOWN) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (ungültige Sprachkennung: %2)", String.valueOf(recordNo),
                                                        importRec.get(KGVZ_SPS)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        // BRRA hat vorne die Baugruppe und hinten (getrennt durch Leerzeichen) die HMMSM Struktur, daher muss size = 2 sein
        String[] seriesAndHmMSm = hmMSmDescHelper.handleValueOfSpecialField(KGVZ_BRRA, importRec).split("\\s+");
        if (seriesAndHmMSm.length != 2) {
            cancelImport(translateForLog("!!Record %1 fehlerhaft (ungültiges Format für Baureihe und HM/M/SM Struktur: %2)",
                                         String.valueOf(recordNo), importRec.get(KGVZ_BRRA)));
            return;
        }
        String hmMSmStructure = seriesAndHmMSm[1].trim();
        // Es können auch Gruppen auftauchen, z.B. ARB -> nicht abbrechen sondern überspringen
        // Es handelt sich um eine Code Gruppe, wenn die Länge der HM/M/SM Struktur ungerade ist
        if ((hmMSmStructure.length() % 2) == 1) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (ungültige HM/M/SM Struktur: %2)",
                                                        String.valueOf(recordNo), hmMSmStructure),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        String series = seriesAndHmMSm[0].trim();
        // Wenn die Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
        if (!hmMSmDescHelper.checkImportRelevanceForSeries(series, getInvalidSeriesSet(), this)) {
            return;
        }

        HmMSmId hmMSmId = HmMSmId.getIdFromRaster(series, seriesAndHmMSm[1].trim());
        // Ab DAIMLER-9404 können im HM-Knoten Buchstaben vorkommen, M und SM Knoten dürfen weiterhin nur Zahlen beinhalten!
        if (!hmMSmDescHelper.areMSmNodesOnlyDigits(hmMSmId, recordNo)) {
            return;
        }
        // Wenn eine HMMSM Struktur eingelesen wurde, dann muss mindestens der HM Knoten existieren
        if ((hmMSmId == null) || hmMSmId.isEmpty() || hmMSmId.getSeries().isEmpty()) {
            cancelImport(translateForLog("!!Record %1 fehlerhaft (leere oder unbekannte HM/M/SM Struktur: %2)",
                                         String.valueOf(recordNo), importRec.get(KGVZ_BRRA)));
            reduceRecordCount();
            return;
        }
        // Erzeugen der ID und füllen des HMMSMDESC DataObjects
        iPartsDataHmMSmDesc dataHmMSmDesc = hmMSmDescDatasets.get(hmMSmId);
        if (dataHmMSmDesc == null) {
            dataHmMSmDesc = new iPartsDataHmMSmDesc(getProject(), hmMSmId);
            if (!dataHmMSmDesc.loadFromDB(hmMSmId)) {
                dataHmMSmDesc.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                // Wenn SDATA und SDATB in einer Konstellation sind, in der sie nicht übernommen werden können, hier abbrechen.
                if (!hmMSmDescHelper.checkSDATABValidity(dataHmMSmDesc, langDef, importRec)) {
                    reduceRecordCount();
                    return;
                } else {
                    // Den neuen Datensatz mit den importierten Werten befüllen und zur Map hinzufügen.
                    hmMSmDescHelper.fillOverrideCompleteDataForDIALOGReverse(dataHmMSmDesc, importRec, langDef);
                    hmMSmDescDatasets.put(hmMSmId, dataHmMSmDesc);
                }
            } else {
                // Den vorhandenen Datensatz mit den importierten Werten befüllen und zur Map hinzufügen
                hmMSmDescDatasets.put(hmMSmId, dataHmMSmDesc);
                // Wenn SDATA und SDATB in einer Konstellation sind, in der sie nicht übernommen werden können, hier abbrechen.
                if (!hmMSmDescHelper.checkSDATABValidity(dataHmMSmDesc, langDef, importRec)) {
                    reduceRecordCount();
                    return;
                }
                // Sprachabhängig unterschiedliche Werte übernehmem.
                hmMSmDescHelper.fillDataObjectLanguageDependent(dataHmMSmDesc, importRec, langDef);
            }
        } else {
            // Diesen Datensatz gab es im Laufe des Imports schon. Also nicht neu anlegen, damit er am Ende nicht mehrmals
            // im Batch-Statement auftaucht. Stattdessen nur die neue Sprache zum Feld FIELD_DH_DESC hinzufügen
            if (!hmMSmDescHelper.checkSDATABValidity(dataHmMSmDesc, langDef, importRec)) {
                reduceRecordCount();
                return;
            }
            // Sprachabhängig unterschiedliche Werte übernehmem.
            hmMSmDescHelper.fillDataObjectLanguageDependent(dataHmMSmDesc, importRec, langDef);
            // Kein neues DataObject wurde angelegt --> diesen Import-Datensatz nicht mitzählen
            reduceRecordCount();
        }

        // Wir verwenden für die HMMSM Struktur zwei Tabellen, wobei HMMSM und HMMSMDESC gleiche Daten halten
        // Die HMMSM Tabelle enthält nur gültige HmMSmIds ohne Zusatzinfos.
        // Die HMMSMDESC Tabelle enthält auch Datensätze in denen nur Hm oder Hm/M befüllt sind, um Informationen an den Hm und M Knoten zu speichern.
        // An Stellen, an denen HmMSm angefragt wird, kann so einfach die HMMSM Tabelle verwendet werden und es muss nicht im Ergebnis
        // alle ungültigen HmMSmIds ausgefiltert werden. Außerdem kann mit der HMMSM Tabelle leichter gejoint werden
        iPartsDataHmMSm dataHmMSm = hmMSmDatasets.get(hmMSmId);
        if (dataHmMSm == null) {
            dataHmMSm = new iPartsDataHmMSm(getProject(), hmMSmId);
            if (hmMSmId.isValidId() && !dataHmMSm.existsInDB()) {
                dataHmMSm.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                hmMSmDatasets.put(hmMSmId, dataHmMSm);
            }
        }
    }

    @Override
    public void postImportTask() {
        if (!isCancelled()) {
            if (importToDB) {
                getMessageLog().fireProgress(0, hmMSmDescDatasets.size(), "", true, false);
                int counter = 0;
                for (iPartsDataHmMSmDesc dataHmMSmDesc : hmMSmDescDatasets.values()) {
                    saveToDB(dataHmMSmDesc);
                    getMessageLog().fireProgress(counter++, hmMSmDescDatasets.size(), "", true, true);
                }
                getMessageLog().hideProgress();

                if (!hmMSmDatasets.isEmpty()) {
                    super.postImportTask();

                    setBufferedSave(doBufferSave);
                    getMessageLog().fireMessage(translateForLog("!!Nachbehandlung: Speichern in HM/M/SM-Tabelle..."),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    getMessageLog().fireProgress(0, +hmMSmDatasets.size(), "", true, false);
                    counter = 0;
                    for (iPartsDataHmMSm dataHmMSm : hmMSmDatasets.values()) {
                        saveToDB(dataHmMSm);
                        getMessageLog().fireProgress(counter++, hmMSmDatasets.size(), "", true, true);
                    }
                    getMessageLog().hideProgress();
                    getMessageLog().fireMessage(translateForLog("!!Nachbehandlung: %1 Datensätze in HM/M/SM-Tabelle importiert",
                                                                String.valueOf(counter)),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    getMessageLog().fireMessage(translateForLog("!!Nachbehandlung beendet"),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                }
            }
        }
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(TABLE_DA_HMMSMDESC)) {
            deleteLanguageEntriesOfTable(TABLE_DA_HMMSMDESC);
            getProject().getDB().delete(TABLE_DA_HMMSMDESC);
            getProject().getDB().delete(TABLE_DA_HMMSM); // Soll HMMSM auch gelöscht werden?
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(TABLE_DA_HMMSMDESC)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    private class HMMSMImportHelper extends DIALOGImportHelper {

        public HMMSMImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if ((sourceField.equals(KGVZ_SDATA)) || (sourceField.equals(KGVZ_SDATB))) {
                value = getDIALOGDateTimeValue(value);
            }
            return value;
        }

        /**
         * Funktion, die nur die entsprechenden Daten passend zur Sprache aus den Importdaten übernimmt.
         * Bei (DE) werden alle Attribute übernommen.
         * Bei allen anderen Sprachen wird nur der sprachabhängige Text übernommen.
         *
         * @param dataHmMSmDesc
         * @param importRec
         * @param langType
         */
        private void fillDataObjectLanguageDependent(iPartsDataHmMSmDesc dataHmMSmDesc, Map<String, String> importRec, iPartsDIALOGLanguageDefs langType) {
            // Hier muss natürlich auch noch nach der Sprache unterschieden werden, da unterschiedliche Werte übernommen werden.
            if (langType == iPartsDIALOGLanguageDefs.DIALOG_DE) {
                // aktualisiere Daten + DE Benennung
                fillOverrideCompleteDataForDIALOGReverse(dataHmMSmDesc, importRec, langType);
            } else {
                // Description in dieser Sprache übernehmen
                fillOverrideOneLanguageText(dataHmMSmDesc, langType.getDbValue(), FIELD_DH_DESC, handleValueOfSpecialField(KGVZ_BEN, importRec));
            }
        }

        private boolean areMSmNodesOnlyDigits(HmMSmId hmMSmId, int recordNo) {
            if (hmMSmId.isMNode()) {
                if (!StrUtils.isDigit(hmMSmId.getM())) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (ungültiger M Knoten: %2)",
                                                                String.valueOf(recordNo), hmMSmId.getM()),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    reduceRecordCount();
                    return false;
                }
            }

            if (hmMSmId.isSmNode()) {
                if (!StrUtils.isDigit(hmMSmId.getSm())) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (ungültiger SM Knoten: %2)",
                                                                String.valueOf(recordNo), hmMSmId.getSm()),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    reduceRecordCount();
                    return false;
                }
            }
            return true;
        }

        /**
         * Überprüft, ob der übergebene Text für die übergebene Sprache abhängig von seinem Gültigkeitszeitraum
         * übernommen werden soll.
         *
         * @param hmMSmDesc Das Objekt gefüllt mit den Daten aus der Datenbank oder den DEFAULT-Werten
         * @return true/false
         */
        private boolean checkSDATABValidity(iPartsDataHmMSmDesc hmMSmDesc, iPartsDIALOGLanguageDefs langType, Map<String, String> importRec) {
            // Die Werte der Datenbank aus dem Datenobjekt holen.
            // Sie sind im vergleichbaren Datenbankformat.
            String importSDatA = handleValueOfSpecialField(mapping.get(FIELD_DH_DATA), importRec);
            String importSDatB = handleValueOfSpecialField(mapping.get(FIELD_DH_DATB), importRec);
            String dbSDatA = hmMSmDesc.getFieldValue(FIELD_DH_DATA);
            String dbSDatB = hmMSmDesc.getFieldValue(FIELD_DH_DATB);

            if (isFinalStateDateTime(dbSDatB)) {
                // FinalStateWerte aus der DB ausgleichen
                hmMSmDesc.setFieldValue(iPartsConst.FIELD_DH_DATB, "", DBActionOrigin.FROM_EDIT);
            }

            if (langType == iPartsDIALOGLanguageDefs.DIALOG_DE) {
                // nur bei DE
                if (dbSDatA.isEmpty()) { // Minus unendlich
                    return true;
                }
                // importSDatA > dbSDataA => aktuallisiere Daten + DE Benennung
                int compareResult = importSDatA.compareTo(dbSDatA);
                if (compareResult > 0) {
                    return true;
                } else if (compareResult == 0) {
                    // Datum ist gleich dem aktuellen Eintrag
                    EtkMultiSprache currentTexts = hmMSmDesc.getFieldValueAsMultiLanguage(FIELD_DH_DESC);
                    if (currentTexts == null) {
                        return true;
                    }
                    // Existiert für "deutsch" noch kein Eintrag, deutschen Text übernehmen. Existiert bei gleichem
                    // Datum schon ein deutscher Text, dann nichts machen.
                    return !currentTexts.containsLanguage(langType.getDbValue(), true);
                } else {
                    return false;
                }
            } else {
                // alle anderen Sprachen
                if (importSDatB.isEmpty()) { // Plus unendlich
                    return true;
                }
                // importSDatB <= dbSDataB => nix machen
                if (importSDatB.compareTo(dbSDatA) <= 0) {
                    return false;
                } else {
                    // Description in dieser Sprache übernehmen
                    return true;
                }
            }
        }
    }
}
