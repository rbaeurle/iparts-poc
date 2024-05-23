/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects.iPartsDataExportContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects.iPartsDataExportRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.endpoints.exportpartslist.iPartsWSExportPartsListRequest;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.collections.dwlist.DwList;

import java.util.Calendar;
import java.util.List;

public class iPartsExportTask implements iPartsConst {

    private iPartsDataExportContent exportContent;
    private List<Language> selectedLanguages;
    private List<String> fallbackLanguages;
    private iPartsExportPictureFormat pictureFormat;
    private String customerId;
    private boolean exportAdditionalPartData;
    private boolean exportEinPASData;
    private boolean exportVisualNav;
    private boolean exportPSKData;

    /**
     * Erzeugt ein {@link iPartsExportTask} für den nächsten aus der DB geladenen Exportauftrag
     *
     * @param exportRequest
     * @param exportContent
     * @return
     */
    public static iPartsExportTask createExportTask(iPartsDataExportRequest exportRequest, iPartsDataExportContent exportContent) {
        List<Language> selectedLanguages = new DwList<>();
        for (String exportLanguageCode : exportRequest.getFieldValueAsSetOfEnum(FIELD_DER_LANGUAGES)) {
            selectedLanguages.add(Language.findLanguage(exportLanguageCode));
        }
        if (selectedLanguages.isEmpty()) {
            String firstFallbackLanguage = exportRequest.getDBProject().getDataBaseFallbackLanguages().iterator().next();
            selectedLanguages.add(Language.findLanguage(firstFallbackLanguage));
        }
        boolean includePictures = exportRequest.getFieldValueAsBoolean(FIELD_DER_INCLUDE_PICTURES);
        iPartsExportPictureFormat pictureFormat = iPartsExportPictureFormat.NONE;
        // Der Benutzer möchte explizit ein Bild haben
        if (includePictures) {
            // Erst wird geprüft, ob auch gewünschtes Format durchgegeben wurde
            pictureFormat = iPartsExportPictureFormat.getEnumFromDBValue(exportRequest.getFieldValue(FIELD_DER_PICTURE_FORMAT));
            // Falls nicht, PNG_AND_SVG als default zurückliefern
            if (pictureFormat == iPartsExportPictureFormat.NONE) {
                pictureFormat = iPartsExportPictureFormat.PNG_AND_SVG;
            }
        }

        iPartsExportTask exportTask = new iPartsExportTask();
        exportTask.init(exportContent, pictureFormat, selectedLanguages, exportRequest.getFieldValue(FIELD_DER_CUSTOMER_ID),
                        exportRequest.getFieldValueAsBoolean(FIELD_DER_INCLUDE_MAT_PROPERTIES),
                        exportRequest.getFieldValueAsBoolean(FIELD_DER_INCLUDE_EINPAS),
                        exportRequest.getFieldValueAsBoolean(FIELD_DER_INCLUDE_VISUAL_NAV));
        return exportTask;
    }

    public iPartsExportTask() {
    }

    /**
     * Initialisiert den ExportTask mit den übergebenen Werten
     *
     * @param exportContent
     * @param pictureFormat
     * @param selectedLanguages
     * @param customerId
     * @param exportAdditionalPartData
     */
    public void init(iPartsDataExportContent exportContent, iPartsExportPictureFormat pictureFormat,
                     List<Language> selectedLanguages, String customerId, boolean exportAdditionalPartData,
                     boolean exportEinPASData, boolean exportVisualNav) {
        this.exportContent = exportContent;
        this.selectedLanguages = selectedLanguages;
        if (this.selectedLanguages.isEmpty()) {
            this.selectedLanguages.add(Language.DE);
        }
        this.fallbackLanguages = exportContent.getDBProject().getDataBaseFallbackLanguages();
        this.pictureFormat = pictureFormat;
        this.customerId = customerId;
        this.exportAdditionalPartData = exportAdditionalPartData;
        this.exportEinPASData = exportEinPASData;
        this.exportVisualNav = exportVisualNav;
    }

    /**
     * Befüllt den übergebenen {@link iPartsDataExportRequest}
     *
     * @param exportRequest
     */
    public void fillNewExportRequest(iPartsDataExportRequest exportRequest) {
        if (exportRequest != null) {
            exportRequest.setFieldValueAsSetOfEnum(FIELD_DER_LANGUAGES, getSelectedLanguageCodes(), DBActionOrigin.FROM_EDIT);
            exportRequest.setFieldValueAsBoolean(FIELD_DER_INCLUDE_SAS, false, DBActionOrigin.FROM_EDIT);
            exportRequest.setFieldValueAsBoolean(FIELD_DER_INCLUDE_PICTURES, getPictureFormat() != iPartsExportPictureFormat.NONE, DBActionOrigin.FROM_EDIT);
            exportRequest.setFieldValue(FIELD_DER_PICTURE_FORMAT, getPictureFormat().getDbValue(), DBActionOrigin.FROM_EDIT);
            exportRequest.setFieldValueAsBoolean(FIELD_DER_INCLUDE_AGGS, false, DBActionOrigin.FROM_EDIT);
            exportRequest.setFieldValueAsBoolean(FIELD_DER_DIRECT_DOWNLOAD, true, DBActionOrigin.FROM_EDIT);
            exportRequest.setFieldValue(FIELD_DER_OUTPUT_FORMAT, iPartsWSExportPartsListRequest.OutputFormat.XML.name(), DBActionOrigin.FROM_EDIT);
            // Issuer aus dem JWT Token als den Ersteller setzen.
            exportRequest.setFieldValue(FIELD_DER_CREATION_USER_ID, iPartsUserAdminDb.getLoginUserName(), DBActionOrigin.FROM_EDIT);
            exportRequest.setFieldValueAsDateTime(FIELD_DER_CREATION_DATE, Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
            exportRequest.setFieldValueAsBoolean(FIELD_DER_INCLUDE_MAT_PROPERTIES, isExportAdditionalPartData(), DBActionOrigin.FROM_EDIT);
            exportRequest.setFieldValueAsBoolean(FIELD_DER_INCLUDE_EINPAS, isExportEinPASData(), DBActionOrigin.FROM_EDIT);
            exportRequest.setFieldValueAsBoolean(FIELD_DER_INCLUDE_VISUAL_NAV, isExportVisualNav(), DBActionOrigin.FROM_EDIT);
        }
    }

    public iPartsDataExportContent getExportContent() {
        return exportContent;
    }

    public List<Language> getSelectedLanguages() {
        return selectedLanguages;
    }

    public List<String> getSelectedLanguageCodes() {
        List<String> languageCodes = new DwList<>();
        for (Language selectedLanguage : selectedLanguages) {
            languageCodes.add(selectedLanguage.getCode());
        }
        return languageCodes;
    }


    public List<String> getFallbackLanguages() {
        return fallbackLanguages;
    }

    public iPartsExportPictureFormat getPictureFormat() {
        return pictureFormat;
    }

    public String getCustomerId() {
        return customerId;
    }

    public boolean isExportAdditionalPartData() {
        return exportAdditionalPartData;
    }

    public boolean isExportEinPASData() {
        return exportEinPASData;
    }

    public boolean isExportVisualNav() {
        return exportVisualNav;
    }

    public void setExportPSKData(boolean exportPSKData) {
        this.exportPSKData = exportPSKData;
    }

    public boolean isExportPSKData() {
        return exportPSKData;
    }
}
