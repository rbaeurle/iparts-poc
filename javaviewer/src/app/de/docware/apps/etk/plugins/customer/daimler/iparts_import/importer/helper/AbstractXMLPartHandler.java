/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.multihandler.AbstractMappedHandlerWithRecord;

/**
 * Abstrakter Handler für XML Teilestamm Importer (SRM u. PRIMUS), die auf den gleichen XML Trigger hören
 */
public abstract class AbstractXMLPartHandler extends AbstractMappedHandlerWithRecord implements iPartsConst {

    private static final String TRIGGER_ELEMENT = "Msg";

    private AbstractXMLPartImporter importer;

    public AbstractXMLPartHandler(EtkProject project, AbstractXMLPartImporter importer) {
        super(project, TRIGGER_ELEMENT, importer.getImportName(iPartsConst.LOG_FILES_LANGUAGE));
        this.importer = importer;
    }

    @Override
    protected void onEndElement(String uri, String localName, String qName) {
        String tagName = getCurrentTagData().getTagName();
        String tagContent = getCurrentTagData().getTextValue().toString();
        if (!tagName.equals(getMainXMLTag())) {
            // Einfach den Feldnamen und den Content speichern reicht nicht. Der ganze Pfad muss als Key gespeichert werden.
            // Damit ist zumindest sichergestellt dass, falls es den gleichen Key auf unterschiedlichen Ebenen gibt,
            // dieser nicht überschrieben wird.
            // currentRecord wird immer neu instantiiert und besetzt und sofort verarbeitet, da sonst bei mehreren gleichen
            // Pfaden die Daten überschrieben werden. Eigentlich bräuchte man einen Baum statt einer simplen Map um
            // das XML genau abzubilden.
            if (currentRecord != null) {
                currentRecord.put(getCurrentXMLPath(), tagContent);
            }
        } else {
            // Ende des Haupttags erreicht, also ist der Record komplett. Diesen also jetzt verarbeiten
            handleCurrentRecord();
        }
    }

    protected AbstractXMLPartImporter getImporter() {
        return importer;
    }
}
