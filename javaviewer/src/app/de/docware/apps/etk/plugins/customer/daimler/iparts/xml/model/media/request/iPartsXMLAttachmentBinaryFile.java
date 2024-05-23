/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLBinaryFile;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.file.DWFile;

/**
 * Repräsentiert einen Binäranhang für das "Attachment" Element in der Transfer XML
 */
public class iPartsXMLAttachmentBinaryFile extends AbstractXMLBinaryFile {

    private DWFile file;

    public iPartsXMLAttachmentBinaryFile(String fileType, String attachmentAsBase64String) {
        super(fileType, attachmentAsBase64String);
    }

    public iPartsXMLAttachmentBinaryFile(DwXmlNode node) {
        super("", "");
        loadFromXML(node);
    }

    @Override
    protected boolean checkFileTypeValid() {
        // Endung ist nicht zwingend notwendig
        if (fileType == null) {
            return true;
        }
        if (AttachmentBinaryFileTypes.isValidFileExtension(fileType)) {
            return true;
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "File type is not valid for XML attachment binary file: " + fileType);
            return false;
        }
    }

    public DWFile getAsDWFile(String path) {
        if ((file == null) && (base64String != null) && checkFileTypeValid()) {
            file = XMLImportExportHelper.convertBase64StringToDWFile(path, base64String, AttachmentBinaryFileTypes.getFromAlias(fileType));
        }
        return file;
    }

    public void setDWFile(DWFile file) {
        this.file = file;
    }
}
