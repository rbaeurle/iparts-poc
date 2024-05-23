package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for s05AltExps complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05AltExps">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="s05AlternativeExplanation" type="{http://bomDbServices.eng.dai/}s05AltExp" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05AltExps", propOrder = {
        "s05AlternativeExplanation"
})
public class S05AltExps {

    @XmlElement(nillable = true)
    protected List<S05AltExp> s05AlternativeExplanation;

    /**
     * Gets the value of the s05AlternativeExplanation property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the s05AlternativeExplanation property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getS05AlternativeExplanation().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link S05AltExp }
     */
    public List<S05AltExp> getS05AlternativeExplanation() {
        if (s05AlternativeExplanation == null) {
            s05AlternativeExplanation = new ArrayList<S05AltExp>();
        }
        return this.s05AlternativeExplanation;
    }

}
