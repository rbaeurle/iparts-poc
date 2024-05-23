/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractMediaOrderRequestWithItemAndReason;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "AbortMediaOrder" Element in der Transfer XML
 */
public class iPartsXMLAbortMediaOrder extends AbstractMediaOrderRequestWithItemAndReason {

    public iPartsXMLAbortMediaOrder(String mcItemId, String mcItemRevId, String reason) {
        super(mcItemId, mcItemRevId, reason);
    }

    public iPartsXMLAbortMediaOrder(DwXmlNode node) {
        super(node);
    }

    @Override
    protected void initMediaOrderRequest() {
        setOperationType(iPartsTransferNodeTypes.ABORT_MEDIA_ORDER);
    }
}
