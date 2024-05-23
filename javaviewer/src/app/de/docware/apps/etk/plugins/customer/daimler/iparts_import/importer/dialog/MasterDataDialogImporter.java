/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChange;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDIALOGChangeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.iPartsMaterialImportHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Importer für DIALOG Teilestammdaten (TS1/TS2/TS6/TS7/VTNR/GEWS)
 * ab DAIMLER-7535 ohne TS7
 */
public class MasterDataDialogImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    // Die Zieltabelle [MAT] (ist in EtkDbConst.java definiert)
    private static final String DEST_TABLENAME = TABLE_MAT;
    public static final boolean IMPORT_HISTORY = false;

    // Zum Checken der zu importierenden Dateien/Daten
    private enum IMPORT_TYPE {
        IMPORT_UNKNOWN, IMPORT_TS1, IMPORT_TS2, IMPORT_TS6, IMPORT_VTNR, IMPORT_GEWS
    }

    public static final String DIALOG_TABLENAME_TS1 = "TS1";
    public static final String DIALOG_TABLENAME_TS2 = "TS2";
    public static final String DIALOG_TABLENAME_TS6 = "TS6";
    public static final String DIALOG_TABLENAME_GEWS = "GEWS";
    public static final String DIALOG_TABLENAME_VTNR = "VTNR";

    // Die importierbaren DIALOG-Tabellen:
    public static final String IMPORT_TABLENAME_TS1 = TABLE_NAME_PREFIX + DIALOG_TABLENAME_TS1;       // Teilestamm Grunddaten ohne Sprachschlüssel
    public static final String IMPORT_TABLENAME_TS2 = TABLE_NAME_PREFIX + DIALOG_TABLENAME_TS2;       // Teilestamm Bennenung und Bemerkung mit Sprachschlüssel   <<=============== mit MULTILANG-Feld
    public static final String IMPORT_TABLENAME_TS6 = TABLE_NAME_PREFIX + DIALOG_TABLENAME_TS6;       // Teilestamm Werkstoffdaten
    public static final String IMPORT_TABLENAME_GEWS = TABLE_NAME_PREFIX + DIALOG_TABLENAME_GEWS;     // Gewichte, ZGS-bezogen
    public static final String IMPORT_TABLENAME_VTNR = TABLE_NAME_PREFIX + DIALOG_TABLENAME_VTNR;     // Teilestammm V, Zusatzdaten von After Sales

    // Die importierbaren Spalten der DIALOG-Tabelle: [T10RTS1], Teilestamm Grunddaten ohne Sprachschlüssel
    public static final String TS1_TEIL = "TS1_TEIL";
    public static final String TS1_SDA = "TS1_SDA";
    public static final String TS1_SDB = "TS1_SDB";
    public static final String TS1_ZBKZ = "TS1_ZBKZ";
    public static final String TS1_FARKZ = "TS1_FARKZ";
    public static final String TS1_EHM = "TS1_EHM";
    public static final String TS1_EATTR = "TS1_EATTR";
    public static final String TS1_DOKKZ = "TS1_DOKKZ";
    public static final String TS1_FGW1 = "TS1_FGW1";
    public static final String TS1_ZGS = "TS1_ZGS";
    public static final String TS1_ZDATUM = "TS1_ZDATUM";
    public static final String TS1_ZSIEHE = "TS1_ZSIEHE";
    public static final String TS1_ETKZ = "TS1_ETKZ";
    public static final String TS1_FGST = "TS1_FGST";
    public static final String TS1_ZBEZUG = "TS1_ZBEZUG";
    public static final String TS1_VERKSNR = "TS1_VERKSNR";

    // Die importierbaren Spalten der DIALOG-Tabelle: [T10RTS2], Teilestamm Bennenung und Bemerkung mit Sprachschlüssel
    public static final String TS2_TEIL = "TS2_TEIL";
    public static final String TS2_BEN = "TS2_BEN";
    public static final String TS2_SDA = "TS2_SDA";
    public static final String TS2_SDB = "TS2_SDB";
    public static final String TS2_SPS = "TS2_SPS";

    // Die importierbaren Spalten der DIALOG-Tabelle: [T10RTS6], Teilestamm Werkstoffdaten
    public static final String TS6_TEIL = "TS6_TEIL";
    public static final String TS6_SDA = "TS6_SDA";
    public static final String TS6_SDB = "TS6_SDB";
    public static final String TS6_WEZ = "TS6_WEZ";

    // Die importierbaren Spalten der DIALOG-Tabelle: [T10RVTNR], Teilestammm V, Zusatzdaten von After Sales
    public static final String VTNR_TEIL = "VTNR_TEIL";
    public static final String VTNR_SDATB = "VTNR_SDATB";
    public static final String VTNR_SPRN = "VTNR_SPRN";
    public static final String VTNR_RECYKL = "VTNR_RECYKL";
    public static final String VTNR_BNR = "VTNR_BNR";
    public static final String VTNR_TBDT = "VTNR_TBDT";
    public static final String VTNR_ITEXT = "VTNR_ITEXT";
    public static final String VTNR_ERST = "VTNR_ERST";

    // Die importierbaren Spalten der DIALOG-Tabelle: [T10RGEWS], Gewichte, ZGS-bezoge
    public static final String GEWS_TEIL = "GEWS_TEIL";
    public static final String GEWS_SDB = "GEWS_SDB";
    public static final String GEWS_GEWGEW = "GEWS_GEWGEW";
    public static final String GEWS_PROGEW = "GEWS_PROGEW";
    public static final String GEWS_ZGS = "GEWS_ZGS";

    // Normale Variablen:
    private IMPORT_TYPE importType;
    // Die Primärschlüssel für die jeweiligen Import-Daten
    private String[] primaryKeys_TS1_ImportData;
    private String[] primaryKeys_TS2_ImportData;
    private String[] primaryKeys_TS6_ImportData;
    private String[] primaryKeys_VTNR_ImportData;
    private String[] primaryKeys_GEWS_ImportData;
    // Die Zuordnungstabellen
    private HashMap<String, String> ts1Mapping;
    private HashMap<String, String> ts2Mapping;
    private HashMap<String, String> ts6Mapping;
    private HashMap<String, String> vtnrMapping;
    private HashMap<String, String> gewsMapping;

    private final List<Ts1DataEmptyKemContainer> ts1DataWithEmptyKEM = new DwList<>();
    private iPartsDataDialogDataList modifiedDialogDataList = new iPartsDataDialogDataList();
    private int lastSkippedRecordNo = -1;
    private boolean importToDB = true; //false; //sollen die Daten abgespeichert werden?
    private MasterDataImportHelper helper;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MasterDataDialogImporter(EtkProject project) {
        super(project, DD_M_ADD,
              new FilesImporterFileListType(IMPORT_TABLENAME_TS1, DD_M_TS1, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }, false),    // Teilestamm Grunddaten ohne Sprachschlüssel
              new FilesImporterFileListType(IMPORT_TABLENAME_TS2, DD_M_TS2, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }, false),    // Teilestamm Bennenung und Bemerkung mit Sprachschlüssel   <<=============== mit MULTILANG-Feld
              new FilesImporterFileListType(IMPORT_TABLENAME_TS6, DD_M_TS6, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }, false),    // Teilestamm Werkstoffdaten
                /* new FilesImporterFileListType(IMPORT_TABLENAME_TS7, DD_M_TS7, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }, false),    // Teilestamm Änderungstexte mit Sprachschlüssel            <<=============== mit MULTILANG-Feld */
              new FilesImporterFileListType(IMPORT_TABLENAME_VTNR, DD_M_VTNR, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }, false),  // Teilestammm V, Zusatzdaten von After Sales
              new FilesImporterFileListType(IMPORT_TABLENAME_GEWS, DD_M_GEWS, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }, false)   // Gewichte, ZGS-bezogen
        );

        this.importType = IMPORT_TYPE.IMPORT_UNKNOWN;

        initMapping();
    }

    /**
     * Das Mapping für die DIALOG-Felder in die Datenbanktabelle [MAT]
     */
    private void initMapping() {
        ts1Mapping = new HashMap<>();
        ts2Mapping = new HashMap<>();
        ts6Mapping = new HashMap<>();
        vtnrMapping = new HashMap<>();
        gewsMapping = new HashMap<>();

        // Tabelle: [T10RTS1], Teilestamm Grunddaten ohne Sprachschlüssel
        ts1Mapping.put(FIELD_M_MATNR, TS1_TEIL);
        ts1Mapping.put(FIELD_M_BESTNR, TS1_TEIL);
        ts1Mapping.put(FIELD_M_ASSEMBLYSIGN, TS1_ZBKZ);
        ts1Mapping.put(FIELD_M_VARIANT_SIGN, TS1_FARKZ);
        ts1Mapping.put(FIELD_M_QUANTUNIT, TS1_EHM);
        ts1Mapping.put(FIELD_M_SECURITYSIGN, TS1_EATTR);
        ts1Mapping.put(FIELD_M_VEDOCSIGN, TS1_EATTR);
        ts1Mapping.put(FIELD_M_ESD_IND, TS1_EATTR);
        ts1Mapping.put(FIELD_M_THEFTRELINFO, TS1_EATTR);
        ts1Mapping.put(FIELD_M_CERTREL, TS1_EATTR);
        ts1Mapping.put(FIELD_M_WEIGHTCALC, TS1_FGW1);
        ts1Mapping.put(FIELD_M_IMAGESTATE, TS1_ZGS);
        ts1Mapping.put(FIELD_M_IMAGEDATE, TS1_ZDATUM);
        ts1Mapping.put(FIELD_M_REFSER, TS1_ZSIEHE);
        ts1Mapping.put(FIELD_M_ETKZ, TS1_ETKZ);
        ts1Mapping.put(FIELD_M_RELEASESTATE, TS1_FGST);
        ts1Mapping.put(FIELD_M_RELATEDPIC, TS1_ZBEZUG);
        ts1Mapping.put(FIELD_M_DOCREQ, TS1_DOKKZ);
        ts1Mapping.put(FIELD_M_VERKSNR, TS1_VERKSNR);

        primaryKeys_TS1_ImportData = new String[]{ TS1_TEIL };

        // Tabelle: [T10RTS2], Teilestamm Bennenung und Bemerkung mit Sprachschlüssel
        ts2Mapping.put(FIELD_M_MATNR, TS2_TEIL);
        ts2Mapping.put(FIELD_M_BESTNR, TS2_TEIL);

        primaryKeys_TS2_ImportData = new String[]{ TS2_TEIL };

        // Tabelle: [T10RTS6], Teilestamm Werkstoffdaten
        ts6Mapping.put(FIELD_M_MATNR, TS6_TEIL);
        ts6Mapping.put(FIELD_M_BESTNR, TS6_TEIL);
        ts6Mapping.put(FIELD_M_MATERIALFINITESTATE, TS6_WEZ);

        primaryKeys_TS6_ImportData = new String[]{ TS6_TEIL };

        // Tabelle: [T10RVTNR], Teilestammm V, Zusatzdaten von After Sales
        vtnrMapping.put(FIELD_M_MATNR, VTNR_TEIL);
        vtnrMapping.put(FIELD_M_BESTNR, VTNR_TEIL);
        vtnrMapping.put(FIELD_M_LAYOUT_FLAG, VTNR_RECYKL);
        vtnrMapping.put(FIELD_M_INTERNAL_TEXT, VTNR_ITEXT);
        vtnrMapping.put(FIELD_M_BASKET_SIGN, VTNR_ERST);

        primaryKeys_VTNR_ImportData = new String[]{ VTNR_TEIL };

        // Tabelle: [T10RGEWS], Gewichte, ZGS-bezoge
        gewsMapping.put(FIELD_M_MATNR, GEWS_TEIL);  //hier keine M_BESTNR nötig, da nicht importiert wird, wenn Datensatz nicht existiert
        gewsMapping.put(FIELD_M_WEIGHTREAL, GEWS_GEWGEW);
        gewsMapping.put(FIELD_M_WEIGHTPROG, GEWS_PROGEW);

        primaryKeys_GEWS_ImportData = new String[]{ GEWS_TEIL };
    }

    @Override
    protected void setCurrentImportTableName(String importTableName) {
        importType = getImportType(importTableName);
    }

    /**
     * Überprüfung, ob die gelieferten Daten die Mindestanforderungen erfüllen.
     * Es wird nach den jeweiligen Importdaten unterschieden.
     *
     * @param importer
     */
    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustExist = null;
        String[] mustHaveData = null;
        switch (importType) {
            case IMPORT_TS1:
                //bestehend aus den primaryKey und den beiden AbfrageFeldern für AS_BIS, KEM_BIS, FRG_DAT_BIS
                mustExist = StrUtils.mergeArrays(primaryKeys_TS1_ImportData, TS1_SDB);
                mustHaveData = primaryKeys_TS1_ImportData;
                break;
            case IMPORT_TS2:
                mustExist = StrUtils.mergeArrays(primaryKeys_TS2_ImportData, TS2_SDB);
                mustHaveData = primaryKeys_TS2_ImportData;
                break;
            case IMPORT_TS6:
                mustExist = StrUtils.mergeArrays(primaryKeys_TS6_ImportData, TS6_SDB);
                mustHaveData = primaryKeys_TS6_ImportData;
                break;
            case IMPORT_VTNR:
                mustExist = StrUtils.mergeArrays(primaryKeys_VTNR_ImportData, VTNR_SDATB);
                mustHaveData = primaryKeys_VTNR_ImportData;
                break;
            case IMPORT_GEWS:
                mustExist = StrUtils.mergeArrays(primaryKeys_GEWS_ImportData, GEWS_SDB);
                mustHaveData = primaryKeys_GEWS_ImportData;
                break;
        }
        importer.setMustExists(mustExist);
        importer.setMustHaveData(mustHaveData);
    }

    /**
     * Checkt die Tabellennamen beim Import
     *
     * @param importer
     * @return
     */
    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        lastSkippedRecordNo = -1;
        if (!importer.getTableNames().isEmpty()) {
            for (String importTableName : importer.getTableNames()) {
                if (getImportType(importTableName) == IMPORT_TYPE.IMPORT_UNKNOWN) { // unbekannte Quell-Tabelle gefunden
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        helper = new MasterDataImportHelper(getProject(), ts1Mapping, DEST_TABLENAME);
    }

    /**
     * Unterscheidet die möglichen Importdaten und leitet die Daten an die entsprechende Importfunktion weiter.
     *
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        switch (importType) {
            case IMPORT_TS1:
                importTS1Record(importRec, recordNo);
                break;
            case IMPORT_TS2:
                importTS2Record(importRec, recordNo);
                break;
            case IMPORT_TS6:
                importTS6Record(importRec, recordNo);
                break;
            case IMPORT_VTNR:
                importVTNRRecord(importRec, recordNo);
                break;
            case IMPORT_GEWS:
                importGEWSRecord(importRec, recordNo);
                break;
            default:
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen", String.valueOf(recordNo),
                                                            translateForLog("!!Importtyp"), importType.name()),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                break;
        }

    }

    /**
     * Importiert die Werte aus [TS1].
     * Sollte ein Materialdatensatz existerien, wird er up-ge-date-d, ansonsten wird ein neuer DS angelegt.
     * Die Importtabelle enthält >>KEINEN<< Sprachschlüssel!
     * Ohne die Prüfung auf eine leere KEM
     *
     * @param importRec
     * @param recordNo
     * @param helper
     */
    private void importTS1RecordWithoutEmptyKEMCheck(Map<String, String> importRec, int recordNo, MasterDataImportHelper helper) {
        // Die Daten nur übernehmen, wenn es der letzte gültige Datensatz (Datum bis = 15x'9') ist.
        if (isRecordValidForImport(importRec, recordNo, TS1_SDB)) {
            // Die Materialnummer aus dem importRecord extrahieren
            EtkDataPart part = getDataPart(helper, importRec, TS1_TEIL, true);
            String existingETKZ = part.getFieldValue(FIELD_M_ETKZ);
            //zusätzlicher Helper für das Aufsplitten von TS1_EATTR und das Abgleichen von BOM-DB und DIALOG Datensätzen
            MasterDataImportHelperTS1 helperTS1 = new MasterDataImportHelperTS1(getProject(), ts1Mapping, DEST_TABLENAME);

            iPartsDIALOGLanguageDefs langDef = iPartsDIALOGLanguageDefs.DIALOG_DE; // Hat keinen Sprachschlüssel ==> Default [DE]
            helperTS1.importHelper = helper;

            // ... die Daten übernehmen.
            helperTS1.fillOverrideCompleteDataForDIALOGReverse(part, importRec, langDef);

            // Ab Daimler-8546 soll zusätzlich der alte ETKZ Wert gespeichert werden.
            part.setFieldValue(FIELD_M_ETKZ_OLD, existingETKZ, DBActionOrigin.FROM_EDIT);

            // Befülle das THEFTREL Flag abhängig vom THEFTRELINFO Wert
            if (part instanceof iPartsDataPart) {
                helperTS1.setTheftRelFlagForDataPart((iPartsDataPart)part);
            }

            // Ab DAIMLER-9068 darf nur der Änderungsdienst Einträge in DA_DIALOG_CHANGES erzeugen, die Urladung nicht.
            if (isDIALOGDeltaDataImport()) {
                String ts1ETKZ = helper.handleValueOfSpecialField(TS1_ETKZ, importRec);
                if ((ts1ETKZ.equals("E") && !existingETKZ.equals("E")) || (!ts1ETKZ.equals("E") && existingETKZ.equals("E"))) {
                    // DAIMLER-7780+DAIMLER-7809: Das Teil wird zum Ersatzteil oder ist ab jetzt kein Ersatzteil mehr. In
                    // diesen Fällen muss pro Verwendung vom Material in der AS-Stückliste ein Eintrag in DA_DIALOG_CHANGES
                    // für den entsprechenden BCTE-Schlüssel angelegt werden.
                    Set<String> bcteKeys = getBCTEKeysForPartUsage(part);
                    iPartsDataDIALOGChangeList dataDIALOGChangeList = new iPartsDataDIALOGChangeList();
                    boolean importRelevantSeriesFound = false;
                    for (String bcteKey : bcteKeys) {
                        iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(bcteKey);
                        // Existiert der BCTE Schlüssel nur in einer PSK Stückliste, dann soll kein DA_DIALOG_CHANGES
                        // Eintrag angelegt werden
                        if ((bctePrimaryKey != null) && !helper.getAsUsageHelper().checkIfOnlyPSKProducts(bctePrimaryKey, false)) {
                            // Ab DAIMLER-9068 dürfen nur versorgungsrelevante Baureihen einen Eintrag in DA_DIALOG_CHANGES erzeugen.
                            if (helperTS1.checkImportRelevanceForSeries(bctePrimaryKey.seriesNo, getInvalidSeriesSet(), null)) {
                                iPartsDataDIALOGChange dataDialogChanges = helper.createChangeRecord(iPartsDataDIALOGChange.ChangeType.MAT_ETKZ,
                                                                                                     part.getAsId(),
                                                                                                     bctePrimaryKey.seriesNo,
                                                                                                     bcteKey, "", "");
                                dataDIALOGChangeList.add(dataDialogChanges, DBActionOrigin.FROM_EDIT);
                                importRelevantSeriesFound = true;
                            }
                        }
                    }
                    if (importRelevantSeriesFound) {
                        if (importToDB) {
                            dataDIALOGChangeList.saveToDB(getProject());
                        }
                    } else {
                        if (bcteKeys.isEmpty()) {
                            getMessageLog().fireMessage(translateForLog("!!Kein Eintrag als DIALOG-Änderung für Record %1: Materialnummer \"%2\" wird in keiner Retail-Stückliste verwendet.",
                                                                        String.valueOf(recordNo), part.getAsId().getMatNr()),
                                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                        } else {
                            getMessageLog().fireMessage(translateForLog("!!Kein Eintrag als DIALOG-Änderung für Record %1: Alle Baureihen für die Materialnummer \"%2\" sind als \"nicht versorgungsrelevant\" gekennzeichnet.",
                                                                        String.valueOf(recordNo), part.getAsId().getMatNr()),
                                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                        }
                    }
                }

                // DAIMLER-15256 Doku-Relevanz auf "nicht festgelegt" setzen, wenn sich ETKZ Stamm von V auf E ändert
                if (existingETKZ.equals("V") && ts1ETKZ.equals("E")) {
                    iPartsDataDialogDataList dialogDataList = iPartsDataDialogDataList.loadDialogDataForMatNr(getProject(), part.getAsId().getMatNr());
                    for (iPartsDataDialogData dialogData : dialogDataList) {
                        String dialogDocuRelevant = dialogData.getFieldValue(FIELD_DD_DOCU_RELEVANT);
                        if (dialogDocuRelevant.equals(iPartsDocuRelevant.DOCU_RELEVANT_NO.getDbValue()) || dialogDocuRelevant.equals(iPartsDocuRelevant.DOCU_RELEVANT_NOT_YET.getDbValue())) {
                            dialogData.setFieldValue(FIELD_DD_DOCU_RELEVANT, iPartsDocuRelevant.DOCU_RELEVANT_NOT_SPECIFIED.getDbValue(),
                                                     DBActionOrigin.FROM_EDIT);
                            modifiedDialogDataList.add(dialogData, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
            }
            updateLastModifiedIfNewer(importRec, TS1_SDA, part, helper);

            // Neue Zuordnung speichern.
            doSaveToDB(part);
        }
    }

    private void updateLastModifiedIfNewer(Map<String, String> importRec, String sdaFieldName, EtkDataPart part, MasterDataImportHelper helper) {
        // M_LAST_MODIFIED nur setzen falls SDA > M_LAST_MODIFIED
        String releaseDate = helper.handleValueOfSpecialField(sdaFieldName, importRec);
        String lastModified = part.getFieldValue(FIELD_M_LAST_MODIFIED);
        if (releaseDate.compareTo(lastModified) > 0) {
            // LAST_MODIFIED setzen für zukünftige DIALOG/BOM-DB Importe
            part.setFieldValue(FIELD_M_LAST_MODIFIED, releaseDate, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Importiert die TS1 Daten. Prüft vorher auf eine Leere KEM ab
     *
     * @param importRec
     * @param recordNo
     */
    private void importTS1Record(Map<String, String> importRec, int recordNo) {
        // Im Änderungsdienst: Datensätze mit leerer KEM sollen erst nach den Datensätzen mit valider KEM importiert werden
        String datasetKEM = helper.getDatasetKem(importRec);
        if (isDIALOGDeltaDataImport() && datasetKEM.isEmpty()) {
            ts1DataWithEmptyKEM.add(new Ts1DataEmptyKemContainer(importRec, recordNo));
            return;
        }

        importTS1RecordWithoutEmptyKEMCheck(importRec, recordNo, helper);
    }

    /**
     * Liefert alle BCTE-Schlüssel aus AS-Stücklisten, in denen das übergebene Teil benutzt wird
     *
     * @param part
     * @return
     */
    private Set<String> getBCTEKeysForPartUsage(EtkDataPart part) {
        String[] selectFields = { FIELD_K_SOURCE_GUID };
        String[] whereFields = { FIELD_K_MATNR, FIELD_K_SOURCE_TYPE };
        String[] whereValues = { part.getAsId().getMatNr(), iPartsEntrySourceType.DIALOG.getDbValue() };
        DBDataObjectAttributesList allSourceContexts = getProject().getDbLayer().getAttributesList(TABLE_KATALOG, selectFields,
                                                                                                   whereFields, whereValues,
                                                                                                   ExtendedDataTypeLoadType.NONE,
                                                                                                   false, true);
        Set<String> bcteKeys = new HashSet<>();
        for (DBDataObjectAttributes sourceContext : allSourceContexts) {
            bcteKeys.add(sourceContext.getFieldValue(FIELD_K_SOURCE_GUID));
        }
        return bcteKeys;
    }

    /**
     * Importiert die Werte aus [TS2].
     * Sollte ein Materialdatensatz existerien, wird er up-ge-date-d, ansonsten wird ein neuer DS angelegt.
     * Die Importtabelle enthält einen Sprachschlüssel!
     *
     * @param importRec
     * @param recordNo
     */
    private void importTS2Record(Map<String, String> importRec, int recordNo) {
        if (isRecordValidForImport(importRec, recordNo, TS2_SDB)) {
            importDataPart(importRec, recordNo, ts2Mapping, TS2_TEIL, TS2_SPS, TS2_SDA);
        }
    }

    /**
     * Erzeugt eine {@link EtkDataPart} Objekt und importiert es
     *
     * @param importRec
     * @param recordNo
     * @param mapping
     * @param fieldName
     * @param langFieldName
     * @param sdaFieldName
     */
    private void importDataPart(Map<String, String> importRec, int recordNo,
                                HashMap<String, String> mapping, String fieldName, String langFieldName, String sdaFieldName) {
        EtkDataPart part = createAndFillPart(importRec, recordNo, mapping, fieldName, langFieldName);
        if (part != null) {
            if (StrUtils.isValid(sdaFieldName)) {
                MasterDataImportHelper helper = new MasterDataImportHelper(getProject(), mapping, DEST_TABLENAME);
                updateLastModifiedIfNewer(importRec, sdaFieldName, part, helper);
            }
            doSaveToDB(part);
        }
    }

    /**
     * Importiert die Werte aus [TS6].
     * Sollte ein Materialdatensatz existerien, wird er up-ge-date-d, ansonsten wird ein neuer DS angelegt.
     * Die Importtabelle enthält >>KEINEN<< Sprachschlüssel!
     *
     * @param importRec
     * @param recordNo
     */
    private void importTS6Record(Map<String, String> importRec, int recordNo) {
        if (isRecordValidForImport(importRec, recordNo, TS6_SDB)) {
            importDataPart(importRec, recordNo, ts6Mapping, TS6_TEIL, "", TS6_SDA);
        }
    }

    /**
     * Importiert die Werte aus [VTNR].
     * Sollte ein Materialdatensatz existerien, wird er up-ge-date-d, ansonsten wird ein neuer DS angelegt.
     * Die Importtabelle enthält >>KEINEN<< Sprachschlüssel!
     *
     * @param importRec
     * @param recordNo
     */
    private void importVTNRRecord(Map<String, String> importRec, int recordNo) {
        if (isRecordValidForImport(importRec, recordNo, VTNR_SDATB)) {
            // Hier der Check, ob das Teil schon einen sprachneutralen text hat
            EtkDataPart part = createAndFillPart(importRec, recordNo, vtnrMapping, VTNR_TEIL, "");
            if (part != null) {
                String neutralText = importRec.get(VTNR_SPRN);
                // DAIMLER-8326: in Edit gepflegten neutralText nicht überschreiben
                boolean isModifiedByEdit = part.getFieldValueAsBoolean(FIELD_M_ADDTEXT_EDITED);
                if (!isModifiedByEdit && (neutralText != null)) {
                    EtkMultiSprache sprache = part.getFieldValueAsMultiLanguage(FIELD_M_ADDTEXT);
                    if (neutralText.isEmpty() && !sprache.isEmpty()) {
                        // Ist der neue Text leer, soll ein bestehender Text entfernt werden
                        sprache.clear();
                        part.setFieldValueAsMultiLanguage(FIELD_M_ADDTEXT, sprache, DBActionOrigin.FROM_EDIT);
                    } else if ((sprache.isEmpty() && !neutralText.isEmpty()) || !sprache.getText(iPartsDIALOGLanguageDefs.DIALOG_DE.getDbValue().getCode()).equals(neutralText)) {
                        // Ist kein Text vorhanden oder der neue Text ungleich dem alten, dann muss der neuen angelegt/geladen
                        // und an das Teil gehängt werden
                        sprache.setText(Language.DE, neutralText);
                        // neuen Text anlegen oder laden
                        DictImportTextIdHelper importHelper = new DictImportTextIdHelper(getProject());
                        importHelper.handleNeutralTextWithCache(sprache, TableAndFieldName.make(TABLE_MAT, FIELD_M_ADDTEXT));
                        part.setFieldValueAsMultiLanguage(FIELD_M_ADDTEXT, sprache, DBActionOrigin.FROM_EDIT);
                    }
                }
                doSaveToDB(part);
            }
        }
    }

    /**
     * Importiert die Werte aus [GEWS].
     * Sollte ein Materialdatensatz existerien, wird er up-ge-date-d, ansonsten wird ein neuer DS angelegt.
     * Die Importtabelle enthält >>KEINEN<< Sprachschlüssel!
     *
     * @param importRec
     * @param recordNo
     */
    private void importGEWSRecord(Map<String, String> importRec, int recordNo) {
        if (isRecordValidForImport(importRec, recordNo, GEWS_SDB)) {
            // Die Materialnummer aus dem importRecord extrahieren
            MasterDataImportHelper helper = new MasterDataImportHelper(getProject(), gewsMapping, DEST_TABLENAME);
            String tmpZGS = importRec.get(GEWS_ZGS);
            EtkDataPart part = getDataPart(helper, importRec, GEWS_TEIL, false);
            if (!part.loadFromDB(part.getAsId())) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen (TS1 Datensatz nicht gefunden)", String.valueOf(recordNo),
                                                            GEWS_ZGS, tmpZGS),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return;
            } else {
                boolean sameZGSValue;
                String tmpImagestate = part.getFieldValue(FIELD_M_IMAGESTATE);
                // Falls beide Werte numerisch sind, kann es sein, dass der ZGS führende 0 besitzt oder auch nicht.
                if (StrUtils.isDigit(tmpImagestate) && StrUtils.isDigit(tmpZGS)) {
                    int currentValue = StrUtils.strToIntDef(tmpImagestate, -1);
                    int newValue = StrUtils.strToIntDef(tmpZGS, -1);
                    sameZGSValue = currentValue == newValue;
                } else {
                    sameZGSValue = tmpImagestate.equals(tmpZGS);
                }
                if (!sameZGSValue) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen (ZGS stimmt nicht überein. In DB: \"%4\")", String.valueOf(recordNo),
                                                                GEWS_ZGS, tmpZGS, tmpImagestate),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    reduceRecordCount();
                    return;
                }
            }
            // Die Daten übernehmen
            helper.fillOverrideCompleteDataForDIALOGReverse(part, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE); // Hat keinen Sprachschlüssel ==> Default [DE]

            // Neue Zuordnung speichern.
            doSaveToDB(part);
        }
    }

    /**
     * Materialdatensatz aus importRec erzeugen.
     * Wenn Sprachkennzeichen vorhanden, werden nur Records mit deutschem Kennzeichen berücksichtigt.
     *
     * @param importRec
     * @param recordNo
     * @param mapping
     * @param fieldName
     * @param langFieldName Feldname das Sprachkennzeichen enthält, z.B. "DIALOG_DE"
     */
    private EtkDataPart createAndFillPart(Map<String, String> importRec, int recordNo,
                                          HashMap<String, String> mapping, String fieldName, String langFieldName) {
        // wenn es ein Feld mit Sprachkennzeichen gibt, muss es DIALOG_DE sein, sonst wird der Datensatz ignoriert.
        MasterDataImportHelper helper = new MasterDataImportHelper(getProject(), mapping, DEST_TABLENAME);
        iPartsDIALOGLanguageDefs langDef = iPartsDIALOGLanguageDefs.DIALOG_DE; // Hat keinen Sprachschlüssel ==> Default [DE]
        if (!langFieldName.isEmpty()) {
            // Sprachdefinition des Records holen ...
            langDef = iPartsDIALOGLanguageDefs.getType(helper.handleValueOfSpecialField(langFieldName, importRec));
            // ... und vom deutschen Datensatz ...
            if (!langFieldName.equals(TS2_SPS) && !langDef.equals(iPartsDIALOGLanguageDefs.DIALOG_DE)) { // Bei TS2 alle Sprachen importieren
                if (lastSkippedRecordNo == -1) {
                    lastSkippedRecordNo = recordNo;
                }
                reduceRecordCount();
                return null;
            }
        }

        // Loggen der übersprungenen Datensätze
        if (lastSkippedRecordNo != -1) {
            if ((lastSkippedRecordNo - recordNo - 1) == 0) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 wegen falschem %2 (nicht DE) übersprungen", String.valueOf(recordNo),
                                                            langFieldName),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            } else {
                getMessageLog().fireMessage(translateForLog("!!Records %1 bis %2 wegen falschem %3 (nicht DE) übersprungen", String.valueOf(lastSkippedRecordNo),
                                                            String.valueOf(recordNo - 1), langFieldName),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
        }
        lastSkippedRecordNo = -1;

        // Material aus DB holen (falls vorhanden) und mit Daten aus Import-Record befüllen
        EtkDataPart part = getDataPart(helper, importRec, fieldName, true);
        helper.fillOverrideCompleteDataForDIALOGReverse(part, importRec, langDef);

        if (langFieldName.equals(TS2_SPS)) {
            handleConstructionDescription(importRec, helper, langDef, part);
        }

        if (fieldName.equals(VTNR_TEIL)) {
            if (!handleETDescription(importRec, recordNo, helper, langDef, part)) {
                cancelImport();
                return null;
            }
        }
        return part;
    }

    /**
     * Sammelroutine für saveToDb, damit vorher noch M_SOURCE gesetzt werden kann
     *
     * @param part
     */
    private void doSaveToDB(EtkDataPart part) {
        if (importToDB) {
            part.addSetOfEnumValueToFieldValue(FIELD_M_SOURCE, iPartsImportDataOrigin.DIALOG.getOrigin(), DBActionOrigin.FROM_EDIT);
            saveToDB(part);
        }
    }

    private boolean handleETDescription(Map<String, String> importRec, int recordNo, MasterDataImportHelper helper, iPartsDIALOGLanguageDefs langDef, EtkDataPart part) {
        // Falls die Text-Id von SRM gesetzt wurde, darf kein anderer Importer diesen Text ändern
        if (iPartsMaterialImportHelper.hasSRMTextId(part)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1: Benennung für Teilenummer %2 konnte nicht übernommen werden, da sie schon von SRM gesetzt wurde!",
                                                        String.valueOf(recordNo), part.getAsId().getMatNr()),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return true;
        }

        DictImportTextIdHelper importHelper = new DictImportTextIdHelper(getProject());
        boolean success = true;
        EtkMultiSprache multiLang = part.getFieldValueAsMultiLanguage(FIELD_M_TEXTNR);
        String tableDotFieldName = TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR);

        // Gibt es in den import-daten eine Term ID (VTNR_BNR)?
        String importTermID = helper.handleValueOfSpecialField(VTNR_BNR, importRec);
        String importText = helper.handleValueOfSpecialField(VTNR_TBDT, importRec);
        if (!StrUtils.isEmpty(importTermID)) { // ja
            if (multiLang.getTextId().isEmpty()) {
                // keine textId am bestehenden Eintrag -> suche im Lexikon nach zu importierender TermId
                // wenn die termId nicht gefunden wird, dann wird ein Eintrag in UNDEF angelegt
                multiLang.setText(langDef.getDbValue(), importText);
                success = importHelper.searchTextInRSKWithFallbackOnCreateUndef(multiLang, tableDotFieldName, importTermID, false);
            } else {
                iPartsDictPrefixAndSuffix multiLangdictPrefix = DictHelper.getDictPrefix(multiLang.getTextId());
                if (multiLangdictPrefix == iPartsDictPrefixAndSuffix.DICT_INDISTINCT_TEXT_PREFIX) { // ist undef text
                    // suche nach DE Text im Lexikon
                    EtkMultiSprache searchText = multiLang.cloneMe();
                    searchText.setText(langDef.getDbValue(), importText);
                    iPartsDictTextKindId txtKindIdRSK = DictTxtKindIdByMADId.getInstance(getProject()).getRSKTxtKindId(DictTextKindRSKTypes.MAT_AFTER_SALES, tableDotFieldName);
                    success = importHelper.handleDictTextId(DictTextKindRSKTypes.MAT_AFTER_SALES, searchText, "", DictHelper.getRSKForeignSource(),
                                                            tableDotFieldName, txtKindIdRSK, DictHelper.getRSKTextPrefix(DictTextKindRSKTypes.MAT_AFTER_SALES));

                    if (importHelper.hasWarnings()) {
                        // multiLang so lassen wie es ist
                        logWarnings(recordNo, part, importHelper, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                        return true;
                    } else {
                        // wenn gefunden, lösche UNDEF Text
                        iPartsDictTextKindId txtKindIdUndef = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(DictTextKindTypes.INDISTINCT_TEXT, tableDotFieldName);
                        importHelper.deleteDictEntry(txtKindIdUndef, multiLang.getTextId());
                        multiLang = searchText;
                    }
                } else {
                    String multiLangTermID = DictHelper.getDictId(multiLang.getTextId());
                    if (!multiLangTermID.equals(importTermID)) {
                        // importTermID und textId in Multilang sind unterschiedlich => importTermID gewinnt
                        // suche nach DE Text und importTermID im Lexikon
                        EtkMultiSprache searchText = multiLang.cloneMe();
                        searchText.setText(langDef.getDbValue(), importText);
                        iPartsDictTextKindId txtKindIdRSK = DictTxtKindIdByMADId.getInstance(getProject()).getRSKTxtKindId(DictTextKindRSKTypes.MAT_AFTER_SALES, tableDotFieldName);
                        success = importHelper.handleDictTextId(DictTextKindRSKTypes.MAT_AFTER_SALES, searchText, importTermID, DictHelper.getRSKForeignSource(),
                                                                tableDotFieldName, txtKindIdRSK, DictHelper.getRSKTextPrefix(DictTextKindRSKTypes.MAT_AFTER_SALES));
                        if (importHelper.hasWarnings()) {
                            // multiLang so lassen wie es ist
                            logWarnings(recordNo, part, importHelper, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                            return true;
                        } else {
                            multiLang = searchText;
                        }
                    }
                }
            }
        } else {
            // termId ist leer
            getMessageLog().fireMessage(translateForLog("!!Record %1 (Teilenummer %2) TermID sollte in VTNR Daten nicht leer sein.",
                                                        String.valueOf(recordNo), part.getAsId().getMatNr()),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            if (multiLang.getTextId().isEmpty()) {
                // keine TextId
                // suche in Lexikon nach DE Text
                multiLang.setText(langDef.getDbValue(), importText);
                success = importHelper.handleDictTextId(DictTextKindRSKTypes.MAT_AFTER_SALES, multiLang, "", DictHelper.getRSKForeignSource(), tableDotFieldName);
                if (importHelper.hasWarnings()) {
                    // wenn nicht gefunden => bleibt MultiLang OHNE TextId
                    multiLang = new EtkMultiSprache();
                    multiLang.setText(langDef.getDbValue(), importText);
                }
            }
        }
        if (importHelper.hasWarnings()) {
            logWarnings(recordNo, part, importHelper, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);

            if (!success) {
                // Die Textart ist nicht definiert -> da können nur verhaute Daten importiert werden
                return false;
            }
            // wurde kein RSK-Text gefunden, dann trage einfach den DE-Text (ohne Lexikon) ein.
            // beim nächsten Import kann dann ggf. der Lexikon-Eintrag gesetzt werden
        }

        part.setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, multiLang, DBActionOrigin.FROM_EDIT);
        return true;
    }

    public void postImportTask() {
        if (isBufferedSave()) {
            saveBufferListToDB(true);
            setBufferedSave(true);
        }
        if (importType == IMPORT_TYPE.IMPORT_TS1) {
            // Falls es TS1 Datensätze mit leerem KEM gibt,
            // muss hier der Import nochmal mit den gesammelten Daten
            // durchgeführt werden
            if (!ts1DataWithEmptyKEM.isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Behandle %1 Datensätze mit leerer KEM...",
                                                            String.valueOf(ts1DataWithEmptyKEM.size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                for (Ts1DataEmptyKemContainer container : ts1DataWithEmptyKEM) {
                    importTS1RecordWithoutEmptyKEMCheck(container.importRec, container.recordNo, helper);
                }
                ts1DataWithEmptyKEM.clear();  // wichtig!
            }

            // Evtl. veränderte DIALOG-Daten über ein technisches ChangeSet speichern
            if (!modifiedDialogDataList.isEmpty()) {
                iPartsRevisionChangeSet changeSet = saveToTechnicalChangeSet(modifiedDialogDataList, iPartsConst.TECHNICAL_USER_DIALOG_DELTA_SUPPLY);
                if (changeSet != null) {
                    getMessageLog().fireMessage(translateForLog("!!Veränderte DIALOG-Konstruktions-Stücklisteneinträge gespeichert. Änderungsset-ID: %1",
                                                                changeSet.getChangeSetId().getGUID()), MessageLogType.tmlMessage,
                                                MessageLogOption.TIME_STAMP);
                }

                modifiedDialogDataList.clear(DBActionOrigin.FROM_EDIT);
            }
        }
        super.postImportTask();
        helper = null;
    }

    private void logWarnings(int recordNo, EtkDataPart part, DictImportTextIdHelper importHelper, MessageLogOption... options) {
        //Fehler beim Dictionary Eintrag
        for (String str : importHelper.getWarnings()) {
            // Anzeige im LogFenster wurde ausgeschaltet, da sonst zu viele Ausgaben
            getMessageLog().fireMessage(translateForLog("!!Record %1 (Teilenummer %2) bei Lexikonablage: \"%3\"",
                                                        String.valueOf(recordNo), part.getAsId().getMatNr(), str),
                                        MessageLogType.tmlWarning, options);
        }
    }

    /**
     * Konstruktionsbezeichnung aus importRec behandeln
     *
     * @param importRec
     * @param helper
     * @param langDef
     * @param part      iPartsDataPart welches aus importRec befüllt bzw. upgedatet wurde
     */
    private void handleConstructionDescription(Map<String, String> importRec, MasterDataImportHelper helper,
                                               iPartsDIALOGLanguageDefs langDef, EtkDataPart part) {
        // Konstruktionsbezeichnung aus Materialstamm holen (wenn vorhanden) und mit Benennung aus importRec aktualisieren
        EtkMultiSprache multiText = part.getFieldValueAsMultiLanguage(FIELD_M_CONST_DESC);
        if (multiText == null) {
            multiText = new EtkMultiSprache();
        }
        multiText.setText(langDef.getDbValue(), helper.handleValueOfSpecialField(TS2_BEN, importRec));

        // Mit DAIMLER-5192 soll die Entwicklungsbenennung nicht mehr im Lexikon, sondern alle Sprachen direkt am
        // Material gespeichert werden

        part.setFieldValueAsMultiLanguage(FIELD_M_CONST_DESC, multiText, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Material-ID aus importRec erstellen
     *
     * @param helper
     * @param importRec Record mit Materialnummer
     * @param fieldName Feld mit Materialnummer
     * @return
     */
    private iPartsPartId getPartId(MasterDataImportHelper helper, Map<String, String> importRec, String fieldName) {
        String matId = helper.handleValueOfSpecialField(fieldName, importRec);
        return new iPartsPartId(matId, "");
    }

    /**
     * iPartsDataPart aus DB holen oder leeres neues erstellen.
     *
     * @param helper
     * @param importRec Record mit Materialnummer
     * @param fieldName Feld mit Materialnummer
     * @param withLoad  true wenn Material aus DB geladen werden soll
     * @return
     */
    private EtkDataPart getDataPart(MasterDataImportHelper helper, Map<String, String> importRec, String fieldName, boolean withLoad) {
        iPartsPartId partId = getPartId(helper, importRec, fieldName);
        EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), partId.getMatNr(), "");
        if (withLoad) {
            if (!part.existsInDB()) {
                part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            // Verarbeite mögliche ES1 und/oder ES2 Schlüssel
            DIALOGImportHelper.handleESKeysInDataPart(getProject(), part, getMessageLog(), getLogLanguage());
        }
        return part;
    }

    private boolean isRecordValidForImport(Map<String, String> importRec, int recordNo, String fieldName) {
        String value = importRec.get(fieldName);
        if (IMPORT_HISTORY || isFinalStateDateTime(value)) {
            return true;
        }
        getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen", String.valueOf(recordNo),
                                                    fieldName, value),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        reduceRecordCount();
        return false;
    }

    /**
     * Löscht alle vorhandenen Daten für den übergebenen Import-Dateilisten-Typ.
     *
     * @param importFileType
     * @return {@code true} falls das Entfernen erfolgreich war
     */
    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        throw new RuntimeException("removeAllExistingData(" + DEST_TABLENAME + "), not allowed!");
    }

    /**
     * Importiert die Datei für den übergebenen Import-Dateilisten-Typ.
     *
     * @param importFileType
     * @param importFile
     * @return {@code true} falls der Import erfolgreich war
     */
    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        String importTableName = importFileType.getFileListType();
        importType = getImportType(importTableName);

        if (importType != IMPORT_TYPE.IMPORT_UNKNOWN) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        } else {
            return false;
        }
    }

    private IMPORT_TYPE getImportType(String importTableName) {
        if (importTableName.equals(IMPORT_TABLENAME_TS1) || importTableName.equals(DIALOG_TABLENAME_TS1)) {
            return IMPORT_TYPE.IMPORT_TS1;
        } else if (importTableName.equals(IMPORT_TABLENAME_TS2) || importTableName.equals(DIALOG_TABLENAME_TS2)) {
            return IMPORT_TYPE.IMPORT_TS2;
        } else if (importTableName.equals(IMPORT_TABLENAME_TS6) || importTableName.equals(DIALOG_TABLENAME_TS6)) {
            return IMPORT_TYPE.IMPORT_TS6;
        } else if (importTableName.equals(IMPORT_TABLENAME_VTNR) || importTableName.equals(DIALOG_TABLENAME_VTNR)) {
            return IMPORT_TYPE.IMPORT_VTNR;
        } else if (importTableName.equals(IMPORT_TABLENAME_GEWS) || importTableName.equals(DIALOG_TABLENAME_GEWS)) {
            return IMPORT_TYPE.IMPORT_GEWS;
        } else {
            return IMPORT_TYPE.IMPORT_UNKNOWN;
        }
    }

    private static class Ts1DataEmptyKemContainer {

        public Map<String, String> importRec;
        public int recordNo;

        public Ts1DataEmptyKemContainer(Map<String, String> importRec, int recordNo) {
            this.importRec = importRec;
            this.recordNo = recordNo;
        }
    }

    private class MasterDataImportHelper extends DIALOGImportHelper {

        private static final int TERMID_LENGTH = 10;

        public MasterDataImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            //  Umwandlung der Sachnummer vom Speicherformat oder Eingabeformat ins Eingabeformat.
            if (sourceField.equals(TS1_TEIL) || sourceField.equals(TS2_TEIL) || sourceField.equals(TS6_TEIL)
                || sourceField.equals(GEWS_TEIL) || sourceField.equals(VTNR_TEIL)) {
                value = checkNumberInputFormat(value, getMessageLog());
            } else if (sourceField.equals(VTNR_BNR)) {
                value = handleTermId(value);
            } else if (sourceField.equals(TS1_SDA) || sourceField.equals(TS1_SDB) ||
                       sourceField.equals(TS2_SDA) || sourceField.equals(TS6_SDA)) {
                iPartsDialogDateTimeHandler handler = new iPartsDialogDateTimeHandler(value);
                value = handler.getDBDateTime();
            }
            return value;
        }

        /**
         * Im Feld VTNR_BNR stehen 2 TermIds hintereinander.
         * Bsp.: '05000000120500000012'
         * Die erste ID ist eine Vorschlags-, die zweite die richtige TermId. Existiert nur die erste TermId oder stehen
         * keine 2 TermIds im Feld, so wird die TermId zurückgesetzt.
         *
         * @param value
         * @return
         */
        private String handleTermId(String value) {
            if (value.length() < (2 * TERMID_LENGTH)) {
                // keine 2 gültigen TermIds
                return "";
            }
            value = StrUtils.copySubString(value, TERMID_LENGTH, TERMID_LENGTH);
            if (value.length() < TERMID_LENGTH) {
                return "";
            }
            // führende Nullen entfernen
            value = iPartsTermIdHandler.removeLeadingZerosFromTermId(value);
            return value;
        }
    }


    private class MasterDataImportHelperTS1 extends DIALOGImportHelper {

        public MasterDataImportHelper importHelper;

        public MasterDataImportHelperTS1(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        /**
         * damit es nur eine Stelle für handleValueOfSpecialField gibt
         *
         * @param sourceField
         * @param value
         * @return
         */
        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            return importHelper.handleValueOfSpecialField(sourceField, value);
        }

        @Override
        protected String extractCorrespondingSubstring(String sourceField, String destField, String sourceValue) {
            String returnValue = sourceValue;
            // Aus dem Feld [TS1_EATTR] müssen mehrere Werte extrahiert werden, je nach Zielfeld
            if (sourceField.equals(TS1_EATTR)) {
                int sourceValueLength = sourceValue.length();

                // Attribut "Sicherheitskennzeichen" wird mit der ersten Stelle aus [TS1_EATTR] "NN   N  N" gefüllt.
                if ((destField.equals(FIELD_M_SECURITYSIGN) && (sourceValueLength > 0))) {
                    returnValue = sourceValue.substring(0, 1);

                    // Das Attribut "Vedoc-Kennzeichen" wird mit der zweiten Stelle aus [TS1_EATTR] gefüllt.
                } else if ((destField.equals(FIELD_M_VEDOCSIGN) && (sourceValueLength > 1))) {
                    returnValue = sourceValue.substring(1, 2);

                    // Das Attribut ESD-Kennzeichen wird mit der fünften Stelle aus [TS1_EATTR] gefüllt.
                } else if ((destField.equals(FIELD_M_ESD_IND)) && (sourceValueLength > 4)) {
                    returnValue = sourceValue.substring(4, 5).trim();

                    // Das Attribute "Diebstahl relevant" soll mit der sechsten Stelle aus [TS1_EATTR] gefüllt werden.
                } else if (destField.equals(FIELD_M_THEFTRELINFO)) {
                    if (sourceValueLength > 5) {
                        returnValue = sourceValue.substring(5, 6).trim();
                    } else {
                        returnValue = "";
                    }

                    // Ist der Wert leer, dann muss als Information ein "N" eingetragen werden.
                    // Anhand dieses Wertes wird nachher das M_THEFTREL Feld befüllt
                    if (returnValue.isEmpty()) {
                        returnValue = THEFT_REL_FLAG_VALUE_FALSE;
                    }

                    // Das Attribut "Zertifizierungsrelevant" aus Stelle 9 TS1_EATTR.
                } else if ((destField.equals(FIELD_M_CERTREL) && (sourceValueLength > 8))) {
                    returnValue = sourceValue.substring(8, 9);
                }
            }
            return returnValue;
        }
    }
}