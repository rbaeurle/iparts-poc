package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for sparePartIdentifiers complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="sparePartIdentifiers">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sparePartIdentifier" type="{http://bomDbServices.eng.dai/}sparePartIdentifier" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sparePartIdentifiers", propOrder = {
        "sparePartIdentifier"
})
public class SparePartIdentifiers {

    @XmlElement(nillable = true)
    protected List<SparePartIdentifier> sparePartIdentifier;

    /**
     * Gets the value of the sparePartIdentifier property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sparePartIdentifier property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSparePartIdentifier().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SparePartIdentifier }
     */
    public List<SparePartIdentifier> getSparePartIdentifier() {
        if (sparePartIdentifier == null) {
            sparePartIdentifier = new ArrayList<SparePartIdentifier>();
        }
        return this.sparePartIdentifier;
    }

}
