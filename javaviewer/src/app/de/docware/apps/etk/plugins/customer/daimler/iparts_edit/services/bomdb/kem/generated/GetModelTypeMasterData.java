package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getModelTypeMasterData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getModelTypeMasterData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="input" type="{http://bomDbServices.eng.dai/}getModelTypeMasterDataInput" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getModelTypeMasterData", propOrder = {
        "input"
})
public class GetModelTypeMasterData {

    protected GetModelTypeMasterDataInput input;

    /**
     * Gets the value of the input property.
     *
     * @return possible object is
     * {@link GetModelTypeMasterDataInput }
     */
    public GetModelTypeMasterDataInput getInput() {
        return input;
    }

    /**
     * Sets the value of the input property.
     *
     * @param value allowed object is
     *              {@link GetModelTypeMasterDataInput }
     */
    public void setInput(GetModelTypeMasterDataInput value) {
        this.input = value;
    }

}
