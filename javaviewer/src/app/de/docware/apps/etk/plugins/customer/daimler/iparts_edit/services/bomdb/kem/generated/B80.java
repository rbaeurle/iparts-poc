package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for b80 complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b80">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="scope" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="position" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="releaseDateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="releaseDateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="engineeringDateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="engineeringDateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="manualFlagFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="manualFlagFromExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="manualFlagTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="manualFlagToExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="versionFrom" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="versionTo" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="statusFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusFromExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusToExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bodyTypes" type="{http://bomDbServices.eng.dai/}bodyTypes" minOccurs="0"/>
 *         &lt;element name="maturityLevel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="maturityLevelExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="productGroup" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="productGroupExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="codeRule" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="scopeMasterDatas" type="{http://bomDbServices.eng.dai/}scopeMasterDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b80", propOrder = {
        "code",
        "scope",
        "position",
        "releaseDateFrom",
        "releaseDateTo",
        "engineeringDateFrom",
        "engineeringDateTo",
        "manualFlagFrom",
        "manualFlagFromExplanation",
        "manualFlagTo",
        "manualFlagToExplanation",
        "ecoFrom",
        "ecoTo",
        "versionFrom",
        "versionTo",
        "statusFrom",
        "statusFromExplanation",
        "statusTo",
        "statusToExplanation",
        "steeringType",
        "steeringTypeExplanation",
        "bodyTypes",
        "maturityLevel",
        "maturityLevelExplanation",
        "productGroup",
        "productGroupExplanation",
        "codeRule",
        "scopeMasterDatas"
})
public class B80 {

    protected String code;
    protected String scope;
    protected Integer position;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDateFrom;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDateTo;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar engineeringDateFrom;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar engineeringDateTo;
    protected String manualFlagFrom;
    protected String manualFlagFromExplanation;
    protected String manualFlagTo;
    protected String manualFlagToExplanation;
    protected String ecoFrom;
    protected String ecoTo;
    protected Integer versionFrom;
    protected Integer versionTo;
    protected String statusFrom;
    protected String statusFromExplanation;
    protected String statusTo;
    protected String statusToExplanation;
    protected String steeringType;
    protected String steeringTypeExplanation;
    protected BodyTypes bodyTypes;
    protected String maturityLevel;
    protected String maturityLevelExplanation;
    protected String productGroup;
    protected String productGroupExplanation;
    protected String codeRule;
    protected ScopeMasterDatas scopeMasterDatas;

    /**
     * Gets the value of the code property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Gets the value of the scope property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setScope(String value) {
        this.scope = value;
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
     * Gets the value of the engineeringDateFrom property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getEngineeringDateFrom() {
        return engineeringDateFrom;
    }

    /**
     * Sets the value of the engineeringDateFrom property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setEngineeringDateFrom(XMLGregorianCalendar value) {
        this.engineeringDateFrom = value;
    }

    /**
     * Gets the value of the engineeringDateTo property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getEngineeringDateTo() {
        return engineeringDateTo;
    }

    /**
     * Sets the value of the engineeringDateTo property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setEngineeringDateTo(XMLGregorianCalendar value) {
        this.engineeringDateTo = value;
    }

    /**
     * Gets the value of the manualFlagFrom property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getManualFlagFrom() {
        return manualFlagFrom;
    }

    /**
     * Sets the value of the manualFlagFrom property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManualFlagFrom(String value) {
        this.manualFlagFrom = value;
    }

    /**
     * Gets the value of the manualFlagFromExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getManualFlagFromExplanation() {
        return manualFlagFromExplanation;
    }

    /**
     * Sets the value of the manualFlagFromExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManualFlagFromExplanation(String value) {
        this.manualFlagFromExplanation = value;
    }

    /**
     * Gets the value of the manualFlagTo property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getManualFlagTo() {
        return manualFlagTo;
    }

    /**
     * Sets the value of the manualFlagTo property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManualFlagTo(String value) {
        this.manualFlagTo = value;
    }

    /**
     * Gets the value of the manualFlagToExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getManualFlagToExplanation() {
        return manualFlagToExplanation;
    }

    /**
     * Sets the value of the manualFlagToExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManualFlagToExplanation(String value) {
        this.manualFlagToExplanation = value;
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
     * Gets the value of the bodyTypes property.
     *
     * @return possible object is
     * {@link BodyTypes }
     */
    public BodyTypes getBodyTypes() {
        return bodyTypes;
    }

    /**
     * Sets the value of the bodyTypes property.
     *
     * @param value allowed object is
     *              {@link BodyTypes }
     */
    public void setBodyTypes(BodyTypes value) {
        this.bodyTypes = value;
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
     * Gets the value of the scopeMasterDatas property.
     *
     * @return possible object is
     * {@link ScopeMasterDatas }
     */
    public ScopeMasterDatas getScopeMasterDatas() {
        return scopeMasterDatas;
    }

    /**
     * Sets the value of the scopeMasterDatas property.
     *
     * @param value allowed object is
     *              {@link ScopeMasterDatas }
     */
    public void setScopeMasterDatas(ScopeMasterDatas value) {
        this.scopeMasterDatas = value;
    }

}
