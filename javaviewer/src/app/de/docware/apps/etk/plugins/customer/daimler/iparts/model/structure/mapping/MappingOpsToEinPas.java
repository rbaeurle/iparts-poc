/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.mapping;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.utils.SortType;
import de.docware.util.cache.ObjectInstanceLRUList;

import java.util.*;

/**
 * Mappingtabelle von Ops nach EinPAS. Entspricht der Tabelle TABLE_DA_EINPASOPS. Jede SAA kann ein eigenes Mapping haben, wobei im Mapping nur die SAA-Prefixe (typisch 6 o. 7 Stellen) sind
 */
public class MappingOpsToEinPas implements iPartsConst {

    private static ObjectInstanceLRUList<Object, MappingOpsToEinPas> instances = new ObjectInstanceLRUList<>(MAX_CACHE_SIZE_MAPPING_KGTU_TO_EINPAS,
                                                                                                             MAX_CACHE_LIFE_TIME_CORE);

    public class SaaAndEinpas {

        public EinPasId einPasId;
        public String saaPrefix;

        public SaaAndEinpas(EinPasId einPasId, String saaPrefix) {
            this.einPasId = einPasId;
            this.saaPrefix = saaPrefix;
        }


        @Override
        public String toString() {
            return saaPrefix + ", " + einPasId.toString();
        }

    }

    // Mapping Ops nach EinPAS
    protected Map<OpsId, List<SaaAndEinpas>> mappingToEinPas;

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized MappingOpsToEinPas getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), MappingOpsToEinPas.class, null, false);
        MappingOpsToEinPas result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new MappingOpsToEinPas();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private void load(EtkProject project) {
        mappingToEinPas = new HashMap<OpsId, List<SaaAndEinpas>>();

        // Die Liste der Mappings laden
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_EINPASOPS);
        attributesList.sort(new String[]{ FIELD_EP_GROUP, FIELD_EP_SCOPE, FIELD_EP_SAAPREFIX, FIELD_EP_HGDEST, FIELD_EP_GDEST, FIELD_EP_TUDEST },
                            SortType.AUTOMATIC);

        for (DBDataObjectAttributes attributes : attributesList) {
            String group = attributes.getField(FIELD_EP_GROUP).getAsString();
            String scope = attributes.getField(FIELD_EP_SCOPE).getAsString();
            String saaPrefix = attributes.getField(FIELD_EP_SAAPREFIX).getAsString();
            String hg = attributes.getField(FIELD_EP_HGDEST).getAsString();
            String g = attributes.getField(FIELD_EP_GDEST).getAsString();
            String tu = attributes.getField(FIELD_EP_TUDEST).getAsString();

            EinPasId einPasId = new EinPasId(hg, g, tu);
            OpsId opsId = new OpsId(group, scope);
            if (opsId.isValidId() && einPasId.isValidId()) {
                List<SaaAndEinpas> einPasIds = mappingToEinPas.get(opsId);
                if (einPasIds == null) {
                    // gibt es noch nicht, erstellen und einfügen
                    einPasIds = new ArrayList<SaaAndEinpas>();
                    mappingToEinPas.put(opsId, einPasIds);
                }

                einPasIds.add(new SaaAndEinpas(einPasId, saaPrefix));
            }
        }
    }

    public List<SaaAndEinpas> get(OpsId opsId) {
        if (mappingToEinPas != null) {
            List<SaaAndEinpas> list = mappingToEinPas.get(opsId);
            if (list != null) {
                return Collections.unmodifiableList(list);
            }
        }

        return null;
    }
}
