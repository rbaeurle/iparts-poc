package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RBMASResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RBMASResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="moduleLangDatas" type="{http://bomDbServices.eng.dai/}moduleLangDatas" minOccurs="0"/>
 *         &lt;element name="modelTypeLangDatas" type="{http://bomDbServices.eng.dai/}modelTypeLangDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RBMASResult", propOrder = {
        "queryHead",
        "moduleLangDatas",
        "modelTypeLangDatas"
})
public class GetT43RBMASResult {

    protected QueryHead queryHead;
    protected ModuleLangDatas moduleLangDatas;
    protected ModelTypeLangDatas modelTypeLangDatas;

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
     * Gets the value of the moduleLangDatas property.
     *
     * @return possible object is
     * {@link ModuleLangDatas }
     */
    public ModuleLangDatas getModuleLangDatas() {
        return moduleLangDatas;
    }

    /**
     * Sets the value of the moduleLangDatas property.
     *
     * @param value allowed object is
     *              {@link ModuleLangDatas }
     */
    public void setModuleLangDatas(ModuleLangDatas value) {
        this.moduleLangDatas = value;
    }

    /**
     * Gets the value of the modelTypeLangDatas property.
     *
     * @return possible object is
     * {@link ModelTypeLangDatas }
     */
    public ModelTypeLangDatas getModelTypeLangDatas() {
        return modelTypeLangDatas;
    }

    /**
     * Sets the value of the modelTypeLangDatas property.
     *
     * @param value allowed object is
     *              {@link ModelTypeLangDatas }
     */
    public void setModelTypeLangDatas(ModelTypeLangDatas value) {
        this.modelTypeLangDatas = value;
    }

}
