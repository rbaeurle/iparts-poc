package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for srmDescriptions complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="srmDescriptions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="srmDescription" type="{http://bomDbServices.eng.dai/}srmDescription" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "srmDescriptions", propOrder = {
        "srmDescription"
})
public class SrmDescriptions {

    @XmlElement(nillable = true)
    protected List<SrmDescription> srmDescription;

    /**
     * Gets the value of the srmDescription property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the srmDescription property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSrmDescription().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SrmDescription }
     */
    public List<SrmDescription> getSrmDescription() {
        if (srmDescription == null) {
            srmDescription = new ArrayList<SrmDescription>();
        }
        return this.srmDescription;
    }

}
