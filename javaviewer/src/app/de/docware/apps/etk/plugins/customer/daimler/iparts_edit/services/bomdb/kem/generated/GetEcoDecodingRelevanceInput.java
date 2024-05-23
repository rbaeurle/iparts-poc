package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getEcoDecodingRelevanceInput complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getEcoDecodingRelevanceInput">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="evaluateEcos" type="{http://bomDbServices.eng.dai/}evaluateEcos"/>
 *         &lt;element name="searchPlantSupplies" type="{http://bomDbServices.eng.dai/}searchPlantSupplies" minOccurs="0"/>
 *         &lt;element name="maxNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getEcoDecodingRelevanceInput", propOrder = {
        "queryName",
        "evaluateEcos",
        "searchPlantSupplies",
        "maxNumber"
})
public class GetEcoDecodingRelevanceInput {

    protected String queryName;
    @XmlElement(required = true)
    protected EvaluateEcos evaluateEcos;
    protected SearchPlantSupplies searchPlantSupplies;
    protected Integer maxNumber;

    /**
     * Gets the value of the queryName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getQueryName() {
        return queryName;
    }

    /**
     * Sets the value of the queryName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setQueryName(String value) {
        this.queryName = value;
    }

    /**
     * Gets the value of the evaluateEcos property.
     *
     * @return possible object is
     * {@link EvaluateEcos }
     */
    public EvaluateEcos getEvaluateEcos() {
        return evaluateEcos;
    }

    /**
     * Sets the value of the evaluateEcos property.
     *
     * @param value allowed object is
     *              {@link EvaluateEcos }
     */
    public void setEvaluateEcos(EvaluateEcos value) {
        this.evaluateEcos = value;
    }

    /**
     * Gets the value of the searchPlantSupplies property.
     *
     * @return possible object is
     * {@link SearchPlantSupplies }
     */
    public SearchPlantSupplies getSearchPlantSupplies() {
        return searchPlantSupplies;
    }

    /**
     * Sets the value of the searchPlantSupplies property.
     *
     * @param value allowed object is
     *              {@link SearchPlantSupplies }
     */
    public void setSearchPlantSupplies(SearchPlantSupplies value) {
        this.searchPlantSupplies = value;
    }

    /**
     * Gets the value of the maxNumber property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getMaxNumber() {
        return maxNumber;
    }

    /**
     * Sets the value of the maxNumber property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setMaxNumber(Integer value) {
        this.maxNumber = value;
    }

}
