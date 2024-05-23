package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RBKVResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RBKVResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="s05PointOfUsageTexts" type="{http://bomDbServices.eng.dai/}s05PointOfUsageTexts" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RBKVResult", propOrder = {
        "queryHead",
        "s05PointOfUsageTexts"
})
public class GetT43RBKVResult {

    protected QueryHead queryHead;
    protected S05PointOfUsageTexts s05PointOfUsageTexts;

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
     * Gets the value of the s05PointOfUsageTexts property.
     *
     * @return possible object is
     * {@link S05PointOfUsageTexts }
     */
    public S05PointOfUsageTexts getS05PointOfUsageTexts() {
        return s05PointOfUsageTexts;
    }

    /**
     * Sets the value of the s05PointOfUsageTexts property.
     *
     * @param value allowed object is
     *              {@link S05PointOfUsageTexts }
     */
    public void setS05PointOfUsageTexts(S05PointOfUsageTexts value) {
        this.s05PointOfUsageTexts = value;
    }

}
