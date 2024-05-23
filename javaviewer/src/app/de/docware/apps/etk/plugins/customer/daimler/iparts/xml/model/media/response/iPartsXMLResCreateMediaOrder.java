/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLMediaContainerCreateOrModifyResponse;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "ResCreateMediaOrder" Element in der Transfer XML
 */
public class iPartsXMLResCreateMediaOrder extends AbstractXMLMediaContainerCreateOrModifyResponse {

    public iPartsXMLResCreateMediaOrder(DwXmlNode node) {
        super(iPartsTransferNodeTypes.RES_CREATE_MEDIA_ORDER, node);
    }

}