package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for b90PositionData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b90PositionData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="versionFrom" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="versionTo" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="requestedVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="b90" type="{http://bomDbServices.eng.dai/}b90" minOccurs="0"/>
 *         &lt;element name="b70B90Remarks" type="{http://bomDbServices.eng.dai/}b70B90Remarks" minOccurs="0"/>
 *         &lt;element name="b90InstallationLocation" type="{http://bomDbServices.eng.dai/}b90InstallationLocation" minOccurs="0"/>
 *         &lt;element name="b90UsageLocation" type="{http://bomDbServices.eng.dai/}b90UsageLocation" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b90PositionData", propOrder = {
        "versionFrom",
        "versionTo",
        "requestedVersion",
        "b90",
        "b70B90Remarks",
        "b90InstallationLocation",
        "b90UsageLocation"
})
public class B90PositionData {

    protected Integer versionFrom;
    protected Integer versionTo;
    protected Integer requestedVersion;
    protected B90 b90;
    protected B70B90Remarks b70B90Remarks;
    protected B90InstallationLocation b90InstallationLocation;
    protected B90UsageLocation b90UsageLocation;

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
     * Gets the value of the b90 property.
     *
     * @return possible object is
     * {@link B90 }
     */
    public B90 getB90() {
        return b90;
    }

    /**
     * Sets the value of the b90 property.
     *
     * @param value allowed object is
     *              {@link B90 }
     */
    public void setB90(B90 value) {
        this.b90 = value;
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

    /**
     * Gets the value of the b90InstallationLocation property.
     *
     * @return possible object is
     * {@link B90InstallationLocation }
     */
    public B90InstallationLocation getB90InstallationLocation() {
        return b90InstallationLocation;
    }

    /**
     * Sets the value of the b90InstallationLocation property.
     *
     * @param value allowed object is
     *              {@link B90InstallationLocation }
     */
    public void setB90InstallationLocation(B90InstallationLocation value) {
        this.b90InstallationLocation = value;
    }

    /**
     * Gets the value of the b90UsageLocation property.
     *
     * @return possible object is
     * {@link B90UsageLocation }
     */
    public B90UsageLocation getB90UsageLocation() {
        return b90UsageLocation;
    }

    /**
     * Sets the value of the b90UsageLocation property.
     *
     * @param value allowed object is
     *              {@link B90UsageLocation }
     */
    public void setB90UsageLocation(B90UsageLocation value) {
        this.b90UsageLocation = value;
    }

}
