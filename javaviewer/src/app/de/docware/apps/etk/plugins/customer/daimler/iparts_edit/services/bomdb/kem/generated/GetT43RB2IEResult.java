package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RB2IEResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RB2IEResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="b90InstallationLocations" type="{http://bomDbServices.eng.dai/}b90InstallationLocations" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RB2IEResult", propOrder = {
        "queryHead",
        "b90InstallationLocations"
})
public class GetT43RB2IEResult {

    protected QueryHead queryHead;
    protected B90InstallationLocations b90InstallationLocations;

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
     * Gets the value of the b90InstallationLocations property.
     *
     * @return possible object is
     * {@link B90InstallationLocations }
     */
    public B90InstallationLocations getB90InstallationLocations() {
        return b90InstallationLocations;
    }

    /**
     * Sets the value of the b90InstallationLocations property.
     *
     * @param value allowed object is
     *              {@link B90InstallationLocations }
     */
    public void setB90InstallationLocations(B90InstallationLocations value) {
        this.b90InstallationLocations = value;
    }

}
