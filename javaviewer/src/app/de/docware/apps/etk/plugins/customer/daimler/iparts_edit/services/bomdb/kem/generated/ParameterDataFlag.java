package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for parameterDataFlag.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="parameterDataFlag">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="GRU"/>
 *     &lt;enumeration value="VAR"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "parameterDataFlag")
@XmlEnum
public enum ParameterDataFlag {

    GRU,
    VAR;

    public String value() {
        return name();
    }

    public static ParameterDataFlag fromValue(String v) {
        return valueOf(v);
    }

}
