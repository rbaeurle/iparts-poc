package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard;

import de.docware.framework.modules.webservice.restful.CallWebserviceException;

/**
 * Exception beim Abfragen von Datenkarten über einen Webservice
 */
public class DataCardRetrievalException extends CallWebserviceException {

    public DataCardRetrievalException(String message) {
        super(message);
    }

    public DataCardRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}