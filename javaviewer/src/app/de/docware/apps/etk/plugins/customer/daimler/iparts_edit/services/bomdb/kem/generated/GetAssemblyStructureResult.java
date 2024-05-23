package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getAssemblyStructureResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getAssemblyStructureResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="s05AssemblyStructures" type="{http://bomDbServices.eng.dai/}s05AssemblyStructures" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAssemblyStructureResult", propOrder = {
        "queryHead",
        "s05AssemblyStructures"
})
public class GetAssemblyStructureResult {

    protected QueryHead queryHead;
    protected S05AssemblyStructures s05AssemblyStructures;

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
     * Gets the value of the s05AssemblyStructures property.
     *
     * @return possible object is
     * {@link S05AssemblyStructures }
     */
    public S05AssemblyStructures getS05AssemblyStructures() {
        return s05AssemblyStructures;
    }

    /**
     * Sets the value of the s05AssemblyStructures property.
     *
     * @param value allowed object is
     *              {@link S05AssemblyStructures }
     */
    public void setS05AssemblyStructures(S05AssemblyStructures value) {
        this.s05AssemblyStructures = value;
    }

}
