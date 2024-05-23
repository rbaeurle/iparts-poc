/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLRequest;
import de.docware.framework.modules.xml.DwXmlNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert das "History" Element in der Transfer XML
 */
public class iPartsXMLHistory extends AbstractXMLObject {

    private iPartsXMLResponse response;
    private iPartsXMLRequest request;
    private List<iPartsXMLTimeStamp> timeStamps;
    private boolean validRequest = true;
    private String invalidRequestAsText;

    public iPartsXMLHistory() {

    }

    public iPartsXMLHistory(DwXmlNode node) {
        this();
        loadFromXML(node);
    }

    public iPartsXMLResponse getResponse() {
        return response;
    }

    public void setResponse(iPartsXMLResponse iPartsXMLResponse) {
        this.response = iPartsXMLResponse;
    }

    public iPartsXMLRequest getRequest() {
        return request;
    }

    public void setRequest(iPartsXMLRequest iPartsXMLRequest) {
        this.request = iPartsXMLRequest;
    }

    public void addTimeStamp(iPartsXMLTimeStamp timeStamp) {
        if (timeStamps == null) {
            timeStamps = new ArrayList<iPartsXMLTimeStamp>();
        }
        timeStamps.add(timeStamp);
    }

    public List<iPartsXMLTimeStamp> getTimeStamps() {
        return timeStamps;
    }

    public void setRequestInvalid() {
        this.validRequest = false;
    }

    public void setInvalidRequestAsText(String invalidRequestAsText) {
        this.invalidRequestAsText = invalidRequestAsText;
    }

    public boolean isValidRequest() {
        return validRequest;
    }

    public String getInvalidRequestAsText() {
        return invalidRequestAsText;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode historyNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.HISTORY.getAlias());
        if (response != null) {
            historyNode.appendChild(response.getAsDwXMLNode(namespacePrefix));
        }
        if (request != null) {
            historyNode.appendChild(request.getAsDwXMLNode(namespacePrefix));
        }
        if (timeStamps != null && !timeStamps.isEmpty()) {
            for (iPartsXMLTimeStamp timeStamp : timeStamps) {
                historyNode.appendChild(timeStamp.getAsDwXMLNode(namespacePrefix));
            }
        }
        if (!isValidRequest()) {
            historyNode.setTextContent(invalidRequestAsText);
        }
        return historyNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.HISTORY)) {
                List<DwXmlNode> childNodes = node.getChildNodes();
                for (DwXmlNode childNode : childNodes) {
                    fillHistoryMessage(childNode);
                }
            }
        }
    }

    private void fillHistoryMessage(DwXmlNode node) {
        iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(node);
        if (nodeType == null) {
            return;
        }
        switch (nodeType) {
            case REQUEST:
                iPartsXMLRequest request = new iPartsXMLRequest(node);
                setRequest(request);
                break;
            case RESPONSE:
                iPartsXMLResponse response = new iPartsXMLResponse(node);
                setResponse(response);
                break;
            case TIMESTAMP:
                iPartsXMLTimeStamp timestamp = new iPartsXMLTimeStamp(node);
                addTimeStamp(timestamp);
                break;
            case INVALID_REQUEST:
                setRequestInvalid();
                setInvalidRequestAsText(node.getTextContent());
                break;
        }
    }


}