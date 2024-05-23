package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for s05ItemUsageML complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05ItemUsageML">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="level" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="position" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="item" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remarkDigit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="alternativeFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="quantity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="quantityExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="disqualifyFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="variantsFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="variableConstructionMeasure" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="maturityLevel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="maturityLevelExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="plantSupplies" type="{http://bomDbServices.eng.dai/}plantSupplies" minOccurs="0"/>
 *         &lt;element name="acquisitionType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pipePartsListFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pipePartsListFlagExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="s05RemarkTexts" type="{http://bomDbServices.eng.dai/}s05RemarkTexts" minOccurs="0"/>
 *         &lt;element name="s05AlternativeExplanationText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="s05TighteningTorques" type="{http://bomDbServices.eng.dai/}s05TighteningTorques" minOccurs="0"/>
 *         &lt;element name="s05AlternativeTogetherWiths" type="{http://bomDbServices.eng.dai/}s05AltTogetherWiths" minOccurs="0"/>
 *         &lt;element name="partMasterDataComplete" type="{http://bomDbServices.eng.dai/}partMasterDataComplete" minOccurs="0"/>
 *         &lt;element name="partsListMasterDataComplete" type="{http://bomDbServices.eng.dai/}partsListMasterDataComplete" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05ItemUsageML", propOrder = {
        "level",
        "position",
        "item",
        "remarkDigit",
        "alternativeFlag",
        "quantity",
        "quantityExplanation",
        "steeringType",
        "steeringTypeExplanation",
        "disqualifyFlag",
        "variantsFlag",
        "variableConstructionMeasure",
        "maturityLevel",
        "maturityLevelExplanation",
        "plantSupplies",
        "acquisitionType",
        "pipePartsListFlag",
        "pipePartsListFlagExplanation",
        "s05RemarkTexts",
        "s05AlternativeExplanationText",
        "s05TighteningTorques",
        "s05AlternativeTogetherWiths",
        "partMasterDataComplete",
        "partsListMasterDataComplete"
})
public class S05ItemUsageML {

    protected Integer level;
    protected Integer position;
    protected String item;
    protected String remarkDigit;
    protected String alternativeFlag;
    protected String quantity;
    protected String quantityExplanation;
    protected String steeringType;
    protected String steeringTypeExplanation;
    protected String disqualifyFlag;
    protected String variantsFlag;
    protected String variableConstructionMeasure;
    protected String maturityLevel;
    protected String maturityLevelExplanation;
    protected PlantSupplies plantSupplies;
    protected String acquisitionType;
    protected String pipePartsListFlag;
    protected String pipePartsListFlagExplanation;
    protected S05RemarkTexts s05RemarkTexts;
    protected String s05AlternativeExplanationText;
    protected S05TighteningTorques s05TighteningTorques;
    protected S05AltTogetherWiths s05AlternativeTogetherWiths;
    protected PartMasterDataComplete partMasterDataComplete;
    protected PartsListMasterDataComplete partsListMasterDataComplete;

    /**
     * Gets the value of the level property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getLevel() {
        return level;
    }

    /**
     * Sets the value of the level property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setLevel(Integer value) {
        this.level = value;
    }

    /**
     * Gets the value of the position property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setPosition(Integer value) {
        this.position = value;
    }

    /**
     * Gets the value of the item property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getItem() {
        return item;
    }

    /**
     * Sets the value of the item property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setItem(String value) {
        this.item = value;
    }

    /**
     * Gets the value of the remarkDigit property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRemarkDigit() {
        return remarkDigit;
    }

    /**
     * Sets the value of the remarkDigit property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRemarkDigit(String value) {
        this.remarkDigit = value;
    }

    /**
     * Gets the value of the alternativeFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAlternativeFlag() {
        return alternativeFlag;
    }

    /**
     * Sets the value of the alternativeFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAlternativeFlag(String value) {
        this.alternativeFlag = value;
    }

    /**
     * Gets the value of the quantity property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getQuantity() {
        return quantity;
    }

    /**
     * Sets the value of the quantity property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setQuantity(String value) {
        this.quantity = value;
    }

    /**
     * Gets the value of the quantityExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getQuantityExplanation() {
        return quantityExplanation;
    }

    /**
     * Sets the value of the quantityExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setQuantityExplanation(String value) {
        this.quantityExplanation = value;
    }

    /**
     * Gets the value of the steeringType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSteeringType() {
        return steeringType;
    }

    /**
     * Sets the value of the steeringType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSteeringType(String value) {
        this.steeringType = value;
    }

    /**
     * Gets the value of the steeringTypeExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSteeringTypeExplanation() {
        return steeringTypeExplanation;
    }

    /**
     * Sets the value of the steeringTypeExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSteeringTypeExplanation(String value) {
        this.steeringTypeExplanation = value;
    }

    /**
     * Gets the value of the disqualifyFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDisqualifyFlag() {
        return disqualifyFlag;
    }

    /**
     * Sets the value of the disqualifyFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDisqualifyFlag(String value) {
        this.disqualifyFlag = value;
    }

    /**
     * Gets the value of the variantsFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVariantsFlag() {
        return variantsFlag;
    }

    /**
     * Sets the value of the variantsFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVariantsFlag(String value) {
        this.variantsFlag = value;
    }

    /**
     * Gets the value of the variableConstructionMeasure property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVariableConstructionMeasure() {
        return variableConstructionMeasure;
    }

    /**
     * Sets the value of the variableConstructionMeasure property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVariableConstructionMeasure(String value) {
        this.variableConstructionMeasure = value;
    }

    /**
     * Gets the value of the maturityLevel property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMaturityLevel() {
        return maturityLevel;
    }

    /**
     * Sets the value of the maturityLevel property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaturityLevel(String value) {
        this.maturityLevel = value;
    }

    /**
     * Gets the value of the maturityLevelExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMaturityLevelExplanation() {
        return maturityLevelExplanation;
    }

    /**
     * Sets the value of the maturityLevelExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaturityLevelExplanation(String value) {
        this.maturityLevelExplanation = value;
    }

    /**
     * Gets the value of the plantSupplies property.
     *
     * @return possible object is
     * {@link PlantSupplies }
     */
    public PlantSupplies getPlantSupplies() {
        return plantSupplies;
    }

    /**
     * Sets the value of the plantSupplies property.
     *
     * @param value allowed object is
     *              {@link PlantSupplies }
     */
    public void setPlantSupplies(PlantSupplies value) {
        this.plantSupplies = value;
    }

    /**
     * Gets the value of the acquisitionType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAcquisitionType() {
        return acquisitionType;
    }

    /**
     * Sets the value of the acquisitionType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAcquisitionType(String value) {
        this.acquisitionType = value;
    }

    /**
     * Gets the value of the pipePartsListFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPipePartsListFlag() {
        return pipePartsListFlag;
    }

    /**
     * Sets the value of the pipePartsListFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPipePartsListFlag(String value) {
        this.pipePartsListFlag = value;
    }

    /**
     * Gets the value of the pipePartsListFlagExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPipePartsListFlagExplanation() {
        return pipePartsListFlagExplanation;
    }

    /**
     * Sets the value of the pipePartsListFlagExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPipePartsListFlagExplanation(String value) {
        this.pipePartsListFlagExplanation = value;
    }

    /**
     * Gets the value of the s05RemarkTexts property.
     *
     * @return possible object is
     * {@link S05RemarkTexts }
     */
    public S05RemarkTexts getS05RemarkTexts() {
        return s05RemarkTexts;
    }

    /**
     * Sets the value of the s05RemarkTexts property.
     *
     * @param value allowed object is
     *              {@link S05RemarkTexts }
     */
    public void setS05RemarkTexts(S05RemarkTexts value) {
        this.s05RemarkTexts = value;
    }

    /**
     * Gets the value of the s05AlternativeExplanationText property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getS05AlternativeExplanationText() {
        return s05AlternativeExplanationText;
    }

    /**
     * Sets the value of the s05AlternativeExplanationText property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setS05AlternativeExplanationText(String value) {
        this.s05AlternativeExplanationText = value;
    }

    /**
     * Gets the value of the s05TighteningTorques property.
     *
     * @return possible object is
     * {@link S05TighteningTorques }
     */
    public S05TighteningTorques getS05TighteningTorques() {
        return s05TighteningTorques;
    }

    /**
     * Sets the value of the s05TighteningTorques property.
     *
     * @param value allowed object is
     *              {@link S05TighteningTorques }
     */
    public void setS05TighteningTorques(S05TighteningTorques value) {
        this.s05TighteningTorques = value;
    }

    /**
     * Gets the value of the s05AlternativeTogetherWiths property.
     *
     * @return possible object is
     * {@link S05AltTogetherWiths }
     */
    public S05AltTogetherWiths getS05AlternativeTogetherWiths() {
        return s05AlternativeTogetherWiths;
    }

    /**
     * Sets the value of the s05AlternativeTogetherWiths property.
     *
     * @param value allowed object is
     *              {@link S05AltTogetherWiths }
     */
    public void setS05AlternativeTogetherWiths(S05AltTogetherWiths value) {
        this.s05AlternativeTogetherWiths = value;
    }

    /**
     * Gets the value of the partMasterDataComplete property.
     *
     * @return possible object is
     * {@link PartMasterDataComplete }
     */
    public PartMasterDataComplete getPartMasterDataComplete() {
        return partMasterDataComplete;
    }

    /**
     * Sets the value of the partMasterDataComplete property.
     *
     * @param value allowed object is
     *              {@link PartMasterDataComplete }
     */
    public void setPartMasterDataComplete(PartMasterDataComplete value) {
        this.partMasterDataComplete = value;
    }

    /**
     * Gets the value of the partsListMasterDataComplete property.
     *
     * @return possible object is
     * {@link PartsListMasterDataComplete }
     */
    public PartsListMasterDataComplete getPartsListMasterDataComplete() {
        return partsListMasterDataComplete;
    }

    /**
     * Sets the value of the partsListMasterDataComplete property.
     *
     * @param value allowed object is
     *              {@link PartsListMasterDataComplete }
     */
    public void setPartsListMasterDataComplete(PartsListMasterDataComplete value) {
        this.partsListMasterDataComplete = value;
    }

}
