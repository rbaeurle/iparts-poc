package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for s05PositionTexts complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05PositionTexts">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="s05RemarkTexts" type="{http://bomDbServices.eng.dai/}s05RemarkTexts" minOccurs="0"/>
 *         &lt;element name="s05AlternativeExplanationText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05PositionTexts", propOrder = {
        "s05RemarkTexts",
        "s05AlternativeExplanationText"
})
public class S05PositionTexts {

    protected S05RemarkTexts s05RemarkTexts;
    protected String s05AlternativeExplanationText;

    /**
     * Gets the value of the s05RemarkTexts property.
     *
     * @return possible object is
     * {@link S05RemarkTexts }
     */
    public S05RemarkTexts getS05RemarkTexts() {
        return s05RemarkTexts;
    }

    /**
     * Sets the value of the s05RemarkTexts property.
     *
     * @param value allowed object is
     *              {@link S05RemarkTexts }
     */
    public void setS05RemarkTexts(S05RemarkTexts value) {
        this.s05RemarkTexts = value;
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
