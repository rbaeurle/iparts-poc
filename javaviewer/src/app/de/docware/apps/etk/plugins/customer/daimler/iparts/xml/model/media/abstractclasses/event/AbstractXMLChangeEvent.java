/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.event;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event.iPartsXMLComment;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event.iPartsXMLTcObject;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;

import java.util.LinkedList;
import java.util.List;

/**
 * Abstrakt Klasse für iParts spezifische Event-Typen
 */
public abstract class AbstractXMLChangeEvent extends AbstractXMLObject {

    private iPartsXMLTcObject tcObject;
    private List<iPartsXMLComment> comments = new LinkedList<iPartsXMLComment>();
    private String userId;

    protected iPartsTransferNodeTypes eventType;

    public iPartsTransferNodeTypes getEventType() {
        return eventType;
    }

    public void setEventType(iPartsTransferNodeTypes eventType) {
        this.eventType = eventType;
    }

    public iPartsXMLTcObject getTcObject() {
        return tcObject;
    }

    public void setTcObject(iPartsXMLTcObject tcObject) {
        this.tcObject = tcObject;
    }

    public List<iPartsXMLComment> getComments() {
        return comments;
    }

    public void setComments(List<iPartsXMLComment> comments) {
        this.comments = comments;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Befüllt den übergebenen Knoten mit den Informationen zum TCObjekt und den Kommentaren
     *
     * @param node
     * @param namespacePrefix
     */
    protected void fillNodeWithCommentsAndTcObject(DwXmlNode node, String namespacePrefix) {
        iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(node);
        if ((nodeType == iPartsTransferNodeTypes.EVENT_RELEASE_STATUS_CHANGE) || (nodeType == iPartsTransferNodeTypes.EVENT_ASSIGNMENT_CHANGE)) {
            node.appendChild(getTcObject().getAsDwXMLNode(namespacePrefix));
            addCommentsToNode(node, namespacePrefix);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Element " + nodeType + " for " + eventType.getAlias() + " does not exist in ASPLM Schema!");

        }
    }

    protected void addCommentsToNode(DwXmlNode node, String namespacePrefix) {
        for (iPartsXMLComment comment : getComments()) {
            node.appendChild(comment.getAsDwXMLNode(namespacePrefix));
        }
    }

    /**
     * Fügt dem Event einen Kommentar hinzu
     *
     * @param comment
     */
    public void addComment(iPartsXMLComment comment) {
        if (comment != null) {
            comments.add(comment);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Comment must not be null!");
        }
    }

    protected void fillCommentsAndTcObject(List<DwXmlNode> childNodes) {
        for (DwXmlNode childNode : childNodes) {
            iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(childNode);
            if (nodeType == null) {
                continue;
            }
            switch (nodeType) {
                case TC_OBJECT:
                    if (tcObject != null) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "MQ event can only hold one TcObject (ASPLM Schema)! fillCommentsAndTcObject tried to overwrite an existing instance.");
                    } else {
                        setTcObject(new iPartsXMLTcObject(childNode));
                    }
                    break;
                case COMMENT:
                    addComment(new iPartsXMLComment(childNode));
                    break;
                default:
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Element " + nodeType + " for " + eventType.getAlias() + " does not exist in ASPLM Schema!");
                    break;
            }
        }

    }
}
