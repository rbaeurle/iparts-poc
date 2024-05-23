/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.AbstractMessageType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.event.AbstractXMLChangeEvent;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;

import java.util.Date;

/**
 * Repräsentiert das "Event" Element in der Transfer XML
 */
public class iPartsXMLEvent extends AbstractMessageType {

    private AbstractXMLChangeEvent actualEvent;

    public iPartsXMLEvent(String fromParticipant, Date when) {
        this.messageType = iPartsTransferNodeTypes.EVENT;
        this.fromParticipant = fromParticipant;
        this.when = XMLImportExportDateHelper.getISOFormattedDateTimeAsString(when);
    }

    public iPartsXMLEvent(DwXmlNode node) {
        this("", null);
        loadFromXML(node);
    }

    public AbstractXMLChangeEvent getActualEvent() {
        return actualEvent;
    }

    public void setActualEvent(AbstractXMLChangeEvent actualEvent) {
        this.actualEvent = actualEvent;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode eventNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.EVENT.getAlias());
        if (fromParticipant != null) {
            eventNode.setAttribute(ATTR_GRR_FROM, fromParticipant);
        }
        if (when != null) {
            eventNode.setAttribute(ATTR_GRR_WHEN, when);
        }
        eventNode.appendChild(actualEvent.getAsDwXMLNode(namespacePrefix));
        return eventNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.EVENT)) {
                fromParticipant = node.getAttribute(ATTR_GRR_FROM);
                when = node.getAttribute(ATTR_GRR_WHEN);
                //Kindknoten von Request
                DwXmlNode childNode = node.getFirstChild();
                iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(childNode);
                // Kann ein Event nicht bestimmt werden, wird ein Platzhalter Event erzeugt. Dadurch kann später
                // der ganz normale Schema-Check und die Ausgabe der Originaldaten erfolgen.
                if (nodeType == null) {
                    nodeType = iPartsTransferNodeTypes.EVENT_UNKNOWN;
                }
                switch (nodeType) {
                    case EVENT_ASSIGNMENT_CHANGE:
                        setActualEvent(new iPartsXMLEventAssingmentChange(childNode));
                        break;
                    case EVENT_RELEASE_STATUS_CHANGE:
                        setActualEvent(new iPartsXMLEventReleaseStatusChange(childNode));
                        break;
                    default:
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Event \"" + childNode.getName()
                                                                                   + "\" does not exist in our workflow!");
                        setActualEvent(new iPartsXMLUnknownEvent(childNode.getName()));
                        break;
                }
            }
        }
    }
}
