package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for s05PositionMLs complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05PositionMLs">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="s05PositionML" type="{http://bomDbServices.eng.dai/}s05PositionML" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05PositionMLs", propOrder = {
        "s05PositionML"
})
public class S05PositionMLs {

    protected List<S05PositionML> s05PositionML;

    /**
     * Gets the value of the s05PositionML property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the s05PositionML property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getS05PositionML().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link S05PositionML }
     */
    public List<S05PositionML> getS05PositionML() {
        if (s05PositionML == null) {
            s05PositionML = new ArrayList<S05PositionML>();
        }
        return this.s05PositionML;
    }

}
