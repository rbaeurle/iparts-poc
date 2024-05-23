/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.ImageFileImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.ImageFileImporterResult;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQPartDataMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLMixedTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLBinaryFile;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLMediaVariant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.primus.iPartsXMLPrimusDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.primus.iPartsXMLSRMDataset;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.defaultconfig.UniversalConfiguration;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.xml.DwXmlFile;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.xml.dom.DomUtils;
import org.apache.commons.codec.binary.Base64;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Helper Klasse, um XML Dateien von AS-PLM einzulesen und XML Dateien für AS-PLM zu schreiben.
 */
public class XMLImportExportHelper implements iPartsConst, iPartsTransferConst {

    private static final String UNKNOWN_LANGUAGE = "No language";

    /**
     * Erzeugt und füllt ein {@link de.docware.framework.modules.xml.DwXmlFile} mit dem Inhalt des übergebenen
     * {@link iPartsXMLMediaMessage} Objekts.
     * Das zurückgegebene DwXmlFile Objekt respräsentiert die XML Datei, die an ASPLM geschickt wird.
     *
     * @param mediaMessage
     * @param channelType
     * @param xmlFilePrefix
     * @param commentForXMLFile
     * @param writeXmlFile      Flag, ob die {@link iPartsXMLMediaMessage} mit <i>xmlFilePrefix</i> und <i>commentForXMLFile</i>
     *                          in eine XML-Datei geschrieben werden soll (sofern dies im iParts Plug-in prinzipiell aktiviert ist)
     * @param isForSimulation
     * @return
     */
    public static DwXmlFile writeXMLFileFromMessageObject(iPartsXMLMediaMessage mediaMessage, MQChannelType channelType,
                                                          String xmlFilePrefix, String commentForXMLFile,
                                                          boolean writeXmlFile, boolean isForSimulation) {

        // Unterscheidung zwischen Erstellen von simulierten Antworten (mit namespace prefix) oder
        // direkte Nachricht an ASPLM (ohne namespace)
        String namespacePrefix;
        if (isForSimulation) {
            namespacePrefix = ASPLM_XML_NAMESPACE_PREFIX;
        } else {
            namespacePrefix = "";
        }
        DwXmlNode node = mediaMessage.getAsDwXMLNode(namespacePrefix);
        DwXmlFile file = new DwXmlFile(node);

        try {
            // Kopieren und validieren der MQ XML Dateien nur wenn ausdrücklich im Adminmodus gewünscht
            if (writeXmlFile && iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_SAVE_XML_FILES)) {
                //Kopie der XML in den vorgesehenen Ordner
                DWFile mqXmlLocation = iPartsPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsPlugin.CONFIG_XML_FILES_DIR);
                if (mqXmlLocation != null) {
                    mqXmlLocation = mqXmlLocation.getChild(DWFile.convertToValidFileName(channelType.getChannelName().getTypeName()));
                    mqXmlLocation.mkDirsWithRepeat();
                    DWFile outFile = mqXmlLocation.getChild(XMLImportExportDateHelper.getFormattedDateTimeForMessageCopies() + "_" + xmlFilePrefix + "_"
                                                            + mediaMessage.getiPartsRequestId() + ".xml");
                    file.writeWithComment(outFile, true, true, commentForXMLFile);
                }
            }
            //Validieren mit übergebenen Schema
            DWFile schemaFile = DWFile.get(iPartsPlugin.XML_SCHEMA_PATH).getChild(iPartsPlugin.XML_SCHEMA_ASPLM_MEDIA);
            if (schemaFile.isFile() && schemaFile.getExtension().equalsIgnoreCase("xsd")) {
                doValidation(file, schemaFile);
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "XML schema file is not valid: " + schemaFile.getAbsolutePath());
            }
        } catch (SAXException e) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Error while validating XML File\n" + e);
            Logger.getLogger().throwRuntimeException(e);
        } catch (IOException e) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Error while loading XML File\n" + e);
        }
        return file;
    }

    /**
     * Erzeugt eine Error-Logdatei, die den Fehler der Exception sowie die ersten 1000 Zeichen der XML Nachricht enthält.
     *
     * @param channelTypeName
     * @param exception
     * @param xmlContent
     * @param requestId
     * @param writeDebugFilesEnabled
     */
    private static void writeValidationErrorLogFile(iPartsMQChannelTypeNames channelTypeName, Exception exception, String xmlContent,
                                                    String requestId, boolean writeDebugFilesEnabled) {
        DWFile savedMQXmlFile = null;
        try {
            if (writeDebugFilesEnabled) {
                savedMQXmlFile = saveMQXmlFile(xmlContent, channelTypeName, requestId, iPartsConst.SUBDIR_ERROR);
            }
            boolean isValidationException = exception instanceof SAXException;
            DWFile validationErrorFile = iPartsJobsManager.getInstance().jobRunning(channelTypeName.getTypeName()
                                                                                    + (isValidationException ? "_XML_validation_error"
                                                                                                             : "_XML_error"));
            String content = (isValidationException ? TranslationHandler.translate("!!Validierungsfehler:")
                                                    : TranslationHandler.translate("!!Fehler:"))
                             + " " + exception.getMessage() + "\n\n";
            final int maxXmlContentLength = 1000;
            if (xmlContent.length() > maxXmlContentLength) {
                content += TranslationHandler.translate("!!XML-Inhalt (nur die ersten %1 Zeichen):", String.valueOf(maxXmlContentLength))
                           + "\n" + xmlContent.substring(0, maxXmlContentLength) + "\n...";
            } else {
                content += TranslationHandler.translate("!!XML-Inhalt:") + "\n" + xmlContent;
            }

            if (savedMQXmlFile != null) {
                String relativePath;
                // Original MQ Import Dateien werden in einem anderen Verzeichnis abgelegt als MQ Media Dateien
                if (channelTypeName == iPartsMQChannelTypeNames.MEDIA) {
                    relativePath = savedMQXmlFile.getRelativePath(iPartsPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsPlugin.CONFIG_XML_FILES_DIR));
                } else {
                    relativePath = savedMQXmlFile.getRelativePath(de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.CONFIG_IMPORT_FILE_DIR));
                }
                content += "\n\n\n" + TranslationHandler.translate("!!Fehlerhafte archivierte MQ XML Datei: %1", relativePath);
            }

            if (validationErrorFile != null) {
                validationErrorFile.writeTextFile(content.getBytes(DWFileCoding.UTF8.getJavaCharsetName()));
                iPartsJobsManager.getInstance().jobError(validationErrorFile);
            }
        } catch (IOException e) {
            Logger.getLogger().handleRuntimeException(e);
        }
    }

    /**
     * Erzeugt und füllt ein {@link de.docware.framework.modules.xml.DwXmlFile} mit dem Inhalt des übergebenen
     * {@link iPartsXMLMediaMessage} Objekts.
     * Das zurückgegebene DwXmlFile Objekt respräsentiert die XML Datei, die an ASPLM geschickt wird.
     *
     * @param iPartsXMLMediaMessage
     * @param channelType
     * @param xmlFilePrefix
     * @param commentForXMLFile
     * @return
     */
    public static DwXmlFile writeXMLFileFromMessageObject(iPartsXMLMediaMessage iPartsXMLMediaMessage, MQChannelType channelType,
                                                          String xmlFilePrefix, String commentForXMLFile, boolean isForSimulation) {
        return writeXMLFileFromMessageObject(iPartsXMLMediaMessage, channelType, xmlFilePrefix, commentForXMLFile, true, isForSimulation);
    }

    /**
     * Baut ein {@link AbstractMQMessage} Objekt mit den Daten aus dem übergebenen <i>xmlContent</i> für den angegebenen <i>channelTypeName</i>.
     *
     * @param xmlContent
     * @param channelTypeName
     * @param notificationOnly       Flag, ob die Nachricht nur als Mitteilung dient und keine Datenbankaktionen basierend auf
     *                               dieser Nachricht stattfinden sollen
     * @param writeDebugFilesEnabled Flag, ob das Abspeichern der XML-Datei sowie weiterer Dateien wie z.B. Zeichnungen
     *                               abhängig vom Inhalt und den Admin-Einstellungen zu Debug-Zwecken erlaubt sein soll
     * @return
     * @throws IOException
     * @throws SAXException
     */
    public static AbstractMQMessage buildMessageFromXmlContent(String xmlContent, iPartsMQChannelTypeNames channelTypeName,
                                                               boolean notificationOnly, boolean writeDebugFilesEnabled) throws IOException, SAXException {

        AbstractMQMessage xmlMQMessage = buildMessageFromXMLFile(xmlContent, channelTypeName, writeDebugFilesEnabled, writeDebugFilesEnabled);
        if (notificationOnly) {
            xmlMQMessage.convertToNotificationOnly();
        }
        return xmlMQMessage;
    }

    /**
     * Baut ein {@link AbstractMQMessage} Objekt mit den Daten aus der übergebenen XML Datei für den angegebenen <i>channelType</i>.
     *
     * @param xmlFile
     * @param channelType
     * @param createBinaryObjectsEnabled
     * @param writeDebugFilesEnabled
     * @return
     * @throws IOException
     * @throws SAXException
     */
    public static AbstractMQMessage buildMessageFromXMLFile(DwXmlFile xmlFile, MQChannelType channelType, boolean createBinaryObjectsEnabled,
                                                            boolean writeDebugFilesEnabled) throws IOException, SAXException {
        return buildMessageFromXMLFile(xmlFile.getContentAsString(), channelType, createBinaryObjectsEnabled, writeDebugFilesEnabled);
    }

    /**
     * Baut ein {@link AbstractMQMessage} Objekt mit den Daten aus dem übergebenen <i>xmlContent</i> für den angegebenen <i>channelType</i>.
     *
     * @param xmlContent
     * @param channelType
     * @param createBinaryObjectsEnabled Flag, ob Binärobjekte wie z.B. Bilder aus der XML-Nachricht erzeugt werden sollen
     * @param writeDebugFilesEnabled     Flag, ob das Abspeichern der XML-Datei sowie weiterer Dateien wie z.B. Zeichnungen
     *                                   abhängig vom Inhalt und den Admin-Einstellungen zu Debug-Zwecken erlaubt sein soll
     * @return
     * @throws IOException, SAXException
     */
    public static AbstractMQMessage buildMessageFromXMLFile(String xmlContent, MQChannelType channelType, boolean createBinaryObjectsEnabled,
                                                            boolean writeDebugFilesEnabled) throws IOException, SAXException {
        return buildMessageFromXMLFile(xmlContent, channelType.getChannelName(), createBinaryObjectsEnabled, writeDebugFilesEnabled);
    }

    /**
     * Baut ein {@link AbstractMQMessage} Objekt mit den Daten aus dem übergebenen <i>xmlContent</i> für den angegebenen <i>channelTypeName</i>.
     *
     * @param xmlContent
     * @param channelTypeName
     * @param createBinaryObjectsEnabled Flag, ob Binärobjekte wie z.B. Bilder aus der XML-Nachricht erzeugt werden sollen
     * @param writeDebugFilesEnabled     Flag, ob das Abspeichern der XML-Datei sowie weiterer Dateien wie z.B. Zeichnungen
     *                                   abhängig vom Inhalt und den Admin-Einstellungen zu Debug-Zwecken erlaubt sein soll
     * @return
     * @throws IOException, SAXException
     */
    private static AbstractMQMessage buildMessageFromXMLFile(String xmlContent, iPartsMQChannelTypeNames channelTypeName, boolean createBinaryObjectsEnabled,
                                                             boolean writeDebugFilesEnabled) throws IOException, SAXException {
        DwXmlNode node;
        AbstractMQMessage iPartsMQMessage;
        DwXmlFile xmlFile = checkIsXMLFile(xmlContent, writeDebugFilesEnabled, channelTypeName);
        //Wenn die XML nicht existiert -> Ende
        if (xmlFile != null) {
            String requestID = "unknownRequestID";
            node = xmlFile.getRootElement();
            iPartsMQMessage = createMQMessageObject(node, channelTypeName, createBinaryObjectsEnabled, writeDebugFilesEnabled);
            if (iPartsMQMessage == null) {
                String errorMessage = "Channel \"" + channelTypeName.getTypeName() + "\": Created MQ message object is null. Node: " + node.getName();
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, errorMessage);
                writeValidationErrorLogFile(channelTypeName, new RuntimeException(errorMessage), xmlContent, requestID, writeDebugFilesEnabled);
                return null;
            }
            iPartsMQMessage.setFileContent(xmlContent);

            try {
                DWFile schemaFile = null;
                boolean mediaMessage = iPartsMQMessage.isOfType(iPartsXMLMediaMessage.TYPE);
                boolean isTableDatasetMessage = iPartsMQMessage.isOfType(iPartsXMLMixedTable.TYPE) || iPartsMQMessage.isOfType(iPartsXMLTable.TYPE);
                boolean isMQPartDatasetMessage = iPartsMQMessage.isOfType(iPartsXMLPrimusDataset.TYPE) || iPartsMQMessage.isOfType(iPartsXMLSRMDataset.TYPE);
                if (mediaMessage) {
                    requestID = ((iPartsXMLMediaMessage)iPartsMQMessage).getiPartsRequestId();
                    schemaFile = DWFile.get(iPartsPlugin.XML_SCHEMA_PATH).getChild(iPartsPlugin.XML_SCHEMA_ASPLM_MEDIA);
                } else if (isTableDatasetMessage) {
                    iPartsXMLMixedTable table = (iPartsXMLMixedTable)iPartsMQMessage;
                    requestID = DWFile.convertToValidFileName(StrUtils.replaceSubstring(channelTypeName.getTypeName(), " ", "_")) + "_" +
                                DWFile.convertToValidFileName(StrUtils.stringListToString(table.getTableNames(), "_"));
                    switch (channelTypeName) {
                        case DIALOG_IMPORT:
                        case DIALOG_DELTA_IMPORT:
                            schemaFile = DWFile.get(iPartsPlugin.XML_SCHEMA_PATH).getChild(iPartsPlugin.XML_SCHEMA_ASPLM_DIALOG);
                            break;
                        case EDS_IMPORT:
                            schemaFile = DWFile.get(iPartsPlugin.XML_SCHEMA_PATH).getChild(iPartsPlugin.XML_SCHEMA_ASPLM_EDS);
                            break;
                        default:
                            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "XML schema does not exist for given import data. Origin: " + table.getOrigin());
                    }
                } else if (isMQPartDatasetMessage) {
                    requestID = DWFile.convertToValidFileName(StrUtils.replaceSubstring(channelTypeName.getTypeName(), " ", "_"));
                } else {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Unknown message type: " + iPartsMQMessage.getMessageType());
                    if (writeDebugFilesEnabled) {
                        saveMQXmlFile(xmlContent, channelTypeName, requestID, iPartsConst.SUBDIR_ERROR);
                    }
                    return null;
                }

                // Validieren der Datei
                if (isTableDatasetMessage || mediaMessage) {
                    if (schemaFile != null) {
                        if (schemaFile.isFile() && schemaFile.getExtension().equalsIgnoreCase("xsd")) {
                            doValidation(xmlFile, schemaFile);
                        } else {
                            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "XML schema file is not valid: " + schemaFile.getAbsolutePath());
                        }
                    } else {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "No XML schema file available for MQ message type: " + iPartsMQMessage.getMessageType());
                    }
                }

                // Erst nach der Validierung die MQ XML-Datei abspeichern, da bei einem Validierungsfehler die MQ XML-Datei
                // innerhalb vom writeValidationErrorLogFile() im catch-Block ins error-Unterverzeichnis geschrieben wird
                if (writeDebugFilesEnabled && iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_SAVE_XML_FILES)) {
                    // Schreibe Kopie in den vorgegebenen Ordner (für Importe in das Unterverzeichnis "running", alle anderen ohne Unterverzeichnis)
                    if (mediaMessage) {
                        iPartsMQMessage.setSavedMQXmlFile(saveMQXmlFile(xmlContent, channelTypeName, requestID, null));
                    }
                }
            } catch (SAXException e) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Error while validating XML File: " + e.getMessage());
                writeValidationErrorLogFile(channelTypeName, e, xmlContent, requestID, writeDebugFilesEnabled);
                throw e;
            } catch (IOException e) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Error while loading XML File: " + e.getMessage());
                writeValidationErrorLogFile(channelTypeName, e, xmlContent, requestID, writeDebugFilesEnabled);
                throw e;
            }

            // Check, ob die Nachricht für den Kanal erlaubt ist
            if (iPartsMQMessage != null) {
                if (iPartsMQChannelTypeNames.isChannelValidatable(channelTypeName) && !iPartsMQMessage.isValidForMQChannelTypeName(channelTypeName)) {
                    String logText = "Received MQ message of type \"" + iPartsMQMessage.getMessageType()
                                     + "\" is not valid for the MQ channel \"" + channelTypeName
                                     + "\". Content of received message: " + MQHelper.getLogTextFromMessageText(xmlFile.getContentAsString(), true);
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, logText);
                    saveMQXmlFile(xmlContent, channelTypeName, requestID, iPartsConst.SUBDIR_ERROR);
                }
            }

            return iPartsMQMessage;
        }
        Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "XML file is null. Could not create MQ message object.");
        return null;
    }

    private static DwXmlFile checkIsXMLFile(String xmlContent, boolean writeDebugFilesEnabled, iPartsMQChannelTypeNames channelTypeName) throws IOException {
        byte[] xmlContentBytes = null;
        try {
            xmlContentBytes = xmlContent.getBytes(DWFileCoding.UTF8.getJavaCharsetName());
            return new DwXmlFile(new ByteArrayInputStream(xmlContentBytes));
        } catch (IOException e) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Received MQ message contains no valid XML file. Content of received message: "
                                                                       + MQHelper.getLogTextFromMessageText(xmlContent, true));

            if (writeDebugFilesEnabled) { // XML als fehlerhaft abspeichern
                DWFile saveMQXmlFile = getSaveMQXmlFile(channelTypeName, "unknownRequestID", iPartsConst.SUBDIR_ERROR);
                if ((saveMQXmlFile != null) && (xmlContentBytes != null)) {
                    saveMQXmlFile.writeTextFile(xmlContentBytes);
                }
            }
            Logger.getLogger().throwRuntimeException(e);
        }
        return null;
    }

    private static DWFile saveMQXmlFile(String xmlContent, iPartsMQChannelTypeNames channelTypeName, String requestID, String subDir) {
        DWFile outFile = getSaveMQXmlFile(channelTypeName, requestID, subDir);
        try {
            if (outFile != null) {
                outFile.writeTextFile(xmlContent.getBytes(DWFileCoding.UTF8.getJavaCharsetName()));
            }
        } catch (IOException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, e);
        }
        return outFile;
    }

    private static DWFile getSaveMQXmlFile(iPartsMQChannelTypeNames channelTypeName, String requestID, String subDir) {
        DWFile mqXmlLocation = getSaveMQXmlDir(channelTypeName, subDir);
        if (mqXmlLocation != null) {
            return mqXmlLocation.getChild(XMLImportExportDateHelper.getFormattedDateTimeForMessageCopies() + "_iPartsIn_" + requestID + ".xml");
        } else {
            return null;
        }
    }

    public static DWFile getSaveMQXmlDir(iPartsMQChannelTypeNames channelTypeName, String subDir) {
        DWFile mqXmlLocation;
        if (channelTypeName == iPartsMQChannelTypeNames.MEDIA) {
            mqXmlLocation = iPartsPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsPlugin.CONFIG_XML_FILES_DIR);
        } else if (iPartsPlugin.isImportPluginActive()) {
            UniversalConfiguration importPluginConfig = de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.getPluginConfig();
            mqXmlLocation = importPluginConfig.getConfigValueAsDWFile(de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.CONFIG_IMPORT_FILE_DIR);
        } else {
            // Kann eigentlich nicht passieren...
            return null;
        }
        mqXmlLocation = mqXmlLocation.getChild(DWFile.convertToValidFileName(channelTypeName.getTypeName()));
        if (!StrUtils.isEmpty(subDir)) {
            mqXmlLocation = mqXmlLocation.getChild(subDir);
        }
        mqXmlLocation.mkDirsWithRepeat();
        return mqXmlLocation;
    }

    private static AbstractMQMessage createMQMessageObject(DwXmlNode node, iPartsMQChannelTypeNames channelTypeName,
                                                           boolean createBinaryObjectsEnabled, boolean writeDebugFilesEnabled) {
        iPartsTransferNodeTypes nodeType = getIPartsNodeType(node, channelTypeName);
        if (nodeType == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "Node type for channel \"" + channelTypeName.getTypeName() + "\" does not exist! Node: " + node.getName());
            return null;
        }
        return MQMessageFactory.createMainMessage(nodeType, node, createBinaryObjectsEnabled, writeDebugFilesEnabled);
    }

    /**
     * Gibt den Knotentyp in Abhängigkeit des namespace zurück
     *
     * @param node
     * @return
     */
    public static iPartsTransferNodeTypes getIPartsNodeType(DwXmlNode node) {
        String nodeName = StrUtils.removeFirstCharacterIfCharacterIs(node.getName(), ASPLM_XML_NAMESPACE_PREFIX);
        iPartsTransferNodeTypes nodeType = iPartsTransferNodeTypes.getFromAlias(nodeName);
        if (nodeType == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Unknown XML tag: " + node.getName()
                                                                       + "; Parent node: " + ((node.getParentNode() == null) ? "[ NONE ]" : node.getParentNode().getName()));
        }
        return nodeType;
    }

    /**
     * Gibt den Knotentyp in Abhängigkeit des namespace und des Kanaltyps zurück
     *
     * @param node
     * @return
     */
    public static iPartsTransferNodeTypes getIPartsNodeType(DwXmlNode node, iPartsMQChannelTypeNames channelTypeName) {
        AbstractMQPartDataMessage abstractMQPartDataMessage = MQMessageFactory.createPartDataMessage(channelTypeName, node);
        if (abstractMQPartDataMessage == null) {
            return getIPartsNodeType(node);
        } else {
            return abstractMQPartDataMessage.getIPartsNodeType();
        }
    }

    /**
     * Konvertiert einen Base64 String zu einem {@link FrameworkImage}
     *
     * @param textContent
     * @return
     */
    public static FrameworkImage convertBase64StringToFrameworkImage(String textContent) {
        return FrameworkImage.getFromByteArray(Base64.decodeBase64(textContent));
    }

    /**
     * Konvertiert ein {@link FrameworkImage} zu einem Base64 String
     *
     * @param image
     * @return
     */
    public static String convertFrameworkImageToBase64String(FrameworkImage image) {
        return Base64.encodeBase64String(image.getOriginal().getContent());

    }

    /**
     * Konvertiert einen Base64 String zu einem {@link DWFile} Objekt
     *
     * @param path
     * @param content
     * @param fileType
     * @return
     */
    public static DWFile convertBase64StringToDWFile(String path, String content, AttachmentBinaryFileTypes fileType) {
        DWFile file = DWFile.get(path);
        file = file.replaceExtensionFile(fileType.getAlias());
        file.saveByteArray(Base64.decodeBase64(content));
        return file;
    }

    /**
     * Konvertiert ein {@link DWFile} Objekt zu einem Base64 String
     *
     * @param file
     * @return
     */
    public static String convertDWFileToBase64String(DWFile file) {
        return Base64.encodeBase64String(file.readByteArray());

    }

    public static boolean checkTagWithNamespace(String nodeName, iPartsTransferNodeTypes tag) {
        if (nodeName.equals(tag.getAlias())) {
            return true;
        }
        if (nodeName.equals(tag.getAliasWithASPLMNamespace())) {
            return true;
        }
        return false;
    }

    /**
     * Wandelt eine Map in einen String mit key value Paare um.
     *
     * @param map
     * @return
     */
    public static String createCodeFromMap(Map<String, String> map) {
        if ((map != null) && !map.isEmpty()) {
            StringBuffer result = new StringBuffer();
            boolean first = true;
            for (Map.Entry<String, String> entries : map.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    result.append("&");
                }
                result.append(entries.getKey());
                result.append("=");
                result.append(entries.getValue());
            }
            return result.toString();
        }

        return "";
    }

    /**
     * Wandelt einen String mit key value paaren in eine Map um.
     *
     * @param code
     * @return
     */
    public static Map<String, String> createMapFromCode(String code) {
        HashMap<String, String> result = new HashMap<String, String>();
        if (code.isEmpty()) {
            return result;
        }

        String[] entries = code.split("&");
        for (String entry : entries) {
            String[] entryParts = entry.split("=");
            if (entryParts.length < 2) {
                if (entryParts.length == 1) {
                    result.put(UNKNOWN_LANGUAGE, entryParts[0]);
                }
            } else {
                result.put(entryParts[0], entryParts[1]);
            }
        }

        return result;
    }

    /**
     * Liefert den Fehlertext für die angegebene Sprache zurück mit Fallback auf Englisch bzw. die erste verfügbare Fehlersprache.
     *
     * @param errorTextsString String, der codiert alle Fehlertexte mit den dazugehörigen Sprachen enthält (siehe {@link #createCodeFromMap(java.util.Map)})
     * @param language
     * @return
     */
    public static String getErrorText(String errorTextsString, String language) {
        Map<String, String> errorTexts = createMapFromCode(errorTextsString);
        if (errorTexts.isEmpty()) {
            return "";
        } else {
            String lastErrorText = errorTexts.get(language.toUpperCase());
            if (lastErrorText != null) {
                return lastErrorText;
            }

            // Englisch als erster Fallback
            lastErrorText = errorTexts.get(Language.EN.getCode());
            if (lastErrorText != null) {
                return lastErrorText;
            }

            // Erster Eintrag als letzter Fallback
            return errorTexts.values().iterator().next();
        }
    }

    /**
     * Validiert eine XML Datei mit Hilfe eines Schemas
     *
     * @param xmlFile
     * @param schemaFile
     * @throws IOException
     * @throws SAXException
     */
    public static void doValidation(DwXmlFile xmlFile, DWFile schemaFile) throws IOException, SAXException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        xmlFile.stream(baos, false, "UTF-8");
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        DomUtils.validate(new StreamSource(bis), new StreamSource(schemaFile), null);
    }

    public static String makeViewForSize(String sizeAsByte) {
        return sizeAsByte + " Byte (ca. " + FrameworkUtils.getFileSizeString(Double.parseDouble(sizeAsByte)) + ")";
    }

    /**
     * Erstellt eine eindeutige GUID für MQ Operationen, die vom Bildnummernimporter gestartet werden
     *
     * @param messageGuid
     * @return
     */
    public static String makePicReferenceGUIDForMediaContent(String messageGuid) {
        return makeGUIDForMediaContent(messageGuid, MEDIA_CONTENT_PIC_REFERENCE_PREFIX);
    }

    public static String makePicRequestGUIDForMediaContent(String messageGuid) {
        return makeGUIDForMediaContent(messageGuid, MEDIA_CONTENT_REQUEST_PICTURES_PREFIX);
    }

    /**
     * Erstellt eine eindeutige GUID für MQ Operationen
     *
     * @param messageGuid
     * @return
     */
    private static String makeGUIDForMediaContent(String messageGuid, String prefix) {
        if (StrUtils.isEmpty(messageGuid)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Message GUID must not be null or empty when " +
                                                                       "creating picReference GUID for MediaContent operation.");
            return null;
        }
        return prefix + MEDIA_CONTENT_PREFIX_DELIMITER + messageGuid;
    }

    public static boolean isMediaContentFromPicReference(String iPartsRequestId) {
        return StrUtils.stringStartsWith(iPartsRequestId, MEDIA_CONTENT_PIC_REFERENCE_PREFIX + MEDIA_CONTENT_PREFIX_DELIMITER, false);
    }

    public static boolean isMediaContentFromPicRequest(String iPartsRequestId) {
        return StrUtils.stringStartsWith(iPartsRequestId, MEDIA_CONTENT_REQUEST_PICTURES_PREFIX + MEDIA_CONTENT_PREFIX_DELIMITER, false);
    }

    public static String removeMediaContentPrefix(String iPartsRequestId) {
        if (StrUtils.isEmpty(iPartsRequestId)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "iPartsRequestId must not be null or empty when " +
                                                                       "removing GUID prefix for MediaContent operation.");
            return null;
        }
        if (StrUtils.stringContains(iPartsRequestId, MEDIA_CONTENT_PREFIX_DELIMITER, false)) {
            return StrUtils.stringAfterCharacter(iPartsRequestId, MEDIA_CONTENT_PREFIX_DELIMITER);
        }
        return iPartsRequestId;
    }

    /**
     * Speichert das Bild samt Hotspots aus der übergebenen {@link iPartsXMLMediaVariant} in der Datenbank ab.
     * Aktuell werden nur PNG und SEN Dateien verarbeitet.
     *
     * @param project
     * @param variant
     * @return
     */
    public static ImageFileImporterResult importBinaryFileWithResult(EtkProject project, iPartsXMLMediaVariant variant) {
        ImageFileImporter fileImporter = new ImageFileImporter(project);
        ImageFileImporterResult importResult = fileImporter.importImageFromMediaVariant(variant);
        if (!importResult.importSuccessful()) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Error while storing new picture in DB");
        }
        return importResult;
    }

    public static boolean importBinaryFile(EtkProject project, iPartsXMLMediaVariant variant) {
        return importBinaryFileWithResult(project, variant).importSuccessful();
    }

    /**
     * Speichert das übergebene Vorschaubild zu einem schon existierenden Bild in der Datenbank
     *
     * @param project
     * @param previewBinaryFile
     * @param pImages
     * @param pVer
     * @return
     */
    public static boolean importPreviewBinaryFile(EtkProject project, AbstractXMLBinaryFile previewBinaryFile, String pImages, String pVer) {
        ImageFileImporter fileImporter = new ImageFileImporter(project);
        if (!fileImporter.importPreviewFile(previewBinaryFile, pImages, pVer)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Error while storing new preview picture in DB");
            return false;
        }
        return true;
    }

    /**
     * Erstellt die AS-PLM spezifischen Projekt-IDs aus den Typkennzahlen des übergebenen Produkts.
     * <p>
     * Der Aufbau einer ID ist folgendermaßen: Bei Aggregatetyp Fahrzeug: "AS_"-Präfix + Typkennzahl ohne Sachnummernkennbuchstabe.
     * Bei allen anderen Aggregatetypen: "AS_"-Präfix + Typkennzahl mit Sachnummernkennbuchstabe.
     * Bei mehr als einer Typkennzahl pro Produkt wird eine Leerzeichen-separierte Liste als String erzeugt.
     *
     * @param project
     * @param product
     * @return
     */
    public static String createAssignedProjectsFromModelTypes(EtkProject project, iPartsProduct product) {
        if ((product == null) || !product.getAsId().isValidId()) {
            return "";
        }

        // Spezialprodukte haben eine feste Projektzuweisung
        if (product.isSpecialCatalog()) {
            return MQHelper.getProjectAssignmentPrefixForSpecialProduct();
        }

        Set<String> result = new TreeSet<>();
        String prefix = getPrefixForAssignedProjects(product);
        boolean isAggTypeCar = product.getAggregateType().equals(AGGREGATE_TYPE_CAR);
        for (String modelType : product.getReferencedSeriesOrAllModelTypes(project)) {
            if (isAggTypeCar) {
                result.add(prefix + StrUtils.removeFirstCharacter(modelType));
            } else {
                result.add(prefix + modelType);
            }
        }

        return StrUtils.stringListToString(result, " ");
    }

    /**
     * Liefert den Präfix für die Produktart
     *
     * @param product
     * @return
     */
    private static String getPrefixForAssignedProjects(iPartsProduct product) {
        if (product.isPSK()) {
            return MQHelper.getProjectAssignmentPrefixForPSK();
        }
        return "";
    }

    public static boolean isASPLMPictureNumber(String imageName) {
        return StrUtils.isValid(imageName) && imageName.startsWith("PV") && (StrUtils.countString(imageName, ".", true) == 3);
    }
}
