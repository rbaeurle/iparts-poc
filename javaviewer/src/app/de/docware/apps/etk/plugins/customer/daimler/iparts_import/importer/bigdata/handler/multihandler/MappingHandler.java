package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.multihandler;

import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.AbstractXMLHandler;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class MappingHandler extends AbstractXMLHandler {

    private SAXParser saxParser;
    private Map<String, AbstractMappedHandler> mainXMLTagsToDataHandler;
    private EtkMessageLog messageLog;

    public MappingHandler(SAXParser parser, AbstractMappedHandler... dataHandlers) {
        this(dataHandlers);
        setParser(parser);
    }

    public MappingHandler(AbstractMappedHandler... dataHandlers) {
        mainXMLTagsToDataHandler = Arrays.stream(dataHandlers).collect(Collectors.toMap(AbstractMappedHandler::getMainXMLTag, dataHandler -> dataHandler, (existingKey, newKey) -> existingKey));
    }

    public void setParser(SAXParser saxParser) {
        this.saxParser = saxParser;
    }

    @Override
    public int getTagCounter() {
        // Tags aller Handler sammeln
        return super.getTagCounter() + mainXMLTagsToDataHandler.values().stream().mapToInt(AbstractXMLHandler::getTagCounter).sum();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // Check, ob für das aktuelle Element ein eigener Handler existiert
        getMessageLog().fireProgress(getTagCounter(), -1,
                                     TranslationHandler.translate("!!%1 gelesene XML Elemente", String.valueOf(getTagCounter())),
                                     true, true);
        AbstractMappedHandler handler = mainXMLTagsToDataHandler.get(localName);
        if (handler != null) {
            if (saxParser == null) {
                throw new SAXException("SAX Parser for multi handler use does not exist!");
            }
            ElementData elementData = new ElementData(uri, localName, qName, attributes);
            MappingHandlerData mappingHandlerData = new MappingHandlerData(this, getParser(), getMessageLog());
            handler.startCollectingXMLData(mappingHandlerData, elementData);
        } else {
            // Falls nicht, wird hier das Tag gesammelt
            super.startElement(uri, localName, qName, attributes);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        // Handler Bescheid geben, dass das Dokument zu ende ist
        for (AbstractMappedHandler handler : mainXMLTagsToDataHandler.values()) {
            handler.endDocument();
        }
    }

    @Override
    public void startDocument() {
        // Handler Bescheid geben, dass das Dokument anfängt
        mainXMLTagsToDataHandler.values().forEach(AbstractXMLHandler::startDocument);
    }

    @Override
    public void setCancelled(boolean cancelled) {
        super.setCancelled(cancelled);
        mainXMLTagsToDataHandler.values().forEach(handler -> handler.setCancelled(cancelled));
    }

    public SAXParser getParser() {
        return saxParser;
    }

    public void setMessageLog(EtkMessageLog messageLog) {
        this.messageLog = messageLog;
    }

    public EtkMessageLog getMessageLog() {
        return messageLog;
    }

    public static class MappingHandlerData {

        private MappingHandler parentHandler;
        private SAXParser saxParser;
        private EtkMessageLog messageLog;

        public MappingHandlerData(MappingHandler parentHandler, SAXParser saxParser, EtkMessageLog messageLog) {
            this.parentHandler = parentHandler;
            this.saxParser = saxParser;
            this.messageLog = messageLog;
        }

        public MappingHandler getParentHandler() {
            return parentHandler;
        }

        public SAXParser getSaxParser() {
            return saxParser;
        }

        public EtkMessageLog getMessageLog() {
            return messageLog;
        }

        public void changeContentHandler(AbstractXMLHandler contentHandler) throws SAXException {
            getSaxParser().getXMLReader().setContentHandler(contentHandler);
        }

        public void changeToParentHandler() throws SAXException {
            changeContentHandler(parentHandler);
        }
    }

    public static class ElementData {

        private String uri;
        private String localName;
        private String qName;
        private Attributes attributes;

        public ElementData(String uri, String localName, String qName, Attributes attributes) {
            this.uri = uri;
            this.localName = localName;
            this.qName = qName;
            this.attributes = attributes;
        }

        public String getUri() {
            return uri;
        }

        public String getLocalName() {
            return localName;
        }

        public String getqName() {
            return qName;
        }

        public Attributes getAttributes() {
            return attributes;
        }
    }
}
