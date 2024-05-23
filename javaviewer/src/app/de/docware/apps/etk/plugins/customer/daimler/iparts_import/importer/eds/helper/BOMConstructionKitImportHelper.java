/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsBOMConstKitContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMConstKitContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.iPartsSaaPartsListPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;

import java.util.Map;

/**
 * Helfer für den Import von BOM Daten zur Tabelle T43RBK (Baukasteinhalt)
 */
public class BOMConstructionKitImportHelper extends EDSImportHelper {

    public static final String IMPORTER_TITLE_CONTENT = "!!BOM Baukasteninhalt (BK)";

    private String releasedValueFrom; // Wert von FRG_KZ_AB
    private String releasedValueTo; // Wert von FRG_KZ_BIS
    private final String releaseDateFromSourceField; // FRG_DAT_AB
    private final String releaseDateToSourceField; // FRG_DAT_BIS
    private final String quantitySourceField; // MENGE
    private final String versionToSourceField; // AS_BIS
    private final String posSourceField; //Position (9-stellig

    /**
     * Konstruktor mit allen Muss-Felder aus beiden Importer
     *
     * @param project
     * @param mapping
     * @param tableName
     * @param releaseDateFromSourceField
     * @param releaseDateToSourceField
     * @param quantitySourceField
     * @param versionToSourceField
     * @param posSourceField
     */
    public BOMConstructionKitImportHelper(EtkProject project, Map<String, String> mapping, String tableName,
                                          String releaseDateFromSourceField, String releaseDateToSourceField,
                                          String quantitySourceField, String versionToSourceField,
                                          String posSourceField) {
        super(project, mapping, tableName);
        this.releaseDateFromSourceField = releaseDateFromSourceField;
        this.releaseDateToSourceField = releaseDateToSourceField;
        this.quantitySourceField = quantitySourceField;
        this.versionToSourceField = versionToSourceField;
        this.posSourceField = posSourceField;
    }

    public void setReleasedValueFrom(String releasedValueFrom) {
        this.releasedValueFrom = releasedValueFrom;
    }

    public void setReleasedValueTo(String releasedValueTo) {
        this.releasedValueTo = releasedValueTo;
    }

    @Override
    protected String handleValueOfSpecialField(String sourceField, String value) {
        // Wenn FRG_KZ_BIS den Wert "1" hat, müssen bestimmte Felder angepasst werden
        if (releasedValueTo.equals(RELEASED_TO_VALUE)) {
            if (sourceField.equals(versionToSourceField)) {
                value = EDSImportHelper.EDS_AS_BIS_UNENDLICH;
            } else if (sourceField.equals(releaseDateToSourceField)) {
                value = "";
            }
        }
        value = value.trim();
        //hier ggf das SAA-DateTime aus der Excel Notation konvertieren
        if (sourceField.equals(releaseDateFromSourceField) || sourceField.equals(releaseDateToSourceField)) {
            iPartsEDSDateTimeHandler dtHandler = new iPartsEDSDateTimeHandler(value);
            value = dtHandler.getBomDbDateValue();
        } else if (sourceField.equals(quantitySourceField)) {
            if (value.equalsIgnoreCase("N.BED")) {
                value = MENGE_NACH_BEDARF;
            } else {
                value = checkQuantityFormat(value);
            }
        } else if (sourceField.equals(posSourceField) && StrUtils.isValid(value)) {
            value = convertIntoEDSBCSPosValue(value);
        }
        return value;
    }

    public static String convertIntoEDSBCSPosValue(String value) {
        if (StrUtils.isEmpty(value)) {
            return "";
        }
        if (value.length() > 6) {
            return value.substring(value.length() - 6);
        } else {
            return StrUtils.leftFill(value, 6, '0');
        }
    }

    /**
     * Check, ob der Datensatz freigegeben wurde. Ein Datensatz ist nur freigegeben, wenn "BK_FRG_KZ_AB" (WICHTIG: AB) den Wert "0"
     * hat
     *
     * @return
     */
    public boolean isReleasedDataset() {
        return StrUtils.isValid(releasedValueFrom) && releasedValueFrom.equals(DATA_RELEASED_VALUE);
    }

    @Override
    protected void importValue(EtkDataObject dataObject, String importFieldName, String dbDestFieldName, String value, Language langDef) {

        // KEM Bis soll nur übernommen werden, wenn FRG_KZ_BIS != 1 ist
        if (dbDestFieldName.equals(FIELD_DCK_KEMTO) && releasedValueTo.equals(RELEASED_TO_VALUE)) {
            return;
        }
        super.importValue(dataObject, importFieldName, dbDestFieldName, value, langDef);
    }

    /**
     * Erzeugt einen {@link iPartsDataBOMConstKitContent} Datensatz aus den übergebenen Daten
     *
     * @param importer
     * @param importRec
     * @param recordNo
     * @param constKitNoSourceField
     * @param posSourceField
     * @param revFromSourceField
     * @return
     */
    public iPartsDataBOMConstKitContent createConstKitDataObject(AbstractDataImporter importer, Map<String, String> importRec, int recordNo,
                                                                 String constKitNoSourceField, String posSourceField, String revFromSourceField) {
        if (checkReleasedDataSets(importer, recordNo)) {
            String constKitNo = handleValueOfSpecialField(constKitNoSourceField, importRec);
            String position = handleValueOfSpecialField(posSourceField, importRec);
            String revFrom = handleValueOfSpecialField(revFromSourceField, importRec);

            iPartsBOMConstKitContentId constKitContentId = new iPartsBOMConstKitContentId(constKitNo, position, revFrom);
            iPartsDataBOMConstKitContent constKitContent = new iPartsDataBOMConstKitContent(getProject(), constKitContentId);
            fillDataObject(constKitContent, importRec);
            constKitContent.setFieldValue(FIELD_DCK_GUID, iPartsSaaPartsListPrimaryKey.buildSaaPartsList_GUID(constKitContent), DBActionOrigin.FROM_EDIT);
            return constKitContent;
        }
        return null;
    }

    protected void fillDataObject(EtkDataObject dataObject, Map<String, String> importRec) {
        if (!dataObject.existsInDB()) {
            dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        fillOverrideCompleteDataForEDSReverse(dataObject, importRec, iPartsEDSLanguageDefs.EDS_DE);
    }

    protected boolean checkReleasedDataSets(AbstractDataImporter importer, int recordNo) {
        if (!isReleasedDataset()) {
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Record %1 wird übersprungen: \"Freigabekennzeichen ab\" enthält \"%2\" und ist nicht \"0\".",
                                                                          String.valueOf(recordNo), releasedValueTo),
                                                 MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            importer.reduceRecordCount();
            return false;
        }
        return true;
    }
}
