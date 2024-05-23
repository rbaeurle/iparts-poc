/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataEPCFootNoteSaRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsEPCFootNoteSaRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEPCLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * EPC Importer für SA Fußnotenreferenzen
 * Dateiname: SA_FOOTNOTES.CSV
 */
public class EPCSaFootnoteRefImporter extends AbstractEPCDataImporter {

    private static final String TABLENAME = TABLE_DA_EPC_FN_SA_REF;

    private final static String SA_FOOTNOTES_SANUM = "SANUM";
    private final static String SA_FOOTNOTES_FTNTNUM = "FTNTNUM";
    private final static String SA_FOOTNOTES_DESCIDX = "DESCIDX";
    private final static String SA_FOOTNOTES_LISTNUM = "LISTNUM";
    private final static String SA_FOOTNOTES_HAS_WISLINK = "HAS_WISLINK";

    boolean importToDB = true;
    private iPartsDataEPCFootNoteSaRef previousDataObject;

    public EPCSaFootnoteRefImporter(EtkProject project) {
        super(project, "EPC SA-Footnotes", "!!EPC Fußnoten-Referenzen (SA)", TABLENAME, true);
    }

    @Override
    protected HashMap<String, String> initMapping() {
        HashMap<String, String> mapping = new HashMap<>();
        mapping.put(FIELD_DEFS_TEXT_ID, SA_FOOTNOTES_DESCIDX);
        return mapping;
    }

    @Override
    protected String[] getHeaderNames() {
        return new String[]{ SA_FOOTNOTES_SANUM, SA_FOOTNOTES_FTNTNUM, SA_FOOTNOTES_DESCIDX, SA_FOOTNOTES_LISTNUM,
                             SA_FOOTNOTES_HAS_WISLINK };
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] musFields = new String[]{ SA_FOOTNOTES_SANUM, SA_FOOTNOTES_FTNTNUM, SA_FOOTNOTES_DESCIDX };
        importer.setMustExists(musFields);
        importer.setMustHaveData(musFields);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        SaFootnoteRefImportHelper importHelper = new SaFootnoteRefImportHelper(getProject(), getFieldMapping());
        String saNumber = importHelper.handleValueOfSpecialField(SA_FOOTNOTES_SANUM, importRec);
        String footnoteNumber = importHelper.handleValueOfSpecialField(SA_FOOTNOTES_FTNTNUM, importRec);
        String epcTextId = importHelper.handleValueOfSpecialField(SA_FOOTNOTES_DESCIDX, importRec);
        String groupNum = importHelper.handleValueOfSpecialField(SA_FOOTNOTES_LISTNUM, importRec);
        if (StrUtils.isValid(saNumber, footnoteNumber, epcTextId)) {
            iPartsEPCFootNoteSaRefId refId = new iPartsEPCFootNoteSaRefId(saNumber, footnoteNumber);
            iPartsDataEPCFootNoteSaRef refData = new iPartsDataEPCFootNoteSaRef(getProject(), refId);
            if (!refData.existsInDB()) {
                refData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            importHelper.fillOverrideCompleteDataForEPCReverse(refData, importRec, iPartsEPCLanguageDefs.EPC_DE);
            checkAndStoreCurrentData(importHelper, refData, groupNum);
        } else {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen. Werte nicht gültig um einen" +
                                                        " Datensatz zu erzeugen. SA: %2, Fußnotennummer: %3, EPC TextId: %4",
                                                        String.valueOf(recordNo), saNumber, footnoteNumber, epcTextId),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
    }

    private void checkAndStoreCurrentData(SaFootnoteRefImportHelper importHelper, iPartsDataEPCFootNoteSaRef refData, String groupNum) {
        boolean sameStructure = true;
        if (previousDataObject != null) {
            sameStructure = previousDataObject.getAsId().getSaNo().equals(refData.getAsId().getSaNo());
        }
        importHelper.setGroupNumber(previousDataObject, refData, groupNum, FIELD_DEFS_GROUP, sameStructure);
        storePreviousObject();
        previousDataObject = refData;
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
        return removeAllExistingDataForTable(importFileType, TABLENAME);
    }

    private class SaFootnoteRefImportHelper extends EPCImportHelper {

        public SaFootnoteRefImportHelper(EtkProject project, Map<String, String> mapping) {
            super(project, mapping, TABLENAME);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value.equals(EPC_NULL_VALUE)) {
                value = "";
            }

            if (sourceField.equals(SA_FOOTNOTES_SANUM)) {
                value = makeSANumberFromEPCValue(value);
            }
            return value;
        }
    }

}
