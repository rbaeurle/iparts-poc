package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for drawingCharacteristic complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="drawingCharacteristic">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sheetNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="drawingFormat" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="microficheBoxNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="storageInformation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="retrievalRemark" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="retrievalDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="retrievalDepartment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="retrievedBy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="storageDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userIndex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "drawingCharacteristic", propOrder = {
        "sheetNumber",
        "drawingFormat",
        "microficheBoxNumber",
        "storageInformation",
        "retrievalRemark",
        "retrievalDate",
        "retrievalDepartment",
        "retrievedBy",
        "storageDate",
        "userIndex"
})
public class DrawingCharacteristic {

    protected String sheetNumber;
    protected String drawingFormat;
    protected String microficheBoxNumber;
    protected String storageInformation;
    protected String retrievalRemark;
    protected String retrievalDate;
    protected String retrievalDepartment;
    protected String retrievedBy;
    protected String storageDate;
    protected String userIndex;

    /**
     * Gets the value of the sheetNumber property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSheetNumber() {
        return sheetNumber;
    }

    /**
     * Sets the value of the sheetNumber property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSheetNumber(String value) {
        this.sheetNumber = value;
    }

    /**
     * Gets the value of the drawingFormat property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDrawingFormat() {
        return drawingFormat;
    }

    /**
     * Sets the value of the drawingFormat property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDrawingFormat(String value) {
        this.drawingFormat = value;
    }

    /**
     * Gets the value of the microficheBoxNumber property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMicroficheBoxNumber() {
        return microficheBoxNumber;
    }

    /**
     * Sets the value of the microficheBoxNumber property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMicroficheBoxNumber(String value) {
        this.microficheBoxNumber = value;
    }

    /**
     * Gets the value of the storageInformation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStorageInformation() {
        return storageInformation;
    }

    /**
     * Sets the value of the storageInformation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStorageInformation(String value) {
        this.storageInformation = value;
    }

    /**
     * Gets the value of the retrievalRemark property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRetrievalRemark() {
        return retrievalRemark;
    }

    /**
     * Sets the value of the retrievalRemark property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRetrievalRemark(String value) {
        this.retrievalRemark = value;
    }

    /**
     * Gets the value of the retrievalDate property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRetrievalDate() {
        return retrievalDate;
    }

    /**
     * Sets the value of the retrievalDate property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRetrievalDate(String value) {
        this.retrievalDate = value;
    }

    /**
     * Gets the value of the retrievalDepartment property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRetrievalDepartment() {
        return retrievalDepartment;
    }

    /**
     * Sets the value of the retrievalDepartment property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRetrievalDepartment(String value) {
        this.retrievalDepartment = value;
    }

    /**
     * Gets the value of the retrievedBy property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRetrievedBy() {
        return retrievedBy;
    }

    /**
     * Sets the value of the retrievedBy property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRetrievedBy(String value) {
        this.retrievedBy = value;
    }

    /**
     * Gets the value of the storageDate property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStorageDate() {
        return storageDate;
    }

    /**
     * Sets the value of the storageDate property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStorageDate(String value) {
        this.storageDate = value;
    }

    /**
     * Gets the value of the userIndex property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getUserIndex() {
        return userIndex;
    }

    /**
     * Sets the value of the userIndex property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUserIndex(String value) {
        this.userIndex = value;
    }

}
