package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for bodyTypes complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="bodyTypes">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bodyType" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="bodyTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "bodyTypes", propOrder = {
        "bodyType",
        "bodyTypeExplanation"
})
public class BodyTypes {

    @XmlElement(nillable = true)
    protected List<String> bodyType;
    @XmlElement(nillable = true)
    protected List<String> bodyTypeExplanation;

    /**
     * Gets the value of the bodyType property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bodyType property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBodyType().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getBodyType() {
        if (bodyType == null) {
            bodyType = new ArrayList<String>();
        }
        return this.bodyType;
    }

    /**
     * Gets the value of the bodyTypeExplanation property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bodyTypeExplanation property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBodyTypeExplanation().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getBodyTypeExplanation() {
        if (bodyTypeExplanation == null) {
            bodyTypeExplanation = new ArrayList<String>();
        }
        return this.bodyTypeExplanation;
    }

}
