package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for partAdditionalMasterDatas complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partAdditionalMasterDatas">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="partAdditionalMasterData" type="{http://bomDbServices.eng.dai/}partAdditionalMasterData" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partAdditionalMasterDatas", propOrder = {
        "partAdditionalMasterData"
})
public class PartAdditionalMasterDatas {

    protected List<PartAdditionalMasterData> partAdditionalMasterData;

    /**
     * Gets the value of the partAdditionalMasterData property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the partAdditionalMasterData property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPartAdditionalMasterData().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PartAdditionalMasterData }
     */
    public List<PartAdditionalMasterData> getPartAdditionalMasterData() {
        if (partAdditionalMasterData == null) {
            partAdditionalMasterData = new ArrayList<PartAdditionalMasterData>();
        }
        return this.partAdditionalMasterData;
    }

}
