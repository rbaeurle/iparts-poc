package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for s05PositionML complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05PositionML">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="s05ItemUsageML" type="{http://bomDbServices.eng.dai/}s05ItemUsageML" minOccurs="0"/>
 *         &lt;element name="s05PointOfUsageTextMLs" type="{http://bomDbServices.eng.dai/}s05PointOfUsageTextMLs" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05PositionML", propOrder = {
        "s05ItemUsageML",
        "s05PointOfUsageTextMLs"
})
public class S05PositionML {

    protected S05ItemUsageML s05ItemUsageML;
    protected S05PointOfUsageTextMLs s05PointOfUsageTextMLs;

    /**
     * Gets the value of the s05ItemUsageML property.
     *
     * @return possible object is
     * {@link S05ItemUsageML }
     */
    public S05ItemUsageML getS05ItemUsageML() {
        return s05ItemUsageML;
    }

    /**
     * Sets the value of the s05ItemUsageML property.
     *
     * @param value allowed object is
     *              {@link S05ItemUsageML }
     */
    public void setS05ItemUsageML(S05ItemUsageML value) {
        this.s05ItemUsageML = value;
    }

    /**
     * Gets the value of the s05PointOfUsageTextMLs property.
     *
     * @return possible object is
     * {@link S05PointOfUsageTextMLs }
     */
    public S05PointOfUsageTextMLs getS05PointOfUsageTextMLs() {
        return s05PointOfUsageTextMLs;
    }

    /**
     * Sets the value of the s05PointOfUsageTextMLs property.
     *
     * @param value allowed object is
     *              {@link S05PointOfUsageTextMLs }
     */
    public void setS05PointOfUsageTextMLs(S05PointOfUsageTextMLs value) {
        this.s05PointOfUsageTextMLs = value;
    }

}
