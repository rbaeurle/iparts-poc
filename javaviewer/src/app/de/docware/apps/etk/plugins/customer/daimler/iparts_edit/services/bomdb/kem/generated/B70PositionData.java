package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for b70PositionData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b70PositionData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="versionFrom" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="versionTo" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="requestedVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="b70" type="{http://bomDbServices.eng.dai/}b70" minOccurs="0"/>
 *         &lt;element name="b70B90Remarks" type="{http://bomDbServices.eng.dai/}b70B90Remarks" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b70PositionData", propOrder = {
        "versionFrom",
        "versionTo",
        "requestedVersion",
        "b70",
        "b70B90Remarks"
})
public class B70PositionData {

    protected Integer versionFrom;
    protected Integer versionTo;
    protected Integer requestedVersion;
    protected B70 b70;
    protected B70B90Remarks b70B90Remarks;

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
     * Gets the value of the requestedVersion property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getRequestedVersion() {
        return requestedVersion;
    }

    /**
     * Sets the value of the requestedVersion property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setRequestedVersion(Integer value) {
        this.requestedVersion = value;
    }

    /**
     * Gets the value of the b70 property.
     *
     * @return possible object is
     * {@link B70 }
     */
    public B70 getB70() {
        return b70;
    }

    /**
     * Sets the value of the b70 property.
     *
     * @param value allowed object is
     *              {@link B70 }
     */
    public void setB70(B70 value) {
        this.b70 = value;
    }

    /**
     * Gets the value of the b70B90Remarks property.
     *
     * @return possible object is
     * {@link B70B90Remarks }
     */
    public B70B90Remarks getB70B90Remarks() {
        return b70B90Remarks;
    }

    /**
     * Sets the value of the b70B90Remarks property.
     *
     * @param value allowed object is
     *              {@link B70B90Remarks }
     */
    public void setB70B90Remarks(B70B90Remarks value) {
        this.b70B90Remarks = value;
    }

}
