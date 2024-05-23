/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.proVal;

import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models.iPartsProValModelsServiceResponseObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.Utils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst.FIELD_DM_SALES_TITLE;
import static de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst.MODEL_NUMBER_PREFIX_CAR;

/**
 * Importer für sprachabhängige Verkaufsbezeichnungen über ProVal Webservice
 */
public class iPartsProValImporter extends AbstractDataImporter {

    private final static String MBAG_COMPANY_TYPE = "0";
    ImportExportLogHelper logHelper;

    Map<iPartsModelId, EtkMultiSprache> salesTitles = new HashMap<>();

    public iPartsProValImporter(EtkProject project, ImportExportLogHelper logHelper) {
        super(project, "!!ProVal sprachabhängige Verkaufsbezeichnungen");
        this.logHelper = logHelper;
    }

    /**
     * Sammelt alle sprachabhängigen Verkaufsbezeichnungen auf
     *
     * @param lang
     * @param iPartsProValModelsServiceResponseObjects
     */
    public void prepareImport(Language lang, List<iPartsProValModelsServiceResponseObject> iPartsProValModelsServiceResponseObjects) {
        iPartsProValModelsServiceResponseObjects.stream()
                // Mercedes-Benz AG --> 0
                .filter(modelData -> (modelData.getVehicleDesignNumber() != null) && (modelData.getCompany() != null)
                                     && Utils.objectEquals(modelData.getCompany().getId(), MBAG_COMPANY_TYPE))
                .forEach(modelData -> {
                    iPartsModelId modelId = new iPartsModelId(MODEL_NUMBER_PREFIX_CAR + modelData.getVehicleDesignNumber().getShortName());
                    if (modelId.isModelNumberValid(true)) {
                        EtkMultiSprache etkMultiSprache = salesTitles.computeIfAbsent(modelId, id -> new EtkMultiSprache());
                        etkMultiSprache.setText(lang, modelData.getName());
                    }
                });
    }

    /**
     * Persistiert die Änderungen in der DB.
     */
    public void doImport() {
        // Prüfen, ob sprachabhängige Verkaufsbezeichnungen vorliegen, wenn ja - importieren
        if (!salesTitles.isEmpty()) {
            int possibleDataUpdates = salesTitles.size();
            AtomicInteger acutalDataUpdates = new AtomicInteger();

            Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.DEBUG, "Importing ProVal sales titles for " + possibleDataUpdates
                                                                       + " MBAG models...");
            logHelper.getMessageLog().fireMessage(translateForLog("!!Importiere sprachabhängige Verkaufsbezeichnungen für %1 MBAG-Baumuster...",
                                                                  String.valueOf(possibleDataUpdates)));
            logHelper.addNewLine();
            EtkDbs etkDbs = getProject().getEtkDbs();
            etkDbs.startTransaction();
            etkDbs.startBatchStatement();
            try {
                salesTitles.forEach((modelId, etkMultiSprache) -> {
                    iPartsDataModel dataModel = new iPartsDataModel(getProject(), modelId);
                    // Daten nur ergänzen, wenn ein Datensatz zum Baumuster existiert
                    if (dataModel.existsInDB()) {
                        // Bestehende Sprachen beibehalten, sofern kein neuerer Datensatz vorhanden ist (merge)
                        EtkMultiSprache existingEtkMultiSprache = dataModel.getFieldValueAsMultiLanguage(FIELD_DM_SALES_TITLE);
                        if ((existingEtkMultiSprache != null) && !existingEtkMultiSprache.isEmpty()) {
                            // Merge der beiden Maps - bei Key-Konflikten immer das neuere übernehmen
                            Map<String, String> mergedLanguages = Stream.of(existingEtkMultiSprache.getLanguagesAndTexts(),
                                                                            etkMultiSprache.getLanguagesAndTexts())
                                    .flatMap(map -> map.entrySet().stream())
                                    .collect(Collectors.toMap(
                                            Map.Entry::getKey,
                                            Map.Entry::getValue,
                                            (v1, v2) -> v2));
                            etkMultiSprache.setLanguagesAndTexts(mergedLanguages);
                        }
                        dataModel.setFieldValueAsMultiLanguage(FIELD_DM_SALES_TITLE, etkMultiSprache, DBActionOrigin.FROM_EDIT);
                        if (dataModel.isModified()) {
                            dataModel.saveToDB();
                            acutalDataUpdates.getAndIncrement();
                        }
                    } else {
                        logHelper.getMessageLog().fireMessage(translateForLog("!!Baumuster %1 existiert nicht in der DB und wird übersprungen.",
                                                                              modelId.getModelNumber()));
                    }
                });

                etkDbs.endBatchStatement();
                etkDbs.commit();
            } catch (Exception e) {
                etkDbs.cancelBatchStatement();
                etkDbs.rollback();
                throw e;
            }

            Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.INFO, "Number of updated MBAG models with new ProVal sales titles: "
                                                                      + acutalDataUpdates.get());
            logHelper.addNewLine();
            logHelper.getMessageLog().fireMessage(translateForLog("!!Tatsächlich aktualisierte MBAG-Baumuster mit neuen ProVal Verkaufsbezeichnungen: %1",
                                                                  String.valueOf(acutalDataUpdates)));
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        return false;
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return false;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return false;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
    }

    public Map<iPartsModelId, EtkMultiSprache> getSalesTitles() {
        return salesTitles;
    }

    public void setSalesTitles(Map<iPartsModelId, EtkMultiSprache> salesTitles) {
        this.salesTitles = salesTitles;
    }
}
