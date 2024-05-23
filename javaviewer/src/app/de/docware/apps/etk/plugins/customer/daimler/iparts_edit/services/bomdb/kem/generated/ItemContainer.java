package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for itemContainer complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="itemContainer">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modelMasterData" type="{http://bomDbServices.eng.dai/}modelMasterData" minOccurs="0"/>
 *         &lt;element name="modelTypeMasterData" type="{http://bomDbServices.eng.dai/}modelTypeMasterData" minOccurs="0"/>
 *         &lt;element name="moduleMasterData" type="{http://bomDbServices.eng.dai/}moduleMasterData" minOccurs="0"/>
 *         &lt;element name="codeMasterData" type="{http://bomDbServices.eng.dai/}codeMasterData" minOccurs="0"/>
 *         &lt;element name="partsListMasterData" type="{http://bomDbServices.eng.dai/}partsListMasterData" minOccurs="0"/>
 *         &lt;element name="partMasterData" type="{http://bomDbServices.eng.dai/}partMasterData" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "itemContainer", propOrder = {
        "modelMasterData",
        "modelTypeMasterData",
        "moduleMasterData",
        "codeMasterData",
        "partsListMasterData",
        "partMasterData"
})
public class ItemContainer {

    protected ModelMasterData modelMasterData;
    protected ModelTypeMasterData modelTypeMasterData;
    protected ModuleMasterData moduleMasterData;
    protected CodeMasterData codeMasterData;
    protected PartsListMasterData partsListMasterData;
    protected PartMasterData partMasterData;

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
     * Gets the value of the codeMasterData property.
     *
     * @return possible object is
     * {@link CodeMasterData }
     */
    public CodeMasterData getCodeMasterData() {
        return codeMasterData;
    }

    /**
     * Sets the value of the codeMasterData property.
     *
     * @param value allowed object is
     *              {@link CodeMasterData }
     */
    public void setCodeMasterData(CodeMasterData value) {
        this.codeMasterData = value;
    }

    /**
     * Gets the value of the partsListMasterData property.
     *
     * @return possible object is
     * {@link PartsListMasterData }
     */
    public PartsListMasterData getPartsListMasterData() {
        return partsListMasterData;
    }

    /**
     * Sets the value of the partsListMasterData property.
     *
     * @param value allowed object is
     *              {@link PartsListMasterData }
     */
    public void setPartsListMasterData(PartsListMasterData value) {
        this.partsListMasterData = value;
    }

    /**
     * Gets the value of the partMasterData property.
     *
     * @return possible object is
     * {@link PartMasterData }
     */
    public PartMasterData getPartMasterData() {
        return partMasterData;
    }

    /**
     * Sets the value of the partMasterData property.
     *
     * @param value allowed object is
     *              {@link PartMasterData }
     */
    public void setPartMasterData(PartMasterData value) {
        this.partMasterData = value;
    }

}
