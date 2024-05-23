package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for s05RemarkTexts complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05RemarkTexts">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="s05RemarkText" type="{http://bomDbServices.eng.dai/}s05RemarkText" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05RemarkTexts", propOrder = {
        "s05RemarkText"
})
public class S05RemarkTexts {

    protected List<S05RemarkText> s05RemarkText;

    /**
     * Gets the value of the s05RemarkText property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the s05RemarkText property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getS05RemarkText().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link S05RemarkText }
     */
    public List<S05RemarkText> getS05RemarkText() {
        if (s05RemarkText == null) {
            s05RemarkText = new ArrayList<S05RemarkText>();
        }
        return this.s05RemarkText;
    }

}
