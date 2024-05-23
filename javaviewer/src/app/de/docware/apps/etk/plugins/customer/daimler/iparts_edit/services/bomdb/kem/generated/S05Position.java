package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for s05Position complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05Position">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="position" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="positionDatas" type="{http://bomDbServices.eng.dai/}s05PositionDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05Position", propOrder = {
        "position",
        "positionDatas"
})
public class S05Position {

    protected int position;
    protected S05PositionDatas positionDatas;

    /**
     * Gets the value of the position property.
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     */
    public void setPosition(int value) {
        this.position = value;
    }

    /**
     * Gets the value of the positionDatas property.
     *
     * @return possible object is
     * {@link S05PositionDatas }
     */
    public S05PositionDatas getPositionDatas() {
        return positionDatas;
    }

    /**
     * Sets the value of the positionDatas property.
     *
     * @param value allowed object is
     *              {@link S05PositionDatas }
     */
    public void setPositionDatas(S05PositionDatas value) {
        this.positionDatas = value;
    }

}
