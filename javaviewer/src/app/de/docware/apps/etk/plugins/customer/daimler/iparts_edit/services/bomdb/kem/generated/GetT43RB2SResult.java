package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RB2SResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RB2SResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="b70b90Remarks" type="{http://bomDbServices.eng.dai/}b70B90Remarks" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RB2SResult", propOrder = {
        "queryHead",
        "b70B90Remarks"
})
public class GetT43RB2SResult {

    protected QueryHead queryHead;
    @XmlElement(name = "b70b90Remarks")
    protected B70B90Remarks b70B90Remarks;

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
     * Gets the value of the b70B90Remarks property.
     *
     * @return possible object is
     * {@link B70B90Remarks }
     */
    public B70B90Remarks getB70B90Remarks() {
        return b70B90Remarks;
    }

    /**
     * Sets the value of the b70B90Remarks property.
     *
     * @param value allowed object is
     *              {@link B70B90Remarks }
     */
    public void setB70B90Remarks(B70B90Remarks value) {
        this.b70B90Remarks = value;
    }

}
