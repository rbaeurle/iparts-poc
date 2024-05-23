package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ecoMasterData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ecoMasterData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="language" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="eco" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="releaseDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="engineeringDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="manualFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="manualFlagExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoInBcsFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userIndex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="engineeringScope" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reason" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="engineeringChangeOver" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remark" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="legalRelevant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="legalRelevantExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="legalRelevantDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="specialToolingRelevant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="specialToolingRelevantExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="circularTechnicalLetterRelevant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="circularTechnicalLetterRelevantExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="typeApprovalRelevant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="exhaustApprovalRelevant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="exhaustApprovalRelevantExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="priority" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="afterSalesRelevant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="stoppedBy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="stopOrderType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="stopOrderTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="technicalDataRelevant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="technicalDataRelevantExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reasonKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="changeRequestNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="estimatedCancellationDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="prolongationDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cancellationEco" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="simultaneousEcos" type="{http://bomDbServices.eng.dai/}simultaneousEcos" minOccurs="0"/>
 *         &lt;element name="scheduledFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="scheduledFlagExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="project" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="projectDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoRegistration" type="{http://bomDbServices.eng.dai/}ecoRegistration" minOccurs="0"/>
 *         &lt;element name="leadingDesignRelease" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="leadingDesignReleaseExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="involvedDesignReleases" type="{http://bomDbServices.eng.dai/}involvedDesignReleases" minOccurs="0"/>
 *         &lt;element name="plantSuppliesEds" type="{http://bomDbServices.eng.dai/}plantSupplies" minOccurs="0"/>
 *         &lt;element name="plantSuppliesBcs" type="{http://bomDbServices.eng.dai/}plantSupplies" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ecoMasterData", propOrder = {
        "language",
        "eco",
        "releaseDate",
        "engineeringDate",
        "manualFlag",
        "manualFlagExplanation",
        "version",
        "status",
        "statusExplanation",
        "ecoInBcsFlag",
        "userIndex",
        "engineeringScope",
        "reason",
        "engineeringChangeOver",
        "remark",
        "legalRelevant",
        "legalRelevantExplanation",
        "legalRelevantDate",
        "specialToolingRelevant",
        "specialToolingRelevantExplanation",
        "circularTechnicalLetterRelevant",
        "circularTechnicalLetterRelevantExplanation",
        "typeApprovalRelevant",
        "exhaustApprovalRelevant",
        "exhaustApprovalRelevantExplanation",
        "priority",
        "afterSalesRelevant",
        "ecoType",
        "stoppedBy",
        "stopOrderType",
        "stopOrderTypeExplanation",
        "technicalDataRelevant",
        "technicalDataRelevantExplanation",
        "reasonKey",
        "changeRequestNumber",
        "estimatedCancellationDate",
        "prolongationDate",
        "cancellationEco",
        "simultaneousEcos",
        "scheduledFlag",
        "scheduledFlagExplanation",
        "project",
        "projectDescription",
        "ecoRegistration",
        "leadingDesignRelease",
        "leadingDesignReleaseExplanation",
        "involvedDesignReleases",
        "plantSuppliesEds",
        "plantSuppliesBcs"
})
public class EcoMasterData {

    protected Language language;
    protected String eco;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDate;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar engineeringDate;
    protected String manualFlag;
    protected String manualFlagExplanation;
    protected Integer version;
    protected String status;
    protected String statusExplanation;
    protected String ecoInBcsFlag;
    protected String userIndex;
    protected String engineeringScope;
    protected String reason;
    protected String engineeringChangeOver;
    protected String remark;
    protected String legalRelevant;
    protected String legalRelevantExplanation;
    protected String legalRelevantDate;
    protected String specialToolingRelevant;
    protected String specialToolingRelevantExplanation;
    protected String circularTechnicalLetterRelevant;
    protected String circularTechnicalLetterRelevantExplanation;
    protected String typeApprovalRelevant;
    protected String exhaustApprovalRelevant;
    protected String exhaustApprovalRelevantExplanation;
    protected String priority;
    protected String afterSalesRelevant;
    protected String ecoType;
    protected String stoppedBy;
    protected String stopOrderType;
    protected String stopOrderTypeExplanation;
    protected String technicalDataRelevant;
    protected String technicalDataRelevantExplanation;
    protected String reasonKey;
    protected String changeRequestNumber;
    protected String estimatedCancellationDate;
    protected String prolongationDate;
    protected String cancellationEco;
    protected SimultaneousEcos simultaneousEcos;
    protected String scheduledFlag;
    protected String scheduledFlagExplanation;
    protected String project;
    protected String projectDescription;
    protected EcoRegistration ecoRegistration;
    protected String leadingDesignRelease;
    protected String leadingDesignReleaseExplanation;
    protected InvolvedDesignReleases involvedDesignReleases;
    protected PlantSupplies plantSuppliesEds;
    protected PlantSupplies plantSuppliesBcs;

    /**
     * Gets the value of the language property.
     *
     * @return possible object is
     * {@link Language }
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     *
     * @param value allowed object is
     *              {@link Language }
     */
    public void setLanguage(Language value) {
        this.language = value;
    }

    /**
     * Gets the value of the eco property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEco() {
        return eco;
    }

    /**
     * Sets the value of the eco property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEco(String value) {
        this.eco = value;
    }

    /**
     * Gets the value of the releaseDate property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getReleaseDate() {
        return releaseDate;
    }

    /**
     * Sets the value of the releaseDate property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setReleaseDate(XMLGregorianCalendar value) {
        this.releaseDate = value;
    }

    /**
     * Gets the value of the engineeringDate property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getEngineeringDate() {
        return engineeringDate;
    }

    /**
     * Sets the value of the engineeringDate property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setEngineeringDate(XMLGregorianCalendar value) {
        this.engineeringDate = value;
    }

    /**
     * Gets the value of the manualFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getManualFlag() {
        return manualFlag;
    }

    /**
     * Sets the value of the manualFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManualFlag(String value) {
        this.manualFlag = value;
    }

    /**
     * Gets the value of the manualFlagExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getManualFlagExplanation() {
        return manualFlagExplanation;
    }

    /**
     * Sets the value of the manualFlagExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManualFlagExplanation(String value) {
        this.manualFlagExplanation = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setVersion(Integer value) {
        this.version = value;
    }

    /**
     * Gets the value of the status property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Gets the value of the statusExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStatusExplanation() {
        return statusExplanation;
    }

    /**
     * Sets the value of the statusExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStatusExplanation(String value) {
        this.statusExplanation = value;
    }

    /**
     * Gets the value of the ecoInBcsFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEcoInBcsFlag() {
        return ecoInBcsFlag;
    }

    /**
     * Sets the value of the ecoInBcsFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEcoInBcsFlag(String value) {
        this.ecoInBcsFlag = value;
    }

    /**
     * Gets the value of the userIndex property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getUserIndex() {
        return userIndex;
    }

    /**
     * Sets the value of the userIndex property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUserIndex(String value) {
        this.userIndex = value;
    }

    /**
     * Gets the value of the engineeringScope property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEngineeringScope() {
        return engineeringScope;
    }

    /**
     * Sets the value of the engineeringScope property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEngineeringScope(String value) {
        this.engineeringScope = value;
    }

    /**
     * Gets the value of the reason property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the value of the reason property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReason(String value) {
        this.reason = value;
    }

    /**
     * Gets the value of the engineeringChangeOver property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEngineeringChangeOver() {
        return engineeringChangeOver;
    }

    /**
     * Sets the value of the engineeringChangeOver property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEngineeringChangeOver(String value) {
        this.engineeringChangeOver = value;
    }

    /**
     * Gets the value of the remark property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRemark() {
        return remark;
    }

    /**
     * Sets the value of the remark property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRemark(String value) {
        this.remark = value;
    }

    /**
     * Gets the value of the legalRelevant property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLegalRelevant() {
        return legalRelevant;
    }

    /**
     * Sets the value of the legalRelevant property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLegalRelevant(String value) {
        this.legalRelevant = value;
    }

    /**
     * Gets the value of the legalRelevantExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLegalRelevantExplanation() {
        return legalRelevantExplanation;
    }

    /**
     * Sets the value of the legalRelevantExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLegalRelevantExplanation(String value) {
        this.legalRelevantExplanation = value;
    }

    /**
     * Gets the value of the legalRelevantDate property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLegalRelevantDate() {
        return legalRelevantDate;
    }

    /**
     * Sets the value of the legalRelevantDate property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLegalRelevantDate(String value) {
        this.legalRelevantDate = value;
    }

    /**
     * Gets the value of the specialToolingRelevant property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSpecialToolingRelevant() {
        return specialToolingRelevant;
    }

    /**
     * Sets the value of the specialToolingRelevant property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSpecialToolingRelevant(String value) {
        this.specialToolingRelevant = value;
    }

    /**
     * Gets the value of the specialToolingRelevantExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSpecialToolingRelevantExplanation() {
        return specialToolingRelevantExplanation;
    }

    /**
     * Sets the value of the specialToolingRelevantExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSpecialToolingRelevantExplanation(String value) {
        this.specialToolingRelevantExplanation = value;
    }

    /**
     * Gets the value of the circularTechnicalLetterRelevant property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCircularTechnicalLetterRelevant() {
        return circularTechnicalLetterRelevant;
    }

    /**
     * Sets the value of the circularTechnicalLetterRelevant property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCircularTechnicalLetterRelevant(String value) {
        this.circularTechnicalLetterRelevant = value;
    }

    /**
     * Gets the value of the circularTechnicalLetterRelevantExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCircularTechnicalLetterRelevantExplanation() {
        return circularTechnicalLetterRelevantExplanation;
    }

    /**
     * Sets the value of the circularTechnicalLetterRelevantExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCircularTechnicalLetterRelevantExplanation(String value) {
        this.circularTechnicalLetterRelevantExplanation = value;
    }

    /**
     * Gets the value of the typeApprovalRelevant property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTypeApprovalRelevant() {
        return typeApprovalRelevant;
    }

    /**
     * Sets the value of the typeApprovalRelevant property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTypeApprovalRelevant(String value) {
        this.typeApprovalRelevant = value;
    }

    /**
     * Gets the value of the exhaustApprovalRelevant property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getExhaustApprovalRelevant() {
        return exhaustApprovalRelevant;
    }

    /**
     * Sets the value of the exhaustApprovalRelevant property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setExhaustApprovalRelevant(String value) {
        this.exhaustApprovalRelevant = value;
    }

    /**
     * Gets the value of the exhaustApprovalRelevantExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getExhaustApprovalRelevantExplanation() {
        return exhaustApprovalRelevantExplanation;
    }

    /**
     * Sets the value of the exhaustApprovalRelevantExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setExhaustApprovalRelevantExplanation(String value) {
        this.exhaustApprovalRelevantExplanation = value;
    }

    /**
     * Gets the value of the priority property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPriority(String value) {
        this.priority = value;
    }

    /**
     * Gets the value of the afterSalesRelevant property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAfterSalesRelevant() {
        return afterSalesRelevant;
    }

    /**
     * Sets the value of the afterSalesRelevant property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAfterSalesRelevant(String value) {
        this.afterSalesRelevant = value;
    }

    /**
     * Gets the value of the ecoType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEcoType() {
        return ecoType;
    }

    /**
     * Sets the value of the ecoType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEcoType(String value) {
        this.ecoType = value;
    }

    /**
     * Gets the value of the stoppedBy property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStoppedBy() {
        return stoppedBy;
    }

    /**
     * Sets the value of the stoppedBy property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStoppedBy(String value) {
        this.stoppedBy = value;
    }

    /**
     * Gets the value of the stopOrderType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStopOrderType() {
        return stopOrderType;
    }

    /**
     * Sets the value of the stopOrderType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStopOrderType(String value) {
        this.stopOrderType = value;
    }

    /**
     * Gets the value of the stopOrderTypeExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStopOrderTypeExplanation() {
        return stopOrderTypeExplanation;
    }

    /**
     * Sets the value of the stopOrderTypeExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStopOrderTypeExplanation(String value) {
        this.stopOrderTypeExplanation = value;
    }

    /**
     * Gets the value of the technicalDataRelevant property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTechnicalDataRelevant() {
        return technicalDataRelevant;
    }

    /**
     * Sets the value of the technicalDataRelevant property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTechnicalDataRelevant(String value) {
        this.technicalDataRelevant = value;
    }

    /**
     * Gets the value of the technicalDataRelevantExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTechnicalDataRelevantExplanation() {
        return technicalDataRelevantExplanation;
    }

    /**
     * Sets the value of the technicalDataRelevantExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTechnicalDataRelevantExplanation(String value) {
        this.technicalDataRelevantExplanation = value;
    }

    /**
     * Gets the value of the reasonKey property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getReasonKey() {
        return reasonKey;
    }

    /**
     * Sets the value of the reasonKey property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReasonKey(String value) {
        this.reasonKey = value;
    }

    /**
     * Gets the value of the changeRequestNumber property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getChangeRequestNumber() {
        return changeRequestNumber;
    }

    /**
     * Sets the value of the changeRequestNumber property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setChangeRequestNumber(String value) {
        this.changeRequestNumber = value;
    }

    /**
     * Gets the value of the estimatedCancellationDate property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEstimatedCancellationDate() {
        return estimatedCancellationDate;
    }

    /**
     * Sets the value of the estimatedCancellationDate property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEstimatedCancellationDate(String value) {
        this.estimatedCancellationDate = value;
    }

    /**
     * Gets the value of the prolongationDate property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getProlongationDate() {
        return prolongationDate;
    }

    /**
     * Sets the value of the prolongationDate property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProlongationDate(String value) {
        this.prolongationDate = value;
    }

    /**
     * Gets the value of the cancellationEco property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCancellationEco() {
        return cancellationEco;
    }

    /**
     * Sets the value of the cancellationEco property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCancellationEco(String value) {
        this.cancellationEco = value;
    }

    /**
     * Gets the value of the simultaneousEcos property.
     *
     * @return possible object is
     * {@link SimultaneousEcos }
     */
    public SimultaneousEcos getSimultaneousEcos() {
        return simultaneousEcos;
    }

    /**
     * Sets the value of the simultaneousEcos property.
     *
     * @param value allowed object is
     *              {@link SimultaneousEcos }
     */
    public void setSimultaneousEcos(SimultaneousEcos value) {
        this.simultaneousEcos = value;
    }

    /**
     * Gets the value of the scheduledFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getScheduledFlag() {
        return scheduledFlag;
    }

    /**
     * Sets the value of the scheduledFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setScheduledFlag(String value) {
        this.scheduledFlag = value;
    }

    /**
     * Gets the value of the scheduledFlagExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getScheduledFlagExplanation() {
        return scheduledFlagExplanation;
    }

    /**
     * Sets the value of the scheduledFlagExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setScheduledFlagExplanation(String value) {
        this.scheduledFlagExplanation = value;
    }

    /**
     * Gets the value of the project property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getProject() {
        return project;
    }

    /**
     * Sets the value of the project property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProject(String value) {
        this.project = value;
    }

    /**
     * Gets the value of the projectDescription property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getProjectDescription() {
        return projectDescription;
    }

    /**
     * Sets the value of the projectDescription property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProjectDescription(String value) {
        this.projectDescription = value;
    }

    /**
     * Gets the value of the ecoRegistration property.
     *
     * @return possible object is
     * {@link EcoRegistration }
     */
    public EcoRegistration getEcoRegistration() {
        return ecoRegistration;
    }

    /**
     * Sets the value of the ecoRegistration property.
     *
     * @param value allowed object is
     *              {@link EcoRegistration }
     */
    public void setEcoRegistration(EcoRegistration value) {
        this.ecoRegistration = value;
    }

    /**
     * Gets the value of the leadingDesignRelease property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLeadingDesignRelease() {
        return leadingDesignRelease;
    }

    /**
     * Sets the value of the leadingDesignRelease property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLeadingDesignRelease(String value) {
        this.leadingDesignRelease = value;
    }

    /**
     * Gets the value of the leadingDesignReleaseExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLeadingDesignReleaseExplanation() {
        return leadingDesignReleaseExplanation;
    }

    /**
     * Sets the value of the leadingDesignReleaseExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLeadingDesignReleaseExplanation(String value) {
        this.leadingDesignReleaseExplanation = value;
    }

    /**
     * Gets the value of the involvedDesignReleases property.
     *
     * @return possible object is
     * {@link InvolvedDesignReleases }
     */
    public InvolvedDesignReleases getInvolvedDesignReleases() {
        return involvedDesignReleases;
    }

    /**
     * Sets the value of the involvedDesignReleases property.
     *
     * @param value allowed object is
     *              {@link InvolvedDesignReleases }
     */
    public void setInvolvedDesignReleases(InvolvedDesignReleases value) {
        this.involvedDesignReleases = value;
    }

    /**
     * Gets the value of the plantSuppliesEds property.
     *
     * @return possible object is
     * {@link PlantSupplies }
     */
    public PlantSupplies getPlantSuppliesEds() {
        return plantSuppliesEds;
    }

    /**
     * Sets the value of the plantSuppliesEds property.
     *
     * @param value allowed object is
     *              {@link PlantSupplies }
     */
    public void setPlantSuppliesEds(PlantSupplies value) {
        this.plantSuppliesEds = value;
    }

    /**
     * Gets the value of the plantSuppliesBcs property.
     *
     * @return possible object is
     * {@link PlantSupplies }
     */
    public PlantSupplies getPlantSuppliesBcs() {
        return plantSuppliesBcs;
    }

    /**
     * Sets the value of the plantSuppliesBcs property.
     *
     * @param value allowed object is
     *              {@link PlantSupplies }
     */
    public void setPlantSuppliesBcs(PlantSupplies value) {
        this.plantSuppliesBcs = value;
    }

}
