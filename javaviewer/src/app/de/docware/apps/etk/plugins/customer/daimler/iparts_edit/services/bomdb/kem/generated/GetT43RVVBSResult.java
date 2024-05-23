package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RVVBSResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RVVBSResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="b84SalesDescriptions" type="{http://bomDbServices.eng.dai/}b84SalesDescriptions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RVVBSResult", propOrder = {
        "queryHead",
        "b84SalesDescriptions"
})
public class GetT43RVVBSResult {

    protected QueryHead queryHead;
    protected B84SalesDescriptions b84SalesDescriptions;

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
     * Gets the value of the b84SalesDescriptions property.
     *
     * @return possible object is
     * {@link B84SalesDescriptions }
     */
    public B84SalesDescriptions getB84SalesDescriptions() {
        return b84SalesDescriptions;
    }

    /**
     * Sets the value of the b84SalesDescriptions property.
     *
     * @param value allowed object is
     *              {@link B84SalesDescriptions }
     */
    public void setB84SalesDescriptions(B84SalesDescriptions value) {
        this.b84SalesDescriptions = value;
    }

}
