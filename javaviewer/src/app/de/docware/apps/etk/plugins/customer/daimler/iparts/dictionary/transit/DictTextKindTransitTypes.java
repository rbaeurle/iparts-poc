package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit;

import de.docware.util.StrUtils;

/**
 * Transit-Kennungen für die einzelnen Dictionary Types (laufen unter Fremdquelle iPrats)
 */
public enum DictTextKindTransitTypes {
    MODEL_KIT_NAME("model kit name", "!!Baumuster-/EVO-Benennungen", "model kit name", "MKN"),
    SUPPLEMENTARY_TEXT("supplementary text", "!!Ergänzungstexte", "supplementary text", "ST"),
    FOOT_NOTE("foot note", "!!Fußnoten", "foot note", "FN"),
    MODEL_ADDITIONAL_TEXT("model additional text", "!!Baumuster Zusatztext", "model additional text", "MAT"),
    CODE_NAME("code name", "!!Code-Benennung", "code name", "CN"),
    SA_NAME("SA-name", "!!SA-Rumpf-Benennung", "SA-name", "SAN"),
    SAA_NAME("SAA-name", "!!SA-Strich-Benennung", "SAA-name", "SAAN"),
    KG_TU_NAME("KG/TU name", "!!KG-TU-Benennung", "KG/TU name", "KTN"),
    PART_NAME("part name", "!!Teilebenennung", "part name", "PN"),
    ES2_KEY_COLOR("ES2 key (color)", "!!Farbe", "ES2 key (color)", "ES2"),
    MODEL_NAME("model name", "!!Farbe", "model name", "MN"),
    STANDARD_FOOT_NOTE("standard foot note", "!!Farbe", "standard foot note", "SFN"),
    UNKNOWN("", "!!Unbekannt", "", "");

    private String dbValue;
    private String description;
    private String xmlMetaInformationType;
    private String prefixForTextId;

    DictTextKindTransitTypes(String dbValue, String description, String xmlMetaInformationType, String prefixForTextId) {
        this.dbValue = dbValue;
        this.description = description;
        this.xmlMetaInformationType = xmlMetaInformationType;
        this.prefixForTextId = prefixForTextId;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getTextKindName() {
        return description;
    }

    public String getXmlMetaInformationType() {
        return xmlMetaInformationType;
    }

    public static DictTextKindTransitTypes getType(String dbValue) {
        for (DictTextKindTransitTypes transitTextKindId : values()) {
            if (transitTextKindId.getDbValue().equals(dbValue)) {
                return transitTextKindId;
            }
        }
        return UNKNOWN;
    }

    public static DictTextKindTransitTypes getXMLType(String xmlValue) {
        for (DictTextKindTransitTypes transitTextKindId : values()) {
            if (transitTextKindId.getXmlMetaInformationType().equals(xmlValue)) {
                return transitTextKindId;
            }
        }
        return UNKNOWN;
    }

    public String getPrefixForTextId() {
        return prefixForTextId;
    }

    public String getTextKindFileName() {
        // System characters entfernen, weil es sonst bei bestimmten Textarten zu Problemen führen könnte, z.B. "KG/TU name"
        String textkindName = StrUtils.removeFilesystemCharacters(getXmlMetaInformationType()).toUpperCase();
        return StrUtils.replaceSubstring(textkindName, " ", "");
    }
}
