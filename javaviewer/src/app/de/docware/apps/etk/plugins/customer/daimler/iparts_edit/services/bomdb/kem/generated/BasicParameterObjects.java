package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for basicParameterObjects complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="basicParameterObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="basicParameterObject" type="{http://bomDbServices.eng.dai/}basicParameterObject" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "basicParameterObjects", propOrder = {
        "basicParameterObject"
})
public class BasicParameterObjects {

    @XmlElement(required = true)
    protected List<BasicParameterObject> basicParameterObject;

    /**
     * Gets the value of the basicParameterObject property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the basicParameterObject property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBasicParameterObject().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BasicParameterObject }
     */
    public List<BasicParameterObject> getBasicParameterObject() {
        if (basicParameterObject == null) {
            basicParameterObject = new ArrayList<BasicParameterObject>();
        }
        return this.basicParameterObject;
    }

}
