/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getsubmodulecategory;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Request für den GetSubModuleCategory Webservice der TruckBOM.foundation
 */
public class iPartsTruckBOMFoundationWSGetSubModuleCategoryRequest extends AbstractiPartsTruckBOMFoundationWebserviceRequest {

    private List<SubModuleCategoryRequestParameter> subModuleCategoryRequestParameter;

    public iPartsTruckBOMFoundationWSGetSubModuleCategoryRequest() {
    }

    public iPartsTruckBOMFoundationWSGetSubModuleCategoryRequest(String identifier) {
        List<SubModuleCategoryRequestParameter> subModuleCategoryRequestParameter = new ArrayList<>(1);
        subModuleCategoryRequestParameter.add(new SubModuleCategoryRequestParameter(identifier));
        setSubModuleCategoryRequestParameter(subModuleCategoryRequestParameter);
    }

    public List<SubModuleCategoryRequestParameter> getSubModuleCategoryRequestParameter() {
        return subModuleCategoryRequestParameter;
    }

    public void setSubModuleCategoryRequestParameter(List<SubModuleCategoryRequestParameter> subModuleCategoryRequestParameter) {
        this.subModuleCategoryRequestParameter = subModuleCategoryRequestParameter;
    }

    public static class SubModuleCategoryRequestParameter implements RESTfulTransferObjectInterface {

        private String identifier;

        public SubModuleCategoryRequestParameter() {
        }

        public SubModuleCategoryRequestParameter(String identifier) {
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
