/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation;

import de.docware.framework.modules.webservice.restful.CallWebserviceException;

/**
 * Allgemeine Exception für TruckBOM.foundation Webservices
 */
public class iPartsTruckBOMFoundationWebserviceException extends CallWebserviceException {

    public iPartsTruckBOMFoundationWebserviceException(String message) {
        super(message);
    }

    public iPartsTruckBOMFoundationWebserviceException(String message, Throwable cause) {
        super(message, cause);
    }
}