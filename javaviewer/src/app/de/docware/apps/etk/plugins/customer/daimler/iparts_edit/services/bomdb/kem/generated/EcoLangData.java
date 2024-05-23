package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ecoLangData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ecoLangData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="language" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="eco" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="engineeringScope" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reason" type="{http://bomDbServices.eng.dai/}reason" minOccurs="0"/>
 *         &lt;element name="engineeringChangeOver" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remark" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ecoLangData", propOrder = {
        "language",
        "eco",
        "engineeringScope",
        "reason",
        "engineeringChangeOver",
        "remark"
})
public class EcoLangData {

    protected Language language;
    protected String eco;
    protected String engineeringScope;
    protected Reason reason;
    protected String engineeringChangeOver;
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
     * Gets the value of the engineeringScope property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEngineeringScope() {
        return engineeringScope;
    }

    /**
     * Sets the value of the engineeringScope property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEngineeringScope(String value) {
        this.engineeringScope = value;
    }

    /**
     * Gets the value of the reason property.
     *
     * @return possible object is
     * {@link Reason }
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * Sets the value of the reason property.
     *
     * @param value allowed object is
     *              {@link Reason }
     */
    public void setReason(Reason value) {
        this.reason = value;
    }

    /**
     * Gets the value of the engineeringChangeOver property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEngineeringChangeOver() {
        return engineeringChangeOver;
    }

    /**
     * Sets the value of the engineeringChangeOver property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEngineeringChangeOver(String value) {
        this.engineeringChangeOver = value;
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
