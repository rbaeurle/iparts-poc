package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for b70ModuleStructure complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b70ModuleStructure">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="model" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="module" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="b70ScopePositions" type="{http://bomDbServices.eng.dai/}b70ScopePositions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b70ModuleStructure", propOrder = {
        "model",
        "module",
        "b70ScopePositions"
})
public class B70ModuleStructure {

    protected String model;
    protected String module;
    protected B70ScopePositions b70ScopePositions;

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
     * Gets the value of the b70ScopePositions property.
     *
     * @return possible object is
     * {@link B70ScopePositions }
     */
    public B70ScopePositions getB70ScopePositions() {
        return b70ScopePositions;
    }

    /**
     * Sets the value of the b70ScopePositions property.
     *
     * @param value allowed object is
     *              {@link B70ScopePositions }
     */
    public void setB70ScopePositions(B70ScopePositions value) {
        this.b70ScopePositions = value;
    }

}
