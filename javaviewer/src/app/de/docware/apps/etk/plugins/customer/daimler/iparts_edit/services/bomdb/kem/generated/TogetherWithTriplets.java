package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for togetherWithTriplets complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="togetherWithTriplets">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="togetherWithTriplet" type="{http://bomDbServices.eng.dai/}togetherWithTriplet" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "togetherWithTriplets", propOrder = {
        "togetherWithTriplet"
})
public class TogetherWithTriplets {

    @XmlElement(required = true)
    protected List<TogetherWithTriplet> togetherWithTriplet;

    /**
     * Gets the value of the togetherWithTriplet property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the togetherWithTriplet property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTogetherWithTriplet().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TogetherWithTriplet }
     */
    public List<TogetherWithTriplet> getTogetherWithTriplet() {
        if (togetherWithTriplet == null) {
            togetherWithTriplet = new ArrayList<TogetherWithTriplet>();
        }
        return this.togetherWithTriplet;
    }

}
