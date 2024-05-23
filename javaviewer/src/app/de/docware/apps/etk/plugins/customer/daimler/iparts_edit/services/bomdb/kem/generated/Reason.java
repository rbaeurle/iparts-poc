package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for reason complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="reason">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="reasonRow1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reasonRow2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reasonRow3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reasonRow4" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "reason", propOrder = {
        "reasonRow1",
        "reasonRow2",
        "reasonRow3",
        "reasonRow4"
})
public class Reason {

    protected String reasonRow1;
    protected String reasonRow2;
    protected String reasonRow3;
    protected String reasonRow4;

    /**
     * Gets the value of the reasonRow1 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getReasonRow1() {
        return reasonRow1;
    }

    /**
     * Sets the value of the reasonRow1 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReasonRow1(String value) {
        this.reasonRow1 = value;
    }

    /**
     * Gets the value of the reasonRow2 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getReasonRow2() {
        return reasonRow2;
    }

    /**
     * Sets the value of the reasonRow2 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReasonRow2(String value) {
        this.reasonRow2 = value;
    }

    /**
     * Gets the value of the reasonRow3 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getReasonRow3() {
        return reasonRow3;
    }

    /**
     * Sets the value of the reasonRow3 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReasonRow3(String value) {
        this.reasonRow3 = value;
    }

    /**
     * Gets the value of the reasonRow4 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getReasonRow4() {
        return reasonRow4;
    }

    /**
     * Sets the value of the reasonRow4 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReasonRow4(String value) {
        this.reasonRow4 = value;
    }

}
