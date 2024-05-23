package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for generalFeasibilityObjects complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="generalFeasibilityObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="generalFeasibilityObject" type="{http://bomDbServices.eng.dai/}generalFeasibilityObject" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "generalFeasibilityObjects", propOrder = {
        "generalFeasibilityObject"
})
public class GeneralFeasibilityObjects {

    @XmlElement(required = true)
    protected List<GeneralFeasibilityObject> generalFeasibilityObject;

    /**
     * Gets the value of the generalFeasibilityObject property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the generalFeasibilityObject property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeneralFeasibilityObject().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GeneralFeasibilityObject }
     */
    public List<GeneralFeasibilityObject> getGeneralFeasibilityObject() {
        if (generalFeasibilityObject == null) {
            generalFeasibilityObject = new ArrayList<GeneralFeasibilityObject>();
        }
        return this.generalFeasibilityObject;
    }

}
