package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Abstrakte Klasse für XML Elemente mit den Attributen "groupId" und "userId", z.B. Contractor und Requestor
 */
public abstract class AbstractXMLUserAndGroupObject extends AbstractXMLObject {

    private String groupId;
    private String userId;

    public AbstractXMLUserAndGroupObject(String groupId, String userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    public AbstractXMLUserAndGroupObject(DwXmlNode node) {
        loadFromXML(node);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode requestorNode = new DwXmlNode(namespacePrefix + getNodeType().getAlias());
        if (userId != null) {
            requestorNode.setAttribute(ATTR_GU_USER_ID, userId);
        }
        if (groupId != null) {
            requestorNode.setAttribute(ATTR_GU_GROUP_ID, groupId);
        }
        return requestorNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, getNodeType())) {
                groupId = node.getAttribute(ATTR_GU_GROUP_ID);
                userId = node.getAttribute(ATTR_GU_USER_ID);
            }
        }
    }

    protected abstract iPartsTransferNodeTypes getNodeType();
}
