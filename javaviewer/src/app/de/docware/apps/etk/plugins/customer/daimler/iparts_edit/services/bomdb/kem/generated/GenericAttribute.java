package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for genericAttribute complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="genericAttribute">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="attName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="operation" type="{http://bomDbServices.eng.dai/}dbOp" />
 *       &lt;attribute name="attValue" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "genericAttribute")
public class GenericAttribute {

    @XmlAttribute(name = "attName")
    protected String attName;
    @XmlAttribute(name = "operation")
    protected DbOp operation;
    @XmlAttribute(name = "attValue")
    protected String attValue;

    /**
     * Gets the value of the attName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAttName() {
        return attName;
    }

    /**
     * Sets the value of the attName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAttName(String value) {
        this.attName = value;
    }

    /**
     * Gets the value of the operation property.
     *
     * @return possible object is
     * {@link DbOp }
     */
    public DbOp getOperation() {
        return operation;
    }

    /**
     * Sets the value of the operation property.
     *
     * @param value allowed object is
     *              {@link DbOp }
     */
    public void setOperation(DbOp value) {
        this.operation = value;
    }

    /**
     * Gets the value of the attValue property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAttValue() {
        return attValue;
    }

    /**
     * Sets the value of the attValue property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAttValue(String value) {
        this.attValue = value;
    }

}
