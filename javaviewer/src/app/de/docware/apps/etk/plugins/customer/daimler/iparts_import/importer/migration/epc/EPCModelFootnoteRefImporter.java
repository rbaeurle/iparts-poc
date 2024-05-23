/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataEPCFootNoteCatalogueRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsEPCFootNoteCatalogueRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * EPC Importer für Baumuster Fußnotenreferenzen zu einer KG
 * Dateiname: BM_FOOTNOTES
 */
public class EPCModelFootnoteRefImporter extends AbstractEPCDataImporter {

    private static final String TABLENAME = TABLE_DA_EPC_FN_KATALOG_REF;

    private final static String BM_FOOTNOTES_CATNUM = "CATNUM";
    private final static String BM_FOOTNOTES_GROUPNUM = "GROUPNUM";
    private final static String BM_FOOTNOTES_FTNTNUM = "FTNTNUM";
    private final static String BM_FOOTNOTES_DESCIDX = "DESCIDX";
    private final static String BM_FOOTNOTES_REVVER = "REVVER";
    private final static String BM_FOOTNOTES_LISTNUM = "LISTNUM";
    private final static String BM_FOOTNOTES_HAS_WISLINK = "HAS_WISLINK";

    boolean importToDB = true;
    private iPartsDataEPCFootNoteCatalogueRef previousDataObject;

    public EPCModelFootnoteRefImporter(EtkProject project) {
        super(project, "EPC BM-Footnotes", "!!EPC Fußnoten-Referenzen (BM)", TABLENAME, true);
    }

    @Override
    protected HashMap<String, String> initMapping() {
        // Hier kein Mapping notwendig, da alle Werte im Schlüssel sind
        HashMap<String, String> mapping = new HashMap<>();
        return mapping;
    }

    @Override
    protected String[] getHeaderNames() {
        return new String[]{ BM_FOOTNOTES_CATNUM, BM_FOOTNOTES_GROUPNUM, BM_FOOTNOTES_FTNTNUM, BM_FOOTNOTES_DESCIDX,
                             BM_FOOTNOTES_REVVER, BM_FOOTNOTES_LISTNUM, BM_FOOTNOTES_HAS_WISLINK };
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] musFields = new String[]{ BM_FOOTNOTES_CATNUM, BM_FOOTNOTES_GROUPNUM, BM_FOOTNOTES_FTNTNUM, BM_FOOTNOTES_DESCIDX };
        importer.setMustExists(musFields);
        importer.setMustHaveData(musFields);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        ModelFootnoteRefImportHelper importHelper = new ModelFootnoteRefImportHelper(getProject(), getFieldMapping());
        String productNumber = importHelper.handleValueOfSpecialField(BM_FOOTNOTES_CATNUM, importRec);
        String kgNumber = importHelper.handleValueOfSpecialField(BM_FOOTNOTES_GROUPNUM, importRec);
        String footnoteNumber = importHelper.handleValueOfSpecialField(BM_FOOTNOTES_FTNTNUM, importRec);
        String epcTextId = importHelper.handleValueOfSpecialField(BM_FOOTNOTES_DESCIDX, importRec);
        String groupNum = importHelper.handleValueOfSpecialField(BM_FOOTNOTES_LISTNUM, importRec);
        if (StrUtils.isValid(productNumber, kgNumber, footnoteNumber, epcTextId)) {
            iPartsEPCFootNoteCatalogueRefId refId = new iPartsEPCFootNoteCatalogueRefId(productNumber, kgNumber, footnoteNumber, epcTextId);
            iPartsDataEPCFootNoteCatalogueRef refData = new iPartsDataEPCFootNoteCatalogueRef(getProject(), refId);
            if (!refData.existsInDB()) {
                refData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            checkAndStoreCurrentData(importHelper, refData, groupNum);
        } else {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen. Werte nich gültig um einen" +
                                                        " Datensatz zu erzeugen. Produkt: %2, KG: %3, Fußnotennummer: %4," +
                                                        " EPC TextId: %5", String.valueOf(recordNo), productNumber, kgNumber,
                                                        footnoteNumber, epcTextId),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
    }

    private void checkAndStoreCurrentData(ModelFootnoteRefImportHelper importHelper, iPartsDataEPCFootNoteCatalogueRef refData,
                                          String groupNum) {
        boolean sameStructure = true;
        if (previousDataObject != null) {
            sameStructure = previousDataObject.getAsId().getProductNo().equals(refData.getAsId().getProductNo())
                            && previousDataObject.getAsId().getKgNumber().equals(refData.getAsId().getKgNumber());
        }
        importHelper.setGroupNumber(previousDataObject, refData, groupNum, FIELD_DEFR_GROUP, sameStructure);
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

    private class ModelFootnoteRefImportHelper extends EPCImportHelper {

        public ModelFootnoteRefImportHelper(EtkProject project, Map<String, String> mapping) {
            super(project, mapping, TABLENAME);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value.equals(EPC_NULL_VALUE)) {
                value = "";
            }
            return value;
        }
    }
}
