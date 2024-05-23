package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for modelMasterDataComplete complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="modelMasterDataComplete">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="model" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="modelMasterData" type="{http://bomDbServices.eng.dai/}modelMasterData" minOccurs="0"/>
 *         &lt;element name="modelLangDatas" type="{http://bomDbServices.eng.dai/}modelLangDatas" minOccurs="0"/>
 *         &lt;element name="itemScopes" type="{http://bomDbServices.eng.dai/}itemScopes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "modelMasterDataComplete", propOrder = {
        "model",
        "version",
        "modelMasterData",
        "modelLangDatas",
        "itemScopes"
})
public class ModelMasterDataComplete {

    protected String model;
    protected int version;
    protected ModelMasterData modelMasterData;
    protected ModelLangDatas modelLangDatas;
    protected ItemScopes itemScopes;

    /**
     * Gets the value of the model property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the value of the model property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setModel(String value) {
        this.model = value;
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
     * Gets the value of the modelMasterData property.
     *
     * @return possible object is
     * {@link ModelMasterData }
     */
    public ModelMasterData getModelMasterData() {
        return modelMasterData;
    }

    /**
     * Sets the value of the modelMasterData property.
     *
     * @param value allowed object is
     *              {@link ModelMasterData }
     */
    public void setModelMasterData(ModelMasterData value) {
        this.modelMasterData = value;
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
