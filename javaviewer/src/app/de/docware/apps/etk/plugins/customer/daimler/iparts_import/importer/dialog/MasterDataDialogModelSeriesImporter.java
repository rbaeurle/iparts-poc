/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelProperties;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.DIALOGModelsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelPropertiesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 *
 */
public class MasterDataDialogModelSeriesImporter extends AbstractDIALOGDataImporter implements iPartsConst {

    //Felder der DIALOG Baumuster Zuordnung zu Baureihen Daten
    public static final String DIALOG_TABLENAME = "X2E";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;

    public static final String X2E_BR = "X2E_BR";
    public static final String X2E_AA = "X2E_AA";
    public static final String X2E_BMAA = "X2E_BMAA";
    public static final String X2E_SDA = "X2E_SDA";
    public static final String X2E_SDB = "X2E_SDB";
    public static final String X2E_LK = "X2E_LK";
    public static final String X2E_PGKZ = "X2E_PGKZ";
    public static final String X2E_CBED = "X2E_CBED";

    private HashMap<String, String> dialogMapping;
    private String tableName;
    private boolean importToDB = true; //sollen die Daten abgespeichert werden?
    private boolean doBufferedSave = true;
    private List<iPartsModelDataId> modelIdsForSynchronisation;
    // Set mit allen Baureihen, deren Einträge bei der Urladung gelöscht werden, falls deren Quelle MAD ist
    private Set<String> seriesMarkedForDeletion;
    // MailboxEvent, um nach dem Import über geänderte Baumuster zu informieren

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MasterDataDialogModelSeriesImporter(EtkProject project) {
        super(project, "!!DIALOG-Stammdaten Baumuster zu Baureihen (X2E)",
              new FilesImporterFileListType(TABLE_DA_MODEL_PROPERTIES, EDS_MODEL_SERIES_NAME, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        this.tableName = TABLE_DA_MODEL_PROPERTIES;

        // Das Mapping für die Baumuster-Felder aus DIALOG in die DA_MODEL-Tabelle
        dialogMapping = new HashMap<>();
        dialogMapping.put(FIELD_DMA_SERIES_NO, X2E_BR);
        dialogMapping.put(FIELD_DMA_AA, X2E_AA);
        dialogMapping.put(FIELD_DMA_MODEL_NO, X2E_BMAA);
        dialogMapping.put(FIELD_DMA_DATB, X2E_SDB);
        dialogMapping.put(FIELD_DMA_STEERING, X2E_LK);
        dialogMapping.put(FIELD_DMA_PRODUCT_GRP, X2E_PGKZ);
        dialogMapping.put(FIELD_DMA_CODE, X2E_CBED);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustExists = new String[]{ X2E_BR, X2E_BMAA, X2E_SDA };
        String[] mustHaveData = new String[]{ X2E_BR, X2E_BMAA, X2E_SDA };

        importer.setMustExists(mustExists);
        importer.setMustHaveData(mustHaveData);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(IMPORT_TABLENAME)
                   || importer.getTableNames().get(0).equals(tableName)
                   || importer.getTableNames().get(0).equals(DIALOG_TABLENAME);
        }
        return false;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        modelIdsForSynchronisation = new DwList<>();
        seriesMarkedForDeletion = new HashSet<>();
        setBufferedSave(doBufferedSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        ModelSeriesHelper importHelper = new ModelSeriesHelper(getProject(), dialogMapping, tableName);
        // Wenn die Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
        if (!importHelper.checkImportRelevanceForSeries(X2E_BR, importRec, getInvalidSeriesSet(), this)) {
            return;
        }

        String constructionBM = importHelper.handleValueOfSpecialField(X2E_BMAA, importRec);
        iPartsModelDataId modelId = new iPartsModelDataId(constructionBM);

        iPartsModelPropertiesId id = getModelPropertyId(importRec, importHelper);
        iPartsDataModelProperties dataModelProperties = new iPartsDataModelProperties(getProject(), id);
        boolean existsInDB = dataModelProperties.existsInDB();
        if (!existsInDB) {
            // Datensatz noch nicht da -> mit leeren Feldern initialisieren
            dataModelProperties.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

            // initial sind alle neuen Baumuster nicht AS-relevant, da das Flag vom Autor im Edit manuell gesetzt wird: Siehe DAIMLER-7565
            dataModelProperties.setFieldValueAsBoolean(FIELD_DMA_AS_RELEVANT, false, DBActionOrigin.FROM_EDIT);
        }
        importHelper.deleteContentIfMADSource(dataModelProperties, FIELD_DMA_SOURCE, false);

        // Datensatz mit den Werten füllen
        importHelper.fillOverrideCompleteDataForDIALOGReverse(dataModelProperties, importRec, null);

        // Die Importdatenquelle auf DIALOG setzen
        dataModelProperties.setFieldValue(iPartsConst.FIELD_DMA_SOURCE, iPartsImportDataOrigin.DIALOG.getOrigin(), DBActionOrigin.FROM_EDIT);

        // Status setzen
        importHelper.setDIALOGStateByImportTypeWithDefault(dataModelProperties, FIELD_DMA_STATUS, isDIALOGInitialDataImport());

        if (importToDB) {
            saveToDB(dataModelProperties);
            // Baumuster aufsammeln, um dann später für alle (nachdem sie in die DB geschrieben worden sind) die Synchronisation
            // zwischen Konstruktion und AS durchzuführen
            modelIdsForSynchronisation.add(modelId);
            String seriesNumber = importHelper.handleValueOfSpecialField(X2E_BR, importRec);
            seriesMarkedForDeletion.add(seriesNumber);
        }
    }

    @Override
    public void postImportTask() {
        if (!isCancelled()) {
            if (!importToDB) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
        }
        // hier wird in die DB gespeichert ...
        super.postImportTask();

        // Nur bei Urladung werden die MAD Daten zu den importierten Baureihen gelöscht.
        if (!isCancelled() && isDIALOGInitialDataImport()) {
            // ... so dass dann hier auch die Konstruktions-BM für den Sync gefunden werden und die überschriebenen
            // MAD Konstruktions-BM bekannt sind, die jetzt als Quelle DIALOG haben.
            for (String seriesNumber : seriesMarkedForDeletion) {
                getProject().getDbLayer().delete(TABLE_DA_MODEL_PROPERTIES, new String[]{ FIELD_DMA_SOURCE, FIELD_DMA_SERIES_NO },
                                                 new String[]{ iPartsImportDataOrigin.MAD.getOrigin(), seriesNumber });
            }
        }

        // Bei Urladung und Delta-Import werden die After-Sales Baumuster synchronisiert.
        if (!isCancelled()) {
            // ... so dass dann hier auch die Konstruktions-BM für den Sync gefunden werden und die überschriebenen
            // MAD Konstruktions-BM bekannt sind, die jetzt als Quelle DIALOG haben.
            for (iPartsModelDataId modelId : modelIdsForSynchronisation) {
                // AS-Baumuster aktualisieren
                DIALOGModelsHelper.synchronizeConstructionAndASModels(modelId, false, true,
                                                                      isDIALOGDeltaDataImport(), getProject());
            }
        }

        modelIdsForSynchronisation = null;
        seriesMarkedForDeletion = null;
    }

    private iPartsModelPropertiesId getModelPropertyId(Map<String, String> importRec, DIALOGImportHelper importHelper) {
        return new iPartsModelPropertiesId(importHelper.handleValueOfSpecialField(X2E_BMAA, importRec),
                                           importHelper.handleValueOfSpecialField(X2E_SDA, importRec));

    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    private class ModelSeriesHelper extends DIALOGImportHelper {

        public ModelSeriesHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(X2E_SDA) || sourceField.equals(X2E_SDB)) {
                value = getDIALOGDateTimeValue(value);
            } else if (sourceField.equals(X2E_BMAA) || sourceField.equals(X2E_BR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            }
            return value;
        }
    }
}