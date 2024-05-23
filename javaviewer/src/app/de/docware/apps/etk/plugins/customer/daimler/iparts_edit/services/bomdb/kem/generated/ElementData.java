package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for elementData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="elementData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="language" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="table" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="element" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="attribute1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="attribute2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dataset" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="attribute3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "elementData", propOrder = {
        "language",
        "table",
        "element",
        "attribute1",
        "attribute2",
        "description",
        "dataset",
        "attribute3"
})
public class ElementData {

    protected Language language;
    protected String table;
    protected String element;
    protected String attribute1;
    protected String attribute2;
    protected String description;
    protected String dataset;
    protected String attribute3;

    /**
     * Gets the value of the language property.
     *
     * @return possible object is
     * {@link Language }
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     *
     * @param value allowed object is
     *              {@link Language }
     */
    public void setLanguage(Language value) {
        this.language = value;
    }

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

    /**
     * Gets the value of the attribute2 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAttribute2() {
        return attribute2;
    }

    /**
     * Sets the value of the attribute2 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAttribute2(String value) {
        this.attribute2 = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the dataset property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDataset() {
        return dataset;
    }

    /**
     * Sets the value of the dataset property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDataset(String value) {
        this.dataset = value;
    }

    /**
     * Gets the value of the attribute3 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAttribute3() {
        return attribute3;
    }

    /**
     * Sets the value of the attribute3 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAttribute3(String value) {
        this.attribute3 = value;
    }

}
