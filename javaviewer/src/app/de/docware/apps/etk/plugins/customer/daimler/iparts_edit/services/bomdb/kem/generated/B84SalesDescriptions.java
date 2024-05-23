package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for b84SalesDescriptions complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b84SalesDescriptions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="b84SalesDescription" type="{http://bomDbServices.eng.dai/}b84SalesDescription" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b84SalesDescriptions", propOrder = {
        "b84SalesDescription"
})
public class B84SalesDescriptions {

    protected List<B84SalesDescription> b84SalesDescription;

    /**
     * Gets the value of the b84SalesDescription property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the b84SalesDescription property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getB84SalesDescription().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link B84SalesDescription }
     */
    public List<B84SalesDescription> getB84SalesDescription() {
        if (b84SalesDescription == null) {
            b84SalesDescription = new ArrayList<B84SalesDescription>();
        }
        return this.b84SalesDescription;
    }

}
