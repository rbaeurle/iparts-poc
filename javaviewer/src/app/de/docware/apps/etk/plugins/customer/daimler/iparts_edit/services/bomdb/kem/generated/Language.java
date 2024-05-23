package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for language.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="language">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="german"/>
 *     &lt;enumeration value="english"/>
 *     &lt;enumeration value="spanish"/>
 *     &lt;enumeration value="portuguese"/>
 *     &lt;enumeration value="french"/>
 *     &lt;enumeration value="turkish"/>
 *     &lt;enumeration value="italian"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "language")
@XmlEnum
public enum Language {

    @XmlEnumValue("german")
    GERMAN("german"),
    @XmlEnumValue("english")
    ENGLISH("english"),
    @XmlEnumValue("spanish")
    SPANISH("spanish"),
    @XmlEnumValue("portuguese")
    PORTUGUESE("portuguese"),
    @XmlEnumValue("french")
    FRENCH("french"),
    @XmlEnumValue("turkish")
    TURKISH("turkish"),
    @XmlEnumValue("italian")
    ITALIAN("italian");
    private final String value;

    Language(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Language fromValue(String v) {
        for (Language c : Language.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
