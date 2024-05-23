package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for bomDBSystem.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="bomDBSystem">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="EDS"/>
 *     &lt;enumeration value="BCS"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "bomDBSystem")
@XmlEnum
public enum BomDBSystem {

    EDS,
    BCS;

    public String value() {
        return name();
    }

    public static BomDBSystem fromValue(String v) {
        return valueOf(v);
    }

}
