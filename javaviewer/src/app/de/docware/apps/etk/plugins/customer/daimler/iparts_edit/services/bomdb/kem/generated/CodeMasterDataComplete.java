package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for codeMasterDataComplete complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="codeMasterDataComplete">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="codeMasterData" type="{http://bomDbServices.eng.dai/}codeMasterData" minOccurs="0"/>
 *         &lt;element name="codeLangDatas" type="{http://bomDbServices.eng.dai/}codeLangDatas" minOccurs="0"/>
 *         &lt;element name="itemScopes" type="{http://bomDbServices.eng.dai/}itemScopes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "codeMasterDataComplete", propOrder = {
        "code",
        "version",
        "codeMasterData",
        "codeLangDatas",
        "itemScopes"
})
public class CodeMasterDataComplete {

    protected String code;
    protected int version;
    protected CodeMasterData codeMasterData;
    protected CodeLangDatas codeLangDatas;
    protected ItemScopes itemScopes;

    /**
     * Gets the value of the code property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCode(String value) {
        this.code = value;
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
     * Gets the value of the codeMasterData property.
     *
     * @return possible object is
     * {@link CodeMasterData }
     */
    public CodeMasterData getCodeMasterData() {
        return codeMasterData;
    }

    /**
     * Sets the value of the codeMasterData property.
     *
     * @param value allowed object is
     *              {@link CodeMasterData }
     */
    public void setCodeMasterData(CodeMasterData value) {
        this.codeMasterData = value;
    }

    /**
     * Gets the value of the codeLangDatas property.
     *
     * @return possible object is
     * {@link CodeLangDatas }
     */
    public CodeLangDatas getCodeLangDatas() {
        return codeLangDatas;
    }

    /**
     * Sets the value of the codeLangDatas property.
     *
     * @param value allowed object is
     *              {@link CodeLangDatas }
     */
    public void setCodeLangDatas(CodeLangDatas value) {
        this.codeLangDatas = value;
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
