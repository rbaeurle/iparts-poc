package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for codeStructure complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="codeStructure">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="b80ScopePositions" type="{http://bomDbServices.eng.dai/}b80ScopePositions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "codeStructure", propOrder = {
        "code",
        "b80ScopePositions"
})
public class CodeStructure {

    protected String code;
    protected B80ScopePositions b80ScopePositions;

    /**
     * Gets the value of the code property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Gets the value of the b80ScopePositions property.
     *
     * @return possible object is
     * {@link B80ScopePositions }
     */
    public B80ScopePositions getB80ScopePositions() {
        return b80ScopePositions;
    }

    /**
     * Sets the value of the b80ScopePositions property.
     *
     * @param value allowed object is
     *              {@link B80ScopePositions }
     */
    public void setB80ScopePositions(B80ScopePositions value) {
        this.b80ScopePositions = value;
    }

}
