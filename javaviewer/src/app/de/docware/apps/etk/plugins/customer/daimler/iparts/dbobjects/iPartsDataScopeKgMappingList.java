/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Liste von {@link iPartsDataScopeKgMapping}.
 */
public class iPartsDataScopeKgMappingList extends EtkDataObjectList<iPartsDataScopeKgMapping> implements iPartsConst {

    public iPartsDataScopeKgMappingList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Zentrale Methode, die die Sortierfelder zurückliefert.
     *
     * @return Die Sortierfelder
     */
    public static String[] getSortFields() {
        return new String[]{ FIELD_DSKM_SCOPE_ID, FIELD_DSKM_KG };
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataScopeKgMapping}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataScopeKgMappingList loadCompleteMapping(EtkProject project) {
        iPartsDataScopeKgMappingList list = new iPartsDataScopeKgMappingList();
        list.loadCompleteMappingFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Holt auch die Liste aller {@link iPartsDataScopeKgMapping}s, nur etwas schneller, direkt als sortierte TreeMap.
     *
     * @param project
     * @return
     */
    public static Map<String, Set<String>> loadCompleteMappingAsMap(EtkProject project) {
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_SCOPE_KG_MAPPING);
        Map<String, Set<String>> kgsPerScopeMap = new HashMap<>();
        // Alle Daten aus der Datenbank lesen
        for (DBDataObjectAttributes attribute : attributesList) {
            // Den Scope (=Umfang) gibt es nur einmal.
            String scope = attribute.getFieldValue(FIELD_DSKM_SCOPE_ID);
            // Aber zum Scope kann es mehrere KGs geben!
            String kg = attribute.getFieldValue(FIELD_DSKM_KG);
            if (StrUtils.isValid(scope, kg)) {
                Set<String> kgs = kgsPerScopeMap.computeIfAbsent(scope, k -> new TreeSet<>());
                kgs.add(kg);
            }
        }
        return kgsPerScopeMap;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataScopeKgMapping}s.
     *
     * @param project
     * @param origin
     */
    private void loadCompleteMappingFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_SCOPE_KG_MAPPING, null, null, getSortFields(),
                          LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataScopeKgMapping getNewDataObject(EtkProject project) {
        return new iPartsDataScopeKgMapping(project, null);
    }
}
