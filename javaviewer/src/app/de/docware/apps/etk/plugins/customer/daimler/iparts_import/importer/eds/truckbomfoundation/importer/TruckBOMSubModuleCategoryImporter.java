/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSubModuleCategory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSubModuleCategoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getsubmodulecategory.TruckBOMSingleBillOfMaterialScope;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getsubmodulecategory.TruckBOMSingleFunctionModule;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getsubmodulecategory.TruckBOMSubModuleCategoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getsubmodulecategory.TruckBOMSubModuleDataVersion;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update.EDSModelScopeUpdateImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.JSONUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;

import static de.docware.util.sql.TableAndFieldName.getFieldName;

/**
 * Importer für die OPS-Umfang- bzw. Sub-Modul-Daten aus TruckBOM.foundation
 */
public class TruckBOMSubModuleCategoryImporter extends AbstractTruckBOMFoundationJSONImporter {

    private boolean useNewStructure; // Sollen die Daten als neue Struktur importiert werden

    public TruckBOMSubModuleCategoryImporter(EtkProject project, boolean isNewStructureActive) {
        super(project, isNewStructureActive ? TRUCK_BOM_FOUNDATION_SUB_MODULE_CATEGORY_IMPORT_NAME : TRUCK_BOM_FOUNDATION_OPS_SCOPE_IMPORT_NAME,
              isNewStructureActive ? TABLE_DA_SUB_MODULE_CATEGORY : TABLE_DA_OPS_SCOPE);
        useNewStructure = isNewStructureActive;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected Map<String, AbstractBOMXMLDataImporter> createSubImporter() {
        Map<String, AbstractBOMXMLDataImporter> importer = new HashMap<>();
        importer.put(TABLE_DA_OPS_SCOPE, new EDSModelScopeUpdateImporter(getProject()));
        return importer;
    }

    @Override
    protected boolean importJSONResponse(String response) {
        String fileName = getImportName(getProject().getDBLanguage());
        Genson genson = JSONUtils.createGenson(true);
        try {
            TruckBOMSubModuleCategoryData truckBOMSubModuleCategoryData = deserializeFromString(genson, response, fileName, TruckBOMSubModuleCategoryData.class);
            if (truckBOMSubModuleCategoryData == null) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine gültigen Werte.", fileName);
                return false;
            }
            if (!checkObjectCount(truckBOMSubModuleCategoryData)) {
                return true;
            }
            // Alle OPS Umfang Knoten holen
            List<TruckBOMSingleBillOfMaterialScope> singleBillOfMaterialScopes = truckBOMSubModuleCategoryData.getBillOfMaterialScope();
            List<TruckBOMSingleFunctionModule> singleFunctionModules = truckBOMSubModuleCategoryData.getFunctionModule();
            if (((singleBillOfMaterialScopes == null) || singleBillOfMaterialScopes.isEmpty()) || ((singleFunctionModules == null) || singleFunctionModules.isEmpty())) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine OPS-Umfang-Daten.", fileName);
                return false;
            }

            if (useNewStructure) {
                importAsNewStructure(singleBillOfMaterialScopes, singleFunctionModules);
            } else {
                // SubImporter starten
                SubModuleCategoryKeyValueHelper subModuleCategoryKeyValueHelper = new SubModuleCategoryKeyValueHelper(getSavedJSONFile(),
                                                                                                                      singleBillOfMaterialScopes,
                                                                                                                      singleFunctionModules,
                                                                                                                      getSubImporters().get(TABLE_DA_OPS_SCOPE));


                if (!startSubImporter(TABLE_DA_OPS_SCOPE, subModuleCategoryKeyValueHelper)) {
                    logSkipImport(TABLE_DA_OPS_SCOPE);
                }
            }


        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            fireErrorLF("!!Fehler beim Importieren der OPS-Umfang-Daten aus TruckBOM.foundation");
            return false;
        }
        return true;
    }

    /**
     * Importiert die Daten als Modul Stammdaten für die neue Struktur in der EDS/BCS Konstruktion
     *
     * @param singleBillOfMaterialScopes
     * @param functionModules
     */
    private void importAsNewStructure(List<TruckBOMSingleBillOfMaterialScope> singleBillOfMaterialScopes, List<TruckBOMSingleFunctionModule> functionModules) {
        VarParam<Integer> importRecordCount = new VarParam<>(0);
        // Erst die alten "Umfang" Daten durchlaufen
        if (singleBillOfMaterialScopes != null) {
            for (TruckBOMSingleBillOfMaterialScope singleBillOfMaterialScope : singleBillOfMaterialScopes) {
                // Sub-Modul aus Umfang bestimmen
                String scopeId = singleBillOfMaterialScope.getIdentifier();
                if (StrUtils.isValid(scopeId)) {
                    // Die neueste Version bestimmen (Änderungsstand ab)
                    Optional<TruckBOMSubModuleDataVersion> newestSubModuleCategoryVersion = singleBillOfMaterialScope.getNewestBillOfMaterialScopeVersion();
                    newestSubModuleCategoryVersion.ifPresent(singleBillOfMaterialScopeDataFromJSON -> {
                        if (handleNewStructureData(singleBillOfMaterialScopeDataFromJSON, scopeId)) {
                            importRecordCount.setValue(importRecordCount.getValue() + 1);
                        }
                    });
                }
            }
        }
        if (functionModules != null) {
            // Danach die neuen Submodul-Daten
            for (TruckBOMSingleFunctionModule functionModule : functionModules) {
                // Sub-Modul aus neuer Struktur bestimmen
                String functionModuleId = functionModule.getIdentifier();
                if (StrUtils.isValid(functionModuleId)) {
                    // Die neueste Version bestimmen (Änderungsstand ab)
                    Optional<TruckBOMSubModuleDataVersion> newestSubModuleCategoryVersion = functionModule.getNewestSubModuleCategoryVersion();
                    newestSubModuleCategoryVersion.ifPresent(functionModuleDataFromJSON -> {
                        if (handleNewStructureData(functionModuleDataFromJSON, functionModuleId)) {
                            importRecordCount.setValue(importRecordCount.getValue() + 1);
                        }
                    });
                }
            }
        }
        logImportRecordsFinished(importRecordCount.getValue());
    }

    private boolean handleNewStructureData(TruckBOMSubModuleDataVersion subModuleDataFromJSON, String valueFromJSON) {
        // Prüfen, ob das Sub-Modul schon in der DB existiert
        iPartsDataSubModuleCategory subModuleCategory = new iPartsDataSubModuleCategory(getProject(), new iPartsSubModuleCategoryId(valueFromJSON));
        if (!subModuleCategory.existsInDB()) {
            subModuleCategory.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        }
        // Benennung setzen
        EtkMultiSprache description = subModuleDataFromJSON.getNomenclatureAsMultiLangObject();
        subModuleCategory.setFieldValueAsMultiLanguage(getFieldName(FIELD_DSMC_DESC), description, DBActionOrigin.FROM_EDIT);
        if (saveToDB(subModuleCategory)) {
            return true;
        }
        return false;
    }

    /**
     * Konvertiert die Daten vom {@link TruckBOMSubModuleDataVersion} Objekt zu einem <code>importRec</code>
     *
     * @param scopeId
     * @param subModuleCategoryVersion
     * @return
     */
    private RecordData convertSubModuleCategoryToImportRecord(String scopeId, TruckBOMSubModuleDataVersion subModuleCategoryVersion) {
        RecordData recordData = new RecordData();
        // Umfang setzen
        addValueIfExists(EDSModelScopeUpdateImporter.UMF_SCOPE_NUMBER, scopeId, recordData);
        // Texte setzen (Description)
        addTexts(recordData, subModuleCategoryVersion);
        return recordData;

    }

    /**
     * Helper um die OPS Umfang TB.f Daten in BOM-DB XML Daten zu konvertieren
     */
    private class SubModuleCategoryKeyValueHelper extends AbstractTruckBOMKeyValueJSONReader {

        private final AbstractBOMXMLDataImporter importer;
        private final List<TruckBOMSingleBillOfMaterialScope> singleBillOfMaterialScopes;
        private final List<TruckBOMSingleFunctionModule> singleFunctionModules;

        public SubModuleCategoryKeyValueHelper(DWFile savedJSONFile, List<TruckBOMSingleBillOfMaterialScope> singleBillOfMaterialScopes,
                                               List<TruckBOMSingleFunctionModule> singleFunctionModules, AbstractBOMXMLDataImporter importer) {
            super(savedJSONFile, getImportRecordCount(singleBillOfMaterialScopes, singleFunctionModules), TABLE_DA_OPS_SCOPE);
            this.importer = importer;
            this.singleBillOfMaterialScopes = singleBillOfMaterialScopes;
            this.singleFunctionModules = singleFunctionModules;
        }

        @Override
        protected List<RecordData> getNextRecordData() {
            if (!isCancelled()) {
                if ((singleBillOfMaterialScopes != null) && !singleBillOfMaterialScopes.isEmpty() && (getImportRecCount() < singleBillOfMaterialScopes.size())) {
                    TruckBOMSingleBillOfMaterialScope singleBillOfMaterialScope = singleBillOfMaterialScopes.get(getImportRecCount());
                    String scopeId = singleBillOfMaterialScope.getIdentifier();
                    // Falls kein Umfang existiert, brauchen wir nicht importieren
                    if (StrUtils.isEmpty(scopeId)) {
                        return null;
                    }
                    // Nur den neuesten Datensatz verarbeiten, da in DA_OPS_SCOPE keine unterschiedlichen Stände berücksichtigt werden
                    Optional<TruckBOMSubModuleDataVersion> newestSubModuleCategoryVersion = singleBillOfMaterialScope.getNewestBillOfMaterialScopeVersion();
                    if (newestSubModuleCategoryVersion.isPresent()) {
                        return createOldStructureData(newestSubModuleCategoryVersion.get(), scopeId);
                    }
                } else if ((singleFunctionModules != null) && !singleFunctionModules.isEmpty()) {
                    int temImportRecordCount = getImportRecCount() - ((singleBillOfMaterialScopes != null) ? singleBillOfMaterialScopes.size() : 0);
                    if (temImportRecordCount < singleFunctionModules.size()) {
                        TruckBOMSingleFunctionModule singleFunctionModule = singleFunctionModules.get(temImportRecordCount);
                        String functionModuleId = singleFunctionModule.getIdentifier();
                        // Falls kein Umfang existiert, brauchen wir nicht importieren
                        if (StrUtils.isEmpty(functionModuleId)) {
                            return null;
                        }
                        // Nur den neuesten Datensatz verarbeiten, da in DA_OPS_SCOPE keine unterschiedlichen Stände berücksichtigt werden
                        Optional<TruckBOMSubModuleDataVersion> newestSubModuleCategoryVersion = singleFunctionModule.getNewestSubModuleCategoryVersion();
                        if (newestSubModuleCategoryVersion.isPresent()) {
                            return createOldStructureData(newestSubModuleCategoryVersion.get(), functionModuleId);

                        }
                    }
                }
            }
            return null;
        }

        private List<RecordData> createOldStructureData(TruckBOMSubModuleDataVersion subModuleCategoryVersion,
                                                        String idFromJSON) {
            RecordData recordData = convertSubModuleCategoryToImportRecord(idFromJSON, subModuleCategoryVersion);
            // Der Text soll nur verarbeitet werden, wenn er auch existiert. Durch EDS_UNKNOWN importiert der
            // Sub-Importer den Text nicht und überspringt den Datensatz. Hier wird dem Sub-Importer vorgespielt,
            // dass die Sprache DE ist und wir importieren können. Die echten Texte werden im Nachgang gesetzt.
            addLangDataMetaInfo(recordData, EDSModelScopeUpdateImporter.UMF_SCOPE_LANG_DATA, EDSModelScopeUpdateImporter.UMF_LANG, iPartsEDSLanguageDefs.EDS_DE);
            List<RecordData> result = new ArrayList<>();
            result.add(recordData);
            return result;
        }

        @Override
        protected void postProcessRecordData(RecordData record) {
            Map<String, EtkMultiSprache> textsForRecord = new HashMap<>();
            addTextObjectIfNotNull(EDSModelScopeUpdateImporter.UMF_DESCRIPTION, record.getDescription(), textsForRecord);
            getImporter().setTextsForMultiLangFields(textsForRecord);
        }

        @Override
        protected String getOriginalTableName() {
            return EDSModelScopeUpdateImporter.IMPORT_TABLENAME;
        }

        public AbstractBOMXMLDataImporter getImporter() {
            return importer;
        }

    }

    private static int getImportRecordCount(List<TruckBOMSingleBillOfMaterialScope> singleBillOfMaterialScopes,
                                            List<TruckBOMSingleFunctionModule> singleFunctionModules) {
        int result = 0;
        if ((singleBillOfMaterialScopes != null) && !singleBillOfMaterialScopes.isEmpty()) {
            result += singleBillOfMaterialScopes.size();
        }
        if ((singleFunctionModules != null) && !singleFunctionModules.isEmpty()) {
            result += singleFunctionModules.size();
        }
        return result;
    }
}
