package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for b90ModuleStructure complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b90ModuleStructure">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="model" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="module" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="b90ScopePositions" type="{http://bomDbServices.eng.dai/}b90ScopePositions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b90ModuleStructure", propOrder = {
        "model",
        "module",
        "b90ScopePositions"
})
public class B90ModuleStructure {

    protected String model;
    protected String module;
    protected B90ScopePositions b90ScopePositions;

    /**
     * Gets the value of the model property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the value of the model property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setModel(String value) {
        this.model = value;
    }

    /**
     * Gets the value of the module property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getModule() {
        return module;
    }

    /**
     * Sets the value of the module property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setModule(String value) {
        this.module = value;
    }

    /**
     * Gets the value of the b90ScopePositions property.
     *
     * @return possible object is
     * {@link B90ScopePositions }
     */
    public B90ScopePositions getB90ScopePositions() {
        return b90ScopePositions;
    }

    /**
     * Sets the value of the b90ScopePositions property.
     *
     * @param value allowed object is
     *              {@link B90ScopePositions }
     */
    public void setB90ScopePositions(B90ScopePositions value) {
        this.b90ScopePositions = value;
    }

}
