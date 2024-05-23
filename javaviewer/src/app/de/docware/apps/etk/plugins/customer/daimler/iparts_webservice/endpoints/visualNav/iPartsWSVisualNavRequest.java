/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.visualNav;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.VinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapper;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;

/**
 * Request Data Transfer Object für den visualNav-Webservice
 */
public class iPartsWSVisualNavRequest extends iPartsWSUserWrapper {

    private String finOrVin;

    public iPartsWSVisualNavRequest() {
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        super.checkIfValid(path);
        checkAttribValid(path, "fin", finOrVin);

        FinId finId = new FinId(finOrVin);
        if (!finId.isValidId()) {
            VinId vinId = new VinId(finOrVin);
            if (!vinId.isValidId()) {
                throwRequestError(WSError.REQUEST_PARAMETER_WRONG, "No valid FIN or VIN", path);
            }
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ finOrVin, getUser() };
    }

    public String getFinOrVin() {
        return finOrVin;
    }

    public void setFinOrVin(String finOrVin) {
        this.finOrVin = finOrVin;
    }
}