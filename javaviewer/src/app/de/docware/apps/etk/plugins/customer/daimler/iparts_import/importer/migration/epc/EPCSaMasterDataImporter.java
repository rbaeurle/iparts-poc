/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;


import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindEPCTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * EPC Importer für SA-Stammdaten (SA_DESCS.)
 */

public class EPCSaMasterDataImporter extends AbstractEPCDataImporter {

    // Die Spalten der Importdatei
    private final static String SA_SANUM = "SANUM";
    private final static String SA_SEQNUM = "SEQNUM";
    private final static String SA_CODETWO = "CODETWO";
    private final static String SA_CODEONE = "CODEONE";
    private final static String SA_DESCIDX = "DESCIDX";
    private final static String SA_APPINF = "APPINF";
    private final static String SA_IS_COMPONENT = "IS_COMPONENT";

    boolean importToDB = true;
    private iPartsDataSa previousDataObject;

    // Die Zieltabelle
    protected static final String DEST_TABLENAME = TABLE_DA_SA;

    protected EPCSaMasterDataImporter(EtkProject project) {
        super(project, "EPC SA-Masterdata", "!!EPC SA-Stammdaten", DEST_TABLENAME, true, true);
    }

    @Override
    protected String[] getHeaderNames() {
        return new String[]{
                SA_SANUM,
                SA_SEQNUM,
                SA_CODETWO,
                SA_CODEONE,
                SA_DESCIDX,
                SA_APPINF,
                SA_IS_COMPONENT
        };
    }

    @Override
    protected HashMap<String, String> initMapping() {
        // Hier kein Mapping notwendig, da alle Werte im Schlüssel sind
        return new HashMap<>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] musFields = new String[]{ SA_SANUM };
        importer.setMustExists(musFields);
        importer.setMustHaveData(musFields);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        SaMasterDataImportHelper importHelper = new SaMasterDataImportHelper(getProject(), getFieldMapping());

        String saNumber = importHelper.handleValueOfSpecialField(SA_SANUM, importRec);
        if (!importHelper.isSARelevantForImport(this, saNumber, recordNo, false)) {
            reduceRecordCount();
            return;
        }
        String epcTextId = importHelper.handleValueOfSpecialField(SA_DESCIDX, importRec);
        if (StrUtils.isValid(saNumber, epcTextId)) {
            iPartsSaId saId = new iPartsSaId(saNumber);
            iPartsDataSa saData = new iPartsDataSa(getProject(), saId);
            if (!saData.existsInDB()) {
                saData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            } else {
                // MultiLang nachladen
                saData.getFieldValueAsMultiLanguage(FIELD_DS_DESC);
            }
            EtkMultiSprache epcText = importHelper.searchEPCTextWithEPCId(DictTextKindEPCTypes.SA_DICTIONARY, epcTextId);
            String codes = importHelper.getCodeValueFromMultipleCodeFields(importRec, SA_CODEONE, SA_CODETWO);
            checkAndStoreCurrentData(importHelper, saData, codes, epcText);
        } else {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen. Werte nicht gültig um einen" +
                                                        " Datensatz zu erzeugen. SA: %2, EPC TextId: %3",
                                                        String.valueOf(recordNo), saNumber, epcTextId),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
    }

    private void checkAndStoreCurrentData(EPCImportHelper importHelper, iPartsDataSa saData, String codes, EtkMultiSprache epcText) {
        if ((previousDataObject != null) && previousDataObject.getAsId().equals(saData.getAsId())) {
            // Zur SA kann es mehrere Zeilen (SEQNUM) geben. Die entsprechenden Codezeilen sind dann ebenfalls weiter zu verknüpfen
            codes += "/" + previousDataObject.getFieldValue(FIELD_DS_CODES);
            importHelper.fillSaMasterDataObject(previousDataObject, codes, epcText);
        } else {
            importHelper.fillSaMasterDataObject(saData, codes, epcText);
        }
        storePreviousObject();
        previousDataObject = saData;
    }

    private void storePreviousObject() {
        if ((previousDataObject != null) && importToDB) {
            saveToDB(previousDataObject);
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            storePreviousObject();
        }
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(DEST_TABLENAME)) {
            getProject().getDbLayer().delete(DEST_TABLENAME, new String[]{ FIELD_DS_SOURCE }, new String[]{ iPartsImportDataOrigin.EPC.getOrigin() });
            return true;
        } else {
            return false;
        }
    }

    private class SaMasterDataImportHelper extends EPCImportHelper {

        private SaMasterDataImportHelper(EtkProject project, Map<String, String> mapping) {
            super(project, mapping, DEST_TABLENAME);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value.equals(EPC_NULL_VALUE)) {
                value = "";
            }
            if (sourceField.equals(SA_SANUM)) {
                value = makeSANumberFromEPCValue(value);
            }
            return value;
        }
    }
}
