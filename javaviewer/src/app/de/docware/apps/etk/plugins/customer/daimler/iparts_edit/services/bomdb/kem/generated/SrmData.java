package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for srmData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="srmData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="object" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="leadingDesignRelease" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="partsType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="srmDescriptions" type="{http://bomDbServices.eng.dai/}srmDescriptions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "srmData", propOrder = {
        "object",
        "leadingDesignRelease",
        "partsType",
        "srmDescriptions"
})
public class SrmData {

    protected String object;
    protected String leadingDesignRelease;
    protected String partsType;
    protected SrmDescriptions srmDescriptions;

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
     * Gets the value of the leadingDesignRelease property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLeadingDesignRelease() {
        return leadingDesignRelease;
    }

    /**
     * Sets the value of the leadingDesignRelease property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLeadingDesignRelease(String value) {
        this.leadingDesignRelease = value;
    }

    /**
     * Gets the value of the partsType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPartsType() {
        return partsType;
    }

    /**
     * Sets the value of the partsType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPartsType(String value) {
        this.partsType = value;
    }

    /**
     * Gets the value of the srmDescriptions property.
     *
     * @return possible object is
     * {@link SrmDescriptions }
     */
    public SrmDescriptions getSrmDescriptions() {
        return srmDescriptions;
    }

    /**
     * Sets the value of the srmDescriptions property.
     *
     * @param value allowed object is
     *              {@link SrmDescriptions }
     */
    public void setSrmDescriptions(SrmDescriptions value) {
        this.srmDescriptions = value;
    }

}
