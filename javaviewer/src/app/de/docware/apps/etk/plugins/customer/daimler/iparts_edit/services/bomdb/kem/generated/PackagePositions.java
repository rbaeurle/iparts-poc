package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for packagePositions complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="packagePositions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="packagePosition" type="{http://bomDbServices.eng.dai/}packagePosition" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "packagePositions", propOrder = {
        "packagePosition"
})
public class PackagePositions {

    @XmlElement(required = true)
    protected List<PackagePosition> packagePosition;

    /**
     * Gets the value of the packagePosition property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the packagePosition property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPackagePosition().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PackagePosition }
     */
    public List<PackagePosition> getPackagePosition() {
        if (packagePosition == null) {
            packagePosition = new ArrayList<PackagePosition>();
        }
        return this.packagePosition;
    }

}
