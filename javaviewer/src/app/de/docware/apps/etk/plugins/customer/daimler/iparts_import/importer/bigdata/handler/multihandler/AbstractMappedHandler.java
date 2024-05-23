package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.multihandler;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.AbstractXMLHandler;
import de.docware.framework.modules.gui.misc.MessageLogType;
import org.xml.sax.SAXException;

/**
 * Abstrakte Klasse für spezifische Handler, die nur bestimmte XML Tags verarbeiten, die sich innerhalb eine Haupt-Tags
 * befinden.
 */
public abstract class AbstractMappedHandler extends AbstractXMLHandler {

    private EtkProject project;
    private String mainXMLTag;
    private MappingHandler.MappingHandlerData mappingHandlerData;

    public AbstractMappedHandler(EtkProject project, String mainXMLTag) {
        this.project = project;
        this.mainXMLTag = mainXMLTag;
    }

    public String getMainXMLTag() {
        return mainXMLTag;
    }

    public EtkProject getProject() {
        return project;
    }

    /**
     * Startet das Vearbeiten der XML Daten über den aktuellen Handler
     *
     * @param mappingHandlerData
     * @param startElementData
     * @throws SAXException
     */
    public void startCollectingXMLData(MappingHandler.MappingHandlerData mappingHandlerData, MappingHandler.ElementData startElementData) throws SAXException {
        // Hier den Handler wechseln (spezifischen Handler benutzen)
        mappingHandlerData.changeContentHandler(this);
        this.mappingHandlerData = mappingHandlerData;
        // Das Trigger XML Element aufnehmen
        startElement(startElementData.getUri(), startElementData.getLocalName(), startElementData.getqName(), startElementData.getAttributes());
    }

    /**
     * Schreibt den übergebenen Text in das {@link de.docware.apps.etk.base.project.base.EtkMessageLog}
     *
     * @param message
     * @param messageLogType
     * @param options
     */
    protected void writeMessage(String message, MessageLogType messageLogType, MessageLogOption... options) {
        if ((mappingHandlerData != null) && (mappingHandlerData.getMessageLog() != null)) {
            mappingHandlerData.getMessageLog().fireMessage(message, messageLogType, options);
        }
    }

    /**
     * Pfad zum aktuellen Tag als String, z.B.: <res><part><start>
     *
     * @return
     */
    protected String getCurrentXMLPath() {
        StringBuilder result = new StringBuilder();
        for (TagData tagData : getCurrentTags()) {
            result.append("<");
            result.append(tagData.getTagName());
            result.append(">");
        }
        return result.toString();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (localName.equals(getMainXMLTag())) {
            mappingHandlerData.changeToParentHandler();
        }
    }
}
