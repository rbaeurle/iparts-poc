/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repräsentiert das "Media" Element in der Transfer XML
 */
public class iPartsXMLMedia extends AbstractXMLObject {

    private String designer;
    private List<iPartsXMLMediaBinaryFile> binaryFiles;
    private Date lastModified;
    private boolean derived;

    public iPartsXMLMedia() {
        binaryFiles = new DwList<>();
    }

    public iPartsXMLMedia(DwXmlNode node) {
        this();
        loadFromXML(node);
    }

    public String getDesigner() {
        return designer;
    }

    public void setDesigner(String designer) {
        this.designer = designer;
    }

    public List<iPartsXMLMediaBinaryFile> getBinaryFiles() {
        return binaryFiles;
    }

    public void setBinaryFiles(List<iPartsXMLMediaBinaryFile> binaryFiles) {
        this.binaryFiles = binaryFiles;
    }

    public void addBinaryFile(iPartsXMLMediaBinaryFile binaryFile) {
        if (binaryFile != null) {
            binaryFiles.add(binaryFile);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "BinaryFile value must not be null");
        }
    }

    /**
     * Liefert das {@link iPartsXMLMediaBinaryFile} Objekt zum übergebenen {@link MediaFileTypes}.
     * Existiert zum übergebenen Typ keine Datei, wird <code>null</code> zurückgeliefert.
     *
     * @param fileType
     * @return
     */
    public Optional<iPartsXMLMediaBinaryFile> findBinaryFileType(MediaFileTypes fileType) {
        Optional<iPartsXMLMediaBinaryFile> preferredBinFile = Optional.empty();
        if (!getBinaryFiles().isEmpty() && (fileType != null)) {
            preferredBinFile = getBinaryFiles().stream()
                    .filter(binaryFile -> isWantedFileType(fileType, binaryFile))
                    .findFirst();
        }
        return preferredBinFile;
    }

    /**
     * Liefert zurück, ob das übergebene {@link iPartsXMLMediaBinaryFile} die gewünschte Endung hat und gültig ist
     *
     * @param fileType
     * @param binaryFile
     * @return
     */
    private boolean isWantedFileType(MediaFileTypes fileType, iPartsXMLMediaBinaryFile binaryFile) {
        MediaFileTypes foundFileType = MediaFileTypes.getFromAlias(binaryFile.getFileType());
        // in einem MediaVariant Element kann neben Bilddateien auch eine SEN Datei enthalten sein
        // getImage würde in diesem Fall "null" zurückliefern, daher die zusätzliche Abfrage
        return (foundFileType != null) && (foundFileType == fileType)
               && ((binaryFile.getImage() != null) || MediaFileTypes.isHotspotFile(foundFileType.getAlias()));
    }

    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode mediaNode = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.MEDIA.getAlias());
        if (!StrUtils.isEmpty(designer)) {
            mediaNode.setAttribute(ATTR_MEDIA_DESIGNER, designer);
        }
        if (lastModified != null) {
            mediaNode.setAttribute(ATTR_LAST_MODFIED, getLastModfiedAsASPLMValue());
        }
        mediaNode.setAttribute(ATTR_MEDIA_DERIVED, String.valueOf(isDerived()));
        for (iPartsXMLMediaBinaryFile binaryFile : binaryFiles) {
            DwXmlNode binaryFileNode = binaryFile.getAsDwXMLNode(namespacePrefix);
            if (binaryFileNode != null) {
                mediaNode.appendChild(binaryFileNode);
            }
        }
        return mediaNode;

    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.MEDIA)) {
                designer = node.getAttribute(ATTR_MEDIA_DESIGNER);
                derived = Boolean.parseBoolean(node.getAttribute(ATTR_MEDIA_DERIVED));
                String lastModifiedString = node.getAttribute(ATTR_LAST_MODFIED);
                if (!StrUtils.isEmpty(lastModifiedString)) {
                    lastModified = XMLImportExportDateHelper.getISOFormattedDateTimeAsDate(lastModifiedString);
                }
                List<DwXmlNode> childNodes = node.getChildNodes();
                if ((childNodes != null) && (!childNodes.isEmpty())) {
                    for (DwXmlNode childNode : childNodes) {
                        addBinaryFile(new iPartsXMLMediaBinaryFile(childNode));
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
        for (iPartsXMLMediaBinaryFile binaryFile : binaryFiles) {
            binaryFile.convertToNotificationOnly();
        }
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

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isDerived() {
        return derived;
    }

    public void setDerived(boolean derived) {
        this.derived = derived;
    }
}
