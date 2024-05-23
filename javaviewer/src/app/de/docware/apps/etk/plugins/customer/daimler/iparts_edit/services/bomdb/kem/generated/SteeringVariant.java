package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for steeringVariant complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="steeringVariant">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="steeringType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringRequest" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringStatusExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringStatusDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "steeringVariant", propOrder = {
        "steeringType",
        "steeringTypeExplanation",
        "steeringRequest",
        "steeringStatus",
        "steeringStatusExplanation",
        "steeringStatusDescription"
})
public class SteeringVariant {

    protected String steeringType;
    protected String steeringTypeExplanation;
    protected String steeringRequest;
    protected String steeringStatus;
    protected String steeringStatusExplanation;
    protected String steeringStatusDescription;

    /**
     * Gets the value of the steeringType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSteeringType() {
        return steeringType;
    }

    /**
     * Sets the value of the steeringType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSteeringType(String value) {
        this.steeringType = value;
    }

    /**
     * Gets the value of the steeringTypeExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSteeringTypeExplanation() {
        return steeringTypeExplanation;
    }

    /**
     * Sets the value of the steeringTypeExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSteeringTypeExplanation(String value) {
        this.steeringTypeExplanation = value;
    }

    /**
     * Gets the value of the steeringRequest property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSteeringRequest() {
        return steeringRequest;
    }

    /**
     * Sets the value of the steeringRequest property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSteeringRequest(String value) {
        this.steeringRequest = value;
    }

    /**
     * Gets the value of the steeringStatus property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSteeringStatus() {
        return steeringStatus;
    }

    /**
     * Sets the value of the steeringStatus property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSteeringStatus(String value) {
        this.steeringStatus = value;
    }

    /**
     * Gets the value of the steeringStatusExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSteeringStatusExplanation() {
        return steeringStatusExplanation;
    }

    /**
     * Sets the value of the steeringStatusExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSteeringStatusExplanation(String value) {
        this.steeringStatusExplanation = value;
    }

    /**
     * Gets the value of the steeringStatusDescription property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSteeringStatusDescription() {
        return steeringStatusDescription;
    }

    /**
     * Sets the value of the steeringStatusDescription property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSteeringStatusDescription(String value) {
        this.steeringStatusDescription = value;
    }

}
