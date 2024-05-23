package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for partsListObjectML complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partsListObjectML">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="object" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partsListObjectML", propOrder = {
        "object",
        "version"
})
public class PartsListObjectML {

    @XmlElement(required = true)
    protected String object;
    protected Integer version;

    /**
     * Gets the value of the object property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getObject() {
        return object;
    }

    /**
     * Sets the value of the object property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setObject(String value) {
        this.object = value;
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

}
