/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChange;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataInternalText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataInternalTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.ChangeSetModificatorImport;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Importer für die Konstruktionsstückliste (BCTE) sowie ereignisgesteuerte Konstruktionsstücklisten (BRTE)
 */
public class PartListDataImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    private static final Set<String> initialImportETKZValues = new HashSet<>();

    // Die Konstruktionsstückliste aus DIALOG (BCTE)
    public static final String TABLENAME_PREFIX = "T10R";
    public static final String BCTE_PREFIX = "BCTE";
    public static final String BRTE_PREFIX = "BRTE";
    public static final String IMPORT_TABLENAME_BCTE = TABLENAME_PREFIX + BCTE_PREFIX;
    public static final String IMPORT_TABLENAME_BRTE = TABLENAME_PREFIX + BRTE_PREFIX;

    public static final String DEST_TABLENAME = TABLE_DA_DIALOG;

    // Die zu importierenden Spalten der Konstruktionsstückliste
    public static final String PG = "PG";
    public static final String BR = "BR";
    public static final String RAS = "RAS";
    public static final String POSE = "POSE";
    public static final String SESI = "SESI";
    public static final String POSP = "POSP";
    public static final String PV = "PV";
    public static final String WW = "WW";
    public static final String ETZ = "ETZ";
    public static final String AA = "AA";
    public static final String SDATA = "SDATA";
    public static final String SDATB = "SDATB";
    public static final String KEMA = "KEMA";
    public static final String KEMB = "KEMB";
    public static final String STEUA = "STEUA";
    public static final String STEUB = "STEUB";
    public static final String FED = "FED";
    public static final String TEIL = "TEIL";
    public static final String L = "L";
    public static final String MGKZ = "MGKZ";
    public static final String MG = "MG";
    public static final String RFMEA = "RFMEA";
    public static final String RFMEN = "RFMEN";
    public static final String PTE = "PTE";
    public static final String KGUM = "KGUM";
    public static final String STR = "STR";
    public static final String RFG = "RFG";
    public static final String VERT = "VERT";
    public static final String ZBKZ = "ZBKZ";
    public static final String VARG = "VARG";
    public static final String VARM = "VARM";
    public static final String GES = "GES";
    public static final String PROJ = "PROJ";
    public static final String ETKZ = "ETKZ";
    public static final String CR = "CR";
    public static final String BZAE_NEU = "BZAE_NEU";

    public static final String BRTE_EREIA = "BRTE_EREIA";     // nur BRTE
    public static final String BRTE_EREIB = "BRTE_EREIB";     // nur BRTE

    private static final String BZA = "BZA";        // aktuell nur BCTE
    private static final String CR_LEN = "CR_LEN";  // aktuell nur BCTE

    static {
        initialImportETKZValues.add("K");
        initialImportETKZValues.add("N");
        initialImportETKZValues.add("V");
    }

    private final String importTableInXML;
    private HashMap<String, String> mapping;
    private String[] primaryKeysForImportData;
    private String prefixForImporterInstance;

    private boolean importToDB = true; //sollen die Daten abgespeichert werden?
    // Map mit dem BCTE Schlüssel als Key, damit keine doppelten Datensätze gespeichert werden
    // (in einer Importdatei kann der gleiche Datensatz mehrfach vorkommen)
    private final Map<iPartsDialogId, iPartsDataDialogData> importedDataDialogDataMap = new HashMap<>();
    private Map<String, Boolean> partToWireHarnessFlag;

    private ASUsageHelper usageHelper;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public PartListDataImporter(EtkProject project, String xmlTableName) {
        super(project, "Invalid Importer");
        // Tabellenname aus der XML Datei
        this.importTableInXML = xmlTableName;
        initImporter();
    }

    private void initImporter() {
        mapping = new HashMap<>();
        prefixForImporterInstance = "";
        String nameForImport = "";
        // Unterscheidung BCTE - BRTE
        if (importTableInXML.equals(IMPORT_TABLENAME_BCTE)) {
            prefixForImporterInstance = BCTE_PREFIX + "_";
            nameForImport = DD_PL_DATA;
            importName = "!!DIALOG-Konstruktionsstückliste (BCTE)";
        } else if (importTableInXML.equals(IMPORT_TABLENAME_BRTE)) {
            prefixForImporterInstance = BRTE_PREFIX + "_";
            nameForImport = DD_PL_DATA_EVENT;
            importName = "!!DIALOG-Konstruktionsstückliste (BRTE)";
            mapping.put(FIELD_DD_EVENT_FROM, BRTE_EREIA);     // nur BRTE
            mapping.put(FIELD_DD_EVENT_TO, BRTE_EREIB);     // nur BRTE
        }
        // Die Primärschlüsselfelder, wie sie in den zu importierenden Daten (die Namen der Import-Spalten) existieren müssen:
        primaryKeysForImportData = new String[]{ prefixForImporterInstance + BR, prefixForImporterInstance + RAS, prefixForImporterInstance + POSE,
                                                 prefixForImporterInstance + PV, prefixForImporterInstance + WW, prefixForImporterInstance + ETZ,
                                                 prefixForImporterInstance + AA, prefixForImporterInstance + SDATA };

        // Die Primärschlüsselfelder müssen auch ins Mapping, weil in DA_DIALOG nur die GUID ein Primärschlüsselfeld ist
        mapping.put(FIELD_DD_SERIES_NO, prefixForImporterInstance + BR);
//        mapping.put(FIELD_DD_RAS, prefixForImporterInstance + RAS);  Extra-Behandlung für HM/M/SM
        mapping.put(FIELD_DD_POSE, prefixForImporterInstance + POSE);
        mapping.put(FIELD_DD_POSV, prefixForImporterInstance + PV);
        mapping.put(FIELD_DD_WW, prefixForImporterInstance + WW);
        mapping.put(FIELD_DD_ETZ, prefixForImporterInstance + ETZ);
        mapping.put(FIELD_DD_AA, prefixForImporterInstance + AA);
        mapping.put(FIELD_DD_SDATA, prefixForImporterInstance + SDATA);

        // Die normalen Felder:
        mapping.put(FIELD_DD_SDATB, prefixForImporterInstance + SDATB);
        mapping.put(FIELD_DD_KEMA, prefixForImporterInstance + KEMA);
        mapping.put(FIELD_DD_KEMB, prefixForImporterInstance + KEMB);
        mapping.put(FIELD_DD_STEUA, prefixForImporterInstance + STEUA);
        mapping.put(FIELD_DD_STEUB, prefixForImporterInstance + STEUB);
        mapping.put(FIELD_DD_PRODUCT_GRP, prefixForImporterInstance + PG);
        mapping.put(FIELD_DD_SESI, prefixForImporterInstance + SESI);
        mapping.put(FIELD_DD_POSP, prefixForImporterInstance + POSP);
        mapping.put(FIELD_DD_FED, prefixForImporterInstance + FED);
        mapping.put(FIELD_DD_PARTNO, prefixForImporterInstance + TEIL);
        mapping.put(FIELD_DD_STEERING, prefixForImporterInstance + L);
        mapping.put(FIELD_DD_QUANTITY_FLAG, prefixForImporterInstance + MGKZ);
        mapping.put(FIELD_DD_QUANTITY, prefixForImporterInstance + MG);
        mapping.put(FIELD_DD_RFMEA, prefixForImporterInstance + RFMEA);
        mapping.put(FIELD_DD_RFMEN, prefixForImporterInstance + RFMEN);
        mapping.put(FIELD_DD_PTE, prefixForImporterInstance + PTE);
        mapping.put(FIELD_DD_KGUM, prefixForImporterInstance + KGUM);
        mapping.put(FIELD_DD_HIERARCHY, prefixForImporterInstance + STR);
        mapping.put(FIELD_DD_RFG, prefixForImporterInstance + RFG);
        mapping.put(FIELD_DD_DISTR, prefixForImporterInstance + VERT);
        mapping.put(FIELD_DD_ZFLAG, prefixForImporterInstance + ZBKZ);
        mapping.put(FIELD_DD_VARG, prefixForImporterInstance + VARG);
        mapping.put(FIELD_DD_VARM, prefixForImporterInstance + VARM);
        mapping.put(FIELD_DD_GES, prefixForImporterInstance + GES);
        mapping.put(FIELD_DD_PROJ, prefixForImporterInstance + PROJ);
        mapping.put(FIELD_DD_ETKZ, prefixForImporterInstance + ETKZ);
        mapping.put(FIELD_DD_CODES, prefixForImporterInstance + CR);
        mapping.put(FIELD_DD_BZAE_NEU, prefixForImporterInstance + BZAE_NEU);
        mapping.put(FIELD_DD_BZA, prefixForImporterInstance + BZA);
        mapping.put(FIELD_DD_CODE_LEN, prefixForImporterInstance + CR_LEN);

        // Setzen des FileListTypes für den Importdialog
        importFileTypes = new FilesImporterFileListType[]{ new FilesImporterFileListType(DEST_TABLENAME,
                                                                                         nameForImport, false, false, false,
                                                                                         new String[]{ MimeTypes.EXTENSION_XML }) };
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {

        String[] mustExists = primaryKeysForImportData;
//        String[] mustHaveData = new String[]{ BCTE_BR, BCTE_RAS, BCTE_POSE, BCTE_PV, BCTE_AA, BCTE_SDATA };
        String[] mustHaveData = new String[]{ prefixForImporterInstance + BR, prefixForImporterInstance + RAS,
                                              prefixForImporterInstance + POSE, prefixForImporterInstance + PV,
                                              prefixForImporterInstance + AA, prefixForImporterInstance + SDATA };
        // An den Importer anhängen.
        importer.setMustExists(mustExists);
        importer.setMustHaveData(mustHaveData);

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(IMPORT_TABLENAME_BCTE)
                   || importer.getTableNames().get(0).equals(IMPORT_TABLENAME_BRTE)
                   || importer.getTableNames().get(0).equals(BRTE_PREFIX);
        }
        return false;

    }

    @Override
    protected boolean skipRecord(AbstractKeyValueRecordReader importer, Map<String, String> importRec) {
        // Überspringe den Datensatz wenn er den SDB Kenner hat, dass ein Update Datensatz folgt
        if (isDIALOGDeltaDataImport() && DIALOGImportHelper.isDatasetMarkedForFollowingUpdate(importRec)) {
            return true;
        }
        return super.skipRecord(importer, importRec);
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return isDIALOGSpecificHmMSmValueValid(importer, importRec, prefixForImporterInstance + RAS, errors);
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        usageHelper = new ASUsageHelper(getProject());
        partToWireHarnessFlag = new HashMap<>();
        setBufferedSave(true);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        PartListDataImportHelper importHelper = new PartListDataImportHelper(getProject(), mapping, DEST_TABLENAME);
        // Wenn die Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
        if (!importHelper.checkImportRelevanceForSeries(prefixForImporterInstance + BR, importRec, getInvalidSeriesSet(), this)) {
            return;
        }

        iPartsDialogBCTEPrimaryKey primaryBCTEKey = importHelper.getPrimaryBCTEKey(this, importRec, recordNo);
        if (primaryBCTEKey == null) {
            importHelper.cancelImporterDueToIncorrectBCTEKey(this, recordNo);
            return;
        }

        //ab Sprint 20: neue GUID
        iPartsDialogId id = new iPartsDialogId(primaryBCTEKey.createDialogGUID());
        iPartsDataDialogData importData = new iPartsDataDialogData(getProject(), id);
        boolean isNewData = false;
        String currentETKZ = importHelper.handleValueOfSpecialField(prefixForImporterInstance + ETKZ, importRec);
        String oldETKZ = currentETKZ;

        // DAIMLER-4412: Doku-relevant behalten, bzw default vorbesetzen
        if (!importData.existsInDB()) {
            importData.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
            isNewData = true;
        } else {
            iPartsDataDialogData existingDataInImportList = importedDataDialogDataMap.get(importData.getAsId());
            String currentSDATB;
            if (existingDataInImportList == null) {
                // Wenn bereits ein Datensatz mit echten KEM-BIS-Datum in der DB existiert und jetzt ein Datensatz mit identischem
                // Primärschlüssel aber leerem KEM-BIS-Datum (unendlich) kommt, dann muss dieser neue Datensatz ignoriert werden
                currentSDATB = importHelper.handleValueOfSpecialField(prefixForImporterInstance + SDATB, importRec);
            } else {
                // wenn bereits ein Datensatz mit gleichem Primärschlüssel in der Speicher Liste enthalten ist, muss dieser auch berücksichtigt werden
                currentSDATB = existingDataInImportList.getFieldValue(FIELD_DD_SDATB);
            }
            if (currentSDATB.isEmpty() && !importData.getFieldValue(FIELD_DD_SDATB).isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (unendliches SDATB bei bereits vorhandenem Datensatz mit echtem SDATB)",
                                                            String.valueOf(recordNo)), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return;
            }

            oldETKZ = importData.getFieldValue(FIELD_DD_ETKZ);
        }
        importHelper.fillOverrideCompleteDataForDIALOGReverse(importData, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);

        // Setzen der separaten HMMSM Felder
        importHelper.setHmMSmFields(importData, importRec, prefixForImporterInstance + BR, prefixForImporterInstance + RAS,
                                    FIELD_DD_HM, FIELD_DD_M, FIELD_DD_SM);
        handleETKZ(importHelper, importData, primaryBCTEKey, isNewData, oldETKZ, currentETKZ);

        // DAIMLER-7918 Übernahme konstr. Attribute auf die Teilepositionen im TU bei Urladung
        if (isDIALOGInitialDataImport()) {
            setASEntriesFromConstructionAttributes(primaryBCTEKey, importData);
        }

        // Neue Zuordnung speichern.
        if (importToDB) {
            importedDataDialogDataMap.put(importData.getAsId(), importData);
        }
    }

    private void setASEntriesFromConstructionAttributes(iPartsDialogBCTEPrimaryKey primaryBCTEKey, iPartsDataDialogData importData) {
        // Bei Positionen in PSK sollen diese Attribute ebenfalls gesetzt werden, daher hier keine Unterscheidung bzw PSK Filterung
        List<EtkDataPartListEntry> retailPartListEntries = usageHelper.getPartListEntriesUsedInAS(primaryBCTEKey, true);
        if ((retailPartListEntries != null) && !retailPartListEntries.isEmpty()) {
            for (EtkDataPartListEntry retailPartListEntry : retailPartListEntries) {
                if (retailPartListEntry != null) {
                    // Gleiche Logik wie bei AS-Übernahme aus Konstruktion -> EditConstructionToRetailHelper
                    retailPartListEntry.setAttributeValue(iPartsConst.FIELD_K_CODES_CONST, importData.getFieldValue(FIELD_DD_CODES),
                                                          DBActionOrigin.FROM_EDIT);
                    retailPartListEntry.setAttributeValue(iPartsConst.FIELD_K_MENGE_CONST, importData.getFieldValue(FIELD_DD_QUANTITY),
                                                          DBActionOrigin.FROM_EDIT);
                    retailPartListEntry.setAttributeValue(iPartsConst.FIELD_K_HIERARCHY_CONST, importData.getFieldValue(FIELD_DD_HIERARCHY),
                                                          DBActionOrigin.FROM_EDIT);

                    retailPartListEntry.setAttributeValue(iPartsConst.FIELD_K_EVENT_FROM_CONST, importData.getFieldValue(FIELD_DD_EVENT_FROM),
                                                          DBActionOrigin.FROM_EDIT);
                    retailPartListEntry.setAttributeValue(iPartsConst.FIELD_K_EVENT_TO_CONST, importData.getFieldValue(FIELD_DD_EVENT_TO),
                                                          DBActionOrigin.FROM_EDIT);
                }
                if (importToDB) {
                    saveToDB(retailPartListEntry, false);
                }
            }
        }
    }

    private void handleETKZ(PartListDataImportHelper importHelper, iPartsDataDialogData importData,
                            iPartsDialogBCTEPrimaryKey primaryBCTEKey, boolean isNewData, String oldETKZ, String currentETKZ) {
        boolean isInitialDataImport = isDIALOGInitialDataImport();
        // Doku-Relevanz bei Delta-Versorgung:
        // - Bei der Delta-Versorgung wird der Status an den DIALOG Stücklisteneinträgen zurückgesetzt, wenn der ETK der
        // Teileposition von N/K sich auf einen anderen Wert als N/K ändert und der manuelle Autorenstatus auf
        // "nicht Doku relevant (K)" steht
        if (!isInitialDataImport && !isNewData) {
            String docuRelValue = importData.getFieldValue(FIELD_DD_DOCU_RELEVANT);
            iPartsDocuRelevant docuRelevant = iPartsDocuRelevant.getFromDBValue(docuRelValue);
            // Aktuelle Doku-Relevanz = "nicht Doku-relevant", bestehender ETKZ is N, K oder V und neuer ETKZ Wert ist nichts davon
            // -> Doku-Relevanz auf "nicht spezifiziert" setzen
            if ((docuRelevant == iPartsDocuRelevant.DOCU_RELEVANT_NO) && initialImportETKZValues.contains(oldETKZ) && !initialImportETKZValues.contains(currentETKZ)) {
                importData.setFieldValue(FIELD_DD_DOCU_RELEVANT, iPartsDocuRelevant.DOCU_RELEVANT_NOT_SPECIFIED.getDbValue(), DBActionOrigin.FROM_EDIT);
            }
        }


        iPartsDataReleaseState datasetState = iPartsDataReleaseState.RELEASED;
        if (!isInitialDataImport && !isNewData) {
            if (!oldETKZ.equals(currentETKZ)) {
                // ETKZ Wert hat sich verändert. Existiert die Position mit dem BCTE Schlüssel nur in einer PSK
                // Stückliste, wird das Status als "nicht in AS verwendet" interpretiert.
                boolean isUsedInAS = usageHelper.isUsedInAS(primaryBCTEKey) && !usageHelper.checkIfOnlyPSKProducts(primaryBCTEKey, true);
                // Wird eine bereits vorhandene Teileposition mit geändertem ET-KZ erneut versorgt, die noch nicht verortet ist
                // und auch in keinem nicht freigegebenen Autorenauftrag verortet wird,
                // dann wird der Status nicht geändert und kein Änderungsset angelegt
                if (!isUsedInAS) {
                    // Status setzen falls leer
                    if (importData.getFieldValue(iPartsConst.FIELD_DD_STATUS).isEmpty()) {
                        importData.setFieldValue(iPartsConst.FIELD_DD_STATUS, datasetState.getDbValue(), DBActionOrigin.FROM_EDIT);
                    }
                    return;
                }

                // Wird eine bereits vorhandene Teileposition mit geändertem ET-KZ erneut versorgt, die verortet ist
                // oder in einem nicht freigegebenen Autorenauftrag verortet wird, dann wird der Status auf "Neu" geändert,
                // ein Änderungsset angelegt und die Daten aktualisiert
                datasetState = iPartsDataReleaseState.getTypeByDBValue(importData.getFieldValue(iPartsConst.FIELD_DD_STATUS));

                // Änderungssatz für Kennzeichnung der Änderung in Stückliste (Änderung -> BCTE-Schlüssel) falls Status nicht NEW
                if (datasetState != iPartsDataReleaseState.NEW) {
                    importData.setFieldValue(iPartsConst.FIELD_DD_STATUS, iPartsDataReleaseState.NEW.getDbValue(), DBActionOrigin.FROM_EDIT);
                    String seriesNo = primaryBCTEKey.getHmMSmId().getSeries();
                    String bcteKey = importData.getAsId().getDialogGuid();
                    iPartsDataDIALOGChange dataDialogChanges = importHelper.createChangeRecord(iPartsDataDIALOGChange.ChangeType.PARTLISTENTRY_ETKZ,
                                                                                               importData.getAsId(), seriesNo,
                                                                                               bcteKey, "", "");
                    if (importToDB) {
                        saveToDB(dataDialogChanges);
                    }
                    return;
                }
            } else {
                // Wird eine bereits vorhandene Teileposition erneut versorgt, die verortet ist
                // und sich der ET-Kenner nicht ändert, dann wird der Status nicht geändert,
                // kein Änderungsset angelegt und die Daten aktualisiert
            }
        }

        // Status setzen falls leer oder Urladung
        if (isInitialDataImport || importData.getFieldValue(iPartsConst.FIELD_DD_STATUS).isEmpty()) {
            importData.setFieldValue(iPartsConst.FIELD_DD_STATUS, datasetState.getDbValue(), DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Überprüft, ob die Teilenummer der Importdaten das Sonstige-KZ = "LA" hat
     *
     * @param importData
     * @return
     */
    private boolean hasAdditionalWireHarnessFlag(iPartsDataDialogData importData) {
        String partNo = importData.getFieldValue(FIELD_DD_PARTNO);
        // Teilenummer nicht gültig -> raus bzw. alte Logik
        if (StrUtils.isEmpty(partNo)) {
            return false;
        }
        Boolean hasFlag = partToWireHarnessFlag.get(partNo);
        // Zur Teilenummer existiert ein Wert -> zurückgeben, ob sonstige-KZ = "LA"
        if (hasFlag != null) {
            return hasFlag;
        }
        // Zur Teilenummer gibt es noch keinen Wert -> Teilenummer aus DB laden und prüfen, ob sonstige-KZ = "LA".
        // Ergebnis in der Map ablegen
        EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), new PartId(partNo, ""));
        if (part.existsInDB()) {
            boolean hasAdditionalWireHarnessFlag = iPartsWireHarnessHelper.hasValidAdditionalWireHarnessFlag(part);
            partToWireHarnessFlag.put(partNo, hasAdditionalWireHarnessFlag);
            return hasAdditionalWireHarnessFlag;
        } else {
            // Teil existiert nicht in DB -> Teil kann bis zum Ende des Imports nicht angelegt werden, daher ist
            // sonstige-KZ <> "LA"
            partToWireHarnessFlag.put(partNo, false);
        }
        return false;

    }

    @Override
    public void postImportTask() {
        // Jetzt alle importierten Datensätze speichern inkl. Aktualisierung aller AS-Stücklisteneinträge bei DIALOG Delta
        boolean isDIALOGDeltaDataImport = isDIALOGDeltaDataImport();
        String dialogDeltaImportString = isDIALOGDeltaDataImport ? (" " + translateForLog("!!inkl. Übernahme in AS-Stücklisten")) : "";
        getMessageLog().fireMessage(translateForLog("!!Bearbeite %1 Datensätze%2...", String.valueOf(importedDataDialogDataMap.size()),
                                                    dialogDeltaImportString), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        int maxProgress = importedDataDialogDataMap.size();
        getMessageLog().fireProgress(0, maxProgress, "", true, false);
        int progressCounter = 0;
        GenericEtkDataObjectList dataObjectList = new GenericEtkDataObjectList();
        List<ChangeSetModificator.ChangeSetModificationTask> changeSetModificationTasks = new ArrayList<>();
        for (final iPartsDataDialogData dataDialogData : importedDataDialogDataMap.values()) {
            if (cancelImportIfInterrupted()) {
                break;
            }

            boolean modifyASPartListEntries = false;
            if (isDIALOGDeltaDataImport) {
                // Ereignis-bis oder KEM-Datum-bis sind verändert und nicht leer
                DBDataObjectAttribute eventToAttribute = dataDialogData.getAttribute(FIELD_DD_EVENT_TO);
                DBDataObjectAttribute sdatBAttribute = dataDialogData.getAttribute(FIELD_DD_SDATB);
                if ((eventToAttribute.isModified() && !eventToAttribute.getAsString().isEmpty())
                    || (sdatBAttribute.isModified() && !sdatBAttribute.getAsString().isEmpty())) {
                    modifyASPartListEntries = true;
                }
            }

            saveToDB(dataDialogData); // Import-Datensatz speichern

            // Ab hier Zusammensuchen und Verändern der AS-Stücklisteneinträge (auch im nicht freigegebenen Autorenaufträgen)
            if (modifyASPartListEntries) {
                String bcteKey = dataDialogData.getFieldValue(FIELD_DD_GUID);
                final iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(bcteKey);
                if (bctePrimaryKey != null) {
                    // Bereits existierende AS-Stücklisteneinträge anpassen (auch PSK Einträge, das KEM Datum bis dort ebenfalls gesetzt werden soll)
                    List<EtkDataPartListEntry> retailPartListEntries = usageHelper.getPartListEntriesUsedInAS(bctePrimaryKey, true);
                    if (retailPartListEntries != null) {
                        modifyPartListEntries(getProject(), retailPartListEntries, dataDialogData, dataObjectList);
                    }

                    // Den BCTE-Schlüssel in den aktiven ChangeSets suchen. Pro ChangeSet wird ein Callback hinzugefügt,
                    // der neue Stücklisteneinträge mit dieser Source-GUID aktualisiert. Alle Callbacks werden dann am Ende
                    // des Imports ausgeführt.
                    Set<String> relevantChangeSetIds = usageHelper.getChangeSetIdsForPartListEntriesUsedInActiveChangeSets(bctePrimaryKey);
                    if (relevantChangeSetIds != null) {
                        for (String relevantChangeSetId : relevantChangeSetIds) {
                            ChangeSetModificator.ChangeSetModificationTask changeSetModificationTask = new ChangeSetModificator.ChangeSetModificationTask(relevantChangeSetId) {
                                @Override
                                public void modifyChangeSet(EtkProject projectForChangeSet, iPartsRevisionChangeSet authorOrderChangeSet,
                                                            GenericEtkDataObjectList dataObjectListForChangeSet) {
                                    List<EtkDataPartListEntry> partListEntries = ChangeSetModificator.getNewASPartListEntriesForBCTEKey(projectForChangeSet,
                                                                                                                                        authorOrderChangeSet,
                                                                                                                                        bctePrimaryKey);
                                    modifyPartListEntries(projectForChangeSet, partListEntries, dataDialogData, dataObjectListForChangeSet);
                                }
                            };
                            changeSetModificationTasks.add(changeSetModificationTask);
                        }
                    }
                }
            }
            progressCounter++;
            getMessageLog().fireProgress(progressCounter, maxProgress, "", true, true);
        }
        getMessageLog().fireProgress(maxProgress, maxProgress, "", true, false);

        // Nachdem die Konstruktionsdatensätze importiert wurden, muss geprüft werden, ob hier Nachfolger angelegt
        // wurden, die den internen Text des Vorgängers erben sollen
        inheritInternalTexts();

        if (!cancelImportIfInterrupted()) {
            if (!dataObjectList.isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Speichere %1 veränderte AS-Stücklisteneinträge...", String.valueOf(dataObjectList.size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                getMessageLog().fireProgress(0, maxProgress, "", true, false);

                // Veränderte AS-Stücklisteneinträge speichern inkl. technischem ChangeSet
                iPartsRevisionChangeSet changeSet = saveToTechnicalChangeSet(dataObjectList, iPartsConst.TECHNICAL_USER_DIALOG_DELTA_SUPPLY);
                getMessageLog().fireProgress(maxProgress, maxProgress, "", true, false);
                if (changeSet != null) {
                    getMessageLog().fireMessage(translateForLog("!!Veränderte AS-Stücklisteneinträge gespeichert. Änderungsset-ID: %1",
                                                                changeSet.getChangeSetId().getGUID()), MessageLogType.tmlMessage,
                                                MessageLogOption.TIME_STAMP);
                }
            }
        }

        // Jetzt die Stücklisteneinträge in allen betroffenen ChangeSets aktualisieren (ist unabhängig vom Speichern
        // der Daten in super.postImportTask())
        ChangeSetModificatorImport helper = new ChangeSetModificatorImport(iPartsImportPlugin.LOG_CHANNEL_DEBUG,
                                                                           this, true);
        helper.executeChangesInAllChangeSets(changeSetModificationTasks, false,
                                             iPartsConst.TECHNICAL_USER_DIALOG_DELTA_SUPPLY);

        super.postImportTask();
        usageHelper.clear();
        partToWireHarnessFlag.clear();
    }

    /**
     * Vererbt den internen Text der Vorgängerpositionen an ihre Nachfolgerpositionen, die in diesem Import angelegt wurden
     */
    private void inheritInternalTexts() {
        // Map mit Baureihe+HM+M+SM auf alle dazugehörigen Importdatensätze gruppiert nach ihrem BCTE Schlüssel OHNE SDATA
        Map<HmMSmId, Map<iPartsDialogBCTEPrimaryKey, List<iPartsDataDialogData>>> hmMSmIdToDataMap = new HashMap<>();
        importedDataDialogDataMap.forEach((dialogId, dialogData) -> {
            HmMSmId hmMSmId = new HmMSmId(dialogData.getFieldValue(FIELD_DD_SERIES_NO),
                                          dialogData.getFieldValue(FIELD_DD_HM),
                                          dialogData.getFieldValue(FIELD_DD_M),
                                          dialogData.getFieldValue(FIELD_DD_SM));
            Map<iPartsDialogBCTEPrimaryKey, List<iPartsDataDialogData>> dataForHmMSmId = hmMSmIdToDataMap.computeIfAbsent(hmMSmId, k -> new HashMap<>());
            iPartsDialogBCTEPrimaryKey primaryKeyWithoutSdata = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dialogId.getDialogGuid());
            if (primaryKeyWithoutSdata != null) {
                primaryKeyWithoutSdata.sData = "";
                List<iPartsDataDialogData> entriesWithSimilarKey = dataForHmMSmId.computeIfAbsent(primaryKeyWithoutSdata, k -> new ArrayList<>());
                entriesWithSimilarKey.add(dialogData);
            }
        });
        int maxProgress = importedDataDialogDataMap.size();
        getMessageLog().fireMessage(translateForLog("!!Prüfe, ob interne Texte vererbt werden können..."),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        getMessageLog().fireProgress(0, maxProgress, "", true, false);
        VarParam<Integer> progressCounter = new VarParam<>(0);
        // Durchlaufe alle Importpositionen, die nach ihrer HMMSM Id gruppiert wurden
        hmMSmIdToDataMap.forEach((hmMSmId, entriesMap) -> {
            // Map mit BCTE Schlüssel auf den dazugehörigen internen Text zur aktuellen HMMSM Struktur
            Map<iPartsDialogBCTEPrimaryKey, List<iPartsDataInternalText>> internalTexts = iPartsDataInternalTextList.loadInternalTextsForAssembly(getProject(), hmMSmId);
            if (!internalTexts.isEmpty()) {
                entriesMap.forEach((primaryKeyWithoutSdata, entries) -> {
                    // Alle Positionen mit dem gleichen BCTE Schlüssel OHNE SDATA aufsteigend sortieren
                    entries.sort(Comparator.comparing(o -> o.getFieldValue(FIELD_DD_SDATA)));
                    List<iPartsDataInternalText> currentTexts = null;
                    String currentSDATB = "";
                    // Durchlaufe alle Positionen aus dem Import, die den gleichen BCTE Schlüssel habe bis auf SDATA.
                    // Vom ältesten Datensatz bis zum jüngsten.
                    for (iPartsDataDialogData importEntry : entries) {
                        // Um den internen Text zu einer Position zu finden, muss ihr BCTE Schlüssel erzeugt werden
                        iPartsDialogBCTEPrimaryKey primaryKeyImport = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(importEntry.getAsId().getDialogGuid());
                        if (primaryKeyImport != null) {
                            // Erst schauen, ob zu der GUID ein interner Text existiert
                            List<iPartsDataInternalText> foundTexts = internalTexts.get(primaryKeyImport);
                            // Existiert ein Text, dann handelt es sich um einen Datensatz, der schon in der DB existiert
                            // aber noch kein SDATB hat. Dieser Datensatz war bisher der aktuellste (SDATB leer).
                            // Wenn dieser Datensatz getroffen wird, kommt mit Sicherheit der nächste gültige Datensatz
                            // in den Importdaten vor. Dieser neue, gültige Datensatz soll also die Texte des aktuellen erhalten
                            if (foundTexts != null) {
                                // internen Text und SDATB zum Abgleich halten
                                currentTexts = foundTexts;
                                currentSDATB = importEntry.getFieldValue(FIELD_DD_SDATB);
                            } else if (currentTexts != null) {
                                // Wir haben keine Texte in der DB ABER ein vorheriger Datensatz mit gleichem BCTE und
                                // älteren SDATA hatte Texte! Prüfe, ob das SDATB vom vorherigen Datensatz das SDATA vom
                                // aktuellen ist. Falls ja, ist der aktuelle der direkte Nachfolger und soll die Texte
                                // bekommen. Ist es nicht der direkte Nachfolger, dann können die Texte für weitere
                                // Positionen nicht von einem direkten Vorgänger stammen!
                                if (currentSDATB.equals(importEntry.getFieldValue(FIELD_DD_SDATA))) {
                                    currentTexts.forEach(internalText -> {
                                        // Für die neue Position einen neuen, eigenen internen Text anlegen
                                        iPartsDataInternalTextId internalTextId = new iPartsDataInternalTextId(internalText.getAsId().getUserId(),
                                                                                                               importEntry.getAsId());
                                        iPartsDataInternalText internalTextForNewEntry = new iPartsDataInternalText(getProject(), internalTextId);
                                        internalText.loadMissingAttributesFromDB(new String[]{ FIELD_DIT_ATTACHMENT }, true, false, true);
                                        internalTextForNewEntry.assignAttributesValues(getProject(), internalText.getAttributes(), false, DBActionOrigin.FROM_EDIT);
                                        internalTextForNewEntry.setId(internalTextId, DBActionOrigin.FROM_EDIT);
                                        internalTextForNewEntry.updateOldId();
                                        saveToDB(internalTextForNewEntry);
                                        getMessageLog().fireMessage(translateForLog("!!An die Position mit " +
                                                                                    "dem BCTE Schlüssel \"%1\" wurden die" +
                                                                                    " internen Texte des Vorgängers vererbt",
                                                                                    importEntry.getAsId().toString("|")),
                                                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                                    });
                                    currentSDATB = importEntry.getFieldValue(FIELD_DD_SDATB);
                                } else {
                                    currentTexts = null;
                                    currentSDATB = "";
                                }
                            }
                        }
                        progressCounter.setValue(progressCounter.getValue() + 1);
                        getMessageLog().fireProgress(progressCounter.getValue(), maxProgress, "", true, true);
                    }
                });
            }
        });
        getMessageLog().fireProgress(maxProgress, maxProgress, "", true, false);
        getMessageLog().fireMessage(translateForLog("!!Prüfung, ob interne Texte vererbt werden können beendet"),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
    }

    private void modifyPartListEntries(EtkProject project, List<EtkDataPartListEntry> partListEntries,
                                       iPartsDataDialogData dataDialogData, GenericEtkDataObjectList dataObjectList) {
        String eventTo = dataDialogData.getFieldValue(FIELD_DD_EVENT_TO);
        String sdatB = dataDialogData.getFieldValue(FIELD_DD_SDATB);
        Map<AssemblyId, Boolean> assemblyIsPSKMap = new HashMap<>();
        for (EtkDataPartListEntry partListEntry : partListEntries) {
            if (cancelImportIfInterrupted()) {
                break;
            }
            // Kem Datum bis soll immer gesetzt werden (unabhängig von PSK)
            if (!sdatB.isEmpty()) {
                partListEntry.setFieldValue(FIELD_K_DATETO, sdatB, DBActionOrigin.FROM_EDIT);
            }
            // Ereignisse sollen nur bei nicht PSK Positionen gesetzt werden
            boolean isWithinPSKProduct = ASUsageHelper.isPSKAssembly(project, partListEntry.getOwnerAssemblyId(), assemblyIsPSKMap);
            if (!isWithinPSKProduct && !eventTo.isEmpty()) {
                partListEntry.setFieldValue(FIELD_K_EVENT_TO, eventTo, DBActionOrigin.FROM_EDIT);
                partListEntry.setFieldValue(FIELD_K_EVENT_TO_CONST, eventTo, DBActionOrigin.FROM_EDIT);
            }

            if (partListEntry.isModified()) {
                dataObjectList.add(partListEntry, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return true;

    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(DEST_TABLENAME)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }


    private class PartListDataImportHelper extends DIALOGImportHelper {

        public PartListDataImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if ((sourceField.equals(prefixForImporterInstance + SDATA)) || (sourceField.equals(prefixForImporterInstance + SDATB))) {
                value = getDIALOGDateTimeValue(value);
            }
            if (sourceField.equals(prefixForImporterInstance + TEIL)) {
                value = checkNumberInputFormat(value, getMessageLog());
            } else if (sourceField.equals(prefixForImporterInstance + STR)) {
                value = StrUtils.removeLeadingCharsFromString(value.trim(), '0'); // führende Nullen entfernen
            } else if (sourceField.equals(prefixForImporterInstance + BR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            } else if (sourceField.equals(prefixForImporterInstance + MG)) {
                value = checkQuantityFormat(value);
            }
            return value;
        }


        public iPartsDialogBCTEPrimaryKey getPrimaryBCTEKey(AbstractDataImporter importer, Map<String, String> importRec, int recordNo) {
            HmMSmId hmMSmId = HmMSmId.getIdFromRaster(importRec.get(prefixForImporterInstance + BR), importRec.get(prefixForImporterInstance + RAS));
            return getPartListPrimaryBCTEKey(importer, recordNo, hmMSmId,
                                             handleValueOfSpecialField(prefixForImporterInstance + POSE, importRec),
                                             handleValueOfSpecialField(prefixForImporterInstance + PV, importRec),
                                             handleValueOfSpecialField(prefixForImporterInstance + WW, importRec),
                                             handleValueOfSpecialField(prefixForImporterInstance + ETZ, importRec),
                                             handleValueOfSpecialField(prefixForImporterInstance + AA, importRec),
                                             handleValueOfSpecialField(prefixForImporterInstance + SDATA, importRec));
        }


    }
}