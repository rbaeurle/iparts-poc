package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getChangedPartsInput complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getChangedPartsInput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="searchObjects" type="{http://bomDbServices.eng.dai/}searchObjects"/>
 *         &lt;element name="searchPlantSupplies" type="{http://bomDbServices.eng.dai/}searchPlantSupplies" minOccurs="0"/>
 *         &lt;element name="searchTables" type="{http://bomDbServices.eng.dai/}searchTablesChangedParts" minOccurs="0"/>
 *         &lt;element name="timeStampFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="timeStampTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="maxNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getChangedPartsInput", propOrder = {
        "queryName",
        "searchObjects",
        "searchPlantSupplies",
        "searchTables",
        "timeStampFrom",
        "timeStampTo",
        "maxNumber"
})
public class GetChangedPartsInput {

    protected String queryName;
    @XmlElement(required = true)
    protected SearchObjects searchObjects;
    protected SearchPlantSupplies searchPlantSupplies;
    protected SearchTablesChangedParts searchTables;
    protected String timeStampFrom;
    protected String timeStampTo;
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
     * Gets the value of the searchObjects property.
     *
     * @return possible object is
     * {@link SearchObjects }
     */
    public SearchObjects getSearchObjects() {
        return searchObjects;
    }

    /**
     * Sets the value of the searchObjects property.
     *
     * @param value allowed object is
     *              {@link SearchObjects }
     */
    public void setSearchObjects(SearchObjects value) {
        this.searchObjects = value;
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
     * Gets the value of the searchTables property.
     *
     * @return possible object is
     * {@link SearchTablesChangedParts }
     */
    public SearchTablesChangedParts getSearchTables() {
        return searchTables;
    }

    /**
     * Sets the value of the searchTables property.
     *
     * @param value allowed object is
     *              {@link SearchTablesChangedParts }
     */
    public void setSearchTables(SearchTablesChangedParts value) {
        this.searchTables = value;
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
