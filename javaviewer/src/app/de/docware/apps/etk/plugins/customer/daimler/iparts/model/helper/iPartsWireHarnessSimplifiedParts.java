/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.events.CacheForGetCacheDataEvent;
import de.docware.apps.etk.base.project.events.SetCacheDataEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.util.Utils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.*;

/**
 * Cache für vereinfachte Einzelteile (V-Teil) innerhalb eines Leitungssatzbaukasten. Zu einem V-Teil können mehrere Mappings auf
 * andere Teile existieren.
 */
public class iPartsWireHarnessSimplifiedParts implements CacheForGetCacheDataEvent<iPartsWireHarnessSimplifiedParts>, iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, iPartsWireHarnessSimplifiedParts> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    @JsonProperty
    private Map<String, List<EtkDataPart>> partToSimplifiedParts; // Map mit V-Teil auf seine Nachfolger (EtkDataPart)

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    private static String getInstanceHashObject(EtkProject project) {
        return CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsWireHarnessSimplifiedParts.class, "WireHarnessSimplifiedParts", false);
    }

    public static synchronized iPartsWireHarnessSimplifiedParts getInstance(EtkProject project) {
        Object hashObject = getInstanceHashObject(project);
        iPartsWireHarnessSimplifiedParts result = instances.get(hashObject);

        if (result == null) {
            result = iPartsPlugin.createCacheInstanceWithCachesProvider(new iPartsWireHarnessSimplifiedParts(), null, instances, hashObject);
            if (result != null) {
                return result;
            }

            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsWireHarnessSimplifiedParts();
            result.load(iPartsPlugin.getMqProject());
            instances.put(hashObject, result);
        }

        return result;
    }


    @Override
    public iPartsWireHarnessSimplifiedParts createInstance(EtkProject project, SetCacheDataEvent setCacheDataEvent) {
        return createInstance(setCacheDataEvent, instances, getInstanceHashObject(project));
    }

    private void load(EtkProject project) {
        partToSimplifiedParts = new HashMap<>();
        // Es werden alle Felder der Tabellen MAT und DA_WH_SIMPLIFIED_PARTS benötigt
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_WH_SIMPLIFIED_PARTS));
        selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_MAT));
        EtkDataObjectList.JoinData joinData = new EtkDataObjectList.JoinData(TABLE_DA_WH_SIMPLIFIED_PARTS,
                                                                             new String[]{ FIELD_M_MATNR },
                                                                             new String[]{ FIELD_DWHS_SUCCESSOR_PARTNO },
                                                                             false, false);
        EtkDataPartList dataPartsList = new EtkDataPartList();
        dataPartsList.searchSortAndFillWithMultiLangValueForAllLanguagesAndJoin(project, project.getDBLanguage(), selectFields, FIELD_M_TEXTNR,
                                                                                null, null,
                                                                                false, null,
                                                                                false, false, joinData);

        // Ergebnis sortieren, damit die Listen hinter den Teilen in partToSimplifiedParts gleich sind
        List<EtkDataPart> list = dataPartsList.getAsList();
        list.sort((o1, o2) -> {
            String partNumber = Utils.toSortString(o1.getFieldValue(FIELD_DWHS_PARTNO));
            String nextPartNumber = Utils.toSortString(o2.getFieldValue(FIELD_DWHS_PARTNO));
            int result = partNumber.compareTo(nextPartNumber);
            if (result == 0) {
                String successorPartNumber = Utils.toSortString(o1.getFieldValue(FIELD_DWHS_SUCCESSOR_PARTNO));
                String nextSuccessorPartNumber = Utils.toSortString(o2.getFieldValue(FIELD_DWHS_SUCCESSOR_PARTNO));
                result = successorPartNumber.compareTo(nextSuccessorPartNumber);
            }
            return result;
        });

        Map<String, EtkDataPart> partCache = new HashMap<>();
        list.forEach(dataPart -> {
            List<EtkDataPart> simplifiedParts
                    = partToSimplifiedParts.computeIfAbsent(dataPart.getFieldValue(FIELD_DWHS_PARTNO), k -> new ArrayList<>());
            EtkDataPart part = partCache.get(dataPart.getAsId().getMatNr());
            // Check, ob wir das Nachfolger-Teil schon zu einem anderen V-Teil geladen haben
            if (part == null) {
                dataPart.removeForeignTablesAttributes();
                part = dataPart;
                partCache.put(part.getAsId().getMatNr(), part);
            }
            simplifiedParts.add(part);
        });

    }

    /**
     * Liefert alle Nachfolger zu einem V-Teil. Optional können Array und/oder MultiLang DB Felder übergebenen werden,
     * die nachgeladen werden
     *
     * @param partNo
     * @param partTextAndArrayFieldsToLoad
     * @return
     */
    public Optional<List<EtkDataPart>> getSimplifiedPartsForWHPart(String partNo, String... partTextAndArrayFieldsToLoad) {
        Optional<List<EtkDataPart>> simplifiedParts = Optional.ofNullable(partToSimplifiedParts.get(partNo));
        if ((partTextAndArrayFieldsToLoad != null) && (partTextAndArrayFieldsToLoad.length > 0)) {
            simplifiedParts.ifPresent(parts -> parts.forEach(part -> {
                // Pro Teil werden die DB Felder durchlaufen und für das Teil geladen, sofern sie noch nicht geladen wurden
                Arrays.stream(partTextAndArrayFieldsToLoad).forEach(fieldNameToLoad -> {
                    DBDataObjectAttribute attribute = part.getAttributes().getField(fieldNameToLoad, false);
                    if (attribute != null) {
                        if (attribute.getType() == DBDataObjectAttribute.TYPE.MULTI_LANGUAGE) {
                            attribute.setLogLoadCompleteMultiLangIfNeeded(false); // Nachladen ist hier leider notwendig
                            attribute.getAsMultiLanguage(part, true);
                        } else if (attribute.getType() == DBDataObjectAttribute.TYPE.ARRAY) {
                            attribute.getAsArray(part);
                        }
                    }
                });
            }));
        }
        return simplifiedParts;
    }

}
