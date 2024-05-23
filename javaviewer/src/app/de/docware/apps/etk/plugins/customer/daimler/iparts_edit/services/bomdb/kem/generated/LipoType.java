package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for lipoType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="lipoType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CN"/>
 *     &lt;enumeration value="FP"/>
 *     &lt;enumeration value="REF"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "lipoType")
@XmlEnum
public enum LipoType {

    CN,
    FP,
    REF;

    public String value() {
        return name();
    }

    public static LipoType fromValue(String v) {
        return valueOf(v);
    }

}
