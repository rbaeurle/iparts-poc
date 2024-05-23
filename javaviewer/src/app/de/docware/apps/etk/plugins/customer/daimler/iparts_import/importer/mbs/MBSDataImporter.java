/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushHandlerImporter;
import de.docware.framework.modules.gui.misc.MimeTypes;

/**
 * Importer für die SAP-MBS Stammdaten (Teilestamm, Baumuster, Code)
 */
public class MBSDataImporter extends AbstractSAXPushHandlerImporter {

    private static final String TYPE = "SAPMBSImportType";

    public MBSDataImporter(EtkProject project, String importName) {
        super(project, importName, null, new FilesImporterFileListType(TYPE, importName, true, false, false,
                                                                       new String[]{ MimeTypes.EXTENSION_XML }));
        setBufferedSave(true);
    }

    public void saveBufferedList() {
        saveBufferListToDB(true);
    }

    @Override
    protected void writeNoHandlerMessage() {
        getMessageLog().fireMessage(translateForLog("!!Kein Stammdaten-Typ ausgewählt. Import wird abgebrochen!"));
    }

    @Override
    protected String getType() {
        return TYPE;
    }

}
