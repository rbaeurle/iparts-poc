/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushDataImporter;
import de.docware.framework.modules.gui.misc.MessageLogType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler für große XML-Dateien basierend auf einem SAX-Parser.
 */
public class BigDataXMLHandler extends AbstractXMLHandler {

    protected AbstractSAXPushDataImporter importer;
    private AbstractKeyValueRecordReader reader;

    public BigDataXMLHandler(AbstractSAXPushDataImporter importer, AbstractKeyValueRecordReader reader) {
        super();
        this.reader = reader;
        this.importer = importer;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (importer.isCancelled()) {
            setCancelled(true);
        }
        super.startElement(uri, localName, qName, attributes);
    }

    /**
     * Pfad zum aktuellen Tag als String, z.B.: <res><part><start>
     *
     * @return
     */
    private String getCurrentXMLPath() {
        StringBuilder result = new StringBuilder();
        for (TagData tagData : getCurrentTags()) {
            result.append("<");
            result.append(tagData.tagName);
            result.append(">");
        }
        return result.toString();
    }

    public AbstractKeyValueRecordReader getReader() {
        return reader;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        Map<String, String> importRec = new HashMap<>();

        TagData tagData = getCurrentTagData();

        String path = getCurrentXMLPath();
        importRec.put(path, tagData.textValue.toString());

        for (Map.Entry<String, String> keyValue : tagData.attributes.entrySet()) {
            importRec.put(path + ":" + keyValue.getKey(), keyValue.getValue());
        }
        super.endElement(uri, localName, qName);

        importer.importPushRecord(getReader(), importRec, getTagCounter());
    }

    /**
     * Tag-Data entfernen
     */


    @Override
    public void error(SAXParseException e) {
        importer.cancelImport(getErrorMessage(e, false), MessageLogType.tmlError);
    }

    @Override
    public void fatalError(SAXParseException e) {
        importer.cancelImport(getErrorMessage(e, true), MessageLogType.tmlError);
    }
}
