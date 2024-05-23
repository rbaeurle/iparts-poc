package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RBMResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RBMResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="modelMasterDatas" type="{http://bomDbServices.eng.dai/}modelMasterDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RBMResult", propOrder = {
        "queryHead",
        "modelMasterDatas"
})
public class GetT43RBMResult {

    protected QueryHead queryHead;
    protected ModelMasterDatas modelMasterDatas;

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
     * Gets the value of the modelMasterDatas property.
     *
     * @return possible object is
     * {@link ModelMasterDatas }
     */
    public ModelMasterDatas getModelMasterDatas() {
        return modelMasterDatas;
    }

    /**
     * Sets the value of the modelMasterDatas property.
     *
     * @param value allowed object is
     *              {@link ModelMasterDatas }
     */
    public void setModelMasterDatas(ModelMasterDatas value) {
        this.modelMasterDatas = value;
    }

}
