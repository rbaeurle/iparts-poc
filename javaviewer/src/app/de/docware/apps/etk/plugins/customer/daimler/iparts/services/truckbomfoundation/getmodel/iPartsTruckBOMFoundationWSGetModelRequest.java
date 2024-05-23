/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getmodel;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Request für den GetModel Webservice der TruckBOM.foundation
 */
public class iPartsTruckBOMFoundationWSGetModelRequest extends AbstractiPartsTruckBOMFoundationWebserviceRequest {

    private List<ModelRequestParameter> modelRequestParameter;

    public iPartsTruckBOMFoundationWSGetModelRequest() {
    }

    public iPartsTruckBOMFoundationWSGetModelRequest(String identifier) {
        List<ModelRequestParameter> requestParameter = new ArrayList<>(1);
        requestParameter.add(new ModelRequestParameter(identifier));
        setModelRequestParameter(requestParameter);
    }

    public List<ModelRequestParameter> getModelRequestParameter() {
        return modelRequestParameter;
    }

    public void setModelRequestParameter(List<ModelRequestParameter> modelRequestParameter) {
        this.modelRequestParameter = modelRequestParameter;
    }


    public static class ModelRequestParameter implements RESTfulTransferObjectInterface {

        private String identifier;

        public ModelRequestParameter() {
        }

        public ModelRequestParameter(String identifier) {
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
