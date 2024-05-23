package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for partMasterDataComplete complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partMasterDataComplete">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="part" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="partMasterData" type="{http://bomDbServices.eng.dai/}partMasterData" minOccurs="0"/>
 *         &lt;element name="partAdditionalMasterData" type="{http://bomDbServices.eng.dai/}partAdditionalMasterData" minOccurs="0"/>
 *         &lt;element name="partLangDatas" type="{http://bomDbServices.eng.dai/}partLangDatas" minOccurs="0"/>
 *         &lt;element name="partSpecialMasterData" type="{http://bomDbServices.eng.dai/}partSpecialMasterData" minOccurs="0"/>
 *         &lt;element name="partWeightDatas" type="{http://bomDbServices.eng.dai/}partWeightDatas" minOccurs="0"/>
 *         &lt;element name="f14BasicDatas" type="{http://bomDbServices.eng.dai/}f14BasicDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partMasterDataComplete", propOrder = {
        "part",
        "version",
        "partMasterData",
        "partAdditionalMasterData",
        "partLangDatas",
        "partSpecialMasterData",
        "partWeightDatas",
        "f14BasicDatas"
})
public class PartMasterDataComplete {

    protected String part;
    protected int version;
    protected PartMasterData partMasterData;
    protected PartAdditionalMasterData partAdditionalMasterData;
    protected PartLangDatas partLangDatas;
    protected PartSpecialMasterData partSpecialMasterData;
    protected PartWeightDatas partWeightDatas;
    protected F14BasicDatas f14BasicDatas;

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
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     */
    public void setVersion(int value) {
        this.version = value;
    }

    /**
     * Gets the value of the partMasterData property.
     *
     * @return possible object is
     * {@link PartMasterData }
     */
    public PartMasterData getPartMasterData() {
        return partMasterData;
    }

    /**
     * Sets the value of the partMasterData property.
     *
     * @param value allowed object is
     *              {@link PartMasterData }
     */
    public void setPartMasterData(PartMasterData value) {
        this.partMasterData = value;
    }

    /**
     * Gets the value of the partAdditionalMasterData property.
     *
     * @return possible object is
     * {@link PartAdditionalMasterData }
     */
    public PartAdditionalMasterData getPartAdditionalMasterData() {
        return partAdditionalMasterData;
    }

    /**
     * Sets the value of the partAdditionalMasterData property.
     *
     * @param value allowed object is
     *              {@link PartAdditionalMasterData }
     */
    public void setPartAdditionalMasterData(PartAdditionalMasterData value) {
        this.partAdditionalMasterData = value;
    }

    /**
     * Gets the value of the partLangDatas property.
     *
     * @return possible object is
     * {@link PartLangDatas }
     */
    public PartLangDatas getPartLangDatas() {
        return partLangDatas;
    }

    /**
     * Sets the value of the partLangDatas property.
     *
     * @param value allowed object is
     *              {@link PartLangDatas }
     */
    public void setPartLangDatas(PartLangDatas value) {
        this.partLangDatas = value;
    }

    /**
     * Gets the value of the partSpecialMasterData property.
     *
     * @return possible object is
     * {@link PartSpecialMasterData }
     */
    public PartSpecialMasterData getPartSpecialMasterData() {
        return partSpecialMasterData;
    }

    /**
     * Sets the value of the partSpecialMasterData property.
     *
     * @param value allowed object is
     *              {@link PartSpecialMasterData }
     */
    public void setPartSpecialMasterData(PartSpecialMasterData value) {
        this.partSpecialMasterData = value;
    }

    /**
     * Gets the value of the partWeightDatas property.
     *
     * @return possible object is
     * {@link PartWeightDatas }
     */
    public PartWeightDatas getPartWeightDatas() {
        return partWeightDatas;
    }

    /**
     * Sets the value of the partWeightDatas property.
     *
     * @param value allowed object is
     *              {@link PartWeightDatas }
     */
    public void setPartWeightDatas(PartWeightDatas value) {
        this.partWeightDatas = value;
    }

    /**
     * Gets the value of the f14BasicDatas property.
     *
     * @return possible object is
     * {@link F14BasicDatas }
     */
    public F14BasicDatas getF14BasicDatas() {
        return f14BasicDatas;
    }

    /**
     * Sets the value of the f14BasicDatas property.
     *
     * @param value allowed object is
     *              {@link F14BasicDatas }
     */
    public void setF14BasicDatas(F14BasicDatas value) {
        this.f14BasicDatas = value;
    }

}
