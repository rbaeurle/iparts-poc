package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getMonitoringOutputResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getMonitoringOutputResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://bomDbServices.eng.dai/}monitoringResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getMonitoringOutputResponse", propOrder = {
        "_return"
})
public class GetMonitoringOutputResponse {

    @XmlElement(name = "return")
    protected MonitoringResult _return;

    /**
     * Gets the value of the return property.
     *
     * @return possible object is
     * {@link MonitoringResult }
     */
    public MonitoringResult getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     *
     * @param value allowed object is
     *              {@link MonitoringResult }
     */
    public void setReturn(MonitoringResult value) {
        this._return = value;
    }

}
