/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchpartswocontext;

import de.docware.apps.etk.base.config.EtkConfigConst;
import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkSectionInfo;
import de.docware.apps.etk.base.config.partlist.EtkSectionInfos;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.*;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.search.model.*;
import de.docware.apps.etk.base.webservice.endpoints.WSAbstractEndpoint;
import de.docware.apps.etk.base.webservice.endpoints.helper.WSHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AggregateDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DataCardRetrievalException;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsRetailPartSearch;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDatasetWithDBDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsCustomProperty;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSAdditionalPartInformationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSNavHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.*;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Endpoint für den SearchPartsWOContext-Webservice
 */
public class iPartsWSSearchPartsWOContextEndpoint extends iPartsWSAbstractEndpoint<iPartsWSSearchPartsWOContextRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/parts/SearchPartsWOContext";

    // Die Suchfelder dürfen wir fix annehmen
    private static final String MATNR_FIELD = EtkDbConst.FIELD_M_BESTNR;

    // Nur gefilterte laufende Nummern pro Baumuster und Modul speichern
    private final ObjectInstanceLRUList<String, Set<String>> filteredPartListsMapCache =
            new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_FILTERED_PART_LISTS,
                                        iPartsPlugin.getCachesLifeTime());

    public iPartsWSSearchPartsWOContextEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    public void clearCaches() {
        super.clearCaches();
        if (filteredPartListsMapCache != null) {
            filteredPartListsMapCache.clear();
        }
    }

    @Override
    protected RESTfulTransferObjectInterface executeWebservice(final EtkProject project, iPartsWSSearchPartsWOContextRequest requestObject) throws RESTfulWebApplicationException {
        iPartsWSUserInfo userInfo = requestObject.getUser();
        setCurrentDatabaseLanguageAndFallbackLanguages(project, userInfo);
        iPartsWSSearchPartsWOContextResponse response;

        if (requestObject.isSupplierNumber()) {
            response = searchPartNoToSupplierNumber(project, requestObject, userInfo);
        } else if (requestObject.isMasterData()) {
            response = searchPartMasterDataToPartNo(project, requestObject);
        } else {
            response = searchPartsWOContext(project, requestObject);
        }
        return response;
    }

    private iPartsWSSearchPartsWOContextResponse searchPartsWOContext(final EtkProject project, iPartsWSSearchPartsWOContextRequest requestObject) {
        iPartsProduct.setProductStructureWithAggregatesForSession(false);

        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        VarParam<String> resultSearchText = new VarParam<>();
        numberHelper.testPartNumber(requestObject.getSearchText(), resultSearchText, project);

        // SearchText bei Bedarf ändern
        if (!resultSearchText.getValue().equals(requestObject.getSearchText())) {
            requestObject.setSearchText(resultSearchText.getValue());
        }

        // Abfragefelder aufbauen
        EtkSearchModel searchModel = new EtkSearchModel(project);
        EtkDisplayFields searchFields = new EtkDisplayFields();
        EtkDisplayField searchField = new EtkDisplayField(TableAndFieldName.make(EtkDbConst.TABLE_MAT, MATNR_FIELD), false, false);
        searchFields.addFeld(searchField);

        searchModel.setSearchFields(searchFields);

        // Parameter Typkennzahl vorhanden -> Filter aktivieren, damit die Filterfelder von der Suche geladen werden
        final boolean deliverModelInfos = StrUtils.isValid(requestObject.getModelTypeId());
        final iPartsFilter filter = iPartsFilter.get();
        // Beim SearchPartsWOC WS soll die Konfiguration für die Ausgabe der freien SAs nicht einbezogen werden
        filter.setIgnoreLooseSaConfiguration(true);
        if (deliverModelInfos) {
            filter.setAllRetailFilterActiveForDataCard(project, null, true);
            filter.setAggModelsFilterActive(false);
        }

        // Suche im gesamten Teilekatalog
        NavigationPath path = new NavigationPath();
        path.add(new PartListEntryId(EtkConfigConst.ROOTKNOTEN, EtkConfigConst.ROOTKNOTENVER, ""));
        searchModel.setPath(path);

        // Zusätzliche benötigte Ergebnisfelder (z.B. für die Filter), die gleich im Select mit abgefragt werden sollen
        EtkSectionInfos resultFields = new EtkSectionInfos();
        Set<String> neededTables = new HashSet<>();
        neededTables.add(EtkDbConst.TABLE_KATALOG);
        for (String tableAndFieldName : iPartsFilter.get().getActiveFilterFields(neededTables)) {
            EtkDatabaseField dbField = project.getConfig().getDBDescription().getFieldByName(tableAndFieldName);
            if (dbField != null) {
                resultFields.addFeld(new EtkSectionInfo(tableAndFieldName, dbField.isMultiLanguage(), dbField.isArray()));
            }
        }

        if (deliverModelInfos) {
            filter.setAllRetailFilterActiveForDataCard(project, null, false);
        }

        // Felder für Wahlweise, virtuellen Materialtyp usw. müssen immer dabei sein
        resultFields.addFelder(iPartsDataAssembly.NEEDED_DISPLAY_FIELDS);
        searchModel.setGridResultFields(resultFields);

        WildCardSettings wildCardSettings = new WildCardSettings();
        searchModel.setWildCardSettings(wildCardSettings);

        // Keine Hierarchie-Prüfung in der Teilesuche, da sowieso im gesamten Teilekatalog gesucht wird und sämtliche
        // Filterung später Daimler-spezifisch stattfindet; lediglich optionale Einschränkung auf KG/TU bzw. EinPAS
        KgTuId optionalKgTuId = null;
        EinPasId optionalEinPasId = null;
        List<iPartsWSNavNode> navContext = requestObject.getNavContext();
        if ((navContext != null) && !navContext.isEmpty()) {
            optionalKgTuId = iPartsWSNavHelper.getKgTuId(navContext);
            if (optionalKgTuId == null) {
                optionalEinPasId = iPartsWSNavHelper.getEinPasId(navContext);

                // ID für KG/TU oder EinPAS muss nun vorhanden sein
                if (optionalEinPasId == null) {
                    throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid first navNodeType '" + navContext.get(0).getType()
                                                                          + "' for navContext", null);
                }
            }
        }

        final EtkPartsSearch search = new iPartsRetailPartSearch(searchModel, true, optionalKgTuId, optionalEinPasId,
                                                                 requestObject.getAggTypeId(), requestObject.isIncludeNavContext(),
                                                                 requestObject.isIncludeSAs(), false, null);

        // Eigentlichen Suchtext als whereValue
        List<String> whereValues = new ArrayList<>();
        whereValues.add(unformatSearchStringAndApplyWildcards(requestObject.getSearchText(), EtkDbConst.TABLE_MAT, MATNR_FIELD,
                                                              project, wildCardSettings));
        search.setSearchConstraints(searchFields, whereValues);

        // Response-Objekt
        iPartsWSSearchPartsWOContextResponse response = new iPartsWSSearchPartsWOContextResponse();
        List<iPartsWSPartResultWithIdent> iPartsWSPartResultList = new ArrayList<>();
        response.setSearchResults(iPartsWSPartResultList);

        boolean validSearch = search.hasSearchConstraints();
        if (validSearch) {
            // Listener registrieren und Suche als Thread starten
            String modelTypeId = requestObject.getModelTypeId();
            VarParam<Exception> searchException = new VarParam<>(null);
            final WebserviceSearchListener listener = new WebserviceSearchListener(project, requestObject, searchException);
            search.addEventHandler(listener);
            long startTime = System.currentTimeMillis();
            long timeoutInMilliSeconds = iPartsWebservicePlugin.getPluginConfig().getConfigValueAsInteger(iPartsWebservicePlugin.CONFIG_TIMEOUT_SEARCH_PARTS) * 1000L;
            FrameworkThread searchThread = Session.startChildThreadInSession(thread -> {
                try {
                    search.search(null);
                } catch (CanceledException e) {
                    // Falls die Suche bereits erfolgreich oder mit einem Fehler/Abbruch beendet wurde, eine CanceledException
                    // ignorieren (kann durch search.cancel() weiter unten sonst ausgelöst werden)
                    if ((listener.getState() != WebserviceSearchState.Finished) && (listener.getState() != WebserviceSearchState.Error)
                        && (listener.getState() != WebserviceSearchState.Canceled)) {
                        search.fireOnError(new RuntimeException(e));
                    }
                } catch (Exception e) {
                    search.fireOnError((e instanceof RuntimeException) ? (RuntimeException)e : new RuntimeException(e));
                } finally {
                    if (deliverModelInfos) {
                        // Filter wieder zurücksetzen, damit die Session wieder ohne Filter weiterverwendet werden kann (z.B. für die Unittests)
                        filter.setAllRetailFilterActiveForDataCard(project, null, false);
                    }
                }
            });
            searchThread.setName("iPartsWSSearchPartsWOContextEndpointSearchThread");

            // warten bis Suche fertig oder beendet wurde (State könnte im Listener geändert worden sein weil ausreichend Suchtreffer da)
            while ((listener.getState() == WebserviceSearchState.Running) || (listener.getState() == WebserviceSearchState.New)) {
                // Timeout überprüfen (kein Timeout bei <= 0)
                if ((timeoutInMilliSeconds > 0) && (System.currentTimeMillis() - startTime >= timeoutInMilliSeconds)) {
                    listener.cancel();
                    search.cancel();

                    // Es muss gewartet werden bis der Such-Thread sich beendet hat, da ansonsten die DB schon zu früh geschlossen
                    // werden könnte
                    searchThread.cancel();

                    Logger.log(iPartsWebservicePlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, getEndpointUri() + ": Timeout after "
                                                                                        + (timeoutInMilliSeconds / 1000)
                                                                                        + " seconds for webservice search request");
                    WSAbstractEndpoint.throwError(HttpConstants.HTTP_STATUS_REQUEST_TIMEOUT, WSError.REQUEST_TIMEOUT,
                                                  "Timeout after " + (timeoutInMilliSeconds / 1000) + " seconds for search request", null);
                    return null;
                }

                if (Java1_1_Utils.sleep(10)) {
                    throwProcessingError(WSError.INTERNAL_ERROR, "Search thread was interrupted", null);
                    return null;
                }
            }
            search.cancel();

            // Es muss gewartet werden bis der Such-Thread sich beendet hat, da ansonsten die DB schon zu früh geschlossen
            // werden könnte
            searchThread.cancel();

            // evtl. Exception aus der Suche auswerten
            if (searchException.getValue() != null) {
                throwProcessingError(WSError.INTERNAL_ERROR, searchException.getValue().getMessage(), null);
                return null;
            }

            Set<Map.Entry<String, Set<String>>> searchResult = listener.getPartNumberToModelTypesMap().entrySet();

            // prüfen ob das Suchergebnis leer ist
            boolean resultIsEmpty = true;
            for (Map.Entry<String, Set<String>> partNumberToModelTypesEntry : searchResult) {
                Set<String> modelTypes = partNumberToModelTypesEntry.getValue();
                if (!modelTypes.isEmpty()) {
                    resultIsEmpty = false;
                    break;
                }
            }

            // bei leerem Suchergebnis prüfen ob es einen Berechtigungsfehler gegeben hat
            if (resultIsEmpty && listener.permissionErrorDetected) {
                throwPermissionsError();
            }

            // Response erstellen
            for (Map.Entry<String, Set<String>> partNumberToModelTypesEntry : searchResult) {
                Set<String> modelTypes = partNumberToModelTypesEntry.getValue();
                if (modelTypes.isEmpty()) { // keine sichtbaren Typkennzahlen für dieses Teil -> ignorieren
                    continue;
                }

                iPartsWSPartResultWithIdent iPartsResultEntry = new iPartsWSPartResultWithIdent();

                // Teilenummer und Name setzen
                String matNr = partNumberToModelTypesEntry.getKey();
                iPartsResultEntry.setName(listener.getPartName(matNr));

                // Gültige Typkennzahlen und AS Produktklassen setzen
                List<iPartsWSModelTypeInfo> wsModelTypeInfos = new ArrayList<>(modelTypes.size());
                for (String modelType : modelTypes) {
                    iPartsWSModelTypeInfo wsModelTypeInfo = new iPartsWSModelTypeInfo();
                    wsModelTypeInfo.setModelTypeId(modelType);

                    // AS Produktklassen für die Typkennzahl
                    Collection<String> productClasses = listener.getProductClasses(modelType);
                    if (productClasses != null) {
                        wsModelTypeInfo.setProductClassIds(productClasses);
                    }

                    // Gültige Baumuster hinzufügen (nur bei vorhandemem Parameter Typkennzahl)
                    if (StrUtils.isValid(modelTypeId)) {
                        Collection<iPartsWSModelInfo> wsModelInfos = listener.getValidModels(matNr);
                        if (wsModelInfos == null) {
                            wsModelInfos = new ArrayList<>(0);
                        }
                        wsModelTypeInfo.setModels(wsModelInfos);
                    }

                    wsModelTypeInfos.add(wsModelTypeInfo);
                }
                iPartsResultEntry.setModelTypes(wsModelTypeInfos);

                // Ab DAIMLER-9716 werden QSL Sachnummer ohne SL ausgegeben
                iPartsResultEntry.setPartNo(iPartsNumberHelper.handleQSLPartNo(matNr));
                iPartsResultEntry.setPartNoFormatted(iPartsNumberHelper.formatPartNo(project, matNr, project.getDBLanguage()));

                iPartsWSPartResultList.add(iPartsResultEntry);
            }

            response.setMoreResults(listener.hasMoreResult());
        }
        return response;
    }

    private iPartsWSSearchPartsWOContextResponse searchPartNoToSupplierNumber(final EtkProject project,
                                                                              iPartsWSSearchPartsWOContextRequest requestObject,
                                                                              iPartsWSUserInfo userInfo) {
        String searchText = requestObject.getSearchText();
        iPartsDataSupplierPartNoMappingList supplierPartNoMappingList =
                iPartsDataSupplierPartNoMappingList.loadMappingForSupplierPartNo(project, searchText, false,
                                                                                 new String[]{ iPartsConst.FIELD_DSPM_SUPPLIER_PARTNO_PLAIN,
                                                                                               iPartsConst.FIELD_DSPM_PARTNO });
        iPartsWSSearchPartsWOContextResponse response = new iPartsWSSearchPartsWOContextResponse();
        List<iPartsWSPartResultWithIdent> iPartsWSPartResultList = new ArrayList<>();
        response.setSearchResults(iPartsWSPartResultList);

        Map<String, Boolean> partNoValidity = new HashMap<>();
        for (iPartsDataSupplierPartNoMapping supplierPartNoMapping : supplierPartNoMappingList) {
            String partNo = supplierPartNoMapping.getFieldValue(iPartsConst.FIELD_DSPM_PARTNO);

            // Für jede Teilenummer prüfen, ob es eine Verwendung in einem für das Token gültigen Produkt gibt
            Boolean validForPermissions = partNoValidity.get(partNo);
            if (validForPermissions == null) {
                validForPermissions = searchPartUsageWithProductValidity(project, partNo, userInfo);
                partNoValidity.put(partNo, validForPermissions);
            }

            // Nur Treffer für gültige Teilenummern werden in der Response ausgegeben
            if (validForPermissions) {
                String supplierPartNo = supplierPartNoMapping.getFieldValue(iPartsConst.FIELD_DSPM_SUPPLIER_PARTNO);
                String supplierName = supplierPartNoMapping.getFieldValue(iPartsConst.FIELD_DSPM_SUPPLIER_NAME);

                iPartsWSPartResultWithIdent iPartsResultEntry = new iPartsWSPartResultWithIdent();
                iPartsResultEntry.setPartNo(partNo);
                iPartsResultEntry.setPartNoFormatted(iPartsNumberHelper.formatPartNo(project, partNo, project.getDBLanguage()));
                iPartsResultEntry.setSupplierPartNo(supplierPartNo);
                iPartsResultEntry.setSupplierName(supplierName);

                PartId partId = new PartId(partNo, "");
                EtkDataPart part = EtkDataObjectFactory.createDataPart(project, partId);
                if (part.existsInDB()) {
                    String name = part.getFieldValue(iPartsConst.FIELD_M_TEXTNR, userInfo.getLanguage(), true);
                    iPartsResultEntry.setName(name);
                }
                iPartsWSPartResultList.add(iPartsResultEntry);
            }
        }
        return response;
    }

    private boolean searchPartUsageWithProductValidity(EtkProject project, String partNo, iPartsWSUserInfo userInfo) {
        if (!StrUtils.isValid(partNo) || (userInfo == null)) {
            return false;
        }
        // Suche in KATALOG mit Join auf DA_PRODUCT_MODULES um die relevanten Produkte zu ermitteln und diese dann gegen das Token checken
        String[] whereFields = new String[]{ iPartsConst.FIELD_K_MATNR };
        String[] whereValues = new String[]{ partNo };
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_DA_PRODUCT_MODULES, iPartsConst.FIELD_DPM_PRODUCT_NO, false, false));
        iPartsDataProductModulesList list = new iPartsDataProductModulesList();

        Set<String> productNumbers = new HashSet<>();
        EtkDataObjectList.FoundAttributesCallback callback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                productNumbers.add(attributes.getFieldValue(iPartsConst.FIELD_DPM_PRODUCT_NO));
                return false; // Ergebnisliste bleibt leer, es werden nur die Produktnummern gebraucht
            }

            @Override
            public boolean createDataObjects() {
                // Es müssen keine DataObjects erzeugt werden, weil nur die Produktnummern aus dem Callback verarbeitet werden
                return false;
            }
        };

        list.searchSortAndFillWithJoin(project, null, selectFields, whereFields, whereValues, false,
                                       null, false, false, true, callback,
                                       new EtkDataObjectList.JoinData(iPartsConst.TABLE_KATALOG,
                                                                      new String[]{ iPartsConst.FIELD_DPM_MODULE_NO },
                                                                      new String[]{ iPartsConst.FIELD_K_VARI },
                                                                      false, false));


        // Prüfen, ob mindestens eines der gefundenen Produkte für das Token gültig ist
        String countryForValidation = userInfo.getCountryForValidation();
        Map<String, Set<String>> permissionsAsMapForValidation = userInfo.getPermissionsAsMapForValidation();
        for (String productNo : productNumbers) {
            iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNo));
            if (product.isValidForPermissions(project, countryForValidation, permissionsAsMapForValidation)) {
                return true;
            }
        }
        return false;
    }

    private iPartsWSSearchPartsWOContextResponse searchPartMasterDataToPartNo(final EtkProject project,
                                                                              iPartsWSSearchPartsWOContextRequest requestObject) {
        iPartsWSSearchPartsWOContextResponse response = new iPartsWSSearchPartsWOContextResponse();
        response.setSearchResults(searchPartMasterDataToPartNo(project, requestObject.getSearchText()));
        return response;
    }

    public static List<iPartsWSPartResultWithIdent> searchPartMasterDataToPartNo(EtkProject project, String partNumber) {
        EtkDataPartList dataPartList = new EtkDataPartList();
        String[] whereFields = new String[1];
        String[] whereValues = new String[1];
        whereFields[0] = TableAndFieldName.make(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR);
        whereValues[0] = partNumber;
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR, true, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_ADDTEXT, true, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_WEIGHT, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_LENGTH, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_WIDTH, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_HEIGHT, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_VOLUME, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_HAZARDOUS_GOODS_INDICATOR, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_IMAGE_AVAILABLE, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_SECURITYSIGN_REPAIR, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_SPRACHE, iPartsConst.FIELD_S_SPRACH, false, false));
        selectFields.addFeld(new EtkDisplayField(iPartsConst.TABLE_SPRACHE, iPartsConst.FIELD_S_BENENN, true, false));

        Map<String, Set<iPartsWSLanguage>> matNrToLanguageDataMap = new HashMap<>();
        iPartsCustomProperty iPartsCustomPropertyCacheInstance = iPartsCustomProperty.getInstance(project);
        String dbLanguage = project.getDBLanguage();
        List<String> dbFallbackLanguages = project.getDataBaseFallbackLanguages();
        // Benennung in verschiedenen Sprachen zur Teilepos in einer Map speichern
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String matNr = attributes.getFieldValue(iPartsConst.FIELD_M_MATNR);
                Set<iPartsWSLanguage> languageList =
                        matNrToLanguageDataMap.computeIfAbsent(matNr, k -> new TreeSet<>(Comparator.comparing(iPartsWSLanguage::getLanguage)));
                // Wenn die Teilenummer in der Map schon vorhanden ist, wurde kein neues Attribut gefunden
                boolean foundAttribute = languageList.isEmpty();
                iPartsWSLanguage languageData = new iPartsWSLanguage();
                String desc = attributes.getFieldValue(iPartsConst.FIELD_S_BENENN);
                String language = attributes.getFieldValue(iPartsConst.FIELD_S_SPRACH);
                if (StrUtils.isValid(desc, language)) {
                    languageData.setDesc(desc);
                    languageData.setLanguage(language);
                    languageList.add(languageData);
                }
                return foundAttribute;
            }
        };

        dataPartList.searchSortAndFillWithJoin(project, null, selectFields, whereFields, whereValues, false,
                                               new String[]{ iPartsConst.FIELD_M_MATNR, iPartsConst.FIELD_S_SPRACH }, false, null,
                                               false, true, true, foundAttributesCallback, false,
                                               new EtkDataObjectList.JoinData(iPartsConst.TABLE_KATALOG,
                                                                              new String[]{ iPartsConst.FIELD_M_MATNR },
                                                                              new String[]{ iPartsConst.FIELD_K_MATNR },
                                                                              false, false),
                                               new EtkDataObjectList.JoinData(iPartsConst.TABLE_SPRACHE,
                                                                              new String[]{ iPartsConst.FIELD_M_TEXTNR },
                                                                              new String[]{ iPartsConst.FIELD_S_TEXTNR },
                                                                              true, false));
        List<iPartsWSPartResultWithIdent> iPartsWSPartResultList = new DwList<>(dataPartList.size());
        for (EtkDataPart dataPart : dataPartList) {
            iPartsWSPartResultWithIdent iPartsResultEntry = new iPartsWSPartResultWithIdent();
            String matNr = dataPart.getFieldValue(iPartsConst.FIELD_M_MATNR);
            EtkMultiSprache addText = project.getEtkDbs().getMultiLanguageByTextNr(TableAndFieldName.make(iPartsConst.TABLE_MAT,
                                                                                                          iPartsConst.FIELD_M_ADDTEXT),
                                                                                   dataPart.getFieldValue(iPartsConst.FIELD_M_ADDTEXT));
            iPartsResultEntry.setPartNo(matNr);
            iPartsResultEntry.setPartNoFormatted(iPartsNumberHelper.formatPartNo(project, matNr, dbLanguage));

            // Benennung
            EtkMultiSprache name = new EtkMultiSprache();
            if (!matNrToLanguageDataMap.isEmpty()) {
                Set<iPartsWSLanguage> languageList = matNrToLanguageDataMap.get(matNr);
                if (!languageList.isEmpty()) {
                    iPartsResultEntry.setLanguageData(languageList);
                    languageList.forEach(languageAndText -> name.setText(languageAndText.getLanguage(), languageAndText.getDesc()));
                }
            }

            iPartsResultEntry.setName(WSHelper.getNullForEmptyString(name.getText(dbLanguage)));

            iPartsResultEntry.setAddText(WSHelper.getNullForEmptyString(addText.getText(dbLanguage)));
            iPartsResultEntry.setImageAvailable(dataPart.getFieldValueAsBoolean(iPartsConst.FIELD_M_IMAGE_AVAILABLE));
            iPartsResultEntry.setSecuritySignRepair(dataPart.getFieldValueAsBoolean(iPartsConst.FIELD_M_SECURITYSIGN_REPAIR));

            // Höhe, Breite, Länge, Gewicht und Volumen aus der Material-Tabelle und Daten aus Custprop
            // werden hier unter einem DTO gespeichert
            List<iPartsWSAdditionalPartInformation> additionalPartInformations =
                    iPartsWSAdditionalPartInformationHelper.fillAdditionalPartInformation(project, dataPart, matNr,
                                                                                          dbLanguage, dbFallbackLanguages,
                                                                                          iPartsCustomPropertyCacheInstance);
            if (!additionalPartInformations.isEmpty()) {
                iPartsResultEntry.setAdditionalPartInformation(additionalPartInformations);
            }
            iPartsWSPartResultList.add(iPartsResultEntry);
        }
        return iPartsWSPartResultList;
    }

    /**
     * Eingabestring normalisieren
     *
     * @param s
     * @param tableName
     * @param fieldName
     * @param project
     * @param wildCardSettings
     * @return
     */
    private String unformatSearchStringAndApplyWildcards(String s, String tableName, String fieldName, EtkProject project,
                                                         WildCardSettings wildCardSettings) {
        //für Suche nach formatierten Strings: Visualierten Wert in DB-Wert rückwandeln (bestimmte Zeichen entfernen)

        // aktuell auskommentiert, da dies auf die Suche im Materialtext negativen Einfluss haben könnte (wird außerdem
        // derzeit auch gar nicht gefordert)
//        s = project.getVisObject().getDatabaseValueOfVisValue(tableName, fieldName, s, project.getDBLanguage());

        s = wildCardSettings.makeWildCard(s);

        return s.trim();
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if ((dataType == iPartsDataChangedEventByEdit.DataType.PRODUCT) || (dataType == iPartsDataChangedEventByEdit.DataType.SERIES)
            || (dataType == iPartsDataChangedEventByEdit.DataType.MODEL) || (dataType == iPartsDataChangedEventByEdit.DataType.MATERIAL)
            || (dataType == iPartsDataChangedEventByEdit.DataType.PART_LIST) || (dataType == iPartsDataChangedEventByEdit.DataType.SA)) {
            clearCaches();
        }
    }


    protected enum WebserviceSearchState {New, Running, Finished, Error, Canceled}


    /**
     * Mechanismus aus JavaviewerWebservice abgeschaut
     */
    protected class WebserviceSearchListener implements SearchEvents {

        private final EtkProject project;
        private final String language;
        private final iPartsFilter filter;
        boolean onlyRetailRelevantProducts;

        // Requestparameter
        private final String modelTypeId;
        private final List<String> productClassIds;
        private final String aggTypeId;
        private final boolean includeNavContext;

        private final VarParam<Exception> searchException;

        private final int maxResults = iPartsWebservicePlugin.getPluginConfig().getConfigValueAsInteger(iPartsWebservicePlugin.CONFIG_MAX_RESULTS_SEARCH_PARTS);

        private int resultCount;
        private final Map<String, Set<String>> partNumberToModelTypesMap = new TreeMap<>();
        private final Map<String, String> partNumberToNamesMap = new HashMap<>();
        private final Map<String, Set<String>> modelTypeToProductClassesMap = new HashMap<>();
        private Map<String, Map<String, iPartsWSModelInfo>> partNumberToModelsMap;
        private volatile WebserviceSearchState state = WebserviceSearchState.New;
        private boolean moreResults;
        private final boolean includeSAs;
        private final boolean includeModel;
        private final iPartsWSUserInfo userInfo;
        private boolean permissionErrorDetected;

        public WebserviceSearchListener(EtkProject project, iPartsWSSearchPartsWOContextRequest requestObject,
                                        VarParam<Exception> searchException) {
            this.project = project;
            this.language = project.getDBLanguage();
            filter = iPartsFilter.get();
            onlyRetailRelevantProducts = iPartsWebservicePlugin.isOnlyRetailRelevantProducts();

            this.modelTypeId = requestObject.getModelTypeId();
            this.productClassIds = requestObject.getProductClassIds();
            this.aggTypeId = requestObject.getAggTypeId();
            this.includeNavContext = requestObject.isIncludeNavContext();
            this.searchException = searchException;
            this.includeModel = requestObject.isIncludeModel();
            this.includeSAs = requestObject.isIncludeSAs();
            this.userInfo = requestObject.getUser();

            // Parameter Typkennzahl vorhanden -> gültige Baumuster müssen bestimmt werden
            if (StrUtils.isValid(modelTypeId)) {
                partNumberToModelsMap = new HashMap<>();
            }
        }

        /**
         * Neuer Suchtreffer eingetroffen
         *
         * @param searchResult
         */
        @Override
        public void OnGetResult(EtkSearchBaseResult searchResult) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            if (state == WebserviceSearchState.New) {
                state = WebserviceSearchState.Running;
            }
            if (limitResult(maxResults) && (resultCount >= maxResults)) {
                moreResults = true;
                if (state == WebserviceSearchState.Running) {
                    state = WebserviceSearchState.Finished; // Abbruch signalisieren
                    Logger.log(iPartsWebservicePlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, getEndpointUri() + ": Webservice search thread aborted due to max results "
                                                                                        + maxResults + " exceeded");
                }
            } else {
                searchResult.load();

                EtkDataPartListEntry partListEntry = ((EtkPartResult)searchResult).getEntry();

                // Gültige Typkennzahlen, Teilebenennungen und AS Produktklassen bestimmen
//                String description = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT, language, true);
//                // description ist optional; wenn leer weglassen
//                if (!description.isEmpty()) {
//                    iPartsResultEntry.setDescription(description);
//                }

                // Daten aus Material
                EtkDataPart part = partListEntry.getPart();
                String matNr = part.getFieldValue(MATNR_FIELD);
                if (!partNumberToNamesMap.containsKey(matNr)) {
                    partNumberToNamesMap.put(matNr, part.getFieldValue(EtkDbConst.FIELD_M_TEXTNR, language, true));
                }

                Set<String> modelTypes = partNumberToModelTypesMap.computeIfAbsent(matNr, k -> new TreeSet<>());

                // Typkennzahlen und AS Produktklassen für das Produkt bestimmen, in dem sich das Modul befindet
                String productNumber = "";
                iPartsDataProductSAs dataProductSAs = null;
                iPartsDataModuleEinPAS dataModuleEinPAS = null;
                // Daten aus DA_MODULES_EINPAS
                if (includeModel) {
                    dataModuleEinPAS = iPartsSearchVirtualDatasetWithDBDataset.getResultDataModuleEinPAS(partListEntry);
                    if (dataModuleEinPAS != null) { // Treffer im Produkt über normale Verortung
                        productNumber = dataModuleEinPAS.getAsId().getProductNumber();
                    }
                }
                if (StrUtils.isEmpty(productNumber) && includeSAs) {
                    // Daten aus DA_PRODUCT_SAS
                    dataProductSAs = iPartsSearchVirtualDatasetWithDBDataset.getResultDataProductSAs(partListEntry);
                    if (dataProductSAs != null) { // Treffer in einer SA
                        productNumber = dataProductSAs.getAsId().getProductNumber();
                    }
                }

                if (StrUtils.isEmpty(productNumber)) {
                    // kein Produkt bestimmbar
                    return;
                } else {
                    iPartsProductId productId = new iPartsProductId(productNumber);
                    iPartsProduct product = iPartsProduct.getInstance(project, productId);
                    // nur retail-relevante bzw. gültige (JWT Token Gültigkeitsprüfung) Produkte berücksichtigen
                    if (onlyRetailRelevantProducts && !product.isRetailRelevant()) {
                        // Produkt ist nicht retail relevant, es sollen aber nur retail relevante Produkte ausgegeben werden
                        return;
                    }

                    // Token Gültigkeitsprüfung
                    boolean validForPermissions = product.isValidForPermissions(project, userInfo.getCountryForValidation(), userInfo.getPermissionsAsMapForValidation());
                    permissionErrorDetected |= !validForPermissions;
                    if (validForPermissions) {
                        // Filter für die AS Produktklasse
                        if (!product.isOneProductClassValid(productClassIds)) {
                            return; // AS Produktklasse passt nicht
                        }

                        // Filter für den Aggregatetyp
                        boolean aggTypeIdValid = StrUtils.isValid(aggTypeId);
                        if (aggTypeIdValid && !product.getAggregateType().equals(aggTypeId)) {
                            return; // Aggregatetyp passt nicht
                        }

                        // Filter für die Typkennzahl
                        boolean checkModelVisibility = iPartsPlugin.isCheckModelVisibility();
                        Set<String> modelTypesForProduct = checkModelVisibility ? product.getVisibleModelTypes(project) : product.getAllModelTypes(project);
                        boolean deliverModelInfos = StrUtils.isValid(modelTypeId);
                        if (deliverModelInfos) {
                            if (modelTypesForProduct.contains(modelTypeId)) {
                                modelTypesForProduct = new TreeSet<>();
                                modelTypesForProduct.add(modelTypeId); // nur die gesuchte Typkennzahl hinzufügen

                                EtkDataAssembly assembly = null;

                                // Filterung pro Baumuster durchführen, um gültige Baumuster zu bestimmen
                                Set<String> modelNumbers = checkModelVisibility ? product.getVisibleModelNumbers(project) : product.getModelNumbers(project);
                                for (String modelNumber : modelNumbers) {
                                    if (Thread.currentThread().isInterrupted()) {
                                        return;
                                    }

                                    // nochmals die Typkennzahl überprüfen (ein Produkt kann mehrere Typkennzahlen und damit
                                    // auch mehrere Baumuster mit unterschiedlichen Typkennzahlen haben) und sicherstellen,
                                    // dass das Baumuster auch retail-relevant ist
                                    iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(modelNumber));
                                    if ((!aggTypeIdValid || model.getAggregateType().equals(aggTypeId))
                                        && model.getModelTypeNumber().equals(modelTypeId)) {
                                        // Maximale Anzahl Suchtreffer überprüfen und Baumusterfilterung bei Bedarf hier abbrechen
                                        if (limitResult(maxResults) && (resultCount >= maxResults)) {
                                            moreResults = true;
                                            break;
                                        }

                                        // Zunächst die nach dem aktuellen Baumuster gefilterte Stückliste im Cache suchen
                                        String filterPartListEntriesKey = modelNumber + "|" + partListEntry.getOwnerAssemblyId().toString("|");
                                        Set<String> filteredPartListEntries = filteredPartListsMapCache.get(filterPartListEntriesKey);

                                        if (filteredPartListEntries == null) { // Keinen Eintrag im Cache gefunden
                                            if (assembly == null) {
                                                // Modul laden
                                                assembly = EtkDataObjectFactory.createDataAssembly(project, partListEntry.getOwnerAssemblyId());
                                            }

                                            assembly.clearFilteredPartLists(); // zunächst gefilterte Stücklisten zurücksetzen

                                            // Aus dem Baumuster eine Baumuster-Datenkarte erzeugen und im Filter setzen
                                            try {
                                                AbstractDataCard dataCard = null;
                                                if (modelNumber.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_CAR)) {
                                                    dataCard = VehicleDataCard.getVehicleDataCard(modelNumber, false, false, true, null, project, false);
                                                } else if (modelNumber.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE)) {
                                                    dataCard = AggregateDataCard.getAggregateDataCard(null, modelNumber, false, true, null, project);
                                                }
                                                if ((dataCard == null) || !dataCard.isModelLoaded()) { // Datenkarte ist ungültig (weil Baumuster ungültig war)
                                                    continue;
                                                }

                                                // Filter setzen und aktivieren
                                                filter.setAllRetailFilterActiveForDataCard(project, dataCard, true);
                                                filter.setAggModelsFilterActive(false);
                                            } catch (DataCardRetrievalException e) {
                                                // Kann nicht auftreten, da wir hier keine Datenkarten laden
                                            }

                                            // Neuen Cache-Eintrag für die gefilterte Stückliste erzeugen (nur laufende Nummern speichern)
                                            List<EtkDataPartListEntry> partList = assembly.getPartList(null);
                                            filteredPartListEntries = new HashSet<>(partList.size());
                                            for (EtkDataPartListEntry partListEntryFromAssembly : partList) {
                                                filteredPartListEntries.add(partListEntryFromAssembly.getAsId().getKLfdnr());
                                            }
                                            filteredPartListsMapCache.put(filterPartListEntriesKey, filteredPartListEntries);
                                        }

                                        // Gefundenen Stücklisteneintrag über die laufende Nummer in der gefilterten Stückliste suchen
                                        if (filteredPartListEntries.contains(partListEntry.getAsId().getKLfdnr())) {
                                            // Gefilterte Stückliste enthält den Stücklisteneintrag -> Baumuster ist gültig
                                            Map<String, iPartsWSModelInfo> modelInfosMap = partNumberToModelsMap.computeIfAbsent(matNr, k -> new TreeMap<>());

                                            String modelInfoKey = modelNumber + "@" + productNumber;
                                            iPartsWSModelInfo wsModelInfo = modelInfosMap.get(modelInfoKey);
                                            if (wsModelInfo == null) {
                                                // ModelInfo erzeugen
                                                wsModelInfo = new iPartsWSModelInfo();
                                                wsModelInfo.setModelId(modelNumber);
                                                wsModelInfo.setAggTypeId(model.getAggregateType());
                                                wsModelInfo.setProductClassIds(product.getAsProductClasses());
                                                wsModelInfo.setProductId(productNumber);
                                                modelInfosMap.put(modelInfoKey, wsModelInfo);

                                                // neues Baumuster im Produkt -> Anzahl gefundener Baumuster ist Gesamttrefferanzahl
                                                // ohne includeNavContext
                                                if (!includeNavContext) {
                                                    resultCount++;
                                                }
                                            }

                                            // navNodesList bestimmen
                                            if (includeNavContext) {
                                                iPartsProductStructures productStructure = iPartsProductStructures.getInstance(project, productId);
                                                List<iPartsWSNavNode> navContext = null;
                                                if ((dataModuleEinPAS != null) && includeModel) {
                                                    navContext = iPartsWSNavHelper.createNavContext(product, productStructure,
                                                                                                    dataModuleEinPAS,
                                                                                                    language, project);
                                                } else if ((dataProductSAs != null) && includeSAs) {
                                                    navContext = iPartsWSNavHelper.createNavContext(product, productStructure,
                                                                                                    dataProductSAs,
                                                                                                    partListEntry.getOwnerAssemblyId(),
                                                                                                    language, project);
                                                }
                                                if (navContext != null) {
                                                    // navContext zur navNodesList hinzufügen bzw. diese bei Bedarf noch erzeugen
                                                    Collection<Collection<iPartsWSNavNode>> navNodesList = wsModelInfo.getNavNodesList();
                                                    if (navNodesList == null) {
                                                        // TreeSet, um mehrere Treffer pro Stückliste zu einem NavContext zusammenzufassen
                                                        // und die Treffer nach Verortung zu sortieren
                                                        navNodesList = new TreeSet<>(Comparator.comparing(iPartsWSNavHelper::getErrorStringForNavNodes));
                                                        wsModelInfo.setNavNodesList(navNodesList);
                                                    }

                                                    if (navNodesList.add(navContext)) {
                                                        // neuer NavContext -> Anzahl gefundener NavContexte ist Gesamttrefferanzahl
                                                        // mit includeNavContext
                                                        resultCount++;
                                                    }
                                                }
                                            }
                                        }

                                        // Baumuster im Filter wieder zurücksetzen, damit folgende Suchergebnisse nicht bei der unterliegende Suche ausgefiltert werden
                                        // Hinweis: dieser Event-Handler läuft synchron mit der Suche. Beide Filterungen kommen sich also nicht in die Quere
                                        filter.setAllRetailFilterActiveForDataCard(project, null, false);
                                    }
                                }
                            } else {
                                return; // Typkennzahl passt nicht
                            }
                        } else { // Anzahl Typkennzahlen ergeben Gesamttrefferanzahl, nicht die Anzahl NavContexte
                            resultCount -= modelTypes.size(); // alte Anzahl der Typkennzahlen von der Gesamttrefferanzahl zunächst abziehen
                        }
                        modelTypes.addAll(modelTypesForProduct);
                        if (!deliverModelInfos) { // Anzahl Typkennzahlen ergeben Gesamttrefferanzahl, nicht die Anzahl NavContexte
                            resultCount += modelTypes.size(); // neue Anzahl der Typkennzahlen zur Gesamttrefferanzahl hinzufügen
                        }
                        for (String modelType : modelTypesForProduct) {
                            Set<String> productClasses = modelTypeToProductClassesMap.computeIfAbsent(modelType, k -> new TreeSet<>());
                            productClasses.addAll(product.getAsProductClasses());
                        }
                    }
                }
            }

            // Falls zwischendrin moreResults auf true gesetzt wurde, jetzt die Suche abbrechen
            if (moreResults && (state == WebserviceSearchState.Running)) {
                state = WebserviceSearchState.Finished; // Abbruch signalisieren
                Logger.log(iPartsWebservicePlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, getEndpointUri() + ": Webservice search thread aborted due to max results "
                                                                                    + maxResults + " exceeded");
            }
        }

        @Override
        public void OnFinish(EtkBaseSearch sender) {
            if ((state == WebserviceSearchState.New) || (state == WebserviceSearchState.Running)) {
                state = WebserviceSearchState.Finished;
                Logger.log(iPartsWebservicePlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, getEndpointUri() + ": Webservice search finished with "
                                                                                    + resultCount + " results");
            }
        }

        @Override
        public void OnError(RuntimeException e) {
            searchException.setValue(e);
            state = WebserviceSearchState.Error;
            Logger.log(iPartsWebservicePlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, getEndpointUri() + ": Webservice search stopped on error: "
                                                                                + e.getMessage());
            Logger.logExceptionWithoutThrowing(iPartsWebservicePlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
        }

        @Override
        public void OnStart(EtkBaseSearch sender) {
            OnClear();
            state = WebserviceSearchState.Running;
        }

        @Override
        public void OnClear() {
            resultCount = 0;
            partNumberToModelTypesMap.clear();
            partNumberToNamesMap.clear();
            modelTypeToProductClassesMap.clear();
            moreResults = false;
        }

        boolean limitResult(final int maxResults) {
            return maxResults > 0;
        }

        public Map<String, Set<String>> getPartNumberToModelTypesMap() {
            return partNumberToModelTypesMap;
        }

        public String getPartName(String partNumber) {
            return partNumberToNamesMap.get(partNumber);
        }

        public Set<String> getProductClasses(String modelType) {
            return modelTypeToProductClassesMap.get(modelType);
        }

        public Collection<iPartsWSModelInfo> getValidModels(String partNumber) {
            if (partNumberToModelsMap != null) {
                Map<String, iPartsWSModelInfo> modelInfosMap = partNumberToModelsMap.get(partNumber);
                if (modelInfosMap != null) {
                    return modelInfosMap.values();
                }
            }
            return null;
        }

        public WebserviceSearchState getState() {
            return state;
        }

        public boolean hasMoreResult() {
            return moreResults;
        }

        public void cancel() {
            state = WebserviceSearchState.Canceled;
        }
    }
}