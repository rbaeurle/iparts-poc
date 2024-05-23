/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getpart;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Request für den GetPart Webservice der TruckBOM.foundation
 */
public class iPartsTruckBOMFoundationWSGetPartRequest extends AbstractiPartsTruckBOMFoundationWebserviceRequest {

    private List<PartRequestParameter> partRequestParameter;

    public iPartsTruckBOMFoundationWSGetPartRequest() {
    }

    public iPartsTruckBOMFoundationWSGetPartRequest(String identifier) {
        List<PartRequestParameter> requestParameter = new ArrayList<>(1);
        requestParameter.add(new PartRequestParameter(identifier));
        setPartRequestParameter(requestParameter);
        // Default bei der Anfrage ist "true"
        setWithDistributionTask(true);
    }

    public List<PartRequestParameter> getPartRequestParameter() {
        return partRequestParameter;
    }

    public void setPartRequestParameter(List<PartRequestParameter> partRequestParameter) {
        this.partRequestParameter = partRequestParameter;
    }

    public static class PartRequestParameter implements RESTfulTransferObjectInterface {

        private String identifier;

        public PartRequestParameter() {
        }

        public PartRequestParameter(String identifier) {
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