/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;


import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQChannel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQMessageReceiver;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserDialog;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileOpenValidatorForExtensions;
import de.docware.framework.modules.gui.controls.filechooser.filefilter.DWFileFilterEnum;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.forms.CopyTextWindow;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.java1_1.Java1_1_Utils;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Dialog zum Anzeigen von MQ Nachrichtenverkehr
 */
public class MQDemoForm implements MQMessageReceiver {

    private static final String SESSION_KEY_MQ_TEST_SELECTED_MQ_CHANNEL_TYPE = "iparts_mq_test_selected_mq_channel_type";
    private static final int MAX_FILES_FOR_DETAILED_LOG = 100;

    private boolean isCancel = false;
    private boolean ignoreErrors = false;
    private boolean multipleFiles = false;

    private String connectionFactoryJNDI;
    private Session session;
    private FrameworkThread receiverDummyThread;
    private MQChannel currentChannel;

    /**
     * Erzeugt eine Instanz von MQTestForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public MQDemoForm(String connectionFactoryJNDI) {
        this.session = Session.get();
        setConnectionFactoryJNDI(connectionFactoryJNDI);
        if (MQHelper.getInstance().hasWorkingChannels()) {
            if (Session.get().hasAttribute(SESSION_KEY_MQ_TEST_SELECTED_MQ_CHANNEL_TYPE)) {
                MQChannelType channelType = (MQChannelType)Session.get().getAttribute(SESSION_KEY_MQ_TEST_SELECTED_MQ_CHANNEL_TYPE);
                if (channelType != null) {
                    currentChannel = MQHelper.getInstance().getChannel(channelType);
                }
            }
        }
        $$internalCreateGui$$(null);
        postCreateGui();
        receiverDummyThread = session.startChildThreadLowPrio(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                // endlos bis zum Schließen vom Fenster warten, damit der Server den Client aktiv über neue Messages benachrichtigen kann...
                while (!Java1_1_Utils.sleep(100)) {
                    ;
                }
            }
        });
        addToChannel();
    }

    private void addToChannel() {
        if (currentChannel != null) {
            MQHelper.getInstance().addMQMessageReceiver(currentChannel.getChannelType(), this);
        }
    }

    private void removeFromChannel() {
        if (currentChannel != null) {
            MQHelper.getInstance().removeMQMessageReceiver(currentChannel.getChannelType(), this);
        }
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        Dimension screenSize = FrameworkUtils.getScreenSize();
        // Größe auf mindestens 80% des Bildschirms/Browsers festlegen
        mainWindow.setMinimumHeight((int)(0.8 * screenSize.getHeight()));
        mainWindow.setMinimumWidth((int)(0.8 * screenSize.getWidth()));
        // Scrollpanes mit den Textareas an das Fenster anpassen (abzüglich Randabstände)
        mainWindow.mqInputScrollpane.setMinimumWidth((mainWindow.getMinimumWidth() / 2) - 32);
        mainWindow.mqOutputScrollpane.setMinimumWidth((mainWindow.getMinimumWidth() / 2) - 32);
        mainWindow.channelComboBox.switchOffEventListeners();
        mainWindow.channelComboBox.addItem(null, "");
        for (MQChannelType channelType : MQChannelType.getChannelTypes()) {
            if (MQHelper.getInstance().isChannelInitialized(channelType)) {
                mainWindow.channelComboBox.addItem(channelType, channelType.getChannelName().getTypeName());
            }
        }
        if (currentChannel != null) {
            mainWindow.channelComboBox.setSelectedUserObject(currentChannel.getChannelType());
        }
        mainWindow.channelComboBox.switchOnEventListeners();
        updateControls();

        // AS-PLM Simulationseinstellungen nur im Redaktionssystem und bei vorhandenem MEDIA-Kanal anzeigen
        if (JavaViewerApplication.getInstance().getProject().isEditModeActive() && (MQChannelType.getChannelTypeByName(iPartsMQChannelTypeNames.MEDIA) != null)) {
            mainWindow.simAutoResponseDelayTextField.setValue(iPartsPlugin.getSimAutoResponseDelayForSession(iPartsPlugin.SIM_AUTO_RESPONSE_DELAY));
            mainWindow.simAutoResponseDelaySearchTextField.setValue(iPartsPlugin.getSimAutoResponseDelayForSession(iPartsPlugin.SIM_AUTO_RESPONSE_DELAY_SEARCH));
        } else {
            mainWindow.mqSimPanel.setVisible(false);
        }
        mainWindow.pack();
    }

    private void closeWindow(Event event) {
        receiverDummyThread.cancel();
        MQHelper.getInstance().removeMQMessageReceiverFromAllChannels(this);
        mainWindow.setVisible(false);
        if (currentChannel != null) {
            Session.get().setAttribute(SESSION_KEY_MQ_TEST_SELECTED_MQ_CHANNEL_TYPE, currentChannel.getChannelType());
        }
        if (JavaViewerApplication.getInstance().getProject().isEditModeActive()) {
            iPartsPlugin.setSimAutoResponseDelayForSession(Session.get(), iPartsPlugin.SIM_AUTO_RESPONSE_DELAY,
                                                           mainWindow.simAutoResponseDelayTextField.getValue());
            iPartsPlugin.setSimAutoResponseDelayForSession(Session.get(), iPartsPlugin.SIM_AUTO_RESPONSE_DELAY_SEARCH,
                                                           mainWindow.simAutoResponseDelaySearchTextField.getValue());
        }
    }

    private void showSelectedMQChannel(Event event) {
        mainWindow.mqInputTextarea.clear();
        mainWindow.mqOutputTextarea.clear();
        removeFromChannel();
        MQChannelType channelType = mainWindow.channelComboBox.getSelectedUserObject();
        if (channelType != null) {
            currentChannel = MQHelper.getInstance().getChannel(channelType);
        } else {
            currentChannel = null;
        }
        addToChannel();
        updateControls();
    }

    /**
     * Passt die Texte zum ausgewählten MQ Kanal an
     */
    private void handleButtons() {
        MQChannelType channelType = mainWindow.channelComboBox.getSelectedUserObject();
        if (channelType != null) {
            String buttonText;
            if (channelType.getChannelName().isXMLChannel()) {
                buttonText = "!!... aus XML-Datei";
                mainWindow.openXMLFileBtn.setText("!!Gespeicherte XML-Dateien anzeigen...");
            } else {
                buttonText = "!!... aus Datei";
                mainWindow.openXMLFileBtn.setText("!!Gespeicherte Dateien anzeigen...");

                // enabled(true) wurde vorher in checkConnection() bereits gesetzt
                mainWindow.mqInputTextarea.setEnabled(false);
                mainWindow.sendBtn.setEnabled(false);
                mainWindow.sendFileBtn.setEnabled(false);
                mainWindow.simulateAnswerBtn.setEnabled(false);
            }
            mainWindow.simulateAnswerFileBtn.setText(buttonText);
            mainWindow.sendFileBtn.setText(buttonText);
        }
    }

    private void sendMQMessage(String outMessage, boolean simulateMQAnswer) {
        if (outMessage.isEmpty()) {
            // Keine Übersetzung, da MQ Test nur für Debug-Zwecke verwendet wird
            MessageDialog.showWarning("Die MQ Nachricht ist leer und wird daher nicht gesendet.", "MQ Test");
            return;
        }

        if (!isCancel) {
            try {
                currentChannel.sendMessage(outMessage, null, simulateMQAnswer);
                if (simulateMQAnswer) {
                    session.invokeThreadSafe(new Runnable() {
                        @Override
                        public void run() {
                            mainWindow.mqOutputTextarea.clear();
                        }
                    });
                }
            } catch (Exception e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);

                // Session wurde zwischenzeitlich beendet -> Reinitialisierung versuchen
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + currentChannel.getChannelType().getChannelName()
                                                                       + ": MQ Session is closed! Trying to reinitialize...");
                if (currentChannel.reinitSession(true)) {
                    sendMQMessage(outMessage, simulateMQAnswer); // Nachricht erneut versenden
                } else {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + currentChannel.getChannelType().getChannelName()
                                                                           + ": MQ message content could not be sent because no active MQ session exists: "
                                                                           + MQHelper.getLogTextFromMessageText(outMessage, true));
                    session.invokeThreadSafe(new Runnable() {
                        @Override
                        public void run() {
                            updateControls();
                        }
                    });
                    MessageDialog.showError(TranslationHandler.translate("!!Senden von MQ Messages nicht möglich, da keine aktive MQ Session für den MQ Kanal \"%1\" existiert.",
                                                                         currentChannel.getChannelType().getChannelName().getTypeName()));
                }
            }
        }
    }

    private List<DWFile> getSelectedFiles() {
        GuiFileChooserDialog fileChooserDialog = new GuiFileChooserDialog(mainWindow, FileChooserPurpose.OPEN,
                                                                          GuiFileChooserDialog.FILE_MODE_FILES, null, false);
        fileChooserDialog.setServerMode(false);
        fileChooserDialog.setMultiSelectionMode(true);
        // Nur bei Kanälen mit XML Dateien, die File Filter verwenden
        iPartsMQChannelTypeNames channelName = currentChannel.getChannelType().getChannelName();
        if (channelName.isXMLChannel()) {
            fileChooserDialog.addChoosableFileFilter(DWFileFilterEnum.XMLFILES.getDescription(), DWFileFilterEnum.XMLFILES.getExtensions());
            fileChooserDialog.setActiveFileFilter(DWFileFilterEnum.XMLFILES.getDescription());
            fileChooserDialog.addFileOpenValidator(new GuiFileOpenValidatorForExtensions(MimeTypes.EXTENSION_XML));
        }
        fileChooserDialog.setVisible(true);
        return fileChooserDialog.getSelectedFiles();
    }

    private List<DWFile> getSelectedFilesFolderMode() {
        GuiFileChooserDialog fileChooserDialog = new GuiFileChooserDialog(mainWindow, FileChooserPurpose.OPEN,
                                                                          GuiFileChooserDialog.FILE_MODE_DIRECTORIES, null, false);
        fileChooserDialog.setServerMode(true);
        try {
            if (iPartsPlugin.isImportPluginActive()) {
                DWFile importServerFilesDir = de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.getPluginConfig().
                        getConfigValueAsDWFile(de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.CONFIG_IMPORT_FILE_SERVER_DIR);
                fileChooserDialog.setRootDirectory("!!Importverzeichnis für große Dateien auf dem Server", importServerFilesDir);
            }
        } catch (NoClassDefFoundError e) {
            // nichts tun -> Root-Verzeichnis bleibt beim Standard
        }
        fileChooserDialog.setVisible(true);
        DWFile selectedFile = fileChooserDialog.getSelectedFile();
        if (selectedFile != null) {
            iPartsMQChannelTypeNames channelName = currentChannel.getChannelType().getChannelName();
            boolean isTextContentChannel = channelName.isTextContentChannel();
            return selectedFile.listDWFilesRecursively(pathname -> {
                // Nur bei Kanälen mit XML Dateien, die Filter verwenden
                return pathname.isFile() && (isTextContentChannel || DWFile.hasExtension(pathname.getPath(), MimeTypes.EXTENSION_XML)); // keine Unterverzeichnisse und ungültige Dateien auflisten
            });
        } else {
            return null;
        }
    }

    private void sendMQMessageFromFile(final boolean simulateMQAnswer, final boolean folderMode) {
        final List<DWFile> selectedFiles;
        if (folderMode) {
            selectedFiles = getSelectedFilesFolderMode();
        } else {
            selectedFiles = getSelectedFiles();
        }
        if ((selectedFiles == null) || selectedFiles.isEmpty()) {
            return;
        }

        final String selectedFilesCountString = String.valueOf(selectedFiles.size());
        final EtkMessageLogForm messageLogForm;
        String fileTypePrefix = ((currentChannel != null) && currentChannel.getChannelType().getChannelName().isXMLChannel()) ? "XML-" : "";
        if (selectedFiles.size() > 1) {
            messageLogForm = new EtkMessageLogForm(fileTypePrefix + TranslationHandler.translate(simulateMQAnswer
                                                                                                 ? "!!Dateien vom MQ-Ziel simulieren..."
                                                                                                 : "!!Dateien an das MQ-Ziel senden..."),
                                                   TranslationHandler.translate("!!Sende %1 %2Dateien", selectedFilesCountString,
                                                                                fileTypePrefix),
                                                   null) {
                @Override
                protected void cancel(Event event) {
                    isCancel = true;
                }
            };
            multipleFiles = true;
        } else {
            messageLogForm = null;
        }

        FrameworkRunnable sendFilesRunnable = new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                boolean reduceLog = selectedFiles.size() > MAX_FILES_FOR_DETAILED_LOG;
                if (reduceLog) {
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Sende %1 %2Dateien...",
                                                                                            selectedFilesCountString, fileTypePrefix),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                }

                int selectedFileIndex = 0;
                for (DWFile selectedFile : selectedFiles) {
                    if (isCancel) {
                        break;
                    }
                    selectedFileIndex++;
                    if (messageLogForm != null) {
                        if (!reduceLog) {
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Sende %1Datei %2/%3: %4",
                                                                                                    fileTypePrefix,
                                                                                                    String.valueOf(selectedFileIndex),
                                                                                                    selectedFilesCountString,
                                                                                                    selectedFile.getName()),
                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                        }
                        messageLogForm.getMessageLog().fireProgress(selectedFileIndex, selectedFiles.size(), "", false, true);
                    }

                    try {
                        String fileContent = selectedFile.readTextFile(DWFileCoding.UTF8);
                        if (J2EEHandler.isJ2EE() && !folderMode) {
                            selectedFile.deleteRecursivelyWithRepeat();
                        }

                        // Nur den Inhalt der letzten Datei in der Textarea anzeigen
                        if (selectedFileIndex == selectedFiles.size()) {
                            session.invokeThreadSafe(new Runnable() {
                                @Override
                                public void run() {
                                    mainWindow.mqInputTextarea.setText(StrUtils.makeAbbreviation(fileContent, iPartsConst.MAX_MQ_MESSAGE_LOG_CHAR_COUNT) + "\n");
                                }
                            });
                        }

                        sendMQMessage(fileContent, simulateMQAnswer);
                    } catch (IOException e) {
                        Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                        MessageDialog.showError("!!Die Datei kann nicht geladen werden!");
                    }
                }

                if (messageLogForm != null) {
                    if (ignoreErrors) {
                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Bei der Verarbeitung mehrerer Nachrichten ist mindestens ein Fehler aufgetreten.") +
                                                                   " " + TranslationHandler.translate("!!Bitte Log-Dateien prüfen."),
                                                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    }
                    if (!reduceLog && !ignoreErrors) {
                        messageLogForm.closeWindow(ModalResult.OK);
                    } else {
                        messageLogForm.getMessageLog().fireMessage("!!Fertig", MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    }
                }
            }
        };

        if (messageLogForm != null) {
            messageLogForm.showModal(sendFilesRunnable);
        } else {
            sendFilesRunnable.run(null);
        }
    }

    /**
     * Schickt den eingegebenen Text Richtung Provider
     *
     * @param event
     */
    private void sendMQMessage(Event event) {
        resetCancelErrorHandling();
        sendMQMessage(mainWindow.mqInputTextarea.getText(), false);
    }

    /**
     * Schickt den eingegebenen Text Richtung Provider
     *
     * @param event
     */
    private void sendMQMessageFile(Event event) {
        resetCancelErrorHandling();
        sendMQMessageFromFile(false, false);
    }

    /**
     * Simuliert eine MQ-Antwort mit einer hochgeladenen Datei
     *
     * @param event
     */
    private void simulateMQAnswer(Event event) {
        resetCancelErrorHandling();
        sendMQMessage(mainWindow.mqInputTextarea.getText(), true);
    }

    /**
     * Simuliert eine MQ-Antwort mit einer hochgeladenen Datei
     *
     * @param event
     */
    private void simulateMQAnswerFile(Event event) {
        resetCancelErrorHandling();
        sendMQMessageFromFile(true, false);
    }

    private void simulateMQAnswerFolder(Event event) {
        resetCancelErrorHandling();
        sendMQMessageFromFile(true, true);
    }

    public void resetCancelErrorHandling() {
        isCancel = false;
        ignoreErrors = false;
        multipleFiles = false;
    }

    public void show() {
        resetCancelErrorHandling();
        mainWindow.showModal();
    }

    private void updateControls() {
        mainWindow.connectionTextfield.setText(connectionFactoryJNDI);
        String out;
        String in;
        if (currentChannel == null) {
            out = "Kein MQ Kanal ausgewählt";
            in = out;
        } else {
            out = currentChannel.getChannelType().getOutQueue();
            in = currentChannel.getChannelType().getInQueue();
        }
        mainWindow.destinationOutTextfield.setText(out);
        mainWindow.destinationInTextfield.setText(in);
        mainWindow.reinitButton.setEnabled(currentChannel != null);
        mainWindow.openXMLFileBtn.setVisible(iPartsPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsPlugin.CONFIG_XML_FILES_DIR) != null);
        mainWindow.openXMLFileBtn.setEnabled(currentChannel != null);
        if (checkConnection()) {
            // Zusätzliche Prüfung, ob die Texte zum Kanal passen
            handleButtons();
        }
    }

    private boolean checkConnection() {
        boolean connectionValid = false;
        if (currentChannel != null) {
            connectionValid = currentChannel.isReadyForCommunication();
        }
        mainWindow.defaultProviderInfoPanel.removeAllChildren();
        if (!connectionValid && (currentChannel != null)) {
            GuiLabel label = new GuiLabel();
            label.setText("MQ Kanal konnte nicht initialisiert werden!");
            mainWindow.defaultProviderInfoPanel.addChild(label);
        }

        mainWindow.mqInputTextarea.setEnabled(connectionValid);
        mainWindow.sendBtn.setEnabled(connectionValid);
        mainWindow.sendFileBtn.setEnabled(connectionValid);
        mainWindow.simulateAnswerBtn.setEnabled(connectionValid);
        mainWindow.simulateAnswerFileBtn.setEnabled(connectionValid);
        mainWindow.simulateAnswerFolderBtn.setEnabled(connectionValid);
        return connectionValid;
    }

    public void setConnectionFactoryJNDI(String connectionFactoryJNDI) {
        this.connectionFactoryJNDI = connectionFactoryJNDI;
    }


    @Override
    public void messageReceived(final Message message, final MQChannelType channelType) throws JMSException {
        if (!mainWindow.isVisible()) { // Der Dialog könnte inzwischen bereits geschlossen worden sein
            return;
        }

        if ((message != null) && (currentChannel != null) && (currentChannel.getChannelType() == channelType)) {
            final String inMessage = MQHelper.getInstance().messageToString(message, false);
            session.startChildThread(new FrameworkRunnable() { // messageReceived() erfolgt aus keinem Session-Thread -> Session-Kind-Thread erzeugen
                @Override
                public void run(FrameworkThread thread) {
                    session.invokeThreadSafe(new Runnable() { // GUI-Änderungen müssen mit invokeThreadSafe durchgeführt werden
                        @Override
                        public void run() {
                            if (!mainWindow.isVisible()) { // Der Dialog könnte inzwischen bereits geschlossen worden sein
                                return;
                            }

                            // Output Fenster nicht befüllen falls mehrere Dateien verarbeitet werden
                            if (!multipleFiles) {
                                mainWindow.mqOutputTextarea.setText(mainWindow.mqOutputTextarea.getText()
                                                                    + (!mainWindow.mqOutputTextarea.getText().isEmpty() ? "\n\n" : "")
                                                                    + inMessage + "\n\n");
                            }

                            // XML Nachricht aus MQ Message analog zu iPartsXMLMessageManager.messageReceived() erzeugen,
                            // um den MQ Kanaltyp zu überprüfen
                            if (message instanceof TextMessage) {
                                String content = null;
                                try {
                                    content = ((TextMessage)message).getText();
                                } catch (JMSException e) {
                                    showErrorMessage(e.getMessage());
                                }

                                // Validieren der XML Datei ist nur notwendig, wenn der Kanal auch XML Dateien empfangen kann
                                if (channelType.getChannelName().isXMLChannel()) {
                                    boolean validXMLContent = true;
                                    if (content == null) {
                                        validXMLContent = false;
                                    }
                                    if (validXMLContent) {
                                        try {
                                            AbstractMQMessage xmlMQMessage = XMLImportExportHelper.buildMessageFromXMLFile(content, channelType, false, false);
                                            if (xmlMQMessage != null) {
                                                if (!xmlMQMessage.isValidForMQChannelTypeName(channelType.getChannelName())) {
                                                    String errorText = TranslationHandler.translate("!!Die empfangene MQ Nachricht vom Typ \"%1\" ist für den MQ Kanal \"%2\" nicht gültig: %3",
                                                                                                    xmlMQMessage.getMessageType(), channelType.getChannelName().getTypeName(),
                                                                                                    '\n' + MQHelper.getLogTextFromMessageText(content, false));
                                                    showErrorMessage(errorText);
                                                    return;
                                                }
                                            } else {
                                                validXMLContent = false;
                                            }
                                        } catch (IOException e) {
                                            validXMLContent = false;
                                        } catch (SAXException e) {

                                            showErrorMessage(TranslationHandler.translate("!!Die Validierung vom XML aus der empfangenen MQ Nachricht gegen das XML Schema war nicht erfolgreich.")
                                                             + '\n' + TranslationHandler.translate("!!Fehlermeldung: %1",
                                                                                                   e.getMessage())
                                                             + '\n' + TranslationHandler.translate("!!Inhalt der empfangenen Nachricht: %1",
                                                                                                   '\n' + MQHelper.getLogTextFromMessageText(content, false)));
                                            return;
                                        }
                                    }

                                    if (!validXMLContent) {
                                        showErrorMessage(TranslationHandler.translate("!!Empfangene MQ Nachricht enthält kein gültiges XML oder ist für den Kanal \"%1\" nicht gültig. Inhalt der empfangenen Nachricht: %2",
                                                                                      channelType.getChannelName().getTypeName(), '\n' + MQHelper.getLogTextFromMessageText(content, false)));
                                    }
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * Unterscheidet beim Anzeigen ob Fehlermeldungen nur einmalig angezeigt werden sollen (beim Import mehrerer Dateien).
     * Sollte nur innerhalb invokeThreadSafe aufgerufen werden
     *
     * @param detailedMessage Text für normale Fehlermeldung
     * @param shortMessage    Text für einmalige Fehlermeldung
     */
    private void showErrorMessage(String detailedMessage, String shortMessage) {
        if (multipleFiles) {
            if (!ignoreErrors) {
                ignoreErrors = true;
                MessageDialog.showError(shortMessage);
            }
        } else {
            MessageDialog.showError(detailedMessage);
        }
    }

    /**
     * Zum Anzeigen der Fehlermeldung mit Standardtext falls nur einmal der Fehler angezeigt werden soll.
     * Sollte nur innerhalb invokeThreadSafe aufgerufen werden
     *
     * @param detailedMessage Text für normale Fehlermeldung
     */
    private void showErrorMessage(String detailedMessage) {
        showErrorMessage(detailedMessage, TranslationHandler.translate("!!Bei der Verarbeitung mehrerer Nachrichten ist mindestens ein Fehler aufgetreten.") +
                                          "\n" + TranslationHandler.translate("!!Bitte Log-Dateien prüfen."));
    }

    private void reinitChannel(Event event) {
        boolean result = MQHelper.getInstance().reinitChannel(currentChannel.getChannelType(), true);
        updateControls();
        if (result) {
            MessageDialog.show("Reinitialisierung vom MQ Kanal \"" + currentChannel.getChannelType().getChannelName() + "\" erfolgreich.");
        } else {
            MessageDialog.showError("Reinitialisierung vom MQ Kanal \"" + currentChannel.getChannelType().getChannelName() + "\" fehlgeschlagen!");
        }
    }

    private void openXMLFile(Event event) {
        DWFile mqFilesLocation = null;
        iPartsMQChannelTypeNames channelName = currentChannel.getChannelType().getChannelName();
        if (channelName == iPartsMQChannelTypeNames.MEDIA) {
            mqFilesLocation = iPartsPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsPlugin.CONFIG_XML_FILES_DIR).getChild(DWFile.convertToValidFileName(channelName.getTypeName()));
        } else {
            try {
                if (iPartsPlugin.isImportPluginActive()) {
                    mqFilesLocation = de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.getPluginConfig().
                            getConfigValueAsDWFile(de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.CONFIG_IMPORT_FILE_DIR).
                            getChild(channelName.getTypeName());
                }
            } catch (NoClassDefFoundError e) {
                MessageDialog.show("Für den MQ Kanal \"" + channelName + "\" konnte der Speicherort nicht geöffnet werden.");
                return;
            }

        }
        if (mqFilesLocation == null) {
            MessageDialog.show("Für den MQ Kanal \"" + channelName + "\" konnte der Speicherort nicht geöffnet werden.");
            return;
        }
        String fileTypePrefix = channelName.isXMLChannel() ? "XML-" : "";
        if (!mqFilesLocation.isDirectory()) {
            MessageDialog.show("Für den MQ Kanal \"" + channelName + "\" existieren noch keine gespeicherten " + fileTypePrefix
                               + "Dateien.");
            return;
        }

        // FileChooser
        GuiFileChooserDialog fileChooserDialog = new GuiFileChooserDialog(FileChooserPurpose.OPEN, GuiFileChooserDialog.FILE_MODE_FILES, mqFilesLocation, false);
        fileChooserDialog.setRootDirectory(fileTypePrefix + "Dateien für MQ Kanal \"" + channelName.getTypeName() + "\"", mqFilesLocation);
        fileChooserDialog.addChoosableFileFilter(DWFileFilterEnum.XMLFILES.getDescription(), DWFileFilterEnum.XMLFILES.getExtensions());
        fileChooserDialog.setActiveFileFilter(DWFileFilterEnum.XMLFILES.getDescription());
        fileChooserDialog.setServerMode(true);
        fileChooserDialog.setShowCreateDirectoryButton(false);
        fileChooserDialog.setVisible(true);
        DWFile selectedFile = fileChooserDialog.getSelectedFile();

        // XML-Datei auslesen und im Clipboard-Dialog anzeigen
        if (selectedFile != null) {
            try {
                String xmlContent = selectedFile.readTextFile(DWFileCoding.UTF8);
                CopyTextWindow window = new CopyTextWindow(xmlContent);
                window.showModal();
            } catch (IOException e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);
                MessageDialog.showError("!!Die Datei kann nicht geladen werden!");
            }
        }
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle mqTestTitle;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel leftPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel mqConfigPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel configLabelPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel channelLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<MQChannelType> channelComboBox;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel connectionLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField connectionTextfield;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel dstinationOutLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField destinationOutTextfield;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel destinationInLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField destinationInTextfield;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel defaultProviderInfoPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel reinitPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton reinitButton;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel mqSimPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel simAutoResponseDelayLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.spinner.GuiIntSpinner simAutoResponseDelayTextField;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel EQU144538;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel simAutoResponseDelaySearchLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.spinner.GuiIntSpinner simAutoResponseDelaySearchTextField;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel LVH144539;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel mqTextPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel mqInputLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel mqOutputLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane mqInputScrollpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea mqInputTextarea;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane mqOutputScrollpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea mqOutputTextarea;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelSendButtons;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton sendBtn;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton sendFileBtn;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton simulateAnswerBtn;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton simulateAnswerFileBtn;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton simulateAnswerFolderBtn;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel sendLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel simulateLabel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiButton openXMLFileBtn;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            mqTestTitle = new de.docware.framework.modules.gui.controls.GuiTitle();
            mqTestTitle.setName("mqTestTitle");
            mqTestTitle.__internal_setGenerationDpi(96);
            mqTestTitle.registerTranslationHandler(translationHandler);
            mqTestTitle.setScaleForResolution(true);
            mqTestTitle.setMinimumWidth(5);
            mqTestTitle.setMinimumHeight(50);
            mqTestTitle.setTitle("MQ Test");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mqTestTitleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mqTestTitleConstraints.setPosition("north");
            mqTestTitle.setConstraints(mqTestTitleConstraints);
            this.addChild(mqTestTitle);
            leftPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            leftPanel.setName("leftPanel");
            leftPanel.__internal_setGenerationDpi(96);
            leftPanel.registerTranslationHandler(translationHandler);
            leftPanel.setScaleForResolution(true);
            leftPanel.setMinimumWidth(10);
            leftPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder leftPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            leftPanel.setLayout(leftPanelLayout);
            mqConfigPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            mqConfigPanel.setName("mqConfigPanel");
            mqConfigPanel.__internal_setGenerationDpi(96);
            mqConfigPanel.registerTranslationHandler(translationHandler);
            mqConfigPanel.setScaleForResolution(true);
            mqConfigPanel.setBorderWidth(4);
            mqConfigPanel.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            mqConfigPanel.setTitle("!!Konfiguration");
            de.docware.framework.modules.gui.layout.LayoutGridBag mqConfigPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            mqConfigPanel.setLayout(mqConfigPanelLayout);
            configLabelPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            configLabelPanel.setName("configLabelPanel");
            configLabelPanel.__internal_setGenerationDpi(96);
            configLabelPanel.registerTranslationHandler(translationHandler);
            configLabelPanel.setScaleForResolution(true);
            configLabelPanel.setPaddingTop(8);
            de.docware.framework.modules.gui.layout.LayoutGridBag configLabelPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            configLabelPanel.setLayout(configLabelPanelLayout);
            channelLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            channelLabel.setName("channelLabel");
            channelLabel.__internal_setGenerationDpi(96);
            channelLabel.registerTranslationHandler(translationHandler);
            channelLabel.setScaleForResolution(true);
            channelLabel.setMinimumWidth(10);
            channelLabel.setMinimumHeight(10);
            channelLabel.setText("!!MQ Kanal wählen:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag channelLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "w", "b", 0, 0, 4, 0);
            channelLabel.setConstraints(channelLabelConstraints);
            configLabelPanel.addChild(channelLabel);
            channelComboBox = new de.docware.framework.modules.gui.controls.GuiComboBox<MQChannelType>();
            channelComboBox.setName("channelComboBox");
            channelComboBox.__internal_setGenerationDpi(96);
            channelComboBox.registerTranslationHandler(translationHandler);
            channelComboBox.setScaleForResolution(true);
            channelComboBox.setMinimumWidth(10);
            channelComboBox.setMinimumHeight(10);
            channelComboBox.setMaximumRowCount(20);
            channelComboBox.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    showSelectedMQChannel(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag channelComboBoxConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "w", "b", 0, 0, 16, 0);
            channelComboBox.setConstraints(channelComboBoxConstraints);
            configLabelPanel.addChild(channelComboBox);
            connectionLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            connectionLabel.setName("connectionLabel");
            connectionLabel.__internal_setGenerationDpi(96);
            connectionLabel.registerTranslationHandler(translationHandler);
            connectionLabel.setScaleForResolution(true);
            connectionLabel.setMinimumWidth(10);
            connectionLabel.setMinimumHeight(10);
            connectionLabel.setText("Connection JNDI Alias:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag connectionLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "w", "n", 0, 0, 4, 0);
            connectionLabel.setConstraints(connectionLabelConstraints);
            configLabelPanel.addChild(connectionLabel);
            connectionTextfield = new de.docware.framework.modules.gui.controls.GuiTextField();
            connectionTextfield.setName("connectionTextfield");
            connectionTextfield.__internal_setGenerationDpi(96);
            connectionTextfield.registerTranslationHandler(translationHandler);
            connectionTextfield.setScaleForResolution(true);
            connectionTextfield.setMinimumWidth(150);
            connectionTextfield.setMinimumHeight(10);
            connectionTextfield.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag connectionTextfieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 0.0, "w", "h", 0, 0, 8, 0);
            connectionTextfield.setConstraints(connectionTextfieldConstraints);
            configLabelPanel.addChild(connectionTextfield);
            dstinationOutLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            dstinationOutLabel.setName("dstinationOutLabel");
            dstinationOutLabel.__internal_setGenerationDpi(96);
            dstinationOutLabel.registerTranslationHandler(translationHandler);
            dstinationOutLabel.setScaleForResolution(true);
            dstinationOutLabel.setMinimumWidth(10);
            dstinationOutLabel.setMinimumHeight(10);
            dstinationOutLabel.setText("Destination OUT JNDI Alias:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag dstinationOutLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 4, 1, 1, 100.0, 0.0, "sw", "n", 0, 0, 4, 0);
            dstinationOutLabel.setConstraints(dstinationOutLabelConstraints);
            configLabelPanel.addChild(dstinationOutLabel);
            destinationOutTextfield = new de.docware.framework.modules.gui.controls.GuiTextField();
            destinationOutTextfield.setName("destinationOutTextfield");
            destinationOutTextfield.__internal_setGenerationDpi(96);
            destinationOutTextfield.registerTranslationHandler(translationHandler);
            destinationOutTextfield.setScaleForResolution(true);
            destinationOutTextfield.setMinimumWidth(150);
            destinationOutTextfield.setMinimumHeight(10);
            destinationOutTextfield.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag destinationOutTextfieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 5, 1, 1, 0.0, 0.0, "w", "h", 0, 0, 8, 0);
            destinationOutTextfield.setConstraints(destinationOutTextfieldConstraints);
            configLabelPanel.addChild(destinationOutTextfield);
            destinationInLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            destinationInLabel.setName("destinationInLabel");
            destinationInLabel.__internal_setGenerationDpi(96);
            destinationInLabel.registerTranslationHandler(translationHandler);
            destinationInLabel.setScaleForResolution(true);
            destinationInLabel.setMinimumWidth(10);
            destinationInLabel.setMinimumHeight(10);
            destinationInLabel.setText("Destination IN JNDI Alias:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag destinationInLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 6, 1, 1, 100.0, 0.0, "w", "n", 0, 0, 4, 0);
            destinationInLabel.setConstraints(destinationInLabelConstraints);
            configLabelPanel.addChild(destinationInLabel);
            destinationInTextfield = new de.docware.framework.modules.gui.controls.GuiTextField();
            destinationInTextfield.setName("destinationInTextfield");
            destinationInTextfield.__internal_setGenerationDpi(96);
            destinationInTextfield.registerTranslationHandler(translationHandler);
            destinationInTextfield.setScaleForResolution(true);
            destinationInTextfield.setMinimumWidth(150);
            destinationInTextfield.setMinimumHeight(10);
            destinationInTextfield.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag destinationInTextfieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 7, 1, 1, 0.0, 0.0, "w", "b", 0, 0, 8, 0);
            destinationInTextfield.setConstraints(destinationInTextfieldConstraints);
            configLabelPanel.addChild(destinationInTextfield);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag configLabelPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "n", "h", 4, 4, 4, 4);
            configLabelPanel.setConstraints(configLabelPanelConstraints);
            mqConfigPanel.addChild(configLabelPanel);
            defaultProviderInfoPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            defaultProviderInfoPanel.setName("defaultProviderInfoPanel");
            defaultProviderInfoPanel.__internal_setGenerationDpi(96);
            defaultProviderInfoPanel.registerTranslationHandler(translationHandler);
            defaultProviderInfoPanel.setScaleForResolution(true);
            defaultProviderInfoPanel.setMinimumWidth(10);
            defaultProviderInfoPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutFlow defaultProviderInfoPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutFlow();
            defaultProviderInfoPanel.setLayout(defaultProviderInfoPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag defaultProviderInfoPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 100.0, "c", "v", 4, 4, 4, 4);
            defaultProviderInfoPanel.setConstraints(defaultProviderInfoPanelConstraints);
            mqConfigPanel.addChild(defaultProviderInfoPanel);
            reinitPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            reinitPanel.setName("reinitPanel");
            reinitPanel.__internal_setGenerationDpi(96);
            reinitPanel.registerTranslationHandler(translationHandler);
            reinitPanel.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutGridBag reinitPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            reinitPanel.setLayout(reinitPanelLayout);
            reinitButton = new de.docware.framework.modules.gui.controls.GuiButton();
            reinitButton.setName("reinitButton");
            reinitButton.__internal_setGenerationDpi(96);
            reinitButton.registerTranslationHandler(translationHandler);
            reinitButton.setScaleForResolution(true);
            reinitButton.setMnemonicEnabled(true);
            reinitButton.setText("!!MQ Kanal reinitialisieren");
            reinitButton.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    reinitChannel(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag reinitButtonConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "s", "h", 0, 0, 0, 0);
            reinitButton.setConstraints(reinitButtonConstraints);
            reinitPanel.addChild(reinitButton);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag reinitPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            reinitPanel.setConstraints(reinitPanelConstraints);
            mqConfigPanel.addChild(reinitPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mqConfigPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mqConfigPanel.setConstraints(mqConfigPanelConstraints);
            leftPanel.addChild(mqConfigPanel);
            mqSimPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            mqSimPanel.setName("mqSimPanel");
            mqSimPanel.__internal_setGenerationDpi(96);
            mqSimPanel.registerTranslationHandler(translationHandler);
            mqSimPanel.setScaleForResolution(true);
            mqSimPanel.setMinimumWidth(10);
            mqSimPanel.setMinimumHeight(10);
            mqSimPanel.setBorderWidth(4);
            mqSimPanel.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            mqSimPanel.setPaddingTop(8);
            mqSimPanel.setTitle("!!AS-PLM Simulation für diese Session");
            de.docware.framework.modules.gui.layout.LayoutGridBag mqSimPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            mqSimPanelLayout.setCentered(false);
            mqSimPanel.setLayout(mqSimPanelLayout);
            simAutoResponseDelayLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            simAutoResponseDelayLabel.setName("simAutoResponseDelayLabel");
            simAutoResponseDelayLabel.__internal_setGenerationDpi(96);
            simAutoResponseDelayLabel.registerTranslationHandler(translationHandler);
            simAutoResponseDelayLabel.setScaleForResolution(true);
            simAutoResponseDelayLabel.setMinimumWidth(10);
            simAutoResponseDelayLabel.setMinimumHeight(10);
            simAutoResponseDelayLabel.setText("!!Automatische Antwort nach x Sekunden");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag simAutoResponseDelayLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "w", "n", 4, 4, 4, 4);
            simAutoResponseDelayLabel.setConstraints(simAutoResponseDelayLabelConstraints);
            mqSimPanel.addChild(simAutoResponseDelayLabel);
            simAutoResponseDelayTextField = new de.docware.framework.modules.gui.controls.spinner.GuiIntSpinner();
            simAutoResponseDelayTextField.setName("simAutoResponseDelayTextField");
            simAutoResponseDelayTextField.__internal_setGenerationDpi(96);
            simAutoResponseDelayTextField.registerTranslationHandler(translationHandler);
            simAutoResponseDelayTextField.setScaleForResolution(true);
            simAutoResponseDelayTextField.setMinimumWidth(100);
            simAutoResponseDelayTextField.setMinimumHeight(10);
            EQU144538 = new de.docware.framework.modules.gui.controls.GuiLabel();
            EQU144538.setName("EQU144538");
            EQU144538.__internal_setGenerationDpi(96);
            EQU144538.registerTranslationHandler(translationHandler);
            EQU144538.setScaleForResolution(true);
            EQU144538.setText("!!-1 für keine automatische Antwort");
            simAutoResponseDelayTextField.setTooltip(EQU144538);
            simAutoResponseDelayTextField.setMinValue(-1);
            simAutoResponseDelayTextField.setMaxValue(1000);
            simAutoResponseDelayTextField.setValue(-1);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag simAutoResponseDelayTextFieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "w", "h", 4, 4, 8, 4);
            simAutoResponseDelayTextField.setConstraints(simAutoResponseDelayTextFieldConstraints);
            mqSimPanel.addChild(simAutoResponseDelayTextField);
            simAutoResponseDelaySearchLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            simAutoResponseDelaySearchLabel.setName("simAutoResponseDelaySearchLabel");
            simAutoResponseDelaySearchLabel.__internal_setGenerationDpi(96);
            simAutoResponseDelaySearchLabel.registerTranslationHandler(translationHandler);
            simAutoResponseDelaySearchLabel.setScaleForResolution(true);
            simAutoResponseDelaySearchLabel.setMinimumWidth(10);
            simAutoResponseDelaySearchLabel.setMinimumHeight(10);
            simAutoResponseDelaySearchLabel.setText("!!Automatische Antwort nach x Sekunden für Suchen");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag simAutoResponseDelaySearchLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "w", "n", 4, 4, 4, 4);
            simAutoResponseDelaySearchLabel.setConstraints(simAutoResponseDelaySearchLabelConstraints);
            mqSimPanel.addChild(simAutoResponseDelaySearchLabel);
            simAutoResponseDelaySearchTextField = new de.docware.framework.modules.gui.controls.spinner.GuiIntSpinner();
            simAutoResponseDelaySearchTextField.setName("simAutoResponseDelaySearchTextField");
            simAutoResponseDelaySearchTextField.__internal_setGenerationDpi(96);
            simAutoResponseDelaySearchTextField.registerTranslationHandler(translationHandler);
            simAutoResponseDelaySearchTextField.setScaleForResolution(true);
            simAutoResponseDelaySearchTextField.setMinimumWidth(100);
            simAutoResponseDelaySearchTextField.setMinimumHeight(10);
            LVH144539 = new de.docware.framework.modules.gui.controls.GuiLabel();
            LVH144539.setName("LVH144539");
            LVH144539.__internal_setGenerationDpi(96);
            LVH144539.registerTranslationHandler(translationHandler);
            LVH144539.setScaleForResolution(true);
            LVH144539.setText("!!-1 für keine automatische Antwort");
            simAutoResponseDelaySearchTextField.setTooltip(LVH144539);
            simAutoResponseDelaySearchTextField.setMinValue(-1);
            simAutoResponseDelaySearchTextField.setMaxValue(1000);
            simAutoResponseDelaySearchTextField.setValue(-1);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag simAutoResponseDelaySearchTextFieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 0.0, "w", "h", 4, 4, 4, 4);
            simAutoResponseDelaySearchTextField.setConstraints(simAutoResponseDelaySearchTextFieldConstraints);
            mqSimPanel.addChild(simAutoResponseDelaySearchTextField);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mqSimPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mqSimPanelConstraints.setPosition("south");
            mqSimPanel.setConstraints(mqSimPanelConstraints);
            leftPanel.addChild(mqSimPanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder leftPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            leftPanelConstraints.setPosition("west");
            leftPanel.setConstraints(leftPanelConstraints);
            this.addChild(leftPanel);
            mqTextPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            mqTextPanel.setName("mqTextPanel");
            mqTextPanel.__internal_setGenerationDpi(96);
            mqTextPanel.registerTranslationHandler(translationHandler);
            mqTextPanel.setScaleForResolution(true);
            mqTextPanel.setMinimumWidth(10);
            mqTextPanel.setBorderWidth(4);
            mqTextPanel.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            mqTextPanel.setTitle("MQ Transaction");
            de.docware.framework.modules.gui.layout.LayoutGridBag mqTextPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            mqTextPanel.setLayout(mqTextPanelLayout);
            mqInputLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            mqInputLabel.setName("mqInputLabel");
            mqInputLabel.__internal_setGenerationDpi(96);
            mqInputLabel.registerTranslationHandler(translationHandler);
            mqInputLabel.setScaleForResolution(true);
            mqInputLabel.setFontStyle(de.docware.framework.modules.gui.controls.misc.DWFontStyle.BOLD);
            mqInputLabel.setText("!!MQ Nachricht zum Senden");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag mqInputLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "w", "h", 4, 8, 0, 0);
            mqInputLabel.setConstraints(mqInputLabelConstraints);
            mqTextPanel.addChild(mqInputLabel);
            mqOutputLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            mqOutputLabel.setName("mqOutputLabel");
            mqOutputLabel.__internal_setGenerationDpi(96);
            mqOutputLabel.registerTranslationHandler(translationHandler);
            mqOutputLabel.setScaleForResolution(true);
            mqOutputLabel.setFontStyle(de.docware.framework.modules.gui.controls.misc.DWFontStyle.BOLD);
            mqOutputLabel.setText("!!Empfangene MQ Nachrichten");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag mqOutputLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "w", "h", 4, 8, 0, 0);
            mqOutputLabel.setConstraints(mqOutputLabelConstraints);
            mqTextPanel.addChild(mqOutputLabel);
            mqInputScrollpane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            mqInputScrollpane.setName("mqInputScrollpane");
            mqInputScrollpane.__internal_setGenerationDpi(96);
            mqInputScrollpane.registerTranslationHandler(translationHandler);
            mqInputScrollpane.setScaleForResolution(true);
            mqInputScrollpane.setBorderWidth(1);
            mqInputScrollpane.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            mqInputTextarea = new de.docware.framework.modules.gui.controls.GuiTextArea();
            mqInputTextarea.setName("mqInputTextarea");
            mqInputTextarea.__internal_setGenerationDpi(96);
            mqInputTextarea.registerTranslationHandler(translationHandler);
            mqInputTextarea.setScaleForResolution(true);
            mqInputTextarea.setPaddingTop(8);
            mqInputTextarea.setPaddingLeft(8);
            mqInputTextarea.setPaddingRight(8);
            mqInputTextarea.setPaddingBottom(8);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mqInputTextareaConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mqInputTextarea.setConstraints(mqInputTextareaConstraints);
            mqInputScrollpane.addChild(mqInputTextarea);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag mqInputScrollpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 100.0, "w", "b", 0, 4, 4, 2);
            mqInputScrollpane.setConstraints(mqInputScrollpaneConstraints);
            mqTextPanel.addChild(mqInputScrollpane);
            mqOutputScrollpane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            mqOutputScrollpane.setName("mqOutputScrollpane");
            mqOutputScrollpane.__internal_setGenerationDpi(96);
            mqOutputScrollpane.registerTranslationHandler(translationHandler);
            mqOutputScrollpane.setScaleForResolution(true);
            mqOutputScrollpane.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            mqOutputScrollpane.setBorderWidth(1);
            mqOutputScrollpane.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            mqOutputTextarea = new de.docware.framework.modules.gui.controls.GuiTextArea();
            mqOutputTextarea.setName("mqOutputTextarea");
            mqOutputTextarea.__internal_setGenerationDpi(96);
            mqOutputTextarea.registerTranslationHandler(translationHandler);
            mqOutputTextarea.setScaleForResolution(true);
            mqOutputTextarea.setBackgroundColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clTransparent"));
            mqOutputTextarea.setPaddingTop(8);
            mqOutputTextarea.setPaddingLeft(8);
            mqOutputTextarea.setPaddingRight(8);
            mqOutputTextarea.setPaddingBottom(8);
            mqOutputTextarea.setEditable(false);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mqOutputTextareaConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mqOutputTextarea.setConstraints(mqOutputTextareaConstraints);
            mqOutputScrollpane.addChild(mqOutputTextarea);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag mqOutputScrollpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 100.0, "c", "b", 0, 2, 4, 0);
            mqOutputScrollpane.setConstraints(mqOutputScrollpaneConstraints);
            mqTextPanel.addChild(mqOutputScrollpane);
            panelSendButtons = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelSendButtons.setName("panelSendButtons");
            panelSendButtons.__internal_setGenerationDpi(96);
            panelSendButtons.registerTranslationHandler(translationHandler);
            panelSendButtons.setScaleForResolution(true);
            panelSendButtons.setMinimumWidth(10);
            panelSendButtons.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelSendButtonsLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelSendButtons.setLayout(panelSendButtonsLayout);
            sendBtn = new de.docware.framework.modules.gui.controls.GuiButton();
            sendBtn.setName("sendBtn");
            sendBtn.__internal_setGenerationDpi(96);
            sendBtn.registerTranslationHandler(translationHandler);
            sendBtn.setScaleForResolution(true);
            sendBtn.setMnemonicEnabled(true);
            sendBtn.setText("!!... aus Textbox");
            sendBtn.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    sendMQMessage(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag sendBtnConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "h", 1, 1, 1, 1);
            sendBtn.setConstraints(sendBtnConstraints);
            panelSendButtons.addChild(sendBtn);
            sendFileBtn = new de.docware.framework.modules.gui.controls.GuiButton();
            sendFileBtn.setName("sendFileBtn");
            sendFileBtn.__internal_setGenerationDpi(96);
            sendFileBtn.registerTranslationHandler(translationHandler);
            sendFileBtn.setScaleForResolution(true);
            sendFileBtn.setMinimumWidth(100);
            sendFileBtn.setMinimumHeight(10);
            sendFileBtn.setMnemonicEnabled(true);
            sendFileBtn.setText("!!... aus XML-Datei");
            sendFileBtn.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    sendMQMessageFile(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag sendFileBtnConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 0, 1, 1, 0.0, 0.0, "c", "h", 1, 1, 1, 1);
            sendFileBtn.setConstraints(sendFileBtnConstraints);
            panelSendButtons.addChild(sendFileBtn);
            simulateAnswerBtn = new de.docware.framework.modules.gui.controls.GuiButton();
            simulateAnswerBtn.setName("simulateAnswerBtn");
            simulateAnswerBtn.__internal_setGenerationDpi(96);
            simulateAnswerBtn.registerTranslationHandler(translationHandler);
            simulateAnswerBtn.setScaleForResolution(true);
            simulateAnswerBtn.setMnemonicEnabled(true);
            simulateAnswerBtn.setText("!!... aus Textbox");
            simulateAnswerBtn.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    simulateMQAnswer(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag simulateAnswerBtnConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "s", "h", 1, 1, 1, 1);
            simulateAnswerBtn.setConstraints(simulateAnswerBtnConstraints);
            panelSendButtons.addChild(simulateAnswerBtn);
            simulateAnswerFileBtn = new de.docware.framework.modules.gui.controls.GuiButton();
            simulateAnswerFileBtn.setName("simulateAnswerFileBtn");
            simulateAnswerFileBtn.__internal_setGenerationDpi(96);
            simulateAnswerFileBtn.registerTranslationHandler(translationHandler);
            simulateAnswerFileBtn.setScaleForResolution(true);
            simulateAnswerFileBtn.setMinimumWidth(100);
            simulateAnswerFileBtn.setMinimumHeight(10);
            simulateAnswerFileBtn.setMnemonicEnabled(true);
            simulateAnswerFileBtn.setText("!!... aus XML-Datei");
            simulateAnswerFileBtn.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    simulateMQAnswerFile(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag simulateAnswerFileBtnConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(2, 1, 1, 1, 0.0, 0.0, "c", "h", 1, 1, 1, 1);
            simulateAnswerFileBtn.setConstraints(simulateAnswerFileBtnConstraints);
            panelSendButtons.addChild(simulateAnswerFileBtn);
            simulateAnswerFolderBtn = new de.docware.framework.modules.gui.controls.GuiButton();
            simulateAnswerFolderBtn.setName("simulateAnswerFolderBtn");
            simulateAnswerFolderBtn.__internal_setGenerationDpi(96);
            simulateAnswerFolderBtn.registerTranslationHandler(translationHandler);
            simulateAnswerFolderBtn.setScaleForResolution(true);
            simulateAnswerFolderBtn.setMinimumWidth(100);
            simulateAnswerFolderBtn.setMinimumHeight(10);
            simulateAnswerFolderBtn.setMnemonicEnabled(true);
            simulateAnswerFolderBtn.setText("!!... aus Verzeichnis");
            simulateAnswerFolderBtn.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    simulateMQAnswerFolder(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag simulateAnswerFolderBtnConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(3, 1, 1, 1, 0.0, 0.0, "c", "h", 1, 1, 1, 1);
            simulateAnswerFolderBtn.setConstraints(simulateAnswerFolderBtnConstraints);
            panelSendButtons.addChild(simulateAnswerFolderBtn);
            sendLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            sendLabel.setName("sendLabel");
            sendLabel.__internal_setGenerationDpi(96);
            sendLabel.registerTranslationHandler(translationHandler);
            sendLabel.setScaleForResolution(true);
            sendLabel.setMinimumWidth(10);
            sendLabel.setMinimumHeight(10);
            sendLabel.setText("!!Nachricht ans MQ-Ziel senden");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag sendLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 1, 1, 1, 1);
            sendLabel.setConstraints(sendLabelConstraints);
            panelSendButtons.addChild(sendLabel);
            simulateLabel = new de.docware.framework.modules.gui.controls.GuiLabel();
            simulateLabel.setName("simulateLabel");
            simulateLabel.__internal_setGenerationDpi(96);
            simulateLabel.registerTranslationHandler(translationHandler);
            simulateLabel.setScaleForResolution(true);
            simulateLabel.setMinimumWidth(10);
            simulateLabel.setMinimumHeight(10);
            simulateLabel.setText("!!Antwort vom MQ-Ziel simulieren");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag simulateLabelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 1, 1, 1, 1);
            simulateLabel.setConstraints(simulateLabelConstraints);
            panelSendButtons.addChild(simulateLabel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelSendButtonsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "c", "n", 0, 0, 0, 0);
            panelSendButtons.setConstraints(panelSendButtonsConstraints);
            mqTextPanel.addChild(panelSendButtons);
            openXMLFileBtn = new de.docware.framework.modules.gui.controls.GuiButton();
            openXMLFileBtn.setName("openXMLFileBtn");
            openXMLFileBtn.__internal_setGenerationDpi(96);
            openXMLFileBtn.registerTranslationHandler(translationHandler);
            openXMLFileBtn.setScaleForResolution(true);
            openXMLFileBtn.setMnemonicEnabled(true);
            openXMLFileBtn.setText("!!Gespeicherte XML-Dateien anzeigen...");
            openXMLFileBtn.addEventListener(new de.docware.framework.modules.gui.event.EventListener("actionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    openXMLFile(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag openXMLFileBtnConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 0.0, 0.0, "n", "n", 0, 0, 8, 0);
            openXMLFileBtn.setConstraints(openXMLFileBtnConstraints);
            mqTextPanel.addChild(openXMLFileBtn);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mqTextPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mqTextPanel.setConstraints(mqTextPanelConstraints);
            this.addChild(mqTextPanel);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCloseActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    closeWindow(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonPanelConstraints.setPosition("south");
            buttonPanel.setConstraints(buttonPanelConstraints);
            this.addChild(buttonPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}