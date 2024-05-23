package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getPartMasterDataResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getPartMasterDataResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="partMasterDatas" type="{http://bomDbServices.eng.dai/}partMasterDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getPartMasterDataResult", propOrder = {
        "queryHead",
        "partMasterDatas"
})
public class GetPartMasterDataResult {

    protected QueryHead queryHead;
    protected PartMasterDatas partMasterDatas;

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
     * Gets the value of the partMasterDatas property.
     *
     * @return possible object is
     * {@link PartMasterDatas }
     */
    public PartMasterDatas getPartMasterDatas() {
        return partMasterDatas;
    }

    /**
     * Sets the value of the partMasterDatas property.
     *
     * @param value allowed object is
     *              {@link PartMasterDatas }
     */
    public void setPartMasterDatas(PartMasterDatas value) {
        this.partMasterDatas = value;
    }

}
