package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

public class iPartsWSsalesAreaInformationObject implements RESTfulTransferObjectInterface {

    @JsonProperty
    private String salesAreaType;
    @JsonProperty
    private String salesArea;

    public String getSalesAreaType() {
        return salesAreaType;
    }

    public void setSalesAreaType(String salesAreaType) {
        this.salesAreaType = salesAreaType;
    }

    public String getSalesArea() {
        return salesArea;
    }

    public void setSalesArea(String salesArea) {
        this.salesArea = salesArea;
    }
}
