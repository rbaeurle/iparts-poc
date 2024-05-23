package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getCodeMasterDataCompleteResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getCodeMasterDataCompleteResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="result" type="{http://bomDbServices.eng.dai/}getCodeMasterDataCompleteResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getCodeMasterDataCompleteResponse", propOrder = {
        "result"
})
public class GetCodeMasterDataCompleteResponse {

    protected GetCodeMasterDataCompleteResult result;

    /**
     * Gets the value of the result property.
     *
     * @return possible object is
     * {@link GetCodeMasterDataCompleteResult }
     */
    public GetCodeMasterDataCompleteResult getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     *
     * @param value allowed object is
     *              {@link GetCodeMasterDataCompleteResult }
     */
    public void setResult(GetCodeMasterDataCompleteResult value) {
        this.result = value;
    }

}
