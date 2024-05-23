/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.initial;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSModelImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.Map;

/**
 * EDS (Bom-DB) Baumuster-Stammdaten Importer (T43RBM)
 */
public class EDSModelImporter extends AbstractBOMDataImporter {

    public static final String IMPORT_TABLENAME = "T43RBM";

    private static final String EDS_BM_SNR = "BM_SNR";
    private static final String EDS_BM_AS_AB = "BM_AS_AB";
    private static final String EDS_BM_VAKZ_AB = "BM_VAKZ_AB";
    private static final String EDS_BM_UNG_KZ_BIS = "BM_UNG_KZ_BIS";
    private static final String EDS_BM_AS_BIS = "BM_AS_BIS";
    private static final String EDS_BM_VAKZ_BIS = "BM_VAKZ_BIS";
    private static final String EDS_BM_PGKZ = "BM_PGKZ";
    private static final String EDS_BM_AGRKZ = "BM_AGRKZ";
    private static final String EDS_BM_BEN = "BM_BEN";
    private static final String EDS_BM_VBEZ = "BM_VBEZ";
    private static final String EDS_BM_BEM = "BM_BEM";
    private static final String EDS_BM_TEDAT = "BM_TEDAT";

    private String[] headerNames = new String[]{
            EDS_BM_SNR,        // Baumuster (Feld "SNR"; 13-stellig; in T43RBM und T43RBMS vorhanden)
            EDS_BM_AS_AB,      // Änderungsstand Ab (Feld "AS_AB"; 3-stellig; in T43RBM und T43RBMS vorhanden)
            EDS_BM_VAKZ_AB,    // Verarbeitungskennzeichen Ab (Feld „VAKZ_AB“; 2-stellig; nur in T43RBM vorhanden)
            EDS_BM_UNG_KZ_BIS, // Ungültig-Kennzeichen (Feld "UNG_KZ_BIS"; einstellig; nur in T43RBM vorhanden; nur übernehmen bei Datensätzen mit VAKZ_BIS = leer)
            EDS_BM_AS_BIS,     // Änderungsstand Bis (Feld "AS_BIS"; 3-stellig; in T43RBM und T43RBMS vorhanden; nur übernehmen bei Datensätzen mit VAKZ_BIS = leer, ansonsten den Wert '999' aufnehmen)
            EDS_BM_VAKZ_BIS,   // Verarbeitungskennzeichen Bis (Feld „VAKZ_BIS“; 2-stellig; nur in T43RBM vorhanden)
            EDS_BM_PGKZ,       // Produktgruppenkennzeichen (Feld "PGKZ"; einstellig; nur in T43RBM vorhanden)
            EDS_BM_AGRKZ,      // Aggregatekennzeichen (Feld "AGRKZ"; einstellig; nur in T43RBM vorhanden)
            EDS_BM_BEN,        // Benennung (Feld "BEN"; 50-stellig; in T43RBM und T43RBMS vorhanden)
            EDS_BM_VBEZ,       // Verkaufsbezeichnung (Feld "VBEZ"; 30-stellig; in T43RBM und T43RBMS vorhanden)
            EDS_BM_BEM,        // Bemerkung (Feld "BEM"; 150-stellig; in T43RBM und T43RBMS vorhanden)
            EDS_BM_TEDAT       // Technische Daten (Feld "TEDAT"; 200-stellig; in T43RBM und T43RBMS vorhanden)
    };

    private String[] primaryKeysImport;
    private HashMap<String, String> noBOMmapping;
    private HashMap<String, String> eldasMmapping;

    public EDSModelImporter(EtkProject project) {
        super(project, "EDS Baumusterstamm (T43RBM)", true, TABLE_DA_MODEL, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_MODEL, "!!EDS/BCS Baumuster Stammdatei", true, false, false, new String[]{ MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected void initMapping(HashMap<String, String> mapping) {
        primaryKeysImport = new String[]{ EDS_BM_SNR };
        // Standard Mapping für neuen oder BOM-DB Datensatz
        mapping.put(FIELD_DM_MODEL_NO, EDS_BM_SNR);
        mapping.put(FIELD_DM_NAME, EDS_BM_BEN);
        mapping.put(FIELD_DM_DEVELOPMENT_TITLE, EDS_BM_BEN);
        mapping.put(FIELD_DM_SALES_TITLE, EDS_BM_VBEZ);
        mapping.put(FIELD_DM_PRODUCT_GRP, EDS_BM_PGKZ);
        mapping.put(FIELD_DM_MODEL_TYPE, EDS_BM_AGRKZ);
        mapping.put(FIELD_DM_AS_FROM, EDS_BM_AS_AB);
        mapping.put(FIELD_DM_COMMENT, EDS_BM_BEM);
        mapping.put(FIELD_DM_TECHDATA, EDS_BM_TEDAT);

        // Mapping für MAD-Datensätze
        noBOMmapping = new HashMap<>();
        noBOMmapping.put(FIELD_DM_AS_FROM, EDS_BM_AS_AB);
        noBOMmapping.put(FIELD_DM_COMMENT, EDS_BM_BEM);
        noBOMmapping.put(FIELD_DM_TECHDATA, EDS_BM_TEDAT);

        // Mapping für ELDAS (TAL40) Datensätze (Erweiterung von noBOMmapping)
        eldasMmapping = new HashMap<>(noBOMmapping);
        eldasMmapping.put(FIELD_DM_NAME, EDS_BM_BEN);
        eldasMmapping.put(FIELD_DM_DEVELOPMENT_TITLE, EDS_BM_BEN);
        eldasMmapping.put(FIELD_DM_PRODUCT_GRP, EDS_BM_PGKZ);
        eldasMmapping.put(FIELD_DM_MODEL_TYPE, EDS_BM_AGRKZ);
    }

    @Override
    protected String[] getMustExist() {
        return primaryKeysImport;
    }

    @Override
    protected String[] getMustHaveData() {
        return primaryKeysImport;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        EDSModelImportHelper helper = new EDSModelImportHelper(getProject(), getMapping(), getDestinationTable(), EDS_BM_AGRKZ, EDS_BM_VAKZ_AB,
                                                               EDS_BM_VAKZ_BIS, EDS_BM_AS_AB, EDS_BM_AS_BIS, EDS_BM_UNG_KZ_BIS);
        String modelNo = helper.handleValueOfSpecialField(EDS_BM_SNR, importRec);
        if (StrUtils.isEmpty(modelNo)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (leere Baumuster Nummer)",
                                                        String.valueOf(recordNo)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        iPartsModelId modelId = new iPartsModelId(modelNo);
        iPartsDataModel dataModel = new iPartsDataModel(getProject(), modelId);
        EDSModelImportHelper tempHelper = helper;
        if (dataModel.loadFromDB(modelId)) {
            // Model bereits vorhanden
            if (!dataModel.getFieldValue(FIELD_DM_SOURCE).equals(iPartsImportDataOrigin.EDS.getOrigin())) {
                // es handelt sich um ein Model NICHT aus dem BOM-DB Baumusterstamm-Importer
                // => nur zusätzliche Werte aus der BOM-DB füllen
                HashMap<String, String> tempMapping;
                if (dataModel.getFieldValue(FIELD_DM_SOURCE).equals(iPartsImportDataOrigin.ELDAS.getOrigin())) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 ELDAS-Baumuster \"%2\" gefüllt",
                                                                String.valueOf(recordNo), modelNo),
                                                MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                    tempMapping = eldasMmapping;
                } else {
                    tempMapping = noBOMmapping;
                }
                tempHelper = new EDSModelImportHelper(getProject(), tempMapping, getDestinationTable(), EDS_BM_AGRKZ, EDS_BM_VAKZ_AB,
                                                      EDS_BM_VAKZ_BIS, EDS_BM_AS_AB, EDS_BM_AS_BIS, EDS_BM_UNG_KZ_BIS);
            } else {
                // Die Importdatenquelle auf BOM-DB setzen
                dataModel.setFieldValue(iPartsConst.FIELD_DM_SOURCE, iPartsImportDataOrigin.EDS.getOrigin(), DBActionOrigin.FROM_EDIT);
            }
        } else {
            // neues Model
            dataModel.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

            // Die Importdatenquelle auf BOM-DB setzen
            dataModel.setFieldValue(iPartsConst.FIELD_DM_SOURCE, iPartsImportDataOrigin.EDS.getOrigin(), DBActionOrigin.FROM_EDIT);
        }
        tempHelper.fillBOMDataObject(importRec, dataModel);

        if (importToDB) {
            saveToDB(dataModel);
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
                return importMasterData(prepareImporterKeyValueGZ(importFile, getDestinationTable(), '|', withHeader, headerNames, Character.MIN_VALUE));
            } else {
                return importMasterData(prepareImporterKeyValue(importFile, getDestinationTable(), '|', withHeader, headerNames, Character.MIN_VALUE));
            }
        }
        return false;
    }
}
