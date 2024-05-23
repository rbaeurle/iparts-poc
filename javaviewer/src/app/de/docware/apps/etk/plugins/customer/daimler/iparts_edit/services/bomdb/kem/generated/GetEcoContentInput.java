package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for getEcoContentInput complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getEcoContentInput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="queryResultAttributes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="queryLanguage" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="genericAttribute" type="{http://bomDbServices.eng.dai/}genericAttribute" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="eco" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="releaseDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="latestVersion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="allStructures" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="modificationFlag" type="{http://bomDbServices.eng.dai/}modificationFlag" minOccurs="0"/>
 *         &lt;element name="releaseFlag" type="{http://bomDbServices.eng.dai/}releaseFlag" minOccurs="0"/>
 *         &lt;element name="maxNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="showChangeNotice" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getEcoContentInput", propOrder = {
        "queryName",
        "queryResultAttributes",
        "queryLanguage",
        "genericAttribute",
        "eco",
        "releaseDate",
        "latestVersion",
        "version",
        "allStructures",
        "modificationFlag",
        "releaseFlag",
        "maxNumber",
        "showChangeNotice"
})
public class GetEcoContentInput {

    protected String queryName;
    @XmlElement(nillable = true)
    protected List<String> queryResultAttributes;
    protected Language queryLanguage;
    @XmlElement(nillable = true)
    protected List<GenericAttribute> genericAttribute;
    @XmlElement(required = true)
    protected String eco;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDate;
    protected Boolean latestVersion;
    protected Integer version;
    protected Boolean allStructures;
    protected ModificationFlag modificationFlag;
    protected ReleaseFlag releaseFlag;
    protected Integer maxNumber;
    protected Boolean showChangeNotice;

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
     * Gets the value of the allStructures property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isAllStructures() {
        return allStructures;
    }

    /**
     * Sets the value of the allStructures property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setAllStructures(Boolean value) {
        this.allStructures = value;
    }

    /**
     * Gets the value of the modificationFlag property.
     *
     * @return possible object is
     * {@link ModificationFlag }
     */
    public ModificationFlag getModificationFlag() {
        return modificationFlag;
    }

    /**
     * Sets the value of the modificationFlag property.
     *
     * @param value allowed object is
     *              {@link ModificationFlag }
     */
    public void setModificationFlag(ModificationFlag value) {
        this.modificationFlag = value;
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
     * Gets the value of the showChangeNotice property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isShowChangeNotice() {
        return showChangeNotice;
    }

    /**
     * Sets the value of the showChangeNotice property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setShowChangeNotice(Boolean value) {
        this.showChangeNotice = value;
    }

}
