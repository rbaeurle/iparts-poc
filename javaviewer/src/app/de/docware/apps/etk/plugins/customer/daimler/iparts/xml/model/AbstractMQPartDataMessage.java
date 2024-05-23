/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Abstrakte Klasse für Teilestamminformationen, die über MQ kommen. Im Moment nur PRIMUS und SRM
 */
public abstract class AbstractMQPartDataMessage extends AbstractMQMessage {

    private DwXmlNode header;
    private DwXmlNode source;
    private DwXmlNode message;

    private iPartsImportDataOrigin origin;

    public AbstractMQPartDataMessage(DwXmlNode node, String type) {
        messageType = type;
        loadFromXML(node);
    }

    @Override
    public boolean isValid() {
        return (header != null) && (source != null) && (message != null) && (origin == getTargetOrigin());
    }

    public DwXmlNode getHeader() {
        return header;
    }

    public DwXmlNode getSource() {
        return source;
    }

    public DwXmlNode getMessage() {
        return message;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode result = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.MQ_PART_DATA_FIRST_ELEMENT.getAlias());
        if (isValid()) {
            result.appendChild(header);
            result.appendChild(source);
            result.appendChild(message);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Could not create " + getTargetOrigin().getOrigin() + " dataset XML Node. PRIMUS dataset is not valid!");
        }
        return result;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if ((node != null) && node.getName().equals(iPartsTransferNodeTypes.MQ_PART_DATA_FIRST_ELEMENT.getAlias())) {
            header = node.getFirstElementByTagName(iPartsTransferNodeTypes.MQ_PART_DATA_HEADER.getAlias());
            source = node.getFirstElementByTagName(iPartsTransferNodeTypes.MQ_PART_DATA_SOURCE.getAlias());
            message = node.getFirstElementByTagName(iPartsTransferNodeTypes.MQ_PART_DATA_MESSAGE.getAlias());
            if (source != null) {
                origin = iPartsImportDataOrigin.getTypeFromCode(source.getFirstElementByTagName(iPartsTransferNodeTypes.MQ_PART_DATA_SYSTEM.getAlias()).getTextContent());
            }
        }
    }

    public iPartsImportDataOrigin getOrigin() {
        return origin;
    }

    protected abstract iPartsImportDataOrigin getTargetOrigin();

    public abstract iPartsTransferNodeTypes getIPartsNodeType();
}
