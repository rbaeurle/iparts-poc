/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.drawing.DataImageId;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsOmittedParts;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteCatalogueRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleEinPASId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditConstructionToRetailHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQPicScheduler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractFilesImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuForProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.images.PictureReference;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.DIALOGImportHelper;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.diskmappedlist.DiskMappedKeyValueEntry;
import de.docware.util.collections.diskmappedlist.compare.DiskMappedKeyValueListCompare;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Import Worker für den Baureihen-Import
 * Alle Teilimporter vor dem POSD-Importer schreiben nicht in die DB sondern befüllen diese Instanz. Der POSD-Importer ruft
 * in postImportTask() die Methode saveSeriesToDB() auf und speichert die enthaltenen Daten.
 */
public class iPartsCatalogImportWorker implements iPartsConst, EtkDbConst {

    // Felder, die beim Vergleich von Stücklisteneinträgen in entryIsSimilar() ignoriert werden sollen
    // TODO RT: müssten die anderen K_SOURCE_XXX Felder nicht auch ausgenommen werden? Sie enthalten doch dasselbe wie K_SOURCE_GUID
    private static final Set<String> ignoreFieldsForEntryComparison = new HashSet<>();
    private static final int MAX_ENTRIES_DELETE_COLORTABLE_DATA = 500;

    static {
        ignoreFieldsForEntryComparison.add(FIELD_STAMP);
        ignoreFieldsForEntryComparison.add(FIELD_K_LFDNR);
        ignoreFieldsForEntryComparison.add(FIELD_K_SEQNR);
        ignoreFieldsForEntryComparison.add(FIELD_K_MATNR);
        ignoreFieldsForEntryComparison.add(FIELD_K_VER);
        ignoreFieldsForEntryComparison.add(FIELD_K_SACH);
        ignoreFieldsForEntryComparison.add(FIELD_K_SVER);
        ignoreFieldsForEntryComparison.add(FIELD_K_SOURCE_GUID);
    }

    private ProductManagement products;
    private AssemblyManagement assemblies;
    private Map<iPartsModelId, iPartsDataModel> modelList;
    private Map<iPartsDialogBCTEPrimaryKey, List<AssemblyId>> btdpKeyToAssemblyListMap;
    private Map<iPartsDialogBCTEPrimaryKey, Map<iPartsProductId, Set<AssemblyId>>> dialogPosToAssemblySetPerProductMap;
    private Map<String, BCTE_FootNote_Container> bcteFootNoteIdList;
    private Map<iPartsDialogBCTEPrimaryKey, List<Fail_Location_Container>> bcteFailLocationMap;
    private Map<iPartsDialogBCTEPrimaryKey, List<Replace_Elem>> bcteReplaceMap;
    private Map<Include_Elem_Key, Include_Elem> bcteIncludeMap;
    private Map<iPartsDialogBCTEPrimaryKey, List<PEMEvaluationEntry>> bctePEMEvaluationList;
    private Map<String, Set<String>> asPemToFactoriesMap;
    private DiskMappedKeyValueListCompare colorTableDatasets;
    private iPartsOmittedParts omittedParts;

    //Map von PartlistEntryId auf Ausführungsart zur Bestimmung der passenden Ersetzungen
    private Map<PartListEntryId, String> partListEntryIdAAMap;

    // Maps mit allen BCTE-Schlüsseln (mit bzw. ohne SData) auf Liste von Stücklisteneinträgen
    private Map<iPartsDialogBCTEPrimaryKey, List<EtkDataPartListEntry>> bcteKeyToPartListEntriesMap = new HashMap<>();
    private Map<iPartsDialogBCTEPrimaryKey, List<EtkDataPartListEntry>> bcteKeyWithoutSDataToPartListEntriesMap = new HashMap<>();

    // Werte für diesen Teilestamm wurden schon übernommen
    private Map<PartId, EtkDataPart> partsDone;
    private EtkProject project;
    private boolean withAACheck;

    private String modificationReason;
    private AbstractFilesImporter importer;
    private Map<AssemblyId, Set<iPartsPicReferenceId>> picReferenceIdsForModule;
    // Zur Vermeidung von zu vielen Logfile-Ausgaben.
    private Set<String> lastLogfileMsgSet = new HashSet<>();
    private Date dataSetDate; // Das Datum der Importdatei
    private iPartsSeriesId seriesId;

    private iPartsCatalogImportWorker catalogImportWorkerToMerge;
    private Map<String, String> originalProductsToMergedProductsMap;

    public iPartsCatalogImportWorker(EtkProject project, Date dataSetDate) {
        this.project = project;
        this.setDataSetDate(dataSetDate);
        init();
    }

    /**
     * Spezial-Konstruktor für die Zusammenführung von Produkten
     *
     * @param catalogImportWorkerToMerge {@link iPartsCatalogImportWorker} mit den Original-Produkten, der als Basis für
     *                                   die Zusammenführung der Produkte verwendet werden soll
     */
    public iPartsCatalogImportWorker(iPartsCatalogImportWorker catalogImportWorkerToMerge) {
        this.catalogImportWorkerToMerge = catalogImportWorkerToMerge;
        originalProductsToMergedProductsMap = new LinkedHashMap<>();
        project = catalogImportWorkerToMerge.project;
        setDataSetDate(catalogImportWorkerToMerge.dataSetDate);
        init();

        // Relevante Daten vom Original-iPartsCatalogImportWorker übernehmen, die nicht neu importiert werden müssen
        modelList = catalogImportWorkerToMerge.modelList;
        bcteFootNoteIdList = catalogImportWorkerToMerge.bcteFootNoteIdList;
        bcteFailLocationMap = catalogImportWorkerToMerge.bcteFailLocationMap;
        bctePEMEvaluationList = catalogImportWorkerToMerge.bctePEMEvaluationList;
        asPemToFactoriesMap = catalogImportWorkerToMerge.asPemToFactoriesMap;
        omittedParts = catalogImportWorkerToMerge.omittedParts;
        partsDone = catalogImportWorkerToMerge.partsDone;
        withAACheck = catalogImportWorkerToMerge.withAACheck;
        seriesId = catalogImportWorkerToMerge.seriesId;

        // Damit später in MigrationDialogPosDImporter.postImportTask() keine Referenzen auf die Daten mehr existieren
        catalogImportWorkerToMerge.clearBCTE_FootNoteIdList();
        catalogImportWorkerToMerge.clearBCTE_FailLocationMap();
        catalogImportWorkerToMerge.clearBCTE_PEMEvaluationList();
        catalogImportWorkerToMerge.asPemToFactoriesMap = null;
    }

    public Date getDataSetDate() {
        return dataSetDate;
    }

    public String getDataSetDateStr() {
        if (dataSetDate != null) {
            return new SimpleDateFormat("yyyyMMdd").format(dataSetDate);
        }
        return "";
    }

    private void setDataSetDate(Date dataSetDate) {
        this.dataSetDate = dataSetDate;
    }

    public iPartsDataProductList getProductList() {
        return products.getProductList();
    }

    public EtkProject getProject() {
        return project;
    }

    public boolean isWithAACheck() {
        return withAACheck;
    }

    /**
     * Legt fest, ob beim Einfügen des POSD-Stücklisten-Elemente die 'Ausführungsart' überprüft werden soll
     * (default: true)
     *
     * @param withAACheck
     */
    public void setWithAACheck(boolean withAACheck) {
        this.withAACheck = withAACheck;
    }

    public Map<String, BCTE_FootNote_Container> getBCTE_FootNoteIdList() {
        return bcteFootNoteIdList;
    }

    public void clearBCTE_FootNoteIdList() {
        bcteFootNoteIdList = null;
    }

    public Map<iPartsDialogBCTEPrimaryKey, List<Fail_Location_Container>> getBCTE_FailLocationMap() {
        return bcteFailLocationMap;
    }

    public void clearBCTE_FailLocationMap() {
        bcteFailLocationMap = null;
    }

    public Map<Include_Elem_Key, Include_Elem> getBCTE_IncludeMap() {
        return bcteIncludeMap;
    }

    public Map<iPartsDialogBCTEPrimaryKey, List<Replace_Elem>> getBCTE_ReplaceMap() {
        return bcteReplaceMap;
    }

    public Map<PartListEntryId, String> getPartListEntryIdAAMap() {
        return partListEntryIdAAMap;
    }

    public void clearPartListEntryIdAAMap() {
        partListEntryIdAAMap = null;
    }

    public void clearBCTE_AssemblyMaps() {
        btdpKeyToAssemblyListMap = null;
        dialogPosToAssemblySetPerProductMap = null;
        bcteKeyToPartListEntriesMap.clear();
        bcteKeyWithoutSDataToPartListEntriesMap.clear();
    }

    /**
     * Aus der Replacement-Map die betroffenen Module heraussuchen
     *
     * @return
     */
    private Set<AssemblyId> getAssembliesFromReplaceMap() {
        Set<AssemblyId> result = new HashSet<AssemblyId>();
        for (Collection<Replace_Elem> replaceList : getBCTE_ReplaceMap().values()) {
            for (Replace_Elem replaceElem : replaceList) {
                if (!replaceElem.originalEntry.partListEntryIdList.isEmpty()) {
                    for (PartListEntryId partListEntryId : replaceElem.originalEntry.partListEntryIdList) {
                        result.add(partListEntryId.getOwnerAssemblyId());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Liste der betroffenen Replace_/Include_Elems in diesem Modul bestimmen
     *
     * @param assemblyId
     * @param replaceElemsList Zu befüllende Liste der Ersetzungen
     * @param includeElemsSet  Zu befüllendes Set der Mitlieferteile
     */
    private void fillReplacementsForAssembly(AssemblyId assemblyId, Set<Replace_Elem> replaceElemsList, Set<Include_Elem> includeElemsSet) {
        replaceElemsList.clear();
        includeElemsSet.clear();
        // über die gesamte Replacement-Map
        for (List<Replace_Elem> replacementList : getBCTE_ReplaceMap().values()) {
            boolean added = false;
            // über alle Replace_Elems
            for (Replace_Elem replaceElem : replacementList) {
                // wurde Vorgänger gefunden?
                if (!replaceElem.originalEntry.partListEntryIdList.isEmpty()) {
                    // über alle gefundenen Vorgänger iterieren
                    for (PartListEntryId predecessorEntryId : replaceElem.originalEntry.partListEntryIdList) {
                        // Gehört der Vorgänger zum aktuellen Modul?
                        if (predecessorEntryId.getOwnerAssemblyId().equals(assemblyId)) {
                            if (!added) {
                                // die komplette replacementList merken - diese wird anschließend bearbeitet
                                replaceElemsList.addAll(replacementList);
                                added = true;
                            }

                            // Gibt es zu dem Vorgänger und Nachfolger DIALOG-Key einen Eintrag in der Include-Map:
                            // Weil Mitlieferteile nicht KEM-Stand spezifisch sind, muss hier mit dem BCTE-Schlüssel
                            // OHNE SDA gesucht werden;
                            Include_Elem includeElem = getBCTE_IncludeMap().get(new Include_Elem_Key(replaceElem.originalEntry.matNo,
                                                                                                     replaceElem.replaceEntry.dialogBCTEKey.getPositionBCTEPrimaryKeyWithoutSDA()));
                            if (includeElem != null) {
                                // Include_Elem merken - dieses wird anschließend bearbeitet
                                includeElemsSet.add(includeElem);
                            }
                        }
                    }
                }
            }
        }
    }

    public void clearBCTE_ReplaceMap() {
        bcteReplaceMap = null;
    }

    public void clearBCTE_IncludeMap() {
        bcteIncludeMap = null;
    }

    public void clearBCTE_PEMEvaluationList() {
        bctePEMEvaluationList = null;
    }

    public Map<iPartsDialogBCTEPrimaryKey, List<PEMEvaluationEntry>> getBCTE_PEMEvaluationList() {
        return bctePEMEvaluationList;
    }

    public Map<String, Set<String>> getAsPemToFactoriesMap() {
        return asPemToFactoriesMap;
    }

    //Methoden, die später in den BasisImporter für Kataloge gehören
    protected void init() {
        products = new ProductManagement();
        assemblies = new AssemblyManagement();
        modelList = new HashMap<>();
        partsDone = new HashMap<>();
        bcteFootNoteIdList = new HashMap<>();
        bcteFailLocationMap = new HashMap<>();
        bcteReplaceMap = new HashMap<>();
        bcteIncludeMap = new HashMap<>();
        bctePEMEvaluationList = new HashMap<>();
        withAACheck = true;
        picReferenceIdsForModule = new HashMap<>();
        asPemToFactoriesMap = new HashMap<>();
        partListEntryIdAAMap = new HashMap<>();
        colorTableDatasets = new DiskMappedKeyValueListCompare(true, false, true);
        omittedParts = iPartsOmittedParts.getInstance(project);
    }

    private void clearModificationReason() {
        modificationReason = "";
    }

    private void setModificationReason(String translationsKey, String... placeHolderTexts) {
        if (importer != null) {
            modificationReason = importer.translateForLog(translationsKey, placeHolderTexts);
        }
    }

    private String getModificationReason() {
        return modificationReason;
    }

    /**
     * Produkt ermitteln.
     * Produkt in gespeicherter Liste suchen; sonst aus DB laden bzw. neu anlegen und in Liste speichern
     *
     * @param importer
     * @param productNo
     * @param origin
     * @return
     */
    public iPartsDataProduct createProductData(AbstractFilesImporter importer, String productNo, DBActionOrigin origin) {
        iPartsProductId productId = new iPartsProductId(productNo);
        iPartsDataProduct dataProduct = getProductData(productId);
        if (dataProduct == null) {
            dataProduct = new iPartsDataProduct(getProject(), productId);
            if (dataProduct.existsInDB()) {
                loadExistingModuleReferencesForProduct(dataProduct);
            } else {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Produkt %1 wird angelegt", productId.getProductNumber()), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                dataProduct.initAttributesWithDefaultValues(origin);
            }
            iPartsDocumentationType documentationType = dataProduct.getDocumentationType();
            if (documentationType == iPartsDocumentationType.UNKNOWN) {
                dataProduct.setDocuMethod(iPartsDocumentationType.DIALOG.getDBValue());
            }
            products.addProduct(dataProduct, origin);
        }
        return dataProduct;
    }

    /**
     * Alle bereits existierenden Modulreferenzen des übergebenen Produkts merken.
     *
     * @param dataProduct
     */
    public void loadExistingModuleReferencesForProduct(iPartsDataProduct dataProduct) {
        // ehemalige Module des Produkts merken
        for (iPartsDataProductModules dataProductModules : dataProduct.getProductModulesList()) {
            AssemblyId pseudoAssemblyId = buildPseudoAssemblyId(dataProductModules);
            //in DiskMappedKeyValueListCompare aufsammeln
            products.listComp.putFirst(buildListCompareKey(dataProduct, pseudoAssemblyId), dataProductModules.getAsId().getModuleNumber());
        }

        // ehemalige Verortung (jeweils genau eine pro Modul) der Module des Produkts merken
        for (iPartsDataModuleEinPAS dataModuleEinPAS : dataProduct.getModulesEinPASList()) {
            products.moduleAssignmentMap.put(dataModuleEinPAS.getAsId().getModuleNumber(), dataModuleEinPAS);
        }
    }

    public ProductManagement getProductManagement() {
        return products;
    }

    public iPartsDataProduct getProductData(iPartsProductId productId) {
        return products.getProduct(productId);
    }

    public iPartsDataProduct getProductData(String productNo) {
        iPartsProductId productId = new iPartsProductId(productNo);
        return getProductData(productId);
    }

    public iPartsDataProductModels addProductModel(iPartsDataProduct dataProduct, iPartsDataProductModels dataProductModel, DBActionOrigin origin) {
        dataProduct.getProductModelsList().add(dataProductModel, origin);
        return dataProductModel;
    }

    public iPartsDataModel getModel(iPartsModelId modelId) {
        return modelList.get(modelId);
    }

    /**
     * Baumuster zu gespeicherter Liste hinzufügen wenn es in DB existiert.
     *
     * @param modelNo
     * @return null wenn Baumuster nicht in DB existiert
     */
    public iPartsDataModel addModel(String modelNo) {
        iPartsModelId modelId = new iPartsModelId(modelNo);
        iPartsDataModel dataModel = getModel(modelId);
        if (dataModel == null) {
            dataModel = new iPartsDataModel(getProject(), modelId);
            if (!dataModel.existsInDB()) {
                //wenn AS-Model nicht existiert => Fehler
                return null;
            }
            modelList.put(modelId, dataModel);
        }
        return dataModel;
    }

    public iPartsDataAssembly getAssembly(AssemblyId assemblyId) {
        return assemblies.getAssembly(assemblyId);
    }

    /**
     * ModulName für Product und KG/TU bestimmen (nach Edit-Regel)
     *
     * @param dataProduct
     * @param kgTuId
     * @return
     */
    private EtkMultiSprache getModuleName(iPartsDataProduct dataProduct, KgTuId kgTuId) {
        KgTuForProduct kgTuForProduct = KgTuForProduct.getInstance(getProject(), dataProduct.getAsId());
        KgTuNode node = kgTuForProduct.getTuNode(kgTuId.getKg(), kgTuId.getTu());
        EtkMultiSprache moduleName;
        if (node != null) {
            moduleName = node.getTitle().cloneMe();
        } else {
            moduleName = new EtkMultiSprache();
        }
        return moduleName;
    }

    /**
     * Pseudo-AssemblyId erstellen (ohne laufende Nummer)
     *
     * @param dataProduct
     * @param kgTuId
     * @return
     */
    private AssemblyId buildPseudoAssemblyId(iPartsDataProduct dataProduct, KgTuId kgTuId) {
        return new AssemblyId(EditModuleHelper.buildKgTuModuleNumberWithoutSerial(dataProduct.getAsId(), kgTuId), "");
    }

    /**
     * Pseudo-AssemblyId aus der ModuleNumber bestimmen (ohne laufende Nummer)
     *
     * @param dataProductModules
     * @return
     */
    private AssemblyId buildPseudoAssemblyId(iPartsDataProductModules dataProductModules) {
        String moduleNumber = dataProductModules.getAsId().getModuleNumber();
        moduleNumber = StrUtils.stringUpToLastCharacter(moduleNumber, '_'); // lfdNr entfernen (nach dem letzten _)
        return new AssemblyId(moduleNumber, "");
    }

    /**
     * Key Value für DiskMappedKeyValueListCompare festlegen
     *
     * @param dataProduct
     * @param pseudoAssemblyId
     * @return
     */
    private String buildListCompareKey(iPartsDataProduct dataProduct, AssemblyId pseudoAssemblyId) {
        return dataProduct.getAsId().getProductNumber() + "_" + pseudoAssemblyId.getKVari();
    }

    /**
     * Modul anlegen und merken (wird von BTDP-Importer aufgerufen
     *
     * @param importer
     * @param dataProduct
     * @param kgTuId
     * @param origin
     * @param warnings
     * @return
     */
    public iPartsDataAssembly createAndAddAssembly(AbstractFilesImporter importer, iPartsDataProduct dataProduct, KgTuId kgTuId,
                                                   DBActionOrigin origin, List<String> warnings) {
        warnings.clear();
        AssemblyId pseudoAssemblyId = buildPseudoAssemblyId(dataProduct, kgTuId);
        iPartsDataAssembly dataAssembly = getAssembly(pseudoAssemblyId);
        if (dataAssembly == null) {
            //Modul wird zum ersten Mal angesprochen
            EtkMultiSprache moduleName = getModuleName(dataProduct, kgTuId);
            boolean isNew = false;
            String moduleNo = products.listComp.getOnlyInFirstItems().get(buildListCompareKey(dataProduct, pseudoAssemblyId));
            if (moduleNo == null) {
                //Modul war in den bereits vorhandenen Produkten nicht eingebunden => Neu
                moduleNo = EditModuleHelper.buildKgTuModuleNumber(dataProduct.getAsId(), kgTuId, getProject());
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Modul \"%1\" wird neu angelegt", moduleNo),
                                                     MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                isNew = true;
            }
            //falls Modul bereits vorhanden, dann kVari auf Dummywert setzen
            AssemblyId assemblyId = new AssemblyId(moduleNo, isNew ? "" : "ImpSe"); // "ImpSe" für "Import Series" (max. 5 Zeichen!)
            EtkDataAssembly actDataAssembly = EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId, false);
            dataAssembly = (iPartsDataAssembly)actDataAssembly;
//                dataAssembly = EditModuleHelper.createAndSaveModuleWithKgTuAssignment(assemblyId, iPartsModuleTypes.DialogRetail,
//                                                                                      moduleName, dataProduct.getAsId(), kgTuId,
//                                                                                      getProject(), false);
//            }
            if (dataAssembly != null) {
                dataAssembly.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                assemblies.addAssembly(dataProduct.getAsId(), pseudoAssemblyId, dataAssembly, isNew, kgTuId, moduleName);
                //für den Vergleich zu löschender Module
                products.listComp.putSecond(buildListCompareKey(dataProduct, pseudoAssemblyId), moduleNo);
            } else {
                warnings.add(importer.translateForLog("!!Modul \"%1\" kann nicht angelegt werden.", assemblyId.toString()));
            }
        }
        return dataAssembly;
    }

    /**
     * wird am Ende des BTDP-Importers aufgerufen
     *
     * @param importer
     * @return
     */
    public boolean handleDeletedModules(AbstractFilesImporter importer) {
        int count = products.listComp.getOnlyInFirstItems().size();
        if (count > 0) {
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Es werden %1 Module gelöscht", String.valueOf(count)),
                                                 MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            //Lösche Module
            Iterator<DiskMappedKeyValueEntry> iter = products.listComp.getOnlyInFirstItems().getIterator();
            while (iter.hasNext()) {
                if (Thread.currentThread().isInterrupted()) {
                    importer.cancelImport("!!Import-Thread wurde frühzeitig beendet");
                    return false;
                }
                DiskMappedKeyValueEntry entry = iter.next();
                String moduleNo = entry.getValue();
                AssemblyId assemblyId = new AssemblyId(moduleNo, "");
                iPartsDataAssembly dataAssembly = (iPartsDataAssembly)EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId, false);
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Modul \"%1\" wird gelöscht", dataAssembly.getAsId().getKVari()),
                                                     MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                //alle Verwendungen des Moduls in Produkten löschen
                iPartsDataProductModulesList productModulesLists = iPartsDataProductModulesList.loadDataProductModulesList(getProject(), dataAssembly.getAsId());
                for (iPartsDataProductModules dataProductModule : productModulesLists) {
                    iPartsDataProduct dataProduct = getProductData(dataProductModule.getAsId().getProductNumber());
                    if (dataProduct == null) {
                        dataProductModule.deleteFromDB(true);
                    } else {
                        int index = 0;
                        for (iPartsDataProductModules testdataProductModule : dataProduct.getProductModulesList().getAsList()) {
                            if (testdataProductModule.getAsId().equals(dataProductModule.getAsId())) {
                                dataProduct.getProductModulesList().delete(index, DBActionOrigin.FROM_EDIT);
                                break;
                            }
                            index++;
                        }
                    }
                }

                // alle Verortungen des Moduls löschen
                iPartsDataModuleEinPASList moduleEinPASList = iPartsDataModuleEinPASList.loadForModule(getProject(), dataAssembly.getAsId());
                for (iPartsDataModuleEinPAS dataModuleEinPAS : moduleEinPASList) {
                    iPartsDataProduct dataProduct = getProductData(dataModuleEinPAS.getAsId().getProductNumber());
                    if (dataProduct == null) {
                        dataModuleEinPAS.deleteFromDB(true);
                    } else {
                        dataProduct.getModulesEinPASList().delete(dataModuleEinPAS, DBActionOrigin.FROM_EDIT);
                        int index = 0;
                        for (iPartsDataModuleEinPAS testdataModuleEinPAS : dataProduct.getModulesEinPASList().getAsList()) {
                            if (testdataModuleEinPAS.getAsId().equals(dataModuleEinPAS.getAsId())) {
                                dataProduct.getModulesEinPASList().delete(index, DBActionOrigin.FROM_EDIT);
                                break;
                            }
                            index++;
                        }
                    }
                }

                // Eintrag in DA_MODULES Tabelle löschen
                iPartsDataModule moduleEntry = dataAssembly.getModuleMetaData();
                moduleEntry.deleteFromDB(true);

                dataAssembly.delete_iPartsAssembly(false);
            }
        }
        products.listComp.cleanup();
        return true;
    }

    public DataObjectBTDP getBTDPElement(iPartsDataProduct dataProduct, KgTuId kgTuId) {
        return assemblies.getDataObjectBTDP(buildPseudoAssemblyId(dataProduct, kgTuId));
    }

    private Map<iPartsDialogBCTEPrimaryKey, List<AssemblyId>> getBTDPKeyToAssemblyList() {
        if (btdpKeyToAssemblyListMap == null) {
            //zu jedem DIALOG-Schlüssel (BTDP) die Module bestimmen
            btdpKeyToAssemblyListMap = new HashMap<>();
            dialogPosToAssemblySetPerProductMap = new HashMap<>();
            for (DataObjectBTDP dataObjectBTDP : assemblies.getAssemblyList()) {
                for (iPartsDialogBCTEPrimaryKey bctePrimaryKey : dataObjectBTDP.btdpList.keySet()) {
                    List<AssemblyId> ids = btdpKeyToAssemblyListMap.get(bctePrimaryKey);
                    if (ids == null) {
                        ids = new DwList<>();
                        btdpKeyToAssemblyListMap.put(bctePrimaryKey, ids);
                    }
                    ids.add(dataObjectBTDP.pseudoAssemblyId);

                    // DAIMLER-6809: Pro DIALOG-Position und Produkt alle Module bestimmen
                    iPartsDialogBCTEPrimaryKey dialogPosKey = bctePrimaryKey.getPositionBCTEPrimaryKey();
                    Map<iPartsProductId, Set<AssemblyId>> productAssemblyIdsMapForDialogPos = dialogPosToAssemblySetPerProductMap.get(dialogPosKey);
                    if (productAssemblyIdsMapForDialogPos == null) {
                        productAssemblyIdsMapForDialogPos = new HashMap<>();
                        dialogPosToAssemblySetPerProductMap.put(dialogPosKey, productAssemblyIdsMapForDialogPos);
                    }
                    Set<AssemblyId> productAssemblyIdsForDialogPos = productAssemblyIdsMapForDialogPos.get(dataObjectBTDP.productId);
                    if (productAssemblyIdsForDialogPos == null) {
                        productAssemblyIdsForDialogPos = new HashSet<>();
                        productAssemblyIdsMapForDialogPos.put(dataObjectBTDP.productId, productAssemblyIdsForDialogPos);
                    }

                    productAssemblyIdsForDialogPos.add(dataObjectBTDP.pseudoAssemblyId);
                }
            }

            //die gültigen Ausführungsarten pro Product aufsummieren
            for (iPartsDataProduct dataProduct : products.getProductList()) {
                Set<String> internAAList = new HashSet<>();
                for (iPartsDataProductModels dataProductModels : dataProduct.getProductModelsList()) {
                    iPartsDataModel dataModel = modelList.get(new iPartsModelId(dataProductModels.getAsId().getModelNumber()));
                    if (dataModel != null) {
                        String aa = dataModel.getFieldValue(FIELD_DM_AA);
                        if (!aa.isEmpty()) {
                            internAAList.add(aa);
                        }
                    }
                }
                products.aaMap.put(dataProduct.getAsId(), internAAList);
            }
        }
        return btdpKeyToAssemblyListMap;
    }

    /**
     * einen Stücklisteneintrag POSD hinzufügen
     *
     * @param importer
     * @param primaryPOSDKey
     * @param importData
     * @param extraPosDData
     * @param warnings       führen zu Ignorieren des Datensatzes
     * @param logMessages    werden auf Konsole des Loggers ausgegeben
     * @return
     */
    public boolean addPOSDValues(AbstractFilesImporter importer, iPartsDialogBCTEPrimaryKey primaryPOSDKey,
                                 iPartsDataDialogData importData, ExtraPosDImportData extraPosDData, List<String> warnings,
                                 List<String> logMessages) {
        warnings.clear();
        Map<iPartsDialogBCTEPrimaryKey, List<AssemblyId>> btdpKeyToAssemblyList = getBTDPKeyToAssemblyList();
        iPartsDialogBCTEPrimaryKey searchKey = primaryPOSDKey.cloneMe();
        searchKey.aa = "";
        List<AssemblyId> ids = btdpKeyToAssemblyList.get(searchKey);
        if (ids != null) {
            // DAIMLER-6809/DAIMLER-7041: assemblyIdsPerProductForSameDialogPos enthält pro Produkt alle Module zu derselben
            // DIALOG-Position
            Map<iPartsProductId, Set<AssemblyId>> assemblyIdsPerProductForSameDialogPos = dialogPosToAssemblySetPerProductMap.get(searchKey.getPositionBCTEPrimaryKey());
            Map<iPartsProductId, Set<AssemblyId>> validAssemblyIdsPerProductForSameDialogPos = null;
            String partNo = importData.getFieldValue(FIELD_DD_PARTNO);
            boolean isOmittedPart = omittedParts.isOmittedPart(partNo);

            // DAIMLER-7041: Alle Module in allen Produkten für dieselbe DIALOG-Position sind relevant bei Wegfallsachnummern
            if (isOmittedPart && (assemblyIdsPerProductForSameDialogPos != null)) {
                // Duplikat erzeugen, weil weiter unten Einträge entfernt werden
                validAssemblyIdsPerProductForSameDialogPos = new HashMap<>();
                for (Map.Entry<iPartsProductId, Set<AssemblyId>> assemblyIdsPerProduct : assemblyIdsPerProductForSameDialogPos.entrySet()) {
                    validAssemblyIdsPerProductForSameDialogPos.put(assemblyIdsPerProduct.getKey(), new TreeSet<>(assemblyIdsPerProduct.getValue()));
                }
            }

            // Für alle Module laut BTDP
            for (AssemblyId pseudoAssemblyId : ids) {
                DataObjectBTDP dataObjectBTDP = assemblies.getDataObjectBTDP(pseudoAssemblyId);
                if (assemblyIdsPerProductForSameDialogPos != null) {
                    Set<AssemblyId> assemblyIdsForProduct = assemblyIdsPerProductForSameDialogPos.get(dataObjectBTDP.productId);
                    if (assemblyIdsForProduct != null) {
                        // DAIMLER-6809/DAIMLER-7041: Es muss entweder in einem Produkt mehr als ein Modul geben für dieselbe
                        // DIALOG-Position oder es handelt sich um eine Wegfallsachnummer
                        if ((assemblyIdsForProduct.size() > 1) || isOmittedPart) {
                            if (validAssemblyIdsPerProductForSameDialogPos == null) {
                                validAssemblyIdsPerProductForSameDialogPos = new HashMap<>();
                            }

                            Set<AssemblyId> validAssemblyIdsForProduct = validAssemblyIdsPerProductForSameDialogPos.get(dataObjectBTDP.productId);
                            if (validAssemblyIdsForProduct == null) {
                                // Duplikat erzeugen, weil weiter unten Einträge entfernt werden
                                validAssemblyIdsForProduct = new TreeSet<>(assemblyIdsForProduct);
                                validAssemblyIdsPerProductForSameDialogPos.put(dataObjectBTDP.productId, validAssemblyIdsForProduct);
                            }

                            validAssemblyIdsForProduct.remove(pseudoAssemblyId); // Stücklisteneintrag wird regulär im Modul erzeugt
                        }
                    }
                }

                // Wurde ein Stücklisteneintrag mit leerem Hotspot entfernt?
                VarParam<Boolean> emptyHotspotRemoved = new VarParam<>(false);

                List<EtkDataPartListEntry> partListEntries = addPartListEntriesForPOSDValues(importer, primaryPOSDKey, importData,
                                                                                             extraPosDData, logMessages,
                                                                                             searchKey, pseudoAssemblyId,
                                                                                             false, emptyHotspotRemoved);

                if ((partListEntries != null) && isMergingProducts()) {
                    // Bei zusammengeführten Produkten alle Stücklisteneinträge als unterdrückt kennzeichnen, die nur
                    // in nicht veröffentlichten (unsichtbaren) Original-Produkten vorkommen
                    boolean onePartListEntryNotOmitted = false;
                    for (EtkDataPartListEntry partListEntry : partListEntries) {
                        Set<iPartsProductId> productIds = dataObjectBTDP.getOriginalProductForBCTEKeyAndHotspot(searchKey,
                                                                                                                partListEntry.getFieldValue(FIELD_K_POS));
                        if (productIds != null) {
                            if (!isAtLeastOneOriginalProductVisible(productIds)) {
                                // Stücklisteneintrag als unterdrückt kennzeichnen
                                partListEntry.setFieldValueAsBoolean(FIELD_K_OMIT, true, DBActionOrigin.FROM_EDIT);
                            } else {
                                onePartListEntryNotOmitted = true;
                            }
                        }
                    }

                    // Auch die Original-Produkte vom evtl. entfernten Stücklisteneintrag mit leerem Hotspot zusätzlich
                    // berücksichtigen falls nicht sowieso mindestens ein Stücklisteneintrag mit echtem Hotspot als nicht
                    // unterdrückt gekennzeichnet ist
                    if (!onePartListEntryNotOmitted && emptyHotspotRemoved.getValue()) {
                        Set<iPartsProductId> productIdsForEmptyHotspot = dataObjectBTDP.getOriginalProductForBCTEKeyAndHotspot(searchKey, "");
                        if (productIdsForEmptyHotspot != null) {
                            if (isAtLeastOneOriginalProductVisible(productIdsForEmptyHotspot)) {
                                // Alle Stücklisteneinträge nicht mehr als unterdrückt kennzeichnen, da es einen Stücklisteneintrag
                                // mit leerem Hotspot gab, der in einem veröffentlichten Original-Produkt vorkommt
                                for (EtkDataPartListEntry partListEntry : partListEntries) {
                                    partListEntry.setFieldValueAsBoolean(FIELD_K_OMIT, false, DBActionOrigin.FROM_EDIT);
                                }

                                if (partListEntries.size() > 1) {
                                    logMessages.add(importer.translateForLog("!!Keinen eindeutigen echten Hotspot aus einem retail-relevanten Produkt für den entfernten leeren Hotspot mit DIALOG-Schlüssel \"%1\" gefunden",
                                                                             searchKey.toString()));
                                }
                            }
                        }
                    }
                }
            }

            // DAIMLER-6809/DAIMLER-7041: Zusätzliche Stücklisteneinträge aus anderen Modulen für dieselbe DIALOG-Position hinzufügen,
            // diese aber nur im Baumuster-Filter berücksichtigen (gleiches Produkt wird schon in getBTDPKeyToAssemblyList()
            // geprüft)
            if (validAssemblyIdsPerProductForSameDialogPos != null) {
                for (Set<AssemblyId> validAssemblyIdsForProduct : validAssemblyIdsPerProductForSameDialogPos.values()) {
                    for (AssemblyId pseudoAssemblyId : validAssemblyIdsForProduct) {
                        addPartListEntriesForPOSDValues(importer, primaryPOSDKey, importData, extraPosDData, logMessages,
                                                        searchKey, pseudoAssemblyId, true, null);
                    }
                }
            }
        } else {
            warnings.add(importer.translateForLog("!!Kein Modul laut BTDP für den DIALOG-Schlüssel \"%1\" gefunden", searchKey.toString()));
        }
        return true;
    }

    private boolean isAtLeastOneOriginalProductVisible(Set<iPartsProductId> productIds) {
        // Ist mindestens eines der Produkte sichtbar/veröffentlicht?
        boolean productVisible = false;
        for (iPartsProductId productId : productIds) {
            // Produkt im catalogImportWorkerToMerge suchen, weil es sich ja um Original-Produkte handelt
            iPartsDataProduct dataProduct = catalogImportWorkerToMerge.products.getProduct(productId);
            if (dataProduct != null) {
                if (dataProduct.getFieldValueAsBoolean(FIELD_DP_PRODUCT_VISIBLE)) {
                    productVisible = true;
                    break;
                }
            }
        }
        return productVisible;
    }

    private List<EtkDataPartListEntry> addPartListEntriesForPOSDValues(AbstractFilesImporter importer, iPartsDialogBCTEPrimaryKey primaryPOSDKey,
                                                                       iPartsDataDialogData importData, ExtraPosDImportData extraPosDData,
                                                                       List<String> logMessages, iPartsDialogBCTEPrimaryKey searchKey,
                                                                       AssemblyId pseudoAssemblyId, boolean onlyModelFilter,
                                                                       VarParam<Boolean> emptyHotspotRemoved) {
        DataObjectBTDP dataObjectBTDP = assemblies.getDataObjectBTDP(pseudoAssemblyId);
        iPartsDataAssembly dataAssembly = dataObjectBTDP.dataAssembly;
        DataElemBTDP dataElemBTDP = dataObjectBTDP.btdpList.get(searchKey);
        Set<String> aaList = products.aaMap.get(dataObjectBTDP.productId);

        boolean doInsert = true;
        if (withAACheck) {
            if (!aaList.isEmpty()) {
                doInsert = aaList.contains(primaryPOSDKey.aa);
            } else {
                doInsert = false; // keine Ausführungsart gültig
            }
        }
        if (doInsert) {
            // Stücklistenposition wird erzeugt -> filter die Bildreferenzen für das Modul mit dem BCTE-Schlüssel
            // aus POSD. Hier muss der BCTE Schlüssel ohne AA genommen werden, da die Daten in BTDP keinen AA-Wert
            // haben. Die Referenzen werden gefiltert allein durch das Vorhandensein eines BCTE Schlüssels für ein
            // spezielles Modul (nicht, ob ein spezielles AA vorhanden ist).
            // DAIMLER-6809: Bei onlyModelFilter ist dieser Stücklisteneintrag laut BTDP eigentlich gar nicht in dieser
            // Stückliste -> für die Zeichnungen ignorieren
            if (!onlyModelFilter) {
                dataObjectBTDP.filterPictureReferenceForBCTEKey(searchKey);
            }
            //create partListEntry
            EtkDataPart part = importPartsData(importData, importer.getMessageLog(), importer.getLogLanguage());
            // In TU-Stückliste eintragen
            DBDataObjectList<EtkDataPartListEntry> destPartList = dataAssembly.getPartListUnfiltered(null, true, false);

            // Sequenznummer (= laufende Nummer) bestimmen
            int seqNr = destPartList.size() + 1;

            PartListEntryId destPartListEntryId = new PartListEntryId(dataAssembly.getAsId().getKVari(), dataAssembly.getAsId().getKVer(),
                                                                      EtkDbsHelper.formatLfdNr(seqNr));
            HmMSmId hmMSmId = searchKey.getHmMSmId();
            String sourceContext = EditConstructionToRetailHelper.createSourceContext(iPartsEntrySourceType.DIALOG, hmMSmId);
            EtkDataPartListEntry destPartListEntry = EditConstructionToRetailHelper.createMigrationRetailPartListEntry(sourceContext,
                                                                                                                       importData,
                                                                                                                       destPartListEntryId,
                                                                                                                       part,
                                                                                                                       iPartsModuleTypes.DialogRetail,
                                                                                                                       getProject(), logMessages);

            // Sequenznummer setzen. Auch ein Stringfeld kann als Integer gesetzt werden
            destPartListEntry.setFieldValueAsInteger(EtkDbConst.FIELD_K_SEQNR, seqNr, DBActionOrigin.FROM_EDIT);

            // Teilenummer setzen
            destPartListEntry.setFieldValue(FIELD_K_MATNR, part.getAsId().getMatNr(), DBActionOrigin.FROM_EDIT);

            // DAIMLER-5371 Teilepositionen mit ME != Stück (01) haben nach der Migration von DIALOG POSD die Menge "NB"
            EditModuleHelper.initMengeFromQuantunit(part, destPartListEntry);

            //Liste der betroffenen PartListEntries für Fußnoten und CombTexte
            List<EtkDataPartListEntry> affectedPartListEntryList = new ArrayList<>();
            affectedPartListEntryList.add(destPartListEntry);
            // Hotspot-Werte für alle Zeichnungen sortiert hinzufügen (die Hotspot-Werte sind vierstellig mit führenden Nullen)
            Map<String, String> hotspots = new HashMap<>();

            // DAIMLER-6809: Bei Stücklisteneinträgen, die aufgrund ihres BCTE-Schlüssels laut BTDP eigentlich gar nicht
            // in dieser Stückliste vorhanden sein sollten, ist dataElemBTDP null (in diesem Fall muss dann aber auch
            // onlyModelFilter true sein)
            if (dataElemBTDP != null) {
                for (Map<String, String> hotspotList : dataElemBTDP.pictureList.values()) {
                    for (Map.Entry<String, String> entry : hotspotList.entrySet()) {
                        String hotspot = StrUtils.removeLeadingCharsFromString(entry.getKey(), '0');
                        if (hotspots.get(hotspot) == null) {
                            hotspots.put(hotspot, entry.getValue());
                        }
                    }
                }

                // Bei zusammengeführten Produkten muss ein leerer Hotspot entfernt werden falls es mindestens einen echten
                // Hotspot gibt
                if (isMergingProducts() && (hotspots.size() > 1)) {
                    if (hotspots.remove("") != null) { // Leeren Hotspot entfernen -> es bleibt noch mindestens ein anderer echter übrig
                        if (emptyHotspotRemoved != null) {
                            emptyHotspotRemoved.setValue(true);
                        }
                    }
                }
            } else if (!onlyModelFilter) {
                // Kann eigentlich nicht passieren -> Meldung nur zur Sicherheit einbauen
                importer.getMessageLog().fireMessage(importer.translateForLog("!!BTDP-Informationen fehlen für das Modul \"%1\" und den DIALOG-Schlüssel \"%2\"",
                                                                              pseudoAssemblyId.getKVari(), searchKey.toString()),
                                                     MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
            String hotspotString;
            if (hotspots.size() > 0) {
                boolean isFirst = true;
                for (Map.Entry<String, String> entry : hotspots.entrySet()) {
                    hotspotString = entry.getKey();
                    if (isFirst) {
                        destPartListEntry.setFieldValue(FIELD_K_POS, hotspotString, DBActionOrigin.FROM_EDIT);
                        dataObjectBTDP.sortOrderList.put(destPartListEntry.getAsId(), entry.getValue());
                        destPartList.add(destPartListEntry, DBActionOrigin.FROM_EDIT);
                        isFirst = false;
                    } else {
                        // Sequenznummer setzen
                        seqNr++;
                        //neuen PartListEntry als Kopie des alten erzeugen
                        PartListEntryId newDestPartListEntryId = new PartListEntryId(dataAssembly.getAsId().getKVari(), dataAssembly.getAsId().getKVer(),
                                                                                     EtkDbsHelper.formatLfdNr(seqNr));
                        EtkDataPartListEntry newDestPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(), newDestPartListEntryId);
                        newDestPartListEntry.assignRecursively(getProject(), destPartListEntry, DBActionOrigin.FROM_EDIT);
                        newDestPartListEntry.setId(newDestPartListEntryId, DBActionOrigin.FROM_EDIT);
                        newDestPartListEntry.setDeleteOldId(false);
                        newDestPartListEntry.setFieldValueAsInteger(EtkDbConst.FIELD_K_SEQNR, seqNr, DBActionOrigin.FROM_EDIT);
                        newDestPartListEntry.setFieldValue(FIELD_K_POS, hotspotString, DBActionOrigin.FROM_EDIT);
                        dataObjectBTDP.sortOrderList.put(newDestPartListEntry.getAsId(), entry.getValue());
                        destPartList.add(newDestPartListEntry, DBActionOrigin.FROM_EDIT);
                        affectedPartListEntryList.add(newDestPartListEntry);
                    }
                }
            } else {
                hotspotString = "";
                destPartListEntry.setFieldValue(FIELD_K_POS, hotspotString, DBActionOrigin.FROM_EDIT);
                dataObjectBTDP.sortOrderList.put(destPartListEntry.getAsId(), hotspotString);
                destPartList.add(destPartListEntry, DBActionOrigin.FROM_EDIT);
            }

            //Fussnoten bestimmen
            BCTE_FootNote_Container container = bcteFootNoteIdList.get(primaryPOSDKey.createDialogGUID());
            if (container != null) {
                for (String footnoteId : container.footnoteIdList) {
                    for (EtkDataPartListEntry partListEntry : affectedPartListEntryList) {
                        // PartListEntryId hier bereits aufs Original normieren
                        PartListEntryId normPartListEntryId = new PartListEntryId(partListEntry.getAsId().getKVari(), "",
                                                                                  partListEntry.getAsId().getKLfdnr());
                        List<String> footnoteIdList = dataObjectBTDP.assemblyFootnoteList.get(normPartListEntryId);
                        if (footnoteIdList == null) {
                            footnoteIdList = new LinkedList<>();
                            dataObjectBTDP.assemblyFootnoteList.put(normPartListEntryId, footnoteIdList);
                        }
                        footnoteIdList.add(footnoteId);
                    }
                }
            }
            if (extraPosDData != null) {
                for (EtkDataPartListEntry partListEntry : affectedPartListEntryList) {
                    PartListEntryId normPartListEntryId = new PartListEntryId(partListEntry.getAsId().getKVari(), "",
                                                                              partListEntry.getAsId().getKLfdnr());
                    //CombTexte setzen
                    int combSeqNo = 1;
                    if (!StrUtils.isEmpty(extraPosDData.eTxtNumber)) {
                        EtkMultiSprache multi = new EtkMultiSprache();
                        multi.setTextId(extraPosDData.eTxtNumber);
                        iPartsDataCombText dataCombText = new iPartsDataCombText(getProject(), normPartListEntryId,
                                                                                 multi, null, combSeqNo);
                        dataObjectBTDP.combTextList.put(dataCombText.getAsId(), dataCombText);
                        combSeqNo++;
                    }
                    if (extraPosDData.multiSprache != null) {
                        iPartsDataCombText dataCombText = new iPartsDataCombText(getProject(), normPartListEntryId,
                                                                                 extraPosDData.multiSprache, null, combSeqNo);
                        dataObjectBTDP.combTextList.put(dataCombText.getAsId(), dataCombText);
                    }
                }
            }

            //Fehlerorte setzen
            iPartsDialogBCTEPrimaryKey searchFailKey = searchKey.cloneMe();
            searchFailKey.posV = "";
            searchFailKey.ww = "";
            searchFailKey.et = "";
            searchFailKey.sData = "";
            List<Fail_Location_Container> failList = getBCTE_FailLocationMap().get(searchFailKey);
            if ((failList != null) && !failList.isEmpty()) {
                String matNo = part.getAsId().getMatNr();
                for (Fail_Location_Container failLocation : failList) {
                    if (matNo.equals(failLocation.partNo)) {
                        for (EtkDataPartListEntry partListEntry : affectedPartListEntryList) {
                            String locElem = partListEntry.getFieldValue(FIELD_K_FAIL_LOCLIST);
                            if (!locElem.isEmpty()) {
                                locElem += ",";
                            }
                            locElem += failLocation.failLocation;
                            partListEntry.setFieldValue(FIELD_K_FAIL_LOCLIST, locElem, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
            }

            if (onlyModelFilter) {
                // DAIMLER-6809: Dieser Stücklisteneintrag soll nur im Baumuster-Filter verwendet werden -> entsprechende
                // Flags setzen und keine Ersetzungen erzeugen
                destPartListEntry.setFieldValueAsBoolean(FIELD_K_OMIT, true, DBActionOrigin.FROM_EDIT);
                destPartListEntry.setFieldValueAsBoolean(FIELD_K_ONLY_MODEL_FILTER, true, DBActionOrigin.FROM_EDIT);
            } else {
                // Ersetzungen suchen (1. Teil: OriginalTeile suchen und BCTE-Key in PartListEntryId wandeln)
                handleReplacementFirstPart(affectedPartListEntryList, searchKey);

                // Map für alle Stücklisteneinträge zu diesem BCTE-Schlüssel aufbauen
                List<EtkDataPartListEntry> partListEntriesForBCTEKey = bcteKeyToPartListEntriesMap.get(searchKey);
                if (partListEntriesForBCTEKey == null) {
                    partListEntriesForBCTEKey = new DwList<>();
                    bcteKeyToPartListEntriesMap.put(searchKey, partListEntriesForBCTEKey);
                }
                partListEntriesForBCTEKey.addAll(affectedPartListEntryList);

                // Und die Map für alle Stücklisteneinträge zu diesem BCTE-Schlüssel ohne Berücksichtigung von SData aufbauen
                iPartsDialogBCTEPrimaryKey searchKeyWithoutSData = searchKey.cloneMe();
                searchKeyWithoutSData.sData = "";
                partListEntriesForBCTEKey = bcteKeyWithoutSDataToPartListEntriesMap.get(searchKeyWithoutSData);
                if (partListEntriesForBCTEKey == null) {
                    partListEntriesForBCTEKey = new DwList<>();
                    bcteKeyWithoutSDataToPartListEntriesMap.put(searchKeyWithoutSData, partListEntriesForBCTEKey);
                }
                partListEntriesForBCTEKey.addAll(affectedPartListEntryList);
            }
            return affectedPartListEntryList;
        }

        return null;
    }

    private void setPEMEvaluation(EtkDataPart part, List<EtkDataPartListEntry> affectedPartListEntryList, iPartsDialogBCTEPrimaryKey searchKey,
                                  boolean searchWithoutSData) {
        iPartsDialogBCTEPrimaryKey searchKeyForMap = searchKey;
        if (searchWithoutSData) {
            searchKeyForMap = searchKey.cloneMe();
            searchKeyForMap.sData = "";
        }

        List<PEMEvaluationEntry> evalList = getBCTE_PEMEvaluationList().get(searchKeyForMap);
        if ((evalList != null) && !evalList.isEmpty()) {
            String matNo = part.getAsId().getMatNr();
            for (PEMEvaluationEntry evalItem : evalList) {
                if (matNo.equals(evalItem.getMatNo())) {
                    for (EtkDataPartListEntry partListEntry : affectedPartListEntryList) {
                        // Flags nur aktiv auf true setzen (false sind sie per Default ja sowieso und bei mehreren
                        // PEMEvaluationEntries pro Stücklisteneintrag würden die Flags ansonsten im Worst
                        // Case wieder zurückgesetzt werden)
                        // Flags nicht bei identischem Vorgänger- bzw. Nachfolger-BCTE-Schlüssel setzen (denn das wäre eine
                        // Ersetzung auf sich selbst, wobei Pseudo-Ersetzungen mit nicht vorhandenem PredecessorBCTEKey
                        // und SuccessorBCTEKey immer durchgehen); hier muss mit searchKey verglichen werden, da nur dieser
                        // auch das SData enthält

                        // "PEM ab auswerten" wird am Nachfolger gesetzt -> Vergleich vom Vorgänger-BCTE-Schlüssel bzgl.
                        // Ersetzung auf sich selbst (das wäre ungültig)
                        iPartsDialogBCTEPrimaryKey predecessorBCTEKey = evalItem.getPredecessorBCTEKey();
                        if (evalItem.isEvalPEMFrom() && !Utils.objectEquals(searchKey, predecessorBCTEKey)) {
                            boolean predecessorFound;
                            if (predecessorBCTEKey != null) {
                                // Prüfen, ob es überhaupt einen Vorgänger in dieser Stückliste gibt. Falls nicht, dann
                                // dieses "PEM ab auswerten" nicht berücksichtigen.
                                if (!evalItem.isWithSDA()) { // Unterscheidung, ob ohne oder mit SDA geprüft werden muss
                                    predecessorFound = hasPredecessorOrSuccessor(partListEntry, predecessorBCTEKey.getPositionBCTEPrimaryKeyWithoutSDA(),
                                                                                 bcteKeyWithoutSDataToPartListEntriesMap);
                                } else {
                                    predecessorFound = hasPredecessorOrSuccessor(partListEntry, predecessorBCTEKey, bcteKeyToPartListEntriesMap);
                                }
                            } else {
                                predecessorFound = true; // Pseudo-Ersetzung hat keinen BCTE-Schlüssel sondern nur Material
                            }

                            if (predecessorFound) {
                                partListEntry.setFieldValueAsBoolean(FIELD_K_EVAL_PEM_FROM, true, DBActionOrigin.FROM_EDIT);
                            }
                        }

                        // "PEM bis auswerten" wird am Vorgänger gesetzt -> Vergleich vom Nachfolger-BCTE-Schlüssel bzgl.
                        // Ersetzung auf sich selbst (das wäre ungültig)
                        iPartsDialogBCTEPrimaryKey successorBCTEKey = evalItem.getSuccessorBCTEKey();
                        if (evalItem.isEvalPEMTo() && !Utils.objectEquals(searchKey, successorBCTEKey)) {
                            boolean successorFound;
                            if (successorBCTEKey != null) {
                                // Prüfen, ob es überhaupt einen Nachfolger in dieser Stückliste gibt. Falls nicht, dann
                                // dieses "PEM bis auswerten" nicht berücksichtigen.
                                if (!evalItem.isWithSDA()) { // Unterscheidung, ob ohne oder mit SDA geprüft werden muss
                                    successorFound = hasPredecessorOrSuccessor(partListEntry, successorBCTEKey.getPositionBCTEPrimaryKeyWithoutSDA(),
                                                                               bcteKeyWithoutSDataToPartListEntriesMap);
                                } else {
                                    successorFound = hasPredecessorOrSuccessor(partListEntry, successorBCTEKey, bcteKeyToPartListEntriesMap);
                                }
                            } else {
                                successorFound = true; // Pseudo-Ersetzung hat keinen BCTE-Schlüssel sondern nur Material
                            }

                            if (successorFound) {
                                partListEntry.setFieldValueAsBoolean(FIELD_K_EVAL_PEM_TO, true, DBActionOrigin.FROM_EDIT);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean hasPredecessorOrSuccessor(EtkDataPartListEntry partListEntry, iPartsDialogBCTEPrimaryKey searchBCTEKey,
                                              Map<iPartsDialogBCTEPrimaryKey, List<EtkDataPartListEntry>> bcteKeyToPartListEntriesMap) {
        List<EtkDataPartListEntry> possiblePredecessorsOrSuccessors = bcteKeyToPartListEntriesMap.get(searchBCTEKey);
        if (possiblePredecessorsOrSuccessors != null) {
            String partListEntryKLfdnr = partListEntry.getAsId().getKLfdnr();
            for (EtkDataPartListEntry possiblePredecessorOrSuccessor : possiblePredecessorsOrSuccessors) {
                if (possiblePredecessorOrSuccessor.getOwnerAssemblyId().equals(partListEntry.getOwnerAssemblyId())
                    && !possiblePredecessorOrSuccessor.getAsId().getKLfdnr().equals(partListEntryKLfdnr)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Ersetzungen suchen (1. Teil): OriginalTeile suchen und BCTE-Key in PartListEntryId wandeln
     *
     * @param affectedPartListEntryList
     * @param searchKey
     */
    private void handleReplacementFirstPart(List<EtkDataPartListEntry> affectedPartListEntryList, iPartsDialogBCTEPrimaryKey searchKey) {
        // searchKey ist ohne AA
        setPartListEntryIdInReplaceElems(affectedPartListEntryList, searchKey);

        // secondSearchKey ist ohne AA und ohne sData (für Ersetzungen mit leerem RPOS_VSDA, die sich auf alle sData vom
        // Vorgänger beziehen)
        iPartsDialogBCTEPrimaryKey secondSearchKey = searchKey.cloneMe();
        secondSearchKey.sData = "";
        setPartListEntryIdInReplaceElems(affectedPartListEntryList, secondSearchKey);
    }

    /**
     * StüLi-DIALOG-Key in der Replacement-Map suchen, mit MatNo vergleichen und PartListId merken
     *
     * @param affectedPartListEntryList
     * @param searchKey
     * @return
     */
    private boolean setPartListEntryIdInReplaceElems(List<EtkDataPartListEntry> affectedPartListEntryList, iPartsDialogBCTEPrimaryKey searchKey) {
        boolean result = false;
        Collection<Replace_Elem> replaceList = getBCTE_ReplaceMap().get(searchKey);
        if ((replaceList != null) && !replaceList.isEmpty()) {
            for (Replace_Elem replaceElem : replaceList) {
                for (EtkDataPartListEntry partListEntry : affectedPartListEntryList) {
                    if (partListEntry.getPart().getAsId().getMatNr().equals(replaceElem.originalEntry.matNo)) {
                        replaceElem.originalEntry.addPartListEntry(partListEntry, getPartListEntryIdAAMap());
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gewünschtes Material suchen und ggf. anlegen.
     *
     * @param importData
     * @param messageLog
     * @param logLanguage
     * @return
     */
    private EtkDataPart importPartsData(iPartsDataDialogData importData, EtkMessageLog messageLog, String logLanguage) {
        String partNumber = importData.getFieldValue(FIELD_DD_PARTNO);

        PartId partId = new PartId(partNumber, "");
        EtkDataPart part = partsDone.get(partId);
        if (part == null) {
            part = EtkDataObjectFactory.createDataPart(getProject(), partId);
            if (!part.existsInDB()) {
                // Nur, falls das Teil noch nicht da ist anlegen
                part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

                // Handelt es sich um eine SMART QV Sachnummer mit Farbe (ES2) oder eine normale Sachnummer mit ES1 und/oder ES2 Schlüssel
                DIALOGImportHelper.handleESKeysInDataPart(getProject(), part, messageLog, logLanguage);

                // Bestellnummer setzen
                part.setFieldValue(FIELD_M_BESTNR, part.getAsId().getMatNr(), DBActionOrigin.FROM_EDIT);

                // Quelle setzen
                part.addSetOfEnumValueToFieldValue(FIELD_M_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);

                //keine Beschreibung oder TextId vorhanden
                part.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.FALSE);
            } else {
                // zur Sicherheit: Bestellnummer setzen
                part.setFieldValue(FIELD_M_BESTNR, part.getAsId().getMatNr(), DBActionOrigin.FROM_EDIT);
            }
            partsDone.put(partId, part);
        }
        return part;
    }

    /**
     * StüLi-Entries finden, die dem Replace-DIALOG-Key und der Replace-MatNo entsprechen
     *
     * @param replaceBCTEKey
     * @param replaceMatNo
     * @param bctePartListEntryReplaceMap
     * @param bctePartListEntrySDataReplaceMap
     * @return
     */
    private List<EtkDataPartListEntry> findBCTEKeyForReplacement(iPartsDialogBCTEPrimaryKey replaceBCTEKey, String replaceMatNo,
                                                                 Map<iPartsDialogBCTEPrimaryKey, List<EtkDataPartListEntry>> bctePartListEntryReplaceMap,
                                                                 Map<iPartsDialogBCTEPrimaryKey, List<EtkDataPartListEntry>> bctePartListEntrySDataReplaceMap) {
        // Result-Liste
        List<EtkDataPartListEntry> partListEntryList;
        if (replaceBCTEKey.sData.isEmpty()) {
            // DAIMLER-3449 AK1: Die Prüfung der Ersetzung erfolgt für alle Änderungsstände der Stückliste (ohne SDA-Abgleich)
            // Replace-DIALOG-Key in Liste ohne AA+sData suchen
            partListEntryList = bctePartListEntrySDataReplaceMap.get(replaceBCTEKey);
        } else {
            // DAIMLER-6782: Aufbau der Ersetzung muss teilweise auf genau einen Nachfolgerstand eingeschränkt werden
            // Replace-DIALOG-Key in Liste ohne AA suchen
            partListEntryList = bctePartListEntryReplaceMap.get(replaceBCTEKey);
        }

        List<EtkDataPartListEntry> resultPartListEntryList = new DwList<>();
        // gab es Treffer?
        if (partListEntryList != null) {
            // Überprüfung der Replace-MatNo
            for (EtkDataPartListEntry partListEntry : partListEntryList) {
                // DAIMLER-6809: Wenn dieser Stücklisteneintrag nur im Baumuster-Filter verwendet werden soll -> keine Ersetzungen
                // erzeugen
                if (partListEntry.getPart().getAsId().getMatNr().equals(replaceMatNo) && !partListEntry.getFieldValueAsBoolean(FIELD_K_ONLY_MODEL_FILTER)) {
                    resultPartListEntryList.add(partListEntry);
                }
            }
        }
        return resultPartListEntryList;
    }

    /**
     * Aufbau 2er Maps mit den DIALOG-Keys der PartListEntries (ohne AA und ohne AA + sDdata)
     * zusätzlich nach den Mitlieferteil-Positionen suchen und merken
     *
     * @param partListEntries
     * @param bctePartListEntryReplaceMap
     * @param bctePartListEntrySDataReplaceMap
     * @param includeElemsList
     */
    private void fillBCTEPartListEntryIdsForAssembly(DBDataObjectList<EtkDataPartListEntry> partListEntries,
                                                     Map<iPartsDialogBCTEPrimaryKey, List<EtkDataPartListEntry>> bctePartListEntryReplaceMap,
                                                     Map<iPartsDialogBCTEPrimaryKey, List<EtkDataPartListEntry>> bctePartListEntrySDataReplaceMap,
                                                     Collection<Include_Elem> includeElemsList) {
        bctePartListEntryReplaceMap.clear();
        bctePartListEntrySDataReplaceMap.clear();
        // über alle StüLi-Entries
        for (EtkDataPartListEntry partListEntry : partListEntries) {
            // DAIMLER-6809: Dieser Stücklisteneintrag soll nur im Baumuster-Filter verwendet werden -> keine Ersetzungen
            // erzeugen
            if (partListEntry.getFieldValueAsBoolean(FIELD_K_ONLY_MODEL_FILTER)) {
                continue;
            }

            // den StüLi DIALOG-Key bestimmen
            iPartsDialogBCTEPrimaryKey sourceBCTEKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(partListEntry.getFieldValue(FIELD_K_SOURCE_GUID));
            if (sourceBCTEKey != null) {
                // DIALOG-Key ohne AA
                sourceBCTEKey.aa = "";

                // In Map merken
                List<EtkDataPartListEntry> partListEntryList = bctePartListEntryReplaceMap.get(sourceBCTEKey);
                if (partListEntryList == null) {
                    partListEntryList = new DwList<>();
                    bctePartListEntryReplaceMap.put(sourceBCTEKey, partListEntryList);
                }
                partListEntryList.add(partListEntry);

                // gibt es für diese StüLi Mitlieferteile?
                if (!includeElemsList.isEmpty()) {
                    String matNo = partListEntry.getPart().getAsId().getMatNr();
                    // MatNr überprüfen und LfdNr des PartListEntries merken
                    for (Include_Elem includeElem : includeElemsList) {
                        if (includeElem.includeElemKey.predecessorMatNo.equals(matNo)) {
                            includeElem.addPredecessorPartListEntryId(partListEntry.getAsId());
                        }
                    }
                }

                sourceBCTEKey = sourceBCTEKey.cloneMe();
                // DIALOG-Key ohne AA und sData
                sourceBCTEKey.sData = "";

                // in 2. Map ohne SDATA merken (reicht laut DAIMLER-3449 AK1 aus)
                partListEntryList = bctePartListEntrySDataReplaceMap.get(sourceBCTEKey);
                if (partListEntryList == null) {
                    partListEntryList = new DwList<>();
                    bctePartListEntrySDataReplaceMap.put(sourceBCTEKey, partListEntryList);
                }
                partListEntryList.add(partListEntry);
            }
        }
    }

    /**
     * Liste der Ersetungen-Datensätze für das übergebene Modul bestimmen
     *
     * @param assemblyId
     * @param partListEntries
     * @return
     */
    private iPartsDataReplacePartList createReplacementPartsList(AssemblyId assemblyId, DBDataObjectList<EtkDataPartListEntry> partListEntries) {
        // zum Speichern der iPartsDataReplacePart
        iPartsDataReplacePartList dataReplacePartList = new iPartsDataReplacePartList();
        // Liste der betroffenen Replace_Elems in diesem Modul
        Set<Replace_Elem> replaceElemsList = new LinkedHashSet<>();
        // Liste der betroffenen Include_Elems in diesem Modul
        Set<Include_Elem> includeElemsSet = new HashSet<>();
        // beide Listen besetzen
        fillReplacementsForAssembly(assemblyId, replaceElemsList, includeElemsSet);
        // 2 Listen mit DIALOG-Key des StüLi-Entries ohne AA und ohne (AA und sData)
        Map<iPartsDialogBCTEPrimaryKey, List<EtkDataPartListEntry>> bctePartListEntryReplaceMap = new HashMap<>();
        Map<iPartsDialogBCTEPrimaryKey, List<EtkDataPartListEntry>> bctePartListEntrySDataReplaceMap = new HashMap<>();
        fillBCTEPartListEntryIdsForAssembly(partListEntries, bctePartListEntryReplaceMap, bctePartListEntrySDataReplaceMap, includeElemsSet);
        // die seqNo der Replacement-Einträge muss in Abhängikeit von der PartListEntryId gesteuert werden (da in einem Modul reicht k_LfdNr,
        // weil es in RPos auch mehrere Ersetzungen pro DIALOG-Schlüssel gibt
        Map<String, Integer> replaceSeqNoMap = new HashMap<>();
        // über alle in diesem Modul betroffenen Replace_Elems
        for (Replace_Elem replaceElem : replaceElemsList) {
            // StüLi-Entries finden, die die Replace MatNo enthalten
            List<EtkDataPartListEntry> replacementEntriesList = findBCTEKeyForReplacement(replaceElem.replaceEntry.dialogBCTEKey,
                                                                                          replaceElem.replaceEntry.matNo,
                                                                                          bctePartListEntryReplaceMap,
                                                                                          bctePartListEntrySDataReplaceMap);
            if (!replacementEntriesList.isEmpty()) {
                // speichern der Treffer
                fillReplaceDataPart(replaceElem, replacementEntriesList, assemblyId, replaceSeqNoMap, dataReplacePartList);
            }
        }

        return dataReplacePartList;
    }

    /**
     * Erzeuge iPartsDataReplacePart aus den Angaben des Replace_Elems für die übergebene Assembly und speichere sie in
     * dataReplacePartList
     *
     * @param replaceElem
     * @param replacementEntriesList
     * @param assemblyId
     * @param dataReplacePartList
     */
    private void fillReplaceDataPart(Replace_Elem replaceElem, List<EtkDataPartListEntry> replacementEntriesList, AssemblyId assemblyId,
                                     Map<String, Integer> replaceSeqNoMap, iPartsDataReplacePartList dataReplacePartList) {
        // über allen betroffenen StüLi-Entries
        for (EtkDataPartListEntry replacementEntry : replacementEntriesList) {
            // Umsetzung Replace-DIALOG-Key nach StüLi-Id setzen
            replaceElem.replaceEntry.addPartListEntry(replacementEntry, getPartListEntryIdAAMap());
            // Einträge des replaceElems in DB speichern
            for (PartListEntryId orgPartListEntryId : replaceElem.originalEntry.partListEntryIdList) {
                // Nur die Stücklisteneinträge der übergebenen Assembly zur Liste hinzufügen und keine Ersetzungen auf sich
                // selbst erzeugen
                if (orgPartListEntryId.getOwnerAssemblyId().equals(assemblyId) && !orgPartListEntryId.getKLfdnr().equals(replacementEntry.getAsId().getKLfdnr())) {
                    // Es dürfen nur Ersetzungen zwischen Stücklisteneinträgen mit gleicher AA aufgebaut werden
                    String orgPartlistEntryAA = getPartListEntryIdAAMap().get(orgPartListEntryId); // Map um DB Zugriff zu sparen
                    String replacementEntryAA = replacementEntry.getFieldValue(iPartsConst.FIELD_K_AA);
                    if (StrUtils.isValid(orgPartlistEntryAA, replacementEntryAA) && orgPartlistEntryAA.equals(replacementEntryAA)) {

                        // Laufende Nummer für die Ersetzung pro Stücklisteneintrag bestimmen
                        Integer lastSeqNo = replaceSeqNoMap.get(orgPartListEntryId.getKLfdnr());
                        if (lastSeqNo == null) {
                            lastSeqNo = 1;
                        } else {
                            lastSeqNo++;
                        }
                        replaceSeqNoMap.put(orgPartListEntryId.getKLfdnr(), lastSeqNo);
                        int seqNo = lastSeqNo;

                        iPartsReplacePartId originalId = new iPartsReplacePartId(orgPartListEntryId.getKVari(), "", orgPartListEntryId.getKLfdnr(), seqNo);
                        iPartsDataReplacePart dataReplacePart = new iPartsDataReplacePart(getProject(), originalId);
                        dataReplacePart.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        dataReplacePart.setFieldValue(FIELD_DRP_REPLACE_MATNR, replaceElem.replaceEntry.matNo, DBActionOrigin.FROM_EDIT);
                        dataReplacePart.setFieldValue(FIELD_DRP_REPLACE_LFDNR, replacementEntry.getAsId().getKLfdnr(), DBActionOrigin.FROM_EDIT);
                        dataReplacePart.setFieldValue(FIELD_DRP_REPLACE_RFMEA, replaceElem.entryRFMEA, DBActionOrigin.FROM_EDIT);
                        dataReplacePart.setFieldValue(FIELD_DRP_REPLACE_RFMEN, replaceElem.entryRFMEN, DBActionOrigin.FROM_EDIT);
                        dataReplacePart.setFieldValue(FIELD_DRP_SOURCE, iPartsReplacement.Source.MAD.getDbValue(), DBActionOrigin.FROM_EDIT);
                        dataReplacePart.setFieldValue(FIELD_DRP_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
                        dataReplacePartList.add(dataReplacePart, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
        }
    }

    /**
     * zentrale Routine für den Modulvergleich, wird am Ende des POSD-Importers aufgerufen
     *
     * @param importer
     * @return
     */
    public boolean compareAssemblies(AbstractFilesImporter importer) {
        // Flags für "PEM AB/BIS relevant für den Endnummern-Filter" an allen Stücklisteneinträgen setzen
        // Das ist erst jetzt möglich, weil pro Ersetzung (für eine "PEM AB/BIS relevant"-Änderung) geprüft werden muss,
        // ob in der jeweiligen Stückliste auch wirklich mindestens ein Vorgänger bzw. Nachfolger vorhanden ist.
        for (Map.Entry<iPartsDialogBCTEPrimaryKey, List<EtkDataPartListEntry>> bcteKeyToPartListEntriesEntry : bcteKeyToPartListEntriesMap.entrySet()) {
            List<EtkDataPartListEntry> affectedPartListEntryList = bcteKeyToPartListEntriesEntry.getValue();
            if (!affectedPartListEntryList.isEmpty()) {
                iPartsDialogBCTEPrimaryKey searchKey = bcteKeyToPartListEntriesEntry.getKey();
                EtkDataPart part = affectedPartListEntryList.get(0).getPart(); // Die Teile sind alle identisch, nimm das erste

                // Flags für "PEM AB/BIS relevant für den Endnummern-Filter" für ECHTE und PSEUDO-Ersetzungen
                // searchKey ist ohne AA
                setPEMEvaluation(part, affectedPartListEntryList, searchKey, false);

                // Zweite Suche ist ohne AA und ohne sData (für Ersetzungen mit leerem RPOS_VSDA, die sich auf alle
                // sData vom Vorgänger beziehen)
                setPEMEvaluation(part, affectedPartListEntryList, searchKey, true);
            }
        }

        clearBCTE_AssemblyMaps(); // Etwas Speicher freigeben

        // Liste der Module, bei denen es Ersetzungen gibt
        Set<AssemblyId> replaceAssemblySet = getAssembliesFromReplaceMap();
        int assemblyCount = assemblies.getAssemblyList().size();
        importer.getMessageLog().fireMessage(importer.translateForLog("!!%1 Module werden verglichen", String.valueOf(assemblyCount)),
                                             MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        importer.getMessageLog().fireProgress(0, assemblyCount, "", true, false);

        int counter = 0;
        int saveCounter = 0;
        int saveReplacementsCounter = 0;
        for (DataObjectBTDP dataObjectBTDP : assemblies.getAssemblyList()) {
            if (Thread.currentThread().isInterrupted()) {
                importer.cancelImport("!!Import-Thread wurde frühzeitig beendet");
                return false;
            }
            dataObjectBTDP.dataAssembly.recalcSeqNrForDaimlerDIALOGImport(dataObjectBTDP.sortOrderList);

            // Wahlweise-Werte konvertieren
            DBDataObjectList<EtkDataPartListEntry> partListUnfiltered = dataObjectBTDP.dataAssembly.getPartListUnfiltered(null, true, false);
            EditConstructionToRetailHelper.convertWWValuesForDIALOGPartListEntries(partListUnfiltered);

            iPartsAssemblyId normAssemblyId = new iPartsAssemblyId(dataObjectBTDP.dataAssembly.getAsId().getKVari(), "");
            boolean isModuleNewOrDeleted = false;
            if (dataObjectBTDP.isNew) {
                //Module ist neu => Speichern (kVari ist bereits richtig) mit Verortung
                if (!partListUnfiltered.isEmpty()) {
                    saveModule(importer, dataObjectBTDP, true);
                    saveCounter++;
                    isModuleNewOrDeleted = true;
                } else {
                    continue; // Modul ist leer und hat bisher auch nicht existiert -> überspringen
                }
            } else {
                // Stammdaten des Moduls aktualisieren. 
                // Die Dokumethode wird hier nicht gesetzt, die wird später über die Synchronisierung ergänzt.
                iPartsDataModule dataModule = dataObjectBTDP.dataAssembly.getModuleMetaData();
                dataModule.setFieldValueAsBoolean(FIELD_DM_SPRING_FILTER, dataObjectBTDP.isSpringFilterRelevant(), DBActionOrigin.FROM_EDIT);
                dataModule.saveToDB(true, DBDataObject.PrimaryKeyExistsInDB.CHECK);

                // Verortung des Moduls überprüfen und notfalls korrigieren
                if ((dataObjectBTDP.kgTuId != null) && (dataObjectBTDP.kgTuId.isValidId())) {
                    String newKg = dataObjectBTDP.kgTuId.getKg();
                    String newTu = dataObjectBTDP.kgTuId.getTu();
                    String moduleNumber = normAssemblyId.getKVari();
                    iPartsDataModuleEinPAS oldDataModuleEinPAS = products.moduleAssignmentMap.get(moduleNumber);
                    if (oldDataModuleEinPAS != null) {
                        String oldKg = oldDataModuleEinPAS.getFieldValue(FIELD_DME_SOURCE_KG);
                        String oldTu = oldDataModuleEinPAS.getFieldValue(FIELD_DME_SOURCE_TU);
                        if (!oldKg.equals(newKg) || !oldTu.equals(newTu)) {
                            // Verortung ist unterschiedlich -> korrigieren
                            oldDataModuleEinPAS.setFieldValue(FIELD_DME_SOURCE_KG, newKg, DBActionOrigin.FROM_EDIT);
                            oldDataModuleEinPAS.setFieldValue(FIELD_DME_SOURCE_TU, newTu, DBActionOrigin.FROM_EDIT);
                            importer.getMessageLog().fireMessage(importer.translateForLog("!!KG/TU-Verortung vom Modul '%1' wurde korrigiert von %2/%3 zu %4/%5",
                                                                                          moduleNumber, oldKg, oldTu,
                                                                                          newKg, newTu),
                                                                 MessageLogType.tmlWarning,
                                                                 MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                        }
                    } else {
                        // Verortung fehlt -> korrigieren
                        iPartsDataModuleEinPAS dataModuleEinPAS = createModuleAssignment(dataObjectBTDP);
                        if (dataModuleEinPAS != null) {
                            products.moduleAssignmentMap.put(moduleNumber, dataModuleEinPAS);
                            importer.getMessageLog().fireMessage(importer.translateForLog("!!Fehlende KG/TU-Verortung vom Modul '%1' wurde korrigiert zu %2/%3",
                                                                                          moduleNumber, newKg, newTu),
                                                                 MessageLogType.tmlWarning,
                                                                 MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                        }
                    }
                }

                // in DB existierendes Modul laden
                EtkDataAssembly dataAssembly = EtkDataObjectFactory.createDataAssembly(getProject(), normAssemblyId, false);
                iPartsDataAssembly existingDataAssembly = (iPartsDataAssembly)dataAssembly;
                existingDataAssembly.loadFromDB(normAssemblyId);
                //Vorbesetzung für Meldung bei Ungleichheit
                this.importer = importer;
                clearModificationReason();
                //Vergleich der beiden Module
                if (assembliesAreEqual(dataObjectBTDP.dataAssembly, existingDataAssembly)) {
                    //compare Footnotes
                    compareFootNotes(importer, dataObjectBTDP);
                    //compare Combined Texte
                    compareCombText(importer, dataObjectBTDP);
                    //sind beide identisch, dann muss nichts mehr getan werden
                    dataObjectBTDP.dataAssembly = null;
                } else {
                    // DM_VARIANTS_VISIBLE vom bereits existierenden Modul übernehmen, oder den Default setzen.
                    iPartsDataModule existingDataModule = existingDataAssembly.getModuleMetaData();
                    dataObjectBTDP.setIsVariantsVisible((existingDataModule != null) ? existingDataModule.isVariantsVisible() : iPartsDataModule.DM_VARIANTS_VISIBLE_DEFAULT);

                    existingDataAssembly.delete_iPartsAssembly(false);

                    if (!getModificationReason().isEmpty()) {
                        importer.getMessageLog().fireMessage("  - " + getModificationReason(),
                                                             MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                    }
                    //bei neu kreiertem Modul die ID umsetzen und speichern
                    resetIdAndSave(importer, dataObjectBTDP);
                    saveCounter++;
                    isModuleNewOrDeleted = true;
                }
            }

            // Bei zusammengeführten Produkten ein Modul auf unsichtbar setzen, wenn dieses nur unterdrückte Stücklisteneinträge
            // enthält
            if (isModuleNewOrDeleted && isMergingProducts()) {
                boolean hideModule = true;

                // Gibt es mindestens einen nicht unterdrückten Stücklisteneintrag?
                for (EtkDataPartListEntry partListEntry : dataObjectBTDP.dataAssembly.getPartListUnfiltered(null)) {
                    if (!partListEntry.getFieldValueAsBoolean(FIELD_K_OMIT)) {
                        hideModule = false;
                        break;
                    }
                }

                iPartsDataModule moduleMetaData = dataObjectBTDP.dataAssembly.getModuleMetaData();
                moduleMetaData.setFieldValueAsBoolean(FIELD_DM_MODULE_HIDDEN, hideModule, DBActionOrigin.FROM_EDIT);
                moduleMetaData.saveToDB();
            }

            // Besitzt das Modul Ersetzungen?
            if (replaceAssemblySet.contains(normAssemblyId)) {
                boolean hasReplacementDiff = false;

                // Ersetzungen bestimmen
                iPartsDataReplacePartList replacePartList = createReplacementPartsList(normAssemblyId, partListUnfiltered);
                iPartsDataReplacePartList existingReplacePartList = null;
                if (!isModuleNewOrDeleted) {
                    existingReplacePartList = iPartsDataReplacePartList.loadReplacementsForAssembly(project, normAssemblyId);
                }

                // Ist das Modul neu bzw. gelöscht worden oder sind die Ersetzungen identisch?
                if (isModuleNewOrDeleted || !replacePartList.isTheSame(existingReplacePartList, true)) {
                    if (!isModuleNewOrDeleted) {
                        // Alte Ersetzungen des Moduls löschen
                        iPartsDataReplacePartList.deleteReplacementsForAssembly(getProject(), normAssemblyId);
                    }
                    //debugPrintReplaceParts();
                    // Neu erzeugte Ersetzungen des Moduls speichern
                    replacePartList.saveToDB(getProject(), false);
                    hasReplacementDiff = true;
                }

                // Mitlieferteile bestimmen
                iPartsDataIncludePartList includePartList = createIncludePartsList(normAssemblyId, importer);
                iPartsDataIncludePartList existingIncludePartList = null;
                if (!isModuleNewOrDeleted) {
                    existingIncludePartList = iPartsDataIncludePartList.loadIncludePartsForAssembly(project, normAssemblyId);
                }

                // Ist das Modul neu bzw. gelöscht worden oder sind die Mitlieferteile identisch?
                if (isModuleNewOrDeleted || !includePartList.isTheSame(existingIncludePartList, true)) {
                    if (!isModuleNewOrDeleted) {
                        // Alte Mitlieferteile des Moduls löschen
                        iPartsDataIncludePartList.deleteIncludePartsForAssembly(getProject(), normAssemblyId);
                    }

                    // Neu erzeugte Mitlieferteile des Moduls speichern
                    includePartList.saveToDB(getProject(), false);
                    hasReplacementDiff = true;
                }

                if (hasReplacementDiff) {
                    saveReplacementsCounter++;
                }
            } else if (!isModuleNewOrDeleted) { // Ersetzungen wurden sowieso schon gelöscht bzw. Modul hat noch gar nicht existiert
                // Keine Ersetzungen im Modul -> alte Ersetzungen und Mitlieferteile des Moduls löschen
                iPartsDataReplacePartList.deleteReplacementsForAssembly(getProject(), normAssemblyId);
                iPartsDataIncludePartList.deleteIncludePartsForAssembly(getProject(), normAssemblyId);
            }

            counter++;
            importer.getMessageLog().fireProgress(counter, assemblyCount, "", true, true);
        }
        //debugPrintReplaceParts();
        importer.getMessageLog().hideProgress();
        importer.getMessageLog().fireMessage(importer.translateForLog("!!Es wurden für %1 Module Ersetzungen und Mitlieferteile gespeichert",
                                                                      String.valueOf(saveReplacementsCounter)),
                                             MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        importer.getMessageLog().fireMessage(importer.translateForLog("!!Es wurden %1 Module gespeichert", String.valueOf(saveCounter)),
                                             MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        //Platz schaffen
        assemblies.textIdHelper.clearCache();
        assemblies.dictMultiLangList = null;
        return true;
    }

    /**
     * Startet die Synchronisierung aller Bildreferenzen
     *
     * @param importer
     */
    public void syncPicReferences(AbstractFilesImporter importer) {
        if (!picReferenceIdsForModule.isEmpty()) {
            importer.getMessageLog().fireMessage(MQPicScheduler.getInstance().createStartRetrievingImagesLogMessage(picReferenceIdsForModule.size(), importer),
                                                 MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            // Bildreferenzen werden in Abhängigkeit von der Modulnummer gehalten. Sammel alle Bildreferenzen und
            // übergebe sie als ein großes Set
            Set<iPartsPicReferenceId> allPicReferences = new LinkedHashSet<>();
            for (Set<iPartsPicReferenceId> picReferencesSet : picReferenceIdsForModule.values()) {
                allPicReferences.addAll(picReferencesSet);
            }
            MQPicScheduler.getInstance().startRetrievingImages(allPicReferences, getProject(), importer.getMessageLog());
            picReferenceIdsForModule.clear();
        }
    }

    /**
     * Liste der Mitlieferteile-Datensätze für das übergebene Modul bestimmen
     *
     * @param assemblyId
     * @param importer
     * @return
     */
    private iPartsDataIncludePartList createIncludePartsList(AssemblyId assemblyId, AbstractFilesImporter importer) {
        iPartsDataIncludePartList dataIncludePartList = new iPartsDataIncludePartList();
        // Map mit den DIALOG-Keys der Nachfolger aufbauen
        Map<iPartsDialogBCTEPrimaryKey, List<Replace_Elem>> replacedMap = new HashMap<>();
        for (Collection<Replace_Elem> replaceList : getBCTE_ReplaceMap().values()) {
            for (Replace_Elem replaceElem : replaceList) {
                // nur übernehmen, wenn die Umsetzung Replace-DIALOG-Key nach Stüli-Key erfolgt ist
                if (!replaceElem.replaceEntry.partListEntryIdList.isEmpty()) {
                    iPartsDialogBCTEPrimaryKey keyForIncludePartsInReplaceParts = replaceElem.replaceEntry.dialogBCTEKey.getPositionBCTEPrimaryKeyWithoutSDA();
                    List<Replace_Elem> searchReplaceList = replacedMap.get(keyForIncludePartsInReplaceParts);
                    if (searchReplaceList == null) {
                        searchReplaceList = new DwList<>();
                        replacedMap.put(keyForIncludePartsInReplaceParts, searchReplaceList);
                    }
                    searchReplaceList.add(replaceElem);
                }
            }
        }

        // Map, um Primärschlüsselverletzungen aufgrund von identischen IncludePartIds (speziell gleiche Positionsnummer)
        // zu vermeiden
        Map<iPartsIncludePartId, iPartsDataIncludePart> includePartsMap = new HashMap<>();

        // Über alle IncludeElems suchen
        for (Map.Entry<Include_Elem_Key, Include_Elem> entry : getBCTE_IncludeMap().entrySet()) {
            // zu jedem Mitliefer-Teil gehört ein Replace_Elem gefunden über den DIALOG-Key des Nachfolgers
            List<Replace_Elem> replaceElemList = replacedMap.get(entry.getKey().dialogBCTEKey);
            if (replaceElemList != null) {
                Include_Elem includeElem = entry.getValue();
                for (Replace_Elem replaceElem : replaceElemList) {
                    // Vorgänger-Teilenummer muss in Replace_Elem und Include_Elem übereinstimmen
                    if (replaceElem.originalEntry.matNo.equals(includeElem.includeElemKey.predecessorMatNo)) {
                        for (PartListEntryId predecessorEntryId : replaceElem.originalEntry.partListEntryIdList) {
                            // Gehört der Vorgänger zum aktuellen Modul?
                            if (predecessorEntryId.getOwnerAssemblyId().equals(assemblyId)) {
                                for (PartListEntryId successorEntryId : replaceElem.replaceEntry.partListEntryIdList) {
                                    // Gehört der Nachfolger zum aktuellen Modul?
                                    if (successorEntryId.getOwnerAssemblyId().equals(assemblyId)) {
                                        for (Include_Mat_Entry matEntry : includeElem.matEntryList) { // Mitlieferteile pro Vorgänger und Nachfolger
                                            iPartsIncludePartId includePartId = new iPartsIncludePartId(predecessorEntryId.getKVari(), "",
                                                                                                        predecessorEntryId.getKLfdnr(),
                                                                                                        replaceElem.replaceEntry.matNo,
                                                                                                        successorEntryId.getKLfdnr(),
                                                                                                        matEntry.pos);
                                            String pos = matEntry.pos;
                                            int posNumber = SQLStringConvert.ppStringToInt(pos);
                                            iPartsDataIncludePart dataPart = includePartsMap.get(includePartId);
                                            if (dataPart != null) {
                                                if (dataPart.getFieldValue(FIELD_DIP_INCLUDE_MATNR).equals(matEntry.matNo)
                                                    && dataPart.getFieldValue(FIELD_DIP_INCLUDE_QUANTITY).equals(matEntry.quantity)) {
                                                    includePartId = null; // doppelter Datensatz -> ignorieren
                                                } else { // Positionsnummer wurde doppelt verwendet mit unterschiedlichen Daten -> freie Positionsnummer suchen
                                                    iPartsIncludePartId originalIncludePartId = includePartId;
                                                    while (includePartsMap.containsKey(includePartId)) {
                                                        posNumber++;
                                                        if (posNumber > 99) {
                                                            importer.getMessageLog().fireMessage(importer.translateForLog("!!Das Mitlieferteil mit der ID \"%1\" konnte nicht gespeichert werden, da die maximale Anzahl an Mitlieferteilen (99) pro Ersetzungsvorgang erreicht ist.",
                                                                                                                          originalIncludePartId.toString(", ")),
                                                                                                 MessageLogType.tmlWarning,
                                                                                                 MessageLogOption.TIME_STAMP);

                                                            // Mitlieferteil kann nicht übernommen werden
                                                            includePartId = null;
                                                            break;
                                                        }

                                                        pos = StrUtils.padStringWithCharsUpToLength(String.valueOf(posNumber), '0', 2); // Format der Positionsnummern ist zweistellig (z.B. "01")
                                                        includePartId = new iPartsIncludePartId(predecessorEntryId.getKVari(), "",
                                                                                                predecessorEntryId.getKLfdnr(),
                                                                                                replaceElem.replaceEntry.matNo,
                                                                                                successorEntryId.getKLfdnr(),
                                                                                                pos);

                                                        importer.getMessageLog().fireMessage(importer.translateForLog("!!Das Mitlieferteil mit der ID \"%1\" ist mit unterschiedlichen Materialnummern (%2 und %3) bzw. Mengen (%4 und %5) vorhanden. Es wurde die neue freie Positionsnummer %6 erzeugt.",
                                                                                                                      includePartId.toString(", "),
                                                                                                                      dataPart.getFieldValue(FIELD_DIP_INCLUDE_MATNR),
                                                                                                                      matEntry.matNo,
                                                                                                                      dataPart.getFieldValue(FIELD_DIP_INCLUDE_QUANTITY),
                                                                                                                      matEntry.quantity,
                                                                                                                      pos),
                                                                                             MessageLogType.tmlWarning,
                                                                                             MessageLogOption.TIME_STAMP);
                                                    }
                                                }
                                            }

                                            if (includePartId != null) {
                                                // Mitlieferteil erzeugen und in die Liste und Map einfügen
                                                iPartsDataIncludePart dataIncludePart = new iPartsDataIncludePart(getProject(), includePartId);
                                                dataIncludePart.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                                                dataIncludePart.setFieldValue(FIELD_DIP_INCLUDE_MATNR, matEntry.matNo, DBActionOrigin.FROM_EDIT);
                                                dataIncludePart.setFieldValue(FIELD_DIP_INCLUDE_QUANTITY, matEntry.quantity, DBActionOrigin.FROM_EDIT);
                                                dataIncludePartList.add(dataIncludePart, DBActionOrigin.FROM_EDIT);
                                                includePartsMap.put(includePartId, dataIncludePart);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return dataIncludePartList;
    }

    private void debugPrintReplaceParts() {
        for (Collection<Replace_Elem> replaceList : getBCTE_ReplaceMap().values()) {
            for (Replace_Elem replaceElem : replaceList) {
                StringBuilder str = new StringBuilder();
                str.append(replaceElem.originalEntry.dialogBCTEKey);
                str.append(";");
                str.append(replaceElem.originalEntry.matNo);
                str.append(";");
                int i = 0;
                for (PartListEntryId partListEntryId : replaceElem.originalEntry.partListEntryIdList) {
                    str.append(partListEntryId.toString());
                    str.append(";");
                    i++;
                }
                while (i <= 4) {
                    str.append(";");
                    i++;
                }
                str.append(replaceElem.replaceEntry.dialogBCTEKey);
                str.append(";");
                str.append(replaceElem.replaceEntry.matNo);
                str.append(";");
                i = 0;
                for (PartListEntryId partListEntryId : replaceElem.replaceEntry.partListEntryIdList) {
                    str.append(partListEntryId.toString());
                    str.append(";");
                    i++;
                }
                while (i <= 4) {
                    str.append(";");
                    i++;
                }
                System.out.println(str.toString());
            }
        }

    }

    private void compareFootNotes(AbstractFilesImporter importer, DataObjectBTDP dataObjectBTDP) {
        //zuerst die aktuell benutzen Fussnoten holen und in die normierte Form bringen
        iPartsDataFootNoteCatalogueRefList refList = iPartsDataFootNoteCatalogueRefList.loadFootNotesForAssembly(getProject(), new AssemblyId(dataObjectBTDP.dataAssembly.getAsId().getKVari(), ""));
        Map<PartListEntryId, List<String>> currentAssemblyFootnoteList = new LinkedHashMap<>();
        for (iPartsDataFootNoteCatalogueRef dataFootNoteCatalogueRef : refList) {
            List<String> footnoteIdList = currentAssemblyFootnoteList.get(dataFootNoteCatalogueRef.getPartListId());
            if (footnoteIdList == null) {
                footnoteIdList = new LinkedList<>();
                currentAssemblyFootnoteList.put(dataFootNoteCatalogueRef.getPartListId(), footnoteIdList);
            }
            footnoteIdList.add(dataFootNoteCatalogueRef.getAsId().getFootNoteId());

        }
        if (currentAssemblyFootnoteList.size() > 0) {
            for (Map.Entry<PartListEntryId, List<String>> entry : currentAssemblyFootnoteList.entrySet()) {
                List<String> footnoteIdList = dataObjectBTDP.assemblyFootnoteList.get(entry.getKey());
                if (footnoteIdList == null) {
                    //Fussnoten bei diesem PartListEntry sind beim neuen nicht dabei => löschen
                    for (int lfdNr = refList.size() - 1; lfdNr >= 0; lfdNr--) {
                        iPartsDataFootNoteCatalogueRef dataFootNoteCatalogueRef = refList.get(lfdNr);
                        if (dataFootNoteCatalogueRef.getPartListId().equals(entry.getKey())) {
                            refList.delete(dataFootNoteCatalogueRef, DBActionOrigin.FROM_EDIT);
                        }
                    }
                } else {
                    List<String> partListEntryFootNoteIds = new LinkedList<>(entry.getValue());
                    for (int lfdNr = partListEntryFootNoteIds.size() - 1; lfdNr >= 0; lfdNr--) {
                        String currentFootnoteId = partListEntryFootNoteIds.get(lfdNr);
                        if (footnoteIdList.contains(currentFootnoteId)) {
                            //Fussnote bleibt gleich
                            partListEntryFootNoteIds.remove(lfdNr);
                            footnoteIdList.remove(currentFootnoteId);
                        } else {
                            //Fussnote nicht vorhanden => löschen
                            for (int i = refList.size() - 1; lfdNr >= 0; lfdNr--) {
                                iPartsDataFootNoteCatalogueRef dataFootNoteCatalogueRef = refList.get(i);
                                if (dataFootNoteCatalogueRef.getPartListId().equals(entry.getKey()) && dataFootNoteCatalogueRef.getAsId().getFootNoteId().equals(currentFootnoteId)) {
                                    refList.delete(dataFootNoteCatalogueRef, DBActionOrigin.FROM_EDIT);
                                    break;
                                }
                            }
                        }
                    }
                    if (footnoteIdList.size() > 0) {
                        int seqNr = 1;
                        for (String footnoteId : footnoteIdList) {
                            //neue Fussnote an PartList
                            iPartsFootNoteCatalogueRefId refId = new iPartsFootNoteCatalogueRefId(entry.getKey(), footnoteId);
                            iPartsDataFootNoteCatalogueRef newDataFootNoteCatalogueRef = new iPartsDataFootNoteCatalogueRef(getProject(), refId, seqNr);
                            refList.add(newDataFootNoteCatalogueRef, DBActionOrigin.FROM_EDIT);
                            seqNr++;
                        }
                    }
                    dataObjectBTDP.assemblyFootnoteList.remove(entry.getKey());
                }
            }
            if (dataObjectBTDP.assemblyFootnoteList.size() > 0) {
                for (Map.Entry<PartListEntryId, List<String>> entry : dataObjectBTDP.assemblyFootnoteList.entrySet()) {
                    int seqNr = 1;
                    for (String footnoteId : entry.getValue()) {
                        //neue Fussnote an PartList
                        iPartsFootNoteCatalogueRefId refId = new iPartsFootNoteCatalogueRefId(entry.getKey(), footnoteId);
                        iPartsDataFootNoteCatalogueRef newDataFootNoteCatalogueRef = new iPartsDataFootNoteCatalogueRef(getProject(), refId, seqNr);
                        refList.add(newDataFootNoteCatalogueRef, DBActionOrigin.FROM_EDIT);
                        seqNr++;
                    }
                }
            }
        } else {
            for (Map.Entry<PartListEntryId, List<String>> entry : dataObjectBTDP.assemblyFootnoteList.entrySet()) {
                int seqNr = 1;
                for (String footnoteId : entry.getValue()) {
                    //neue Fussnote an PartList
                    iPartsFootNoteCatalogueRefId refId = new iPartsFootNoteCatalogueRefId(entry.getKey(), footnoteId);
                    iPartsDataFootNoteCatalogueRef newDataFootNoteCatalogueRef = new iPartsDataFootNoteCatalogueRef(getProject(), refId, seqNr);
                    refList.add(newDataFootNoteCatalogueRef, DBActionOrigin.FROM_EDIT);
                    seqNr++;
                }
            }
        }
        refList.saveToDB(getProject());
    }

    /**
     * Differenzimport für Ergänzungstexte und sprachneutrale Texte eines Moduls
     *
     * @param importer
     * @param dataObjectBTDP alle zu importierenden Daten zu einem Modul (nicht nur BTDP)
     */
    private void compareCombText(AbstractFilesImporter importer, DataObjectBTDP dataObjectBTDP) {
        //zuerst die aktuell benutzen CombTexte des Moduls holen
        iPartsDataCombTextList dataCombList = new iPartsDataCombTextList();
        dataCombList.loadCombTexts(new AssemblyId(dataObjectBTDP.dataAssembly.getAsId().getKVari(), ""), getProject());

        // dataObjectBTDP.combTextList enthält die neuen Text für das Modul
        // erstmal die gleichen aussortieren und nicht mehr vorhandene löschen
        for (iPartsDataCombText dataCombText : dataCombList.getAsList()) {
            iPartsCombTextId combTextId = dataCombText.getAsId();
            iPartsDataCombText currentCombText = dataObjectBTDP.combTextList.get(combTextId);
            if (currentCombText != null) {
                String id = currentCombText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT).getTextId();
                if (DictHelper.isDictTextId(id)) {
                    //es handelt sich um Neutral Text
                    if (!dataCombText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT).getTextId().equals(id)) {
                        //CombText an gleicher Stelle, jedoch mit neuer TextId => löschen + später einfügen
                        dataCombList.delete(dataCombText, DBActionOrigin.FROM_EDIT);
                    } else {
                        //bereits vorhanden und behandelt
                        dataObjectBTDP.combTextList.remove(combTextId);
                    }
                } else {
                    //es handelt sich um DialogId
                    String dialogTextId = id;
                    if (!StrUtils.isEmpty(dialogTextId)) {
                        String textId = assemblies.textIdHelper.getDictTextIdForDialogId(DictTextKindTypes.ADD_TEXT, dialogTextId);
                        if (!StrUtils.isEmpty(textId)) {
                            if (!dataCombText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT).getTextId().equals(textId)) {
                                //CombText an gleicher Stelle, jedoch mit neuer TextId => löschen + später einfügen
                                dataCombList.delete(dataCombText, DBActionOrigin.FROM_EDIT);
                            } else {
                                //bereits vorhanden und behandelt
                                dataObjectBTDP.combTextList.remove(combTextId);
                            }
                        }
                    } else {
                        //CombText nicht mehr vorhanden => löschen
                        dataCombList.delete(dataCombText, DBActionOrigin.FROM_EDIT);
                    }
                }
            } else {
                //CombText nicht mehr vorhanden => löschen
                dataCombList.delete(dataCombText, DBActionOrigin.FROM_EDIT);
            }
        }

        // nun enthält dataObjectBTDP.combTextList noch die neuen bzw. geänderten TextIds
        createAndSaveCombTexte(importer, dataObjectBTDP, dataCombList);
    }

    /**
     * MultiLangText in iPartsDataCombText setzen.
     * Damit wird nach vorhandenen Wörterbuchtexten im kombinierten Text gesucht
     * Bereits bearbeitete Texte werden im Cache gespeichert.
     *
     * @param dataCombText
     * @param textId
     * @return
     */
    private boolean calculateMultiLangAndSet(iPartsDataCombText dataCombText, String textId) {
        boolean doAdd = false;
        if (!StrUtils.isEmpty(textId)) {
            doAdd = true;
            EtkMultiSprache multi = assemblies.dictMultiLangList.get(textId);
            if (multi == null) {
                // Suche über kombin. Text
                DBDataObjectAttribute attrib = dataCombText.getAttribute(FIELD_DCT_DICT_TEXT, false);
                attrib.setTextIdForMultiLanguage(textId, textId, DBActionOrigin.FROM_DB);
                multi = dataCombText.loadMultiLanguageFromDB(attrib);
                if (multi.getTextId().isEmpty()) {
                    doAdd = false;
                    // kein Wörterbuchtext
                }
                if (doAdd) {
                    assemblies.dictMultiLangList.put(textId, multi);
                }
            }
            if (doAdd) {
                dataCombText.setFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT, multi.cloneMe(), DBActionOrigin.FROM_EDIT);
            }
        }
        return doAdd;
    }

    /**
     * die ID eines neu zusammengebauten Modules umsetzen und speichern
     *
     * @param importer
     * @param dataObjectBTDP
     */
    private void resetIdAndSave(AbstractFilesImporter importer, DataObjectBTDP dataObjectBTDP) {
        iPartsDataAssembly newPartsAssembly = dataObjectBTDP.dataAssembly;
        String kVari = newPartsAssembly.getAsId().getKVari();
        newPartsAssembly.setId(new AssemblyId(kVari, ""), DBActionOrigin.FROM_EDIT);
        dataObjectBTDP.dataAssembly.setDeleteOldId(false);
        //alle Partlistentries nachziehen
        for (EtkDataPartListEntry dataPartListEntry : newPartsAssembly.getPartListUnfiltered(null, true, false)) {
            PartListEntryId partListEntryId = new PartListEntryId(kVari, "", dataPartListEntry.getFieldValue(FIELD_K_LFDNR));
            dataPartListEntry.setId(partListEntryId, DBActionOrigin.FROM_EDIT);
            dataPartListEntry.setDeleteOldId(false);
        }
        //alle Bildverweise nachziehen
        for (EtkDataImage dataImage : newPartsAssembly.getUnfilteredImages()) {
            DataImageId dataImageId = new DataImageId(kVari, "", dataImage.getBlattNr());
            dataImage.setId(dataImageId, DBActionOrigin.FROM_EDIT);
            dataImage.setDeleteOldId(false);
        }
        //Modul speichern
        saveModule(importer, dataObjectBTDP, false);
    }

    /**
     * Modul speichern und die Verlinkungen in den weiteren Tabellen vornehmen
     *
     * @param importer
     * @param dataObjectBTDP
     * @param moduleIsNew
     */
    private boolean saveModule(AbstractFilesImporter importer, DataObjectBTDP dataObjectBTDP, boolean moduleIsNew) {
        String newOrModifiedText = importer.translateForLog(moduleIsNew ? "!!Neues Modul" : "!!Verändertes Modul");
        importer.getMessageLog().fireMessage(importer.translateForLog("!!%1 \"%2\" wird gespeichert", newOrModifiedText,
                                                                      dataObjectBTDP.dataAssembly.getAsId().getKVari()),
                                             MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);

        List<String> enumAutoValues = EditModuleHelper.calculateEnumAutoValues(project, dataObjectBTDP.productId, dataObjectBTDP.kgTuId,
                                                                               dataObjectBTDP.isSpringFilterRelevant(),
                                                                               dataObjectBTDP.isVariantsVisible());
        boolean result;
        if (enumAutoValues == null) {
            result = dataObjectBTDP.dataAssembly.create_iPartsAssembly(iPartsModuleTypes.DialogRetail, dataObjectBTDP.moduleName,
                                                                       null, null, true,
                                                                       getProductData(dataObjectBTDP.productId).getDocumentationType(),
                                                                       dataObjectBTDP.isSpringFilterRelevant(),
                                                                       DCAggregateTypes.UNKNOWN, dataObjectBTDP.isVariantsVisible(),
                                                                       true, null);
        } else {
            result = dataObjectBTDP.dataAssembly.create_iPartsAssembly(iPartsModuleTypes.DialogRetail, dataObjectBTDP.moduleName,
                                                                       null, null, true,
                                                                       getProductData(dataObjectBTDP.productId).getDocumentationType(),
                                                                       DCAggregateTypes.UNKNOWN, true, enumAutoValues, null);
        }
        if (result && moduleIsNew) {
            // Knoten KgTuTemplate angegeben?
            if ((dataObjectBTDP.kgTuId != null) && dataObjectBTDP.kgTuId.isValidId()) {
                if (result) {
                    iPartsDataProduct dataProduct = getProductData(dataObjectBTDP.productId.getProductNumber());
                    if (dataProduct != null) {
                        importer.getMessageLog().fireMessage(importer.translateForLog("!!KG/TU-Verortung vom neuen Modul '%1' wird mit %2/%3 gespeichert",
                                                                                      dataObjectBTDP.dataAssembly.getAsId().getKVari(),
                                                                                      dataObjectBTDP.kgTuId.getKg(), dataObjectBTDP.kgTuId.getTu()),
                                                             MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);

                        createModuleAssignment(dataObjectBTDP);

                        iPartsProductModulesId productModulesId = new iPartsProductModulesId(dataObjectBTDP.productId.getProductNumber(),
                                                                                             dataObjectBTDP.dataAssembly.getAsId().getKVari());
                        iPartsDataProductModules dataProductModules = new iPartsDataProductModules(getProject(), productModulesId);
                        if (!dataProductModules.existsInDB()) {
                            dataProductModules.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        }
                        dataProduct.getProductModulesList().add(dataProductModules, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
        }

        //Fußnoten abspeichern
        if (dataObjectBTDP.assemblyFootnoteList.size() > 0) {
            iPartsDataFootNoteCatalogueRefList refList = new iPartsDataFootNoteCatalogueRefList();
            for (Map.Entry<PartListEntryId, List<String>> entry : dataObjectBTDP.assemblyFootnoteList.entrySet()) {
                for (String footnoteId : entry.getValue()) {
                    iPartsFootNoteCatalogueRefId id = new iPartsFootNoteCatalogueRefId(entry.getKey(), footnoteId);
                    iPartsDataFootNoteCatalogueRef dataFootNoteCatalogueRef = new iPartsDataFootNoteCatalogueRef(getProject(), id);
                    dataFootNoteCatalogueRef.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    refList.add(dataFootNoteCatalogueRef, DBActionOrigin.FROM_EDIT);
                }
            }
            refList.saveToDB(getProject());
        }

        //CombTexte abspeichern
        if (!dataObjectBTDP.combTextList.isEmpty()) {
            iPartsDataCombTextList combTextList = new iPartsDataCombTextList();
            createAndSaveCombTexte(importer, dataObjectBTDP, combTextList);
        }
        return result;
    }

    private iPartsDataModuleEinPAS createModuleAssignment(DataObjectBTDP dataObjectBTDP) {
        iPartsDataProduct dataProduct = getProductData(dataObjectBTDP.productId.getProductNumber());
        if (dataProduct != null) {
            iPartsModuleEinPASId moduleEinPASId = new iPartsModuleEinPASId(dataProduct.getAsId().getProductNumber(),
                                                                           dataObjectBTDP.dataAssembly.getAsId().getKVari(), "");
            iPartsDataModuleEinPAS dataModuleEinPAS = new iPartsDataModuleEinPAS(getProject(), moduleEinPASId);
            dataModuleEinPAS.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT); // dataModuleEinPAS kann noch nicht in der DB existieren
            dataModuleEinPAS.setAttributeValue(iPartsConst.FIELD_DME_SOURCE_KG, dataObjectBTDP.kgTuId.getKg(), DBActionOrigin.FROM_EDIT);
            dataModuleEinPAS.setAttributeValue(iPartsConst.FIELD_DME_SOURCE_TU, dataObjectBTDP.kgTuId.getTu(), DBActionOrigin.FROM_EDIT);
            dataProduct.getModulesEinPASList().add(dataModuleEinPAS, DBActionOrigin.FROM_EDIT);
            return dataModuleEinPAS;
        } else {
            return null;
        }
    }

    /**
     * Kombinierte Texte bestimmen und abspeichern
     *
     * @param importer
     * @param dataObjectBTDP
     * @param combTextList
     */
    private void createAndSaveCombTexte(AbstractFilesImporter importer, DataObjectBTDP dataObjectBTDP, iPartsDataCombTextList combTextList) {
        for (iPartsDataCombText dataCombText : dataObjectBTDP.combTextList.values()) {
            String id = dataCombText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT).getTextId();
            boolean doAdd = DictHelper.isDictTextId(id);
            if (!doAdd) {
                String dialogTextId = id;
                String textId = assemblies.textIdHelper.getDictTextIdForDialogId(DictTextKindTypes.ADD_TEXT, dialogTextId);
                doAdd = calculateMultiLangAndSet(dataCombText, textId);
                if (!doAdd) {
                    //log to file
                    String msg;
                    String msgKey;
                    if (StrUtils.isEmpty(textId)) {
                        msg = importer.translateForLog("!!Keine iParts Lexikon Id zu DialogId \"%1\" gefunden", dialogTextId);
                        msgKey = dialogTextId;
                    } else {
                        msg = importer.translateForLog("!!Kein iParts Lexikon Eintrag zu Text-ID \"%1\" und DialogId \"%2\" gefunden", textId, dialogTextId);
                        msgKey = textId + "|" + dialogTextId;
                    }
                    // Meldung nur ausgeben, wenn sie sich von den Vorgängermeldungen unterscheidet.
                    if (!lastLogfileMsgSet.contains(msgKey)) {
                        importer.getMessageLog().fireMessage(msg, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                        lastLogfileMsgSet.add(msgKey);
                    }
                }
            }
            if (doAdd) {
                combTextList.add(dataCombText, DBActionOrigin.FROM_EDIT);
            }
        }
        combTextList.saveToDB(getProject());
    }

    /**
     * 2 Assemblies miteinander vergleichen
     *
     * @param createdDataAssembly
     * @param existingDataAssembly
     * @return
     */
    private boolean assembliesAreEqual(iPartsDataAssembly createdDataAssembly, iPartsDataAssembly existingDataAssembly) {
        //Anzahl der Stücklisteneinträge gleich?
        boolean isEqual = createdDataAssembly.getPartListUnfiltered(null, true, false).size() == existingDataAssembly.getPartListUnfiltered(null, true, false).size();
        if (isEqual) {
            //Bilder gleich?
            isEqual = picturesAreEqual(createdDataAssembly, existingDataAssembly);
            if (isEqual) {
                //Stücklisteneinträge gleich?
                isEqual = partListEntriesAreEqual(createdDataAssembly, existingDataAssembly);
            }
        } else {
            setModificationReason("!!Anzahl Stücklisteneinträge unterschiedlich (neu: %1 - alt: %2)",
                                  String.valueOf(createdDataAssembly.getPartListUnfiltered(null, true, false).size()),
                                  String.valueOf(existingDataAssembly.getPartListUnfiltered(null, true, false).size()));
        }
        return isEqual;
    }

    /**
     * Die Stücklistenbeinträge zweier Assmeblies vergleichen
     * (Die Anzahl wurde bereits überprüft)
     *
     * @param createdDataAssembly
     * @param existingDataAssembly
     * @return
     */
    private boolean partListEntriesAreEqual(iPartsDataAssembly createdDataAssembly, iPartsDataAssembly existingDataAssembly) {
        boolean isEqual = true;
        // loadAllFields bei getPartListUnfiltered() muss unbedingt true sein, damit nicht jeder Stücklisteneintrag beim
        // Vergleich dann nochmal einzeln nachgeladen wird, weil Felder fehlen
        List<EtkDataPartListEntry> existingPartListEntries = existingDataAssembly.getPartListUnfiltered(null, true, false).getAsList();
        int index = 0;
        //über alle Stcüklisteneinträge
        for (EtkDataPartListEntry createdDataPartListEntry : createdDataAssembly.getPartListUnfiltered(null, true, false)) {
            //überprüfe einen Stücklisteneintrag
            isEqual = partListEntryAreEqual(createdDataPartListEntry, existingPartListEntries.get(index));
            if (!isEqual) {
                break;
            }
            index++;
        }
        return isEqual;
    }

    /**
     * Überprüfung eines Stücklisteneintrags
     *
     * @param createdPartListEntry
     * @param existingPartListEntryDataAssembly
     * @return
     */
    private boolean partListEntryAreEqual(EtkDataPartListEntry createdPartListEntry, EtkDataPartListEntry existingPartListEntryDataAssembly) {
        //überprüfe die Materialnummer
        boolean isEqual = createdPartListEntry.getPart().getAsId().equals(existingPartListEntryDataAssembly.getPart().getAsId());
        if (isEqual) {
            //überprüfe die Sequenznummer
            isEqual = attributeValueIsEqual(createdPartListEntry, existingPartListEntryDataAssembly, FIELD_K_SEQNR);
            if (isEqual) {
                //überprüfe den DIALOG-Schlüssel
                isEqual = attributeValueIsEqual(createdPartListEntry, existingPartListEntryDataAssembly, FIELD_K_SOURCE_GUID);
                if (isEqual) {
                    //jetzt die einzelnen Werte vergleichen
                    isEqual = entryIsSimilar(createdPartListEntry, existingPartListEntryDataAssembly);
                } else {
                    setModificationReason("!!DIALOG-Schlüssel unterschiedlich (neu: %1 - alt: %2)",
                                          createdPartListEntry.getFieldValue(FIELD_K_SOURCE_GUID),
                                          existingPartListEntryDataAssembly.getFieldValue(FIELD_K_SOURCE_GUID));
                }
            } else {
                setModificationReason("!!Sequenznummer unterschiedlich (neu: %1 - alt: %2)",
                                      createdPartListEntry.getFieldValue(FIELD_K_SEQNR),
                                      existingPartListEntryDataAssembly.getFieldValue(FIELD_K_SEQNR));
            }
        } else {
            setModificationReason("!!Materialnummer unterschiedlich (neu: %1 - alt: %2)",
                                  createdPartListEntry.getPart().getAsId().getMatNr(),
                                  existingPartListEntryDataAssembly.getPart().getAsId().getMatNr());
        }
        return isEqual;
    }

    /**
     * Vergleich der Werte zweier PartListEntries
     *
     * @param entry1
     * @param entry2
     * @return
     */
    private boolean entryIsSimilar(EtkDataPartListEntry entry1, EtkDataPartListEntry entry2) {
        for (DBDataObjectAttribute field1 : entry1.getAttributes().getFields()) {
            if (!field1.isVirtual() && !ignoreFieldsForEntryComparison.contains(field1.getName())) {
                DBDataObjectAttribute field2 = entry2.getAttribute(field1.getName(), false);

                //2 Feldwerte vergleichen
                if (!attributeValueIsEqual(field1, field2, entry2)) {
                    // field1 ist auf jeden Fall != null
                    if (field2 != null) {
                        setModificationReason("!!Unterschiedliche Attribute %1 (neu: %2 - alt: %3) bei %4",
                                              field1.getName(),
                                              field1.getAsString(), field2.getAsString(), entry2.getAsId().toString());
                    } else {
                        setModificationReason("!!Unterschiedliche Attribute %1 (neu: %2 - alt: nicht definiert) bei %3",
                                              field1.getName(),
                                              field1.getAsString(), entry2.getAsId().toString());
                    }
                    return false;
                }
            }
        }
        // Keinen Unterschied gefunden
        return true;
    }

    /**
     * Vergleich zweier Feldwerte aus zwei PartListEntries
     *
     * @param field1
     * @param field2
     * @return
     */
    private boolean attributeValueIsEqual(DBDataObjectAttribute field1, DBDataObjectAttribute field2, EtkDataObject dataObject) {
        if ((field1 == null) || (field2 == null)) {
            return false;
        }

        if ((field2.getType() == DBDataObjectAttribute.TYPE.MULTI_LANGUAGE) || (field2.getType() == DBDataObjectAttribute.TYPE.ARRAY)) {
            // TextNr/TextId bzw. ArrayId aus getAsString() reicht für den Vergleich
            return field1.getAsString().equals(field2.getAsString());
        } else {
            return field1.equalContent(field2);
        }
    }

    private boolean attributeValueIsEqual(EtkDataPartListEntry partListEntryOne, EtkDataPartListEntry partListEntryTwo,
                                          String attributeName) {
        return attributeValueIsEqual(partListEntryOne.getAttribute(attributeName, false), partListEntryOne.getAttribute(attributeName, false), partListEntryTwo);
    }

    private boolean attributeValueIsEqual(EtkDataImage dataImageOne, EtkDataImage dataImageTwo,
                                          String attributeName) {
        return attributeValueIsEqual(dataImageOne.getAttribute(attributeName, false), dataImageTwo.getAttribute(attributeName, false), dataImageTwo);

    }

    /**
     * Vergleich der Bild-Einträge
     *
     * @param createdDataAssembly
     * @param existingDataAssembly
     * @return
     */
    private boolean picturesAreEqual(iPartsDataAssembly createdDataAssembly, iPartsDataAssembly existingDataAssembly) {
        //zuerst Anzahl überprüfen
        boolean isEqual = createdDataAssembly.getUnfilteredImages().size() == existingDataAssembly.getUnfilteredImages().size();
        if (isEqual) {
            List<EtkDataImage> existingDataImageList = existingDataAssembly.getUnfilteredImages().getAsList();
            for (EtkDataImage createdDataImage : createdDataAssembly.getUnfilteredImages()) {
                EtkDataImage existingDataImage = findDataImage(createdDataImage, existingDataImageList);
                isEqual = existingDataImage != null;
                if (isEqual) {
                    // Ein leeres Bilddatum aus der Importdatei darf nicht zu einem Unterschied führen (leer bedeutet ja
                    // auch, dass das neueste Bild verwendet werden soll), da ansonsten auch das vorhandene Bilddatum in
                    // der DB gelöscht werden würde durch den Import
                    String createdImageDate = createdDataImage.getFieldValue(FIELD_I_IMAGEDATE);
                    isEqual = createdImageDate.isEmpty() || attributeValueIsEqual(createdDataImage, existingDataImage, FIELD_I_IMAGEDATE);
                    if (!isEqual) {
                        setModificationReason("!!Bild %1 Datum unterschiedlich (neu: %2 - alt: %3)",
                                              createdDataImage.getImagePoolNo(),
                                              createdImageDate,
                                              existingDataImage.getFieldValue(FIELD_I_IMAGEDATE));
                    } else if (isMergingProducts()) { // Baumuster-Gültigkeiten nur bei der Zusammenführung von Produkten vergleichen
                        EtkDataArray existingImageModelValidity = existingDataImage.getFieldValueAsArrayOriginal(FIELD_I_MODEL_VALIDITY);
                        EtkDataArray createdImageModelValidity = createdDataImage.getFieldValueAsArrayOriginal(FIELD_I_MODEL_VALIDITY);
                        isEqual = existingImageModelValidity.equalValues(createdImageModelValidity);
                        if (!isEqual) {
                            setModificationReason("!!Bild %1 Baumuster-Gültigkeit unterschiedlich (neu: %2 - alt: %3)",
                                                  createdDataImage.getImagePoolNo(),
                                                  createdImageModelValidity.getArrayAsString(),
                                                  existingImageModelValidity.getArrayAsString());
                        }
                    }
                } else {
                    setModificationReason("!!Bild %1 nicht in altem Modul enthalten",
                                          createdDataImage.getImagePoolNo());
                }
                if (!isEqual) {
                    break;
                }
            }
        } else {
            setModificationReason("!!Anzahl Bilder unterschiedlich (neu: %1 - alt: %2)",
                                  String.valueOf(createdDataAssembly.getUnfilteredImages().size()),
                                  String.valueOf(existingDataAssembly.getUnfilteredImages().size()));
        }
        return isEqual;
    }

    private EtkDataImage findDataImage(EtkDataImage createdDataImage, List<EtkDataImage> existingDataImageList) {
        for (EtkDataImage dataImage : existingDataImageList) {
            if (dataImage.getImagePoolNo().equals(createdDataImage.getImagePoolNo())) {
                return dataImage;
            }
        }
        return null;
    }

    /**
     * alle Produkte und Module des Baureihen-Imports speichern (soweit noch nicht getan)
     *
     * @param importer
     * @return
     */
    public boolean saveSeriesToDB(AbstractFilesImporter importer) {
        return products.saveToDB(importer);
    }

    /**
     * Löscht alle enthaltenen Assemblies, die keine Stücklistenpositionen haben. Wird am Ende des Baureihenimporters
     * aufgerufen.
     *
     * @param importer
     */
    public void deleteEmptyAssemblies(MigrationDialogSeriesMainImporter importer) {
        for (DataObjectBTDP btcpObject : assemblies.getAssemblyList()) {
            // sortOrderList enthält alle Stücklisteneinträge für das aktuelle Modul (sortiert). Falls die Liste leer ist,
            // enthält das Modul keine vom Importer hinzugefügten Stücklistenpositionen
            if (btcpObject.sortOrderList.isEmpty()) {
                iPartsDataAssembly assembly = null;
                if (btcpObject.dataAssembly == null) {
                    // Beim Vergleich der Module wird die Assembly durch "null" ersetzt, wenn die Module gleich sind.
                    // Nachdem alle leeren Module aus der DB entfernt wurde, dürfte dieser Zweig eigentlich nicht erreicht werden.
                    AssemblyId existingAssemblyId = new AssemblyId(btcpObject.realAssembly.getKVari(), "");
                    EtkDataAssembly tempAssembly = EtkDataObjectFactory.createDataAssembly(getProject(), existingAssemblyId);
                    if (tempAssembly instanceof iPartsDataAssembly) {
                        assembly = (iPartsDataAssembly)tempAssembly;
                    }
                } else {
                    assembly = btcpObject.dataAssembly;
                }
                if (assembly != null) {
                    // Nochmal zur Sicherheit überprüfen, ob Stücklisteneinträge existieren
                    if (assembly.getPartListUnfiltered(null, false, false).isEmpty()) {
                        importer.getMessageLog().fireMessage(importer.translateForLog("!!Modul \"%1\" enthält keine " +
                                                                                      "Stücklistenpositionen und wird " +
                                                                                      "nicht importiert", assembly.getAsId().getKVari()),
                                                             MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                        // Entferne die Bildreferenze zu diesem Modul. Dadurch werden unnötige MQ Anfrage vermieden.
                        picReferenceIdsForModule.remove(btcpObject.realAssembly);
                        assembly.delete_iPartsAssembly(true);
                    }
                }
            }
        }
    }

    /**
     * Durchläuft alle erzeugten Module und erzeugt die dazugehörigen Bildreferenzen, die später für den AS-PLM oder DASTi
     * Import der Bilder benötigt werden. Zusätzlich werden die dazugehörigen {@link EtkDataImage} Objekte an die jeweiligen
     * {@link iPartsDataAssembly}s gehängt.
     * <p>
     * Wichtig: Das darf erst nach dem POSD Import geschehen, da dort die Bildreferenzen anhand der importiereten BCTE
     * Daten gefiltert werden.
     */
    public void addPictureReferencesToAssemblies() {
        //Bilder zum Assembly hinzufügen
        for (DataObjectBTDP dataObjectBTDP : assemblies.getAssemblyList()) {
            Map<String, String> pictureAndDateMap = dataObjectBTDP.getBCTEKeyfilteredPictureAndDateMap();
            Set<iPartsPicReferenceId> referenceIdSet = picReferenceIdsForModule.get(dataObjectBTDP.realAssembly);

            // Referenzen aufsteigend nach Bildnummer sortieren
            Map<String, String> sortedMap = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    String firstPictureNameSortString = Utils.toSortString(o1);
                    String secondPictureNameSortString = Utils.toSortString(o2);
                    return firstPictureNameSortString.compareTo(secondPictureNameSortString);
                }
            });
            sortedMap.putAll(pictureAndDateMap);

            // Nachdem die Map anhand der Bildnummern sortiert wurde, kann man sie den Modulen zuordnen
            for (Map.Entry<String, String> pictureNameAndDate : sortedMap.entrySet()) {
                String pictureDate = StrUtils.isValid(pictureNameAndDate.getValue()) ? pictureNameAndDate.getValue() : getDataSetDateStr();
                PictureReference pictureReference = new PictureReference(getProject(), pictureNameAndDate.getKey(), pictureDate);

                EtkDataImage image;
                if (!pictureReference.isImageExists()) {
                    if (referenceIdSet == null) {
                        referenceIdSet = new LinkedHashSet<>();
                        picReferenceIdsForModule.put(dataObjectBTDP.realAssembly, referenceIdSet);
                    }
                    // Falls das Bild zur DASTI Referenz noch nicht existiert -> Erstelle Verknüpfung zum noch nicht existierenden Bild (via Bildnummer)
                    image = dataObjectBTDP.dataAssembly.addImage(pictureReference.getPictureNumber(), "", false, DBActionOrigin.FROM_EDIT);
                    referenceIdSet.add(new iPartsPicReferenceId(pictureReference.getPictureNumber(), pictureReference.getPictureDate()));
                } else {
                    // Falls das Bild zur DASTI Referenz schon existiert -> Erstelle Verknüpfung zum existierenden Bild (via VarItemId und VarItemRevId)
                    image = dataObjectBTDP.dataAssembly.addImage(pictureReference.getVarItemId(), pictureReference.getVarItemRevId(),
                                                                 false, DBActionOrigin.FROM_EDIT);
                }

                // Bilddatum muss immer gesetzt werden, weil der Datensatz aufgrund von kVer='ImpSe' von dataObjectBTDP.dataAssembly
                // überhaupt nicht in der DB gefunden werden kann
                if (!StrUtils.isEmpty(pictureReference.getPictureDate())) {
                    image.setFieldValue(FIELD_I_IMAGEDATE, pictureReference.getPictureDate(), DBActionOrigin.FROM_EDIT);
                }

                // Nur bei der Zusammenführung von Produkten die gültigen Baumuster für das Bild abspeichern
                if (isMergingProducts()) {
                    Set<String> modelNumbersForPicture = dataObjectBTDP.getModelNumbersForPicture(pictureReference.getPictureNumber());
                    EtkDataArray modelNumbersArray = null;
                    if (!modelNumbersForPicture.isEmpty()) {
                        modelNumbersArray = new EtkDataArray();
                        modelNumbersArray.setArrayId(image.getFieldValueAsArrayOriginal(FIELD_I_MODEL_VALIDITY).getArrayId());
                        modelNumbersArray.add(modelNumbersForPicture);
                    }
                    image.setFieldValueAsArray(FIELD_I_MODEL_VALIDITY, modelNumbersArray, DBActionOrigin.FROM_EDIT);
                }
            }
        }
    }

    /**
     * Beendet den Import einer Baureihe.
     *
     * @param seriesImporter
     */
    public void finishSeriesImport(MigrationDialogSeriesMainImporter seriesImporter) {
        // Löschen aller leeren Module und ihre Bildreferenzen. Dadurch werden nicht unnötige MQ Anfrage losgeschickt
        deleteEmptyAssemblies(seriesImporter);
        if (!isMergingProducts()) {
            deleteColorTableData();
            syncPicReferences(seriesImporter);
        }

        // Um etwas mehr Platz zu schaffen, wobei die Daten nur dann gelöscht werden dürfen, wenn für die Baureihe keine
        // Produkte zusammengeführt werden sollen oder wir gerade beim Zusammenführen sind
        if (!isMergeProductsForSeries() || isMergingProducts()) {
            asPemToFactoriesMap = null;
        }
    }

    public void clearDiffMap() {
        products.listComp.cleanup();
        if (colorTableDatasets != null) {
            colorTableDatasets.cleanup();
            colorTableDatasets = null;
        }
    }

    private void deleteColorTableData() {
        // Liste für alle Farbtabellen Objekte, die gelöscht werden sollen
        GenericEtkDataObjectList deleteList = new GenericEtkDataObjectList();

        // Diff für alle Variantentabellen berechnen
        iPartsDataColorTableDataList colorTableDataList = calculateColortableDataForDeletion();

        // Diff für alle Werkseinsatzdaten zu den Variantentabellen berechnen
        iPartsDataColorTableFactoryList colorTableFactoryList = calculateColortableFactoryDataForDeletion();

        // Zu löschende Werkseinsatzdaten
        deleteList.addAll(colorTableFactoryList, DBActionOrigin.FROM_EDIT);
        // Bei kompletten Variantentabellen werden die verknüpften Daten in DA_COLORTABLE_CONTENT, DA_COLORTABLE_PART und
        // DA_COLORTABLE_FACTORY ebenfalls gelöscht.
        for (iPartsDataColorTableData colorTableObject : colorTableDataList) {
            deleteList.add(colorTableObject, DBActionOrigin.FROM_EDIT);
            iPartsDataColorTableContentList colorTableContentList =
                    iPartsDataColorTableContentList.loadColorTableContentListForColorTable(getProject(),
                                                                                           colorTableObject.getAsId());
            deleteList.addAll(colorTableContentList, DBActionOrigin.FROM_EDIT);
            iPartsDataColorTableToPartList colorTableToPartList =
                    iPartsDataColorTableToPartList.loadColorTableToPartListForColortableId(getProject(),
                                                                                           colorTableObject.getAsId());
            deleteList.addAll(colorTableToPartList, DBActionOrigin.FROM_EDIT);
            iPartsDataColorTableFactoryList colorTableFactoryListForColortable =
                    iPartsDataColorTableFactoryList.loadAllColortableFactoryDataForColortableId(getProject(),
                                                                                                colorTableObject.getAsId(),
                                                                                                iPartsImportDataOrigin.MAD);
            deleteList.addAll(colorTableFactoryListForColortable, DBActionOrigin.FROM_EDIT);
            if (deleteList.size() >= MAX_ENTRIES_DELETE_COLORTABLE_DATA) {
                saveDeleteList(deleteList);
            }
        }
        saveDeleteList(deleteList);
        clearDiffMap();
    }

    /**
     * Berechnet den Diff für die importiereten Variantentabellen-Werkseinsatzdaten und die vorhandenen Werkseisnatdzaten.
     *
     * @return
     */
    private iPartsDataColorTableFactoryList calculateColortableFactoryDataForDeletion() {
        iPartsDataColorTableFactoryList result = new iPartsDataColorTableFactoryList();
        EtkDataObjectList.FoundAttributesCallback callback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String colortableId = attributes.getFieldValue(FIELD_DCCF_TABLE_ID);
                String pos = attributes.getFieldValue(FIELD_DCCF_POS);
                String factory = attributes.getFieldValue(FIELD_DCCF_FACTORY);
                String adat = attributes.getFieldValue(FIELD_DCCF_ADAT);
                String dataId = attributes.getFieldValue(FIELD_DCCF_DATA_ID);
                String sdata = attributes.getFieldValue(FIELD_DCCF_SDATA);

                iPartsColorTableFactoryId colorTableFactoryId = new iPartsColorTableFactoryId(colortableId, pos, factory, adat, dataId, sdata);
                // Wurde die ganze Farbtabelle nicht versorgt, dann braucht man die einzelnen Werkseinsatzdaten nicht separat
                // löschen, da die Frabtabelle später komplett gelöscht wird.
                if (!colorTableDatasets.getOnlyInSecondItems().containsKey(colortableId)) {
                    String idAsString = colorTableFactoryId.toString();
                    colorTableDatasets.putSecond(idAsString, idAsString);
                    return colorTableDatasets.getOnlyInSecondItems().containsKey(idAsString);

                }
                return false;
            }
        };
        // Like-Abfrage für alle, die mit QFT[Baureihe] beginnen
        result.searchSortAndFillWithJoin(getProject(), getProject().getDBLanguage(), null,
                                         new String[]{ TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_TABLE_ID),
                                                       TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_SOURCE) },
                                         new String[]{ ColorTableHelper.makeWhereValueForColorTableWithSeries(getSeriesId()),
                                                       iPartsImportDataOrigin.MAD.getOrigin() },
                                         false, null, false, true, callback);
        return result;
    }

    /**
     * Berechnet den Diff für die importiereten Variantentabellen und die vorhandenen Variantentabellen.
     *
     * @return
     */
    private iPartsDataColorTableDataList calculateColortableDataForDeletion() {
        iPartsDataColorTableDataList result = new iPartsDataColorTableDataList();
        EtkDataObjectList.FoundAttributesCallback callback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String colortableId = attributes.getFieldValue(FIELD_DCTD_TABLE_ID);
                iPartsColorTableDataId colorTableDataId = new iPartsColorTableDataId(colortableId);
                String colorTableId = colorTableDataId.getColorTableId();
                colorTableDatasets.putSecond(colorTableId, colorTableId);
                return colorTableDatasets.getOnlyInSecondItems().containsKey(colorTableId);
            }
        };
        // Like-Abfrage für alle, die mit QFT[Baureihe] beginnen
        result.searchSortAndFillWithJoin(getProject(), getProject().getDBLanguage(), null,
                                         new String[]{ TableAndFieldName.make(TABLE_DA_COLORTABLE_DATA, FIELD_DCTD_TABLE_ID),
                                                       TableAndFieldName.make(TABLE_DA_COLORTABLE_DATA, FIELD_DCTD_SOURCE) },
                                         new String[]{ ColorTableHelper.makeWhereValueForColorTableWithSeries(getSeriesId()),
                                                       iPartsImportDataOrigin.MAD.getOrigin() }, false, null, false, true, callback);
        return result;
    }

    private void saveDeleteList(GenericEtkDataObjectList deleteList) {
        deleteList.deleteAll(DBActionOrigin.FROM_EDIT);
        deleteList.saveToDB(getProject());
    }

    public Set<iPartsModelId> getProcessedModels() {
        return modelList.keySet();
    }

    public void addColorTableDatasetToDiff(String colorTableDataId) {
        if (colorTableDatasets == null) {
            colorTableDatasets = new DiskMappedKeyValueListCompare(true, false, true);
        }
        colorTableDatasets.putFirst(colorTableDataId, colorTableDataId);
    }

    public void setSeriesId(iPartsSeriesId seriesId) {
        this.seriesId = seriesId;
    }

    public iPartsSeriesId getSeriesId() {
        return seriesId;
    }

    /**
     * Sollen die Produkte für die Baureihe dieses Imports zusammengeführt werden?
     *
     * @return
     */
    public boolean isMergeProductsForSeries() {
        return iPartsDialogSeries.getInstance(getProject(), getSeriesId()).isMergeProducts();
    }

    /**
     * Ist dies der Import für die Zusammenführung von Produkten?
     *
     * @return
     */
    public boolean isMergingProducts() {
        return catalogImportWorkerToMerge != null;
    }

    /**
     * Liefert den {@link iPartsCatalogImportWorker} mit den Original-Produkten zurück, der als Basis für die Zusammenführung
     * der Produkte verwendet werden soll.
     *
     * @return
     */
    public iPartsCatalogImportWorker getCatalogImportWorkerToMerge() {
        return catalogImportWorkerToMerge;
    }

    /**
     * Liefert die Map von Original-{@link iPartsProductId}s auf zusammengeführte {@link iPartsProductId}s zurück falls
     * dies der Import für die Zusammenführung von Produkten ist.
     *
     * @return
     */
    public Map<String, String> getOriginalProductsToMergedProductsMap() {
        return originalProductsToMergedProductsMap;
    }


    protected class ProductManagement {

        private Map<iPartsProductId, Integer> productHash;
        private iPartsDataProductList productList;
        public Map<String, iPartsDataModuleEinPAS> moduleAssignmentMap;
        public Map<iPartsProductId, Set<String>> aaMap; // Produkt -> Ausführungsarten
        private DiskMappedKeyValueListCompare listComp;


        public ProductManagement() {
            productHash = new HashMap<>();
            productList = new iPartsDataProductList();
            moduleAssignmentMap = new HashMap<>();
            aaMap = new HashMap<>();
            listComp = new DiskMappedKeyValueListCompare(true, false, true);
        }

        public void addProduct(iPartsDataProduct dataProduct, DBActionOrigin origin) {
            productHash.put(dataProduct.getAsId(), productList.size());
            productList.add(dataProduct, origin);
        }

        public iPartsDataProduct getProduct(iPartsProductId productId) {
            Integer index = productHash.get(productId);
            if (index != null) {
                return productList.get(index);
            }
            return null;
        }

        public iPartsDataProductList getProductList() {
            return productList;
        }

        public boolean saveToDB(AbstractFilesImporter importer) {
            List<DataObjectBTDP> saveAssemblyList = new ArrayList<>();
            for (DataObjectBTDP dataObjectBTDP : assemblies.getAssemblyList()) {
                iPartsDataAssembly dataAssembly = dataObjectBTDP.dataAssembly;
                if (dataAssembly != null) {
                    boolean saveAssembly;
                    if (dataAssembly.isNew()) {
                        saveAssembly = !dataAssembly.getPartListUnfiltered(null, true, false).isEmpty();
                    } else {
                        saveAssembly = dataAssembly.isModifiedWithChildren();
                    }
                    if (saveAssembly) {
                        saveAssemblyList.add(dataObjectBTDP);
                    }
                }
            }
            int assemblyCount = saveAssemblyList.size();
            if (assemblyCount > 0) {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!%1 Module werden gespeichert", String.valueOf(assemblyCount)),
                                                     MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                importer.getMessageLog().fireProgress(0, assemblyCount, "", true, false);

                int counter = 0;
                for (DataObjectBTDP assembly : saveAssemblyList) {
                    if (Thread.currentThread().isInterrupted()) {
                        importer.cancelImport("!!Import-Thread wurde frühzeitig beendet");
                        return false;
                    }
                    if (assembly.dataAssembly != null) {
                        assembly.dataAssembly.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.FALSE);
                    }
                    counter++;
                    importer.getMessageLog().fireProgress(counter, assemblyCount, "", true, true);
                }
                importer.getMessageLog().hideProgress();
            }

            int productCount = getProductList().size();
            importer.getMessageLog().fireMessage(importer.translateForLog("!!%1 Produkte werden gespeichert:", String.valueOf(productCount)),
                                                 MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            importer.getMessageLog().fireProgress(0, productCount, "", true, false);

            int counter = 0;
            for (iPartsDataProduct product : getProductList()) {
                if (Thread.currentThread().isInterrupted()) {
                    importer.cancelImport("!!Import-Thread wurde frühzeitig beendet");
                    return false;
                }

                // Einschränkung auf Ausführungsarten bestimmen
                String aaListString;
                Set<String> aaSet = products.aaMap.get(product.getAsId());
                if (!aaSet.isEmpty()) {
                    List<String> aaList = new ArrayList<>(aaSet);
                    Collections.sort(aaList);
                    aaListString = " " + importer.translateForLog("!!(enthält nur die Ausführungsarten %1)",
                                                                  StrUtils.stringListToString(aaList, ", "));
                } else { // keine Ausführungsart gültig
                    aaListString = " " + importer.translateForLog("!!(enthält nur leere Module, da keine Ausführungsart über die Baumuster zum Produkt gültig ist)");
                }
                String action = "!!Produkt %1";
                if (product.isNew() || product.isModifiedWithChildren()) {
                    action = "!!Produkt %1 wird gespeichert";
                }
                importer.getMessageLog().fireMessage("- " + importer.translateForLog(action, product.getAsId().getProductNumber())
                                                     + aaListString, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                product.saveToDB();
                counter++;
                importer.getMessageLog().fireProgress(counter, productCount, "", true, true);
            }
            importer.getMessageLog().hideProgress();
            return true;
        }
    }


    private class AssemblyManagement {

        private Map<AssemblyId, DataObjectBTDP> dataAssemblies;
        private DictImportTextIdHelper textIdHelper;
        //Beziehung zwischen Dict-Id und EtkMultiSprache
        private Map<String, EtkMultiSprache> dictMultiLangList;

        public AssemblyManagement() {
            dataAssemblies = new LinkedHashMap<>();
            textIdHelper = new DictImportTextIdHelper(getProject());
            dictMultiLangList = new HashMap<>();
        }

        public void addAssembly(iPartsProductId productId, AssemblyId pseudoAssemblyId, iPartsDataAssembly dataAssembly,
                                boolean isNew, KgTuId kgTuId, EtkMultiSprache moduleName) {
            DataObjectBTDP dataObjectBTDP = new DataObjectBTDP(productId, pseudoAssemblyId, dataAssembly, isNew, kgTuId, moduleName);
            dataAssemblies.put(pseudoAssemblyId, dataObjectBTDP);
        }

        public iPartsDataAssembly getAssembly(AssemblyId pseudoAssemblyId) {
            DataObjectBTDP dataObjectBTDP = getDataObjectBTDP(pseudoAssemblyId);
            if (dataObjectBTDP != null) {
                return dataObjectBTDP.dataAssembly;
            }
            return null;
        }

        public DataObjectBTDP getDataObjectBTDP(AssemblyId pseudoAssemblyId) {
            return dataAssemblies.get(pseudoAssemblyId);
        }

        public Collection<DataObjectBTDP> getAssemblyList() {
            return dataAssemblies.values();
        }

    }


    /**
     * Ein BTDP-Satz stellt die Verbindung einer DIALOG-Position (BCTE) zu einer Katalogposition in KG/TU her.
     * Die Modul-Info ist für alle Datensätz desselben Moduls gleich. Es unterscheidet sich die Positionsinfo.
     * Diese Klasse gruppiert alle BTDP-Daten eines Moduls. Außerdem werden hier noch andere Moduldaten angehängt,
     * die aber nicht aus BTDP kommen (z.B. combTextList).
     * <p/>
     * Die positionsabh. Daten stecken in Maps mit Stücklisteintrag als Key.
     */
    public class DataObjectBTDP {

        public iPartsProductId productId;
        public Map<iPartsDialogBCTEPrimaryKey, Map<String, Set<iPartsProductId>>> bcteKeyAndHotspotToOriginalProductIdsMap;
        public AssemblyId pseudoAssemblyId;
        public AssemblyId realAssembly;
        public iPartsDataAssembly dataAssembly;
        public boolean isNew;
        public KgTuId kgTuId;
        public EtkMultiSprache moduleName;
        private Map<iPartsDialogBCTEPrimaryKey, DataElemBTDP> btdpList;
        private Map<String, String> unfilteredPictureToDateMap;
        private Map<String, String> filteredPictureToDateMap; // nach BCTE Schlüssel aus POSD gefiltert
        private Map<String, Set<String>> pictureNameToBCTEKeysMap;
        private Map<String, Set<String>> pictureNameToOriginalProductNumbersMap;
        private Map<PartListEntryId, List<String>> assemblyFootnoteList;
        private Map<iPartsCombTextId, iPartsDataCombText> combTextList;
        private Map<PartListEntryId, String> sortOrderList;
        private boolean isVariantsVisible = iPartsDataModule.DM_VARIANTS_VISIBLE_DEFAULT;  // Wenn es sich um ein ganz neues Modul handelt, dann muss der default-Wert verwendet werden

        public DataObjectBTDP(iPartsProductId productId, AssemblyId pseudoAssemblyId, iPartsDataAssembly dataAssembly, boolean isNew,
                              KgTuId kgTuId, EtkMultiSprache moduleName) {
            this.productId = productId;
            this.pseudoAssemblyId = pseudoAssemblyId;
            this.dataAssembly = dataAssembly;
            if (dataAssembly != null) {
                this.realAssembly = dataAssembly.getAsId();
            }
            this.isNew = isNew;
            this.kgTuId = kgTuId;
            this.moduleName = moduleName;
            this.btdpList = new LinkedHashMap<>();
            this.unfilteredPictureToDateMap = new HashMap<>();
            this.filteredPictureToDateMap = new HashMap<>();
            this.pictureNameToBCTEKeysMap = new HashMap<>();
            this.assemblyFootnoteList = new HashMap<>();
            this.combTextList = new HashMap<>();
            this.sortOrderList = new HashMap<>();

            if (isMergingProducts()) {
                bcteKeyAndHotspotToOriginalProductIdsMap = new HashMap<>();
                pictureNameToOriginalProductNumbersMap = new HashMap<>();
            }
        }

        public void addPictureNameAndHotspot(iPartsDialogBCTEPrimaryKey btdpPrimaryKey, String pictureName, String pictureChangeDate,
                                             String hotspot, String lfdNr, String originalProductNumber) {
            DataElemBTDP dataElemBTDP = btdpList.get(btdpPrimaryKey);
            if (dataElemBTDP == null) {
                dataElemBTDP = new DataElemBTDP();
                btdpList.put(btdpPrimaryKey, dataElemBTDP);
            }
            // Da beim BTDP Import alle Referenzen vorkommen, werden diese direkt in die "ungefilterte" Map gelegt
            unfilteredPictureToDateMap.put(pictureName, pictureChangeDate);
            // Zusätzlich wird eine Map erzeugt mit Bildreferenznummer auf alle BCTE Schlüssel, mit denen die Referenz
            // importiert wurde. Da mehrere verschiedene BCTE Schlüssel aus BTDP auf die gleiche Bildreferenz verweisen
            // können, wird hier ein Set angelegt.
            Set<String> bcteKeysForPicture = pictureNameToBCTEKeysMap.get(pictureName);
            if (bcteKeysForPicture == null) {
                bcteKeysForPicture = new HashSet<>();
                pictureNameToBCTEKeysMap.put(pictureName, bcteKeysForPicture);
            }
            bcteKeysForPicture.add(btdpPrimaryKey.toString());
            dataElemBTDP.addPictureNameAndHotspot(pictureName, hotspot, lfdNr);

            // Nur bei der Zusammenführung von Produkten die gültigen Baumuster für das Bild merken
            if (isMergingProducts()) {
                Set<String> originalProductNumbers = pictureNameToOriginalProductNumbersMap.get(pictureName);
                if (originalProductNumbers == null) {
                    originalProductNumbers = new HashSet<>();
                    pictureNameToOriginalProductNumbersMap.put(pictureName, originalProductNumbers);
                }
                originalProductNumbers.add(originalProductNumber);
            }
        }

        /**
         * Filtert die Bildreferenzen in diesem Modul anhand dem übergebenenen BCTE-Schlüssel. BTDP-Import sammelt ALLE
         * Bildreferenzen zu einem Modul. POSD liefert die eigentlichen Stücklisteninformationen. Bildreferenzen sind in
         * diesem Modul nur gültig, wenn POSD Daten (Stücklistenpositionen) tatsächlich an dieses Modul gehängt wurden.
         *
         * @param bcteKeyForFilter
         */
        public void filterPictureReferenceForBCTEKey(iPartsDialogBCTEPrimaryKey bcteKeyForFilter) {
            Set<String> validPictures = new HashSet<>();
            // Durchlaufe alle Referenznummern und die dazugehörigen BCTE-Schlüssel
            for (Map.Entry<String, Set<String>> pictureToBCTEKeyEntry : pictureNameToBCTEKeysMap.entrySet()) {
                String picturename = pictureToBCTEKeyEntry.getKey();
                Set<String> bcteKeys = pictureToBCTEKeyEntry.getValue();
                // Gehört der übergebene BCTE Schlüssel zur aktuellen Referenznummer, dann lege die Referenz in die
                // Map mit den gefilterten Referenzen.
                if (bcteKeys.contains(bcteKeyForFilter.toString())) {
                    filteredPictureToDateMap.put(picturename, unfilteredPictureToDateMap.get(picturename));
                    // Die Referenznummern werden zwischengespeichert, damit sie später entfernt werden können.
                    validPictures.add(picturename);
                }
            }
            // Wenn eine Referenznummer zum übergebenen BCTE Schlüssel gefunden wurden, dann ist sie für dieses Modul
            // gültig und wird später angehängt (am Ende des kompletten POSD Imports). Damit spätere BCTE Schlüssel,
            // die auf die gleiche Referenznummer zeigen die Referenzn nicht unnötigerweise wieder hinzuügen, wird hier
            // die Beziehung zwischen Referenznummer und BCTE-Schlüssel gelöscht. Wäre ja unnötig, weil die Referenznummer
            // für das Modul schon vorher als "gültig" bestimmt wurde.
            for (String picturename : validPictures) {
                pictureNameToBCTEKeysMap.remove(picturename);
            }
        }

        public void addElemValues(iPartsDialogBCTEPrimaryKey btdpPrimaryKey, String dateTo, String pictureKZ, String entityAttribut) {
            DataElemBTDP dataElemBTDP = btdpList.get(btdpPrimaryKey);
            if (dataElemBTDP != null) {
                dataElemBTDP.addElemValues(dateTo, pictureKZ, entityAttribut);
            }
        }

        public void addOriginalProductForBCTEKeyAndHotspot(iPartsDialogBCTEPrimaryKey btdpPrimaryKey, String hotspot, iPartsProductId originalProductId) {
            Map<String, Set<iPartsProductId>> hotspotsToProductIdsMap = bcteKeyAndHotspotToOriginalProductIdsMap.get(btdpPrimaryKey);
            if (hotspotsToProductIdsMap == null) {
                hotspotsToProductIdsMap = new HashMap<>();
                bcteKeyAndHotspotToOriginalProductIdsMap.put(btdpPrimaryKey, hotspotsToProductIdsMap);
            }

            hotspot = StrUtils.removeLeadingCharsFromString(hotspot, '0');
            Set<iPartsProductId> originalProductIds = hotspotsToProductIdsMap.get(hotspot);
            if (originalProductIds == null) {
                originalProductIds = new HashSet<>();
                hotspotsToProductIdsMap.put(hotspot, originalProductIds);
            }
            originalProductIds.add(originalProductId);
        }

        public Set<iPartsProductId> getOriginalProductForBCTEKeyAndHotspot(iPartsDialogBCTEPrimaryKey btdpPrimaryKey, String hotspot) {
            Map<String, Set<iPartsProductId>> hotspotsToProductIdsMap = bcteKeyAndHotspotToOriginalProductIdsMap.get(btdpPrimaryKey);
            if (hotspotsToProductIdsMap != null) {
                hotspot = StrUtils.removeLeadingCharsFromString(hotspot, '0');
                return hotspotsToProductIdsMap.get(hotspot);
            } else {
                return null;
            }
        }

        //Testen, ob es die KG 32 ist. Diese KG ist Federrelevant
        public boolean isSpringFilterRelevant() {
            if (getProductData(productId).getAggType().equals(AGGREGATE_TYPE_CAR)) {

                if ((kgTuId != null) && (kgTuId.isValidId())) {
                    if (kgTuId.getKg().equals("32")) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean isVariantsVisible() {
            return isVariantsVisible;
        }

        public void setIsVariantsVisible(boolean isVariantsVisible) {
            this.isVariantsVisible = isVariantsVisible;
        }

        public Map<String, String> getUnfilteredPictureAndDateMap() {
            return unfilteredPictureToDateMap;
        }

        public Map<String, String> getBCTEKeyfilteredPictureAndDateMap() {
            return filteredPictureToDateMap;
        }

        /**
         * Liefert alle Baumusternummern der Original-Produkte zurück, denen das übergebene Bild zugeordnet ist.
         *
         * @param pictureName
         * @return
         */
        public Set<String> getModelNumbersForPicture(String pictureName) {
            Set<String> modelNumbers = new TreeSet<>();
            if (pictureNameToOriginalProductNumbersMap != null) {
                Set<String> originalProductNumbers = pictureNameToOriginalProductNumbersMap.get(pictureName);
                if (originalProductNumbers != null) {
                    for (String originalProductNumber : originalProductNumbers) {
                        iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(originalProductNumber));
                        modelNumbers.addAll(product.getModelNumbers(project));
                    }
                }
            }
            return modelNumbers;
        }
    }


    public class DataElemBTDP {

        private Map<String, Map<String, String>> pictureList;
        private String dateTo;
        private String pictureKZ;
        private String entityAttribut;
        //private String pictureChangeDate;

        public DataElemBTDP() {
            pictureList = new LinkedHashMap<>();
        }

        public void addPictureNameAndHotspot(String pictureName, String hotspot, String lfdNr) {
            hotspot = StrUtils.prefixStringWithCharsUpToLength(hotspot, '0', 4);  // die Angaben in iPartsDialogBCTEPrimaryKey sind 4stellig
            Map<String, String> hotspotList = getHotspotList(pictureName);
            if (hotspotList == null) {
                hotspotList = new LinkedHashMap<>();
                pictureList.put(pictureName, hotspotList);
            }
            hotspotList.put(hotspot, lfdNr);
        }

        public void addElemValues(String dateTo, String pictureKZ, String entityAttribut) {
            this.dateTo = dateTo;
            this.pictureKZ = pictureKZ;
            this.entityAttribut = entityAttribut;
            //this.pictureChangeDate = pictureChangeDate;
        }

        public Map<String, String> getHotspotList(String pictureName) {
            return pictureList.get(pictureName);
        }

        public Set<String> getPictureNameList() {
            return pictureList.keySet();
        }
    }

    /*
     * einfacher Container für weitere POSD-Daten außerhalb von iPartsDataDialogData
     */

    public static class ExtraPosDImportData {

        public EtkMultiSprache multiSprache;
        public String eTxtNumber;

        public ExtraPosDImportData() {

        }
    }


    public static class BCTE_FootNote_Container {

        public iPartsDialogBCTEPrimaryKey primaryBCTEKey = null;
        public List<String> footnoteIdList = new LinkedList<>();
    }


    public static class Fail_Location_Container {

        public String failLocation;
        //public String failLocOrder;
        public String partNo;

    }


    public static class Replace_Entry {

        public iPartsDialogBCTEPrimaryKey dialogBCTEKey;
        public String matNo;
        public List<PartListEntryId> partListEntryIdList;

        public Replace_Entry() {
            this.dialogBCTEKey = null;
            this.matNo = "";
            partListEntryIdList = new DwList<>();
        }

        /**
         * Fügt die Id des übergebenen {@link EtkDataPartListEntry} zur Liste {@link #partListEntryIdList} hinzu, wobei {@code kVer}
         * leer gesetzt wird, um Konflikte zwischen den importierten und vorhandenen Stücklisteneinträgen zu vermeiden.
         * <p>
         * Außerdem wird die Ausführungsart des übergebenen {@link EtkDataPartListEntry} in einer Map gespeichert um
         * später beim Erzeugen der Ersetzungen diese auf die Ausführungsart einschränken zu können.
         *
         * @param partListEntry
         * @param partlistEntryAAMap
         */
        public void addPartListEntry(EtkDataPartListEntry partListEntry, Map<PartListEntryId, String> partlistEntryAAMap) {
            PartListEntryId partListEntryId = new PartListEntryId(partListEntry.getAsId().getKVari(), "", partListEntry.getAsId().getKLfdnr());
            partListEntryIdList.add(partListEntryId);

            if (partlistEntryAAMap != null) {
                partlistEntryAAMap.put(partListEntryId, partListEntry.getFieldValue(iPartsConst.FIELD_K_AA));
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Replace_Entry)) {
                return false;
            }

            Replace_Entry otherReplaceEntry = (Replace_Entry)obj;
            return Utils.objectEquals(dialogBCTEKey, otherReplaceEntry.dialogBCTEKey) && Utils.objectEquals(matNo, otherReplaceEntry.matNo);
        }

        @Override
        public int hashCode() {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(dialogBCTEKey.hashCode());
            hcb.append(matNo);
            return hcb.toHashCode();
        }
    }


    public static class Replace_Elem {

        public Replace_Entry originalEntry;
        public Replace_Entry replaceEntry;
        public String entryPos;
        public String entryRFMEA;
        public String entryRFMEN;
        public boolean withSDA;

        public Replace_Elem() {
            this.originalEntry = new Replace_Entry();
            this.replaceEntry = new Replace_Entry();
            this.entryPos = "";
            this.entryRFMEA = "";
            this.entryRFMEN = "";
        }

        public Replace_Elem(iPartsDialogBCTEPrimaryKey dialogBCTEKeyReplacePart, String replaceMatNo,
                            iPartsDialogBCTEPrimaryKey dialogBCTEKeyOriginalPart, String originalMatNo, boolean withSDA) {
            this();
            this.originalEntry.dialogBCTEKey = dialogBCTEKeyOriginalPart;
            this.originalEntry.matNo = originalMatNo;
            this.replaceEntry.dialogBCTEKey = dialogBCTEKeyReplacePart;
            this.replaceEntry.matNo = replaceMatNo;
            this.withSDA = withSDA;
        }

        public boolean isValid() {
            return ((replaceEntry.dialogBCTEKey != null) && !replaceEntry.matNo.isEmpty());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Replace_Elem)) {
                return false;
            }

            Replace_Elem otherReplaceElem = (Replace_Elem)obj;
            return Utils.objectEquals(originalEntry, otherReplaceElem.originalEntry) && Utils.objectEquals(replaceEntry, otherReplaceElem.replaceEntry)
                   && Utils.objectEquals(entryRFMEA, otherReplaceElem.entryRFMEA) && Utils.objectEquals(entryRFMEN, otherReplaceElem.entryRFMEN)
                   && (withSDA == otherReplaceElem.withSDA);
        }

        @Override
        public int hashCode() {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(originalEntry.hashCode());
            hcb.append(replaceEntry.hashCode());
            hcb.append(entryRFMEA);
            hcb.append(entryRFMEN);
            hcb.append(withSDA);
            return hcb.toHashCode();
        }
    }


    public static class Include_Elem_Key {

        String predecessorMatNo;
        iPartsDialogBCTEPrimaryKey dialogBCTEKey;

        public Include_Elem_Key(String predecessorMatNo, iPartsDialogBCTEPrimaryKey dialogBCTEKey) {
            this.predecessorMatNo = predecessorMatNo;
            this.dialogBCTEKey = dialogBCTEKey;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Include_Elem_Key)) {
                return false;
            }

            Include_Elem_Key otherElemKey = (Include_Elem_Key)obj;
            return predecessorMatNo.equals(otherElemKey.predecessorMatNo) && dialogBCTEKey.equals(otherElemKey.dialogBCTEKey);
        }

        @Override
        public int hashCode() {
            return predecessorMatNo.hashCode() + dialogBCTEKey.hashCode();
        }
    }


    public static class Include_Mat_Entry {

        String matNo;
        String quantity;
        String pos;
    }


    /**
     * Klasse, die bei ECHTEN und PSEUDO-Ersetzungen benutzt wird, um die Flags für PEM AB/BIS relevant für den Endnummern-Filter
     * in die Datenbank zu transportieren.
     */
    public static class PEMEvaluationEntry {

        private String matNo = "";
        private iPartsDialogBCTEPrimaryKey predecessorBCTEKey;
        private iPartsDialogBCTEPrimaryKey successorBCTEKey;
        private boolean evalPEMFrom; // Wird im FIELD_K_EVAL_PEM_FROM gespeichert
        private boolean evalPEMTo;   // Wird im FIELD_K_EVAL_PEM_TO gespeichert
        private boolean withSDA;

        public String getMatNo() {
            return matNo;
        }

        public void setMatNo(String matNo) {
            this.matNo = matNo;
        }

        public iPartsDialogBCTEPrimaryKey getPredecessorBCTEKey() {
            return predecessorBCTEKey;
        }

        public void setPredecessorBCTEKey(iPartsDialogBCTEPrimaryKey predecessorBCTEKey) {
            this.predecessorBCTEKey = predecessorBCTEKey;
        }

        public iPartsDialogBCTEPrimaryKey getSuccessorBCTEKey() {
            return successorBCTEKey;
        }

        public void setSuccessorBCTEKey(iPartsDialogBCTEPrimaryKey successorSData) {
            this.successorBCTEKey = successorSData;
        }

        public boolean isEvalPEMFrom() {
            return evalPEMFrom;
        }

        public void setEvalPEMFrom(boolean evalPEMFrom) {
            this.evalPEMFrom = evalPEMFrom;
        }

        public boolean isEvalPEMTo() {
            return evalPEMTo;
        }

        public void setEvalPEMTo(boolean evalPEMTo) {
            this.evalPEMTo = evalPEMTo;
        }

        public boolean isWithSDA() {
            return withSDA;
        }

        public void setWithSDA(boolean withSDA) {
            this.withSDA = withSDA;
        }
    }


    public static class Include_Elem {

        public Include_Elem_Key includeElemKey;

        // Sortierung nach Include_Mat_Entry.pos (TTEL_MITLPOS)
        Set<Include_Mat_Entry> matEntryList = new TreeSet<>(new Comparator<Include_Mat_Entry>() {
            @Override
            public int compare(Include_Mat_Entry o1, Include_Mat_Entry o2) {
                return o1.pos.compareTo(o2.pos);
            }
        });

        List<PartListEntryId> predecessorPartListEntryIdList = new DwList<>();

        public Include_Elem(Include_Elem_Key includeElemKey) {
            this.includeElemKey = includeElemKey;
        }

        public boolean isValid() {
            return includeElemKey.dialogBCTEKey != null;
        }

        public void addIncludeMatEntry(String matNo, String pos, String quantity) {
            Include_Mat_Entry matEntry = new Include_Mat_Entry();
            matEntry.matNo = matNo;
            matEntry.pos = pos;
            matEntry.quantity = quantity;
            addIncludeMatEntry(matEntry);
        }

        public void addIncludeMatEntry(Include_Mat_Entry matEntry) {
            matEntryList.add(matEntry);
        }

        /**
         * Fügt die übergebene {@link PartListEntryId} zur Liste {@link #predecessorPartListEntryIdList} hinzu, wobei {@code kVer}
         * leer gesetzt wird, um Konflikte zwischen den importierten und vorhandenen Stücklisteneinträgen zu vermeiden.
         *
         * @param partListEntryId
         */
        public void addPredecessorPartListEntryId(PartListEntryId partListEntryId) {
            predecessorPartListEntryIdList.add(new PartListEntryId(partListEntryId.getKVari(), "", partListEntryId.getKLfdnr()));
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Include_Elem)) {
                return false;
            }

            return includeElemKey.equals(((Include_Elem)obj).includeElemKey);
        }

        @Override
        public int hashCode() {
            return includeElemKey.hashCode();
        }
    }


    /**
     * Hilfsklasse für die PEM-abhängigen Rückmeldedaten beim Baureihenimporter (PEMQ und PEMZ)
     */
    protected static class PemForSeries {

        private String pem;
        private String series;

        public PemForSeries(String pem, String series) {
            this.pem = pem;
            this.series = series;
        }

        public String getPem() {
            return pem;
        }

        public String getSeries() {
            return series;
        }

        @Override
        public String toString() {
            return pem + "||" + series;
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            return this.toString().equals(obj.toString());
        }
    }
}