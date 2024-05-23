package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for partAdditionalMasterData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partAdditionalMasterData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="language" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="part" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="designChangeCounter" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="s05Remarks" type="{http://bomDbServices.eng.dai/}s05Remarks" minOccurs="0"/>
 *         &lt;element name="s05AlternativeExplanations" type="{http://bomDbServices.eng.dai/}s05AltExps" minOccurs="0"/>
 *         &lt;element name="materialsAndSpecifications" type="{http://bomDbServices.eng.dai/}matNSpecs" minOccurs="0"/>
 *         &lt;element name="additionalMaterialQualities" type="{http://bomDbServices.eng.dai/}addMatQuals" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partAdditionalMasterData", propOrder = {
        "language",
        "part",
        "version",
        "designChangeCounter",
        "s05Remarks",
        "s05AlternativeExplanations",
        "materialsAndSpecifications",
        "additionalMaterialQualities"
})
public class PartAdditionalMasterData {

    protected Language language;
    protected String part;
    protected Integer version;
    protected Integer designChangeCounter;
    protected S05Remarks s05Remarks;
    protected S05AltExps s05AlternativeExplanations;
    protected MatNSpecs materialsAndSpecifications;
    protected AddMatQuals additionalMaterialQualities;

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
     * Gets the value of the version property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setVersion(Integer value) {
        this.version = value;
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
     * Gets the value of the s05Remarks property.
     *
     * @return possible object is
     * {@link S05Remarks }
     */
    public S05Remarks getS05Remarks() {
        return s05Remarks;
    }

    /**
     * Sets the value of the s05Remarks property.
     *
     * @param value allowed object is
     *              {@link S05Remarks }
     */
    public void setS05Remarks(S05Remarks value) {
        this.s05Remarks = value;
    }

    /**
     * Gets the value of the s05AlternativeExplanations property.
     *
     * @return possible object is
     * {@link S05AltExps }
     */
    public S05AltExps getS05AlternativeExplanations() {
        return s05AlternativeExplanations;
    }

    /**
     * Sets the value of the s05AlternativeExplanations property.
     *
     * @param value allowed object is
     *              {@link S05AltExps }
     */
    public void setS05AlternativeExplanations(S05AltExps value) {
        this.s05AlternativeExplanations = value;
    }

    /**
     * Gets the value of the materialsAndSpecifications property.
     *
     * @return possible object is
     * {@link MatNSpecs }
     */
    public MatNSpecs getMaterialsAndSpecifications() {
        return materialsAndSpecifications;
    }

    /**
     * Sets the value of the materialsAndSpecifications property.
     *
     * @param value allowed object is
     *              {@link MatNSpecs }
     */
    public void setMaterialsAndSpecifications(MatNSpecs value) {
        this.materialsAndSpecifications = value;
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

}
