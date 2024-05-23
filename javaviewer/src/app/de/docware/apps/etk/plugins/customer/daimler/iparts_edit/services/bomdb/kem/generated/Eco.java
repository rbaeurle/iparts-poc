package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for eco complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="eco">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="eco" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoMasterData" type="{http://bomDbServices.eng.dai/}ecoMasterData" minOccurs="0"/>
 *         &lt;element name="k3s" type="{http://bomDbServices.eng.dai/}k3S" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "eco", propOrder = {
        "eco",
        "ecoMasterData",
        "k3S"
})
public class Eco {

    protected String eco;
    protected EcoMasterData ecoMasterData;
    @XmlElement(name = "k3s")
    protected K3S k3S;

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
     * Gets the value of the ecoMasterData property.
     *
     * @return possible object is
     * {@link EcoMasterData }
     */
    public EcoMasterData getEcoMasterData() {
        return ecoMasterData;
    }

    /**
     * Sets the value of the ecoMasterData property.
     *
     * @param value allowed object is
     *              {@link EcoMasterData }
     */
    public void setEcoMasterData(EcoMasterData value) {
        this.ecoMasterData = value;
    }

    /**
     * Gets the value of the k3S property.
     *
     * @return possible object is
     * {@link K3S }
     */
    public K3S getK3S() {
        return k3S;
    }

    /**
     * Sets the value of the k3S property.
     *
     * @param value allowed object is
     *              {@link K3S }
     */
    public void setK3S(K3S value) {
        this.k3S = value;
    }

}
