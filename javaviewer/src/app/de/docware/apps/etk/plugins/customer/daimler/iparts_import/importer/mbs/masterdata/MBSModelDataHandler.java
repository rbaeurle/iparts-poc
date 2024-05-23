package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.masterdata;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.AbstractMBSDataHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler für das Verarbeiten von SAP-MBS Baumuster-Stammdaten
 */
public class MBSModelDataHandler extends AbstractMBSDataHandler {

    private static final String TRIGGER_ELEMENT = "ModelMasterData";

    private Map<iPartsModelId, iPartsDataModel> dataModelsToBeSavedMap;

    public MBSModelDataHandler(EtkProject project, MBSDataImporter importer) {
        super(project, TRIGGER_ELEMENT, importer, "!!SAP-MBS Baumuster-Stammdaten", TABLE_DA_MODEL);
    }

    @Override
    protected void onStartDocument() {
        super.onStartDocument();
        dataModelsToBeSavedMap = new HashMap<>();
    }

    @Override
    protected void onEndDocument() {
        super.onEndDocument();
        for (iPartsDataModel dataModel : dataModelsToBeSavedMap.values()) {
            if ((dataModel != null) && dataModel.isModifiedWithChildren()) {
                saveDataObject(dataModel);
            }
        }
        dataModelsToBeSavedMap.clear();
    }

    @Override
    protected void initMapping(Map<String, String> mapping) {
        mapping.put(FIELD_DM_MODEL_SUFFIX, MODEL_NUMBER_SUFFIX);
        mapping.put(FIELD_DM_NAME, DESCRIPTION);
    }

    @Override
    protected void handleCurrentRecord() {
        // Check, ob es sich um eine INSERT oder UPDATE Operation handelt
        if (!isValidAction(getCompleteModelNumber())) {
            return;
        }

        String modelNumber = getCurrentRecord().get(MODEL_NUMBER);
        String modelNumberSuffix = getCurrentRecord().get(MODEL_NUMBER_SUFFIX);

        // DAIMLER-10821: Baumuster mit Suffix überspringen
        if (StrUtils.isValid(modelNumberSuffix)) {
            writeMessage(TranslationHandler.translate("!!Baumuster \"%1\" mit Suffix \"%2\" wird übersprungen!",
                                                      modelNumber, modelNumberSuffix), MessageLogType.tmlMessage,
                         MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return;
        }

        iPartsDataModel modelData = getModelDataFromDB(modelNumber);
        if (modelData == null) {
            return;
        }

        // Check, ob der Datensatz schon mit einer anderen Quelle angelegt wurde
        if (checkIfModelExistsWithDifferentSource(modelData)) {
            return;
        }

        // Check, ob der neue Datensatz jünger ist, als der bisherige
        if (!isValidDateTime(modelData)) {
            return;
        }

        fillModelData(modelData);
        // Speichern der Daten erfolgt in onEndDocument()
    }

    /**
     * Befüllt das übergebene {@link iPartsDataModel} Objekt
     *
     * @param modelData
     */
    private void fillModelData(iPartsDataModel modelData) {
        // Quelle setzen
        modelData.setFieldValue(FIELD_DM_SOURCE, iPartsImportDataOrigin.SAP_MBS.getOrigin(), DBActionOrigin.FROM_EDIT);
        // Objekt befüllen
        getImportHelper().fillOverrideCompleteDataForSAPMBS(modelData, getCurrentRecord());
        modelData.setFieldValue(FIELD_DM_DATA, getReleaseDateFrom(), DBActionOrigin.FROM_EDIT);
        modelData.setFieldValue(FIELD_DM_DATB, getReleaseDateTo(), DBActionOrigin.FROM_EDIT);
    }

    /**
     * Erzeugt aus den Importdaten die Baumusternummer mit optionalem Suffix
     *
     * @return
     */
    private String getCompleteModelNumber() {
        String modelNumber = getCurrentRecord().get(MODEL_NUMBER);
        String modelNumberSuffix = getCurrentRecord().get(MODEL_NUMBER_SUFFIX);
        if (StrUtils.isValid(modelNumberSuffix)) {
            modelNumber += modelNumberSuffix;
        }
        return modelNumber;
    }

    /**
     * Überprüft, ob das neue Freigabedatum jünger ist als das bisherige
     *
     * @param modelData
     * @return
     */
    private boolean isValidDateTime(iPartsDataModel modelData) {
        String newDateTo = getReleaseDateTo();
        String currentDateTo = modelData.getFieldValue(FIELD_DM_DATB);
        // Ist das neue Datum leer, weitermachen.
        // Falls nicht und das bestehende Datum nicht leer ist: Prüfe, ob das neue Datum jünger (gleichalt) ist als das alte
        if (StrUtils.isEmpty(newDateTo) || (StrUtils.isValid(currentDateTo) && (newDateTo.compareTo(currentDateTo) >= 0))) {
            return true;
        }
        writeMessage(TranslationHandler.translate("!!Zum Baumuster \"%1\" existiert ein Freigabedatum, das neuer" +
                                                  " als das zu importierende Freigabedatum ist. Aktuelles Datum: \"%2\", " +
                                                  "Datum des Importdatensatzes: \"%3\". Datensatz wird übersprungen!",
                                                  modelData.getAsId().getModelNumber(), currentDateTo, newDateTo), MessageLogType.tmlMessage,
                     MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        return false;

    }

    /**
     * Überprüft, ob der übergebene Datensatz schon von einer anderen Quelle erzeugt wurde
     *
     * @param modelData
     * @return
     */
    private boolean checkIfModelExistsWithDifferentSource(iPartsDataModel modelData) {
        if (!modelData.isNew()) {
            iPartsImportDataOrigin currentOrigin = iPartsImportDataOrigin.getTypeFromCode(modelData.getFieldValue(FIELD_DM_SOURCE));
            if ((currentOrigin != iPartsImportDataOrigin.UNKNOWN) && (currentOrigin != iPartsImportDataOrigin.SAP_MBS)) {
                writeMessage(TranslationHandler.translate("!!Baumuster \"%1\" existiert schon in der Datenbank und " +
                                                          "hat die Quelle \"%2\". Datensatz wird übersprungen!",
                                                          modelData.getAsId().getModelNumber(), currentOrigin.getOrigin()), MessageLogType.tmlMessage,
                             MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                return true;
            }
        }
        return false;
    }

    /**
     * Liefert das Datenbak-Objekt zur übergebenen Baumusternummer. Existiert das baumuster noch nicht, wird ein neue
     * {@link iPartsDataModel} Objekt angelegt.
     *
     * @param modelNumber
     * @return
     */
    private iPartsDataModel getModelDataFromDB(String modelNumber) {
        iPartsModelId modelId = new iPartsModelId(modelNumber);
        return dataModelsToBeSavedMap.computeIfAbsent(modelId, modelIdFromDBObject -> {
            if (modelIdFromDBObject.isAggregateModel()) {
                // Prüfen, ob es sich um ein vermeintliches D-Baumuster handelt, was eigentlich ein C-Baumuster ist
                iPartsModelId vehicleModelId = new iPartsModelId(iPartsConst.MODEL_NUMBER_PREFIX_CAR + modelIdFromDBObject.getModelNumber().substring(1));
                iPartsDataModel vehicleDataModel = dataModelsToBeSavedMap.get(vehicleModelId);
                if (vehicleDataModel != null) {
                    // In der Import-Datei gibt es ein passendes C-Baumuster -> vermeintliches D-Baumuster ignorieren
                    return null;
                }

                vehicleDataModel = new iPartsDataModel(getProject(), vehicleModelId);
                if (vehicleDataModel.existsInDB()) {
                    // In der DB gibt es ein passendes C-Baumuster -> dieses C-Baumuster für das vermeintliche D-Baumuster verwenden
                    return vehicleDataModel;
                }
            } else {
                // In der Import-Datei gibt es ein passendes D-Baumuster -> vermeintliches D-Baumuster entfernen
                iPartsModelId aggregateModelId = new iPartsModelId(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE + modelIdFromDBObject.getModelNumber().substring(1));
                dataModelsToBeSavedMap.remove(aggregateModelId);
            }

            iPartsDataModel result = new iPartsDataModel(getProject(), modelIdFromDBObject);
            if (!result.existsInDB()) {
                result.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
            }
            return result;
        });
    }
}
