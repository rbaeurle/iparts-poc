/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_export.endpoints.exportpartslist;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Response für den {@link iPartsWSExportPartsListEndpoint}, welche die {@link ##statusUrl} zu einer {@link #jobId}
 * enthält, über die der Status des Jobs abgerufen werden kann.
 */
public class iPartsWSExportPartsListResponse implements RESTfulTransferObjectInterface {

    private String jobId;
    private String statusUrl;
    private String[] missingData;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStatusUrl() {
        return statusUrl;
    }

    public void setStatusUrl(String statusUrl) {
        this.statusUrl = statusUrl;
    }

    public String[] getMissingData() {
        return missingData;
    }

    public void setMissingData(String[] missingData) {
        this.missingData = missingData;
    }
}
