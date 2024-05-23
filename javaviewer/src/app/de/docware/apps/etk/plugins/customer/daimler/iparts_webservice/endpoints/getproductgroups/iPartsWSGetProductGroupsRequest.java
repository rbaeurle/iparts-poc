/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getproductgroups;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserWrapper;

/**
 * Request Data Transfer Object für den GetProductGroups-Webservice
 *
 * Beispiele:
 * - {"user":{"userId":"sb","language":"de","country":"200"}} -> korrekte JSON-Response für Deutsch
 * - {"user":{"userId":"sb","country":"200"}}                 -> korrekte JSON-Response für Englisch (Default)
 * - {}                                                       -> 400 Bad Request (user fehlt komplett)
 * - {"user":{"language":"de","country":"200"}}               -> 400 Bad Request (userId fehlt)
 * - {"user":{"userId":"sb"}}                                 -> 400 Bad Request (country fehlt)
 */
public class iPartsWSGetProductGroupsRequest extends iPartsWSUserWrapper {

    public iPartsWSGetProductGroupsRequest() {
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ getUser() };
    }
}
