/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.xml.DwXmlNode;

import java.text.ParseException;
import java.util.Date;

/**
 * Repräsentiert das "Timestamp" Element in der Transfer XML
 */
public class iPartsXMLTimeStamp extends AbstractXMLObject {

    private String creator;
    private String event;
    private String dateTime;

    public iPartsXMLTimeStamp(String creator, String event, String dateTime) {
        this.creator = creator;
        this.event = event;
        this.dateTime = dateTime;
    }

    public iPartsXMLTimeStamp(String creator, String event, Date dateTime) {
        this.creator = creator;
        this.event = event;
        this.dateTime = XMLImportExportDateHelper.getISOFormattedDateTimeAsString(dateTime);
    }

    public iPartsXMLTimeStamp(DwXmlNode node) {
        loadFromXML(node);
    }

    public String getCreator() {
        return creator;
    }

    public String getEvent() {
        return event;
    }

    public String getDateTime() {
        return dateTime;
    }

    public Date getDateTimeAsDate() throws ParseException {
        return XMLImportExportDateHelper.getISOFormattedDateTimeAsDate(dateTime);
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode timeStampNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.TIMESTAMP.getAlias());
        if (creator != null) {
            timeStampNode.setAttribute(iPartsTransferConst.ATTR_TIMESTAMP_CREATOR, creator);
        }
        if (event != null) {
            timeStampNode.setAttribute(iPartsTransferConst.ATTR_TIMESTAMP_EVENT, event);
        }
        timeStampNode.setTextContent(dateTime);
        return timeStampNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.TIMESTAMP)) {
                creator = node.getAttribute(ATTR_TIMESTAMP_CREATOR);
                event = node.getAttribute(ATTR_TIMESTAMP_EVENT);
                dateTime = node.getTextContent();
            }
        }
    }
}
