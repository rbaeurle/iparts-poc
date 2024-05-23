/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImageList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPicReferenceState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReference;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReferenceList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsPicReferenceId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQPicScheduler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEPCPicReferenceImporter extends AbstractEPCDataImporter {

    private static final String IMAGE_DATE_SPLIT_YEAR = "30";
    private static int MAX_AMOUNT_PIC_REFERENCE = 1000;
    protected static final String MODULE_SUFFIX = "_00001";

    protected static final String PIC_REFERENCE_SEQNUM = "SEQNUM";
    protected static final String PIC_REFERENCE_RESTIMG = "RESTIMG";
    protected static final String PIC_REFERENCE_SUBGRP = "SUBGRP";
    protected static final String PIC_REFERENCE_CALLOUT = "CALLOUT";
    protected static final String PIC_REFERENCE_CONTREC = "CONTREC";
    protected static final String PIC_REFERENCE_SEQNO = "SEQNO";
    protected static final String PIC_REFERENCE_IMGTYPE = "IMGTYPE";
    protected static final String PIC_REFERENCE_ARCHIVED = "ARCHIVED";

    private iPartsDataPicReferenceList dataPicReferenceList;

    boolean importToDB = true;
    boolean deleteData = true;

    public AbstractEPCPicReferenceImporter(EtkProject project, String importName, String fileListName, String tabelname) {
        super(project, importName, fileListName, tabelname, true);
    }

    @Override
    protected HashMap<String, String> initMapping() {
        return new HashMap<>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {

    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        dataPicReferenceList = new iPartsDataPicReferenceList();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        EPCPicReferenceImportHelper helper = new EPCPicReferenceImportHelper(getProject(), getFieldMapping(), getTablename());
        if (isAlreadyCreatedFromMAD(importRec, recordNo)) {
            reduceRecordCount();
            return;
        }
        checkAndDeleteExitingData(importRec, recordNo);
        String moduleName = getModuleName(importRec, recordNo);
        if (StrUtils.isEmpty(moduleName)) {
            reduceRecordCount();
            return;
        }
        String imageSeqNumber = helper.handleValueOfSpecialField(PIC_REFERENCE_SEQNUM, importRec);
        String imageSuffixAndDate = helper.handleValueOfSpecialField(PIC_REFERENCE_RESTIMG, importRec);
        String[] suffixAndDate = StrUtils.toStringArray(imageSuffixAndDate, ".", false);
        String date = null;
        String suffix = null;
        if (suffixAndDate != null) {
            int length = suffixAndDate.length;
            if (length == 1) {
                date = suffixAndDate[1];
            } else if (length > 1) {
                suffix = suffixAndDate[0];
                date = suffixAndDate[1];
            }
        }
        String imageNameSeqNo = helper.handleValueOfSpecialField(PIC_REFERENCE_SEQNO, importRec);
        String imageType = helper.handleValueOfSpecialField(PIC_REFERENCE_IMGTYPE, importRec);
        String imageNumber = getImageNumber(imageType, imageNameSeqNo, suffix, importRec, recordNo);
        if (StrUtils.isEmpty(imageNumber)) {
            reduceRecordCount();
            return;
        }

        EtkDataImage dataImage = EtkDataObjectFactory.createDataImage(getProject(), new AssemblyId(moduleName, ""), imageSeqNumber, imageNumber, "");
        if (!dataImage.existsInDB()) {
            dataImage.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        dataImage.setFieldValue(FIELD_I_IMAGES, imageNumber, DBActionOrigin.FROM_EDIT);
        if (StrUtils.isValid(date)) {
            helper.handleImageDateWithoutFirstTwoYearDigits(dataImage, date, FIELD_I_IMAGEDATE, IMAGE_DATE_SPLIT_YEAR);
        }
        if (importToDB) {
            saveToDB(dataImage);
        }

        iPartsPicReferenceId referenceId = new iPartsPicReferenceId(imageNumber, dataImage.getFieldValue(FIELD_I_IMAGEDATE));
        iPartsDataPicReference dataPicReference = new iPartsDataPicReference(getProject(), referenceId);
        if (!dataPicReference.existsInDB()) {
            dataPicReference.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            dataPicReference.setStatus(iPartsPicReferenceState.NEW);
            iPartsDataPicReferenceList referenceList = iPartsDataPicReferenceList.loadPicReferencesWithoutDate(getProject(), referenceId);
            referenceList.add(dataPicReference, DBActionOrigin.FROM_DB);
            dataPicReferenceList.addAll(MQPicScheduler.getInstance().adjustPicReferenceDatasets(referenceList, importToDB, getProject()), DBActionOrigin.FROM_DB);
        } else {
            dataPicReferenceList.add(dataPicReference, DBActionOrigin.FROM_DB);
        }

        handlePicReferenceList();
    }

    private void handlePicReferenceList() {
        if (dataPicReferenceList.size() > MAX_AMOUNT_PIC_REFERENCE) {
            if (importToDB) {
                MQPicScheduler.getInstance().requestNewPicturesViaFileSystem(dataPicReferenceList);
            }
            dataPicReferenceList = new iPartsDataPicReferenceList();
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (!dataPicReferenceList.isEmpty() && importToDB) {
                MQPicScheduler.getInstance().requestNewPicturesViaFileSystem(dataPicReferenceList);
            }
        }
        super.postImportTask();
    }

    protected void deleteImages(String whereLikeValue) {
        if (deleteData) {
            EtkDataImageList imageList = EtkDataObjectFactory.createDataImageList();
            imageList.searchAndFillWithLike(getProject(), TABLE_IMAGES, null, new String[]{ FIELD_I_TIFFNAME },
                                            new String[]{ whereLikeValue + "*" }, DBDataObjectList.LoadType.ONLY_IDS,
                                            false, DBActionOrigin.FROM_DB);
            if ((imageList.size() > 0) && importToDB) {
                imageList.deleteFromDB(getProject(), true);
            }
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(getTablename())) {
            deleteData = true;
        }
        return true;
    }

    protected abstract void checkAndDeleteExitingData(Map<String, String> importRec, int recordNo);

    protected abstract String getImageNumber(String imageType, String imageNameSeqNo, String suffix, Map<String, String> importRec, int recordNo);

    protected abstract String getModuleName(Map<String, String> importRec, int recordNo);

    protected abstract boolean isAlreadyCreatedFromMAD(Map<String, String> importRec, int recordNo);

    public class EPCPicReferenceImportHelper extends EPCImportHelper {

        public EPCPicReferenceImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
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
