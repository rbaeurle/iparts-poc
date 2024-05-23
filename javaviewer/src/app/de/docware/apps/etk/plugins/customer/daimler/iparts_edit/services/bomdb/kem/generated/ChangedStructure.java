package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for changedStructure complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="changedStructure">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="partsList" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="changedPositions" type="{http://bomDbServices.eng.dai/}changedPositions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "changedStructure", propOrder = {
        "partsList",
        "changedPositions"
})
public class ChangedStructure {

    protected String partsList;
    protected ChangedPositions changedPositions;

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
     * Gets the value of the changedPositions property.
     *
     * @return possible object is
     * {@link ChangedPositions }
     */
    public ChangedPositions getChangedPositions() {
        return changedPositions;
    }

    /**
     * Sets the value of the changedPositions property.
     *
     * @param value allowed object is
     *              {@link ChangedPositions }
     */
    public void setChangedPositions(ChangedPositions value) {
        this.changedPositions = value;
    }

}
