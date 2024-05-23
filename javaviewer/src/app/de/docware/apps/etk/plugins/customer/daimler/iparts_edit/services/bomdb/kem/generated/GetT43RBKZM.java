package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RBKZM complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RBKZM">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="input" type="{http://bomDbServices.eng.dai/}getT43RBKZMInput" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RBKZM", propOrder = {
        "input"
})
public class GetT43RBKZM {

    protected GetT43RBKZMInput input;

    /**
     * Gets the value of the input property.
     *
     * @return possible object is
     * {@link GetT43RBKZMInput }
     */
    public GetT43RBKZMInput getInput() {
        return input;
    }

    /**
     * Sets the value of the input property.
     *
     * @param value allowed object is
     *              {@link GetT43RBKZMInput }
     */
    public void setInput(GetT43RBKZMInput value) {
        this.input = value;
    }

}
