package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for s05Alternatives complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05Alternatives">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="S05Alternative" type="{http://bomDbServices.eng.dai/}s05Alternative" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05Alternatives", propOrder = {
        "s05Alternative"
})
public class S05Alternatives {

    @XmlElement(name = "S05Alternative")
    protected List<S05Alternative> s05Alternative;

    /**
     * Gets the value of the s05Alternative property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the s05Alternative property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getS05Alternative().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link S05Alternative }
     */
    public List<S05Alternative> getS05Alternative() {
        if (s05Alternative == null) {
            s05Alternative = new ArrayList<S05Alternative>();
        }
        return this.s05Alternative;
    }

}
