package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for partMasterData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partMasterData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="language" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="part" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
 *         &lt;element name="designChangeCounter" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="flashwareType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="flashwareTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="partsType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="partsTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="assemblyStructureType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="assemblyStructureTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dgv" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="drawingDateOrType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="drawingDateOrTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="referencePart" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="referenceDrawing" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="partReleaseStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="partReleaseStatusExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="quantityUnit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="quantityUnitExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="colorItemType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="colorItemTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cadIdType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cadIdTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cadSystem" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="documentationObligation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="documentationObligationExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="calculatedWeight" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="safetyRelevant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="safetyRelevantExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="certificationRelevant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="certificationRelevantExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="basicMaterial" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="daimlerMaterialDeliveryPropertySpecification" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="materialDeliveryQuality" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="materialSurfaceQuality1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="materialSurfaceQuality2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="auxiliaryMaterialQuality" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="additionalMaterialQualities" type="{http://bomDbServices.eng.dai/}addMatQuals" minOccurs="0"/>
 *         &lt;element name="remark" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="electromagneticCompatibility" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="electromagneticCompatibilityFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="electromagneticCompatibilityFlagExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vehicleDocumentationRelevant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vehicleDocumentationRelevantExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="theftRelevant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="theftRelevantExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecuQualifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="flashwareSequence" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dependencyExisting" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="seriesPreliminaryTest" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="seriesPreliminaryTestExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="drawingCharacteristics" type="{http://bomDbServices.eng.dai/}drawingCharacteristics" minOccurs="0"/>
 *         &lt;element name="repetitionFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="exchangeGroup" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="leadingDesignRelease" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="leadingDesignReleaseExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="involvedDesignReleases" type="{http://bomDbServices.eng.dai/}involvedDesignReleases" minOccurs="0"/>
 *         &lt;element name="plantSupplies" type="{http://bomDbServices.eng.dai/}plantSupplies" minOccurs="0"/>
 *         &lt;element name="labelingCharacteristicsExisting" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="labelingCharacteristicsExistingExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ecoFromMasterData" type="{http://bomDbServices.eng.dai/}ecoMasterData" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partMasterData", propOrder = {
        "language",
        "part",
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
        "designChangeCounter",
        "flashwareType",
        "flashwareTypeExplanation",
        "partsType",
        "partsTypeExplanation",
        "description",
        "assemblyStructureType",
        "assemblyStructureTypeExplanation",
        "dgv",
        "drawingDateOrType",
        "drawingDateOrTypeExplanation",
        "referencePart",
        "referenceDrawing",
        "partReleaseStatus",
        "partReleaseStatusExplanation",
        "quantityUnit",
        "quantityUnitExplanation",
        "colorItemType",
        "colorItemTypeExplanation",
        "cadIdType",
        "cadIdTypeExplanation",
        "cadSystem",
        "documentationObligation",
        "documentationObligationExplanation",
        "calculatedWeight",
        "safetyRelevant",
        "safetyRelevantExplanation",
        "certificationRelevant",
        "certificationRelevantExplanation",
        "basicMaterial",
        "daimlerMaterialDeliveryPropertySpecification",
        "materialDeliveryQuality",
        "materialSurfaceQuality1",
        "materialSurfaceQuality2",
        "auxiliaryMaterialQuality",
        "additionalMaterialQualities",
        "remark",
        "electromagneticCompatibility",
        "electromagneticCompatibilityFlag",
        "electromagneticCompatibilityFlagExplanation",
        "vehicleDocumentationRelevant",
        "vehicleDocumentationRelevantExplanation",
        "theftRelevant",
        "theftRelevantExplanation",
        "ecuQualifier",
        "flashwareSequence",
        "dependencyExisting",
        "seriesPreliminaryTest",
        "seriesPreliminaryTestExplanation",
        "drawingCharacteristics",
        "repetitionFlag",
        "exchangeGroup",
        "leadingDesignRelease",
        "leadingDesignReleaseExplanation",
        "involvedDesignReleases",
        "plantSupplies",
        "labelingCharacteristicsExisting",
        "labelingCharacteristicsExistingExplanation",
        "ecoFromMasterData"
})
public class PartMasterData {

    protected Language language;
    protected String part;
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
    protected Integer designChangeCounter;
    protected String flashwareType;
    protected String flashwareTypeExplanation;
    protected String partsType;
    protected String partsTypeExplanation;
    protected String description;
    protected String assemblyStructureType;
    protected String assemblyStructureTypeExplanation;
    protected String dgv;
    protected String drawingDateOrType;
    protected String drawingDateOrTypeExplanation;
    protected String referencePart;
    protected String referenceDrawing;
    protected String partReleaseStatus;
    protected String partReleaseStatusExplanation;
    protected String quantityUnit;
    protected String quantityUnitExplanation;
    protected String colorItemType;
    protected String colorItemTypeExplanation;
    protected String cadIdType;
    protected String cadIdTypeExplanation;
    protected String cadSystem;
    protected String documentationObligation;
    protected String documentationObligationExplanation;
    protected Double calculatedWeight;
    protected String safetyRelevant;
    protected String safetyRelevantExplanation;
    protected String certificationRelevant;
    protected String certificationRelevantExplanation;
    protected String basicMaterial;
    protected String daimlerMaterialDeliveryPropertySpecification;
    protected String materialDeliveryQuality;
    protected String materialSurfaceQuality1;
    protected String materialSurfaceQuality2;
    protected String auxiliaryMaterialQuality;
    protected AddMatQuals additionalMaterialQualities;
    protected String remark;
    protected String electromagneticCompatibility;
    protected String electromagneticCompatibilityFlag;
    protected String electromagneticCompatibilityFlagExplanation;
    protected String vehicleDocumentationRelevant;
    protected String vehicleDocumentationRelevantExplanation;
    protected String theftRelevant;
    protected String theftRelevantExplanation;
    protected String ecuQualifier;
    protected String flashwareSequence;
    protected String dependencyExisting;
    protected String seriesPreliminaryTest;
    protected String seriesPreliminaryTestExplanation;
    protected DrawingCharacteristics drawingCharacteristics;
    protected String repetitionFlag;
    protected String exchangeGroup;
    protected String leadingDesignRelease;
    protected String leadingDesignReleaseExplanation;
    protected InvolvedDesignReleases involvedDesignReleases;
    protected PlantSupplies plantSupplies;
    protected String labelingCharacteristicsExisting;
    protected String labelingCharacteristicsExistingExplanation;
    protected EcoMasterData ecoFromMasterData;

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
     * Gets the value of the part property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPart() {
        return part;
    }

    /**
     * Sets the value of the part property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPart(String value) {
        this.part = value;
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
     * Gets the value of the designChangeCounter property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getDesignChangeCounter() {
        return designChangeCounter;
    }

    /**
     * Sets the value of the designChangeCounter property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setDesignChangeCounter(Integer value) {
        this.designChangeCounter = value;
    }

    /**
     * Gets the value of the flashwareType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFlashwareType() {
        return flashwareType;
    }

    /**
     * Sets the value of the flashwareType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFlashwareType(String value) {
        this.flashwareType = value;
    }

    /**
     * Gets the value of the flashwareTypeExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFlashwareTypeExplanation() {
        return flashwareTypeExplanation;
    }

    /**
     * Sets the value of the flashwareTypeExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFlashwareTypeExplanation(String value) {
        this.flashwareTypeExplanation = value;
    }

    /**
     * Gets the value of the partsType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPartsType() {
        return partsType;
    }

    /**
     * Sets the value of the partsType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPartsType(String value) {
        this.partsType = value;
    }

    /**
     * Gets the value of the partsTypeExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPartsTypeExplanation() {
        return partsTypeExplanation;
    }

    /**
     * Sets the value of the partsTypeExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPartsTypeExplanation(String value) {
        this.partsTypeExplanation = value;
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
     * Gets the value of the assemblyStructureType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAssemblyStructureType() {
        return assemblyStructureType;
    }

    /**
     * Sets the value of the assemblyStructureType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAssemblyStructureType(String value) {
        this.assemblyStructureType = value;
    }

    /**
     * Gets the value of the assemblyStructureTypeExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAssemblyStructureTypeExplanation() {
        return assemblyStructureTypeExplanation;
    }

    /**
     * Sets the value of the assemblyStructureTypeExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAssemblyStructureTypeExplanation(String value) {
        this.assemblyStructureTypeExplanation = value;
    }

    /**
     * Gets the value of the dgv property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDgv() {
        return dgv;
    }

    /**
     * Sets the value of the dgv property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDgv(String value) {
        this.dgv = value;
    }

    /**
     * Gets the value of the drawingDateOrType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDrawingDateOrType() {
        return drawingDateOrType;
    }

    /**
     * Sets the value of the drawingDateOrType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDrawingDateOrType(String value) {
        this.drawingDateOrType = value;
    }

    /**
     * Gets the value of the drawingDateOrTypeExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDrawingDateOrTypeExplanation() {
        return drawingDateOrTypeExplanation;
    }

    /**
     * Sets the value of the drawingDateOrTypeExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDrawingDateOrTypeExplanation(String value) {
        this.drawingDateOrTypeExplanation = value;
    }

    /**
     * Gets the value of the referencePart property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getReferencePart() {
        return referencePart;
    }

    /**
     * Sets the value of the referencePart property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReferencePart(String value) {
        this.referencePart = value;
    }

    /**
     * Gets the value of the referenceDrawing property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getReferenceDrawing() {
        return referenceDrawing;
    }

    /**
     * Sets the value of the referenceDrawing property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReferenceDrawing(String value) {
        this.referenceDrawing = value;
    }

    /**
     * Gets the value of the partReleaseStatus property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPartReleaseStatus() {
        return partReleaseStatus;
    }

    /**
     * Sets the value of the partReleaseStatus property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPartReleaseStatus(String value) {
        this.partReleaseStatus = value;
    }

    /**
     * Gets the value of the partReleaseStatusExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPartReleaseStatusExplanation() {
        return partReleaseStatusExplanation;
    }

    /**
     * Sets the value of the partReleaseStatusExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPartReleaseStatusExplanation(String value) {
        this.partReleaseStatusExplanation = value;
    }

    /**
     * Gets the value of the quantityUnit property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getQuantityUnit() {
        return quantityUnit;
    }

    /**
     * Sets the value of the quantityUnit property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setQuantityUnit(String value) {
        this.quantityUnit = value;
    }

    /**
     * Gets the value of the quantityUnitExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getQuantityUnitExplanation() {
        return quantityUnitExplanation;
    }

    /**
     * Sets the value of the quantityUnitExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setQuantityUnitExplanation(String value) {
        this.quantityUnitExplanation = value;
    }

    /**
     * Gets the value of the colorItemType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getColorItemType() {
        return colorItemType;
    }

    /**
     * Sets the value of the colorItemType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setColorItemType(String value) {
        this.colorItemType = value;
    }

    /**
     * Gets the value of the colorItemTypeExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getColorItemTypeExplanation() {
        return colorItemTypeExplanation;
    }

    /**
     * Sets the value of the colorItemTypeExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setColorItemTypeExplanation(String value) {
        this.colorItemTypeExplanation = value;
    }

    /**
     * Gets the value of the cadIdType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCadIdType() {
        return cadIdType;
    }

    /**
     * Sets the value of the cadIdType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCadIdType(String value) {
        this.cadIdType = value;
    }

    /**
     * Gets the value of the cadIdTypeExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCadIdTypeExplanation() {
        return cadIdTypeExplanation;
    }

    /**
     * Sets the value of the cadIdTypeExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCadIdTypeExplanation(String value) {
        this.cadIdTypeExplanation = value;
    }

    /**
     * Gets the value of the cadSystem property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCadSystem() {
        return cadSystem;
    }

    /**
     * Sets the value of the cadSystem property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCadSystem(String value) {
        this.cadSystem = value;
    }

    /**
     * Gets the value of the documentationObligation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDocumentationObligation() {
        return documentationObligation;
    }

    /**
     * Sets the value of the documentationObligation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDocumentationObligation(String value) {
        this.documentationObligation = value;
    }

    /**
     * Gets the value of the documentationObligationExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDocumentationObligationExplanation() {
        return documentationObligationExplanation;
    }

    /**
     * Sets the value of the documentationObligationExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDocumentationObligationExplanation(String value) {
        this.documentationObligationExplanation = value;
    }

    /**
     * Gets the value of the calculatedWeight property.
     *
     * @return possible object is
     * {@link Double }
     */
    public Double getCalculatedWeight() {
        return calculatedWeight;
    }

    /**
     * Sets the value of the calculatedWeight property.
     *
     * @param value allowed object is
     *              {@link Double }
     */
    public void setCalculatedWeight(Double value) {
        this.calculatedWeight = value;
    }

    /**
     * Gets the value of the safetyRelevant property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSafetyRelevant() {
        return safetyRelevant;
    }

    /**
     * Sets the value of the safetyRelevant property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSafetyRelevant(String value) {
        this.safetyRelevant = value;
    }

    /**
     * Gets the value of the safetyRelevantExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSafetyRelevantExplanation() {
        return safetyRelevantExplanation;
    }

    /**
     * Sets the value of the safetyRelevantExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSafetyRelevantExplanation(String value) {
        this.safetyRelevantExplanation = value;
    }

    /**
     * Gets the value of the certificationRelevant property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCertificationRelevant() {
        return certificationRelevant;
    }

    /**
     * Sets the value of the certificationRelevant property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCertificationRelevant(String value) {
        this.certificationRelevant = value;
    }

    /**
     * Gets the value of the certificationRelevantExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCertificationRelevantExplanation() {
        return certificationRelevantExplanation;
    }

    /**
     * Sets the value of the certificationRelevantExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCertificationRelevantExplanation(String value) {
        this.certificationRelevantExplanation = value;
    }

    /**
     * Gets the value of the basicMaterial property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getBasicMaterial() {
        return basicMaterial;
    }

    /**
     * Sets the value of the basicMaterial property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setBasicMaterial(String value) {
        this.basicMaterial = value;
    }

    /**
     * Gets the value of the daimlerMaterialDeliveryPropertySpecification property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDaimlerMaterialDeliveryPropertySpecification() {
        return daimlerMaterialDeliveryPropertySpecification;
    }

    /**
     * Sets the value of the daimlerMaterialDeliveryPropertySpecification property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDaimlerMaterialDeliveryPropertySpecification(String value) {
        this.daimlerMaterialDeliveryPropertySpecification = value;
    }

    /**
     * Gets the value of the materialDeliveryQuality property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMaterialDeliveryQuality() {
        return materialDeliveryQuality;
    }

    /**
     * Sets the value of the materialDeliveryQuality property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaterialDeliveryQuality(String value) {
        this.materialDeliveryQuality = value;
    }

    /**
     * Gets the value of the materialSurfaceQuality1 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMaterialSurfaceQuality1() {
        return materialSurfaceQuality1;
    }

    /**
     * Sets the value of the materialSurfaceQuality1 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaterialSurfaceQuality1(String value) {
        this.materialSurfaceQuality1 = value;
    }

    /**
     * Gets the value of the materialSurfaceQuality2 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMaterialSurfaceQuality2() {
        return materialSurfaceQuality2;
    }

    /**
     * Sets the value of the materialSurfaceQuality2 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaterialSurfaceQuality2(String value) {
        this.materialSurfaceQuality2 = value;
    }

    /**
     * Gets the value of the auxiliaryMaterialQuality property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAuxiliaryMaterialQuality() {
        return auxiliaryMaterialQuality;
    }

    /**
     * Sets the value of the auxiliaryMaterialQuality property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAuxiliaryMaterialQuality(String value) {
        this.auxiliaryMaterialQuality = value;
    }

    /**
     * Gets the value of the additionalMaterialQualities property.
     *
     * @return possible object is
     * {@link AddMatQuals }
     */
    public AddMatQuals getAdditionalMaterialQualities() {
        return additionalMaterialQualities;
    }

    /**
     * Sets the value of the additionalMaterialQualities property.
     *
     * @param value allowed object is
     *              {@link AddMatQuals }
     */
    public void setAdditionalMaterialQualities(AddMatQuals value) {
        this.additionalMaterialQualities = value;
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
     * Gets the value of the electromagneticCompatibility property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getElectromagneticCompatibility() {
        return electromagneticCompatibility;
    }

    /**
     * Sets the value of the electromagneticCompatibility property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setElectromagneticCompatibility(String value) {
        this.electromagneticCompatibility = value;
    }

    /**
     * Gets the value of the electromagneticCompatibilityFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getElectromagneticCompatibilityFlag() {
        return electromagneticCompatibilityFlag;
    }

    /**
     * Sets the value of the electromagneticCompatibilityFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setElectromagneticCompatibilityFlag(String value) {
        this.electromagneticCompatibilityFlag = value;
    }

    /**
     * Gets the value of the electromagneticCompatibilityFlagExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getElectromagneticCompatibilityFlagExplanation() {
        return electromagneticCompatibilityFlagExplanation;
    }

    /**
     * Sets the value of the electromagneticCompatibilityFlagExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setElectromagneticCompatibilityFlagExplanation(String value) {
        this.electromagneticCompatibilityFlagExplanation = value;
    }

    /**
     * Gets the value of the vehicleDocumentationRelevant property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVehicleDocumentationRelevant() {
        return vehicleDocumentationRelevant;
    }

    /**
     * Sets the value of the vehicleDocumentationRelevant property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVehicleDocumentationRelevant(String value) {
        this.vehicleDocumentationRelevant = value;
    }

    /**
     * Gets the value of the vehicleDocumentationRelevantExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVehicleDocumentationRelevantExplanation() {
        return vehicleDocumentationRelevantExplanation;
    }

    /**
     * Sets the value of the vehicleDocumentationRelevantExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVehicleDocumentationRelevantExplanation(String value) {
        this.vehicleDocumentationRelevantExplanation = value;
    }

    /**
     * Gets the value of the theftRelevant property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTheftRelevant() {
        return theftRelevant;
    }

    /**
     * Sets the value of the theftRelevant property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTheftRelevant(String value) {
        this.theftRelevant = value;
    }

    /**
     * Gets the value of the theftRelevantExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTheftRelevantExplanation() {
        return theftRelevantExplanation;
    }

    /**
     * Sets the value of the theftRelevantExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTheftRelevantExplanation(String value) {
        this.theftRelevantExplanation = value;
    }

    /**
     * Gets the value of the ecuQualifier property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getEcuQualifier() {
        return ecuQualifier;
    }

    /**
     * Sets the value of the ecuQualifier property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEcuQualifier(String value) {
        this.ecuQualifier = value;
    }

    /**
     * Gets the value of the flashwareSequence property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFlashwareSequence() {
        return flashwareSequence;
    }

    /**
     * Sets the value of the flashwareSequence property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFlashwareSequence(String value) {
        this.flashwareSequence = value;
    }

    /**
     * Gets the value of the dependencyExisting property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDependencyExisting() {
        return dependencyExisting;
    }

    /**
     * Sets the value of the dependencyExisting property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDependencyExisting(String value) {
        this.dependencyExisting = value;
    }

    /**
     * Gets the value of the seriesPreliminaryTest property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSeriesPreliminaryTest() {
        return seriesPreliminaryTest;
    }

    /**
     * Sets the value of the seriesPreliminaryTest property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSeriesPreliminaryTest(String value) {
        this.seriesPreliminaryTest = value;
    }

    /**
     * Gets the value of the seriesPreliminaryTestExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSeriesPreliminaryTestExplanation() {
        return seriesPreliminaryTestExplanation;
    }

    /**
     * Sets the value of the seriesPreliminaryTestExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSeriesPreliminaryTestExplanation(String value) {
        this.seriesPreliminaryTestExplanation = value;
    }

    /**
     * Gets the value of the drawingCharacteristics property.
     *
     * @return possible object is
     * {@link DrawingCharacteristics }
     */
    public DrawingCharacteristics getDrawingCharacteristics() {
        return drawingCharacteristics;
    }

    /**
     * Sets the value of the drawingCharacteristics property.
     *
     * @param value allowed object is
     *              {@link DrawingCharacteristics }
     */
    public void setDrawingCharacteristics(DrawingCharacteristics value) {
        this.drawingCharacteristics = value;
    }

    /**
     * Gets the value of the repetitionFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRepetitionFlag() {
        return repetitionFlag;
    }

    /**
     * Sets the value of the repetitionFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRepetitionFlag(String value) {
        this.repetitionFlag = value;
    }

    /**
     * Gets the value of the exchangeGroup property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getExchangeGroup() {
        return exchangeGroup;
    }

    /**
     * Sets the value of the exchangeGroup property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setExchangeGroup(String value) {
        this.exchangeGroup = value;
    }

    /**
     * Gets the value of the leadingDesignRelease property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLeadingDesignRelease() {
        return leadingDesignRelease;
    }

    /**
     * Sets the value of the leadingDesignRelease property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLeadingDesignRelease(String value) {
        this.leadingDesignRelease = value;
    }

    /**
     * Gets the value of the leadingDesignReleaseExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLeadingDesignReleaseExplanation() {
        return leadingDesignReleaseExplanation;
    }

    /**
     * Sets the value of the leadingDesignReleaseExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLeadingDesignReleaseExplanation(String value) {
        this.leadingDesignReleaseExplanation = value;
    }

    /**
     * Gets the value of the involvedDesignReleases property.
     *
     * @return possible object is
     * {@link InvolvedDesignReleases }
     */
    public InvolvedDesignReleases getInvolvedDesignReleases() {
        return involvedDesignReleases;
    }

    /**
     * Sets the value of the involvedDesignReleases property.
     *
     * @param value allowed object is
     *              {@link InvolvedDesignReleases }
     */
    public void setInvolvedDesignReleases(InvolvedDesignReleases value) {
        this.involvedDesignReleases = value;
    }

    /**
     * Gets the value of the plantSupplies property.
     *
     * @return possible object is
     * {@link PlantSupplies }
     */
    public PlantSupplies getPlantSupplies() {
        return plantSupplies;
    }

    /**
     * Sets the value of the plantSupplies property.
     *
     * @param value allowed object is
     *              {@link PlantSupplies }
     */
    public void setPlantSupplies(PlantSupplies value) {
        this.plantSupplies = value;
    }

    /**
     * Gets the value of the labelingCharacteristicsExisting property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLabelingCharacteristicsExisting() {
        return labelingCharacteristicsExisting;
    }

    /**
     * Sets the value of the labelingCharacteristicsExisting property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLabelingCharacteristicsExisting(String value) {
        this.labelingCharacteristicsExisting = value;
    }

    /**
     * Gets the value of the labelingCharacteristicsExistingExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLabelingCharacteristicsExistingExplanation() {
        return labelingCharacteristicsExistingExplanation;
    }

    /**
     * Sets the value of the labelingCharacteristicsExistingExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLabelingCharacteristicsExistingExplanation(String value) {
        this.labelingCharacteristicsExistingExplanation = value;
    }

    /**
     * Gets the value of the ecoFromMasterData property.
     *
     * @return possible object is
     * {@link EcoMasterData }
     */
    public EcoMasterData getEcoFromMasterData() {
        return ecoFromMasterData;
    }

    /**
     * Sets the value of the ecoFromMasterData property.
     *
     * @param value allowed object is
     *              {@link EcoMasterData }
     */
    public void setEcoFromMasterData(EcoMasterData value) {
        this.ecoFromMasterData = value;
    }

}
