package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for modulesWithVersions complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="modulesWithVersions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="moduleWithVersion" type="{http://bomDbServices.eng.dai/}moduleWithVersion" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "modulesWithVersions", propOrder = {
        "moduleWithVersion"
})
public class ModulesWithVersions {

    @XmlElement(required = true)
    protected List<ModuleWithVersion> moduleWithVersion;

    /**
     * Gets the value of the moduleWithVersion property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the moduleWithVersion property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModuleWithVersion().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ModuleWithVersion }
     */
    public List<ModuleWithVersion> getModuleWithVersion() {
        if (moduleWithVersion == null) {
            moduleWithVersion = new ArrayList<ModuleWithVersion>();
        }
        return this.moduleWithVersion;
    }

}
