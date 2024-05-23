package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for packagePositionData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="packagePositionData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="package" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="position" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="packageVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="invalidFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="engineeringDateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="engineeringDateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="manualFlagFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="manualFlagTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="releaseDateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" type="{http://bomDbServices.eng.dai/}lipoType" minOccurs="0"/>
 *         &lt;element name="typeCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="steeringType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="codeRule" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remark" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="positioningData" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "packagePositionData", propOrder = {
        "_package",
        "position",
        "packageVersion",
        "invalidFlag",
        "engineeringDateFrom",
        "engineeringDateTo",
        "manualFlagFrom",
        "manualFlagTo",
        "ecoFrom",
        "ecoTo",
        "releaseDateFrom",
        "description",
        "type",
        "typeCode",
        "steeringType",
        "codeRule",
        "remark",
        "positioningData"
})
public class PackagePositionData {

    @XmlElement(name = "package")
    protected String _package;
    protected Integer position;
    protected Integer packageVersion;
    protected String invalidFlag;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar engineeringDateFrom;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar engineeringDateTo;
    protected String manualFlagFrom;
    protected String manualFlagTo;
    protected String ecoFrom;
    protected String ecoTo;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDateFrom;
    protected String description;
    protected LipoType type;
    protected String typeCode;
    protected String steeringType;
    protected String codeRule;
    protected String remark;
    protected String positioningData;

    /**
     * Gets the value of the package property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPackage() {
        return _package;
    }

    /**
     * Sets the value of the package property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPackage(String value) {
        this._package = value;
    }

    /**
     * Gets the value of the position property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setPosition(Integer value) {
        this.position = value;
    }

    /**
     * Gets the value of the packageVersion property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getPackageVersion() {
        return packageVersion;
    }

    /**
     * Sets the value of the packageVersion property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setPackageVersion(Integer value) {
        this.packageVersion = value;
    }

    /**
     * Gets the value of the invalidFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getInvalidFlag() {
        return invalidFlag;
    }

    /**
     * Sets the value of the invalidFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setInvalidFlag(String value) {
        this.invalidFlag = value;
    }

    /**
     * Gets the value of the engineeringDateFrom property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getEngineeringDateFrom() {
        return engineeringDateFrom;
    }

    /**
     * Sets the value of the engineeringDateFrom property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setEngineeringDateFrom(XMLGregorianCalendar value) {
        this.engineeringDateFrom = value;
    }

    /**
     * Gets the value of the engineeringDateTo property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getEngineeringDateTo() {
        return engineeringDateTo;
    }

    /**
     * Sets the value of the engineeringDateTo property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setEngineeringDateTo(XMLGregorianCalendar value) {
        this.engineeringDateTo = value;
    }

    /**
     * Gets the value of the manualFlagFrom property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getManualFlagFrom() {
        return manualFlagFrom;
    }

    /**
     * Sets the value of the manualFlagFrom property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManualFlagFrom(String value) {
        this.manualFlagFrom = value;
    }

    /**
     * Gets the value of the manualFlagTo property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getManualFlagTo() {
        return manualFlagTo;
    }

    /**
     * Sets the value of the manualFlagTo property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setManualFlagTo(String value) {
        this.manualFlagTo = value;
    }

    /**
     * Gets the value of the ecoFrom property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEcoFrom() {
        return ecoFrom;
    }

    /**
     * Sets the value of the ecoFrom property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEcoFrom(String value) {
        this.ecoFrom = value;
    }

    /**
     * Gets the value of the ecoTo property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEcoTo() {
        return ecoTo;
    }

    /**
     * Sets the value of the ecoTo property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEcoTo(String value) {
        this.ecoTo = value;
    }

    /**
     * Gets the value of the releaseDateFrom property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getReleaseDateFrom() {
        return releaseDateFrom;
    }

    /**
     * Sets the value of the releaseDateFrom property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setReleaseDateFrom(XMLGregorianCalendar value) {
        this.releaseDateFrom = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is
     * {@link LipoType }
     */
    public LipoType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is
     *              {@link LipoType }
     */
    public void setType(LipoType value) {
        this.type = value;
    }

    /**
     * Gets the value of the typeCode property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTypeCode() {
        return typeCode;
    }

    /**
     * Sets the value of the typeCode property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTypeCode(String value) {
        this.typeCode = value;
    }

    /**
     * Gets the value of the steeringType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSteeringType() {
        return steeringType;
    }

    /**
     * Sets the value of the steeringType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSteeringType(String value) {
        this.steeringType = value;
    }

    /**
     * Gets the value of the codeRule property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCodeRule() {
        return codeRule;
    }

    /**
     * Sets the value of the codeRule property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCodeRule(String value) {
        this.codeRule = value;
    }

    /**
     * Gets the value of the remark property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRemark() {
        return remark;
    }

    /**
     * Sets the value of the remark property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRemark(String value) {
        this.remark = value;
    }

    /**
     * Gets the value of the positioningData property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPositioningData() {
        return positioningData;
    }

    /**
     * Sets the value of the positioningData property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPositioningData(String value) {
        this.positioningData = value;
    }

}
