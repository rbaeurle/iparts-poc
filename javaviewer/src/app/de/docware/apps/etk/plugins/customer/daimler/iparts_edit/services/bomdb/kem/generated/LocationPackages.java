package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for locationPackages complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="locationPackages">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="locationPackage" type="{http://bomDbServices.eng.dai/}locationPackage" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "locationPackages", propOrder = {
        "locationPackage"
})
public class LocationPackages {

    @XmlElement(required = true)
    protected List<LocationPackage> locationPackage;

    /**
     * Gets the value of the locationPackage property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the locationPackage property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocationPackage().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LocationPackage }
     */
    public List<LocationPackage> getLocationPackage() {
        if (locationPackage == null) {
            locationPackage = new ArrayList<LocationPackage>();
        }
        return this.locationPackage;
    }

}
