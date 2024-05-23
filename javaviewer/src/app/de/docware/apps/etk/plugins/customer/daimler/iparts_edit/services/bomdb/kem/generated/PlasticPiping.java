package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for plasticPiping complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="plasticPiping">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="segmentLeft" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="segmentRight" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="positionInformation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "plasticPiping", propOrder = {
        "segmentLeft",
        "segmentRight",
        "positionInformation"
})
public class PlasticPiping {

    protected String segmentLeft;
    protected String segmentRight;
    protected String positionInformation;

    /**
     * Gets the value of the segmentLeft property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSegmentLeft() {
        return segmentLeft;
    }

    /**
     * Sets the value of the segmentLeft property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSegmentLeft(String value) {
        this.segmentLeft = value;
    }

    /**
     * Gets the value of the segmentRight property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSegmentRight() {
        return segmentRight;
    }

    /**
     * Sets the value of the segmentRight property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSegmentRight(String value) {
        this.segmentRight = value;
    }

    /**
     * Gets the value of the positionInformation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPositionInformation() {
        return positionInformation;
    }

    /**
     * Sets the value of the positionInformation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPositionInformation(String value) {
        this.positionInformation = value;
    }

}
