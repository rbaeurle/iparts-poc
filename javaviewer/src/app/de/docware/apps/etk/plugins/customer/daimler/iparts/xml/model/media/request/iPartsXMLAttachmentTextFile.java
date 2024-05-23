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

import java.util.List;

/**
 * Repräsentiert das "TextFile" Element in der Transfer XML
 */
public class iPartsXMLAttachmentTextFile extends AbstractXMLObject {

    private String content;
    private String fileType;

    public iPartsXMLAttachmentTextFile(String content, String fileType) {
        this.content = content;
        this.fileType = fileType;
    }

    public iPartsXMLAttachmentTextFile(DwXmlNode node) {
        loadFromXML(node);
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        if (!checkFileTypeValid()) {
            return null;
        }
        DwXmlNode tfNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.TEXT_FILE.getAlias());
        if (fileType != null) {
            tfNode.setAttribute(ATT_FILETYPE, fileType);
        }
        if (content != null) {
            DwXmlNode cdataNode = new DwXmlNode(DwXmlNode.CDATA_NODE_NAME);
            cdataNode.setTextContent(content);
            tfNode.appendChild(cdataNode);
        }
        return tfNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.TEXT_FILE)) {
                setFileType(node.getAttribute(ATT_FILETYPE));
                if (checkFileTypeValid()) {
                    List<DwXmlNode> childNodes = node.getChildNodes();
                    // Ein TextFile Element kann nur den Inhalt einer Textdatei enthalten
                    if (childNodes.size() > 1) {
                        // Fehler
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Only one text file per \"TextFile\" element is allowed.");
                    }
                    if (childNodes.isEmpty()) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "No content in TextFile XML element.");
                        setContent("");
                    } else {
                        setContent(childNodes.get(0).getTextContent());
                    }
                }
            }
        }
    }

    private boolean checkFileTypeValid() {
        // Endung ist nicht zwingend notwendig
        if (fileType == null) {
            return true;
        }
        if (AttachmentTextFileTypes.isValidFileExtension(fileType)) {
            return true;
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "File type is not valid for XML attachment text file: " + fileType);
            return false;
        }
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
