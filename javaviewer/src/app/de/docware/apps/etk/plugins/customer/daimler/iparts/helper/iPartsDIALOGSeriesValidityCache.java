/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeriesList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.HashMap;

/**
 * Hilfsklasse zur gezielten Steuerung, ob eine Baureihe importiert werden darf oder nicht.
 * Wird von den Änderungsdiensten benutzt.
 * Siehe: [DAIMLER-1247]
 */

public class iPartsDIALOGSeriesValidityCache {

    private static ObjectInstanceStrongLRUList<Object, iPartsDIALOGSeriesValidityCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_MODELS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    public static synchronized iPartsDIALOGSeriesValidityCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDIALOGSeriesValidityCache.class,
                                                             "DIALOGSeriesValidityCache", false);
        iPartsDIALOGSeriesValidityCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsDIALOGSeriesValidityCache();
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

    private HashMap<String, Boolean> seriesValidity; // Baureihen und ihre Gültigkeit
    private int seriesMinLength;

    private void load(EtkProject project) {
        seriesValidity = new HashMap<String, Boolean>();
        iPartsDataSeriesList seriesList = iPartsDataSeriesList.loadDataSeriesList(project, DBDataObjectList.LoadType.COMPLETE);
        for (iPartsDataSeries series : seriesList) {
            String seriesNumber = series.getAsId().getSeriesNumber();
            int seriesLength = seriesNumber.length();
            if ((seriesMinLength == 0) || (seriesLength < seriesMinLength)) {
                seriesMinLength = seriesLength;
            }
            seriesValidity.put(series.getAsId().getSeriesNumber(), series.isImportRelevant());
        }
    }

    /**
     * Liefert, ob die übergebene Baureihe versorgungsrelevant ist (sofern sie existiert)
     *
     * @param series
     * @return
     */
    public boolean isSeriesValidForDIALOGImport(String series) {
        if (StrUtils.isEmpty(series)) {
            return false;
        }
        if (!seriesExists(series)) {
            return false;
        }
        return seriesValidity.get(series);
    }

    /**
     * Liefert, ob die übergebene Baureihe zu den (nicht-) versorgungsrelevanten Baureihen gehört
     *
     * @param seriesNumber
     * @return
     */
    public boolean seriesExists(String seriesNumber) {
        return seriesValidity.containsKey(seriesNumber);
    }

    public String getExistingSeriesFromModel(String modelNumber) {
        if (StrUtils.isValid(modelNumber)) {
            int modelNumberLength = modelNumber.length();
            if ((modelNumberLength < seriesMinLength) && seriesExists(modelNumber)) {
                return modelNumber;
            }
            for (int seriesLength = seriesMinLength; seriesLength <= modelNumber.length(); seriesLength++) {
                String tempSeries = StrUtils.copySubString(modelNumber, 0, seriesLength);
                if (seriesExists(tempSeries)) {
                    return tempSeries;
                }
            }
        }
        return "";
    }

    public boolean isSeriesInModelValidForDIALOGImport(String modelNumber) {
        if (StrUtils.isValid(modelNumber)) {
            String seriesFromModel = getExistingSeriesFromModel(modelNumber);
            if (seriesExists(seriesFromModel)) {
                return seriesValidity.get(seriesFromModel);
            }
        }
        return false;
    }
}
