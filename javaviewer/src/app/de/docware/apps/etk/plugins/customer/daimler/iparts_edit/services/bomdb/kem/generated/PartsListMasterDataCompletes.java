package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for partsListMasterDataCompletes complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partsListMasterDataCompletes">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="partsListMasterDataComplete" type="{http://bomDbServices.eng.dai/}partsListMasterDataComplete" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partsListMasterDataCompletes", propOrder = {
        "partsListMasterDataComplete"
})
public class PartsListMasterDataCompletes {

    @XmlElement(nillable = true)
    protected List<PartsListMasterDataComplete> partsListMasterDataComplete;

    /**
     * Gets the value of the partsListMasterDataComplete property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the partsListMasterDataComplete property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPartsListMasterDataComplete().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PartsListMasterDataComplete }
     */
    public List<PartsListMasterDataComplete> getPartsListMasterDataComplete() {
        if (partsListMasterDataComplete == null) {
            partsListMasterDataComplete = new ArrayList<PartsListMasterDataComplete>();
        }
        return this.partsListMasterDataComplete;
    }

}
