/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Repräsentiert eine Assoziation zwischen einem Hauptdatensatz und einem Sub-Datensatz im JSON aus TruckBOM.foundation
 */
public class TruckBOMAssociation implements RESTfulTransferObjectInterface {

    private String idFrom;
    private String idTo;
    private String role;

    public TruckBOMAssociation() {
    }

    public String getIdFrom() {
        return idFrom;
    }

    public void setIdFrom(String idFrom) {
        this.idFrom = idFrom;
    }

    public String getIdTo() {
        return idTo;
    }

    public void setIdTo(String idTo) {
        this.idTo = idTo;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
