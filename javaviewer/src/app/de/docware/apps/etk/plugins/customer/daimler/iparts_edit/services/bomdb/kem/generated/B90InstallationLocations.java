package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for b90InstallationLocations complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b90InstallationLocations">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="b90InstallationLocation" type="{http://bomDbServices.eng.dai/}b90InstallationLocation" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b90InstallationLocations", propOrder = {
        "b90InstallationLocation"
})
public class B90InstallationLocations {

    protected List<B90InstallationLocation> b90InstallationLocation;

    /**
     * Gets the value of the b90InstallationLocation property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the b90InstallationLocation property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getB90InstallationLocation().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link B90InstallationLocation }
     */
    public List<B90InstallationLocation> getB90InstallationLocation() {
        if (b90InstallationLocation == null) {
            b90InstallationLocation = new ArrayList<B90InstallationLocation>();
        }
        return this.b90InstallationLocation;
    }

}
