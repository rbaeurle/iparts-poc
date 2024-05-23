/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLMediaContainerCreateOrModifyResponse;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "ResCorrectMediaOrder" Element in der Transfer XML
 */
public class iParstXMLResCorrectMediaOrder extends AbstractXMLMediaContainerCreateOrModifyResponse {

    public iParstXMLResCorrectMediaOrder(DwXmlNode node) {
        super(iPartsTransferNodeTypes.RES_CORRECT_MEDIA_ORDER, node);
    }
}