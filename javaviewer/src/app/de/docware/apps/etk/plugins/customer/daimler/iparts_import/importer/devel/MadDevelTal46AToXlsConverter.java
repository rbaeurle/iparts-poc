/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.devel;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MadTal4XABaseImporter;


/**
 * Converter ELDAS -> EPC nach Excel, der die Mechanik der normalen Importer verwendet
 * Nur im Developementmodus sichtbar.
 * Da die Importdateien sehr groß sind läuft das nur unter Swing (maximal 100MByte download)
 * Das erstellen der Exceldateien ist sehr speicheraufwendig, deshalb sollte mit einer 64Bit JVM und 4GByte Speicher gearbeitet werden
 */
public class MadDevelTal46AToXlsConverter extends MadDevelTalXToXlsBaseConverter {

    public MadDevelTal46AToXlsConverter(EtkProject project) {
        super(project, "TAL46A Daten zu Excel konvertieren", MadTal4XABaseImporter.Tal4XAType.TAL46A);
    }

}
