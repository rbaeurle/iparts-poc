package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for aTextResultNew complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="aTextResultNew">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="exchangeability" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="partMasterData" type="{http://bomDbServices.eng.dai/}partMasterData" minOccurs="0"/>
 *         &lt;element name="s05s" type="{http://bomDbServices.eng.dai/}s05S" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "aTextResultNew", propOrder = {
        "exchangeability",
        "partMasterData",
        "s05S"
})
public class ATextResultNew {

    protected String exchangeability;
    protected PartMasterData partMasterData;
    @XmlElement(name = "s05s")
    protected S05S s05S;

    /**
     * Gets the value of the exchangeability property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getExchangeability() {
        return exchangeability;
    }

    /**
     * Sets the value of the exchangeability property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setExchangeability(String value) {
        this.exchangeability = value;
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
