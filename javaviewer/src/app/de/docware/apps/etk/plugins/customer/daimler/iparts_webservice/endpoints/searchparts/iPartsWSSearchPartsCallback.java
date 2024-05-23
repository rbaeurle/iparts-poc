/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchparts;

import de.docware.apps.etk.base.search.model.EtkPartResult;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartResult;

/**
 * Interface für Callback in iParts WS-Suchen, um die Suchtreffer zu verarbeiten, bevor sie als Result zusammengefasst werden
 */
public interface iPartsWSSearchPartsCallback {

    void processSearchResult(EtkPartResult searchResult, iPartsWSPartResult wsPartResult);
}