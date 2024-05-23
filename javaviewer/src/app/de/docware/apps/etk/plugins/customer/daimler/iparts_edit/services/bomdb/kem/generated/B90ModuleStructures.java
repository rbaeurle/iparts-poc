package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for b90ModuleStructures complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b90ModuleStructures">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="b90ModuleStructure" type="{http://bomDbServices.eng.dai/}b90ModuleStructure" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b90ModuleStructures", propOrder = {
        "b90ModuleStructure"
})
public class B90ModuleStructures {

    protected List<B90ModuleStructure> b90ModuleStructure;

    /**
     * Gets the value of the b90ModuleStructure property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the b90ModuleStructure property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getB90ModuleStructure().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link B90ModuleStructure }
     */
    public List<B90ModuleStructure> getB90ModuleStructure() {
        if (b90ModuleStructure == null) {
            b90ModuleStructure = new ArrayList<B90ModuleStructure>();
        }
        return this.b90ModuleStructure;
    }

}
