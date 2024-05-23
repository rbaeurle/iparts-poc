/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.ctt;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAAModelsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Klasse zum cachen der echten Struktur zu einem CTT Baumuster. "Echt" bedeutet, dass am Ende einer Baugruppe auch eine
 * Stückliste vorhanden ist.
 */
public class iPartsCTTModel implements iPartsConst {

    private static final ObjectInstanceLRUList<Object, iPartsCTTModel> INSTANCES = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_MODELS,
                                                                                                               iPartsPlugin.getCachesLifeTime());

    private final iPartsModelId modelId;
    private volatile iPartsCatalogNode cachedCTTStructure;
    private Map<String, String> saaToHmoMapping; // Mapping SAA-Nummer auf HMO
    private Map<String, String> hmoToSaaMapping; // Mapping HMO auf SAA-Nummer
    private volatile Set<EdsSaaId> saaIdsForModel;

    private EtkMultiSprache modelTitle;

    public static synchronized void clearCache() {
        INSTANCES.clear();
    }

    public static synchronized iPartsCTTModel getInstance(EtkProject project, iPartsModelId modelId) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsCTTModel.class, modelId.getModelNumber(), false);
        iPartsCTTModel result = INSTANCES.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsCTTModel(project, modelId);
            INSTANCES.put(hashObject, result);
        }

        return result;
    }

    protected iPartsCTTModel(EtkProject project, iPartsModelId modelId) {
        this.modelId = modelId;
        loadHeader(project);
    }

    private boolean loadHeader(EtkProject project) {
        this.saaToHmoMapping = new HashMap<>();
        this.hmoToSaaMapping = new HashMap<>();
        if (modelId.isValidId()) {
            modelTitle = new EtkMultiSprache(modelId.getModelNumber(), project.getConfig().getDatabaseLanguages());
            return true;
        } else {
            modelTitle = new EtkMultiSprache("!!Baumuster '%1' nicht gefunden", project.getConfig().getDatabaseLanguages(),
                                             modelId.getModelNumber());
            return false;
        }
    }

    public iPartsModelId getModelId() {
        return modelId;
    }

    public EtkMultiSprache getModelTitle() {
        return modelTitle;
    }

    /**
     * Liefert die komplette Struktur zum aktuellen Baumuster
     *
     * @param project
     * @return
     */
    public iPartsCatalogNode getCompleteCTTStructure(EtkProject project) {
        if (cachedCTTStructure != null) {
            return cachedCTTStructure;
        }

        loadIfNeeded(project);
        iPartsCatalogNode allNodesForModel = new iPartsCatalogNode(modelId, true);
        for (EdsSaaId structureId : saaIdsForModel) {
            allNodesForModel.getOrCreateChild(structureId, true);
        }
        synchronized (this) {
            if (cachedCTTStructure == null) {
                cachedCTTStructure = allNodesForModel;
            }
        }

        return cachedCTTStructure;
    }

    private void loadIfNeeded(EtkProject project) {
        if (saaIdsForModel == null) {
            EtkDisplayFields selectFields = new EtkDisplayFields();
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO, false, false));
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_HMO_SAA_MAPPING, FIELD_DHSM_HMO, false, false));
            Set<EdsSaaId> saas = new TreeSet<>();
            EtkDataObjectList.FoundAttributesCallback callback = getCallbackForSaaLoad(saas);
            iPartsDataSAAModelsList saaModelsList = new iPartsDataSAAModelsList();
            // Join von DA_EDS_SAA_MODELS auf DA_HMO_SAA_MAPPING und DA_EDS_CONST_KIT
            saaModelsList.searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields,
                                                    new String[]{ TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_MODEL_NO) },
                                                    new String[]{ modelId.getModelNumber() }, false, null,
                                                    false, callback,
                                                    new EtkDataObjectList.JoinData(TABLE_DA_HMO_SAA_MAPPING,
                                                                                   new String[]{ FIELD_DA_ESM_SAA_NO },
                                                                                   new String[]{ FIELD_DHSM_SAA }, false, false
                                                    ),
                                                    new EtkDataObjectList.JoinData(TABLE_DA_EDS_CONST_KIT,
                                                                                   new String[]{ TableAndFieldName.make(TABLE_DA_HMO_SAA_MAPPING, FIELD_DHSM_HMO) },
                                                                                   new String[]{ FIELD_DCK_SNR }, false, false
                                                    ));

            synchronized (this) {
                if (saaIdsForModel == null) {
                    saaIdsForModel = saas;
                }
            }
        }
    }

    private EtkDataObjectList.FoundAttributesCallback getCallbackForSaaLoad(Set<EdsSaaId> saas) {
        return new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String saa = attributes.getFieldValue(FIELD_DA_ESM_SAA_NO);
                if (StrUtils.isValid(saa)) {
                    saas.add(new EdsSaaId(saa));
                    String hmo = attributes.getFieldValue(FIELD_DHSM_HMO);
                    if (StrUtils.isValid(hmo)) {
                        // Beim Laden der Daten direkt die Mappings erzeugen
                        saaToHmoMapping.put(saa, hmo);
                        hmoToSaaMapping.put(hmo, saa);
                    }
                }
                return false;
            }
        };
    }

    public String getHmoForSaa(String saaNumber) {
        return saaToHmoMapping.get(saaNumber);
    }

    public String getSaaForHmo(String hmoNumber) {
        return hmoToSaaMapping.get(hmoNumber);
    }
}

