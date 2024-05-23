package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ecoRegistration complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ecoRegistration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ecoRequester" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoMainEditor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoVerifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoApprover" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoEmissionResponsible" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoHomologationAndRegulationResponsible" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="technicalStandardResponsible" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="productDocumentationEditor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="additionalResponsible1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="additionalResponsible2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ecoRegistration", propOrder = {
        "ecoRequester",
        "ecoMainEditor",
        "ecoVerifier",
        "ecoApprover",
        "ecoEmissionResponsible",
        "ecoHomologationAndRegulationResponsible",
        "technicalStandardResponsible",
        "productDocumentationEditor",
        "additionalResponsible1",
        "additionalResponsible2"
})
public class EcoRegistration {

    protected String ecoRequester;
    protected String ecoMainEditor;
    protected String ecoVerifier;
    protected String ecoApprover;
    protected String ecoEmissionResponsible;
    protected String ecoHomologationAndRegulationResponsible;
    protected String technicalStandardResponsible;
    protected String productDocumentationEditor;
    protected String additionalResponsible1;
    protected String additionalResponsible2;

    /**
     * Gets the value of the ecoRequester property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEcoRequester() {
        return ecoRequester;
    }

    /**
     * Sets the value of the ecoRequester property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEcoRequester(String value) {
        this.ecoRequester = value;
    }

    /**
     * Gets the value of the ecoMainEditor property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEcoMainEditor() {
        return ecoMainEditor;
    }

    /**
     * Sets the value of the ecoMainEditor property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEcoMainEditor(String value) {
        this.ecoMainEditor = value;
    }

    /**
     * Gets the value of the ecoVerifier property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEcoVerifier() {
        return ecoVerifier;
    }

    /**
     * Sets the value of the ecoVerifier property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEcoVerifier(String value) {
        this.ecoVerifier = value;
    }

    /**
     * Gets the value of the ecoApprover property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEcoApprover() {
        return ecoApprover;
    }

    /**
     * Sets the value of the ecoApprover property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEcoApprover(String value) {
        this.ecoApprover = value;
    }

    /**
     * Gets the value of the ecoEmissionResponsible property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEcoEmissionResponsible() {
        return ecoEmissionResponsible;
    }

    /**
     * Sets the value of the ecoEmissionResponsible property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEcoEmissionResponsible(String value) {
        this.ecoEmissionResponsible = value;
    }

    /**
     * Gets the value of the ecoHomologationAndRegulationResponsible property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEcoHomologationAndRegulationResponsible() {
        return ecoHomologationAndRegulationResponsible;
    }

    /**
     * Sets the value of the ecoHomologationAndRegulationResponsible property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEcoHomologationAndRegulationResponsible(String value) {
        this.ecoHomologationAndRegulationResponsible = value;
    }

    /**
     * Gets the value of the technicalStandardResponsible property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTechnicalStandardResponsible() {
        return technicalStandardResponsible;
    }

    /**
     * Sets the value of the technicalStandardResponsible property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTechnicalStandardResponsible(String value) {
        this.technicalStandardResponsible = value;
    }

    /**
     * Gets the value of the productDocumentationEditor property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getProductDocumentationEditor() {
        return productDocumentationEditor;
    }

    /**
     * Sets the value of the productDocumentationEditor property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setProductDocumentationEditor(String value) {
        this.productDocumentationEditor = value;
    }

    /**
     * Gets the value of the additionalResponsible1 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAdditionalResponsible1() {
        return additionalResponsible1;
    }

    /**
     * Sets the value of the additionalResponsible1 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAdditionalResponsible1(String value) {
        this.additionalResponsible1 = value;
    }

    /**
     * Gets the value of the additionalResponsible2 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAdditionalResponsible2() {
        return additionalResponsible2;
    }

    /**
     * Sets the value of the additionalResponsible2 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAdditionalResponsible2(String value) {
        this.additionalResponsible2 = value;
    }

}
