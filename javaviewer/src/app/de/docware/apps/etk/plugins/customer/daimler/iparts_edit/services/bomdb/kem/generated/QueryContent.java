package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for queryContent.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="queryContent">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="modelContent"/>
 *     &lt;enumeration value="partsListContent"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "queryContent")
@XmlEnum
public enum QueryContent {

    @XmlEnumValue("modelContent")
    MODEL_CONTENT("modelContent"),
    @XmlEnumValue("partsListContent")
    PARTS_LIST_CONTENT("partsListContent");
    private final String value;

    QueryContent(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static QueryContent fromValue(String v) {
        for (QueryContent c : QueryContent.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
