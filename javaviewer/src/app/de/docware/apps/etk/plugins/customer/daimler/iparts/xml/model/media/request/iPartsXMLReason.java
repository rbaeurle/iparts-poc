/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;

/**
 * Repräsentiert das "Reason" Element in der Transfer XML
 */
public class iPartsXMLReason extends AbstractXMLObject {

    private String text;

    public iPartsXMLReason(String text) {
        this.text = text;
    }

    public iPartsXMLReason(DwXmlNode node) {
        loadFromXML(node);
    }

    public String getText() {
        return text;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode reasonNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.REASON.getAlias());
        if (StrUtils.isEmpty(text)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Reason must not be null or empty (ASPLM Schema)!");
        }
        reasonNode.setTextContent(text);
        return reasonNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.REASON)) {
                text = node.getTextContent();
            }
        }
    }
}
