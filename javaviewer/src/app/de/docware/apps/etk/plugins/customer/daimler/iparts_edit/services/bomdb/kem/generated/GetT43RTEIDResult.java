package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RTEIDResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RTEIDResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="partSpecialMasterDatas" type="{http://bomDbServices.eng.dai/}partSpecialMasterDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RTEIDResult", propOrder = {
        "queryHead",
        "partSpecialMasterDatas"
})
public class GetT43RTEIDResult {

    protected QueryHead queryHead;
    protected PartSpecialMasterDatas partSpecialMasterDatas;

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
     * Gets the value of the partSpecialMasterDatas property.
     *
     * @return possible object is
     * {@link PartSpecialMasterDatas }
     */
    public PartSpecialMasterDatas getPartSpecialMasterDatas() {
        return partSpecialMasterDatas;
    }

    /**
     * Sets the value of the partSpecialMasterDatas property.
     *
     * @param value allowed object is
     *              {@link PartSpecialMasterDatas }
     */
    public void setPartSpecialMasterDatas(PartSpecialMasterDatas value) {
        this.partSpecialMasterDatas = value;
    }

}