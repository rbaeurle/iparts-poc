package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getAehContentAtCorrespondingObjectsResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getAehContentAtCorrespondingObjectsResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="result" type="{http://bomDbServices.eng.dai/}getAehContentAtCorrespondingObjectsResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAehContentAtCorrespondingObjectsResponse", propOrder = {
        "result"
})
public class GetAehContentAtCorrespondingObjectsResponse {

    protected GetAehContentAtCorrespondingObjectsResult result;

    /**
     * Gets the value of the result property.
     *
     * @return possible object is
     * {@link GetAehContentAtCorrespondingObjectsResult }
     */
    public GetAehContentAtCorrespondingObjectsResult getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     *
     * @param value allowed object is
     *              {@link GetAehContentAtCorrespondingObjectsResult }
     */
    public void setResult(GetAehContentAtCorrespondingObjectsResult value) {
        this.result = value;
    }

}
