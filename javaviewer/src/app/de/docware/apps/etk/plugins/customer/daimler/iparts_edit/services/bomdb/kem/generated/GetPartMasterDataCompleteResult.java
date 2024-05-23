package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getPartMasterDataCompleteResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getPartMasterDataCompleteResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="partMasterDataCompletes" type="{http://bomDbServices.eng.dai/}partMasterDataCompletes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getPartMasterDataCompleteResult", propOrder = {
        "queryHead",
        "partMasterDataCompletes"
})
public class GetPartMasterDataCompleteResult {

    protected QueryHead queryHead;
    protected PartMasterDataCompletes partMasterDataCompletes;

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
     * Gets the value of the partMasterDataCompletes property.
     *
     * @return possible object is
     * {@link PartMasterDataCompletes }
     */
    public PartMasterDataCompletes getPartMasterDataCompletes() {
        return partMasterDataCompletes;
    }

    /**
     * Sets the value of the partMasterDataCompletes property.
     *
     * @param value allowed object is
     *              {@link PartMasterDataCompletes }
     */
    public void setPartMasterDataCompletes(PartMasterDataCompletes value) {
        this.partMasterDataCompletes = value;
    }

}
