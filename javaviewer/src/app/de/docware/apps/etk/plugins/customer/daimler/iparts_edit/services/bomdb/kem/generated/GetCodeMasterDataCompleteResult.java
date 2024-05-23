package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getCodeMasterDataCompleteResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getCodeMasterDataCompleteResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="codeMasterDataCompletes" type="{http://bomDbServices.eng.dai/}codeMasterDataCompletes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getCodeMasterDataCompleteResult", propOrder = {
        "queryHead",
        "codeMasterDataCompletes"
})
public class GetCodeMasterDataCompleteResult {

    protected QueryHead queryHead;
    protected CodeMasterDataCompletes codeMasterDataCompletes;

    /**
     * Gets the value of the queryHead property.
     *
     * @return possible object is
     * {@link QueryHead }
     */
    public QueryHead getQueryHead() {
        return queryHead;
    }

    /**
     * Sets the value of the queryHead property.
     *
     * @param value allowed object is
     *              {@link QueryHead }
     */
    public void setQueryHead(QueryHead value) {
        this.queryHead = value;
    }

    /**
     * Gets the value of the codeMasterDataCompletes property.
     *
     * @return possible object is
     * {@link CodeMasterDataCompletes }
     */
    public CodeMasterDataCompletes getCodeMasterDataCompletes() {
        return codeMasterDataCompletes;
    }

    /**
     * Sets the value of the codeMasterDataCompletes property.
     *
     * @param value allowed object is
     *              {@link CodeMasterDataCompletes }
     */
    public void setCodeMasterDataCompletes(CodeMasterDataCompletes value) {
        this.codeMasterDataCompletes = value;
    }

}
