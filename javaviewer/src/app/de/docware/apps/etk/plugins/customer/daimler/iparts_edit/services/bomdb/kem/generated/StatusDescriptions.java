package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for statusDescriptions complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="statusDescriptions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="steeringStatusDescription1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringStatusDescription2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "statusDescriptions", propOrder = {
        "steeringStatusDescription1",
        "steeringStatusDescription2"
})
public class StatusDescriptions {

    protected String steeringStatusDescription1;
    protected String steeringStatusDescription2;

    /**
     * Gets the value of the steeringStatusDescription1 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSteeringStatusDescription1() {
        return steeringStatusDescription1;
    }

    /**
     * Sets the value of the steeringStatusDescription1 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSteeringStatusDescription1(String value) {
        this.steeringStatusDescription1 = value;
    }

    /**
     * Gets the value of the steeringStatusDescription2 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSteeringStatusDescription2() {
        return steeringStatusDescription2;
    }

    /**
     * Sets the value of the steeringStatusDescription2 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSteeringStatusDescription2(String value) {
        this.steeringStatusDescription2 = value;
    }

}
