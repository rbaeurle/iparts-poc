/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import org.apache.commons.collections4.map.LRUMap;

import java.util.Map;

/**
 * Helper für EDS (Bom-DB) Wahlweise-Kennbuchstabe und Bemerkungsziffer (Nfz) Importer (T43RTEIE) Urladung + Änderungsdienst
 */
public class EDSMaterialRemarksImportHelper extends EDSImportHelper implements iPartsConst {

    private final LRUMap remarkTextCache = new LRUMap(10000);

    /**
     * Der normale Konstuktor für den Helper
     *
     * @param project
     * @param mapping
     * @param tableName
     */
    public EDSMaterialRemarksImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
        super(project, mapping, tableName);
    }

    @Override
    protected String handleValueOfSpecialField(String sourceField, String value) {
        if (StrUtils.isValid(value)) {
            value = value.trim();
            if (value.isEmpty()) {
                return "";
            } else {
                return value;
            }
        }
        return "";
    }

    /**
     * Erstellt ein Kind-DBObject für die zu importierenden SAA Stammdaten
     *
     * @param partHistory
     * @param entryValue
     * @param textValue
     * @param dbFieldNameForText
     * @param type
     * @return
     */
    public EtkDataObject createPartChildObject(iPartsDataBOMPartHistory partHistory, String entryValue, String textValue,
                                               String dbFieldNameForText, RemarkAndWWChildren type) {

        switch (type) {
            case WW_FLAGS:
                iPartsMaterialWWFlagsId wwFlagsId = new iPartsMaterialWWFlagsId(partHistory.getAsId().getPartNo(),
                                                                                partHistory.getAsId().getRevFrom(),
                                                                                entryValue);
                iPartsDataMaterialWWFlag wwFlag = new iPartsDataMaterialWWFlag(getProject(), wwFlagsId);
                if (!wwFlag.existsInDB()) {
                    wwFlag.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }
                wwFlag.setFieldValue(dbFieldNameForText, textValue, DBActionOrigin.FROM_EDIT);
                return wwFlag;

            case REMARKS:
                iPartsMaterialRemarksId remarksId = new iPartsMaterialRemarksId(partHistory.getAsId().getPartNo(),
                                                                                partHistory.getAsId().getRevFrom(),
                                                                                entryValue);
                iPartsDataMaterialRemark remark = new iPartsDataMaterialRemark(getProject(), remarksId);
                if (!remark.existsInDB()) {
                    remark.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }
                // Check, ob der Text im Cache oder in der DB vorkommt. Falls nicht, wird ein neues Objekt angelegt
                EtkMultiSprache multiLang = EDSRemarkTextHelper.determineMultiLangForEDSBCSRemark(getProject(),
                                                                                                  textValue,
                                                                                                  remarkTextCache,
                                                                                                  TABLE_DA_EDS_MAT_REMARKS,
                                                                                                  FIELD_DEMR_TEXT);
                multiLang.setText(Language.DE, textValue);
                remark.setFieldValueAsMultiLanguage(dbFieldNameForText, multiLang, DBActionOrigin.FROM_EDIT);
                return remark;
        }
        return null;
    }

    /**
     * Befüllt die Wahlweise-Kennzeichen-Kind-Dataobjects für die SAA Stammdaten aus dem Änderungsdienst heraus
     *
     * @param importRec
     * @param wwFlagsElement
     * @param singleWWFlagElement
     * @param wwTypeAttribute
     */
    public void fillWWFlagsForUpdate(iPartsDataBOMPartHistory bomPartHistory, Map<String, String> importRec, String wwFlagsElement, String singleWWFlagElement, String wwTypeAttribute) {
        fillValuesAndAttributesForUpdate(importRec, wwFlagsElement, singleWWFlagElement, wwTypeAttribute, EDSImportHelper.RemarkAndWWChildren.WW_FLAGS, getCallBack(bomPartHistory));
    }

    /**
     * Befüllt die Bemerkungen-Kind-DataObjects für die SAA Stammdaten aus dem Änderungsdienst heraus
     *
     * @param importRec
     * @param remarksElement
     * @param singleRemarkElement
     * @param remarkNoAttribute
     */
    public void fillRemarksForUpdate(iPartsDataBOMPartHistory bomPartHistory, Map<String, String> importRec, String remarksElement, String singleRemarkElement, String remarkNoAttribute) {
        fillValuesAndAttributesForUpdate(importRec, remarksElement, singleRemarkElement, remarkNoAttribute, EDSImportHelper.RemarkAndWWChildren.REMARKS, getCallBack(bomPartHistory));
    }

    private EDSImportHelper.RemarksAndWWCallback getCallBack(iPartsDataBOMPartHistory partHistory) {
        return (type, attValue, textValue) -> {
            switch (type) {
                case WW_FLAGS:
                    partHistory.addWWFlag((iPartsDataMaterialWWFlag)createPartChildObject(partHistory, attValue, textValue, FIELD_DEMW_TEXT, type), DBActionOrigin.FROM_EDIT);
                    break;
                case REMARKS:
                    partHistory.addRemark((iPartsDataMaterialRemark)createPartChildObject(partHistory, attValue, textValue, FIELD_DEMR_TEXT, type), DBActionOrigin.FROM_EDIT);
                    break;
            }
        };
    }

    /**
     * Befüllt die Wahlweise-Kennzeichen-Kind-Dataobjects für die SAA Stammdaten aus der Urladung heraus
     *
     * @param bomPartHistory
     * @param importRec
     * @param wwFlagPrefix
     * @param wwTextPrefix
     */
    public void fillWWFlagsForInitialImport(iPartsDataBOMPartHistory bomPartHistory, Map<String, String> importRec, String wwFlagPrefix, String wwTextPrefix) {
        fillValuesWithAttributesForInitialImport(importRec, wwFlagPrefix, wwTextPrefix, RemarkAndWWChildren.WW_FLAGS, getCallBack(bomPartHistory));
    }

    /**
     * Befüllt die Bemerkungen-Kind-DataObjects für die SAA Stammdaten aus der Urladung heraus
     *
     * @param bomPartHistory
     * @param importRec
     * @param remarkNoPrefix
     * @param remarkTextPrefix
     */
    public void fillRemarksForInitialImport(iPartsDataBOMPartHistory bomPartHistory, Map<String, String> importRec, String remarkNoPrefix, String remarkTextPrefix) {
        fillValuesWithAttributesForInitialImport(importRec, remarkNoPrefix, remarkTextPrefix, RemarkAndWWChildren.REMARKS, getCallBack(bomPartHistory));
    }

    /**
     * Überprüft, ob der übergebene Änderungsstand kleiner/gleich dem höchsten Änderungsstand in der DB ist (DA_BOM_MAT_HISTORY)
     * und gibt dementsprechend eine Meldung aus.
     *
     * @param partNo
     * @param revFrom
     * @param importer
     * @return
     */
    public void checkImportRecordRevision(String partNo, String revFrom, AbstractBOMDataImporter importer) {
        iPartsDataBOMPartHistoryList allRevisionsForPartNo = iPartsDataBOMPartHistoryList.loadBOMHistoryDataForPartNumber(getProject(), partNo, "");
        if (!allRevisionsForPartNo.isEmpty()) {
            String highestRevision = allRevisionsForPartNo.getLast().getAsId().getRevFrom();
            if (Utils.toSortString(revFrom).compareTo(Utils.toSortString(highestRevision)) > 0) {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Die Revision des zu importierenden Datensatzes" +
                                                                              " \"%1\" ist höher als die Revision des höchsten Datensatzes" +
                                                                              " aus TEIE (DA_BOM_MAT_HISTORY) \"%2\"", revFrom, highestRevision),
                                                     MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            }
        } else {
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Zur Teilenummer \"%1\" existieren keine historischen Daten.", partNo),
                                                 MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
        }
    }
}
