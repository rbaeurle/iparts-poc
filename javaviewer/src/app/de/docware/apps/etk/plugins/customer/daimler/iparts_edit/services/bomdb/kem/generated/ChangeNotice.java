package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for changeNotice complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="changeNotice">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="system" type="{http://bomDbServices.eng.dai/}bomDBSystem" minOccurs="0"/>
 *         &lt;element name="item" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="eco" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="orderSupplementData" type="{http://bomDbServices.eng.dai/}orderSupplementData" minOccurs="0"/>
 *         &lt;element name="plantSupplyData" type="{http://bomDbServices.eng.dai/}plantSupplyData" minOccurs="0"/>
 *         &lt;element name="changeNoticeTexts" type="{http://bomDbServices.eng.dai/}changeNoticeTexts" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "changeNotice", propOrder = {
        "system",
        "item",
        "eco",
        "ecoVersion",
        "orderSupplementData",
        "plantSupplyData",
        "changeNoticeTexts"
})
public class ChangeNotice {

    protected BomDBSystem system;
    protected String item;
    protected String eco;
    protected Integer ecoVersion;
    protected OrderSupplementData orderSupplementData;
    protected PlantSupplyData plantSupplyData;
    protected ChangeNoticeTexts changeNoticeTexts;

    /**
     * Gets the value of the system property.
     *
     * @return possible object is
     * {@link BomDBSystem }
     */
    public BomDBSystem getSystem() {
        return system;
    }

    /**
     * Sets the value of the system property.
     *
     * @param value allowed object is
     *              {@link BomDBSystem }
     */
    public void setSystem(BomDBSystem value) {
        this.system = value;
    }

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
     * Gets the value of the plantSupplyData property.
     *
     * @return possible object is
     * {@link PlantSupplyData }
     */
    public PlantSupplyData getPlantSupplyData() {
        return plantSupplyData;
    }

    /**
     * Sets the value of the plantSupplyData property.
     *
     * @param value allowed object is
     *              {@link PlantSupplyData }
     */
    public void setPlantSupplyData(PlantSupplyData value) {
        this.plantSupplyData = value;
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

}
