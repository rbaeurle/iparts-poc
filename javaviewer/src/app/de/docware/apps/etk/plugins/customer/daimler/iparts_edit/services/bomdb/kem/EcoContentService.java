/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem;

import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.helper.BomDBServiceHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated.*;
import de.docware.util.security.PasswordString;

import javax.xml.ws.soap.SOAPFaultException;
import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse dient zum Aufruf von SOAP Webservices zur Abfrage von KEM-Daten aus der BOM-DB.
 */
public class EcoContentService {

    /**
     * Ruft mittels SOAP Webservice KEM-Daten aus der BOM-DB ab.
     *
     * @param kemNumber KEM-Nummer
     * @param language  Gewünschte Sprache
     * @return eine Liste mit anzuzeigenden Textelementen
     */
    public GetEcoContentResult getEcoContentByKem(String kemNumber, String language) throws NotImplementedException_Exception, BomDbServiceException_Exception,
                                                                                            SOAPFaultException {
        // Benutzername
        String username = BomDBServiceHelper.getBomDbUserName();
        // Passwort
        PasswordString password = BomDBServiceHelper.getBomDbPassword();
        // Application Token (AppToken)
        String appToken = BomDBServiceHelper.getBomDbAppToken();

        DaiEngBomDbService bomDbService = BomDBServiceHelper.createAndInitBomDBService(new EcoContentServiceHandler(username, password, appToken));

        // Request-XML-Payload erzeugen
        GetEcoContentInput ecoContentInput = getEcoContentInput(username, kemNumber, language);

        List<String> messagesToDisplay = new ArrayList<>();

        return bomDbService.getEcoContent(ecoContentInput);
    }

    /**
     * Diese Methode erzeugt den Request-XML-Payload für den Input des SOAP Webservices.
     *
     * @param username  Der Antragssteller / Benutzername
     * @param kemNumber KEM-Nummer
     * @param language  Gewünschte Sprache
     * @return Der Request-XML-Payload
     */
    private GetEcoContentInput getEcoContentInput(String username, String kemNumber, String language) {
        GetEcoContentInput ecoContentInput = new GetEcoContentInput();

        ecoContentInput.setQueryName(username);
        ecoContentInput.setEco(kemNumber);

        ecoContentInput.setQueryLanguage(BomDBServiceHelper.getEcoLanguage(language));

        // Statischer Input - evtl. Kandidaten für weitere Admin-Optionen
        ecoContentInput.setLatestVersion(true);
        ecoContentInput.setAllStructures(true);
        ecoContentInput.setModificationFlag(ModificationFlag.BOTH);
        ecoContentInput.setReleaseFlag(ReleaseFlag.RELEASED_OR_WORKING);
        ecoContentInput.setMaxNumber(10000);
        ecoContentInput.setShowChangeNotice(true);
        return ecoContentInput;
    }
}