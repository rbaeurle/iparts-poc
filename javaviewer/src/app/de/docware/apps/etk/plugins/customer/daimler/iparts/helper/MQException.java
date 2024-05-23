/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

/**
 * Exception bei MQ Aktionen.
 */
public class MQException extends Exception {

    public MQException(Throwable cause) {
        super(cause);
    }

    public MQException(String message) {
        super(message);
    }

    public MQException(String message, Throwable cause) {
        super(message, cause);
    }
}
