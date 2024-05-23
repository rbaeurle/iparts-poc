package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for queryB70B90.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="queryB70B90">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="b70"/>
 *     &lt;enumeration value="b90"/>
 *     &lt;enumeration value="b70AndB90"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "queryB70B90")
@XmlEnum
public enum QueryB70B90 {

    @XmlEnumValue("b70")
    B_70("b70"),
    @XmlEnumValue("b90")
    B_90("b90"),
    @XmlEnumValue("b70AndB90")
    B_70_AND_B_90("b70AndB90");
    private final String value;

    QueryB70B90(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static QueryB70B90 fromValue(String v) {
        for (QueryB70B90 c : QueryB70B90.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
