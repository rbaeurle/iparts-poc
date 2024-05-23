package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for getT43RSNRUInput complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RSNRUInput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="queryResultAttributes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="genericAttribute" type="{http://bomDbServices.eng.dai/}genericAttribute" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="itemsWithVersions" type="{http://bomDbServices.eng.dai/}itemsWithVersions" minOccurs="0"/>
 *         &lt;element name="scopeFlags" type="{http://bomDbServices.eng.dai/}scopeFlags" minOccurs="0"/>
 *         &lt;element name="scopes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="timeStampFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="timeStampTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="validVersion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="maxNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RSNRUInput", propOrder = {
        "queryName",
        "queryResultAttributes",
        "genericAttribute",
        "itemsWithVersions",
        "scopeFlags",
        "scopes",
        "timeStampFrom",
        "timeStampTo",
        "validVersion",
        "maxNumber"
})
public class GetT43RSNRUInput {

    protected String queryName;
    @XmlElement(nillable = true)
    protected List<String> queryResultAttributes;
    @XmlElement(nillable = true)
    protected List<GenericAttribute> genericAttribute;
    protected ItemsWithVersions itemsWithVersions;
    protected ScopeFlags scopeFlags;
    @XmlElement(nillable = true)
    protected List<String> scopes;
    protected String timeStampFrom;
    protected String timeStampTo;
    protected Boolean validVersion;
    protected Integer maxNumber;

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
     * Gets the value of the itemsWithVersions property.
     *
     * @return possible object is
     * {@link ItemsWithVersions }
     */
    public ItemsWithVersions getItemsWithVersions() {
        return itemsWithVersions;
    }

    /**
     * Sets the value of the itemsWithVersions property.
     *
     * @param value allowed object is
     *              {@link ItemsWithVersions }
     */
    public void setItemsWithVersions(ItemsWithVersions value) {
        this.itemsWithVersions = value;
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
     * Gets the value of the scopes property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the scopes property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScopes().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getScopes() {
        if (scopes == null) {
            scopes = new ArrayList<String>();
        }
        return this.scopes;
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
     * Gets the value of the validVersion property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isValidVersion() {
        return validVersion;
    }

    /**
     * Sets the value of the validVersion property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setValidVersion(Boolean value) {
        this.validVersion = value;
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

}