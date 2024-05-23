/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLResponseOperation;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "ResGetMediaContents" Element in der Transfer XML
 */
public class iPartsXMLResGetMediaContents extends AbstractXMLResponseOperation {

    private iPartsXMLMediaContainer mContainer;

    public iPartsXMLResGetMediaContents() {
        resultType = iPartsTransferNodeTypes.RES_GET_MEDIA_CONTENTS;
    }

    public iPartsXMLResGetMediaContents(DwXmlNode node) {
        this();
        loadFromXML(node);
    }

    public iPartsXMLMediaContainer getmContainer() {
        return mContainer;
    }

    public void setmContainer(iPartsXMLMediaContainer mContainer) {
        this.mContainer = mContainer;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode rgmcNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.RES_GET_MEDIA_CONTENTS.getAlias());
        if (mContainer != null) {
            rgmcNode.appendChild(mContainer.getAsDwXMLNode(namespacePrefix));
        }
        return rgmcNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.RES_GET_MEDIA_CONTENTS)) {
                DwXmlNode childNode = node.getFirstChild();
                if (checkNodeType(childNode, iPartsTransferNodeTypes.MEDIA_CONTAINER)) {
                    setmContainer(new iPartsXMLMediaContainer(childNode));
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
}
