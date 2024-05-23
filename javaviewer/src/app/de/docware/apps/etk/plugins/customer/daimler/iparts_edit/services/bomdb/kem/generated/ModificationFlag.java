package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for modificationFlag.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="modificationFlag">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="directOnly"/>
 *     &lt;enumeration value="indirectOnly"/>
 *     &lt;enumeration value="both"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "modificationFlag")
@XmlEnum
public enum ModificationFlag {

    @XmlEnumValue("directOnly")
    DIRECT_ONLY("directOnly"),
    @XmlEnumValue("indirectOnly")
    INDIRECT_ONLY("indirectOnly"),
    @XmlEnumValue("both")
    BOTH("both");
    private final String value;

    ModificationFlag(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ModificationFlag fromValue(String v) {
        for (ModificationFlag c : ModificationFlag.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
