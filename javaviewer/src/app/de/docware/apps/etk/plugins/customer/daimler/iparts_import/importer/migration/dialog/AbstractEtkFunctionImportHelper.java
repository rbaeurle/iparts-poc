/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.forms.functions.EtkFunction;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;

/**
 * Abstrakte Klasse für EtkFunctions speziell für "Main" Importer
 */
public abstract class AbstractEtkFunctionImportHelper extends EtkFunction {

    protected String importAliasName;
    public boolean isCanceled;
    public int errorCount;
    public int warningCount;
    protected AbstractDataImporter currentImporter;

    public AbstractEtkFunctionImportHelper(String importAliasName) {
        this.importAliasName = importAliasName;
        this.isCanceled = false;
        this.errorCount = 0;
        this.warningCount = 0;
    }

    public void cancelImport() {
        isCanceled = true;
        if (currentImporter != null) {
            currentImporter.cancelImport();
            iPartsJobsManager.getInstance().jobCancelled(currentImporter.getLogFile(), false);
        }
    }

    // Hier ist die Verknüpfung zur anonymen Klasse; Diese wird auf diese Weise gezwungen createImporter() zu implementieren.
    abstract public AbstractDataImporter createImporter();

}
