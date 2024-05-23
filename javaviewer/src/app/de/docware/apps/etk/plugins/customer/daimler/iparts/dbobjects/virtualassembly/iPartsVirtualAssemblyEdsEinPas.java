/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ConstructionValidationDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.eds.iPartsEdsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.misc.id.IdWithType;

import java.util.List;

/**
 * Virtuelle Sicht auf die EDS-Daten gemappt nach EinPAS
 */
public class iPartsVirtualAssemblyEdsEinPas extends iPartsVirtualAssemblyEdsBase {

    private final boolean withFlatten = true;

    public iPartsVirtualAssemblyEdsEinPas(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        super(project, virtualIds, assemblyId);
    }

    @Override
    public DBDataObjectList<EtkDataPartListEntry> loadPartList(boolean subAssembliesOnly, EtkDisplayFields fields) {
        iPartsEdsModel model = iPartsEdsModel.getInstance(getEtkProject(), (iPartsModelId)(getRootNode().getId()));
        iPartsCatalogNode nodes = model.getCompleteEinPasStructureFromOps(getEtkProject());
        EinPasId subIdEinPas = null;
        EdsSaaId subIdSaa = null;
        OpsId subIdOps = null;
        // Suche den EinPAS- bzw. Saa-Knoten
        for (int i = getVirtualIds().size() - 1; i > 0; i--) {
            IdWithType iPartsVirtualNodeId = getVirtualIds().get(i).getId();
            if (iPartsVirtualNodeId instanceof EinPasId) {
                subIdEinPas = (EinPasId)iPartsVirtualNodeId;
                break;
            }

            if (iPartsVirtualNodeId instanceof OpsId) {
                subIdOps = (OpsId)iPartsVirtualNodeId;
                break;
            }
        }


        // Suche, ob der letzte Knoten ein Saa-Knoten ist
        for (int i = getVirtualIds().size() - 1; i > 0; i--) {
            IdWithType iPartsVirtualNodeId = getVirtualIds().get(i).getId();
            if (iPartsVirtualNodeId instanceof EdsSaaId) {
                subIdSaa = (EdsSaaId)iPartsVirtualNodeId;
                break;
            }
        }
        if (subIdSaa != null) {
            ConstructionValidationDateHelper validationHelper = ConstructionValidationDateHelper.getSaaPartsListConstructionDateHelper();
            return partsListHelper.loadVirtualSaaPartsList(subIdSaa.getSaaNumber(), subIdSaa.getSaaNumber(), subAssembliesOnly, validationHelper);
        } else {
            if (subIdOps != null) {
                // Knoten ohne Mapping werden als Ops Knoten angezeigt
                return loadVirtualEdsStructure(nodes, getRootNode(), subIdOps);
            }


            if (withFlatten) {
                if ((subIdEinPas != null) && subIdEinPas.isTuNode()) {
                    return loadVirtualFlattenEdsSaa(nodes, subIdEinPas, subAssembliesOnly);
                }
            }
            // Knoten für subIdEinPas oder subIdEinPas == null, dann erste Ebene laden
            return loadVirtualEdsEinPas(nodes, getRootNode(), subIdEinPas);

        }
    }


    @Override
    public String getPartsListType() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof EinPasId) { // EinPAS
            EinPasId einPasId = (EinPasId)lastNodeId;
            if (einPasId.isTuNode()) {
                // Unter dem EinPas-TU bei der Anzeige der Konstruktionsdaten aus EDS werden die SAAs angezeigt
                if (withFlatten) {
                    return PARTS_LIST_TYPE_EDS_SAA;
                }
                return PARTS_LIST_TYPE_EINPAS_TU_EDS;
            }
        }
        return super.getPartsListType();
    }

    @Override
    public EtkMultiSprache getTexts() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof iPartsModelId) { // Baumuster
            iPartsEdsModel model = iPartsEdsModel.getInstance(getEtkProject(), (iPartsModelId)lastNodeId);

            String titlePostFix = " (EinPAS)";

            // Die Art des Einstiegs in dem mehrsprachigen Feld hinzufügen
            EtkMultiSprache result = new EtkMultiSprache();
            EtkMultiSprache modelTitle = model.getModelTitle();

            List<String> fallbackLanguages = getEtkProject().getDataBaseFallbackLanguages();
            for (String lang : getEtkProject().getConfig().getDatabaseLanguages()) {
                result.setText(lang, modelTitle.getTextByNearestLanguage(lang, fallbackLanguages) + titlePostFix);
            }

            return result;
        }

        return super.getTexts();
    }
}
