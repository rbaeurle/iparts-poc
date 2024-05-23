package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for queryHead complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="queryHead">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="queryTimeStamp" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="recordNumber" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="serviceVersion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "queryHead", propOrder = {
        "queryName",
        "queryTimeStamp",
        "recordNumber",
        "serviceVersion"
})
public class QueryHead {

    protected String queryName;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar queryTimeStamp;
    protected int recordNumber;
    protected String serviceVersion;

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
     * Gets the value of the queryTimeStamp property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getQueryTimeStamp() {
        return queryTimeStamp;
    }

    /**
     * Sets the value of the queryTimeStamp property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setQueryTimeStamp(XMLGregorianCalendar value) {
        this.queryTimeStamp = value;
    }

    /**
     * Gets the value of the recordNumber property.
     */
    public int getRecordNumber() {
        return recordNumber;
    }

    /**
     * Sets the value of the recordNumber property.
     */
    public void setRecordNumber(int value) {
        this.recordNumber = value;
    }

    /**
     * Gets the value of the serviceVersion property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Sets the value of the serviceVersion property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setServiceVersion(String value) {
        this.serviceVersion = value;
    }

}
