package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for scopePositions complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="scopePositions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="scopePosition" type="{http://bomDbServices.eng.dai/}scopePosition" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "scopePositions", propOrder = {
        "scopePosition"
})
public class ScopePositions {

    @XmlElement(required = true)
    protected List<ScopePosition> scopePosition;

    /**
     * Gets the value of the scopePosition property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the scopePosition property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScopePosition().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ScopePosition }
     */
    public List<ScopePosition> getScopePosition() {
        if (scopePosition == null) {
            scopePosition = new ArrayList<ScopePosition>();
        }
        return this.scopePosition;
    }

}
