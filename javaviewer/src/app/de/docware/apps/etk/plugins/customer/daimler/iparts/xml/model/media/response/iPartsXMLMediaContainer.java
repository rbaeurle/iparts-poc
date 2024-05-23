/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repräsentiert das "MediaContainer" Element in der Transfer XML
 */
public class iPartsXMLMediaContainer extends AbstractXMLObject {

    private String mcItemId;
    private String mcItemRevId;
    private Map<String, String> attElements;
    private List<iPartsXMLMediaVariant> mediaVariants;
    private Date lastModified;

    public iPartsXMLMediaContainer(String mcItemId, String mcItemRevId) {
        this.mcItemId = mcItemId;
        this.mcItemRevId = mcItemRevId;
        this.attElements = new HashMap<String, String>();
        this.mediaVariants = new DwList<iPartsXMLMediaVariant>();
    }

    public iPartsXMLMediaContainer(DwXmlNode node) {
        this("", "");
        loadFromXML(node);
    }

    public String getMcItemId() {
        return mcItemId;
    }

    public String getMcItemRevId() {
        return mcItemRevId;
    }

    public void addAttrElement(String attValue, String textValue) {
        if ((attValue != null && !attValue.isEmpty()) && (textValue != null && !textValue.isEmpty())) {
            attElements.put(attValue, textValue);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Attribute name or value must not be null");
        }
    }

    public void addMediaVariant(iPartsXMLMediaVariant mediaVariant) {
        if (mediaVariant != null) {
            mediaVariants.add(mediaVariant);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "MediaVariant value must not be null");
        }
    }

    public Map<String, String> getAttElements() {
        return attElements;
    }

    public List<iPartsXMLMediaVariant> getMediaVariants() {
        return mediaVariants;
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode mediaContainerNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.MEDIA_CONTAINER.getAlias());
        if (mcItemId == null || mcItemId.isEmpty()) {
            Logger.getLogger().throwRuntimeException("McItemId is a required value. It must not be null or empty.");
        }
        mediaContainerNode.setAttribute(ATTR_MC_ITEM_ID, mcItemId);
        mediaContainerNode.setAttribute(ATTR_MC_ITEM_REV_ID, mcItemRevId);
        if (lastModified != null) {
            mediaContainerNode.setAttribute(ATTR_LAST_MODFIED, getLastModfiedAsASPLMValue());
        }
        for (String attValue : attElements.keySet()) {
            DwXmlNode mCChildNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.MEDIA_CONTAINER_ATTRIBUTE, attElements.get(attValue));
            mCChildNode.setAttribute(ATTR_NAME, attValue);
            mediaContainerNode.appendChild(mCChildNode);
        }

        for (iPartsXMLMediaVariant mediaVariant : mediaVariants) {
            mediaContainerNode.appendChild(mediaVariant.getAsDwXMLNode(namespacePrefix));
        }
        return mediaContainerNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.MEDIA_CONTAINER)) {
                this.mcItemId = StrUtils.removeWhitespace(node.getAttribute(iPartsTransferConst.ATTR_MC_ITEM_ID));
                this.mcItemRevId = StrUtils.removeWhitespace(node.getAttribute(iPartsTransferConst.ATTR_MC_ITEM_REV_ID));
                String lastModifiedString = node.getAttribute(ATTR_LAST_MODFIED);
                if (!StrUtils.isEmpty(lastModifiedString)) {
                    lastModified = XMLImportExportDateHelper.getISOFormattedDateTimeAsDate(lastModifiedString);
                }
                List<DwXmlNode> childNodes = node.getChildNodes();
                if (childNodes != null && (!childNodes.isEmpty())) {
                    fillMediaContainer(childNodes);
                }
            }
        }
    }

    private void fillMediaContainer(List<DwXmlNode> childNodes) {
        for (DwXmlNode node : childNodes) {
            iPartsTransferNodeTypes nodeType = XMLImportExportHelper.getIPartsNodeType(node);
            if (nodeType == null) {
                continue;
            }
            switch (nodeType) {
                case MEDIA_VARIANT:
                    addMediaVariant(new iPartsXMLMediaVariant(node));
                    break;
                case MEDIA_CONTAINER_ATTRIBUTE:
                    String attValue = node.getAttribute(iPartsTransferConst.ATTR_NAME);
                    addAttrElement(attValue, node.getTextContent());
                    break;
            }
        }
    }

    /**
     * Wird aufgerufen, wenn diese Nachricht nur als Mitteilung ohne Binärdaten dienen soll und keine Datenbankaktionen
     * basierend auf dieser Nachricht stattfinden sollen.
     */
    public void convertToNotificationOnly() {
        for (iPartsXMLMediaVariant mediaVariant : mediaVariants) {
            mediaVariant.convertToNotificationOnly();
        }
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Gibt das LastMofified datum als ASPLM String zurück
     *
     * @return
     */
    public String getLastModfiedAsASPLMValue() {
        return XMLImportExportDateHelper.getISOFormattedDateTimeAsString(lastModified);
    }

    /**
     * Gibt das LastMofified Datum als Date Objekt zurück
     *
     * @return
     */
    public Date getLastModifiedAsDateObject() {
        return lastModified;
    }

    /**
     * Gibt das LastModfied Date als DB Wert zurück ("yyyyMMddHHmmss")
     *
     * @return
     */
    public String getLastMofifiedAsDBValue() {
        return DateUtils.toyyyyMMddHHmmss_DateTime(lastModified);
    }

    public String getAttValueForAttName(String attName) {
        if ((attElements == null) || attElements.isEmpty()) {
            return null;
        }
        return attElements.get(attName);
    }

    public iPartsXMLMediaVariant findPreferredVariantByVariantType() {
        iPartsXMLMediaVariant preferredVariant = null;
        for (iPartsXMLMediaVariant variant : getMediaVariants()) {
            switch (variant.getColorType()) {
                case COLOR:
                    return variant;
                case BLACK_WHITE:
                    preferredVariant = variant;
                    break;
                case NEUTRAL:
                    if (preferredVariant == null) {
                        preferredVariant = variant;
                    }
                    break;
            }
        }
        return preferredVariant;
    }

    public iPartsXMLMediaVariant findPreferredVariantByFileType() {
        iPartsXMLMediaVariant preferredVariant = null;
        for (iPartsXMLMediaVariant variant : getMediaVariants()) {
            if (variant.hasSVGImageFile()) {
                return variant;
            } else if (variant.hasPNGImageFile()) {
                if (variant.hasSENHotspotFile()) {
                    return variant;
                } else {
                    preferredVariant = variant;
                }
            }
        }
        return preferredVariant;
    }

    public void addMediaVariants(List<iPartsXMLMediaVariant> variants) {
        if ((variants != null) && !variants.isEmpty()) {
            for (iPartsXMLMediaVariant variant : variants) {
                addMediaVariant(variant);
            }
        }
    }
}