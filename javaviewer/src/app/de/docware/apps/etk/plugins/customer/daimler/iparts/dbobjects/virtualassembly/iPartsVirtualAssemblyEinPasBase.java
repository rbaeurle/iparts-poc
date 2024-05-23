/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.search.model.PartsSearchSqlSelect;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductModules;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductModulesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDatasetAssemblyWithDBDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDatasetWithDBDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDatasetWithEntries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.SessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPas;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuNodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModelElementUsage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModelElementUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModelElementUsageNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.Ops;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.db.DBDataSetCancelable;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.db.etkrecord.EtkRecords;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.ArrayUtil;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.collections.weak.WeakKeysMap;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLQuery;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Basisklasse für die virtuelle Sicht auf die EinPAS-Struktur. Alle die EinPAS-Strukturen leiten davon ab.
 * TODO Der Name ist nicht ganz glücklich, da hier auch KG/TU behandelt wird.
 */

public abstract class iPartsVirtualAssemblyEinPasBase extends iPartsVirtualAssembly implements iPartsConst, EtkDbConst {

    /**
     * Im Retail können die Daten in der EinPAS KG/TU angezeigt werden. Die Parentassemblies für die diese Knoten werden hier ermittelt
     *
     * @param assemblyId
     * @param filtered
     * @param project
     * @param result     wird hier weiter befüllt
     */
    public static void addParentAssemblyEntriesForRetailStructures(AssemblyId assemblyId, boolean filtered, EtkProject project, List<EtkDataPartListEntry> result) {
        String moduleNumber = assemblyId.getKVari();

        // Modul in EinPAS-Struktur pro Produkt (Join von Modul in Produkt auf EinPAS-Struktur pro Modul und Produkt)
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODULES_EINPAS, FIELD_DME_PRODUCT_NO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODULES_EINPAS, FIELD_DME_MODULE_NO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODULES_EINPAS, FIELD_DME_EINPAS_HG, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODULES_EINPAS, FIELD_DME_EINPAS_G, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODULES_EINPAS, FIELD_DME_EINPAS_TU, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_KG, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_TU, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_HM, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_M, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_SM, false, false));
        iPartsDataProductModulesList productModulesEinPASList = new iPartsDataProductModulesList();
        productModulesEinPASList.searchSortAndFillWithJoin(project, null, selectFields, new String[]{ FIELD_DPM_PRODUCT_NO, FIELD_DPM_MODULE_NO },
                                                           TABLE_DA_MODULES_EINPAS, new String[]{ FIELD_DME_PRODUCT_NO, FIELD_DME_MODULE_NO },
                                                           false, false, new String[]{ TableAndFieldName.make(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_MODULE_NO) },
                                                           new String[]{ moduleNumber }, false, null, false);

        // EinPAS-Vaterknoten pro Modul und Produkt zu den ParentAssemblyEntries hinzufügen
        for (iPartsDataProductModules einPasAttributes : productModulesEinPASList) {
            String parentProductNumber = einPasAttributes.getFieldValue(FIELD_DME_PRODUCT_NO);
            iPartsProduct parentProduct = iPartsProduct.getInstance(project, new iPartsProductId(parentProductNumber));

            // an erster Stelle das eigentliche Produkt hinzufügen
            List<iPartsProduct> products = new DwList<>();
            products.add(parentProduct);

            // Aggregate-Produkt eingebaut in Fahrzeug-Produkte müssen ebenfalls als ParentAssemblyEntries hinzugefügt werden
            // (sofern das Fahrzeug-Produkt Aggregate anzeigt)
            for (iPartsProduct vehicleProduct : parentProduct.getVehicles(project)) {
                if (vehicleProduct.isStructureWithAggregates()) {
                    products.add(vehicleProduct);
                }
            }

            String einPasHg = einPasAttributes.getFieldValue(FIELD_DME_EINPAS_HG);
            if (!einPasHg.isEmpty()) { // EinPAS direkt über Edit zugewiesen
                EinPasId virtualEinPasId = new EinPasId(einPasHg,
                                                        einPasAttributes.getFieldValue(FIELD_DME_EINPAS_G),
                                                        einPasAttributes.getFieldValue(FIELD_DME_EINPAS_TU));
                iPartsVirtualNode virtualEinPasNode = new iPartsVirtualNode(iPartsNodeType.EINPAS, virtualEinPasId);
                for (iPartsProduct product : products) {
                    if (product.getProductStructuringType() == PRODUCT_STRUCTURING_TYPE.EINPAS) { // nur für EinPAS-Produkte
                        iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(result, filtered, project,
                                                                                            new iPartsVirtualNode(iPartsNodeType.getProductEinPASType(product, project),
                                                                                                                  product.getAsId()),
                                                                                            virtualEinPasNode);
                    }
                }
            } else {
                String sourceKg = einPasAttributes.getFieldValue(FIELD_DME_SOURCE_KG);
                if (!sourceKg.isEmpty()) { // KG/TU und KG/TU virtuell in EinPAS
                    KgTuId kgTuId = new KgTuId(sourceKg, einPasAttributes.getFieldValue(FIELD_DME_SOURCE_TU));
                    iPartsVirtualNode virtualKgTuNode = new iPartsVirtualNode(iPartsNodeType.KGTU, kgTuId);
                    iPartsVirtualNode virtualEinPASNode = new iPartsVirtualNode(iPartsNodeType.EINPAS, EinPasId.createVirtualEinPasIdForKgTu(kgTuId));
                    for (iPartsProduct product : products) {
                        if (product.getProductStructuringType() == PRODUCT_STRUCTURING_TYPE.KG_TU) { // echten KG/TU-Knoten für KG/TU-Produkte
                            if (EditModuleHelper.isCarPerspectiveKgTuId(kgTuId)) {
                                iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(result, filtered, project,
                                                                                                    new iPartsVirtualNode(iPartsNodeType.getProductKgTuType(product, project),
                                                                                                                          product.getAsId()));
                            } else {
                                iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(result, filtered, project,
                                                                                                    new iPartsVirtualNode(iPartsNodeType.getProductKgTuType(product, project),
                                                                                                                          product.getAsId()),
                                                                                                    virtualKgTuNode);
                            }
                        } else if (product.getProductStructuringType() == PRODUCT_STRUCTURING_TYPE.EINPAS) { // virtuellen KG/TU-Knoten in EinPAS für EinPAS-Produkte
                            iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(result, filtered, project,
                                                                                                new iPartsVirtualNode(iPartsNodeType.getProductEinPASType(product, project),
                                                                                                                      product.getAsId()),
                                                                                                virtualEinPASNode);
                        }
                    }
                }
            }
        }
    }

    /**
     * Sucht alle Stücklisteneinträge mit den entsprechenden Suchwerten in den Modulen der EinPAS und KG/TU-Struktur im Retail,
     * wobei optional eine <i>optionalRootAssemblyId</i> angegeben werden kann, die bei enthaltenem Produkt die Suche
     * erheblich beschleunigt.
     * Außerdem kann über <i>optionalKgTuId</i> ODER <i>optionalEinPasId</i> eine Einschränkung auf einen KG/TU-Knoten
     * bzw. EinPAS-Knoten (und alle darunterliegenden Knoten) gemacht werden sowie über <i>optionalAggregateType</i> auf
     * den Aggregatetyp vom Produkt.
     *
     * @param optionalRootAssemblyId
     * @param optionalKgTuId            Falls gesetzt, muss <i>optionalEinPasId</i> {@code null} sein.
     * @param optionalEinPasId          Falls gesetzt, muss <i>optionalKgTuId</i> {@code null} sein.
     * @param optionalAggregateType
     * @param isSearchValuesDisjunction
     * @param selectFields
     * @param selectValues
     * @param whereFields
     * @param whereValues
     * @param andOrSearch
     * @param project
     * @param multiLanguageCache
     * @param wildCardSettings
     * @param includeAggregates         true, wenn bei Fahrzeug-Produkten auch in Aggregaten gesucht werden soll; normal richtig
     * @param additionalAggregates      Nur gültig bei {@code includeAggregates == true}: Optionale Liste der zusätzlichen
     *                                  Aggregate, die mit durchsucht werden sollen; bei {@code null} wird in allen Aggregate-Produkte
     *                                  gesucht, die für das Fahrzeug-Produkt gültig sind (sofern in einem Fahrzeug-Produkt
     *                                  gesucht wird)
     * @return
     */
    public static iPartsSearchVirtualDataset searchPartListEntriesForRetailStructures(final AssemblyId optionalRootAssemblyId,
                                                                                      final KgTuId optionalKgTuId,
                                                                                      final EinPasId optionalEinPasId,
                                                                                      final String optionalAggregateType,
                                                                                      final boolean isSearchValuesDisjunction,
                                                                                      final EtkDisplayFields selectFields,
                                                                                      final List<String> selectValues,
                                                                                      final EtkDisplayFields whereFields,
                                                                                      final List<String> whereValues,
                                                                                      final boolean andOrSearch, EtkProject project,
                                                                                      WeakKeysMap<String, String> multiLanguageCache,
                                                                                      final WildCardSettings wildCardSettings,
                                                                                      final boolean includeAggregates,
                                                                                      final Collection<iPartsProduct> additionalAggregates) {
        return new iPartsSearchVirtualDatasetWithDBDataset(selectFields, project, multiLanguageCache) {
            @Override
            public DBDataSetCancelable createDBDataSet() throws CanceledException {
                if ((optionalKgTuId != null) && (optionalEinPasId != null)) {
                    Logger.getLogger().throwRuntimeException("iPartsVirtualAssemblyEinPasBase.searchPartListEntriesForRetailStructures: optionalKgTuId and optionalEinPasId must not both be != null!");
                }

                // Bestimmung der Produktnummer anhand einer virtuellen optionalRootAssemblyId
                String productNumber = iPartsVirtualNode.getProductNumberFromAssemblyId(optionalRootAssemblyId);

                // Teilesuche über Stücklistenfelder
                PartsSearchSqlSelect sqlSelect = PartsSearchSqlSelect.buildPartsSearchSqlSelectForSearchByFields(isSearchValuesDisjunction,
                                                                                                                 selectFields, selectValues,
                                                                                                                 whereFields, whereValues,
                                                                                                                 andOrSearch, project,
                                                                                                                 wildCardSettings, project.getDBLanguage());
                SQLQuery query = sqlSelect.getQuery();

                // Module direkt an Produkten
                // Join von gefundenen Stücklisteneinträgen auf eingebaute Module (in allen Produkten)
                Condition moduleCondition = new Condition(TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI), Condition.OPERATOR_EQUALS,
                                                          new Fields(TableAndFieldName.make(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_MODULE_NO)));

                String fieldDpmProductNo = TableAndFieldName.make(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PRODUCT_NO);

                // bei vorhandener Produktnummer diese zusätzlich als Bedingung im Join verwenden
                if (productNumber != null) {
                    AbstractCondition[] conditions = new AbstractCondition[2];

                    Condition productNumberCondition = new Condition(fieldDpmProductNo, Condition.OPERATOR_EQUALS, productNumber);

                    // wenn es sich nicht um ein Aggregate-Produkt handelt, dann müssen auch alle Aggregate dieses Produkts betrachtet werden wenn gefordert
                    iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNumber));
                    if (includeAggregates && (product.isStructureWithAggregates() && !product.isAggregateProduct(project))) {
                        Collection<iPartsProduct> aggregateProducts;
                        if (additionalAggregates != null) {
                            // wenn additionalAggregates nicht null ist, dann werden alle Produkte aus dieser Liste verwendet,
                            aggregateProducts = additionalAggregates;
                        } else {
                            // wenn additionalAggregates null, und includeAggregates true ist werden die Aggregate bestimmt
                            aggregateProducts = product.getAggregates(project);
                        }
                        if (!aggregateProducts.isEmpty()) {
                            AbstractCondition productWithAggsCondition = productNumberCondition;
                            for (iPartsProduct aggregateProduct : aggregateProducts) {
                                productWithAggsCondition = productWithAggsCondition.or(new Condition(fieldDpmProductNo,
                                                                                                     Condition.OPERATOR_EQUALS,
                                                                                                     aggregateProduct.getAsId().getProductNumber()));
                            }
                            conditions[0] = new ConditionList(productWithAggsCondition);
                        } else {
                            conditions[0] = productNumberCondition; // keine Aggregate zum Produkt gefunden
                        }
                    } else {
                        conditions[0] = productNumberCondition; // Aggregate-Produkt bzw. !includeAggregates
                    }
                    conditions[1] = moduleCondition;

                    query.join(new InnerJoin(TABLE_DA_PRODUCT_MODULES, conditions));
                } else {
                    query.join(new InnerJoin(TABLE_DA_PRODUCT_MODULES, moduleCondition));
                }

                if (hasFieldOfTable(selectFields, TABLE_DA_MODULES_EINPAS) || (optionalKgTuId != null) || (optionalEinPasId != null)) {
                    // Join mit DA_MODULES_EINPAS um den Navigationskontext zu ermitteln
                    String fieldDmeProductNo = TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_PRODUCT_NO);
                    Condition productJoinCondition = new Condition(fieldDmeProductNo, Condition.OPERATOR_EQUALS, new Fields(fieldDpmProductNo));

                    String fieldKatalogModuleNo = TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI);
                    String fieldDmeModuleNo = TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_MODULE_NO);
                    Condition moduleJoinCondition = new Condition(fieldKatalogModuleNo, Condition.OPERATOR_EQUALS, new Fields(fieldDmeModuleNo));

                    // Einschränkung über einen KG/TU-Knoten und dessen Unterknoten
                    if (optionalKgTuId != null) {
                        moduleJoinCondition.and(new Condition(TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_KG),
                                                              Condition.OPERATOR_EQUALS, optionalKgTuId.getKg()));
                        if (optionalKgTuId.isTuNode()) {
                            moduleJoinCondition.and(new Condition(TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_TU),
                                                                  Condition.OPERATOR_EQUALS, optionalKgTuId.getTu()));
                        }
                    }

                    // Einschränkung über einen EinPAS-Knoten und dessen Unterknoten
                    if (optionalEinPasId != null) {
                        moduleJoinCondition.and(new Condition(TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_EINPAS_HG),
                                                              Condition.OPERATOR_EQUALS, optionalEinPasId.getHg()));
                        if (!optionalEinPasId.isHgNode()) { // G oder TU
                            moduleJoinCondition.and(new Condition(TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_EINPAS_G),
                                                                  Condition.OPERATOR_EQUALS, optionalEinPasId.getG()));
                            if (optionalEinPasId.isTuNode()) {
                                moduleJoinCondition.and(new Condition(TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_EINPAS_TU),
                                                                      Condition.OPERATOR_EQUALS, optionalEinPasId.getTu()));
                            }
                        }
                    }

                    query.join(new InnerJoin(TABLE_DA_MODULES_EINPAS, productJoinCondition, moduleJoinCondition));
                }

                if (hasFieldOfTable(selectFields, TABLE_DA_PRODUCT) || StrUtils.isValid(optionalAggregateType)) {
                    // Join mit DA_PRODUCT um den Aggregate-Typ zu ermitteln
                    String fieldDpProductNo = TableAndFieldName.make(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO);
                    Condition productJoinCondition = new Condition(fieldDpmProductNo, Condition.OPERATOR_EQUALS, new Fields(fieldDpProductNo));

                    // Einschränkung auf einen Aggregatetyp
                    if (StrUtils.isValid(optionalAggregateType)) {
                        productJoinCondition.and(new Condition(TableAndFieldName.make(TABLE_DA_PRODUCT, FIELD_DP_AGGREGATE_TYPE),
                                                               Condition.OPERATOR_EQUALS, optionalAggregateType));
                    }

                    query.join(new InnerJoin(TABLE_DA_PRODUCT, productJoinCondition));
                }

                return sqlSelect.createAbfrageCancelable();
            }
        };
    }

    /**
     * Sucht alle Stücklisteneinträge mit den entsprechenden Suchwerten in den Modulen von SAs im Retail,
     * wobei optional eine <i>optionalRootAssemblyId</i> angegeben werden kann, die bei enthaltenem Produkt die Suche
     * erheblich beschleunigt.
     *
     * @param optionalRootAssemblyId
     * @param isSearchValuesDisjunction
     * @param selectFields
     * @param selectValues
     * @param whereFields
     * @param whereValues
     * @param andOrSearch
     * @param project
     * @param multiLanguageCache
     * @param wildCardSettings
     * @param includeAggregates         true, wenn bei Fahrzeug-Produkten auch in Aggregaten gesucht werden soll; normal richtig
     * @param additionalAggregates      Nur gültig bei {@code includeAggregates == true}: Optionale Liste der zusätzlichen
     *                                  Aggregate, die mit durchsucht werden sollen; bei {@code null} wird in allen Aggregate-Produkte
     *                                  gesucht, die für das Fahrzeug-Produkt gültig sind (sofern in einem Fahrzeug-Produkt
     *                                  gesucht wird)
     * @return
     */
    public static iPartsSearchVirtualDataset searchPartListEntriesForRetailSAs(final AssemblyId optionalRootAssemblyId,
                                                                               final boolean isSearchValuesDisjunction,
                                                                               final EtkDisplayFields selectFields,
                                                                               final List<String> selectValues,
                                                                               final EtkDisplayFields whereFields,
                                                                               final List<String> whereValues,
                                                                               final boolean andOrSearch, EtkProject project,
                                                                               WeakKeysMap<String, String> multiLanguageCache,
                                                                               final WildCardSettings wildCardSettings,
                                                                               final boolean includeAggregates,
                                                                               final Collection<iPartsProduct> additionalAggregates) {
        return new iPartsSearchVirtualDatasetWithDBDataset(selectFields, project, multiLanguageCache) {
            @Override
            public DBDataSetCancelable createDBDataSet() throws CanceledException {
                // Bestimmung der Produktnummer anhand einer virtuellen optionalRootAssemblyId
                String productNumber = iPartsVirtualNode.getProductNumberFromAssemblyId(optionalRootAssemblyId);

                // Teilesuche über Stücklistenfelder
                PartsSearchSqlSelect sqlSelect = PartsSearchSqlSelect.buildPartsSearchSqlSelectForSearchByFields(isSearchValuesDisjunction,
                                                                                                                 selectFields, selectValues,
                                                                                                                 whereFields, whereValues,
                                                                                                                 andOrSearch, project,
                                                                                                                 wildCardSettings, project.getDBLanguage());
                SQLQuery query = sqlSelect.getQuery();

                // Module von SAs, die an Produkten hängen
                // Join von gefundenen Stücklisteneinträgen auf Module von den SAs
                Condition saModulesCondition = new Condition(TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI), "=",
                                                             new Fields(TableAndFieldName.make(TABLE_DA_SA_MODULES, FIELD_DSM_MODULE_NO)));

                query.join(new InnerJoin(TABLE_DA_SA_MODULES, saModulesCondition));

                // Join von SAs zu den Produkten
                Condition saCondition = new Condition(TableAndFieldName.make(TABLE_DA_PRODUCT_SAS, FIELD_DPS_SA_NO), "=",
                                                      new Fields(TableAndFieldName.make(TABLE_DA_SA_MODULES, FIELD_DSM_SA_NO)));

                String fieldDpsProductNo = TableAndFieldName.make(TABLE_DA_PRODUCT_SAS, FIELD_DPS_PRODUCT_NO);

                // bei vorhandener Produktnummer diese zusätzlich als Bedingung im Join verwenden
                if (productNumber != null) {
                    AbstractCondition[] conditions = new AbstractCondition[2];

                    Condition productNumberCondition = new Condition(fieldDpsProductNo, "=", productNumber);

                    // wenn es sich nicht um ein Aggregate-Produkt handelt, dann müssen auch alle Aggregate dieses Produkts betrachtet werden wenn gefordert
                    iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNumber));
                    if (includeAggregates && (product.isStructureWithAggregates() && !product.isAggregateProduct(project))) {
                        Collection<iPartsProduct> aggregateProducts;
                        if (additionalAggregates != null) {
                            // wenn additionalAggregates nicht null ist, dann werden alle Produkte aus dieser Liste verwendet,
                            aggregateProducts = additionalAggregates;
                        } else {
                            // wenn additionalAggregates null, und includeAggregates true ist werden die Aggregate bestimmt
                            aggregateProducts = product.getAggregates(project);
                        }
                        if (!aggregateProducts.isEmpty()) {
                            AbstractCondition productWithAggsCondition = productNumberCondition;
                            for (iPartsProduct aggregateProduct : aggregateProducts) {
                                productWithAggsCondition = productWithAggsCondition.or(new Condition(fieldDpsProductNo,
                                                                                                     Condition.OPERATOR_EQUALS,
                                                                                                     aggregateProduct.getAsId().getProductNumber()));
                            }
                            conditions[0] = new ConditionList(productWithAggsCondition);
                        } else {
                            conditions[0] = productNumberCondition; // keine Aggregate zum Produkt gefunden
                        }
                    } else {
                        conditions[0] = productNumberCondition; // Aggregate-Produkt bzw. !includeAggregates
                    }
                    conditions[1] = saCondition;

                    query.join(new InnerJoin(TABLE_DA_PRODUCT_SAS, conditions));
                } else {
                    query.join(new InnerJoin(TABLE_DA_PRODUCT_SAS, saCondition));
                }

                if (hasFieldOfTable(selectFields, TABLE_DA_PRODUCT)) {
                    // Join mit DA_PRODUCT um den Aggregate-Typ zu ermitteln
                    String fieldDpProductNo = TableAndFieldName.make(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO);
                    Condition productJoinCondition = new Condition(fieldDpsProductNo, "=", new Fields(fieldDpProductNo));
                    query.join(new InnerJoin(TABLE_DA_PRODUCT, productJoinCondition));
                }

                return sqlSelect.createAbfrageCancelable();
            }
        };
    }

    /**
     * Tabelle DA_PRODUCT in Abfrage enthalten?
     *
     * @param selectFields
     * @return
     */
    private static boolean hasFieldOfTable(final EtkDisplayFields selectFields, final String table) {
        for (EtkDisplayField field : selectFields.getFields()) {
            if (field.getKey().getTableName().equals(table)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sucht alle Assemblies mit den entsprechenden Suchwerten in den Modulen der EinPAS KG/TU-Struktur im Retail,
     * wobei optional eine <i>optionalRootAssemblyId</i> angegeben werden kann, die bei enthaltenem Produkt die Suche
     * erheblich beschleunigt.
     *
     * @param optionalRootAssemblyId
     * @param isSearchValuesDisjunction
     * @param selectFields
     * @param selectValues
     * @param whereFields
     * @param whereValues
     * @param andOrSearch
     * @param project
     * @param multiLanguageCache
     * @param wildCardSettings
     * @return
     */
    public static iPartsSearchVirtualDataset searchAssemblyEntriesForRetailStructures(final AssemblyId optionalRootAssemblyId,
                                                                                      final boolean isSearchValuesDisjunction,
                                                                                      EtkDisplayFields selectFields,
                                                                                      List<String> selectValues,
                                                                                      final EtkDisplayFields whereFields,
                                                                                      final List<String> whereValues,
                                                                                      final boolean andOrSearch, EtkProject project,
                                                                                      WeakKeysMap<String, String> multiLanguageCache,
                                                                                      final WildCardSettings wildCardSettings) {
        final int fieldIndexProductNo;
        final int fieldIndexKG;
        final int fieldIndexTU;
        final int fieldIndexEinPAS_HG;
        final int fieldIndexEinPAS_G;
        final int fieldIndexEinPAS_TU;
        final EtkDisplayFields mySelectFields = new EtkDisplayFields(selectFields);
        final List<String> mySelectValues = new DwList<String>(selectValues);

        fieldIndexProductNo = mySelectFields.size();
        mySelectFields.addFeld(new EtkDisplayField(TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_PRODUCT_NO), false, false));
        mySelectValues.add("");
        fieldIndexKG = mySelectFields.size();
        mySelectFields.addFeld(new EtkDisplayField(TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_KG), false, false));
        mySelectValues.add("");
        fieldIndexTU = mySelectFields.size();
        mySelectFields.addFeld(new EtkDisplayField(TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_TU), false, false));
        mySelectValues.add("");
        fieldIndexEinPAS_HG = mySelectFields.size();
        mySelectFields.addFeld(new EtkDisplayField(TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_EINPAS_HG), false, false));
        mySelectValues.add("");
        fieldIndexEinPAS_G = mySelectFields.size();
        mySelectFields.addFeld(new EtkDisplayField(TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_EINPAS_G), false, false));
        mySelectValues.add("");
        fieldIndexEinPAS_TU = mySelectFields.size();
        mySelectFields.addFeld(new EtkDisplayField(TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_EINPAS_TU), false, false));
        mySelectValues.add("");

        return new iPartsSearchVirtualDatasetAssemblyWithDBDataset(mySelectFields, project, multiLanguageCache) {
            @Override
            public DBDataSetCancelable createDBDataSet() throws CanceledException {
                // Bestimmung der Produktnummer anhand einer virtuellen optionalRootAssemblyId
                String productNumber = iPartsVirtualNode.getProductNumberFromAssemblyId(optionalRootAssemblyId);

                // Teilesuche über Stücklistenfelder
                PartsSearchSqlSelect sqlSelect = PartsSearchSqlSelect.buildPartsSearchSqlSelectForSearchByFields(isSearchValuesDisjunction,
                                                                                                                 mySelectFields, mySelectValues,
                                                                                                                 whereFields, whereValues,
                                                                                                                 andOrSearch, project,
                                                                                                                 wildCardSettings, project.getDBLanguage());
                SQLQuery query = sqlSelect.getQuery();

                // Join von gefundenen Stücklisteneinträgen auf eingebaute Module (in allen Produkten)
                Condition moduleCondition = new Condition(TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI), Condition.OPERATOR_EQUALS,
                                                          new Fields(TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_MODULE_NO)));

                // bei vorhandener Produktnummer diese zusätzlich als Where Bedingung verwenden
                if (productNumber != null) {
                    Condition productNumberCondition = new Condition(TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_PRODUCT_NO),
                                                                     Condition.OPERATOR_EQUALS, productNumber);
                    query.where(productNumberCondition);
                }
                query.join(new InnerJoin(TABLE_DA_MODULES_EINPAS, moduleCondition));
                return sqlSelect.createAbfrageCancelable();
            }

            @Override
            public EtkDataPartListEntry[] get() {
                if (ds == null) {
                    return null;
                }

                List<String> values = ds.getStringList();
                iPartsProductId productId = new iPartsProductId(values.get(fieldIndexProductNo));
                iPartsProduct parentProduct = iPartsProduct.getInstance(project, productId);
                KgTuId kgTuId = new KgTuId(values.get(fieldIndexKG), values.get(fieldIndexTU));
                List<EtkDataPartListEntry> result = new DwList<>();
                List<iPartsVirtualNode> nodes = new DwList<>();
                // KG-Eintrag vorhanden?
                if (kgTuId.isValidId()) {
                    // Virtueller KG/TU-Knoten in EinPAS-Produkten
                    if (parentProduct.getProductStructuringType() == PRODUCT_STRUCTURING_TYPE.KG_TU) {
                        nodes.add(new iPartsVirtualNode(iPartsNodeType.getProductKgTuType(parentProduct, project), productId));
                        nodes.add(new iPartsVirtualNode(iPartsNodeType.KGTU, kgTuId));
                        addSubAssemblyEntries(project, nodes, result);
                    } else if (parentProduct.getProductStructuringType() == PRODUCT_STRUCTURING_TYPE.EINPAS) {
                        nodes.add(new iPartsVirtualNode(iPartsNodeType.getProductEinPASType(parentProduct, project),
                                                        productId));
                        nodes.add(new iPartsVirtualNode(iPartsNodeType.EINPAS, EinPasId.createVirtualEinPasIdForKgTu(kgTuId)));
                        addSubAssemblyEntries(project, nodes, result);
                    }
                } else {
                    // Überprüfung der nach EinPAS verorteten Module
                    if (parentProduct.getProductStructuringType() == PRODUCT_STRUCTURING_TYPE.EINPAS) {
                        EinPasId einPasId = new EinPasId(values.get(fieldIndexEinPAS_HG), values.get(fieldIndexEinPAS_G), values.get(fieldIndexEinPAS_TU));
                        if (einPasId.isValidId()) {
                            nodes.add(new iPartsVirtualNode(iPartsNodeType.getProductEinPASType(parentProduct, project), productId));
                            nodes.add(new iPartsVirtualNode(iPartsNodeType.EINPAS, einPasId));
                            addSubAssemblyEntries(project, nodes, result);
                        }
                    }
                }
                return ArrayUtil.toArray(result);
            }
        };
    }

    public iPartsVirtualAssemblyEinPasBase(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        super(project, virtualIds, assemblyId);
    }

    protected DBDataObjectList<EtkDataPartListEntry> loadVirtualEinPas(iPartsCatalogNode completeStructure, iPartsVirtualNode rootNode,
                                                                       EinPasId subNode) {
        iPartsCatalogNode nodeParent;
        if (subNode != null) {
            nodeParent = completeStructure.getNode(subNode);
        } else {
            // Kein subknoten -> nehme den obersten
            nodeParent = completeStructure;
        }

        if (nodeParent == null) {
            // Stückliste nicht mehr da, evtl. von jemandem gelöscht worden
            return new EtkDataPartListEntryList();
        }


        //Jetzt die Childs an diese Baugruppe anfügen
        DBDataObjectList<EtkDataPartListEntry> result = new EtkDataPartListEntryList();
        int lfdNumber = 0;
        for (iPartsCatalogNode nodeChild : nodeParent.getChildren()) {
            lfdNumber++;
            if (nodeChild.getId() instanceof AssemblyId) {
                // Hier wird eine ganz normale Baugruppe als Child angezeigt
                AssemblyId childAssemblyId = (AssemblyId)nodeChild.getId();
                EtkDataPartListEntry newEntry = createAssemblyChildNode(lfdNumber, childAssemblyId);
                if (newEntry != null) {
                    result.add(newEntry, DBActionOrigin.FROM_DB);
                }
            } else if (nodeChild.getId() instanceof EinPasId) {
                // Hier wird die EinPAS angezeigt
                EinPasId childId = (EinPasId)nodeChild.getId();
                EtkDataPartListEntry newEntry = createVirtualNode(lfdNumber, rootNode, new iPartsVirtualNode(iPartsNodeType.EINPAS, childId));
                if (newEntry != null) {
                    result.add(newEntry, DBActionOrigin.FROM_DB);
                }
            }
        }
        return result;
    }

    protected EtkDataPartListEntry createAssemblyChildNode(int lfdNumber, AssemblyId childAssemblyId) {
        // Attribute erzeugen und alle Felder mit Leer auffüllen
        DBDataObjectAttributes katAttributes = getEtkProject().getDbLayer().getAttributes(TABLE_KATALOG,
                                                                                          new String[]{ FIELD_K_VARI, FIELD_K_VER,
                                                                                                        FIELD_K_SACH, FIELD_K_SVER },
                                                                                          new String[]{ childAssemblyId.getKVari(),
                                                                                                        childAssemblyId.getKVer(),
                                                                                                        childAssemblyId.getKVari(),
                                                                                                        childAssemblyId.getKVer() });
        if (katAttributes != null) {
            katAttributes.addField(FIELD_K_VARI, getAsId().getKVari(), DBActionOrigin.FROM_DB);
            katAttributes.addField(FIELD_K_VER, getAsId().getKVer(), DBActionOrigin.FROM_DB);
            katAttributes.addField(FIELD_K_SACH, childAssemblyId.getKVari(), DBActionOrigin.FROM_DB);
            katAttributes.addField(FIELD_K_VER, childAssemblyId.getKVer(), DBActionOrigin.FROM_DB);
            katAttributes.addField(FIELD_K_LFDNR, EtkDbsHelper.formatLfdNr(lfdNumber), DBActionOrigin.FROM_DB);
            katAttributes.addField(FIELD_K_SEQNR, EtkDbsHelper.formatLfdNr(lfdNumber), DBActionOrigin.FROM_DB);

            EtkDataPartListEntry newPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getEtkProject(), katAttributes);
            newPartListEntry.getPart().setId(new iPartsPartId(katAttributes.getField(FIELD_K_MATNR).getAsString(),
                                                              katAttributes.getField(FIELD_K_MVER).getAsString()), DBActionOrigin.FROM_DB);

            if (newPartListEntry instanceof iPartsDataPartListEntry) {
                ((iPartsDataPartListEntry)newPartListEntry).setDataLoadedFlagsForVirtualPLE(true);
            }
            return newPartListEntry;
        } else {
            return null;
        }
    }

    @Override
    public String getOrderNumber() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();

        IdWithType lastNodeId = lastVirtualNode.getId();

        // Alle EinPasKnoten werdne hier behandelt
        if (lastNodeId instanceof EinPasId) { // EinPAS
            EinPasId einPasId = (EinPasId)lastNodeId;
            if (einPasId.isHgNode()) {
                return einPasId.getHg();
            } else if (einPasId.isGNode()) {
                return einPasId.getG();
            } else if (einPasId.isTuNode()) {
                return einPasId.getTu();
            }
        }
        return super.getOrderNumber();
    }

    @Override
    public String getPartsListType() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof EinPasId) { // EinPAS
            EinPasId einPasId = (EinPasId)lastNodeId;
            if (einPasId.isHgNode()) {
                return PARTS_LIST_TYPE_EINPAS_HG;
            } else if (einPasId.isGNode()) {
                return PARTS_LIST_TYPE_EINPAS_G;
            } else if (einPasId.isTuNode()) {
                return PARTS_LIST_TYPE_EINPAS_TU;
            }
        }
        return super.getPartsListType();
    }

    @Override
    public EtkMultiSprache getTexts() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof EinPasId) { // EinPAS
            EinPas einpas = EinPas.getInstance(getEtkProject());
            EinPasId einPasId = (EinPasId)lastNodeId;

            EinPasNode node = einpas.getNode(einPasId);

            if (node != null) {
                return node.getTitle();
            } else if (einPasId.getHg().equals(EinPasId.VIRTUAL_HG_FOR_KG_TU)) { // Spezialfall für virtuellen KG/TU-Knoten in EinPAS
                if (einPasId.isHgNode()) {
                    return new EtkMultiSprache("!!Module in KG/TU-Struktur", getEtkProject().getConfig().getDatabaseLanguages());
                } else { // KG/TU Text bestimmen
                    String productNumber = iPartsVirtualNode.getProductNumberFromAssemblyId(getAsId());
                    if ((productNumber == null) || productNumber.isEmpty()) {
                        return null; // ohne Produkt kann es keine KG/TU-Benennungen geben
                    }

                    iPartsProductId productId = new iPartsProductId(productNumber);

                    KgTuId kgTuId;
                    if (einPasId.isGNode()) { // EinPAS G -> KG/TU KG
                        kgTuId = new KgTuId(einPasId.getG(), "");
                    } else { // EinPAS G+TU -> KG/TU KG+TU
                        kgTuId = new KgTuId(einPasId.getG(), einPasId.getTu());
                    }

                    KgTuNode kgTuNode = iPartsProductStructures.getInstance(getEtkProject(), productId).getKgTuNode(getEtkProject(), kgTuId);

                    if (kgTuNode != null) {
                        return kgTuNode.getTitle();
                    } else {
                        return null;
                    }

                }
            } else {
                return null;
            }
        }

        return super.getTexts();
    }

    @Override
    public String getPictureName() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof EinPasId) { // EinPAS
            EinPasId einPasId = (EinPasId)lastNodeId;
            EinPasNode node = EinPas.getInstance(getEtkProject()).getNode(einPasId);
            if (node != null) {
                return node.getPictureName();
            }
            return "";
        }

        // Hier darf keine Prüfung auf structureHelper.isNewStructureActive() stattfinden
        if (lastNodeId instanceof ModelElementUsageId) {// EDS ModelElementUsage
            ModelElementUsageId modelElementUsageId = (ModelElementUsageId)lastNodeId;
            iPartsModelId modelId = new iPartsModelId(iPartsVirtualNode.getModelNumberFromAssemblyId(getAsId()));
            if (modelId.isValidId()) {
                ModelElementUsageNode node = ModelElementUsage.getInstance(getEtkProject(), modelId).getNode(modelElementUsageId);
                if (node != null) {
                    return node.getPictureName();
                }
            }
            return "";
        }

        if (lastNodeId instanceof OpsId) {// EDS OPS
            OpsId opsId = (OpsId)lastNodeId;
            iPartsModelId modelId = new iPartsModelId(iPartsVirtualNode.getModelNumberFromAssemblyId(getAsId()));
            if (modelId.isValidId()) {
                OpsNode node = Ops.getInstance(getEtkProject(), modelId).getNode(opsId);
                if (node != null) {
                    return node.getPictureName();
                }
            }
            return "";
        }


        return super.getPictureName();
    }

    @Override
    public void getParentAssemblyEntries(boolean filtered, List<EtkDataPartListEntry> result) {
        if (getParentAssemblyEntriesForParentId(EinPasId.class, iPartsNodeType.EINPAS, filtered, result)) {
            return;
        }

        super.getParentAssemblyEntries(filtered, result);
    }


    /**
     * Sucht in der EinPAS-Struktur nach den Werten aus der Suche
     *
     * @param constructionType
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
    public static iPartsSearchVirtualDataset searchPartListEntriesForEinPasStruct(final STRUCTURE_CONSTRUCTION_TYPE constructionType,
                                                                                  final AssemblyId optionalRootAssemblyId,
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
                String product = iPartsVirtualNode.getProductNumberFromAssemblyId(optionalRootAssemblyId);
                String series = iPartsVirtualNode.getSeriesNumberFromAssemblyId(optionalRootAssemblyId);
                String model = iPartsVirtualNode.getModelNumberFromAssemblyId(optionalRootAssemblyId);

                // Jetzt einfach alle Treffer aus dem EinPas-Object holen
                EinPas einPas = EinPas.getInstance(project);

                List<EinPasNode> einPasNodes = einPas.search(selectFields, selectValues, whereFields, andOrWhereValues,
                                                             andOrSearch, wildCardSettings, project.getDBLanguage(), project.getDataBaseFallbackLanguages());
                List<EtkDataPartListEntry> result = new DwList<>();

                if (!einPasNodes.isEmpty()) {
                    List<String> validProductList = new DwList<>();
                    List<String> validSeriesList = new DwList<>();
                    List<String> validModelList = new DwList<>();

                    if (product != null) {
                        validProductList.add(product);
                    }
                    if (series != null) {
                        validSeriesList.add(series);
                    }
                    if (model != null) {
                        validModelList.add(model);
                    }

                    if ((product == null) && (series == null) && (model == null)) {
                        // Wenn via assemblyId weder auf ein Produkt, ein Baumuster oder eine Baureihe eingeschränkt
                        // werden kann, dann ist theoretisch alles möglich -> hole deshalb alles aus der Datenbank

                        // Da wir in der EinPAS-Struktur unterwegs sind, hole alle Produkte mit dem Strukturtyp "EinPAS"
                        EtkRecords recs = project.getEtkDbs().getRecords(TABLE_DA_PRODUCT,
                                                                         new String[]{ FIELD_DP_STRUCTURING_TYPE },
                                                                         new String[]{ PRODUCT_STRUCTURING_TYPE.EINPAS.name() });
                        for (EtkRecord rec : recs) {
                            validProductList.add(rec.getField(FIELD_DP_PRODUCT_NO).getAsString());
                        }

                        // EinPAS via Baureihen bestimmen -> nicht bei EDS/BCS-Strukturtyp
                        if ((constructionType != null) && (constructionType != STRUCTURE_CONSTRUCTION_TYPE.EDS_MODEL)) {
                            recs = project.getEtkDbs().getRecords(TABLE_DA_SERIES);
                            for (EtkRecord rec : recs) {
                                validSeriesList.add(rec.getField(FIELD_DS_SERIES_NO).getAsString());
                            }
                        }

                        // EinPAS via Baumuster bestimmen -> nicht bei DIALOG-Strukturtyp
                        if (((constructionType != null) && (constructionType != STRUCTURE_CONSTRUCTION_TYPE.DIALOG_SERIES))
                            && SessionKeyHelper.isConstructionModelForEDSBCSSelected()) {
                            Map<String, Set<String>> selectedModelsMap = (Map<String, Set<String>>)Session.get().getAttribute(iPartsPlugin.SESSION_KEY_SELECT_EDS_CONST_MODEL);
                            if ((selectedModelsMap != null) && !selectedModelsMap.isEmpty()) {
                                for (Set<String> models : selectedModelsMap.values()) {
                                    validModelList.addAll(models);
                                }
                            }
                        }
                    }

                    List<iPartsVirtualNode> nodes = new DwList<>();

                    // Und jetzt alle gültigen Knoten einfügen
                    // dazu die ParentAssembly ermitteln und davon alle Childs für die spätere Filterung zurückliefern
                    for (EinPasNode einPasNode : einPasNodes) {
                        iPartsVirtualNode einPasParentNode = null;
                        if (einPasNode.getParent() != null) {
                            einPasParentNode = new iPartsVirtualNode(iPartsNodeType.EINPAS, einPasNode.getParent().getId());
                        }

                        // Produkte
                        for (String productNo : validProductList) {
                            if (Session.currentSessionThreadAppActionCancelled()) {
                                throw new CanceledException(null);
                            }

                            iPartsProductId productId = new iPartsProductId(productNo);

                            // Produkt -> EinPAS
                            nodes.clear();
                            nodes.add(new iPartsVirtualNode(iPartsNodeType.getProductEinPASType(iPartsProduct.getInstance(project, productId),
                                                                                                project), productId));
                            if (einPasParentNode != null) {
                                nodes.add(einPasParentNode);
                            }
                            addSubAssemblyEntries(project, nodes, result);
                        }

                        // Baureihen
                        for (String seriesNo : validSeriesList) {
                            if (Session.currentSessionThreadAppActionCancelled()) {
                                throw new CanceledException(null);
                            }

                            iPartsSeriesId seriesId = new iPartsSeriesId(seriesNo);

                            // DIALOG -> EinPAS
                            nodes.clear();
                            nodes.add(new iPartsVirtualNode(iPartsNodeType.DIALOG_EINPAS, seriesId));
                            if (einPasParentNode != null) {
                                nodes.add(einPasParentNode);
                            }
                            addSubAssemblyEntries(project, nodes, result);
                        }

                        // Baumuster
                        for (String modelNo : validModelList) {
                            if (Session.currentSessionThreadAppActionCancelled()) {
                                throw new CanceledException(null);
                            }

                            iPartsModelId modelId = new iPartsModelId(modelNo);

                            // EDS -> EinPAS
                            nodes.clear();
                            nodes.add(new iPartsVirtualNode(iPartsNodeType.EDS_EINPAS, modelId));
                            if (einPasParentNode != null) {
                                nodes.add(einPasParentNode);
                            }

                            addSubAssemblyEntries(project, nodes, result);
                        }
                    }
                }
                return result;
            }
        };
    }

    /**
     * Überprüft, ob die Knoten in der übergebenen Liste zu einer gültigen Baugruppe in der DB führen und fügt deren ungefilterte
     * Unterbaugruppen der übergebenenen Liste hinzu.
     *
     * @param project
     * @param nodes
     * @param result
     */
    private static void addSubAssemblyEntries(EtkProject project, List<iPartsVirtualNode> nodes, List<EtkDataPartListEntry> result) {
        if (nodes != null) {
            AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(ArrayUtil.toArray(nodes)), "");
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);

            if (assembly.existsInDB()) {
                // Alle ungefilterten Unterbaugruppen vom EinPAS Knoten zurückliefern (darunter befindet sich auch der
                // gesuchte EinPAS-Knoten)
                result.addAll(assembly.getSubAssemblyEntries(false, null));
            }
        }
    }


    /**
     * Sucht in der KgTuStruktur nach den Werten aus der Suche
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
    public static iPartsSearchVirtualDataset searchPartListEntriesForKgTuStruct(final AssemblyId optionalRootAssemblyId,
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
                String productNumber = iPartsVirtualNode.getProductNumberFromAssemblyId(optionalRootAssemblyId);

                List<EtkDataPartListEntry> result = new DwList<>();

                // In den KgTuTexten wird nur gesucht, wenn ein Produkt ausgewählt wurde
                if (productNumber != null) {
                    // Jetzt einfach alle Treffer aus dem KgTuForProduct-Object holen
                    iPartsProductId productId = new iPartsProductId(productNumber);
                    iPartsProduct product = iPartsProduct.getInstance(project, productId);
                    iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, productId);
                    KgTuNodes allKgTuNodes = productStructures.getAllKgTuNodes(project, product.isStructureWithAggregates());

                    List<KgTuNode> kgTuNodes = allKgTuNodes.search(selectFields, selectValues, whereFields, andOrWhereValues,
                                                                   andOrSearch, wildCardSettings, project.getDBLanguage(),
                                                                   project.getDataBaseFallbackLanguages());

                    if (!kgTuNodes.isEmpty()) {
                        List<iPartsProductId> validProductList = new DwList<>();

                        if (productId.isValidId()) {
                            validProductList.add(productId);
                        } else {
                            // Auf kein Produkt eingeschränkt -> alles ist deshalb möglich -> hole deshalb alles aus der Datenbank
                            EtkRecords recs = project.getEtkDbs().getRecords(TABLE_DA_PRODUCT);
                            for (EtkRecord rec : recs) {
                                validProductList.add(new iPartsProductId(rec.getField(FIELD_DP_PRODUCT_NO).getAsString()));
                            }
                        }


                        // Und jetzt alle gültigen Knoten einfügen
                        // dazu die ParentAssembly ermitteln und davon alle Childs für die spätere Filterung zurückliefern
                        for (KgTuNode kgTuNode : kgTuNodes) {
                            for (iPartsProductId validProductId : validProductList) {
                                if (Session.currentSessionThreadAppActionCancelled()) {
                                    throw new CanceledException(null);
                                }

                                iPartsProduct productForKgTu = iPartsProduct.getInstance(project, validProductId);
                                if (productForKgTu.getProductStructuringType() != PRODUCT_STRUCTURING_TYPE.KG_TU) {
                                    continue;
                                }

                                List<iPartsVirtualNode> nodes = new DwList<>();
                                nodes.add(new iPartsVirtualNode(iPartsNodeType.getProductKgTuType(productForKgTu, project),
                                                                validProductId));
                                if (kgTuNode.getParent() != null) {
                                    nodes.add(new iPartsVirtualNode(iPartsNodeType.KGTU, kgTuNode.getParent().getId()));
                                }

                                addSubAssemblyEntries(project, nodes, result);
                            }
                        }
                    }
                }

                return result;
            }
        };
    }

}