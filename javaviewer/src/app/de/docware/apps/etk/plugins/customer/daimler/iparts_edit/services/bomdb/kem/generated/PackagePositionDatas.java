package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for packagePositionDatas complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="packagePositionDatas">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="packagePositionData" type="{http://bomDbServices.eng.dai/}packagePositionData" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "packagePositionDatas", propOrder = {
        "packagePositionData"
})
public class PackagePositionDatas {

    @XmlElement(nillable = true)
    protected List<PackagePositionData> packagePositionData;

    /**
     * Gets the value of the packagePositionData property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the packagePositionData property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPackagePositionData().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PackagePositionData }
     */
    public List<PackagePositionData> getPackagePositionData() {
        if (packagePositionData == null) {
            packagePositionData = new ArrayList<PackagePositionData>();
        }
        return this.packagePositionData;
    }

}
