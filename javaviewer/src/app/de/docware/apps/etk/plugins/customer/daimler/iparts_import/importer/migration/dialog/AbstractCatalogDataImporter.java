/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;

import java.util.Map;

/**
 * AbstractMasterDataImporter für den Katalog-Import (bietet Zugriff auf den Katalog-Import-Worker)
 */
public abstract class AbstractCatalogDataImporter extends AbstractDataImporter {

    /**
     * falls ein Importer das Lexikon benutzt, dann hier eintragen:
     * DictTextKindTypes und tabelDotFieldName
     * dient zur Überprüfung, ob die Lexikon-Art und der Eintrag vorhanden sind.
     *
     * @return
     */
    public static Map<DictTextKindTypes, String> getDictionaryEntries() {
        return null;
    }

    protected iPartsCatalogImportWorker catalogImportWorker;

    public AbstractCatalogDataImporter(EtkProject project, String importName, FilesImporterFileListType... importFileTypes) {
        super(project, importName, false, importFileTypes);
    }

    public AbstractCatalogDataImporter(EtkProject project, String importName, boolean withHeader, FilesImporterFileListType... importFileTypes) {
        super(project, importName, withHeader, importFileTypes);
    }

    public void setProgressMessageType(ProgressMessageType progressMessageType) {
        this.progressMessageType = progressMessageType;
    }

    public iPartsCatalogImportWorker getCatalogImportWorker() {
        return catalogImportWorker;
    }

    public void setCatalogImportWorker(iPartsCatalogImportWorker catalogImportWorker) {
        this.catalogImportWorker = catalogImportWorker;
    }
}
