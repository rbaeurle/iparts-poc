package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for k3 complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="k3">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="eco" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="item" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="typeFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="position" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="modificationFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="engineeringDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="manualFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="itemContainer" type="{http://bomDbServices.eng.dai/}itemContainer" minOccurs="0"/>
 *         &lt;element name="changeNotices" type="{http://bomDbServices.eng.dai/}changeNotices" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "k3", propOrder = {
        "eco",
        "item",
        "ecoVersion",
        "typeFlag",
        "position",
        "modificationFlag",
        "engineeringDate",
        "manualFlag",
        "itemContainer",
        "changeNotices"
})
public class K3 {

    protected String eco;
    protected String item;
    protected Integer ecoVersion;
    protected String typeFlag;
    protected String position;
    protected String modificationFlag;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar engineeringDate;
    protected String manualFlag;
    protected ItemContainer itemContainer;
    protected ChangeNotices changeNotices;

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
     * Gets the value of the ecoVersion property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getEcoVersion() {
        return ecoVersion;
    }

    /**
     * Sets the value of the ecoVersion property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setEcoVersion(Integer value) {
        this.ecoVersion = value;
    }

    /**
     * Gets the value of the typeFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTypeFlag() {
        return typeFlag;
    }

    /**
     * Sets the value of the typeFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTypeFlag(String value) {
        this.typeFlag = value;
    }

    /**
     * Gets the value of the position property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPosition(String value) {
        this.position = value;
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
     * Gets the value of the itemContainer property.
     *
     * @return possible object is
     * {@link ItemContainer }
     */
    public ItemContainer getItemContainer() {
        return itemContainer;
    }

    /**
     * Sets the value of the itemContainer property.
     *
     * @param value allowed object is
     *              {@link ItemContainer }
     */
    public void setItemContainer(ItemContainer value) {
        this.itemContainer = value;
    }

    /**
     * Gets the value of the changeNotices property.
     *
     * @return possible object is
     * {@link ChangeNotices }
     */
    public ChangeNotices getChangeNotices() {
        return changeNotices;
    }

    /**
     * Sets the value of the changeNotices property.
     *
     * @param value allowed object is
     *              {@link ChangeNotices }
     */
    public void setChangeNotices(ChangeNotices value) {
        this.changeNotices = value;
    }

}
