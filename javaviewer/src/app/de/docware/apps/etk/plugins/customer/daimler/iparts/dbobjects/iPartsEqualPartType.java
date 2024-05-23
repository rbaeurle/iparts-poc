package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

/**
 * Enum für die Darstellung des Gleichteile-Typs
 */
public enum iPartsEqualPartType {

    NONE(""),
    MB("MB"),
    DT("DT"),
    BOTH("BOTH");

    private String dbValue;

    iPartsEqualPartType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static iPartsEqualPartType getTypeByDBValue(String value) {
        if (value != null) {
            value = value.trim();
            for (iPartsEqualPartType type : values()) {
                if (type.getDbValue().equals(value)) {
                    return type;
                }
            }
        }
        return NONE;
    }
}
