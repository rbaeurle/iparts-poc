package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for f14BasicDatas complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="f14BasicDatas">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="f14BasicData" type="{http://bomDbServices.eng.dai/}f14BasicData" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "f14BasicDatas", propOrder = {
        "f14BasicData"
})
public class F14BasicDatas {

    protected List<F14BasicData> f14BasicData;

    /**
     * Gets the value of the f14BasicData property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the f14BasicData property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getF14BasicData().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link F14BasicData }
     */
    public List<F14BasicData> getF14BasicData() {
        if (f14BasicData == null) {
            f14BasicData = new ArrayList<F14BasicData>();
        }
        return this.f14BasicData;
    }

}
