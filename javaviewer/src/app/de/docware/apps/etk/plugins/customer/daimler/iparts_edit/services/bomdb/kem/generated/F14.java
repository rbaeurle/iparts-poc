package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for f14 complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="f14">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="language" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="basicParameter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sequenceNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parameterDataFlag" type="{http://bomDbServices.eng.dai/}parameterDataFlag" minOccurs="0"/>
 *         &lt;element name="variable" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="releaseDateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="releaseDateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="ecoFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="versionFrom" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="versionTo" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="statusFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusFromExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusToExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digitFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digitSeparator" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="digitTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="format" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="formatExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="formulaOperator" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="formulaFactor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="function" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parameterUnit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="stickerSequenceNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="stickerFormat" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="stickerFormatExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="defaultVariable" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="defaultVariableExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "f14", propOrder = {
        "language",
        "basicParameter",
        "sequenceNumber",
        "parameterDataFlag",
        "variable",
        "releaseDateFrom",
        "releaseDateTo",
        "ecoFrom",
        "ecoTo",
        "versionFrom",
        "versionTo",
        "statusFrom",
        "statusFromExplanation",
        "statusTo",
        "statusToExplanation",
        "digitFrom",
        "digitSeparator",
        "digitTo",
        "format",
        "formatExplanation",
        "value",
        "formulaOperator",
        "formulaFactor",
        "function",
        "parameterUnit",
        "stickerSequenceNumber",
        "stickerFormat",
        "stickerFormatExplanation",
        "defaultVariable",
        "defaultVariableExplanation"
})
public class F14 {

    protected Language language;
    protected String basicParameter;
    protected String sequenceNumber;
    protected ParameterDataFlag parameterDataFlag;
    protected String variable;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDateFrom;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar releaseDateTo;
    protected String ecoFrom;
    protected String ecoTo;
    protected Integer versionFrom;
    protected Integer versionTo;
    protected String statusFrom;
    protected String statusFromExplanation;
    protected String statusTo;
    protected String statusToExplanation;
    protected String digitFrom;
    protected String digitSeparator;
    protected String digitTo;
    protected String format;
    protected String formatExplanation;
    protected String value;
    protected String formulaOperator;
    protected String formulaFactor;
    protected String function;
    protected String parameterUnit;
    protected String stickerSequenceNumber;
    protected String stickerFormat;
    protected String stickerFormatExplanation;
    protected String defaultVariable;
    protected String defaultVariableExplanation;

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
     * Gets the value of the sequenceNumber property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the value of the sequenceNumber property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSequenceNumber(String value) {
        this.sequenceNumber = value;
    }

    /**
     * Gets the value of the parameterDataFlag property.
     *
     * @return possible object is
     * {@link ParameterDataFlag }
     */
    public ParameterDataFlag getParameterDataFlag() {
        return parameterDataFlag;
    }

    /**
     * Sets the value of the parameterDataFlag property.
     *
     * @param value allowed object is
     *              {@link ParameterDataFlag }
     */
    public void setParameterDataFlag(ParameterDataFlag value) {
        this.parameterDataFlag = value;
    }

    /**
     * Gets the value of the variable property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVariable() {
        return variable;
    }

    /**
     * Sets the value of the variable property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVariable(String value) {
        this.variable = value;
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
     * Gets the value of the releaseDateTo property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getReleaseDateTo() {
        return releaseDateTo;
    }

    /**
     * Sets the value of the releaseDateTo property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setReleaseDateTo(XMLGregorianCalendar value) {
        this.releaseDateTo = value;
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
     * Gets the value of the versionFrom property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getVersionFrom() {
        return versionFrom;
    }

    /**
     * Sets the value of the versionFrom property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setVersionFrom(Integer value) {
        this.versionFrom = value;
    }

    /**
     * Gets the value of the versionTo property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getVersionTo() {
        return versionTo;
    }

    /**
     * Sets the value of the versionTo property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setVersionTo(Integer value) {
        this.versionTo = value;
    }

    /**
     * Gets the value of the statusFrom property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStatusFrom() {
        return statusFrom;
    }

    /**
     * Sets the value of the statusFrom property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStatusFrom(String value) {
        this.statusFrom = value;
    }

    /**
     * Gets the value of the statusFromExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStatusFromExplanation() {
        return statusFromExplanation;
    }

    /**
     * Sets the value of the statusFromExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStatusFromExplanation(String value) {
        this.statusFromExplanation = value;
    }

    /**
     * Gets the value of the statusTo property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStatusTo() {
        return statusTo;
    }

    /**
     * Sets the value of the statusTo property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStatusTo(String value) {
        this.statusTo = value;
    }

    /**
     * Gets the value of the statusToExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStatusToExplanation() {
        return statusToExplanation;
    }

    /**
     * Sets the value of the statusToExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStatusToExplanation(String value) {
        this.statusToExplanation = value;
    }

    /**
     * Gets the value of the digitFrom property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDigitFrom() {
        return digitFrom;
    }

    /**
     * Sets the value of the digitFrom property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDigitFrom(String value) {
        this.digitFrom = value;
    }

    /**
     * Gets the value of the digitSeparator property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDigitSeparator() {
        return digitSeparator;
    }

    /**
     * Sets the value of the digitSeparator property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDigitSeparator(String value) {
        this.digitSeparator = value;
    }

    /**
     * Gets the value of the digitTo property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDigitTo() {
        return digitTo;
    }

    /**
     * Sets the value of the digitTo property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDigitTo(String value) {
        this.digitTo = value;
    }

    /**
     * Gets the value of the format property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the value of the format property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFormat(String value) {
        this.format = value;
    }

    /**
     * Gets the value of the formatExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFormatExplanation() {
        return formatExplanation;
    }

    /**
     * Sets the value of the formatExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFormatExplanation(String value) {
        this.formatExplanation = value;
    }

    /**
     * Gets the value of the value property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the formulaOperator property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFormulaOperator() {
        return formulaOperator;
    }

    /**
     * Sets the value of the formulaOperator property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFormulaOperator(String value) {
        this.formulaOperator = value;
    }

    /**
     * Gets the value of the formulaFactor property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFormulaFactor() {
        return formulaFactor;
    }

    /**
     * Sets the value of the formulaFactor property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFormulaFactor(String value) {
        this.formulaFactor = value;
    }

    /**
     * Gets the value of the function property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFunction() {
        return function;
    }

    /**
     * Sets the value of the function property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFunction(String value) {
        this.function = value;
    }

    /**
     * Gets the value of the parameterUnit property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getParameterUnit() {
        return parameterUnit;
    }

    /**
     * Sets the value of the parameterUnit property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setParameterUnit(String value) {
        this.parameterUnit = value;
    }

    /**
     * Gets the value of the stickerSequenceNumber property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStickerSequenceNumber() {
        return stickerSequenceNumber;
    }

    /**
     * Sets the value of the stickerSequenceNumber property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStickerSequenceNumber(String value) {
        this.stickerSequenceNumber = value;
    }

    /**
     * Gets the value of the stickerFormat property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStickerFormat() {
        return stickerFormat;
    }

    /**
     * Sets the value of the stickerFormat property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStickerFormat(String value) {
        this.stickerFormat = value;
    }

    /**
     * Gets the value of the stickerFormatExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStickerFormatExplanation() {
        return stickerFormatExplanation;
    }

    /**
     * Sets the value of the stickerFormatExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStickerFormatExplanation(String value) {
        this.stickerFormatExplanation = value;
    }

    /**
     * Gets the value of the defaultVariable property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDefaultVariable() {
        return defaultVariable;
    }

    /**
     * Sets the value of the defaultVariable property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDefaultVariable(String value) {
        this.defaultVariable = value;
    }

    /**
     * Gets the value of the defaultVariableExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDefaultVariableExplanation() {
        return defaultVariableExplanation;
    }

    /**
     * Sets the value of the defaultVariableExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDefaultVariableExplanation(String value) {
        this.defaultVariableExplanation = value;
    }

}
