package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for partWeightData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partWeightData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="part" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="weightDateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="weightDateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="dgv" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="weightType" type="{http://bomDbServices.eng.dai/}weightType" minOccurs="0"/>
 *         &lt;element name="weight" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="weightUnit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="system" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partWeightData", propOrder = {
        "part",
        "weightDateFrom",
        "weightDateTo",
        "dgv",
        "weightType",
        "weight",
        "weightUnit",
        "system"
})
public class PartWeightData {

    protected String part;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar weightDateFrom;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar weightDateTo;
    protected String dgv;
    protected WeightType weightType;
    protected Double weight;
    protected String weightUnit;
    protected String system;

    /**
     * Gets the value of the part property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPart() {
        return part;
    }

    /**
     * Sets the value of the part property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPart(String value) {
        this.part = value;
    }

    /**
     * Gets the value of the weightDateFrom property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getWeightDateFrom() {
        return weightDateFrom;
    }

    /**
     * Sets the value of the weightDateFrom property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setWeightDateFrom(XMLGregorianCalendar value) {
        this.weightDateFrom = value;
    }

    /**
     * Gets the value of the weightDateTo property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getWeightDateTo() {
        return weightDateTo;
    }

    /**
     * Sets the value of the weightDateTo property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setWeightDateTo(XMLGregorianCalendar value) {
        this.weightDateTo = value;
    }

    /**
     * Gets the value of the dgv property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDgv() {
        return dgv;
    }

    /**
     * Sets the value of the dgv property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDgv(String value) {
        this.dgv = value;
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
     * Gets the value of the weight property.
     *
     * @return possible object is
     * {@link Double }
     */
    public Double getWeight() {
        return weight;
    }

    /**
     * Sets the value of the weight property.
     *
     * @param value allowed object is
     *              {@link Double }
     */
    public void setWeight(Double value) {
        this.weight = value;
    }

    /**
     * Gets the value of the weightUnit property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getWeightUnit() {
        return weightUnit;
    }

    /**
     * Sets the value of the weightUnit property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setWeightUnit(String value) {
        this.weightUnit = value;
    }

    /**
     * Gets the value of the system property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSystem() {
        return system;
    }

    /**
     * Sets the value of the system property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSystem(String value) {
        this.system = value;
    }

}
