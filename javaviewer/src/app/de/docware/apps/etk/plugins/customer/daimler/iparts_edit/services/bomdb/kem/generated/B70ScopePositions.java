package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for b70ScopePositions complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b70ScopePositions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="b70ScopePosition" type="{http://bomDbServices.eng.dai/}b70ScopePosition" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b70ScopePositions", propOrder = {
        "b70ScopePosition"
})
public class B70ScopePositions {

    protected List<B70ScopePosition> b70ScopePosition;

    /**
     * Gets the value of the b70ScopePosition property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the b70ScopePosition property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getB70ScopePosition().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link B70ScopePosition }
     */
    public List<B70ScopePosition> getB70ScopePosition() {
        if (b70ScopePosition == null) {
            b70ScopePosition = new ArrayList<B70ScopePosition>();
        }
        return this.b70ScopePosition;
    }

}
