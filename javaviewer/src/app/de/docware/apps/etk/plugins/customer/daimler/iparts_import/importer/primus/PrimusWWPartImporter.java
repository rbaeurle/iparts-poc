/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.primus;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCacheType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.AbstractXMLPartImporter;
import de.docware.framework.modules.gui.misc.MimeTypes;

import java.util.EnumSet;

/**
 * PRIMUS Wahlweise-Hinweise Importer
 * In den XML-Dateien sind die Zusatzinformationen von Primus zu den Wahlweise-Hinweisen
 */
public class PrimusWWPartImporter extends AbstractXMLPartImporter {

    private PrimusPartDataHandler handler;

    /**
     * Constructor für XML-Datei
     *
     * @param project
     */
    public PrimusWWPartImporter(EtkProject project) {
        super(project, "!!PRIMUS-Wahlweise-Hinweise", "",
              new FilesImporterFileListType(TABLE_DA_PRIMUS_WW_PART, "!!PRIMUS-Wahlweise-Hinweise", false, false, false,
                                            new String[]{ MimeTypes.EXTENSION_XML }));
        tableName = TABLE_DA_PRIMUS_WW_PART;
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return true;
    }

    @Override
    protected PrimusPartDataHandler getHandler() {
        handler = new PrimusPartDataHandler(getProject(), this);
        return handler;
    }

    @Override
    protected void clearCaches() {
        iPartsPlugin.fireClearGlobalCaches(EnumSet.of(iPartsCacheType.ALL_SMALL_CACHES));
    }
}
