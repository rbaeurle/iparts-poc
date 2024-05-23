/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLObjectWithMCAttributes;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "GetMediaPreview" Element in der Transfer XML
 */
public class iPartsXMLGetMediaPreview extends AbstractXMLObjectWithMCAttributes {


    public iPartsXMLGetMediaPreview(String mcItemId, String mcItemRevId) {
        setOperationType(iPartsTransferNodeTypes.GET_MEDIA_PREVIEW);
        this.mcItemId = mcItemId;
        this.mcItemRevId = mcItemRevId;

    }

    public iPartsXMLGetMediaPreview(DwXmlNode node) {
        this("", "");
        loadFromXML(node);
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode mediaPreviewNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.GET_MEDIA_PREVIEW);
        if (mcItemId != null) {
            mediaPreviewNode.setAttribute(ATTR_MC_ITEM_ID, mcItemId);
        }
        if (mcItemRevId != null) {
            mediaPreviewNode.setAttribute(ATTR_MC_ITEM_REV_ID, mcItemRevId);
        }
        return mediaPreviewNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.GET_MEDIA_PREVIEW)) {
                mcItemId = node.getAttribute(ATTR_MC_ITEM_ID);
                mcItemRevId = node.getAttribute(ATTR_MC_ITEM_REV_ID);
            }
        }

    }

}
