package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for s05AltExp complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05AltExp">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="s05AlternativeExplanationType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="s05AlternativeExplanationText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05AltExp", propOrder = {
        "s05AlternativeExplanationType",
        "s05AlternativeExplanationText"
})
public class S05AltExp {

    protected String s05AlternativeExplanationType;
    protected String s05AlternativeExplanationText;

    /**
     * Gets the value of the s05AlternativeExplanationType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getS05AlternativeExplanationType() {
        return s05AlternativeExplanationType;
    }

    /**
     * Sets the value of the s05AlternativeExplanationType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setS05AlternativeExplanationType(String value) {
        this.s05AlternativeExplanationType = value;
    }

    /**
     * Gets the value of the s05AlternativeExplanationText property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getS05AlternativeExplanationText() {
        return s05AlternativeExplanationText;
    }

    /**
     * Sets the value of the s05AlternativeExplanationText property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setS05AlternativeExplanationText(String value) {
        this.s05AlternativeExplanationText = value;
    }

}
