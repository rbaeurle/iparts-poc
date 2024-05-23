package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for elementExplanation complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="elementExplanation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="table" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="element" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="attribute1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "elementExplanation", propOrder = {
        "table",
        "element",
        "attribute1"
})
public class ElementExplanation {

    protected String table;
    protected String element;
    protected String attribute1;

    /**
     * Gets the value of the table property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTable() {
        return table;
    }

    /**
     * Sets the value of the table property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTable(String value) {
        this.table = value;
    }

    /**
     * Gets the value of the element property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getElement() {
        return element;
    }

    /**
     * Sets the value of the element property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setElement(String value) {
        this.element = value;
    }

    /**
     * Gets the value of the attribute1 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAttribute1() {
        return attribute1;
    }

    /**
     * Sets the value of the attribute1 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAttribute1(String value) {
        this.attribute1 = value;
    }

}
