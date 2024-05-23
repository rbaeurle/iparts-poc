/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.multihandler;

import de.docware.apps.etk.base.project.EtkProject;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstrakte Klasse für spezifische Handler, die nur bestimmte XML Tags verarbeiten, die sich innerhalb eine Haupt-Tags
 * befinden und die Tags samt Werte in einer Map hält.
 */
public abstract class AbstractMappedHandlerWithRecord extends AbstractMappedHandler {

    protected Map<String, String> currentRecord; // Der aktuelle Datensatz, der aufgesammelt wird
    private String handlerName; // Name des Handlers

    public AbstractMappedHandlerWithRecord(EtkProject project, String mainXMLTag, String handlerName) {
        super(project, mainXMLTag);
        this.handlerName = handlerName;
    }

    protected Map<String, String> getCurrentRecord() {
        return currentRecord;
    }

    public String getHandlerName() {
        return handlerName;
    }

    protected void setFieldValue(String fieldname, String content) {
        if (currentRecord != null) {
            currentRecord.put(fieldname, content);
        }
    }

    protected String getValueFromCurrentRecord(String key) {
        String value = getCurrentRecord().get(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    @Override
    protected void onStartElement(String uri, String localName, String qName, Map<String, String> attributes) {
        if (localName.equals(getMainXMLTag())) {
            // Jedesmal, wenn ein Teilestammblock im XML startet, wird ein neuer Record angelegt
            this.currentRecord = new HashMap<>();
        }
    }

    @Override
    protected void onEndElement(String uri, String localName, String qName) {
        super.onEndElement(uri, localName, qName);
        TagData tagData = getCurrentTagData();
        String tagName = tagData.getTagName();
        String content = tagData.getTextValue().toString();
        if (!tagName.equals(getMainXMLTag())) {
            // Schreibe den Wert des aktuellen Tags an der aktuellen Record
            setFieldValue(tagName, content);
        } else {
            // Ende des Haupttags erreicht, also ist der Record komplett. Diesen also jetzt verarbeiten
            handleCurrentRecord();
        }
    }

    /**
     * Methode, die die Logik zum Verarbeiten des aktuellen Records ({@link #getCurrentRecord()}), enthählt.
     */
    protected abstract void handleCurrentRecord();
}
