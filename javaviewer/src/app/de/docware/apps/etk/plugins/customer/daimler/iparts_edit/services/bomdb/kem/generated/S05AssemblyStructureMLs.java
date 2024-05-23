package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for s05AssemblyStructureMLs complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05AssemblyStructureMLs">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="s05AssemblyStructureML" type="{http://bomDbServices.eng.dai/}s05AssemblyStructureML" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05AssemblyStructureMLs", propOrder = {
        "s05AssemblyStructureML"
})
public class S05AssemblyStructureMLs {

    @XmlElement(nillable = true)
    protected List<S05AssemblyStructureML> s05AssemblyStructureML;

    /**
     * Gets the value of the s05AssemblyStructureML property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the s05AssemblyStructureML property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getS05AssemblyStructureML().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link S05AssemblyStructureML }
     */
    public List<S05AssemblyStructureML> getS05AssemblyStructureML() {
        if (s05AssemblyStructureML == null) {
            s05AssemblyStructureML = new ArrayList<S05AssemblyStructureML>();
        }
        return this.s05AssemblyStructureML;
    }

}
