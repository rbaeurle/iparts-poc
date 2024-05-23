package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getAssemblyStructureMultiLevelResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getAssemblyStructureMultiLevelResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="s05AssemblyStructureMLs" type="{http://bomDbServices.eng.dai/}s05AssemblyStructureMLs" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAssemblyStructureMultiLevelResult", propOrder = {
        "queryHead",
        "s05AssemblyStructureMLs"
})
public class GetAssemblyStructureMultiLevelResult {

    protected QueryHead queryHead;
    protected S05AssemblyStructureMLs s05AssemblyStructureMLs;

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
     * Gets the value of the s05AssemblyStructureMLs property.
     *
     * @return possible object is
     * {@link S05AssemblyStructureMLs }
     */
    public S05AssemblyStructureMLs getS05AssemblyStructureMLs() {
        return s05AssemblyStructureMLs;
    }

    /**
     * Sets the value of the s05AssemblyStructureMLs property.
     *
     * @param value allowed object is
     *              {@link S05AssemblyStructureMLs }
     */
    public void setS05AssemblyStructureMLs(S05AssemblyStructureMLs value) {
        this.s05AssemblyStructureMLs = value;
    }

}
