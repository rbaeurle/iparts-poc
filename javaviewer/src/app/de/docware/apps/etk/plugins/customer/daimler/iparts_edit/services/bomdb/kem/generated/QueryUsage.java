package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for queryUsage.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="queryUsage">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="usageInModel"/>
 *     &lt;enumeration value="usageInPartsList"/>
 *     &lt;enumeration value="usageInModelAndPartsList"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "queryUsage")
@XmlEnum
public enum QueryUsage {

    @XmlEnumValue("usageInModel")
    USAGE_IN_MODEL("usageInModel"),
    @XmlEnumValue("usageInPartsList")
    USAGE_IN_PARTS_LIST("usageInPartsList"),
    @XmlEnumValue("usageInModelAndPartsList")
    USAGE_IN_MODEL_AND_PARTS_LIST("usageInModelAndPartsList");
    private final String value;

    QueryUsage(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static QueryUsage fromValue(String v) {
        for (QueryUsage c : QueryUsage.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
