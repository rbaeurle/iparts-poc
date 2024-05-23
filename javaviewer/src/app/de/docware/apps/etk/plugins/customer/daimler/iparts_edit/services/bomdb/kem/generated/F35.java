package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for f35 complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="f35">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="upperFlashware" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="position" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="lowerFlashware" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
 *         &lt;element name="dgvFromLowerFlashware" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dgvToLowerFlashware" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dgvFromUpperFlashware" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dgvToUpperFlashware" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="productGroup" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="productGroupExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="codeRule" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="maturityLevel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="maturityLevelExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="wasWillFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="wasWillItem" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="plantSupplies" type="{http://bomDbServices.eng.dai/}plantSupplies" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "f35", propOrder = {
        "upperFlashware",
        "position",
        "lowerFlashware",
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
        "dgvFromLowerFlashware",
        "dgvToLowerFlashware",
        "dgvFromUpperFlashware",
        "dgvToUpperFlashware",
        "productGroup",
        "productGroupExplanation",
        "codeRule",
        "maturityLevel",
        "maturityLevelExplanation",
        "text",
        "wasWillFlag",
        "wasWillItem",
        "steeringType",
        "steeringTypeExplanation",
        "plantSupplies"
})
public class F35 {

    protected String upperFlashware;
    protected Integer position;
    protected String lowerFlashware;
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
    protected String dgvFromLowerFlashware;
    protected String dgvToLowerFlashware;
    protected String dgvFromUpperFlashware;
    protected String dgvToUpperFlashware;
    protected String productGroup;
    protected String productGroupExplanation;
    protected String codeRule;
    protected String maturityLevel;
    protected String maturityLevelExplanation;
    protected String text;
    protected String wasWillFlag;
    protected String wasWillItem;
    protected String steeringType;
    protected String steeringTypeExplanation;
    protected PlantSupplies plantSupplies;

    /**
     * Gets the value of the upperFlashware property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getUpperFlashware() {
        return upperFlashware;
    }

    /**
     * Sets the value of the upperFlashware property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUpperFlashware(String value) {
        this.upperFlashware = value;
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
     * Gets the value of the lowerFlashware property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLowerFlashware() {
        return lowerFlashware;
    }

    /**
     * Sets the value of the lowerFlashware property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLowerFlashware(String value) {
        this.lowerFlashware = value;
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
     * Gets the value of the dgvFromLowerFlashware property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDgvFromLowerFlashware() {
        return dgvFromLowerFlashware;
    }

    /**
     * Sets the value of the dgvFromLowerFlashware property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDgvFromLowerFlashware(String value) {
        this.dgvFromLowerFlashware = value;
    }

    /**
     * Gets the value of the dgvToLowerFlashware property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDgvToLowerFlashware() {
        return dgvToLowerFlashware;
    }

    /**
     * Sets the value of the dgvToLowerFlashware property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDgvToLowerFlashware(String value) {
        this.dgvToLowerFlashware = value;
    }

    /**
     * Gets the value of the dgvFromUpperFlashware property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDgvFromUpperFlashware() {
        return dgvFromUpperFlashware;
    }

    /**
     * Sets the value of the dgvFromUpperFlashware property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDgvFromUpperFlashware(String value) {
        this.dgvFromUpperFlashware = value;
    }

    /**
     * Gets the value of the dgvToUpperFlashware property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDgvToUpperFlashware() {
        return dgvToUpperFlashware;
    }

    /**
     * Sets the value of the dgvToUpperFlashware property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDgvToUpperFlashware(String value) {
        this.dgvToUpperFlashware = value;
    }

    /**
     * Gets the value of the productGroup property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getProductGroup() {
        return productGroup;
    }

    /**
     * Sets the value of the productGroup property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProductGroup(String value) {
        this.productGroup = value;
    }

    /**
     * Gets the value of the productGroupExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getProductGroupExplanation() {
        return productGroupExplanation;
    }

    /**
     * Sets the value of the productGroupExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProductGroupExplanation(String value) {
        this.productGroupExplanation = value;
    }

    /**
     * Gets the value of the codeRule property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCodeRule() {
        return codeRule;
    }

    /**
     * Sets the value of the codeRule property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCodeRule(String value) {
        this.codeRule = value;
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
     * Gets the value of the text property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setText(String value) {
        this.text = value;
    }

    /**
     * Gets the value of the wasWillFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getWasWillFlag() {
        return wasWillFlag;
    }

    /**
     * Sets the value of the wasWillFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setWasWillFlag(String value) {
        this.wasWillFlag = value;
    }

    /**
     * Gets the value of the wasWillItem property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getWasWillItem() {
        return wasWillItem;
    }

    /**
     * Sets the value of the wasWillItem property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setWasWillItem(String value) {
        this.wasWillItem = value;
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

}
