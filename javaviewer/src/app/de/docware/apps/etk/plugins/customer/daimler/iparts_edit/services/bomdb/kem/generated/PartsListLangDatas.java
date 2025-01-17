package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for partsListLangDatas complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="partsListLangDatas">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="partsListLangData" type="{http://bomDbServices.eng.dai/}partsListLangData" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "partsListLangDatas", propOrder = {
        "partsListLangData"
})
public class PartsListLangDatas {

    @XmlElement(nillable = true)
    protected List<PartsListLangData> partsListLangData;

    /**
     * Gets the value of the partsListLangData property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the partsListLangData property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPartsListLangData().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PartsListLangData }
     */
    public List<PartsListLangData> getPartsListLangData() {
        if (partsListLangData == null) {
            partsListLangData = new ArrayList<PartsListLangData>();
        }
        return this.partsListLangData;
    }

}
