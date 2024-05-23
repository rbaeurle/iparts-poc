package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for changeNoticeText complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="changeNoticeText">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="changeNoticeLanguage" type="{http://bomDbServices.eng.dai/}language" minOccurs="0"/>
 *         &lt;element name="changeNoticeTextRow1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="changeNoticeTextRow2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="changeNoticeTextRow3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "changeNoticeText", propOrder = {
        "changeNoticeLanguage",
        "changeNoticeTextRow1",
        "changeNoticeTextRow2",
        "changeNoticeTextRow3"
})
public class ChangeNoticeText {

    protected Language changeNoticeLanguage;
    protected String changeNoticeTextRow1;
    protected String changeNoticeTextRow2;
    protected String changeNoticeTextRow3;

    /**
     * Gets the value of the changeNoticeLanguage property.
     *
     * @return possible object is
     * {@link Language }
     */
    public Language getChangeNoticeLanguage() {
        return changeNoticeLanguage;
    }

    /**
     * Sets the value of the changeNoticeLanguage property.
     *
     * @param value allowed object is
     *              {@link Language }
     */
    public void setChangeNoticeLanguage(Language value) {
        this.changeNoticeLanguage = value;
    }

    /**
     * Gets the value of the changeNoticeTextRow1 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getChangeNoticeTextRow1() {
        return changeNoticeTextRow1;
    }

    /**
     * Sets the value of the changeNoticeTextRow1 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setChangeNoticeTextRow1(String value) {
        this.changeNoticeTextRow1 = value;
    }

    /**
     * Gets the value of the changeNoticeTextRow2 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getChangeNoticeTextRow2() {
        return changeNoticeTextRow2;
    }

    /**
     * Sets the value of the changeNoticeTextRow2 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setChangeNoticeTextRow2(String value) {
        this.changeNoticeTextRow2 = value;
    }

    /**
     * Gets the value of the changeNoticeTextRow3 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getChangeNoticeTextRow3() {
        return changeNoticeTextRow3;
    }

    /**
     * Sets the value of the changeNoticeTextRow3 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setChangeNoticeTextRow3(String value) {
        this.changeNoticeTextRow3 = value;
    }

}
