/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLMediaContainerCreateOrModifyResponse;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "ResAcceptMediaContainer" Element in der Transfer XML
 */
public class iPartsXMLResAcceptMediaContainer extends AbstractXMLMediaContainerCreateOrModifyResponse {

    public iPartsXMLResAcceptMediaContainer(DwXmlNode node) {
        super(iPartsTransferNodeTypes.RES_ACCEPT_MEDIA_CONTAINER, node);
    }

}
