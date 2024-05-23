package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for wdPrognoseResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="wdPrognoseResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="item" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="eco" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="orderSupplementData" type="{http://bomDbServices.eng.dai/}orderSupplementData" minOccurs="0"/>
 *         &lt;element name="partMasterData" type="{http://bomDbServices.eng.dai/}partMasterData" minOccurs="0"/>
 *         &lt;element name="s05s" type="{http://bomDbServices.eng.dai/}s05S" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wdPrognoseResult", propOrder = {
        "item",
        "eco",
        "ecoVersion",
        "orderSupplementData",
        "partMasterData",
        "s05S"
})
public class WdPrognoseResult {

    protected String item;
    protected String eco;
    protected Integer ecoVersion;
    protected OrderSupplementData orderSupplementData;
    protected PartMasterData partMasterData;
    @XmlElement(name = "s05s")
    protected S05S s05S;

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
     * Gets the value of the orderSupplementData property.
     *
     * @return possible object is
     * {@link OrderSupplementData }
     */
    public OrderSupplementData getOrderSupplementData() {
        return orderSupplementData;
    }

    /**
     * Sets the value of the orderSupplementData property.
     *
     * @param value allowed object is
     *              {@link OrderSupplementData }
     */
    public void setOrderSupplementData(OrderSupplementData value) {
        this.orderSupplementData = value;
    }

    /**
     * Gets the value of the partMasterData property.
     *
     * @return possible object is
     * {@link PartMasterData }
     */
    public PartMasterData getPartMasterData() {
        return partMasterData;
    }

    /**
     * Sets the value of the partMasterData property.
     *
     * @param value allowed object is
     *              {@link PartMasterData }
     */
    public void setPartMasterData(PartMasterData value) {
        this.partMasterData = value;
    }

    /**
     * Gets the value of the s05S property.
     *
     * @return possible object is
     * {@link S05S }
     */
    public S05S getS05S() {
        return s05S;
    }

    /**
     * Sets the value of the s05S property.
     *
     * @param value allowed object is
     *              {@link S05S }
     */
    public void setS05S(S05S value) {
        this.s05S = value;
    }

}
