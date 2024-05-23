package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getPartsListMasterDataCompleteResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getPartsListMasterDataCompleteResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="result" type="{http://bomDbServices.eng.dai/}getPartsListMasterDataCompleteResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getPartsListMasterDataCompleteResponse", propOrder = {
        "result"
})
public class GetPartsListMasterDataCompleteResponse {

    protected GetPartsListMasterDataCompleteResult result;

    /**
     * Gets the value of the result property.
     *
     * @return possible object is
     * {@link GetPartsListMasterDataCompleteResult }
     */
    public GetPartsListMasterDataCompleteResult getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     *
     * @param value allowed object is
     *              {@link GetPartsListMasterDataCompleteResult }
     */
    public void setResult(GetPartsListMasterDataCompleteResult value) {
        this.result = value;
    }

}
