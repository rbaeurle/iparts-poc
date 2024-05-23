package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getModuleMasterDataCompleteResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getModuleMasterDataCompleteResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="result" type="{http://bomDbServices.eng.dai/}getModuleMasterDataCompleteResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getModuleMasterDataCompleteResponse", propOrder = {
        "result"
})
public class GetModuleMasterDataCompleteResponse {

    protected GetModuleMasterDataCompleteResult result;

    /**
     * Gets the value of the result property.
     *
     * @return possible object is
     * {@link GetModuleMasterDataCompleteResult }
     */
    public GetModuleMasterDataCompleteResult getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     *
     * @param value allowed object is
     *              {@link GetModuleMasterDataCompleteResult }
     */
    public void setResult(GetModuleMasterDataCompleteResult value) {
        this.result = value;
    }

}