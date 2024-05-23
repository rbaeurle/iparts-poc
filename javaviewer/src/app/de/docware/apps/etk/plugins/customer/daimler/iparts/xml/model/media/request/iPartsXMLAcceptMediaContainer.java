/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractMediaOrderRequestWithItem;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "AcceptMediaContainer" Element in der Transfer XML
 */
public class iPartsXMLAcceptMediaContainer extends AbstractMediaOrderRequestWithItem {

    public iPartsXMLAcceptMediaContainer(String mcItemId, String mcItemRevId) {
        super(mcItemId, mcItemRevId);
    }

    public iPartsXMLAcceptMediaContainer(DwXmlNode node) {
        super(node);
    }

    @Override
    protected void initMediaOrderRequest() {
        setOperationType(iPartsTransferNodeTypes.ACCEPT_MEDIA_CONTAINER);
    }
}
