/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataGenericPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsGenericPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Importer für Daten zum Generic Part aus DIALOG (BCTG)
 */
public class GenericPartImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    public static final String DIALOG_TABLENAME = "BCTG";
    public static final String IMPORT_TABLENAME_BCTG = TABLE_NAME_PREFIX + DIALOG_TABLENAME;

    public static final String DEST_TABLENAME = TABLE_DA_GENERIC_PART;

    // Die zu importierenden Spalten des Generic Part
    public static final String BCTG_BR = "BCTG_BR";
    public static final String BCTG_RAS = "BCTG_RAS";
    public static final String BCTG_POSE = "BCTG_POSE";
    public static final String BCTG_SESI = "BCTG_SESI";
    public static final String BCTG_POSP = "BCTG_POSP";
    public static final String BCTG_PV = "BCTG_PV";
    public static final String BCTG_WW = "BCTG_WW";
    public static final String BCTG_ETZ = "BCTG_ETZ";
    public static final String BCTG_AA = "BCTG_AA";
    public static final String BCTG_SDA = "BCTG_SDA";
    public static final String BCTG_SDB = "BCTG_SDB";
    public static final String BCTG_TEIL = "BCTG_TEIL";
    public static final String BCTG_GP = "BCTG_GP";
    public static final String BCTG_VNR = "BCTG_VNR";
    public static final String BCTG_SOLUTION = "BCTG_SOLUTION";

    private HashMap<String, String> mapping;

    private boolean importToDB = true; //sollen die Daten abgespeichert werden?
    // Map mit dem BCTE Schlüssel als Key, damit keine doppelten Datensätze gespeichert werden
    // (in einer Importdatei kann der gleiche Datensatz mehrfach vorkommen)
    private final Map<iPartsGenericPartId, iPartsDataGenericPart> importedDataGenericPartList = new HashMap<>();

    /**
     * Constructor für XML-Datei
     *
     * @param project
     */
    public GenericPartImporter(EtkProject project) {
        super(project, DD_GENERIC_PART, new FilesImporterFileListType(DEST_TABLENAME, DD_GENERIC_PART,
                                                                      false, false, false,
                                                                      new String[]{ MimeTypes.EXTENSION_XML }));
        initImporter();
    }

    private void initImporter() {
        mapping = new HashMap<>();

        // Die Primärschlüsselfelder müssen auch ins Mapping, weil in DA_GENERIC_PART nur die GUID ein Primärschlüsselfeld ist
        mapping.put(FIELD_DGP_SERIES_NO, BCTG_BR);
        mapping.put(FIELD_DGP_POSE, BCTG_POSE);
        mapping.put(FIELD_DGP_POSV, BCTG_PV);
        mapping.put(FIELD_DGP_WW, BCTG_WW);
        mapping.put(FIELD_DGP_ETZ, BCTG_ETZ);
        mapping.put(FIELD_DGP_AA, BCTG_AA);
        mapping.put(FIELD_DGP_SDATA, BCTG_SDA);

        // Die übrigen Felder:
        mapping.put(FIELD_DGP_SESI, BCTG_SESI);
        mapping.put(FIELD_DGP_POSP, BCTG_POSP);
        mapping.put(FIELD_DGP_SDATB, BCTG_SDB);
        mapping.put(FIELD_DGP_PARTNO, BCTG_TEIL);
        mapping.put(FIELD_DGP_GENERIC_PARTNO, BCTG_GP);
        mapping.put(FIELD_DGP_VARIANTNO, BCTG_VNR);
        mapping.put(FIELD_DGP_SOLUTION, BCTG_SOLUTION);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustExists = new String[]{ BCTG_BR, BCTG_RAS, BCTG_POSE, BCTG_PV, BCTG_WW, BCTG_ETZ,
                                            BCTG_AA, BCTG_SDA };
        String[] mustHaveData = new String[]{ BCTG_BR, BCTG_RAS, BCTG_POSE, BCTG_PV, BCTG_AA, BCTG_SDA };
        // An den Importer anhängen.
        importer.setMustExists(mustExists);
        importer.setMustHaveData(mustHaveData);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(IMPORT_TABLENAME_BCTG)
                   || importer.getTableNames().get(0).equals(DIALOG_TABLENAME);
        }
        return false;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return isDIALOGSpecificHmMSmValueValid(importer, importRec, BCTG_RAS, errors);
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(true);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        GenericPartImportHelper importHelper = new GenericPartImportHelper(getProject(), mapping, DEST_TABLENAME);
        // Wenn die Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
        if (!importHelper.checkImportRelevanceForSeries(BCTG_BR, importRec, getInvalidSeriesSet(), this)) {
            return;
        }

        // Wenn BCTG_SDA mit "9" beginnt, ist der Datensatz noch nicht freigegeben,
        // dann nicht importieren und kompletten Datensatz überspringen
        if (!importHelper.checkImportReleaseDateValid(BCTG_SDA, importRec, this)) {
            return;
        }

        iPartsDialogBCTEPrimaryKey primaryBCTEKey = importHelper.getPrimaryBCTEKey(this, importRec, recordNo);
        if (primaryBCTEKey == null) {
            importHelper.cancelImporterDueToIncorrectBCTEKey(this, recordNo);
            return;
        }

        iPartsGenericPartId id = new iPartsGenericPartId(primaryBCTEKey.createDialogGUID());
        iPartsDataGenericPart importData = new iPartsDataGenericPart(getProject(), id);
        iPartsDataGenericPart existingDataInImportList = importedDataGenericPartList.get(importData.getAsId());

        Optional<iPartsDataGenericPart> result = importHelper.doGenericDataSDBCheck(importData, existingDataInImportList,
                                                                                    FIELD_DGP_SDATB, importRec, recordNo,
                                                                                    this);
        result.ifPresent(data -> {
            // Setzen der separaten HMMSM Felder
            importHelper.setHmMSmFields(data, importRec, BCTG_BR, BCTG_RAS, FIELD_DGP_HM, FIELD_DGP_M, FIELD_DGP_SM);
            // Neue Zuordnung speichern.
            if (importToDB) {
                importedDataGenericPartList.put(data.getAsId(), data);
            }
        });
    }

    @Override
    public void postImportTask() {
        DIALOGImportHelper.saveCollectedObjects(this, importedDataGenericPartList.values());
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return true;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(DEST_TABLENAME)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }


    private class GenericPartImportHelper extends DIALOGImportHelper {

        public GenericPartImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if ((sourceField.equals(BCTG_SDA)) || (sourceField.equals(BCTG_SDB))) {
                value = getDIALOGDateTimeValue(value);
            }
            if (sourceField.equals(BCTG_TEIL)) {
                value = checkNumberInputFormat(value, getMessageLog());
            } else if (sourceField.equals(BCTG_BR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            }
            return value;
        }

        public iPartsDialogBCTEPrimaryKey getPrimaryBCTEKey(AbstractDataImporter importer, Map<String, String> importRec, int recordNo) {
            HmMSmId hmMSmId = HmMSmId.getIdFromRaster(importRec.get(BCTG_BR), importRec.get(BCTG_RAS));
            return getPartListPrimaryBCTEKey(importer, recordNo, hmMSmId,
                                             handleValueOfSpecialField(BCTG_POSE, importRec),
                                             handleValueOfSpecialField(BCTG_PV, importRec),
                                             handleValueOfSpecialField(BCTG_WW, importRec),
                                             handleValueOfSpecialField(BCTG_ETZ, importRec),
                                             handleValueOfSpecialField(BCTG_AA, importRec),
                                             handleValueOfSpecialField(BCTG_SDA, importRec));
        }
    }
}