/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;

public class EPCModelPicReferenceImporter extends AbstractEPCPicReferenceImporter {

    private static final String MODEL_PSEUDO_TABLENAME = "MODEL_PIC_REFERENCE_TABLE";
    private static final String MODEL_PIC_REFERENCE_CATNUM = "CATNUM";
    private static final String MODEL_PIC_REFERENCE_GROUPNUM = "GROUPNUM";

    private EPCModelPicReferenceImportHelper helper;
    private Map<iPartsProductId, Boolean> productRelevanceCache; // Ein kleiner Cache, damit die Produkte nicht immer wieder aus der DB geladen werden.
    private String currentProductAndKG;

    public EPCModelPicReferenceImporter(EtkProject project) {
        super(project, "EPC BM-NPG", "!!EPC BM-NPG", MODEL_PSEUDO_TABLENAME);
    }

    @Override
    protected String[] getHeaderNames() {
        return new String[]{
                MODEL_PIC_REFERENCE_CATNUM,
                MODEL_PIC_REFERENCE_GROUPNUM,
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
        helper = new EPCModelPicReferenceImportHelper(getProject());
        productRelevanceCache = new HashMap<>();
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
        helper = null;
        productRelevanceCache = null;
    }

    /**
     * Löschen der KG-spezifischen Bilder des Produkts, falls der [importRecord] zu einem anderen Produkt
     * oder mindestens zu einer anderen KG gehört.
     *
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void checkAndDeleteExitingData(Map<String, String> importRec, int recordNo) {
        String productNumber = helper.handleValueOfSpecialField(MODEL_PIC_REFERENCE_CATNUM, importRec);
        String kg = helper.handleValueOfSpecialField(MODEL_PIC_REFERENCE_GROUPNUM, importRec);
        if (StrUtils.isValid(productNumber, kg)) {
            String productAndKg = productNumber + "_" + kg;
            if ((currentProductAndKG == null) || !currentProductAndKG.equals(productAndKg)) {
                deleteImages(productAndKg + "_");
            }
            currentProductAndKG = productAndKg;
        }
    }

    @Override
    protected String getImageNumber(String imageType, String imageNameSeqNo, String suffix, Map<String, String> importRec, int recordNo) {
        String kg = helper.handleValueOfSpecialField(MODEL_PIC_REFERENCE_GROUPNUM, importRec);
        String subGroup = helper.handleValueOfSpecialField(PIC_REFERENCE_SUBGRP, importRec);
        String errorMessage;
        if (StrUtils.isValid(imageType, imageNameSeqNo, kg, subGroup) && (imageType.equals("B") || imageType.equals("O"))) {
            if (imageType.equals("B")) {
                if (StrUtils.isValid(suffix)) {
                    return imageType + kg + subGroup + imageNameSeqNo + suffix;
                } else {
                    errorMessage = translateForLog("!!Record %1 übersprungen. Für den Typ \"%2\" konnte aus RESTIMG " +
                                                   "kein Bild-Suffix für die Bildnummer extrahiert werden.",
                                                   String.valueOf(recordNo), imageType);
                }
            } else if (imageType.equals("O")) {
                String productNumber = helper.handleValueOfSpecialField(MODEL_PIC_REFERENCE_CATNUM, importRec);
                if (StrUtils.isValid(productNumber)) {
                    return productNumber + kg + subGroup + imageNameSeqNo;
                } else {
                    errorMessage = translateForLog("!!Record %1 übersprungen. Aus IMGTYPE \"%2\", SEQNO \"%3\", " +
                                                   "RESTIMG-Suffix \"%4\", Produkt \"%5\", KG \"%6\" und TU \"%7\" konnte " +
                                                   "keine Bildnummer erzeugt werden.",
                                                   String.valueOf(recordNo), imageType, imageNameSeqNo, suffix, productNumber, kg, subGroup);
                }
            } else {
                errorMessage = translateForLog("!!Record %1 übersprungen. IMGTYPE \"%2\" ist kein gültiger Typ " +
                                               "für das Erzeugen von Bildnummern zu KG \"%3\" und TU \"%4\".",
                                               String.valueOf(recordNo), imageType, kg, subGroup);
            }
        } else {
            errorMessage = translateForLog("!!Record %1 übersprungen. Aus IMGTYPE \"%2\", SEQNO \"%3\", " +
                                           "RESTIMG-Suffix \"%4\", KG \"%5\" und TU \"%6\" konnte keine Bildnummer erzeugt werden.",
                                           String.valueOf(recordNo), imageType, imageNameSeqNo, suffix, kg, subGroup);
        }
        getMessageLog().fireMessage(errorMessage, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        return null;
    }

    @Override
    protected String getModuleName(Map<String, String> importRec, int recordNo) {
        String productNumber = helper.handleValueOfSpecialField(MODEL_PIC_REFERENCE_CATNUM, importRec);
        String kgNumber = helper.handleValueOfSpecialField(MODEL_PIC_REFERENCE_GROUPNUM, importRec);
        String subGroup = helper.handleValueOfSpecialField(PIC_REFERENCE_SUBGRP, importRec);

        if (StrUtils.isValid(productNumber, kgNumber, subGroup)) {
            return productNumber + "_" + kgNumber + "_" + subGroup + MODULE_SUFFIX;
        }
        getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen. Für Produkt \"%2\", " +
                                                    "KG \"%3\" und TU \"%4\" konnte keine Modulnummer erzeugt werden.",
                                                    String.valueOf(recordNo), productNumber, kgNumber, subGroup),
                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        return "";
    }

    @Override
    protected boolean isAlreadyCreatedFromMAD(Map<String, String> importRec, int recordNo) {
        String productNumber = helper.handleValueOfSpecialField(MODEL_PIC_REFERENCE_CATNUM, importRec);
        // Logische Prüfung mit Ausgabe von Meldungen
        return !helper.isProductRelevantForImport(this, productNumber, productRelevanceCache, recordNo);
    }

    private class EPCModelPicReferenceImportHelper extends EPCPicReferenceImportHelper {

        public EPCModelPicReferenceImportHelper(EtkProject project) {
            super(project, new HashMap<String, String>(), MODEL_PSEUDO_TABLENAME);
        }
    }
}
