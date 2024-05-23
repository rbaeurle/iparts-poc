package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for releaseFlag.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="releaseFlag">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="releasedOnly"/>
 *     &lt;enumeration value="releasedOrWorking"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "releaseFlag")
@XmlEnum
public enum ReleaseFlag {

    @XmlEnumValue("releasedOnly")
    RELEASED_ONLY("releasedOnly"),
    @XmlEnumValue("releasedOrWorking")
    RELEASED_OR_WORKING("releasedOrWorking");
    private final String value;

    ReleaseFlag(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ReleaseFlag fromValue(String v) {
        for (ReleaseFlag c : ReleaseFlag.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
