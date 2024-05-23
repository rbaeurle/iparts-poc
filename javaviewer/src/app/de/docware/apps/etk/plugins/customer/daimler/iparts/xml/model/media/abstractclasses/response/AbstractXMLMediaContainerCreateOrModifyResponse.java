/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLMediaContainer;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLMediaOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLSuccess;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Abstrakte Repräsentiert eines XML Elements/Objekts, das Media Container erstellen oder modifizieren kann
 */
public abstract class AbstractXMLMediaContainerCreateOrModifyResponse extends AbstractXMLResponseOperation {

    protected iPartsXMLMediaContainer mContainer;
    protected iPartsXMLMediaOrder mOrder;
    protected List<iPartsXMLSuccess> successes;

    public AbstractXMLMediaContainerCreateOrModifyResponse(iPartsTransferNodeTypes resultType, DwXmlNode node) {
        this.resultType = resultType;
        successes = new DwList<>();
        loadFromXML(node);
    }

    public iPartsXMLMediaContainer getMContainer() {
        return mContainer;
    }

    public void setMContainer(iPartsXMLMediaContainer mContainer) {
        this.mContainer = mContainer;
    }


    public void setMOrder(iPartsXMLMediaOrder mOrder) {
        this.mOrder = mOrder;
    }

    public iPartsXMLMediaOrder getMOrder() {
        return mOrder;
    }


    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode rcmoNode = new DwXmlNode(namespacePrefix + resultType.getAlias());
        if (mOrder != null) {
            rcmoNode.appendChild(mOrder.getAsDwXMLNode(namespacePrefix));
        }
        if (mContainer != null) {
            rcmoNode.appendChild(mContainer.getAsDwXMLNode(namespacePrefix));
        }
        if ((successes != null) && !successes.isEmpty()) {
            for (iPartsXMLSuccess success : successes) {
                rcmoNode.appendChild(success.getAsDwXMLNode(namespacePrefix));
            }
        }
        return rcmoNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, resultType)) {
                List<DwXmlNode> childNodes = node.getChildNodes();
                for (DwXmlNode childNode : childNodes) {
                    iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(childNode);
                    if (nodeType == null) {
                        continue;
                    }
                    switch (nodeType) {
                        case MEDIA_ORDER:
                            setMOrder(new iPartsXMLMediaOrder(childNode));
                            break;
                        case MEDIA_CONTAINER:
                            setMContainer(new iPartsXMLMediaContainer(childNode));
                            break;
                        case SUCCESS:
                            successes.add(new iPartsXMLSuccess(childNode));
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void convertToNotificationOnly() {
        super.convertToNotificationOnly();
        if (mContainer != null) {
            mContainer.convertToNotificationOnly();
        }
    }

    public List<iPartsXMLSuccess> getSuccesses() {
        return successes;
    }

    public boolean hasErrors() {
        for (iPartsXMLSuccess success : successes) {
            if (!success.isErrorFree()) {
                return true;
            }
        }
        return false;
    }
}
