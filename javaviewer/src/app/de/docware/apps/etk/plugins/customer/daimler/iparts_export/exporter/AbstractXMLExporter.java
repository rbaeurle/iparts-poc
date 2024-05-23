/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.translation.TranslationKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.iPartsExportPlugin;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserDialog;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWOutputStream;
import de.docware.util.misc.CompressionUtils;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstrakter Exporter mit allen Hilfsfunktionen zum Schreiben von XML Dateien. Enthät auch viele Methoden zum Loggen
 * von Meldungen.
 */
public abstract class AbstractXMLExporter implements MessageEvent, ProgressEvent, iPartsConst {

    protected static final String DATEFORMAT_EXPORT_FILE = "yyyyMMddHHmmss";
    protected static final String INVALID_XML_10_CHARACTER = "[^"
                                                             + "\u0009\r\n"
                                                             + "\u0020-\uD7FF"
                                                             + "\uE000-\uFFFD"
                                                             + "\ud800\udc00-\udbff\udfff"
                                                             + "]";

    private EtkProject project;
    private EtkMessageLog messageLog;
    protected int warningCount = 0;
    protected int errorCount = 0;
    private String logLanguage = iPartsConst.LOG_FILES_LANGUAGE;
    private int lastLogFilePercentage = -1;
    private DWFile logFile;
    private XMLStreamWriter xmlWriter;
    private DWOutputStream outputStream;
    private StringWriter stringWriter;
    private Map<String, Integer> elementCount = new HashMap<>();
    private boolean isCancelled = false;
    private String translatedErrorMessage; // Fehlermeldung, die zum kompletten Abbruch des Exporters geführt hat.
    private DWFile exportFile;
    private String exportName;

    public AbstractXMLExporter(EtkProject project, String exportName) {
        this.project = project;
        this.exportName = exportName;
    }

    protected EtkProject getProject() {
        return project;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public boolean isFailed() {
        return (translatedErrorMessage != null) || failExportIfInterrupted();
    }

    public boolean isRunning() {
        return !isCancelled() && !isFailed();
    }

    public String getTranslatedErrorMessage() {
        return (translatedErrorMessage != null) ? translatedErrorMessage : "";
    }

    /**
     * Bricht den Import mit entsprechender Fehlermeldung ab, wenn der Thread gestoppt wurde.
     *
     * @return
     */
    private boolean failExportIfInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            stopExport("!!Export-Thread wurde frühzeitig beendet");
            return true;
        } else {
            return false;
        }
    }


    protected void deleteExportFileDirectory() {
        // Datei wurde heruntergeladen oder Export wurde abgebrochen-> Lösche das temporäre Verzeichnis mit der erzeugten XML Datei
        Session.startChildThreadInSession(thread -> {
            if (exportFile != null) {
                exportFile.getParentDWFile().deleteRecursivelyWithRepeat();
            }
        });
    }

    protected DWFile getExportFile() {
        return exportFile;
    }

    protected EtkMessageLogForm initMessageLogForm(String windowTitle, String title) {
        EtkMessageLogForm logForm = new EtkMessageLogForm(windowTitle, title, null);
        messageLog = logForm.getMessageLog();
        messageLog.addMessageEventListener(this);
        messageLog.addProgressEventListener(this);
        logForm.getButtonPanel().removeEventListeners(GuiButtonPanel.BUTTON_CANCEL_ACTION_PERFORMED_EVENT);
        logForm.getButtonPanel().addEventListener(new EventListener(GuiButtonPanel.BUTTON_CANCEL_ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                cancelledByUser("!!Abbruch durch Benutzer");
            }
        });
        return logForm;
    }

    /**
     * Erstellt die XML Datei, in die alle Informationen geschrieben werden, im angegebenen Verzeichnis.
     *
     * @return
     */
    protected boolean createExportFile() {
        try {
            DWFile tempDir = DWFile.createTempDirectory("daim");
            if (tempDir != null) {
                exportFile = tempDir.getChild(getExportFileName());
            }
        } catch (Exception e) {
            stopExport("!!Fehler beim Anlegen der Export Datei.");
            Logger.logExceptionWithoutThrowing(iPartsExportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            return false;
        }
        if (exportFile == null) {
            stopExport("!!Fehler beim Anlegen der Export Datei.");
            return false;
        }
        return true;
    }

    /**
     * Initialisiert den Outputstream der Exportdatei
     */
    protected boolean initOutputStream() {
        outputStream = getExportFile().getOutputStream();
        if (outputStream == null) {
            stopExport(translateForLog("!!OutputStream konnte nicht geöffnet werden."));
            return false;
        }
        return true;
    }

    /**
     * Initialisiert den XML Writer für das Schreiben in eine echte Datei
     *
     * @param prettyPrint
     * @throws XMLStreamException
     */
    protected boolean initWriterForFile(boolean prettyPrint) throws XMLStreamException {
        if (xmlWriter != null) {
            closeXMLWriter();
        }
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        if (!initOutputStream()) {
            return false;
        }
        xmlWriter = factory.createXMLStreamWriter(outputStream, "UTF-8");
        if (prettyPrint) {
            xmlWriter = new IndentingXMLStreamWriter(xmlWriter);
        }
        return true;
    }

    /**
     * Initialisiert den XML Writer für das Schreiben in einen String
     *
     * @param prettyPrint
     * @throws XMLStreamException
     * @throws IOException
     */
    protected void initWriterForString(boolean prettyPrint) throws XMLStreamException, IOException {
        if (xmlWriter != null) {
            closeXMLWriter();
        }
        if (stringWriter != null) {
            closeStringWriter();
        }
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        stringWriter = new StringWriter();
        xmlWriter = factory.createXMLStreamWriter(new BufferedWriter(stringWriter));
        if (prettyPrint) {
            xmlWriter = new IndentingXMLStreamWriter(xmlWriter);
        }

    }

    protected void closeXMLWriterAndOutputStreamAfterExport() throws XMLStreamException, IOException {
        closeXMLWriter();
        closeOutputStream();
    }

    protected void closeXMLWriter() throws XMLStreamException {
        xmlWriter.close();
    }

    protected void closeStringWriter() throws IOException {
        stringWriter.flush();
        stringWriter.close();
    }

    protected void closeOutputStream() throws IOException {
        // Mögliche gecachte Informationen ausspülen und Streams closen.
        outputStream.flush();
        outputStream.close();
    }

    /**
     * Führt alle nötigen Handlungen nach einem Export durch
     */
    protected void finishExportAndLogFile() {
        if (!isRunning() || (getExportFile() == null)) {
            return;
        }
        fireLineSeparator();
        if ((warningCount > 0) || (errorCount > 0)) {
            fireMessageWithTimeStamp(translateForLog("!!Der Export wurde mit %1 Fehlern und %2 Warnungen beendet",
                                                     Integer.toString(errorCount), Integer.toString(warningCount)));
        } else {
            fireMessageWithTimeStamp(translateForLog("!!Export der Stücklisten erfolgreich beendet."));
        }
        hideProgress();
        iPartsJobsManager.getInstance().jobProcessed(getLogFile());
    }

    public XMLStreamWriter getXmlWriter() {
        return xmlWriter;
    }

    public StringWriter getStringWriter() {
        return stringWriter;
    }

    /**
     * Setzt ein optionales Attribut. Sollte der Wert oder die Attribute leer sein, wird das Element nicht erzeugt
     *
     * @param attributeName
     * @param value
     * @param attributes
     */
    protected void setOptionalAttribute(String attributeName, String value, Map<String, String> attributes) {
        if (attributes == null) {
            return;
        }
        if (StrUtils.isValid(value)) {
            attributes.put(attributeName, value);
        }
    }

    /**
     * Packt die erzeugte XML Datei in ein ZIP Archiv und bietet die gepackte Datei als Download an. Die temporär erzeugte
     * Archivdatei wird nach dem Download gelöscht.
     */
    protected void downloadExportFile() {
        if (exportFile == null) {
            showDownloadError();
            return;
        }

        GuiFileChooserDialog fileChooserDialog = new GuiFileChooserDialog(FileChooserPurpose.SAVE, GuiFileChooserDialog.FILE_MODE_FILES,
                                                                          null, false);
        fileChooserDialog.setServerMode(false);
        try {
            DWFile zipFile;
            do {
                try {
                    zipFile = makeZipFile();
                } catch (Exception e) {
                    Logger.logExceptionWithoutThrowing(iPartsExportPlugin.LOG_CHANNEL_EXPORT, LogType.ERROR, e);
                    zipFile = null;
                }
                if ((zipFile == null) && MessageDialog.showYesNo(TranslationHandler.translate("!!Fehler beim Speichern der Exportdatei.") + "\n"
                                                                 + TranslationHandler.translate("!!Erneut versuchen?"), "!!Export") != ModalResult.YES) {
                    fireMessage(translateForLog("!!Fehler beim Speichern der Exportdatei."), MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                    return;
                }
            } while (zipFile == null);

            DWFile zipFileFinal = zipFile;
            fileChooserDialog.setVisible(zipFile);
            zipFileFinal.deleteOnExit(); // Beim Beenden der JVM auf jeden Fall löschen
            Session.get().addEventListener(new EventListener(Event.DISPOSED_EVENT) { // Beim sauberen Beenden der Session löschen
                @Override
                public void fire(Event event) {
                    if (zipFileFinal != null) {
                        zipFileFinal.deleteRecursivelyWithRepeat();
                    }
                }
            });
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsExportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            showDownloadError();
            return;
        }
    }

    /**
     * Erzeugt aus der Exportdatei eine gepackte ZIP Datei
     *
     * @return
     * @throws IOException
     */
    protected DWFile makeZipFile() throws IOException {
        DWFile zipFile = null;
        if (getExportFile() != null) {
            String fileName = getExportFile().extractFileName(false);
            zipFile = getExportFile().getParentDWFile().getChild(fileName + ".zip");
            zipFile.deleteRecursivelyWithRepeat(1000);
            CompressionUtils.zipDir(zipFile.getAbsolutePath(), getExportFile().extractDirectory(), null, StandardCharsets.UTF_8);
        }
        return zipFile;
    }

    /**
     * Verschiebt die Export-Datei in das eingestellte Verzeichnis
     *
     * @param exportDirectory
     */
    protected boolean moveExportFileToExportDirectory(DWFile exportDirectory) {
        try {
            if (exportDirectory != null) {
                DWFile zipFile = makeZipFile();
                if (zipFile.move(exportDirectory, true)) {
                    fireMessageToLogFileWithTimeStamp(translateForLog("!!Gezippte Datei erfolgreich nach \"%1\" verschoben",
                                                                      exportDirectory.getAbsolutePath()));
                    return true;
                } else {
                    moveUzippedFileAfterError(exportDirectory);
                }
            }
        } catch (IOException e) {
            Logger.logExceptionWithoutThrowing(iPartsExportPlugin.LOG_CHANNEL_EXPORT, LogType.ERROR, e);
            moveUzippedFileAfterError(exportDirectory);
        }
        stopExport("!!Fehler beim Erzeugen der Zip-Datei.");
        return false;
    }

    private void moveUzippedFileAfterError(DWFile directory) {
        fireMessageToLogFileWithTimeStamp(translateForLog("!!Fehler beim Erzeugen der Zip-Datei. Verschiebe die ungezippte Datei..."));
        if (getExportFile().move(directory, true)) {
            fireMessageToLogFileWithTimeStamp(translateForLog("!!Ungezippte Datei wurde nach \"%1\" verschoben.", directory.getAbsolutePath()));
        } else {
            fireMessageToLogFileWithTimeStamp(translateForLog("!!Ungezippte Datei konnte nicht verschoben werden"));
        }
    }

    protected void showDownloadError() {
        errorCount++;
        MessageDialog.showError("!!Fehler beim Speichern der Exportdatei.");
    }

    /**
     * Startet ein XML Element
     *
     * @param xmlElement
     * @throws XMLStreamException
     */
    protected void startElement(String xmlElement) throws XMLStreamException {
        startElement(xmlElement, null);
    }

    /**
     * Startet ein XML Element mit zusätzlichen Attributen
     *
     * @param xmlElement
     * @param attributes
     * @throws XMLStreamException
     */
    protected void startElement(String xmlElement, Map<String, String> attributes) throws XMLStreamException {
        startElement(xmlElement, "", attributes);
    }

    /**
     * Startet ein Element mit Textinhalt und zusätzlichen Attributen
     *
     * @param xmlElement
     * @param content
     * @param attributes
     * @throws XMLStreamException
     */
    protected void startElement(String xmlElement, String content, Map<String, String> attributes) throws XMLStreamException {
        if ((xmlWriter != null) && StrUtils.isValid(xmlElement)) {
            if (elementCount.containsKey(xmlElement)) {
                elementCount.put(xmlElement, elementCount.get(xmlElement) + 1);
            } else {
                elementCount.put(xmlElement, 1);
            }
            xmlWriter.writeStartElement(xmlElement);
            writeContent(xmlElement, content);
            writeAttributes(xmlElement, attributes);
        }
    }

    /**
     * Schreibt den Inhalt(Text) eines XML Elements in das XML Dokument
     *
     * @param xmlElement
     * @param content
     */
    private void writeContent(String xmlElement, String content) {
        // Bei uns sind Texte vorgekommen mit nicht lesbaren Zeichen (Daten aus 2016). Daher hier ein
        // Check, falls so etwas bei DAIMLER auch passieren sollte.
        try {
            if (StrUtils.isValid(content)) {
                xmlWriter.writeCharacters(replaceInvalidChars(content));
            }
        } catch (Exception e) {
            fireMessageToLogFileWithTimeStamp(translateForLog("!!Fehler beim Schreiben des Inhalts zum" +
                                                              " XML Element \"%1\". Der Inhalt wird nicht ausgegeben.",
                                                              xmlElement));
        }
    }

    /**
     * Schreibt die Attribute zum übergebenen XML Element in das XML Dokument
     *
     * @param xmlElement
     * @param attributes
     */
    private void writeAttributes(String xmlElement, Map<String, String> attributes) {
        String attributeName = "";
        if (attributes != null) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                attributeName = entry.getKey();
                writeSingleAttribute(xmlElement, attributeName, entry.getValue());
            }
        }
    }

    /**
     * Schreibt das übergebene Attribut zum übergebenen XML Element in das XML Dokument
     *
     * @param xmlElement
     * @param attributeName
     * @param attributeValue
     */
    private void writeSingleAttribute(String xmlElement, String attributeName, String attributeValue) {
        // Bei uns sind Texte vorgekommen mit nicht lesbaren Zeichen (Daten aus 2016). Daher hier ein
        // Check, falls so etwas bei DAIMLER auch passieren sollte.
        try {
            xmlWriter.writeAttribute(attributeName, replaceInvalidChars(attributeValue));
        } catch (Exception e) {
            fireMessageToLogFileWithTimeStamp(translateForLog("!!Fehler beim Schreiben der Attribute zum" +
                                                              " XML Element \"%1\". Der Text zu Attribut \"%2\" wird " +
                                                              "nicht ausgegeben.",
                                                              xmlElement, attributeName));
        }
    }

    /**
     * Ersetzt XML 1.0 ungültige Character im übergebenen Text mit einem leeren Zeichen
     *
     * @param text
     * @return
     */
    private String replaceInvalidChars(String text) {
        // Character, die in XML 1.0 nicht erlaubt sind müssen entfernt werden
        if (StrUtils.isValid(text)) {
            String result = text.replaceAll(INVALID_XML_10_CHARACTER, "");
            if (!result.equals(text)) {
                fireMessageToLogFileWithTimeStamp(translateForLog("!!Der Text \"%1\" enthält Zeichen, die nicht XML 1.0 konform sind! Es wird folgender text ausgegeben: \"%2\"",
                                                                  text, result));
            }
            return result;
        }
        return text;
    }

    protected void writeEmptyElement(String xmlElement, Map<String, String> attributes, boolean isOptional) throws XMLStreamException {
        if (isOptional && ((attributes == null) || attributes.isEmpty())) {
            return;
        }
        if ((xmlWriter != null) && StrUtils.isValid(xmlElement)) {
            xmlWriter.writeEmptyElement(xmlElement);
            writeAttributes(xmlElement, attributes);
        }
    }

    /**
     * Schreibt ein umschließendes XML Element (mit Anfangs- und Endelement)
     *
     * @param xmlElement
     * @param content
     * @param attributes
     * @param optional
     * @throws XMLStreamException
     */
    protected void writeEnclosedElement(String xmlElement, String content, Map<String, String> attributes,
                                        boolean optional) throws XMLStreamException {
        if (optional && StrUtils.isEmpty(content) && ((attributes == null) || attributes.isEmpty())) {
            return;
        }
        if (StrUtils.isEmpty(content)) {
            writeEmptyElement(xmlElement, attributes, optional);
        } else {
            startElement(xmlElement, content, attributes);
            endElement(xmlElement);
        }
    }

    /**
     * Beendet ein XML Element
     *
     * @param xmlElement
     * @throws XMLStreamException
     */
    protected void endElement(String xmlElement) throws XMLStreamException {
        if (xmlWriter != null) {
            boolean writeEndElement = (elementCount.get(xmlElement) != null) && (elementCount.get(xmlElement) > 0);
            if (writeEndElement) {
                xmlWriter.writeEndElement();
                elementCount.put(xmlElement, elementCount.get(xmlElement) - 1);
            }
        }
    }

    /**
     * Schließt alle verbleibenden (offenen) XML Element
     *
     * @throws XMLStreamException
     */
    protected void closeRemainingElements() throws XMLStreamException {
        for (String element : elementCount.keySet()) {
            endElement(element);
        }
    }

    protected void handleMessageEventData(List<MessageEventData> messageEventDatas, String message, MessageLogType messageLogType, MessageLogOption... options) {
        MessageEventData messageEventData = new MessageEventData(message, messageLogType, options);
        if (messageEventDatas != null) {
            messageEventDatas.add(messageEventData);
        } else {
            ImportExportLogHelper.addLogMessageToLogFile(logFile, messageEventData.getFormattedMessage(getLogLanguage()), true);
        }
    }

    /**
     * Startet den Exporter ohne aktive {@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet}s.
     *
     * @param runnable
     */
    protected void startExportWithoutChangeSets(Runnable runnable) {
        // Aktive Änderungssets temporär deaktivieren
        getProject().executeWithoutActiveChangeSets(runnable, true);
    }


    @Override
    public void fireEvent(MessageEventData event) {
        if (event.getMessageLogType() == MessageLogType.tmlWarning) {
            warningCount++;
        } else if (event.getMessageLogType() == MessageLogType.tmlError) {
            errorCount++;
        }

        ImportExportLogHelper.addLogMessageToLogFile(logFile, event.getFormattedMessage(getLogLanguage()), true);
    }

    @Override
    public void fireEvent(ProgressEventData event) {
        int position = event.getPosition();
        int maxPosition = event.getMaxPosition();

        // Wenn maxPosition <= 0, dann gibt es keine Prozentangaben, weil die Anzahl der zu importierenden Datensätze unbekannt ist
        if (maxPosition > 0) {
            int percentage = (position * 100) / maxPosition;
            if (((percentage % 5) == 0) && (percentage != lastLogFilePercentage)) { // nur alle 5% loggen
                lastLogFilePercentage = percentage;
                ImportExportLogHelper.addLogMessageToLogFile(logFile, event.formatMessage(percentage + "%", getLogLanguage()), true);
            }
        }
    }

    /**
     * Setzt die Sprache, die für Logausgaben verwendet wird.
     *
     * @param logLanguage
     */
    protected void setLogLanguage(String logLanguage) {
        if ((logLanguage == null) || logLanguage.isEmpty()) {
            logLanguage = iPartsConst.LOG_FILES_LANGUAGE; // Standard-Logsprache
        }
        this.logLanguage = logLanguage;
    }

    /**
     * Liefert die Sprache zurück, die für Logausgaben verwendet wird.
     *
     * @return
     */
    protected String getLogLanguage() {
        return logLanguage;
    }

    protected DWFile getLogFile() {
        return logFile;
    }

    protected EtkMessageLog getMessageLog() {
        if (messageLog != null) {
            return messageLog;
        }
        return null;
    }

    /**
     * Liefert den übergebenen Übersetzungsschlüssel für die Logsprache zurück inkl. optionaler Platzhaltertexte.
     *
     * @param translationsKey
     * @param placeHolderTexts
     * @return
     */
    public String translateForLog(String translationsKey, String... placeHolderTexts) {
        return TranslationHandler.translateForLanguage(translationsKey, getLogLanguage(), placeHolderTexts);
    }

    /**
     * Bricht den Import mit der übergebenen Nachricht ab. Wenn der Abbruch durch einen Fehler zustande kam, wird die
     * Log-Meldung als Error-Log abgespeichert.
     *
     * @param message
     */
    protected void stopExport(String message) {
        if (StrUtils.isValid(message)) {
            translatedErrorMessage = TranslationHandler.translateForLanguage(message, getLogLanguage());
        } else {
            translatedErrorMessage = TranslationHandler.translateForLanguage("!!Unbekannter Fehler", getLogLanguage());
        }
        fireLineSeparator();
        if ((message != null) && !message.isEmpty()) {
            fireMessage(message, MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
        }
        iPartsJobsManager.getInstance().jobError(getLogFile());
        // Schreiben ins LogFile verhindern, da es schon verschoben ist und der Export abgebrochen.
        logFile = null;
    }


    /**
     * Setzt die Logdatei, in die sämtliche Logausgaben vom Importer geschrieben werden sollen.
     */
    public void setLogFile() {
        logFile = iPartsJobsManager.getInstance().exportJobRunning(exportName);
        if (!ImportExportLogHelper.checkLogFileState(logFile, true)) {
            logFile = null;
        }
    }

    /**
     * Bricht den Export ab, löscht die Logdatei und schreibt die übergebene Message ins MessageLog, falls diese nicht leer ist.
     * Danach ist also alles so, als ob der Export nie stattgefunden hat.
     */
    protected void cancelledByUser(String message) {
        isCancelled = true;
        fireLineSeparator();
        if ((message != null) && !message.isEmpty()) {
            fireMessage(message, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        }
        iPartsJobsManager.getInstance().jobCancelled(getLogFile(), true);
        // Schreiben ins LogFile verhindern, da es schon gelöscht ist und der Export abgebrochen.
        logFile = null;
    }

    protected void fireLineSeparator() {
        fireMessage(TranslationKeys.LINE_SEPARATOR, MessageLogType.tmlMessage);
    }

    protected void fireWarning(String message) {
        fireMessage(message, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
    }

    protected void fireWarningToLogFile(String message) {
        fireMessage(message, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
    }

    protected void fireMessageWithTimeStamp(String message) {
        fireMessage(message, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
    }

    protected void fireMessageToLogFileWithTimeStamp(String message) {
        fireMessage(message, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
    }

    protected void fireMessage(String message, MessageLogType messageLogType, MessageLogOption... options) {
        MessageEventData messageEventData = new MessageEventData(message, messageLogType, options);
        fireMessage(messageEventData);
    }

    protected void fireMessage(MessageEventData messageEventData) {
        if (getMessageLog() != null) {
            getMessageLog().fireMessage(messageEventData);
        } else {
            ImportExportLogHelper.addLogMessageToLogFile(logFile, messageEventData.getFormattedMessage(getLogLanguage()), true);
        }
    }

    protected void fireProgress(int count, int maxCount, String test, boolean withTimeStamp, boolean lazy) {
        if (getMessageLog() != null) {
            getMessageLog().fireProgress(count, maxCount, test, withTimeStamp, lazy);
        }
    }

    protected void hideProgress() {
        if (getMessageLog() != null) {
            getMessageLog().hideProgress();
        }
    }

    protected abstract String getExportFileName();

    protected abstract String getExportTitle();

}
