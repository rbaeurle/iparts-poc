/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsEventStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.event.AbstractXMLChangeEvent;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "EventReleaseStatusChange" Element in der Transfer XML
 */
public class iPartsXMLEventReleaseStatusChange extends AbstractXMLChangeEvent {

    private String oldStatus;
    private String newStatus;

    public iPartsXMLEventReleaseStatusChange(String newStatus) {
        this.eventType = iPartsTransferNodeTypes.EVENT_RELEASE_STATUS_CHANGE;
        this.newStatus = newStatus;
    }

    public iPartsXMLEventReleaseStatusChange(DwXmlNode node) {
        this(node.getAttribute(ATTR_EVENT_NEW));
        loadFromXML(node);
    }

    public iPartsEventStates getNewEventState() {
        return iPartsEventStates.getFromAlias(getNewStatusAsString());
    }

    public iPartsEventStates getOldEventState() {
        return iPartsEventStates.getFromAlias(getOldStatusAsString());
    }

    public String getNewStatusAsString() {
        return newStatus;
    }

    public String getOldStatusAsString() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode eventRSNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.EVENT_RELEASE_STATUS_CHANGE.getAlias());
        if (newStatus == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "new status must not be null (ASPLM Schema)!");
        }
        eventRSNode.setAttribute(ATTR_EVENT_NEW, newStatus);
        if ((oldStatus != null) && !oldStatus.isEmpty()) {
            eventRSNode.setAttribute(ATTR_EVENT_OLD, oldStatus);
        }
        if ((getUserId() != null) && !getUserId().isEmpty()) {
            eventRSNode.setAttribute(ATTR_GU_USER_ID, getUserId());
        }
        fillNodeWithCommentsAndTcObject(eventRSNode, namespacePrefix);
        return eventRSNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.EVENT_RELEASE_STATUS_CHANGE)) {
                setUserId(node.getAttribute(ATTR_GU_USER_ID));
                oldStatus = node.getAttribute(ATTR_EVENT_OLD);

                // Kindknoten von EventReleaseStatusChange
                fillCommentsAndTcObject(node.getChildNodes());
            }
        }
    }
}
