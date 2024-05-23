/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EPCSaPicReferenceImporter extends AbstractEPCPicReferenceImporter {

    private static final String SA_PSEUDO_TABLENAME = "SA_PIC_REFERENCE_TABLE";
    private static final String SA_PIC_REFERENCE_SANO = "SANO";
    private static final String SA_PIC_REFERENCE_SANUM = "SANUM";

    private EPCSaPicReferenceImportHelper helper;
    private Set<String> invalidSas;
    private String currentSa;

    public EPCSaPicReferenceImporter(EtkProject project) {
        super(project, "EPC SA-NPG", "!!EPC SA-NPG", SA_PSEUDO_TABLENAME);
    }

    @Override
    protected String[] getHeaderNames() {
        return new String[]{
                SA_PIC_REFERENCE_SANO,
                PIC_REFERENCE_SEQNUM,
                PIC_REFERENCE_RESTIMG,
                PIC_REFERENCE_SUBGRP,
                PIC_REFERENCE_CALLOUT,
                PIC_REFERENCE_CONTREC,
                PIC_REFERENCE_SEQNO,
                PIC_REFERENCE_IMGTYPE,
                PIC_REFERENCE_ARCHIVED
        };
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        helper = new EPCSaPicReferenceImportHelper(getProject());
        invalidSas = new HashSet<>();
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
        helper = null;
        invalidSas = null;
    }

    /**
     * Löschen der SA-spezifischen Bilder, falls der [importRecord] zu einer anderen SA gehört.
     *
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void checkAndDeleteExitingData(Map<String, String> importRec, int recordNo) {
        String saNumber = helper.handleValueOfSpecialField(SA_PIC_REFERENCE_SANO, importRec);
        if (StrUtils.isValid(saNumber)) {
            if ((currentSa == null) || !currentSa.equals(saNumber)) {
                deleteImages(SA_MODULE_PREFIX + saNumber);
            }
            currentSa = saNumber;
        }
    }

    @Override
    protected String getImageNumber(String imageType, String imageNameSeqNo, String suffix, Map<String, String> importRec, int recordNo) {
        String saNumber = helper.handleValueOfSpecialField(SA_PIC_REFERENCE_SANUM, importRec);
        String errorMessage;
        if (imageType.equals("S")) {
            if (StrUtils.isValid(imageType, imageNameSeqNo, suffix, saNumber)) {
                return imageType + saNumber + imageNameSeqNo + suffix;
            } else {
                errorMessage = translateForLog("!!Record %1 übersprungen. Aus IMGTYPE \"%2\", SEQNO \"%3\", " +
                                               "RESTIMG-Suffix \"%4\" und SA \"%5\" konnte keine Bildnummer erzeugt werden.",
                                               String.valueOf(recordNo), imageType, imageNameSeqNo, suffix, saNumber);
            }
        } else {
            errorMessage = translateForLog("!!Record %1 übersprungen. IMGTYPE \"%2\" ist kein gültiger Typ " +
                                           "für das Erzeugen von Bildnummern zur SA \"%3\".",
                                           String.valueOf(recordNo), imageType, saNumber);
        }
        getMessageLog().fireMessage(errorMessage, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        return null;
    }

    @Override
    protected String getModuleName(Map<String, String> importRec, int recordNo) {
        String saNumber = helper.handleValueOfSpecialField(SA_PIC_REFERENCE_SANO, importRec);
        if (StrUtils.isValid(saNumber)) {
            return SA_MODULE_PREFIX + saNumber;
        }
        getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen. Für SA \"%2\"" +
                                                    " konnte keine Modulnummer erzeugt werden.",
                                                    String.valueOf(recordNo), saNumber),
                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        return "";
    }

    @Override
    protected boolean isAlreadyCreatedFromMAD(Map<String, String> importRec, int recordNo) {
        String saNumber = helper.handleValueOfSpecialField(SA_PIC_REFERENCE_SANO, importRec);
        return !helper.isSARelevantForImport(this, saNumber, invalidSas, recordNo);
    }

    private class EPCSaPicReferenceImportHelper extends EPCPicReferenceImportHelper {

        public EPCSaPicReferenceImportHelper(EtkProject project) {
            super(project, new HashMap<String, String>(), SA_PSEUDO_TABLENAME);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            value = super.handleValueOfSpecialField(sourceField, value);
            if (sourceField.equals(SA_PIC_REFERENCE_SANO)) {
                value = makeSANumberFromEPCValue(value);
            }
            return value;
        }
    }
}
