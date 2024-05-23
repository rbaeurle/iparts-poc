package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for itemPartsListUsage complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="itemPartsListUsage">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="item" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="partsListUsages" type="{http://bomDbServices.eng.dai/}partsListUsages" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "itemPartsListUsage", propOrder = {
        "item",
        "partsListUsages"
})
public class ItemPartsListUsage {

    protected String item;
    protected PartsListUsages partsListUsages;

    /**
     * Gets the value of the item property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getItem() {
        return item;
    }

    /**
     * Sets the value of the item property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setItem(String value) {
        this.item = value;
    }

    /**
     * Gets the value of the partsListUsages property.
     *
     * @return possible object is
     * {@link PartsListUsages }
     */
    public PartsListUsages getPartsListUsages() {
        return partsListUsages;
    }

    /**
     * Sets the value of the partsListUsages property.
     *
     * @param value allowed object is
     *              {@link PartsListUsages }
     */
    public void setPartsListUsages(PartsListUsages value) {
        this.partsListUsages = value;
    }

}
