package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem;

import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.helper.BomDBDefaultServiceHandler;
import de.docware.util.security.PasswordString;

/**
 * Diese Klasse implementiert den SOAPHandler für den SOAP Webservices zur Abfrage von KEM-Daten aus der BOM-DB und agiert
 * als Interceptor. Sie wird genutzt, um SOAP Nachrichten zu manipulieren.
 */
public class EcoContentServiceHandler extends BomDBDefaultServiceHandler {

    public EcoContentServiceHandler(String username, PasswordString password, String appToken) {
        super(username, password, appToken);
    }
}
