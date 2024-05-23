/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getpartslist;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Request für den GetPartsList Webservice der TruckBOM.foundation
 */
public class iPartsTruckBOMFoundationWSGetPartsListRequest extends AbstractiPartsTruckBOMFoundationWebserviceRequest {

    private List<PartsListRequestParameter> partsListRequestParameter;

    public iPartsTruckBOMFoundationWSGetPartsListRequest() {
    }

    public iPartsTruckBOMFoundationWSGetPartsListRequest(String identifier) {
        List<PartsListRequestParameter> requestParameter = new ArrayList<>(1);
        requestParameter.add(new PartsListRequestParameter(identifier));
        setPartsListRequestParameter(requestParameter);
        // Default bei der Anfrage ist "true"
        setWithDistributionTask(true);
    }

    public List<PartsListRequestParameter> getPartsListRequestParameter() {
        return partsListRequestParameter;
    }

    public void setPartsListRequestParameter(List<PartsListRequestParameter> partsListRequestParameter) {
        this.partsListRequestParameter = partsListRequestParameter;
    }

    public static class PartsListRequestParameter implements RESTfulTransferObjectInterface {

        private String identifier;

        public PartsListRequestParameter() {
        }

        public PartsListRequestParameter(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
    }
}