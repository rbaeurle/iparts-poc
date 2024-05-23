/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.events.CacheForGetCacheDataEvent;
import de.docware.apps.etk.base.project.events.SetCacheDataEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataPrimusReplacePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataES1;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsES1Id;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsEqualPartType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsES1;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Cache für PRIMUS-Ersetzungs-Hinweise
 */
public class iPartsPRIMUSReplacementsCache implements CacheForGetCacheDataEvent<iPartsPRIMUSReplacementsCache>, iPartsConst {

    private static final ObjectInstanceStrongLRUList<Object, iPartsPRIMUSReplacementsCache> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE, MAX_CACHE_LIFE_TIME_CORE);

    // Gültige PRIMUS-Vorwärts-Code (01, 02, 11 und 25 für virtuelle Fußnoten und 22, 23 und 28 für virtuelle Ersetzungen)
    private static final Set<String> VALID_PRIMUS_CODES_FORWARD = new HashSet(Arrays.asList("01", "02", "11", "22", "23", "25", "28"));

    @JsonProperty
    private final Map<String, iPartsPRIMUSReplacementCacheObject> partNoToPrimusReplacementCacheObjectsMap;
    @JsonProperty
    private final Map<String, Set<EtkDataPart>> primusReplacementAlternativePartsMap;
    @JsonProperty
    private final Map<String, String> partNumberToForwardCodeMap;
    @JsonProperty
    private final Set<String> partNumbersWithForwardCode74Set; // Eigenes Set für schnelle Bestimmung, ob eine Teilenummer den PRIMUS-Vorwärts-Code 74 hat

    private static String getInstanceHashObject(EtkProject project) {
        return CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsPRIMUSReplacementsCache.class, "PRIMUSReplacementsCache", false);
    }

    public static synchronized iPartsPRIMUSReplacementsCache getInstance(EtkProject project) {
        Object hashObject = getInstanceHashObject(project);
        iPartsPRIMUSReplacementsCache result = instances.get(hashObject);

        if (result == null) {
            result = iPartsPlugin.createCacheInstanceWithCachesProvider(new iPartsPRIMUSReplacementsCache(), null, instances, hashObject);
            if (result != null) {
                return result;
            }

            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsPRIMUSReplacementsCache();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    public static void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }


    public iPartsPRIMUSReplacementsCache() {
        partNoToPrimusReplacementCacheObjectsMap = new HashMap<>();
        primusReplacementAlternativePartsMap = new HashMap<>();
        partNumberToForwardCodeMap = new HashMap<>();
        partNumbersWithForwardCode74Set = new HashSet<>();
    }

    @Override
    public iPartsPRIMUSReplacementsCache createInstance(EtkProject project, SetCacheDataEvent setCacheDataEvent) {
        iPartsPRIMUSReplacementsCache cache = createInstance(setCacheDataEvent, instances, getInstanceHashObject(project));
        if (cache != null) {
            // Bei allen EtkDataPart-Objekten von den Alternativteilen das EtkProject setzen
            for (Set<EtkDataPart> alternativeParts : cache.primusReplacementAlternativePartsMap.values()) {
                if (alternativeParts != null) {
                    for (EtkDataPart alternativePart : alternativeParts) {
                        alternativePart.setDBProject(project);
                    }
                }
            }
        }
        return cache;
    }

    public iPartsPRIMUSReplacementCacheObject getReplacementCacheObjectForMatNr(String matNr) {
        return partNoToPrimusReplacementCacheObjectsMap.get(matNr);
    }

    public String getForwardCodeForPartNo(String partNo) {
        return partNumberToForwardCodeMap.get(partNo);
    }

    public boolean hasForwardCode74ForPartNo(String partNo) {
        return partNumbersWithForwardCode74Set.contains(partNo);
    }

    private void load(EtkProject project) {
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallbackForIncludeParts = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String primusCodeForward = attributes.getFieldValue(FIELD_PRP_PSS_CODE_FORWARD);
                String predecessorPartNo = attributes.getFieldValue(FIELD_PRP_PART_NO);
                if (StrUtils.isValid(primusCodeForward)) {
                    // Teilenummer und Vorwärts-Code cachen
                    partNumberToForwardCodeMap.put(predecessorPartNo, primusCodeForward);
                    if (primusCodeForward.equals("74")) {
                        partNumbersWithForwardCode74Set.add(predecessorPartNo);
                    }
                }

                // Ist der PRIMUS-Vorwärts-Code gültig für die virtuellen Fußnoten bzw. Ersetzungen?
                if (!VALID_PRIMUS_CODES_FORWARD.contains(primusCodeForward)) {
                    return false;
                }

                iPartsPRIMUSReplacementCacheObject primusReplacementCacheObject = partNoToPrimusReplacementCacheObjectsMap.get(predecessorPartNo);
                if (primusReplacementCacheObject == null) {
                    primusReplacementCacheObject = new iPartsPRIMUSReplacementCacheObject(attributes);
                    partNoToPrimusReplacementCacheObjectsMap.put(predecessorPartNo, primusReplacementCacheObject);
                }

                // Evtl. Mitlieferteil hinzufügen
                String includePartNumber = attributes.getFieldValue(FIELD_PIP_INCLUDE_PART_NO);
                if (!includePartNumber.isEmpty()) {
                    // Liste der Mitlieferteile bei Bedarf erzeugen (nicht den Getter verwenden, weil der eine nicht veränderbare
                    // Liste zurückliefert)
                    List<iPartsPRIMUSIncludePartCacheObject> includePartCacheObjects = primusReplacementCacheObject.includeParts;
                    if (includePartCacheObjects == null) {
                        includePartCacheObjects = new DwList<>();
                        primusReplacementCacheObject.includeParts = includePartCacheObjects;
                    }
                    String includePartQuantity = attributes.getFieldValue(FIELD_PIP_QUANTITY);
                    iPartsPRIMUSIncludePartCacheObject includePartCacheObject = new iPartsPRIMUSIncludePartCacheObject(includePartNumber,
                                                                                                                       includePartQuantity);
                    includePartCacheObjects.add(includePartCacheObject);
                }

                return false;
            }
        };

        iPartsDataPrimusReplacePartList.loadPrimusReplacePartsWithIncludeParts(project, foundAttributesCallbackForIncludeParts);

        // Alternativteile für die PRIMUS-Ersetzungen laden
        if (iPartsPlugin.isShowAlternativePartsForPRIMUS()) {
            iPartsES1 es1Cache = iPartsES1.getInstance(project);
            EtkDataObjectList.FoundAttributesCallback foundAttributesCallbackForAlternativeParts = new EtkDataObjectList.FoundAttributesCallback() {
                @Override
                public boolean foundAttributes(DBDataObjectAttributes attributes) {
                    String predecessorPartNo = attributes.getFieldValue(FIELD_PRP_PART_NO);
                    iPartsPRIMUSReplacementCacheObject primusReplacementCacheObject = partNoToPrimusReplacementCacheObjectsMap.get(predecessorPartNo);
                    if (primusReplacementCacheObject != null) { // Muss eigentlich vorhanden sein
                        String successorPartNo = attributes.getFieldValue(FIELD_PRP_SUCCESSOR_PARTNO);
                        if (!successorPartNo.isEmpty()) {
                            String alternativePartNo = attributes.getFieldValue(FIELD_M_MATNR);
                            if (!alternativePartNo.isEmpty()) {
                                // Prüfung analog zu iPartsDataAssembly.loadAllAlternativePartsForPartList()

                                // M_IS_DELETED nachträglich ausfiltern und nicht in die whereFields aufnehmen, damit es keine Probleme bei
                                // der Verwendung von Indizes gibt und das Statement unnötig langsam wird
                                if (attributes.getField(FIELD_M_IS_DELETED).getAsBoolean()) {
                                    return false;
                                }

                                String baseMatNr = attributes.getFieldValue(FIELD_M_BASE_MATNR);
                                String es1Code = attributes.getFieldValue(FIELD_M_AS_ES_1);
                                String es2Code = attributes.getFieldValue(FIELD_M_AS_ES_2);

                                // Nur Materialien mit leerem ES2 oder passender Fußnote am Basisteil (DAIMLER-9557)...
                                if ((es2Code.isEmpty()) || es1Cache.checkFootnoteValidity(es1Code, baseMatNr, project)) {
                                    // ... und State iPartsES1.VALID_MAT_STATE_FOR_ES1=30 werden als Alternativteil erkannt und berücksichtigt.
                                    String state = attributes.getFieldValue(FIELD_M_STATE);
                                    if (StrUtils.isValid(es1Code) && state.equals(iPartsES1.VALID_MAT_STATE_FOR_ES1)) {
                                        String es1Type = es1Cache.getType(es1Code);
                                        if (StrUtils.isValid(es1Type)) {
                                            // DataObject für DA_ES1 an dataPart hängen
                                            EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(project, alternativePartNo, "");
                                            dataPart.assignAttributes(project, attributes, true, DBActionOrigin.FROM_DB);
                                            dataPart.removeForeignTablesAttributes();
                                            iPartsES1Id es1Id = new iPartsES1Id(es1Code, "");
                                            iPartsDataES1 dataES1 = new iPartsDataES1(project, es1Id);
                                            dataES1.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                                            dataES1.setFieldValue(FIELD_DES_TYPE, es1Type, DBActionOrigin.FROM_DB);
                                            DBDataObjectList children = new DBDataObjectList();
                                            children.add(dataES1, DBActionOrigin.FROM_DB);
                                            dataPart.setChildren(TABLE_DA_ES1, children);

                                            addAlternativePart(baseMatNr, dataPart);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return false;
                }
            };

            iPartsDataPrimusReplacePartList.loadPrimusReplacePartsWithAlternativeParts(project, foundAttributesCallbackForAlternativeParts);
        }
    }

    private void addAlternativePart(String primusMatNr, EtkDataPart alternativePart) {
        Set<EtkDataPart> alternativeParts = primusReplacementAlternativePartsMap.computeIfAbsent(primusMatNr, matNr -> new TreeSet<>());
        alternativeParts.add(alternativePart);
    }

    /**
     * Liefert die Alternativteile für die übergebene PRIMUS-Teilenummer (in der Regel der Nachfolger einer PRIMUS-Ersetzung)
     * zurück inkl. Gleichteile-Behandlung.
     *
     * @param primusMatNr
     * @param filter
     * @param project
     * @return {@code null} falls es keine Alternativteile für die PRIMUS-Teilenummer gibt
     */
    public Set<EtkDataPart> getAlternativeParts(String primusMatNr, iPartsFilter filter, EtkProject project) {
        Set<EtkDataPart> alternativeParts = primusReplacementAlternativePartsMap.get(primusMatNr);
        if (alternativeParts != null) {
            String baseEqualPartNumber = null;

            // Alternativteile immer klonen mit dem übergebenen EtkProject, damit mehrsprachige Texte korrekt nachgeladen
            // werden können
            Set<EtkDataPart> clonedAlternativeParts = new TreeSet<>();
            for (EtkDataPart alternativePart : alternativeParts) {
                EtkDataPart clonedAlternativePart = alternativePart.cloneMe(project);
                if ((filter != null) && (filter.getEqualPartTypeForMainModel() != iPartsEqualPartType.NONE)) {
                    // Gleichteile-Teilenummer bestimmen und in einem geklonten EtkDataPart setzen
                    if (clonedAlternativePart instanceof iPartsDataPart) {
                        ((iPartsDataPart)clonedAlternativePart).setMappedMatNr(filter.getEqualPartNumber(alternativePart));
                    }

                    // Gleichteile-Teilenummer vom Basis-Material (ist ja für alle Alternativteile identisch) nur einmal bestimmen
                    if (baseEqualPartNumber == null) {
                        EtkDataPart basePart = EtkDataObjectFactory.createDataPart(project, primusMatNr, "");
                        baseEqualPartNumber = filter.getEqualPartNumber(basePart);
                    }

                    clonedAlternativePart.setFieldValue(iPartsConst.FIELD_M_BASE_MATNR, baseEqualPartNumber, DBActionOrigin.FROM_DB);
                }
                clonedAlternativeParts.add(clonedAlternativePart);
            }
            return Collections.unmodifiableSet(clonedAlternativeParts);
        } else {
            return null;
        }
    }
}
