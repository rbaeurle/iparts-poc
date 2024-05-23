package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for positioningObjects complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="positioningObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="positioningObject" type="{http://bomDbServices.eng.dai/}positioningObject" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "positioningObjects", propOrder = {
        "positioningObject"
})
public class PositioningObjects {

    @XmlElement(required = true)
    protected List<PositioningObject> positioningObject;

    /**
     * Gets the value of the positioningObject property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the positioningObject property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPositioningObject().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PositioningObject }
     */
    public List<PositioningObject> getPositioningObject() {
        if (positioningObject == null) {
            positioningObject = new ArrayList<PositioningObject>();
        }
        return this.positioningObject;
    }

}
