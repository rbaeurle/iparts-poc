package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for b80ScopePositions complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b80ScopePositions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="b80ScopePosition" type="{http://bomDbServices.eng.dai/}b80ScopePosition" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b80ScopePositions", propOrder = {
        "b80ScopePosition"
})
public class B80ScopePositions {

    protected List<B80ScopePosition> b80ScopePosition;

    /**
     * Gets the value of the b80ScopePosition property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the b80ScopePosition property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getB80ScopePosition().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link B80ScopePosition }
     */
    public List<B80ScopePosition> getB80ScopePosition() {
        if (b80ScopePosition == null) {
            b80ScopePosition = new ArrayList<B80ScopePosition>();
        }
        return this.b80ScopePosition;
    }

}
