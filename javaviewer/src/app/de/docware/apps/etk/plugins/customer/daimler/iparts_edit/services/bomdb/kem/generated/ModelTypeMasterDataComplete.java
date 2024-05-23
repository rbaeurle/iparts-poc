package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for modelTypeMasterDataComplete complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="modelTypeMasterDataComplete">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modelType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="modelTypeMasterData" type="{http://bomDbServices.eng.dai/}modelTypeMasterData" minOccurs="0"/>
 *         &lt;element name="modelTypeLangDatas" type="{http://bomDbServices.eng.dai/}modelTypeLangDatas" minOccurs="0"/>
 *         &lt;element name="itemScopes" type="{http://bomDbServices.eng.dai/}itemScopes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "modelTypeMasterDataComplete", propOrder = {
        "modelType",
        "version",
        "modelTypeMasterData",
        "modelTypeLangDatas",
        "itemScopes"
})
public class ModelTypeMasterDataComplete {

    protected String modelType;
    protected int version;
    protected ModelTypeMasterData modelTypeMasterData;
    protected ModelTypeLangDatas modelTypeLangDatas;
    protected ItemScopes itemScopes;

    /**
     * Gets the value of the modelType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getModelType() {
        return modelType;
    }

    /**
     * Sets the value of the modelType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setModelType(String value) {
        this.modelType = value;
    }

    /**
     * Gets the value of the version property.
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     */
    public void setVersion(int value) {
        this.version = value;
    }

    /**
     * Gets the value of the modelTypeMasterData property.
     *
     * @return possible object is
     * {@link ModelTypeMasterData }
     */
    public ModelTypeMasterData getModelTypeMasterData() {
        return modelTypeMasterData;
    }

    /**
     * Sets the value of the modelTypeMasterData property.
     *
     * @param value allowed object is
     *              {@link ModelTypeMasterData }
     */
    public void setModelTypeMasterData(ModelTypeMasterData value) {
        this.modelTypeMasterData = value;
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

    /**
     * Gets the value of the itemScopes property.
     *
     * @return possible object is
     * {@link ItemScopes }
     */
    public ItemScopes getItemScopes() {
        return itemScopes;
    }

    /**
     * Sets the value of the itemScopes property.
     *
     * @param value allowed object is
     *              {@link ItemScopes }
     */
    public void setItemScopes(ItemScopes value) {
        this.itemScopes = value;
    }

}
