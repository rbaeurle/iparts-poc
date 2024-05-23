/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Importer für die FTAB Daten.
 */
public class MigrationDialogProductColorTableFactoryFTabImporter extends MigrationDialogProductColorFactoryBaseImporter {

    final static String FTAB_HERK = "FTAB_HERK";  // wird nicht migriert
    final static String FTAB_NR = "FTAB_NR";
    final static String FTAB_ES2 = "FTAB_ES2";
    final static String FTAB_SDA = "FTAB_SDA";
    final static String FTAB_WERK = "FTAB_WERK";
    final static String FTAB_POS = "FTAB_POS";
    final static String FTAB_SDB = "FTAB_SDB";
    final static String FTAB_ADAT1 = "FTAB_ADAT1";
    final static String FTAB_PTAB = "FTAB_PTAB";
    final static String FTAB_PTBI = "FTAB_PTBI";
    final static String FTAB_PEMA = "FTAB_PEMA";
    final static String FTAB_PEMB = "FTAB_PEMB";
    final static String FTAB_ESC = "FTAB_ESC";  // wird nicht migriert
    final static String FTAB_AFNR = "FTAB_AFNR";  // wird nicht migriert
    final static String FTAB_DT = "FTAB_DT";  // wird nicht migriert
    final static String FTAB_FIKZ = "FTAB_FIKZ";
    final static String FTAB_STCA = "FTAB_STCA";
    final static String FTAB_STCB = "FTAB_STCB";
    final static String FTAB_CBED = "FTAB_CBED";
    final static String FTAB_PG = "FTAB_PG";
    final static String FTAB_EDAT = "FTAB_EDAT";  // wird nicht migriert
    final static String FTAB_ADAT = "FTAB_ADAT";  // wird nicht migriert
    final static String FTAB_CBRED = "FTAB_CBRED";  // wird nicht migriert
    final static String FTAB_AB = "FTAB_AB";  // wird nicht migriert
    final static String FTAB_BIS = "FTAB_BIS";  // wird nicht migriert

    protected static final String DATASET_PREFIX = "FTAB";
    private static final String[] headerNames = new String[]{
            FTAB_NR,
            FTAB_ES2,
            FTAB_SDA,
            FTAB_WERK,
            FTAB_POS,
            FTAB_SDB,
            FTAB_ADAT1,
            FTAB_PTAB,
            FTAB_PTBI,
            FTAB_PEMA,
            FTAB_PEMB,
            FTAB_ESC,
            FTAB_AFNR,
            FTAB_DT,
            FTAB_FIKZ,
            FTAB_STCA,
            FTAB_STCB,
            FTAB_CBED,
            FTAB_PG,
            FTAB_EDAT,
            FTAB_ADAT,
            FTAB_CBRED
    };

    public MigrationDialogProductColorTableFactoryFTabImporter(EtkProject project, boolean withHeader) {
        super(project, "DIALOG Color Table Factory (FTAB)", withHeader, headerNames, DATASET_PREFIX,
              new FilesImporterFileListType("table", "!!DIALOG Color Table Factory (FTAB)", true, false, false, new String[]{ MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysImport = new String[]{ FTAB_NR, FTAB_SDA };
        // X9E Mapping
        mappingXXE = new HashMap<>();
        mappingXXE.put(FIELD_DCTC_COLOR_VAR, FTAB_ES2);  //{X9E_FARB}
        mappingXXE.put(FIELD_DCTC_SDATA, FTAB_SDA);      //{X9E_SDA}
        mappingXXE.put(FIELD_DCTC_POS, FTAB_POS);        //{X9E_POS}
        mappingXXE.put(FIELD_DCTC_SDATB, FTAB_SDB);      //{X9E_SDB}
        mappingXXE.put(FIELD_DCTC_CODE, FTAB_CBED);      // Code
        mappingXXE.put(FIELD_DCTC_PGRP, FTAB_PG);        //{X9E_PGKZ}

        // VX9 und WX9 Mapping
        mappingColortableFactory = new HashMap<>();
        mappingColortableFactory.put(FIELD_DCCF_SDATA, FTAB_SDA);
        mappingColortableFactory.put(FIELD_DCCF_SDATB, FTAB_SDB);
        mappingColortableFactory.put(FIELD_DCCF_FACTORY, FTAB_WERK);
        mappingColortableFactory.put(FIELD_DCCF_POS, FTAB_POS);
        mappingColortableFactory.put(FIELD_DCCF_PEMTA, FTAB_PTAB);
        mappingColortableFactory.put(FIELD_DCCF_PEMTB, FTAB_PTBI);
        mappingColortableFactory.put(FIELD_DCCF_PEMA, FTAB_PEMA);
        mappingColortableFactory.put(FIELD_DCCF_PEMB, FTAB_PEMB);
        mappingColortableFactory.put(FIELD_DCCF_STCA, FTAB_STCA);
        mappingColortableFactory.put(FIELD_DCCF_STCB, FTAB_STCB);

    }


    @Override
    protected boolean importXXEDataset(Map<String, String> importRec, int recordNo, String colorTableId, String sdata) {
        FTABImportHelper helper = new FTABImportHelper(getProject(), mappingXXE, importTableInDB);
        // ID und DataObject bauen
        String pos = helper.handleValueOfSpecialField(FTAB_POS, importRec);
        iPartsColorTableContentId id = new iPartsColorTableContentId(colorTableId, pos, sdata);
        iPartsDataColorTableContent dataObject = new iPartsDataColorTableContent(getProject(), id);
        // Falls noch nicht in der DB vorhanden -> Initialisieren mit leeren Werten
        if (!dataObject.existsInDB()) {
            dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            // Bei Neuanlge Status auf RELEASED setzen weil Migration
            dataObject.setFieldValue(FIELD_DCTC_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
        }
        dataObject.setFieldValue(FIELD_DCTC_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);
        helper.fillOverrideCompleteDataForMADReverse(dataObject, importRec, iPartsMADLanguageDefs.MAD_DE);

        // Die Flags "PEM ab/bis auswerten" zunächst auf false setzen (diese werden später vom FBST-Importer bei Bedarf
        // wieder auf true gesetzt)
        dataObject.setFieldValueAsBoolean(FIELD_DCTC_EVAL_PEM_FROM, false, DBActionOrigin.FROM_EDIT);
        dataObject.setFieldValueAsBoolean(FIELD_DCTC_EVAL_PEM_TO, false, DBActionOrigin.FROM_EDIT);

        // Code aus DA_ACC_CODES und AS_CODE werden aus der Coderegel entfernt und in DCTC_CODE_AS gespeichert
        Set<String> removedCodes = new HashSet<>();
        List<String> logMessages = new ArrayList<>();
        String codeString = helper.handleValueOfSpecialField(FTAB_CBED, importRec);
        String reducedCodeString = DaimlerCodes.reduceCodeString(getProject(), codeString, removedCodes, logMessages);
        if (!codeString.equals(reducedCodeString) && dataObject.getFieldValue(FIELD_DCTC_CODE_AS).isEmpty()) {
            dataObject.setFieldValue(FIELD_DCTC_CODE_AS, reducedCodeString, DBActionOrigin.FROM_EDIT);
        }
        for (String logMessage : logMessages) {
            getMessageLog().fireMessage(logMessage, MessageLogType.tmlWarning);
        }

        if (importToDB) {
            // Füge den neuen Datensatz zum Diff hinzu
            putFirst(id);
            saveToDB(dataObject);
        }

        return true;
    }

    @Override
    protected boolean importColortableFactoryDataset(Map<String, String> importRec, int recordNo, String colorTableId,
                                                     String factory, String aDat, String sdata) {
        HashMap<String, String> tempMapping = new HashMap<>(mappingColortableFactory);
        if (currentDatasetId == iPartsFactoryDataTypes.COLORTABLE_CONTENT) {
            tempMapping.put(FIELD_DCCF_ADAT, FTAB_ADAT1);
        }
        FTABImportHelper helper = new FTABImportHelper(getProject(), tempMapping, importTableInDB);
        String pemA = helper.handleValueOfSpecialField(FTAB_PEMA, importRec);
        String pemB = helper.handleValueOfSpecialField(FTAB_PEMB, importRec);
        handleSingleASPemFactoryCacheEntry(helper, colorTableId, factory, pemA, pemB);

        String pos = helper.handleValueOfSpecialField(FTAB_POS, importRec);
        iPartsColorTableFactoryId colorTableFactoryId = makeColorTableFactoryId(colorTableId, pos, factory, aDat, currentDatasetId.getDbValue(), sdata);
        iPartsDataColorTableFactory ctfObj = new iPartsDataColorTableFactory(getProject(), colorTableFactoryId);
        if (!ctfObj.existsInDB()) {
            ctfObj.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        helper.fillOverrideCompleteDataForMADReverse(ctfObj, importRec, iPartsMADLanguageDefs.MAD_DE);
        ctfObj.setFieldValue(FIELD_DCCF_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);
        ctfObj.setFieldValue(FIELD_DCCF_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
        if (importToDB) {
            saveToDB(ctfObj);
        }

        return true;
    }

    @Override
    protected void postImportAction() {
    }

    @Override
    protected void deleteUnprovidedData() {
        iPartsDataColorTableContentList colorTableContentList = new iPartsDataColorTableContentList();
        EtkDataObjectList.FoundAttributesCallback callback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String colortableId = attributes.getFieldValue(FIELD_DCTC_TABLE_ID);
                String pos = attributes.getFieldValue(FIELD_DCTC_POS);
                String sdata = attributes.getFieldValue(FIELD_DCTC_SDATA);
                iPartsColorTableContentId contentId = new iPartsColorTableContentId(colortableId, pos, sdata);
                // Füge den gefundenen Datensatz zum Diff hinzu
                putSecond(contentId);
                // Wenn der gerade hinzugefügte Datensatz in der zweiten Liste existiert,dann wurde er initial nicht importiert
                // -> muss aus der DB gelöscht werden
                return contentOrPartData.getOnlyInSecondItems().containsKey(contentId.toString());
            }
        };
        // Like-Abfrage für alle, die mit QFT[Baureihe] beginnen
        colorTableContentList.searchSortAndFillWithJoin(getProject(), getProject().getDBLanguage(), null,
                                                        new String[]{ TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_TABLE_ID),
                                                                      TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_SOURCE) },
                                                        new String[]{ getColortableSeriesWhereValue(), iPartsImportDataOrigin.MAD.getOrigin() },
                                                        false, null, false, true, callback);
        colorTableContentList.deleteAll(DBActionOrigin.FROM_EDIT);
        if (importToDB) {
            colorTableContentList.saveToDB(getProject());
        }
    }

    protected class FTABImportHelper extends MigrationDialogProductColorFactoryBaseImporter.CTImportHelper {

        public FTABImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(FTAB_PEMA) || sourceField.equals(FTAB_PEMB) || sourceField.equals(FTAB_STCA) || sourceField.equals(FTAB_STCB)) {
                value = value.trim();
            } else {
                value = super.handleValueOfSpecialField(sourceField, value);
            }
            return value;
        }
    }
}
