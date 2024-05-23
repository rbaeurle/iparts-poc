/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getsparepartusage;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Request für den GetSparePartUsage Webservice der TruckBOM.foundation
 */
public class iPartsTruckBOMFoundationWSGetSparePartUsageRequest extends AbstractiPartsTruckBOMFoundationWebserviceRequest {

    private List<SparePartUsageRequestParameter> sparePartUsageRequestParameter;

    public iPartsTruckBOMFoundationWSGetSparePartUsageRequest() {
    }

    public iPartsTruckBOMFoundationWSGetSparePartUsageRequest(String identifier) {
        List<SparePartUsageRequestParameter> sparePartUsageRequestParameter = new ArrayList<>(1);
        sparePartUsageRequestParameter.add(new SparePartUsageRequestParameter(identifier));
        setSparePartUsageRequestParameter(sparePartUsageRequestParameter);
    }

    public List<SparePartUsageRequestParameter> getSparePartUsageRequestParameter() {
        return sparePartUsageRequestParameter;
    }

    public void setSparePartUsageRequestParameter(List<SparePartUsageRequestParameter> sparePartUsageRequestParameter) {
        this.sparePartUsageRequestParameter = sparePartUsageRequestParameter;
    }

    public static class SparePartUsageRequestParameter implements RESTfulTransferObjectInterface {

        private String sparePartIdentifier;

        public SparePartUsageRequestParameter() {
        }

        public SparePartUsageRequestParameter(String identifier) {
            this.sparePartIdentifier = identifier;
        }

        public String getSparePartIdentifier() {
            return sparePartIdentifier;
        }

        public void setSparePartIdentifier(String sparePartIdentifier) {
            this.sparePartIdentifier = sparePartIdentifier;
        }
    }
}
