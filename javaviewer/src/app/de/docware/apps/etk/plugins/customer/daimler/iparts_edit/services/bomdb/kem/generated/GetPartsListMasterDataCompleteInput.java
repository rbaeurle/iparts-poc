package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for getPartsListMasterDataCompleteInput complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getPartsListMasterDataCompleteInput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="queryResultAttributes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="queryLanguage" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="genericAttribute" type="{http://bomDbServices.eng.dai/}genericAttribute" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="bcsObjects" type="{http://bomDbServices.eng.dai/}bcsObjects"/>
 *         &lt;element name="releaseDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="engineeringDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="timeStampFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="timeStampTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="latestVersion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="eco" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="releaseFlag" type="{http://bomDbServices.eng.dai/}releaseFlag" minOccurs="0"/>
 *         &lt;element name="maxNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="scopeFlags" type="{http://bomDbServices.eng.dai/}scopeFlags" minOccurs="0"/>
 *         &lt;element name="explainAttributes" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="explanationLanguage" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="includeEdsPlants" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="searchPlantSupplies" type="{http://bomDbServices.eng.dai/}searchPlantSupplies" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getPartsListMasterDataCompleteInput", propOrder = {
        "queryName",
        "queryResultAttributes",
        "queryLanguage",
        "genericAttribute",
        "bcsObjects",
        "releaseDate",
        "engineeringDate",
        "timeStampFrom",
        "timeStampTo",
        "latestVersion",
        "eco",
        "releaseFlag",
        "maxNumber",
        "scopeFlags",
        "explainAttributes",
        "explanationLanguage",
        "includeEdsPlants",
        "searchPlantSupplies"
})
public class GetPartsListMasterDataCompleteInput {

    protected String queryName;
    @XmlElement(nillable = true)
    protected List<String> queryResultAttributes;
    protected Language queryLanguage;
    @XmlElement(nillable = true)
    protected List<GenericAttribute> genericAttribute;
    @XmlElement(required = true)
    protected BcsObjects bcsObjects;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDate;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar engineeringDate;
    protected String timeStampFrom;
    protected String timeStampTo;
    protected Boolean latestVersion;
    protected String eco;
    protected ReleaseFlag releaseFlag;
    protected Integer maxNumber;
    protected ScopeFlags scopeFlags;
    protected Boolean explainAttributes;
    protected Language explanationLanguage;
    protected Boolean includeEdsPlants;
    protected SearchPlantSupplies searchPlantSupplies;

    /**
     * Gets the value of the queryName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getQueryName() {
        return queryName;
    }

    /**
     * Sets the value of the queryName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setQueryName(String value) {
        this.queryName = value;
    }

    /**
     * Gets the value of the queryResultAttributes property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the queryResultAttributes property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQueryResultAttributes().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getQueryResultAttributes() {
        if (queryResultAttributes == null) {
            queryResultAttributes = new ArrayList<String>();
        }
        return this.queryResultAttributes;
    }

    /**
     * Gets the value of the queryLanguage property.
     *
     * @return possible object is
     * {@link Language }
     */
    public Language getQueryLanguage() {
        return queryLanguage;
    }

    /**
     * Sets the value of the queryLanguage property.
     *
     * @param value allowed object is
     *              {@link Language }
     */
    public void setQueryLanguage(Language value) {
        this.queryLanguage = value;
    }

    /**
     * Gets the value of the genericAttribute property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the genericAttribute property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGenericAttribute().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GenericAttribute }
     */
    public List<GenericAttribute> getGenericAttribute() {
        if (genericAttribute == null) {
            genericAttribute = new ArrayList<GenericAttribute>();
        }
        return this.genericAttribute;
    }

    /**
     * Gets the value of the bcsObjects property.
     *
     * @return possible object is
     * {@link BcsObjects }
     */
    public BcsObjects getBcsObjects() {
        return bcsObjects;
    }

    /**
     * Sets the value of the bcsObjects property.
     *
     * @param value allowed object is
     *              {@link BcsObjects }
     */
    public void setBcsObjects(BcsObjects value) {
        this.bcsObjects = value;
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
     * Gets the value of the timeStampFrom property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTimeStampFrom() {
        return timeStampFrom;
    }

    /**
     * Sets the value of the timeStampFrom property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTimeStampFrom(String value) {
        this.timeStampFrom = value;
    }

    /**
     * Gets the value of the timeStampTo property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTimeStampTo() {
        return timeStampTo;
    }

    /**
     * Sets the value of the timeStampTo property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTimeStampTo(String value) {
        this.timeStampTo = value;
    }

    /**
     * Gets the value of the latestVersion property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isLatestVersion() {
        return latestVersion;
    }

    /**
     * Sets the value of the latestVersion property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setLatestVersion(Boolean value) {
        this.latestVersion = value;
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
     * Gets the value of the releaseFlag property.
     *
     * @return possible object is
     * {@link ReleaseFlag }
     */
    public ReleaseFlag getReleaseFlag() {
        return releaseFlag;
    }

    /**
     * Sets the value of the releaseFlag property.
     *
     * @param value allowed object is
     *              {@link ReleaseFlag }
     */
    public void setReleaseFlag(ReleaseFlag value) {
        this.releaseFlag = value;
    }

    /**
     * Gets the value of the maxNumber property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getMaxNumber() {
        return maxNumber;
    }

    /**
     * Sets the value of the maxNumber property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setMaxNumber(Integer value) {
        this.maxNumber = value;
    }

    /**
     * Gets the value of the scopeFlags property.
     *
     * @return possible object is
     * {@link ScopeFlags }
     */
    public ScopeFlags getScopeFlags() {
        return scopeFlags;
    }

    /**
     * Sets the value of the scopeFlags property.
     *
     * @param value allowed object is
     *              {@link ScopeFlags }
     */
    public void setScopeFlags(ScopeFlags value) {
        this.scopeFlags = value;
    }

    /**
     * Gets the value of the explainAttributes property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isExplainAttributes() {
        return explainAttributes;
    }

    /**
     * Sets the value of the explainAttributes property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setExplainAttributes(Boolean value) {
        this.explainAttributes = value;
    }

    /**
     * Gets the value of the explanationLanguage property.
     *
     * @return possible object is
     * {@link Language }
     */
    public Language getExplanationLanguage() {
        return explanationLanguage;
    }

    /**
     * Sets the value of the explanationLanguage property.
     *
     * @param value allowed object is
     *              {@link Language }
     */
    public void setExplanationLanguage(Language value) {
        this.explanationLanguage = value;
    }

    /**
     * Gets the value of the includeEdsPlants property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isIncludeEdsPlants() {
        return includeEdsPlants;
    }

    /**
     * Sets the value of the includeEdsPlants property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setIncludeEdsPlants(Boolean value) {
        this.includeEdsPlants = value;
    }

    /**
     * Gets the value of the searchPlantSupplies property.
     *
     * @return possible object is
     * {@link SearchPlantSupplies }
     */
    public SearchPlantSupplies getSearchPlantSupplies() {
        return searchPlantSupplies;
    }

    /**
     * Sets the value of the searchPlantSupplies property.
     *
     * @param value allowed object is
     *              {@link SearchPlantSupplies }
     */
    public void setSearchPlantSupplies(SearchPlantSupplies value) {
        this.searchPlantSupplies = value;
    }

}
