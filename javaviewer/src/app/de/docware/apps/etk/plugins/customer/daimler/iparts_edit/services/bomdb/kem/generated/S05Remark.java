package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for s05Remark complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05Remark">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="s05RemarkDigit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="s05RemarkText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05Remark", propOrder = {
        "s05RemarkDigit",
        "s05RemarkText"
})
public class S05Remark {

    protected String s05RemarkDigit;
    protected String s05RemarkText;

    /**
     * Gets the value of the s05RemarkDigit property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getS05RemarkDigit() {
        return s05RemarkDigit;
    }

    /**
     * Sets the value of the s05RemarkDigit property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setS05RemarkDigit(String value) {
        this.s05RemarkDigit = value;
    }

    /**
     * Gets the value of the s05RemarkText property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getS05RemarkText() {
        return s05RemarkText;
    }

    /**
     * Sets the value of the s05RemarkText property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setS05RemarkText(String value) {
        this.s05RemarkText = value;
    }

}
