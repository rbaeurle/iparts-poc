/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.AbstractEtkClusterEvent;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.search.model.ModuleSearchCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.apps.etk.viewer.ClearAllCachesEvent;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.plugins.AbstractPlugin;
import de.docware.framework.modules.plugins.PluginRegistry;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Event, um alle für den Retail notwendigen Caches zurückzusetzen, die z.B. innerhalb von Autoren-Aufträgen verändert
 * werden können.
 */
public class ClearRetailRelevantCachesEvent extends AbstractEtkClusterEvent {

    private boolean clearResponseDataCache;
    private boolean clearResponseSpikesCache;
    private Set<iPartsAssemblyId> assemblyIdsToClearInCache;
    private Set<iPartsProductId> productStructureIdsToClearInCache;

    /**
     * Alle für den Retail notwendigen Caches (inkl. aller Module und Produktstrukturen) zurücksetzen, indem ein
     * {@link ClearRetailRelevantCachesEvent} gefeuert wird, der dann in diesem und allen anderen Cluster-Knoten die Methode
     * {@link #clearRetailRelevantCaches(EtkProject)} aufruft.
     *
     * @param clearResponseDataCache   Flag, ob auch der Cache für die Idents gelöscht werden soll
     * @param clearResponseSpikesCache Flag, ob auch der Cache für die Ausreißer gelöscht werden soll
     */
    public static void invalidateRetailRelevantCaches(boolean clearResponseDataCache, boolean clearResponseSpikesCache) {
        invalidateRetailRelevantCaches(clearResponseDataCache, clearResponseSpikesCache, null, null);
    }

    /**
     * Alle für den Retail notwendigen Caches zurücksetzen, indem ein {@link ClearRetailRelevantCachesEvent} gefeuert wird,
     * der dann in diesem und allen anderen Cluster-Knoten die Methode {@link #clearRetailRelevantCaches(EtkProject)} aufruft.
     *
     * @param clearResponseDataCache            Flag, ob auch der Cache für die Idents gelöscht werden soll
     * @param clearResponseSpikesCache          Flag, ob auch der Cache für die Ausreißer gelöscht werden soll
     * @param assemblyIdsToClearInCache         Set mit {@link iPartsAssemblyId}s für alle explizit aus dem Cache zu löschenden
     *                                          Module; bei {@code null} werden alle Module aus dem Cache gelöscht
     * @param productStructureIdsToClearInCache Set mit {@link iPartsProductId}s für alle explizit aus dem Cache zu löschenden
     *                                          Produktstrukturen; bei {@code null} werden alle Produktstrukturen aus dem
     *                                          Cache gelöscht
     */
    public static void invalidateRetailRelevantCaches(boolean clearResponseDataCache, boolean clearResponseSpikesCache,
                                                      Set<iPartsAssemblyId> assemblyIdsToClearInCache, Set<iPartsProductId> productStructureIdsToClearInCache) {
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new ClearRetailRelevantCachesEvent(clearResponseDataCache,
                                                                                                  clearResponseSpikesCache,
                                                                                                  assemblyIdsToClearInCache,
                                                                                                  productStructureIdsToClearInCache));
    }

    public ClearRetailRelevantCachesEvent() {
    }

    public ClearRetailRelevantCachesEvent(boolean clearResponseDataCache, boolean clearResponseSpikesCache,
                                          Set<iPartsAssemblyId> assemblyIdsToClearInCache, Set<iPartsProductId> productStructureIdsToClearInCache) {
        this.clearResponseDataCache = clearResponseDataCache;
        this.clearResponseSpikesCache = clearResponseSpikesCache;
        this.assemblyIdsToClearInCache = assemblyIdsToClearInCache;
        this.productStructureIdsToClearInCache = productStructureIdsToClearInCache;
    }

    /**
     * Alle für den Retail notwendigen Caches zurücksetzen.
     *
     * @param project
     */
    public void clearRetailRelevantCaches(EtkProject project) {
        CacheHelper.ClearCachesCallback clearCachesCallback = new CacheHelper.ClearCachesCallback() {
            @Override
            public void clearCaches(ClearAllCachesEvent event) {
                // Keine neue eindeutige Cache-ID erzeugen, damit die alten Caches nicht ungültig werden, die nicht gelöscht
                // werden sollen -> alle relevanten Caches müssen natürlich explizit gelöscht werden

                // Nur die Caches von EtkDbs löschen
                EtkDbs.clearGlobalCaches();

                // Nur die relevanten Module aus dem Cache löschen
                String moduleIdsString;
                if (assemblyIdsToClearInCache == null) {
                    EtkDataAssembly.clearGlobalEntriesCache();
                    moduleIdsString = "all modules";
                } else if (clearResponseDataCache || clearResponseSpikesCache) {
                    EtkDataAssembly.clearNormalEntriesCache();
                    moduleIdsString = "all retail modules";
                } else {
                    Set<String> moduleIds = new TreeSet<>();
                    for (AssemblyId assemblyId : assemblyIdsToClearInCache) {
                        moduleIds.add(assemblyId.getKVari());
                        EtkDataAssembly.removeDataAssemblyFromAllCaches(project, assemblyId);
                    }
                    moduleIdsString = "modules [" + StrUtils.stringListToString(moduleIds, ", ") + "]";
                }

                // Loggen der betroffenen Module nur mit Log-Level DEBUG, weil das sehr viele Module sein können und für
                // Module ja auch kein Cache-Aufwärmen stattfindet, was in der Regel zu den Performance-Problemen führt
                Logger.log(iPartsPlugin.LOG_CHANNEL_PERFORMANCE, LogType.DEBUG, "Clear cache for " + moduleIdsString
                                                                                + " requested by " + iPartsUserAdminDb.getUserNameForLogging(project));

                // Sonderbehandlungen für die iParts Plug-ins
                for (AbstractPlugin plugin : PluginRegistry.getRegistry().getRegisteredPlugins()) {
                    if (plugin.getClass().getName().equals(iPartsConst.PLUGIN_CLASS_NAME_IPARTS)) {
                        // Sonderbehandlung für das iPartsPlugin
                        iPartsPlugin iPartsPluginInstance = (iPartsPlugin)plugin;
                        iPartsPluginInstance.clearPluginCaches(event, false, clearResponseDataCache, clearResponseSpikesCache,
                                                               productStructureIdsToClearInCache);
                    } else if (plugin.getClass().getName().equals(iPartsConst.PLUGIN_CLASS_NAME_IPARTS_EDIT)) {
                        // Sonderbehandlung für das iPartsEditPlugin
                        de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin iPartsPluginInstance = (de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin)plugin;
                        iPartsPluginInstance.clearPluginCaches(false);
                    } else {
                        plugin.clearPluginCaches(event);
                    }
                }

                ModuleSearchCache.clearAllCaches();

                // Keine weiteren Standard-Caches löschen
            }
        };
        CacheHelper.onClearAllCachesEvent(clearCachesCallback, null);
    }

    @Override
    public boolean isLoadingAssemblyNeeded() {
        return true;
    }

    @Override
    public boolean isDataChanged() {
        return true;
    }

    public boolean isClearResponseDataCache() {
        return clearResponseDataCache;
    }

    public void setClearResponseDataCache(boolean clearResponseDataCache) {
        this.clearResponseDataCache = clearResponseDataCache;
    }

    public boolean isClearResponseSpikesCache() {
        return clearResponseSpikesCache;
    }

    public void setClearResponseSpikesCache(boolean clearResponseSpikesCache) {
        this.clearResponseSpikesCache = clearResponseSpikesCache;
    }

    @JsonIgnore
    public Set<iPartsAssemblyId> getAssemblyIdsToClearInCache() {
        return assemblyIdsToClearInCache;
    }

    @JsonIgnore
    public void setAssemblyIdsToClearInCache(Set<iPartsAssemblyId> assemblyIdsToClearInCache) {
        this.assemblyIdsToClearInCache = assemblyIdsToClearInCache;
    }

    @JsonIgnore
    public Set<iPartsProductId> getProductStructureIdsToClearInCache() {
        return productStructureIdsToClearInCache;
    }

    @JsonIgnore
    public void setProductStructureIdsToClearInCache(Set<iPartsProductId> productStructureIdsToClearInCache) {
        this.productStructureIdsToClearInCache = productStructureIdsToClearInCache;
    }

    /**
     * Wird nur für die Serialisierung der {@link #assemblyIdsToClearInCache} nach JSON benötigt.
     *
     * @return
     */
    public List<String[]> getAssemblyIdsToClearInCachesForJSON() {
        return getIdsForJSON(assemblyIdsToClearInCache);
    }

    /**
     * Wird nur für die Deserialisierung der {@link #assemblyIdsToClearInCache} von JSON benötigt.
     *
     * @param assemblyIdsAsString
     */
    public void setAssemblyIdsToClearInCachesForJSON(List<String[]> assemblyIdsAsString) {
        if (assemblyIdsAsString != null) {
            assemblyIdsToClearInCache = new TreeSet();
            for (String[] assemblyIdAsString : assemblyIdsAsString) {
                assemblyIdsToClearInCache.add(new iPartsAssemblyId(assemblyIdAsString[0], assemblyIdAsString[1]));
            }
        } else {
            assemblyIdsToClearInCache = null;
        }
    }

    /**
     * Wird nur für die Serialisierung der {@link #productStructureIdsToClearInCache} nach JSON benötigt.
     *
     * @return
     */
    public List<String[]> getProductStructureIdsToClearInCacheForJSON() {
        return getIdsForJSON(productStructureIdsToClearInCache);
    }

    /**
     * Wird nur für die Deserialisierung der {@link #productStructureIdsToClearInCache} von JSON benötigt.
     *
     * @param productStructureIdsAsString
     */
    public void setProductStructureIdsToClearInCacheForJSON(List<String[]> productStructureIdsAsString) {
        if (productStructureIdsAsString != null) {
            productStructureIdsToClearInCache = new TreeSet<>();
            for (String[] productStructureIdAsString : productStructureIdsAsString) {
                productStructureIdsToClearInCache.add(new iPartsProductId(productStructureIdAsString[0]));
            }
        } else {
            productStructureIdsToClearInCache = null;
        }
    }

    @JsonIgnore
    private List<String[]> getIdsForJSON(Set<? extends IdWithType> idSet) {
        if (idSet != null) {
            List<String[]> idsAsString = new ArrayList<>(idSet.size());
            for (IdWithType id : idSet) {
                idsAsString.add(id.toStringArrayWithoutType());
            }
            return idsAsString;
        }

        return null;
    }
}