/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.AbstractMessageType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLRequestOperation;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;

import java.util.List;

/**
 * Repräsentiert das "Request" Element in der Transfer XML
 */
public class iPartsXMLRequest extends AbstractMessageType {

    private iPartsXMLRequestor requestor;
    private AbstractXMLRequestOperation operation;

    public iPartsXMLRequest(String iPartsRequestID, String fromParticipant, String toParticipant) {
        this(iPartsRequestID, null, fromParticipant, toParticipant);
    }

    public iPartsXMLRequest(String iPartsRequestID, boolean answerToThisClusterOnly, String fromParticipant, String toParticipant) {
        this(iPartsRequestID, answerToThisClusterOnly ? ApplicationEvents.getClusterId() : null, fromParticipant, toParticipant);
    }

    public iPartsXMLRequest(String iPartsRequestID, String targetClusterID, String fromParticipant, String toParticipant) {
        this.messageType = iPartsTransferNodeTypes.REQUEST;
        this.iPartsRequestID = iPartsRequestID;
        this.targetClusterID = targetClusterID;
        this.fromParticipant = fromParticipant;
        this.toParticipant = toParticipant;
    }

    public iPartsXMLRequest(DwXmlNode node) {
        this("", "", "");
        loadFromXML(node);
    }

    public void setRequestor(iPartsXMLRequestor iPartsXMLRequestor) {
        this.requestor = iPartsXMLRequestor;
    }

    public void setOperation(AbstractXMLRequestOperation iPartsXMLOperation) {
        this.operation = iPartsXMLOperation;
    }

    public AbstractXMLRequestOperation getOperation() {
        return operation;
    }

    public iPartsXMLRequestor getRequestor() {
        return requestor;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode requestNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.REQUEST.getAlias());
        if (fromParticipant != null) {
            requestNode.setAttribute(ATTR_GRR_FROM, fromParticipant);
        }
        if (toParticipant != null) {
            requestNode.setAttribute(ATTR_GRR_TO, toParticipant);
        }
        if (iPartsRequestID != null) {
            requestNode.setAttribute(ATTR_GRR_REQUEST_ID, getXmlRequestID());
        }
        requestNode.appendChild(requestor.getAsDwXMLNode(namespacePrefix));
        // Das Element Operation im XML ist nur ein "ZwischenKnoten" ohne Attribute oder Inhalte -> ASPLM Media Schema
        DwXmlNode operationNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.OPERATION.getAlias());
        operationNode.appendChild(operation.getAsDwXMLNode(namespacePrefix));
        requestNode.appendChild(operationNode);
        return requestNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(node);
            if ((nodeType != null) && (nodeType == iPartsTransferNodeTypes.REQUEST)) {
                setXmlRequestID(node.getAttribute(ATTR_GRR_REQUEST_ID));
                fromParticipant = node.getAttribute(ATTR_GRR_FROM);
                toParticipant = node.getAttribute(ATTR_GRR_TO);

                //Kindknoten von Request
                List<DwXmlNode> childNodes = node.getChildNodes();
                for (DwXmlNode childNode : childNodes) {
                    nodeType = XMLImportExportHelper.getIPartsNodeType(childNode);
                    if (nodeType == null) {
                        continue;
                    }
                    switch (nodeType) {
                        case REQUESTOR:
                            setRequestor(new iPartsXMLRequestor(childNode));
                            break;
                        case OPERATION:
                            handleOperation(childNode);
                            break;
                        default:
                            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Element " + nodeType + " for Request does not exist in ASPLM Schema!");
                            break;
                    }
                }

            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Wrong element for creating a request object! Nodetype: " + nodeType);
            }
        }
    }

    private void handleOperation(DwXmlNode node) {
        if (node != null) {
            //Kindknoten von Operation
            List<DwXmlNode> childNodes = node.getChildNodes();
            for (DwXmlNode childNode : childNodes) {
                iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(childNode);
                if (nodeType == null) {
                    continue;
                }
                switch (nodeType) {
                    case CREATE_MEDIA_ORDER:
                        setOperation(new iPartsXMLCreateMediaOrder(childNode));
                        break;
                    case GET_MEDIA_PREVIEW:
                        setOperation(new iPartsXMLGetMediaPreview(childNode));
                        break;
                    case SEARCH_MEDIA_CONTAINERS:
                        setOperation(new iPartsXMLSearchMediaContainers(childNode));
                        break;
                    case GET_MEDIA_CONTENTS:
                        setOperation(new iPartsXMLGetMediaContents(childNode));
                        break;
                    case CREATE_MC_ATTACHMENTS:
                        setOperation(new iPartsXMLCreateMcAttachments(childNode));
                        break;
                    case CORRECT_MEDIA_ORDER:
                        setOperation(new iPartsXMLCorrectMediaOrder(childNode));
                        break;
                    case CHANGE_MEDIA_ORDER:
                        setOperation(new iPartsXMLChangeMediaOrder(childNode));
                        break;
                    case UPDATE_MEDIA_ORDER:
                        setOperation(new iPartsXMLUpdateMediaOrder(childNode));
                        break;
                    case ACCEPT_MEDIA_CONTAINER:
                        setOperation(new iPartsXMLAcceptMediaContainer(childNode));
                        break;
                    case ABORT_MEDIA_ORDER:
                        setOperation(new iPartsXMLAbortMediaOrder(childNode));
                        break;
                    default:
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Element " + nodeType + " for operation does not exist in ASPLM Schema!");
                        break;
                }
            }
        }
    }

}