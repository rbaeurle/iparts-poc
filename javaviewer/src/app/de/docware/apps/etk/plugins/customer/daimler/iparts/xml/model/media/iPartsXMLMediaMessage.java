/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.AbstractMessageType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLRequestOperation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event.iPartsXMLEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLHistory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLResGetMediaPreview;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLResponse;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.file.DWFile;

import java.util.List;

/**
 * Repräsentiert das "Message" Element in der Transfer XML
 */
public class iPartsXMLMediaMessage extends AbstractMQMessage {

    public static final String TYPE = "iPartsXMLMediaMessage";

    private iPartsXMLHistory history;
    private AbstractMessageType messageTypeObject;
    private String namespaceForElementDeclaration;
    private String schemaForElementDeclaration;
    private String schemaLocationForElementDeclaration;

    public iPartsXMLMediaMessage(boolean iniDefaultASPLMHeader) {
        super();
        messageType = TYPE;
        if (iniDefaultASPLMHeader) {
            initDefaultASPLMHeader();
        }
    }

    public iPartsXMLMediaMessage(DwXmlNode node, boolean createBinaryObjectsEnabled,
                                 boolean writeDebugFilesEnabled) {
        this(false);
        fillFromXML(node, createBinaryObjectsEnabled, writeDebugFilesEnabled);
    }

    @Override
    public boolean isValidForMQChannelTypeName(iPartsMQChannelTypeNames channelTypeName) {
        return channelTypeName == iPartsMQChannelTypeNames.MEDIA;
    }

    public AbstractMessageType getTypeObject() {
        return messageTypeObject;
    }

    public void setTypeObject(AbstractMessageType typeObject) {
        if (typeObject != null) {
            this.messageTypeObject = typeObject;
        }
    }

    public iPartsXMLHistory getHistory() {
        return history;
    }

    public void setHistory(iPartsXMLHistory iPartsXMLHistory) {
        if (iPartsXMLHistory != null) {
            this.history = iPartsXMLHistory;
        }
    }

    public String getNamespaceForElementDeclaration() {
        return namespaceForElementDeclaration;
    }

    public void setNamespaceForElementDeclaration(String namespaceForElementDeclaration) {
        this.namespaceForElementDeclaration = namespaceForElementDeclaration;
    }

    public String getSchemaForElementDeclaration() {
        return schemaForElementDeclaration;
    }

    public void setSchemaForElementDeclaration(String schemaForElementDeclaration) {
        this.schemaForElementDeclaration = schemaForElementDeclaration;
    }

    public String getSchemaLocationForElementDeclaration() {
        return schemaLocationForElementDeclaration;
    }

    public void setSchemaLocationForElementDeclaration(String schemaLocationForElementDeclaration) {
        this.schemaLocationForElementDeclaration = schemaLocationForElementDeclaration;
    }

    /**
     * Initialisiert den default ASPLM Header
     */
    public void initDefaultASPLMHeader() {
        namespaceForElementDeclaration = iPartsTransferConst.DEFAULT_NAMESPACE;
        schemaForElementDeclaration = iPartsTransferConst.DEFAULT_NAMESPACE_SCHEMA;
        schemaLocationForElementDeclaration = iPartsTransferConst.DEFAULT_NAMESPACE_SCHEMA_LOCATION;
    }

    public boolean isRequest() {
        return (messageTypeObject instanceof iPartsXMLRequest);
    }

    public boolean isResponse() {
        return (messageTypeObject instanceof iPartsXMLResponse);
    }

    public boolean isEvent() {
        return (messageTypeObject instanceof iPartsXMLEvent);
    }

    /**
     * Gibt das Response Objekt zurück, sofern es sich um ein Response handelt
     *
     * @return
     */
    public iPartsXMLResponse getResponse() {
        if (isResponse()) {
            return (iPartsXMLResponse)messageTypeObject;
        }
        return null;
    }

    /**
     * Gibt das Request Objekt zurück, sofern es sich um ein Request handelt
     *
     * @return
     */
    public iPartsXMLRequest getRequest() {
        if (isRequest()) {
            return (iPartsXMLRequest)messageTypeObject;
        }
        return null;
    }

    public iPartsTransferNodeTypes getRequestOperationTypeFromResponse() {
        iPartsXMLResponse response = getResponse();
        if (response != null) {
            iPartsTransferNodeTypes operation = response.getRequestOperation();
            if (operation != null) {
                return operation;

            }
        }
        return null;
    }

    public iPartsTransferNodeTypes getRequestOperationType() {
        iPartsXMLRequest request = getRequest();
        if (request != null) {
            AbstractXMLRequestOperation operation = request.getOperation();
            if (operation != null) {
                iPartsTransferNodeTypes operationType = operation.getOperationType();
                if (operationType != null) {
                    return operationType;
                }

            }
        }
        return null;
    }

    /**
     * Gibt die (Pseudo-)ID vom Request zurück, sofern es sich um ein Request handelt
     *
     * @return
     */
    public String getiPartsRequestId() {
        if (messageTypeObject != null) {
            if (messageTypeObject.isEvent()) {
                return DWFile.convertToValidFileName(((iPartsXMLEvent)messageTypeObject).getActualEvent().getEventType().getAlias());
            } else {
                return DWFile.convertToValidFileName(messageTypeObject.getiPartsRequestID());
            }
        } else {
            return "unknownRequestID";
        }
    }

    /**
     * Gibt das Event Objekt zurück, sofern es sich um ein Event handelt
     *
     * @return
     */
    public iPartsXMLEvent getEvent() {
        if (isEvent()) {
            return (iPartsXMLEvent)messageTypeObject;
        }
        return null;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode mediaMessageNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.MESSAGE.getAlias());
        if (namespacePrefix != null) {
            if ((namespaceForElementDeclaration == null) || namespaceForElementDeclaration.isEmpty()) {
                namespaceForElementDeclaration = DEFAULT_NAMESPACE;
            }
            if (namespacePrefix.isEmpty()) {
                if ((schemaForElementDeclaration == null) || schemaForElementDeclaration.isEmpty()) {
                    schemaForElementDeclaration = DEFAULT_NAMESPACE_SCHEMA;
                }
                if ((schemaLocationForElementDeclaration == null) || schemaLocationForElementDeclaration.isEmpty()) {
                    schemaLocationForElementDeclaration = DEFAULT_NAMESPACE_SCHEMA_LOCATION;
                }

                mediaMessageNode.setAttribute(ATTR_M_NAMESPACE_SCHEMA, schemaForElementDeclaration);
                mediaMessageNode.setAttribute(ATTR_M_NAMESPACE_SCHEMA_LOCATION, schemaLocationForElementDeclaration);
                mediaMessageNode.setAttribute(ATTR_M_NAMESPACE, namespaceForElementDeclaration);
            } else {
                mediaMessageNode.setAttribute(ATTR_M_NAMESPACE_ASPLM, namespaceForElementDeclaration);
            }
            //Message kann entweder einen Request, einen Response oder ein Event haben, aber nicht alle zusammen
            if (isRequest() || isResponse() || isEvent()) {
                mediaMessageNode.appendChild(messageTypeObject.getAsDwXMLNode(namespacePrefix));
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Message must not be anything else than Type:Request or Type:Response.");
            }
            //Falls eine History enthalten ist -> Füge das History Element dem XML Dokument hinzu
            if (history != null) {
                mediaMessageNode.appendChild(history.getAsDwXMLNode(namespacePrefix));
            }
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Namespace parameter must not be null!");
        }
        return mediaMessageNode;
    }

    @Override
    public void loadFromXML(DwXmlNode node) {
    }

    public void fillFromXML(DwXmlNode node, boolean createBinaryObjectsEnabled,
                            boolean writeDebugFilesEnabled) {
        iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(node);
        if (nodeType == null) {
            return;
        }
        switch (nodeType) {
            case MESSAGE:
                setNamespaceForElementDeclaration(node.getAttribute(ATTR_M_NAMESPACE_ASPLM));
                setSchemaForElementDeclaration(node.getAttribute(ATTR_M_NAMESPACE_SCHEMA));

                List<DwXmlNode> childNodes = node.getChildNodes();
                for (DwXmlNode childNode : childNodes) {
                    fillMediaMessage(childNode, createBinaryObjectsEnabled, writeDebugFilesEnabled);
                }
                break;
        }
    }

    private void fillMediaMessage(DwXmlNode node, boolean createBinaryObjectsEnabled,
                                  boolean writeDebugFilesEnabled) {
        iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(node);
        if (nodeType == null) {
            return;
        }
        switch (nodeType) {
            case REQUEST:
                setTypeObject(new iPartsXMLRequest(node));
                break;
            case RESPONSE:
                setTypeObject(new iPartsXMLResponse(node));
                processMediaPreviewDebugParameter(getResponse(), createBinaryObjectsEnabled, writeDebugFilesEnabled);
                break;
            case HISTORY:
                setHistory(new iPartsXMLHistory(node));
                if (getHistory().getResponse() != null) {
                    processMediaPreviewDebugParameter(getHistory().getResponse(), createBinaryObjectsEnabled, writeDebugFilesEnabled);
                }
                break;
            case EVENT:
                setTypeObject(new iPartsXMLEvent(node));
                break;
        }

    }

    private void processMediaPreviewDebugParameter(iPartsXMLResponse response, boolean createBinaryObjects, boolean writeDebugFiles) {
        if (response.getResult() != null) {
            if (createBinaryObjects || writeDebugFiles) {
                if (response.getResult().getResultType() == iPartsTransferNodeTypes.RES_GET_MEDIA_PREVIEW) {
                    iPartsXMLResGetMediaPreview rgmp = (iPartsXMLResGetMediaPreview)response.getResult();
                    rgmp.processDebugParameters(createBinaryObjects, writeDebugFiles);
                }
            }
        }
    }

    @Override
    public void convertToNotificationOnly() {
        super.convertToNotificationOnly();
        history = null;
        if (messageTypeObject != null) {
            messageTypeObject.convertToNotificationOnly();
        }
    }

    @Override
    public boolean isValid() {
        return isRequest() || isResponse() || isEvent();
    }
}