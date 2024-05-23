package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for changedPosition complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="changedPosition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="position" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="pointOfUsageTextPosition" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="changedVersions" type="{http://bomDbServices.eng.dai/}changedVersions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "changedPosition", propOrder = {
        "position",
        "pointOfUsageTextPosition",
        "changedVersions"
})
public class ChangedPosition {

    protected Integer position;
    protected boolean pointOfUsageTextPosition;
    protected ChangedVersions changedVersions;

    /**
     * Gets the value of the position property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setPosition(Integer value) {
        this.position = value;
    }

    /**
     * Gets the value of the pointOfUsageTextPosition property.
     */
    public boolean isPointOfUsageTextPosition() {
        return pointOfUsageTextPosition;
    }

    /**
     * Sets the value of the pointOfUsageTextPosition property.
     */
    public void setPointOfUsageTextPosition(boolean value) {
        this.pointOfUsageTextPosition = value;
    }

    /**
     * Gets the value of the changedVersions property.
     *
     * @return possible object is
     * {@link ChangedVersions }
     */
    public ChangedVersions getChangedVersions() {
        return changedVersions;
    }

    /**
     * Sets the value of the changedVersions property.
     *
     * @param value allowed object is
     *              {@link ChangedVersions }
     */
    public void setChangedVersions(ChangedVersions value) {
        this.changedVersions = value;
    }

}
