package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for partObject complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="part" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="versionFrom" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="dgv" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dgvFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partObject", propOrder = {
        "part",
        "version",
        "versionFrom",
        "dgv",
        "dgvFrom"
})
public class PartObject {

    protected String part;
    protected Integer version;
    protected Integer versionFrom;
    protected String dgv;
    protected String dgvFrom;

    /**
     * Gets the value of the part property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPart() {
        return part;
    }

    /**
     * Sets the value of the part property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPart(String value) {
        this.part = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setVersion(Integer value) {
        this.version = value;
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
     * Gets the value of the dgv property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDgv() {
        return dgv;
    }

    /**
     * Sets the value of the dgv property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDgv(String value) {
        this.dgv = value;
    }

    /**
     * Gets the value of the dgvFrom property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDgvFrom() {
        return dgvFrom;
    }

    /**
     * Sets the value of the dgvFrom property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDgvFrom(String value) {
        this.dgvFrom = value;
    }

}
