package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for searchTablesChangedAssyStrucs complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="searchTablesChangedAssyStrucs">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="searchT43rbk" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="searchT43rbkv" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="searchT43rbkww" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="searchT43rbkzm" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "searchTablesChangedAssyStrucs", propOrder = {
        "searchT43Rbk",
        "searchT43Rbkv",
        "searchT43Rbkww",
        "searchT43Rbkzm"
})
public class SearchTablesChangedAssyStrucs {

    @XmlElement(name = "searchT43rbk")
    protected Boolean searchT43Rbk;
    @XmlElement(name = "searchT43rbkv")
    protected Boolean searchT43Rbkv;
    @XmlElement(name = "searchT43rbkww")
    protected Boolean searchT43Rbkww;
    @XmlElement(name = "searchT43rbkzm")
    protected Boolean searchT43Rbkzm;

    /**
     * Gets the value of the searchT43Rbk property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isSearchT43Rbk() {
        return searchT43Rbk;
    }

    /**
     * Sets the value of the searchT43Rbk property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSearchT43Rbk(Boolean value) {
        this.searchT43Rbk = value;
    }

    /**
     * Gets the value of the searchT43Rbkv property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isSearchT43Rbkv() {
        return searchT43Rbkv;
    }

    /**
     * Sets the value of the searchT43Rbkv property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSearchT43Rbkv(Boolean value) {
        this.searchT43Rbkv = value;
    }

    /**
     * Gets the value of the searchT43Rbkww property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isSearchT43Rbkww() {
        return searchT43Rbkww;
    }

    /**
     * Sets the value of the searchT43Rbkww property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSearchT43Rbkww(Boolean value) {
        this.searchT43Rbkww = value;
    }

    /**
     * Gets the value of the searchT43Rbkzm property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isSearchT43Rbkzm() {
        return searchT43Rbkzm;
    }

    /**
     * Sets the value of the searchT43Rbkzm property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setSearchT43Rbkzm(Boolean value) {
        this.searchT43Rbkzm = value;
    }

}
