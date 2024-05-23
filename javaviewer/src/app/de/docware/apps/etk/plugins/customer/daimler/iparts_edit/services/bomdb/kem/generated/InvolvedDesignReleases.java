package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for involvedDesignReleases complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="involvedDesignReleases">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="involvedDesignRelease" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="involvedDesignReleaseExplanation" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "involvedDesignReleases", propOrder = {
        "involvedDesignRelease",
        "involvedDesignReleaseExplanation"
})
public class InvolvedDesignReleases {

    @XmlElement(nillable = true)
    protected List<String> involvedDesignRelease;
    @XmlElement(nillable = true)
    protected List<String> involvedDesignReleaseExplanation;

    /**
     * Gets the value of the involvedDesignRelease property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the involvedDesignRelease property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInvolvedDesignRelease().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getInvolvedDesignRelease() {
        if (involvedDesignRelease == null) {
            involvedDesignRelease = new ArrayList<String>();
        }
        return this.involvedDesignRelease;
    }

    /**
     * Gets the value of the involvedDesignReleaseExplanation property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the involvedDesignReleaseExplanation property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInvolvedDesignReleaseExplanation().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getInvolvedDesignReleaseExplanation() {
        if (involvedDesignReleaseExplanation == null) {
            involvedDesignReleaseExplanation = new ArrayList<String>();
        }
        return this.involvedDesignReleaseExplanation;
    }

}
