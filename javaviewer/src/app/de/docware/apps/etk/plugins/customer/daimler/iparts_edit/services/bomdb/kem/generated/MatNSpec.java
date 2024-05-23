package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for matNSpec complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="matNSpec">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="basicMaterial" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="daimlerMaterialDeliveryPropertySpecification" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "matNSpec", propOrder = {
        "basicMaterial",
        "daimlerMaterialDeliveryPropertySpecification"
})
public class MatNSpec {

    protected String basicMaterial;
    protected String daimlerMaterialDeliveryPropertySpecification;

    /**
     * Gets the value of the basicMaterial property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getBasicMaterial() {
        return basicMaterial;
    }

    /**
     * Sets the value of the basicMaterial property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setBasicMaterial(String value) {
        this.basicMaterial = value;
    }

    /**
     * Gets the value of the daimlerMaterialDeliveryPropertySpecification property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDaimlerMaterialDeliveryPropertySpecification() {
        return daimlerMaterialDeliveryPropertySpecification;
    }

    /**
     * Sets the value of the daimlerMaterialDeliveryPropertySpecification property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDaimlerMaterialDeliveryPropertySpecification(String value) {
        this.daimlerMaterialDeliveryPropertySpecification = value;
    }

}
