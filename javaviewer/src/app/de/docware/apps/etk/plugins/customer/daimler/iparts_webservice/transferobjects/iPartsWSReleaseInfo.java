/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Release Info Data Transfer Object für die iParts Webservices
 * Ausgabe im GetParts Webservice
 */
public class iPartsWSReleaseInfo implements RESTfulTransferObjectInterface {

    private String datasetDate;
    private String lastChangedDate;
    private String lastPublishedDate;
    private String softwareVersion;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSReleaseInfo() {
    }

    public String getLastChangedDate() {
        return lastChangedDate;
    }

    public void setLastChangedDate(String lastChangedDate) {
        this.lastChangedDate = lastChangedDate;
    }

    public String getDatasetDate() {
        return datasetDate;
    }

    public void setDatasetDate(String datasetDate) {
        this.datasetDate = datasetDate;
    }

    public String getLastPublishedDate() {
        return lastPublishedDate;
    }

    public void setLastPublishedDate(String lastPublishedDate) {
        this.lastPublishedDate = lastPublishedDate;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }
}
