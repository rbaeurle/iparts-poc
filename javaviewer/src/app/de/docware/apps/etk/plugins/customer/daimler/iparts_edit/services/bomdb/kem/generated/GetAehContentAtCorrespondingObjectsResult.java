package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getAehContentAtCorrespondingObjectsResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getAehContentAtCorrespondingObjectsResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="aehCorrespondingResults" type="{http://bomDbServices.eng.dai/}aehCorrespondingResults" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAehContentAtCorrespondingObjectsResult", propOrder = {
        "queryHead",
        "aehCorrespondingResults"
})
public class GetAehContentAtCorrespondingObjectsResult {

    protected QueryHead queryHead;
    protected AehCorrespondingResults aehCorrespondingResults;

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
     * Gets the value of the aehCorrespondingResults property.
     *
     * @return possible object is
     * {@link AehCorrespondingResults }
     */
    public AehCorrespondingResults getAehCorrespondingResults() {
        return aehCorrespondingResults;
    }

    /**
     * Sets the value of the aehCorrespondingResults property.
     *
     * @param value allowed object is
     *              {@link AehCorrespondingResults }
     */
    public void setAehCorrespondingResults(AehCorrespondingResults value) {
        this.aehCorrespondingResults = value;
    }

}
