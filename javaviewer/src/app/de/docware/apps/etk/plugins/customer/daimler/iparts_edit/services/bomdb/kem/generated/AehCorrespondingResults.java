package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for aehCorrespondingResults complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="aehCorrespondingResults">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="rfmeResults" type="{http://bomDbServices.eng.dai/}rfmeResults" minOccurs="0"/>
 *         &lt;element name="wdPrognoseResults" type="{http://bomDbServices.eng.dai/}wdPrognoseResults" minOccurs="0"/>
 *         &lt;element name="aTextResults" type="{http://bomDbServices.eng.dai/}aTextResults" minOccurs="0"/>
 *         &lt;element name="changeNotices" type="{http://bomDbServices.eng.dai/}changeNotices" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "aehCorrespondingResults", propOrder = {
        "rfmeResults",
        "wdPrognoseResults",
        "aTextResults",
        "changeNotices"
})
public class AehCorrespondingResults {

    protected RfmeResults rfmeResults;
    protected WdPrognoseResults wdPrognoseResults;
    protected ATextResults aTextResults;
    protected ChangeNotices changeNotices;

    /**
     * Gets the value of the rfmeResults property.
     *
     * @return possible object is
     * {@link RfmeResults }
     */
    public RfmeResults getRfmeResults() {
        return rfmeResults;
    }

    /**
     * Sets the value of the rfmeResults property.
     *
     * @param value allowed object is
     *              {@link RfmeResults }
     */
    public void setRfmeResults(RfmeResults value) {
        this.rfmeResults = value;
    }

    /**
     * Gets the value of the wdPrognoseResults property.
     *
     * @return possible object is
     * {@link WdPrognoseResults }
     */
    public WdPrognoseResults getWdPrognoseResults() {
        return wdPrognoseResults;
    }

    /**
     * Sets the value of the wdPrognoseResults property.
     *
     * @param value allowed object is
     *              {@link WdPrognoseResults }
     */
    public void setWdPrognoseResults(WdPrognoseResults value) {
        this.wdPrognoseResults = value;
    }

    /**
     * Gets the value of the aTextResults property.
     *
     * @return possible object is
     * {@link ATextResults }
     */
    public ATextResults getATextResults() {
        return aTextResults;
    }

    /**
     * Sets the value of the aTextResults property.
     *
     * @param value allowed object is
     *              {@link ATextResults }
     */
    public void setATextResults(ATextResults value) {
        this.aTextResults = value;
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
