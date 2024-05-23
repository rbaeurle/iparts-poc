/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Repräsentiert ein KEM-Objekt im JSON für die Teile-Stammdaten aus TruckBOM.foundation
 */
public class TruckBOMSingleKEM implements RESTfulTransferObjectInterface {

    private static final String DELIMITER = "||";

    private String id;
    private String identifier;
    private String status;
    private String releaseDate;

    public TruckBOMSingleKEM() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    @JsonIgnore
    public String getDataAsString() {
        return getId() + DELIMITER + getIdentifier() + DELIMITER + getStatus() + DELIMITER + getReleaseDate();
    }
}
