package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for approvedModelType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="approvedModelType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modelType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="approvalFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "approvedModelType", propOrder = {
        "modelType",
        "approvalFlag"
})
public class ApprovedModelType {

    protected String modelType;
    protected String approvalFlag;

    /**
     * Gets the value of the modelType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getModelType() {
        return modelType;
    }

    /**
     * Sets the value of the modelType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setModelType(String value) {
        this.modelType = value;
    }

    /**
     * Gets the value of the approvalFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getApprovalFlag() {
        return approvalFlag;
    }

    /**
     * Sets the value of the approvalFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setApprovalFlag(String value) {
        this.approvalFlag = value;
    }

}
