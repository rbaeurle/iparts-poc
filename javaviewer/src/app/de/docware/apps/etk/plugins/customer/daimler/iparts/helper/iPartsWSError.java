/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.framework.modules.webservice.restful.WSError;

/**
 * iParts-spezifische Error-Codes für das JSON-Error-Objekt
 */
public class iPartsWSError extends WSError {

    /**
     * Es wurde die Datenkarte für ein verschrottetes Fahrzeug übergeben, es darf nur ein entsprechender Fehlercode zurückgegeben werden.
     */
    public static final iPartsWSError REQUEST_VEHICLE_SCRAPPED = new iPartsWSError(4004);

    /**
     * Es wurde die Datenkarte für ein gestohlenes Fahrzeug übergeben, es darf nur ein entsprechender Fehlercode zurückgegeben werden.
     */
    public static final iPartsWSError REQUEST_VEHICLE_STOLEN = new iPartsWSError(4005);

    /**
     * Beim Export-Webservice wurden Baumuster/SAA angefragt, die alle kein Produkt haben
     */
    public static final iPartsWSError REQUEST_NO_PRODUCT_FOUND = new iPartsWSError(4004);

    private iPartsWSError(int code) {
        super(code);
    }
}
