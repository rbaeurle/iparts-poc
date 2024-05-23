/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.code;

import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.helper.BomDBServiceHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated.*;
import de.docware.util.security.PasswordString;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.Set;

/**
 * Diese Klasse dient zum Aufruf von SOAP Webservices zur Abfrage von Code-Daten aus der BOM-DB.
 */
public class EcoCodeDataService {

    public GetCodeMasterDataResult getEcoCodeData(Set<String> codes, String compareDate, String language) throws NotImplementedException_Exception, BomDbServiceException_Exception,
                                                                                                                 SOAPFaultException {

        // Benutzername
        String username = BomDBServiceHelper.getBomDbUserName();
        // Passwort
        PasswordString password = BomDBServiceHelper.getBomDbPassword();
        // Application Token (AppToken)
        String appToken = BomDBServiceHelper.getBomDbAppToken();
        DaiEngBomDbService bomDbService = BomDBServiceHelper.createAndInitBomDBService(new EcoContentCodeDataServiceHandler(username, password, appToken));

        // Request-XML-Payload erzeugen
        GetCodeMasterDataInput ecoCodeInput = getEcoCodeMasterDataInput(username, codes, compareDate, language);

        return bomDbService.getCodeMasterData(ecoCodeInput);

    }

    private GetCodeMasterDataInput getEcoCodeMasterDataInput(String username, Set<String> codes, String compareDate, String language) {
        GetCodeMasterDataInput ecoCodeMasterDataInput = new GetCodeMasterDataInput();

        ecoCodeMasterDataInput.setQueryName(username);
        ecoCodeMasterDataInput.setQueryLanguage(BomDBServiceHelper.getEcoLanguage(language));

        BcsObjects bcsObjects = new BcsObjects();
        for (String code : codes) {
            BcsObject bcsObject = new BcsObject();
            bcsObject.setItem(code);
            bcsObjects.getBcsObject().add(bcsObject);
        }
        ecoCodeMasterDataInput.setBcsObjects(bcsObjects);

        // Statischer Input - evtl. Kandidaten für weitere Admin-Optionen
        ecoCodeMasterDataInput.setLatestVersion(false);
        ecoCodeMasterDataInput.setReleaseFlag(ReleaseFlag.RELEASED_ONLY);
        ecoCodeMasterDataInput.setMaxNumber(10000);
        XMLGregorianCalendar txnTimeXMLCalendar = BomDBServiceHelper.convertDateTime(compareDate);
        if (txnTimeXMLCalendar != null) {
            ecoCodeMasterDataInput.setReleaseDate(txnTimeXMLCalendar);
        }
        return ecoCodeMasterDataInput;
    }


}
