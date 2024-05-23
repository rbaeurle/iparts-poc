package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getModuleMasterDataCompleteResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getModuleMasterDataCompleteResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="moduleMasterDataCompletes" type="{http://bomDbServices.eng.dai/}moduleMasterDataCompletes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getModuleMasterDataCompleteResult", propOrder = {
        "queryHead",
        "moduleMasterDataCompletes"
})
public class GetModuleMasterDataCompleteResult {

    protected QueryHead queryHead;
    protected ModuleMasterDataCompletes moduleMasterDataCompletes;

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
     * Gets the value of the moduleMasterDataCompletes property.
     *
     * @return possible object is
     * {@link ModuleMasterDataCompletes }
     */
    public ModuleMasterDataCompletes getModuleMasterDataCompletes() {
        return moduleMasterDataCompletes;
    }

    /**
     * Sets the value of the moduleMasterDataCompletes property.
     *
     * @param value allowed object is
     *              {@link ModuleMasterDataCompletes }
     */
    public void setModuleMasterDataCompletes(ModuleMasterDataCompletes value) {
        this.moduleMasterDataCompletes = value;
    }

}
