/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;

import java.io.IOException;

/**
 * Superklasse für alle möglichen MQ Nachrichten, z.B. XMLMessage, DIALOG, etc.
 */
public abstract class AbstractMQMessage extends AbstractXMLObject {

    protected String messageType;
    protected MQChannelType channelType;
    protected DWFile savedMQXmlFile;
    protected String fileContent;
    protected boolean notificationOnly;

    public AbstractMQMessage() {
        this.messageType = "iPartsMQMessage";
    }

    public String getMessageType() {
        return messageType;
    }

    public boolean isOfType(String messageType) {
        return this.messageType.equals(messageType);
    }

    public MQChannelType getMQChannelType() {
        return channelType;
    }

    public void setMQChannelType(MQChannelType channelType) {
        this.channelType = channelType;
    }

    public abstract boolean isValidForMQChannelTypeName(iPartsMQChannelTypeNames channelTypeName);

    /**
     * Speichert die Originaldatei in das übergebene Verzeichnis
     *
     * @param directory
     * @param prefix    - falls ein bestimmter Prefix gewünscht ist
     * @return
     */
    public synchronized boolean saveToFile(DWFile directory, String prefix) {
        if (directory != null) {
            if (!directory.exists()) {
                directory.mkDirsWithRepeat();
            }
            if ((fileContent != null) && directory.exists()) {
                String prefixForFile = XMLImportExportDateHelper.getFormattedDateTimeForMessageCopies();
                if (StrUtils.isValid(prefix)) {
                    prefixForFile = prefixForFile + "_" + DWFile.convertToValidFileName(TranslationHandler.translate(prefix));
                }

                String channelTypeName = DWFile.convertToValidFileName(channelType.getChannelName().getTypeName().replace(' ', '_'));
                savedMQXmlFile = directory.getChild(prefixForFile + "_" + channelTypeName + ".xml");
                int i = 0;
                while (savedMQXmlFile.exists()) {
                    String newPrefix = prefixForFile + "_" + i;
                    savedMQXmlFile = directory.getChild(newPrefix + "_" + channelTypeName + ".xml");
                    i++;
                }
                try {
                    savedMQXmlFile.writeTextFile(fileContent.getBytes(DWFileCoding.UTF8.getJavaCharsetName()));
                } catch (IOException e) {
                    Logger.getLogger().handleRuntimeException(e);
                }
                return true;
            }
        } else {
            Logger.log(LogChannels.APPLICATION, LogType.DEBUG, "No save directory for import selected.");
        }
        return false;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public DWFile getSavedMQXmlFile() {
        return savedMQXmlFile;
    }

    public boolean canBeWritten() {
        return (getSavedMQXmlFile() != null) || (getFileContent() != null);
    }

    public void setSavedMQXmlFile(DWFile savedMQXmlFile) {
        this.savedMQXmlFile = savedMQXmlFile;
    }

    /**
     * Flag, ob diese Nachricht nur als Mitteilung ohne Binärdaten dient und keine Datenbankaktionen basierend auf dieser
     * Nachricht stattfinden sollen.
     *
     * @return
     */
    public boolean isNotificationOnly() {
        return notificationOnly;
    }

    /**
     * Wird aufgerufen, wenn diese Nachricht nur als Mitteilung ohne Binärdaten dienen soll und keine Datenbankaktionen
     * basierend auf dieser Nachricht stattfinden sollen.
     */
    public void convertToNotificationOnly() {
        this.notificationOnly = true;
    }

    /**
     * Verschiebt die gespeicherte MQ XML-Datei ins das neue angegebene Unterverzeichnis, wobei sich die MQ XML-Datei
     * bereits in einem Unterverzeichnis (z.B. {@link de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst#SUBDIR_RUNNING}
     * befinden muss.
     *
     * @param newSubDir
     * @return Das verschobene {@link DWFile} im Zielverzeichnis
     */
    public DWFile moveSavedMQXmlFile(String newSubDir) {
        DWFile srcFile = getSavedMQXmlFile();
        try {
            if (srcFile == null) {
                srcFile = XMLImportExportHelper.getSaveMQXmlDir(getMQChannelType().getChannelName(), newSubDir);
                saveToFile(srcFile, "");
            } else {
                // Datei *\running\file.xml verschieben nach *\newSubDir\file.xml
                DWFile destFile = srcFile.getParentDWFile().getParentDWFile();
                if (destFile == null) {
                    // es gibt kein Verzeichnis oberhalb vom Verzeichnis, das die XML-Datei enthält
                    // -> darf eigentlich bei korrekter Verwendung der Funktion nicht vorkommen, aber sicher ist sicher
                    destFile = srcFile.getParentDWFile();
                }
                destFile = destFile.getChild(newSubDir);
                destFile.mkDirsWithRepeat();
                destFile = destFile.getChild(srcFile.extractFileName(true));
                srcFile.move(destFile);
                setSavedMQXmlFile(destFile);
                return destFile;
            }
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, e);
        }
        return null;
    }

    public abstract boolean isValid();

}
