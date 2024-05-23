/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkSectionInfo;
import de.docware.apps.etk.base.config.partlist.EtkSectionInfos;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.search.model.*;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObjectInterface;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDatasetWithDBDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsColorTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchparts.iPartsWSSearchPartsCallback;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSIdentContextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSNavHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartResult;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.utils.VarParam;
import de.docware.util.CanceledException;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Abstrakter Endpoint für die meisten Such-Webservices in iParts
 */
public abstract class iPartsWSAbstractSearchPartsEndpoint<REQUEST_PARTS extends WSRequestTransferObjectInterface> extends iPartsWSAbstractEndpoint<REQUEST_PARTS> {

    // Begrenzung der Suchergebnisse
    public static int DEFAULT_MAX_RESULTS = -1; // Default für Konfiguration ist -1 (keine Begrenzung)

    // Timeout
    public static int DEFAULT_TIMEOUT = 60; // Default für Konfiguration is 60 Sekunden

    // Die Suchfelder dürfen wir fix annehmen
    protected static final String MATNR_FIELD = EtkDbConst.FIELD_M_BESTNR;
    protected static final String BENENN_FIELD = EtkDbConst.FIELD_M_TEXTNR;

    private int maxResults = -1; // -1 bedeuetet: Wert aus der Konfiguration verwenden

    public iPartsWSAbstractSearchPartsEndpoint(String endpointUri) {
        super(endpointUri);
    }

    /**
     * Maximale Anzahl Ergebnisse
     *
     * @return {@code -1} für den Wert aus der Admin-Konfiguration
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Maximale Anzahl Ergebnisse
     *
     * @param maxResults {@code -1} für den Wert aus der Admin-Konfiguration
     */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    protected LogChannels getLogChannelDebugForSearchListener() {
        return logChannelDebug;
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
    protected String unformatSearchStringAndApplyWildcards(String s, String tableName, String fieldName, EtkProject project, WildCardSettings wildCardSettings) {
        //für Suche nach formatierten Strings: Visualierten Wert in DB-Wert rückwandeln (bestimmte Zeichen entfernen)

        // aktuell auskommentiert, da dies auf die Suche im Materialtext negativen Einfluss haben könnte (wird außerdem
        // derzeit auch gar nicht gefordert)
//        s = project.getVisObject().getDatabaseValueOfVisValue(tableName, fieldName, s, project.getDBLanguage());

        s = wildCardSettings.makeWildCard(s);

        return s.trim(); // wichtig wg. JAVA_VIEWER-3430
    }

    protected enum WebserviceSearchState {New, Running, Finished, Error, Canceled}

    protected String getMatNrFieldName(iPartsEqualPartType equalPartType) {
        if (equalPartType == iPartsEqualPartType.MB) {
            return iPartsConst.FIELD_M_MATNR_MBAG;
        } else if (equalPartType == iPartsEqualPartType.DT) {
            return iPartsConst.FIELD_M_MATNR_DTAG;
        }
        return MATNR_FIELD;
    }

    protected EtkDisplayFields setupSearchFields(boolean isPartNo, iPartsEqualPartType equalPartType) {
        EtkDisplayFields searchFields = new EtkDisplayFields();
        EtkDisplayField searchField;

        String matNrFieldName = getMatNrFieldName(equalPartType);
        searchField = new EtkDisplayField(TableAndFieldName.make(EtkDbConst.TABLE_MAT, matNrFieldName), false, false);
        searchFields.addFeld(searchField);

        if (!isPartNo) {
            searchField = new EtkDisplayField(TableAndFieldName.make(EtkDbConst.TABLE_MAT, BENENN_FIELD), true, false);
            searchFields.addFeld(searchField);
        }
        return searchFields;
    }

    protected EtkSearchModel setupSearchModel(EtkProject project, iPartsProduct product, List<iPartsWSNavNode> navContext,
                                              boolean isPartNo, iPartsEqualPartType equalPartType) {
        // Abfragefelder aufbauen
        EtkSearchModel searchModel = new EtkSearchModel(project);
        searchModel.setSearchFields(setupSearchFields(isPartNo, equalPartType));

        String k_vari = iPartsWSNavHelper.getVirtualIdString(product, navContext, project);
        PartListEntryId productPlusNavigationAssyId = new PartListEntryId(k_vari, "", "");

        NavigationPath path = new NavigationPath();
        path.add(productPlusNavigationAssyId);
        searchModel.setPath(path);

        WildCardSettings wildCardSettings = new WildCardSettings();
        searchModel.setWildCardSettings(wildCardSettings);

        return searchModel;
    }

    protected EtkSectionInfos setupResultFields(EtkProject project) {
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

        // Felder für Wahlweise, virtuellen Materialtyp usw. müssen immer dabei sein
        resultFields.addFelder(iPartsDataAssembly.NEEDED_DISPLAY_FIELDS);

        // M_IMAGE_AVAILABLE wird im Suchergebnis benötigt
        resultFields.addFeld(new EtkSectionInfo(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_IMAGE_AVAILABLE),
                                                false, false));
        return resultFields;
    }

    protected Collection<EtkSearchBaseResult> executeSearch(final EtkPartsSearch search, String searchText, VarParam<Boolean> hasMoreResults,
                                                            EtkProject project) {
        /**
         * Einschränkung der Suche über Produkt und ggf. navContext. Dies entspricht der Einschränkung bei der interaktiven Suche über
         * den "Suche in..." Dialog. Die Produkteinschränkung ist immer da. navContext ist optional.
         * Bei der interaktiven Suche wird in der EtkSearchModel dadurch ein Pfad gesetzt, der den kompletten Assembly-Knotenpfad von
         * Root bis zum gewählten Knoten enthält. Für die Gültigkeitsprüfung der Treffer wird aber nur der letzte Knoten ausgewertet.
         * Daher setzen wir auch hier nur diesen letzen Knoten (wegen der virtuellen Navigation enthält dieser aber die Information über
         * Produkt und ggf. Navigationspfad in einer AssemblyId zusammengefasst.).
         * Sollte sich später herausstellen, dass man doch einen kompletten Pfad benötigt, so hätte man hier die Informationen zur Pfadbildung
         * ab Produktknoten zur Verfügung. Die drüberliegenden Strukturknoten stehen hier noch nicht unmittelbar zur Verfügung.
         */

        // Eigentliche Suchtext als whereValue
        List<String> whereValues = new ArrayList<String>();
        whereValues.add(unformatSearchStringAndApplyWildcards(searchText, EtkDbConst.TABLE_MAT, MATNR_FIELD, project,
                                                              search.getModel().getWildCardSettings()));
        search.setSearchConstraints(search.getModel().getSearchFields(), whereValues);

        boolean validSearch = search.hasSearchConstraints();
        if (validSearch) {
            // Listener registrieren und Suche als Thread starten
            final VarParam<Exception> searchException = new VarParam<Exception>(null);
            final WebserviceSearchListener listener = new WebserviceSearchListener(getLogChannelDebugForSearchListener(), searchException);
            search.addEventHandler(listener);
            long startTime = System.currentTimeMillis();
            long timeoutInMilliSeconds = iPartsWebservicePlugin.getPluginConfig().getConfigValueAsInteger(iPartsWebservicePlugin.CONFIG_TIMEOUT_SEARCH_PARTS) * 1000;
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
                }
            });
            searchThread.setName("iPartsWSSearchPartsEndpointSearchThread");

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
                    throwError(HttpConstants.HTTP_STATUS_REQUEST_TIMEOUT, WSError.REQUEST_TIMEOUT,
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
                throwProcessingError(WSError.INTERNAL_ERROR, Utils.exceptionToString(searchException.getValue()), null);
                return null;
            }
            hasMoreResults.setValue(listener.hasMoreResult());
            return listener.getResults().values();
        }
        return null;
    }

    protected List<iPartsWSPartResult> createResultList(Collection<EtkSearchBaseResult> searchResults, iPartsWSUserInfo userInfo,
                                                        EtkProject project, iPartsProduct product, iPartsProductStructures productStructures,
                                                        boolean includeES2Keys, iPartsEqualPartType equalPartType,
                                                        iPartsWSSearchPartsCallback searchCallback) {
        String matNrFieldName = getMatNrFieldName(equalPartType);
        List<iPartsWSPartResult> iPartsWSPartResultList = new DwList<>();
        boolean permissionErrorDetected = false;
        // Response erstellen
        for (EtkSearchBaseResult searchBaseResultEntry : searchResults) {
            if (!(searchBaseResultEntry instanceof EtkPartResult)) {
                continue;
            }
            iPartsWSPartResult iPartsResultEntry = new iPartsWSPartResult();

            // Daten aus Stücklisteneintrag
            EtkPartResult partResultEntry = (EtkPartResult)searchBaseResultEntry;
            EtkDataPartListEntry partListEntry = partResultEntry.getEntry();

            // ggf. Daten aus Produkt für Aggregatetreffer, falls Treffer gültig
            iPartsDataProduct entryDataProduct = iPartsSearchVirtualDatasetWithDBDataset.getResultDataProduct(partListEntry);
            if ((entryDataProduct != null) && !entryDataProduct.getAsId().getProductNumber().equals(product.getAsId().getProductNumber())) {
                // Treffer kommt aus Aggregat
                iPartsResultEntry.setAggProductId(entryDataProduct.getAsId().getProductNumber());
                iPartsResultEntry.setAggTypeId(entryDataProduct.getAttribute(iPartsConst.FIELD_DP_AGGREGATE_TYPE).getAsString());

                iPartsProduct entryProduct = iPartsProduct.getInstance(project, entryDataProduct.getAsId());
                // Token Gültigkeitsprüfung
                VarParam<Boolean> permissionError = new VarParam<>(false);
                boolean isValidForPermissions = iPartsWSIdentContextHelper.isPermittedAggregateProduct(entryProduct, product.getAsProductClasses(),
                                                                                                       userInfo, project, permissionError);
                permissionErrorDetected |= permissionError.getValue();
                if (!isValidForPermissions) {
                    continue;
                }
            }

            // Jetzt erst den kombinierten Text bestimmen, um unnötige DB-Abfragen zu vermeiden
            String description = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT, userInfo.getLanguage(), true);
            // description ist optional; wenn leer weglassen
            if (!description.isEmpty()) {
                iPartsResultEntry.setDescription(description);
            }

            iPartsWSPartResultList.add(iPartsResultEntry);

            if (partListEntry instanceof iPartsDataPartListEntry) {
                iPartsDataPartListEntry iPartsPLE = (iPartsDataPartListEntry)partListEntry;
                if (iPartsPLE.isSpecialProductEntry()) {
                    List<String> assortmentClassIdsEnums = partListEntry.getFieldValueAsSetOfEnum(iPartsConst.FIELD_K_PCLASSES_VALIDITY);
                    if (!assortmentClassIdsEnums.isEmpty()) {
                        iPartsResultEntry.setAssortmentClassIds(assortmentClassIdsEnums);
                    }
                }

                // ES2Keys zur Fahrzeugvalidierung, nur wenn Flag "includeES2Keys" im Request gesetzt wurde
                if (includeES2Keys) {
                    iPartsColorTable allColorTable = iPartsPLE.getColorTableForRetail();
                    if (allColorTable != null) {
                        Set<String> es2Keys = new TreeSet<>();
                        for (iPartsColorTable.ColorTable colorTable : allColorTable.getColorTablesMap().values()) {
                            colorTable.colorTableContents
                                    .stream()
                                    .map(colorTableContent -> colorTableContent.colorNumber)
                                    .forEach(colorNumber -> es2Keys.add(colorNumber));
                        }
                        if (!es2Keys.isEmpty()) {
                            iPartsResultEntry.setEs2Keys(new ArrayList<>(es2Keys));
                        }
                    }
                }
            }

            // Daten aus Material
            EtkDataPart part = partListEntry.getPart();
            String matNr = part.getFieldValue(matNrFieldName);

            // Ab DAIMLER-9716 werden QSL Sachnummer ohne SL ausgegeben
            iPartsResultEntry.setPartNo(iPartsNumberHelper.handleQSLPartNo(matNr));
            iPartsResultEntry.setPartNoFormatted(iPartsNumberHelper.formatPartNo(project, matNr, project.getDBLanguage()));
            iPartsResultEntry.setName(part.getFieldValue(BENENN_FIELD, userInfo.getLanguage(), true));

            List<iPartsWSNavNode> navContext;
            // Daten aus DA_MODULES_EINPAS
            iPartsDataModuleEinPAS dataModuleEinPAS = iPartsSearchVirtualDatasetWithDBDataset.getResultDataModuleEinPAS(partListEntry);
            if (dataModuleEinPAS != null) { // Treffer im Produkt über normale Verortung
                navContext = iPartsWSNavHelper.createNavContext(product, productStructures, dataModuleEinPAS, userInfo.getLanguage(), project);
            } else {
                // Daten aus DA_PRODUCT_SAS
                iPartsDataProductSAs dataProductSAs = iPartsSearchVirtualDatasetWithDBDataset.getResultDataProductSAs(partListEntry);
                if (dataProductSAs != null) { // Treffer in einer SA
                    navContext = iPartsWSNavHelper.createNavContext(product, productStructures, dataProductSAs, partListEntry.getOwnerAssemblyId(),
                                                                    userInfo.getLanguage(), project);
                } else {
                    continue; // Keine Verortung gefunden -> Treffer überspringen
                }
            }
            iPartsResultEntry.setNavContext(navContext);

            // Flag für Einzelteilbild nur im NICHT-RMI-Kontext
            if (!iPartsWebservicePlugin.isRMIActive()) {
                iPartsResultEntry.setPictureAvailable(part.getFieldValueAsBoolean(iPartsConst.FIELD_M_IMAGE_AVAILABLE));
            }

            if (searchCallback != null) {
                searchCallback.processSearchResult(partResultEntry, iPartsResultEntry);
            }
        }

        // Das Ergebnis ist leer, aber es hat Daten gegeben die durch Berechtigungsprüfung ausgefiltert wurden
        if (!searchResults.isEmpty() && iPartsWSPartResultList.isEmpty() && permissionErrorDetected) {
            throwPermissionsError();
        }
        return iPartsWSPartResultList;
    }

    /**
     * Erzeugt für den übergebenen Teiletreffer einen Schlüssel für die Ergebnis-Map. Über identische Schlüssel können
     * mehrere Ergebnisse zu einem zusammengefasst werden, wobei das erste Ergebnis übrigbleibt. Das Flag {@link WebserviceSearchListener#hasMoreResult()}
     * hängt ebenfalls von der Größe der Ergebnis-Map ab.
     *
     * @param partResult
     * @return
     */
    protected String getResultMapKey(EtkPartResult partResult) {
        // TODO Um alle Verwendungen von SAs (sowohl im Fahrzeug als auch Aggregaten sowie in mehreren KGs) zurückgeliefert
        // zu bekommen, müsste hier über iPartsSearchVirtualDatasetWithDBDataset.getResultDataProductSAs() noch die ID
        // von der konkreten SA-Verwendung (sofern vorhanden) in den ResultMapKey integriert werden
        return partResult.getEntry().getAsId().toString("|");
    }


    /**
     * Mechanismus aus JavaviewerWebservice abgeschaut
     */
    protected class WebserviceSearchListener implements SearchEvents {

        private LogChannels logChannelDebug;
        private VarParam<Exception> searchException;

        private final int maxResults = (getMaxResults() >= 0) ? getMaxResults() : iPartsWebservicePlugin.getPluginConfig().getConfigValueAsInteger(iPartsWebservicePlugin.CONFIG_MAX_RESULTS_SEARCH_PARTS);

        private Map<String, EtkSearchBaseResult> resultMap = new LinkedHashMap<>();

        private volatile WebserviceSearchState state = WebserviceSearchState.New;

        private boolean moreResults;

        public WebserviceSearchListener(LogChannels logChannelDebug, VarParam<Exception> searchException) {
            this.logChannelDebug = logChannelDebug;
            this.searchException = searchException;
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
            if (resultMap.size() == maxResults) {
                moreResults = true;
                if (state == WebserviceSearchState.Running) {
                    state = WebserviceSearchState.Finished; // Abbruch signalisieren
                    if (logChannelDebug != null) {
                        Logger.log(logChannelDebug, LogType.DEBUG, getEndpointUri() + ": Webservice search thread aborted due to max results "
                                                                   + maxResults + " exceeded");
                    }
                }
            } else {
                searchResult.load();

                /**
                 * Für die Standardsuche ist der Einbauort der Schlüssel im Suchergebnis, d.h. im Suchtreffer können doppelte
                 * Teilenummern auftauchen solange sich der verwendete Einbauort unterscheidet. Wenn aber für das Suchergebnis keine
                 * Verwendungsfelder konfiguriert sind, kann das für den Benutzer verwirrend sein. Den Unterschied merkt er erst wenn er über
                 * den Treffer in die Verwendung springt, denn dann springt er in unterschiedliche Baugruppen.
                 * Für die WS Suche kann der Aufrufer dass am navContext im Response unterscheiden.
                 */
                String resultMapKey = getResultMapKey((EtkPartResult)searchResult);

                // Erster Treffer soll erhalten bleiben (dadurch ergibt sich bei der Suche in SAs auch automatisch, dass
                // Treffer im Fahrzeug vor Treffern in Aggregaten zurückgeliefert werden bei dem standardmäßigen EesultMapKey)
                if (!resultMap.containsKey(resultMapKey)) {
                    resultMap.put(resultMapKey, searchResult);
                }
            }
        }

        @Override
        public void OnFinish(EtkBaseSearch sender) {
            if ((state == WebserviceSearchState.New) || (state == WebserviceSearchState.Running)) {
                state = WebserviceSearchState.Finished;
                if (logChannelDebug != null) {
                    Logger.log(logChannelDebug, LogType.DEBUG, getEndpointUri() + ": Webservice search finished with "
                                                               + resultMap.size() + " results");
                }
            }
        }

        @Override
        public void OnError(RuntimeException e) {
            searchException.setValue(e);
            state = WebserviceSearchState.Error;
            if (logChannelDebug != null) {
                Logger.log(iPartsWebservicePlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, getEndpointUri() + ": Webservice search stopped on error: "
                                                                                    + e.getMessage());
                Logger.logExceptionWithoutThrowing(logChannelDebug, LogType.ERROR, e);
            }
        }

        @Override
        public void OnStart(EtkBaseSearch sender) {
            resultMap.clear();
            state = WebserviceSearchState.Running;
        }

        @Override
        public void OnClear() {
            resultMap.clear();
        }

        public Map<String, EtkSearchBaseResult> getResults() {
            return resultMap;
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