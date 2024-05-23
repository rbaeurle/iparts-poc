/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractMediaOrderRequestWithItem;
import de.docware.framework.modules.xml.DwXmlNode;

public class iPartsXMLUpdateMediaOrder extends AbstractMediaOrderRequestWithItem {


    public iPartsXMLUpdateMediaOrder(String mcItemId, String mcItemRevId) {
        super(mcItemId, mcItemRevId);
    }

    public iPartsXMLUpdateMediaOrder(DwXmlNode node) {
        super(node);
    }

    @Override
    protected void initMediaOrderRequest() {
        setOperationType(iPartsTransferNodeTypes.UPDATE_MEDIA_ORDER);
    }

}
