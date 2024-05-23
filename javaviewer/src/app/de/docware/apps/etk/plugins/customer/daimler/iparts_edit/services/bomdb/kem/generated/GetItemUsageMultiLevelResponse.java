package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getItemUsageMultiLevelResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getItemUsageMultiLevelResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="result" type="{http://bomDbServices.eng.dai/}getItemUsageMultiLevelResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getItemUsageMultiLevelResponse", propOrder = {
        "result"
})
public class GetItemUsageMultiLevelResponse {

    protected GetItemUsageMultiLevelResult result;

    /**
     * Gets the value of the result property.
     *
     * @return possible object is
     * {@link GetItemUsageMultiLevelResult }
     */
    public GetItemUsageMultiLevelResult getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     *
     * @param value allowed object is
     *              {@link GetItemUsageMultiLevelResult }
     */
    public void setResult(GetItemUsageMultiLevelResult value) {
        this.result = value;
    }

}
