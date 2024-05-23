/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLMediaContainerCreateOrModifyResponse;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "ResAbortMediaOrder" Element in der Transfer XML
 */
public class iPartsXMLResAbortMediaOrder extends AbstractXMLMediaContainerCreateOrModifyResponse {

    public iPartsXMLResAbortMediaOrder(DwXmlNode node) {
        super(iPartsTransferNodeTypes.RES_ABORT_MEDIA_ORDER, node);
    }
}
