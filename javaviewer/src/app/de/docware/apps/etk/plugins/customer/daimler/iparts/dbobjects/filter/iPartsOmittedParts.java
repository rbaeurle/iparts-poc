/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.HashSet;
import java.util.Set;

/**
 * Repräsentation der Entfallteile (Tabelle DA_OMITTED_PARTS).
 */
public class iPartsOmittedParts implements iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, iPartsOmittedParts> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    // Entfallteile
    protected Set<String> omittedParts;

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized iPartsOmittedParts getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsOmittedParts.class, "OmittedParts", false);
        iPartsOmittedParts result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsOmittedParts();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private void load(EtkProject project) {
        omittedParts = new HashSet<String>();
        iPartsDataOmittedPartList omittedPartsList = iPartsDataOmittedPartList.loadOmittedParts(project);
        for (iPartsDataOmittedPart omittedPart : omittedPartsList) {
            omittedParts.add(omittedPart.getAsId().getPartNo());
        }
    }

    public boolean isOmittedPart(String partNo) {
        return omittedParts.contains(partNo);
    }

    public boolean isOmittedPart(EtkDataPartListEntry partListEntry) {
        return isOmittedPart(partListEntry.getPart().getAsId().getMatNr());
    }
}
