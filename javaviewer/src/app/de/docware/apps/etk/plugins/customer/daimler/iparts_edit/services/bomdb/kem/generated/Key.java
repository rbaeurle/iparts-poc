package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for key complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="key">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="keyName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="keyFormat" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="keyContent" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "key", propOrder = {
        "keyName",
        "keyFormat",
        "keyContent"
})
public class Key {

    protected String keyName;
    protected String keyFormat;
    protected String keyContent;

    /**
     * Gets the value of the keyName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * Sets the value of the keyName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setKeyName(String value) {
        this.keyName = value;
    }

    /**
     * Gets the value of the keyFormat property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getKeyFormat() {
        return keyFormat;
    }

    /**
     * Sets the value of the keyFormat property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setKeyFormat(String value) {
        this.keyFormat = value;
    }

    /**
     * Gets the value of the keyContent property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getKeyContent() {
        return keyContent;
    }

    /**
     * Sets the value of the keyContent property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setKeyContent(String value) {
        this.keyContent = value;
    }

}
