package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for moduleMasterDataComplete complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="moduleMasterDataComplete">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="model" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="module" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="moduleMasterData" type="{http://bomDbServices.eng.dai/}moduleMasterData" minOccurs="0"/>
 *         &lt;element name="moduleLangDatas" type="{http://bomDbServices.eng.dai/}moduleLangDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "moduleMasterDataComplete", propOrder = {
        "model",
        "module",
        "version",
        "moduleMasterData",
        "moduleLangDatas"
})
public class ModuleMasterDataComplete {

    protected String model;
    protected String module;
    protected int version;
    protected ModuleMasterData moduleMasterData;
    protected ModuleLangDatas moduleLangDatas;

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
     * Gets the value of the module property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getModule() {
        return module;
    }

    /**
     * Sets the value of the module property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setModule(String value) {
        this.module = value;
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
     * Gets the value of the moduleMasterData property.
     *
     * @return possible object is
     * {@link ModuleMasterData }
     */
    public ModuleMasterData getModuleMasterData() {
        return moduleMasterData;
    }

    /**
     * Sets the value of the moduleMasterData property.
     *
     * @param value allowed object is
     *              {@link ModuleMasterData }
     */
    public void setModuleMasterData(ModuleMasterData value) {
        this.moduleMasterData = value;
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

}
