package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for partSpecialMasterDatas complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partSpecialMasterDatas">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="partSpecialMasterData" type="{http://bomDbServices.eng.dai/}partSpecialMasterData" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partSpecialMasterDatas", propOrder = {
        "partSpecialMasterData"
})
public class PartSpecialMasterDatas {

    protected List<PartSpecialMasterData> partSpecialMasterData;

    /**
     * Gets the value of the partSpecialMasterData property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the partSpecialMasterData property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPartSpecialMasterData().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PartSpecialMasterData }
     */
    public List<PartSpecialMasterData> getPartSpecialMasterData() {
        if (partSpecialMasterData == null) {
            partSpecialMasterData = new ArrayList<PartSpecialMasterData>();
        }
        return this.partSpecialMasterData;
    }

}
