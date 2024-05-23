/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataConstKitContentList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.Set;

/**
 * Construction Kits (Baukästen) zu Materialnummer
 */
public class iPartsConstructionKits implements iPartsConst {

    protected Set<String> constructionKitSet;

    private static ObjectInstanceStrongLRUList<Object, iPartsConstructionKits> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized iPartsConstructionKits getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsConstructionKits.class, "ConstructionKit", false);
        iPartsConstructionKits result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsConstructionKits();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private void load(EtkProject project) {
        constructionKitSet = iPartsDataConstKitContentList.loadAllConstKitPartNumbers(project);
    }

    /**
     * Überprüft, ob eine Materialnummer ein Baukasten ist
     *
     * @param partId
     * @return
     */
    public boolean isConstructionKit(PartId partId) {
        return isConstructionKit(partId.getMatNr());
    }

    /**
     * Überprüft, ob eine Materialnummer ein Baukasten ist
     *
     * @param partNo
     * @return
     */
    public boolean isConstructionKit(String partNo) {
        return constructionKitSet.contains(partNo);
    }
}
