package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for searchTablesChangedParts complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="searchTablesChangedParts">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="searchT43rteil" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="searchT43rteis" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="searchT43rteig" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="searchT43rteid" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="searchT43rteib" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="searchT43rtedz" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "searchTablesChangedParts", propOrder = {
        "searchT43Rteil",
        "searchT43Rteis",
        "searchT43Rteig",
        "searchT43Rteid",
        "searchT43Rteib",
        "searchT43Rtedz"
})
public class SearchTablesChangedParts {

    @XmlElement(name = "searchT43rteil")
    protected Boolean searchT43Rteil;
    @XmlElement(name = "searchT43rteis")
    protected Boolean searchT43Rteis;
    @XmlElement(name = "searchT43rteig")
    protected Boolean searchT43Rteig;
    @XmlElement(name = "searchT43rteid")
    protected Boolean searchT43Rteid;
    @XmlElement(name = "searchT43rteib")
    protected Boolean searchT43Rteib;
    @XmlElement(name = "searchT43rtedz")
    protected Boolean searchT43Rtedz;

    /**
     * Gets the value of the searchT43Rteil property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isSearchT43Rteil() {
        return searchT43Rteil;
    }

    /**
     * Sets the value of the searchT43Rteil property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSearchT43Rteil(Boolean value) {
        this.searchT43Rteil = value;
    }

    /**
     * Gets the value of the searchT43Rteis property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isSearchT43Rteis() {
        return searchT43Rteis;
    }

    /**
     * Sets the value of the searchT43Rteis property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSearchT43Rteis(Boolean value) {
        this.searchT43Rteis = value;
    }

    /**
     * Gets the value of the searchT43Rteig property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isSearchT43Rteig() {
        return searchT43Rteig;
    }

    /**
     * Sets the value of the searchT43Rteig property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSearchT43Rteig(Boolean value) {
        this.searchT43Rteig = value;
    }

    /**
     * Gets the value of the searchT43Rteid property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isSearchT43Rteid() {
        return searchT43Rteid;
    }

    /**
     * Sets the value of the searchT43Rteid property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSearchT43Rteid(Boolean value) {
        this.searchT43Rteid = value;
    }

    /**
     * Gets the value of the searchT43Rteib property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isSearchT43Rteib() {
        return searchT43Rteib;
    }

    /**
     * Sets the value of the searchT43Rteib property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSearchT43Rteib(Boolean value) {
        this.searchT43Rteib = value;
    }

    /**
     * Gets the value of the searchT43Rtedz property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isSearchT43Rtedz() {
        return searchT43Rtedz;
    }

    /**
     * Sets the value of the searchT43Rtedz property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSearchT43Rtedz(Boolean value) {
        this.searchT43Rtedz = value;
    }

}
