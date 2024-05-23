package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for matNSpecs complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="matNSpecs">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="materialAndSpecification" type="{http://bomDbServices.eng.dai/}matNSpec" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "matNSpecs", propOrder = {
        "materialAndSpecification"
})
public class MatNSpecs {

    @XmlElement(nillable = true)
    protected List<MatNSpec> materialAndSpecification;

    /**
     * Gets the value of the materialAndSpecification property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the materialAndSpecification property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMaterialAndSpecification().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MatNSpec }
     */
    public List<MatNSpec> getMaterialAndSpecification() {
        if (materialAndSpecification == null) {
            materialAndSpecification = new ArrayList<MatNSpec>();
        }
        return this.materialAndSpecification;
    }

}
