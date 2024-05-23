package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for itemUsageMLOutputLayout.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="itemUsageMLOutputLayout">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="itemUsage"/>
 *     &lt;enumeration value="itemPartsListUsage"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "itemUsageMLOutputLayout")
@XmlEnum
public enum ItemUsageMLOutputLayout {

    @XmlEnumValue("itemUsage")
    ITEM_USAGE("itemUsage"),
    @XmlEnumValue("itemPartsListUsage")
    ITEM_PARTS_LIST_USAGE("itemPartsListUsage");
    private final String value;

    ItemUsageMLOutputLayout(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ItemUsageMLOutputLayout fromValue(String v) {
        for (ItemUsageMLOutputLayout c : ItemUsageMLOutputLayout.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
