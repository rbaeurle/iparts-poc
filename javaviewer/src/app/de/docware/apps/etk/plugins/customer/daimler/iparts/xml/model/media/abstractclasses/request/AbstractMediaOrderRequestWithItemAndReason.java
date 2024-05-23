/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLReason;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;

/**
 * Abstrakte Klasse für MediaOrder-Operationen mit mcItemId, mcitemRevId und einem Grund (iPartsXMLReason) von iParts
 * nach AS-PLM (ChangeMediaOrder und AbortMediaOrder).
 * Enthält alle Basiselemente einer Anfrage
 */
public abstract class AbstractMediaOrderRequestWithItemAndReason extends AbstractMediaOrderRequestWithItem {

    private iPartsXMLReason reason; // Muss-Feld

    public AbstractMediaOrderRequestWithItemAndReason(String mcItemId, String mcItemRevId, String reason) {
        super(mcItemId, mcItemRevId);
        setReason(reason);
    }

    public AbstractMediaOrderRequestWithItemAndReason(DwXmlNode node) {
        super(node);
    }

    public iPartsXMLReason getReason() {
        return reason;
    }

    public void setReason(String reason) {
        if (StrUtils.isValid(reason)) {
            this.reason = new iPartsXMLReason(reason);
        }
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode cmoNode = super.getAsDwXMLNode(namespacePrefix);
        if (getReason() != null) {
            DwXmlNode firsChild = cmoNode.getFirstChild();
            DwXmlNode reasonNode = getReason().getAsDwXMLNode(namespacePrefix);
            if (firsChild != null) {
                cmoNode.insertBefore(reasonNode, firsChild);
            } else {
                cmoNode.appendChild(reasonNode);
            }
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "A reason must be set in order to send a ChangeMediaOrder request. It must not be null or empty.");
        }
        return cmoNode;
    }

    @Override
    protected void handleChildNode(DwXmlNode parentNode, DwXmlNode childNode, iPartsTransferNodeTypes nodeType) {
        if (nodeType == iPartsTransferNodeTypes.REASON) {
            reason = new iPartsXMLReason(childNode);
        } else {
            super.handleChildNode(parentNode, childNode, nodeType);
        }
    }
}
