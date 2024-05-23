package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for t3 complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="t3">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="eco" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="item" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="scope" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="position" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="package" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="locationPosition" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="ecoPosition" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="engineeringDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="manualFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="releaseDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="modificationFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="user" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="engineeringDateUser" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t3", propOrder = {
        "eco",
        "item",
        "scope",
        "position",
        "_package",
        "locationPosition",
        "version",
        "ecoPosition",
        "engineeringDate",
        "manualFlag",
        "status",
        "releaseDate",
        "modificationFlag",
        "user",
        "engineeringDateUser"
})
public class T3 {

    protected String eco;
    protected String item;
    protected String scope;
    protected Integer position;
    @XmlElement(name = "package")
    protected String _package;
    protected Integer locationPosition;
    protected Integer version;
    protected String ecoPosition;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar engineeringDate;
    protected String manualFlag;
    protected String status;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDate;
    protected String modificationFlag;
    protected String user;
    protected String engineeringDateUser;

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
     * Gets the value of the item property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getItem() {
        return item;
    }

    /**
     * Sets the value of the item property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setItem(String value) {
        this.item = value;
    }

    /**
     * Gets the value of the scope property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setScope(String value) {
        this.scope = value;
    }

    /**
     * Gets the value of the position property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setPosition(Integer value) {
        this.position = value;
    }

    /**
     * Gets the value of the package property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPackage() {
        return _package;
    }

    /**
     * Sets the value of the package property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPackage(String value) {
        this._package = value;
    }

    /**
     * Gets the value of the locationPosition property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getLocationPosition() {
        return locationPosition;
    }

    /**
     * Sets the value of the locationPosition property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setLocationPosition(Integer value) {
        this.locationPosition = value;
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
     * Gets the value of the ecoPosition property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEcoPosition() {
        return ecoPosition;
    }

    /**
     * Sets the value of the ecoPosition property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEcoPosition(String value) {
        this.ecoPosition = value;
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
     * Gets the value of the modificationFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getModificationFlag() {
        return modificationFlag;
    }

    /**
     * Sets the value of the modificationFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setModificationFlag(String value) {
        this.modificationFlag = value;
    }

    /**
     * Gets the value of the user property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUser(String value) {
        this.user = value;
    }

    /**
     * Gets the value of the engineeringDateUser property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEngineeringDateUser() {
        return engineeringDateUser;
    }

    /**
     * Sets the value of the engineeringDateUser property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEngineeringDateUser(String value) {
        this.engineeringDateUser = value;
    }

}
