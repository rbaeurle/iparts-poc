package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getPartsListMasterDataCompleteResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getPartsListMasterDataCompleteResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="partsListMasterDataCompletes" type="{http://bomDbServices.eng.dai/}partsListMasterDataCompletes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getPartsListMasterDataCompleteResult", propOrder = {
        "queryHead",
        "partsListMasterDataCompletes"
})
public class GetPartsListMasterDataCompleteResult {

    protected QueryHead queryHead;
    protected PartsListMasterDataCompletes partsListMasterDataCompletes;

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
     * Gets the value of the partsListMasterDataCompletes property.
     *
     * @return possible object is
     * {@link PartsListMasterDataCompletes }
     */
    public PartsListMasterDataCompletes getPartsListMasterDataCompletes() {
        return partsListMasterDataCompletes;
    }

    /**
     * Sets the value of the partsListMasterDataCompletes property.
     *
     * @param value allowed object is
     *              {@link PartsListMasterDataCompletes }
     */
    public void setPartsListMasterDataCompletes(PartsListMasterDataCompletes value) {
        this.partsListMasterDataCompletes = value;
    }

}
