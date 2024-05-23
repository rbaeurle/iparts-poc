package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for modelMasterDatas complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="modelMasterDatas">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modelMasterData" type="{http://bomDbServices.eng.dai/}modelMasterData" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "modelMasterDatas", propOrder = {
        "modelMasterData"
})
public class ModelMasterDatas {

    protected List<ModelMasterData> modelMasterData;

    /**
     * Gets the value of the modelMasterData property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modelMasterData property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModelMasterData().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ModelMasterData }
     */
    public List<ModelMasterData> getModelMasterData() {
        if (modelMasterData == null) {
            modelMasterData = new ArrayList<ModelMasterData>();
        }
        return this.modelMasterData;
    }

}
