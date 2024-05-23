/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataModuleCategory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataOPSGroup;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsModuleCategoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsOPSGroupId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodulecategory.TruckBOMModuleCategoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodulecategory.TruckBOMModuleDataVersion;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodulecategory.TruckBOMSingleGroup;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodulecategory.TruckBOMSingleModule;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.JSONUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Importer für die OPS Gruppen- bzw. Modul-Struktur-Daten aus TruckBOM.foundation
 */
public class TruckBOMModuleCategoryImporter extends AbstractTruckBOMFoundationJSONImporter {

    // Mapping Felde von OPS_GROUP auf MODULE_CATEGORY
    private static final Map<String, String> OLD_STRUCTURE_MAPPING = new HashMap<>();

    static {
        OLD_STRUCTURE_MAPPING.put(FIELD_DOG_GROUP, FIELD_DMC_MODULE);
        OLD_STRUCTURE_MAPPING.put(FIELD_DOG_AS_FROM, FIELD_DMC_AS_FROM);
        OLD_STRUCTURE_MAPPING.put(FIELD_DOG_DESC, FIELD_DMC_DESC);
        OLD_STRUCTURE_MAPPING.put(FIELD_DOG_PICTURE, FIELD_DMC_PICTURE);
    }

    private boolean useNewStructure; // Sollen die Daten als neue Struktur importiert werden

    public TruckBOMModuleCategoryImporter(EtkProject project, boolean isNewStructureActive) {
        super(project, isNewStructureActive ? TRUCK_BOM_FOUNDATION_MODULE_CATEGORY_IMPORT_NAME : TRUCK_BOM_FOUNDATION_OPS_GROUP_IMPORT_NAME,
              isNewStructureActive ? TABLE_DA_MODULE_CATEGORY : TABLE_DA_OPS_GROUP);
        useNewStructure = isNewStructureActive;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected Map<String, AbstractBOMXMLDataImporter> createSubImporter() {
        return new HashMap<>();
    }

    @Override
    protected boolean importJSONResponse(String response) {
        String fileName = getImportName(getProject().getDBLanguage());
        Genson genson = JSONUtils.createGenson(true);
        try {
            TruckBOMModuleCategoryData truckBOMModuleCategoryData = deserializeFromString(genson, response, fileName, TruckBOMModuleCategoryData.class);
            if (truckBOMModuleCategoryData == null) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine gültigen Werte.", fileName);
                return false;
            }
            if (!checkObjectCount(truckBOMModuleCategoryData)) {
                return true;
            }

            // Alle Modul Daten holen
            List<TruckBOMSingleModule> singleModules = truckBOMModuleCategoryData.getNavigationModule();
            List<TruckBOMSingleGroup> singleGroups = truckBOMModuleCategoryData.getGroup();
            if (((singleModules == null) || (singleModules.isEmpty())) && ((singleGroups == null) || singleGroups.isEmpty())) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine Daten für Module.", fileName);
                return false;
            }
            VarParam<Integer> importRecordCount = new VarParam<>(0);
            // Erst die "alten" Gruppendaten durchgehen
            if (singleGroups != null) {
                for (TruckBOMSingleGroup singleGroup : singleGroups) {
                    // Gruppe bestimmen
                    String valueFromJSON = singleGroup.getIdentifier();
                    if (StrUtils.isValid(valueFromJSON)) {
                        // Die neueste Version bestimmen (Änderungsstand ab)
                        Optional<TruckBOMModuleDataVersion> newestModuleCategoryVersion = singleGroup.getNewestGroupVersion();
                        newestModuleCategoryVersion.ifPresent(groupDataFromJSON -> {
                            if (handleDataSet(groupDataFromJSON, valueFromJSON)) {
                                importRecordCount.setValue(importRecordCount.getValue() + 1);
                            }
                        });
                    }
                }
            }
            // Danach die neuen Moduldaten durchgehen
            if (singleModules != null) {
                for (TruckBOMSingleModule singleModule : singleModules) {
                    // Modul bestimmen
                    String valueFromJSON = singleModule.getIdentifier();
                    if (StrUtils.isValid(valueFromJSON)) {
                        // Die neueste Version bestimmen (Änderungsstand ab)
                        Optional<TruckBOMModuleDataVersion> newestModuleCategoryVersion = singleModule.getNewestModuleVersion();
                        newestModuleCategoryVersion.ifPresent(moduleDataFromJSON -> {
                            if (handleDataSet(moduleDataFromJSON, valueFromJSON)) {
                                importRecordCount.setValue(importRecordCount.getValue() + 1);
                            }
                        });
                    }
                }
            }

            logImportRecordsFinished(importRecordCount.getValue());
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            fireErrorLF("!!Fehler beim Importieren der Modul Informationen aus TruckBOM.foundation");
            return false;
        }
        return true;
    }

    private boolean handleDataSet(TruckBOMModuleDataVersion groupDataFromJSON, String valueFromJSON) {
        // Prüfen, ob das Modul schon in der DB existiert
        EtkDataObject dataObjectForDB = createDataObject(valueFromJSON);
        String versionFromJSON;
        if (useNewStructure) {
            versionFromJSON = groupDataFromJSON.getVersion();
        } else {
            versionFromJSON = StrUtils.leftFill(groupDataFromJSON.getVersion(), 3, '0');
        }
        if (dataObjectForDB.existsInDB()) {
            // Die Gruppe existiert bereits -> Prüfen, ob der Änderungsstand in der DB neuer ist als der
            // in der JSON Antwort
            String versionFromDB = dataObjectForDB.getFieldValue(getFieldName(FIELD_DOG_AS_FROM));
            if (StrUtils.isValid(versionFromDB) && (Utils.toSortString(versionFromDB).compareTo(Utils.toSortString(versionFromJSON)) > 0)) {
                // DB ist neuer -> Gruppe überspringen
                fireMessageLF("!!Zum Modul %1 existiert in der DB ein neuerer Datensatz!" +
                              " Revision in DB \"%2\", Revision vom Webservice \"%3\"", valueFromJSON,
                              versionFromDB, versionFromJSON);
                return false;
            }
        } else {
            dataObjectForDB.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
            // TB.f liefert nur freigegebene Daten. Außerdem kann man in den Daten kein AS bis bestimmen
            // In der neuen Struktur wird "999" nicht mehr als "unendlich" gesetzt
            if (!useNewStructure) {
                dataObjectForDB.setFieldValue(FIELD_DOG_AS_TO, EDSImportHelper.EDS_AS_BIS_UNENDLICH, DBActionOrigin.FROM_EDIT);
            }
        }
        dataObjectForDB.setFieldValue(getFieldName(FIELD_DOG_AS_FROM), versionFromJSON, DBActionOrigin.FROM_EDIT);
        // Benennung setzen
        EtkMultiSprache description = groupDataFromJSON.getNomenclatureAsMultiLangObject();
        dataObjectForDB.setFieldValueAsMultiLanguage(getFieldName(FIELD_DOG_DESC), description, DBActionOrigin.FROM_EDIT);
        if (saveToDB(dataObjectForDB)) {
            return true;
        }
        return false;
    }

    /**
     * Liefert abhängig vom Strukturtyp das richtige DB Feld
     *
     * @param originalFieldName
     * @return
     */
    private String getFieldName(String originalFieldName) {
        if (useNewStructure) {
            return OLD_STRUCTURE_MAPPING.get(originalFieldName);
        } else {
            return originalFieldName;
        }
    }

    /**
     * Erzeugt abhängig vom Strukturtyp das richtige {@link EtkDataObject}
     *
     * @param groupFromJSON
     * @return
     */
    private EtkDataObject createDataObject(String groupFromJSON) {
        if (useNewStructure) {
            return new iPartsDataModuleCategory(getProject(), new iPartsModuleCategoryId(groupFromJSON));
        } else {
            return new iPartsDataOPSGroup(getProject(), new iPartsOPSGroupId("", groupFromJSON));
        }
    }
}
