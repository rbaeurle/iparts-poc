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
public class MadDevelAnalyzeQuantityFieldTal40A extends MadDevelAnalyzeQuantityFieldTalXA {

    public MadDevelAnalyzeQuantityFieldTal40A(EtkProject project) {
        super(project, MadTal4XABaseImporter.Tal4XAType.TAL40A);
    }

}
