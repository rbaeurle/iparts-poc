package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getItemUsageMultiLevelResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getItemUsageMultiLevelResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="itemUsages" type="{http://bomDbServices.eng.dai/}itemUsages" minOccurs="0"/>
 *         &lt;element name="itemPartsListUsages" type="{http://bomDbServices.eng.dai/}itemPartsListUsages" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getItemUsageMultiLevelResult", propOrder = {
        "queryHead",
        "itemUsages",
        "itemPartsListUsages"
})
public class GetItemUsageMultiLevelResult {

    protected QueryHead queryHead;
    protected ItemUsages itemUsages;
    protected ItemPartsListUsages itemPartsListUsages;

    /**
     * Gets the value of the queryHead property.
     *
     * @return possible object is
     * {@link QueryHead }
     */
    public QueryHead getQueryHead() {
        return queryHead;
    }

    /**
     * Sets the value of the queryHead property.
     *
     * @param value allowed object is
     *              {@link QueryHead }
     */
    public void setQueryHead(QueryHead value) {
        this.queryHead = value;
    }

    /**
     * Gets the value of the itemUsages property.
     *
     * @return possible object is
     * {@link ItemUsages }
     */
    public ItemUsages getItemUsages() {
        return itemUsages;
    }

    /**
     * Sets the value of the itemUsages property.
     *
     * @param value allowed object is
     *              {@link ItemUsages }
     */
    public void setItemUsages(ItemUsages value) {
        this.itemUsages = value;
    }

    /**
     * Gets the value of the itemPartsListUsages property.
     *
     * @return possible object is
     * {@link ItemPartsListUsages }
     */
    public ItemPartsListUsages getItemPartsListUsages() {
        return itemPartsListUsages;
    }

    /**
     * Sets the value of the itemPartsListUsages property.
     *
     * @param value allowed object is
     *              {@link ItemPartsListUsages }
     */
    public void setItemPartsListUsages(ItemPartsListUsages value) {
        this.itemPartsListUsages = value;
    }

}
