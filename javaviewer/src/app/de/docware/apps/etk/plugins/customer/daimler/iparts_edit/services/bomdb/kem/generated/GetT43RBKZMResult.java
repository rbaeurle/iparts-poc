package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RBKZMResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RBKZMResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="s05AlternativeTogetherWiths" type="{http://bomDbServices.eng.dai/}s05AltTogetherWiths" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RBKZMResult", propOrder = {
        "queryHead",
        "s05AlternativeTogetherWiths"
})
public class GetT43RBKZMResult {

    protected QueryHead queryHead;
    protected S05AltTogetherWiths s05AlternativeTogetherWiths;

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
     * Gets the value of the s05AlternativeTogetherWiths property.
     *
     * @return possible object is
     * {@link S05AltTogetherWiths }
     */
    public S05AltTogetherWiths getS05AlternativeTogetherWiths() {
        return s05AlternativeTogetherWiths;
    }

    /**
     * Sets the value of the s05AlternativeTogetherWiths property.
     *
     * @param value allowed object is
     *              {@link S05AltTogetherWiths }
     */
    public void setS05AlternativeTogetherWiths(S05AltTogetherWiths value) {
        this.s05AlternativeTogetherWiths = value;
    }

}
