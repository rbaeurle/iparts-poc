package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RAEHResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RAEHResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="changeNotices" type="{http://bomDbServices.eng.dai/}changeNotices" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RAEHResult", propOrder = {
        "queryHead",
        "changeNotices"
})
public class GetT43RAEHResult {

    protected QueryHead queryHead;
    protected ChangeNotices changeNotices;

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
     * Gets the value of the changeNotices property.
     *
     * @return possible object is
     * {@link ChangeNotices }
     */
    public ChangeNotices getChangeNotices() {
        return changeNotices;
    }

    /**
     * Sets the value of the changeNotices property.
     *
     * @param value allowed object is
     *              {@link ChangeNotices }
     */
    public void setChangeNotices(ChangeNotices value) {
        this.changeNotices = value;
    }

}
