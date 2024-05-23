package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for monitoringResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="monitoringResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="environment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="startTs" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="elapsedTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="totalCalls" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="totalErrors" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="result" type="{http://bomDbServices.eng.dai/}serviceResult" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "monitoringResult", propOrder = {
        "environment",
        "version",
        "startTs",
        "elapsedTime",
        "totalCalls",
        "totalErrors",
        "result"
})
public class MonitoringResult {

    protected String environment;
    protected String version;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startTs;
    protected String elapsedTime;
    protected long totalCalls;
    protected long totalErrors;
    @XmlElement(nillable = true)
    protected List<ServiceResult> result;

    /**
     * Gets the value of the environment property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Sets the value of the environment property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEnvironment(String value) {
        this.environment = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the startTs property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getStartTs() {
        return startTs;
    }

    /**
     * Sets the value of the startTs property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setStartTs(XMLGregorianCalendar value) {
        this.startTs = value;
    }

    /**
     * Gets the value of the elapsedTime property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Sets the value of the elapsedTime property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setElapsedTime(String value) {
        this.elapsedTime = value;
    }

    /**
     * Gets the value of the totalCalls property.
     */
    public long getTotalCalls() {
        return totalCalls;
    }

    /**
     * Sets the value of the totalCalls property.
     */
    public void setTotalCalls(long value) {
        this.totalCalls = value;
    }

    /**
     * Gets the value of the totalErrors property.
     */
    public long getTotalErrors() {
        return totalErrors;
    }

    /**
     * Sets the value of the totalErrors property.
     */
    public void setTotalErrors(long value) {
        this.totalErrors = value;
    }

    /**
     * Gets the value of the result property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the result property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResult().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceResult }
     */
    public List<ServiceResult> getResult() {
        if (result == null) {
            result = new ArrayList<ServiceResult>();
        }
        return this.result;
    }

}
