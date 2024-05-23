package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for itemScope complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="itemScope">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="item" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="scopeFlag" type="{http://bomDbServices.eng.dai/}scopeFlag" minOccurs="0"/>
 *         &lt;element name="scopeFlagExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="scope" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="versionFrom" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="versionTo" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="scopeMasterDatas" type="{http://bomDbServices.eng.dai/}scopeMasterDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "itemScope", propOrder = {
        "item",
        "scopeFlag",
        "scopeFlagExplanation",
        "scope",
        "versionFrom",
        "versionTo",
        "scopeMasterDatas"
})
public class ItemScope {

    protected String item;
    protected ScopeFlag scopeFlag;
    protected String scopeFlagExplanation;
    protected String scope;
    protected Integer versionFrom;
    protected Integer versionTo;
    protected ScopeMasterDatas scopeMasterDatas;

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
     * Gets the value of the scopeFlag property.
     *
     * @return possible object is
     * {@link ScopeFlag }
     */
    public ScopeFlag getScopeFlag() {
        return scopeFlag;
    }

    /**
     * Sets the value of the scopeFlag property.
     *
     * @param value allowed object is
     *              {@link ScopeFlag }
     */
    public void setScopeFlag(ScopeFlag value) {
        this.scopeFlag = value;
    }

    /**
     * Gets the value of the scopeFlagExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getScopeFlagExplanation() {
        return scopeFlagExplanation;
    }

    /**
     * Sets the value of the scopeFlagExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setScopeFlagExplanation(String value) {
        this.scopeFlagExplanation = value;
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
     * Gets the value of the scopeMasterDatas property.
     *
     * @return possible object is
     * {@link ScopeMasterDatas }
     */
    public ScopeMasterDatas getScopeMasterDatas() {
        return scopeMasterDatas;
    }

    /**
     * Sets the value of the scopeMasterDatas property.
     *
     * @param value allowed object is
     *              {@link ScopeMasterDatas }
     */
    public void setScopeMasterDatas(ScopeMasterDatas value) {
        this.scopeMasterDatas = value;
    }

}
