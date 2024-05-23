/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPool;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PoolId;
import de.docware.apps.etk.base.webservice.endpoints.WSAbstractEndpoint;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPartlistTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPAS;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductSAs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataTopTUsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPas;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuForProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.*;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Hilfsklasse für die Navigation im iParts Katalog über Webservices wie z.B. {@link de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getnavopts.iPartsWSGetNavOptsEndpoint}.
 */
public class iPartsWSNavHelper {

    /**
     * Überprüft, ob der übergebene <i>navContext</i> gültig ist, wobei er auch fehlen oder leer sein darf, wenn <i>isOptional</i>
     * gesetzt ist.
     *
     * @param navContext
     * @param isOptional
     * @param path
     */
    public static void checkIfNavContextValid(List<? extends iPartsWSNavNode> navContext, boolean isOptional, String path) {
        if (isOptional && ((navContext == null) || (navContext.isEmpty()))) {
            return;
        }

        WSRequestTransferObject.checkAttribValid(path, "navContext", navContext);
        int i = 0;
        for (iPartsWSNavNode navNode : navContext) {
            WSRequestTransferObject.checkAttribValid(path, "navContext[" + i + "]", navNode);
            i++;
        }
    }

    /**
     * Liefert die Kind-{@link iPartsWSNavNode}s für das übergebene Produkt, zusätzlichen Filter-Schlüssel für den Stücklisten-
     * Cache und Navigations-Kontext zurück.
     *
     * @param project
     * @param product
     * @param productStructures
     * @param modelNumbers
     * @param additionalFilterCacheKey
     * @param identContext
     * @param navContext
     * @param endpoint
     * @param includeNotes             Sollen Notizen zu den Teilen hinzugefügt werden?
     * @param includeImages            Sollen Zeichnungen (Thumbnails) zu den Knoten hinzugefügt werden?
     * @param disableAllFilters        Soll der Filter am Ende deaktiviert werden?
     * @param withExtendedDescriptions Sollen Text-Ids zu den Labels ausgegeben werden?
     * @param reducedInformation       Sollen die Informationen auf ein Minimum reduziert werden?
     * @return
     */
    public static List<iPartsWSNavNode> getChildNavNodes(EtkProject project, iPartsProduct product, iPartsProductStructures productStructures,
                                                         Set<String> modelNumbers, String additionalFilterCacheKey,
                                                         iPartsWSIdentContext identContext, List<? extends iPartsWSNavNode> navContext,
                                                         iPartsWSAbstractEndpoint endpoint, boolean includeNotes, boolean includeImages,
                                                         boolean disableAllFilters, boolean withExtendedDescriptions,
                                                         boolean reducedInformation, Set<String> specialUserPermissions) {
        List<iPartsWSNavNode> nextNodes = null;
        String language = project.getDBLanguage();
        try {
            // ohne navContext Liste aller KGs bzw. EinPAS Hauptgruppen des Produkts aus dem identContext zurückliefern
            if ((navContext == null) || navContext.isEmpty()) {
                nextNodes = iPartsWSNavHelper.getProductChildNavNodes(product, productStructures, modelNumbers, additionalFilterCacheKey,
                                                                      identContext, project, language, endpoint, withExtendedDescriptions,
                                                                      reducedInformation, specialUserPermissions);
            } else { // Unterscheidung anhand des ersten NavNodes
                iPartsWSNavNode.TYPE nodeType = navContext.get(0).getTypeAsEnum();
                switch (nodeType) {
                    case cg_group:
                        nextNodes = iPartsWSNavHelper.getKgTuChildNavNodes(product, productStructures, modelNumbers,
                                                                           navContext, project, language, true,
                                                                           includeImages, product.isSpecialCatalog(),
                                                                           additionalFilterCacheKey, identContext, endpoint,
                                                                           withExtendedDescriptions, reducedInformation,
                                                                           specialUserPermissions);
                        break;
                    case maingroup:
                        nextNodes = iPartsWSNavHelper.getEinPasChildNavNodes(product, productStructures, navContext, identContext, project,
                                                                             language, true, includeImages, withExtendedDescriptions,
                                                                             reducedInformation, specialUserPermissions);
                        break;
                    default:
                        WSAbstractEndpoint.throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid type of first node in attribute 'navContext': "
                                                                                                 + nodeType.name(), null);
                }
            }
        } finally {
            if (disableAllFilters) {
                // Filter wieder zurücksetzen, damit die Session wieder ohne Filter weiterverwendet werden kann (z.B. für die Unittests)
                iPartsFilter.disableAllFilters();
            }
        }

        if (nextNodes == null) {
            nextNodes = new DwList<>(0);
        } else if (includeNotes && !reducedInformation) {
            // Notizen zu den jeweiligen NavNodes anhängen
            List<String> dataBaseFallbackLanguages = project.getDataBaseFallbackLanguages();
            for (iPartsWSNavNode nextNode : nextNodes) {
                String virtualIdString;
                if (!nextNode.getTypeAsEnum().equals(iPartsWSNavNode.TYPE.module)) {
                    List<iPartsWSNavNode> completeNavPath = new DwList<>();
                    if (navContext != null) {
                        completeNavPath.addAll(navContext);
                    }
                    completeNavPath.add(nextNode);
                    virtualIdString = iPartsWSNavHelper.getVirtualIdString(product, completeNavPath, project);
                } else {
                    virtualIdString = nextNode.getId(); // Modulnummer direkt verwenden (kein NavPath notwendig)
                }

                List<iPartsNote> notes = iPartsNote.getNotes(new iPartsPartId(virtualIdString, ""), project);
                nextNode.setNotes(iPartsWSNote.convertToWSNotes(notes, language, dataBaseFallbackLanguages));
            }
        }
        return nextNodes;
    }

    public static List<iPartsWSNavNode> getChildNavNodes(EtkProject project, iPartsProduct product, iPartsProductStructures productStructures,
                                                         String additionalFilterCacheKey, iPartsWSIdentContext identContext,
                                                         List<? extends iPartsWSNavNode> navContext,
                                                         iPartsWSAbstractEndpoint endpoint, boolean includeNotes, boolean includeImages,
                                                         boolean disableAllFilters, boolean reducedInformation,
                                                         Set<String> specialUserPermissions) {
        return getChildNavNodes(project, product, productStructures, null, additionalFilterCacheKey, identContext, navContext,
                                endpoint, includeNotes, includeImages, disableAllFilters, false,
                                reducedInformation, specialUserPermissions);
    }

    public static List<iPartsWSNavNode> getProductChildNavNodes(iPartsProduct product, iPartsProductStructures productStructures,
                                                                Set<String> modelNumbers, String additionalFilterCacheKey,
                                                                iPartsWSIdentContext identContext, EtkProject project,
                                                                String language, iPartsWSAbstractEndpoint endpoint,
                                                                boolean withExtendedDescriptions, boolean reducedInformation, Set<String> specialUserPermissions) {
        Collection<iPartsCatalogNode> einPasNodes = productStructures.getCompleteEinPasStructure(project, product.isStructureWithAggregates()).getChildren();
        Collection<iPartsCatalogNode> kgTuNodes = productStructures.getKgTuStructureWithoutCarPerspective(project, product.isStructureWithAggregates()).getChildren();
        List<iPartsWSNavNode> nextNodes = new ArrayList<>(einPasNodes.size() + kgTuNodes.size());

        if (!einPasNodes.isEmpty()) {
            // EinPAS HGs als NavNodes zu den nextNodes hinzufügen
            EinPas einPas = EinPas.getInstance(project);
            for (iPartsCatalogNode einPasNode : einPasNodes) {
                if (einPasNode.getId() instanceof EinPasId) {
                    iPartsWSNavNode navNode = new iPartsWSNavNode();
                    navNode.setType(iPartsWSNavNode.TYPE.maingroup);

                    EinPasId einPasId = (EinPasId)einPasNode.getId();
                    String hg = einPasId.getHg();
                    navNode.setId(hg);

                    if (!reducedInformation) {
                        EinPasNode hgNode = einPas.getHGNode(hg);
                        addAdditionalDataToNavNode(navNode, hgNode, einPasNode, project, language, false, withExtendedDescriptions);
                    }
                    nextNodes.add(navNode);
                }
            }
        }

        if (!kgTuNodes.isEmpty()) {
            iPartsFilter filter = iPartsFilter.get();

            // KG/TU KGs als NavNodes zu den nextNodes hinzufügen
            boolean isStructureWithAggregates = product.isStructureWithAggregates();
            for (iPartsCatalogNode kgTuNode : kgTuNodes) {
                if (kgTuNode.getId() instanceof KgTuId) {
                    KgTuId kgTuId = (KgTuId)kgTuNode.getId();
                    if (!isStructureWithAggregates && !iPartsFilterHelper.isKgTuNodeVisible(product.getAsId(), modelNumbers,
                                                                                            kgTuId, kgTuNode, filter, project)) {
                        continue;
                    }

                    iPartsWSNavNode navNode = new iPartsWSNavNode();
                    navNode.setType(iPartsWSNavNode.TYPE.cg_group);

                    String kg = kgTuId.getKg();
                    navNode.setId(kg);

                    boolean isSpecialCatalog = product.isSpecialCatalog();
                    if (!reducedInformation) {
                        KgTuNode kgNode = productStructures.getKgTuNode(project, kgTuId);
                        addAdditionalDataToNavNode(navNode, kgNode, kgTuNode, project, language, isSpecialCatalog,
                                                   withExtendedDescriptions);
                    }
                    // Wenn der Knoten (rekursiv) bei Spezialkatalogen nur ein Submodul hat, dann partsAvailable auf true
                    // setzen, wenn das resultierende Modul mindestens einen Stücklisteneintrag hat (ansonsten den Knoten
                    // gar nicht zu nextNodes hinzufügen)
                    boolean addNode = true;
                    if (isSpecialCatalog) {
                        iPartsCatalogNode catalogNode = kgTuNode;
                        while (catalogNode.getChildren().size() == 1) {
                            catalogNode = catalogNode.getChildren().iterator().next();
                        }
                        if (catalogNode.getId() instanceof AssemblyId) {
                            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, (AssemblyId)catalogNode.getId());
                            // Check, ob das Modul nur mit Spezial-Rechte gesehen werden darf
                            if ((assembly instanceof iPartsDataAssembly) && !checkSpecialPermissions((iPartsDataAssembly)assembly, specialUserPermissions)) {
                                addNode = false;
                            } else {
                                Set<String> filteredPartListSequenceNumbers = iPartsWSFilteredPartListsCache.getFilteredPartListSequenceNumbers(assembly,
                                                                                                                                                iPartsPartlistTypes.PART_LIST_TYPE_FOR_GET_PARTS,
                                                                                                                                                product, additionalFilterCacheKey, identContext,
                                                                                                                                                endpoint);
                                if (!filteredPartListSequenceNumbers.isEmpty()) {
                                    navNode.setPartsAvailable(true);
                                } else {
                                    addNode = false;
                                }
                            }
                        }
                    }

                    if (addNode) {
                        nextNodes.add(navNode);
                    }
                }
            }
        }
        return nextNodes;
    }

    public static List<iPartsWSNavNode> getKgTuChildNavNodes(iPartsProduct product, iPartsProductStructures productStructures,
                                                             Set<String> modelNumbers, List<? extends iPartsWSNavNode> navNodes,
                                                             EtkProject project, String language, boolean includeModuleTitle,
                                                             boolean includeImages, boolean isSpecialCatalog, String additionalFilterCacheKey,
                                                             iPartsWSIdentContext identContext, iPartsWSAbstractEndpoint endpoint,
                                                             boolean withExtendedDescriptions, boolean reducedInformation,
                                                             Set<String> specialUserPermissions) {
        iPartsCatalogNode productNode = productStructures.getKgTuStructureWithoutCarPerspective(project, product.isStructureWithAggregates());
        List<iPartsWSNavNode> nextNodes = null;

        iPartsWSNavNode kgNode = navNodes.get(0);
        iPartsWSNavNode.TYPE nodeType = kgNode.getTypeAsEnum();
        if (nodeType != iPartsWSNavNode.TYPE.cg_group) {
            WSAbstractEndpoint.throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid type for KG in attribute 'navContext' for KG/TU: "
                                                                                     + nodeType.name(), null);
        }
        String kg = kgNode.getId();

        iPartsFilter filter = iPartsFilter.get();
        if (navNodes.size() == 1) { // KG -> TU-Knoten
            iPartsCatalogNode kgCatalogNode = productNode.getNode(new KgTuId(kg, ""));
            if ((kgCatalogNode != null) && !kgCatalogNode.getChildren().isEmpty()) {
                // KG/TU TUs als NavNodes zu den nextNodes hinzufügen
                Collection<iPartsCatalogNode> tuCatalogNodes = kgCatalogNode.getChildren();
                nextNodes = new DwList<>(tuCatalogNodes.size());

                // Gibt es nur genau einen TU-Kindknoten und sollen einzelne Unterbaugruppen übersprungen werden?
                if (isSpecialCatalog && (tuCatalogNodes.size() == 1)) {
                    iPartsCatalogNode tuCatalogNode = tuCatalogNodes.iterator().next();
                    if (tuCatalogNode.getId() instanceof KgTuId) {
                        // Module vom KG/TU TU als NavNodes zu den nextNodes hinzufügen
                        return getModuleNavNodes(tuCatalogNode, project, language, includeModuleTitle, includeImages,
                                                 isSpecialCatalog, product, additionalFilterCacheKey, identContext, endpoint,
                                                 filter, withExtendedDescriptions, reducedInformation, specialUserPermissions, false);
                    }
                }

                boolean isStructureWithAggregates = product.isStructureWithAggregates();
                boolean isHideEmptyTUs = iPartsWebservicePlugin.getPluginConfig().
                        getConfigValueAsBoolean(iPartsWebservicePlugin.CONFIG_HIDE_EMPTY_TUS_IN_RESPONSE);
                for (iPartsCatalogNode tuCatalogNode : tuCatalogNodes) {
                    if (tuCatalogNode.getId() instanceof KgTuId) { // KG/TU
                        KgTuId kgTuId = (KgTuId)tuCatalogNode.getId();
                        if ((!isStructureWithAggregates && !iPartsFilterHelper.isKgTuNodeVisible(product.getAsId(), modelNumbers,
                                                                                                 kgTuId, tuCatalogNode, filter,
                                                                                                 project))
                            || (isHideEmptyTUs && iPartsFilterHelper.isCatalogNodeEmptyWithFilter(tuCatalogNode, project))) {
                            continue;
                        }
                        iPartsWSNavNode navNode = new iPartsWSNavNode();
                        navNode.setType(iPartsWSNavNode.TYPE.cg_subgroup);
                        navNode.setId(kgTuId.getTu());

                        if (!reducedInformation) {
                            KgTuNode tuNode = productStructures.getKgTuNode(project, kgTuId);
                            addAdditionalDataToNavNode(navNode, tuNode, tuCatalogNode, project, language, includeImages,
                                                       withExtendedDescriptions);
                        }

                        // Sichtbare Module bestimmen
                        AssemblyId singleModuleAssemblyId = null;
                        int visibleModuleCounter = 0;
                        for (iPartsCatalogNode moduleCatalogNode : tuCatalogNode.getChildren()) {
                            if (moduleCatalogNode.getId() instanceof AssemblyId) {
                                AssemblyId assemblyId = (AssemblyId)moduleCatalogNode.getId();
                                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);

                                // Ist das Modul ausgeblendet?
                                if (!isModuleVisible(assembly, filter, isStructureWithAggregates, specialUserPermissions)) {
                                    continue;
                                }

                                // Modul ist sichtbar
                                singleModuleAssemblyId = assemblyId;
                                visibleModuleCounter++;
                            }
                        }

                        if (visibleModuleCounter > 0) {
                            navNode.setPartsAvailable(visibleModuleCounter == 1); // nur bei 1 Modul hängt direkt die Stückliste darunter

                            // Wenn es prinzipiell mehr als ein Modul im TU gibt, aber nach Filterung nur ein Modul übrig
                            // bleibt, dann trotzdem die Thumbnails von diesem Modul am TU-Knoten ausgeben
                            if (includeImages && (singleModuleAssemblyId != null) && (visibleModuleCounter == 1) && (tuCatalogNode.getChildren().size() > 1)) {
                                List<iPartsWSImage> partListImages = getPartListImages(project, singleModuleAssemblyId, language);
                                if (!partListImages.isEmpty()) {
                                    navNode.setThumbNails(partListImages);
                                }
                            }

                            if (visibleModuleCounter > 1) { // Mehrere Module im TU -> Module direkt im TU ausgeben
                                List<iPartsWSNavNode> moduleNavNodes = getModuleNavNodes(tuCatalogNode, project, language,
                                                                                         includeModuleTitle, includeImages,
                                                                                         isSpecialCatalog, product, additionalFilterCacheKey,
                                                                                         identContext, endpoint, filter,
                                                                                         withExtendedDescriptions, reducedInformation,
                                                                                         specialUserPermissions, true);
                                navNode.setModules(moduleNavNodes);
                            }
                            nextNodes.add(navNode);
                        }
                    } else if (tuCatalogNode.getId() instanceof KgSaId) { // KG/SA
                        KgSaId kgSaId = (KgSaId)tuCatalogNode.getId();
                        String saNumber = kgSaId.getSa();
                        if (!filter.isSaVisible(project, saNumber, product.getAsId())) { // Freie SA sichtbar mit Filterung?
                            continue;
                        }

                        // Ist das SA-Modul ausgeblendet?
                        iPartsSA sa = iPartsSA.getInstance(project, new iPartsSAId(saNumber));
                        AssemblyId assemblyId = sa.getModuleId();
                        if (assemblyId != null) {
                            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
                            if ((assembly instanceof iPartsDataAssembly) && !filter.isModuleVisible((iPartsDataAssembly)assembly)) {
                                continue;
                            }
                        }

                        iPartsWSNavNode navNode = new iPartsWSNavNode();
                        navNode.setType(iPartsWSNavNode.TYPE.sa_number);

                        navNode.setId(iPartsNumberHelper.formatPartNo(project, saNumber, language));

                        if (!reducedInformation) {
                            addAdditionalDataToNavNode(navNode, null, tuCatalogNode, project, language, includeImages,
                                                       withExtendedDescriptions);
                            navNode.setLabel(project, sa.getTitle(project), withExtendedDescriptions);

                            // SA-Codes
                            String codes = sa.getCodes(project);
                            if (!codes.isEmpty()) {
                                navNode.setSaCodes(codes);
                            }

                            // SAAs inkl. deren Zusatzinformationen
                            List<iPartsWSSaaNavNodeInfo> saaNavNodeInfoList = new DwList<>();
                            Map<String, List<iPartsFootNote>> saaFootNotesMap = sa.getSaaFootNotesMap(project);
                            Map<String, Set<String>> saaReferencedSAsMap = sa.getSaaReferencedSAsMap(project);
                            Set<String> saas;

                            // SAAs der Aggregate-Datenkarten dazumischen falls Aggregate-Zumischung aktiv ist
                            // Das Ergebnis von getAllSelectedSubDatacards() enthält auch die Fahrzeugdatenkarte selbst
                            if (product.isStructureWithAggregates()) {
                                saas = new TreeSet<>();
                                for (AbstractDataCard aggregateDataCard : filter.getCurrentDataCard().getAllSelectedSubDatacards(project)) {
                                    saas.addAll(aggregateDataCard.getSaas().getAllCheckedValues());
                                }
                            } else { // Nur die SAAs der aktuellen Datenkarte berücksichtigen
                                saas = filter.getCurrentDataCard().getSaas().getAllCheckedValues();
                            }

                            for (String saaNumber : saas) {
                                if (saaNumber.startsWith(saNumber)) { // Es ist eine SAA der SA
                                    String formattedSaaNumber = iPartsNumberHelper.formatPartNo(project, saaNumber, language);
                                    String saaDescription = iPartsSA.getSaSaaDescription(project, saaNumber, language);
                                    iPartsWSSaaNavNodeInfo saaNavNodeInfo = new iPartsWSSaaNavNodeInfo(formattedSaaNumber, saaDescription);

                                    // Fußnoten
                                    if (saaFootNotesMap != null) {
                                        List<iPartsFootNote> footNotes = saaFootNotesMap.get(saaNumber);
                                        if (footNotes != null) {
                                            List<iPartsWSFootNote> wsFootNotes = new DwList<>(footNotes.size());
                                            for (iPartsFootNote footNote : footNotes) {
                                                iPartsWSFootNote wsFootNote = new iPartsWSFootNote(project, footNote);
                                                wsFootNotes.add(wsFootNote);
                                            }
                                            saaNavNodeInfo.setFootnotes(wsFootNotes);
                                        }
                                    }

                                    // Verbindungs-SAs
                                    if (saaReferencedSAsMap != null) {
                                        Set<String> referencedSAs = saaReferencedSAsMap.get(saaNumber);
                                        if (referencedSAs != null) {
                                            List<iPartsWSSaCode> wsReferencedSAs = new DwList<>(referencedSAs.size());
                                            for (String referencedSA : referencedSAs) {
                                                String formattedReferencedSaNumber = iPartsNumberHelper.formatPartNo(project, referencedSA, language);
                                                String referencedsaDescription = iPartsSA.getSaSaaDescription(project, saaNumber, language);
                                                iPartsWSSaCode wsSaCode = new iPartsWSSaCode(formattedReferencedSaNumber, referencedsaDescription, null);
                                                wsReferencedSAs.add(wsSaCode);
                                            }
                                            saaNavNodeInfo.setReferencedSAs(wsReferencedSAs);
                                        }
                                    }

                                    saaNavNodeInfoList.add(saaNavNodeInfo);
                                }
                            }
                            if (!saaNavNodeInfoList.isEmpty()) {
                                navNode.setSaaNavNodeInfos(saaNavNodeInfoList);
                            }
                        }
                        navNode.setPartsAvailable(tuCatalogNode.getChildren().size() <= 1); // nur bei maximal 1 Modul hängt direkt die Stückliste darunter
                        nextNodes.add(navNode);
                    }
                }
            }
        } else if (navNodes.size() == 2) { // TU/SA -> Modul-Knoten
            iPartsWSNavNode tuNode = navNodes.get(1);
            nodeType = tuNode.getTypeAsEnum();
            if (nodeType == iPartsWSNavNode.TYPE.cg_subgroup) { // KG/TU
                String tu = tuNode.getId();
                iPartsCatalogNode tuCatalogNode = productNode.getNode(new KgTuId(kg, tu));

                // Module vom KG/TU TU als NavNodes zu den nextNodes hinzufügen
                nextNodes = getModuleNavNodes(tuCatalogNode, project, language, includeModuleTitle, includeImages,
                                              isSpecialCatalog, product, additionalFilterCacheKey, identContext, endpoint,
                                              filter, withExtendedDescriptions, reducedInformation, specialUserPermissions, false);
            } else if (nodeType == iPartsWSNavNode.TYPE.sa_number) { // KG/SA
                String sa = null;
                try {
                    sa = new iPartsNumberHelper().unformatSaForDB(tuNode.getId());
                } catch (Exception e) {
                    WSAbstractEndpoint.throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid SA number for SA in attribute 'navContext': "
                                                                                             + tuNode.getId(), null);
                }
                if (filter.isSaVisible(project, sa, product.getAsId())) { // Freie SA sichtbar mit Filterung?
                    iPartsCatalogNode saCatalogNode = productNode.getNode(new KgSaId(kg, sa));

                    // Module vom KG/SA SA als NavNodes zu den nextNodes hinzufügen
                    nextNodes = getModuleNavNodes(saCatalogNode, project, language, includeModuleTitle, includeImages,
                                                  isSpecialCatalog, product, additionalFilterCacheKey, identContext, endpoint,
                                                  filter, withExtendedDescriptions, reducedInformation, specialUserPermissions, false);
                }
            } else {
                WSAbstractEndpoint.throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid type for TU in attribute 'navContext' for KG/TU: "
                                                                                         + nodeType.name(), null);
            }
        } else {
            // KG/TU kann höchstens zwei NavNodes enthalten
            WSAbstractEndpoint.throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid length of attribute 'navContext' for KG/TU: "
                                                                                     + getErrorStringForNavNodes(navNodes), null);
        }

        return nextNodes;
    }

    private static boolean isModuleVisible(EtkDataAssembly assembly, iPartsFilter filter, boolean isStructureWithAggregates,
                                           Set<String> specialUserPermissions) {
        if (assembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
            if (!filter.isModuleVisible(iPartsAssembly)) {
                return false;
            }

            if (!checkSpecialPermissions(iPartsAssembly, specialUserPermissions)) {
                return false;
            }

            if (isStructureWithAggregates) {
                Set<String> modelNumbersForAssembly = filter.getRelevantModelNumbers(iPartsAssembly);

                // Wenn ein Modul für kein relevantes Baumuster der Datenkarte gültig ist, dann
                // ist das gesamte Modul für die Datenkarte nicht gültig
                if (modelNumbersForAssembly.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check, ob am Modul ein Spezial-Recht eingetragen ist und ob das mit den Rechten der Anfrage übereinstimmt
     *
     * @param iPartsAssembly
     * @param specialUserPermissions
     * @return
     */
    public static boolean checkSpecialPermissions(iPartsDataAssembly iPartsAssembly, Set<String> specialUserPermissions) {
        // Wert am Modul
        String dbValue = iPartsAssembly.getModuleMetaData().getSpecialTUDbValue();
        if (StrUtils.isValid(dbValue)) {
            // Wert ist vorhanden aber die Anfrage hat keine Spezial-Rechte -> nicht anzeigen
            if ((specialUserPermissions == null) || specialUserPermissions.isEmpty()) {
                return false;
            }
            // Wert ist vorhanden aber die Anfrage hat nicht das passende Spezial-Recht -> nicht anzeigen
            if (!specialUserPermissions.contains(dbValue)) {
                return false;
            }
        }
        return true;
    }

    public static List<iPartsWSNavNode> getEinPasChildNavNodes(iPartsProduct product, iPartsProductStructures productStructures,
                                                               List<? extends iPartsWSNavNode> navNodes, iPartsWSIdentContext identContext,
                                                               EtkProject project, String language, boolean includeModuleTitle, boolean includeImages,
                                                               boolean withExtendedDescriptions, boolean reducedInformation,
                                                               Set<String> specialUserPermissions) {
        iPartsCatalogNode productNode = productStructures.getCompleteEinPasStructure(project, product.isStructureWithAggregates());
        List<iPartsWSNavNode> nextNodes = null;

        iPartsWSNavNode einPasNode = navNodes.get(0);
        iPartsWSNavNode.TYPE nodeType = einPasNode.getTypeAsEnum();
        if (nodeType != iPartsWSNavNode.TYPE.maingroup) {
            WSAbstractEndpoint.throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid type for HG in attribute 'navContext' for EinPAS: "
                                                                                     + nodeType.name(), null);
        }
        String hg = einPasNode.getId();

        if (navNodes.size() == 1) { // HG -> G-Knoten
            iPartsCatalogNode hgCatalogNode = productNode.getNode(new EinPasId(hg, "", ""));
            if ((hgCatalogNode != null) && !hgCatalogNode.getChildren().isEmpty()) {
                // EinPAS Gs als NavNodes zu den nextNodes hinzufügen
                EinPas einPas = EinPas.getInstance(project);
                Collection<iPartsCatalogNode> gCatalogNodes = hgCatalogNode.getChildren();
                nextNodes = new DwList<>(gCatalogNodes.size());
                for (iPartsCatalogNode gCatalogNode : gCatalogNodes) {
                    if (gCatalogNode.getId() instanceof EinPasId) {
                        iPartsWSNavNode navNode = new iPartsWSNavNode();
                        navNode.setType(iPartsWSNavNode.TYPE.group);

                        EinPasId einPasId = (EinPasId)gCatalogNode.getId();
                        navNode.setId(einPasId.getG());
                        if (!reducedInformation) {
                            EinPasNode gNode = einPas.getNode(einPasId);
                            addAdditionalDataToNavNode(navNode, gNode, gCatalogNode, project, language, false,
                                                       withExtendedDescriptions);
                        }
                        nextNodes.add(navNode);
                    }
                }
            }
        } else { // G oder TU
            iPartsWSNavNode gNode = navNodes.get(1); // G-Knoten sowohl für G als auch TU bestimmen
            nodeType = gNode.getTypeAsEnum();
            if (nodeType != iPartsWSNavNode.TYPE.group) {
                WSAbstractEndpoint.throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid type for G in attribute 'navContext' for EinPAS: "
                                                                                         + nodeType.name(), null);
            }
            String g = gNode.getId();
            if (navNodes.size() == 2) { // G -> TU-Knoten
                iPartsCatalogNode gCatalogNode = productNode.getNode(new EinPasId(hg, g, ""));
                if ((gCatalogNode != null) && !gCatalogNode.getChildren().isEmpty()) {
                    // EinPAS TUs als NavNodes zu den nextNodes hinzufügen
                    EinPas einPas = EinPas.getInstance(project);
                    Collection<iPartsCatalogNode> tuCatalogNodes = gCatalogNode.getChildren();
                    nextNodes = new DwList<>(tuCatalogNodes.size());
                    for (iPartsCatalogNode tuCatalogNode : tuCatalogNodes) {
                        if (tuCatalogNode.getId() instanceof EinPasId) {
                            iPartsWSNavNode navNode = new iPartsWSNavNode();
                            navNode.setType(iPartsWSNavNode.TYPE.subgroup);

                            EinPasId einPasId = (EinPasId)tuCatalogNode.getId();
                            navNode.setId(einPasId.getTu());
                            if (!reducedInformation) {
                                EinPasNode tuNode = einPas.getNode(einPasId);
                                addAdditionalDataToNavNode(navNode, tuNode, tuCatalogNode, project, language, includeImages,
                                                           withExtendedDescriptions);
                            }
                            navNode.setPartsAvailable(tuCatalogNode.getChildren().size() <= 1); // nur bei maximal 1 Modul hängt direkt die Stückliste darunter
                            nextNodes.add(navNode);
                        }
                    }
                }
            } else if (navNodes.size() == 3) { // TU -> Modul-Knoten
                iPartsWSNavNode tuNode = navNodes.get(2);
                nodeType = tuNode.getTypeAsEnum();
                if (nodeType != iPartsWSNavNode.TYPE.subgroup) {
                    WSAbstractEndpoint.throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid type for TU in attribute 'navContext' for EinPAS: "
                                                                                             + nodeType.name(), null);
                }
                String tu = tuNode.getId();
                iPartsCatalogNode tuCatalogNode = productNode.getNode(new EinPasId(hg, g, tu));

                // Module vom EinPAS TU als NavNodes zu den nextNodes hinzufügen
                nextNodes = getModuleNavNodes(tuCatalogNode, project, language, includeModuleTitle, includeImages, false,
                                              product, null, identContext, null, iPartsFilter.get(),
                                              withExtendedDescriptions, reducedInformation, specialUserPermissions, false);
            } else {
                // EinPAS kann höchstens drei NavNodes enthalten
                WSAbstractEndpoint.throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid length of attribute 'navContext' for EinPAS: "
                                                                                         + getErrorStringForNavNodes(navNodes), null);
            }
        }

        return nextNodes;
    }

    public static List<iPartsWSNavNode> getModuleNavNodes(iPartsCatalogNode tuCatalogNode, EtkProject project, String language,
                                                          boolean includeModuleTitle, boolean includeImages, boolean isSpecialCatalog,
                                                          iPartsProduct product, String additionalFilterCacheKey,
                                                          iPartsWSIdentContext identContext, iPartsWSAbstractEndpoint endpoint,
                                                          iPartsFilter filter, boolean withExtendedDescriptions,
                                                          boolean reducedInformation, Set<String> specialUserPermissions,
                                                          boolean setModelId) {
        List<iPartsWSNavNode> nextNodes = null;
        if ((tuCatalogNode != null) && !tuCatalogNode.getChildren().isEmpty()) {
            Collection<iPartsCatalogNode> moduleCatalogNodes = tuCatalogNode.getChildren();
            nextNodes = new DwList<>(moduleCatalogNodes.size());
            boolean isStructureWithAggregates = product.isStructureWithAggregates();
            for (iPartsCatalogNode moduleCatalogNode : moduleCatalogNodes) {
                if (moduleCatalogNode.getId() instanceof AssemblyId) {
                    AssemblyId assemblyId = (AssemblyId)moduleCatalogNode.getId();
                    EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);

                    // Ist das Modul ausgeblendet?
                    if (!isModuleVisible(assembly, filter, isStructureWithAggregates, specialUserPermissions)) {
                        continue;
                    }

                    // Bei Spezialkatalogen das Modul nur dann hinzufügen, wenn es mindestens einen Stücklisteneintrag hat
                    if (isSpecialCatalog) {
                        Set<String> filteredPartListSequenceNumbers = iPartsWSFilteredPartListsCache.getFilteredPartListSequenceNumbers(assembly,
                                                                                                                                        iPartsPartlistTypes.PART_LIST_TYPE_FOR_GET_PARTS,
                                                                                                                                        product, additionalFilterCacheKey, identContext,
                                                                                                                                        endpoint);
                        if (filteredPartListSequenceNumbers.isEmpty()) {
                            continue;
                        }
                    }

                    iPartsWSNavNode navNode = new iPartsWSNavNode();
                    navNode.setType(iPartsWSNavNode.TYPE.module);
                    navNode.setId(assemblyId.getKVari());
                    if (!reducedInformation) {
                        if (includeModuleTitle) {
                            navNode.setLabel(project, iPartsDataAssembly.getAssemblyText(project, assemblyId), withExtendedDescriptions);
                        } else {
                            navNode.setLabel(assemblyId.getKVari());
                        }
                        if (includeImages) {
                            List<iPartsWSImage> partListImages = getPartListImages(project, assemblyId, language);
                            if (!partListImages.isEmpty()) {
                                navNode.setThumbNails(partListImages);
                            }
                        }

                        if (setModelId && (filter != null) && (assembly instanceof iPartsDataAssembly)) {
                            Set<String> relevantModelNumbers = filter.getRelevantModelNumbers((iPartsDataAssembly)assembly);
                            if (Utils.isValid(relevantModelNumbers)) {
                                navNode.setModelId(StrUtils.stringListToString(relevantModelNumbers, ", "));
                            }
                        }
                    }
                    navNode.setPartsAvailable(true);
                    nextNodes.add(navNode);
                }
            }
        }
        return nextNodes;
    }

    public static void addAdditionalDataToNavNode(iPartsWSNavNode navNode, AbstractiPartsNode iPartsNode, iPartsCatalogNode catalogNode,
                                                  EtkProject project, String language, boolean hideSingleSubAssemblyNodes,
                                                  boolean withExtendedDescriptions) {
        if (iPartsNode != null) {
            navNode.setLabel(project, iPartsNode.getTitle(), withExtendedDescriptions);
        } else {
            navNode.setLabel("");
        }

        if (hideSingleSubAssemblyNodes) {
            // Wenn der Knoten (rekursiv) nur ein Submodul hat, dann die Thumbnails zuordnen
            while (catalogNode.getChildren().size() == 1) {
                catalogNode = catalogNode.getChildren().iterator().next();
                IdWithType childId = catalogNode.getId();
                if (childId instanceof AssemblyId) {
                    List<iPartsWSImage> partListImages = getPartListImages(project, (AssemblyId)childId, language);
                    if (!partListImages.isEmpty()) {
                        navNode.setThumbNails(partListImages);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Ermittelt für eine Assembly die Liste der zugeordneten Zeichnungen inkl. Thumbnails und liefert sie zurück.
     *
     * @param project
     * @param assemblyId
     * @return
     */
    public static List<iPartsWSImage> getPartListImages(EtkProject project, AssemblyId assemblyId, String language) {
        // Die Assembly laden ...
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
        List<EtkDataImage> dataImages = assembly.getImages();
        List<iPartsWSImage> images = new DwList<>(dataImages.size());

        Boolean useSVGs = null;
        // Für alle Zeichnungen und darin für alle Verwendungen/Varianten ein iPartsWSImage-Objekt erzeugen und dem Ergebnis hinzufügen
        for (EtkDataImage dataImage : dataImages) {
            for (EtkDataPool imageVariant : dataImage.getFilteredPoolVariants()) {
                PoolId imagePoolId = imageVariant.getAsId();

                // Sollen SVGs ausgegeben werden?
                if (imagePoolId.getPUsage().equals(EtkDataImage.IMAGE_USAGE_SVG)) {
                    if (useSVGs == null) { // useSVGs lazy bestimmen
                        useSVGs = iPartsWSImageHelper.checkIfUseSVGImages(assembly, project);
                    }

                    if (!useSVGs) {
                        continue;
                    }
                }
                iPartsWSImage wsImage = new iPartsWSImage(imagePoolId, dataImage, language, project); // wsImage mit imagePoolId initialisieren inkl. URLs
                images.add(wsImage);
            }
        }
        return images;
    }

    public static List<iPartsWSImage> createNavNodeThumbnails(String pictureName) {
        if (!pictureName.isEmpty()) {
            iPartsWSImage thumbnail = new iPartsWSImage(pictureName); // wsImage mit pictureName initialisieren inkl. URLs
            List<iPartsWSImage> thumbnails = new DwList<>(1);
            thumbnails.add(thumbnail);
            return thumbnails;
        } else {
            return null;
        }
    }

    public static String getErrorStringForNavNodes(Collection<? extends iPartsWSNavNode> navNodes) {
        String errorString = "";
        for (iPartsWSNavNode navNode : navNodes) {
            if (!errorString.isEmpty()) {
                errorString += "/";
            }
            errorString += navNode.getId();
        }
        return errorString;
    }


    /**
     * k_vari für Assembly ID bestimmen
     * Namen analog zu entsprechenden Methoden in {@link iPartsVirtualNode}
     *
     * @param product
     * @param navContext
     * @param project
     * @return
     */
    public static String getVirtualIdString(iPartsProduct product, List<? extends iPartsWSNavNode> navContext, EtkProject project) {
        iPartsNodeType navType = null;
        iPartsNodeType productType = null;
        if (product.getProductStructuringType() == iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU) {
            productType = iPartsNodeType.getProductKgTuType(product, project);

            // Erkennen, ob es sich um einen KG/SA-NavContext handelt
            boolean isKgSa = false;
            if ((navContext != null) && (navContext.size() >= 2)) { // SA steht an zweiter Stelle
                isKgSa = navContext.get(1).getTypeAsEnum() == iPartsWSNavNode.TYPE.sa_number;
            }
            if (isKgSa) {
                navType = iPartsNodeType.KGSA;
            } else {
                navType = iPartsNodeType.KGTU;
            }
        } else if (product.getProductStructuringType() == iPartsConst.PRODUCT_STRUCTURING_TYPE.EINPAS) {
            productType = iPartsNodeType.getProductEinPASType(product, project);
            navType = iPartsNodeType.EINPAS;
        } else {
            WSAbstractEndpoint.throwProcessingError(WSError.INTERNAL_ERROR, "Unexpected productStructuringType: "
                                                                            + product.getProductStructuringType(), null);
        }
        iPartsVirtualNode productNode = new iPartsVirtualNode(productType, product.getAsId());

        boolean hasNavContextRestriction = (navContext != null) && !navContext.isEmpty();
        iPartsVirtualNode[] virtualNodes = hasNavContextRestriction ? new iPartsVirtualNode[2] : new iPartsVirtualNode[1];
        virtualNodes[0] = productNode;
        if (hasNavContextRestriction) {
            Map<String, String> nodePropMap = getNavContextNodesMap(navContext, navType);
            iPartsVirtualNode virtualNode = new iPartsVirtualNode(navType, nodePropMap);
            virtualNodes[1] = virtualNode;
        }

        return iPartsVirtualNode.getVirtualIdString(virtualNodes);
    }

    /**
     * Liefert basierend auf dem übergebenen <i>navContext</i> für den gewünschten {@link iPartsNodeType} eine Map von
     * {@link iPartsWSNavNode.TYPE#getiPartsVirtualNodeId()} auf die dazu passende Knoten-Nummer.
     *
     * @param navContext
     * @param navType
     * @return
     */
    private static Map<String, String> getNavContextNodesMap(List<? extends iPartsWSNavNode> navContext, iPartsNodeType navType) {
        // virtuellen Knoten für Navigationskontext bestimmen
        Map<String, String> nodePropMap = new HashMap<>();
        for (int iLevel = 0; iLevel < navContext.size(); iLevel++) {
            iPartsWSNavNode navNode = navContext.get(iLevel);
            iPartsWSNavNode.TYPE type = navNode.getTypeAsEnum();
            if (type == null) {
                WSAbstractEndpoint.throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid navNodeType '"
                                                                                         + navNode.getType() + "' for structuringType '"
                                                                                         + navType + "' in navContext '"
                                                                                         + getErrorStringForNavNodes(navContext) + "'", null);
                return nodePropMap; // egal, weil durch RuntimeException sowieso unerreichbar -> nur für die Code-Analyse
            }

            // Stimmt der Knotentyp?
            if (type.getNodeType() != navType) {
                boolean isError = true;
                if (type.getNodeType() == iPartsNodeType.KGTU) {
                    if (type == iPartsWSNavNode.TYPE.cg_group) {
                        // KG von KG/TU ist bei KG/SA oder EinPAS ebenfalls zulässig
                        isError = (navType != iPartsNodeType.KGSA) && (navType != iPartsNodeType.EINPAS);
                    } else if (type == iPartsWSNavNode.TYPE.cg_subgroup) {
                        // TU von KG/TU ist auch bei Einpas zulässig
                        isError = navType != iPartsNodeType.EINPAS;
                    }
                }

                if (isError) {
                    WSAbstractEndpoint.throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Wrong navNodeType '"
                                                                                             + type.name() + "' for structuringType '"
                                                                                             + navType + "' in navContext '"
                                                                                             + getErrorStringForNavNodes(navContext) + "'", null);
                }
            }

            // Stimmt die Knotentiefe?
            if (type.getLevel() != iLevel) {
                WSAbstractEndpoint.throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Wrong level "
                                                                                         + iLevel + " for navNodeType '"
                                                                                         + type.name() + "' in navContext '"
                                                                                         + getErrorStringForNavNodes(navContext)
                                                                                         + "' (expected level: " + type.getLevel() + ")", null);
            }
            String iPartsVirtualNodeTypeKey = type.getiPartsVirtualNodeId();
            String navNodeId = navNode.getId();
            if (type == iPartsWSNavNode.TYPE.sa_number) {
                navNodeId = new iPartsNumberHelper().unformatSaForDB(navNodeId); // Formatierung entfernen
            }
            nodePropMap.put(iPartsVirtualNodeTypeKey, navNodeId);
        }
        return nodePropMap;
    }

    public static List<iPartsWSNavNode> createNavContext(iPartsProduct product, iPartsProductStructures productStructures,
                                                         iPartsDataModuleEinPAS dataModuleEinPAS, String language, EtkProject project) {
        List<iPartsWSNavNode> navContext = new DwList<>();
        if (dataModuleEinPAS != null) {
            iPartsModuleEinPASId moduleEinPASId = dataModuleEinPAS.getAsId();
            String entryProductNo = moduleEinPASId.getProductNumber();
            iPartsProductId entryProductId = new iPartsProductId(entryProductNo);
            boolean partsAvailable;
            if (product.getProductStructuringType() == iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU) {
                String kg = dataModuleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_KG);
                String tu = dataModuleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_TU);
                KgTuForProduct kgTuForProduct = KgTuForProduct.getInstance(project, entryProductId);

                // bestimmen, ob es nur ein Modul gibt oder mehrere; bei mehreren müssen wir noch einen Modulknoten einfügen
                iPartsCatalogNode productNode = productStructures.getKgTuStructureWithoutCarPerspective(project, product.isStructureWithAggregates());
                iPartsCatalogNode tuCatalogNode = productNode.getNode(new KgTuId(kg, tu));
                if (tuCatalogNode == null) {
                    return null;
                }

                partsAvailable = tuCatalogNode.getChildren().size() == 1; // Anzahl Module im TU-Knoten

                KgTuNode kgTuNode = kgTuForProduct.getKgNode(kg);
                String kgLabel = "";
                if (kgTuNode != null) {
                    kgLabel = kgTuNode.getTitle().getTextByNearestLanguage(language, project.getDataBaseFallbackLanguages());
                }
                iPartsWSNavNode kgNode = new iPartsWSNavNode(iPartsWSNavNode.TYPE.cg_group, kg, false, kgLabel);
                navContext.add(kgNode);

                kgTuNode = kgTuForProduct.getTuNode(kg, tu);
                String tuLabel = "";
                if (kgTuNode != null) {
                    tuLabel = kgTuNode.getTitle().getTextByNearestLanguage(language, project.getDataBaseFallbackLanguages());
                }
                iPartsWSNavNode tuNode = new iPartsWSNavNode(iPartsWSNavNode.TYPE.cg_subgroup, tu, partsAvailable, tuLabel);
                navContext.add(tuNode);
            } else if (product.getProductStructuringType() == iPartsConst.PRODUCT_STRUCTURING_TYPE.EINPAS) {
                String hg = dataModuleEinPAS.getFieldValue(iPartsConst.FIELD_DME_EINPAS_HG);
                String g = dataModuleEinPAS.getFieldValue(iPartsConst.FIELD_DME_EINPAS_G);
                String tu = dataModuleEinPAS.getFieldValue(iPartsConst.FIELD_DME_EINPAS_TU);
                EinPas einPas = EinPas.getInstance(project);

                // bestimmen, ob es nur ein Modul gibt oder mehrere; bei mehreren müssen wir noch einen Modulknoten einfügen
                iPartsCatalogNode productNode = productStructures.getCompleteEinPasStructure(project, product.isStructureWithAggregates());
                iPartsCatalogNode tuCatalogNode = productNode.getNode(new EinPasId(hg, g, tu));
                if (tuCatalogNode == null) {
                    return null;
                }

                partsAvailable = tuCatalogNode.getChildren().size() == 1; // Anzahl Module im TU-Knoten

                EinPasNode einPasNode = einPas.getHGNode(hg);
                String hgLabel = "";
                if (einPasNode != null) {
                    hgLabel = einPasNode.getTitle().getTextByNearestLanguage(language, project.getDataBaseFallbackLanguages());
                }
                iPartsWSNavNode hgNode = new iPartsWSNavNode(iPartsWSNavNode.TYPE.maingroup, hg, false, hgLabel);
                navContext.add(hgNode);

                einPasNode = einPas.getGNode(hg, g);
                String gLabel = "";
                if (einPasNode != null) {
                    gLabel = einPasNode.getTitle().getTextByNearestLanguage(language, project.getDataBaseFallbackLanguages());
                }
                iPartsWSNavNode gNode = new iPartsWSNavNode(iPartsWSNavNode.TYPE.group, g, false, gLabel);
                navContext.add(gNode);

                einPasNode = einPas.getTuNode(hg, g, tu);
                String tuLabel = "";
                if (einPasNode != null) {
                    tuLabel = einPasNode.getTitle().getTextByNearestLanguage(language, project.getDataBaseFallbackLanguages());
                }
                iPartsWSNavNode tuNode = new iPartsWSNavNode(iPartsWSNavNode.TYPE.subgroup, tu, partsAvailable, tuLabel);
                navContext.add(tuNode);
            } else {
                WSAbstractEndpoint.throwProcessingError(WSError.INTERNAL_ERROR, "Unexpected productStructuringType for module usage: "
                                                                                + product.getProductStructuringType(), null);
                return null;
            }

            // falls !partsAvailable muss als letzter Knoten noch ein NavNode vom Typ Modul hinzugefügt werden
            if (!partsAvailable) {
                String moduleNo = moduleEinPASId.getModuleNumber();

                // Der Name vom Modul ist zwar fast immer mit dem TU-Namen identisch, könnte über iPartsEdit aber
                // auch verändert worden sein
                String moduleName = iPartsDataAssembly.getAssemblyText(project, new AssemblyId(moduleNo, "")).getTextByNearestLanguage(language, project.getDataBaseFallbackLanguages());

                iPartsWSNavNode moduleNode = new iPartsWSNavNode(iPartsWSNavNode.TYPE.module, moduleNo, true, moduleName);
                navContext.add(moduleNode);
            }
        }
        return navContext;
    }

    public static List<iPartsWSNavNode> createNavContext(iPartsProduct product, iPartsProductStructures productStructures,
                                                         iPartsDataProductSAs dataProductSAs, AssemblyId saAssemblyId, String language, EtkProject project) {
        List<iPartsWSNavNode> navContext = new DwList<>();
        if ((dataProductSAs != null) && (saAssemblyId != null)) {
            iPartsProductId entryProductId = product.getAsId();
            boolean partsAvailable;
            if (product.getProductStructuringType() == iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU) {
                String kg = dataProductSAs.getAsId().getKG();
                KgTuForProduct kgTuForProduct = KgTuForProduct.getInstance(project, entryProductId);
                String saNumber = dataProductSAs.getAsId().getSaNumber();

                // bestimmen, ob es nur ein Modul gibt oder mehrere; bei mehreren müssen wir noch einen Modulknoten einfügen
                iPartsCatalogNode productNode = productStructures.getKgTuStructureWithoutCarPerspective(project, product.isStructureWithAggregates());
                iPartsCatalogNode saCatalogNode = productNode.getNode(new KgSaId(kg, saNumber));
                partsAvailable = saCatalogNode.getChildren().size() == 1; // Anzahl Module im SA-Knoten

                KgTuNode kgTuNode = kgTuForProduct.getKgNode(kg);
                String kgLabel = "";
                if (kgTuNode != null) {
                    kgLabel = kgTuNode.getTitle().getTextByNearestLanguage(language, project.getDataBaseFallbackLanguages());
                }
                iPartsWSNavNode kgNode = new iPartsWSNavNode(iPartsWSNavNode.TYPE.cg_group, kg, false, kgLabel);
                navContext.add(kgNode);

                String formattedSaNumber = iPartsNumberHelper.formatPartNo(project, saNumber, language);
                String saDescription = iPartsSA.getSaSaaDescription(project, saNumber, language);
                iPartsWSNavNode saNode = new iPartsWSNavNode(iPartsWSNavNode.TYPE.sa_number, formattedSaNumber, partsAvailable,
                                                             saDescription);
                navContext.add(saNode);
            } else {
                WSAbstractEndpoint.throwProcessingError(WSError.INTERNAL_ERROR, "Unexpected productStructuringType for SA usage: "
                                                                                + product.getProductStructuringType(), null);
                return null;
            }

            // falls !partsAvailable muss als letzter Knoten noch ein NavNode vom Typ Modul hinzugefügt werden
            if (!partsAvailable) {
                // Der Name vom Modul ist zwar fast immer mit dem TU-Namen identisch, könnte über iPartsEdit aber
                // auch verändert worden sein
                String moduleName = iPartsDataAssembly.getAssemblyText(project, saAssemblyId).getTextByNearestLanguage(language, project.getDataBaseFallbackLanguages());

                iPartsWSNavNode moduleNode = new iPartsWSNavNode(iPartsWSNavNode.TYPE.module, saAssemblyId.getKVari(), true,
                                                                 moduleName);
                navContext.add(moduleNode);
            }
        }
        return navContext;
    }

    public static KgTuId getKgTuId(List<? extends iPartsWSNavNode> navContext) {
        if ((navContext == null) || navContext.isEmpty()) {
            return null;
        }

        if (navContext.get(0).getTypeAsEnum() != iPartsWSNavNode.TYPE.cg_group) {
            return null;
        }

        Map<String, String> nodesMap = getNavContextNodesMap(navContext, iPartsNodeType.KGTU);

        // KG
        String kg = nodesMap.get(iPartsVirtualNode.KG);
        if (kg == null) {
            return null;
        }

        // TU
        String tu = nodesMap.get(iPartsVirtualNode.TU);
        if (tu == null) {
            tu = "";
        }

        return new KgTuId(kg, tu);
    }

    public static EinPasId getEinPasId(List<? extends iPartsWSNavNode> navContext) {
        if ((navContext == null) || navContext.isEmpty()) {
            return null;
        }

        if (navContext.get(0).getTypeAsEnum() != iPartsWSNavNode.TYPE.maingroup) {
            return null;
        }

        Map<String, String> nodesMap = getNavContextNodesMap(navContext, iPartsNodeType.EINPAS);

        // HG
        String hg = nodesMap.get(iPartsVirtualNode.HG);
        if (hg == null) {
            return null;
        }

        // G
        String g = nodesMap.get(iPartsVirtualNode.G);
        String tu = null;
        if (g == null) {
            g = "";
        } else {
            tu = nodesMap.get(iPartsVirtualNode.TU); // TU macht nur mit G Sinn
        }

        // TU
        if (tu == null) {
            tu = "";
        }

        return new EinPasId(hg, g, tu);
    }

    private static Map<String, Map<String, String>> loadTopNodes(EtkProject project, iPartsProductId productId, String countryCode) {
        return iPartsDataTopTUsList.loadTopNodes(project, productId, countryCode);
    }

    /**
     * Liefert bei nicht vorhandenem {@code navContext} eine Liste aller {@link iPartsWSTopNode}s für das übergebene Produkt
     * und Land zurück.
     *
     * @param project
     * @param product
     * @param productStructures
     * @param countryCode
     * @param navContext
     * @return
     */
    public static List<iPartsWSTopNode> getTopNodes(EtkProject project, iPartsProduct product, iPartsProductStructures productStructures,
                                                    String countryCode, List<? extends iPartsWSNavNode> navContext) {
        // Nur ohne navContext und mit gültigem Land die Liste aller Top-Knoten zurückliefern
        if (((navContext != null) && !navContext.isEmpty()) || StrUtils.isEmpty(countryCode)) {
            return null;
        }

        Map<String, Map<String, String>> topNodesMap = loadTopNodes(project, product.getAsId(), countryCode);
        if (!topNodesMap.isEmpty()) {
            String dbLanguage = project.getDBLanguage();
            List<String> dbFallbackLanguages = project.getDataBaseFallbackLanguages();
            List<iPartsWSTopNode> topNodes = new ArrayList<>();
            for (Map.Entry<String, Map<String, String>> topNodeEntry : topNodesMap.entrySet()) {
                // Top-KG-Benennung
                String kg = topNodeEntry.getKey();
                KgTuNode kgNode = productStructures.getKgTuNode(project, new KgTuId(kg, ""));
                String kgLabel = "";
                if (kgNode != null) {
                    kgLabel = kgNode.getTitle().getTextByNearestLanguage(dbLanguage, dbFallbackLanguages);
                }

                // Top-KG/TUs sortiert nach KG/TU (da es auch gleiche Ränge gibt) zu topNodes hinzufügen
                for (Map.Entry<String, String> topTuNodes : topNodeEntry.getValue().entrySet()) {
                    int topRank = StrUtils.strToIntDef(topTuNodes.getValue(), 0);

                    // Top-KG
                    iPartsWSTopNode kgNavNode = new iPartsWSTopNode(iPartsWSNavNode.TYPE.cg_group, kg, kgLabel);
                    kgNavNode.setTopRank(topRank);

                    // Top-TU inkl. Benennung
                    String tu = topTuNodes.getKey();
                    KgTuNode tuNode = productStructures.getKgTuNode(project, new KgTuId(kg, tu));
                    String tuLabel = "";
                    if (tuNode != null) {
                        tuLabel = tuNode.getTitle().getTextByNearestLanguage(dbLanguage, dbFallbackLanguages);
                    }
                    iPartsWSTopNode tuNavNode = new iPartsWSTopNode(iPartsWSNavNode.TYPE.cg_subgroup, tu, tuLabel);
                    tuNavNode.setTopRank(topRank);

                    kgNavNode.setNextTopNode(tuNavNode);
                    topNodes.add(kgNavNode);
                }
            }
            return topNodes;
        }

        return null;
    }
}