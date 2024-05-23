package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for partsListAdditionalMasterData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partsListAdditionalMasterData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="language" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="partsList" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="releaseDateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="releaseDateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="ecoFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="versionFrom" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="versionTo" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="statusFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="modularPartsListCreationFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="s05Remarks" type="{http://bomDbServices.eng.dai/}s05Remarks" minOccurs="0"/>
 *         &lt;element name="s05AlternativeExplanations" type="{http://bomDbServices.eng.dai/}s05AltExps" minOccurs="0"/>
 *         &lt;element name="leadingDesignRelease" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="involvedDesignReleases" type="{http://bomDbServices.eng.dai/}involvedDesignReleases" minOccurs="0"/>
 *         &lt;element name="plantSupplies" type="{http://bomDbServices.eng.dai/}plantSupplies" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partsListAdditionalMasterData", propOrder = {
        "language",
        "partsList",
        "releaseDateFrom",
        "releaseDateTo",
        "ecoFrom",
        "ecoTo",
        "versionFrom",
        "versionTo",
        "statusFrom",
        "statusTo",
        "modularPartsListCreationFlag",
        "s05Remarks",
        "s05AlternativeExplanations",
        "leadingDesignRelease",
        "involvedDesignReleases",
        "plantSupplies"
})
public class PartsListAdditionalMasterData {

    protected Language language;
    protected String partsList;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDateFrom;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDateTo;
    protected String ecoFrom;
    protected String ecoTo;
    protected Integer versionFrom;
    protected Integer versionTo;
    protected String statusFrom;
    protected String statusTo;
    protected String modularPartsListCreationFlag;
    protected S05Remarks s05Remarks;
    protected S05AltExps s05AlternativeExplanations;
    protected String leadingDesignRelease;
    protected InvolvedDesignReleases involvedDesignReleases;
    protected PlantSupplies plantSupplies;

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
     * Gets the value of the modularPartsListCreationFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getModularPartsListCreationFlag() {
        return modularPartsListCreationFlag;
    }

    /**
     * Sets the value of the modularPartsListCreationFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setModularPartsListCreationFlag(String value) {
        this.modularPartsListCreationFlag = value;
    }

    /**
     * Gets the value of the s05Remarks property.
     *
     * @return possible object is
     * {@link S05Remarks }
     */
    public S05Remarks getS05Remarks() {
        return s05Remarks;
    }

    /**
     * Sets the value of the s05Remarks property.
     *
     * @param value allowed object is
     *              {@link S05Remarks }
     */
    public void setS05Remarks(S05Remarks value) {
        this.s05Remarks = value;
    }

    /**
     * Gets the value of the s05AlternativeExplanations property.
     *
     * @return possible object is
     * {@link S05AltExps }
     */
    public S05AltExps getS05AlternativeExplanations() {
        return s05AlternativeExplanations;
    }

    /**
     * Sets the value of the s05AlternativeExplanations property.
     *
     * @param value allowed object is
     *              {@link S05AltExps }
     */
    public void setS05AlternativeExplanations(S05AltExps value) {
        this.s05AlternativeExplanations = value;
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

}
