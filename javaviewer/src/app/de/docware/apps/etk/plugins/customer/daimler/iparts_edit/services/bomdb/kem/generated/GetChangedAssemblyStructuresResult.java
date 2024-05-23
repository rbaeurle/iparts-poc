package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getChangedAssemblyStructuresResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getChangedAssemblyStructuresResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="changedStructures" type="{http://bomDbServices.eng.dai/}changedStructures" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getChangedAssemblyStructuresResult", propOrder = {
        "queryHead",
        "changedStructures"
})
public class GetChangedAssemblyStructuresResult {

    protected QueryHead queryHead;
    protected ChangedStructures changedStructures;

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
     * Gets the value of the changedStructures property.
     *
     * @return possible object is
     * {@link ChangedStructures }
     */
    public ChangedStructures getChangedStructures() {
        return changedStructures;
    }

    /**
     * Sets the value of the changedStructures property.
     *
     * @param value allowed object is
     *              {@link ChangedStructures }
     */
    public void setChangedStructures(ChangedStructures value) {
        this.changedStructures = value;
    }

}
