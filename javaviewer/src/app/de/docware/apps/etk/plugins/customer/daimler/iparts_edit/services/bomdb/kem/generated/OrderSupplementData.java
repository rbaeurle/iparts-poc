package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for orderSupplementData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="orderSupplementData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="rawMaterial" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rawMaterialExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="finishedPart" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="finishedPartExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mounting" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mountingExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sparePart" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sparePartExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="exchangeability" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tool" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="toolExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="diagnose" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="pieceNumberForecast" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "orderSupplementData", propOrder = {
        "rawMaterial",
        "rawMaterialExplanation",
        "finishedPart",
        "finishedPartExplanation",
        "mounting",
        "mountingExplanation",
        "sparePart",
        "sparePartExplanation",
        "exchangeability",
        "tool",
        "toolExplanation",
        "diagnose",
        "pieceNumberForecast"
})
public class OrderSupplementData {

    protected String rawMaterial;
    protected String rawMaterialExplanation;
    protected String finishedPart;
    protected String finishedPartExplanation;
    protected String mounting;
    protected String mountingExplanation;
    protected String sparePart;
    protected String sparePartExplanation;
    protected String exchangeability;
    protected String tool;
    protected String toolExplanation;
    protected String diagnose;
    protected String pieceNumberForecast;

    /**
     * Gets the value of the rawMaterial property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRawMaterial() {
        return rawMaterial;
    }

    /**
     * Sets the value of the rawMaterial property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRawMaterial(String value) {
        this.rawMaterial = value;
    }

    /**
     * Gets the value of the rawMaterialExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRawMaterialExplanation() {
        return rawMaterialExplanation;
    }

    /**
     * Sets the value of the rawMaterialExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRawMaterialExplanation(String value) {
        this.rawMaterialExplanation = value;
    }

    /**
     * Gets the value of the finishedPart property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFinishedPart() {
        return finishedPart;
    }

    /**
     * Sets the value of the finishedPart property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFinishedPart(String value) {
        this.finishedPart = value;
    }

    /**
     * Gets the value of the finishedPartExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFinishedPartExplanation() {
        return finishedPartExplanation;
    }

    /**
     * Sets the value of the finishedPartExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFinishedPartExplanation(String value) {
        this.finishedPartExplanation = value;
    }

    /**
     * Gets the value of the mounting property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMounting() {
        return mounting;
    }

    /**
     * Sets the value of the mounting property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMounting(String value) {
        this.mounting = value;
    }

    /**
     * Gets the value of the mountingExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMountingExplanation() {
        return mountingExplanation;
    }

    /**
     * Sets the value of the mountingExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMountingExplanation(String value) {
        this.mountingExplanation = value;
    }

    /**
     * Gets the value of the sparePart property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSparePart() {
        return sparePart;
    }

    /**
     * Sets the value of the sparePart property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSparePart(String value) {
        this.sparePart = value;
    }

    /**
     * Gets the value of the sparePartExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSparePartExplanation() {
        return sparePartExplanation;
    }

    /**
     * Sets the value of the sparePartExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSparePartExplanation(String value) {
        this.sparePartExplanation = value;
    }

    /**
     * Gets the value of the exchangeability property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getExchangeability() {
        return exchangeability;
    }

    /**
     * Sets the value of the exchangeability property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setExchangeability(String value) {
        this.exchangeability = value;
    }

    /**
     * Gets the value of the tool property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTool() {
        return tool;
    }

    /**
     * Sets the value of the tool property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTool(String value) {
        this.tool = value;
    }

    /**
     * Gets the value of the toolExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getToolExplanation() {
        return toolExplanation;
    }

    /**
     * Sets the value of the toolExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setToolExplanation(String value) {
        this.toolExplanation = value;
    }

    /**
     * Gets the value of the diagnose property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDiagnose() {
        return diagnose;
    }

    /**
     * Sets the value of the diagnose property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDiagnose(String value) {
        this.diagnose = value;
    }

    /**
     * Gets the value of the pieceNumberForecast property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPieceNumberForecast() {
        return pieceNumberForecast;
    }

    /**
     * Sets the value of the pieceNumberForecast property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPieceNumberForecast(String value) {
        this.pieceNumberForecast = value;
    }

}
