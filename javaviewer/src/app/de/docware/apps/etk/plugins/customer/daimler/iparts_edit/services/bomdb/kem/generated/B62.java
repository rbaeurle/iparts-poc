package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for b62 complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b62">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="toPartsList" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="versionFrom" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="versionTo" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="fromPartsList" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ckdFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b62", propOrder = {
        "toPartsList",
        "versionFrom",
        "versionTo",
        "fromPartsList",
        "ckdFlag"
})
public class B62 {

    protected String toPartsList;
    protected Integer versionFrom;
    protected Integer versionTo;
    protected String fromPartsList;
    protected String ckdFlag;

    /**
     * Gets the value of the toPartsList property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getToPartsList() {
        return toPartsList;
    }

    /**
     * Sets the value of the toPartsList property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setToPartsList(String value) {
        this.toPartsList = value;
    }

    /**
     * Gets the value of the versionFrom property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getVersionFrom() {
        return versionFrom;
    }

    /**
     * Sets the value of the versionFrom property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setVersionFrom(Integer value) {
        this.versionFrom = value;
    }

    /**
     * Gets the value of the versionTo property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getVersionTo() {
        return versionTo;
    }

    /**
     * Sets the value of the versionTo property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setVersionTo(Integer value) {
        this.versionTo = value;
    }

    /**
     * Gets the value of the fromPartsList property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFromPartsList() {
        return fromPartsList;
    }

    /**
     * Sets the value of the fromPartsList property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFromPartsList(String value) {
        this.fromPartsList = value;
    }

    /**
     * Gets the value of the ckdFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCkdFlag() {
        return ckdFlag;
    }

    /**
     * Sets the value of the ckdFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCkdFlag(String value) {
        this.ckdFlag = value;
    }

}
