/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataInvoiceRelevance;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataInvoiceRelevanceList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Cache für abrechnungsrelevante Objektinformationen innerhalb eines Autorenauftrags
 */
public class BillableDataCache {

    private static ObjectInstanceStrongLRUList<Object, BillableDataCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE_INFOS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    public static synchronized BillableDataCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), BillableDataCache.class, "BillableDataCache", false);
        BillableDataCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new BillableDataCache();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    private Map<String, Set<String>> idTypeToBillableDataFields = new HashMap<>();

    private void load(EtkProject project) {
        iPartsDataInvoiceRelevanceList list = iPartsDataInvoiceRelevanceList.loadAllInvoiceRelevances(project);
        if (!list.isEmpty()) {
            for (iPartsDataInvoiceRelevance data : list) {
                String type = data.getAsId().getObjectType();
                String field = TableAndFieldName.getFieldName(data.getAsId().getTableAndFieldName());
                if (StrUtils.isValid(type)) {
                    Set<String> fieldNames = idTypeToBillableDataFields.get(type);
                    if (fieldNames == null) {
                        fieldNames = new HashSet<>();
                        idTypeToBillableDataFields.put(type, fieldNames);
                    }
                    if (StrUtils.isValid(field)) {
                        fieldNames.add(field);
                    }
                }
            }
        }
    }

    public boolean isIdTypeBillable(IdWithType id) {
        if (id != null) {
            return idTypeToBillableDataFields.containsKey(id.getType());
        }
        return false;
    }

    public boolean isAttributeBillable(IdWithType id, String fieldName) {
        if (!isIdTypeBillable(id)) {
            return false;
        }
        Set<String> fieldNames = idTypeToBillableDataFields.get(id.getType());
        // Der ID Typ kommt in der Konfigurationsdatei nicht vor -> Daten mit dem ID Typ sind nicht abrechnungsrelevant
        if (fieldNames == null) {
            return false;
        }
        if (!fieldNames.isEmpty()) {
            // Wurden spezifische Felder definiert, dann sind nur diese abrechnungsrelevant
            return fieldNames.contains(fieldName);
        } else {
            // Wenn keine spezifischen Felder definiert wurden, die Id aber existiert, dann wird bei jeder vorhandenen
            // Änderung das Objekt abgerechnet
            return true;
        }
    }

}
