/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsPemId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.PEMDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.DIALOGImportHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Superklasse, für alle DIALOG XML Importer, die auch PEM Stammdaten anlegen (Urladung oder Änderungsdienst)
 */
public abstract class AbstractDIALOGWithPEMDataImporter extends AbstractDIALOGDataImporter {

    private Map<iPartsPemId, PEMDataHelper.PEMImportRecord> pemData; // PEM Stammdaten sammeln

    public AbstractDIALOGWithPEMDataImporter(EtkProject project, String importName, FilesImporterFileListType... importFileTypes) {
        super(project, importName, importFileTypes);
        pemData = new HashMap<>();
    }

    /**
     * Erzeugt die PEM Stammdaten für den PEM ab und den PEM bis Datensatz im übergebenen <code>importRec</code>. Die
     * erzeugten Stammdaten werden in der <code>pemData</code> Mapp abgelegt.
     *
     * @param importRec
     * @param helper
     */
    protected void handlePEMData(Map<String, String> importRec, DIALOGImportHelper helper) {
        if (pemData != null) {
            String adatFieldname = getADATFieldname();
            String factoryFieldname = getFactoryFieldname();
            helper.checkPEMData(pemData, importRec, adatFieldname, getPemFieldName(true),
                                getPEMTFieldname(true), getSTCFieldname(true), factoryFieldname);
            helper.checkPEMData(pemData, importRec, adatFieldname, getPemFieldName(false),
                                getPEMTFieldname(false), getSTCFieldname(false), factoryFieldname);

        }
    }

    /**
     * Importiert die aufgesammelten PEM Stammdaten in die DB
     */
    protected void importPEMData() {
        if (pemData != null) {
            // Gesammelte PEM Stammdaten importieren/aktualisieren
            DIALOGImportHelper.importPEMMasterData(getProject(), this, pemData);
        }
    }

    /**
     * Deaktiviert das Aufsammeln der PEM Stammdaten
     */
    protected void disablePEMImport() {
        pemData = null;
    }

    protected void clearPEMs() {
        pemData.clear();
    }

    protected abstract String getSTCFieldname(boolean isFrom);

    protected abstract String getPEMTFieldname(boolean isFrom);

    protected abstract String getPemFieldName(boolean isFrom);

    protected abstract String getFactoryFieldname();

    protected abstract String getADATFieldname();
}
