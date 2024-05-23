package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for decodingRelevances complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="decodingRelevances">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="decodingRelevance" type="{http://bomDbServices.eng.dai/}decodingRelevance" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "decodingRelevances", propOrder = {
        "decodingRelevance"
})
public class DecodingRelevances {

    @XmlElement(nillable = true)
    protected List<DecodingRelevance> decodingRelevance;

    /**
     * Gets the value of the decodingRelevance property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the decodingRelevance property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDecodingRelevance().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DecodingRelevance }
     */
    public List<DecodingRelevance> getDecodingRelevance() {
        if (decodingRelevance == null) {
            decodingRelevance = new ArrayList<DecodingRelevance>();
        }
        return this.decodingRelevance;
    }

}
