package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RBKResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RBKResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="s05s" type="{http://bomDbServices.eng.dai/}s05S" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RBKResult", propOrder = {
        "queryHead",
        "s05S"
})
public class GetT43RBKResult {

    protected QueryHead queryHead;
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
