/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getmodeltype;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Request für den GetModelType Webservice der TruckBOM.foundation
 */
public class iPartsTruckBOMFoundationWSGetModelTypeRequest extends AbstractiPartsTruckBOMFoundationWebserviceRequest {

    private List<ModelTypeRequestParameter> modelTypeRequestParameter;

    public iPartsTruckBOMFoundationWSGetModelTypeRequest() {
    }

    public iPartsTruckBOMFoundationWSGetModelTypeRequest(String identifier) {
        List<ModelTypeRequestParameter> requestParameter = new ArrayList<>(1);
        requestParameter.add(new ModelTypeRequestParameter(identifier));
        setPartRequestParameter(requestParameter);
    }

    public List<ModelTypeRequestParameter> getPartRequestParameter() {
        return modelTypeRequestParameter;
    }

    public void setPartRequestParameter(List<ModelTypeRequestParameter> partRequestParameter) {
        this.modelTypeRequestParameter = partRequestParameter;
    }


    public static class ModelTypeRequestParameter implements RESTfulTransferObjectInterface {

        private String identifier;

        public ModelTypeRequestParameter() {
        }

        public ModelTypeRequestParameter(String identifier) {
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
