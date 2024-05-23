package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for changeLog complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="changeLog">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dateType" type="{http://bomDbServices.eng.dai/}dateType" minOccurs="0"/>
 *         &lt;element name="timeStampFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="timeStampTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dateInitialLoad" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="dueDateInitialLoad" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "changeLog", propOrder = {
        "dateType",
        "timeStampFrom",
        "timeStampTo",
        "dateInitialLoad",
        "dueDateInitialLoad"
})
public class ChangeLog {

    protected DateType dateType;
    protected String timeStampFrom;
    protected String timeStampTo;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateInitialLoad;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dueDateInitialLoad;

    /**
     * Gets the value of the dateType property.
     *
     * @return possible object is
     * {@link DateType }
     */
    public DateType getDateType() {
        return dateType;
    }

    /**
     * Sets the value of the dateType property.
     *
     * @param value allowed object is
     *              {@link DateType }
     */
    public void setDateType(DateType value) {
        this.dateType = value;
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
     * Gets the value of the dateInitialLoad property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getDateInitialLoad() {
        return dateInitialLoad;
    }

    /**
     * Sets the value of the dateInitialLoad property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setDateInitialLoad(XMLGregorianCalendar value) {
        this.dateInitialLoad = value;
    }

    /**
     * Gets the value of the dueDateInitialLoad property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getDueDateInitialLoad() {
        return dueDateInitialLoad;
    }

    /**
     * Sets the value of the dueDateInitialLoad property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setDueDateInitialLoad(XMLGregorianCalendar value) {
        this.dueDateInitialLoad = value;
    }

}
