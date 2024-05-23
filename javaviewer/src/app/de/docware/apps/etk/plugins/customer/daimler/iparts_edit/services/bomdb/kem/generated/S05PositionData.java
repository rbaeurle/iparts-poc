package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for s05PositionData complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05PositionData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requestedVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="s05" type="{http://bomDbServices.eng.dai/}s05" minOccurs="0"/>
 *         &lt;element name="s05TighteningTorques" type="{http://bomDbServices.eng.dai/}s05TighteningTorques" minOccurs="0"/>
 *         &lt;element name="s05Alternative" type="{http://bomDbServices.eng.dai/}s05Alternative" minOccurs="0"/>
 *         &lt;element name="s05AlternativeTogetherWiths" type="{http://bomDbServices.eng.dai/}s05AltTogetherWiths" minOccurs="0"/>
 *         &lt;element name="s05PointOfUsageTexts" type="{http://bomDbServices.eng.dai/}s05PointOfUsageTexts" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05PositionData", propOrder = {
        "requestedVersion",
        "s05",
        "s05TighteningTorques",
        "s05Alternative",
        "s05AlternativeTogetherWiths",
        "s05PointOfUsageTexts"
})
public class S05PositionData {

    protected Integer requestedVersion;
    protected S05 s05;
    protected S05TighteningTorques s05TighteningTorques;
    protected S05Alternative s05Alternative;
    protected S05AltTogetherWiths s05AlternativeTogetherWiths;
    protected S05PointOfUsageTexts s05PointOfUsageTexts;

    /**
     * Gets the value of the requestedVersion property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getRequestedVersion() {
        return requestedVersion;
    }

    /**
     * Sets the value of the requestedVersion property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setRequestedVersion(Integer value) {
        this.requestedVersion = value;
    }

    /**
     * Gets the value of the s05 property.
     *
     * @return possible object is
     * {@link S05 }
     */
    public S05 getS05() {
        return s05;
    }

    /**
     * Sets the value of the s05 property.
     *
     * @param value allowed object is
     *              {@link S05 }
     */
    public void setS05(S05 value) {
        this.s05 = value;
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

    /**
     * Gets the value of the s05Alternative property.
     *
     * @return possible object is
     * {@link S05Alternative }
     */
    public S05Alternative getS05Alternative() {
        return s05Alternative;
    }

    /**
     * Sets the value of the s05Alternative property.
     *
     * @param value allowed object is
     *              {@link S05Alternative }
     */
    public void setS05Alternative(S05Alternative value) {
        this.s05Alternative = value;
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
