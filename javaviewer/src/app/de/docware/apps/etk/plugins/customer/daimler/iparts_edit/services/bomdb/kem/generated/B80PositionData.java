package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for b80PositionData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b80PositionData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="versionFrom" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="versionTo" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="requestedVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="b80" type="{http://bomDbServices.eng.dai/}b80" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b80PositionData", propOrder = {
        "versionFrom",
        "versionTo",
        "requestedVersion",
        "b80"
})
public class B80PositionData {

    protected Integer versionFrom;
    protected Integer versionTo;
    protected Integer requestedVersion;
    protected B80 b80;

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
     * Gets the value of the b80 property.
     *
     * @return possible object is
     * {@link B80 }
     */
    public B80 getB80() {
        return b80;
    }

    /**
     * Sets the value of the b80 property.
     *
     * @param value allowed object is
     *              {@link B80 }
     */
    public void setB80(B80 value) {
        this.b80 = value;
    }

}
