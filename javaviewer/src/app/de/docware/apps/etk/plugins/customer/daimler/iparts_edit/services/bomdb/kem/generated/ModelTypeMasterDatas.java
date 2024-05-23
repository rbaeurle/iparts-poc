package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for modelTypeMasterDatas complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="modelTypeMasterDatas">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modelTypeMasterData" type="{http://bomDbServices.eng.dai/}modelTypeMasterData" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "modelTypeMasterDatas", propOrder = {
        "modelTypeMasterData"
})
public class ModelTypeMasterDatas {

    protected List<ModelTypeMasterData> modelTypeMasterData;

    /**
     * Gets the value of the modelTypeMasterData property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modelTypeMasterData property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModelTypeMasterData().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ModelTypeMasterData }
     */
    public List<ModelTypeMasterData> getModelTypeMasterData() {
        if (modelTypeMasterData == null) {
            modelTypeMasterData = new ArrayList<ModelTypeMasterData>();
        }
        return this.modelTypeMasterData;
    }

}
