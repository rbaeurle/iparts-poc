/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObjectInterface;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsHashHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsPublishingHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWireHarnessHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsCustomProperty;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsSPKMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsWireHarness;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getparts.iPartsWSGetPartsRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getparts.iPartsWSGetPartsResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.versioninfo.iPartsWSVersionInfoResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.*;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Abstrakter Endpoint enthält die gemeinsame Funktionalität für GetParts und GetMaterialParts
 */
public abstract class iPartsWSAbstractGetPartsEndpoint<REQUEST_PARTS extends WSRequestTransferObjectInterface> extends iPartsWSAbstractEndpoint<REQUEST_PARTS> implements iPartsConst {

    public static boolean IN_UNITTEST_MODE = false;

    public iPartsWSAbstractGetPartsEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    public void clearCaches() {
        super.clearCaches();
        iPartsWSFilteredPartListsCache.clearCaches();
    }

    /**
     * Bei TU-Knoten, die nur ein Submodul haben, liefert isPartsAvailable() schon true. Die Stückliste dieses Moduls wird dann als
     * Stückliste des TU-Knotens dargestellt. Die AssemblyId dieses Moduls wird hier bestimmt.
     * Wenn der Client sauber bei den Voraufrufen das partsAvaiable Flag im Response ausgewertet hat, können u.a. die Exceptions
     * eigentlich garnicht erst auftreten.
     *
     * @param navNodes
     * @param moduleNodes
     * @return
     */
    protected AssemblyId getAssemblyIdForModuleNodes(List<? extends iPartsWSNavNode> navNodes, List<iPartsWSNavNode> moduleNodes) {
        if ((moduleNodes == null) || moduleNodes.isEmpty()) {
            throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid attribute 'navContext' (no module found): "
                                                                  + iPartsWSNavHelper.getErrorStringForNavNodes(navNodes), null);
        } else if (moduleNodes.size() > 1) {
            throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Ambiguous attribute 'navContext' (more than one module found): "
                                                                  + iPartsWSNavHelper.getErrorStringForNavNodes(navNodes), null);
        } else {
            iPartsWSNavNode moduleNode = moduleNodes.get(0);
            if (moduleNode.getTypeAsEnum() != iPartsWSNavNode.TYPE.module) {
                throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid type of last node in attribute 'navContext' (must be one of ["
                                                                      + iPartsWSNavNode.TYPE.subgroup.name() + ", "
                                                                      + iPartsWSNavNode.TYPE.cg_subgroup.name() + ", "
                                                                      + iPartsWSNavNode.TYPE.module.name() + "]): "
                                                                      + iPartsWSNavHelper.getErrorStringForNavNodes(navNodes), null);
            } else if (!moduleNode.isPartsAvailable()) {
                throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "No parts available for attribute 'navContext': "
                                                                      + iPartsWSNavHelper.getErrorStringForNavNodes(navNodes), null);
            } else { // genau ein Modul mit Teilen gefunden
                return new AssemblyId(moduleNode.getId(), "");
            }
        }

        return null;
    }

    /**
     * Erstellt das gemeinsame Response Objekt für GetParts und GetMaterialParts und füllt es mit den verfügbaren Informationen
     *
     * @param assemblyId
     * @param product
     * @param requestObject
     * @param language
     * @param modelNumber
     * @param countryCode
     * @param project
     * @return
     */
    protected iPartsWSGetPartsResponse createResponse(AssemblyId assemblyId, iPartsProduct product, REQUEST_PARTS requestObject,
                                                      String language, String modelNumber, String countryCode, EtkProject project) {
        iPartsWSGetPartsResponse response = new iPartsWSGetPartsResponse();
        if (assemblyId != null) {
            try {
                // Modul und ungefilterte Stückliste holen
                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);

                // gefilterte Stückliste holen inkl. Laden der Befestigungsteile falls notwendig
                Collection<EtkDataPartListEntry> partListEntries = getFilteredPartListEntries(assembly, product, requestObject);

                // EinPAS Daten für Modul nur laden, wenn nicht RMI:
                Map<PartListEntryId, List<iPartsDataModuleCemat>> einPasDataMap = null;
                if (!iPartsWebservicePlugin.isRMIActive()) {
                    einPasDataMap = iPartsDataModuleCematList.loadCematMapForModule(project, assemblyId);
                }

                // Alternativteile ausgeben (Paramter nur für GetParts gültig)
                boolean includeAlternativeParts = false;
                if (requestObject instanceof iPartsWSGetPartsRequest) {
                    includeAlternativeParts = ((iPartsWSGetPartsRequest)requestObject).isIncludeAlternativeParts();
                }

                // Teile für das Modul bestimmen
                boolean includeReplacementChain = false;
                if ((requestObject instanceof iPartsWSGetPartsRequest) && !iPartsWebservicePlugin.isRMIActive()) {
                    includeReplacementChain = ((iPartsWSGetPartsRequest)requestObject).isIncludeReplacementChain();
                }
                List<iPartsWSPart> partsForPartList = getPartsForPartList(assembly, partListEntries, true,
                                                                          language, false,
                                                                          modelNumber, countryCode, null,
                                                                          false, false,
                                                                          einPasDataMap, includeReplacementChain,
                                                                          includeAlternativeParts,
                                                                          product.isWireHarnessDataVisible(), project);
                response.assignRMIValues(partsForPartList);
                if (!iPartsWebservicePlugin.isRMIActive()) {
                    iPartsWSReleaseInfo releaseInfoForProduct = null;
                    if (!IN_UNITTEST_MODE) {
                        releaseInfoForProduct = getReleaseInfoForProduct(product, project);
                    }
                    // Zeichnungen für das Modul bestimmen
                    List<iPartsWSImage> imagesForModule = iPartsWSImageHelper.getImagesForModule(assembly, partListEntries, project, language, true);
                    response.assignNonRMIValues(releaseInfoForProduct, imagesForModule);
                }
            } finally {
                // Filter wieder zurücksetzen, damit die Session wieder ohne Filter weiterverwendet werden kann (z.B. für die Unittests)
                iPartsFilter.disableAllFilters();
            }
        } else {
            // Hier sollte man eigentlich nie landen weil productId und NavContext schon zuvor geprüft werden
            throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Unable to find module for product \"" + product.getAsId().getProductNumber()
                                                                  + "\" and given navContext", null);
        }
        return response;
    }

    /**
     * Gibt bei Leerstrings null zurück, damit der Wert im Response automatisch unterdrückt wird.
     *
     * @param value Der zu prüfende Wert.
     * @return Keine Leerstrings! String oder null.
     */
    private String getWSDisplayableStringValue(String value) {
        return StrUtils.isValid(value) ? value : null;
    }

    /**
     * Füllt das Objekt {@link iPartsWSReleaseInfo} für die Ausgabe.
     * Leerstrings werden ge-null-t, damit sie bei der Ausgabe nicht leer ausgegeben werden.
     *
     * @param product
     * @param project
     * @return
     */
    protected iPartsWSReleaseInfo getReleaseInfoForProduct(iPartsProduct product, EtkProject project) {
        iPartsWSReleaseInfo releaseInfo = new iPartsWSReleaseInfo();
        String datasetDateString = DateUtils.formatDateTime(product.getDatasetDateString(),
                                                            DateUtils.simpleDateFormatyyyyMMdd,
                                                            DateUtils.simpleDateFormatIso);
        releaseInfo.setDatasetDate(getWSDisplayableStringValue(datasetDateString));
        String modificationTimestamp = DateUtils.formatDateTime(product.getModificationTimestamp(),
                                                                DateUtils.simpleTimeFormatyyyyMMddHHmmss,
                                                                DateUtils.simpleDateTimeZoneFormatIso);
        releaseInfo.setLastChangedDate(getWSDisplayableStringValue(modificationTimestamp));

        // Bei den WS-Unittests ist der publishingHelper nicht initialisiert ==> einen anlegen.
        iPartsPublishingHelper publishingHelper = iPartsPlugin.getPublishingHelper();
        if (publishingHelper == null) {
            publishingHelper = new iPartsPublishingHelper(project, Session.get());
        }

        iPartsWSVersionInfoResponse versionInfoResponse = new iPartsWSVersionInfoResponse(project, publishingHelper);
        releaseInfo.setLastPublishedDate(getWSDisplayableStringValue(versionInfoResponse.getLastPublishedDate()));
        releaseInfo.setSoftwareVersion(getWSDisplayableStringValue(versionInfoResponse.getVersion()));

        return releaseInfo;
    }

    /**
     * Teile für Stückliste bestimmen
     *
     * @param assembly
     * @param partListEntries          Stückliste
     * @param includePartContext       Soll der {@link iPartsWSPartContext} zu den Teilen hinzugefügt werden?
     * @param language
     * @param withExtendedDescriptions Soll für Ergänzungstexte die einfache (Attribut "description") oder die
     *                                 erweiterte (Attribute "additionalDesc und materialDesc) Darstellung genutzt werden
     * @param modelNumber
     * @param countryCode
     * @param bomKeysMap               Optionale Map für die Ausgabe der BOM-Schlüssel in Verbindung mit {@code withExtendedDescriptions}
     * @param reducedInformation       Sollen die Informationen auf ein Minimum reduziert werden?
     * @param einPasDataMap            Optionale Map von {@link PartListEntryId} auf EinPAS-Daten aus Cemat
     * @param includeReplacementChain
     * @param showWireHarnessData
     * @param project
     * @return
     */
    protected List<iPartsWSPart> getPartsForPartList(EtkDataAssembly assembly, Collection<EtkDataPartListEntry> partListEntries,
                                                     boolean includePartContext, String language, boolean withExtendedDescriptions,
                                                     String modelNumber, String countryCode, Map<String, String> bomKeysMap,
                                                     boolean reducedInformation, boolean showEinPasData,
                                                     Map<PartListEntryId, List<iPartsDataModuleCemat>> einPasDataMap,
                                                     boolean includeReplacementChain, boolean includeAlternativeParts,
                                                     boolean showWireHarnessData, EtkProject project) {
        List<iPartsWSPart> parts = new ArrayList<>(partListEntries.size());
        String hotspotFieldName = TableAndFieldName.getFieldName(assembly.getEbene().getKeyFeldName());

        // BOM-Schlüssel nur bei withExtendedDescriptions bei vorhandener bomKeysMap sowie DIALOG-Stücklisten setzen
        iPartsHashHelper hashHelper = null;
        if (withExtendedDescriptions && (bomKeysMap != null) && ((iPartsDataAssembly)assembly).getDocumentationType().isPKWDocumentationType()) {
            hashHelper = iPartsHashHelper.getInstance();
        }

        // Für alle Teile aus der Stückliste ein iPartsWSPart-Objekt erzeugen und dem Ergebnis hinzufügen
        boolean isRMIActive = iPartsWebservicePlugin.isRMIActive();
        String dbLanguage = project.getDBLanguage();
        List<String> dbFallbackLanguages = project.getDataBaseFallbackLanguages();
        iPartsCustomProperty iPartsCustomPropertyCache = iPartsCustomProperty.getInstance(project);
        boolean wireHarnessFilterConfigActive = iPartsWireHarnessHelper.isWireHarnessFilterConfigActive();
        iPartsWireHarness wireHarnessCache = iPartsWireHarness.getInstance(project);
        iPartsPRIMUSReplacementsCache primusReplacementsCache = iPartsPRIMUSReplacementsCache.getInstance(project);
        for (EtkDataPartListEntry partListEntry : partListEntries) {
            iPartsWSPart wsPart = new iPartsWSPart();
            String matNr = partListEntry.getPart().getAsId().getMatNr();

            // Additional Part Information & Custom Properties
            List<iPartsWSAdditionalPartInformation> additionalPartInformationList = iPartsWSAdditionalPartInformationHelper.fillAdditionalPartInformation(project, partListEntry.getPart(), matNr, dbLanguage, dbFallbackLanguages, iPartsCustomPropertyCache);
            if (!additionalPartInformationList.isEmpty()) {
                wsPart.setAdditionalPartInformation(additionalPartInformationList);
            }

            // Leitungssatzbaukästen
            if (showWireHarnessData && wireHarnessFilterConfigActive && iPartsWireHarnessHelper.hasValidAdditionalWireHarnessFlag(partListEntry)) {
                String originalMatNumber = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_MAT_NR);
                if (wireHarnessCache.isWireHarness(originalMatNumber)) {
                    if (withExtendedDescriptions) {
                        List<iPartsDataWireHarness> dataWireHarnessList = iPartsFilterHelper.getFilteredWireHarnessComponent(project, originalMatNumber,
                                                                                                                             iPartsWSWireHarnessHelper.WIRING_HARNESS_DISPLAY_FIELDS);
                        if (!dataWireHarnessList.isEmpty()) {
                            HmMSmId hmMSmId = null;
                            iPartsSPKMappingCache spkMappingCache = null; // SPK Mapping Cache für die Baureihe und Lenkung
                            String steeringValue = "L";
                            if (iPartsPlugin.isUseSPKMapping() && (partListEntry instanceof iPartsDataPartListEntry)) {
                                iPartsDialogBCTEPrimaryKey bctePrimaryKey = ((iPartsDataPartListEntry)partListEntry).getDialogBCTEPrimaryKey();
                                if (bctePrimaryKey != null) {
                                    hmMSmId = bctePrimaryKey.getHmMSmId();
                                    // Lenkung aus den aktuellen Filtereinstellungen auslesen
                                    // Wenn keine Lenkung angegeben wurde, oder der Lenkungsfilter deaktiviert ist, wird als default "L" verwendet
                                    if (iPartsFilter.get().isSteeringFilterActive(((iPartsDataPartListEntry)partListEntry).getOwnerAssembly())) {
                                        String steeringFilterValue = iPartsFilter.get().getSteeringValue();
                                        if (!steeringFilterValue.isEmpty()) {
                                            steeringValue = steeringFilterValue;
                                        }
                                    }

                                    spkMappingCache = iPartsSPKMappingCache.getInstance(project, hmMSmId.getSeriesId(), steeringValue);
                                }
                            }
                            List<iPartsWSWiringHarness> iPartsWSWiringHarnessList = iPartsWSWireHarnessHelper.fillWiringHarnessKit(project,
                                                                                                                                   dataWireHarnessList, withExtendedDescriptions, hmMSmId, spkMappingCache, steeringValue);
                            wsPart.setWiringHarnessKit(iPartsWSWiringHarnessList);
                        }
                    } else {
                        wsPart.setWiringHarnessKitAvailable(true);
                    }
                }
            }

            // EinPAS Daten (bei RMI Webservices ist einPasDataMap null)
            List<iPartsWSEinPAS> einPasData = iPartsWSEinPasHelper.fillEinPasList(einPasDataMap, partListEntry);
            if (einPasData != null) {
                if (showEinPasData) {
                    wsPart.setEinPAS(einPasData);
                } else {
                    wsPart.setEinPASNodeAvailable(true);

                    // Den EinPAS-Knoten mit der höchsten Versionsnummer nur setzen, wenn der entsprechende Konfigurationsschalter eingeschaltet ist.
                    if (iPartsWebservicePlugin.isShowLatestEinPASNodeInResponse()) {
                        wsPart.setLatestEinPASNode(iPartsWSEinPasHelper.getLatestEinPASNode(einPasData));
                    }
                }
            }

            wsPart.assignRMIValues(project, (iPartsDataPartListEntry)partListEntry, includePartContext, language, withExtendedDescriptions,
                                   reducedInformation);
            if (!isRMIActive && !reducedInformation) {
                // reducedInformation wird hier immer false sein
                wsPart.assignNonRMIValues(project, (iPartsDataPartListEntry)partListEntry, hotspotFieldName, includePartContext,
                                          true, language, withExtendedDescriptions, includeReplacementChain, includeAlternativeParts, modelNumber,
                                          countryCode, primusReplacementsCache);

                // BOM-Schlüssel setzen
                if ((bomKeysMap != null) && (hashHelper != null)) {
                    iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(partListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID));
                    String bomKey = hashHelper.getHashValueForBCTEPrimaryKey(bctePrimaryKey, bomKeysMap);
                    if (StrUtils.isValid(bomKey)) {
                        wsPart.setBomKey(bomKey);
                    }
                }
            }
            parts.add(wsPart);
        }
        return parts;
    }

    protected AssemblyId getAssemblyForNavContext(iPartsProduct product, iPartsProductStructures productStructures,
                                                  Set<String> modelNumbers, List<? extends iPartsWSNavNode> navContext,
                                                  String language, EtkProject project, boolean isSpecialCatalog, String additionalFilterCacheKey,
                                                  iPartsWSIdentContext identContext, boolean withExtendedDescriptions,
                                                  boolean reducedInformation, Set<String> specialUserPermissions) {
        if (product != null) {
            // navContext darf nicht null oder leer sein
            if ((navContext != null) && !navContext.isEmpty()) {
                iPartsWSNavNode lastNode = navContext.get(navContext.size() - 1);
                AssemblyId assemblyId = null;
                if (lastNode.getTypeAsEnum() == iPartsWSNavNode.TYPE.module) { // letzter Knoten ist ein Modul -> direkt dieses nehmen
                    assemblyId = new AssemblyId(lastNode.getId(), "");
                } else { // Unterscheidung anhand des ersten NavNodes, ob KG/TU oder EinPAS
                    iPartsWSNavNode.TYPE firstNodeType = navContext.get(0).getTypeAsEnum();
                    switch (firstNodeType) {
                        case cg_group:
                            List<iPartsWSNavNode> moduleNodes = iPartsWSNavHelper.getKgTuChildNavNodes(product, productStructures,
                                                                                                       modelNumbers, navContext,
                                                                                                       project, language, false,
                                                                                                       false, isSpecialCatalog,
                                                                                                       additionalFilterCacheKey,
                                                                                                       identContext, this,
                                                                                                       withExtendedDescriptions,
                                                                                                       reducedInformation,
                                                                                                       specialUserPermissions);
                            if ((moduleNodes != null) && !moduleNodes.isEmpty()) { // null/leer kann z.B. bei freien SAs passieren, die nur für einige Baumuster eines Produkts gültig sein können
                                assemblyId = getAssemblyIdForModuleNodes(navContext, moduleNodes);
                            }
                            break;
                        case maingroup:
                            moduleNodes = iPartsWSNavHelper.getEinPasChildNavNodes(product, productStructures, navContext,
                                                                                   identContext, project, language, false, false,
                                                                                   withExtendedDescriptions,
                                                                                   reducedInformation, specialUserPermissions);
                            if ((moduleNodes != null) && !moduleNodes.isEmpty()) { // null/leer kann z.B. bei freien SAs passieren, die nur für einige Baumuster eines Produkts gültig sein können
                                assemblyId = getAssemblyIdForModuleNodes(navContext, moduleNodes);
                            }
                            break;
                        default:
                            throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid type of first node in attribute 'navContext': "
                                                                                  + firstNodeType.name(), null);
                    }
                }
                return assemblyId;
            }
        }
        return null;
    }

    protected AssemblyId getAssemblyForNavContext(iPartsProduct product, iPartsProductStructures productStructures,
                                                  Set<String> modelNumbers, List<? extends iPartsWSNavNode> navContext,
                                                  String language, EtkProject project, boolean isSpecialCatalog, String additionalFilterCacheKey,
                                                  iPartsWSIdentContext identContext, Set<String> specialUserPermissions) {
        return getAssemblyForNavContext(product, productStructures, modelNumbers, navContext, language, project, isSpecialCatalog,
                                        additionalFilterCacheKey, identContext, false, false, specialUserPermissions);

    }

    protected abstract Collection<EtkDataPartListEntry> getFilteredPartListEntries(EtkDataAssembly
                                                                                           assembly, iPartsProduct product,
                                                                                   REQUEST_PARTS requestObject);

    protected abstract iPartsWSIdentContext getIdentContext(REQUEST_PARTS requestObject);
}