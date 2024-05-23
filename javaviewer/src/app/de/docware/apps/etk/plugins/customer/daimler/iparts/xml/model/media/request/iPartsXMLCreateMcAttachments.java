/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLObjectWithMCAttributes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert das "CreateMcAttachments" Element in der Transfer XML
 */
public class iPartsXMLCreateMcAttachments extends AbstractXMLObjectWithMCAttributes {

    private List<iPartsXMLAttachment> attachments;

    public iPartsXMLCreateMcAttachments(String mcItemId, String mcItemRevId) {
        setOperationType(iPartsTransferNodeTypes.CREATE_MC_ATTACHMENTS);
        this.mcItemId = mcItemId;
        this.mcItemRevId = mcItemRevId;
        this.attachments = new ArrayList<iPartsXMLAttachment>();
    }

    public iPartsXMLCreateMcAttachments(DwXmlNode node) {
        this("", "");
        loadFromXML(node);
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode cmaNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.CREATE_MC_ATTACHMENTS.getAlias());
        if (mcItemId != null) {
            cmaNode.setAttribute(ATTR_MC_ITEM_ID, mcItemId);
        }
        if (mcItemRevId != null) {
            cmaNode.setAttribute(ATTR_MC_ITEM_REV_ID, mcItemRevId);
        }
        for (iPartsXMLAttachment attachment : attachments) {
            cmaNode.appendChild(attachment.getAsDwXMLNode(namespacePrefix));
        }
        return cmaNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.CREATE_MC_ATTACHMENTS)) {
                mcItemId = node.getAttribute(ATTR_MC_ITEM_ID);
                mcItemRevId = node.getAttribute(ATTR_MC_ITEM_REV_ID);
            }
            fillCreateMcAttachments(node.getChildNodes());
        }
    }

    private void fillCreateMcAttachments(List<DwXmlNode> childNodes) {
        for (DwXmlNode childNode : childNodes) {
            if (XMLImportExportHelper.checkTagWithNamespace(childNode.getName(), iPartsTransferNodeTypes.ATTACHMENT)) {
                addAttachment(new iPartsXMLAttachment(childNode));
            }
        }
    }

    /**
     * Fügt dem Anhang Element einen Anhang hinzu.
     *
     * @param attachment
     */
    public void addAttachment(iPartsXMLAttachment attachment) {
        if (attachment != null) {
            attachments.add(attachment);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Attachment value must not be null.");
        }
    }

    public List<iPartsXMLAttachment> getAttachments() {
        return attachments;
    }
}
