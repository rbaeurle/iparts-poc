/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLTableDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.iPartsBomDBFactoriesHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

import java.util.*;

/**
 * Helper für EDS Importe
 */
public class EDSImportHelper extends iPartsMainImportHelper {

    /**
     * Enums für den speziellen Import von Wahlweise- und Bemerkungstexten
     */
    public enum RemarkAndWWChildren {
        WW_FLAGS, REMARKS
    }

    private static final String BOM_IMAGE_DATE_SPLIT_YEAR = "50";
    public static final String EDS_AS_BIS_UNENDLICH = "999";
    public static final String BOM_VALUE_TRUE = "J";
    public static final String BOM_VALUE_FALSE = "N";
    public static final String DATA_RELEASED_VALUE = "0";
    public static final String RELEASED_TO_VALUE = "1";

    public EDSImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
        super(project, mapping, tableName);
    }

    /**
     * bei XML-Importdateien erscheinen unbesetzte Tags nicht im importRec
     * Hier wird das importRec auf Leerwerte erweitert, damit die Abfrage auf null des Values entfällt
     *
     * @param importRec        aktuelle importRec
     * @param extraXMLElements zusätzliche Import-Feldnamen, die nicht im mapping stehen, jedoch benutzt werden
     * @param allXMLElements   alle gültigen XML-Tags dieses Importers
     */
    public void prepareXMLImportRec(Map<String, String> importRec, Set<String> extraXMLElements, Set<String> allXMLElements) {
        // über alle Mapping Felder
        for (Map.Entry<String, String> mappingForField : mapping.entrySet()) {
            String importFieldName = mappingForField.getValue();
            if ((importRec.get(importFieldName) == null) && allXMLElements.contains(importFieldName)) {
                importRec.put(importFieldName, "");
            }
        }
        // über alle extraXML Elements
        if (extraXMLElements != null) {
            for (String importFieldName : extraXMLElements) {
                if ((importRec.get(importFieldName) == null) && allXMLElements.contains(importFieldName)) {
                    importRec.put(importFieldName, "");
                }
            }
        }
    }

    /**
     * Die Import-Werte komplett in einen Datensatz übertragen (DIALOG spezifisch)
     * Das Mapping besitzt dabei den folgenden Aufbau:
     * Key = DB-FieldName, Value = Import-Spaltenname
     *
     * @param dataObject
     * @param importRec
     * @param langDef    DIALOG spezifisch
     */
    public void fillOverrideCompleteDataForEDSReverse(EtkDataObject dataObject, Map<String, String> importRec,
                                                      iPartsEDSLanguageDefs langDef) {
        Language language = (langDef != null) ? langDef.getDbValue() : null;
        fillOverrideCompleteDataReverse(dataObject, importRec, language);
    }

    /**
     * einen Multilang Text (destField) zu einer Sprache (langDef) setzen bzw überschreiben (DIALOG spezifisch)
     *
     * @param dataObject
     * @param langDef    DIALOG spezifisch
     * @param destField  Feld in der Datenbank
     * @param value
     */
    public void fillOverrideOneLanguageTextForEDS(EtkDataObject dataObject, iPartsEDSLanguageDefs langDef,
                                                  String destField, String value) {
        fillOverrideOneLanguageText(dataObject, langDef.getDbValue(), destField, value);
    }

    /**
     * Check bezüglich dem "Änderungsstand Ab" im übergebenen Record und dem aktuelle "Änderungsstand Ab".
     * Der neue "Änderungsstand Ab" ist nur valide, wenn VAKZ AB leer und der neue Wert höher als der aktuelle ist.
     * Optional kann angegeben werden, dass der neue Wert auch bei Gleichheit valide ist.
     *
     * @param importRec
     * @param asFromValueDataObject - Änderungsstand Ab Wert vom DataObject
     * @param vakzFromFieldname
     * @param asFromFieldname
     * @return
     */
    private boolean checkVersion(Map<String, String> importRec, String asFromValueDataObject, String
            vakzFromFieldname, String asFromFieldname, boolean euqalsIsOK) {
        if (handleValueOfSpecialField(vakzFromFieldname, importRec).isEmpty()) {
            int newVersionFrom = StrUtils.strToIntDef(handleValueOfSpecialField(asFromFieldname, importRec), -1);
            int currentVersionFrom = StrUtils.strToIntDef(asFromValueDataObject, -1);
            if (euqalsIsOK) {
                return newVersionFrom >= currentVersionFrom;
            } else {
                return newVersionFrom > currentVersionFrom;
            }
        }
        return false;
    }

    /**
     * Überprüft, ob der "Änderungsstand Ab" im übergebenen Record gleich oder höher als der aktuelle "Änderungsstand Ab" ist.
     * Der neue Datensatz wird nur übernommen, wenn VAKZ AB leer und der neue "Änderungsstand Ab" Wert gleich/ höher als
     * der aktuelle ist
     *
     * @param importRec
     * @param asFromValueDataObject
     * @param vakzFromFieldname
     * @param asFromFieldname
     * @return
     */
    public boolean hasHigherOrEqualsVersion(Map<String, String> importRec, String asFromValueDataObject, String
            vakzFromFieldname, String asFromFieldname) {
        return checkVersion(importRec, asFromValueDataObject, vakzFromFieldname, asFromFieldname, true);
    }

    /**
     * Überprüft, ob der "Änderungsstand Ab" im übergebenen Record höher ist als der aktuelle "Änderungsstand Ab".
     * Der neue Datensatz wird nur übernommen, wenn VAKZ AB leer und der neue "Änderungsstand Ab" Wert höher  als
     * der aktuelle ist
     *
     * @param importRec
     * @param asFromValueDataObject
     * @param vakzFromFieldname
     * @param asFromFieldname
     * @return
     */
    public boolean hasHigherVersion(Map<String, String> importRec, String asFromValueDataObject, String
            vakzFromFieldname, String asFromFieldname) {
        return checkVersion(importRec, asFromValueDataObject, vakzFromFieldname, asFromFieldname, false);
    }

    public boolean getInvalidSign(Map<String, String> importRec, String ungKZFieldname) {
        String value = handleValueOfSpecialField(ungKZFieldname, importRec);
        return StrUtils.isValid(value);
    }

    /**
     * Prüft, ob der Datensatz valide ist und importiert werden kann (VAKZ ab muss leer sein)
     *
     * @param importRec
     * @return
     */
    public boolean isValidRecord(Map<String, String> importRec, String vakzFromFieldname) {
        String vakzFrom = handleValueOfSpecialField(vakzFromFieldname, importRec);
        if (StrUtils.isEmpty(vakzFrom)) {
            return true;
        }
        return false;
    }

    /**
     * Extrahiert alle Werte für ein XML Element aus dem Importdatensatz. Bei Multi-Ebenen-XML Elementen werden alle
     * Werte extrahiert.
     * Z.B. hat der Importdatensatz mehr als eine Ebene und die Unterelemente heißen alle "PlantSupply", dann werden alle Werte
     * für "PlantSupply" extrahiert.
     *
     * @param importRec
     * @param mainField
     * @param subField
     * @return
     */
    protected List<String> extractAllValuesFromSubDatasets(Map<String, String> importRec, String
            mainField, String subField) {
        List<String> result = new ArrayList<String>();
        Map<String, ImportRecMultiLevelDataset> allValuesFromSubdatasets = getMultiLevelValues(mainField, subField, importRec, false);
        for (ImportRecMultiLevelDataset values : allValuesFromSubdatasets.values()) {
            result.add(values.getValue());

        }
        return result;
    }

    /**
     * Setzt die Elemente eines Subdatensatze zu einem String zusammen (aktuell <BodyTypes> und <PalntSupplies>)
     *
     * @param importRec
     * @param mainField
     * @param subField
     * @param forcedValueLength
     * @return
     */
    public String buildStringFromSubDatasets(Map<String, String> importRec, String mainField,
                                             String subField, int forcedValueLength) {
        Collection<String> subValues = extractAllValuesFromSubDatasets(importRec, mainField, subField);
        StringBuilder str = new StringBuilder(subValues.size() * forcedValueLength);
        for (String subValue : subValues) {
            String finalValue = StrUtils.pad(subValue, forcedValueLength);
            str.append(finalValue);
        }
        return str.toString();
    }

    /**
     * Liefert alle Werte und Attributwerte zu einem Multiebenen Subelement
     *
     * @param mainField
     * @param subField
     * @param importRec
     * @param withAttributes
     * @return
     */
    public Map<String, ImportRecMultiLevelDataset> getMultiLevelValues(String mainField, String
            subField, Map<String, String> importRec, boolean withAttributes) {
        Map<String, ImportRecMultiLevelDataset> result = new TreeMap<String, ImportRecMultiLevelDataset>();
        String mainFieldValue = importRec.get(mainField);
        if (StrUtils.isValid(mainFieldValue) && mainFieldValue.equals(iPartsXMLTableDataset.SUB_DATASETS)) {
            String value = importRec.get(subField);
            String key = subField;
            if (value != null) {
                int count = 1;
                do {
                    ImportRecMultiLevelDataset multiLevelDataset = new ImportRecMultiLevelDataset(key, subField, value);
                    if (withAttributes) {
                        multiLevelDataset.setAttributes(getAttributesForTag(key, importRec));
                    }
                    result.put(key, multiLevelDataset);
                    key = subField + "_" + EtkDbsHelper.formatLfdNr(count);
                    value = importRec.get(key);
                    count++;
                } while (value != null);
            }
        }
        return result;
    }

    /**
     * Liefert alle Attributwerte zu einem XML Element im ImportRecord
     *
     * @param fieldname
     * @param importRec
     * @return
     */
    public Map<String, String> getAttributesForTag(String fieldname, Map<String, String> importRec) {
        Map<String, String> result = new TreeMap<String, String>();
        String keyPrefix = fieldname + iPartsTransferConst.ELEMENT_ATTRIBUTE_DELIMITER;
        for (String key : importRec.keySet()) {
            if (StrUtils.stringStartsWith(key, keyPrefix, true)) {
                String[] keyParts = StrUtils.toStringArray(key, iPartsTransferConst.ELEMENT_ATTRIBUTE_DELIMITER, false);
                if ((keyParts.length >= 2) && StrUtils.isValid(keyParts[1])) {
                    result.put(keyParts[1], importRec.get(key));
                }
            }
        }
        return result;
    }

    protected void handleBOMImageDate(iPartsDataPart dataPart, String imagedateFieldname) {
        handleImageDateWithoutFirstTwoYearDigitsWithinObject(dataPart, imagedateFieldname, BOM_IMAGE_DATE_SPLIT_YEAR);
    }

    public iPartsEDSLanguageDefs getEDSLanguageDefFromAttribute(String elementName, String attributeName, Map<String, String> importRec) {
        Map<String, String> attributes = getAttributesForTag(elementName, importRec);
        if (attributes.containsKey(attributeName)) {
            String language = attributes.get(attributeName);
            if (StrUtils.isValid(language)) {
                return iPartsEDSLanguageDefs.getFromXMLValue(language);
            }
        }
        return iPartsEDSLanguageDefs.EDS_UNKNOWN;
    }

    public class ImportRecMultiLevelDataset {

        private String originalFieldname;
        private String fieldname;
        private String value;
        private Map<String, String> attributes;

        public ImportRecMultiLevelDataset(String fieldname) {
            this.fieldname = fieldname;
            attributes = new TreeMap<String, String>();
        }

        public ImportRecMultiLevelDataset(String fieldname, String value) {
            this(fieldname);
            this.value = value;
        }

        public ImportRecMultiLevelDataset(String fieldname, String originalFieldname, String value) {
            this(fieldname, value);
            this.originalFieldname = originalFieldname;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
        }

        public void addAttribute(String key, String value) {
            if (StrUtils.isValid(key, value)) {
                attributes.put(key, value);
            }
        }

        public String getFieldname() {
            return fieldname;
        }

        public String getOriginalFieldname() {
            return originalFieldname;
        }
    }

    /**
     * Befüllt das übergeben DataObject mit den BOM-DB spezifischen Werkskennungen aus den BOM-DB XML-Dateien.
     *
     * @param dataObject
     * @param importRec
     * @param mainFieldname
     * @param subFieldname
     * @param dbFieldname
     */
    public void fillPlantSupplies(EtkDataObject dataObject,
                                  Map<String, String> importRec, String mainFieldname, String subFieldname,
                                  String dbFieldname) {
        List<String> plantSupplies = extractAllValuesFromSubDatasets(importRec, mainFieldname, subFieldname);
        String plantSuppliesDBValue = iPartsBomDBFactoriesHelper.getDBValueForPlantSupplies(plantSupplies);
        if (StrUtils.isValid(plantSuppliesDBValue)) {
            dataObject.setAttributeValue(dbFieldname, plantSuppliesDBValue, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Befüllt das übergeben DataObject mit den BOM-DB spezifischen Werkskennungen aus den BOM-DB Urladungsdateien.
     *
     * @param dataObject
     * @param importRec
     * @param sourceFieldName
     * @param dbFieldname
     */
    public void fillPlantSupplies(EtkDataObject dataObject, Map<String, String> importRec, String sourceFieldName, String dbFieldname) {
        String factories = importRec.get(sourceFieldName);
        if (StrUtils.isValid(factories)) {
            String plantSuppliesDBValue = iPartsBomDBFactoriesHelper.getDBValueForPlantSupplies(factories);
            if (StrUtils.isValid(plantSuppliesDBValue)) {
                dataObject.setAttributeValue(dbFieldname, plantSuppliesDBValue, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    @Override
    protected String handleValueOfMultiLangField(String destField, String value, Language langDef) {
        return StrUtils.isValid(value) ? value.trim() : value;
    }

    /**
     * Die typischen EDS SDA/SDB-Felder parsen, die es in vielen Importen gibt, wobei anstatt {@code null} im Zweifelsfall
     * ein leerer String zurückgegeben wird.
     *
     * @param value
     * @return
     */
    protected String getEDSDateTimeValue(String value) {
        iPartsEDSDateTimeHandler dtHandler = new iPartsEDSDateTimeHandler(value);
        value = dtHandler.getDBDateTime();
        if (value == null) {
            value = "";
        }
        return value;
    }

    /**
     * Wandelt das EDS ASPLM ISO Datum in das Datenbankformat für DateTime um, wobei {@code null} zurückgegeben wird,
     * wenn die Umwandlung nicht erfolgreich war
     *
     * @param value
     * @return
     */
    protected String getEDSDateTimeValueFromISO(String value) {
//        String dateWithoutTimezone = value.split("\\+")[0];
        iPartsEDSDateTimeHandler dtHandler = new iPartsEDSDateTimeHandler(value);
        return dtHandler.convertASPLMISOToDBDateTime(value);
    }

    /**
     * Liefert den Wert zu dem übergebenen Quell-Feld in dem übergebenen Import-Record.
     *
     * @param importRec
     * @param sourceField
     * @return
     */
    public static String getTrimmedValueFromRecord(Map<String, String> importRec, String sourceField) {
        if (StrUtils.isValid(sourceField)) {
            String value = importRec.get(sourceField);
            if (StrUtils.isValid(value)) {
                return value.trim();
            }
        }
        return "";
    }


    /**
     * Befüllt das Stammdaten-DBObject mit Kind-Objekten, die aus Elementen und Attributen der Urladung
     * zusammengebaut wurden.
     *
     * @param importRec
     * @param mainFieldname
     * @param subFieldname
     * @param attributeName
     * @param type
     * @param callback
     */
    public void fillValuesAndAttributesForUpdate(Map<String, String> importRec, String mainFieldname, String subFieldname,
                                                 String attributeName, RemarkAndWWChildren type, RemarksAndWWCallback callback) {
        Map<String, EDSImportHelper.ImportRecMultiLevelDataset> multiLevelValues = getMultiLevelValues(mainFieldname, subFieldname, importRec, true);
        if ((multiLevelValues != null) && !multiLevelValues.isEmpty()) {
            for (EDSImportHelper.ImportRecMultiLevelDataset multilevelData : multiLevelValues.values()) {
                String attValue = multilevelData.getAttributes().get(attributeName);
                if (attValue == null) {
                    attValue = "";
                }
                callback.handleAttributeValue(type, attValue, multilevelData.getValue());
            }
        }
    }

    /**
     * Befüllt das Stammdaten-DBObject mit Kind-Objekten, die aus Elementen und Attributen der Urladung
     * zusammengebaut wurden.
     *
     * @param importRec
     * @param keyFieldPrefix
     * @param valueFieldPrefix
     * @param type
     */
    public void fillValuesWithAttributesForInitialImport(Map<String, String> importRec, String keyFieldPrefix, String valueFieldPrefix,
                                                         EDSImportHelper.RemarkAndWWChildren type, RemarksAndWWCallback callback) {
        for (Map.Entry<String, String> entry : importRec.entrySet()) {
            if (StrUtils.stringStartsWith(entry.getKey(), keyFieldPrefix, true)) {
                String suffix = StrUtils.removeFirstCharacterIfCharacterIs(entry.getKey(), keyFieldPrefix);
                String textValue = handleValueOfSpecialField(valueFieldPrefix + suffix, importRec);
                String entryValue = handleValueOfSpecialField(entry.getKey(), entry.getValue());
                if (!StrUtils.isEmpty(entryValue, textValue)) {
                    callback.handleAttributeValue(type, entryValue, textValue);
                }
            }
        }
    }

    /**
     * Callback für die handhabung von Bemerkungs- und Wahlweisetexten beim Import
     */
    public interface RemarksAndWWCallback {

        void handleAttributeValue(RemarkAndWWChildren type, String entryValue, String textValue);
    }
}
