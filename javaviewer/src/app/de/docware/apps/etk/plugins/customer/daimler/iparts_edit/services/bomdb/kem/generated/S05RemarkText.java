package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for s05RemarkText complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05RemarkText">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="language" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="remarkText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05RemarkText", propOrder = {
        "language",
        "remarkText"
})
public class S05RemarkText {

    protected Language language;
    protected String remarkText;

    /**
     * Gets the value of the language property.
     *
     * @return possible object is
     * {@link Language }
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     *
     * @param value allowed object is
     *              {@link Language }
     */
    public void setLanguage(Language value) {
        this.language = value;
    }

    /**
     * Gets the value of the remarkText property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRemarkText() {
        return remarkText;
    }

    /**
     * Sets the value of the remarkText property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRemarkText(String value) {
        this.remarkText = value;
    }

}
