package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getT43RLIPAResult complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getT43RLIPAResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryHead" type="{http://bomDbServices.eng.dai/}queryHead" minOccurs="0"/>
 *         &lt;element name="packageMasterDatas" type="{http://bomDbServices.eng.dai/}packageMasterDatas" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getT43RLIPAResult", propOrder = {
        "queryHead",
        "packageMasterDatas"
})
public class GetT43RLIPAResult {

    protected QueryHead queryHead;
    protected PackageMasterDatas packageMasterDatas;

    /**
     * Gets the value of the queryHead property.
     *
     * @return possible object is
     * {@link QueryHead }
     */
    public QueryHead getQueryHead() {
        return queryHead;
    }

    /**
     * Sets the value of the queryHead property.
     *
     * @param value allowed object is
     *              {@link QueryHead }
     */
    public void setQueryHead(QueryHead value) {
        this.queryHead = value;
    }

    /**
     * Gets the value of the packageMasterDatas property.
     *
     * @return possible object is
     * {@link PackageMasterDatas }
     */
    public PackageMasterDatas getPackageMasterDatas() {
        return packageMasterDatas;
    }

    /**
     * Sets the value of the packageMasterDatas property.
     *
     * @param value allowed object is
     *              {@link PackageMasterDatas }
     */
    public void setPackageMasterDatas(PackageMasterDatas value) {
        this.packageMasterDatas = value;
    }

}
