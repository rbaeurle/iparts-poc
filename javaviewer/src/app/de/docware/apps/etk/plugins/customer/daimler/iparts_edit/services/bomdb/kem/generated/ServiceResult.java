package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for serviceResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="serviceResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="svcName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="totalCallCounter" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="successCounter" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="errCounter" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="avgResponseTimeInMs" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="avgSuccessResponseTimeInMs" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "serviceResult", propOrder = {
        "svcName",
        "totalCallCounter",
        "successCounter",
        "errCounter",
        "avgResponseTimeInMs",
        "avgSuccessResponseTimeInMs"
})
public class ServiceResult {

    protected String svcName;
    protected long totalCallCounter;
    protected long successCounter;
    protected long errCounter;
    protected long avgResponseTimeInMs;
    protected long avgSuccessResponseTimeInMs;

    /**
     * Gets the value of the svcName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSvcName() {
        return svcName;
    }

    /**
     * Sets the value of the svcName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSvcName(String value) {
        this.svcName = value;
    }

    /**
     * Gets the value of the totalCallCounter property.
     */
    public long getTotalCallCounter() {
        return totalCallCounter;
    }

    /**
     * Sets the value of the totalCallCounter property.
     */
    public void setTotalCallCounter(long value) {
        this.totalCallCounter = value;
    }

    /**
     * Gets the value of the successCounter property.
     */
    public long getSuccessCounter() {
        return successCounter;
    }

    /**
     * Sets the value of the successCounter property.
     */
    public void setSuccessCounter(long value) {
        this.successCounter = value;
    }

    /**
     * Gets the value of the errCounter property.
     */
    public long getErrCounter() {
        return errCounter;
    }

    /**
     * Sets the value of the errCounter property.
     */
    public void setErrCounter(long value) {
        this.errCounter = value;
    }

    /**
     * Gets the value of the avgResponseTimeInMs property.
     */
    public long getAvgResponseTimeInMs() {
        return avgResponseTimeInMs;
    }

    /**
     * Sets the value of the avgResponseTimeInMs property.
     */
    public void setAvgResponseTimeInMs(long value) {
        this.avgResponseTimeInMs = value;
    }

    /**
     * Gets the value of the avgSuccessResponseTimeInMs property.
     */
    public long getAvgSuccessResponseTimeInMs() {
        return avgSuccessResponseTimeInMs;
    }

    /**
     * Sets the value of the avgSuccessResponseTimeInMs property.
     */
    public void setAvgSuccessResponseTimeInMs(long value) {
        this.avgSuccessResponseTimeInMs = value;
    }

}
