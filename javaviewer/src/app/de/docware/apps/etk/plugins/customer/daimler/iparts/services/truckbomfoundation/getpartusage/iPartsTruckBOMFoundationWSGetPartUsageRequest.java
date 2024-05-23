/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getpartusage;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Request für den GetPartUsage Webservice der TruckBOM.foundation
 */
public class iPartsTruckBOMFoundationWSGetPartUsageRequest extends AbstractiPartsTruckBOMFoundationWebserviceRequest {

    private List<PartUsageRequestParameter> partUsageRequestParameter;

    public iPartsTruckBOMFoundationWSGetPartUsageRequest() {
    }

    public iPartsTruckBOMFoundationWSGetPartUsageRequest(String identifier) {
        List<PartUsageRequestParameter> requestParameter = new ArrayList<>(1);
        requestParameter.add(new PartUsageRequestParameter(identifier));
        setPartUsageRequestParameter(requestParameter);
        // Default bei der Anfrage ist "true"
        setWithDistributionTask(true);
    }

    public List<PartUsageRequestParameter> getPartUsageRequestParameter() {
        return partUsageRequestParameter;
    }

    public void setPartUsageRequestParameter(List<PartUsageRequestParameter> partUsageRequestParameter) {
        this.partUsageRequestParameter = partUsageRequestParameter;
    }

    public static class PartUsageRequestParameter implements RESTfulTransferObjectInterface {

        private String partUsageParentElementIdentifier;

        public PartUsageRequestParameter() {
        }

        public PartUsageRequestParameter(String partUsageParentElementIdentifier) {
            this.partUsageParentElementIdentifier = partUsageParentElementIdentifier;
        }

        public String getPartUsageParentElementIdentifier() {
            return partUsageParentElementIdentifier;
        }

        public void setPartUsageParentElementIdentifier(String partUsageParentElementIdentifier) {
            this.partUsageParentElementIdentifier = partUsageParentElementIdentifier;
        }
    }
}