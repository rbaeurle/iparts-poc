/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.devel;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MadTal4XABaseImporter;


public class MadDevelAnalyzeQuantityFieldTal46A extends MadDevelAnalyzeQuantityFieldTalXA {

    public MadDevelAnalyzeQuantityFieldTal46A(EtkProject project) {
        super(project, MadTal4XABaseImporter.Tal4XAType.TAL46A);
    }

}
