/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.eds;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModelElementUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.mapping.MappingOpsToEinPas;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.db.etkrecord.EtkRecords;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Repräsentation eines Baumusters der Kostruktionsstückliste EDS/BCS (Tabelle DA_EDS_MODEL).
 */
public class iPartsEdsModel implements iPartsConst {

    private static ObjectInstanceLRUList<Object, iPartsEdsModel> instances = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_MODELS,
                                                                                                         iPartsPlugin.getCachesLifeTime());

    protected List<iPartsEdsModelSaaRef> opsSubModuleRefIdSaaInModels;
    protected List<iPartsEdsModelSaaRef> modelElementUsageSubModuleRefIdSaaInModels;

    protected iPartsModelId modelId;
    protected EtkMultiSprache modelTitle;


    private iPartsCatalogNode cachedEinPasStructure;
    private iPartsCatalogNode cachedOpsStructure;
    private iPartsCatalogNode cachedModelElementUsageStructure;

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized iPartsEdsModel getInstance(EtkProject project, iPartsModelId modelId) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsEdsModel.class, modelId.getModelNumber(), false);
        iPartsEdsModel result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsEdsModel(project, modelId);
            instances.put(hashObject, result);
        }

        return result;
    }

    protected iPartsEdsModel(EtkProject project, iPartsModelId modelId) {
        this.modelId = modelId;
        loadHeader(project);
    }

    private boolean loadHeader(EtkProject project) {
        if (modelId.isValidId()) {
            modelTitle = new EtkMultiSprache(modelId.getModelNumber(), project.getConfig().getDatabaseLanguages());
            return true;
        } else {
            modelTitle = new EtkMultiSprache("!!Baumuster '%1' nicht gefunden", project.getConfig().getDatabaseLanguages(),
                                             modelId.getModelNumber());
            return false;
        }
    }


    public String getModelNumber() {
        return modelId.getModelNumber();
    }

    protected iPartsCatalogNode getOrCreateOpsNode(iPartsCatalogNode parentNode, OpsId opsId) {
        // Erst die OPS-Struktur erstellen oder holen
        iPartsCatalogNode groupNode = parentNode.getOrCreateChild(new OpsId(opsId.getGroup(), ""), true);
        iPartsCatalogNode scopeNode = groupNode.getOrCreateChild(opsId, true);

        return scopeNode;
    }

    protected iPartsCatalogNode getOrCreateModelElementUsageNode(iPartsCatalogNode parentNode, ModelElementUsageId modelElementUsageId) {
        // Erst die ModelElementUsage-Struktur erstellen oder holen
        iPartsCatalogNode moduleNode = parentNode.getOrCreateChild(new ModelElementUsageId(modelElementUsageId.getModule(), ""), true);
        iPartsCatalogNode subModuleNode = moduleNode.getOrCreateChild(modelElementUsageId, true);

        return subModuleNode;
    }

    protected iPartsCatalogNode getOrCreateEinPasNode(iPartsCatalogNode parentNode, EinPasId einPasId) {
        // Erst die EinPAS-Struktur erstellen oder holen
        iPartsCatalogNode einPasHgNode = parentNode.getOrCreateChild(new EinPasId(einPasId.getHg(), "", ""), true);
        iPartsCatalogNode einPasGNode = einPasHgNode.getOrCreateChild(new EinPasId(einPasId.getHg(), einPasId.getG(), ""), true);
        iPartsCatalogNode einPasTuNode = einPasGNode.getOrCreateChild(einPasId, false);

        return einPasTuNode;
    }

    // Module der OPS Struktur laden
    protected void loadOPSIfNeeded(EtkProject project) {
        if (opsSubModuleRefIdSaaInModels == null) {
            List<iPartsEdsModelSaaRef> opsSubModuleRefIdSaaInModelsLocal = loadModelData(project);
            synchronized (this) {
                if (opsSubModuleRefIdSaaInModels == null) {
                    opsSubModuleRefIdSaaInModels = opsSubModuleRefIdSaaInModelsLocal;
                }
            }
        }
    }

    protected void loadModelElementUsageIfNeeded(EtkProject project) {
        if (modelElementUsageSubModuleRefIdSaaInModels == null) {
            List<iPartsEdsModelSaaRef> modelElementUsageSubModuleRefIdSaaInModelsLocal = loadModelData(project);
            synchronized (this) {
                if (modelElementUsageSubModuleRefIdSaaInModels == null) {
                    modelElementUsageSubModuleRefIdSaaInModels = modelElementUsageSubModuleRefIdSaaInModelsLocal;
                }
            }
        }
    }

    private List<iPartsEdsModelSaaRef> loadModelData(EtkProject project) {
        iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
        List<iPartsEdsModelSaaRef> newSubModuleRefIdSaaInModels = new DwList<>();

        EtkRecords recs = project.getDB().getRecords(structureHelper.getStructureTableName(), new String[]{ structureHelper.getModelNumberField() },
                                                     new String[]{ getModelNumber() });

        String upperValueField = structureHelper.getUpperStructureValueField();
        String lowerValueField = structureHelper.getLowerStructureValueField();
        String subElementField = structureHelper.getSubElementField();
        recs.sort(new String[]{ upperValueField, lowerValueField, subElementField });

        for (EtkRecord rec : recs) {
            String saa = rec.getField(subElementField).getAsString();

            // Baumuster-Aggregate ignorieren im OPS-Scope
            if (!saa.startsWith(MODEL_NUMBER_PREFIX_AGGREGATE)) {
                String upperValue = rec.getField(upperValueField).getAsString();
                String lowerValue = rec.getField(lowerValueField).getAsString();
                newSubModuleRefIdSaaInModels.add(new iPartsEdsModelSaaRef(upperValue, lowerValue, new EdsSaaId(saa)));
            }
        }
        return newSubModuleRefIdSaaInModels;
    }

    public iPartsCatalogNode getCompleteOpsStructure(EtkProject project) {
        if (cachedOpsStructure != null) {
            return cachedOpsStructure;
        }

        loadOPSIfNeeded(project);

        iPartsCatalogNode result = new iPartsCatalogNode(modelId, true);
        for (iPartsEdsModelSaaRef saaSaaIn : opsSubModuleRefIdSaaInModels) {
            iPartsCatalogNode parentNode = getOrCreateOpsNode(result, saaSaaIn.getOpsId());
            parentNode.addChild(new iPartsCatalogNode(saaSaaIn.getEdsSaaId()));
        }

        synchronized (this) {
            if (cachedOpsStructure == null) {
                cachedOpsStructure = result;
            }
        }

        return cachedOpsStructure;
    }

    public iPartsCatalogNode getCompleteModelElementUsageStructure(EtkProject project) {
        if (cachedModelElementUsageStructure != null) {
            return cachedModelElementUsageStructure;
        }

        loadModelElementUsageIfNeeded(project);

        iPartsCatalogNode result = new iPartsCatalogNode(modelId, true);
        for (iPartsEdsModelSaaRef saaSaaIn : modelElementUsageSubModuleRefIdSaaInModels) {
            iPartsCatalogNode parentNode = getOrCreateModelElementUsageNode(result, saaSaaIn.getModelElementUsageId());
            parentNode.addChild(new iPartsCatalogNode(saaSaaIn.getEdsSaaId()));
        }

        synchronized (this) {
            if (cachedModelElementUsageStructure == null) {
                cachedModelElementUsageStructure = result;
            }
        }

        return cachedModelElementUsageStructure;
    }

    public iPartsCatalogNode getCompleteEinPasStructureFromOps(EtkProject project) {
        if (cachedEinPasStructure != null) {
            return cachedEinPasStructure;
        }

        loadOPSIfNeeded(project);

        MappingOpsToEinPas mapping = MappingOpsToEinPas.getInstance(project);

        iPartsCatalogNode result = new iPartsCatalogNode(modelId, true);
        for (iPartsEdsModelSaaRef saaSaaIn : opsSubModuleRefIdSaaInModels) {
            // Alle EinPAS-Knoten, an das das Modul direkt eingehängt wurde
            List<EinPasId> einPasNodes = new DwList<>();
            List<MappingOpsToEinPas.SaaAndEinpas> mappedValues = mapping.get(saaSaaIn.getOpsId());

            List<iPartsEdsModelSaaRef> saaWithoutMappingList = new DwList<>();

            if (mappedValues != null) {
                for (MappingOpsToEinPas.SaaAndEinpas mappedDestination : mappedValues) {
                    if (saaSaaIn.getEdsSaaId().getSaaNumber().startsWith(mappedDestination.saaPrefix)) {
                        if (mappedDestination != null) {
                            einPasNodes.add(mappedDestination.einPasId);
                        }
                    }
                }
            }


            if (einPasNodes.isEmpty()) {
                // Für diese SAA in dieser OPS ist kein Mapping angegeben
                saaWithoutMappingList.add(saaSaaIn);
            }

            // Jetzt die Knoten einfügen
            for (EinPasId einPasId : einPasNodes) {
                iPartsCatalogNode parent = getOrCreateEinPasNode(result, einPasId);
                parent.addChild(new iPartsCatalogNode(saaSaaIn.getEdsSaaId()));
            }

            // Und die nicht gemappten
            for (iPartsEdsModelSaaRef saaWithoutMapping : saaWithoutMappingList) {
                // Knoten für die fehlenden finden oder erstellen
                iPartsCatalogNode missingNode = result.getOrCreateChild(new EinPasId(TranslationHandler.translate("!!Fehlendes EinPAS-Mapping"), "", ""), true);

                iPartsCatalogNode parentNode = getOrCreateOpsNode(missingNode, saaWithoutMapping.getOpsId());
                parentNode.addChild(new iPartsCatalogNode(saaWithoutMapping.getEdsSaaId()));

            }


        }

        synchronized (this) {
            if (cachedEinPasStructure == null) {
                cachedEinPasStructure = result;
            }
        }

        return cachedEinPasStructure;
    }

    public EtkMultiSprache getModelTitle() {
        return modelTitle;
    }
}
