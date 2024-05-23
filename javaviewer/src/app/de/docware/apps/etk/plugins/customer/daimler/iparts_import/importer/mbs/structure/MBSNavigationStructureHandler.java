/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.structure;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsMBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDistributionHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.helper.MBSWbSaaCalculationHelper;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handler für die Navigationsstruktur von SAP-MBS
 */
public class MBSNavigationStructureHandler extends AbstractMBSStructureHandler {

    private static final String INVALID_CTT_NUMBER_PREFIX = "UHU";
    private static final Set<String> STRUCTURE_SNR_ELEMENTS = new HashSet<>();

    // Mögliche XML Elemente in denen die obere Sachnummer stehen könnte
    static {
        STRUCTURE_SNR_ELEMENTS.add(MODEL_NUMBER);
        STRUCTURE_SNR_ELEMENTS.add(BASE_LIST_NUMBER);
        STRUCTURE_SNR_ELEMENTS.add(PARTS_LIST_NUMBER);
        STRUCTURE_SNR_ELEMENTS.add(CTT_LIST_NUMBER);
    }

    private Set<IdWithType> usedSaaModelsIds;
    private Set<IdWithType> usedModelsAggsIds;
    private Map<String, String> retailModelNumberMap = new HashMap<>();
    private MBSWbSaaCalculationHelper wbCalcHelper;


    public MBSNavigationStructureHandler(EtkProject project, MBSDataImporter importer, MBSDistributionHandler tagHandler) {
        super(project, importer, "!!SAP-MBS Navigationsstruktur", TABLE_DA_STRUCTURE_MBS, tagHandler);
        usedSaaModelsIds = new HashSet<>();
        usedModelsAggsIds = new HashSet<>();
    }

    @Override
    protected void initMapping(Map<String, String> mapping) {
        mapping.put(FIELD_DSM_KEM_TO, ECO_TO);
        mapping.put(FIELD_DSM_SUB_SNR_SUFFIX, ITEM_SUFFIX);
        mapping.put(FIELD_DSM_CODE, CODE);
        mapping.put(FIELD_DSM_SNR_TEXT, SNR_TEXT);
        mapping.put(FIELD_DSM_CTT_QUANTITY_FLAG, CTT_QUANTITY_FLAG);
    }

    @Override
    protected String getQuantityFieldname() {
        return FIELD_DSM_QUANTITY;
    }

    @Override
    protected String getItemFieldname() {
        return FIELD_DSM_SUB_SNR;
    }

    @Override
    protected String getReleaseDateFromFieldname() {
        return FIELD_DSM_RELEASE_FROM;
    }

    @Override
    protected String getReleaseDateToFieldname() {
        return FIELD_DSM_RELEASE_TO;
    }

    @Override
    protected EtkDataObject getSpecificDataObject(String snrValue, String position, String sortValue, String kemFrom) {
        if (snrValue.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE)) {
            snrValue = getRetailModelNumber(snrValue);
        }

        // Falls obere und untere Sachummer jeweils ein Fahrzeug- oder Aggregate-Baumuster darstellen und die Baumusternummern
        // abgesehen vom Sachnummernkennbuchstaben identisch sind, dann den Suffix VEHICLE_AGGREGATE_MAPPING_SUFFIX an die
        // obere Sachnummer hinzufügen, um Primärschlüsselkonflikte zu vermeiden durch die Korrektur von vermeintlichen
        // D-Baumustern zu C-Baumustern
        if (isPseudoVehicleAggregateMapping(snrValue, getItemValue())) {
            snrValue += MBS_VEHICLE_AGGREGATE_MAPPING_SUFFIX;
        }

        // Suffix kommt eigentlich nur bei Baumuster vor
        String snrSuffix = getCurrentRecord().getOrDefault(MODEL_NUMBER_SUFFIX, "");
        iPartsMBSStructureId mbsStructureId = new iPartsMBSStructureId(snrValue, snrSuffix, position, sortValue, kemFrom);
        return new iPartsDataMBSStructure(getProject(), mbsStructureId);
    }

    private boolean isPseudoVehicleAggregateMapping(String snrValue, String subSnrValue) {
        return (snrValue.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_CAR) || snrValue.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE))
               && (subSnrValue.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_CAR) || subSnrValue.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE))
               && snrValue.substring(1).equals(subSnrValue.substring(1));
    }

    @Override
    protected Set<String> getStructureElements() {
        return STRUCTURE_SNR_ELEMENTS;
    }

    @Override
    public void handleCurrentRecord() {
        super.handleCurrentRecord();
        String model = getValueFromCurrentRecord(MODEL_NUMBER);
        String subSNR = getItemValue();

        // Falls obere und untere Sachummer jeweils ein Fahrzeug- oder Aggregate-Baumuster darstellen und die Baumusternummern
        // abgesehen vom Sachnummernkennbuchstaben identisch sind, dann diesen Datensatz für die folgenden Spezial-Importe
        // ignorieren
        if (StrUtils.isValid(model, subSNR) && !subSNR.startsWith(BASE_LIST_NUMBER_PREFIX) && !isPseudoVehicleAggregateMapping(model, subSNR)) {
            if (model.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE)) {
                model = getRetailModelNumber(model);
            }
            if (!getImportHelper().addSaaModelsData(getImporter(), model, subSNR, usedSaaModelsIds)) {
                checkIdSet(usedSaaModelsIds);
            }
            if (!getImportHelper().addVehicleModelToAggsModelsData(getImporter(), model, subSNR, usedModelsAggsIds)) {
                checkIdSet(usedModelsAggsIds);
            }
        }
    }


    @Override
    public void onPreImportTask() {
        super.onPreImportTask();
        wbCalcHelper = new MBSWbSaaCalculationHelper(getProject(), getImporter());
    }

    @Override
    public void onPostImportTask() {
        super.onPostImportTask();
        wbCalcHelper.handleSaaAVEntries();
        wbCalcHelper = null;
    }

    @Override
    protected void saveDataObject(EtkDataObject dataObject) {
        wbCalcHelper.addToMap(!dataObject.isNew(), dataObject);
        if (dataObject.isModifiedWithChildren()) {
            super.saveDataObject(dataObject);
        }
    }


    private String getRetailModelNumber(String constructionModelNumber) {
        return retailModelNumberMap.computeIfAbsent(constructionModelNumber, modelNumber -> {
            iPartsModelId modelId = new iPartsModelId(modelNumber);
            if (modelId.isAggregateModel()) {
                modelId = new iPartsModelId(iPartsConst.MODEL_NUMBER_PREFIX_CAR + modelNumber.substring(1));
                iPartsModel model = iPartsModel.getInstance(getProject(), modelId);
                if (model.existsInDB()) {
                    return modelId.getModelNumber(); // C-Baumuster existiert -> vermeintliches D-Baumuster ist ein C-Baumuster
                }
            }

            return modelNumber; // Ist schon ein C-Baumuster oder ein echtes D-Baumuster
        });
    }

    @Override
    protected String getStructureSNRValue(String currentNoXMLElement) {
        if (StrUtils.isValid(currentNoXMLElement)) {
            String snrNumber = getCurrentRecord().get(currentNoXMLElement);
            if (!currentNoXMLElement.equals(CTT_LIST_NUMBER)) {
                return getImportHelper().getRetailSAA(snrNumber);
            }
            // UHU Nummern und obere W-Sachnummern mit KG sollen nicht importiert werden. Wenn eine obere Sachnummer
            // eine KG hat, dann handelt es sich um einen Datensatz aus der DA_PARTSLIST_MBS Tabelle.
            if (!snrNumber.contains(MBS_CON_GROUP_DELIMITER) && !snrNumber.startsWith(INVALID_CTT_NUMBER_PREFIX)) {
                return getImportHelper().addSaaPrefixIfNeeded(snrNumber, true);
            }
        }
        return null;
    }

    private void checkIdSet(Set<IdWithType> usedIdSet) {
        if (usedIdSet.size() >= MAX_DB_OBJECTS_CACHE_SIZE) {
            usedIdSet.clear();
        }
    }
}
