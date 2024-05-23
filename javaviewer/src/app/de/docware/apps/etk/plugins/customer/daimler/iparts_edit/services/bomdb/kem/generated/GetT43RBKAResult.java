package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RBKAResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RBKAResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="s05TighteningTorques" type="{http://bomDbServices.eng.dai/}s05TighteningTorques" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RBKAResult", propOrder = {
        "queryHead",
        "s05TighteningTorques"
})
public class GetT43RBKAResult {

    protected QueryHead queryHead;
    protected S05TighteningTorques s05TighteningTorques;

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
     * Gets the value of the s05TighteningTorques property.
     *
     * @return possible object is
     * {@link S05TighteningTorques }
     */
    public S05TighteningTorques getS05TighteningTorques() {
        return s05TighteningTorques;
    }

    /**
     * Sets the value of the s05TighteningTorques property.
     *
     * @param value allowed object is
     *              {@link S05TighteningTorques }
     */
    public void setS05TighteningTorques(S05TighteningTorques value) {
        this.s05TighteningTorques = value;
    }

}
