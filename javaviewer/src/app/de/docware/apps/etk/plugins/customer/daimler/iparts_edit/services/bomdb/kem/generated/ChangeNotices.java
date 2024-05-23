package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for changeNotices complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="changeNotices">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="changeNotice" type="{http://bomDbServices.eng.dai/}changeNotice" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "changeNotices", propOrder = {
        "changeNotice"
})
public class ChangeNotices {

    @XmlElement(nillable = true)
    protected List<ChangeNotice> changeNotice;

    /**
     * Gets the value of the changeNotice property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the changeNotice property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChangeNotice().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ChangeNotice }
     */
    public List<ChangeNotice> getChangeNotice() {
        if (changeNotice == null) {
            changeNotice = new ArrayList<ChangeNotice>();
        }
        return this.changeNotice;
    }

}
