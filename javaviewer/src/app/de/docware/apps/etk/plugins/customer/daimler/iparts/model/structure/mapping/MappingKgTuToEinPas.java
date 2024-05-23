/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.mapping;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.utils.SortType;
import de.docware.util.cache.ObjectInstanceLRUList;

import java.util.*;

/**
 * Mappingtabelle von KG/TU nach EinPAS pro Baureihe. Entspricht der Tabelle TABLE_DA_EINPASKGTU.
 */
public class MappingKgTuToEinPas implements iPartsConst {

    private static ObjectInstanceLRUList<Object, MappingKgTuToEinPas> instances = new ObjectInstanceLRUList<>(MAX_CACHE_SIZE_MAPPING_KGTU_TO_EINPAS,
                                                                                                              MAX_CACHE_LIFE_TIME_CORE);

    // Mapping KG/TU nach EinPAS
    protected Map<KgTuId, List<EinPasId>> mappingToEinPas;

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized MappingKgTuToEinPas getInstance(EtkProject project, String seriesType) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), MappingKgTuToEinPas.class, seriesType, false);
        MappingKgTuToEinPas result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new MappingKgTuToEinPas();
            result.load(project, seriesType);
            instances.put(hashObject, result);
        }

        return result;
    }

    private void load(EtkProject project, String seriesType) {
        mappingToEinPas = new HashMap<KgTuId, List<EinPasId>>();

        // Die Liste der Mappings laden
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_EINPASKGTU, null,
                                                                                           new String[]{ FIELD_EP_MODELTYPE },
                                                                                           new String[]{ seriesType });
        attributesList.sort(new String[]{ FIELD_EP_KG, FIELD_EP_TU, FIELD_EP_HGDEST, FIELD_EP_GDEST, FIELD_EP_TUDEST },
                            new SortType[]{ SortType.AUTOMATIC, SortType.AUTOMATIC, SortType.AUTOMATIC, SortType.AUTOMATIC,
                                            SortType.AUTOMATIC });

        for (DBDataObjectAttributes attributes : attributesList) {
            String hg = attributes.getField(FIELD_EP_HGDEST).getAsString();
            String g = attributes.getField(FIELD_EP_GDEST).getAsString();
            String tu = attributes.getField(FIELD_EP_TUDEST).getAsString();
            String kgSource = attributes.getField(FIELD_EP_KG).getAsString();
            String tuSource = attributes.getField(FIELD_EP_TU).getAsString();

            EinPasId einPasId = new EinPasId(hg, g, tu);
            KgTuId kgTuId = new KgTuId(kgSource, tuSource);
            if (kgTuId.isValidId() && einPasId.isValidId()) {
                List<EinPasId> einPasIds = mappingToEinPas.get(kgTuId);
                if (einPasIds == null) {
                    // gibt es noch nicht, erstellen und einfügen
                    einPasIds = new ArrayList<EinPasId>();
                    mappingToEinPas.put(kgTuId, einPasIds);
                }

                einPasIds.add(einPasId);
            }
        }
    }

    public List<EinPasId> get(KgTuId kgTuId) {
        if (mappingToEinPas != null) {
            List<EinPasId> list = mappingToEinPas.get(kgTuId);
            if (list != null) {
                return Collections.unmodifiableList(list);
            }
        }

        return null;
    }
}
