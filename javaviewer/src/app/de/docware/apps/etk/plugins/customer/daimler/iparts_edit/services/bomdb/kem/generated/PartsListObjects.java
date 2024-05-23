package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for partsListObjects complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partsListObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="partsListObject" type="{http://bomDbServices.eng.dai/}partsListObject" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partsListObjects", propOrder = {
        "partsListObject"
})
public class PartsListObjects {

    @XmlElement(required = true)
    protected List<PartsListObject> partsListObject;

    /**
     * Gets the value of the partsListObject property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the partsListObject property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPartsListObject().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PartsListObject }
     */
    public List<PartsListObject> getPartsListObject() {
        if (partsListObject == null) {
            partsListObject = new ArrayList<PartsListObject>();
        }
        return this.partsListObject;
    }

}
