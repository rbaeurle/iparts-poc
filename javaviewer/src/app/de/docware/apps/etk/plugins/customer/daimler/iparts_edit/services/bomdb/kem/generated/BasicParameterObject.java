package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for basicParameterObject complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="basicParameterObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="basicParameter" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="basicParameterVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "basicParameterObject", propOrder = {
        "basicParameter",
        "basicParameterVersion"
})
public class BasicParameterObject {

    @XmlElement(required = true)
    protected String basicParameter;
    protected Integer basicParameterVersion;

    /**
     * Gets the value of the basicParameter property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getBasicParameter() {
        return basicParameter;
    }

    /**
     * Sets the value of the basicParameter property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setBasicParameter(String value) {
        this.basicParameter = value;
    }

    /**
     * Gets the value of the basicParameterVersion property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getBasicParameterVersion() {
        return basicParameterVersion;
    }

    /**
     * Sets the value of the basicParameterVersion property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setBasicParameterVersion(Integer value) {
        this.basicParameterVersion = value;
    }

}
