/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Abstrakter Request für den Aufruf von einem TruckBOM.foundation Webservice.
 */
public abstract class AbstractiPartsTruckBOMFoundationWebserviceRequest implements RESTfulTransferObjectInterface {

    private static final String DEFAULT_MAX_NUMBER = "100000";

    private String releasedLaterThan;
    private String releasedEarlierThan;
    private boolean withDistributionTask;
    private String maxNumber = DEFAULT_MAX_NUMBER;

    public String getReleasedLaterThan() {
        return releasedLaterThan;
    }

    public void setReleasedLaterThan(String releasedLaterThan) {
        this.releasedLaterThan = releasedLaterThan;
    }

    public String getReleasedEarlierThan() {
        return releasedEarlierThan;
    }

    public void setReleasedEarlierThan(String releasedEarlierThan) {
        this.releasedEarlierThan = releasedEarlierThan;
    }

    public boolean isWithDistributionTask() {
        return withDistributionTask;
    }

    public void setWithDistributionTask(boolean withDistributionTask) {
        this.withDistributionTask = withDistributionTask;
    }

    public String getMaxNumber() {
        return maxNumber;
    }

    public void setMaxNumber(String maxNumber) {
        this.maxNumber = maxNumber;
    }
}