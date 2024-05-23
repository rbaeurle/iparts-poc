/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.modules.webservice.restful.WSErrorResponse;

import java.util.Set;

/**
 * Response für einen Fehler bei der Abarbeitung eines iParts-Webservices.
 * Enthält einen Fehlercode aus {@link WSError}, eine Nachricht und die Gültigkeiten, mit denen der Fehler nicht aufgetreten wäre.
 */
public class iPartsWSIdentErrorResponse extends WSErrorResponse {

    private Set<String> missingPermissions;

    public iPartsWSIdentErrorResponse() {
    }

    public iPartsWSIdentErrorResponse(WSError code, String message, Set<String> missingPermissions) {
        super(code, message);
        this.missingPermissions = missingPermissions;
    }

    public Set<String> getMissingPermissions() {
        return missingPermissions;
    }

    public void setMissingPermissions(Set<String> missingPermissions) {
        this.missingPermissions = missingPermissions;
    }
}
