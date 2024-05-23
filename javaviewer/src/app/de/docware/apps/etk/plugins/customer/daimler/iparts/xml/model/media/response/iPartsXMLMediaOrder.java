/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.date.DateUtils;

import java.util.Date;

/**
 * Repräsentiert das "MediaOrder" Element in der Transfer XML
 */
public class iPartsXMLMediaOrder extends AbstractXMLObject {

    private Date dateOrdered;

    public iPartsXMLMediaOrder(String dateOrdered) {
        this.dateOrdered = XMLImportExportDateHelper.getISOFormattedDateTimeAsDate(dateOrdered);
    }

    public iPartsXMLMediaOrder(Date dateOrdered) {
        this.dateOrdered = dateOrdered;
    }

    public iPartsXMLMediaOrder(DwXmlNode node) {
        this(node.getAttribute(ATTR_GRP_MEDIA_DATE_ORDERED));
        loadFromXML(node);
    }

    /**
     * Gibt das Order datum als ASPLM String zurück
     *
     * @return
     */
    public String getDateOrderedAsASPLMValue() {
        return XMLImportExportDateHelper.getISOFormattedDateTimeAsString(dateOrdered);
    }

    /**
     * Gibt das Order Datum als Date Objekt zurück
     *
     * @return
     */
    public Date getDateOrderedAsDateObject() {
        return dateOrdered;
    }

    /**
     * Gibt das Order Date als DB Wert zurück ("yyyyMMddHHmmss")
     *
     * @return
     */
    public String getDateOrderedAsDBValue() {
        return DateUtils.toyyyyMMddHHmmss_DateTime(dateOrdered);
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode mediaOrderNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.MEDIA_ORDER.getAlias());
        String dateOrderedString = getDateOrderedAsASPLMValue();
        if ((dateOrderedString != null) && !dateOrderedString.isEmpty()) {
            mediaOrderNode.setAttribute(ATTR_GRP_MEDIA_DATE_ORDERED, dateOrderedString);
        } else {
            Logger.getLogger().throwRuntimeException("Tag \"dateOrdered\" is a required value. It must not be null or empty.");
        }
        return mediaOrderNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        // keine weiteren Aktionen nötig, da alles im Konstruktor passiert
    }
}
