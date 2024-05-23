package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for codeLangData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="codeLangData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="language" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="versionFrom" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="versionTo" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="extendedDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remark" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "codeLangData", propOrder = {
        "language",
        "code",
        "versionFrom",
        "versionTo",
        "description",
        "extendedDescription",
        "remark"
})
public class CodeLangData {

    protected Language language;
    protected String code;
    protected Integer versionFrom;
    protected Integer versionTo;
    protected String description;
    protected String extendedDescription;
    protected String remark;

    /**
     * Gets the value of the language property.
     *
     * @return possible object is
     * {@link Language }
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     *
     * @param value allowed object is
     *              {@link Language }
     */
    public void setLanguage(Language value) {
        this.language = value;
    }

    /**
     * Gets the value of the code property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCode(String value) {
        this.code = value;
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
     * Gets the value of the description property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the extendedDescription property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getExtendedDescription() {
        return extendedDescription;
    }

    /**
     * Sets the value of the extendedDescription property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setExtendedDescription(String value) {
        this.extendedDescription = value;
    }

    /**
     * Gets the value of the remark property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRemark() {
        return remark;
    }

    /**
     * Sets the value of the remark property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRemark(String value) {
        this.remark = value;
    }

}
