package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCacheType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.file.DWFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 * Ableitung eines {@link DefaultHandler} um große XML Dateien via {@link javax.xml.parsers.SAXParser} einzulesen
 */
public abstract class AbstractXMLHandler extends DefaultHandler {

    public static final String IMPORT_CANCELLED_MESSAGE = "SAX Reader stopped because import was cancelled by importer!";

    /**
     * Attribute und Textvalue für ein XML-Tag
     */
    protected static class TagData {

        String tagName;
        Map<String, String> attributes = new HashMap<>();
        StringBuffer textValue = new StringBuffer();

        TagData(String tagName) {
            this.tagName = tagName;
        }

        public String getTagName() {
            return tagName;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public StringBuffer getTextValue() {
            return textValue;
        }
    }

    private int tagCounter;
    private boolean isCancelled;

    // Liste der noch nicht geschlossenen Tags
    private List<TagData> currentTags;
    private DWFile schemaFile;

    public AbstractXMLHandler() {
        currentTags = new ArrayList<>();
    }

    @Override
    public void startDocument() {
        tagCounter = 0;
        onStartDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        onEndDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (isCancelled()) {
            throw new SAXException(IMPORT_CANCELLED_MESSAGE);
        }
        currentTags.add(new TagData(qName));

        TagData tagData = getCurrentTagData();

        // Die Attribute müssen kopiert werden, weil der SaxParser das übergebene Object attributes wiederverwendet und beim nächsten Tag alles überschreibt
        tagData.attributes.clear();
        for (int i = 0; i < attributes.getLength(); i++) {
            tagData.attributes.put(attributes.getQName(i), attributes.getValue(i));
        }

        onStartElement(uri, localName, qName, tagData.attributes);
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        // characters() kann pro TextElement mehrfach vom SAX-Parser mit einzelnen Text-Fragmenten aufgerufen werden

        TagData tagData = getCurrentTagData();
        tagData.textValue.append(ch, start, length);

        onTextElement(tagData.tagName, ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        tagCounter++;
        onEndElement(uri, localName, qName);
        currentTags.remove(currentTags.size() - 1);
    }

    @Override
    public void error(SAXParseException e) {
        Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, getErrorMessage(e, false));
    }

    @Override
    public void fatalError(SAXParseException e) {
        Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, getErrorMessage(e, true));
    }

    @Override
    public void warning(SAXParseException e) {
        Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "SAX Validation warning. Line=" + e.getLineNumber() + ":" + e.getMessage());
    }

    protected String getErrorMessage(SAXParseException e, boolean isFatal) {
        String prefix = isFatal ? "Fatal error" : "Error";
        if (schemaFile == null) {
            return prefix + " reading XML-File. Row=" + e.getLineNumber() + ":" + e.getMessage();
        } else {
            return prefix + " validating file with schema \"" + schemaFile.getName() + "\". Row=" + e.getLineNumber() + ":" + e.getMessage();
        }
    }

    /**
     * Die Inhalte zu dem aktuellen Tag. Das aktuelle Tag ist das letzte in currentTags
     *
     * @return
     */
    protected TagData getCurrentTagData() {
        return currentTags.get(currentTags.size() - 1);
    }

    public int getTagCounter() {
        return tagCounter;
    }

    public List<TagData> getCurrentTags() {
        return currentTags;
    }

    /**
     * Check, ob die übergebene Map Attribut-Werte enthält
     *
     * @param attributes
     * @return
     */
    protected boolean attributesLoaded(Map<String, String> attributes) {
        return (attributes != null) && !attributes.isEmpty();
    }

    /**
     * Holt zum übergebenen Attributnamen das Attribut aus der übergebenen Map
     *
     * @param attributeName
     * @param attributes
     * @return
     */
    protected String getAttributeValue(String attributeName, Map<String, String> attributes) {
        if (attributesLoaded(attributes)) {
            if (attributes.containsKey(attributeName)) {
                return attributes.get(attributeName);
            }
        }
        return "";
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public void setSchemaFile(DWFile schemaFile) {
        this.schemaFile = schemaFile;
    }

    protected void onStartDocument() {
    }

    protected void onEndDocument() {
    }

    protected void onStartElement(String uri, String localName, String qName, Map<String, String> attributes) {
    }

    protected void onEndElement(String uri, String localName, String qName) {
    }

    protected void onTextElement(String tagName, char[] ch, int start, int length) {
    }

    public void onPreImportTask() {
    }

    public void onPostImportTask() {
    }

    public EnumSet<iPartsCacheType> getCacheTypesForClearCaches() {
        // Nur die "kleinen" Caches löschen und nicht die speziellen großen Caches
        return EnumSet.of(iPartsCacheType.ALL_SMALL_CACHES);
    }
}
