package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for partsListMasterDataComplete complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partsListMasterDataComplete">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="partsList" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="partsListMasterData" type="{http://bomDbServices.eng.dai/}partsListMasterData" minOccurs="0"/>
 *         &lt;element name="partsListLangDatas" type="{http://bomDbServices.eng.dai/}partsListLangDatas" minOccurs="0"/>
 *         &lt;element name="itemScopes" type="{http://bomDbServices.eng.dai/}itemScopes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partsListMasterDataComplete", propOrder = {
        "partsList",
        "version",
        "partsListMasterData",
        "partsListLangDatas",
        "itemScopes"
})
public class PartsListMasterDataComplete {

    protected String partsList;
    protected int version;
    protected PartsListMasterData partsListMasterData;
    protected PartsListLangDatas partsListLangDatas;
    protected ItemScopes itemScopes;

    /**
     * Gets the value of the partsList property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPartsList() {
        return partsList;
    }

    /**
     * Sets the value of the partsList property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPartsList(String value) {
        this.partsList = value;
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
     * Gets the value of the partsListMasterData property.
     *
     * @return possible object is
     * {@link PartsListMasterData }
     */
    public PartsListMasterData getPartsListMasterData() {
        return partsListMasterData;
    }

    /**
     * Sets the value of the partsListMasterData property.
     *
     * @param value allowed object is
     *              {@link PartsListMasterData }
     */
    public void setPartsListMasterData(PartsListMasterData value) {
        this.partsListMasterData = value;
    }

    /**
     * Gets the value of the partsListLangDatas property.
     *
     * @return possible object is
     * {@link PartsListLangDatas }
     */
    public PartsListLangDatas getPartsListLangDatas() {
        return partsListLangDatas;
    }

    /**
     * Sets the value of the partsListLangDatas property.
     *
     * @param value allowed object is
     *              {@link PartsListLangDatas }
     */
    public void setPartsListLangDatas(PartsListLangDatas value) {
        this.partsListLangDatas = value;
    }

    /**
     * Gets the value of the itemScopes property.
     *
     * @return possible object is
     * {@link ItemScopes }
     */
    public ItemScopes getItemScopes() {
        return itemScopes;
    }

    /**
     * Sets the value of the itemScopes property.
     *
     * @param value allowed object is
     *              {@link ItemScopes }
     */
    public void setItemScopes(ItemScopes value) {
        this.itemScopes = value;
    }

}
