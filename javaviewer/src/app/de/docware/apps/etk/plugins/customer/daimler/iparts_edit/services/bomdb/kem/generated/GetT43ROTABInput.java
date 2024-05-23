package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for getT43ROTABInput complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43ROTABInput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="queryResultAttributes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="genericAttribute" type="{http://bomDbServices.eng.dai/}genericAttribute" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="queryLanguage" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="elementExplanations" type="{http://bomDbServices.eng.dai/}elementExplanations" minOccurs="0"/>
 *         &lt;element name="maxNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43ROTABInput", propOrder = {
        "queryName",
        "queryResultAttributes",
        "genericAttribute",
        "queryLanguage",
        "elementExplanations",
        "maxNumber"
})
public class GetT43ROTABInput {

    protected String queryName;
    @XmlElement(nillable = true)
    protected List<String> queryResultAttributes;
    @XmlElement(nillable = true)
    protected List<GenericAttribute> genericAttribute;
    protected Language queryLanguage;
    protected ElementExplanations elementExplanations;
    protected Integer maxNumber;

    /**
     * Gets the value of the queryName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getQueryName() {
        return queryName;
    }

    /**
     * Sets the value of the queryName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setQueryName(String value) {
        this.queryName = value;
    }

    /**
     * Gets the value of the queryResultAttributes property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the queryResultAttributes property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQueryResultAttributes().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getQueryResultAttributes() {
        if (queryResultAttributes == null) {
            queryResultAttributes = new ArrayList<String>();
        }
        return this.queryResultAttributes;
    }

    /**
     * Gets the value of the genericAttribute property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the genericAttribute property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGenericAttribute().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GenericAttribute }
     */
    public List<GenericAttribute> getGenericAttribute() {
        if (genericAttribute == null) {
            genericAttribute = new ArrayList<GenericAttribute>();
        }
        return this.genericAttribute;
    }

    /**
     * Gets the value of the queryLanguage property.
     *
     * @return possible object is
     * {@link Language }
     */
    public Language getQueryLanguage() {
        return queryLanguage;
    }

    /**
     * Sets the value of the queryLanguage property.
     *
     * @param value allowed object is
     *              {@link Language }
     */
    public void setQueryLanguage(Language value) {
        this.queryLanguage = value;
    }

    /**
     * Gets the value of the elementExplanations property.
     *
     * @return possible object is
     * {@link ElementExplanations }
     */
    public ElementExplanations getElementExplanations() {
        return elementExplanations;
    }

    /**
     * Sets the value of the elementExplanations property.
     *
     * @param value allowed object is
     *              {@link ElementExplanations }
     */
    public void setElementExplanations(ElementExplanations value) {
        this.elementExplanations = value;
    }

    /**
     * Gets the value of the maxNumber property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getMaxNumber() {
        return maxNumber;
    }

    /**
     * Sets the value of the maxNumber property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setMaxNumber(Integer value) {
        this.maxNumber = value;
    }

}
