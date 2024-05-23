package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RTEISResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RTEISResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="partLangDatas" type="{http://bomDbServices.eng.dai/}partLangDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RTEISResult", propOrder = {
        "queryHead",
        "partLangDatas"
})
public class GetT43RTEISResult {

    protected QueryHead queryHead;
    protected PartLangDatas partLangDatas;

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
     * Gets the value of the partLangDatas property.
     *
     * @return possible object is
     * {@link PartLangDatas }
     */
    public PartLangDatas getPartLangDatas() {
        return partLangDatas;
    }

    /**
     * Sets the value of the partLangDatas property.
     *
     * @param value allowed object is
     *              {@link PartLangDatas }
     */
    public void setPartLangDatas(PartLangDatas value) {
        this.partLangDatas = value;
    }

}
