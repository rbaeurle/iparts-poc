/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbSpec;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

public class iPartsMbSpecData implements RESTfulTransferObjectInterface {

    @JsonProperty
    private String spec;
    @JsonProperty
    private String quantity;
    @JsonProperty
    private String saeClass;

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getSaeClass() {
        return saeClass;
    }

    public void setSaeClass(String saeClass) {
        this.saeClass = saeClass;
    }
}
