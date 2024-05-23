/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataEPCFootNoteCatalogueRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataEPCFootNoteCatalogueRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.iPartsMigrationHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * EPC Importer für SA-Stammdaten ([CATSinIPARTS_]BM_PARTS.csv.)
 */
public class EPCModelPartListImporter extends AbstractEPCPartListImporter {

    // Die Spalten der Importdatei
    private static final String BM_CATNUM = "CATNUM";
    private static final String BM_GROUPNUM = "GROUPNUM";
    private static final String BM_SUBGRP = "SUBGRP";
    private static final String BM_CALLOUT = "CALLOUT";
    private static final String BM_INDENT = "INDENT";
    private static final String BM_STEERING = "STEERING";
    private static final String BM_TYPE = "TYPE";
    private static final String BM_FOOTNOTES = "FOOTNOTES";
    private static final String BM_CODEB = "CODEB";
    private static final String BM_QUANTBM = "QUANTBM";
    private static final String BM_COMPONENTS = "COMPONENTS";
    private static final String BM_SUBMODS = "SUBMODS";
    private static final String BM_DAMAGE_PART = "DAMAGE_PART";
    private static final String BM_RANKING_NUMBER = "RANKING_NUMBER";
    private static final String BM_POS_ADDR = "POS_ADDR";
    private static final String BM_PARTTYPE = "PARTTYPE";

    private static final int PRODUCT_MODELS_AMOUNT_PLACEHOLDER = -1;


    private List<iPartsDataAssembly> currentAssembliesForKgInProduct;
    private Map<String, Set<iPartsModelId>> currentModelsForTypeInGlobalModelProduct;                   // Map von Typkennzahl auf gültige Baumuster für diese Typkennzahl in diesem Globalbaumuster-Produkt


    private EPCModelPartListImportHelper helper;
    private String currentProductNo;
    private KgTuId currentKgTuId;

    private boolean currentProductIsGlobalModel;
    private boolean currentProductIsSpecialCatalog;
    private int kgCounter;
    private int tuCounter;
    private int emtpyTuCounter;

    private boolean importToDB = true;


    public EPCModelPartListImporter(EtkProject project) {
        super(project, "EPC BM-Parts", "!!EPC Baumuster-Teilepositionen (BM Parts)", ImportType.IMPORT_BM_PARTS);
    }

    @Override
    protected String[] getHeaderNames() {
        return new String[]{
                BM_CATNUM,
                BM_GROUPNUM,
                BM_SUBGRP,
                SEQNUM,
                SEQNO,
                BM_CALLOUT,
                REPLFLG,
                BM_PARTTYPE,
                PARTNUM,
                NOUNIDX,
                DESCIDX,
                BM_INDENT,
                TUVSIGN,
                OPTFLAG,
                CHANGEFLAG,
                BM_STEERING,
                BM_TYPE,
                REPTYPE,
                REPPNO,
                BM_FOOTNOTES,
                NEUTRAL,
                BM_CODEB,
                BM_QUANTBM,
                BM_COMPONENTS,
                BM_SUBMODS,
                REPPART,
                OPTPART,
                BM_DAMAGE_PART,
                BM_RANKING_NUMBER,
                BM_POS_ADDR

        };
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        currentAssembliesForKgInProduct = new ArrayList<>();
        currentProductIsGlobalModel = false;
        helper = new EPCModelPartListImportHelper(getProject(), DEST_TABLENAME);
    }

    @Override
    protected String getPartTypeFieldname() {
        return BM_PARTTYPE;
    }

    @Override
    protected void checkSaaValidity(Set<String> saaBkValidity, iPartsMigrationHelper.QuantityForModelOrSAA quantityForModelOrSAA, String currentQuantity, boolean isTextPos) {
        // Hier nichts machen, da nur für SA Importer relevant
    }

    @Override
    protected String getHotspotNumber(Map<String, String> importRec) {
        return StrUtils.removeLeadingCharsFromString(helper.handleValueOfSpecialField(BM_CALLOUT, importRec), '0');
    }

    @Override
    protected void setPartlistEntryValidities(EtkDataPartListEntry destPartListEntry,
                                              iPartsMigrationHelper.QuantityForModelOrSAA quantityForModelOrSAA,
                                              String currentQuantity, boolean isTextPos) {
        if (!isTextPos) { // Gültige Baumuster nicht bei Textpositionen importieren
            iPartsMigrationHelper.setModelValidity(destPartListEntry, quantityForModelOrSAA, currentQuantity,
                                                   currentProductIsSpecialCatalog, PRODUCT_MODELS_AMOUNT_PLACEHOLDER);
        }
        if (currentProductIsSpecialCatalog) {
            // Bei TAL40 Lacke und Betriebsstoffe Katalogen die Sortimentsklassengültigkeit prüfen
            // Leeres Feld bedeutet alle Sortimentsklassen sind gültig. Laut DAIMLER-4265 sollen dann auch alle
            // Sortimentsklassen eingetragen werden (auch bei reinen TextPos(V-Positionen))
            iPartsMigrationHelper.setPClassValidity(destPartListEntry);
        }
    }

    @Override
    protected Set<String> getSaaBkValidityValues(Map<String, String> importRec, boolean isTextPos) {
        if (!isTextPos) { // SAA-Gültigkeiten nicht bei Textpositionen importieren
            String saaBk = helper.handleValueOfSpecialField(BM_COMPONENTS, importRec);
            String type = "";
            List<String> values = null;
            if (saaBk.length() > 3) {
                if (saaBk.startsWith("EVO")) {
                    type = "BK";
                    values = helper.getAsArray(saaBk.substring(3), 13, true, true);
                } else if (saaBk.startsWith("SA")) {
                    type = "SA";
                    // Nicht trimmen, weul SAs auch leerezeichen haben können!
                    values = helper.getAsArray(saaBk.substring(3), 24, false, true);
                }
            }
            return iPartsMigrationHelper.handleSasOrBkValidityForModel(this, type, values);
        }
        return null;
    }

    @Override
    protected iPartsMigrationHelper.QuantityForModelOrSAA getQuantityForModelOrSAA(Map<String, String> importRec, List<String> quantityValues) {
        String productNo = helper.handleValueOfSpecialField(BM_CATNUM, importRec);
        if (currentProductIsGlobalModel && (currentModelsForTypeInGlobalModelProduct == null)) {
            currentModelsForTypeInGlobalModelProduct = iPartsMigrationHelper.loadAllModelsForTypeInGlobaldModelProduct(getProject(), productNo);
        }
        // Mengen pro Baumuster
        iPartsMigrationHelper.QuantityForModelOrSAA quantityForModelOrSAA = iPartsMigrationHelper.handleQuantityForModel(this,
                                                                                                                         getModels(importRec, helper, productNo),
                                                                                                                         quantityValues,
                                                                                                                         currentModelsForTypeInGlobalModelProduct,
                                                                                                                         productNo, currentProductIsGlobalModel);
        return quantityForModelOrSAA;
    }

    private List<String> getModels(Map<String, String> importRec, EPCPartListImportHelper helper, String productNo) {
        String modelSuffixAsString = helper.handleValueOfSpecialField(BM_SUBMODS, importRec);
        List<String> result = new ArrayList<>();
        if (StrUtils.isValid(modelSuffixAsString)) {
            List<String> modelSuffixAsList = helper.getAsArray(modelSuffixAsString, 3, true, true);
            if (!modelSuffixAsList.isEmpty()) {
                String modelType = helper.handleValueOfSpecialField(BM_TYPE, importRec);
                if (StrUtils.isValid(modelType)) {
                    iPartsProduct product = iPartsProduct.getInstance(getProject(), new iPartsProductId(productNo));
                    String prefix = product.isAggregateProduct(getProject()) ? iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE : iPartsConst.MODEL_NUMBER_PREFIX_CAR;
                    for (String modelSuffix : modelSuffixAsList) {
                        result.add(prefix + modelType + modelSuffix);
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected Set<iPartsFootNoteId> handleFootNotes(Map<String, String> importRec, boolean isVTextPos) {
        Set<iPartsFootNoteId> footNoteIds = null;
        if (!isVTextPos) { // Fußnoten nicht bei V-Textpositionen importieren (bei Y-Textpositionen wohl schon)
            List<String> footnoteNumbers = helper.getAsArray(helper.handleValueOfSpecialField(BM_FOOTNOTES, importRec),
                                                             3, true, false);
            String tuvValue = helper.handleValueOfSpecialField(TUVSIGN, importRec);
            String kg = helper.handleValueOfSpecialField(BM_GROUPNUM, importRec);
            String productNo = helper.handleValueOfSpecialField(BM_CATNUM, importRec);

            footNoteIds = getFootnotesHandler().handleFootnotesForModel(footnoteNumbers, tuvValue, productNo, kg, false);
        }
        return footNoteIds;
    }

    @Override
    protected String getCodes(Map<String, String> importRec) {
        return helper.handleValueOfSpecialField(BM_CODEB, importRec);
    }

    @Override
    protected String getIndentValue(Map<String, String> importRec) {
        return helper.handleValueOfSpecialField(BM_INDENT, importRec);
    }

    @Override
    protected String getSteeringAndGearboxFieldname() {
        return BM_STEERING;
    }

    @Override
    protected void clearQuantityValue(Map<String, String> importRec) {
        importRec.put(BM_QUANTBM, "");
    }

    @Override
    protected List<String> getQuantityValues(Map<String, String> importRec) {
        String quantityPerModel = helper.handleValueOfSpecialField(BM_QUANTBM, importRec);
        return helper.getAsArray(quantityPerModel, 3, false, true);
    }

    @Override
    protected String getShelfLife(Map<String, String> importRec, List<String> quantityValues) {
        return iPartsMigrationHelper.getShelfLife(currentProductIsSpecialCatalog, quantityValues);
    }

    @Override
    protected String getSteeringAndGearboxValue(Map<String, String> importRec) {
        return helper.handleValueOfSpecialField(BM_STEERING, importRec);
    }

    @Override
    protected boolean checkIfAlreadyCreatedFromMAD(Map<String, String> importRec) {
        String productNumber = helper.handleValueOfSpecialField(BM_CATNUM, importRec);
        // Logische Prüfung mit Ausgabe von Meldungen
        return !helper.isProductRelevantForImport(this, productNumber, getProductRelevanceCache(), getCurrentRecordNo());

    }

    @Override
    protected void checkAndCreateNewAssembly(Map<String, String> importRec) {
        String productNumber = helper.handleValueOfSpecialField(BM_CATNUM, importRec);
        String kg = helper.handleValueOfSpecialField(BM_GROUPNUM, importRec);
        String tu = helper.handleValueOfSpecialField(BM_SUBGRP, importRec);
        KgTuId kgTuId = new KgTuId(kg, tu);
        // CHeck, ob ein neues Modul erstellt werden muss
        checkIfNewAssemblyForProductAndKgTu(productNumber, kgTuId);
        if (getCurrentAssembly() == null) {
            getMessageLog().fireMessage(translateForLog("!!Fehler beim Erzeugen des TUs \"%1\" für das " +
                                                        "Produkt \"%2\"", kgTuId.toString(), productNumber),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                        MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            reduceRecordCount();
        }

    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            storeCurrentAssemblies();
        }
        if (!importToDB) {
            cancelImport(translateForLog("!!Import nicht aktiv!"));
        }
        super.postImportTask();
    }

    @Override
    protected void storeFinishedAssembly() {
        // Restliche Aufräumarbeiten
        if (!iPartsMigrationHelper.storeFinishedAssemblyForModel(this, getCurrentAssembly(),
                                                                 currentAssembliesForKgInProduct, null, true)) {
            emtpyTuCounter++;
        }
    }

    @Override
    protected String getOmittedPartNumber(Map<String, String> importRec) {
        // Gibt's nur bei SA Stücklistenpositionen
        return "";
    }

    private void storeCurrentAssemblies() {
        if (currentKgTuId != null) {
            iPartsMigrationHelper.saveCurrentAssembliesForKgInProduct(this, currentAssembliesForKgInProduct, currentKgTuId.getKg(), true); // alle Module für die aktuelle KG speichern
        }
        getFootnotesHandler().saveCurrentFootNotesForPartListEntries(getAllCurrentColorTablefootnotes(), getAllFootnoteGroupsForFootnoteNumber(), false); // alle Fußnoten für KG speichern
    }

    private void checkIfNewAssemblyForProductAndKgTu(String productNumber, KgTuId kgTuId) {
        boolean makeNewAssembly = true;
        if ((getCurrentAssembly() != null)) {
            if (currentKgTuId == null) {
                getMessageLog().fireMessage(translateForLog("!!Fehler beim Prüfen des TUs \"%1\" für das " +
                                                            "Produkt \"%2\". KGTU darf nicht null sein!", kgTuId.toString(),
                                                            productNumber),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                return;
            }
            // Assembly existiert schon -> Check, ob eine neue angelegt werden soll
            if (currentKgTuId.equals(kgTuId) && (currentProductNo != null) && currentProductNo.equals(productNumber)) {
                makeNewAssembly = false;
            } else {
                // Unterschiedliche AssemblyIds -> alte Speichern
                finishAssembly();
                tuCounter++;
            }
        }

        if (makeNewAssembly) {
            createNewAssembly(productNumber, kgTuId);
        }
    }

    private void createNewAssembly(String productNo, KgTuId kgTuId) {
        String moduleNameWithoutSerial = EditModuleHelper.buildKgTuModuleNumberWithoutSerial(new iPartsProductId(productNo), kgTuId);
        String moduleNumber = moduleNameWithoutSerial + EditModuleHelper.IPARTS_MODULE_NAME_DELIMITER + EditModuleHelper.formatModuleSerialNumber(1);
        AssemblyId newAssemblyId = new AssemblyId(moduleNumber, "");
//        AssemblyId newAssemblyId = new AssemblyId(EditModuleHelper.buildKgTuModuleNumber(new iPartsProductId(productNo), kgTuId,getProject()), "");
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), newAssemblyId);
        boolean oldModuleHidden = deleteAssemblyIfExists(assembly);
        iPartsProductId productId = new iPartsProductId(productNo);
        iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);

        // Dokumentationstyp als Produkt bestimmen
        iPartsDocumentationType documentationType = product.getDocumentationType();
        currentProductIsGlobalModel = product.getDocumentationType() == iPartsDocumentationType.BCS_PLUS_GLOBAL_BM;

        // Modultyp aus Dokumentationstyp bestimmen ausser bei Spezialkatalogen
        iPartsModuleTypes moduleType;
        if (currentProductIsSpecialCatalog) {
            moduleType = iPartsModuleTypes.WorkshopMaterial;
        } else {
            moduleType = documentationType.getModuleType(false);
        }

        // Fallback auf Modultyp EDSRetail und den zugehörigen Dokumentationstyp
        if (moduleType == iPartsModuleTypes.UNKNOWN) {
            moduleType = iPartsModuleTypes.EDSRetail;
        }
        if (documentationType == iPartsDocumentationType.UNKNOWN) {
            documentationType = moduleType.getDefaultDocumentationType();
        }

        iPartsDataAssembly currentAssembly = EditModuleHelper.createAndSaveModuleWithKgTuAssignment(newAssemblyId,
                                                                                                    moduleType,
                                                                                                    getModuleName(newAssemblyId),
                                                                                                    productId,
                                                                                                    kgTuId,
                                                                                                    getProject(),
                                                                                                    documentationType,
                                                                                                    false, null);
        if (oldModuleHidden) {
            currentAssembly.getModuleMetaData().setFieldValueAsBoolean(FIELD_DM_MODULE_HIDDEN, true, DBActionOrigin.FROM_EDIT);
        }

        setCurrentAssembly(currentAssembly);

        boolean productChanged = (currentProductNo == null) || !currentProductNo.equals(productNo);
        boolean kgChanged = (currentKgTuId == null) || !currentKgTuId.getKg().equals(kgTuId.getKg());
        boolean tuChanged = (currentKgTuId == null) || !currentKgTuId.getTu().equals(kgTuId.getTu());


        if (productChanged) {
            logProductStats();
            deleteProductData(productNo);
            handleKgChanged(productNo, kgTuId);
            currentKgTuId = kgTuId;
            kgCounter++;
            currentProductNo = productNo;
            getMessageLog().fireMessage(translateForLog("!!Importiere Produkt \"%1\"", productNo),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            clearPartsDoneCache();
            getModuleVariantsVisibleMap().clear();
        } else if (kgChanged) {
            handleKgChanged(productNo, kgTuId);
            currentKgTuId = kgTuId;
            kgCounter++;
        } else if (tuChanged) {
            currentKgTuId = kgTuId;
        }
    }

    protected EtkMultiSprache getModuleName(AssemblyId assemblyId) {
        // Der Modulname soll die Modulnummer verwendet werden
        EtkMultiSprache modulename = new EtkMultiSprache();
        modulename.setText(Language.DE, assemblyId.getKVari());
        return modulename;
    }

    private void logProductStats() {
        if (currentProductNo != null) {
            getMessageLog().fireMessage(translateForLog("!!Für Produkt \"%1\" wurden %2 KGs mit %3 TUs verarbeitet. " +
                                                        "%4 TUs gespeichert, %5 TUs enthielten keine Stücklistenpositionen.",
                                                        currentProductNo, String.valueOf(kgCounter),
                                                        String.valueOf(tuCounter + emtpyTuCounter),
                                                        String.valueOf(tuCounter), String.valueOf(emtpyTuCounter)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
        kgCounter = 0;
        tuCounter = 0;
        emtpyTuCounter = 0;

    }

    private void deleteProductData(String productNo) {
        EtkDbObjectsLayer dbLayer = getProject().getDbLayer();
        try {
            // EPC Fußnoten löschen
            iPartsDataFootNoteList dataFootNoteList = new iPartsDataFootNoteList();
            dataFootNoteList.loadEPCFootNoteListForProductOrSAFromDB(getProject(), productNo);
            dataFootNoteList.deleteFromDB(getProject(), true);

            // Werkseinsatzdaten löschen (eigentlich nur bei ELDAS- und EPC-Produkten notwendig)
            iPartsDataFactoryDataList dataFactoryDataList = new iPartsDataFactoryDataList();
            dataFactoryDataList.loadELDASAndEPCFactoryDataListForProductOrSAFromDB(getProject(), productNo);
            dataFactoryDataList.deleteFromDB(getProject(), true);

            // EPC Rückmeldedaten löschen
            iPartsDataResponseDataList dataResponseDataList = new iPartsDataResponseDataList();
            dataResponseDataList.loadEPCResponseDataListForProductOrSAFromDB(getProject(), productNo);
            dataResponseDataList.deleteFromDB(getProject(), true);

            // Beim Start vom Importer wird bereits immer eine Transaktion gestartet -> Commit und neue Transaktion starten
            dbLayer.commit();
            dbLayer.startTransaction();
        } catch (RuntimeException e) {
            getMessageLog().fireMessage(translateForLog("!!Fehler beim Löschen der Daten für das Produkt \"%1\".", productNo),
                                        MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
            throw e;
        }
    }

    private void initKgData(String productNo, KgTuId kgTuId) {
        loadFootnotesForCurrentProductAndKG(productNo, kgTuId.getKg());
        currentAssembliesForKgInProduct.clear();
        if (productNo.equals("598")) {
            currentProductIsSpecialCatalog = true;
        } else {
            currentProductIsSpecialCatalog = false;
        }
    }

    private void loadFootnotesForCurrentProductAndKG(String productNo, String kg) {
        iPartsDataEPCFootNoteCatalogueRefList allFootnotesRefsForKG
                = iPartsDataEPCFootNoteCatalogueRefList.loadAllRefsForKGWithPlaceholderSigns(getProject(), productNo, kg);
        for (iPartsDataEPCFootNoteCatalogueRef footnote : allFootnotesRefsForKG) {
            if (isCancelled()) {
                return;
            }
            FootnoteObject footnoteObject = new FootnoteObject(footnote.getFieldValue(FIELD_DEFR_PRODUCT_NO),
                                                               footnote.getFieldValue(FIELD_DEFR_KG),
                                                               footnote.getFieldValue(FIELD_DEFR_FN_NO),
                                                               footnote.getFieldValue(FIELD_DEFR_TEXT_ID),
                                                               footnote.getFieldValue(FIELD_DEFR_GROUP),
                                                               footnote.getFieldValue(FIELD_DEFC_ABBR));
            importFootNote(footnoteObject);
        }
    }

    private void handleKgChanged(String productNo, KgTuId newkgTuId) {
        if (currentKgTuId != null) {
            storeCurrentAssemblies();
        }
        initKgData(productNo, newkgTuId);
    }

    /**
     * Der Model Helper
     */
    protected class EPCModelPartListImportHelper extends EPCPartListImportHelper {

        public EPCModelPartListImportHelper(EtkProject project, String tableName) {
            super(project, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            value = super.handleValueOfSpecialField(sourceField, value);
            return value;
        }
    }
}
