/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.event.AbstractXMLChangeEvent;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;

/**
 * Platzhalter Event für AS-PLM Events, die uns nicht bekannt sind aber trotzdem an uns verschickt werden
 */
public class iPartsXMLUnknownEvent extends AbstractXMLChangeEvent {

    public iPartsXMLUnknownEvent(String name) {
        this.eventType = iPartsTransferNodeTypes.EVENT_UNKNOWN;
        addUnknownEventAsComment(name);
    }

    /**
     * Speichert den unbekannten Eventtyp als Kommentar
     *
     * @param name
     */
    private void addUnknownEventAsComment(String name) {
        if (StrUtils.isValid(name)) {
            addComment(new iPartsXMLComment("Unknown event type: " + name));
        }
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode node = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.EVENT.getAlias());
        if (StrUtils.isValid(getUserId())) {
            node.setAttribute(ATTR_GU_USER_ID, getUserId());
        }
        addCommentsToNode(node, namespacePrefix);
        return node;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.EVENT_UNKNOWN)) {
                setUserId(node.getAttribute(ATTR_GU_USER_ID));
                // Kindknoten von EventReleaseStatusChange
                fillCommentsAndTcObject(node.getChildNodes());
            }
        }
    }
}
