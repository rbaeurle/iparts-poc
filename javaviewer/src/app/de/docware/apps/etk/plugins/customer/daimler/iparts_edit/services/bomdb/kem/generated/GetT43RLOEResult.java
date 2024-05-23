package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RLOEResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RLOEResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="deleteRecords" type="{http://bomDbServices.eng.dai/}deleteRecords" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RLOEResult", propOrder = {
        "queryHead",
        "deleteRecords"
})
public class GetT43RLOEResult {

    protected QueryHead queryHead;
    protected DeleteRecords deleteRecords;

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
     * Gets the value of the deleteRecords property.
     *
     * @return possible object is
     * {@link DeleteRecords }
     */
    public DeleteRecords getDeleteRecords() {
        return deleteRecords;
    }

    /**
     * Sets the value of the deleteRecords property.
     *
     * @param value allowed object is
     *              {@link DeleteRecords }
     */
    public void setDeleteRecords(DeleteRecords value) {
        this.deleteRecords = value;
    }

}
