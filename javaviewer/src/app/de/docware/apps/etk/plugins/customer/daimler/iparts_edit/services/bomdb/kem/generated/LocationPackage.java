package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for locationPackage complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="locationPackage">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="package" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="packageVersion" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "locationPackage", propOrder = {
        "_package",
        "packageVersion"
})
public class LocationPackage {

    @XmlElement(name = "package", required = true)
    protected String _package;
    protected Integer packageVersion;

    /**
     * Gets the value of the package property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPackage() {
        return _package;
    }

    /**
     * Sets the value of the package property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPackage(String value) {
        this._package = value;
    }

    /**
     * Gets the value of the packageVersion property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getPackageVersion() {
        return packageVersion;
    }

    /**
     * Sets the value of the packageVersion property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setPackageVersion(Integer value) {
        this.packageVersion = value;
    }

}
