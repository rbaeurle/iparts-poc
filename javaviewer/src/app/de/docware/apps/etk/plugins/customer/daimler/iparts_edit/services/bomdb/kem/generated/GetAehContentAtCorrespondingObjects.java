package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getAehContentAtCorrespondingObjects complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getAehContentAtCorrespondingObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="input" type="{http://bomDbServices.eng.dai/}getAehContentAtCorrespondingObjectsInput" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAehContentAtCorrespondingObjects", propOrder = {
        "input"
})
public class GetAehContentAtCorrespondingObjects {

    protected GetAehContentAtCorrespondingObjectsInput input;

    /**
     * Gets the value of the input property.
     *
     * @return possible object is
     * {@link GetAehContentAtCorrespondingObjectsInput }
     */
    public GetAehContentAtCorrespondingObjectsInput getInput() {
        return input;
    }

    /**
     * Sets the value of the input property.
     *
     * @param value allowed object is
     *              {@link GetAehContentAtCorrespondingObjectsInput }
     */
    public void setInput(GetAehContentAtCorrespondingObjectsInput value) {
        this.input = value;
    }

}
