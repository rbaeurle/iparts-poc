package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for b70ModuleStructures complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b70ModuleStructures">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="b70ModuleStructure" type="{http://bomDbServices.eng.dai/}b70ModuleStructure" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b70ModuleStructures", propOrder = {
        "b70ModuleStructure"
})
public class B70ModuleStructures {

    protected List<B70ModuleStructure> b70ModuleStructure;

    /**
     * Gets the value of the b70ModuleStructure property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the b70ModuleStructure property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getB70ModuleStructure().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link B70ModuleStructure }
     */
    public List<B70ModuleStructure> getB70ModuleStructure() {
        if (b70ModuleStructure == null) {
            b70ModuleStructure = new ArrayList<B70ModuleStructure>();
        }
        return this.b70ModuleStructure;
    }

}
