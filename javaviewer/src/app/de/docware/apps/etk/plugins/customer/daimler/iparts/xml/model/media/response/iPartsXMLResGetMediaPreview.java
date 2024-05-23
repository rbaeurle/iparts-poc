/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLResponseOperation;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.List;

/**
 * Repräsentiert das "ResGetMediaPreview" Element in der Transfer XML
 */
public class iPartsXMLResGetMediaPreview extends AbstractXMLResponseOperation {

    private String mcItemId;
    private String mcItemRevId;
    private iPartsXMLMediaBinaryFile binaryFile;

    public iPartsXMLResGetMediaPreview(String mcItemId, String mcItemRevId) {
        this.resultType = iPartsTransferNodeTypes.RES_GET_MEDIA_PREVIEW;
        this.mcItemId = mcItemId;
        this.mcItemRevId = mcItemRevId;
    }

    public iPartsXMLResGetMediaPreview(DwXmlNode node) {
        this("", "");
        loadFromXML(node);
    }

    public iPartsXMLMediaBinaryFile getBinaryFile() {
        return binaryFile;
    }

    public void setBinaryFile(iPartsXMLMediaBinaryFile binaryFile) {
        this.binaryFile = binaryFile;
    }

    public String getMcItemId() {
        return mcItemId;
    }

    public String getMcItemRevId() {
        return mcItemRevId;
    }


    @Override
    public DwXmlNode getAsDwXMLNode(String namespacePrefix) {
        DwXmlNode rgmp = new DwXmlNode(namespacePrefix + iPartsTransferNodeTypes.RES_GET_MEDIA_PREVIEW.getAlias());
        if (mcItemId != null) {
            rgmp.setAttribute(ATTR_MC_ITEM_ID, mcItemId);
        }
        if (mcItemRevId != null) {
            rgmp.setAttribute(ATTR_MC_ITEM_REV_ID, mcItemRevId);
        }
        DwXmlNode binaryFileNode = binaryFile.getAsDwXMLNode(namespacePrefix);
        if (binaryFileNode != null) {
            rgmp.appendChild(binaryFileNode);
        }
        return rgmp;
    }

    @Override
    protected void loadFromXML(DwXmlNode node) {
        if (node != null) {
            if (checkNodeType(node, iPartsTransferNodeTypes.RES_GET_MEDIA_PREVIEW)) {
                mcItemId = StrUtils.removeWhitespace(node.getAttribute(ATTR_MC_ITEM_ID));
                mcItemRevId = StrUtils.removeWhitespace(node.getAttribute(ATTR_MC_ITEM_REV_ID));
                fillResGetMediaPreview(node.getChildNodes());
            }
        }
    }

    private void fillResGetMediaPreview(List<DwXmlNode> childNodes) {
        if (childNodes.size() == 1) {
            DwXmlNode child = childNodes.get(0);
            if (XMLImportExportHelper.checkTagWithNamespace(child.getName(), iPartsTransferNodeTypes.BINARY_FILE)) {
                iPartsXMLMediaBinaryFile binaryFile = new iPartsXMLMediaBinaryFile(child);
                if (!MediaFileTypes.isValidFileExtension(binaryFile.getFileType())) {
                    Logger.getLogger().throwRuntimeException("Received FileType is not valid. Current FileType: " + binaryFile.getFileType());
                }
                setBinaryFile(binaryFile);
            } else {
                Logger.getLogger().throwRuntimeException("Schema dictates only BinaryFile Elements are allowed as childnodes. Current Element: " + child.getName());
            }
        } else {
            Logger.getLogger().throwRuntimeException("ResGetMediaPreview must only have 1 BinaryFile child. Amount of children: " + childNodes.size());
        }
    }

    public void processDebugParameters(boolean createBinaryObjectsEnabled, boolean writeDebugFilesEnabled) {
        if (createBinaryObjectsEnabled) {
            FrameworkImage decodedImage = XMLImportExportHelper.convertBase64StringToFrameworkImage(binaryFile.getBase64String());
            binaryFile.setImage(decodedImage);

            // Originalbild aus der XML-Nachricht für Debug-Zwecke abspeichern
            if (writeDebugFilesEnabled && iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_SAVE_XML_BINARY_CONTENT_FILES)) {
                // Kopie vom Bild aus der XML Message in den vorgesehenen Ordner
                DWFile mqXmlLocation = iPartsPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsPlugin.CONFIG_XML_FILES_DIR);
                DWFile imageFile = mqXmlLocation.getChild(DWFile.convertToValidFileName(iPartsMQChannelTypeNames.MEDIA.getTypeName())).
                        getChild(XMLImportExportDateHelper.getFormattedDateTimeForMessageCopies() + "_preview_image_" + getMcItemId()
                                 + "_" + getMcItemRevId() + "." + binaryFile.getFileType());
                imageFile.saveByteArray(decodedImage.getOriginal().getContent());
            }
        }
    }

    @Override
    public void convertToNotificationOnly() {
        super.convertToNotificationOnly();
        if (binaryFile != null) {
            binaryFile.convertToNotificationOnly();
        }
    }
}

