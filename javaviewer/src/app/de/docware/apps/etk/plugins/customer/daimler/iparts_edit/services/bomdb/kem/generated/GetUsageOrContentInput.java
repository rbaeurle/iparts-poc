package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for getUsageOrContentInput complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getUsageOrContentInput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="queryResultAttributes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="genericAttribute" type="{http://bomDbServices.eng.dai/}genericAttribute" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="queryContent" type="{http://bomDbServices.eng.dai/}queryContent" minOccurs="0"/>
 *         &lt;element name="queryUsage" type="{http://bomDbServices.eng.dai/}queryUsage" minOccurs="0"/>
 *         &lt;element name="contentObjects" type="{http://bomDbServices.eng.dai/}contentObjects" minOccurs="0"/>
 *         &lt;element name="items" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="releaseDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="releaseDateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="releaseDateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="engineeringDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="engineeringDateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="engineeringDateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="latestVersion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="releaseFlag" type="{http://bomDbServices.eng.dai/}releaseFlag" minOccurs="0"/>
 *         &lt;element name="maxNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="explainAttributes" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="explanationLanguage" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getUsageOrContentInput", propOrder = {
        "queryName",
        "queryResultAttributes",
        "genericAttribute",
        "queryContent",
        "queryUsage",
        "contentObjects",
        "items",
        "releaseDate",
        "releaseDateFrom",
        "releaseDateTo",
        "engineeringDate",
        "engineeringDateFrom",
        "engineeringDateTo",
        "latestVersion",
        "releaseFlag",
        "maxNumber",
        "explainAttributes",
        "explanationLanguage"
})
public class GetUsageOrContentInput {

    protected String queryName;
    @XmlElement(nillable = true)
    protected List<String> queryResultAttributes;
    @XmlElement(nillable = true)
    protected List<GenericAttribute> genericAttribute;
    protected QueryContent queryContent;
    protected QueryUsage queryUsage;
    protected ContentObjects contentObjects;
    @XmlElement(nillable = true)
    protected List<String> items;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDate;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDateFrom;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDateTo;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar engineeringDate;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar engineeringDateFrom;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar engineeringDateTo;
    protected Boolean latestVersion;
    protected ReleaseFlag releaseFlag;
    protected Integer maxNumber;
    protected Boolean explainAttributes;
    protected Language explanationLanguage;

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
     * Gets the value of the queryContent property.
     *
     * @return possible object is
     * {@link QueryContent }
     */
    public QueryContent getQueryContent() {
        return queryContent;
    }

    /**
     * Sets the value of the queryContent property.
     *
     * @param value allowed object is
     *              {@link QueryContent }
     */
    public void setQueryContent(QueryContent value) {
        this.queryContent = value;
    }

    /**
     * Gets the value of the queryUsage property.
     *
     * @return possible object is
     * {@link QueryUsage }
     */
    public QueryUsage getQueryUsage() {
        return queryUsage;
    }

    /**
     * Sets the value of the queryUsage property.
     *
     * @param value allowed object is
     *              {@link QueryUsage }
     */
    public void setQueryUsage(QueryUsage value) {
        this.queryUsage = value;
    }

    /**
     * Gets the value of the contentObjects property.
     *
     * @return possible object is
     * {@link ContentObjects }
     */
    public ContentObjects getContentObjects() {
        return contentObjects;
    }

    /**
     * Sets the value of the contentObjects property.
     *
     * @param value allowed object is
     *              {@link ContentObjects }
     */
    public void setContentObjects(ContentObjects value) {
        this.contentObjects = value;
    }

    /**
     * Gets the value of the items property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the items property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItems().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getItems() {
        if (items == null) {
            items = new ArrayList<String>();
        }
        return this.items;
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

}
