/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObjectInterface;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPartlistTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsCodeDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getpartinfo.iPartsWSGetPartInfoResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.*;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.booleanfunctionparser.BooleanFunctionSyntaxException;
import de.docware.util.misc.booleanfunctionparser.model.AbstractTerm;
import de.docware.util.misc.booleanfunctionparser.model.Conjunction;
import de.docware.util.misc.booleanfunctionparser.model.Disjunction;
import de.docware.util.sort.SortUtils;

import java.util.*;

/**
 * Endpoint für den GetPartInfo-Webservice
 * Der GetMaterialPartInfo-Webservice für die Sonderkataloge ist davon abgeleitet.
 */
public abstract class iPartsWSAbstractGetPartInfoEndpoint<REQUEST_CLASS extends WSRequestTransferObjectInterface> extends iPartsWSAbstractEndpoint<REQUEST_CLASS> implements iPartsConst {

    public static final String DEFAULT_ENDPOINT_URI = "/parts/GetPartInfo";

    private ObjectInstanceLRUList<String, iPartsWSCodeValidityDetail> codeCache = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_CODE,
                                                                                                              iPartsPlugin.getCachesLifeTime());

    private static final List<iPartsWSPlantInformation> EMPTY_PLANT_INFOS = new DwList<>();

    public iPartsWSAbstractGetPartInfoEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    public void clearCaches() {
        super.clearCaches();
        if (codeCache != null) {
            codeCache.clear();
        }
        iPartsWSFilteredPartListsCache.clearCaches();
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if ((dataType == iPartsDataChangedEventByEdit.DataType.PRODUCT) || (dataType == iPartsDataChangedEventByEdit.DataType.SERIES)
            || (dataType == iPartsDataChangedEventByEdit.DataType.MODEL) || (dataType == iPartsDataChangedEventByEdit.DataType.PART_LIST) ||
            (dataType == iPartsDataChangedEventByEdit.DataType.SA)) {
            clearCaches();
        }
    }

    protected abstract iPartsWSIdentContext getIdentContext(REQUEST_CLASS requestObject);

    protected abstract Set<String> getFilteredPartListSequenceNumbers(EtkDataAssembly assembly, iPartsProduct product,
                                                                      REQUEST_CLASS requestObject);

    protected void checkModuleValid(EtkProject project, String inputProductId, iPartsAssemblyId assemblyId,
                                    String inputModuleId, Set<String> specialPermissions) {
        iPartsProductId productId = new iPartsProductId(inputProductId);
        iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, productId);
        Set<AssemblyId> moduleIdsForProduct = productStructures.getModuleIds(project);

        boolean isValid = true;
        // Models für SA Filterung bestimmen
        if (!moduleIdsForProduct.contains(assemblyId)) {
            // Könnte auch das Modul von einer freien SA sein
            iPartsDataSAModulesList dataSAModulesList = iPartsDataSAModulesList.loadDataForModule(project, new iPartsModuleId(assemblyId.getKVari()));
            isValid = false;
            for (iPartsDataSAModules dataSAModule : dataSAModulesList) {
                if (iPartsFilter.get().isSaVisible(project, dataSAModule.getAsId().getSaNumber(), productId)) {
                    isValid = true;
                    break;
                }
            }
        }
        // Check, ob das Modul nur mit Spezial-Rechten gesehen werden darf
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
        if (isValid) {
            if (assembly instanceof iPartsDataAssembly) {
                if (!iPartsWSNavHelper.checkSpecialPermissions((iPartsDataAssembly)assembly, specialPermissions)) {
                    isValid = false;
                }
            }
        }
        if (!isValid) {
            throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Module '" + inputModuleId + "' is invalid for product '"
                                                                  + inputProductId + "', current context and filter options", null);
        }
    }


    protected iPartsWSGetPartInfoResponse createResponse(iPartsAssemblyId assemblyId, String inputLfdNr, String productNumber,
                                                         REQUEST_CLASS requestObject, String modelNumber, String countryCode,
                                                         EtkProject project) {
        try {
            // Gefilterte Stückliste holen inkl. Laden der Befestigungsteile falls notwendig
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
            iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNumber));
            Set<String> filteredPartListEntries = getFilteredPartListSequenceNumbers(assembly, product, requestObject);

            // Prüfen ob die laufende Nummer vom PartContext in der gefilterten Stückliste enthalten ist
            if (!filteredPartListEntries.contains(inputLfdNr)) {
                // Kein gültiger Stücklisteneintrag vorhanden
                throwInvalidPartListEntryRequestError(assemblyId, inputLfdNr);
                return null;
            }

            // Alle Prüfungen bestanden -> Stücklisteneintrag aus der ungefilterten Stückliste ermitteln (es wurde ja bereits
            // sichergestellt, dass inputLfdNr in der gefilterten Stückliste enthalten ist)
            EtkDataPartListEntry partListEntry = assembly.getPartListEntryFromKLfdNrUnfiltered(inputLfdNr);

            if (partListEntry == null) {
                // Kein gültiger Stücklisteneintrag vorhanden
                throwInvalidPartListEntryRequestError(assemblyId, inputLfdNr);
                return null;
            }

            // Nachladen von Feldern für diesen einzelnen Stücklisteneintrag kann passieren, wenn die ungefilterten
            // Stücklisteneinträge des Moduls nicht alle benötigten Felder enthalten -> ist für einen Datensatz aber
            // auch kein Performance-Problem und besser als die Stückliste komplett mit den benötigten Feldern zu laden
            partListEntry.setLogLoadFieldIfNeeded(false);

            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;

                // Baumuster-Filter muss explizit ausgeführt werden (partListEntry kommt aus der ungefilterten Stückliste)
                // falls er aktiv ist, damit die Filterung der Werkseinsatzdaten korrekt durchgeführt wird -> es würden
                // ansonsten die falschen Werkseinsatzdazen zurückgegeben werden
                iPartsFilter filter = iPartsFilter.get();
                if (filter.isModelFilterActive(iPartsPartListEntry.getOwnerAssembly())) {
                    // Befestigungsteile nachladen falls notwendig
                    iPartsWSIdentContext identContext = getIdentContext(requestObject);
                    if (identContext != null) {
                        identContext.loadFixingPartsIfNeeded(assembly, iPartsPartlistTypes.PART_LIST_TYPE_FOR_GET_PARTS);
                    }

                    if (!filter.checkFilter(partListEntry)) { // inkl. Baumuster-Filter
                        // Kann eigentlich nicht mehr passieren, da filteredPartListEntries ja bereits überprüft wurde
                        // Kein gültiger Stücklisteneintrag vorhanden
                        throwInvalidPartListEntryRequestError(assemblyId, inputLfdNr);
                        return null;
                    }
                } else {
                    // Zumindest die Gleichteile-Teilenummer muss gesetzt werden
                    filter.setEqualPartNumber(iPartsPartListEntry);
                }

                // Produktgruppe nur über Stücklisteneintrag bestimmen (K_PRODUCT_GRP) (kein Fallback wie im iPartsRelatedInfoCodeMasterDataForm)
                String productGroup = iPartsPartListEntry.getFieldValue(iPartsConst.FIELD_K_PRODUCT_GRP);

                // Baureihe nicht aus den Input Parametern nehmen, da diese bei Aggregaten falsch wäre
                // Bestimmung der Baureihe nur über SourceContext (Unterschiedlich zu iPartsRelatedInfoCodeMasterDataForm)
                // da noch nicht klar ist, ob Fallbacks hier überhaupt Sinn machen
                String seriesNumber = "";
                iPartsSeriesId seriesId = EditConstructionToRetailHelper.getSeriesIdFromDIALOGSourceContext(iPartsPartListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_CONTEXT));
                if (seriesId != null) {
                    seriesNumber = seriesId.getSeriesNumber();
                }

                String codeCompareDate = iPartsPartListEntry.getFieldValue(iPartsConst.FIELD_K_DATEFROM);

                // FACTORY DATA
                List<iPartsWSPlantInformation> plantInformation = null;
                if (iPartsPartListEntry.getFactoryDataValidity() == iPartsFactoryData.ValidityType.VALID) {
                    iPartsFactoryData factoryData = iPartsPartListEntry.getFactoryDataForRetail();
                    if (factoryData != null) {
                        plantInformation = fillFactoryData(factoryData, iPartsPartListEntry, productGroup, product.getAggregateType(),
                                                           codeCompareDate, project);
                    }
                }

                // COLORS
                // Hier sollen die Werksdaten der einzelnen (Farb-)Variante einer Variantentabelle verwendet werden
                List<iPartsWSColorInfo> colors = null;
                iPartsColorTable colorTables = iPartsPartListEntry.getColorTableForRetail();
                if (colorTables != null) {
                    colors = fillColorInfos(colorTables, project, productGroup, seriesNumber, product.getAggregateType(),
                                            codeCompareDate);
                }

                iPartsWSPartInfo partInfo = new iPartsWSPartInfo();
                partInfo.assignRMIValues(colors, plantInformation);

                if (!iPartsWebservicePlugin.isRMIActive()) {
                    // CODE VALIDITY DETAILS
                    String codeValidity = iPartsPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_WITH_EVENTS);
                    List<iPartsWSCodeValidityDetail> codeValidityDetails = getCodeValidityDetails(codeValidity, project, productGroup, seriesNumber, codeCompareDate);
                    List<List<iPartsWSCodeMatrixElement>> codeValidityMatrix = getCodeValidityMatrix(codeValidity);

                    // SAA VALIDITY DETAILS
                    // Gültige SAAs zusammen mit ihren SAs bestimmen
                    List<iPartsWSSaCode> saaValidityDetails = null;
                    EtkDataArray saaValidity = iPartsPartListEntry.getFieldValueAsArrayOriginal(iPartsConst.FIELD_K_SA_VALIDITY);
                    if (saaValidity != null) {
                        saaValidityDetails = fillSAValidity(saaValidity, project);
                    }

                    // ALTERNATIVE PARTS
                    List<iPartsWSAlternativePart> alternativeParts = null;
                    Set<EtkDataPart> alternativeMaterials = iPartsPartListEntry.getAlternativePartsFilteredByReplacements(modelNumber, countryCode);
                    if (alternativeMaterials != null) {
                        alternativeParts = iPartsWSAlternativePartsHelper.fillAlternativeParts(alternativeMaterials, project);
                    }
                    partInfo.assignNonRMIValues(codeValidityDetails, saaValidityDetails, alternativeParts, codeValidityMatrix);
                }

                // LEITUNGSSATZBAUKÄSTEN
                // Anzeige nur bei aktivierter Admin-Option "Leitungssatz-Baukasten Teilepositionen filtern", nur bei
                // einem Produkt, dass Connect Daten anzeigt, bei vorhandenem sonstige-Kenner "LA" (M_LAYOUT_FLAG) und
                // nach Prüfung ob ein LSBK-Datensatz vorhanden ist.
                if (product.isWireHarnessDataVisible() && iPartsWireHarnessHelper.isWireHarnessFilterConfigActive() && iPartsWireHarnessHelper.hasValidAdditionalWireHarnessFlag(partListEntry)) {
                    iPartsWireHarness wireHarnessCache = iPartsWireHarness.getInstance(project);
                    String originalMatNumber = iPartsPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_MAT_NR);
                    if (wireHarnessCache.isWireHarness(originalMatNumber)) {
                        List<iPartsDataWireHarness> dataWireHarnessList = iPartsFilterHelper.getFilteredWireHarnessComponent(project, originalMatNumber,
                                                                                                                             iPartsWSWireHarnessHelper.WIRING_HARNESS_DISPLAY_FIELDS);
                        if (!dataWireHarnessList.isEmpty()) {
                            // Falls die Option für SPK Mapping Texte aktiv ist, hier den BCTE Key und damit HM/M/SM ermitteln.
                            HmMSmId hmMSmId = null;
                            iPartsSPKMappingCache spkMappingCache = null; // SPK Mapping Cache für die Baureihe und Lenkung
                            String steeringValue = "L";
                            if (iPartsPlugin.isUseSPKMapping()) {
                                iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsPartListEntry.getDialogBCTEPrimaryKey();
                                if (bctePrimaryKey != null) {
                                    hmMSmId = bctePrimaryKey.getHmMSmId();
                                    // Lenkung aus den aktuellen Filtereinstellungen auslesen
                                    // Wenn keine Lenkung angegeben wurde, oder der Lenkungsfilter deaktiviert ist, wird als default "L" verwendet
                                    if (iPartsFilter.get().isSteeringFilterActive(iPartsPartListEntry.getOwnerAssembly())) {
                                        String steeringFilterValue = iPartsFilter.get().getSteeringValue();
                                        if (!steeringFilterValue.isEmpty()) {
                                            steeringValue = steeringFilterValue;
                                        }
                                    }

                                    spkMappingCache = iPartsSPKMappingCache.getInstance(project, hmMSmId.getSeriesId(), steeringValue);
                                }
                            }
                            List<iPartsWSWiringHarness> iPartsWSWiringHarnessList = iPartsWSWireHarnessHelper.fillWiringHarnessKit(project,
                                                                                                                                   dataWireHarnessList, false, hmMSmId, spkMappingCache, steeringValue);
                            partInfo.setWiringHarnessKit(iPartsWSWiringHarnessList);
                        }
                    }
                }

                // Die EinPAS Daten nur ausgeben, wenn nicht RMI:
                if (!iPartsWebservicePlugin.isRMIActive()) {
                    Map<PartListEntryId, List<iPartsDataModuleCemat>> einPasDataMap = iPartsDataModuleCematList.loadCematMapForModule(project,
                                                                                                                                      assembly.getAsId());
                    partInfo.setEinPAS(iPartsWSEinPasHelper.fillEinPasList(einPasDataMap, partListEntry));
                }

                return new iPartsWSGetPartInfoResponse(partInfo);
            }
            throwBadRequestError(WSError.INTERNAL_ERROR, "Web service GetPartInfo is only valid for iPartsDataPartListEntry", null);
        } finally {
            // Filter wieder zurücksetzen, damit die Session wieder ohne Filter weiterverwendet werden kann (z.B. für die Unittests)
            iPartsFilter.disableAllFilters();
        }
        return null;
    }

    private void throwInvalidPartListEntryRequestError(iPartsAssemblyId assemblyId, String inputLfdNr) {
        throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "SequenceId '" + inputLfdNr + "' is invalid in module '"
                                                              + assemblyId.getKVari() + "' for current context and filter options",
                             "partContext");
    }

    private List<List<iPartsWSCodeMatrixElement>> getCodeValidityMatrix(String codes) {
        if (!DaimlerCodes.isEmptyCodeString(codes) && DaimlerCodes.syntaxIsOK(codes)) {
            List<List<iPartsWSCodeMatrixElement>> result = new DwList<>();
            try {
                List<String> codeList = new DwList<>(DaimlerCodes.getCodeSet(codes)); // Codestring zerlegen
                SortUtils.sortList(codeList, false, false, true);
                Disjunction dnfCode = DaimlerCodes.getDnfCodeOriginal(codes); // dnfCode wird nicht verändert -> kein Klon der DNF notwendig
                if (!dnfCode.isEmpty()) {
                    for (Conjunction conjunction : dnfCode) {
                        List<iPartsWSCodeMatrixElement> partResult = new DwList<>();
                        for (String code : codeList) {
                            int cIndex = findConjunctionIndex(conjunction, code);
                            if (cIndex != -1) {
                                AbstractTerm term = conjunction.get(cIndex);
                                iPartsWSCodeMatrixElement element = new iPartsWSCodeMatrixElement(term.getVarName());
                                if (term.isNot()) {
                                    element.setNegative(true);
                                }
                                partResult.add(element);
                            }
                        }
                        result.add(partResult);
                    }
                }
            } catch (BooleanFunctionSyntaxException e) {
                throwBadRequestError(WSError.INTERNAL_ERROR, "Error while building code matrix for code \"" + codes + "\"", null);
                return null;
            }

            return result;
        }
        return null;
    }

    private int findConjunctionIndex(Conjunction conjunction, String varName) {
        for (int index = 0; index < conjunction.size(); index++) {
            AbstractTerm term = conjunction.get(index);
            if (term.getVarName().equals(varName)) {
                return index;
            }
        }
        return -1;
    }

    protected List<iPartsWSPlantInformation> fillFactoryData(iPartsFactoryData factoryData, iPartsDataPartListEntry partListEntry,
                                                             String productGroup, String aggregateType, String codeCompareDate, EtkProject project) {
        if (!factoryData.getFactoryDataMap().isEmpty()) {
            List<iPartsWSPlantInformation> plantInformationList = new DwList<>();
            String dbLanguage = project.getDBLanguage();
            List<String> dbFallbackLanguages = project.getDataBaseFallbackLanguages();
            iPartsFactoryModel factoryModel = iPartsFactoryModel.getInstance(project);
            for (Map.Entry<String, List<iPartsFactoryData.DataForFactory>> factoryEntry : factoryData.getFactoryDataMap().entrySet()) {
                for (iPartsFactoryData.DataForFactory dataForFactory : factoryEntry.getValue()) {
                    // Werkskennbuchstaben zu Werksnummer ermitteln aus Cache
                    String factorySigns = factoryModel.getFactorySignsStringForFactoryNumberAndSeries(factoryEntry.getKey(),
                                                                                                      new iPartsSeriesId(dataForFactory.seriesNumber),
                                                                                                      aggregateType);

                    if ((dataForFactory.eldasFootNoteId != null) && !dataForFactory.eldasFootNoteId.trim().isEmpty()) {
                        // Es handelt sich um eine ELDAS Fußnote, also wird nur der Text gesetzt
                        iPartsDataFootNoteContentList footNoteContentList = iPartsDataFootNoteContentList.loadFootNote(project, dataForFactory.eldasFootNoteId);
                        if (!footNoteContentList.isEmpty()) {
                            iPartsWSPlantInformation plantInformation = new iPartsWSPlantInformation(project, PlantInformationType.UNDEFINED);

                            // Mehrzeilige Fußnoten mit Newline als String zusammenbauen
                            StringBuilder completeText = new StringBuilder();
                            for (iPartsDataFootNoteContent footnoteContent : footNoteContentList) {
                                if (completeText.length() > 0) {
                                    completeText.append('\n');
                                }
                                completeText.append(footnoteContent.getText(dbLanguage, dbFallbackLanguages));
                            }

                            plantInformation.assignELDASRMIValues(completeText.toString());

                            if (!iPartsWebservicePlugin.isRMIActive()) {
                                String plant = null;
                                if (!StrUtils.isEmpty(factorySigns)) {
                                    plant = factorySigns;
                                }
                                plantInformation.assignELDASNonRMIValues(plant);
                            }

                            plantInformationList.add(plantInformation);
                        }
                    } else {
                        // Normale Rückmeldedaten werden in From und Upto Daten aufgeteilt
                        if (partListEntry.isPEMFromRelevant()) { // PEM ab relevant?
                            plantInformationList.addAll(createPlantInformationDTO(project, productGroup, dataForFactory.seriesNumber, codeCompareDate,
                                                                                  dataForFactory, factorySigns, PlantInformationType.FROM));
                        }
                        if (partListEntry.isPEMToRelevant()) { // PEM bis relevant?
                            plantInformationList.addAll(createPlantInformationDTO(project, productGroup, dataForFactory.seriesNumber, codeCompareDate,
                                                                                  dataForFactory, factorySigns, PlantInformationType.UPTO));
                        }
                    }
                }
            }
            if (!plantInformationList.isEmpty()) {
                return plantInformationList;
            }
        }
        return null;
    }

    /**
     * Erzeugt die {@link iPartsWSPlantInformation}-Einträge vom Response-DTO.
     *
     * @param project
     * @param productGroup
     * @param seriesNumber
     * @param codeCompareDate
     * @param dataForFactory       Die eigentlichen Werkseinsatzdaten (auch für Farbvarianten)
     * @param factorySigns         Kommaseparierte Liste von Werkskennzeichen (alle Werkskennzeichen zu einer Werksnummer)
     * @param plantInformationType Entweder {@link PlantInformationType#FROM} für Ab- oder {@link PlantInformationType#UPTO}
     *                             für Bis-Werkseinsatzdaten
     * @return
     */
    private List<iPartsWSPlantInformation> createPlantInformationDTO(EtkProject project, String productGroup, String seriesNumber,
                                                                     String codeCompareDate, iPartsFactoryData.AbstractDataForFactory dataForFactory,
                                                                     String factorySigns, PlantInformationType plantInformationType) {
        long date;
        String stCode;
        Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> idents;
        if ((plantInformationType == PlantInformationType.FROM) && dataForFactory.hasPEMFrom()) {
            date = dataForFactory.dateFrom;
            idents = dataForFactory.identsFrom;
            stCode = dataForFactory.stCodeFrom;
        } else if ((plantInformationType == PlantInformationType.UPTO) && dataForFactory.hasPEMTo()) {
            idents = dataForFactory.identsTo;
            date = dataForFactory.dateTo;
            stCode = dataForFactory.stCodeTo;
        } else {
            return EMPTY_PLANT_INFOS;
        }

        List<iPartsWSPlantInformation> plantInfos = EMPTY_PLANT_INFOS;
        if ((idents != null) && !idents.isEmpty()) { // (Mehrere) Werkseinsatzdaten mit Idents
            plantInfos = new DwList<>(idents.size());
            for (Map.Entry<iPartsFactoryData.IdentWithModelNumber, Set<String>> identEntry : idents.entrySet()) {

                iPartsWSPlantInformation plantInformation = new iPartsWSPlantInformation(project, plantInformationType);
                fillPlantInformationRMIValues(date, stCode, identEntry.getKey(), plantInformation);

                if (!iPartsWebservicePlugin.isRMIActive()) {
                    fillPlantInformationNonRMIValues(plantInformation, project, factorySigns, identEntry.getValue(),
                                                     productGroup, seriesNumber, codeCompareDate, identEntry.getKey().eldasType);
                }

                // Falls kein gültiger Inhalt gesetzt ist, keine Werkseinsatzdaten zurückliefern
                if (plantInformation.hasContent()) {
                    plantInfos.add(plantInformation);
                }
            }
        } else { // Werkseinsatzdaten ohne Ident
            iPartsWSPlantInformation plantInformation = new iPartsWSPlantInformation(project, plantInformationType);
            fillPlantInformationRMIValues(date, stCode, null, plantInformation);

            if (!iPartsWebservicePlugin.isRMIActive()) {
                fillPlantInformationNonRMIValues(plantInformation, project, factorySigns, null, productGroup,
                                                 seriesNumber, codeCompareDate, null);
            }

            boolean hasContentApartFromDate = StrUtils.isValid(plantInformation.getWmi());
            // Falls kein gültiger Inhalt oder nur Datum < 0 (also ungültig) gesetzt ist, keine Werkseinsatzdaten zurückliefern
            if (plantInformation.hasContent() && (hasContentApartFromDate || (date >= 0))) {
                plantInfos = new DwList<>(1);
                plantInfos.add(plantInformation);
            }
        }
        return plantInfos;
    }


    public static String convertDate(long date) throws RESTfulWebApplicationException {
        String isoDateWithoutTime = null;
        if (date > 0) {
            try {
                isoDateWithoutTime = DateUtils.toISO_yyyyMMdd_from_yyyyMMddHHmmss(String.valueOf(date));
            } catch (DateException e) {
                WSRequestTransferObject.throwRequestError(WSError.INTERNAL_ERROR, "Error in plant information date '"
                                                                                  + date + "': " + e.getMessage(), null);
            }
        }
        return isoDateWithoutTime;
    }

    private void fillPlantInformationRMIValues(long date, String stCode,
                                               iPartsFactoryData.IdentWithModelNumber identWithModelNumber,
                                               iPartsWSPlantInformation plantInformation) {
        String ident = null;
        String wmi = null;
        if (identWithModelNumber != null) {
            ident = identWithModelNumber.ident;
            if (!identWithModelNumber.eldasWMI.isEmpty()) {
                wmi = identWithModelNumber.eldasWMI;
            }

        }
        String isoDateWithoutTime = convertDate(date);
        String modelYear = null;
        if (StrUtils.isValid(stCode) && (iPartsModelYearCode.isModelYearCode(stCode))) {
            modelYear = stCode;
        }

        plantInformation.assignRMIValues(wmi, ident, isoDateWithoutTime, modelYear);
    }

    private void fillPlantInformationNonRMIValues(iPartsWSPlantInformation plantInformation, EtkProject project,
                                                  String factorySigns, Set<String> spikeIdents,
                                                  String productGroup, String seriesNumber, String codeCompareDate, String eldasType) {
        String plant = null;
        if (!StrUtils.isEmpty(factorySigns)) {
            plant = factorySigns;
        }
        List<String> exceptionIdents = null;
        if ((spikeIdents != null) && !spikeIdents.isEmpty()) {
            exceptionIdents = new DwList<>(spikeIdents);
        }
        List<iPartsWSCodeValidityDetail> modelYearValidityDetails = null;
        if (plantInformation.hasContent()) {
            modelYearValidityDetails = getCodeValidityDetails(plantInformation.getModelYear(), project, productGroup,
                                                              seriesNumber, codeCompareDate);
        }

        String madAggType = null;
        // Mapping nur bei vorhandenem EldasType - kein Fallback "" -> "F" von DA_AGGS_MAPPING (EldasType muss also
        // mindestens Länge 2 haben)
        if ((eldasType != null) && (eldasType.length() >= 2)) {
            String aggTypeFromEldasType = eldasType.substring(1);
            // NULL wenn Schlüssel nicht vorhanden; String-Resultat muss noch gemapped werden
            madAggType = iPartsAggTypeMappingCache.getInstance(project).getAggTypeMapping(aggTypeFromEldasType);
        }

        plantInformation.assignNonRMIValues(plant, modelYearValidityDetails, exceptionIdents, null, madAggType);
    }

    protected List<iPartsWSColorInfo> fillColorInfos(iPartsColorTable colorTables, EtkProject project, String productGroup,
                                                     String seriesNumber, String aggregateType, String codeCompareDate) {
        if ((seriesNumber != null) && !colorTables.getColorTablesMap().isEmpty()) {
            iPartsFactoryModel factoryModel = iPartsFactoryModel.getInstance(project);
            boolean isPrimusOrigin = false;
            List<iPartsWSColorInfo> colorInfos = new DwList<>();
            for (iPartsColorTable.ColorTable variantTable : colorTables.getColorTablesMap().values()) { // hier sollte laut Kommentar in Confluence nach der Filterung nur noch eine Tabelle übrig bleiben
                // Check, ob Datenquelle PRIMUS ist
                isPrimusOrigin = variantTable.colorTableToPartsMap.values().stream()
                        .anyMatch(colorTableToPart -> colorTableToPart.dataOrigin == iPartsImportDataOrigin.PRIMUS);

                for (iPartsColorTable.ColorTableContent variantsForVariantTable : variantTable.colorTableContents) {

                    iPartsWSColorInfo colorInfo = new iPartsWSColorInfo();
                    List<iPartsWSPlantInformation> plantInformation = getPlantInformationForColors(project, productGroup, seriesNumber, aggregateType,
                                                                                                   codeCompareDate, factoryModel, variantsForVariantTable.getFactoryData());

                    colorInfo.assignRMIValues(variantsForVariantTable.colorNumber, plantInformation);

                    if (!iPartsWebservicePlugin.isRMIActive()) {
                        String name = variantsForVariantTable.colorName.getTextByNearestLanguage(project.getDBLanguage(), project.getConfig().getDatabaseLanguages());
                        if (!StrUtils.isValid(name)) {
                            name = null;
                        }

                        // Code-Regeln der Ereignisse hinzufügen bei ereignisgesteuerten Baureihen
                        String codeValidity = ColorTableHelper.getColorTableCodeForSeries(project, variantsForVariantTable, seriesNumber);
                        List<iPartsWSCodeValidityDetail> codeValidityDetails = getCodeValidityDetails(codeValidity, project, productGroup, seriesNumber, codeCompareDate);
                        List<List<iPartsWSCodeMatrixElement>> codeValidityMatrix = getCodeValidityMatrix(codeValidity);

                        colorInfo.assignNonRMIValues(name, codeValidity, codeValidityDetails, codeValidityMatrix);
                    }

                    colorInfos.add(colorInfo);
                }
            }
            if (!colorInfos.isEmpty()) {
                // Nur bei PRIMUS Farbtabellen neu sortieren
                if (isPrimusOrigin) {
                    // Die Standardsortierung für den String Comparator ist [0-9;A-Z]
                    colorInfos.sort(Comparator.comparing(iPartsWSColorInfo::getEs2Key));
                }
                return colorInfos;
            }
        }
        return null;
    }

    private List<iPartsWSPlantInformation> getPlantInformationForColors(EtkProject project, String productGroup, String seriesNumber, String aggregateType, String codeCompareDate,
                                                                        iPartsFactoryModel factoryModel, iPartsColorFactoryDataForRetail factoryData) {
        if (factoryData == null) {
            return null;
        }

        List<iPartsWSPlantInformation> plantInformation = new DwList<>();
        for (Map.Entry<String, List<iPartsColorFactoryDataForRetail.DataForFactory>> factoryEntry : factoryData.getFactoryDataMap().entrySet()) {
            // Werkskennbuchstabe ermitteln aus Cache
            String factorySigns = factoryModel.getFactorySignsStringForFactoryNumberAndSeries(factoryEntry.getKey(),
                                                                                              new iPartsSeriesId(seriesNumber),
                                                                                              aggregateType);

            for (iPartsColorFactoryDataForRetail.DataForFactory dataForFactory : factoryEntry.getValue()) {
                // Aufteilen der Rückmeldedaten in von / bis
                plantInformation.addAll(createPlantInformationDTO(project, productGroup, seriesNumber, codeCompareDate,
                                                                  dataForFactory, factorySigns, PlantInformationType.FROM));
                plantInformation.addAll(createPlantInformationDTO(project, productGroup, seriesNumber, codeCompareDate,
                                                                  dataForFactory, factorySigns, PlantInformationType.UPTO));
            }
        }
        return plantInformation;
    }

    protected List<iPartsWSSaCode> fillSAValidity(EtkDataArray saaValidity, EtkProject project) {
        if (!saaValidity.isEmpty()) {
            String dbLanguage = project.getDBLanguage();
            Map<String, TreeSet<String>> saMapping = new TreeMap<>();
            iPartsNumberHelper helper = new iPartsNumberHelper();
            for (String saa : saaValidity.getArrayAsStringList()) {
                if (!helper.isValidSaa(saa)) {
                    continue;
                }
                // Zugehörige SA bestimmen
                String sa = StrUtils.copySubString(saa, 0, 7);

                // Daten in saMapping sammeln
                TreeSet<String> saaSet = saMapping.computeIfAbsent(sa, k -> new TreeSet<>());
                saaSet.add(saa);
            }

            if (!saMapping.isEmpty()) {
                List<iPartsWSSaCode> saCodes = new DwList<>(saMapping.size());

                // Aus saMapping die finalen Datenstrukturen mit Beschreibung erzeugen
                for (Map.Entry<String, TreeSet<String>> saEntry : saMapping.entrySet()) {
                    // Beschreibungen der SAA und der SA cachen
                    String saNumber = saEntry.getKey();
                    String saDescription = iPartsSA.getSaSaaDescription(project, saNumber, dbLanguage);

                    // SAAs
                    TreeSet<String> saaSet = saEntry.getValue();
                    List<iPartsWSSaaCode> saaCodes = new DwList<>(saaSet.size());
                    for (String saaNumber : saaSet) {
                        String saaDescription = iPartsSA.getSaSaaDescription(project, saaNumber, dbLanguage);

                        // Alle SAAs inkl. Beschreibung zur Liste hinzufügen
                        String formattedSaaNumber = iPartsNumberHelper.formatPartNo(project, saaNumber, dbLanguage);
                        saaCodes.add(new iPartsWSSaaCode(formattedSaaNumber, saaDescription));
                    }

                    // Die Liste der SAAs zur zugehörigen SA hinzufügen
                    String formattedSaNumber = iPartsNumberHelper.formatPartNo(project, saNumber, dbLanguage);
                    saCodes.add(new iPartsWSSaCode(formattedSaNumber, saDescription, saaCodes));
                }
                return saCodes;
            }
        }
        return null;
    }

    protected List<iPartsWSCodeValidityDetail> getCodeValidityDetails(String codeString, EtkProject project, String productGroup,
                                                                      String seriesNumber, String codeCompareDate) {
        List<iPartsWSCodeValidityDetail> codeValidityDetails = null;
        if ((codeString != null) && (seriesNumber != null) && (productGroup != null)) {
            if (!DaimlerCodes.isEmptyCodeString(codeString) && DaimlerCodes.syntaxIsOK(codeString)) {
                String dbLanguage = project.getDBLanguage();

                // Code String in einzelne Codes zerlegen
                // Zwischenspeichern als TreeSet, damit die einzelnen Code nur einmal vorhanden sind und sortiert werden
                Set<iPartsWSCodeValidityDetail> codes = new TreeSet<>(Comparator.comparing(iPartsWSCodeValidityDetail::getCode));
                for (String inputCode : DaimlerCodes.getCodeSet(codeString)) {
                    String codeCacheKey = (new iPartsCodeDataId(inputCode, seriesNumber, productGroup, codeCompareDate, iPartsImportDataOrigin.UNKNOWN)).toString("|")
                                          + "|" + project.getDBLanguage();
                    iPartsWSCodeValidityDetail code = codeCache.get(codeCacheKey);
                    if (code == null) {
                        iPartsDataCode dataCode = iPartsDataCodeList.getFittingDateTimeCodeWithAddSearch(project, dbLanguage, inputCode,
                                                                                                         seriesNumber, productGroup, codeCompareDate);

                        if (dataCode != null) {
                            code = new iPartsWSCodeValidityDetail(project, dataCode);
                            codeCache.put(codeCacheKey, code);
                        }
                    }

                    if (code != null) {
                        codes.add(code);
                    }
                }

                if (!codes.isEmpty()) {
                    codeValidityDetails = new DwList<>(codes.size());
                    codeValidityDetails.addAll(codes);
                }
            }
        }
        return codeValidityDetails;
    }
}