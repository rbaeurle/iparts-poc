/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;

/**
 * Abstrakte Klasse für MediaOrder-Operationen mit mcItemId und mcitemRevId von iParts nach AS-PLM (CreateMediaOrder und UpdateMediaOrder).
 * Enthält alle Basiselemente einer Anfrage
 */
public abstract class AbstractMediaOrderRequestWithItem extends AbstractMediaOrderRequest {

    private String mcItemId; // Muss-Feld
    private String mcItemRevId; // Muss-Feld

    protected AbstractMediaOrderRequestWithItem(String mcItemId, String mcItemRevId) {
        setMcItemId(mcItemId);
        setMcItemRevId(mcItemRevId);
    }

    public AbstractMediaOrderRequestWithItem(DwXmlNode node) {
        super(node);
    }


    public String getMcItemId() {
        return mcItemId;
    }

    public void setMcItemId(String itemId) {
        this.mcItemId = itemId;
    }

    public String getMcItemRevId() {
        return mcItemRevId;
    }

    public void setMcItemRevId(String itemRevId) {
        this.mcItemRevId = itemRevId;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode mediaOrderNodeWithIds = super.getAsDwXMLNode(namespacePrefix);
        if (!StrUtils.isValid(mcItemId, mcItemRevId)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "MCItemId and MCItemRevId must be set in order to send a request. It must not be null or empty.");
        }
        if (mcItemId != null) {
            mediaOrderNodeWithIds.setAttribute(ATTR_MC_ITEM_ID, mcItemId);
        }
        if (mcItemRevId != null) {
            mediaOrderNodeWithIds.setAttribute(ATTR_MC_ITEM_REV_ID, mcItemRevId);
        }
        return mediaOrderNodeWithIds;
    }

}
