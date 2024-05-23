package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for usageOrContent.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="usageOrContent">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="usage"/>
 *     &lt;enumeration value="content"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "usageOrContent")
@XmlEnum
public enum UsageOrContent {

    @XmlEnumValue("usage")
    USAGE("usage"),
    @XmlEnumValue("content")
    CONTENT("content");
    private final String value;

    UsageOrContent(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UsageOrContent fromValue(String v) {
        for (UsageOrContent c : UsageOrContent.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
