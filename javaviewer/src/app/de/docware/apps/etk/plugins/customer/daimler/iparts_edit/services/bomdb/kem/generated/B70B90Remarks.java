package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for b70B90Remarks complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="b70B90Remarks">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="b70B90Remark" type="{http://bomDbServices.eng.dai/}b70B90Remark" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "b70B90Remarks", propOrder = {
        "b70B90Remark"
})
public class B70B90Remarks {

    @XmlElement(nillable = true)
    protected List<B70B90Remark> b70B90Remark;

    /**
     * Gets the value of the b70B90Remark property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the b70B90Remark property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getB70B90Remark().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link B70B90Remark }
     */
    public List<B70B90Remark> getB70B90Remark() {
        if (b70B90Remark == null) {
            b70B90Remark = new ArrayList<B70B90Remark>();
        }
        return this.b70B90Remark;
    }

}
