package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getUsageOrContentResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getUsageOrContentResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="b90s" type="{http://bomDbServices.eng.dai/}b90S" minOccurs="0"/>
 *         &lt;element name="s05s" type="{http://bomDbServices.eng.dai/}s05S" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getUsageOrContentResult", propOrder = {
        "queryHead",
        "b90S",
        "s05S"
})
public class GetUsageOrContentResult {

    protected QueryHead queryHead;
    @XmlElement(name = "b90s")
    protected B90S b90S;
    @XmlElement(name = "s05s")
    protected S05S s05S;

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
     * Gets the value of the b90S property.
     *
     * @return possible object is
     * {@link B90S }
     */
    public B90S getB90S() {
        return b90S;
    }

    /**
     * Sets the value of the b90S property.
     *
     * @param value allowed object is
     *              {@link B90S }
     */
    public void setB90S(B90S value) {
        this.b90S = value;
    }

    /**
     * Gets the value of the s05S property.
     *
     * @return possible object is
     * {@link S05S }
     */
    public S05S getS05S() {
        return s05S;
    }

    /**
     * Sets the value of the s05S property.
     *
     * @param value allowed object is
     *              {@link S05S }
     */
    public void setS05S(S05S value) {
        this.s05S = value;
    }

}
