/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;

import java.util.Date;

/**
 * Repräsentiert das "PartNumber" Element in der Transfer XML
 */
public class iPartsXMLPartNumber extends AbstractXMLObject {

    private String zgs;
    private Date releaseDate;
    private String partNumber;

    public iPartsXMLPartNumber(String zgs, Date releaseDate, String partNumber) {
        this.zgs = zgs;
        this.releaseDate = releaseDate;
        this.partNumber = partNumber;
    }

    public iPartsXMLPartNumber(DwXmlNode node) {
        loadFromXML(node);
    }

    public String getZgs() {
        return zgs;
    }

    public String getReleaseDateAsString() {
        return XMLImportExportDateHelper.getISOFormattedDateTimeAsString(releaseDate);
    }

    public Date getReleaseDateAsDate() {
        return releaseDate;
    }

    public String getPartNumberText() {
        return partNumber;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode partNumberNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.PART_NUMBER.getAlias());
        // Zeichnungsgeometriestand
        if (StrUtils.isValid(zgs)) {
            String numericZGS;
            if (zgs.length() >= 3) {
                numericZGS = zgs.substring((zgs.length() - 3));
            } else {
                numericZGS = StrUtils.leftFill(zgs, 3, '0'); // AS-PLM erwartet den ZGS 3-stellig
            }

            // Falls die letzten 3 Stellen numerisch sind -> versenden
            if (StrUtils.isDigit(numericZGS)) {
                int intValue = StrUtils.strToIntDef(numericZGS, -1);
                if (intValue >= 0) {
                    partNumberNode.setAttribute(CMO_ZGS, numericZGS);
                } else {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Could not transform ZGS value into a numeric value. ZGS value: " + zgs);
                }
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "ZGS value contains non-numeric characters. ZGS value: " + zgs);
            }
        }
        // Freigabetermin
        if (releaseDate != null) {
            partNumberNode.setAttribute(CMO_RELEASE_DATE, XMLImportExportDateHelper.getISOFormattedDateTimeAsString(releaseDate));
        }
        // Teilenummer
        if (partNumber != null) {
            partNumberNode.setTextContent(partNumber);
        }
        return partNumberNode;

    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.PART_NUMBER)) {
                zgs = node.getAttribute(iPartsTransferConst.CMO_ZGS);
                releaseDate = XMLImportExportDateHelper.getISOFormattedDateTimeAsDate(node.getAttribute(iPartsTransferConst.CMO_RELEASE_DATE));
                partNumber = node.getTextContent();
            }
        }
    }
}
