/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlFile;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.file.DWOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * Log Meldungen die von CLM / CBSL / Transit an iParts gesendet werden und umgekehrt.
 * Die Meldungen können entweder OK oder ERR Meldungen sein. ERR Meldungen enthalten zusätzlich einen Fehlertext
 */
public class TranslationLogObject {

    private static final String NODE_TRANSJOB = "TransJob";
    private static final String NODE_ANSWER = "Answer";
    private static final String NODE_ID = "id";
    private static final String NODE_INFORMATION = "information";
    private static final String NODE_FILE = "file";
    private static final String NODE_AUTHORINGSYSTEM = "authoringsystem";
    private static final String NODE_TIME = "time";

    private static final String ATT_TYPE = "type";
    private static final String ATT_XMLNS = "xmlns:xsi";
    private static final String ATT_SCHEMA_LOCATION = "xsi:noNamespaceSchemaLocation";
    private static final String ATT_STATE = "state";

    private static final String DEFAULT_SYSTEM_ID = "XPRT";
    private static final String DEFAULT_ID = "1234";
    private static final String SCHEMA_LOCATION = "XPRT-TRANSLATION-BASE-V2.xsd";
    private static final String XML_NS = "http://www.w3.org/2001/XMLSchema-instance";

    private AnswerState state;
    private String id;
    private String message;
    private String filename;
    private String systemId;
    private String timestamp;

    /**
     * Erzeugt ein OK TranslationLogObject mit entsprechender Meldung
     *
     * @param filename inkl. Extension (.zip)
     * @return
     */
    public static TranslationLogObject createOkLogObject(String filename) {
        TranslationLogObject logObject = new TranslationLogObject();
        logObject.setState(AnswerState.OK);
        logObject.setId(getIdFromFilename(filename));
        logObject.setMessage("The file \"" + filename + "\" was accepted. Translations import started.");
        logObject.setFilename(filename);
        logObject.setSystemId(DEFAULT_SYSTEM_ID);
        logObject.setCurrentTimestamp();
        return logObject;
    }

    /**
     * Erzeugt ein ERR TranslationLogObject mit entsprechender Fehlermeldung
     *
     * @param filename inkl. Extension (.zip)
     * @return
     */
    public static TranslationLogObject createErrLogObject(String filename, String errorMessage) {
        TranslationLogObject logObject = new TranslationLogObject();
        logObject.setState(AnswerState.ERR);
        logObject.setId(getIdFromFilename(filename));
        logObject.setMessage("The file \"" + filename + "\" was rejected. " + errorMessage);
        logObject.setFilename(filename);
        logObject.setSystemId(DEFAULT_SYSTEM_ID);
        logObject.setCurrentTimestamp();
        return logObject;
    }

    public static String[] getAllTranslationLogAliases() {
        return AnswerState.getAllAliases();
    }

    /**
     * Erzeugt aus dem übergebenen xml file ein TranslationLogObject
     *
     * @param importFile
     * @return
     */
    public static TranslationLogObject createFromXmlContent(DWFile importFile) {
        try {
            DwXmlFile xmlFile = new DwXmlFile(importFile);
            DwXmlNode rootElement = xmlFile.getRootElement(); // TransJob
            if (rootElement != null) {
                for (DwXmlNode childNode : rootElement.getChildNodes()) {
                    if (childNode.getName().equals(NODE_ANSWER)) {
                        String fileExtension = importFile.getExtension().toUpperCase();
                        String stateAttribute = childNode.getAttribute(ATT_STATE);
                        String state = "";
                        if (!StrUtils.isEmpty(fileExtension, stateAttribute)) {
                            // beide nicht leer
                            if (StrUtils.isValid(fileExtension, stateAttribute)) {
                                if (fileExtension.equals(stateAttribute)) {
                                    state = stateAttribute;
                                }
                            } else {
                                // eines von beiden ist nicht leer
                                if (StrUtils.isValid(stateAttribute)) {
                                    state = stateAttribute;
                                } else if (StrUtils.isValid(fileExtension)) {
                                    state = fileExtension;
                                }
                            }
                        }
                        if (state.isEmpty()) {
                            return null;
                        }
                        return createAnswerObject(state, childNode.getChildNodes());
                    }
                }
            }
        } catch (IOException e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            return null;
        }
        return null;
    }

    private static TranslationLogObject createAnswerObject(String state, List<DwXmlNode> remainingChildren) {
        TranslationLogObject answer = new TranslationLogObject();
        answer.setState(state);
        for (DwXmlNode childNode : remainingChildren) {
            String nodeName = childNode.getName();
            if (StrUtils.isValid(nodeName)) {
                String textContent = childNode.getTextContent();
                if (textContent == null) {
                    textContent = "";
                }
                switch (nodeName) {
                    case NODE_ID:
                        answer.setId(textContent);
                        break;
                    case NODE_INFORMATION:
                        answer.setMessage(textContent);
                        break;
                    case NODE_FILE:
                        answer.setFilename(textContent);
                        break;
                    case NODE_AUTHORINGSYSTEM:
                        answer.setSystemId(textContent);
                        break;
                    case NODE_TIME:
                        answer.setTimestamp(textContent);
                        break;
                }
            }
        }
        return answer;
    }

    private static String getIdFromFilename(String filename) {
        if (StrUtils.isValid(filename)) {
            int endIndexId = filename.indexOf("__");
            if (endIndexId > -1) {
                String firstPart = filename.substring(0, endIndexId);
                int startId = firstPart.lastIndexOf("_");
                if (startId > -1) {
                    String idString = firstPart.substring(startId + 1);
                    if (!idString.isEmpty()) {
                        return idString;
                    }
                }
            }
        }
        return DEFAULT_ID;
    }

    public TranslationLogObject() {
    }

    /**
     * Erzeugt eine XML Antwort für den Truck-Übersetzungsprozess über einen Object Store
     *
     * @param errMessagefileName
     * @param exportDirectoryInObjectStore
     * @return
     */
    public boolean createXmlForObjectStore(String errMessagefileName, String exportDirectoryInObjectStore) {
        try (StringWriter stringWriter = new StringWriter(); BufferedWriter bufferedWriter = new BufferedWriter(stringWriter)) {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            // Den xmlWriter muss man nicht schließen, weil der darunterliegende BuffereWriter schon geschlossen wird
            IndentingXMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(bufferedWriter));
            writeXMLData(xmlWriter, true);
            // Mögliche gecachte Informationen ausspülen
            xmlWriter.flush();
            stringWriter.flush();
            String text = stringWriter.toString();
            TranslationsObjectStoreHelper.getInstance().exportLogMessage(text, errMessagefileName, exportDirectoryInObjectStore);
        } catch (Exception e) {
            Logger.getLogger().handleRuntimeException(e);
            return false;
        }
        return true;
    }

    /**
     * Schreib die eigentlichen Informationen in die XML Datei
     *
     * @param xmlWriter
     * @param isTruckObjectStoreTranslations
     * @throws XMLStreamException
     */
    private void writeXMLData(IndentingXMLStreamWriter xmlWriter, boolean isTruckObjectStoreTranslations) throws XMLStreamException {

        // XML Dokument öffnen
        xmlWriter.writeStartDocument("UTF-8", "1.0");

        xmlWriter.writeStartElement(NODE_TRANSJOB);
        xmlWriter.writeAttribute(ATT_TYPE, NODE_ANSWER);
        xmlWriter.writeAttribute(ATT_XMLNS, XML_NS);
        String schemaLocation = SCHEMA_LOCATION;
        DWFile schemaFile = TranslationsHelper.getSchemaFile(isTruckObjectStoreTranslations);
        if (schemaFile != null) {
            schemaLocation = schemaFile.extractFileName(true);
        }
        xmlWriter.writeAttribute(ATT_SCHEMA_LOCATION, schemaLocation);

        xmlWriter.writeStartElement(NODE_ANSWER);
        xmlWriter.writeAttribute(ATT_STATE, state.name());
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement(NODE_ID);
        xmlWriter.writeCharacters(id);
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement(NODE_INFORMATION);
        xmlWriter.writeCharacters(message);
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement(NODE_FILE);
        xmlWriter.writeCharacters(filename);
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement(NODE_AUTHORINGSYSTEM);
        xmlWriter.writeCharacters(systemId);
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement(NODE_TIME);
        xmlWriter.writeCharacters(timestamp);
        xmlWriter.writeEndElement();

        // XML Dokument schließen
        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    /**
     * Schreibt das aktuelle TranslationLogObject in das übergebene xml file
     *
     * @param targetFile
     * @return
     */
    public boolean createXml(DWFile targetFile) {

        IndentingXMLStreamWriter xmlWriter = null;
        DWOutputStream outputStream = null;
        try {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            outputStream = targetFile.getOutputStream();
            xmlWriter = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(outputStream, "UTF-8"));
            byte[] fileBOM = DWFileCoding.UTF8_BOM.getBom();
            if (fileBOM.length > 0) {
                outputStream.write(fileBOM);
            }
            writeXMLData(xmlWriter, false);
            // Mögliche gecachte Informationen ausspülen
            xmlWriter.flush();
            outputStream.flush();
        } catch (Exception e) {
            Logger.getLogger().handleRuntimeException(e);
            return false;
        } finally {
            try {
                if (xmlWriter != null) {
                    xmlWriter.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }

            } catch (Exception e) {
                Logger.getLogger().handleRuntimeException(e);
                return false;
            }
        }
        return true;
    }

    public AnswerState getState() {
        return state;
    }

    public void setState(String state) {
        this.state = AnswerState.valueOf(state);
    }

    public void setState(AnswerState state) {
        this.state = state;
    }

    public boolean isValid() {
        return state != null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getBundleName() {
        if (StrUtils.isValid(filename)) {
            String bundleName = DWFile.removeExtension(filename);
            if (StrUtils.isValid(bundleName)) {
                return bundleName;
            }
        }
        return "";
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setCurrentTimestamp() {
        this.timestamp = DateUtils.getCurrentDateFormatted(DateUtils.simpleDateTimeFormatddDOTMMDOTyyyy);
    }

    public boolean isOKState() {
        return state == AnswerState.OK;
    }

    protected enum AnswerState {
        ERR,
        OK;

        public static String[] getAllAliases() {
            DwList<String> result = new DwList<>();
            for (AnswerState value : values()) {
                result.add(value.name());
            }
            return ArrayUtil.toStringArray(result);
        }
    }

}
