/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.search.model.EtkPartsSearchDataset;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.helper.iPartsSaaBkConstPartsListHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.SessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureNode;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.CanceledException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Spezielles {@link EtkPartsSearchDataset} für die virtuelle Navigation in iParts.
 */
public class iPartsSearchDataset extends EtkPartsSearchDataset {

    private List<iPartsSearchVirtualDataset> datasets = new ArrayList<iPartsSearchVirtualDataset>();
    // Mit diesem Dataset suchen wir gerade
    private int datasetIterator = -1;

    public iPartsSearchDataset(EtkProject project) {
        super(project);
    }

    /**
     * Baut {@link #datasets} auf.
     * Die Suche wird in Teilbereiche unterteilt in denen die Suche jeweils anders läuft. Details im u.a. Code bei den
     * jeweiligen if-Blöcken für die Teilbereiche. Jeder Teilbereich ist ein Element in {@link #datasets}.
     *
     * @param optionalRootAssemblyId    muss nicht ausgewertet werden, kann aber zu Performanceoptimierung verwendet werden
     * @param isSearchValuesDisjunction
     * @param selectFields
     * @param selectValues
     * @param whereFields
     * @param whereValues
     * @param andOrSearch
     * @param wildCardSettings
     */
    @Override
    public void createSearchInAllAssemblies(final AssemblyId optionalRootAssemblyId, final boolean isSearchValuesDisjunction,
                                            final EtkDisplayFields selectFields, final List<String> selectValues,
                                            final EtkDisplayFields whereFields, final List<String> whereValues,
                                            final boolean andOrSearch, WildCardSettings wildCardSettings,
                                            String language, String explicitSearchLanguage) {
        // je nach virtuellem Rootknoten einige Suchen von vornherein ausschließen
        iPartsVirtualNode virtualRootNode = iPartsVirtualNode.getVirtualRootNodeFromAssemblyId(optionalRootAssemblyId);

        iPartsNodeType rootType;
        if (virtualRootNode == null) {
            // Ohne Roottyp ist es ROOT :-), also normaler Einstieg ohne Einschränkung
            rootType = iPartsNodeType.ROOT;
        } else {
            rootType = virtualRootNode.getType();
        }


        // Wenn wir in der Struktur sind, dann kann es Knoten geben, die nur Konstruktion oder Retail sind
        // in diesem Fall können wir die speziellen Suchen überspringen
        boolean structureHasRetailPartList = false;
        boolean structureHasConstructionDialogPartList = false;
        boolean structureHasConstructionEdsPartList = false;
        boolean structureHasConstructionMBSPartList = false;
        boolean structureHasConstructionCTTPartList = false;

        /**
         * Suche in der Struktur
         * Das sind die Knoten zwischen Root und KG/TU, EINPAS, HM/M/SM, ...
         * Identifiziert werden diese Knoten aktuell durch den Knotentyp STRUCTURE.
         * Knoten mit Beschriftungen wie "PKW", "Konstruktion" würden in diesem Teilbereich gefunden.
         */
        boolean searchInStructure = (rootType == iPartsNodeType.ROOT) || rootType.isStructureType();
        if (searchInStructure) {
            datasets.add(iPartsVirtualAssemblyStructureBase.searchPartListEntriesForStructure(optionalRootAssemblyId,
                                                                                              selectFields, selectValues,
                                                                                              whereFields, whereValues,
                                                                                              andOrSearch, wildCardSettings, project, multiLanguageCache));

            // Wir sind in der Struktur, ermittele den aktuellen Knoten und suche in den Childs dieses Knotens,
            // ob Retail, Dialog oder Eds-Knoten enthalten sind. Die Struktur ist im Cache, deshalb ist das kein Performanceproblem.

            iPartsStructureNode structureNode = getStructureNode(rootType, virtualRootNode);

            // Über alle Unterknoten in der Struktur iterieren und testen, ob die speziellen Knoten dort enthalten sind
            if (structureNode != null) {
                List<iPartsStructureNode> nodes = new ArrayList<>();
                // Der eigentlich ausgewählte Knoten muss in die Liste
                nodes.add(structureNode);
                // Und noch alle seine Subknoten
                nodes.addAll(structureNode.getAllSubNodes());
                for (iPartsStructureNode node : nodes) {
                    if (!structureHasRetailPartList && node.hasRetailPartList()) {
                        structureHasRetailPartList = true;
                    }
                    if (!structureHasConstructionDialogPartList && node.hasDialogPartList()) {
                        structureHasConstructionDialogPartList = true;
                    }
                    if (!structureHasConstructionEdsPartList && node.hasEdsPartList()) {
                        structureHasConstructionEdsPartList = true;
                    }
                    if (!structureHasConstructionMBSPartList && node.hasMBSPartList()) {
                        structureHasConstructionMBSPartList = true;
                    }
                    if (!structureHasConstructionCTTPartList && node.hasCTTPartList()) {
                        structureHasConstructionCTTPartList = true;
                    }

                    // Alle relevanten Knotentypen wurden gefunden -> Schleife abbrechen
                    if (structureHasRetailPartList && structureHasConstructionDialogPartList && structureHasConstructionEdsPartList
                        && structureHasConstructionMBSPartList && structureHasConstructionCTTPartList) {
                        break;
                    }
                }
            }
        }

        // Befinden wir uns in einem EDS/BCS Konstruktionsknoten, dann muss überprüft werden, ob der Benutzer schon
        // EDS/BCS Baumuster ausgewählt hat. Ist keins ausgewählt, dann brauchen wir in der EDS/BCS Struktur nicht suchen
        // (und erst Recht nicht im EinPAS-Mapping zu EDS/BCS).
        boolean rightForTruckAndBus = iPartsRight.checkTruckAndBusInSession();
        boolean searchInEDSBCSNodes = SessionKeyHelper.isConstructionModelForEDSBCSSelected() && rightForTruckAndBus;

        /**
         * Suche in der EinPAS-Struktur, gültig bei allen Strukturen + der speziellen EinPAS-Strukturen
         * Knoten wie "21: Bremsen" würden bei Suche nach "Bremsen" in diesem Teilbereich gefunden.
         */
        // Ist das EinPAS-Mapping ausgeschaltet, dann brauchen wir in der EinPAS-Struktur nicht zu suchen
        Boolean showEinPASMapping = Session.get().getAttribute(iPartsPlugin.SESSION_KEY_SHOW_EINPAS_MAPPING, false);
        boolean searchInEinPas = showEinPASMapping && (searchInStructure
                                                       || rootType.isProductEinPASType()
                                                       || (rootType == iPartsNodeType.DIALOG_EINPAS)
                                                       || ((rootType == iPartsNodeType.EDS_EINPAS) && searchInEDSBCSNodes));
        if (searchInEinPas) {
            iPartsStructureNode structureNode = getStructureNode(rootType, virtualRootNode);
            iPartsConst.STRUCTURE_CONSTRUCTION_TYPE constType = (structureNode != null) ? structureNode.getConstructionType()
                                                                                        : iPartsConst.STRUCTURE_CONSTRUCTION_TYPE.NONE;

            datasets.add(iPartsVirtualAssemblyEinPasBase.searchPartListEntriesForEinPasStruct(constType,
                                                                                              optionalRootAssemblyId,
                                                                                              selectFields, selectValues,
                                                                                              whereFields, whereValues,
                                                                                              andOrSearch, wildCardSettings,
                                                                                              project,
                                                                                              multiLanguageCache));
        }

        /**
         * Suche in der KG/TU-Struktur, gültig nur wenn die Struktur Retailstücklisten enthält oder es ein KG/TU Knoten ist.
         * Die Suche wird nur durchgeführt, wenn ein Produkt ausgewählt wurde - das wurde mit Daimler so vereinbart.
         * Knoten wie "15: Motorelektrik" würden bei Suche nach "Motorelektrik" in diesem Teilbereich gefunden.
         */
        boolean searchInKgTu = structureHasRetailPartList || rootType.isProductKgTuType();
        if (searchInKgTu) {
            datasets.add(iPartsVirtualAssemblyEinPasBase.searchPartListEntriesForKgTuStruct(optionalRootAssemblyId,
                                                                                            selectFields, selectValues,
                                                                                            whereFields, whereValues,
                                                                                            andOrSearch, wildCardSettings,
                                                                                            project,
                                                                                            multiLanguageCache));
        }

        // Suche nach SAs innerhalb eines KG/TU-Produkts
        if ((structureHasRetailPartList || rootType.isProductKgTuType()) && !rootType.isProductEinPASType()) {
            datasets.add(iPartsVirtualAssemblyProductKgTu.searchPartListEntriesForKgSaStruct(optionalRootAssemblyId,
                                                                                             selectFields, selectValues,
                                                                                             whereFields, whereValues,
                                                                                             andOrSearch, wildCardSettings,
                                                                                             project,
                                                                                             multiLanguageCache));
        }

        // Befinden wir uns in einem MBS Konstruktionsknoten, dann muss überprüft werden, ob der Benutzer schon
        // MBS Baumuster ausgewählt hat. Ist keins ausgewählt, dann brauchen wir in der MBS Struktur nicht suchen
        boolean searchInMBSNodes = SessionKeyHelper.isConstructionModelForMBSSelected() && rightForTruckAndBus;

        // Befinden wir uns in einem CTT Konstruktionsknoten, dann muss überprüft werden, ob der Benutzer schon
        // CTT Baumuster ausgewählt hat. Ist keins ausgewählt, dann brauchen wir in der CTT Struktur nicht zu suchen
        boolean searchInCTTNodes = SessionKeyHelper.isConstructionModelForCTTSelected() && rightForTruckAndBus;

        // nur im EditMode in den Konstruktions-Strukturen suchen
        boolean searchInConstruction = project.isEditModeActive() && iPartsRight.EDIT_PARTS_DATA.checkRightInSession();

        if (searchInConstruction) {
            // Suche in der HM/M/SM-Struktur, nur wenn die Struktur Dialog-Stücklisten enthält oder es ein HM/M/SM Knoten ist
            boolean searchInHmMSm = (structureHasConstructionDialogPartList || (rootType == iPartsNodeType.DIALOG_HMMSM))
                                    && iPartsRight.checkCarAndVanInSession();
            if (searchInHmMSm) {
                datasets.add(iPartsVirtualAssemblyDialogHmMSm.searchPartListEntriesForHmMSmStruct(optionalRootAssemblyId,
                                                                                                  selectFields, selectValues,
                                                                                                  whereFields, whereValues,
                                                                                                  andOrSearch, wildCardSettings,
                                                                                                  project,
                                                                                                  multiLanguageCache));
            }

            // Suche in der EDS-Struktur, nur wenn ein Konstruktionsbaumuster ausgewählt wurde und die Struktur
            // EDS-Stücklisten enthält oder es ein EDS Struktur Knoten ist
            boolean searchInEdsStructure = searchInEDSBCSNodes && (structureHasConstructionEdsPartList
                                                                   || iPartsEdsStructureHelper.isEdsModelStructureNode(rootType));

            // Suche SAA's in EDS-Struktur
            if (searchInEdsStructure) {
                datasets.add(iPartsVirtualAssemblyEdsStructure.searchSaaConstKitListEntriesForEdsStruct(selectFields, selectValues,
                                                                                                        whereValues, andOrSearch,
                                                                                                        project, multiLanguageCache));

                datasets.add(iPartsVirtualAssemblyEdsStructure.searchPartListEntriesForEdsStruct(selectFields, selectValues,
                                                                                                 whereFields, whereValues,
                                                                                                 andOrSearch, wildCardSettings,
                                                                                                 project,
                                                                                                 multiLanguageCache));
            }

            // Suche in MBS Struktur
            boolean searchInMBS = searchInMBSNodes && (structureHasConstructionMBSPartList || (rootType == iPartsNodeType.MBS_STRUCTURE));
            if (searchInMBS) {
                // speziell die Strukturknoten
                datasets.add(iPartsVirtualAssemblyMBS.searchPartListEntriesWithinMBSStruct(selectFields, selectValues,
                                                                                           whereFields, whereValues,
                                                                                           andOrSearch, wildCardSettings,
                                                                                           project,
                                                                                           multiLanguageCache));
            }

            // Suche in CTT
            boolean searchInCTT = searchInCTTNodes && (structureHasConstructionCTTPartList || (rootType == iPartsNodeType.CTT_MODEL));

            if (searchInCTT) {
                // Suche in der Baumuster <-> SAA Beziehungen (z.B. SAA Benennung)
                datasets.add(iPartsVirtualAssemblyCTT.searchPartListEntriesWithinCTTStruct(selectFields, selectValues,
                                                                                           whereValues, andOrSearch,
                                                                                           project, multiLanguageCache));
            }
        }

        // Suche in normalen Retailstücklisten, nur wenn die Struktur Retailstücklisten enthält oder wir sind nicht
        // in der Konstruktion
        // Hier werden alle ungültigen ausgeschlossen (negative Logik)
        boolean searchInRegularPartLists = structureHasRetailPartList
                                           || (!rootType.isStructureType()
                                               && (rootType != iPartsNodeType.DIALOG_HMMSM)
                                               && (rootType != iPartsNodeType.DIALOG_EINPAS)
                                               && (rootType != iPartsNodeType.EDS_EINPAS)
                                               && (rootType != iPartsNodeType.MBS_STRUCTURE)
                                               && (rootType != iPartsNodeType.CTT_MODEL)
                                               && !iPartsEdsStructureHelper.isEdsModelStructureNode(rootType));
        if (searchInRegularPartLists) {
            // Es muss nur explizit zusätzlich in den jeweils gültigen Aggregaten gesucht werden, wenn diese in der
            // Stückliste beigemischt sind. Dadurch können auch bei Einschränkung auf ein Fahrzeugprodukt bei (Suche in)
            // die zugeordneten Aggregate mit durchsucht werden.
            // Wenn ohne Einschränkung gesucht wird, dann werden regulär Suchtreffer in den Aggregaten gefunden, da diese
            // eigene Produkte sind.
            boolean includeAggs = iPartsProduct.isProductStructureWithAggregatesForSession();
            Collection<iPartsProduct> additionalAggregates = null;
            if (includeAggs && iPartsFilter.get().isAggModelsFilterActive()) {
                // die relevanten zusätzlichen Aggregate-Produkte bestimmen
                additionalAggregates = iPartsFilter.get().getCurrentDataCard().getSubDatacardsProducts(project);
            }

            datasets.add(iPartsVirtualAssemblyEinPasBase.searchPartListEntriesForRetailStructures(optionalRootAssemblyId,
                                                                                                  null, null, null,
                                                                                                  isSearchValuesDisjunction,
                                                                                                  selectFields, selectValues,
                                                                                                  whereFields, whereValues,
                                                                                                  andOrSearch, project,
                                                                                                  multiLanguageCache,
                                                                                                  wildCardSettings,
                                                                                                  includeAggs, additionalAggregates));

            datasets.add(iPartsVirtualAssemblyEinPasBase.searchPartListEntriesForRetailSAs(optionalRootAssemblyId,
                                                                                           isSearchValuesDisjunction,
                                                                                           selectFields, selectValues,
                                                                                           whereFields, whereValues,
                                                                                           andOrSearch, project,
                                                                                           multiLanguageCache,
                                                                                           wildCardSettings,
                                                                                           includeAggs, additionalAggregates));

            datasets.add(iPartsVirtualAssemblyEinPasBase.searchAssemblyEntriesForRetailStructures(optionalRootAssemblyId,
                                                                                                  isSearchValuesDisjunction,
                                                                                                  selectFields, selectValues,
                                                                                                  whereFields, whereValues,
                                                                                                  andOrSearch, project,
                                                                                                  multiLanguageCache,
                                                                                                  wildCardSettings));
        }

        // nur im EditMode in den Konstruktions-Stücklisten suchen
        if (searchInConstruction) {
            // Suche in DIALOG-Stücklisten, nur wenn die Struktur Dialog-Stücklisten enthält oder wir sind nicht
            // in der Retailstückliste oder EDS bzw MBS
            // Hier werden alle ungültigen ausgeschlossen (negative Logik)
            boolean searchInDialogPartLists = structureHasConstructionDialogPartList
                                              || (!rootType.isStructureType()
                                                  && !rootType.isProductKgTuType()
                                                  && !rootType.isProductEinPASType()
                                                  && !iPartsEdsStructureHelper.isEdsModelStructureNode(rootType)
                                                  && (rootType != iPartsNodeType.EDS_EINPAS)
                                                  && (rootType != iPartsNodeType.CTT_MODEL)
                                                  && (rootType != iPartsNodeType.MBS_STRUCTURE));
            if (searchInDialogPartLists) {
                datasets.add(iPartsVirtualAssemblyDialogBase.searchPartListEntriesForDialogPartLists(optionalRootAssemblyId,
                                                                                                     isSearchValuesDisjunction,
                                                                                                     selectFields, selectValues,
                                                                                                     whereFields, whereValues,
                                                                                                     andOrSearch, project,
                                                                                                     multiLanguageCache,
                                                                                                     wildCardSettings));
            }

            // Suche in SAA-Stücklisten (EDS oder CTT), nur wenn ein Konstruktionsbaumuster ausgewählt wurde und die Struktur
            // SAA-Stücklisten enthält oder wir sind nicht in der Retailstückliste oder Dialog bzw MBS
            // Hier werden alle ungültigen ausgeschlossen (negative Logik)
            boolean searchInEdsPartsLists = searchInEDSBCSNodes && (structureHasConstructionEdsPartList
                                                                    || (!rootType.isStructureType()
                                                                        && !rootType.isProductKgTuType()
                                                                        && !rootType.isProductEinPASType()
                                                                        && (rootType != iPartsNodeType.DIALOG_HMMSM)
                                                                        && (rootType != iPartsNodeType.DIALOG_EINPAS)
                                                                        && (rootType != iPartsNodeType.MBS_STRUCTURE)
                                                                        && (rootType != iPartsNodeType.CTT_MODEL)));
            if (searchInEdsPartsLists) {
                datasets.add(iPartsSaaBkConstPartsListHelper.searchPartListEntriesInSaaPartLists(optionalRootAssemblyId,
                                                                                                 isSearchValuesDisjunction,
                                                                                                 selectFields, selectValues,
                                                                                                 whereFields, whereValues,
                                                                                                 andOrSearch, project,
                                                                                                 multiLanguageCache,
                                                                                                 wildCardSettings,
                                                                                                 true));
            }

            boolean searchInCTTPartsLists = searchInCTTNodes && (structureHasConstructionCTTPartList
                                                                 || (!rootType.isStructureType()
                                                                     && !rootType.isProductKgTuType()
                                                                     && !rootType.isProductEinPASType()
                                                                     && (rootType != iPartsNodeType.DIALOG_HMMSM)
                                                                     && (rootType != iPartsNodeType.DIALOG_EINPAS)
                                                                     && (rootType != iPartsNodeType.MBS_STRUCTURE)
                                                                     && !iPartsEdsStructureHelper.isEdsModelStructureNode(rootType)
                                                                     && (rootType != iPartsNodeType.EDS_EINPAS)));
            if (searchInCTTPartsLists) {
                datasets.add(iPartsSaaBkConstPartsListHelper.searchPartListEntriesInSaaPartLists(optionalRootAssemblyId,
                                                                                                 isSearchValuesDisjunction,
                                                                                                 selectFields, selectValues,
                                                                                                 whereFields, whereValues,
                                                                                                 andOrSearch, project,
                                                                                                 multiLanguageCache,
                                                                                                 wildCardSettings,
                                                                                                 false));
            }

            // Suche in MBS-Stücklisten, nur wenn ein Konstruktionsbaumuster ausgewählt wurde und die Struktur
            // MBS-Stücklisten enthält oder wir sind nicht in der Retailstückliste oder Dialog bzw EDS/BCS
            // Hier werden alle ungültigen ausgeschlossen (negative Logik)
            boolean searchInMBSPartsLists = searchInMBSNodes && (structureHasConstructionMBSPartList
                                                                 || (!rootType.isStructureType()
                                                                     && !rootType.isProductKgTuType()
                                                                     && !rootType.isProductEinPASType()
                                                                     && (rootType != iPartsNodeType.DIALOG_HMMSM)
                                                                     && (rootType != iPartsNodeType.DIALOG_EINPAS)
                                                                     && (rootType != iPartsNodeType.CTT_MODEL)
                                                                     && !iPartsEdsStructureHelper.isEdsModelStructureNode(rootType)
                                                                     && (rootType != iPartsNodeType.EDS_EINPAS)));
            if (searchInMBSPartsLists) {
                datasets.add(iPartsVirtualAssemblyMBS.searchPartListEntriesForMBSPartLists(optionalRootAssemblyId,
                                                                                           isSearchValuesDisjunction,
                                                                                           selectFields, selectValues,
                                                                                           whereFields, whereValues,
                                                                                           andOrSearch, project,
                                                                                           multiLanguageCache, wildCardSettings));
            }
        }
    }

    private iPartsStructureNode getStructureNode(iPartsNodeType rootType, iPartsVirtualNode virtualRootNode) {
        if (rootType == iPartsNodeType.ROOT) {
            return iPartsStructure.getInstance(project).getRootNode();
        } else if (virtualRootNode.getId() instanceof iPartsStructureId) {
            return iPartsStructure.getInstance(project).findNodeInAllChilds((iPartsStructureId)virtualRootNode.getId());
        }

        return null;
    }

    /**
     * Sucht alle Stücklisteneinträge mit den entsprechenden Suchwerten in den Modulen der EinPAS und KG/TU-Struktur im Retail,
     * wobei optional eine <i>optionalRootAssemblyId</i> angegeben werden kann, die bei enthaltenem Produkt die Suche
     * erheblich beschleunigt.
     * Außerdem kann über <i>optionalKgTuId</i> ODER <i>optionalEinPasId</i> eine Einschränkung auf einen KG/TU-Knoten
     * bzw. EinPAS-Knoten (und alle darunterliegenden Knoten) gemacht werden sowie über <i>optionalAggregateType</i> auf
     * den Aggregatetyp vom Produkt.
     *
     * @param optionalRootAssemblyId    Einschränkung per Produkt und ggf. Navigation
     * @param optionalKgTuId            Falls gesetzt, muss <i>optionalEinPasId</i> {@code null} sein.
     * @param optionalEinPasId          Falls gesetzt, muss <i>optionalKgTuId</i> {@code null} sein.
     * @param isSearchValuesDisjunction
     * @param selectFields
     * @param selectValues
     * @param whereFields
     * @param whereValues
     * @param andOrSearch
     * @param wildCardSettings
     * @param includeAggregates         Aggregate in Suche einbeziehen
     * @param additionalAggregates      Nur gültig bei {@code includeAggregates == true}: Optionale Liste der zusätzlichen
     *                                  Aggregate, die mit durchsucht werden sollen; bei {@code null} wird in allen Aggregate-Produkte
     *                                  gesucht, die für das Fahrzeug-Produkt gültig sind (sofern in einem Fahrzeug-Produkt
     *                                  gesucht wird)
     */
    public void createSearchInRetailPartlists(final AssemblyId optionalRootAssemblyId, final KgTuId optionalKgTuId,
                                              final EinPasId optionalEinPasId, final String optionalAggregateType,
                                              final boolean isSearchValuesDisjunction,
                                              final EtkDisplayFields selectFields, final List<String> selectValues,
                                              final EtkDisplayFields whereFields, final List<String> whereValues,
                                              final boolean andOrSearch, WildCardSettings wildCardSettings,
                                              boolean includeAggregates, final Collection<iPartsProduct> additionalAggregates) {
        datasets.add(iPartsVirtualAssemblyEinPasBase.searchPartListEntriesForRetailStructures(optionalRootAssemblyId,
                                                                                              optionalKgTuId, optionalEinPasId,
                                                                                              optionalAggregateType,
                                                                                              isSearchValuesDisjunction, selectFields,
                                                                                              selectValues, whereFields, whereValues,
                                                                                              andOrSearch, project, multiLanguageCache,
                                                                                              wildCardSettings, includeAggregates,
                                                                                              additionalAggregates));
    }

    /**
     * Sucht alle Stücklisteneinträge mit den entsprechenden Suchwerten in den Modulen der freien SAs im Retail, wobei
     * optional eine <i>optionalRootAssemblyId</i> angegeben werden kann, die bei enthaltenem Produkt die Suche erheblich
     * beschleunigt.
     *
     * @param optionalRootAssemblyId    Einschränkung per Produkt und ggf. Navigation
     * @param isSearchValuesDisjunction
     * @param selectFields
     * @param selectValues
     * @param whereFields
     * @param whereValues
     * @param andOrSearch
     * @param wildCardSettings
     * @param includeAggregates         Aggregate in Suche einbeziehen
     * @param additionalAggregates      Nur gültig bei {@code includeAggregates == true}: Optionale Liste der zusätzlichen
     *                                  Aggregate, die mit durchsucht werden sollen; bei {@code null} wird in allen Aggregate-Produkte
     *                                  gesucht, die für das Fahrzeug-Produkt gültig sind (sofern in einem Fahrzeug-Produkt
     *                                  gesucht wird)
     */
    public void createSearchInRetailPartlistsForSAs(final AssemblyId optionalRootAssemblyId,
                                                    final boolean isSearchValuesDisjunction,
                                                    final EtkDisplayFields selectFields, final List<String> selectValues,
                                                    final EtkDisplayFields whereFields, final List<String> whereValues,
                                                    final boolean andOrSearch, WildCardSettings wildCardSettings,
                                                    boolean includeAggregates, final Collection<iPartsProduct> additionalAggregates) {
        datasets.add(iPartsVirtualAssemblyEinPasBase.searchPartListEntriesForRetailSAs(optionalRootAssemblyId,
                                                                                       isSearchValuesDisjunction, selectFields,
                                                                                       selectValues, whereFields, whereValues,
                                                                                       andOrSearch, project, multiLanguageCache,
                                                                                       wildCardSettings, includeAggregates,
                                                                                       additionalAggregates));
    }


    @Override
    public void createSearchInOneAssembly(final AssemblyId assemblyId, final EtkDisplayFields selectFields, final List<String> selectValues,
                                          final EtkDisplayFields whereFields, final List<String> whereValues, final boolean andOrSearch,
                                          WildCardSettings wildCardSettings, String language, String explicitSearchLanguage) {
        MessageDialog.showError(TranslationHandler.translate("!!Falsche Suchstrategie für iParts in der DWK ausgewählt. Bitte '%1' auswählen.",
                                                             TranslationHandler.translate("!!Suche über Suchindex")));
    }

    @Override
    public void createSearchWithSearchIndex(AssemblyId rootAssemblyId, boolean sortByFirstColumn, EtkDisplayFields selectFields, List<String> selectValues,
                                            EtkDisplayFields whereFields, List<String> whereValues, boolean andOrSearch,
                                            WildCardSettings wildCardSettings, String language, String explicitSearchLanguage) {
        MessageDialog.showError(TranslationHandler.translate("!!Falsche Suchstrategie für iParts in der DWK ausgewählt. Bitte '%1' auswählen.",
                                                             TranslationHandler.translate("!!Suche über Suchindex")));
    }

    @Override
    public EtkDataPartListEntry[] get() {
        return datasets.get(datasetIterator).get();
    }

    /**
     * Hier passieren die Abfragen - ein etwas unerwarteter Seiteneffekte einer Methode mit Namen next() ;-)
     *
     * @return
     */
    @Override
    public boolean next() throws CanceledException {
        while ((datasetIterator < 0) || !datasets.get(datasetIterator).next()) {
            // wir beginnen mit der Suche, oder das zuletzt durchsuchte dataset ist am Ende
            // schalte auf das nächste dataset
            datasetIterator++;
            if (datasetIterator >= datasets.size()) {
                // Jetzt sind wirklich alle Ergebnisslisten am Ende
                return false;
            }

            if (Thread.currentThread().isInterrupted()) {
                throw new CanceledException(new RuntimeException("Search thread was interrupted"));
            }

            // Lade das nächste Dataset
            iPartsSearchVirtualDataset dataset = datasets.get(datasetIterator);
            dataset.create();

            // Jetzt gehen wir wieder in die while-Schleife, damit von dem neuen Dataset das next() gerufen wird.
            // Wir dürfen es hier nicht machen, weil das Dataset ja auch komplett leer sein kann.
            // Falls es leer ist, dann kommt hier einfach das nächste dran.
        }

        // Passt, aktuelles Dataset ist noch nicht am Ende
        return true;
    }

    @Override
    public void close() {
        for (iPartsSearchVirtualDataset dataset : datasets) {
            dataset.close();
        }
        datasets.clear();
        datasetIterator = -1;
    }
}
