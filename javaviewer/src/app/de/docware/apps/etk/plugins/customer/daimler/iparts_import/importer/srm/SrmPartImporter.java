/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.srm;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.multihandler.AbstractMappedHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.AbstractXMLPartImporter;
import de.docware.framework.modules.gui.misc.MimeTypes;

/**
 * SRM Teilestamm Importer via XML
 */
public class SrmPartImporter extends AbstractXMLPartImporter {


    public SrmPartImporter(EtkProject project) {
        super(project, "!!SRM-Teilestamm", "",
              new FilesImporterFileListType(TABLE_MAT, "!!SRM-Teilestamm", true, false, false,
                                            new String[]{ MimeTypes.EXTENSION_XML }));
    }

    @Override
    protected AbstractMappedHandler getHandler() {
        return new SrmPartDataHandler(getProject(), this);
    }

}
