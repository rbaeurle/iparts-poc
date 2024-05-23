package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for s05AssemblyStructure complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05AssemblyStructure">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="partsList" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="s05Positions" type="{http://bomDbServices.eng.dai/}s05Positions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05AssemblyStructure", propOrder = {
        "partsList",
        "s05Positions"
})
public class S05AssemblyStructure {

    protected String partsList;
    protected S05Positions s05Positions;

    /**
     * Gets the value of the partsList property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPartsList() {
        return partsList;
    }

    /**
     * Sets the value of the partsList property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPartsList(String value) {
        this.partsList = value;
    }

    /**
     * Gets the value of the s05Positions property.
     *
     * @return possible object is
     * {@link S05Positions }
     */
    public S05Positions getS05Positions() {
        return s05Positions;
    }

    /**
     * Sets the value of the s05Positions property.
     *
     * @param value allowed object is
     *              {@link S05Positions }
     */
    public void setS05Positions(S05Positions value) {
        this.s05Positions = value;
    }

}
