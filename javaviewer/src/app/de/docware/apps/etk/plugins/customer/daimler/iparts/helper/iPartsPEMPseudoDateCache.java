/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPseudoPEMDate;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPseudoPEMDateList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.Set;
import java.util.TreeSet;

/**
 * Cache für die DIALOG PEM Pseudotermine
 */
public class iPartsPEMPseudoDateCache {

    private static ObjectInstanceStrongLRUList<Object, iPartsPEMPseudoDateCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE_INFOS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    public static String getComparablePseudoPemDate(String pemDate) {
        return StrUtils.copySubString(pemDate, 0, iPartsConst.COMPARABLE_PSEUDO_PEM_DIGITS);
    }

    public static synchronized iPartsPEMPseudoDateCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsPEMPseudoDateCache.class, "PEMPseudoDates", false);
        iPartsPEMPseudoDateCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsPEMPseudoDateCache();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private Set<String> pseudoDates;

    public iPartsPEMPseudoDateCache() {
    }

    /**
     * Ab DAIMLER-13914 sollen bei den Pseudo-Terminen nur noch die ersten 8 Stellen berücksichtigt werden.
     *
     * @param project
     */
    private void load(EtkProject project) {
        iPartsDataPseudoPEMDateList allDates = iPartsDataPseudoPEMDateList.loadAllEntriesFromDB(project);
        if (!allDates.isEmpty()) {
            pseudoDates = new TreeSet<>();
            for (iPartsDataPseudoPEMDate pseudoDate : allDates) {
                String date = getComparablePseudoPemDate(pseudoDate.getAsId().getPemDate());
                if (StrUtils.isValid(date)) {
                    pseudoDates.add(date);
                }
            }
        }
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    /**
     * Ab DAIMLER-13914 sollen bei den Pseudo-Terminen nur noch die ersten 8 Stellen berücksichtigt werden.
     *
     * @param date
     * @return
     */
    public boolean isPEMPseudoDate(String date) {
        return StrUtils.isValid(date) && (pseudoDates != null) && !pseudoDates.isEmpty() &&
               pseudoDates.contains(getComparablePseudoPemDate(date));
    }
}
