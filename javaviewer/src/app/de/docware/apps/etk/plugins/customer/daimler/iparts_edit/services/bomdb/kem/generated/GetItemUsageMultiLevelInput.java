package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for getItemUsageMultiLevelInput complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getItemUsageMultiLevelInput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="item" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *         &lt;element name="searchPlantSupplies" type="{http://bomDbServices.eng.dai/}searchPlantSupplies" minOccurs="0"/>
 *         &lt;element name="outputLayout" type="{http://bomDbServices.eng.dai/}itemUsageMLOutputLayout" minOccurs="0"/>
 *         &lt;element name="releaseDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="latestVersion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="releaseFlag" type="{http://bomDbServices.eng.dai/}releaseFlag" minOccurs="0"/>
 *         &lt;element name="maxNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getItemUsageMultiLevelInput", propOrder = {
        "queryName",
        "item",
        "searchPlantSupplies",
        "outputLayout",
        "releaseDate",
        "latestVersion",
        "releaseFlag",
        "maxNumber"
})
public class GetItemUsageMultiLevelInput {

    protected String queryName;
    @XmlElement(required = true)
    protected List<String> item;
    protected SearchPlantSupplies searchPlantSupplies;
    protected ItemUsageMLOutputLayout outputLayout;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDate;
    protected Boolean latestVersion;
    protected ReleaseFlag releaseFlag;
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
     * Gets the value of the item property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the item property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItem().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getItem() {
        if (item == null) {
            item = new ArrayList<String>();
        }
        return this.item;
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

    /**
     * Gets the value of the outputLayout property.
     *
     * @return possible object is
     * {@link ItemUsageMLOutputLayout }
     */
    public ItemUsageMLOutputLayout getOutputLayout() {
        return outputLayout;
    }

    /**
     * Sets the value of the outputLayout property.
     *
     * @param value allowed object is
     *              {@link ItemUsageMLOutputLayout }
     */
    public void setOutputLayout(ItemUsageMLOutputLayout value) {
        this.outputLayout = value;
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

}
