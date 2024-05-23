/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert das "Comment" Element in der Transfer XML
 */
public class iPartsXMLComment extends AbstractXMLObject {

    private String time;
    private String userID;
    private String comment;

    public iPartsXMLComment(String comment) {
        this.comment = comment;
    }

    public iPartsXMLComment(DwXmlNode node) {
        loadFromXML(node);
    }

    public String getComment() {
        return comment;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode commentNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.COMMENT.getAlias());
        if ((time != null) && !time.isEmpty()) {
            commentNode.setAttribute(ATTR_TIME, time);
        }
        if ((userID != null) && !userID.isEmpty()) {
            commentNode.setAttribute(ATTR_GU_USER_ID, userID);
        }
        if ((comment != null) && !comment.isEmpty()) {
            commentNode.setTextContent(comment);
        }
        return commentNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.COMMENT)) {
                time = node.getAttribute(ATTR_TIME);
                userID = node.getAttribute(ATTR_GU_USER_ID);
                comment = node.getTextContent();
            }
        }
    }
}
