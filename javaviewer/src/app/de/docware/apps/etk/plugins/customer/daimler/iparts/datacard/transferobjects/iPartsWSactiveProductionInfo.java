/*
 * Copyright (c) 2018 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

public class iPartsWSactiveProductionInfo implements RESTfulTransferObjectInterface {

    private String originCabId;

    public String getOriginCabId() {
        return originCabId;
    }

    public void setOriginCabId(String originCabId) {
        this.originCabId = originCabId;
    }
}
