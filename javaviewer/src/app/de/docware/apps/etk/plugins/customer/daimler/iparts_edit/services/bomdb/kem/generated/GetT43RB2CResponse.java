package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RB2CResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RB2CResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="result" type="{http://bomDbServices.eng.dai/}getT43RB2CResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RB2CResponse", propOrder = {
        "result"
})
public class GetT43RB2CResponse {

    protected GetT43RB2CResult result;

    /**
     * Gets the value of the result property.
     *
     * @return possible object is
     * {@link GetT43RB2CResult }
     */
    public GetT43RB2CResult getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     *
     * @param value allowed object is
     *              {@link GetT43RB2CResult }
     */
    public void setResult(GetT43RB2CResult value) {
        this.result = value;
    }

}
