/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "BinaryFile" Element in der Transfer XML
 */
public abstract class AbstractXMLBinaryFile extends AbstractXMLObject {

    protected String fileType;
    protected String base64String;

    public AbstractXMLBinaryFile(String fileType, String imageAsBase64String) {
        this.base64String = imageAsBase64String;
        this.fileType = fileType;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getBase64String() {
        return base64String;
    }

    public void setBase64String(String dataAsBase64String) {
        this.base64String = dataAsBase64String;
    }

    protected abstract boolean checkFileTypeValid();

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        if (!checkFileTypeValid()) {
            return null;
        }
        DwXmlNode bfNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.BINARY_FILE.getAlias());
        if (fileType != null) {
            bfNode.setAttribute(ATT_FILETYPE, fileType);
        }
        if (base64String != null) {
            bfNode.setTextContent(base64String);
        }
        return bfNode;

    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.BINARY_FILE)) {
                fileType = node.getAttribute(ATT_FILETYPE);
                if (checkFileTypeValid()) {
                    setBase64String(node.getTextContent());
                }
            }
        }
    }

    /**
     * Wird aufgerufen, wenn diese Nachricht nur als Mitteilung ohne Binärdaten dienen soll und keine Datenbankaktionen
     * basierend auf dieser Nachricht stattfinden sollen.
     */
    public void convertToNotificationOnly() {
        // Binärdaten entfernen
        base64String = null;
    }
}
