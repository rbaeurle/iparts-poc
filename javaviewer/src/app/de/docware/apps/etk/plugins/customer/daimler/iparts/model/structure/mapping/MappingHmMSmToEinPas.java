/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.mapping;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.utils.SortType;
import de.docware.util.cache.ObjectInstanceLRUList;

import java.util.*;

/**
 * Mappingtabelle von HM/M/SM nach EinPAS pro Baureihe. Entspricht der Tabelle TABLE_DA_EINPASHMMSM.
 */
public class MappingHmMSmToEinPas implements iPartsConst {

    private static ObjectInstanceLRUList<Object, MappingHmMSmToEinPas> instances = new ObjectInstanceLRUList<>(MAX_CACHE_SIZE_MAPPING_HMMSM_TO_EINPAS,
                                                                                                               iPartsPlugin.getCachesLifeTime());

    // Mapping HM/M/SM nach EinPAS
    protected Map<HmMSmId, List<EinPasId>> mappingToEinPas;

    // Mapping EinPAS nach HM/M/SM
    protected Map<EinPasId, List<HmMSmId>> mappingToHmMSm;

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized MappingHmMSmToEinPas getInstance(EtkProject project, iPartsSeriesId seriesId) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), MappingHmMSmToEinPas.class, seriesId.getSeriesNumber(), false);
        MappingHmMSmToEinPas result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new MappingHmMSmToEinPas();
            result.load(project, seriesId.getSeriesNumber());
            instances.put(hashObject, result);
        }

        return result;
    }

    private void load(EtkProject project, String seriesNumber) {
        mappingToEinPas = new HashMap<HmMSmId, List<EinPasId>>();
        mappingToHmMSm = new HashMap<EinPasId, List<HmMSmId>>();

        // Die Liste der Mappings laden
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_EINPASHMMSM, null,
                                                                                           new String[]{ FIELD_EP_SERIES },
                                                                                           new String[]{ seriesNumber });
        attributesList.sort(new String[]{ FIELD_EP_HM, FIELD_EP_M, FIELD_EP_SM, FIELD_EP_HGDEST, FIELD_EP_GDEST, FIELD_EP_TUDEST },
                            new SortType[]{ SortType.AUTOMATIC, SortType.AUTOMATIC, SortType.AUTOMATIC, SortType.AUTOMATIC,
                                            SortType.AUTOMATIC, SortType.AUTOMATIC });

        for (DBDataObjectAttributes attributes : attributesList) {
            String hg = attributes.getField(FIELD_EP_HGDEST).getAsString();
            String g = attributes.getField(FIELD_EP_GDEST).getAsString();
            String tu = attributes.getField(FIELD_EP_TUDEST).getAsString();
            String series = attributes.getField(FIELD_EP_SERIES).getAsString();
            String hmSource = attributes.getField(FIELD_EP_HM).getAsString();
            String mSource = attributes.getField(FIELD_EP_M).getAsString();
            String smSource = attributes.getField(FIELD_EP_SM).getAsString();

            EinPasId einPasId = new EinPasId(hg, g, tu);
            HmMSmId hmMSmId = new HmMSmId(series, hmSource, mSource, smSource);
            if (hmMSmId.isValidId() && einPasId.isValidId()) {
                // Mapping in die eine Richtung
                List<EinPasId> einPasIds = mappingToEinPas.get(hmMSmId);
                if (einPasIds == null) {
                    // gibt es noch nicht, erstellen und einfügen
                    einPasIds = new ArrayList<EinPasId>();
                    mappingToEinPas.put(hmMSmId, einPasIds);
                }
                einPasIds.add(einPasId);


                //Und in die andere Richtung
                List<HmMSmId> hmMSmIds = mappingToHmMSm.get(einPasId);
                if (hmMSmIds == null) {
                    // gibt es noch nicht, erstellen und einfügen
                    hmMSmIds = new ArrayList<HmMSmId>();
                    mappingToHmMSm.put(einPasId, hmMSmIds);
                }
                hmMSmIds.add(hmMSmId);

            }
        }
    }

    /**
     * Alle EinPAS-Knoten ermitteln, in die der HM/M/SM-Knoten gemappt ist
     *
     * @param hmMSmId
     * @return
     */
    public List<EinPasId> get(HmMSmId hmMSmId) {
        if (mappingToEinPas != null) {
            List<EinPasId> list = mappingToEinPas.get(hmMSmId);
            if (list != null) {
                return Collections.unmodifiableList(list);
            }
        }

        return null;
    }

    /**
     * Alle HM/M/SM Knoten ermitteln, die zu diesem EinPAS-Knoten gemappt sind
     *
     * @param einPasId
     * @return
     */
    public List<HmMSmId> get(EinPasId einPasId) {
        if (mappingToHmMSm != null) {
            List<HmMSmId> list = mappingToHmMSm.get(einPasId);
            if (list != null) {
                return Collections.unmodifiableList(list);
            }
        }

        return null;
    }
}
