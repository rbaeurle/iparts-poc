package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for getAssemblyStructureMultiLevelInput complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getAssemblyStructureMultiLevelInput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="queryResultAttributes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="queryLanguage" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="partsListObjectsML" type="{http://bomDbServices.eng.dai/}partsListObjectsML"/>
 *         &lt;element name="releaseDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="latestVersion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="releaseFlag" type="{http://bomDbServices.eng.dai/}releaseFlag" minOccurs="0"/>
 *         &lt;element name="maxNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="usageOrContent" type="{http://bomDbServices.eng.dai/}usageOrContent"/>
 *         &lt;element name="maxLevel" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="sparePartsIncluding" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="showS05PositionTexts" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="explainAttributes" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="explanationLanguage" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="sparePartIdentifierSpecification" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAssemblyStructureMultiLevelInput", propOrder = {
        "queryName",
        "queryResultAttributes",
        "queryLanguage",
        "partsListObjectsML",
        "releaseDate",
        "latestVersion",
        "releaseFlag",
        "maxNumber",
        "usageOrContent",
        "maxLevel",
        "sparePartsIncluding",
        "showS05PositionTexts",
        "explainAttributes",
        "explanationLanguage",
        "sparePartIdentifierSpecification"
})
public class GetAssemblyStructureMultiLevelInput {

    protected String queryName;
    @XmlElement(nillable = true)
    protected List<String> queryResultAttributes;
    protected Language queryLanguage;
    @XmlElement(required = true)
    protected PartsListObjectsML partsListObjectsML;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDate;
    protected Boolean latestVersion;
    protected ReleaseFlag releaseFlag;
    protected Integer maxNumber;
    @XmlElement(required = true)
    protected UsageOrContent usageOrContent;
    protected Integer maxLevel;
    protected Boolean sparePartsIncluding;
    protected Boolean showS05PositionTexts;
    protected Boolean explainAttributes;
    protected Language explanationLanguage;
    protected String sparePartIdentifierSpecification;

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
     * Gets the value of the partsListObjectsML property.
     *
     * @return possible object is
     * {@link PartsListObjectsML }
     */
    public PartsListObjectsML getPartsListObjectsML() {
        return partsListObjectsML;
    }

    /**
     * Sets the value of the partsListObjectsML property.
     *
     * @param value allowed object is
     *              {@link PartsListObjectsML }
     */
    public void setPartsListObjectsML(PartsListObjectsML value) {
        this.partsListObjectsML = value;
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
     * Gets the value of the usageOrContent property.
     *
     * @return possible object is
     * {@link UsageOrContent }
     */
    public UsageOrContent getUsageOrContent() {
        return usageOrContent;
    }

    /**
     * Sets the value of the usageOrContent property.
     *
     * @param value allowed object is
     *              {@link UsageOrContent }
     */
    public void setUsageOrContent(UsageOrContent value) {
        this.usageOrContent = value;
    }

    /**
     * Gets the value of the maxLevel property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getMaxLevel() {
        return maxLevel;
    }

    /**
     * Sets the value of the maxLevel property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setMaxLevel(Integer value) {
        this.maxLevel = value;
    }

    /**
     * Gets the value of the sparePartsIncluding property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isSparePartsIncluding() {
        return sparePartsIncluding;
    }

    /**
     * Sets the value of the sparePartsIncluding property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSparePartsIncluding(Boolean value) {
        this.sparePartsIncluding = value;
    }

    /**
     * Gets the value of the showS05PositionTexts property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isShowS05PositionTexts() {
        return showS05PositionTexts;
    }

    /**
     * Sets the value of the showS05PositionTexts property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setShowS05PositionTexts(Boolean value) {
        this.showS05PositionTexts = value;
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
     * Gets the value of the sparePartIdentifierSpecification property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSparePartIdentifierSpecification() {
        return sparePartIdentifierSpecification;
    }

    /**
     * Sets the value of the sparePartIdentifierSpecification property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSparePartIdentifierSpecification(String value) {
        this.sparePartIdentifierSpecification = value;
    }

}
