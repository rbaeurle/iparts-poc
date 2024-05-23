/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.products;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.apps.etk.util.AbstractCacheWithChangeSets;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Repräsentation der Strukturen (Module, freie SAs und KG/TU bzw. EinPAS-Knoten) eines Produkts als ChangeSet-abhängige
 * Ergänzung zu {@link iPartsProduct}. Die Module und freien SAs werden bei Bedarf nachgeladen.
 */
public class iPartsProductStructures implements EtkDbConst, iPartsConst {

    // Lebensdauer für die ChangeSets ist iPartsPlugin.getCachesLifeTime(); Lebensdauer im Cache ebenfalls, da die
    // Strukturen pro Produkt relativ viel Speicher benötigen und nur dann gebraucht werden, wenn das Produkt gerade in
    // iParts angezeigt oder in den Webservices verwendet wird
    private static AbstractCacheWithChangeSets<ObjectInstanceStrongLRUList<Object, iPartsProductStructures>> productStructuresCacheWithChangeSets =
            new AbstractCacheWithChangeSets<ObjectInstanceStrongLRUList<Object, iPartsProductStructures>>(MAX_CACHE_SIZE_CHANGE_SETS, iPartsPlugin.getCachesLifeTime()) {
                @Override
                protected ObjectInstanceStrongLRUList<Object, iPartsProductStructures> createNewCache() {
                    return new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_PRODUCT, iPartsPlugin.getCachesLifeTime());
                }
            };

    private static Set<String> allProductModulesLoaded = new HashSet<>();

    protected iPartsProductId productId;
    protected Boolean productWithCarPerspective;
    protected boolean carPerspectiveModuleExistsInStructure; // Wir beim Laden der kompletten Struktur gesetzt. Falls das Produkt die Fahrzeugperspektive nicht erlaubt, istd er Kenner "false"
    protected volatile Map<iPartsSA, List<String>> sas;  // Diese SAs sind diesem Produkt zugeordnet. Die einzelne SA kann an List<String> KGs referenziert sein
    protected volatile LinkedHashMap<AssemblyId, iPartsModuleReferences> modules;
    protected volatile boolean modulesLoaded;
    private volatile iPartsCatalogNode cachedEinPasStructure; // wird für EinPAS evtl. gar nicht benötigt, wenn dort immer eine Durchmischung mit den Aggregaten stattfindet
    private volatile iPartsCatalogNode cachedEinPasKgTuStructure;
    private volatile iPartsCatalogNode cachedKgTuStructure;
    private volatile iPartsCatalogNode cachedKgTuStructureWithoutCarPerspective; // Struktur ohne Fahrzeugperspektive, sofern es das spezielle Modul in der Produktstruktur gibt
    private volatile iPartsCatalogNode cachedKgTuStructureWithAggregates;
    private volatile iPartsCatalogNode cachedKgTuStructureWithAggregatesWithoutCarPerspective; // Struktur ohne Fahrzeugperspektive, sofern es das spezielle Modul in der Produktstruktur gibt
    private volatile iPartsCatalogNode cachedEinPasStructureWithAggregates;
    private volatile iPartsCatalogNode cachedEinPasKgTuStructureWithAggregates;
    protected volatile boolean productStructuresLoaded;

    public static synchronized void warmUpCache(EtkProject project) {
        loadAllProductModulesIfNeeded(project);
    }

    public static synchronized void clearCache() {
        synchronized (productStructuresCacheWithChangeSets) {
            productStructuresCacheWithChangeSets.clearCache();
        }
        synchronized (allProductModulesLoaded) {
            allProductModulesLoaded.clear();
        }
    }

    /**
     * Entfernt den Cache-Eintrag für die gerade aktiven {@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet}s.
     *
     * @param project
     */
    public static void removeCacheForActiveChangeSets(EtkProject project) {
        synchronized (productStructuresCacheWithChangeSets) {
            productStructuresCacheWithChangeSets.removeCacheForActiveChangeSets(project);
        }
        synchronized (allProductModulesLoaded) {
            allProductModulesLoaded.remove(CacheHelper.getActiveRevisionChangeSetsKey(project.getEtkDbs()));
        }
    }

    public static void removeProductFromCache(EtkProject project, iPartsProductId productId) {
        synchronized (productStructuresCacheWithChangeSets) {
            Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsProductStructures.class, productId.getProductNumber(), false);
            productStructuresCacheWithChangeSets.removeCacheKey(hashObject);

            // Das Produkt gleich wieder in den normalen Cache legen, was sehr wichtig ist, weil in loadAllProductModulesIfNeeded()
            // über alle Einträge vom normalen Cache iteriert wird
            iPartsProductStructures result = productStructuresCacheWithChangeSets.getNormalCache().get(hashObject);
            if (result == null) {
                // Noch nicht vorhanden -> erzeugen
                result = new iPartsProductStructures(productId);
                productStructuresCacheWithChangeSets.getNormalCache().put(hashObject, result);
            }
        }
    }

    /**
     * Ersetzt den Cache, der mit {@code destinationCacheKey} referenziert wurde, durch den Cache der aktuell aktiven
     * {@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet}s ohne den Cache dort zu entfernen. Wird z.B. verwendet,
     * um vor Edit-Aktionen den Cache als reine Referenz vollständig für den {@code destinationCacheKey} zu übernehmen.
     *
     * @param project
     * @param destinationCacheKey
     */
    public static void useCacheForActiveChangeSets(EtkProject project, String destinationCacheKey) {
        synchronized (productStructuresCacheWithChangeSets) {
            productStructuresCacheWithChangeSets.useActiveChangeSetCache(project, destinationCacheKey);
        }
        synchronized (allProductModulesLoaded) {
            allProductModulesLoaded.add(destinationCacheKey);
        }
    }

    /**
     * Erzeugt eine neue Instanz.
     *
     * @param project
     * @param productId
     * @return
     */
    public static synchronized iPartsProductStructures getInstance(EtkProject project, iPartsProductId productId) {
        ObjectInstanceStrongLRUList<Object, iPartsProductStructures> cache;
        synchronized (productStructuresCacheWithChangeSets) {
            cache = productStructuresCacheWithChangeSets.getCacheInstance(project);
        }

        // Hier nun keine ChangeSets mehr verwenden für das hashObject, weil dies ja bereits über productStructuresCacheWithChangeSets
        // gelöst wurde
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsProductStructures.class, productId.getProductNumber(), false);
        iPartsProductStructures result = cache.get(hashObject);

        if (result == null) {
            // Noch nicht vorhanden -> erzeugen
            result = new iPartsProductStructures(productId);
            cache.put(hashObject, result);
        }

        return result;
    }

    /**
     * Lädt alle Modul-Referenzen zu allen Produkten falls notwendig.
     *
     * @param project
     */
    private static synchronized void loadAllProductModulesIfNeeded(final EtkProject project) {
        String cacheKey = CacheHelper.getActiveRevisionChangeSetsKey(project.getEtkDbs());
        synchronized (allProductModulesLoaded) {
            if (allProductModulesLoaded.contains(cacheKey)) {
                return;
            }
        }

        // Map für Produkt zu Module befüllen
        if (cacheKey.isEmpty()) { // Normaler Cache ohne aktive ChangeSets
            Map<String, LinkedHashMap<AssemblyId, iPartsModuleReferences>> productToAllModulesMap = new HashMap<>();
            iPartsDataProductModulesList productModulesList = iPartsDataProductModulesList.loadAllDataProductModulesList(project);
            for (iPartsDataProductModules dataProductModule : productModulesList) {
                String productNumber = dataProductModule.getAsId().getProductNumber();
                LinkedHashMap<AssemblyId, iPartsModuleReferences> modulesMap = productToAllModulesMap.get(productNumber);
                if (modulesMap == null) {
                    modulesMap = new LinkedHashMap<>();
                    productToAllModulesMap.put(productNumber, modulesMap);
                }

                String moduleNo = dataProductModule.getAsId().getModuleNumber();
                iPartsAssemblyId moduleId = new iPartsAssemblyId(moduleNo, "");
                iPartsModuleReferences newModuleReference = new iPartsModuleReferences(moduleId);
                modulesMap.put(moduleId, newModuleReference);
            }

            // Alle Modul-Referenzen für alle Produkte zuweisen
            for (iPartsProductId productId : iPartsProduct.getAllProductIds(project)) {
                iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, productId);
                LinkedHashMap<AssemblyId, iPartsModuleReferences> newModules = productToAllModulesMap.get(productId.getProductNumber());
                if (newModules == null) { // modules darf nicht null bleiben
                    newModules = new LinkedHashMap<>();
                }
                synchronized (productStructures) {
                    productStructures.modules = newModules;
                    productStructures.modulesLoaded = true;
                }
            }
        } else { // Cache für aktive ChangeSets
            // Normalen Cache als Basis für die Modul-Referenzen verwenden
            ObjectInstanceStrongLRUList<Object, iPartsProductStructures> normalCache;
            synchronized (productStructuresCacheWithChangeSets) {
                normalCache = productStructuresCacheWithChangeSets.getNormalCache();
            }
            if (normalCache.isEmpty()) { // Darf eigentlich nicht passieren, aber sicher ist sicher
                if (project.getEtkDbs().isRevisionChangeSetActive()) {
                    project.getRevisionsHelper().executeWithoutActiveChangeSets(new Runnable() {
                        @Override
                        public void run() {
                            loadAllProductModulesIfNeeded(project); // Normalen Cache aufbauen ohne aktive ChangeSets
                        }
                    }, false, project);
                } else {
                    loadAllProductModulesIfNeeded(project); // Normalen Cache aufbauen
                }
            }

            // Neuen Cache für aktive ChangeSets aufbauen
            ObjectInstanceStrongLRUList<Object, iPartsProductStructures> cache;
            synchronized (productStructuresCacheWithChangeSets) {
                cache = productStructuresCacheWithChangeSets.getCacheInstance(project);
            }
            for (Map.Entry<Object, iPartsProductStructures> normalEntry : normalCache.getMap().entrySet()) {
                iPartsProductStructures normalProductStructures = normalEntry.getValue();
                iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, normalProductStructures.getAsId());

                // Modul-Referenzen aus dem normalen Cache übernehmen
                normalProductStructures.loadModulesIfNeeded(project);
                LinkedHashMap<AssemblyId, iPartsModuleReferences> newModules = new LinkedHashMap<>();
                for (AssemblyId assemblyId : normalProductStructures.modules.keySet()) {
                    iPartsModuleReferences newModuleReference = new iPartsModuleReferences(assemblyId);
                    newModules.put(assemblyId, newModuleReference);
                }
                synchronized (productStructures) {
                    productStructures.modules = newModules;
                    productStructures.modulesLoaded = true;
                }

                cache.put(normalEntry.getKey(), productStructures);
            }

            // Änderungen aus den aktiven ChangeSets berücksichtigen
            Collection<AbstractRevisionChangeSet> activeChangeSets = project.getEtkDbs().getActiveRevisionChangeSets();
            if (activeChangeSets != null) {
                for (AbstractRevisionChangeSet changeSet : activeChangeSets) {
                    Collection<SerializedDBDataObject> serializedObjectsForProductModules = changeSet.getSerializedObjectsByTable(TABLE_DA_PRODUCT_MODULES);
                    if (serializedObjectsForProductModules != null) {
                        for (SerializedDBDataObject serializedObjectForProductModules : serializedObjectsForProductModules) {
                            // Nur die Zustände NEW und DELETED sind relevant bei DA_PRODUCT_MODULES
                            boolean isNew = serializedObjectForProductModules.getState() == SerializedDBDataObjectState.NEW;
                            if (isNew || (serializedObjectForProductModules.getState() == SerializedDBDataObjectState.DELETED)) {
                                iPartsProductId productId = new iPartsProductId(serializedObjectForProductModules.getPkValues()[0]); // DPM_PRODUCT_NO an Index 0
                                iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, productId);
                                LinkedHashMap<AssemblyId, iPartsModuleReferences> newModules = productStructures.modules;
                                if (newModules == null) {
                                    newModules = new LinkedHashMap<>();
                                }

                                AssemblyId assemblyId = new AssemblyId(serializedObjectForProductModules.getPkValues()[1], ""); // DPM_MODULE_NO an Index 1
                                if (isNew) {
                                    iPartsModuleReferences newModuleReference = new iPartsModuleReferences(assemblyId);
                                    newModules.put(assemblyId, newModuleReference);

                                    // Primärschlüsseländerung mit Löschen vom alten Datensatz?
                                    if (serializedObjectForProductModules.arePkValuesChanged() && serializedObjectForProductModules.isDeleteOldId()) {
                                        AssemblyId oldAssemblyId = new AssemblyId(serializedObjectForProductModules.getOldPkValues()[1], ""); // DPM_MODULE_NO an Index 1
                                        newModules.remove(oldAssemblyId);
                                    }
                                } else { // DELETED
                                    newModules.remove(assemblyId);
                                }

                                synchronized (productStructures) {
                                    productStructures.modules = newModules;
                                    // Hier productStructures.modulesLoaded nicht auf true setzen, da nur die zusätzlichen
                                    // Module aus dem ChangeSet hinzugefügt/gelöscht wurden, es aber insgesamt noch mehr
                                    // geben kann, wenn vorher etwas beim Laden der Module schiefgegangen ist -> notfalls
                                    // die Module lieber nochmal nachladen
                                }
                            }
                        }
                    }
                }
            }
        }

        synchronized (allProductModulesLoaded) {
            allProductModulesLoaded.add(cacheKey);
        }
    }


    /**
     * Ezeugt eine neue Instanz
     *
     * @param productId
     */
    private iPartsProductStructures(iPartsProductId productId) {
        this.productId = productId;
    }

    public iPartsProductId getAsId() {
        return productId;
    }

    /**
     * Liefert eine Map von allen SAs für dieses Produkt auf den jeweiligen KG-Knoten innerhalb dieses Produkts zurück.
     *
     * @return
     */
    public Map<iPartsSA, List<String>> getSAs(EtkProject project) {
        loadSAsIfNeeded(project);
        return Collections.unmodifiableMap(sas);
    }

    public boolean hasModules(EtkProject project) {
        loadModulesIfNeeded(project);
        return !modules.isEmpty();
    }

    public Set<AssemblyId> getModuleIds(EtkProject project) {
        loadModulesIfNeeded(project);
        return Collections.unmodifiableSet(modules.keySet());
    }

    public Collection<iPartsModuleReferences> getModules(EtkProject project) {
        loadModulesIfNeeded(project);
        return Collections.unmodifiableCollection(modules.values());
    }

    public iPartsModuleReferences getModule(AssemblyId moduleId, EtkProject project) {
        loadModulesIfNeeded(project);
        return modules.get(moduleId);
    }

    // Module laden
    protected void loadModulesIfNeeded(EtkProject project) {
        if (!modulesLoaded) {
            LinkedHashMap<AssemblyId, iPartsModuleReferences> newModules = new LinkedHashMap<>();
            iPartsDataProductModulesList productModulesList = iPartsDataProductModulesList.loadDataProductModulesList(project, productId);
            for (iPartsDataProductModules dataProductModule : productModulesList) {
                String moduleNo = dataProductModule.getAsId().getModuleNumber();
                iPartsAssemblyId moduleId = new iPartsAssemblyId(moduleNo, "");
                iPartsModuleReferences newModuleReference = new iPartsModuleReferences(moduleId);
                newModules.put(moduleId, newModuleReference);
            }

            synchronized (this) {
                if (!modulesLoaded) {
                    modules = newModules;
                    modulesLoaded = true;
                }
            }
        }
    }

    // Module, Baureihe und Mappings laden
    protected void loadProductStructuresIfNeeded(EtkProject project) {
        if (!productStructuresLoaded) {
            loadModulesIfNeeded(project);

            // Dieser Code-Block sollte wirklich synchronized ablaufen, weil direkt die Inhalte von modules bearbeitet werden.
            // Deadlocks können aber keine entstehen, weil nur Daten (ohne Join) aus der DB geladen werden.
            synchronized (this) {
                if (!productStructuresLoaded) {
                    // Jetzt noch die Mappings der Module zu den einzelnen Strukturen laden
                    iPartsDataModuleEinPASList moduleEinPASList = iPartsDataModuleEinPASList.loadForProduct(project, productId);
                    for (iPartsDataModuleEinPAS dataModuleEinPAS : moduleEinPASList) {
                        String moduleNo = dataModuleEinPAS.getAsId().getModuleNumber();
                        iPartsAssemblyId moduleId = new iPartsAssemblyId(moduleNo, "");
                        iPartsModuleReferences module = modules.get(moduleId);
                        if (module != null) {
                            EinPasId einPasId = new EinPasId(dataModuleEinPAS.getFieldValue(FIELD_DME_EINPAS_HG),
                                                             dataModuleEinPAS.getFieldValue(FIELD_DME_EINPAS_G),
                                                             dataModuleEinPAS.getFieldValue(FIELD_DME_EINPAS_TU));

                            if (einPasId.isValidId()) {
                                module.addReference(einPasId);
                            }

                            KgTuId kgTuId = new KgTuId(dataModuleEinPAS.getFieldValue(FIELD_DME_SOURCE_KG),
                                                       dataModuleEinPAS.getFieldValue(FIELD_DME_SOURCE_TU));
                            if (kgTuId.isValidId()) {
                                module.addReference(kgTuId);
                            }

                            HmMSmId hmMSmId = new HmMSmId(dataModuleEinPAS.getFieldValue(FIELD_DME_PRODUCT_NO),
                                                          dataModuleEinPAS.getFieldValue(FIELD_DME_SOURCE_HM),
                                                          dataModuleEinPAS.getFieldValue(FIELD_DME_SOURCE_M),
                                                          dataModuleEinPAS.getFieldValue(FIELD_DME_SOURCE_SM));
                            if (hmMSmId.isValidId()) {
                                module.addReference(hmMSmId);
                            }
                        }
                    }

                    productStructuresLoaded = true;
                }
            }
        }
    }

    // SAs laden
    protected void loadSAsIfNeeded(EtkProject project) {
        if (sas == null) {
            // Die SelectFields können durch den Join weiter unten sowohl aus DA_PRODUCT_SAS als auch aus DA_SA_MODULES kommen
            // und landen einfach alle in den iPartsDataProductSAs DBDataObjects
            EtkDisplayFields selectFields = new EtkDisplayFields();
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_PRODUCT_SAS, FIELD_DPS_KG, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_SA_MODULES, FIELD_DSM_MODULE_NO, false, false));

            // Join von DA_PRODUCT_SAS mit DA_SA_MODULES, um neben den KG-Knoten auch die Module für die SAs zu bekommen
            iPartsDataProductSAsList saList = new iPartsDataProductSAsList();
            saList.searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields, new String[]{ FIELD_DPS_SA_NO },
                                             TABLE_DA_SA_MODULES, new String[]{ FIELD_DSM_SA_NO }, false, false,
                                             new String[]{ TableAndFieldName.make(TABLE_DA_PRODUCT_SAS, FIELD_DPS_PRODUCT_NO) },
                                             new String[]{ getAsId().getProductNumber() }, false, new String[]{ FIELD_DPS_SA_NO }, false);

            Map<iPartsSA, List<String>> newSas = new LinkedHashMap<>(saList.size());
            for (iPartsDataProductSAs dataProductSAs : saList) {
                iPartsSA sa = iPartsSA.addSAToCache(project, new iPartsSAId(dataProductSAs.getAsId().getSaNumber()),
                                                    new AssemblyId(dataProductSAs.getFieldValue(FIELD_DSM_MODULE_NO), ""));
                List<String> kgs = newSas.get(sa);
                if (kgs == null) {
                    kgs = new ArrayList<>();
                    newSas.put(sa, kgs);
                }
                kgs.add(dataProductSAs.getFieldValue(FIELD_DPS_KG));
            }

            synchronized (this) {
                if (sas == null) {
                    sas = newSas;
                }
            }
        }
    }

    public iPartsCatalogNode getCompleteEinPasStructure(EtkProject project, boolean structureWithAggregates) {
        List<iPartsProduct> aggregates = iPartsProduct.getInstance(project, getAsId()).getAggregates(project);
        if (structureWithAggregates && aggregates.isEmpty()) {
            // Das Produkt enthält keine Aggregate, deshalb wird der Cache ohne die Aggregate verwendet, um einen Cache zu sparen
            structureWithAggregates = false;
        }

        if (structureWithAggregates) {
            if (cachedEinPasStructureWithAggregates != null) {
                return cachedEinPasStructureWithAggregates;
            }
        } else {
            if (cachedEinPasStructure != null) {
                return cachedEinPasStructure;
            }
        }

        iPartsCatalogNode result = new iPartsCatalogNode(productId, true);

        if (structureWithAggregates) {
            for (iPartsProduct aggregate : aggregates) {
                // Struktur des Aggregates ermitteln und zusammenmischen, Aggregate haben keine Unteraggregate mehr
                iPartsCatalogNode aggStructure = getInstance(project, aggregate.getAsId()).getCompleteEinPasStructure(project, false);
                result.mergeSubStructure(aggStructure, true, aggregate.getAsId());
            }
        }

        loadProductStructuresIfNeeded(project);
        for (iPartsModuleReferences moduleReferences : modules.values()) {
            // Alle EinPAS-Knoten, an das das Modul direkt eingehängt wurde
            Set<EinPasId> einPasNodes = new HashSet<>();
            einPasNodes.addAll(moduleReferences.getReferencesEinPas());

            // Jetzt die Knoten einfügen
            for (EinPasId einPasId : einPasNodes) {
                iPartsCatalogNode einPasNode = iPartsCatalogNode.getOrCreateEinPasNode(result, einPasId);
                einPasNode.addChild(new iPartsCatalogNode(moduleReferences.getAssemblyId(), false));
            }
        }

        synchronized (this) {
            if (structureWithAggregates) {
                if (cachedEinPasStructureWithAggregates == null) {
                    cachedEinPasStructureWithAggregates = result;
                }
                return cachedEinPasStructureWithAggregates;
            } else {
                if (cachedEinPasStructure == null) {
                    cachedEinPasStructure = result;
                }
                return cachedEinPasStructure;
            }
        }
    }

    public iPartsCatalogNode getCompleteEinPasStructureWithVirtualKgTu(EtkProject project, boolean structureWithAggregates) {
        List<iPartsProduct> aggregates = iPartsProduct.getInstance(project, getAsId()).getAggregates(project);
        if (structureWithAggregates && aggregates.isEmpty()) {
            // Das Produkt enthält keine Aggregate, deshalb wird der Cache ohne die Aggregate verwendet, um einen Cache zu sparen
            structureWithAggregates = false;
        }

        if (structureWithAggregates) {
            if (cachedEinPasKgTuStructureWithAggregates != null) {
                return cachedEinPasKgTuStructureWithAggregates;
            }
        } else {
            if (cachedEinPasKgTuStructure != null) {
                return cachedEinPasKgTuStructure;
            }
        }

        iPartsCatalogNode result = new iPartsCatalogNode(productId, true);

        if (structureWithAggregates) {
            for (iPartsProduct aggregate : aggregates) {
                // Struktur des Aggregates ermitteln und zusammenmischen, Aggregate haben keine Unteraggregate mehr
                iPartsCatalogNode aggStructure = getInstance(project, aggregate.getAsId()).getCompleteEinPasStructureWithVirtualKgTu(project, false);
                result.mergeSubStructure(aggStructure, true, aggregate.getAsId());
            }
        }

        loadProductStructuresIfNeeded(project);
        for (iPartsModuleReferences moduleReferences : modules.values()) {
            // Alle EinPAS-Knoten, an das das Modul direkt eingehängt wurde
            Set<EinPasId> einPasNodes = new HashSet<>();
            einPasNodes.addAll(moduleReferences.getReferencesEinPas());

            // Jetzt die virtuellen KG/TU-Knoten in EinPAS
            for (KgTuId kgTuId : moduleReferences.getReferencesKgTu()) {
                einPasNodes.add(EinPasId.createVirtualEinPasIdForKgTu(kgTuId));
            }

            // Jetzt die Knoten einfügen
            for (EinPasId einPasId : einPasNodes) {
                iPartsCatalogNode einPasNode = iPartsCatalogNode.getOrCreateEinPasNode(result, einPasId);
                einPasNode.addChild(new iPartsCatalogNode(moduleReferences.getAssemblyId(), false));
            }
        }

        synchronized (this) {
            if (structureWithAggregates) {
                if (cachedEinPasKgTuStructureWithAggregates == null) {
                    cachedEinPasKgTuStructureWithAggregates = result;
                }
                return cachedEinPasKgTuStructureWithAggregates;
            } else {
                if (cachedEinPasKgTuStructure == null) {
                    cachedEinPasKgTuStructure = result;
                }
                return cachedEinPasKgTuStructure;
            }
        }
    }

    private boolean isProductWithCarPerspective(EtkProject project) {
        if (productWithCarPerspective == null) {
            iPartsProduct product = iPartsProduct.getInstance(project, getAsId());
            productWithCarPerspective = product.isCarPerspective();
        }
        return productWithCarPerspective;
    }

    /**
     * Erzeugt die übergebene Struktur ohne eine optional vorhandene Fahrzeugperspektive
     *
     * @param kgTuStructure
     * @return
     */
    private iPartsCatalogNode createKgTuStructureWithoutCarPerspective(iPartsCatalogNode kgTuStructure) {
        boolean doCopy = false;
        if (!kgTuStructure.getChildren().isEmpty()) {
            iPartsCatalogNode firstKgNode = kgTuStructure.getChildren().iterator().next();
            if (firstKgNode.isKgTuId()) {
                if (EditModuleHelper.isCarPerspectiveKgTuId(firstKgNode.getId())) {
                    doCopy = true;
                }
            }
        }
        if (doCopy) {
            iPartsCatalogNode result = new iPartsCatalogNode(productId, true);
            for (iPartsCatalogNode kgNode : kgTuStructure.getChildren()) {
                if (kgNode.isKgTuId()) {
                    if (EditModuleHelper.isCarPerspectiveKgTuId(kgNode.getId())) {
                        continue;
                    }
                    result.addChild(kgNode);
                }
            }
            return result;
        }
        return kgTuStructure;
    }

    /**
     * Liefert die KG/TU Struktur zum Produkt ohne den Fahrzeugperspektiven-Knoten
     *
     * @param project
     * @param structureWithAggregates
     * @return
     */
    public iPartsCatalogNode getKgTuStructureWithoutCarPerspective(EtkProject project, boolean structureWithAggregates) {
        // Hol die komplette Struktur zum Produkt
        iPartsCatalogNode completeStructureForProduct = getCompleteKgTuStructure(project, structureWithAggregates);
        // Falls ein Fahrzeugperspektiven-Knoten existiert, muss eine Kopie der Struktur zurückgeliefert werden in der
        // es den Knoten NICHT gibt, z.B. Export, WS, etc.
        if (carPerspectiveModuleExistsInStructure) {
            synchronized (this) {
                if (structureWithAggregates) {
                    if (cachedKgTuStructureWithAggregatesWithoutCarPerspective == null) {
                        cachedKgTuStructureWithAggregatesWithoutCarPerspective = createKgTuStructureWithoutCarPerspective(completeStructureForProduct);
                    }
                    return cachedKgTuStructureWithAggregatesWithoutCarPerspective;
                } else {
                    if (cachedKgTuStructureWithoutCarPerspective == null) {
                        cachedKgTuStructureWithoutCarPerspective = createKgTuStructureWithoutCarPerspective(completeStructureForProduct);
                    }
                    return cachedKgTuStructureWithoutCarPerspective;
                }
            }
        }
        return completeStructureForProduct;
    }

    /**
     * Liefert die komplette KG/TU Struktur zum Produkt zurück
     *
     * @param project
     * @param structureWithAggregates
     * @return
     */
    public iPartsCatalogNode getCompleteKgTuStructure(EtkProject project, boolean structureWithAggregates) {
        List<iPartsProduct> aggregates = iPartsProduct.getInstance(project, getAsId()).getAggregates(project);
        if (structureWithAggregates && aggregates.isEmpty()) {
            // Das Produkt enthält keine Aggregate, deshalb wird der Cache ohne die Aggregate verwendet, um einen Cache zu sparen
            structureWithAggregates = false;
        }

        synchronized (this) {
            if (structureWithAggregates) {
                if (cachedKgTuStructureWithAggregates != null) {
                    return cachedKgTuStructureWithAggregates;
                }
            } else {
                if (cachedKgTuStructure != null) {
                    return cachedKgTuStructure;
                }
            }
        }

        iPartsCatalogNode result = new iPartsCatalogNode(productId, true);

        // SAs unterhalb von KG-Knoten zuerst hinzufügen
        for (Map.Entry<iPartsSA, List<String>> saReference : getSAs(project).entrySet()) { // lädt indirekt alle SAs
            // Jetzt alle SAs als TU-Knoten eintragen
            iPartsSA sa = saReference.getKey();
            List<String> kgs = saReference.getValue();
            for (String kg : kgs) {
                KgSaId kgSaId = new KgSaId(kg, sa.getSaId().getSaNumber());
                iPartsCatalogNode kgSaNode = iPartsCatalogNode.getOrCreateKgSaNode(result, kgSaId);
                kgSaNode.addChild(new iPartsCatalogNode(sa.getModuleId(), false));
            }
        }

        // Aggregate-Produkte dazumischen (inkl. deren SAs)
        if (structureWithAggregates) {
            for (iPartsProduct aggregate : aggregates) {
                // Struktur des Aggregates ermitteln und zusammenmischen, Aggregate haben keine Unteraggregate mehr und
                // keine Fahrzeugperspektive
                iPartsCatalogNode aggStructure = getInstance(project, aggregate.getAsId()).getKgTuStructureWithoutCarPerspective(project, false);
                result.mergeSubStructure(aggStructure, true, aggregate.getAsId());
            }
        }

        // Verortete Module des Produkts hinzufügen
        loadProductStructuresIfNeeded(project);
        for (iPartsModuleReferences moduleReferences : modules.values()) {
            // Jetzt alle KG/TU als Knoten eintragen
            for (KgTuId kgTuId : moduleReferences.getReferencesKgTu()) {
                // Wir haben ein Fahrzeugperspektiven-KG/TU-Knoten
                if (EditModuleHelper.isCarPerspectiveKgTuId(kgTuId)) {
                    // Falls das Produkt, die Perspektive erlaubt, wird der Knoten hinzugefügt und ein Kenner gesetzt.
                    // Falls nicht, wird der Knoten nicht hinzugefügt, denn dieser Knoten darf dann im kompletten
                    // Programm nicht verwendet werden (egal, ob Retail-Stückliste, Export, WS, etc.). Im Edit wird der
                    // TU direkt aus der DB geladen.
                    if (isProductWithCarPerspective(project)) {
                        carPerspectiveModuleExistsInStructure = true;
                    } else {
                        continue;
                    }
                }
                iPartsCatalogNode kgTuNode = iPartsCatalogNode.getOrCreateKgTuNode(result, kgTuId);
                kgTuNode.addChild(new iPartsCatalogNode(moduleReferences.getAssemblyId(), false));
            }
        }

        synchronized (this) {
            if (structureWithAggregates) {
                if (cachedKgTuStructureWithAggregates == null) {
                    cachedKgTuStructureWithAggregates = result;
                }
                return cachedKgTuStructureWithAggregates;
            } else {
                if (cachedKgTuStructure == null) {
                    cachedKgTuStructure = result;
                }
                return cachedKgTuStructure;
            }
        }
    }

    /**
     * Ermittelt zu der {@link KgTuId} die Zusatzdaten (Bild und Text des Knotens)
     *
     * @param project
     * @param kgTuId
     * @return
     */
    public KgTuNode getKgTuNode(EtkProject project, KgTuId kgTuId) {
        // Die Texte und Bilder der KG/TU Struktur sind vom Produkt (ehemals Katalog abhängig). Wird in diesem Produkt ein
        // Aggregat angezeigt, so kann es sein, dass dieser Knoten nicht im Produkt vorhanden ist. In diesem Fall alle
        // Aggregate der Reihe nach durchgehen, bis ein Knoten gefunden wurde.
        // Für das Einmischen der Aggrgate TUs in das Fahrzeug ist das evtl. nicht 100% richtig, aber als Heuristik ausreichend
        // Problematisch kann es eigentlich nur sein, wenn die gleichen Knoten in einem Aggregat anders benannt wurden.
        // Das sollte aber bei Fahrzeugen und den zur fast gleichen Zeit gebauten Aggregaten nicht vorkommen.
        // Bei bekannten Aggregaten über eine Datenkarte oder zumindest Baumuster wäre eine noch bessere Einschränkung möglich.

        project.startPseudoTransactionForActiveChangeSet(true);
        try {

            KgTuNode result = KgTuForProduct.getInstance(project, getAsId()).getNode(kgTuId);

            if (result == null) {
                // lädt indirekt alle notwendigen Aggregate-Daten
                List<iPartsProduct> aggregates = iPartsProduct.getInstance(project, getAsId()).getAggregates(project);
                for (iPartsProduct aggregate : aggregates) {
                    result = KgTuForProduct.getInstance(project, aggregate.getAsId()).getNode(kgTuId);
                    if (result != null) {
                        return result;
                    }
                }
            }

            return result;
        } finally {
            project.stopPseudoTransactionForActiveChangeSet();
        }
    }

    /**
     * Ermittle alle {@link KgTuNodes} dieses Produktes
     *
     * @param project
     * @param structureWithAggregates Sollen alle relevanten Aggregate dazugemischt werden?
     * @return
     */
    public KgTuNodes getAllKgTuNodes(EtkProject project, boolean structureWithAggregates) {
        iPartsCatalogNode nodes = getCompleteKgTuStructure(project, structureWithAggregates);

        KgTuNodes result = new KgTuNodes();

        // Über alle KGs iterieren
        for (iPartsCatalogNode kgCatalogNode : nodes.getChildren()) {
            KgTuId kgId = (KgTuId)kgCatalogNode.getId();

            KgTuNode kgNode = result.getOrCreate(KgTuType.KG, kgId.getKg(), null);

            KgTuNode productKgTuNode = getKgTuNode(project, kgId);
            if (productKgTuNode != null) {
                // Für diesen Knoten gibt es im Produkt einen Text -> klone den in den zurückgelieferten Knoten
                // ich muss aber mit einer Kopie arbeiten, weil sonst der Parent im Node nicht mehr stimmt
                kgNode.assignAddData(productKgTuNode);
            }

            // Jetzt die TUs dazu
            for (iPartsCatalogNode tuCatalogNode : kgCatalogNode.getChildren()) {
                if (tuCatalogNode.getId() instanceof KgTuId) { // KG/TU
                    KgTuId tuId = (KgTuId)tuCatalogNode.getId();

                    KgTuNode tuNode = kgNode.getOrCreateChild(KgTuType.TU, tuId.getTu(), kgNode);

                    productKgTuNode = getKgTuNode(project, tuId);
                    if (productKgTuNode != null) {
                        // Für diesen Knoten gibt es im Produkt einen Text -> klone den in den zurückgelieferten Knoten
                        // ich muss aber mit einer Kopie arbeiten, weil sonst der Parent im Node nicht mehr stimmt
                        tuNode.assignAddData(productKgTuNode);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Für Anzeige im Debugger
     *
     * @return
     */
    @Override
    public String toString() {
        return productId.toString();
    }
}