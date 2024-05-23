package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for s05AssemblyStructureML complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="s05AssemblyStructureML">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="object" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="partsListMasterDataComplete" type="{http://bomDbServices.eng.dai/}partsListMasterDataComplete" minOccurs="0"/>
 *         &lt;element name="partMasterDataComplete" type="{http://bomDbServices.eng.dai/}partMasterDataComplete" minOccurs="0"/>
 *         &lt;element name="s05PositionMLs" type="{http://bomDbServices.eng.dai/}s05PositionMLs" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s05AssemblyStructureML", propOrder = {
        "object",
        "partsListMasterDataComplete",
        "partMasterDataComplete",
        "s05PositionMLs"
})
public class S05AssemblyStructureML {

    protected String object;
    protected PartsListMasterDataComplete partsListMasterDataComplete;
    protected PartMasterDataComplete partMasterDataComplete;
    protected S05PositionMLs s05PositionMLs;

    /**
     * Gets the value of the object property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getObject() {
        return object;
    }

    /**
     * Sets the value of the object property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setObject(String value) {
        this.object = value;
    }

    /**
     * Gets the value of the partsListMasterDataComplete property.
     *
     * @return possible object is
     * {@link PartsListMasterDataComplete }
     */
    public PartsListMasterDataComplete getPartsListMasterDataComplete() {
        return partsListMasterDataComplete;
    }

    /**
     * Sets the value of the partsListMasterDataComplete property.
     *
     * @param value allowed object is
     *              {@link PartsListMasterDataComplete }
     */
    public void setPartsListMasterDataComplete(PartsListMasterDataComplete value) {
        this.partsListMasterDataComplete = value;
    }

    /**
     * Gets the value of the partMasterDataComplete property.
     *
     * @return possible object is
     * {@link PartMasterDataComplete }
     */
    public PartMasterDataComplete getPartMasterDataComplete() {
        return partMasterDataComplete;
    }

    /**
     * Sets the value of the partMasterDataComplete property.
     *
     * @param value allowed object is
     *              {@link PartMasterDataComplete }
     */
    public void setPartMasterDataComplete(PartMasterDataComplete value) {
        this.partMasterDataComplete = value;
    }

    /**
     * Gets the value of the s05PositionMLs property.
     *
     * @return possible object is
     * {@link S05PositionMLs }
     */
    public S05PositionMLs getS05PositionMLs() {
        return s05PositionMLs;
    }

    /**
     * Sets the value of the s05PositionMLs property.
     *
     * @param value allowed object is
     *              {@link S05PositionMLs }
     */
    public void setS05PositionMLs(S05PositionMLs value) {
        this.s05PositionMLs = value;
    }

}
