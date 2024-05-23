/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Oberstes DTO für eine Aggregate-Datenkarte vom Datenkarten-Webservice.
 */
public class iPartsWSgetAggregateDatacardResponse implements RESTfulTransferObjectInterface {

    private iPartsWSmajAssyRestrictedResponse majAssyRestrictedResponse;

    public iPartsWSgetAggregateDatacardResponse() {
    }

    public iPartsWSmajAssyRestrictedResponse getMajAssyRestrictedResponse() {
        return majAssyRestrictedResponse;
    }

    public void setMajAssyRestrictedResponse(iPartsWSmajAssyRestrictedResponse majAssyRestrictedResponse) {
        this.majAssyRestrictedResponse = majAssyRestrictedResponse;
    }
}
