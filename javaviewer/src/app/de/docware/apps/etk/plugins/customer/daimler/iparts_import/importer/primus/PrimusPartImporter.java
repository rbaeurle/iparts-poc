/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.primus;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindPRIMUSTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.AbstractXMLPartImporter;
import de.docware.framework.modules.gui.misc.MimeTypes;

/**
 * PRIMUS Partsdata Importer
 * In den XML-Dateien sind die Zusatzinformationen von Primus zum Teilestamm
 */
public class PrimusPartImporter extends AbstractXMLPartImporter {

    private PrimusPartDataHandler handler;

    /**
     * Constructor für XML-Datei
     *
     * @param project
     */
    public PrimusPartImporter(EtkProject project) {
        super(project, "!!PRIMUS-Teilestamm", "",
              new FilesImporterFileListType(TABLE_MAT, "!!PRIMUS-Teilestamm", true, false, false,
                                            new String[]{ MimeTypes.EXTENSION_XML }));
    }

    public PrimusPartImporter(EtkProject project, String importName, String xsdFile, FilesImporterFileListType... importFileTypes) {
        super(project, importName, xsdFile, importFileTypes);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForPRIMUS(getMessageLog(), getLogLanguage(),
                                                                                      DictTextKindPRIMUSTypes.MAT_AFTER_SALES)) {
            return false;
        }
        return true;
    }

    @Override
    protected PrimusPartDataHandler getHandler() {
        handler = new PrimusPartDataHandler(getProject(), this);
        return handler;
    }

    @Override
    protected void clearCaches() {
        iPartsPlugin.fireClearGlobalCaches(handler.getCacheTypesForClearCaches());
    }
}