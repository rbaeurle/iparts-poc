/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;

/**
 * Interface für mögliche Results von ASPLM z.B. {@link de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLResCreateMediaOrder}
 */
public abstract class AbstractXMLResponseOperation extends AbstractXMLObject {

    protected iPartsTransferNodeTypes resultType;

    public iPartsTransferNodeTypes getResultType() {
        return resultType;
    }

    public void setResultType(iPartsTransferNodeTypes resultType) {
        this.resultType = resultType;
    }

    /**
     * Wird aufgerufen, wenn diese Nachricht nur als Mitteilung ohne Binärdaten dienen soll und keine Datenbankaktionen
     * basierend auf dieser Nachricht stattfinden sollen.
     */
    public void convertToNotificationOnly() {
    }
}
