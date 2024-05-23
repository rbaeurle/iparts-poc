package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for flashwareObject complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="flashwareObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="upperFlashware" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="positions" type="{http://bomDbServices.eng.dai/}positions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "flashwareObject", propOrder = {
        "upperFlashware",
        "version",
        "positions"
})
public class FlashwareObject {

    protected String upperFlashware;
    protected Integer version;
    protected Positions positions;

    /**
     * Gets the value of the upperFlashware property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getUpperFlashware() {
        return upperFlashware;
    }

    /**
     * Sets the value of the upperFlashware property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUpperFlashware(String value) {
        this.upperFlashware = value;
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
     * Gets the value of the positions property.
     *
     * @return possible object is
     * {@link Positions }
     */
    public Positions getPositions() {
        return positions;
    }

    /**
     * Sets the value of the positions property.
     *
     * @param value allowed object is
     *              {@link Positions }
     */
    public void setPositions(Positions value) {
        this.positions = value;
    }

}
