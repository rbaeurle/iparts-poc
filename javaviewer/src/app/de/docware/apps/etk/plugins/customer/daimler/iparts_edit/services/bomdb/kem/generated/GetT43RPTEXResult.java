package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RPTEXResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RPTEXResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="f14BasicDatas" type="{http://bomDbServices.eng.dai/}f14BasicDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RPTEXResult", propOrder = {
        "queryHead",
        "f14BasicDatas"
})
public class GetT43RPTEXResult {

    protected QueryHead queryHead;
    protected F14BasicDatas f14BasicDatas;

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
     * Gets the value of the f14BasicDatas property.
     *
     * @return possible object is
     * {@link F14BasicDatas }
     */
    public F14BasicDatas getF14BasicDatas() {
        return f14BasicDatas;
    }

    /**
     * Sets the value of the f14BasicDatas property.
     *
     * @param value allowed object is
     *              {@link F14BasicDatas }
     */
    public void setF14BasicDatas(F14BasicDatas value) {
        this.f14BasicDatas = value;
    }

}
