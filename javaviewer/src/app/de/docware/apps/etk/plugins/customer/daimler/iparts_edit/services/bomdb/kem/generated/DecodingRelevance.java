package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for decodingRelevance complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="decodingRelevance">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="eco" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="decodingRelevantFlagRough" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="decodingRelevantFlagPrecise" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="decodingRelevantFlagDicvAdditional" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "decodingRelevance", propOrder = {
        "eco",
        "decodingRelevantFlagRough",
        "decodingRelevantFlagPrecise",
        "decodingRelevantFlagDicvAdditional"
})
public class DecodingRelevance {

    protected String eco;
    protected boolean decodingRelevantFlagRough;
    protected boolean decodingRelevantFlagPrecise;
    protected boolean decodingRelevantFlagDicvAdditional;

    /**
     * Gets the value of the eco property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEco() {
        return eco;
    }

    /**
     * Sets the value of the eco property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEco(String value) {
        this.eco = value;
    }

    /**
     * Gets the value of the decodingRelevantFlagRough property.
     */
    public boolean isDecodingRelevantFlagRough() {
        return decodingRelevantFlagRough;
    }

    /**
     * Sets the value of the decodingRelevantFlagRough property.
     */
    public void setDecodingRelevantFlagRough(boolean value) {
        this.decodingRelevantFlagRough = value;
    }

    /**
     * Gets the value of the decodingRelevantFlagPrecise property.
     */
    public boolean isDecodingRelevantFlagPrecise() {
        return decodingRelevantFlagPrecise;
    }

    /**
     * Sets the value of the decodingRelevantFlagPrecise property.
     */
    public void setDecodingRelevantFlagPrecise(boolean value) {
        this.decodingRelevantFlagPrecise = value;
    }

    /**
     * Gets the value of the decodingRelevantFlagDicvAdditional property.
     */
    public boolean isDecodingRelevantFlagDicvAdditional() {
        return decodingRelevantFlagDicvAdditional;
    }

    /**
     * Sets the value of the decodingRelevantFlagDicvAdditional property.
     */
    public void setDecodingRelevantFlagDicvAdditional(boolean value) {
        this.decodingRelevantFlagDicvAdditional = value;
    }

}
