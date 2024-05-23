package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for s05Remarks complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05Remarks">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="s05Remark" type="{http://bomDbServices.eng.dai/}s05Remark" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05Remarks", propOrder = {
        "s05Remark"
})
public class S05Remarks {

    @XmlElement(nillable = true)
    protected List<S05Remark> s05Remark;

    /**
     * Gets the value of the s05Remark property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the s05Remark property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getS05Remark().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link S05Remark }
     */
    public List<S05Remark> getS05Remark() {
        if (s05Remark == null) {
            s05Remark = new ArrayList<S05Remark>();
        }
        return this.s05Remark;
    }

}
