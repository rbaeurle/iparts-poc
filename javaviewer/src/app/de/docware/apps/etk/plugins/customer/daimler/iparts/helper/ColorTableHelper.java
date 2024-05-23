/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hilfsklasse für Farb-/Variantentabellen und deren Inhalte in iParts.
 */
public class ColorTableHelper implements iPartsConst {

    public static final String QFT_COLORTABLE_ID_PREFIX = "QFT";
    public static final char COLORTABLE_MANUAL_INDICATOR = 'B';

    /**
     * Liefert eine gefilterte Liste von {@link VariantTablesDataStructure}-Objekten für die übergebene Materialnummer
     * und Filterkriterien zurück.
     *
     * @param project
     * @param currentPartListEntry
     * @param filterProductId
     * @param filterSeriesId
     * @param withHistoryData
     * @param retailFilter
     * @return
     */
    public static List<VariantTablesDataStructure> getVariantTablesDataForPartListEntry(EtkProject project, EtkDataPartListEntry currentPartListEntry,
                                                                                        iPartsProductId filterProductId, iPartsSeriesId filterSeriesId,
                                                                                        boolean withHistoryData, boolean retailFilter) {
        List<iPartsDataColorTableToPart> colorTableToPartList = iPartsDataColorTableToPartList.loadColorTableToPartListForPartNumber(project, currentPartListEntry.getPart().getAsId().getMatNr()).getAsList();
        if (!withHistoryData) {
            removeColorTableToPartHistoryDataFromList(colorTableToPartList);
        }

        // Primus Tabellen sollen an das Ende sortiert werden
        Collections.sort(colorTableToPartList, new Comparator<iPartsDataColorTableToPart>() {

            @Override
            public int compare(iPartsDataColorTableToPart o1, iPartsDataColorTableToPart o2) {
                boolean o1IsPrimus = o1.getDataOrigin() == iPartsImportDataOrigin.PRIMUS;
                boolean o2IsPrimus = o2.getDataOrigin() == iPartsImportDataOrigin.PRIMUS;

                if (o1IsPrimus && !o2IsPrimus) {
                    return 1;
                } else if (!o1IsPrimus && o2IsPrimus) {
                    return -1;
                } else {
                    return o1.getAsId().getColorTableId().compareTo(o2.getAsId().getColorTableId());
                }
            }
        });

        // zu jedem gefundenen colorTableToPart die passsende colorTableData in einer VariantTablesDataStructure zusammenfassen
        List<VariantTablesDataStructure> result = new ArrayList<VariantTablesDataStructure>(colorTableToPartList.size());
        for (iPartsDataColorTableToPart colorTableToPart : colorTableToPartList) {
            VariantTablesDataStructure variantTablesDataStructure = new VariantTablesDataStructure();
            variantTablesDataStructure.colorTableToPart = colorTableToPart;
            variantTablesDataStructure.colorTableData = new iPartsDataColorTableData(project, new iPartsColorTableDataId(colorTableToPart.getAsId().getColorTableId()));
            result.add(variantTablesDataStructure);
        }

        if (retailFilter && !result.isEmpty()) {
            removeColorTableToPartNonRetailDataFromList(project, result, filterProductId, filterSeriesId, EditModuleHelper.getDocumentationTypeFromPartListEntry(currentPartListEntry));
        }

        return result;
    }

    /**
     * Aus der Liste alle historischen Stände entfernen.
     *
     * @param dataList
     */
    public static void removeColorTableToPartHistoryDataFromList(List<iPartsDataColorTableToPart> dataList) {
        for (int i = dataList.size() - 1; i >= 0; i--) {
            if (dataList.get(i).isHistoryRecord()) {
                dataList.remove(i);
            }
        }
    }

    /**
     * Aus der übergebenen Liste die dokumentationstypabhängigen Farbtabellen herausfiltern
     *
     * @param dataList
     */
    public static void filterPartListByDocumentationType(iPartsDocumentationType docType, List<VariantTablesDataStructure> dataList) {
        Set<iPartsImportDataOrigin> origins = new HashSet<iPartsImportDataOrigin>();
        switch (docType) {
            case BCS_PLUS:
            case BCS_PLUS_GLOBAL_BM:
            case ELDAS:
            case PLUS_MINUS:
                origins.add(iPartsImportDataOrigin.PRIMUS); // Import via PrimusImporter (PSEUDO)
                break;
            case DIALOG:
            case DIALOG_IPARTS:
                // QFT Farbtabellen können über zwei Importer in die DB gelangen
                origins.add(iPartsImportDataOrigin.MAD); // Import durch Baureihenimporter (QFT)
                origins.add(iPartsImportDataOrigin.DIALOG);// Import via ColorTableImporter (FTS) DIALOG-Konstruktion (QFT)
                origins.add(iPartsImportDataOrigin.UNKNOWN); // Fallback auf bestehenden Zustand (alte Datensätze die bisher
                // keine Herkunft hatten -> wurden aktuell als DIALOG Datensätze betrachtet)
                break;
        }
        Iterator<VariantTablesDataStructure> iterator = dataList.iterator();
        while (iterator.hasNext()) {
            iPartsDataColorTableToPart currentDataObject = iterator.next().colorTableToPart;
            if (!origins.contains(currentDataObject.getDataOrigin())) {
                iterator.remove();
            }
        }
    }

    /**
     * Aus der Liste alle nicht-Retail-relevanten Daten entfernen für die übergebene Baureihe.
     *
     * @param project
     * @param dataList
     * @param filterProductId
     * @param filterSeriesId
     * @param docType
     */
    public static void removeColorTableToPartNonRetailDataFromList(EtkProject project, List<VariantTablesDataStructure> dataList,
                                                                   iPartsProductId filterProductId, iPartsSeriesId filterSeriesId,
                                                                   iPartsDocumentationType docType) {
        // Logik für den Retailfilter: https://confluence.docware.de/confluence/x/fIDa

        if (dataList.isEmpty()) {
            return;
        }
        // Abhängig vom Dokumentationstyp des Moduls müssen Teilepositionen ausgefiltert werden. Wenn  Farbtabellen aus
        // einer ELDAS Stückliste aufgerufen werden, dann sollen die "QFT" DIALOG Farbtabellen ausgefiltert werden. Wenn
        // der Aufruf aus einer DIALOG Stückliste erfolgt, dann sollen die PSEUDO Farbtabellen aus ELDAS entfernt werden
        filterPartListByDocumentationType(docType, dataList);
        // Wenn der Dokumentationstyp "BCS_PLUS" ist, dann handelt es sich um eine PSEUDO Farbtabelle -> keine weiteren
        // Filterungen nötig
        if (docType.isTruckDocumentationType()) {
            return;
        }

        String seriesNumber;
        if (filterSeriesId != null) {
            seriesNumber = filterSeriesId.getSeriesNumber();
        } else {
            seriesNumber = "";
        }

        // Ohne Baureihe kann der Retailfilter nur ein leeres Ergebnis zurückliefern
        if (seriesNumber.isEmpty()) {
            dataList.clear();
            return;
        }

        Set<String> validFactories = FactoriesHelper.getValidFactories(project, filterProductId);

        for (int i = dataList.size() - 1; i >= 0; i--) {
            VariantTablesDataStructure variantTablesDataStructure = dataList.get(i);
            // ET-KZ muss leer sein und Baureihe muss übereinstimmen
            if (!variantTablesDataStructure.colorTableToPart.getET_KZ().isEmpty()
                || !variantTablesDataStructure.colorTableData.getValidSeries().equals(seriesNumber)) {
                dataList.remove(i);
                continue;
            }

            // Auf gültigen Einsatztermin überprüfen
            iPartsColorTableToPartId colorTableToPartId = variantTablesDataStructure.colorTableToPart.getAsId();
            if (!hasValidFactoryDate(project, validFactories, colorTableToPartId, null)) {
                dataList.remove(i);
            }
        }

    }

    /**
     * Liefert eine gefilterte Liste von {@link VariantsDataStructure}-Objekten für die übergebene ID und Filterkriterien zurück.
     *
     * @param project
     * @param currentPartListEntry
     * @param variantTableId
     * @param filterProductId
     * @param withHistoryData
     * @param retailFilter         @return
     */
    public static List<VariantsDataStructure> getVariantsDataForVariantTableId(EtkProject project, EtkDataPartListEntry currentPartListEntry, iPartsColorTableDataId variantTableId,
                                                                               iPartsProductId filterProductId, iPartsDataColorTableData colorTableData, boolean withHistoryData,
                                                                               boolean retailFilter) {
        List<iPartsDataColorTableContent> colorTableContentList = iPartsDataColorTableContentList.loadColorTableContentListForColorTable(project, variantTableId).getAsList();

        if (!withHistoryData) {
            removeColorTableContentHistoryDataFromList(colorTableContentList);
        }

        // zu jedem gefundenen colorTableContent die passende colorTableData und colorNumber in einer VariantsDataStructure zusammenfassen
        // colorTableData wird als Parameter übergeben weil es in einem vorigen Schritt bereits initialisiert wurde
        List<VariantsDataStructure> result = new ArrayList<VariantsDataStructure>(colorTableContentList.size());
        if (!colorTableContentList.isEmpty()) {

            for (iPartsDataColorTableContent colorTableContent : colorTableContentList) {
                VariantsDataStructure variantsDataStructure = new VariantsDataStructure();
                variantsDataStructure.colorTableContent = colorTableContent;
                variantsDataStructure.colorTableData = colorTableData;
                variantsDataStructure.colorNumber = new iPartsDataColorNumber(project, new iPartsColorNumberId(colorTableContent.getColorNumber()));
                result.add(variantsDataStructure);
            }

            if (retailFilter) {
                removeColorTableContentNonRetailDataFromList(project, result, filterProductId, EditModuleHelper.getDocumentationTypeFromPartListEntry(currentPartListEntry));
            }
        }

        return result;
    }

    /**
     * Aus der Liste alle historischen Stände entfernen.
     *
     * @param dataList
     */
    public static void removeColorTableContentHistoryDataFromList(List<iPartsDataColorTableContent> dataList) {
        for (int i = dataList.size() - 1; i >= 0; i--) {
            if (dataList.get(i).isHistoryRecord()) {
                dataList.remove(i);
            }
        }
    }

    /**
     * Aus der Liste alle nicht-Retail-relevanten Daten entfernen.
     *
     * @param project
     * @param dataList
     * @param filterProductId
     * @param docType
     */
    public static void removeColorTableContentNonRetailDataFromList(EtkProject project, List<VariantsDataStructure> dataList,
                                                                    iPartsProductId filterProductId, iPartsDocumentationType docType) {
        // Logik für den Retailfilter: https://confluence.docware.de/confluence/x/fIDa

        if (dataList.isEmpty()) {
            return;
        }
        // Nur für Dokumentationstypen ungleich "BCS_PLUS" weitere Checks durchführen lassen (Dok-Typ "BCS_PLUS" -> PSEUDO Farbtabellen)
        if (docType.isTruckDocumentationType()) {
            return;
        }

        // Baureihe aus iPartsDataColorTableData entnehmen, wobei diese in allen Einträgen identisch ist
        iPartsDataColorTableData colorTableData = dataList.get(0).colorTableData;
        String seriesNumber = colorTableData.getValidSeries();

        // Ohne Baureihe kann der Retailfilter nur ein leeres Ergebnis zurückliefern
        if (seriesNumber.isEmpty()) {
            dataList.clear();
            return;
        }

        Set<String> validFactories = FactoriesHelper.getValidFactories(project, filterProductId);

        for (int i = dataList.size() - 1; i >= 0; i--) {
            VariantsDataStructure variantTablesDataStructure = dataList.get(i);
            // DAIMLER-10055: Es werden nur noch Varianten ausgefiltert, die nicht freigegeben sind
            // ETKZ ist irrelevant
            if (variantTablesDataStructure.colorTableContent.getStatus() != iPartsDataReleaseState.RELEASED) {
                dataList.remove(i);
                continue;
            }

            // Auf gültigen Einsatztermin überprüfen
            iPartsColorTableContentId colorTableContentId = variantTablesDataStructure.colorTableContent.getAsId();
            if (!hasValidFactoryDate(project, validFactories, null, colorTableContentId)) {
                dataList.remove(i);
            }
        }
    }

    /**
     * Überprüft, ob für die übergebene {@link iPartsColorTableToPartId} bzw. {@link iPartsColorTableContentId} mindestens
     * ein gültiger Einsatztermin in den After-Sales-Werkseinsatzdaten bzw. Produktions-Einsatzdaten existiert mit
     * gültigem Werk.
     *
     * @param project
     * @param validFactories
     * @param colorTableToPartId  ID muss exklusiv gesetzt sein für die Überprüfung von Variantentabellen
     * @param colorTableContentId ID muss exklusiv gesetzt sein für die Überprüfung von Varianten zu einer Variantentabelle
     * @return
     */
    private static boolean hasValidFactoryDate(EtkProject project, Set<String> validFactories, iPartsColorTableToPartId colorTableToPartId,
                                               iPartsColorTableContentId colorTableContentId) {
        iPartsDataColorTableFactoryList factoryDataList;
        if (colorTableToPartId != null) {
            factoryDataList = iPartsDataColorTableFactoryList.loadColorTableFactoryListForColorTableToPartIdForAS(project, colorTableToPartId);
        } else {
            factoryDataList = iPartsDataColorTableFactoryList.loadColorTableFactoryListForColorTableContentIdForAS(project, colorTableContentId);
        }

        // Es muss mindestens einen gültigen Einsatztermin in den After-Sales-Werkseinsatzdaten geben
        boolean validRecordFound = checkIfValidForSeriesAndDifferentPEMTDates(factoryDataList, validFactories);

        if (!validRecordFound) {
            if (colorTableToPartId != null) {
                factoryDataList = iPartsDataColorTableFactoryList.loadColorTableFactoryListForColorTableToPartId(project, colorTableToPartId);
            } else {
                factoryDataList = iPartsDataColorTableFactoryList.loadColorTableFactoryListForColorTableContentId(project, colorTableContentId);
            }
            // Werksdaten filtern
            filterAndSortColorFactoryData(factoryDataList);
            // Oder es muss mindestens einen gültigen Einsatztermin in den Produktions-Werkseinsatzdaten geben
            validRecordFound = checkIfValidForSeriesAndDifferentPEMTDates(factoryDataList, validFactories);
        }

        return validRecordFound;
    }

    /**
     * Check, ob mind. ein Werkseinsatzdatensatz ein gültiges Werk zur Baureihe hat und PEM-Termin-AB <> PEM-Termin-BIS
     *
     * @param factoryDataList
     * @param validFactories
     * @return
     */
    private static boolean checkIfValidForSeriesAndDifferentPEMTDates(iPartsDataColorTableFactoryList factoryDataList, Set<String> validFactories) {
        if (factoryDataList != null) {
            for (iPartsDataColorTableFactory dataColorTableFactory : factoryDataList) {
                if ((validFactories != null) && validFactories.contains(dataColorTableFactory.getFactory())) {
                    long pemtFrom = iPartsFactoryData.getFactoryDateFromDateString(dataColorTableFactory.getPEMTerminAb(),
                                                                                   TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTA));
                    long pemtTo = iPartsFactoryData.getFactoryDateFromDateString(dataColorTableFactory.getPEMTerminBis(),
                                                                                 TableAndFieldName.make(TABLE_DA_COLORTABLE_FACTORY, FIELD_DCCF_PEMTB));
                    if (pemtFrom != pemtTo) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Filtert die übergebenen Produktions-Werkseinsatzdaten nach dem höchsten ADAT pro Werk (nicht notwendig für AS-Werkseinsatzdaten).
     *
     * @param factoryDataList
     * @return
     */
    public static Collection<iPartsDataColorTableFactory> filterForLatestColorTableFactoryData(iPartsDataColorTableFactoryList factoryDataList) {
        Map<String, iPartsDataColorTableFactory> factoryToFactoryDataMap = new LinkedHashMap<String, iPartsDataColorTableFactory>();
        for (iPartsDataColorTableFactory dataColorTableFactory : factoryDataList) {
            String factory = dataColorTableFactory.getFactory();
            iPartsDataColorTableFactory otherFactoryData = factoryToFactoryDataMap.get(factory);
            if ((otherFactoryData == null) || (otherFactoryData.getADat().compareTo(dataColorTableFactory.getADat()) < 0)) {
                factoryToFactoryDataMap.put(factory, dataColorTableFactory);
            }
        }
        return factoryToFactoryDataMap.values();
    }

    /**
     * Liefert alle für den Retail gefilterten Werkseinsatzdaten für die übergebene Farbvariantentabelle zu dem Teil des
     * übergebenen Stücklisteneintrags und Baureihe zurück.
     *
     * @param entry
     * @param colorTableToPartId
     * @param seriesId
     * @param project
     * @param responseDataForPemRetailFilter
     * @return
     */
    public static iPartsDataColorTableFactoryList getColorToPartFactoryDataListForRetail(EtkDataPartListEntry entry,
                                                                                         iPartsColorTableToPartId colorTableToPartId,
                                                                                         iPartsSeriesId seriesId, EtkProject project,
                                                                                         Map<String, List<iPartsResponseDataWithSpikes>> responseDataForPemRetailFilter) {
        // Werkseinsatzdaten mit Retail-Filter von der Farbvariantentabelle am Stücklisteneintrag übernehmen
        iPartsDataColorTableFactoryList list = new iPartsDataColorTableFactoryList();
        if (entry instanceof iPartsDataPartListEntry) {
            iPartsDataPartListEntry iPartsEntry = (iPartsDataPartListEntry)entry;
            iPartsColorTable colorTableForRetail = iPartsEntry.getColorTableForRetail();
            if (colorTableForRetail != null) {
                iPartsColorTable.ColorTable colorTable = colorTableForRetail.getColorTable(colorTableToPartId.getColorTableId());
                if (colorTable != null) {
                    iPartsColorTable.ColorTableToPart colorTableToPart = colorTable.colorTableToPartsMap.get(colorTableToPartId);
                    if (colorTableToPart != null) {
                        iPartsColorFactoryDataForRetail colorFactoryDataForRetail = colorTableToPart.getFactoryData();

                        // Aggregatetyp über das Produkt zur Stückliste bestimmen
                        String aggregateType = iPartsEntry.getOwnerAssembly().getAggregateTypeOfProductFromModuleUsage();

                        fillColorTableFactoryList(entry, colorFactoryDataForRetail, seriesId, aggregateType, list, true,
                                                  project, responseDataForPemRetailFilter);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Liefert alle für den Retail gefilterten Werkseinsatzdaten für den übergebenen Farbvarianteninhalt zu dem Teil
     * des übergebenen Stücklisteneintrags und Baureihe zurück.
     *
     * @param entry
     * @param colorTableContentId
     * @param seriesId
     * @param project
     * @param responseDataForPemRetailFilter
     * @return
     */
    public static iPartsDataColorTableFactoryList getColorContentFactoryDataListForRetail(EtkDataPartListEntry entry,
                                                                                          iPartsColorTableContentId colorTableContentId,
                                                                                          iPartsSeriesId seriesId, EtkProject project,
                                                                                          Map<String, List<iPartsResponseDataWithSpikes>> responseDataForPemRetailFilter) {
        // Werkseinsatzdaten mit Retail-Filter von dem Farbvarianteninhalt am Stücklisteneintrag übernehmen
        iPartsDataColorTableFactoryList list = new iPartsDataColorTableFactoryList();
        if (entry instanceof iPartsDataPartListEntry) {
            iPartsDataPartListEntry iPartsEntry = (iPartsDataPartListEntry)entry;
            iPartsColorTable colorTableForRetail = iPartsEntry.getColorTableForRetail();
            if (colorTableForRetail != null) {
                iPartsColorTable.ColorTable colorTable = colorTableForRetail.getColorTable(colorTableContentId.getColorTableId());
                if (colorTable != null) {
                    List<iPartsColorTable.ColorTableContent> colorTableContents = colorTable.colorTableContents;
                    for (iPartsColorTable.ColorTableContent colorTableContent : colorTableContents) {
                        if (colorTableContent.colorTableContentId.equals(colorTableContentId)) { // Farbvarianteninhalt über die ID suchen
                            iPartsColorFactoryDataForRetail colorFactoryDataForRetail = colorTableContent.getFactoryData();

                            // Aggregatetyp über das Produkt zur Stückliste bestimmen
                            String aggregateType = iPartsEntry.getOwnerAssembly().getAggregateTypeOfProductFromModuleUsage();

                            fillColorTableFactoryList(entry, colorFactoryDataForRetail, seriesId, aggregateType, list, true, project, responseDataForPemRetailFilter);
                            break;
                        }
                    }
                }
            }
        }
        return list;
    }

    /**
     * Werkseinsatzdaten für Farbvariantentabellen bzw. Farbvarianteninhalte befüllen
     *
     * @param entry
     * @param colorFactoryDataForRetail
     * @param seriesId
     * @param aggregateType
     * @param listToBeFilled
     * @param checkIfValidForFilter
     * @param project
     * @param responseDataForPemRetailFilter
     */
    private static void fillColorTableFactoryList(EtkDataPartListEntry entry, iPartsColorFactoryDataForRetail colorFactoryDataForRetail, iPartsSeriesId seriesId,
                                                  String aggregateType, iPartsDataColorTableFactoryList listToBeFilled, boolean checkIfValidForFilter,
                                                  EtkProject project, Map<String, List<iPartsResponseDataWithSpikes>> responseDataForPemRetailFilter) {
        assert listToBeFilled.isEmpty();
        responseDataForPemRetailFilter.clear();

        if (colorFactoryDataForRetail != null) {
            for (Map.Entry<String, List<iPartsColorFactoryDataForRetail.DataForFactory>> dataForFactoryEntries : colorFactoryDataForRetail.getFactoryDataMap().entrySet()) {
//                            String factoryNumber = dataForFactoryEntries.getKey();
                for (iPartsColorFactoryDataForRetail.DataForFactory dataForFactory : dataForFactoryEntries.getValue()) {
                    iPartsDataColorTableFactory factoryData = new iPartsDataColorTableFactory(project, dataForFactory.factoryDataId);

                    // Nur freigegebene Datensätze übrig lassen.
                    String status = factoryData.getFieldValue(FIELD_DCCF_STATUS);
                    if (StrUtils.isValid(status) && !status.equals(iPartsDataReleaseState.RELEASED.getDbValue())) {
                        continue;
                    }

                    // Baureihe als virtuelles Feld an iPartsDataColorTableFactory hängen, damit diese später in
                    // iPartsDataColorTableFactory.loadVirtualField() ausgewertet werden kann
                    if (factoryData.existsInDB()) {
                        if (checkIfValidForFilter && !iPartsFactories.getInstance(project).isValidForFilter(factoryData.getFactory())) {
                            continue;
                        }
                        if (seriesId != null) {
                            factoryData.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DCCF_SERIES_NUMBER, seriesId.getSeriesNumber(),
                                                                 true, DBActionOrigin.FROM_DB);
                        }

                        if (StrUtils.isValid(aggregateType)) {
                            factoryData.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DCCF_AGGREGATE_TYPE,
                                                                 aggregateType, true, DBActionOrigin.FROM_DB);
                        }

                        boolean responseDataAvailableForPem = dataForFactory.isResponseDataAvailableForPEMFrom();
                        if (responseDataAvailableForPem) {
                            List<iPartsResponseDataWithSpikes> responseDataList = new DwList<>();
                            String pem = dataForFactory.pemFrom;
                            for (Map.Entry<iPartsFactoryData.IdentWithModelNumber, Set<String>> identFrom : dataForFactory.identsFrom.entrySet()) {
                                iPartsFactoryData.IdentWithModelNumber identWithModel = identFrom.getKey();
                                Set<String> spikes = identFrom.getValue();

                                // jetzt diesen Eintrag im Cache suchen
                                List<iPartsDataResponseData> responseDataForPem = iPartsResponseData.getInstance(project).getResponseData(pem);
                                responseDataList.addAll(ResponseDataHelper.getSpikesForResponseData(responseDataForPem,
                                                                                                    dataForFactory.factoryDataId.getFactory(),
                                                                                                    identWithModel, spikes,
                                                                                                    entry, project));
                            }
                            responseDataForPemRetailFilter.put(pem, responseDataList);
                        }

                        // Werte für virtuelle Felder "Response-Daten für PEM verfügbar" setzen
                        factoryData.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DCCF_PEMA_RESPONSE_DATA_AVAILABLE,
                                                             SQLStringConvert.booleanToPPString(responseDataAvailableForPem), true,
                                                             DBActionOrigin.FROM_DB);

                        responseDataAvailableForPem = dataForFactory.isResponseDataAvailableForPEMTo();
                        if (responseDataAvailableForPem) {
                            List<iPartsResponseDataWithSpikes> responseDataList = new DwList<>();
                            String pem = dataForFactory.pemTo;
                            for (Map.Entry<iPartsFactoryData.IdentWithModelNumber, Set<String>> identTo : dataForFactory.identsTo.entrySet()) {
                                iPartsFactoryData.IdentWithModelNumber identWithModel = identTo.getKey();
                                Set<String> spikes = identTo.getValue();

                                // jetzt diesen Eintrag im Cache suchen
                                List<iPartsDataResponseData> responseDataForPem = iPartsResponseData.getInstance(project).getResponseData(pem);
                                responseDataList.addAll(ResponseDataHelper.getSpikesForResponseData(responseDataForPem,
                                                                                                    dataForFactory.factoryDataId.getFactory(),
                                                                                                    identWithModel, spikes,
                                                                                                    entry, project));
                            }
                            responseDataForPemRetailFilter.put(pem, responseDataList);
                        }

                        factoryData.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DCCF_PEMB_RESPONSE_DATA_AVAILABLE,
                                                             SQLStringConvert.booleanToPPString(responseDataAvailableForPem), true,
                                                             DBActionOrigin.FROM_DB);
                    }

                    // Die folgenden Zeilen sind auskommentiert, da sie nur benötigt werden, wenn auch für
                    // die Werkseinsatzdaten an Farbvarianten Zuatzwerke verdichtet werden sollen

                    // Die folgenden Felder explizit aufgrund der Retail-Daten setzen, da diese sich durch
                    // die Verdichtung von Zusatzwerken von den Werten in der DB unterscheiden können
//                                factoryData.setFieldValue(iPartsConst.FIELD_DCCF_FACTORY, factoryNumber, DBActionOrigin.FROM_DB);
//
//                                // PEM ab
//                                factoryData.setFieldValue(iPartsConst.FIELD_DCCF_PEMA, dataForFactory.pemFrom, DBActionOrigin.FROM_DB);
//                                factoryData.setFieldValue(iPartsConst.FIELD_DCCF_PEMTA, String.valueOf(dataForFactory.dateFrom), DBActionOrigin.FROM_DB);
//                                factoryData.setFieldValue(iPartsConst.FIELD_DCCF_STCA, dataForFactory.stCodeFrom, DBActionOrigin.FROM_DB);
//
//                                // PEM bis
//                                factoryData.setFieldValue(iPartsConst.FIELD_DCCF_PEMB, dataForFactory.pemTo, DBActionOrigin.FROM_DB);
//                                factoryData.setFieldValue(iPartsConst.FIELD_DCCF_PEMTB, String.valueOf(dataForFactory.dateTo), DBActionOrigin.FROM_DB);
//                                factoryData.setFieldValue(iPartsConst.FIELD_DCCF_STCB, dataForFactory.stCodeTo, DBActionOrigin.FROM_DB);

                    listToBeFilled.add(factoryData, DBActionOrigin.FROM_DB);
                }
            }
        }
    }

    /**
     * Sortiert die für das Modul relevanten Werksdaten nach ID (ohne Adat) und filtert die Datensätze nach Adat und
     * Status (pro ID).
     * "Modul-relevant" sind Werksdaten, die einen Status ungleich "nicht relevant" besitzen und von Werken kommen, die
     * gültig zum Produkt sind, in dem dieses Modul vorkommt (bzw. Produkte bei freien SAs).
     *
     * @param list
     * @param dataAssembly
     */
    public static void filterAndSortOnlyAssemblyRelevantColorFactoryData(iPartsDataColorTableFactoryList list,
                                                                         EtkDataAssembly dataAssembly) {
        if (dataAssembly instanceof iPartsDataAssembly) {
            List<iPartsDataColorTableFactory> factoryList = list.getAsList();
            // Nicht-relevante Daten ausfiltern, wenn eine Assembly
            factoryList = factoryList.stream()
                    .filter(factoryData -> FactoryDataHelper.isRelevantFactoryData((iPartsDataAssembly)dataAssembly,
                                                                                   factoryData, FIELD_DCCF_STATUS,
                                                                                   FIELD_DCCF_FACTORY))
                    .collect(Collectors.toList());
            list.clear(DBActionOrigin.FROM_DB);
            list.addAll(factoryList, DBActionOrigin.FROM_DB);
        } else {
            filterAndSortColorFactoryData(list);
        }
    }

    /**
     * Sortiert die Werksdaten nach ID (ohne Adat) und filtert die Datensätze nach Adat und Status (pro ID)
     *
     * @param list
     */
    public static void filterAndSortColorFactoryData(iPartsDataColorTableFactoryList list) {
        Map<iPartsColorTableFactoryId, List<iPartsDataColorTableFactory>> factoryDataForId = new LinkedHashMap<>();
        // Datensätze werden nach ihrer ID gruppiert, weil die ADAT- und Status auf Basis der "gleichen" Datensätze
        // geschehen muss. Weil das ADAT aber ein Bestandteil des Schlüssels ist, wird hier ein künstlicher Schlüssel
        // - ohne ADAT - erzeugt und als Gruppierungsschlüssel für die einzelnen Datensätze verwendet.
        for (iPartsDataColorTableFactory factoryData : list) {
            iPartsColorTableFactoryId existingId = factoryData.getAsId();
            iPartsColorTableFactoryId idWithoutAdat = new iPartsColorTableFactoryId(existingId.getTableId(), existingId.getPos(),
                                                                                    existingId.getFactory(), "",
                                                                                    existingId.getDataId(), existingId.getSdata());
            List<iPartsDataColorTableFactory> dataFactoriesForIdWithoutAdat = factoryDataForId.computeIfAbsent(idWithoutAdat, k -> new ArrayList<>());
            dataFactoriesForIdWithoutAdat.add(factoryData);
        }
        list.clear(DBActionOrigin.FROM_DB);
        FactoryDataHelper.fillListWithFilteredFactoryData(list, factoryDataForId.values(), false, iPartsConst.FIELD_DCCF_STATUS, iPartsConst.FIELD_DCCF_ADAT);
    }

    /**
     * Baureihe aus Farbtabelle ermitteln (für Bestimmung Code-Stammdaten).
     * Liefert nur für QFT-Farbtabellen einen Wert. Für Primus-Farbtabellen (= Non QFT-ID) soll die BR aus dem Stücklisteneintrag verwendet werden.
     *
     * @param colorTableData            DataObject für Farbtabelle
     * @param seriesNoFromPartlistEntry
     * @return
     */
    public static iPartsSeriesId getSeriesFromColorTableOrPartListEntry(EtkDataObject colorTableData, String seriesNoFromPartlistEntry) {
        String colorTableId = colorTableData.getFieldValue(iPartsConst.FIELD_DCTD_TABLE_ID);
        String seriesNoFromColorTable = colorTableData.getFieldValue(iPartsConst.FIELD_DCTD_VALID_SERIES);
        // Erst nach dem Wert im DB Feld schauen
        if (StrUtils.isValid(seriesNoFromColorTable)) {
            return new iPartsSeriesId(seriesNoFromColorTable);
        }
        // Falls das Feld nicht gbelegt ist, die Baureihe aus der QFT Id ziehen
        if (colorTableId.startsWith(QFT_COLORTABLE_ID_PREFIX)) {
            return new iPartsSeriesId(extractSeriesNumberFromTableId(colorTableId));
        }
        // Wenn alles nicht klappt, dann mache die ID aus der übergebenen Baureihe
        return new iPartsSeriesId(seriesNoFromPartlistEntry);
    }

    /**
     * Extrahiert die gültige Baureihe aus der Farbtabellen ID (siehe WikiPage)
     *
     * @param colorTableId
     * @return
     */
    public static String extractSeriesNumberFromTableId(String colorTableId) {
        if ((colorTableId != null) && (colorTableId.length() >= 6)) {
            return iPartsConst.MODEL_NUMBER_PREFIX_CAR + StrUtils.copySubString(colorTableId, 3, 3);
        }
        return "";
    }

    public static boolean isUserManualColorTable(String colorTableId) {
        if ((colorTableId != null) && (colorTableId.length() >= 7) && colorTableId.startsWith(QFT_COLORTABLE_ID_PREFIX)) {
            return Character.toUpperCase(colorTableId.charAt(6)) == COLORTABLE_MANUAL_INDICATOR;
        }
        return false;
    }

    public static String makeWhereValueForColorTableWithSeries(iPartsSeriesId seriesId) {
        if ((seriesId == null) || !seriesId.isValidId()) {
            return "";
        }
        return "QFT" + seriesId.getSeriesWithoutPrefix() + "*";
    }

    public static String getColorTableCodeForSeries(EtkProject project, iPartsColorTable.ColorTableContent colorTableContent, String seriesNumber) {
        String code = colorTableContent.code;
        if (StrUtils.isValid(seriesNumber)) {
            iPartsDialogSeries series = iPartsDialogSeries.getInstance(project, new iPartsSeriesId(seriesNumber));
            if (series.isEventTriggered()) {
                String eventFromId = colorTableContent.getFilterEventFromId();
                String eventToId = colorTableContent.getFilterEventToId();
                code = DaimlerCodes.addEventsCodes(code, series.getEvent(eventFromId), series.getEvent(eventToId), project);
            }
        }
        if (DaimlerCodes.isEmptyCodeString(code)) {
            return null;
        }
        return code;
    }

    public static String getColorTableCode(EtkProject project, iPartsColorTable.ColorTableContent colorTableContent) {
        String seriesNumber = extractSeriesNumberFromTableId(colorTableContent.colorTableContentId.getColorTableId());
        return getColorTableCodeForSeries(project, colorTableContent, seriesNumber);
    }

    public static class VariantTablesDataStructure {

        public iPartsDataColorTableData colorTableData;
        public iPartsDataColorTableToPart colorTableToPart;
    }


    public static class VariantsDataStructure {

        public iPartsDataColorTableData colorTableData;
        public iPartsDataColorTableContent colorTableContent;
        public iPartsDataColorNumber colorNumber;
    }
}
