/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLRequestOperation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLMediaVariant;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Repräsentiert das "CorrectMediaOrder" Element in der Transfer XML
 */
public class iPartsXMLCorrectMediaOrder extends AbstractXMLRequestOperation {

    private String mcItemId; // Muss-Feld
    private String mcItemRevId; // Muss-Feld
    private iPartsXMLReason reason; // Muss-Feld
    private iPartsXMLContractor contractor;
    private Date dateDue;
    private List<iPartsXMLMediaVariant> mediaVariants;

    public iPartsXMLCorrectMediaOrder(String mcItemId, String mcItemRevId) {
        setOperationType(iPartsTransferNodeTypes.CORRECT_MEDIA_ORDER);
        this.mcItemId = mcItemId;
        this.mcItemRevId = mcItemRevId;
        this.mediaVariants = new ArrayList<iPartsXMLMediaVariant>();
    }

    public iPartsXMLCorrectMediaOrder(DwXmlNode node) {
        this("", "");
        loadFromXML(node);
    }

    public String getMcItemId() {
        return mcItemId;
    }

    public void setMcItemId(String itemId) {
        this.mcItemId = itemId;
    }

    public String getMcItemRevId() {
        return mcItemRevId;
    }

    public void setMcItemRevId(String itemRevId) {
        this.mcItemRevId = itemRevId;
    }

    public iPartsXMLContractor getContractor() {
        return contractor;
    }

    public void setContractor(iPartsXMLContractor contractor) {
        this.contractor = contractor;
    }

    public iPartsXMLReason getReason() {
        return reason;
    }

    public void setReason(iPartsXMLReason reason) {
        this.reason = reason;
    }

    public Date getDateDue() {
        return dateDue;
    }

    public void setDateDue(Date dateDue) {
        this.dateDue = dateDue;
    }

    /**
     * Fügt dem CorrectMediaOrder Element ein Media-Variante hinzu.
     *
     * @param mediaVariant
     */
    public void addMediaVariant(iPartsXMLMediaVariant mediaVariant) {
        if (mediaVariant != null) {
            mediaVariants.add(mediaVariant);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "MediaVariant value must not be null");
        }
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode comoNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.CORRECT_MEDIA_ORDER.getAlias());
        if (mcItemId != null) {
            comoNode.setAttribute(ATTR_MC_ITEM_ID, mcItemId);
        }
        if (mcItemRevId != null) {
            comoNode.setAttribute(ATTR_MC_ITEM_REV_ID, mcItemRevId);
        }
        if (reason != null) {
            comoNode.appendChild(reason.getAsDwXMLNode(namespacePrefix));
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "A reason must be set in order to sent a CorrectMediaOrder request. It must not be null or empty");
        }
        if (contractor != null) {
            comoNode.appendChild(contractor.getAsDwXMLNode(namespacePrefix));
        }
        if (dateDue != null) {
            SimpleDateFormat formatter = new SimpleDateFormat(DATE_DUE_DATEFORMAT);
            DwXmlNode comoChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.DATE_DUE.getAlias(), formatter.format(dateDue));
            comoNode.appendChild(comoChildNode);
        }
        for (iPartsXMLMediaVariant mediaVariant : mediaVariants) {
            comoNode.appendChild(mediaVariant.getAsDwXMLNode(namespacePrefix));
        }
        return comoNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.CORRECT_MEDIA_ORDER)) {
                mcItemId = node.getAttribute(ATTR_MC_ITEM_ID);
                mcItemRevId = node.getAttribute(ATTR_MC_ITEM_REV_ID);
            }
            fillCorrectMediaOrder(node.getChildNodes());
        }
    }

    private void fillCorrectMediaOrder(List<DwXmlNode> childNodes) {
        for (DwXmlNode childNode : childNodes) {
            iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(childNode);
            if (nodeType == null) {
                continue;
            }
            switch (nodeType) {
                case CONTRACTOR:
                    setContractor(new iPartsXMLContractor(childNode));
                    break;
                case DATE_DUE:
                    SimpleDateFormat formatter = new SimpleDateFormat(iPartsTransferConst.DATE_DUE_DATEFORMAT);
                    try {
                        setDateDue(formatter.parse(childNode.getTextContent()));
                    } catch (ParseException e) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Error while parsing String to Date: " + e);
                    }
                    break;
                case REASON:
                    setReason(new iPartsXMLReason(childNode));
                    break;
                case MEDIA_VARIANT:
                    addMediaVariant(new iPartsXMLMediaVariant(childNode));
                    break;
                default:
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Wrong child for CorrectMediaOrder object! Nodetype: " + nodeType);
            }
        }
    }

    public List<iPartsXMLMediaVariant> getMediaVariants() {
        return mediaVariants;
    }

}
