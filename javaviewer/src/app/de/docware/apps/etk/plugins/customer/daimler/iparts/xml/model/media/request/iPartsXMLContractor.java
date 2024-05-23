/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLUserAndGroupObject;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;

/**
 * Repräsentiert das "Contractor" Element in der Transfer XML
 */
public class iPartsXMLContractor extends AbstractXMLUserAndGroupObject {

    public iPartsXMLContractor(String groupID) {
        super(groupID, null);
    }

    public iPartsXMLContractor(DwXmlNode node) {
        super(node);
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        if (StrUtils.isEmpty(getGroupId())) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "GroupId must not be null or empty (ASPLM Schema)!");
        }
        return super.getAsDwXMLNode(namespacePrefix);
    }

    @Override
    protected iPartsTransferNodeTypes getNodeType() {
        return iPartsTransferNodeTypes.CONTRACTOR;
    }
}
