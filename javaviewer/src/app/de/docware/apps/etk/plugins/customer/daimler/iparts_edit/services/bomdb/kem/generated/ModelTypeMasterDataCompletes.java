package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for modelTypeMasterDataCompletes complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="modelTypeMasterDataCompletes">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modelTypeMasterDataComplete" type="{http://bomDbServices.eng.dai/}modelTypeMasterDataComplete" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "modelTypeMasterDataCompletes", propOrder = {
        "modelTypeMasterDataComplete"
})
public class ModelTypeMasterDataCompletes {

    @XmlElement(nillable = true)
    protected List<ModelTypeMasterDataComplete> modelTypeMasterDataComplete;

    /**
     * Gets the value of the modelTypeMasterDataComplete property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modelTypeMasterDataComplete property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModelTypeMasterDataComplete().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ModelTypeMasterDataComplete }
     */
    public List<ModelTypeMasterDataComplete> getModelTypeMasterDataComplete() {
        if (modelTypeMasterDataComplete == null) {
            modelTypeMasterDataComplete = new ArrayList<ModelTypeMasterDataComplete>();
        }
        return this.modelTypeMasterDataComplete;
    }

}
