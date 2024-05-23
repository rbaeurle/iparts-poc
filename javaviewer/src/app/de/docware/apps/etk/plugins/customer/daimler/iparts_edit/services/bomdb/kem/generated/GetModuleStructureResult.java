package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getModuleStructureResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getModuleStructureResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="b70ModuleStructures" type="{http://bomDbServices.eng.dai/}b70ModuleStructures" minOccurs="0"/>
 *         &lt;element name="b90ModuleStructures" type="{http://bomDbServices.eng.dai/}b90ModuleStructures" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getModuleStructureResult", propOrder = {
        "queryHead",
        "b70ModuleStructures",
        "b90ModuleStructures"
})
public class GetModuleStructureResult {

    protected QueryHead queryHead;
    protected B70ModuleStructures b70ModuleStructures;
    protected B90ModuleStructures b90ModuleStructures;

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
     * Gets the value of the b70ModuleStructures property.
     *
     * @return possible object is
     * {@link B70ModuleStructures }
     */
    public B70ModuleStructures getB70ModuleStructures() {
        return b70ModuleStructures;
    }

    /**
     * Sets the value of the b70ModuleStructures property.
     *
     * @param value allowed object is
     *              {@link B70ModuleStructures }
     */
    public void setB70ModuleStructures(B70ModuleStructures value) {
        this.b70ModuleStructures = value;
    }

    /**
     * Gets the value of the b90ModuleStructures property.
     *
     * @return possible object is
     * {@link B90ModuleStructures }
     */
    public B90ModuleStructures getB90ModuleStructures() {
        return b90ModuleStructures;
    }

    /**
     * Sets the value of the b90ModuleStructures property.
     *
     * @param value allowed object is
     *              {@link B90ModuleStructures }
     */
    public void setB90ModuleStructures(B90ModuleStructures value) {
        this.b90ModuleStructures = value;
    }

}
