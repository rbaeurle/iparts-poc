/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataGenInstallLocation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsGenInstallLocationId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Importer für Generischer-Verbauort-Daten aus DIALOG (POS)
 */
public class GenericInstallLocationImporter extends AbstractDIALOGDataImporter {

    public static final String DIALOG_TABLENAME = "POS";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;
    public static final String DEST_TABLENAME = TABLE_DA_GENERIC_INSTALL_LOCATION;

    public static final String POS_BR = "POS_BR";
    public static final String POS_RAS = "POS_RAS";
    public static final String POS_POSE = "POS_POSE";
    public static final String POS_SESI = "POS_SESI";
    public static final String POS_SDA = "POS_SDA";
    public static final String POS_SDB = "POS_SDB";
    public static final String POS_FED = "POS_FED";
    public static final String POS_STR = "POS_STR";
    public static final String POS_PS = "POS_PS";
    public static final String POS_MK_KZ = "POS_MK_KZ";
    public static final String POS_PETK = "POS_PETK";
    public static final String POS_PWK_KZ = "POS_PWK_KZ";
    public static final String POS_PTK_KZ = "POS_PTK_KZ";
    public static final String POS_ITEXT = "POS_ITEXT";
    public static final String POS_LOEKZ = "POS_LOEKZ";
    public static final String POS_SPLIT = "POS_SPLIT";
    public static final String POS_GEN_VO = "POS_GEN_VO";

    private HashMap<String, String> mapping;
    // Map mit der ID als Key, damit keine doppelten Datensätze gespeichert werden
    // (in einer Importdatei kann der gleiche Datensatz mehrfach vorkommen)
    private final Map<iPartsGenInstallLocationId, iPartsDataGenInstallLocation> importDataMap = new HashMap<>();
    private boolean importToDB = true; //sollen die Daten abgespeichert werden?


    public GenericInstallLocationImporter(EtkProject project) {
        super(project, DD_GENERIC_INSTALL_LOCATION,
              new FilesImporterFileListType(DEST_TABLENAME, DD_GENERIC_INSTALL_LOCATION, false,
                                            false, false, new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        mapping = new HashMap<>();
        mapping.put(FIELD_DGIL_SDB, POS_SDB);
        mapping.put(FIELD_DGIL_SESI, POS_SESI);
        mapping.put(FIELD_DGIL_FED, POS_FED);
        mapping.put(FIELD_DGIL_HIERARCHY, POS_STR);
        mapping.put(FIELD_DGIL_POS_KEY, POS_PS);
        mapping.put(FIELD_DGIL_MK_SIGN, POS_MK_KZ);
        mapping.put(FIELD_DGIL_PET_SIGN, POS_PETK);
        mapping.put(FIELD_DGIL_PWK_SIGN, POS_PWK_KZ);
        mapping.put(FIELD_DGIL_PTK_SIGN, POS_PTK_KZ);
        mapping.put(FIELD_DGIL_INFO_TEXT, POS_ITEXT);
        mapping.put(FIELD_DGIL_DELETE_SIGN, POS_LOEKZ);
        mapping.put(FIELD_DGIL_SPLIT_SIGN, POS_SPLIT);
        mapping.put(FIELD_DGIL_GEN_INSTALL_LOCATION, POS_GEN_VO);
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return isDIALOGSpecificHmMSmValueValid(importer, importRec, POS_RAS, errors);
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

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustExistsAndHave = new String[]{ POS_BR, POS_RAS, POS_POSE, POS_SDA };
        importer.setMustExists(mustExistsAndHave);
        importer.setMustHaveData(mustExistsAndHave);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(IMPORT_TABLENAME)
                   || importer.getTableNames().get(0).equals(DEST_TABLENAME)
                   || importer.getTableNames().get(0).equals(DIALOG_TABLENAME);
        }
        return false;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        GenInstallLocationHelper importHelper = new GenInstallLocationHelper(getProject(), mapping, DEST_TABLENAME);
        // Wenn die Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
        if (!importHelper.checkImportRelevanceForSeries(POS_BR, importRec, getInvalidSeriesSet(), this)) {
            return;
        }

        // Wenn BCTG_SDA mit "9" beginnt, ist der Datensatz noch nicht freigegeben,
        // dann nicht importieren und kompletten Datensatz überspringen
        if (!importHelper.checkImportReleaseDateValid(POS_SDA, importRec, this)) {
            return;
        }

        iPartsGenInstallLocationId id = importHelper.createIdFromImportRec(importRec);
        iPartsDataGenInstallLocation importData = new iPartsDataGenInstallLocation(getProject(), id);
        iPartsDataGenInstallLocation existingImportDataSet = importDataMap.get(importData.getAsId());

        Optional<iPartsDataGenInstallLocation> result = importHelper.doGenericDataSDBCheck(importData, existingImportDataSet,
                                                                                           FIELD_DGIL_SDB, importRec, recordNo,
                                                                                           this);
        result.ifPresent(data -> {
            // Neue Zuordnung speichern.
            if (importToDB) {
                importDataMap.put(data.getAsId(), data);
            }
        });

    }

    @Override
    public void postImportTask() {
        DIALOGImportHelper.saveCollectedObjects(this, importDataMap.values());
        super.postImportTask();
    }

    private class GenInstallLocationHelper extends DIALOGImportHelper {

        public GenInstallLocationHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if ((sourceField.equals(POS_SDA)) || (sourceField.equals(POS_SDB))) {
                value = getDIALOGDateTimeValue(value);
            }
            if (sourceField.equals(POS_BR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            }
            return value;
        }

        /**
         * Erzeugt eine {@link iPartsGenInstallLocationId} aus dem importRec
         *
         * @param importRec
         * @return
         */
        public iPartsGenInstallLocationId createIdFromImportRec(Map<String, String> importRec) {
            HmMSmId hmMSmId = HmMSmId.getIdFromRaster(importRec.get(POS_BR), importRec.get(POS_RAS));
            return new iPartsGenInstallLocationId(hmMSmId,
                                                  handleValueOfSpecialField(POS_POSE, importRec),
                                                  handleValueOfSpecialField(POS_SDA, importRec));
        }
    }


}
