package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for b90UsageLocation complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b90UsageLocation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="model" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="module" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="scope" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="position" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="versionFrom" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="versionTo" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="usageLocationType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="usageLocationTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="usageLocations" type="{http://bomDbServices.eng.dai/}usageLocations" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b90UsageLocation", propOrder = {
        "model",
        "module",
        "scope",
        "position",
        "versionFrom",
        "versionTo",
        "usageLocationType",
        "usageLocationTypeExplanation",
        "usageLocations"
})
public class B90UsageLocation {

    protected String model;
    protected String module;
    protected String scope;
    protected Integer position;
    protected Integer versionFrom;
    protected Integer versionTo;
    protected String usageLocationType;
    protected String usageLocationTypeExplanation;
    protected UsageLocations usageLocations;

    /**
     * Gets the value of the model property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the value of the model property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setModel(String value) {
        this.model = value;
    }

    /**
     * Gets the value of the module property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getModule() {
        return module;
    }

    /**
     * Sets the value of the module property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setModule(String value) {
        this.module = value;
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
     * Gets the value of the versionFrom property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getVersionFrom() {
        return versionFrom;
    }

    /**
     * Sets the value of the versionFrom property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setVersionFrom(Integer value) {
        this.versionFrom = value;
    }

    /**
     * Gets the value of the versionTo property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getVersionTo() {
        return versionTo;
    }

    /**
     * Sets the value of the versionTo property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setVersionTo(Integer value) {
        this.versionTo = value;
    }

    /**
     * Gets the value of the usageLocationType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getUsageLocationType() {
        return usageLocationType;
    }

    /**
     * Sets the value of the usageLocationType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUsageLocationType(String value) {
        this.usageLocationType = value;
    }

    /**
     * Gets the value of the usageLocationTypeExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getUsageLocationTypeExplanation() {
        return usageLocationTypeExplanation;
    }

    /**
     * Sets the value of the usageLocationTypeExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUsageLocationTypeExplanation(String value) {
        this.usageLocationTypeExplanation = value;
    }

    /**
     * Gets the value of the usageLocations property.
     *
     * @return possible object is
     * {@link UsageLocations }
     */
    public UsageLocations getUsageLocations() {
        return usageLocations;
    }

    /**
     * Sets the value of the usageLocations property.
     *
     * @param value allowed object is
     *              {@link UsageLocations }
     */
    public void setUsageLocations(UsageLocations value) {
        this.usageLocations = value;
    }

}
