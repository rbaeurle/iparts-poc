package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for partWeightDatas complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partWeightDatas">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="partWeightData" type="{http://bomDbServices.eng.dai/}partWeightData" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partWeightDatas", propOrder = {
        "partWeightData"
})
public class PartWeightDatas {

    protected List<PartWeightData> partWeightData;

    /**
     * Gets the value of the partWeightData property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the partWeightData property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPartWeightData().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PartWeightData }
     */
    public List<PartWeightData> getPartWeightData() {
        if (partWeightData == null) {
            partWeightData = new ArrayList<PartWeightData>();
        }
        return this.partWeightData;
    }

}
