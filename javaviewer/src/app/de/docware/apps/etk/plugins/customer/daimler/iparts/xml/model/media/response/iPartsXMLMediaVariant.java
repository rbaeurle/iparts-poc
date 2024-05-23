/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsColorTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Repräsentiert das "MediaVariant" Element in der Transfer XML
 */
public class iPartsXMLMediaVariant extends AbstractXMLObject {

    private final List<iPartsXMLMedia> mediaList = new DwList<>();
    private String itemId;
    private String itemRevId;
    private Language pictureLanguage;
    private iPartsColorTypes colorType;
    private Date lastModified;
    private boolean isTemplate;
    private String automationLevel;

    public iPartsXMLMediaVariant() {
    }

    public iPartsXMLMediaVariant(iPartsXMLMedia xmlMedia) {
        addMedia(xmlMedia);
    }

    public iPartsXMLMediaVariant(DwXmlNode node) {
        loadFromXML(node);
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemRevId() {
        return itemRevId;
    }

    public void setItemRevId(String itemRevId) {
        this.itemRevId = itemRevId;
    }

    public String getPictureLanguage() {
        if (pictureLanguage != null) {
            return pictureLanguage.getCode();
        } else {
            return "";
        }
    }

    public void setPictureLanguage(String pictureLanguage) {
        // in manchen Fällen liefert AS-PLM den Wert "language independent". Für diesen Fall setzen wir einen leeren
        // Sprachwert
        if (!StrUtils.isEmpty(pictureLanguage)) {
            this.pictureLanguage = Language.getFromCode(pictureLanguage);
        } else {
            this.pictureLanguage = null;
        }
    }

    public iPartsColorTypes getColorType() {
        return colorType;
    }

    public void setColorType(iPartsColorTypes colorType) {
        this.colorType = colorType;
    }

    public List<iPartsXMLMedia> getMediaList() {
        return mediaList;
    }

    public void addMedia(iPartsXMLMedia media) {
        if (media != null) {
            mediaList.add(media);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Media value must not be null");
        }
    }

    /**
     * Liefert das erste {@link iPartsXMLMediaBinaryFile} zurück, deren Endung gleich der übergebenen Endung ist.
     *
     * @param preferredType
     * @return
     */
    public iPartsXMLMediaBinaryFile findBinaryFileType(MediaFileTypes preferredType) {
        if (!getMediaList().isEmpty()) {
            Optional<iPartsXMLMediaBinaryFile> binFile = Optional.empty();
            boolean isSVG = preferredType == MediaFileTypes.SVG;
            // Bei SVG muss erst nach dem Attribut "derived" gefiltert werden (reduzierte SVGs)
            if (isSVG) {
                Stream<iPartsXMLMedia> filteredStream = getMediaList().stream().filter(iPartsXMLMedia::isDerived);
                binFile = findFirstValidBinaryFile(filteredStream, preferredType); // Ergebnis des gefilterten Streams
            }

            // Wenn das Optional des gefilterten Streams leer ist, wird geschaut, ob im ungefilterten Stream ein Bild
            // vorhanden ist. Falls da auch keins drin ist, wird "null" zurückgeliefert
            return binFile.orElseGet(() -> findFirstValidBinaryFile(getMediaList().stream(), preferredType).orElse(null));
        }
        return null;
    }

    /**
     * Liefert aus dem übergebenen Stream das erste Objekt, das zum übergebenen {@link MediaFileTypes} passt.
     *
     * @param xmlMediaStream
     * @param preferredType
     * @return
     */
    private Optional<iPartsXMLMediaBinaryFile> findFirstValidBinaryFile(Stream<iPartsXMLMedia> xmlMediaStream, MediaFileTypes preferredType) {
        return xmlMediaStream
                .map(mediaFile -> mediaFile.findBinaryFileType(preferredType)) // Stream mit Optionals
                .filter(Optional::isPresent) // Nur die nehmen, die auch einen echten Wert besitzen (iPartsXMLMediaBinaryFile)
                .findFirst() // Den ersten Datensatz verwenden
                .flatMap(Function.identity()); // Weil findFirst() das Ergebnis nochmals in ein Optional packt, hier flatMap damit nur ein Optional entsteht (map und flatten)
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode mediaVariantNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.MEDIA_VARIANT.getAlias());
        if ((itemId != null) && !itemId.isEmpty()) {
            mediaVariantNode.setAttribute(ATTR_ITEM_ID, itemId);
        }
        if ((itemRevId != null) && !itemRevId.isEmpty()) {
            mediaVariantNode.setAttribute(ATTR_ITEM_REV_ID, itemRevId);
        }
        if ((pictureLanguage != null)) {
            mediaVariantNode.setAttribute(ATTR_LANGUAGE, getPictureLanguage().toLowerCase());
        }
        if (colorType != null) {
            mediaVariantNode.setAttribute(ATTR_MEDIA_COLOR, colorType.getAsplmValue());
        }
        if (lastModified != null) {
            mediaVariantNode.setAttribute(ATTR_LAST_MODFIED, getLastModfiedAsASPLMValue());
        }
        if (StrUtils.isValid(automationLevel)) {
            mediaVariantNode.setAttribute(ATTR_AUTOMATION_LEVEL, getAutomationLevel());
        }
        mediaVariantNode.setAttribute(ATTR_IS_TEMPLATE, String.valueOf(isTemplate()));
        for (iPartsXMLMedia media : mediaList) {
            mediaVariantNode.appendChild(media.getAsDwXMLNode(namespacePrefix));
        }
        return mediaVariantNode;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.MEDIA_VARIANT)) {
                // Attribute
                itemId = StrUtils.removeWhitespace(node.getAttribute(iPartsTransferConst.ATTR_ITEM_ID));
                itemRevId = StrUtils.removeWhitespace(node.getAttribute(iPartsTransferConst.ATTR_ITEM_REV_ID));
                setPictureLanguage(node.getAttribute(ATTR_LANGUAGE));
                String lastModifiedString = node.getAttribute(ATTR_LAST_MODFIED);
                if (StrUtils.isValid(lastModifiedString)) {
                    lastModified = XMLImportExportDateHelper.getISOFormattedDateTimeAsDate(lastModifiedString);
                }
                String colorTypeString = node.getAttribute(ATTR_MEDIA_COLOR);
                if (StrUtils.isValid(colorTypeString)) {
                    colorType = iPartsColorTypes.getFromASPLMValue(colorTypeString);
                    if (colorType == null) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Not allowed value for MediaVariant colortype. Value: " + colorTypeString);
                    }
                }
                automationLevel = StrUtils.removeWhitespace(node.getAttribute(iPartsTransferConst.ATTR_AUTOMATION_LEVEL));
                isTemplate = Boolean.parseBoolean(node.getAttribute(iPartsTransferConst.ATTR_IS_TEMPLATE));
                // Kindknoten
                List<DwXmlNode> childNodes = node.getChildNodes();
                if ((childNodes != null) && (!childNodes.isEmpty())) {
                    for (DwXmlNode childNode : childNodes) {
                        addMedia(new iPartsXMLMedia(childNode));
                    }
                }
            }
        }
    }

    /**
     * Wird aufgerufen, wenn diese Nachricht nur als Mitteilung ohne Binärdaten dienen soll und keine Datenbankaktionen
     * basierend auf dieser Nachricht stattfinden sollen.
     */
    public void convertToNotificationOnly() {
        for (iPartsXMLMedia media : mediaList) {
            media.convertToNotificationOnly();
        }
    }

    public iPartsXMLMedia getMediaForBinFile(iPartsXMLMediaBinaryFile binFile) {
        for (iPartsXMLMedia media : mediaList) {
            if (media.getBinaryFiles().contains(binFile)) {
                return media;
            }
        }
        return null;
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

    /**
     * Wenn die Media-Variante eine SEN-Datei enthält, wird diese zurückgeliefert.
     *
     * @return null Falls die Media-Variante keine SEN Datei enthält
     */
    public iPartsXMLMediaBinaryFile getSENBinaryFile() {
        return findBinaryFileType(MediaFileTypes.SEN);
    }

    /**
     * Wenn die Media-Variante eine PNG-Datei enthält, wird diese zurückgeliefert.
     *
     * @return null Falls die Media-Variante keine PNG Datei enthält
     */
    public iPartsXMLMediaBinaryFile getPNGBinaryFile() {
        return findBinaryFileType(MediaFileTypes.PNG);
    }

    /**
     * Wenn die Media-Variante eine SVG-Datei enthält, wird diese zurückgeliefert.
     *
     * @return null Falls die Media-Variante keine SVG Datei enthält
     */
    public iPartsXMLMediaBinaryFile getSVGBinaryFile() {
        return findBinaryFileType(MediaFileTypes.SVG);
    }

    /**
     * Check, ob diese Media-Variante eine PNG-Datei enthält
     *
     * @return
     */
    public boolean hasPNGImageFile() {
        return getPNGBinaryFile() != null;
    }

    /**
     * Check, ob diese Media-Variante eine SVG-Datei enthält
     *
     * @return
     */
    public boolean hasSVGImageFile() {
        return getSVGBinaryFile() != null;
    }

    /**
     * Check, ob diese Media-Variante eine SEN-Datei enthält
     *
     * @return
     */
    public boolean hasSENHotspotFile() {
        return getSENBinaryFile() != null;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    public String getAutomationLevel() {
        return automationLevel;
    }

    public void setAutomationLevel(String automationLevel) {
        this.automationLevel = automationLevel;
    }
}