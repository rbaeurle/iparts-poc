/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLBinaryFile;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.xml.DwXmlNode;

/**
 * Repräsentiert einen Binäranhang für das "ResGetMediaPreview" Element in der Transfer XML
 */
public class iPartsXMLMediaBinaryFile extends AbstractXMLBinaryFile {

    private FrameworkImage image;

    public iPartsXMLMediaBinaryFile(String fileType, String imageAsBase64String) {
        super(fileType, imageAsBase64String);
    }

    public iPartsXMLMediaBinaryFile(DwXmlNode node) {
        super("", "");
        loadFromXML(node);
    }

    public FrameworkImage getImage() {
        // Es kann sich hier auch um eine SEN Datei handeln. Daher nur ein FrameworkImage erzeugen, wenn es keine
        // SEN Datei ist
        if ((image == null) && (base64String != null) && checkFileTypeValid() && !MediaFileTypes.isHotspotFile(getFileType())) {
            image = XMLImportExportHelper.convertBase64StringToFrameworkImage(base64String);
        }
        return image;
    }

    public void setImage(FrameworkImage image) {
        this.image = image;
    }

    @Override
    protected boolean checkFileTypeValid() {
        if (MediaFileTypes.isValidFileExtension(fileType)) {
            return true;
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "File type is not valid for XML media binary file: " + fileType);
            return false;
        }
    }
}
