/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.misc.id.IdWithType;

import java.util.List;

/**
 * Virtuelle Sicht auf DIALOG-Daten gemappt nach EinPAS
 */

public class iPartsVirtualAssemblyDialogEinPas extends iPartsVirtualAssemblyDialogBase {

    public iPartsVirtualAssemblyDialogEinPas(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        super(project, virtualIds, assemblyId);
    }

    @Override
    public DBDataObjectList<EtkDataPartListEntry> loadPartList(boolean subAssembliesOnly, EtkDisplayFields fields) {
        iPartsDialogSeries series = iPartsDialogSeries.getInstance(getEtkProject(), (iPartsSeriesId)(getRootNode().getId()));
        iPartsCatalogNode nodes = series.getCompleteEinPasStructureFromHmMSm(getEtkProject());
        EinPasId subIdEinPas = null;
        HmMSmId subHmMSmId = null;
        // Suche den EinPAS- bzw. HM/M/SM-Knoten
        for (int i = getVirtualIds().size() - 1; i > 0; i--) {
            IdWithType iPartsVirtualNodeId = getVirtualIds().get(i).getId();
            if (iPartsVirtualNodeId instanceof EinPasId) {
                subIdEinPas = (EinPasId)iPartsVirtualNodeId;
                break;
            }
            if (iPartsVirtualNodeId instanceof HmMSmId) {
                subHmMSmId = (HmMSmId)iPartsVirtualNodeId;
                break;
            }
        }
        if (subHmMSmId != null) {
            // Es kann sein, dass hier unten kein HM/M/SM-Knoten ist; das kann sein, wenn es kein Mapping gibt
            // Dann wird einfach ein HM/M/SM-Knoten eingehängt
            return loadVirtualDialogHmMSm(nodes, getRootNode(), subHmMSmId, subAssembliesOnly, fields);
        } else {
            // Knoten für subIdEinPas oder subIdEinPas == null, dann erste Ebene laden
            return loadVirtualDialogEinPas(nodes, getRootNode(), subIdEinPas, subAssembliesOnly, fields);
        }
    }

    @Override
    public String getPartsListType() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof EinPasId) { // EinPAS
            EinPasId einPasId = (EinPasId)lastNodeId;
            if (einPasId.isTuNode()) {
                // Unter dem EinPas-TU bei der Anzeige der Konstruktionsdaten werden alle Teile in diesem TU angezeigt
                // Deshalb muss hier ein eigener Stücklistentyp da sein, in welchem die Dialog-Daten konfiguriert werden können
                return PARTS_LIST_TYPE_EINPAS_TU_DIALOG;
            }
        }
        return super.getPartsListType();
    }

    @Override
    public EtkMultiSprache getTexts() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();
        if (lastNodeId instanceof iPartsSeriesId) { // Baureihe
            iPartsDialogSeries series = iPartsDialogSeries.getInstance(getEtkProject(), (iPartsSeriesId)lastNodeId);

            String titlePostFix = " (EinPAS)";

            // Die Art des Einstiegs in dem mehrsprachigen Feld hinzufügen
            EtkMultiSprache result = new EtkMultiSprache();
            EtkMultiSprache seriesTitle = series.getSeriesTitle();

            List<String> fallbackLanguages = getEtkProject().getDataBaseFallbackLanguages();
            for (String lang : getEtkProject().getConfig().getDatabaseLanguages()) {
                result.setText(lang, seriesTitle.getTextByNearestLanguage(lang, fallbackLanguages) + titlePostFix);
            }

            return result;
        }

        return super.getTexts();
    }
}
