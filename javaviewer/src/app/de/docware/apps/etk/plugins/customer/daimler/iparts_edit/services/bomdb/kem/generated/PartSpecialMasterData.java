package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for partSpecialMasterData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partSpecialMasterData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="part" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sharedPartDgv" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sharedPartType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sharedPartTypeExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sharedPartFlag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sharedPartFlagExplanation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="surface" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         &lt;element name="sparePartIdentifiers" type="{http://bomDbServices.eng.dai/}sparePartIdentifiers" minOccurs="0"/>
 *         &lt;element name="srmIdentifiers" type="{http://bomDbServices.eng.dai/}srmIdentifiers" minOccurs="0"/>
 *         &lt;element name="plasticPiping" type="{http://bomDbServices.eng.dai/}plasticPiping" minOccurs="0"/>
 *         &lt;element name="replacementAndRawParts" type="{http://bomDbServices.eng.dai/}replacementAndRawParts" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partSpecialMasterData", propOrder = {
        "part",
        "sharedPartDgv",
        "sharedPartType",
        "sharedPartTypeExplanation",
        "sharedPartFlag",
        "sharedPartFlagExplanation",
        "surface",
        "sparePartIdentifiers",
        "srmIdentifiers",
        "plasticPiping",
        "replacementAndRawParts"
})
public class PartSpecialMasterData {

    protected String part;
    protected String sharedPartDgv;
    protected String sharedPartType;
    protected String sharedPartTypeExplanation;
    protected String sharedPartFlag;
    protected String sharedPartFlagExplanation;
    protected Double surface;
    protected SparePartIdentifiers sparePartIdentifiers;
    protected SrmIdentifiers srmIdentifiers;
    protected PlasticPiping plasticPiping;
    protected ReplacementAndRawParts replacementAndRawParts;

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
     * Gets the value of the sharedPartDgv property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSharedPartDgv() {
        return sharedPartDgv;
    }

    /**
     * Sets the value of the sharedPartDgv property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSharedPartDgv(String value) {
        this.sharedPartDgv = value;
    }

    /**
     * Gets the value of the sharedPartType property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSharedPartType() {
        return sharedPartType;
    }

    /**
     * Sets the value of the sharedPartType property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSharedPartType(String value) {
        this.sharedPartType = value;
    }

    /**
     * Gets the value of the sharedPartTypeExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSharedPartTypeExplanation() {
        return sharedPartTypeExplanation;
    }

    /**
     * Sets the value of the sharedPartTypeExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSharedPartTypeExplanation(String value) {
        this.sharedPartTypeExplanation = value;
    }

    /**
     * Gets the value of the sharedPartFlag property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSharedPartFlag() {
        return sharedPartFlag;
    }

    /**
     * Sets the value of the sharedPartFlag property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSharedPartFlag(String value) {
        this.sharedPartFlag = value;
    }

    /**
     * Gets the value of the sharedPartFlagExplanation property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSharedPartFlagExplanation() {
        return sharedPartFlagExplanation;
    }

    /**
     * Sets the value of the sharedPartFlagExplanation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSharedPartFlagExplanation(String value) {
        this.sharedPartFlagExplanation = value;
    }

    /**
     * Gets the value of the surface property.
     *
     * @return possible object is
     * {@link Double }
     */
    public Double getSurface() {
        return surface;
    }

    /**
     * Sets the value of the surface property.
     *
     * @param value allowed object is
     *              {@link Double }
     */
    public void setSurface(Double value) {
        this.surface = value;
    }

    /**
     * Gets the value of the sparePartIdentifiers property.
     *
     * @return possible object is
     * {@link SparePartIdentifiers }
     */
    public SparePartIdentifiers getSparePartIdentifiers() {
        return sparePartIdentifiers;
    }

    /**
     * Sets the value of the sparePartIdentifiers property.
     *
     * @param value allowed object is
     *              {@link SparePartIdentifiers }
     */
    public void setSparePartIdentifiers(SparePartIdentifiers value) {
        this.sparePartIdentifiers = value;
    }

    /**
     * Gets the value of the srmIdentifiers property.
     *
     * @return possible object is
     * {@link SrmIdentifiers }
     */
    public SrmIdentifiers getSrmIdentifiers() {
        return srmIdentifiers;
    }

    /**
     * Sets the value of the srmIdentifiers property.
     *
     * @param value allowed object is
     *              {@link SrmIdentifiers }
     */
    public void setSrmIdentifiers(SrmIdentifiers value) {
        this.srmIdentifiers = value;
    }

    /**
     * Gets the value of the plasticPiping property.
     *
     * @return possible object is
     * {@link PlasticPiping }
     */
    public PlasticPiping getPlasticPiping() {
        return plasticPiping;
    }

    /**
     * Sets the value of the plasticPiping property.
     *
     * @param value allowed object is
     *              {@link PlasticPiping }
     */
    public void setPlasticPiping(PlasticPiping value) {
        this.plasticPiping = value;
    }

    /**
     * Gets the value of the replacementAndRawParts property.
     *
     * @return possible object is
     * {@link ReplacementAndRawParts }
     */
    public ReplacementAndRawParts getReplacementAndRawParts() {
        return replacementAndRawParts;
    }

    /**
     * Sets the value of the replacementAndRawParts property.
     *
     * @param value allowed object is
     *              {@link ReplacementAndRawParts }
     */
    public void setReplacementAndRawParts(ReplacementAndRawParts value) {
        this.replacementAndRawParts = value;
    }

}
