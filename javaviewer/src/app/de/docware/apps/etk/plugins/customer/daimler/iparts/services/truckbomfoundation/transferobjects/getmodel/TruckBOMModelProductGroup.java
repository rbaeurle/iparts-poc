/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodel;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Repräsentiert die Produktgruppen Informationen im JSON für die Baumuster-Stammdaten aus der TruckBOM.foundation
 */
public class TruckBOMModelProductGroup implements RESTfulTransferObjectInterface {

    private String id;
    private String identifier;

    public TruckBOMModelProductGroup() {
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
}
