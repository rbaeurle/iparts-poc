package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for s05AltTogetherWiths complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05AltTogetherWiths">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="s05AlternativeTogetherWith" type="{http://bomDbServices.eng.dai/}s05AltTogetherWith" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05AltTogetherWiths", propOrder = {
        "s05AlternativeTogetherWith"
})
public class S05AltTogetherWiths {

    protected List<S05AltTogetherWith> s05AlternativeTogetherWith;

    /**
     * Gets the value of the s05AlternativeTogetherWith property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the s05AlternativeTogetherWith property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getS05AlternativeTogetherWith().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link S05AltTogetherWith }
     */
    public List<S05AltTogetherWith> getS05AlternativeTogetherWith() {
        if (s05AlternativeTogetherWith == null) {
            s05AlternativeTogetherWith = new ArrayList<S05AltTogetherWith>();
        }
        return this.s05AlternativeTogetherWith;
    }

}
