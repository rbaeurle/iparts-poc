/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractMediaOrderRequestWithItemAndReason;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLMediaVariant;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.collections.dwlist.DwList;

import java.util.Date;
import java.util.List;

/**
 * Repräsentiert das "ChangeMediaOrder" Element in der Transfer XML
 */
public class iPartsXMLChangeMediaOrder extends AbstractMediaOrderRequestWithItemAndReason {

    private List<iPartsXMLMediaVariant> mediaVariants;
    private boolean isCopy;

    public iPartsXMLChangeMediaOrder(String mcItemId, String mcItemRevId, String reason, iPartsXMLContractor contractor,
                                     Date dateDue) {
        super(mcItemId, mcItemRevId, reason);
        setRequiredChangeOrderValues(contractor, dateDue);
    }

    public iPartsXMLChangeMediaOrder(DwXmlNode node) {
        super(node);
    }

    /**
     * Setzt die nötigen Pflichfelder
     *
     * @param contractor
     * @param dateDue
     */
    private void setRequiredChangeOrderValues(iPartsXMLContractor contractor, Date dateDue) {
        if ((contractor != null) && (dateDue != null)) {
            setContractor(contractor);
            setDateDue(dateDue);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Required values for ChangeMediaOrder must not be null or empty");
        }
    }

    @Override
    protected void initMediaOrderRequest() {
        mediaVariants = new DwList<>();
        setOperationType(iPartsTransferNodeTypes.CHANGE_MEDIA_ORDER);
    }

    @Override
    protected void addXmlNodes(DwXmlNode cmoNode, String namespacePrefix) {
        super.addXmlNodes(cmoNode, namespacePrefix);
        if (!mediaVariants.isEmpty()) {
            for (iPartsXMLMediaVariant mediaVariant : mediaVariants) {
                cmoNode.appendChild(mediaVariant.getAsDwXMLNode(namespacePrefix));
            }
        }
    }

    @Override
    protected void handleChildNode(DwXmlNode parentNode, DwXmlNode childNode, iPartsTransferNodeTypes nodeType) {
        if (nodeType == iPartsTransferNodeTypes.MEDIA_VARIANT) {
            mediaVariants.add(new iPartsXMLMediaVariant(childNode));
        } else {
            super.handleChildNode(parentNode, childNode, nodeType);
        }
    }

    /**
     * Fügt dem ChangeMediaOrder-Element eine Media-Variante hinzu.
     *
     * @param mediaVariant
     */
    public void addMediaVariant(iPartsXMLMediaVariant mediaVariant) {
        if (mediaVariant != null) {
            mediaVariants.add(mediaVariant);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "MediaVariant value must not be null");
        }
    }


    public List<iPartsXMLMediaVariant> getMediaVariants() {
        return mediaVariants;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode changeMediaOrderNode = super.getAsDwXMLNode(namespacePrefix);
        if (isCopy) {
            changeMediaOrderNode.setAttribute(ATTR_IS_COPY, String.valueOf(true));
        }
        return changeMediaOrderNode;
    }

    public boolean isCopy() {
        return isCopy;
    }

    public void setCopy(boolean copy) {
        isCopy = copy;
    }
}
