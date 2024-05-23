package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for contentObject complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="contentObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contentItem" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="module" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="scopePositions" type="{http://bomDbServices.eng.dai/}scopePositions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contentObject", propOrder = {
        "contentItem",
        "module",
        "version",
        "scopePositions"
})
public class ContentObject {

    protected String contentItem;
    protected String module;
    protected Integer version;
    protected ScopePositions scopePositions;

    /**
     * Gets the value of the contentItem property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getContentItem() {
        return contentItem;
    }

    /**
     * Sets the value of the contentItem property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setContentItem(String value) {
        this.contentItem = value;
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
     * Gets the value of the scopePositions property.
     *
     * @return possible object is
     * {@link ScopePositions }
     */
    public ScopePositions getScopePositions() {
        return scopePositions;
    }

    /**
     * Sets the value of the scopePositions property.
     *
     * @param value allowed object is
     *              {@link ScopePositions }
     */
    public void setScopePositions(ScopePositions value) {
        this.scopePositions = value;
    }

}
