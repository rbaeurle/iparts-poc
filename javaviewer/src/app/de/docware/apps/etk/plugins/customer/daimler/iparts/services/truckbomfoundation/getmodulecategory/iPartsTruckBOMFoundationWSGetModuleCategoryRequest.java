/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getmodulecategory;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Request für den GetModuleCategory Webservice der TruckBOM.foundation
 */
public class iPartsTruckBOMFoundationWSGetModuleCategoryRequest extends AbstractiPartsTruckBOMFoundationWebserviceRequest {

    private List<ModuleCategoryRequestParameter> moduleCategoryRequestParameter;

    public iPartsTruckBOMFoundationWSGetModuleCategoryRequest() {
    }

    public iPartsTruckBOMFoundationWSGetModuleCategoryRequest(String identifier) {
        List<ModuleCategoryRequestParameter> moduleCategoryRequestParameter = new ArrayList<>(1);
        moduleCategoryRequestParameter.add(new ModuleCategoryRequestParameter(identifier));
        setModuleCategoryRequestParameter(moduleCategoryRequestParameter);
    }

    public List<ModuleCategoryRequestParameter> getModuleCategoryRequestParameter() {
        return moduleCategoryRequestParameter;
    }

    public void setModuleCategoryRequestParameter(List<ModuleCategoryRequestParameter> moduleCategoryRequestParameter) {
        this.moduleCategoryRequestParameter = moduleCategoryRequestParameter;
    }

    public static class ModuleCategoryRequestParameter implements RESTfulTransferObjectInterface {

        private String identifier;

        public ModuleCategoryRequestParameter() {
        }

        public ModuleCategoryRequestParameter(String identifier) {
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
