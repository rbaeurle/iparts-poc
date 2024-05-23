/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.event.AbstractXMLChangeEvent;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "EventAssingmentChange" Element in der Transfer XML
 */
public class iPartsXMLEventAssingmentChange extends AbstractXMLChangeEvent {

    private String groupId;
    private String workflowName;

    public iPartsXMLEventAssingmentChange(String groupId) {
        this.groupId = groupId;
        this.eventType = iPartsTransferNodeTypes.EVENT_ASSIGNMENT_CHANGE;
    }

    public iPartsXMLEventAssingmentChange(DwXmlNode node) {
        this(node.getAttribute(ATTR_GU_GROUP_ID));
        loadFromXML(node);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode eventANode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.EVENT_ASSIGNMENT_CHANGE.getAlias());
        if ((groupId == null) || groupId.isEmpty()) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "GroupId must not be null or empty (ASPLM Schema)!");
        }
        eventANode.setAttribute(ATTR_GU_GROUP_ID, getGroupId());
        if (workflowName != null) {
            eventANode.setAttribute(ATTR_EAC_WORKFLOW, workflowName);
        }
        if (getUserId() != null) {
            eventANode.setAttribute(ATTR_GU_USER_ID, getUserId());
        }
        fillNodeWithCommentsAndTcObject(eventANode, namespacePrefix);
        return eventANode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.EVENT_ASSIGNMENT_CHANGE)) {
                groupId = node.getAttribute(ATTR_GU_GROUP_ID);
                setUserId(node.getAttribute(ATTR_GU_USER_ID));
                workflowName = node.getAttribute(ATTR_EAC_WORKFLOW);

                //Kindknoten von EventAssignmentChange
                fillCommentsAndTcObject(node.getChildNodes());
            }
        }
    }
}
