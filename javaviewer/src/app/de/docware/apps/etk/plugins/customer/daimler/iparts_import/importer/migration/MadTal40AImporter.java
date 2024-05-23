/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.project.EtkProject;


/**
 * Eldas TAL40A Importer
 * Die TAL40A Dateien sind fixed Length und enthalten die Stücklistendaten der MAD.
 * Diese Datei ist die zentrale Datei der Migration
 * Der eigentliche Import erfolgt in dem kombinierten Importer für TAL40A und TAL46A
 */
public class MadTal40AImporter extends MadTal4XABaseImporter {

    public MadTal40AImporter(EtkProject project) {
        super(project, "!!MAD BM-Kataloge BL (TAL40A)", Tal4XAType.TAL40A);
    }

}