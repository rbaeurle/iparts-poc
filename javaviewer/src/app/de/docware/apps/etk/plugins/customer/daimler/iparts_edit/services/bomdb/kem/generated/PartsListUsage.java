package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for partsListUsage complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partsListUsage">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="partsList" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="models" type="{http://bomDbServices.eng.dai/}models" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partsListUsage", propOrder = {
        "partsList",
        "models"
})
public class PartsListUsage {

    protected String partsList;
    protected Models models;

    /**
     * Gets the value of the partsList property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPartsList() {
        return partsList;
    }

    /**
     * Sets the value of the partsList property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPartsList(String value) {
        this.partsList = value;
    }

    /**
     * Gets the value of the models property.
     *
     * @return possible object is
     * {@link Models }
     */
    public Models getModels() {
        return models;
    }

    /**
     * Sets the value of the models property.
     *
     * @param value allowed object is
     *              {@link Models }
     */
    public void setModels(Models value) {
        this.models = value;
    }

}
