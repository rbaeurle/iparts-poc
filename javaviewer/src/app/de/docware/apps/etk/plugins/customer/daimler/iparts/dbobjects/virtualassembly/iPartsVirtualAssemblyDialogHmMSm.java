/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDatasetWithEntries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.ArrayUtil;
import de.docware.util.CanceledException;
import de.docware.util.collections.weak.WeakKeysMap;
import de.docware.util.misc.id.IdWithType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Virtuelle Sicht auf DIALOG-Daten in HM/M/SM Sicht
 */
public class iPartsVirtualAssemblyDialogHmMSm extends iPartsVirtualAssemblyDialogBase {

    public iPartsVirtualAssemblyDialogHmMSm(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        super(project, virtualIds, assemblyId);
    }

    @Override
    public DBDataObjectAttributes loadAssemblyHeadAttributes(String[] resultFields) {
        DBDataObjectAttributes result = super.loadAssemblyHeadAttributes(resultFields);
        String partsListType = getPartsListType();
        if (partsListType.equals(PARTS_LIST_TYPE_DIALOG_HM) || partsListType.equals(PARTS_LIST_TYPE_DIALOG_M) ||
            partsListType.equals(PARTS_LIST_TYPE_DIALOG_SM)) {
            iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
            IdWithType lastNodeId = lastVirtualNode.getId();

            if (lastNodeId instanceof HmMSmId) { // HM/M/SM
                HmMSmId hmMSmId = (HmMSmId)lastNodeId;
                fillHmMSmAttributes(result, hmMSmId, false);
            }
        }

        return result;
    }

    @Override
    public DBDataObjectList<EtkDataPartListEntry> loadPartList(boolean subAssembliesOnly, EtkDisplayFields fields) {
        iPartsDialogSeries series = iPartsDialogSeries.getInstance(getEtkProject(), (iPartsSeriesId)(getRootNode().getId()));
        iPartsCatalogNode nodes = series.getCompleteHmMSmStructure(getEtkProject());
        HmMSmId subId = null;
        // Suche den HM/M/SM-Knoten
        for (int i = getVirtualIds().size() - 1; i > 0; i--) {
            IdWithType iPartsVirtualNodeId = getVirtualIds().get(i).getId();
            if (iPartsVirtualNodeId instanceof HmMSmId) {
                subId = (HmMSmId)iPartsVirtualNodeId;
                break;
            }
        }
        // Knoten für subId oder subId == null, dann erste Ebene laden
        return loadVirtualDialogHmMSm(nodes, getRootNode(), subId, subAssembliesOnly, fields);

    }

    @Override
    public EtkMultiSprache getTexts() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();
        if (lastNodeId instanceof iPartsSeriesId) { // Baureihe
            iPartsDialogSeries series = iPartsDialogSeries.getInstance(getEtkProject(), (iPartsSeriesId)lastNodeId);

            String titlePostFix = " (HM/M/SM)";

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


    /**
     * Sucht in der DialogStruktur nach den Werten aus der Suche
     *
     * @param optionalRootAssemblyId
     * @param selectFields
     * @param selectValues
     * @param whereFields
     * @param andOrWhereValues
     * @param andOrSearch
     * @param wildCardSettings
     * @param project
     * @param multiLanguageCache
     * @return
     */
    public static iPartsSearchVirtualDataset searchPartListEntriesForHmMSmStruct(final AssemblyId optionalRootAssemblyId,
                                                                                 final EtkDisplayFields selectFields,
                                                                                 final List<String> selectValues,
                                                                                 final EtkDisplayFields whereFields,
                                                                                 final List<String> andOrWhereValues,
                                                                                 final boolean andOrSearch,
                                                                                 final WildCardSettings wildCardSettings,
                                                                                 EtkProject project,
                                                                                 WeakKeysMap<String, String> multiLanguageCache) {
        return new iPartsSearchVirtualDatasetWithEntries(selectFields, project, multiLanguageCache) {
            @Override
            public List<EtkDataPartListEntry> createResultPartListEntries() throws CanceledException {
                String series = iPartsVirtualNode.getSeriesNumberFromAssemblyId(optionalRootAssemblyId);

                // Jetzt einfach alle Treffer aus dem HmMSm-Object holen
                List<HmMSm> listOfAllHmMSm = HmMSm.getALLHmMSmInstances(project);
                List<EtkDataPartListEntry> result = new ArrayList<EtkDataPartListEntry>();

                for (HmMSm hmMSm : listOfAllHmMSm) {
                    List<HmMSmNode> hmMSmNodes = hmMSm.search(selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings, project.getDBLanguage(), project.getDataBaseFallbackLanguages());

                    if (!hmMSmNodes.isEmpty()) {
                        List<String> validSeriesList = new ArrayList<String>();

                        if (series != null) {
                            validSeriesList.add(series);
                        }

                        if (series == null) {
                            // Weder auf ein Baumuster oder eine Baureihe ist eingeschränkt
                            // alles ist deshalb möglich -> hole deshalb alles aus der Datenbank
                            DBDataObjectAttributesList recs = project.getDbLayer().getAttributesListCancelable(TABLE_DA_SERIES, new String[]{}, new String[]{});
                            for (DBDataObjectAttributes rec : recs) {
                                validSeriesList.add(rec.getField(FIELD_DS_SERIES_NO).getAsString());
                            }
                        }


                        // Und jetzt alle gültigen Knoten einfügen
                        // dazu die ParentAssembly ermitteln und davon alle Childs für die spätere Filterung zurückliefern
                        Set<AssemblyId> resultAssemblyIds = new HashSet<>(); // Set mit allen AssemblyIds, um doppeltes Laden zu vermeiden
                        for (HmMSmNode hmMSmNode : hmMSmNodes) {
                            for (String seriesNo : validSeriesList) {
                                if (Session.currentSessionThreadAppActionCancelled()) {
                                    throw new CanceledException(null);
                                }

                                List<iPartsVirtualNode> nodes = new ArrayList<>();

                                nodes.add(new iPartsVirtualNode(iPartsNodeType.DIALOG_HMMSM, new iPartsSeriesId(seriesNo)));
                                if (hmMSmNode.getParent() != null) {
                                    nodes.add(new iPartsVirtualNode(iPartsNodeType.HMMSM, hmMSmNode.getParent().getId()));
                                }
                                AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(ArrayUtil.toArray(nodes)), "");
                                if (!resultAssemblyIds.contains(assemblyId)) {
                                    EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);

                                    if (assembly.existsInDB()) {
                                        // Alle ungefilterten Unterbaugruppen vom HM/M/SM Knoten zurückliefern (darunter befindet sich auch der gesuchte HM/M/SM Knoten)
                                        result.addAll(assembly.getSubAssemblyEntries(false, null));
                                    }
                                    resultAssemblyIds.add(assemblyId);
                                }
                            }
                        }
                    }
                }
                return result;
            }
        };
    }

}
