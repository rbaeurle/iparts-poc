/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsRelationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.XMLObjectCreationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLObjectWithMCAttributes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;

import java.util.List;
import java.util.Set;

/**
 * Repräsentiert das "GetMediaContents" Element in der Transfer XML
 */
public class iPartsXMLGetMediaContents extends AbstractXMLObjectWithMCAttributes {

    private iPartsXMLRelation relation;

    public iPartsXMLGetMediaContents(String mcItemId, String mcItemRevId) {
        setOperationType(iPartsTransferNodeTypes.GET_MEDIA_CONTENTS);
        this.mcItemId = mcItemId;
        this.mcItemRevId = mcItemRevId;
        // Ist die Adminoption für reduzierte SVGs gesetzt, muss das in Form des <Relation> Elements an AS-PLM übermittelt werden
        if (MQHelper.isDerivedSVGsMQRequest()) {
            setRelation(new iPartsXMLRelation(iPartsRelationType.DERIVED));
        }
    }

    public iPartsXMLGetMediaContents(DwXmlNode node) {
        this("", "");
        loadFromXML(node);
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode mediaContentsNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.GET_MEDIA_CONTENTS);
        if (mcItemId != null) {
            mediaContentsNode.setAttribute(ATTR_MC_ITEM_ID, mcItemId);
        }
        if (mcItemRevId != null) {
            mediaContentsNode.setAttribute(ATTR_MC_ITEM_REV_ID, mcItemRevId);
        }
        addFileTypesAttribute(mediaContentsNode);
        if (relation != null) {
            mediaContentsNode.appendChild(relation.getAsDwXMLNode(namespacePrefix));
        }
        return mediaContentsNode;
    }

    /**
     * Fügt die fest definierten Dateitypen zur Bildanfrage hinzu. Dateitypen können über den Admin-Modus vorgegeben werden.
     *
     * @param mediaContentsNode
     */
    private void addFileTypesAttribute(DwXmlNode mediaContentsNode) {
        Set<String> fileTypes = XMLObjectCreationHelper.getInstance().getFileTypesForGetMediaContents();
        // Wurde im Admin-Modus keine Dateiendung angegeben, dann frage die Bilder ohne vorgegebene Dateiendungen an
        // ("fileTypes" Attribut leer = alle Dateitypen werden angefragt)
        if ((fileTypes == null) || fileTypes.isEmpty()) {
            return;
        }
        String fileTypesAttValue = StrUtils.stringListToString(fileTypes, " ");
        mediaContentsNode.setAttribute(ATTR_MC_FILETYPES, fileTypesAttValue);
    }

    public iPartsXMLRelation getRelation() {
        return relation;
    }

    public void setRelation(iPartsXMLRelation relation) {
        this.relation = relation;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.GET_MEDIA_CONTENTS)) {
                mcItemId = node.getAttribute(ATTR_MC_ITEM_ID);
                mcItemRevId = node.getAttribute(ATTR_MC_ITEM_REV_ID);
                List<DwXmlNode> childNodes = node.getChildNodes();
                for (DwXmlNode childNode : childNodes) {
                    iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(childNode);
                    if (nodeType == null) {
                        continue;
                    }
                    if (nodeType == iPartsTransferNodeTypes.RELATION) {
                        setRelation(new iPartsXMLRelation(childNode));
                    } else {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Unknown child for GetMediaContents object! Node type: "
                                                                                   + nodeType);
                    }
                }
            }
        }
    }
}
