package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for partMasterDataCompletes complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partMasterDataCompletes">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="partMasterDataComplete" type="{http://bomDbServices.eng.dai/}partMasterDataComplete" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partMasterDataCompletes", propOrder = {
        "partMasterDataComplete"
})
public class PartMasterDataCompletes {

    @XmlElement(nillable = true)
    protected List<PartMasterDataComplete> partMasterDataComplete;

    /**
     * Gets the value of the partMasterDataComplete property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the partMasterDataComplete property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPartMasterDataComplete().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PartMasterDataComplete }
     */
    public List<PartMasterDataComplete> getPartMasterDataComplete() {
        if (partMasterDataComplete == null) {
            partMasterDataComplete = new ArrayList<PartMasterDataComplete>();
        }
        return this.partMasterDataComplete;
    }

}
