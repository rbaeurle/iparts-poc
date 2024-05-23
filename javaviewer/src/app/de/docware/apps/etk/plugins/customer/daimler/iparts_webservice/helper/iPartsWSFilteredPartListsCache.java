/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper;

import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPartlistTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.util.cache.ObjectInstanceLRUList;

import java.util.*;

/**
 * Cache zur Verwaltung von gefilterten Stücklisten basierend auf den laufenden Nummern der Stücklisteneinträge zur Verwendung
 * in iParts Webservices
 */
public class iPartsWSFilteredPartListsCache {

    private static ObjectInstanceLRUList<String, Set<String>> filteredPartListsCache = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_FILTERED_PART_LISTS,
                                                                                                                   iPartsPlugin.getCachesLifeTime());

    public static synchronized void clearCaches() {
        filteredPartListsCache.clear();
    }

    /**
     * Liefert den Cache-Key für die übergebenen Daten zurück.
     *
     * @param identContext
     * @param moduleId
     * @param endpoint
     * @return
     */
    public static String getCacheKey(iPartsWSIdentContext identContext, String moduleId, iPartsWSAbstractEndpoint endpoint,
                                     boolean isExtendedDescription) {
        return moduleId + "|" + endpoint.getCacheKeyForResponseCache(null, identContext.createCacheKeyObjectsForResponseCache()) +
               "|" + isExtendedDescription;
    }

    /**
     * Liefert den Cache-Key für die übergebenen Daten zurück.
     *
     * @param specialProduct
     * @param additionalKey
     * @param identContext
     * @param moduleId
     * @param endpoint
     * @return
     */
    public static String getCacheKey(iPartsProduct specialProduct, String additionalKey, iPartsWSIdentContext identContext,
                                     String moduleId, iPartsWSAbstractEndpoint endpoint) {
        String cacheKey = moduleId + "|" + endpoint.getCacheKeyForResponseCache(null, new Object[]{ "specialProduct=",
                                                                                                    specialProduct.getAsId().getProductNumber(),
                                                                                                    "additionalKey=", additionalKey });
        if (identContext != null) {
            cacheKey += "|" + endpoint.getCacheKeyForResponseCache(null, identContext.createCacheKeyObjectsForResponseCache());
        }
        return cacheKey;
    }

    /**
     * Fügt die gefilterten laufenden Nummern mit dem übergebenen Cache-Key zum Cache hinzu.
     *
     * @param cacheKey
     * @param filteredPartListSequenceNumbers
     */
    public static synchronized void addFilteredPartListSequenceNumbers(String cacheKey, Set<String> filteredPartListSequenceNumbers) {
        filteredPartListsCache.put(cacheKey, filteredPartListSequenceNumbers);
    }

    /**
     * Liefert die gefilterten laufenden Nummern für den übergebenen Cache-Key zurück.
     *
     * @param cacheKey
     * @return {@code null} falls keine Cache-Eintrag für den Cache-Key existiert
     */
    public static synchronized Set<String> getFilteredPartListSequenceNumbersFromCache(String cacheKey) {
        return filteredPartListsCache.get(cacheKey);
    }

    /**
     * Liefert die gefilterten laufenden Nummern für die übergebene Stückliste, Stücklistentyp und {@link iPartsWSIdentContext}
     * für die Filterung zurück, wobei die Stückliste im Parameter <i>assembly</i> bei Bedarf geladen und gefiltert wird.
     * Falls notwendig werden zur aktuellen Datenkarte die Befestigungsteile nachgeladen.
     *
     * @param assembly
     * @param partListType
     * @param identContext
     * @param endpoint
     * @return
     */
    public static Set<String> getFilteredPartListSequenceNumbers(EtkDataAssembly assembly, EtkEbenenDaten partListType,
                                                                 iPartsWSIdentContext identContext, iPartsWSAbstractEndpoint endpoint) {
        String cacheKey = getCacheKey(identContext, assembly.getAsId().getKVari(), endpoint, false);
        return getFilteredPartListSequenceNumbers(assembly, partListType, cacheKey, identContext);
    }

    /**
     * Liefert die gefilterten laufenden Nummern für die übergebene Stückliste, Stücklistentyp, Spezial-Produkt und zusätzlichen
     * Schlüsselwert für die Filterung zurück, wobei die Stückliste im Parameter <i>assembly</i> bei Bedarf geladen und
     * gefiltert wird.
     *
     * @param assembly
     * @param partListType
     * @param specialProduct
     * @param additionalKey
     * @param identContext
     * @param endpoint
     * @return
     */
    public static Set<String> getFilteredPartListSequenceNumbers(EtkDataAssembly assembly, EtkEbenenDaten partListType,
                                                                 iPartsProduct specialProduct, String additionalKey, iPartsWSIdentContext identContext,
                                                                 iPartsWSAbstractEndpoint endpoint) {
        String cacheKey = getCacheKey(specialProduct, additionalKey, identContext, assembly.getAsId().getKVari(), endpoint);
        return getFilteredPartListSequenceNumbers(assembly, partListType, cacheKey, null);
    }

    /**
     * Liefert die gefilterten laufenden Nummern für den übergebenen Cache-Key für die Filterung zurück, wobei die Stückliste
     * im Parameter <i>assembly</i> bei Bedarf geladen und gefiltert wird.
     * Falls notwendig werden zur aktuellen Datenkarte die Befestigungsteile nachgeladen sofern {@code identContext != null}.
     *
     * @param assembly
     * @param partListType
     * @param identContext
     * @return
     * @parm cacheKey
     */
    public static Set<String> getFilteredPartListSequenceNumbers(EtkDataAssembly assembly, EtkEbenenDaten partListType,
                                                                 String cacheKey, iPartsWSIdentContext identContext) {
        Set<String> filteredPartListSequenceNumbers = getFilteredPartListSequenceNumbersFromCache(cacheKey);
        if (filteredPartListSequenceNumbers != null) {
            return filteredPartListSequenceNumbers;
        } else {
            return createFilteredPartListEntriesMap(assembly, partListType, identContext, cacheKey).keySet();
        }
    }

    /**
     * Liefert die gefilterten Stücklisteneinträge für die übergebene Stückliste, Stücklistentyp und {@link iPartsWSIdentContext}
     * für die Filterung zurück, wobei die Stückliste im Parameter <i>assembly</i> bei Bedarf geladen und gefiltert wird.
     * Falls notwendig werden zur aktuellen Datenkarte die Befestigungsteile nachgeladen.
     *
     * @param assembly
     * @param partListType
     * @param identContext
     * @param endpoint
     * @return
     */
    public static Collection<EtkDataPartListEntry> getFilteredPartListEntries(EtkDataAssembly assembly, EtkEbenenDaten partListType,
                                                                              iPartsWSIdentContext identContext,
                                                                              iPartsWSAbstractEndpoint endpoint,
                                                                              boolean isExtendedDescription) {
        String cacheKey = getCacheKey(identContext, assembly.getAsId().getKVari(), endpoint, isExtendedDescription);
        return getFilteredPartListEntries(assembly, partListType, cacheKey, identContext);
    }

    /**
     * Liefert die gefilterten Stücklisteneinträge für die übergebene Stückliste, Stücklistentyp, Spezial-Produkt und zusätzlichen
     * Schlüsselwert für die Filterung zurück, wobei die Stückliste im Parameter <i>assembly</i> bei Bedarf geladen und
     * gefiltert wird.
     *
     * @param assembly
     * @param partListType
     * @param specialProduct
     * @param additionalKey
     * @param identContext
     * @param endpoint
     * @return
     */
    public static Collection<EtkDataPartListEntry> getFilteredPartListEntries(EtkDataAssembly assembly, EtkEbenenDaten partListType,
                                                                              iPartsProduct specialProduct, String additionalKey,
                                                                              iPartsWSIdentContext identContext, iPartsWSAbstractEndpoint endpoint) {
        String cacheKey = getCacheKey(specialProduct, additionalKey, identContext, assembly.getAsId().getKVari(), endpoint);
        return getFilteredPartListEntries(assembly, partListType, cacheKey, null);
    }

    /**
     * Liefert die gefilterten Stücklisteneinträge für den übergebenen Cache-Key für die Filterung zurück, wobei die Stückliste
     * im Parameter <i>assembly</i> bei Bedarf geladen und gefiltert wird.
     * Falls notwendig werden zur aktuellen Datenkarte die Befestigungsteile nachgeladen sofern {@code identContext != null}.
     *
     * @param assembly
     * @param partListType
     * @param cacheKey
     * @return
     */
    public static Collection<EtkDataPartListEntry> getFilteredPartListEntries(EtkDataAssembly assembly, EtkEbenenDaten partListType,
                                                                              String cacheKey, iPartsWSIdentContext identContext) {
        Set<String> filteredPartListSequenceNumbers = getFilteredPartListSequenceNumbersFromCache(cacheKey);
        if (filteredPartListSequenceNumbers != null) {
            // Baumuster-Filter muss explizit ausgeführt werden falls er aktiv ist, damit die Filterung der Werkseinsatzdaten
            // korrekt durchgeführt wird -> es würden ansonsten die falschen Werkseinsatzdazen zurückgegeben werden
            iPartsFilter filter = iPartsFilter.get();
            if (filter.isModelFilterActive((assembly instanceof iPartsDataAssembly) ? (iPartsDataAssembly)assembly : null)) {
                // Befestigungsteile nachladen falls notwendig
                if (identContext != null) {
                    identContext.loadFixingPartsIfNeeded(assembly, iPartsPartlistTypes.PART_LIST_TYPE_FOR_GET_PARTS);
                }

                return assembly.getPartList(partListType); // inkl. Baumuster-Filter
            } else { // Kein Baumuster-Filter notwendig -> Stückliste muss nicht erneut gefiltert werden
                // Gefilterte laufende Nummern vorhanden -> nur diese Stücklisteneinträge aus der ungefilterten Stückliste übernehmen
                Collection<EtkDataPartListEntry> partListEntries = new ArrayList<>(filteredPartListSequenceNumbers.size());
                for (EtkDataPartListEntry partListEntryFromAssembly : assembly.getPartListUnfiltered(partListType)) {
                    if (filteredPartListSequenceNumbers.contains(partListEntryFromAssembly.getAsId().getKLfdnr())) {
                        // Zumindest die Gleichteile-Teilenummer muss gesetzt werden
                        if (partListEntryFromAssembly instanceof iPartsDataPartListEntry) {
                            filter.setEqualPartNumber((iPartsDataPartListEntry)partListEntryFromAssembly);
                        }

                        partListEntries.add(partListEntryFromAssembly);
                    }
                }
                return partListEntries;
            }
        } else {
            return createFilteredPartListEntriesMap(assembly, partListType, identContext, cacheKey).values();
        }
    }

    /**
     * Erzeugt eine Map von gefilterten laufenden Nummern auf Stücklisteneinträge für die übergebene Stückliste und Stücklistentyp
     * für die Filterung, wobei die Stückliste im Parameter <i>assembly</i> geladen und gefiltert wird. Die gefilterten
     * laufenden Nummern werden außerdem zum Cache hinzugefügt mit dem übergebenen Cache-Key. Die gewünschten Filter müssen
     * vorher über {@link de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter} gesetzt worden sein.
     * Falls notwendig werden zur aktuellen Datenkarte die Befestigungsteile nachgeladen.
     *
     * @param assembly
     * @param partListType
     * @param identContext
     * @param cacheKey
     * @return
     */
    private static LinkedHashMap<String, EtkDataPartListEntry> createFilteredPartListEntriesMap(EtkDataAssembly assembly,
                                                                                                EtkEbenenDaten partListType,
                                                                                                iPartsWSIdentContext identContext,
                                                                                                String cacheKey) {
        // Für dieses Modul und IdentContext wurde noch keine Stücklisten Filterung durchgeführt
        assembly.clearFilteredPartLists();

        // Befestigungsteile nachladen falls notwendig
        if (identContext != null) {
            identContext.loadFixingPartsIfNeeded(assembly, iPartsPartlistTypes.PART_LIST_TYPE_FOR_GET_PARTS);
        }

        List<EtkDataPartListEntry> partList = assembly.getPartList(partListType); // Gefilterte Stückliste

        // Neuen Cache-Eintrag für die gefilterte Stückliste erzeugen (nur laufende Nummern speichern)
        LinkedHashMap<String, EtkDataPartListEntry> filteredPartListEntriesMap = new LinkedHashMap<>(partList.size());
        for (EtkDataPartListEntry partListEntryFromAssembly : partList) {
            filteredPartListEntriesMap.put(partListEntryFromAssembly.getAsId().getKLfdnr(), partListEntryFromAssembly);
        }

        // Unbedingt eine Kopie vom KeySet in den Cache legen, weil ansonsten über das originale KeySet die gesamte Map
        // und damit auch alle Stücklisteneinträge als Values ungewollt im Cache hängen und extrem viel Speicher benötigen
        // (das KeySet ist ein Variable in der HashMap-Implementierung)
        addFilteredPartListSequenceNumbers(cacheKey, new HashSet<>(filteredPartListEntriesMap.keySet()));
        return filteredPartListEntriesMap;
    }
}