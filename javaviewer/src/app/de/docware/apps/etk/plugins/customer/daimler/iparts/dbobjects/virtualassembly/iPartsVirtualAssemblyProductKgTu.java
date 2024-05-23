/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductSAs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductSAsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDatasetWithEntries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.ArrayUtil;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.collections.weak.WeakKeysMap;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Virtuelle Sicht auf die Retaildaten gemappt nach KG/TU inkl. KG/SA
 */
public class iPartsVirtualAssemblyProductKgTu extends iPartsVirtualAssemblyEinPasBase {

    /**
     * Im Retail können die Daten in einer SA unterhalb von einem KG-Knoten angezeigt werden. Die Parentassemblies für
     * diese KG/SA-Knoten werden hier ermittelt
     *
     * @param assemblyId
     * @param filtered
     * @param project
     * @param result     wird hier weiter befüllt
     */
    public static void addParentAssemblyEntriesForKgSaStruct(AssemblyId assemblyId, boolean filtered, EtkProject project,
                                                             List<EtkDataPartListEntry> result) {
        String moduleNumber = assemblyId.getKVari();

        // Die SelectFields können durch den Join weiter unten sowohl aus DA_PRODUCT_SAS als auch aus DA_SA_MODULES kommen
        // und landen einfach alle in den iPartsDataProductSAs DBDataObjects
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_PRODUCT_SAS, FIELD_DPS_KG, false, false));

        // Join von DA_PRODUCT_SAS mit DA_SA_MODULES, um neben den KG-Knoten auch die Module für die SAs zu bekommen
        iPartsDataProductSAsList saList = new iPartsDataProductSAsList();
        saList.searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields, new String[]{ FIELD_DPS_SA_NO },
                                         TABLE_DA_SA_MODULES, new String[]{ FIELD_DSM_SA_NO }, false, false,
                                         new String[]{ TableAndFieldName.make(TABLE_DA_SA_MODULES, FIELD_DSM_MODULE_NO) },
                                         new String[]{ moduleNumber }, false, new String[]{ FIELD_DPS_SA_NO }, false);

        // Alle gültigen SA-Knoten zum Ergebnis hinzufügen
        for (iPartsDataProductSAs dataProductSAs : saList) {
            iPartsProductId productId = new iPartsProductId(dataProductSAs.getAsId().getProductNumber());
            iPartsProduct productForSA = iPartsProduct.getInstance(project, productId);

            if (productForSA.getProductStructuringType() != PRODUCT_STRUCTURING_TYPE.KG_TU) {
                continue; // kein KG/TU-Produkt
            }

            String kg = dataProductSAs.getFieldValue(FIELD_DPS_KG);
            String saNumber = dataProductSAs.getAsId().getSaNumber();
            iPartsVirtualNode virtualKgSaNode = new iPartsVirtualNode(iPartsNodeType.KGSA, new KgSaId(kg, saNumber));
            iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(result, filtered, project,
                                                                                new iPartsVirtualNode(iPartsNodeType.getProductKgTuType(productForSA, project),
                                                                                                      productId),
                                                                                virtualKgSaNode);

            // Bei Aggregate-Produkten auch die Verwendungen in allen Fahrzeug-Produkten hinzufügen, wo dieses Aggregate-Produkt
            // verwendet wird
            if (productForSA.isAggregateProduct(project)) {
                for (iPartsProduct vehicleProduct : productForSA.getVehicles(project)) {
                    if (!vehicleProduct.isStructureWithAggregates() || (vehicleProduct.getProductStructuringType() != PRODUCT_STRUCTURING_TYPE.KG_TU)) {
                        continue; // Produkt zeigt keine Aggregate oder ist kein KG/TU-Produkt
                    }

                    iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(result, filtered, project,
                                                                                        new iPartsVirtualNode(iPartsNodeType.getProductKgTuType(false, true),
                                                                                                              vehicleProduct.getAsId()),
                                                                                        virtualKgSaNode);
                }
            }
        }
    }

    /**
     * Sucht in KG/SA-Knoten nach den Werten aus der Suche.
     *
     * @param optionalRootAssemblyId
     * @param selectFields
     * @param selectValues
     * @param whereFields
     * @param andOrWhereValues
     * @param andOrSearch
     * @param wildCardSettings
     * @param project
     * @param multiLanguageCache
     * @return
     */
    public static iPartsSearchVirtualDataset searchPartListEntriesForKgSaStruct(final AssemblyId optionalRootAssemblyId,
                                                                                final EtkDisplayFields selectFields,
                                                                                final List<String> selectValues,
                                                                                final EtkDisplayFields whereFields,
                                                                                final List<String> andOrWhereValues,
                                                                                final boolean andOrSearch,
                                                                                final WildCardSettings wildCardSettings,
                                                                                EtkProject project,
                                                                                WeakKeysMap<String, String> multiLanguageCache) {
        return new iPartsSearchVirtualDatasetWithEntries(selectFields, project, multiLanguageCache) {
            @Override
            public List<EtkDataPartListEntry> createResultPartListEntries() throws CanceledException {
                List<EtkDataPartListEntry> result = new ArrayList<EtkDataPartListEntry>();
                if (andOrSearch) { // kombinierte Suche wird nicht unterstützt
                    return result;
                }

                List<String> saWhereFields = new ArrayList<String>(3);
                List<String> saWhereValues = new ArrayList<String>(3);

                // Suchwerte für M_BESTNR und M_TEXTNR aus den selectValues raussuchen
                String mBestNr = null;
                String mTextNr = null;
                int selectFieldIndex = 0;
                for (EtkDisplayField selectField : selectFields.getFields()) {
                    if ((mBestNr == null) && selectField.getKey().getName().equals(TableAndFieldName.make(TABLE_MAT, FIELD_M_BESTNR))) {
                        mBestNr = selectValues.get(selectFieldIndex).toUpperCase(); // SA-Nummer ist immer in Großbuchstaben
                    } else if ((mTextNr == null) && selectField.getKey().getName().equals(TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR))) {
                        mTextNr = selectValues.get(selectFieldIndex);
                    }
                    if ((mBestNr != null) && (mTextNr != null)) {
                        break;
                    }
                    selectFieldIndex++;
                }

                // Bedingungen für M_BESTNR und M_TEXTNR
                if (StrUtils.isValid(mBestNr)) {
                    saWhereFields.add(TableAndFieldName.make(TABLE_DA_PRODUCT_SAS, FIELD_DPS_SA_NO));
                    saWhereValues.add(mBestNr);
                }

                boolean searchForSATitle = StrUtils.isValid(mTextNr);
                int saTitleWhereFieldIndex = -1;
                if (searchForSATitle) {
                    saTitleWhereFieldIndex = saWhereFields.size();
                    saWhereFields.add(TableAndFieldName.make(TABLE_DA_SA, FIELD_DS_DESC));
                    saWhereValues.add(mTextNr);
                }

                // Optionale Bedingung für ein Produkt
                String productNumber = iPartsVirtualNode.getProductNumberFromAssemblyId(optionalRootAssemblyId);
                if (productNumber != null) {
                    saWhereFields.add(TableAndFieldName.make(TABLE_DA_PRODUCT_SAS, FIELD_DPS_PRODUCT_NO));
                    saWhereValues.add(productNumber);
                }

                iPartsDataProductSAsList saList = new iPartsDataProductSAsList();
                if (searchForSATitle) { // Join von DA_PRODUCT_SAS mit DA_SAA, um neben den KG-Knoten auch die SA-Titel zu bekommen
                    // Die saSelectFields können durch den Join weiter unten sowohl aus DA_PRODUCT_SAS als auch aus DA_SAA kommen
                    // und landen einfach alle in den iPartsDataProductSAs DBDataObjects
                    EtkDisplayFields saSelectFields = new EtkDisplayFields();
                    saSelectFields.addFeld(new EtkDisplayField(TABLE_DA_PRODUCT_SAS, FIELD_DPS_KG, false, false));
                    saSelectFields.addFeld(new EtkDisplayField(TABLE_DA_SA, FIELD_DS_DESC, true, false));

                    // Nur in der SA-Benennung caseInsensitive suchen
                    boolean[] searchCaseInsensitives = new boolean[saWhereFields.size()];
                    Arrays.fill(searchCaseInsensitives, false);
                    if (saTitleWhereFieldIndex >= 0) {
                        searchCaseInsensitives[saTitleWhereFieldIndex] = true;
                    }

                    saList.searchSortAndFillWithJoin(project, project.getDBLanguage(), saSelectFields,
                                                     ArrayUtil.toStringArray(saWhereFields), ArrayUtil.toStringArray(saWhereValues),
                                                     false, new String[]{ FIELD_DPS_SA_NO }, false, searchCaseInsensitives,
                                                     false, true, false, null, true,
                                                     new EtkDataObjectList.JoinData(TABLE_DA_SA, new String[]{ FIELD_DPS_SA_NO },
                                                                                    new String[]{ FIELD_DS_SA }, false, false));
                } else { // Join nicht notwendig (und sogar kontraproduktiv), da nicht nach SA-Titeln gesucht wird
                    // Bei searchWithWildCardsSortAndFill() dürfen die Tabellennamen nicht in den whereFields enthalten sein
                    String[] saWhereFieldNames = new String[saWhereFields.size()];
                    for (int i = 0; i < saWhereFields.size(); i++) {
                        saWhereFieldNames[i] = TableAndFieldName.getFieldName(saWhereFields.get(i));
                    }

                    saList.searchWithWildCardsSortAndFill(project, saWhereFieldNames, ArrayUtil.toStringArray(saWhereValues),
                                                          new String[]{ FIELD_DPS_SA_NO }, DBDataObjectList.LoadType.COMPLETE,
                                                          DBActionOrigin.FROM_DB);
                }

                // Alle gültigen SA-Knoten als Ergebnis zurückliefern
                // Dazu vom KG-Knoten alle Kinder für die spätere Filterung zurückliefern
                Set<AssemblyId> resultAssemblyIds = new HashSet<>(); // Set mit allen AssemblyIds, um doppeltes Laden zu vermeiden
                for (iPartsDataProductSAs dataProductSAs : saList) {
                    if (Session.currentSessionThreadAppActionCancelled()) {
                        throw new CanceledException(null);
                    }
                    iPartsProductId productId = new iPartsProductId(dataProductSAs.getAsId().getProductNumber());
                    iPartsProduct productForSA = iPartsProduct.getInstance(project, productId);

                    if (productForSA.getProductStructuringType() != PRODUCT_STRUCTURING_TYPE.KG_TU) {
                        continue; // kein KG/TU-Produkt
                    }

                    iPartsVirtualNode[] nodes = new iPartsVirtualNode[2];
                    nodes[0] = new iPartsVirtualNode(iPartsNodeType.getProductKgTuType(productForSA, project), productId);
                    String kg = dataProductSAs.getFieldValue(FIELD_DPS_KG);
                    nodes[1] = new iPartsVirtualNode(iPartsNodeType.KGTU, new KgTuId(kg, ""));
                    AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(nodes), "");
                    if (!resultAssemblyIds.contains(assemblyId)) {
                        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);

                        if (assembly.existsInDB()) {
                            // Alle ungefilterten Unterbaugruppen vom KG/TU Knoten zurückliefern (darunter befindet sich auch
                            // der gesuchte SA-Knoten)
                            result.addAll(assembly.getSubAssemblyEntries(false, null));
                        }
                        resultAssemblyIds.add(assemblyId);
                    }
                }

                return result;
            }
        };
    }

    public iPartsVirtualAssemblyProductKgTu(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        super(project, virtualIds, assemblyId);
    }

    @Override
    public DBDataObjectList<EtkDataPartListEntry> loadPartList(boolean subAssembliesOnly, EtkDisplayFields fields) {
        iPartsProductStructures productStructures = iPartsProductStructures.getInstance(getEtkProject(), (iPartsProductId)(getRootNode().getId()));
        iPartsCatalogNode nodes = productStructures.getCompleteKgTuStructure(getEtkProject(), getRootNode().getType().isProductStructureWithAggregates());
        IdWithType subId = null;
        // Suche den KG/TU bzw. KG/SA-Knoten
        for (int i = getVirtualIds().size() - 1; i > 0; i--) {
            IdWithType iPartsVirtualNodeId = getVirtualIds().get(i).getId();
            if ((iPartsVirtualNodeId instanceof KgTuId) || (iPartsVirtualNodeId instanceof KgSaId)) {
                subId = iPartsVirtualNodeId;
                break;
            }
        }
        // Knoten für subId oder subId == null, dann erste Ebene laden
        return loadVirtualKgTu(nodes, getRootNode(), subId);
    }

    private DBDataObjectList<EtkDataPartListEntry> loadVirtualKgTu(iPartsCatalogNode completeStructure, iPartsVirtualNode rootNode, IdWithType subNode) {
        iPartsCatalogNode nodeParent = null;
        if (subNode != null) {
            if (subNode instanceof KgTuId) {
                nodeParent = completeStructure.getNode((KgTuId)subNode);
            } else if (subNode instanceof KgSaId) {
                nodeParent = completeStructure.getNode((KgSaId)subNode);
            }
        }
        if (nodeParent == null) {
            // Kein subknoten -> nehme den obersten
            nodeParent = completeStructure;
        }

        if (nodeParent == null) {
            // Stückliste nicht mehr da, evtl. von jemandem gelöscht worden
            return new EtkDataPartListEntryList();
        }


        //Jetzt die Childs an diese Baugruppe anfügen
        //DBDataObjectList<EtkDataPartListEntry> result = new DBDataObjectList<EtkDataPartListEntry>();
        DBDataObjectList<EtkDataPartListEntry> result = new EtkDataPartListEntryList();
        int lfdNumber = 0;
        for (iPartsCatalogNode nodeChild : nodeParent.getChildren()) {
            lfdNumber++;
            EtkDataPartListEntry entry = null;
            if (nodeChild.isAssemblyId()) {
                // Hier wird eine ganz normale Baugruppe als Child angezeigt
                AssemblyId childAssemblyId = (AssemblyId)nodeChild.getId();
                entry = createAssemblyChildNode(lfdNumber, childAssemblyId);
            } else if (nodeChild.isKgTuId()) {
                // Hier wird die KG/TU erzeugt
                KgTuId childId = (KgTuId)nodeChild.getId();
                entry = handleCarPerspectiveOrKgTuNode(lfdNumber, rootNode, nodeChild, childId);
            } else if (nodeChild.isKgSaId()) {
                // Hier wird die KG/SA erzeugt
                KgSaId childId = (KgSaId)nodeChild.getId();
                entry = createVirtualNode(lfdNumber, rootNode, new iPartsVirtualNode(iPartsNodeType.KGSA, childId));
            }
            if (entry != null) {
                result.add(entry, DBActionOrigin.FROM_DB);
            }
        }
        return result;
    }

    private EtkDataPartListEntry handleCarPerspectiveOrKgTuNode(int lfdNumber, iPartsVirtualNode rootNode, iPartsCatalogNode nodeChild, KgTuId childId) {
        if (lfdNumber == 1) {
            if (EditModuleHelper.isCarPerspectiveKgTuId(childId) && !nodeChild.getChildren().isEmpty()) {
                iPartsCatalogNode tuNodeChild = nodeChild.getChildren().iterator().next();
                if ((tuNodeChild.isKgTuId()) && !tuNodeChild.getChildren().isEmpty()) {
                    KgTuId tuChildId = (KgTuId)tuNodeChild.getId();
                    if (EditModuleHelper.isCarPerspectiveKgTuId(tuChildId)) {
                        iPartsCatalogNode assemblyNodeChild = tuNodeChild.getChildren().iterator().next();
                        if (assemblyNodeChild.isAssemblyId()) {
                            AssemblyId childAssemblyId = (AssemblyId)assemblyNodeChild.getId();
                            return createAssemblyChildNode(lfdNumber, childAssemblyId);
                        }
                    }
                }
            }
        }
        return createVirtualNode(lfdNumber, rootNode, new iPartsVirtualNode(iPartsNodeType.KGTU, childId));
    }

    @Override
    public DBDataObjectAttributes loadAssemblyHeadAttributes(String[] resultFields) {
        DBDataObjectAttributes result = super.loadAssemblyHeadAttributes(resultFields);
        if (iPartsVirtualNode.isProductKgTuNode(getVirtualIds())) {
            iPartsVirtualNode firstVirtualNode = getRootNode();
            IdWithType firststNodeId = firstVirtualNode.getId();
            if (firststNodeId.getType().equals(iPartsProductId.TYPE)) {
                result.addField(iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_SERIES_NO,
                                ((iPartsProductId)firststNodeId).getProductNumber(), true, DBActionOrigin.FROM_DB);

            }
        }
        return result;
    }

    @Override
    public SubAssemblyState getSubAssemblyState() {
        return SubAssemblyState.HAS_ALWAYS;
    }

    @Override
    public String getOrderNumber() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();

        IdWithType lastNodeId = lastVirtualNode.getId();

        // Die KG/TU und KG/SA werden in dieser Klasse behandelt
        if (lastNodeId instanceof KgTuId) { // KG/TU
            KgTuId kgTuId = (KgTuId)lastNodeId;
            if (kgTuId.isKgNode()) {
                return kgTuId.getKg();
            } else if (kgTuId.isTuNode()) {
                return kgTuId.getTu();
            }
        } else if (lastNodeId instanceof KgSaId) { // KG/SA
            KgSaId kgSaId = (KgSaId)lastNodeId;
            return kgSaId.getSa();
        }
        return super.getOrderNumber();
    }

    @Override
    public String getPartsListType() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof KgTuId) { // KG/TU
            KgTuId kgTuId = (KgTuId)lastNodeId;
            if (kgTuId.isKgNode()) {
                // Teste, ob wir in einem Spezialkatalog sind
                String productNumber = iPartsVirtualNode.getProductNumberFromAssemblyId(getAsId());

                if (productNumber != null) {
                    iPartsProduct product = iPartsProduct.getInstance(getEtkProject(), new iPartsProductId(productNumber));
                    if (product.isSpecialCatalog()) {
                        // Die Spezialkatalog haben einen eigenen Stücklistentyp, dieser KG-Knoten wird aber in der Regel ausgeblendet
                        return PARTS_LIST_TYPE_STRUCT_SPECIAL_CAT_KG;
                    }
                }

                return PARTS_LIST_TYPE_STRUCT_KG;
            }
            if (kgTuId.isTuNode()) {
                return PARTS_LIST_TYPE_STRUCT_TU;
            }
        } else if (lastNodeId instanceof KgSaId) { // KG/SA
            return PARTS_LIST_TYPE_STRUCT_SA;
        }

        return super.getPartsListType();
    }

    @Override
    public EtkMultiSprache getTexts() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();
        if (lastNodeId instanceof KgTuId) { // KG/TU
            iPartsProductStructures productStructures = iPartsProductStructures.getInstance(getEtkProject(), (iPartsProductId)(getRootNode().getId()));

            KgTuId kgTuId = (KgTuId)lastNodeId;
            KgTuNode node = productStructures.getKgTuNode(getEtkProject(), kgTuId);

            if (node != null) {
                return node.getTitle();
            } else {
                return null;
            }
        } else if (lastNodeId instanceof KgSaId) { // KG/SA
            iPartsSA sa = iPartsSA.getInstance(getEtkProject(), new iPartsSAId(((KgSaId)lastNodeId).getSa()));
            return sa.getTitle(getEtkProject());
        } else if (lastNodeId instanceof iPartsProductId) { // Produkt
            iPartsProduct product = iPartsProduct.getInstance(getEtkProject(), (iPartsProductId)lastNodeId);

            String titlePrefix = "";
            if (product.isPSK()) {
                titlePrefix = "PSK: ";
            }
            String titlePostfix = " (KG/TU)";

            // Die Art des Einstiegs in dem mehrsprachigen Feld hinzufügen
            EtkMultiSprache result = new EtkMultiSprache();
            EtkMultiSprache productTitle = product.getProductTitle(getEtkProject());

            List<String> fallbackLanguages = getEtkProject().getDataBaseFallbackLanguages();
            for (String lang : getEtkProject().getConfig().getDatabaseLanguages()) {
                String productText = productTitle.getTextByNearestLanguage(lang, fallbackLanguages);
                if (!product.isRetailRelevantFromDB()) {
                    productText = "<" + productText + "> " + TranslationHandler.translateForLanguage("!!(nicht Retail-relevant)", lang);
                }
                result.setText(lang, titlePrefix + productText + titlePostfix);
            }

            return result;
        }

        return super.getTexts();
    }

    @Override
    public String getPictureName() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();
        if (lastNodeId instanceof KgTuId) { // KG/TU
            iPartsProductStructures productStructures = iPartsProductStructures.getInstance(getEtkProject(), (iPartsProductId)(getRootNode().getId()));
            KgTuId kgTuId = (KgTuId)lastNodeId;

            KgTuNode node = productStructures.getKgTuNode(getEtkProject(), kgTuId);
            if (node != null) {
                return node.getPictureName();
            }
            return "";
        } else if (lastNodeId instanceof KgSaId) { // KG/SA
            return ""; // SAs haben aktuell keine Zusatzgrafiken
        }

        return super.getPictureName();
    }

    @Override
    public void getParentAssemblyEntries(boolean filtered, List<EtkDataPartListEntry> result) {
        if (getParentAssemblyEntriesForParentId(KgTuId.class, iPartsNodeType.KGTU, filtered, result)
            || getParentAssemblyEntriesForParentId(KgSaId.class, iPartsNodeType.KGTU, filtered, result)) {
            return;
        }

        super.getParentAssemblyEntries(filtered, result);
    }
}
