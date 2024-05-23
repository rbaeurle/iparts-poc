/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.utils.EtkMultiSprache;
import org.apache.commons.collections4.map.LRUMap;

import java.util.Map;

/**
 * Helper für den Import der SAA Stammdaten aus EDS (Urladung und Änderungsdienst)
 */
public class EDSSaaMasterDataImportHelper extends EDSImportHelper implements iPartsConst {

    private final boolean isVakzToEmpty;
    private final String asToFieldname;
    private final String kemToFieldname;
    private final String releaseFromFieldname;
    private final String releaseToFieldname;
    private final LRUMap remarkTextCache = new LRUMap(10000);

    public EDSSaaMasterDataImportHelper(EtkProject project, Map<String, String> mapping, String tableName, boolean isVakzToEmpty,
                                        String asToFieldname, String kemToFieldname, String releaseFromFieldname, String releaseToFieldname) {
        super(project, mapping, tableName);
        this.isVakzToEmpty = isVakzToEmpty;
        this.asToFieldname = asToFieldname;
        this.kemToFieldname = kemToFieldname;
        this.releaseFromFieldname = releaseFromFieldname;
        this.releaseToFieldname = releaseToFieldname;
    }

    @Override
    public String handleValueOfSpecialField(String sourceField, String value) {
        // nur bei VAKZ_BIS = leer wird der Originalwert übernommen bei den folgenden Feldern, ansonsten wird der Wert abgeändert
        if (!isVakzToEmpty) {
            if (sourceField.equals(asToFieldname)) { // Änderungsstand bis
                value = EDS_AS_BIS_UNENDLICH;
            } else if (sourceField.equals(kemToFieldname)) { // KEM bis
                value = "";
            } else if (sourceField.equals(releaseToFieldname)) { // Freigabetermin bis
                value = "";
            }
        }
        //
        value = value.trim();
        if (sourceField.equals(releaseFromFieldname) || sourceField.equals(releaseToFieldname)) {
            iPartsEDSDateTimeHandler dateTimeHandler = new iPartsEDSDateTimeHandler(value);
            value = dateTimeHandler.getBomDbDateValue();
        }
        return value;
    }

    /**
     * Erstellt ein Kind-DBObject für die zu importierenden SAA Stammdaten
     *
     * @param saaHistory
     * @param entryValue
     * @param textValue
     * @param dbFieldNameForText
     * @param type
     * @return
     */
    public EtkDataObject createSaaChildObject(iPartsDataSaaHistory saaHistory, String entryValue, String textValue,
                                              String dbFieldNameForText, RemarkAndWWChildren type) {
        switch (type) {
            case WW_FLAGS:
                iPartsSaaWWFlagsId wwFlagsId = new iPartsSaaWWFlagsId(saaHistory.getAsId().getSaaNumber(), saaHistory.getAsId().getRevFrom(), entryValue);
                iPartsDataSaaWWFlags wwFlag = new iPartsDataSaaWWFlags(getProject(), wwFlagsId);
                if (!wwFlag.existsInDB()) {
                    wwFlag.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }
                wwFlag.setFieldValue(dbFieldNameForText, textValue, DBActionOrigin.FROM_EDIT);
                return wwFlag;
            case REMARKS:
                iPartsSaaRemarksId remarkId = new iPartsSaaRemarksId(saaHistory.getAsId().getSaaNumber(), saaHistory.getAsId().getRevFrom(), entryValue);
                iPartsDataSaaRemarks remark = new iPartsDataSaaRemarks(getProject(), remarkId);
                if (!remark.existsInDB()) {
                    remark.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }
                // Check, ob der Text im Cache oder in der DB vorkommt. Falls nicht, wird ein neues Objekt angelegt
                EtkMultiSprache multiLang = EDSRemarkTextHelper.determineMultiLangForEDSBCSRemark(getProject(),
                                                                                                  textValue,
                                                                                                  remarkTextCache,
                                                                                                  TABLE_DA_EDS_SAA_REMARKS,
                                                                                                  FIELD_DESR_TEXT);
                multiLang.setText(Language.DE, textValue);
                remark.setFieldValueAsMultiLanguage(dbFieldNameForText, multiLang, DBActionOrigin.FROM_EDIT);
                return remark;
        }

        return null;
    }

    /**
     * Überprüft, ob die SAA in der Tabelle DA_SAA existiert. Falls nicht, dann wird sie mit den minimalsten Angaben
     * angelegt.
     *
     * @param importer
     * @param saaHistory
     */
    public void addSaaIfNotExists(AbstractDataImporter importer, iPartsDataSaaHistory saaHistory,
                                  EtkMultiSprache saaDescriptionText, boolean updateConstDescription) {
        if ((importer == null) || (saaHistory == null)) {
            return;
        }
        iPartsSaaId saaId = new iPartsSaaId(saaHistory.getAsId().getSaaNumber());
        iPartsDataSaa dataSaa = new iPartsDataSaa(getProject(), saaId);

        boolean existsInDB = dataSaa.existsInDB();
        // Sollen bestehende EDS Einträge
        boolean updateData = updateConstDescription && existsInDB && (iPartsImportDataOrigin.getTypeFromCode(dataSaa.getFieldValue(FIELD_DS_SOURCE)) == iPartsImportDataOrigin.EDS);
        if (!existsInDB || updateData) {
            if (!existsInDB) {
                dataSaa.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            dataSaa.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.EDS.getOrigin(), DBActionOrigin.FROM_EDIT);
            if ((saaDescriptionText != null) && !saaDescriptionText.isEmpty()) {
                dataSaa.setFieldValueAsMultiLanguage(FIELD_DS_CONST_DESC, saaDescriptionText, DBActionOrigin.FROM_EDIT);
            }
            importer.saveToDB(dataSaa);
        }

    }

    public void addSaaIfNotExists(AbstractDataImporter importer, iPartsDataSaaHistory saaHistory) {
        addSaaIfNotExists(importer, saaHistory, null, false);
    }

    /**
     * Befüllt die Wahlweise-Kennzeichen-Kind-Dataobjects für die SAA Stammdaten
     *
     * @param saaHistory
     * @param importRec
     * @param wwFlagsElement
     * @param singleWWFlagElement
     * @param wwTypeAttribute
     */
    public void fillWWFlagsUpdate(iPartsDataSaaHistory saaHistory, Map<String, String> importRec, String wwFlagsElement, String singleWWFlagElement, String wwTypeAttribute) {
        fillValuesAndAttributesForUpdate(importRec, wwFlagsElement, singleWWFlagElement, wwTypeAttribute, EDSImportHelper.RemarkAndWWChildren.WW_FLAGS, getCallBack(saaHistory));
    }

    /**
     * Befüllt die Bemerkungen-Kind-Dataobjects für die SAA Stammdaten
     *
     * @param saaHistory
     * @param importRec
     * @param remarksElement
     * @param singleRemarkElement
     * @param remarkNoAttribute
     */
    public void fillRemarksUpdate(iPartsDataSaaHistory saaHistory, Map<String, String> importRec, String remarksElement, String singleRemarkElement, String remarkNoAttribute) {
        fillValuesAndAttributesForUpdate(importRec, remarksElement, singleRemarkElement, remarkNoAttribute, EDSImportHelper.RemarkAndWWChildren.REMARKS, getCallBack(saaHistory));
    }

    public EDSImportHelper.RemarksAndWWCallback getCallBack(iPartsDataSaaHistory saaHistory) {
        return (type, entryValue, textValue) -> {
            switch (type) {
                case WW_FLAGS:
                    saaHistory.addWWFlag((iPartsDataSaaWWFlags)createSaaChildObject(saaHistory, entryValue, textValue, FIELD_DESW_TEXT, type), DBActionOrigin.FROM_EDIT);
                    break;
                case REMARKS:
                    saaHistory.addRemark((iPartsDataSaaRemarks)createSaaChildObject(saaHistory, entryValue, textValue, FIELD_DESR_TEXT, type), DBActionOrigin.FROM_EDIT);
                    break;
            }
        };
    }

    public void fillWWFlagsForInitialImport(iPartsDataSaaHistory saaHistory, Map<String, String> importRec, String prefixWwType, String prefixWwText) {
        fillValuesWithAttributesForInitialImport(importRec, prefixWwType, prefixWwText, RemarkAndWWChildren.WW_FLAGS, getCallBack(saaHistory));
    }

    public void fillRemarksForInitialImport(iPartsDataSaaHistory saaHistory, Map<String, String> importRec, String prefixRemarkNo, String prefixRemarkText) {
        fillValuesWithAttributesForInitialImport(importRec, prefixRemarkNo, prefixRemarkText, RemarkAndWWChildren.REMARKS, getCallBack(saaHistory));
    }
}
