package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RSNRUResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RSNRUResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="itemScopes" type="{http://bomDbServices.eng.dai/}itemScopes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RSNRUResult", propOrder = {
        "queryHead",
        "itemScopes"
})
public class GetT43RSNRUResult {

    protected QueryHead queryHead;
    protected ItemScopes itemScopes;

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
     * Gets the value of the itemScopes property.
     *
     * @return possible object is
     * {@link ItemScopes }
     */
    public ItemScopes getItemScopes() {
        return itemScopes;
    }

    /**
     * Sets the value of the itemScopes property.
     *
     * @param value allowed object is
     *              {@link ItemScopes }
     */
    public void setItemScopes(ItemScopes value) {
        this.itemScopes = value;
    }

}
