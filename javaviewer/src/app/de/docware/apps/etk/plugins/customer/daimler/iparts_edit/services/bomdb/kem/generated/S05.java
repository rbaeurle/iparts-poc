package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for s05 complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="partsList" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="position" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="item" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="releaseDateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="releaseDateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="ecoFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="versionFrom" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="versionTo" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="statusFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusFromExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusToExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
 *         &lt;element name="s05PositionTexts" type="{http://bomDbServices.eng.dai/}s05PositionTexts" minOccurs="0"/>
 *         &lt;element name="partsListContainer" type="{http://bomDbServices.eng.dai/}itemContainer" minOccurs="0"/>
 *         &lt;element name="partMasterData" type="{http://bomDbServices.eng.dai/}partMasterData" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05", propOrder = {
        "partsList",
        "position",
        "item",
        "releaseDateFrom",
        "releaseDateTo",
        "ecoFrom",
        "ecoTo",
        "versionFrom",
        "versionTo",
        "statusFrom",
        "statusFromExplanation",
        "statusTo",
        "statusToExplanation",
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
        "s05PositionTexts",
        "partsListContainer",
        "partMasterData"
})
public class S05 {

    protected String partsList;
    protected Integer position;
    protected String item;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDateFrom;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDateTo;
    protected String ecoFrom;
    protected String ecoTo;
    protected Integer versionFrom;
    protected Integer versionTo;
    protected String statusFrom;
    protected String statusFromExplanation;
    protected String statusTo;
    protected String statusToExplanation;
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
    protected S05PositionTexts s05PositionTexts;
    protected ItemContainer partsListContainer;
    protected PartMasterData partMasterData;

    /**
     * Gets the value of the partsList property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPartsList() {
        return partsList;
    }

    /**
     * Sets the value of the partsList property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPartsList(String value) {
        this.partsList = value;
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
     * Gets the value of the releaseDateFrom property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getReleaseDateFrom() {
        return releaseDateFrom;
    }

    /**
     * Sets the value of the releaseDateFrom property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setReleaseDateFrom(XMLGregorianCalendar value) {
        this.releaseDateFrom = value;
    }

    /**
     * Gets the value of the releaseDateTo property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getReleaseDateTo() {
        return releaseDateTo;
    }

    /**
     * Sets the value of the releaseDateTo property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setReleaseDateTo(XMLGregorianCalendar value) {
        this.releaseDateTo = value;
    }

    /**
     * Gets the value of the ecoFrom property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEcoFrom() {
        return ecoFrom;
    }

    /**
     * Sets the value of the ecoFrom property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEcoFrom(String value) {
        this.ecoFrom = value;
    }

    /**
     * Gets the value of the ecoTo property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEcoTo() {
        return ecoTo;
    }

    /**
     * Sets the value of the ecoTo property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEcoTo(String value) {
        this.ecoTo = value;
    }

    /**
     * Gets the value of the versionFrom property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getVersionFrom() {
        return versionFrom;
    }

    /**
     * Sets the value of the versionFrom property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setVersionFrom(Integer value) {
        this.versionFrom = value;
    }

    /**
     * Gets the value of the versionTo property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getVersionTo() {
        return versionTo;
    }

    /**
     * Sets the value of the versionTo property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setVersionTo(Integer value) {
        this.versionTo = value;
    }

    /**
     * Gets the value of the statusFrom property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStatusFrom() {
        return statusFrom;
    }

    /**
     * Sets the value of the statusFrom property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStatusFrom(String value) {
        this.statusFrom = value;
    }

    /**
     * Gets the value of the statusFromExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStatusFromExplanation() {
        return statusFromExplanation;
    }

    /**
     * Sets the value of the statusFromExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStatusFromExplanation(String value) {
        this.statusFromExplanation = value;
    }

    /**
     * Gets the value of the statusTo property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStatusTo() {
        return statusTo;
    }

    /**
     * Sets the value of the statusTo property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStatusTo(String value) {
        this.statusTo = value;
    }

    /**
     * Gets the value of the statusToExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStatusToExplanation() {
        return statusToExplanation;
    }

    /**
     * Sets the value of the statusToExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStatusToExplanation(String value) {
        this.statusToExplanation = value;
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
     * Gets the value of the s05PositionTexts property.
     *
     * @return possible object is
     * {@link S05PositionTexts }
     */
    public S05PositionTexts getS05PositionTexts() {
        return s05PositionTexts;
    }

    /**
     * Sets the value of the s05PositionTexts property.
     *
     * @param value allowed object is
     *              {@link S05PositionTexts }
     */
    public void setS05PositionTexts(S05PositionTexts value) {
        this.s05PositionTexts = value;
    }

    /**
     * Gets the value of the partsListContainer property.
     *
     * @return possible object is
     * {@link ItemContainer }
     */
    public ItemContainer getPartsListContainer() {
        return partsListContainer;
    }

    /**
     * Sets the value of the partsListContainer property.
     *
     * @param value allowed object is
     *              {@link ItemContainer }
     */
    public void setPartsListContainer(ItemContainer value) {
        this.partsListContainer = value;
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
