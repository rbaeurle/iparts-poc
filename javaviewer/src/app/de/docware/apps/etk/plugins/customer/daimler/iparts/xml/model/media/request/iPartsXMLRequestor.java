/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLUserAndGroupObject;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.framework.utils.FrameworkUtils;

/**
 * Repräsentiert das "Requestor" Element in der Transfer XML
 */
public class iPartsXMLRequestor extends AbstractXMLUserAndGroupObject {

    public iPartsXMLRequestor(String userId) {
        super(null, (userId != null) ? userId : FrameworkUtils.getUserName());
    }

    public iPartsXMLRequestor() {
        this((String)null);
    }

    public iPartsXMLRequestor(DwXmlNode node) {
        super(node);
    }

    @Override
    protected iPartsTransferNodeTypes getNodeType() {
        return iPartsTransferNodeTypes.REQUESTOR;
    }
}