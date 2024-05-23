package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for partsListMasterData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partsListMasterData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="language" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="partsList" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
 *         &lt;element name="versionToInvalidFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="versionToInvalidFlagExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="installationType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="installationTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="partsListType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="partsListTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="extendedDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dmuFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dmuFlagExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dgv" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringDependentFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringVariants" type="{http://bomDbServices.eng.dai/}steeringVariants" minOccurs="0"/>
 *         &lt;element name="leadingDesignRelease" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="leadingDesignReleaseExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="involvedDesignReleases" type="{http://bomDbServices.eng.dai/}involvedDesignReleases" minOccurs="0"/>
 *         &lt;element name="plantSupplies" type="{http://bomDbServices.eng.dai/}plantSupplies" minOccurs="0"/>
 *         &lt;element name="remark" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partsListMasterData", propOrder = {
        "language",
        "partsList",
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
        "versionToInvalidFlag",
        "versionToInvalidFlagExplanation",
        "installationType",
        "installationTypeExplanation",
        "partsListType",
        "partsListTypeExplanation",
        "description",
        "extendedDescription",
        "dmuFlag",
        "dmuFlagExplanation",
        "dgv",
        "steeringDependentFlag",
        "steeringVariants",
        "leadingDesignRelease",
        "leadingDesignReleaseExplanation",
        "involvedDesignReleases",
        "plantSupplies",
        "remark"
})
public class PartsListMasterData {

    protected Language language;
    protected String partsList;
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
    protected String versionToInvalidFlag;
    protected String versionToInvalidFlagExplanation;
    protected String installationType;
    protected String installationTypeExplanation;
    protected String partsListType;
    protected String partsListTypeExplanation;
    protected String description;
    protected String extendedDescription;
    protected String dmuFlag;
    protected String dmuFlagExplanation;
    protected String dgv;
    protected String steeringDependentFlag;
    protected SteeringVariants steeringVariants;
    protected String leadingDesignRelease;
    protected String leadingDesignReleaseExplanation;
    protected InvolvedDesignReleases involvedDesignReleases;
    protected PlantSupplies plantSupplies;
    protected String remark;

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
     * Gets the value of the versionToInvalidFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVersionToInvalidFlag() {
        return versionToInvalidFlag;
    }

    /**
     * Sets the value of the versionToInvalidFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVersionToInvalidFlag(String value) {
        this.versionToInvalidFlag = value;
    }

    /**
     * Gets the value of the versionToInvalidFlagExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVersionToInvalidFlagExplanation() {
        return versionToInvalidFlagExplanation;
    }

    /**
     * Sets the value of the versionToInvalidFlagExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVersionToInvalidFlagExplanation(String value) {
        this.versionToInvalidFlagExplanation = value;
    }

    /**
     * Gets the value of the installationType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getInstallationType() {
        return installationType;
    }

    /**
     * Sets the value of the installationType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setInstallationType(String value) {
        this.installationType = value;
    }

    /**
     * Gets the value of the installationTypeExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getInstallationTypeExplanation() {
        return installationTypeExplanation;
    }

    /**
     * Sets the value of the installationTypeExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setInstallationTypeExplanation(String value) {
        this.installationTypeExplanation = value;
    }

    /**
     * Gets the value of the partsListType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPartsListType() {
        return partsListType;
    }

    /**
     * Sets the value of the partsListType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPartsListType(String value) {
        this.partsListType = value;
    }

    /**
     * Gets the value of the partsListTypeExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPartsListTypeExplanation() {
        return partsListTypeExplanation;
    }

    /**
     * Sets the value of the partsListTypeExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPartsListTypeExplanation(String value) {
        this.partsListTypeExplanation = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the extendedDescription property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getExtendedDescription() {
        return extendedDescription;
    }

    /**
     * Sets the value of the extendedDescription property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setExtendedDescription(String value) {
        this.extendedDescription = value;
    }

    /**
     * Gets the value of the dmuFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDmuFlag() {
        return dmuFlag;
    }

    /**
     * Sets the value of the dmuFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDmuFlag(String value) {
        this.dmuFlag = value;
    }

    /**
     * Gets the value of the dmuFlagExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDmuFlagExplanation() {
        return dmuFlagExplanation;
    }

    /**
     * Sets the value of the dmuFlagExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDmuFlagExplanation(String value) {
        this.dmuFlagExplanation = value;
    }

    /**
     * Gets the value of the dgv property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDgv() {
        return dgv;
    }

    /**
     * Sets the value of the dgv property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDgv(String value) {
        this.dgv = value;
    }

    /**
     * Gets the value of the steeringDependentFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSteeringDependentFlag() {
        return steeringDependentFlag;
    }

    /**
     * Sets the value of the steeringDependentFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSteeringDependentFlag(String value) {
        this.steeringDependentFlag = value;
    }

    /**
     * Gets the value of the steeringVariants property.
     *
     * @return possible object is
     * {@link SteeringVariants }
     */
    public SteeringVariants getSteeringVariants() {
        return steeringVariants;
    }

    /**
     * Sets the value of the steeringVariants property.
     *
     * @param value allowed object is
     *              {@link SteeringVariants }
     */
    public void setSteeringVariants(SteeringVariants value) {
        this.steeringVariants = value;
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

}
