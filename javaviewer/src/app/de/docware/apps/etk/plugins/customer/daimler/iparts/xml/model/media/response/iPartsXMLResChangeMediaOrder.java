/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLMediaContainerCreateOrModifyResponse;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "ResChangeMediaOrder" Element in der Transfer XML
 */
public class iPartsXMLResChangeMediaOrder extends AbstractXMLMediaContainerCreateOrModifyResponse {

    public iPartsXMLResChangeMediaOrder(DwXmlNode node) {
        super(iPartsTransferNodeTypes.RES_CHANGE_MEDIA_ORDER, node);
    }
}
