package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for generalFeasibilityRestriction complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="generalFeasibilityRestriction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="restrictionScope" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="restrictionPosition" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "generalFeasibilityRestriction", propOrder = {
        "restrictionScope",
        "restrictionPosition"
})
public class GeneralFeasibilityRestriction {

    protected String restrictionScope;
    protected String restrictionPosition;

    /**
     * Gets the value of the restrictionScope property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRestrictionScope() {
        return restrictionScope;
    }

    /**
     * Sets the value of the restrictionScope property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRestrictionScope(String value) {
        this.restrictionScope = value;
    }

    /**
     * Gets the value of the restrictionPosition property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRestrictionPosition() {
        return restrictionPosition;
    }

    /**
     * Sets the value of the restrictionPosition property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRestrictionPosition(String value) {
        this.restrictionPosition = value;
    }

}
