package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RBMSResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RBMSResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="modelLangDatas" type="{http://bomDbServices.eng.dai/}modelLangDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RBMSResult", propOrder = {
        "queryHead",
        "modelLangDatas"
})
public class GetT43RBMSResult {

    protected QueryHead queryHead;
    protected ModelLangDatas modelLangDatas;

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
     * Gets the value of the modelLangDatas property.
     *
     * @return possible object is
     * {@link ModelLangDatas }
     */
    public ModelLangDatas getModelLangDatas() {
        return modelLangDatas;
    }

    /**
     * Sets the value of the modelLangDatas property.
     *
     * @param value allowed object is
     *              {@link ModelLangDatas }
     */
    public void setModelLangDatas(ModelLangDatas value) {
        this.modelLangDatas = value;
    }

}
