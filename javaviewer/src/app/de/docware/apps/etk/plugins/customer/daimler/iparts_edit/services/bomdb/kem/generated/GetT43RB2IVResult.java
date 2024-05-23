package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RB2IVResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RB2IVResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="b90UsageLocations" type="{http://bomDbServices.eng.dai/}b90UsageLocations" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RB2IVResult", propOrder = {
        "queryHead",
        "b90UsageLocations"
})
public class GetT43RB2IVResult {

    protected QueryHead queryHead;
    protected B90UsageLocations b90UsageLocations;

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
     * Gets the value of the b90UsageLocations property.
     *
     * @return possible object is
     * {@link B90UsageLocations }
     */
    public B90UsageLocations getB90UsageLocations() {
        return b90UsageLocations;
    }

    /**
     * Sets the value of the b90UsageLocations property.
     *
     * @param value allowed object is
     *              {@link B90UsageLocations }
     */
    public void setB90UsageLocations(B90UsageLocations value) {
        this.b90UsageLocations = value;
    }

}
