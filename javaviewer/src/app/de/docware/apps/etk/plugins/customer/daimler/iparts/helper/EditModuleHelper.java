/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.db.ChangeSetId;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataGenVoPairingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.KGTUAutosetTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModuleReferences;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sort.SortBetweenHelper;

import java.util.*;

/**
 * Hilfsklasse für das Editieren von Modulen in iParts.
 */
public class EditModuleHelper implements iPartsConst {

    public static final String IPARTS_MODULE_NAME_DELIMITER = "_";
    public static final String CAR_PERSPECTIVE_NAME = "!!Fahrzeugnavigation";
    public static final String CAR_PERSPECTIVE_KVARI_SUFFIX = "_Navigation";
    private static final int IPARTS_MODULE_SERIAL_NUMBER_LENGTH = 5;
    private static final int HOTSPOT_INCREMENT = 10;
    private static final KgTuId DUMMY_KGTU_ID = buildCarPerspectiveKgTuId();
    private static final Set<String> IGNORE_GENVOS = new HashSet<>();

    static {
        IGNORE_GENVOS.add("88888");
        IGNORE_GENVOS.add("77777");
    }

    public static String buildCarPerspectiveKVari(iPartsProductId productId) {
        return buildCarPerspectiveKVari(productId.getProductNumber());
    }

    public static String buildCarPerspectiveKVari(String productNumber) {
        return productNumber + CAR_PERSPECTIVE_KVARI_SUFFIX;
    }

    public static boolean carPerspectiveModuleExists(EtkProject project, String productNumber) {
        String moduleName = buildCarPerspectiveKVari(productNumber);
        return existsModule(project, moduleName);
    }

    public static boolean isCarPerspectiveAssemblyShort(AssemblyId assemblyId) {
        return assemblyId.getKVari().endsWith(CAR_PERSPECTIVE_KVARI_SUFFIX);
    }

    public static boolean isCarPerspectiveAssembly(EtkProject project, AssemblyId assemblyId) {
        return isCarPerspectiveAssembly(EtkDataObjectFactory.createDataAssembly(project, assemblyId, true));
    }

    public static boolean isCarPerspectiveAssembly(EtkDataAssembly assembly) {
        if (assembly != null) {
            return isCarPerspectiveAssembly(assembly.getEbeneName());
        }
        return false;
    }

    public static boolean isCarPerspectiveAssembly(String ebeneName) {
        return ebeneName.equals(iPartsConst.PARTS_LIST_TYPE_CAR_PERSPECTIVE);
    }

    public static AssemblyId buildCarPerspectiveId(iPartsProductId productId) {
        return new AssemblyId(buildCarPerspectiveKVari(productId), "");
    }

    public static KgTuId buildCarPerspectiveKgTuId() {
        return new KgTuId("#", "#");
    }

    public static boolean isCarPerspectiveKgTuId(IdWithType idWithType) {
        if (idWithType.getType().equals(KgTuId.TYPE)) {
            KgTuId kgTuId = new KgTuId(idWithType.getValue(1), idWithType.getValue(2));
            return isCarPerspectiveKgTuId(kgTuId);
        }
        return false;
    }

    public static boolean isCarPerspectiveKgTuId(KgTuId kgTuId) {
        if (kgTuId == null) {
            return false;
        }
        if (kgTuId.isTuNode()) {
            return kgTuId.equals(DUMMY_KGTU_ID);
        } else {
            return kgTuId.getKg().equals(DUMMY_KGTU_ID.getKg());
        }
    }

    public static EtkMultiSprache buildCarPerspectiveModuleName(EtkProject project) {
        return new EtkMultiSprache(CAR_PERSPECTIVE_NAME, project.getConfig().getDatabaseLanguages());
    }

    public static EtkDataAssembly createCarPerspectiveDataAssembly(EtkProject project, iPartsProductId productId) {
        return EtkDataObjectFactory.createDataAssembly(project, buildCarPerspectiveId(productId), true);
    }

    /**
     * Ermittelt alle relevanten Daten für das übergebene Sub-Modul in einer Fahrzeug-Navigation.
     *
     * @param project
     * @param subAssembly
     * @param productStructuresMap Map von Produkt-ID auf dessen Produkt-Struktur für lokales Caching
     * @param datacardModelNumbers Alle Baumuster inkl. Aggregate-Baumuster der aktiven Datenkarte
     * @return
     */
    public static CarPerspectiveSubModuleData getCarPerspectiveSubModuleData(EtkProject project, iPartsDataAssembly subAssembly,
                                                                             Map<iPartsProductId, iPartsProductStructures> productStructuresMap,
                                                                             Set<String> datacardModelNumbers) {
        CarPerspectiveSubModuleData data = new CarPerspectiveSubModuleData();
        iPartsProductId productIdFromModuleUsage = subAssembly.getProductIdFromModuleUsage();
        if (productIdFromModuleUsage != null) {
            iPartsProduct productInst = iPartsProduct.getInstance(project, productIdFromModuleUsage);
            Set<String> modelNumbers = productInst.getVisibleModelNumbers(project);
            if (Utils.isValid(modelNumbers)) {
                for (String modelNumber : modelNumbers) {
                    if (datacardModelNumbers.contains(modelNumber)) {
                        data.modelNumber = modelNumber;
                        break;
                    }
                }
            }

            data.productNumber = productIdFromModuleUsage.getProductNumber();

            iPartsProductStructures structures = productStructuresMap.get(productIdFromModuleUsage);
            if (structures == null) {
                structures = iPartsProductStructures.getInstance(project, productIdFromModuleUsage);
                structures.getCompleteKgTuStructure(project, true); // notwendig um KG/TUs pro Module zu laden
                productStructuresMap.put(productIdFromModuleUsage, structures);
            }

            iPartsModuleReferences module = structures.getModule(subAssembly.getAsId(), project);
            if (module != null) {
                Set<KgTuId> referencesKgTu = module.getReferencesKgTu();
                if ((referencesKgTu != null) && !referencesKgTu.isEmpty()) {
                    KgTuId firstKGTURef = referencesKgTu.iterator().next();
                    data.cg = firstKGTURef.getKg();
                    data.csg = firstKGTURef.getTu();
                }
            }
        }

        return data;
    }

    /**
     * Berechnung eines Teil-NavigationPaths bei Sprung zu Aggregaten, damit der Sprung klappt
     *
     * @param assemblyId
     * @return
     */
    public static NavigationPath calculatePartialPathForCarPerspective(EtkProject project, AssemblyId assemblyId) {
        NavigationPath resultPath = new NavigationPath();
        // Produkt des Ziels bestimmen
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
        iPartsProductId productId = ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage();
        iPartsProduct product = iPartsProduct.getInstance(project, productId);
        if (product.isAggregateProduct(project)) {
            // Ziel ist ein Aggregate-Produkt
            // KG/TU bestimmen
            KgTuId kgTuId = calculateKgTuForAssembly(project, product, assemblyId);
            if (kgTuId != null) {
                // Ziel konnte sauber bestimmt werden => Teil-NavigationPath zusammenbauen
                String productNav = iPartsVirtualNode.getVirtualIdString(product, product.isStructureWithAggregates(), project);
                List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(productNav);
                virtualNodes.add(new iPartsVirtualNode(iPartsNodeType.KGTU, kgTuId));
                resultPath.addAssembly(new AssemblyId(iPartsVirtualNode.getVirtualIdString(ArrayUtil.toArray(virtualNodes)), ""));
            }
        }
        resultPath.addAssembly(assemblyId);
        return resultPath;

        // ALTERNATIVE
//        MechanicUsageCalculator calculator = new MechanicUsageCalculator(project, getConnector().getRootAssemblyId(),
//                                                                         assembly, true, true);
//        MechanicUsageCalculator.CalculatorResult result = null;
//        startPseudoTransactionForActiveChangeSet(true);
//        try {
//            result = calculator.calculate();
//            if ((result != null) && (result.getDirectUsagesOfSearchObject() != null) && !result.getDirectUsagesOfSearchObject().isEmpty()) {
//                AssemblyId searchAssemblyId = result.getDirectUsagesOfSearchObject().get(0).getParentAssemblyId();
//                resultPath = ModuleHierarchyNet.getShortPathToModule(project, getConnector().getRootAssemblyId(), searchAssemblyId, true,
//                                                                     ModuleHierarchyNet.SearchNetType.sntMuch,
//                                                                     false);
//
//            }
//        } finally {
//            stopPseudoTransactionForActiveChangeSet();
//        }
    }

    private static KgTuId calculateKgTuForAssembly(EtkProject project, iPartsProduct product, AssemblyId assemblyId) {
        KgTuId kgTuId = null;
        // KG/TU bestimmen
        iPartsDataModuleEinPASList moduleEinPASList = iPartsDataModuleEinPASList.loadForProductAndModule(project, product.getAsId(), assemblyId);
        if (!moduleEinPASList.isEmpty()) {
            iPartsDataModuleEinPAS moduleEinPAS = moduleEinPASList.get(0);
            // zur Sicherheit nochmal abfragen
            if (product.getProductStructuringType() == iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU) {
                // Wird sind in einem KGTU Modul
                String kg = moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_KG);
                String tu = moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_TU);
                kgTuId = new KgTuId(kg, tu);
            }
        }
        return kgTuId;
    }

    /**
     * Liefert für den übergebenen {@link AbstractJavaViewerFormIConnector} die aktuelle {@link EtkDataAssembly} zurück.
     *
     * @param connector
     * @return {@code null} falls aus dem übergebenen {@link AbstractJavaViewerFormIConnector} keine {@link EtkDataAssembly}
     * bestimmt werden konnte
     */
    public static EtkDataAssembly getAssemblyFromConnector(AbstractJavaViewerFormIConnector connector) {
        EtkDataAssembly assembly = null;
        if (connector instanceof RelatedInfoBaseFormIConnector) {
            RelatedInfoBaseFormIConnector relatedInfoConnector = (RelatedInfoBaseFormIConnector)(connector);
            assembly = EtkDataObjectFactory.createDataAssembly(connector.getProject(), relatedInfoConnector.getRelatedInfoData().getSachAssemblyId());
        } else if (connector instanceof EditFormIConnector) {
            assembly = ((EditFormIConnector)connector).getCurrentAssembly();
        } else {
            if (connector instanceof AssemblyListFormIConnector) {
                assembly = AbstractRelatedInfoPartlistDataForm.getDestinationAssemblyForPartListEntryFromConnector((AssemblyListFormIConnector)connector);
            }

            // MechanicFormConnector implementiert sowohl AssemblyListFormIConnector als auch AssemblyTreeFormIConnector
            // -> kein else-Zweig verwenden sondern assembly auf null prüfen
            if ((assembly == null) && (connector instanceof AssemblyTreeFormIConnector)) {
                AssemblyTreeFormIConnector assemblyTreeFormConnector = (AssemblyTreeFormIConnector)connector;
                assembly = EtkDataObjectFactory.createDataAssembly(connector.getProject(), assemblyTreeFormConnector.getCurrentAssembly().getAsId());
            }
        }

        if (assembly != null) {
            assembly = assembly.getLastHiddenSingleSubAssemblyOrThis(null);
            return assembly;
        } else {
            return null;
        }
    }

    public static iPartsDataAssembly createAndSaveModuleWithKgTuAssignment(AssemblyId assemblyId, iPartsModuleTypes moduleType,
                                                                           EtkMultiSprache moduleName, iPartsProductId productId,
                                                                           KgTuId kgTuId, EtkProject project,
                                                                           iPartsDocumentationType documentationType,
                                                                           boolean checkIfExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        return createAndSaveModuleWithKgTuAssignment(assemblyId, moduleType, moduleName, productId, kgTuId, project,
                                                     documentationType, false, DCAggregateTypes.UNKNOWN, checkIfExistsInDB,
                                                     techChangeSet);
    }

    public static iPartsDataAssembly createAndSaveModuleWithKgTuAssignment(AssemblyId assemblyId, iPartsModuleTypes moduleType,
                                                                           EtkMultiSprache moduleName, iPartsProductId productId,
                                                                           KgTuId kgTuId, EtkProject project,
                                                                           iPartsDocumentationType documentationType,
                                                                           boolean moduleIsSpringFilterRelevant,
                                                                           DCAggregateTypes aggTypeForSpecialZBFilter,
                                                                           boolean checkIfExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        return createAndSaveModuleWithGenericAssignment(assemblyId, moduleType, moduleName, productId, kgTuId, null, null,
                                                        project, documentationType, null, moduleIsSpringFilterRelevant,
                                                        aggTypeForSpecialZBFilter, checkIfExistsInDB, techChangeSet);
    }

    public static iPartsDataAssembly createAndSaveModuleWithEinPASAssignment(AssemblyId assemblyId, iPartsModuleTypes moduleType,
                                                                             EtkMultiSprache moduleName, iPartsProductId productId,
                                                                             EinPasId einPasId, EtkProject project,
                                                                             iPartsDocumentationType documentationType,
                                                                             boolean checkIfExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        return createAndSaveModuleWithEinPASAssignment(assemblyId, moduleType, moduleName, productId, einPasId, project,
                                                       documentationType, false, DCAggregateTypes.UNKNOWN, checkIfExistsInDB,
                                                       techChangeSet);
    }

    public static iPartsDataAssembly createAndSaveModuleWithEinPASAssignment(AssemblyId assemblyId, iPartsModuleTypes moduleType,
                                                                             EtkMultiSprache moduleName, iPartsProductId productId,
                                                                             EinPasId einPasId, EtkProject project,
                                                                             iPartsDocumentationType documentationType,
                                                                             boolean moduleIsSpringFilterRelevant,
                                                                             DCAggregateTypes aggTypeForSpecialZBFilter,
                                                                             boolean checkIfExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        return createAndSaveModuleWithGenericAssignment(assemblyId, moduleType, moduleName, productId, einPasId, null, null,
                                                        project, documentationType, null, moduleIsSpringFilterRelevant,
                                                        aggTypeForSpecialZBFilter, checkIfExistsInDB, techChangeSet);
    }

    public static iPartsDataAssembly createAndSaveModuleWithSAAssignment(AssemblyId assemblyId, iPartsModuleTypes moduleType,
                                                                         EtkMultiSprache moduleName, iPartsProductId productId,
                                                                         iPartsSAModulesId saModulesId, String kgForSA, EtkProject project,
                                                                         iPartsDocumentationType documentationType,
                                                                         iPartsImportDataOrigin dataOrigin,
                                                                         boolean checkIfExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        return createAndSaveModuleWithSAAssignment(assemblyId, moduleType, moduleName, productId, saModulesId,
                                                   kgForSA, project, documentationType, dataOrigin, false,
                                                   DCAggregateTypes.UNKNOWN, checkIfExistsInDB, techChangeSet);
    }

    public static iPartsDataAssembly createAndSaveModuleWithSAAssignment(AssemblyId assemblyId, iPartsModuleTypes moduleType,
                                                                         EtkMultiSprache moduleName, iPartsProductId productId,
                                                                         iPartsSAModulesId saModulesId, String kgForSA, EtkProject project,
                                                                         iPartsDocumentationType documentationType,
                                                                         iPartsImportDataOrigin dataOrigin,
                                                                         boolean moduleIsSpringFilterRelevant,
                                                                         DCAggregateTypes aggTypeForSpecialZBFilter,
                                                                         boolean checkIfExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        return createAndSaveModuleWithGenericAssignment(assemblyId, moduleType, moduleName, productId, null, saModulesId, kgForSA,
                                                        project, documentationType, dataOrigin, moduleIsSpringFilterRelevant,
                                                        aggTypeForSpecialZBFilter, checkIfExistsInDB, techChangeSet);
    }

    public static iPartsDataAssembly createAndSaveModuleWithCarPerspectiveAssignment(AssemblyId assemblyId,
                                                                                     iPartsProductId productId,
                                                                                     EtkProject project,
                                                                                     boolean checkIfExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        EtkMultiSprache moduleName = buildCarPerspectiveModuleName(project);
        KgTuId kgTuId = buildCarPerspectiveKgTuId();
        iPartsModuleTypes moduleType = iPartsModuleTypes.CAR_PERSPECTIVE;
        iPartsDocumentationType documentationType = iPartsDocumentationType.DIALOG_IPARTS;
        return createAndSaveModuleWithGenericAssignment(assemblyId, moduleType, moduleName, productId, kgTuId, null, null,
                                                        project, documentationType, null,
                                                        false, DCAggregateTypes.UNKNOWN, checkIfExistsInDB, techChangeSet);
    }

    private static iPartsDataAssembly createAndSaveModuleWithGenericAssignment(AssemblyId assemblyId, iPartsModuleTypes moduleType,
                                                                               EtkMultiSprache moduleName, iPartsProductId productId,
                                                                               HierarchicalIDWithType einPasOrKgTuId,
                                                                               iPartsSAModulesId saModulesId, String kgForSA,
                                                                               EtkProject project,
                                                                               iPartsDocumentationType documentationType,
                                                                               iPartsImportDataOrigin dataOrigin,
                                                                               boolean moduleIsSpringFilterRelevant,
                                                                               DCAggregateTypes aggTypeForSpecialZBFilter,
                                                                               boolean checkIfExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        // Modul anlegen
        EtkDataAssembly newAssembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId, false);
        if (newAssembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)newAssembly;
            iPartsAssembly.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

            boolean result;
            boolean revisionChangeSetActive = project.getDbLayer().isRevisionChangeSetActiveForEdit();
            if (!revisionChangeSetActive) {
                project.getDbLayer().startTransaction();
            }
            try {
                List<String> enumAutoValues = calculateEnumAutoValues(project, productId, einPasOrKgTuId, moduleIsSpringFilterRelevant,
                                                                      iPartsDataModule.DM_VARIANTS_VISIBLE_DEFAULT);

                if (enumAutoValues != null) {
                    result = iPartsAssembly.create_iPartsAssembly(moduleType, moduleName, productId, saModulesId,
                                                                  true, documentationType,
                                                                  aggTypeForSpecialZBFilter,
                                                                  checkIfExistsInDB, enumAutoValues, techChangeSet);
                } else {
                    result = iPartsAssembly.create_iPartsAssembly(moduleType, moduleName, productId, saModulesId,
                                                                  true, documentationType,
                                                                  moduleIsSpringFilterRelevant, aggTypeForSpecialZBFilter,
                                                                  checkIfExistsInDB, techChangeSet);
                }
                if (result && (productId != null) && productId.isValidId()) {
                    // Knoten EinPAS oder KG/TU angegeben?
                    if ((einPasOrKgTuId != null) && (einPasOrKgTuId.isValidId())) {
                        result = createGenericAssignment(productId, iPartsAssembly.getAsId().getKVari(),
                                                         einPasOrKgTuId, project, checkIfExistsInDB, techChangeSet);
                    }

                    // KG-Knoten für SA angegeben?
                    if (result && (saModulesId != null) && saModulesId.isValidId() && StrUtils.isValid(kgForSA)) {
                        result = createSAAssignment(productId, saModulesId, kgForSA, project, dataOrigin, checkIfExistsInDB);
                    }
                }

                if (!revisionChangeSetActive) {
                    if (result) {
                        project.getDbLayer().commit();
                    } else {
                        project.getDbLayer().rollback();
                    }
                }
            } catch (Exception e) {
                if (!revisionChangeSetActive) {
                    project.getDbLayer().rollback();
                }
                Logger.getLogger().throwRuntimeException(e);
                result = false;
            }

            if (result) {
                //neu erzeugtes Modul laden
                return iPartsAssembly;
            }
        }

        return null;
    }

    public static List<String> calculateEnumAutoValues(EtkProject project, iPartsProductId productId, HierarchicalIDWithType einPasOrKgTuId,
                                                       boolean moduleIsSpringFilterRelevant, boolean isVariantsVisible) {
        if ((productId != null) && productId.isValidId() && (einPasOrKgTuId != null) && (einPasOrKgTuId.isValidId()) &&
            (einPasOrKgTuId instanceof KgTuId)) {
            KgTuId kgTuId = (KgTuId)einPasOrKgTuId;
            // da auch ein Importer (iPartsCatalogImportWorker) diese Funktionalität benutzt, kann der KgTuForProduct-Cache nicht benutzt werden
            // Wenn der aktuelle KG/TU-Knoten nicht im KgTUForProduct ist, dann handelt es sich um einen Template-Knoten
            String[] whereFields = new String[]{ FIELD_DA_DKM_PRODUCT, FIELD_DA_DKM_KG, FIELD_DA_DKM_TU };
            String[] whereValues = new String[]{ productId.getProductNumber(), kgTuId.getKg(), kgTuId.getTu() };
            boolean isTemplateKgTu = !project.getEtkDbs().getRecordExists(TABLE_DA_KGTU_AS, whereFields, whereValues);
            if (isTemplateKgTu) {
                // Für jede ProductClass wird ein eigener Eintrag erzeugt, mit den gleichen Daten bzgl DA_DKT_TU_OPTIONS => nimm den ersten
                iPartsProduct product = iPartsProduct.getInstance(project, productId);
                String aggregateType = product.getAggregateType();
                Set<String> asProductClasses = product.getAsProductClasses();
                iPartsKgTuTemplateId kgTuTemplateId = new iPartsKgTuTemplateId(aggregateType,
                                                                               asProductClasses.iterator().next(),
                                                                               kgTuId.getKg(), kgTuId.getTu());
                iPartsDataKgTuTemplate dataKgTuTemplate = new iPartsDataKgTuTemplate(project, kgTuTemplateId);
                if (dataKgTuTemplate.existsInDB()) {
                    List<String> enumAutoValues = dataKgTuTemplate.getFieldValueAsSetOfEnum(iPartsConst.FIELD_DA_DKT_TU_OPTIONS);
                    if (moduleIsSpringFilterRelevant && !enumAutoValues.contains(KGTUAutosetTypes.FF.getDbValue())) {
                        enumAutoValues.add(KGTUAutosetTypes.FF.getDbValue());
                    }
                    if (isVariantsVisible && !enumAutoValues.contains(KGTUAutosetTypes.VA.getDbValue())) {
                        enumAutoValues.add(KGTUAutosetTypes.VA.getDbValue());
                    }
                    return enumAutoValues;
                }
            }
        }
        return null;
    }

    public static boolean createGenericAssignment(iPartsProductId productId, String moduleNumber, HierarchicalIDWithType einPasOrKgTuId,
                                                  EtkProject project, boolean checkIfExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        // WICHTIG: keine SerialNo angeben, da diese bei initAttributesWithEmptyValues automatisch gebildet wird!!
        iPartsModuleEinPASId moduleEinPASId = new iPartsModuleEinPASId(productId.getProductNumber(), moduleNumber, "");
        iPartsDataModuleEinPAS moduleEinPASEntry = new iPartsDataModuleEinPAS(project, moduleEinPASId);
        // Eintrag in DA_MODULES_EINPAS Tabelle vorbereiten
        moduleEinPASEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

        if (einPasOrKgTuId instanceof EinPasId) {
            moduleEinPASEntry.create_iPartsVerortung((EinPasId)einPasOrKgTuId, checkIfExistsInDB, techChangeSet);
        } else if (einPasOrKgTuId instanceof KgTuId) {
            moduleEinPASEntry.create_iPartsVerortung((KgTuId)einPasOrKgTuId, checkIfExistsInDB, techChangeSet);
        } else {
            Logger.getLogger().throwRuntimeException("EditModulHelper.createGenericAssignment: Parameter einPasOrKgTuId must be EinPasId or KgTuId");
        }

        return true;
    }

    public static boolean createSAAssignment(iPartsProductId productId, iPartsSAModulesId saModulesId, String kg, EtkProject project,
                                             iPartsImportDataOrigin dataOrigin, boolean checkIfExistsInDB) {
        // DBDataObject mit der Verortung der SA für den angegebenen KG-Knoten im Produkt erzeugen und speichern
        iPartsProductSAsId productSAsId = new iPartsProductSAsId(productId, saModulesId, kg);
        iPartsDataProductSAs dataProductSAs = new iPartsDataProductSAs(project, productSAsId);
        dataProductSAs.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        if (dataOrigin == null) {
            dataOrigin = iPartsImportDataOrigin.ELDAS;
        }
        dataProductSAs.setFieldValue(FIELD_DPS_SOURCE, dataOrigin.getOrigin(), DBActionOrigin.FROM_EDIT);
        dataProductSAs.saveToDB(checkIfExistsInDB, DBDataObject.PrimaryKeyExistsInDB.CHECK);
        return true;
    }

    /**
     * Modulnummer aus den Eingaben bilden (OHNE laufende Nummer am Ende)
     *
     * @param productId
     * @param einPasId
     * @return
     */
    public static String buildEinPasModuleNumberWithoutSerial(iPartsProductId productId, EinPasId einPasId) {
        StringBuilder str = new StringBuilder();
        str.append(productId.getProductNumber());
        if (einPasId != null) {
            str.append(IPARTS_MODULE_NAME_DELIMITER);
            str.append(einPasId.getHg());
            str.append(IPARTS_MODULE_NAME_DELIMITER);
            str.append(einPasId.getG());
            str.append(IPARTS_MODULE_NAME_DELIMITER);
            str.append(einPasId.getTu());
        }
        return str.toString();
    }

    public static boolean standardModuleExists(EtkProject project, iPartsProductId productId, KgTuId kgTuId) {
        if (kgTuId != null) {
            String moduleName = createStandardModuleName(productId, kgTuId);
            return existsModule(project, moduleName);
        }
        return false;
    }

    public static boolean existsModule(EtkProject project, String moduleName) {
        return project.getDB().getRecordExists(iPartsConst.TABLE_DA_MODULE, new String[]{ iPartsConst.FIELD_DM_MODULE_NO },
                                               new String[]{ moduleName });
    }


    /**
     * Überprüft, ob ein Modul in der Reserved PK-Tabelle enthalten ist. Liefert Fehlermeldungen (falls messages besetzt ist)
     *
     * @param project
     * @param productId
     * @param kgTuId
     * @param withOwnChangeSet falls {@code true}, wird IMMER {@code true} zurückgeliefert, wenn das Modul vom gerade aktiven
     *                         oder einem anderen Autorenauftrag reserviert ist.
     *                         ansonsten wird nur {@code true} zurückgeliefert, wenn das Modul von einem anderen Autorenauftrag,
     *                         als dem gerade aktiven reserviert ist.
     * @param messages
     * @return
     */
    public static boolean isStandardModuleInReservedPK(EtkProject project, iPartsProductId productId, KgTuId kgTuId, boolean withOwnChangeSet, List<String> messages) {
        if (kgTuId != null) {
            String newModuleName = createStandardModuleName(productId, kgTuId);
            AssemblyId assemblyId = new AssemblyId(newModuleName, "");
            return isModuleInReservedPK(project, withOwnChangeSet, messages, assemblyId);
        } else {
            if (messages != null) {
                messages.add(TranslationHandler.translate("!!Ungültiger KG/TU Schlüssel"));
            }
        }
        return true;
    }

    /**
     * Überprüft, ob ein Modul in der Reserved PK-Tabelle enthalten ist. Liefert Fehlermeldungen (falls messages besetzt ist)
     *
     * @param project
     * @param withOwnChangeSet falls {@code true}, wird IMMER {@code true} zurückgeliefert, wenn das Modul vom gerade aktiven
     *                         oder einem anderen Autorenauftrag reserviert ist.
     *                         ansonsten wird nur {@code true} zurückgeliefert, wenn das Modul von einem anderen Autorenauftrag,
     *                         als dem gerade aktiven reserviert ist.
     * @param messages
     * @return
     */
    public static boolean isModuleInReservedPK(EtkProject project, boolean withOwnChangeSet, List<String> messages, AssemblyId assemblyId) {
        iPartsDataReservedPKList reservedPKList = iPartsDataReservedPKList.loadChangeSetsForIdWithType(project, assemblyId);
        if (!reservedPKList.isEmpty()) {
            boolean moduleExistsInChangeSet = true;
            iPartsDataReservedPK reservedPK = reservedPKList.get(0);
            String changeSetId = reservedPK.getChangeSetId();
            if (!withOwnChangeSet) {
                String activeChangeSetGuidAsDbValue = project.getActiveChangeSetGuidAsDbValue();
                if (StrUtils.isValid(activeChangeSetGuidAsDbValue)) {
                    if (changeSetId.equals(activeChangeSetGuidAsDbValue)) {
                        moduleExistsInChangeSet = false;
                    }
                }
            }
            if (!moduleExistsInChangeSet) {
                // Modul wurde im eigenen Autoren-Auftrag erzeugt
                return false;
            }
            if (messages != null) {
                iPartsDataAuthorOrder dataAuthorOrder = iPartsDataAuthorOrderList.getAuthorOrderByChangeSetId(project, new ChangeSetId(changeSetId));
                if (dataAuthorOrder != null) {
                    messages.add(TranslationHandler.translate("!!Der TU \"%1\" wurde bereits im Autoren-Auftrag \"%2\" angelegt.",
                                                              assemblyId.getKVari(), dataAuthorOrder.getAuthorOrderName()));
                } else {
                    messages.add(TranslationHandler.translate("!!Der TU \"%1\" wurde bereits in einem Autoren-Auftrag angelegt.",
                                                              assemblyId.getKVari()));
                }
            }


//                String activeChangeSetGuidAsDbValue = project.getActiveChangeSetGuidAsDbValue();
//                if (StrUtils.isValid(activeChangeSetGuidAsDbValue)) {
//                    Set<String> occupiedChangeSetIds = new HashSet<>();
//                    for (iPartsDataReservedPK reservedPK : reservedPKList) {
//                        if (!reservedPK.getChangeSetId().equals(activeChangeSetGuidAsDbValue)) {
//                            occupiedChangeSetIds.add(reservedPK.getChangeSetId());
//                        } else if (withOwnChangeSet) {
//                            occupiedChangeSetIds.add(reservedPK.getChangeSetId());
//                        }
//                    }
//                    if (occupiedChangeSetIds.isEmpty()) {
//                        // Modul wurde im eigenen Autoren-Auftrag erzeugt
//                        return false;
//                    }
//                    if (messages != null) {
//                        String firstChangeSetId = occupiedChangeSetIds.iterator().next();
//                        iPartsDataAuthorOrder dataAuthorOrder = iPartsDataAuthorOrderList.getAuthorOrderByChangeSetId(project, new ChangeSetId(firstChangeSetId));
//                        if (dataAuthorOrder != null) {
//                            messages.add(TranslationHandler.translate("!!Der TU \"%1\" wurde bereits im Autoren-Auftrag \"%2\" angelegt",
//                                                                      newModuleName, dataAuthorOrder.getAuthorOrderName()));
//                        } else {
//                            messages.add(TranslationHandler.translate("!!Der TU \"%1\" wurde bereits in einem Autoren-Auftrag angelegt",
//                                                                      newModuleName));
//                        }
//                        messages.add("\n");
//                        messages.add(TranslationHandler.translate("!!Dieser KG/TU-Knoten (%1/%2) ist damit belegt und kann nicht gewählt werden!",
//                                                                  kgTuId.getKg(), kgTuId.getTu()));
//                    }
//                } else {
//                    if (messages != null) {
//                        messages.add(TranslationHandler.translate("!!Kein aktiver Autoren-Auftrag"));
//                    }
//                }
        } else {
            // kein Eintrag in ReservedPK
            return false;
        }
        return true;
    }

    /**
     * Modulnummer aus den Eingaben bilden (OHNE laufende Nummer am Ende)
     *
     * @param productId
     * @param kgTuId
     * @return
     */
    public static String buildKgTuModuleNumberWithoutSerial(iPartsProductId productId, KgTuId kgTuId) {
        StringBuilder str = new StringBuilder();
        str.append(productId.getProductNumber());
        if (kgTuId != null) {
            str.append(IPARTS_MODULE_NAME_DELIMITER);
            str.append(kgTuId.getKg());
            str.append(IPARTS_MODULE_NAME_DELIMITER);
            str.append(kgTuId.getTu());
        }
        return str.toString();
    }

    public static String createStandardModuleName(iPartsProductId productId, KgTuId kgTuId) {
        return buildKgTuModuleNumberWithoutSerial(productId, kgTuId) + IPARTS_MODULE_NAME_DELIMITER + formatModuleSerialNumber(1);
    }

    /**
     * Modulnummer mit laufender Nummer (DB-Abfrage) bilden
     *
     * @param productId
     * @param einPasId
     * @return
     */
    public static String buildEinPasModuleNumber(iPartsProductId productId, EinPasId einPasId, EtkProject project) {
        return makeEinPASModuleNumber(productId, einPasId, project, true);
    }

    /**
     * Modulnummer mit der zuletzt erzeugten laufenden Nummer (DB-Abfrage) bilden
     *
     * @param productId
     * @param einPasId
     * @return
     */
    public static String buildEinPasModuleNumberForLatestModule(iPartsProductId productId, EinPasId einPasId, EtkProject project) {
        return makeEinPASModuleNumber(productId, einPasId, project, true);
    }

    private static String makeEinPASModuleNumber(iPartsProductId productId, EinPasId einPasId, EtkProject project, boolean makeNewModule) {
        StringBuilder str = new StringBuilder(buildEinPasModuleNumberWithoutSerial(productId, einPasId));
        String lfdNr;
        if (makeNewModule) {
            lfdNr = calculateModuleNumber(einPasId, str.toString(), project);
        } else {
            lfdNr = retrieveLatestExistingModuleNumberAsString(einPasId, str.toString(), project);
        }
        str.append(IPARTS_MODULE_NAME_DELIMITER);
        str.append(lfdNr);
        return str.toString();
    }


    /**
     * Modulnummer mit laufender Nummer (DB-Abfrage) bilden
     *
     * @param productId
     * @param kgTuId
     * @return
     */
    public static String buildKgTuModuleNumber(iPartsProductId productId, KgTuId kgTuId, EtkProject project) {
        return makeKgTuModuleNumber(productId, kgTuId, project, true);
    }

    /**
     * Modulnummer mit der zuletzt erzeugten laufenden Nummer (DB-Abfrage) bilden
     *
     * @param productId
     * @param kgTuId
     * @return
     */
    public static String buildKgTuModuleNumberForLatestModule(iPartsProductId productId, KgTuId kgTuId, EtkProject project) {
        return makeKgTuModuleNumber(productId, kgTuId, project, false);
    }

    private static String makeKgTuModuleNumber(iPartsProductId productId, KgTuId kgTuId, EtkProject project, boolean makeNewModule) {
        StringBuilder str = new StringBuilder(buildKgTuModuleNumberWithoutSerial(productId, kgTuId));
        String lfdNr;
        if (makeNewModule) {
            lfdNr = calculateModuleNumber(kgTuId, str.toString(), project);
        } else {
            lfdNr = retrieveLatestExistingModuleNumberAsString(kgTuId, str.toString(), project);
        }
        str.append(IPARTS_MODULE_NAME_DELIMITER);
        str.append(lfdNr);
        return str.toString();
    }


    /**
     * nächste laufende Nummer aus DB-Abfrage bestimmen
     *
     * @param namePrefix
     * @return
     */
    public static String calculateModuleNumber(HierarchicalIDWithType einPasOrKgTUId, String namePrefix, EtkProject project) {
        int resultNumber = 1;
        // build laufende Nummer
        DBDataObjectAttributesList attributeList = findExistingModules(einPasOrKgTUId, namePrefix, project);
        if (!attributeList.isEmpty()) {
            String moduleNo = attributeList.get(attributeList.size() - 1).get(FIELD_DM_MODULE_NO).getAsString();
            int lastNum = extractModuleNumberSerial(moduleNo);
            if (lastNum >= 0) {
                resultNumber = lastNum + 1;
            }
        }
        return formatModuleSerialNumber(resultNumber);
    }

    /**
     * Bestimmt die zuletzt erzeugte laufende Nummer des übergebenen Moduls als String mit vorne aufgefüllten "0"
     *
     * @param einPasOrKgTUId
     * @param namePrefix
     * @param project
     * @return
     */
    public static String retrieveLatestExistingModuleNumberAsString(HierarchicalIDWithType einPasOrKgTUId, String namePrefix, EtkProject project) {
        int result = retrieveLatestExistingModuleNumber(einPasOrKgTUId, namePrefix, project);
        return formatModuleSerialNumber(result);
    }

    /**
     * Bestimmt die zuletzt erzeugte laufende Nummer des übergebenen Moduls
     *
     * @param einPasOrKgTUId
     * @param namePrefix
     * @param project
     * @return
     */
    public static int retrieveLatestExistingModuleNumber(HierarchicalIDWithType einPasOrKgTUId, String namePrefix, EtkProject project) {
        int resultNumber = 1;
        // build laufende Nummer
        DBDataObjectAttributesList attributeList = findExistingModules(einPasOrKgTUId, namePrefix, project);
        if (!attributeList.isEmpty()) {
            String lastName = attributeList.get(attributeList.size() - 1).get(FIELD_DM_MODULE_NO).getAsString();
            int lastNum = extractModuleNumberSerial(lastName);
            if (lastNum >= 0) {
                resultNumber = lastNum;
            }
        }
        return resultNumber;
    }


    /**
     * laufende Nummer aus einer Modulnummer bestimmen
     *
     * @param moduleName
     * @return
     */
    public static int extractModuleNumberSerial(String moduleName) {
        int resultNumber = -1;
        String[] parts = moduleName.split(IPARTS_MODULE_NAME_DELIMITER);
        if (parts.length > 1) {
            String lastNo = parts[parts.length - 1];
            if (StrUtils.isInteger(lastNo)) {
                int lastNum = Integer.valueOf(lastNo);
                resultNumber = lastNum;
            }
        }
        return resultNumber;
    }

    /**
     * DB-Abfrage, um neue Laufende Nummer bestimmen zu können
     *
     * @param einPasOrKgTUId
     * @param namePrefix
     * @return
     */
    public static DBDataObjectAttributesList findExistingModules(HierarchicalIDWithType einPasOrKgTUId, String namePrefix, EtkProject project) {
        String[] fields = new String[]{ FIELD_DM_MODULE_NO };

        String searchString = namePrefix + IPARTS_MODULE_NAME_DELIMITER + "*";

        DBDataObjectAttributesList attributeList = project.getEtkDbs().getAttributesList(TABLE_DA_MODULE,
                                                                                         fields,
                                                                                         null,
                                                                                         null,
                                                                                         fields,
                                                                                         new String[]{ searchString },
                                                                                         null, null, false, -1);

        if (einPasOrKgTUId == null) {
            // überflüssige ModulNummer aussortieren
            // (das sind Nummern mit KG/TU oder EinPas-Id) in der Form <Prefix>_<KG>_<TU>
            // Hier kommt man nur rein, wenn man bei Modul erstellen die Auswahl der KG/TU oder die Auswahl der EinPas überspringt
            for (int lfdNr = attributeList.size() - 1; lfdNr >= 0; lfdNr--) {
                String moduleNo = attributeList.get(lfdNr).get(FIELD_DM_MODULE_NO).getAsString();
                String[] helper = moduleNo.split(IPARTS_MODULE_NAME_DELIMITER);
                if (helper.length > 2) {
                    attributeList.remove(lfdNr);
                }
            }
        }
        attributeList.sortBetterSort(fields);
        return attributeList;
    }

    /**
     * laufende Nummer formatieren
     *
     * @param num
     * @return
     */
    public static String formatModuleSerialNumber(int num) {
        return StrUtils.prefixStringWithCharsUpToLength(num + "", '0', IPARTS_MODULE_SERIAL_NUMBER_LENGTH);
    }

    /**
     * Liefert den Dokumentationstyp für das Modul vom übergebenen Stücklisteneintrag zurück.
     *
     * @param partListEntry
     * @return
     */
    public static iPartsDocumentationType getDocumentationTypeFromPartListEntry(EtkDataPartListEntry partListEntry) {
        // Fallback auf DIALOG
        iPartsDocumentationType docType = iPartsDocumentationType.DIALOG;
        if (partListEntry.getOwnerAssembly() instanceof iPartsDataAssembly) {
            docType = ((iPartsDataAssembly)partListEntry.getOwnerAssembly()).getDocumentationType();
        }
        return docType;
    }

    public static void initMengeFromQuantunit(EtkDataPart part, EtkDataPartListEntry destPartListEntry) {
        if (!part.getFieldValue(FIELD_M_QUANTUNIT).equals(QUANTUNIT_STUECK)) {
            destPartListEntry.setFieldValue(FIELD_K_MENGE, MENGE_NACH_BEDARF, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * DAIMLER-7812 Teilepositionen mit ME != Stück ("01") und Mengenkennzeichen = Z haben nach der Übernahme aus der DIALOG Konstruktion die Menge "NB"
     * Teilepositionen mit Menge "99" haben nach der Übernahme aus der DIALOG Konstruktion immer die Menge "NB"
     *
     * @param part
     * @param destPartListEntry
     */
    public static void adaptQuantity(EtkDataPart part, EtkDataPartListEntry destPartListEntry) {
        boolean oldLogLoadFieldIfNeeded = part.isLogLoadFieldIfNeeded();
        try {
            part.setLogLoadFieldIfNeeded(false); // M_QUANTUNIT ist beim part nicht unbedingt geladen -> Meldung beim Nachladen unterdrücken
            if (destPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_QUANTITY).equals("99")) {
                destPartListEntry.setFieldValue(FIELD_K_MENGE, MENGE_NACH_BEDARF, DBActionOrigin.FROM_EDIT);
            } else if (!part.getFieldValue(FIELD_M_QUANTUNIT).equals(QUANTUNIT_STUECK) && destPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_QUANTITY_FLAG).equals("Z")) {
                destPartListEntry.setFieldValue(FIELD_K_MENGE, MENGE_NACH_BEDARF, DBActionOrigin.FROM_EDIT);
            }
        } finally {
            part.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
        }
    }

    /**
     * Aktualisiert alle (virtuellen) Code-Felder für den übergebenen Stücklisteneintrag aufgrund der übergebenen Code-Regel
     * (außer der Original Code-Regel im Feld {@code K_CODES}).
     *
     * @param codeString
     * @param partListEntry
     * @param updateVirtualFields Flag, ob die virtuellen Code-Felder auch aktualisiert werden sollen
     * @param logMessages
     */
    public static void updateCodeFieldsForPartListEntry(String codeString, EtkDataPartListEntry partListEntry, boolean updateVirtualFields,
                                                        List<String> logMessages) {
        if (partListEntry != null) {
            // AS- und Zubehörcodes aus Code-String entfernen
            Set<String> codesRemoved = new HashSet<>();
            String reducedCodeString = DaimlerCodes.reduceCodeString(partListEntry.getEtkProject(), codeString, codesRemoved,
                                                                     logMessages);
            // logFieldIfNeeded
            boolean oldLogLoadFieldIfNeed = partListEntry.isLogLoadFieldIfNeeded();
            partListEntry.setLogLoadFieldIfNeeded(false);
            try {
                setCodeReductionFlags(partListEntry, codesRemoved);

                // Um AS- und Zubehörcodes reduzierte Code-String in das Feld K_CODES_REDUCED schreiben
                partListEntry.setFieldValue(FIELD_K_CODES_REDUCED, reducedCodeString, DBActionOrigin.FROM_EDIT);

                if (updateVirtualFields) {
                    // Um AS- und Zubehörcodes reduzierte Code-Regeln sowie erweiterte Code-Regeln aufgrund von Ereignissen von
                    // ereignisgesteuerten Baureihen am Stücklisteneintrag neu berechnen bzw. entfernen, damit diese später neu
                    // berechnet werden
                    if (partListEntry instanceof iPartsDataPartListEntry) {
                        ((iPartsDataPartListEntry)partListEntry).calculateRetailCodesReducedAndFiltered(iPartsFilter.get());
                        partListEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_WITH_EVENTS);
                        partListEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_CHANGED_CODE);
                    }
                }
            } finally {
                partListEntry.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeed);
            }
        }
    }

    /**
     * Flags an Stücklisteneintrag für Codestring-Reduktion setzen
     *
     * @param partListEntry
     * @param codesRemoved
     */
    public static void setCodeReductionFlags(EtkDataPartListEntry partListEntry, Set<String> codesRemoved) {
        if (partListEntry == null) {
            return;
        }
        boolean asCodesRemoved = false, accessoryCodesRemoved = false;
        if ((codesRemoved != null) && !codesRemoved.isEmpty()) {
            Set<String> asCodesToRemove = iPartsAccAndAsCodeCache.getInstance(partListEntry.getEtkProject()).getAllAsCodes();
            Set<String> accessoryCodesToRemove = iPartsAccAndAsCodeCache.getInstance(partListEntry.getEtkProject()).getAllAccCodes();
            for (String codeRemoved : codesRemoved) {
                if (asCodesToRemove.contains(codeRemoved)) {
                    asCodesRemoved = true;
                }
                if (accessoryCodesToRemove.contains(codeRemoved)) {
                    accessoryCodesRemoved = true;
                }
            }
        }
        partListEntry.setFieldValueAsBoolean(FIELD_K_AS_CODE, asCodesRemoved, DBActionOrigin.FROM_EDIT);
        partListEntry.setFieldValueAsBoolean(FIELD_K_ACC_CODE, accessoryCodesRemoved, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Berechnet auf Basis der vorhandenene Stücklistenpositionen in der Ziel-Stückliste die nächste Sequenznummer,
     * den maximalen Hotspot-Wert sowie die nächste mögliche laufende Nummer.
     * <p>
     * Zusätzlich werden alle bestehenden Stücklistenpositionen auf Basis ihrer Source-GUID in der übergebenen Map
     * abgelegt (um später besser auf die Positionen zugreifen zu können).
     *
     * @param destPartList
     * @param calculatedDestLfdNr
     * @param calculatedSeqNo
     * @param calculatedMaxHotSpot
     * @param destPartListSourceGUIDMap
     * @param hotspotSuggestions
     * @param moduleType
     * @param sourceContext
     * @param genVoHotspots
     */
    public static void preprocessDestPartListEntries(DBDataObjectList<EtkDataPartListEntry> destPartList, VarParam<Integer> calculatedDestLfdNr,
                                                     VarParam<String> calculatedSeqNo, VarParam<String> calculatedMaxHotSpot,
                                                     HashMap<String, List<EtkDataPartListEntry>> destPartListSourceGUIDMap,
                                                     HashMap<String, String> hotspotSuggestions, iPartsModuleTypes moduleType,
                                                     String sourceContext, Map<String, String> genVoHotspots) {
        for (EtkDataPartListEntry destPartListEntry : destPartList) {
            calculatedDestLfdNr.setValue(Math.max(calculatedDestLfdNr.getValue(), Integer.valueOf(destPartListEntry.getAsId().getKLfdnr())));

            // Maximalwert für Sequenznumner ermitteln
            String seqNr = destPartListEntry.getFieldValue(EtkDbConst.FIELD_K_SEQNR);
            if (SortBetweenHelper.isGreater(seqNr, calculatedSeqNo.getValue())) {
                calculatedSeqNo.setValue(seqNr);
            }

            // Positionen anhand ihrer Source-GUID in der übergebenen Map ablegen
            String sourceGUID = destPartListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
            String pos = destPartListEntry.getFieldValue(iPartsConst.FIELD_K_POS);
            if (((moduleType == null) && StrUtils.isEmpty(sourceContext))
                || ((moduleType != null) && destPartListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_TYPE).equals(moduleType.getSourceType().getDbValue())
                    && destPartListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_CONTEXT).equals(sourceContext))) {

                List<EtkDataPartListEntry> destPartListEntries = destPartListSourceGUIDMap.get(sourceGUID);
                if (destPartListEntries == null) {
                    destPartListEntries = new DwList<>();
                    destPartListSourceGUIDMap.put(sourceGUID, destPartListEntries);
                }
                destPartListEntries.add(destPartListEntry);
            }

            if (calculatedMaxHotSpot != null) {
                if (StrUtils.isValid(pos) && SortBetweenHelper.isGreater(pos, calculatedMaxHotSpot.getValue())) {
                    calculatedMaxHotSpot.setValue(pos);
                }
            }

            if (hotspotSuggestions != null) { // für EDS gibt es keine generierten Hotspot Vorschläge
                // Es wird nur der erste gefundene Hotspot pro PV-Schlüssel gespeichert, nachfolgende Treffer werden ignoriert
                // dazu muss die Stückliste in sortierter Reihenfolge durchlaufen werden
                iPartsDialogBCTEPrimaryKey key = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(sourceGUID);
                if (key != null) {
                    String positionKey = key.getPositionKeyWithAA();
                    String hotspot = hotspotSuggestions.get(positionKey);
                    if (hotspot == null) {
                        hotspotSuggestions.put(positionKey, pos);
                    }
                }
            }

            addToGenVoHotspotSuggestions(genVoHotspots, destPartListEntry.getFieldValue(iPartsConst.FIELD_K_POS),
                                         destPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GENVO));
        }

        // Den nächsten Wert für die Sequenznummer ermitteln
        calculatedSeqNo.setValue(SortBetweenHelper.getNextSortValue(calculatedSeqNo.getValue()));
    }

    /**
     * Einen Hotspot Vorschlag anhand des GenVO ermittlen
     * Dazu werden alle in der Stückliste vorhandenen Paare aus GenVO und Hotspot gesammelt.
     * Findet sich zum GenVO des aktuellen Stücklisteneintrags ein Hotspot, wird dieser übernommen.
     * Falls nicht, wird zusätzlich über DA_GENVO_PAIRING geprüft, ob es zum GenVO einen Rechts-Links-Sachverhalt gibt,
     * und dann ggf. von diesem der Hotspot übernommen.
     *
     * @param project
     * @param genVoHotspots gesammelte GenVO-Hotspot Paare der Zielstückliste
     * @param genVo         GenVO des aktuellen Stücklisteneintrags
     * @return der ermittelte Hotspot Vorschlag. Kann auch <code>null</code> sein
     */
    public static String getGenVoHotspotSuggestion(EtkProject project, Map<String, String> genVoHotspots, String genVo) {
        String genVoHotspotSuggestion = null;
        if (StrUtils.isValid(genVo) && (genVoHotspots != null)) {
            genVoHotspotSuggestion = genVoHotspots.get(genVo);
            if (StrUtils.isEmpty(genVoHotspotSuggestion)) {
                Set<String> correspondigGenVOs = iPartsDataGenVoPairingCache.getInstance(project).getCorrespondingGenVOs(genVo);
                for (String correspondigGenVO : correspondigGenVOs) {
                    String leftRightSuggestion = genVoHotspots.get(correspondigGenVO);
                    if (leftRightSuggestion != null) {
                        genVoHotspotSuggestion = leftRightSuggestion;
                        break;
                    }
                }
            }
        }
        return genVoHotspotSuggestion;
    }

    public static String getSuggestedHotSpot(HashMap<String, String> hotspotSuggestions, iPartsDialogBCTEPrimaryKey bctePrimaryKey,
                                             VarParam<String> calculatedMaxHotSpot, EtkProject project,
                                             Map<String, String> genVoHotspots, String genVo) {
        return getSuggestedHotSpot(hotspotSuggestions, bctePrimaryKey, calculatedMaxHotSpot, project, genVoHotspots, genVo, null);
    }

    /**
     * Ermittelt einen Hotspot-Vorschlag auf Basis des aktuellen höchsten Hotspot, bisherige Hotspot-Vorschläge und dem
     * BCTE Schlüssel der aktuellen Stücklistenposition und dem GenVO.
     *
     * @param hotspotSuggestions
     * @param bctePrimaryKey
     * @param calculatedMaxHotSpot
     * @param project
     * @param genVoHotspots
     * @param genVo
     * @return
     */
    public static String getSuggestedHotSpot(HashMap<String, String> hotspotSuggestions, iPartsDialogBCTEPrimaryKey bctePrimaryKey,
                                             VarParam<String> calculatedMaxHotSpot, EtkProject project,
                                             Map<String, String> genVoHotspots, String genVo, Set<String> entriesWithNotRelatedHotSpot) {
        String suggestedHotspot = "";
        // zuerst einen Vorschlag über den BCTE Schlüssel ermitteln
        if (bctePrimaryKey != null) {
            String tempHotspot = hotspotSuggestions.get(bctePrimaryKey.getPositionKeyWithAA());
            if (tempHotspot != null) {
                suggestedHotspot = tempHotspot;
            }
        }
        if (StrUtils.isEmpty(suggestedHotspot)) {
            // Hotspot Vorschlagen über GenVO
            suggestedHotspot = getGenVoHotspotSuggestion(project, genVoHotspots, genVo);
            if (StrUtils.isEmpty(suggestedHotspot)) {
                // falls das nicht klappt die nächste freie Nummer nehmen
                suggestedHotspot = calculatedMaxHotSpot.getValue();
                calculatedMaxHotSpot.setValue(incrementMaxHotspot(calculatedMaxHotSpot.getValue(), HOTSPOT_INCREMENT));
                // Konnte kein HotSpot bestimmt werden, muss man sich die Position für eine spätere Berechnung
                // auf Basis der AS Strukturstufe merken
                if ((bctePrimaryKey != null) && (entriesWithNotRelatedHotSpot != null)) {
                    entriesWithNotRelatedHotSpot.add(bctePrimaryKey.getPositionKeyWithAA());
                }
            }
        }
        //eigentlich darf der Hotspot hier nicht mehr leer sein, aber falls doch als letzten Fallback "?" setzen
        if (StrUtils.isEmpty(suggestedHotspot)) {
            suggestedHotspot = iPartsConst.HOTSPOT_NOT_SET_INDICATOR;
        }
        return suggestedHotspot;
    }

    /**
     * Erhöht den HotSpot Wert um den übergebenen Faktor
     *
     * @param inputHotspot
     * @param factor
     * @return
     */
    public static String incrementMaxHotspot(String inputHotspot, int factor) {
        if (StrUtils.isEmpty(inputHotspot) || StrUtils.isInteger(inputHotspot)) {
            return Integer.toString(StrUtils.strToIntDef(inputHotspot, 0) + factor);
        }
        if (StrUtils.isValid(inputHotspot)) {
            return inputHotspot;
        }
        return iPartsConst.HOTSPOT_NOT_SET_INDICATOR;
    }

    public static String incrementMaxHotspotWithDefaultIncrement(String maxHotspot) {
        return incrementMaxHotspot(maxHotspot, HOTSPOT_INCREMENT);
    }

    public static void addToGenVoHotspotSuggestions(Map<String, String> genVoHotspots, String pos, String genVo) {
        if (StrUtils.isValid(genVo, pos) && (genVoHotspots != null)) {
            if (!IGNORE_GENVOS.contains(genVo)) {
                String mapPos = genVoHotspots.computeIfAbsent(genVo, k -> pos);
                // immer nur den kleinsten Wert pro GenVo merken
                if (Utils.toSortString(pos).compareTo(Utils.toSortString(mapPos)) < 0) {
                    genVoHotspots.put(genVo, pos);
                }
            }
        }
    }

    public static boolean partListEntryAlreadyExistInDestPartList(HashMap<String, List<EtkDataPartListEntry>> destPartListSourceGUIDMap,
                                                                  String sourceGUID, String hotspot) {
        // Überprüfen, ob der sourcePartListEntry bereits in der Ziel-Stückliste enthalten ist
        boolean partlistEntryExists = false;
        List<EtkDataPartListEntry> existingPartlistEntries = destPartListSourceGUIDMap.get(sourceGUID);
        if (existingPartlistEntries != null) {
            for (EtkDataPartListEntry existingPartlistEntry : existingPartlistEntries) {
                if (existingPartlistEntry.getFieldValue(FIELD_K_POS).equals(hotspot)) {
                    partlistEntryExists = true;
                    break;
                }
            }

            if (partlistEntryExists) {
                return true;
            }
        }
        return false;
    }

    public static boolean partListEntryAlreadyExistInDestPartList(HashMap<String, List<EtkDataPartListEntry>> destPartListSourceGUIDMap,
                                                                  String sourceGUID) {
        // Überprüfen, ob der sourcePartListEntry bereits in der Ziel-Stückliste enthalten ist
        List<EtkDataPartListEntry> existingPartlistEntries = destPartListSourceGUIDMap.get(sourceGUID);
        if (existingPartlistEntries != null) {
            return true;
        }
        return false;
    }

    /**
     * Setzt den übergebenen Hotspot und bestimmt eine passende Sequenznummer, die ebenfalls gesetzt wird.
     * Wenn hotspot und sourceGUID leer sind, kommt der Stücklisteneintrag auf die letzte Position.
     *
     * @param destPartListEntry
     * @param hotspot
     * @param sourceGUID
     * @param destPartList
     * @param finalSeqNr
     * @param sourcePartListEntriesToTransferFiltered
     */
    public static void setHotSpotAndNextSequenceNumber(EtkDataPartListEntry destPartListEntry, String hotspot,
                                                       String sourceGUID, DBDataObjectList<EtkDataPartListEntry> destPartList,
                                                       String finalSeqNr, List<EtkDataPartListEntry> sourcePartListEntriesToTransferFiltered) {
        // Hotspot setzen
        destPartListEntry.setAttributeValue(iPartsConst.FIELD_K_POS, hotspot, DBActionOrigin.FROM_EDIT);

        // SequenceNumber des neuen Eintrag ermitteln und setzen
        EditSortStringforSourceGUIDHelper searchHelper = new EditSortStringforSourceGUIDHelper();
        searchHelper.setCompareOne(sourceGUID, hotspot);
        String seqNumber = null;
        if (!searchHelper.getSortStringforSourceGUIDOne().isEmpty()) {
            seqNumber = findDestinationSequenceNumberForSortedPartlist(destPartList, sourcePartListEntriesToTransferFiltered,
                                                                       hotspot, searchHelper, null);
        }
        if (seqNumber == null) {
            // wenn keine Sequenznummer bestimmt werden konnte wird der Stücklisteneintrag hinten angehängt
            seqNumber = finalSeqNr;
        }
        destPartListEntry.setFieldValue(EtkDbConst.FIELD_K_SEQNR, seqNumber, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Setzt den übergebenen Hotspot und bestimmt eine passende Sequenznummer, die ebenfalls gesetzt wird.
     * Wenn hotspot leer ist wird nach Materialnummer sortiert. Der Eintrag kommt dann unter den letzten Eintrag mit gleicher
     * Materialnummer.
     *
     * @param destPartListEntry
     * @param hotspot
     * @param destPartList
     * @param finalSeqNr
     * @param sourcePartListEntriesToTransferFiltered
     */
    public static void setHotSpotAndNextSequenceNumberELDAS(EtkDataPartListEntry destPartListEntry, String hotspot,
                                                            DBDataObjectList<EtkDataPartListEntry> destPartList,
                                                            String finalSeqNr, List<EtkDataPartListEntry> sourcePartListEntriesToTransferFiltered) {
        // Hotspot setzen
        destPartListEntry.setAttributeValue(iPartsConst.FIELD_K_POS, hotspot, DBActionOrigin.FROM_EDIT);

        // SequenceNumber des neuen Eintrag ermitteln und setzen
        EditSortStringforSourceGUIDHelper searchHelper = new EditSortStringforSourceGUIDHelper();
        searchHelper.setCompareOne("", hotspot);
        String matNr = null;
        if (searchHelper.getSortStringforSourceGUIDOne().isEmpty()) {
            // wenn der Hotspot leer war stattdessen die Materialnummer verwenden
            matNr = destPartListEntry.getPart().getAsId().getMatNr();
        }

        String seqNumber = null;
        if (!searchHelper.getSortStringforSourceGUIDOne().isEmpty() || (matNr != null && !matNr.isEmpty())) {
            seqNumber = findDestinationSequenceNumberForSortedPartlist(destPartList, sourcePartListEntriesToTransferFiltered,
                                                                       hotspot, searchHelper, matNr);
        }
        if (seqNumber == null) {
            // wenn keine Sequenznummer bestimmt werden konnte wird der Stücklisteneintrag hinten angehängt
            seqNumber = finalSeqNr;
        }
        destPartListEntry.setFieldValue(EtkDbConst.FIELD_K_SEQNR, seqNumber, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Der SortString besteht aus Hotspot | GUID. Es kann auch bei bereits existierenden Einträgen passieren dass der
     * Hotspot leer ist. Dann beginnt der SortString mit | was beim sortieren immer größer ist als eine Zahl.
     * Fall die SourceGUID leer ist, besteht der SortString nur aus dem Hotspot.
     * Um sicher zu stellen dass z.B. 190 nach 19 kommt wird toSortString auf den Hotspot angewendet.
     *
     * @param sourceGUID
     * @param hotspot
     * @return SortString im Normalfall Hotspot | GUID
     */
    public static String getSortStringforSourceGUID(String sourceGUID, String hotspot) {
        String sortString = "";

        if (!hotspot.isEmpty()) {
            sortString = Utils.toSortString(hotspot); // ggf. mit 0 auffüllen damit 190 auch nach 19 kommt
        }

        if (!sourceGUID.isEmpty()) {
            iPartsDialogBCTEPrimaryKey key = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(sourceGUID);
            if (key != null) {
                sortString += "|" + key.toString("|", true);
            }
        }
        return sortString;
    }

    private static String findDestinationSequenceNumberForSortedPartlist(DBDataObjectList<EtkDataPartListEntry> destPartList,
                                                                         List<EtkDataPartListEntry> partListEntriesForTransfer,
                                                                         String hotspotForCurrentEntry,
                                                                         EditSortStringforSourceGUIDHelper searchHelper,
                                                                         String matNr) {
        // diese Liste entspricht dem Endzustand der Stückliste also enthält auch alle bis zu dieser Iteration eingefügten
        // Stücklisteneinträge und ist nach Sequenznummer sortiert
        List<EtkDataPartListEntry> sortedPartlist = new DwList<>(destPartList.getAsList());
        sortedPartlist.addAll(partListEntriesForTransfer);
        Collections.sort(sortedPartlist, new Comparator<EtkDataPartListEntry>() {
            @Override
            public int compare(EtkDataPartListEntry o1, EtkDataPartListEntry o2) {
                // toSortString verwenden, damit die Zahlen korrekt mit 0 aufgefüllt werden
                String o1SeqNr = Utils.toSortString(o1.getFieldValue(FIELD_K_SEQNR));
                String o2SeqNr = Utils.toSortString(o2.getFieldValue(FIELD_K_SEQNR));
                return o1SeqNr.compareTo(o2SeqNr);
            }
        });

        int endIndex;
        if (matNr == null) {
            // Suche mit BCTE-Key
            List<EtkDataPartListEntry> bcteSortedPartlist = new DwList<>(destPartList.getAsList());
            // Die Stückliste mit Hilfe des SortStringforSourceGUIDHelper sortieren
            bcteSortedPartlist.addAll(partListEntriesForTransfer);
            Collections.sort(bcteSortedPartlist, (o1, o2) -> {
                EditSortStringforSourceGUIDHelper helper = new EditSortStringforSourceGUIDHelper();
                helper.setCompareOne(o1.getFieldValue(FIELD_K_SOURCE_GUID), o1.getFieldValue(FIELD_K_POS));
                helper.setCompareTwo(o2.getFieldValue(FIELD_K_SOURCE_GUID), o2.getFieldValue(FIELD_K_POS));
                return helper.compareOneWithTwo();
            });

            endIndex = -1;
            EditSortStringforSourceGUIDHelper lastSortStringHelper = new EditSortStringforSourceGUIDHelper();
            // den ersten Eintrag finden der einen größeren Sortierstring hat. Der Neue Eintrag kommt dann nach dessen Vorgänger
            for (EtkDataPartListEntry destEntry : bcteSortedPartlist) {
                // Im searchHelper befindet sich schon der sortString für den einzufügenden Stücklisteneintrag
                // jetzt noch den sortString des Stücklisteneintrags in der Zielstückliste hinzufügen
                searchHelper.setCompareTwo(destEntry.getFieldValue(FIELD_K_SOURCE_GUID), destEntry.getFieldValue(FIELD_K_POS));
                if (searchHelper.compareTwoWithOne() > 0) {
                    if (lastSortStringHelper.getSortStringforSourceGUIDTwo().isEmpty()) {
                        endIndex = getEndIndexBefore(sortedPartlist, searchHelper);
                    } else {
                        int startIndex = getEndIndexBefore(sortedPartlist, lastSortStringHelper);
                        if ((startIndex + 1) >= sortedPartlist.size()) {
                            endIndex = -1;
                        } else {
                            endIndex = startIndex + 1;
                        }
                    }
                    break;
                }
                lastSortStringHelper = searchHelper.cloneMe();
            }
            if (endIndex == -1) {
                return null;
            }

            // Solange in der echten Ziel-Stückliste vom gefundenen Index aus weitersuchen bis der Hotspot an der Einfügeposition
            // größer oder gleich ist als der Hotspot der zu übernehmenden Position. Falls die Ziel-Stückliste nämlich nicht korrekt
            // nach BCTE-Schlüssel sortiert ist, könnte das Einfügen aufgrund des Indexes des zuletzt kleineren Stücklisteneintrags
            // basierend auf der sortierten Stückliste innerhalb von einem falschen Hotspot passieren.
            // Das Einfügen ganz am Ende der Stückliste ist weiter oben bereits durch endIndex = -1 behandelt.
            EditSortStringforSourceGUIDHelper searchHelperHotSpot = new EditSortStringforSourceGUIDHelper();
            searchHelperHotSpot.setCompareOne("", hotspotForCurrentEntry);
            for (int i = endIndex; i < sortedPartlist.size(); i++) {
                EtkDataPartListEntry destEntry = sortedPartlist.get(i);
                searchHelperHotSpot.setCompareTwo("", destEntry.getFieldValue(FIELD_K_POS));
                if (searchHelperHotSpot.compareTwoWithOne() >= 0) {
                    endIndex = i;
                    break;
                }
            }
        } else {
            // beim Einsortieren nach MatNr soll der Eintrag als letzter unterhalb der anderen Einträge mit gleicher MatNr einsortiert werden
            endIndex = sortedPartlist.size();
            for (int i = sortedPartlist.size() - 1; i >= 0; i--) {
                EtkDataPartListEntry destEntry = sortedPartlist.get(i);
                String destMatNr = destEntry.getPart().getAsId().getMatNr();
                if (destMatNr.equals(matNr)) {
                    break;
                }
                endIndex--;
            }
            if (endIndex == 0) {
                // keine passende Position gefunden also ganz unten in die Stückliste einordnen
                return null;
            }
        }

        String endSeqNr = "";
        if (endIndex < sortedPartlist.size()) {
            EtkDataPartListEntry partListEntry = sortedPartlist.get(endIndex);
            if (partListEntry != null) {
                endSeqNr = partListEntry.getFieldValue(FIELD_K_SEQNR);
            }
        }

        if (!endSeqNr.isEmpty()) {
            int startIndex = endIndex - 1;
            // sollte endSeqNr leer sein, wird am Ende der Stückliste eingefügt. Die seqNr dafür wurde schon bestimmt
            String startSeqNr = "";
            if ((startIndex >= 0) && (sortedPartlist.get(startIndex) != null)) {
                EtkDataPartListEntry partListEntry = sortedPartlist.get(startIndex);
                if (partListEntry != null) {
                    startSeqNr = partListEntry.getFieldValue(FIELD_K_SEQNR);
                }
            }
            if (startSeqNr.equals(endSeqNr)) {
                // bei gleichen Werten kann kein Wert dazwischen gefunden werden
                return null;
            }
            return SortBetweenHelper.getSortBetween(startSeqNr, endSeqNr);
        }
        return null;
    }

    /**
     * Der letzte gefundene Such-string ist im searchHelper als zweiter String gespeichert
     * Den Index des Vorgängers suchen
     *
     * @param sortedPartlist
     * @param searchHelper
     * @return
     */
    private static int getEndIndexBefore(List<EtkDataPartListEntry> sortedPartlist, EditSortStringforSourceGUIDHelper searchHelper) {
        int endIndex = 0;
        boolean noBCTE = false;
        if (searchHelper.getGUIDSearchStringSourceTwo().isEmpty()) {
            // Es ist kein BCTE-Schlüssel vorhanden um eindeutig zu bleiben muss
            // man im sortString die K_SOURCE_GUID mir übergeben
            String lastGuid = searchHelper.getGUIDUnformattedSearchStringSourceTwo();
            String lastPos = searchHelper.getHotspotSearchStringSourceTwo();
            searchHelper.setCompareTwo(lastGuid, lastPos);
            noBCTE = true;
        }
        for (EtkDataPartListEntry destEntry : sortedPartlist) {
            String currentBcteKey = destEntry.getFieldValue(FIELD_K_SOURCE_GUID);
            String currentPOS = destEntry.getFieldValue(FIELD_K_POS);
            searchHelper.setCompareOne(currentBcteKey, currentPOS);
            if (noBCTE) {
                if (searchHelper.compareOneWithTwoUnformatted() == 0) {
                    return endIndex;
                }
            } else {
                if (searchHelper.compareOneWithTwo() == 0) {
                    return endIndex;
                }
            }
            endIndex++;
        }
        return -1;
    }

    /**
     * Schließt die Erzeugung einer neuen Stücklistenposition in einer Ziel-Stückliste ab. Die neue Position wird anhand
     * ihrer GUID in der Map mit den anderen Position abgelegt und in der Liste der neuen Positionen abgelegt.
     * Die Hotspot-Vorschläge werden ggf. erweitert. Sequenznummer und laufende Nummer werden ebenfalls erhöht.
     *
     * @param destPartListSourceGUIDMap
     * @param sourceGUID
     * @param destPartListEntry
     * @param hotspot
     * @param bctePrimaryKey
     * @param hotspotSuggestions
     * @param sourcePartListEntriesToTransferFiltered
     * @param destLfdNr
     * @param finalSeqNr
     */
    public static void finishPartListEntryCreation(HashMap<String, List<EtkDataPartListEntry>> destPartListSourceGUIDMap,
                                                   String sourceGUID, EtkDataPartListEntry destPartListEntry, String hotspot,
                                                   iPartsDialogBCTEPrimaryKey bctePrimaryKey, HashMap<String, String> hotspotSuggestions,
                                                   List<EtkDataPartListEntry> sourcePartListEntriesToTransferFiltered,
                                                   VarParam<Integer> destLfdNr, VarParam<String> finalSeqNr) {
        // neuen Eintrag zur destPartListSourceGUIDMap hinzufügen, damit seine SOURCE_GUID beim nächsten Durchlauf bekannt ist
        List<EtkDataPartListEntry> destPartListEntries = destPartListSourceGUIDMap.get(sourceGUID);
        if (destPartListEntries == null) {
            destPartListEntries = new DwList<>();
            destPartListSourceGUIDMap.put(sourceGUID, destPartListEntries);
        }
        destPartListEntries.add(destPartListEntry);

        // den Eintrag nun auch der Hotspot Vorschlagsliste hinzufügen damit alle passenden
        // Positionsvarianten den gleichen Hotspot-Vorschlag bekommen
        if (!hotspot.equals(iPartsConst.HOTSPOT_NOT_SET_INDICATOR) && (hotspotSuggestions != null)) {
            if (bctePrimaryKey != null) {
                String positionKey = bctePrimaryKey.getPositionKeyWithAA();
                if (!hotspotSuggestions.containsKey(positionKey)) {
                    hotspotSuggestions.put(positionKey, hotspot);
                }
            }
        }

        sourcePartListEntriesToTransferFiltered.add(destPartListEntry);

        String seqNumber = destPartListEntry.getFieldValue(EtkDbConst.FIELD_K_SEQNR);
        // prüfen ob die letzte SeqNummer aktualisiert werden muss
        if (seqNumber.equals(finalSeqNr.getValue()) || SortBetweenHelper.isGreater(seqNumber, finalSeqNr.getValue())) {
            finalSeqNr.setValue(SortBetweenHelper.getNextSortValue(finalSeqNr.getValue()));
        }
    }

    /**
     * Schließt die Erzeugung einer neuen Stücklistenposition in einer Ziel-Stückliste ab. Die neue Position wird anhand
     * ihrer GUID in der Map mit den anderen Position abgelegt und in der Liste der neuen Positionen abgelegt.
     * Die Hotspot-Vorschläge werden ggf. erweitert. Sequenznummer und laufende Nummer werden ebenfalls erhöht.
     *
     * @param destPartListSourceGUIDMap
     * @param sourceGUID
     * @param destPartListEntry
     * @param sourcePartListEntriesToTransferFiltered
     * @param finalSeqNr
     */
    public static void finishPartListEntryCreation(HashMap<String, List<EtkDataPartListEntry>> destPartListSourceGUIDMap,
                                                   String sourceGUID, EtkDataPartListEntry destPartListEntry,
                                                   List<EtkDataPartListEntry> sourcePartListEntriesToTransferFiltered,
                                                   VarParam<String> finalSeqNr) {
        // neuen Eintrag zur destPartListSourceGUIDMap hinzufügen, damit seine SOURCE_GUID beim nächsten Durchlauf bekannt ist
        List<EtkDataPartListEntry> destPartListEntries = destPartListSourceGUIDMap.get(sourceGUID);
        if (destPartListEntries == null) {
            destPartListEntries = new DwList<>();
            destPartListSourceGUIDMap.put(sourceGUID, destPartListEntries);
        }
        destPartListEntries.add(destPartListEntry);

        sourcePartListEntriesToTransferFiltered.add(destPartListEntry);

        String seqNumber = destPartListEntry.getFieldValue(EtkDbConst.FIELD_K_SEQNR);
        // prüfen ob die letzte SeqNummer aktualisiert werden muss
        if (seqNumber.equals(finalSeqNr.getValue()) || SortBetweenHelper.isGreater(seqNumber, finalSeqNr.getValue())) {
            finalSeqNr.setValue(SortBetweenHelper.getNextSortValue(finalSeqNr.getValue()));
        }
    }

    /**
     * Sammelt alle neuen und modifizierten Objekte auf und setzt bestimmte Parameter an den Stücklistenpositionen und
     * der Stückliste zurück.
     *
     * @param dataObjectListToBeSaved
     * @param destPartList
     * @param assemblyIsNew
     * @param destAssembly
     * @param dataReplacementsRetail
     * @param dataIncludePartsRetail
     * @param combinedTextList
     * @param fnCatalogueRefList
     * @param markAssemblyAsModified
     */
    public static void finishModuleModification(EtkDataObjectList dataObjectListToBeSaved, DBDataObjectList<EtkDataPartListEntry> destPartList,
                                                boolean assemblyIsNew, EtkDataAssembly destAssembly,
                                                iPartsDataReplacePartList dataReplacementsRetail,
                                                iPartsDataIncludePartList dataIncludePartsRetail,
                                                iPartsDataCombTextList combinedTextList,
                                                iPartsDataFootNoteCatalogueRefList fnCatalogueRefList, boolean markAssemblyAsModified) {
        // Zuerst die Stücklisteneinträge zu dataObjectListToBeSaved hinzufügen, da beim Serialisieren
        // die Modified-Flags zurückgesetzt werden, damit diese beim Serialisieren vom Modul danach
        // nicht nochmal unterhalb des Moduls serialisiert werden
        // Nicht die Methode iPartsDataAssembly.savePartListEntries() verwenden, weil dadurch jeweils
        // zwei separate ChangeSet-Änderungen stattfinden -> relevante Logik von dort hier duplizieren
        // und die zentrale dataObjectListToBeSaved mit den Stücklisteneinträgen befüllen
        dataObjectListToBeSaved.addAll(destPartList, DBActionOrigin.FROM_EDIT);
        destPartList.resetModifiedFlags();

        // Modul explizit als verändert markieren wenn es nicht sowieso neu ist, damit es im ChangeSet
        // angezeigt wird
        if (!assemblyIsNew && markAssemblyAsModified) {
            destAssembly.getAttributes().markAsModified();
        }

        // Modul, Ersetzungen und Mitlieferteile zu dataObjectListToBeSaved hinzufügen
        dataObjectListToBeSaved.add(destAssembly, DBActionOrigin.FROM_EDIT);
        addObjectsIfExist(dataObjectListToBeSaved, dataReplacementsRetail);
        addObjectsIfExist(dataObjectListToBeSaved, dataIncludePartsRetail);
        addObjectsIfExist(dataObjectListToBeSaved, combinedTextList);
        addObjectsIfExist(dataObjectListToBeSaved, fnCatalogueRefList);

        // Alle Werkseinsatzdaten für den Retail MIT Berücksichtigung von Ersetzungen müssen für
        // alle Stücklisteneinträge neu berechnet werden
        for (EtkDataPartListEntry partListEntry : destPartList) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;
                iPartsPartListEntry.clearReplacements();
                iPartsPartListEntry.clearFactoryDataForRetail();
            }
        }
        destAssembly.ensureAllAdditionalDataIsLoaded(destPartList, null);
    }


    public static void finishModuleModificationEDS(EtkDataObjectList dataObjectListToBeSaved,
                                                   DBDataObjectList<EtkDataPartListEntry> destPartList, boolean assemblyIsNew,
                                                   EtkDataAssembly destAssembly, iPartsDataCombTextList combinedTextList,
                                                   iPartsDataFootNoteCatalogueRefList fnCatalogueRefList, boolean markAssemblyAsModified) {
        // Zuerst die Stücklisteneinträge zu dataObjectListToBeSaved hinzufügen, da beim Serialisieren
        // die Modified-Flags zurückgesetzt werden, damit diese beim Serialisieren vom Modul danach
        // nicht nochmal unterhalb des Moduls serialisiert werden
        // Nicht die Methode iPartsDataAssembly.savePartListEntries() verwenden, weil dadurch jeweils
        // zwei separate ChangeSet-Änderungen stattfinden -> relevante Logik von dort hier duplizieren
        // und die zentrale dataObjectListToBeSaved mit den Stücklisteneinträgen befüllen
        dataObjectListToBeSaved.addAll(destPartList, DBActionOrigin.FROM_EDIT);
        destPartList.resetModifiedFlags();

        // Modul explizit als verändert markieren wenn es nicht sowieso neu ist, damit es im ChangeSet
        // angezeigt wird
        if (!assemblyIsNew && markAssemblyAsModified) {
            destAssembly.getAttributes().markAsModified();
        }

        // Modul zu dataObjectListToBeSaved hinzufügen
        dataObjectListToBeSaved.add(destAssembly, DBActionOrigin.FROM_EDIT);
        addObjectsIfExist(dataObjectListToBeSaved, combinedTextList);
        addObjectsIfExist(dataObjectListToBeSaved, fnCatalogueRefList);
    }

    public static void addObjectsIfExist(EtkDataObjectList dataObjectListToBeSaved, EtkDataObjectList objects) {
        if ((dataObjectListToBeSaved != null) && (objects != null) && !objects.isEmpty()) {
            dataObjectListToBeSaved.addAll(objects, DBActionOrigin.FROM_EDIT);
        }
    }

    public static DBDataObjectList<EtkDataPartListEntry> getDestPartList(EtkDataAssembly destAssembly,
                                                                         boolean assemblyIsNew) {
        if (destAssembly.existsInDB()) {
            if (!assemblyIsNew) {
                return destAssembly.getPartListUnfiltered(null);
            } else {
                DBDataObjectList<EtkDataPartListEntry> destPartList = new DBDataObjectList<>();
                destAssembly.setChildren(EtkDataAssembly.CHILDREN_NAME_PART_LIST_ENTRIES, destPartList);
                return destPartList;
            }
        } else {
            return null;
        }
    }

    /**
     * Aktualisiert alle Ersetzungen und davon abhängigen Daten (Werkseinsatzdaten und Fußnoten) sowie Fehlerorte für die
     * übergebenen Hotspots in dem übergebenen {@link AssemblyListFormIConnector} falls es sich um eine Stückliste mit Doku-Methode
     * DIALOG und bzgl. Ersetzungen um eine versorgungsrelevante Baureihe handelt.
     *
     * @param connector
     * @param hotspots
     */
    public static void updateReplacementsAndFailLocationsForPLEsForHotspots(AssemblyListFormIConnector connector, Set<String> hotspots) {
        EtkDataAssembly currentAssembly = connector.getCurrentAssembly();
        if (currentAssembly instanceof iPartsDataAssembly) {
            // Doku-Methode DIALOG?
            iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)currentAssembly;
            if (!iPartsAssembly.getDocumentationType().isPKWDocumentationType()) {
                return;
            }

            // Für das Aktualisieren der Ersetzungen: versorgungsrelevante Baureihe?
            boolean updateReplacements = iPartsAssembly.isSeriesRelevantForImport();

            boolean hotspotFound = false;
            DBDataObjectList<EtkDataPartListEntry> partListEntries = iPartsAssembly.getPartListUnfiltered(null);
            for (EtkDataPartListEntry partListEntry : partListEntries) {
                if (partListEntry instanceof iPartsDataPartListEntry) {
                    iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;
                    if (hotspots.contains(iPartsPartListEntry.getFieldValue(FIELD_K_POS))) {
                        hotspotFound = true;
                        if (updateReplacements) {
                            iPartsPartListEntry.clearReplacements();
                            iPartsPartListEntry.clearFactoryDataForRetail();
                            iPartsPartListEntry.clearFootnotes();
                        }

                        iPartsPartListEntry.clearCalculatedFailLocation();
                    }
                }
            }
            iPartsAssembly.ensureAllAdditionalDataIsLoaded(partListEntries, null);

            if (hotspotFound) {
                connector.setPartListEntriesModified();
            }
        }
    }

    /**
     * Speichert bei allen Truck-TUs des übergebebenen {@link iPartsRevisionChangeSet}s alle SAA/BK-Gültigkeiten aller Stücklisteneinträge
     * in der Tabelle {@code DA_MODULES_EINPAS} für die schnellere Filterung direkt in der DB.
     *
     * @param project
     * @param changeSet
     */
    public static void updateAllModuleSAAValiditiesForFilter(EtkProject project, iPartsRevisionChangeSet changeSet) {
        Set<iPartsAssemblyId> assemblyIds = changeSet.getAssemblyIDsByTable(TABLE_KATALOG, 0, false);
        for (iPartsAssemblyId assemblyId : assemblyIds) {
            // Ohne Caches, da nur minimale Daten verwendet werden und die Caches z.B. beim Speichern eines ChangeSets innerhalb
            // einer Transaktion noch gar nicht gelöscht wurden
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId, false);
            if (assembly instanceof iPartsDataAssembly) {
                ((iPartsDataAssembly)assembly).saveAllSAAValiditiesForFilter();
            }
        }
    }

    /**
     * Liefert eine Liste aller nicht-freigegebenen Autoren-Aufträge zurück, die das übergebene Modul gerade bearbeiten.
     *
     * @param assemblyId
     * @param project
     * @return
     */
    public static iPartsDataAuthorOrderList getActiveAuthorOrderListForModule(AssemblyId assemblyId, EtkProject project) {
        iPartsDataChangeSetEntryList changeSetEntryList = new iPartsDataChangeSetEntryList();
        changeSetEntryList.loadForDataObjectIdWithTypeAndChangeSetStatus(project, assemblyId, iPartsChangeSetStatus.IN_PROCESS);
        Set<String> authorOrderGUIDs = new TreeSet<>();
        for (iPartsDataChangeSetEntry dataChangeSetEntry : changeSetEntryList) {
            String authorOrderGUID = dataChangeSetEntry.getFieldValue(FIELD_DAO_GUID);
            if (!authorOrderGUID.isEmpty()) {
                authorOrderGUIDs.add(authorOrderGUID);
            }
        }

        iPartsDataAuthorOrderList authorOrderList = new iPartsDataAuthorOrderList();
        for (String authorOrderGUID : authorOrderGUIDs) {
            authorOrderList.add(new iPartsDataAuthorOrder(project, new iPartsAuthorOrderId(authorOrderGUID)), DBActionOrigin.FROM_DB);
        }
        return authorOrderList;
    }


    /**
     * Datenklasse für die Infos zu einem Sub-Modul einer Fahrzeug-Navigation.
     */
    public static class CarPerspectiveSubModuleData {

        public String modelNumber;
        public String productNumber;
        public String cg; // KG
        public String csg; // TU
    }
}
