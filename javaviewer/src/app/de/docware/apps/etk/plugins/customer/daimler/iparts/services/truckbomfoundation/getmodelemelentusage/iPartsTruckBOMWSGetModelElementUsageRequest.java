/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getmodelemelentusage;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Request für den GetModelElementUsage Webservice der TruckBOM.foundation
 */
public class iPartsTruckBOMWSGetModelElementUsageRequest extends AbstractiPartsTruckBOMFoundationWebserviceRequest {

    private List<ModelElementUsageRequestParameter> modelElementUsageRequestParameter;

    public iPartsTruckBOMWSGetModelElementUsageRequest() {

    }

    public iPartsTruckBOMWSGetModelElementUsageRequest(String identifier) {
        List<ModelElementUsageRequestParameter> requestParameter = new ArrayList<>(1);
        requestParameter.add(new ModelElementUsageRequestParameter(identifier));
        setModelElementUsageRequestParameter(requestParameter);
        // Default bei der Anfrage ist "true"
        setWithDistributionTask(true);
    }

    public List<ModelElementUsageRequestParameter> getModelElementUsageRequestParameter() {
        return modelElementUsageRequestParameter;
    }

    public void setModelElementUsageRequestParameter(List<ModelElementUsageRequestParameter> modelElementUsageRequestParameter) {
        this.modelElementUsageRequestParameter = modelElementUsageRequestParameter;
    }

    public static class ModelElementUsageRequestParameter implements RESTfulTransferObjectInterface {

        private String modelIdentifier;

        public ModelElementUsageRequestParameter() {
        }

        public ModelElementUsageRequestParameter(String modelIdentifier) {
            this.modelIdentifier = modelIdentifier;
        }

        public String getModelIdentifier() {
            return modelIdentifier;
        }

        public void setModelIdentifier(String modelIdentifier) {
            this.modelIdentifier = modelIdentifier;
        }
    }
}
