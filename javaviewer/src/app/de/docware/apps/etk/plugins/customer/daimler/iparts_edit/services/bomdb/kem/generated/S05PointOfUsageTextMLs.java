package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for s05PointOfUsageTextMLs complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05PointOfUsageTextMLs">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="s05PointOfUsageTextML" type="{http://bomDbServices.eng.dai/}s05PointOfUsageTextML" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05PointOfUsageTextMLs", propOrder = {
        "s05PointOfUsageTextML"
})
public class S05PointOfUsageTextMLs {

    protected List<S05PointOfUsageTextML> s05PointOfUsageTextML;

    /**
     * Gets the value of the s05PointOfUsageTextML property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the s05PointOfUsageTextML property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getS05PointOfUsageTextML().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link S05PointOfUsageTextML }
     */
    public List<S05PointOfUsageTextML> getS05PointOfUsageTextML() {
        if (s05PointOfUsageTextML == null) {
            s05PointOfUsageTextML = new ArrayList<S05PointOfUsageTextML>();
        }
        return this.s05PointOfUsageTextML;
    }

}
