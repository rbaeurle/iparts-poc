/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractGenericImporter;
import de.docware.util.file.DWFile;

/**
 * Interface für TruckBOM.foundation WebServices um den Import der Responses zu garantieren
 */
public interface iPartsTruckBOMFoundationWSWithImporter {

    /**
     * Startet den Import mit der übergebenen Response vom Webservice.
     *
     * @param project
     * @param response
     * @return
     */
    AbstractGenericImporter startImportFromResponse(EtkProject project, String response, DWFile jobLogFile);

    /**
     * Liefert den Namen vom Webservice zurück.
     *
     * @return
     */
    String getWebServiceName();

    /**
     * Erzeugt einen Request für den Webservice mit dem übergebenen Identifier.
     *
     * @param identifier
     * @return
     */
    AbstractiPartsTruckBOMFoundationWebserviceRequest createRequest(String identifier);
}
