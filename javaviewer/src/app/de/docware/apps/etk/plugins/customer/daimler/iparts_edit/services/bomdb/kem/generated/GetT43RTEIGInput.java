package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for getT43RTEIGInput complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RTEIGInput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="queryResultAttributes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="genericAttribute" type="{http://bomDbServices.eng.dai/}genericAttribute" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="itemsWithDgvs" type="{http://bomDbServices.eng.dai/}itemsWithDgvs" minOccurs="0"/>
 *         &lt;element name="weightType" type="{http://bomDbServices.eng.dai/}weightType" minOccurs="0"/>
 *         &lt;element name="weightDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="timeStampFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="timeStampTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="maxNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RTEIGInput", propOrder = {
        "queryName",
        "queryResultAttributes",
        "genericAttribute",
        "itemsWithDgvs",
        "weightType",
        "weightDate",
        "timeStampFrom",
        "timeStampTo",
        "maxNumber"
})
public class GetT43RTEIGInput {

    protected String queryName;
    @XmlElement(nillable = true)
    protected List<String> queryResultAttributes;
    @XmlElement(nillable = true)
    protected List<GenericAttribute> genericAttribute;
    protected ItemsWithDgvs itemsWithDgvs;
    protected WeightType weightType;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar weightDate;
    protected String timeStampFrom;
    protected String timeStampTo;
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
     * Gets the value of the itemsWithDgvs property.
     *
     * @return possible object is
     * {@link ItemsWithDgvs }
     */
    public ItemsWithDgvs getItemsWithDgvs() {
        return itemsWithDgvs;
    }

    /**
     * Sets the value of the itemsWithDgvs property.
     *
     * @param value allowed object is
     *              {@link ItemsWithDgvs }
     */
    public void setItemsWithDgvs(ItemsWithDgvs value) {
        this.itemsWithDgvs = value;
    }

    /**
     * Gets the value of the weightType property.
     *
     * @return possible object is
     * {@link WeightType }
     */
    public WeightType getWeightType() {
        return weightType;
    }

    /**
     * Sets the value of the weightType property.
     *
     * @param value allowed object is
     *              {@link WeightType }
     */
    public void setWeightType(WeightType value) {
        this.weightType = value;
    }

    /**
     * Gets the value of the weightDate property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getWeightDate() {
        return weightDate;
    }

    /**
     * Sets the value of the weightDate property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setWeightDate(XMLGregorianCalendar value) {
        this.weightDate = value;
    }

    /**
     * Gets the value of the timeStampFrom property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTimeStampFrom() {
        return timeStampFrom;
    }

    /**
     * Sets the value of the timeStampFrom property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTimeStampFrom(String value) {
        this.timeStampFrom = value;
    }

    /**
     * Gets the value of the timeStampTo property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTimeStampTo() {
        return timeStampTo;
    }

    /**
     * Sets the value of the timeStampTo property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTimeStampTo(String value) {
        this.timeStampTo = value;
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
