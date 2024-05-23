/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSpkMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSpkMappingList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.cache.ObjectInstanceLRUList;

import java.util.HashMap;
import java.util.Map;

/**
 * Baureihen-spezifischer Cache für die SPK-Mapping-Texte für Leitungssatzbaukästen
 */
public class iPartsSPKMappingCache implements iPartsConst {

    private static final String STEERING_INDEPENDENT = SteeringIdentKeys.STEERING_IDENT_INDEPENDENT.getKey();
    private static ObjectInstanceLRUList<Object, iPartsSPKMappingCache> instances = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_SERIES,
                                                                                                                iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    // Mapping Einträge aufgeteilt nach Lenkung Links, Rechts und Lenkunsgunabhängig
    // HmMSmId enthält als SM die SPK-Kurz-Benennung
    private Map<HmMSmId, SPKEntries> entriesForNodeSteeringLeft = new HashMap<>();
    private Map<HmMSmId, SPKEntries> entriesForNodeSteeringRight = new HashMap<>();
    private Map<HmMSmId, SPKEntries> entriesForNodeSteeringIndependent = new HashMap<>();

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized void removeSeriesFromCache(EtkProject project, iPartsSeriesId seriesId) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsSPKMappingCache.class, seriesId.getSeriesNumber(), false);
        instances.removeKey(hashObject);
    }

    public static synchronized iPartsSPKMappingCache getInstance(EtkProject project, iPartsSeriesId seriesId, String steeringValue) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsSPKMappingCache.class, seriesId.getSeriesNumber(),
                                                             false);
        iPartsSPKMappingCache result = instances.get(hashObject);

        if (result == null) {
            // Cache für diese Baureihe existiert noch nicht -> neu aufbauen
            result = new iPartsSPKMappingCache();
            instances.put(hashObject, result);
        }

        // Lenkungs-abhängigen Cache bei Bedarf laden
        if (result.getEntriesForSteering(steeringValue).isEmpty()) {
            result.loadSPKEntriesForModelAndSteering(project, seriesId, steeringValue);
        }

        // Lenkungs-unabhängigen Cache bei Bedarf laden
        if (result.getEntriesForSteering(STEERING_INDEPENDENT).isEmpty()) {
            result.loadSPKEntriesForModelAndSteering(project, seriesId, STEERING_INDEPENDENT);
        }

        return result;
    }

    public SPKEntries getTextEntriesForMapping(HmMSmId id, String spkKurzE, String connectorNo, String steeringValue) {
        if (id != null) {
            // zuerst nach Lenkungs-abhängigen Einträgen suchen
            SPKEntries textEntries = getTextEntries(id, spkKurzE, connectorNo, steeringValue);
            if (textEntries != null) {
                return textEntries;
            }

            // falls keine Lenkungs-abhängigen Einträgen vorhanden sind, nochmal Lenkungs-unabhängig suchen
            return getTextEntries(id, spkKurzE, connectorNo, STEERING_INDEPENDENT);

        }
        return null;
    }

    private Map<HmMSmId, SPKEntries> getEntriesForSteering(String steering) {
        if (steering.equals(SteeringIdentKeys.STEERING_RIGHT)) {
            return entriesForNodeSteeringRight;
        }
        if (steering.equals(STEERING_INDEPENDENT)) {
            return entriesForNodeSteeringIndependent;
        }
        return entriesForNodeSteeringLeft;
    }

    private void loadSPKEntriesForModelAndSteering(EtkProject project, iPartsSeriesId seriesId, String steeringValue) {
        iPartsDataSpkMappingList spkMappingList = iPartsDataSpkMappingList.loadSPKMappingForSeries(project, seriesId, steeringValue);

        Map<HmMSmId, SPKEntries> currentEntries = getEntriesForSteering(steeringValue);
        if (currentEntries == null) {
            currentEntries = new HashMap<>();
        }

        for (iPartsDataSpkMapping spkMapping : spkMappingList) {
            // Schlüsselfelder für den Cache
            String hm = spkMapping.getFieldValue(FIELD_SPKM_HM);
            String m = spkMapping.getFieldValue(FIELD_SPKM_M);
            String kurzE = spkMapping.getFieldValue(FIELD_SPKM_KURZ_E);
            HmMSmId id = new HmMSmId(seriesId.getSeriesNumber(), hm, m, kurzE);

            // Eigentliche Nutzdaten
            String kurzAS = spkMapping.getFieldValue(FIELD_SPKM_KURZ_AS);
            EtkMultiSprache langAS = spkMapping.getFieldValueAsMultiLanguage(FIELD_SPKM_LANG_AS);
            String connectorE = spkMapping.getFieldValue(FIELD_SPKM_CONNECTOR_E);
            SPKEntries spkEntries = new SPKEntries(langAS, kurzAS, connectorE);

            currentEntries.put(id, spkEntries);
        }
    }

    private SPKEntries getTextEntries(HmMSmId id, String spkKurzE, String connectorNo, String steeringValue) {
        HmMSmId requestId = new HmMSmId(id.getSeries(), id.getHm(), id.getM(), spkKurzE);

        SPKEntries spkEntries = getEntriesForSteering(steeringValue).get(requestId);

        if (spkEntries != null) {
            // Connector nur prüfen, wenn spkKurzE/DWH_REF nicht mit X beginnt
            if (!spkKurzE.startsWith("X") || spkEntries.getConnectorE().equals(connectorNo)) {
                return spkEntries;
            }
        }
        return null;
    }


    /**
     * Record für die SPK-Lang- und Kurz-Texte aus dem Mapping
     */
    public static class SPKEntries {

        private EtkMultiSprache longText;
        private String shortText;
        private String connectorE;

        public SPKEntries(EtkMultiSprache longText, String shortText, String connectorE) {
            this.longText = longText;
            this.shortText = shortText;
            this.connectorE = connectorE;
        }

        public EtkMultiSprache getLongText() {
            return longText;
        }

        public void setLongText(EtkMultiSprache longText) {
            this.longText = longText;
        }

        public String getShortText() {
            return shortText;
        }

        public void setShortText(String shortText) {
            this.shortText = shortText;
        }

        public String getConnectorE() {
            return connectorE;
        }

        public void setConnectorE(String connectorE) {
            this.connectorE = connectorE;
        }
    }
}
