package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RBKWWResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RBKWWResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="s05Alternatives" type="{http://bomDbServices.eng.dai/}s05Alternatives" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RBKWWResult", propOrder = {
        "queryHead",
        "s05Alternatives"
})
public class GetT43RBKWWResult {

    protected QueryHead queryHead;
    protected S05Alternatives s05Alternatives;

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
     * Gets the value of the s05Alternatives property.
     *
     * @return possible object is
     * {@link S05Alternatives }
     */
    public S05Alternatives getS05Alternatives() {
        return s05Alternatives;
    }

    /**
     * Sets the value of the s05Alternatives property.
     *
     * @param value allowed object is
     *              {@link S05Alternatives }
     */
    public void setS05Alternatives(S05Alternatives value) {
        this.s05Alternatives = value;
    }

}
