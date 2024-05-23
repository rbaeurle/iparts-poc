package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for b80ScopePosition complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b80ScopePosition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="scope" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="position" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="positionDatas" type="{http://bomDbServices.eng.dai/}b80PositionDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b80ScopePosition", propOrder = {
        "scope",
        "position",
        "positionDatas"
})
public class B80ScopePosition {

    protected String scope;
    protected int position;
    protected B80PositionDatas positionDatas;

    /**
     * Gets the value of the scope property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setScope(String value) {
        this.scope = value;
    }

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
     * {@link B80PositionDatas }
     */
    public B80PositionDatas getPositionDatas() {
        return positionDatas;
    }

    /**
     * Sets the value of the positionDatas property.
     *
     * @param value allowed object is
     *              {@link B80PositionDatas }
     */
    public void setPositionDatas(B80PositionDatas value) {
        this.positionDatas = value;
    }

}
