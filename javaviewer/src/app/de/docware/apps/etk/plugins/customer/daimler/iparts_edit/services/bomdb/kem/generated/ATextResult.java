package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for aTextResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="aTextResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="item" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="eco" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="changeNoticeTexts" type="{http://bomDbServices.eng.dai/}changeNoticeTexts" minOccurs="0"/>
 *         &lt;element name="aTextResultOld" type="{http://bomDbServices.eng.dai/}aTextResultOld" minOccurs="0"/>
 *         &lt;element name="aTextResultNew" type="{http://bomDbServices.eng.dai/}aTextResultNew" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "aTextResult", propOrder = {
        "item",
        "eco",
        "ecoVersion",
        "changeNoticeTexts",
        "aTextResultOld",
        "aTextResultNew"
})
public class ATextResult {

    protected String item;
    protected String eco;
    protected Integer ecoVersion;
    protected ChangeNoticeTexts changeNoticeTexts;
    protected ATextResultOld aTextResultOld;
    protected ATextResultNew aTextResultNew;

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
     * Gets the value of the changeNoticeTexts property.
     *
     * @return possible object is
     * {@link ChangeNoticeTexts }
     */
    public ChangeNoticeTexts getChangeNoticeTexts() {
        return changeNoticeTexts;
    }

    /**
     * Sets the value of the changeNoticeTexts property.
     *
     * @param value allowed object is
     *              {@link ChangeNoticeTexts }
     */
    public void setChangeNoticeTexts(ChangeNoticeTexts value) {
        this.changeNoticeTexts = value;
    }

    /**
     * Gets the value of the aTextResultOld property.
     *
     * @return possible object is
     * {@link ATextResultOld }
     */
    public ATextResultOld getATextResultOld() {
        return aTextResultOld;
    }

    /**
     * Sets the value of the aTextResultOld property.
     *
     * @param value allowed object is
     *              {@link ATextResultOld }
     */
    public void setATextResultOld(ATextResultOld value) {
        this.aTextResultOld = value;
    }

    /**
     * Gets the value of the aTextResultNew property.
     *
     * @return possible object is
     * {@link ATextResultNew }
     */
    public ATextResultNew getATextResultNew() {
        return aTextResultNew;
    }

    /**
     * Sets the value of the aTextResultNew property.
     *
     * @param value allowed object is
     *              {@link ATextResultNew }
     */
    public void setATextResultNew(ATextResultNew value) {
        this.aTextResultNew = value;
    }

}
