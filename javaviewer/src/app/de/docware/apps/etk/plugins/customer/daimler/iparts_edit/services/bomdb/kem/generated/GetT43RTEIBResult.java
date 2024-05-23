package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RTEIBResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RTEIBResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="partBcsMasterDatas" type="{http://bomDbServices.eng.dai/}partBcsMasterDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RTEIBResult", propOrder = {
        "queryHead",
        "partBcsMasterDatas"
})
public class GetT43RTEIBResult {

    protected QueryHead queryHead;
    protected PartBcsMasterDatas partBcsMasterDatas;

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
     * Gets the value of the partBcsMasterDatas property.
     *
     * @return possible object is
     * {@link PartBcsMasterDatas }
     */
    public PartBcsMasterDatas getPartBcsMasterDatas() {
        return partBcsMasterDatas;
    }

    /**
     * Sets the value of the partBcsMasterDatas property.
     *
     * @param value allowed object is
     *              {@link PartBcsMasterDatas }
     */
    public void setPartBcsMasterDatas(PartBcsMasterDatas value) {
        this.partBcsMasterDatas = value;
    }

}
