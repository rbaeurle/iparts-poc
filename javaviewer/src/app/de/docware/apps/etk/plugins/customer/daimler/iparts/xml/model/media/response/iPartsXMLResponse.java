/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.AbstractMessageType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLResponseOperation;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;

import java.util.List;

/**
 * Repräsentiert das "Response" Element in der Transfer XML
 */
public class iPartsXMLResponse extends AbstractMessageType {

    private iPartsTransferNodeTypes requestOperation;
    private iPartsXMLSuccess success;
    private AbstractXMLResponseOperation result;

    public iPartsXMLResponse(String iPartsRequestID, iPartsTransferNodeTypes requestOperation,
                             String fromParticipant, String toParticipant) {
        this(iPartsRequestID, null, requestOperation, fromParticipant, toParticipant);
    }

    public iPartsXMLResponse(String iPartsRequestID, String targetClusterID, iPartsTransferNodeTypes requestOperation,
                             String fromParticipant, String toParticipant) {
        this.messageType = iPartsTransferNodeTypes.RESPONSE;
        this.iPartsRequestID = iPartsRequestID;
        this.targetClusterID = targetClusterID;
        this.requestOperation = requestOperation;
        this.fromParticipant = fromParticipant;
        this.toParticipant = toParticipant;
    }

    public iPartsXMLResponse(DwXmlNode node) {
        this("", "", null, "", "");
        loadFromXML(node);
    }

    public void setSuccess(iPartsXMLSuccess iPartsXMLSuccess) {
        this.success = iPartsXMLSuccess;
    }

    public iPartsXMLSuccess getSuccess() {
        return success;
    }

    public AbstractXMLResponseOperation getResult() {
        return result;
    }

    public void setResult(AbstractXMLResponseOperation result) {
        this.result = result;
    }

    public boolean isErrorFree() {
        if (success != null) {
            return success.isErrorFree();
        }
        return false;
    }

    /**
     * Liefert den Fehlertext für die angegebene Sprache zurück inkl. vorangestelltem Fehlercode mit Fallback auf Englisch
     * bzw. die erste verfügbare Fehlersprache.
     *
     * @param language
     * @return
     */
    public String getErrorCodeAndText(String language) {
        List<iPartsXMLErrorText> errorTexts = getSuccess().getErrors();
        String errorCode = String.valueOf(getSuccess().getErrorCode());
        if (errorTexts.isEmpty()) {
            return errorCode;
        } else {
            String lastErrorText = null;
            for (iPartsXMLErrorText error : errorTexts) {
                if (error.getLanguage().equalsIgnoreCase(language)) {
                    lastErrorText = error.getText();
                    break;
                }
            }

            // Englisch als erster Fallback
            if (lastErrorText == null) {
                for (iPartsXMLErrorText error : errorTexts) {
                    if (error.getLanguage().toUpperCase().equals(Language.EN.getCode())) {
                        lastErrorText = error.getText();
                        break;
                    }
                }

                // Erster Eintrag als letzter Fallback
                if (lastErrorText == null) {
                    lastErrorText = errorTexts.iterator().next().getText();
                }
            }

            return errorCode + " " + lastErrorText;
        }
    }

    /**
     * Liefert den Typ vom ursprünglichen XML Request für diese XML Response zurück.
     *
     * @return
     */
    public iPartsTransferNodeTypes getRequestOperation() {
        return requestOperation;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode responseNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.RESPONSE.getAlias());
        if (fromParticipant != null) {
            responseNode.setAttribute(ATTR_GRR_FROM, fromParticipant);
        }
        if (toParticipant != null) {
            responseNode.setAttribute(ATTR_GRR_TO, toParticipant);
        }
        if (iPartsRequestID != null) {
            responseNode.setAttribute(ATTR_GRR_REQUEST_ID, getXmlRequestID());
        }
        if (when != null) {
            responseNode.setAttribute(ATTR_GRR_WHEN, when);
        }
        if (requestOperation != null) {
            responseNode.setAttribute(ATTR_GRP_REQUEST_OPERATION, requestOperation.getAlias());
        }
        if (success != null) {
            responseNode.appendChild(success.getAsDwXMLNode(namespacePrefix));
        }
        if (result != null) {
            responseNode.appendChild(result.getAsDwXMLNode(namespacePrefix));
        }
        return responseNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.RESPONSE)) {
                setXmlRequestID(node.getAttribute(ATTR_GRR_REQUEST_ID));
                requestOperation = iPartsTransferNodeTypes.getFromAlias(node.getAttribute(ATTR_GRP_REQUEST_OPERATION));
                fromParticipant = node.getAttribute(ATTR_GRR_FROM);
                toParticipant = node.getAttribute(ATTR_GRR_TO);
                when = node.getAttribute(ATTR_GRR_WHEN);

                //Kindknoten von Response
                List<DwXmlNode> childNodes = node.getChildNodes();
                for (DwXmlNode childNode : childNodes) {
                    iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(childNode);
                    if (nodeType == null) {
                        continue;
                    }
                    switch (nodeType) {
                        case SUCCESS:
                            iPartsXMLSuccess success = new iPartsXMLSuccess(childNode);
                            setSuccess(success);
                            break;
                        case RES_CREATE_MEDIA_ORDER:
                            setResult(new iPartsXMLResCreateMediaOrder(childNode));
                            break;
                        case RES_SEARCH_MEDIA_CONTAINERS:
                            setResult(new iPartsXMLResSearchMediaContainers(childNode));
                            break;
                        case RES_GET_MEDIA_PREVIEW:
                            setResult(new iPartsXMLResGetMediaPreview(childNode));
                            break;
                        case RES_GET_MEDIA_CONTENTS:
                            setResult(new iPartsXMLResGetMediaContents(childNode));
                            break;
                        case RES_CREATE_MC_ATTACHMENTS:
                            setResult(new iPartsXMLResCreateMcAttachments(childNode));
                            break;
                        case RES_CORRECT_MEDIA_ORDER:
                            setResult(new iParstXMLResCorrectMediaOrder(childNode));
                            break;
                        case RES_CHANGE_MEDIA_ORDER:
                            setResult(new iPartsXMLResChangeMediaOrder(childNode));
                            break;
                        case RES_UPDATE_MEDIA_ORDER:
                            setResult(new iPartsXMLResUpdateMediaOrder(childNode));
                            break;
                        default:
                            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Element " + nodeType + " for Response does not exist in ASPLM Schema!");
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void convertToNotificationOnly() {
        super.convertToNotificationOnly();
        if (result != null) {
            result.convertToNotificationOnly();
        }
    }
}