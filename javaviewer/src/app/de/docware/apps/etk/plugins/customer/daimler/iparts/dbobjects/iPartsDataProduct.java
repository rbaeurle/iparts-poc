/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.date.DateUtils;

import java.util.*;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_PRODUCT.
 */
public class iPartsDataProduct extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DP_PRODUCT_NO };

    public static final String CHILDREN_NAME_PRODUCT_MODELS = "iPartsDataProduct.productModels";
    public static final String CHILDREN_NAME_PRODUCT_FACTORIES = "iPartsDataProduct.productFactories";
    public static final String CHILDREN_NAME_PRODUCT_VARIANTS = "iPartsDataProduct.productVariants";
    public static final String CHILDREN_NAME_PRODUCT_MODULES = "iPartsDataProduct.productModules";
    public static final String CHILDREN_NAME_MODULES_EINPAS = "iPartsDataProduct.modulesEinPAS";
    public static final String CHILDREN_NAME_PRODUCT_SAS = "iPartsDataProduct.productSAs";

    public iPartsDataProduct(EtkProject project, iPartsProductId id) {
        super(KEYS);
        tableName = TABLE_DA_PRODUCT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsProductId createId(String... idValues) {
        return new iPartsProductId(idValues[0]);
    }

    @Override
    public iPartsProductId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsProductId)id;
    }

    public void clear() {
        setChildren(CHILDREN_NAME_PRODUCT_MODELS, null);
        setChildren(CHILDREN_NAME_PRODUCT_FACTORIES, null);
        setChildren(CHILDREN_NAME_PRODUCT_VARIANTS, null);
        setChildren(CHILDREN_NAME_PRODUCT_MODULES, null);
        setChildren(CHILDREN_NAME_MODULES_EINPAS, null);
        setChildren(CHILDREN_NAME_PRODUCT_SAS, null);
    }

    @Override
    public void deleteFromDB(boolean forceDelete) {
        if (forceDelete || !isNew()) { // ein neuer Datensatz muss keine Kindelemente aus der DB laden
            loadChildren();
        }
        super.deleteFromDB(forceDelete);
    }

    @Override
    public void initAttributesWithDefaultValues(DBActionOrigin origin) {
        super.initAttributesWithDefaultValues(origin);
        Set<String> brands = new HashSet<String>();
        brands.add(BRAND_MERCEDES_BENZ);
        setFieldValueAsSetOfEnum(FIELD_DP_BRAND, brands, origin);
    }

    @Override
    public boolean loadVirtualField(String attributeName) {
        if (attributeName.equals(iPartsDataVirtualFieldsDefinition.DP_MODEL_TYPES)) {
            attributes.addField(getModelTypeAttribute(), DBActionOrigin.FROM_DB);
            return false;
        }
        return super.loadVirtualField(attributeName);
    }

    public DBDataObjectAttribute getModelTypeAttribute() {
        iPartsProduct product = iPartsProduct.getInstance(getEtkProject(), getAsId());
        DBDataObjectAttribute attribute = new DBDataObjectAttribute(iPartsDataVirtualFieldsDefinition.DP_MODEL_TYPES, DBDataObjectAttribute.TYPE.ARRAY, true);
        Set<String> allModelTypes = product.getReferencedSeriesOrAllModelTypes(getEtkProject());
        if (!allModelTypes.isEmpty()) {
            EtkDataArray modelTypesArray = new EtkDataArray(iPartsDataVirtualFieldsDefinition.DP_MODEL_TYPES); // Dummy-ArrayId
            modelTypesArray.add(allModelTypes);
            attribute.setValueAsArray(modelTypesArray, DBActionOrigin.FROM_DB);
            attribute.setIdForArray(modelTypesArray.getArrayId(), DBActionOrigin.FROM_DB);
        }
        return attribute;
    }

    /**
     * Löscht das Produkt aus der Datenbank inkl. aller darin befindlichen Module. Module, die auch noch in anderen Produkten
     * verwendet werden, werden nicht gelöscht. Das Löschen kann auch abgebrochen werden, wenn der aufrufende Thread bei
     * {@link Thread#isInterrupted()}) {@code true} zurückliefert; in diesem Fall liefert diese Methode {@code false} zurück.
     *
     * @param forceDelete
     * @param messageLog  Optionales {@link EtkMessageLog} für Fortschrittsausgaben
     * @return {@code true} falls das Produkt vollständig gelöscht wurde
     */
    public boolean deleteFromDBWithModules(boolean forceDelete, EtkMessageLog messageLog) {
        loadChildren();
        DBDataObjectList<iPartsDataProductModules> productModulesList = getProductModulesList();
        List<String> moduleNumbersToDelete = new ArrayList<String>(productModulesList.size());
        for (iPartsDataProductModules dataProductModule : productModulesList) {
            moduleNumbersToDelete.add(dataProductModule.getAsId().getModuleNumber());
        }

        EtkDbObjectsLayer dbLayer = getEtkProject().getDbLayer();
        dbLayer.startBatchStatement();

        try {
            super.deleteFromDB(forceDelete);

            // Verwendete Module löschen
            int maxProgress = moduleNumbersToDelete.size() + 1; // + 1 wegen Fußnoten
            if (messageLog != null) {
                messageLog.fireProgress(0, maxProgress, "", true, false);
            }
            int counter = 0;
            for (String moduleNumber : moduleNumbersToDelete) {
                if (Thread.currentThread().isInterrupted()) {
                    if (messageLog != null) {
                        messageLog.hideProgress();
                        messageLog.fireMessage("!!Löschen vom Produkt abgebrochen, da der Thread frühzeitig beendet wurde",
                                               MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                    }
                    return false;
                }

                iPartsAssemblyId assemblyId = new iPartsAssemblyId(moduleNumber, "");
                productModulesList = iPartsDataProductModulesList.loadDataProductModulesList(getEtkProject(), assemblyId);
                if (productModulesList.isEmpty()) { // es gibt keine anderen Produkte mehr, die dieses Modul verwenden -> Modul löschen
                    EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getEtkProject(), assemblyId);
                    if (assembly instanceof iPartsDataAssembly) {
                        ((iPartsDataAssembly)assembly).delete_iPartsAssembly(true, true);
                    }
                }
                counter++;
                if (messageLog != null) {
                    messageLog.fireProgress(counter, maxProgress, "", true, true);
                }
            }

            // ELDAS Fußnoten löschen
            iPartsDataFootNoteList dataFootNoteList = new iPartsDataFootNoteList();
            dataFootNoteList.loadELDASFootNoteListForProductOrSAFromDB(getEtkProject(), getAsId().getProductNumber());
            dataFootNoteList.deleteFromDB(getEtkProject(), true);

            // EPC Fußnoten löschen
            dataFootNoteList = new iPartsDataFootNoteList();
            dataFootNoteList.loadEPCFootNoteListForProductOrSAFromDB(getEtkProject(), getAsId().getProductNumber());
            dataFootNoteList.deleteFromDB(getEtkProject(), true);

            // Werkseinsatzdaten löschen (eigentlich nur bei ELDAS- und EPC-Produkten notwendig)
            iPartsDataFactoryDataList dataFactoryDataList = new iPartsDataFactoryDataList();
            dataFactoryDataList.loadELDASAndEPCFactoryDataListForProductOrSAFromDB(getEtkProject(), getAsId().getProductNumber());
            dataFactoryDataList.deleteFromDB(getEtkProject(), true);

            // ELDAS Rückmeldedaten löschen
            iPartsDataResponseDataList dataResponseDataList = new iPartsDataResponseDataList();
            dataResponseDataList.loadELDASResponseDataListForProductOrSAFromDB(getEtkProject(), getAsId().getProductNumber());
            dataResponseDataList.deleteFromDB(getEtkProject(), true);

            // EPC Rückmeldedaten löschen
            dataResponseDataList = new iPartsDataResponseDataList();
            dataResponseDataList.loadEPCResponseDataListForProductOrSAFromDB(getEtkProject(), getAsId().getProductNumber());
            dataResponseDataList.deleteFromDB(getEtkProject(), true);

            // DELETE FROM DA_EPC_FN_KATALOG_REF WHERE DEFR_PRODUCT_NO = '<Produktnummer>';
            dbLayer.delete(TABLE_DA_EPC_FN_KATALOG_REF, new String[]{ FIELD_DEFR_PRODUCT_NO }, new String[]{ getAsId().getProductNumber() });

            // DELETE FROM DA_KGTU_AS WHERE DA_DKM_PRODUCT = '<Produktnummer>';
            dbLayer.delete(TABLE_DA_KGTU_AS, new String[]{ FIELD_DA_DKM_PRODUCT }, new String[]{ getAsId().getProductNumber() });

            // DELETE FROM DA_PICORDER_USAGE WHERE POU_PRODUCT_NO = '<Produktnummer>';
            dbLayer.delete(TABLE_DA_PICORDER_USAGE, new String[]{ FIELD_DA_POU_PRODUCT_NO }, new String[]{ getAsId().getProductNumber() });

            // DA_MODULE_CEMAT nur beim Löschen vom Produkt mit löschen.
            iPartsDataModuleCematList dataModuleCematList = iPartsDataModuleCematList.loadCematModuleForProduct(getEtkProject(), getAsId());
            dataModuleCematList.deleteFromDB(getEtkProject(), true);

            if (messageLog != null) {
                messageLog.fireProgress(maxProgress, maxProgress, "", true, true);
                messageLog.hideProgress();
            }

            return true;
        } finally {
            dbLayer.endBatchStatement();
        }
    }

    public void loadChildren() {
        getProductModelsList();
        getProductFactoriesList();
        getProductVariantsList();
        getProductModulesList();
        getModulesEinPASList();
        getProductSAsList();
    }

    public DBDataObjectList<iPartsDataProductModels> getProductModelsList() {
        DBDataObjectList<iPartsDataProductModels> productModelsList = (DBDataObjectList<iPartsDataProductModels>)getChildren(CHILDREN_NAME_PRODUCT_MODELS);
        if (productModelsList == null) {
            productModelsList = iPartsDataProductModelsList.loadDataProductModelsList(getEtkProject(), getAsId());
            setChildren(CHILDREN_NAME_PRODUCT_MODELS, productModelsList);
        }
        return productModelsList;
    }

    public DBDataObjectList<iPartsDataProductFactory> getProductFactoriesList() {
        DBDataObjectList<iPartsDataProductFactory> productFactoriesList = (DBDataObjectList<iPartsDataProductFactory>)getChildren(CHILDREN_NAME_PRODUCT_FACTORIES);
        if (productFactoriesList == null) {
            productFactoriesList = iPartsDataProductFactoryList.loadDataProductFactoryListForProduct(getEtkProject(), getAsId());
            setChildren(CHILDREN_NAME_PRODUCT_FACTORIES, productFactoriesList);
        }
        return productFactoriesList;
    }

    public DBDataObjectList<iPartsDataPSKProductVariant> getProductVariantsList() {
        DBDataObjectList<iPartsDataPSKProductVariant> productVariantsList = (DBDataObjectList<iPartsDataPSKProductVariant>)getChildren(CHILDREN_NAME_PRODUCT_VARIANTS);
        if (productVariantsList == null) {
            productVariantsList = iPartsDataPSKProductVariantList.loadPSKProductVariants(getEtkProject(), getAsId());
            setChildren(CHILDREN_NAME_PRODUCT_VARIANTS, productVariantsList);
        }
        return productVariantsList;
    }

    public DBDataObjectList<iPartsDataProductModules> getProductModulesList() {
        DBDataObjectList<iPartsDataProductModules> productModulesList = (DBDataObjectList<iPartsDataProductModules>)getChildren(CHILDREN_NAME_PRODUCT_MODULES);
        if (productModulesList == null) {
            productModulesList = iPartsDataProductModulesList.loadDataProductModulesList(getEtkProject(), getAsId());
            setChildren(CHILDREN_NAME_PRODUCT_MODULES, productModulesList);
        }
        return productModulesList;
    }

    public DBDataObjectList<iPartsDataProductSAs> getProductSAsList() {
        DBDataObjectList<iPartsDataProductSAs> productSAsList = (DBDataObjectList<iPartsDataProductSAs>)getChildren(CHILDREN_NAME_PRODUCT_SAS);
        if (productSAsList == null) {
            productSAsList = iPartsDataProductSAsList.loadDataForProduct(getEtkProject(), getAsId());
            setChildren(CHILDREN_NAME_PRODUCT_SAS, productSAsList);
        }
        return productSAsList;
    }


    public DBDataObjectList<iPartsDataModuleEinPAS> getModulesEinPASList() {
        DBDataObjectList<iPartsDataModuleEinPAS> modulesEinPASList = (DBDataObjectList<iPartsDataModuleEinPAS>)getChildren(CHILDREN_NAME_MODULES_EINPAS);
        if (modulesEinPASList == null) {
            modulesEinPASList = iPartsDataModuleEinPASList.loadForProduct(getEtkProject(), getAsId());
            setChildren(CHILDREN_NAME_MODULES_EINPAS, modulesEinPASList);
        }
        return modulesEinPASList;
    }

    public void setDatasetDate(Date datasetDate) {
        if (datasetDate != null) {
            setFieldValueAsDate(FIELD_DP_DATASET_DATE, DateUtils.toCalendar_Date(datasetDate), DBActionOrigin.FROM_EDIT);
        }
    }

    public void setMigrationDate(Date migrationDate) {
        setFieldValueAsDateTime(FIELD_DP_MIGRATION_DATE, DateUtils.toCalendar_Date(migrationDate), DBActionOrigin.FROM_EDIT);
    }

    public void setModificationTimeStamp(Date modificationTimeStamp) {
        setTimeStampField(modificationTimeStamp, FIELD_DP_MODIFICATION_TIMESTAMP);
    }

    public void setExportTimeStamp(Date exportTimeStamp) {
        setTimeStampField(exportTimeStamp, FIELD_DP_ES_EXPORT_TIMESTAMP);
    }

    private void setTimeStampField(Date timeStamp, String fieldName) {
        if (timeStamp != null) {
            setFieldValueAsDateTime(fieldName, DateUtils.toCalendar_Date(timeStamp), DBActionOrigin.FROM_EDIT);
        }
    }

    public void refreshModificationTimeStamp() {
        setModificationTimeStamp(DateUtils.toDate_currentDate());
    }

    public String getAggType() {
        return getFieldValue(FIELD_DP_AGGREGATE_TYPE);
    }

    public String getDocuMethod() {
        return getFieldValue(FIELD_DP_DOCU_METHOD);
    }

    public void setDocuMethod(String docuMethod) {
        setFieldValue(FIELD_DP_DOCU_METHOD, docuMethod, DBActionOrigin.FROM_EDIT);
    }

    public iPartsDocumentationType getDocumentationType() {
        return iPartsDocumentationType.getFromDBValue(getDocuMethod());
    }

    /**
     * Setze den Dokumentationstyp im Produkt. In alle Module des Produkts wird dieser Dokumentationstyp kopiert
     *
     * @param docuType
     */
    public void setDocumentationTypeAndInheritToModules(iPartsDocumentationType docuType) {
        iPartsDocumentationType currentDocuType = getDocumentationType();
        // Modulverwendung ermitteln
        iPartsDataProductModulesList dataProductModulesList = iPartsDataProductModulesList.loadDataProductModulesList(getEtkProject(), getAsId());

        // Alle Datensätze aus Tabelle DA_MODULES die zu diesem Produkt gehören und noch eine andere Dokumethode haben ermitteln
        iPartsDataModuleList dataModulesList = new iPartsDataModuleList();
        for (iPartsDataProductModules dataProductModules : dataProductModulesList) {
            iPartsModuleId moduleId = new iPartsModuleId(dataProductModules.getAsId().getModuleNumber());
            iPartsDataModule dataModule = new iPartsDataModule(getEtkProject(), moduleId);
            if (dataModule.loadFromDB(moduleId)) {
                if (dataModule.getDocumentationType() != docuType) {
                    dataModule.setDocuType(docuType, DBActionOrigin.FROM_EDIT);
                    dataModulesList.add(dataModule, DBActionOrigin.FROM_EDIT);
                }
            }
        }

        // Dokumethode in Modulen speichern
        if (!dataModulesList.isEmpty()) {
            dataModulesList.saveToDB(getEtkProject(), false);
        }

        // Dokumethode in Produkt speichern
        if (currentDocuType != docuType) {
            setDocuMethod(docuType.getDBValue());
        }
    }

    public void setDocumentationTypeAndInheritToModules(String docuMethod) {
        setDocumentationTypeAndInheritToModules(iPartsDocumentationType.getFromDBValue(docuMethod));
    }

}
