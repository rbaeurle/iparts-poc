package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getPartsListMasterDataComplete complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getPartsListMasterDataComplete">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="input" type="{http://bomDbServices.eng.dai/}getPartsListMasterDataCompleteInput" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getPartsListMasterDataComplete", propOrder = {
        "input"
})
public class GetPartsListMasterDataComplete {

    protected GetPartsListMasterDataCompleteInput input;

    /**
     * Gets the value of the input property.
     *
     * @return possible object is
     * {@link GetPartsListMasterDataCompleteInput }
     */
    public GetPartsListMasterDataCompleteInput getInput() {
        return input;
    }

    /**
     * Sets the value of the input property.
     *
     * @param value allowed object is
     *              {@link GetPartsListMasterDataCompleteInput }
     */
    public void setInput(GetPartsListMasterDataCompleteInput value) {
        this.input = value;
    }

}
