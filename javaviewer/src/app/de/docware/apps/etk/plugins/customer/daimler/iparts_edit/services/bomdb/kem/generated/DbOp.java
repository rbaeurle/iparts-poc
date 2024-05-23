package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dbOp.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="dbOp">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="LT"/>
 *     &lt;enumeration value="LE"/>
 *     &lt;enumeration value="EQ"/>
 *     &lt;enumeration value="NOT_EQ"/>
 *     &lt;enumeration value="GE"/>
 *     &lt;enumeration value="GT"/>
 *     &lt;enumeration value="LIKE"/>
 *     &lt;enumeration value="NOT_LIKE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "dbOp")
@XmlEnum
public enum DbOp {

    LT,
    LE,
    EQ,
    NOT_EQ,
    GE,
    GT,
    LIKE,
    NOT_LIKE;

    public String value() {
        return name();
    }

    public static DbOp fromValue(String v) {
        return valueOf(v);
    }

}
