/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;

/**
 * Repräsentiert das "Attachment" Element in der Transfer XML
 */
public class iPartsXMLAttachment extends AbstractXMLObject {

    private String id; // Muss Feld
    private String name; // Muss Feld
    private String description;
    private String purpose;
    private iPartsXMLAttachmentBinaryFile binaryFile;
    private iPartsXMLAttachmentTextFile textFile;

    public iPartsXMLAttachment(String id, String name) {
        setId(id);
        setName(name);
        this.name = name;
    }

    public iPartsXMLAttachment(DwXmlNode node) {
        loadFromXML(node);
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode attachmentNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.ATTACHMENT.getAlias());

        attachmentNode.setAttribute(ATTR_ATTACHMENT_ID, getId());
        attachmentNode.setAttribute(ATTR_NAME, getName());

        if (description != null) {
            attachmentNode.setAttribute(ATTR_ATTACHMENT_DESCRIPTION, getDescription());
        }
        if (purpose != null) {
            attachmentNode.setAttribute(ATTR_ATTACHMENT_PURPOSE, getPurpose());
        }
        if (binaryFile != null) {
            DwXmlNode binaryFileNode = getBinaryFile().getAsDwXMLNode(namespacePrefix);
            if (binaryFileNode != null) {
                attachmentNode.appendChild(binaryFileNode);
            }
        }
        if (textFile != null) {
            DwXmlNode textFileNode = getTextFile().getAsDwXMLNode(namespacePrefix);
            if (textFileNode != null) {
                attachmentNode.appendChild(textFileNode);
            }
        }

        return attachmentNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.ATTACHMENT)) {
                setId(node.getAttribute(iPartsTransferConst.ATTR_ATTACHMENT_ID));
                setName(node.getAttribute(iPartsTransferConst.ATTR_NAME));
                setDescription(node.getAttribute(iPartsTransferConst.ATTR_ATTACHMENT_DESCRIPTION));
                setPurpose(node.getAttribute(iPartsTransferConst.ATTR_ATTACHMENT_PURPOSE));
                if (node.getChildNodes().size() > 1) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Only one attachment file per attachment element is allowed. Attachment ID: "
                                                                               + getId());
                }
                DwXmlNode childNode = node.getFirstChild();
                String name = StrUtils.removeFirstCharacterIfCharacterIs(childNode.getName(), iPartsTransferConst.ASPLM_XML_NAMESPACE_PREFIX);
                iPartsTransferNodeTypes type = iPartsTransferNodeTypes.getFromAlias(name);
                if (type == null) {
                    return;
                }
                switch (type) {
                    case BINARY_FILE:
                        iPartsXMLAttachmentBinaryFile binaryFile = new iPartsXMLAttachmentBinaryFile(childNode);
                        setBinaryFile(binaryFile);
                        break;
                    case TEXT_FILE:
                        iPartsXMLAttachmentTextFile textFile = new iPartsXMLAttachmentTextFile(childNode);
                        setTextFile(textFile);
                        break;
                }
            }
        }
    }

    public String getId() {
        if (id == null) {
            return "";
        }
        return id;
    }

    public String getName() {
        if (name == null) {
            return "";
        }
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPurpose() {
        return purpose;
    }

    public iPartsXMLAttachmentBinaryFile getBinaryFile() {
        return binaryFile;
    }

    public iPartsXMLAttachmentTextFile getTextFile() {
        return textFile;
    }

    public void setId(String id) {
        if (!StrUtils.isEmpty(id)) {
            this.id = id;
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Attachment ID is a required value. It must not be null or empty!");
        }
    }

    public void setName(String name) {
        if (!StrUtils.isEmpty(name)) {
            this.name = name;
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Attachment name is a required value. It must not be null or empty!");
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public void setBinaryFile(iPartsXMLAttachmentBinaryFile binaryFile) {
        this.binaryFile = binaryFile;
    }

    public void setTextFile(iPartsXMLAttachmentTextFile textFile) {
        this.textFile = textFile;
    }
}
