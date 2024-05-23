package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for b90ScopePositions complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b90ScopePositions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="b90ScopePosition" type="{http://bomDbServices.eng.dai/}b90ScopePosition" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b90ScopePositions", propOrder = {
        "b90ScopePosition"
})
public class B90ScopePositions {

    protected List<B90ScopePosition> b90ScopePosition;

    /**
     * Gets the value of the b90ScopePosition property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the b90ScopePosition property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getB90ScopePosition().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link B90ScopePosition }
     */
    public List<B90ScopePosition> getB90ScopePosition() {
        if (b90ScopePosition == null) {
            b90ScopePosition = new ArrayList<B90ScopePosition>();
        }
        return this.b90ScopePosition;
    }

}
