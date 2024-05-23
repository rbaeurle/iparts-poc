/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLMediaContainerCreateOrModifyResponse;
import de.docware.framework.modules.xml.DwXmlNode;

public class iPartsXMLResUpdateMediaOrder extends AbstractXMLMediaContainerCreateOrModifyResponse {

    public iPartsXMLResUpdateMediaOrder(DwXmlNode node) {
        super(iPartsTransferNodeTypes.RES_UPDATE_MEDIA_ORDER, node);
    }
}
