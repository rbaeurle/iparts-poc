package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for plantSupplies complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="plantSupplies">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="plantSupply" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="plantSupplyExplanation" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "plantSupplies", propOrder = {
        "plantSupply",
        "plantSupplyExplanation"
})
public class PlantSupplies {

    @XmlElement(nillable = true)
    protected List<String> plantSupply;
    @XmlElement(nillable = true)
    protected List<String> plantSupplyExplanation;

    /**
     * Gets the value of the plantSupply property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the plantSupply property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlantSupply().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getPlantSupply() {
        if (plantSupply == null) {
            plantSupply = new ArrayList<String>();
        }
        return this.plantSupply;
    }

    /**
     * Gets the value of the plantSupplyExplanation property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the plantSupplyExplanation property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlantSupplyExplanation().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getPlantSupplyExplanation() {
        if (plantSupplyExplanation == null) {
            plantSupplyExplanation = new ArrayList<String>();
        }
        return this.plantSupplyExplanation;
    }

}
