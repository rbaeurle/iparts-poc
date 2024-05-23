package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for s05PointOfUsageTexts complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05PointOfUsageTexts">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="s05PointOfUsageText" type="{http://bomDbServices.eng.dai/}s05PointOfUsageText" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05PointOfUsageTexts", propOrder = {
        "s05PointOfUsageText"
})
public class S05PointOfUsageTexts {

    protected List<S05PointOfUsageText> s05PointOfUsageText;

    /**
     * Gets the value of the s05PointOfUsageText property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the s05PointOfUsageText property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getS05PointOfUsageText().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link S05PointOfUsageText }
     */
    public List<S05PointOfUsageText> getS05PointOfUsageText() {
        if (s05PointOfUsageText == null) {
            s05PointOfUsageText = new ArrayList<S05PointOfUsageText>();
        }
        return this.s05PointOfUsageText;
    }

}
