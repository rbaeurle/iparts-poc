package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RCODResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RCODResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="result" type="{http://bomDbServices.eng.dai/}getT43RCODResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RCODResponse", propOrder = {
        "result"
})
public class GetT43RCODResponse {

    protected GetT43RCODResult result;

    /**
     * Gets the value of the result property.
     *
     * @return possible object is
     * {@link GetT43RCODResult }
     */
    public GetT43RCODResult getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     *
     * @param value allowed object is
     *              {@link GetT43RCODResult }
     */
    public void setResult(GetT43RCODResult value) {
        this.result = value;
    }

}
