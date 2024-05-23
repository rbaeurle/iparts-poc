/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AggregateDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLStringConvert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hilfsklasse für Werkseinsatzdaten in iParts.
 */
public class FactoryDataHelper {

    /**
     * Werkseinsatzdaten für Stücklistenposition bestimmen
     * Für DIALOG kann es Werkseinsatzdaten aus AS und/oder Produktion geben. Für retailFilter true werden die Einsatzdaten
     * aus beiden Bereichen verdichtet und Daten für ungültige Fabriken entfernt.
     *
     * @param entry
     * @param retailFilter Flag, ob der Retail-Filter gesetzt ist (nur DIALOG relevant)
     * @param forAS        Nur relevant für DIALOG und retailFilter false: true wenn AS-Daten bestimmt werden sollen, false für Produktions-Daten (nur DIALOG relevant)
     * @param project
     * @return
     */
    public static iPartsDataFactoryDataList getFactoryDataList(EtkDataPartListEntry entry, boolean retailFilter, boolean forAS,
                                                               EtkProject project) {
        iPartsDocumentationType documentationType = iPartsDocumentationType.UNKNOWN;
        String aggregateType = null;
        if (entry.getOwnerAssembly() instanceof iPartsDataAssembly) {
            iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)entry.getOwnerAssembly();
            documentationType = iPartsAssembly.getDocumentationType();

            // Aggregatetyp über das Produkt zur Stückliste bestimmen
            aggregateType = iPartsAssembly.getAggregateTypeOfProductFromModuleUsage();
        }
        iPartsDataFactoryDataList list;
        if ((documentationType.isPKWDocumentationType()) || entry.getPartListType().equals(iPartsConst.PARTS_LIST_TYPE_DIALOG_SM)) {
            // DIALOG: die Verbindung zwischen Stückliste und Werkseinsatzdaten ist der BCTE-Schlüssel
            String dialogGuid = entry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
            if (StrUtils.isValid(dialogGuid)) {
                if (retailFilter) {
                    // Werkseinsatzdaten mit Retail-Filter vom Stücklisteneintrag übernehmen
                    list = getRetailFilteredFactoryDataDIALOG(entry, project, aggregateType);
                } else if (forAS) {
                    list = iPartsDataFactoryDataList.loadAfterSalesFactoryDataListForDialogPositionsVariant(project, dialogGuid, true);
                } else {
                    list = iPartsDataFactoryDataList.loadConstructionFactoryDataListForDialogPositionsVariant(project, dialogGuid, true);
                }
            } else {
                list = new iPartsDataFactoryDataList();
            }
        } else if (documentationType.isTruckDocumentationType()) {
            // EDSRetail: die Verbindung zwischen Stückliste und Werkseinsatzdaten ist eine GUID die dem Einbauort entspricht
            String eldasGuid = entry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
            if (retailFilter) {
                list = getRetailFilteredFactoryDataELDAS(entry, eldasGuid, project);
            } else {
                list = iPartsDataFactoryDataList.loadFactoryDataListForEldasPosition(project, eldasGuid);
            }
        } else {
            list = new iPartsDataFactoryDataList();
        }

        return list;
    }

    private static iPartsDataFactoryDataList getRetailFilteredFactoryDataELDAS(EtkDataPartListEntry entry, String eldasGuid, EtkProject project) {
        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        if (entry instanceof iPartsDataPartListEntry) {
            iPartsDataPartListEntry iPartsEntry = (iPartsDataPartListEntry)entry;

            // Die Retail-Werksdaten vom Stücklisteneintrag bestimmen, und davon die Ids merken
            iPartsFactoryData factoryDataForRetail = iPartsEntry.getFactoryDataForRetail();
            Set<iPartsFactoryDataId> retailFactoryDataIds = new HashSet<>();

            if (factoryDataForRetail != null) {
                for (List<iPartsFactoryData.DataForFactory> value : factoryDataForRetail.getFactoryDataMap().values()) {
                    for (iPartsFactoryData.DataForFactory dataForFactory : value) {
                        iPartsDataFactoryData factoryData = new iPartsDataFactoryData(project, dataForFactory.factoryDataId);
                        if (!factoryData.existsInDB()) { // Lädt auch den Datensatz aus der DB
                            // Kann während der Bearbeitung von Werkseinsatzdaten passieren, wenn die Retail-Werkseinsatzdaten
                            // noch nicht neu berechnet wurden und auf Datensätze zeigen, die es nicht mehr gibt (z.B. Werksänderung)
                            continue;
                        }
                        retailFactoryDataIds.add(dataForFactory.factoryDataId);
                    }
                }
            }

            // Die ungefiltertern Werksdaten zur EldasGuid laden (dort werden auch die Einsatzfußnoten mitgeladen)
            iPartsDataFactoryDataList unfilteredList = iPartsDataFactoryDataList.loadFactoryDataListForEldasPosition(project, eldasGuid);
            for (iPartsDataFactoryData factoryData : unfilteredList) {
                iPartsFactoryDataId factoryDataId = factoryData.getAsId();
                // nur noch Einträge übrig lassen die entweder eine Fußnote sind oder in den Retaildaten enthalten sind
                DBDataObject dataObject = factoryData.getAggregateObject(iPartsDataFactoryData.AGGREGATE_ELDAS_FOOTNOTE);
                if (retailFactoryDataIds.contains(factoryDataId) || (dataObject instanceof iPartsDataFootNote)) {
                    list.add(factoryData, DBActionOrigin.FROM_DB);
                }
            }
        }
        return list;
    }

    private static iPartsDataFactoryDataList getRetailFilteredFactoryDataDIALOG(EtkDataPartListEntry entry, EtkProject project, String aggregateType) {
        iPartsDataFactoryDataList list;
        list = new iPartsDataFactoryDataList();
        if (!(entry instanceof iPartsDataPartListEntry)) {
            return list;
        }
        iPartsDataPartListEntry iPartsEntry = (iPartsDataPartListEntry)entry;

        if (iPartsEntry.isValidFactoryDataRelevantForEndNumberFilter()) {
            iPartsFactoryData factoryDataForRetail = iPartsEntry.getFactoryDataForRetail();
            for (Map.Entry<String, List<iPartsFactoryData.DataForFactory>> dataForFactoryEntries : factoryDataForRetail.getFactoryDataMap().entrySet()) {
                String factoryNumber = dataForFactoryEntries.getKey();
                if (!iPartsFactories.getInstance(project).isValidForFilter(factoryNumber)) {
                    continue;
                }
                for (iPartsFactoryData.DataForFactory dataForFactory : dataForFactoryEntries.getValue()) {
                    iPartsDataFactoryData factoryData = new iPartsDataFactoryData(project, dataForFactory.factoryDataId);
                    if (!factoryData.existsInDB()) { // Lädt auch den Datensatz aus der DB
                        // Kann während der Bearbeitung von Werkseinsatzdaten passieren, wenn die Retail-Werkseinsatzdaten
                        // noch nicht neu berechnet wurden und auf Datensätze zeigen, die es nicht mehr gibt (z.B. Werksänderung)
                        continue;
                    }

                    // Die folgenden Felder explizit aufgrund der Retail-Daten setzen, da diese sich durch
                    // die Verdichtung von Zusatzwerken von den Werten in der DB unterscheiden können
                    factoryData.setFieldValue(iPartsConst.FIELD_DFD_FACTORY, factoryNumber, DBActionOrigin.FROM_DB);

                    // PEM ab
                    if (iPartsEntry.isPEMFromRelevant()) {
                        factoryData.setFieldValue(iPartsConst.FIELD_DFD_PEMA, dataForFactory.pemFrom, DBActionOrigin.FROM_DB);
                        String dateFromStr = "";
                        if (dataForFactory.dateFrom > 0) {
                            dateFromStr = String.valueOf(dataForFactory.dateFrom);
                        }
                        factoryData.setFieldValue(iPartsConst.FIELD_DFD_PEMTA, dateFromStr, DBActionOrigin.FROM_DB);
                        factoryData.setFieldValue(iPartsConst.FIELD_DFD_STCA, dataForFactory.stCodeFrom, DBActionOrigin.FROM_DB);
                    } else { // PEM ab leeren, da irrelevant
                        factoryData.setFieldValue(iPartsConst.FIELD_DFD_PEMA, "", DBActionOrigin.FROM_DB);
                        factoryData.setFieldValue(iPartsConst.FIELD_DFD_PEMTA, "", DBActionOrigin.FROM_DB);
                        factoryData.setFieldValue(iPartsConst.FIELD_DFD_STCA, "", DBActionOrigin.FROM_DB);
                    }

                    // PEM bis
                    if (iPartsEntry.isPEMToRelevant()) {
                        factoryData.setFieldValue(iPartsConst.FIELD_DFD_PEMB, dataForFactory.pemTo, DBActionOrigin.FROM_DB);
                        String dateToStr = "";
                        if ((dataForFactory.dateTo > 0) && (dataForFactory.dateTo != Long.MAX_VALUE)) {
                            dateToStr = String.valueOf(dataForFactory.dateTo);
                        }
                        factoryData.setFieldValue(iPartsConst.FIELD_DFD_PEMTB, dateToStr, DBActionOrigin.FROM_DB);
                        factoryData.setFieldValue(iPartsConst.FIELD_DFD_STCB, dataForFactory.stCodeTo, DBActionOrigin.FROM_DB);
                    } else { // PEM bis leeren, da irrelevant
                        factoryData.setFieldValue(iPartsConst.FIELD_DFD_PEMB, "", DBActionOrigin.FROM_DB);
                        factoryData.setFieldValue(iPartsConst.FIELD_DFD_PEMTB, "", DBActionOrigin.FROM_DB);
                        factoryData.setFieldValue(iPartsConst.FIELD_DFD_STCB, "", DBActionOrigin.FROM_DB);
                    }

                    if (StrUtils.isValid(aggregateType)) {
                        factoryData.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DFD_AGGREGATE_TYPE,
                                                             aggregateType, true, DBActionOrigin.FROM_DB);
                    }

                    // Flag ob es virtuelle Werksdaten sind setzen
                    factoryData.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DFD_INHERITED_FACTORY_DATA,
                                                         SQLStringConvert.booleanToPPString(dataForFactory.isInherited()), true,
                                                         DBActionOrigin.FROM_DB);

                    // Filter-Informationen setzen
                    factoryData.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DFD_FILTER_INFO,
                                                         dataForFactory.getFilterInfo(), true, DBActionOrigin.FROM_DB);

                    list.add(factoryData, DBActionOrigin.FROM_DB);
                }
            }
        }
        return list;
    }

    /**
     * Setzt die Werte für die virtuellen Felder "Rückmeldedaten vorhanden" anhand der übergebenen Map. Diese enthält
     * pro PEM die Rückmeldedaten inklusive der Ausreißer. Eine evetuelle Filterung der Daten passiert schon bei der Bestimmung
     *
     * @param list
     * @param entry
     * @param retailFilter
     * @param responseDataMap Map von PEM auf Rückmeldedaten inklusive Ausreißer
     */
    public static void setResponseDataFlags(iPartsDataFactoryDataList list, iPartsDataPartListEntry entry,
                                            boolean retailFilter, Map<String, List<iPartsResponseDataWithSpikes>> responseDataMap) {
        // Werte für virtuelle Felder "Response-Daten für PEM verfügbar" setzen
        for (iPartsDataFactoryData factoryData : list.getAsList()) {
            if (!retailFilter || ((entry != null) && entry.isPEMFromRelevant())) { // PEM ab relevant?
                String pemFrom = factoryData.getAttribute(iPartsConst.FIELD_DFD_PEMA).getAsString();
                boolean responseDataAvailable = ResponseDataHelper.isResponseDataAvailableForPEM(pemFrom, responseDataMap);
                factoryData.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DFD_PEMA_RESPONSE_DATA_AVAILABLE,
                                                     SQLStringConvert.booleanToPPString(responseDataAvailable), true,
                                                     DBActionOrigin.FROM_DB);
            }

            if (!retailFilter || ((entry != null) && entry.isPEMToRelevant())) { // PEM bis relevant?
                String pemTo = factoryData.getAttribute(iPartsConst.FIELD_DFD_PEMB).getAsString();
                boolean responseDataAvailable = ResponseDataHelper.isResponseDataAvailableForPEM(pemTo, responseDataMap);
                factoryData.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DFD_PEMB_RESPONSE_DATA_AVAILABLE,
                                                     SQLStringConvert.booleanToPPString(responseDataAvailable), true,
                                                     DBActionOrigin.FROM_DB);
            }
        }
    }

    /**
     * Ermittelt per Tiefensuche im Ersetzungsgraphen die jeweiligen Blattknoten, also die Knoten die nicht weiter durch
     * ihre Vorgänger bzw. Nachfolger ersetzt werden können.
     * Dazu werden die RFMEA bzw. RFMEN Flags der jeweiligen Ersetzungen ausgewertet und nur tatsächlich austauschbare
     * Ersetzungen weiter verfolgt.
     * Es werden nur Blattknoten zurückgeliefert die das jeweilige Flag für "PEM auswerten" gesetzt haben.
     * Es wird sichergestellt, dass der Ausgangs-Stücklisteneintrag (Startpunkt) nicht in der Ergebnisliste enthalten ist.
     *
     * @param partListEntry Startpunkt des Ersetzungsgraphen
     * @param isPredecessor bei <i>true</i> wird die Vorgänger Richtung abgeprüft, sonst die Nachfolger Richtung
     * @return Liste der ermittelten Endpunkte bzw. {@code null} falls es keine gibt
     */
    public static Collection<iPartsDataPartListEntry> findFinalReplacements(iPartsDataPartListEntry partListEntry, boolean isPredecessor) {
        if (isPredecessor && !partListEntry.hasPredecessors()) {
            return null;
        }
        if (!isPredecessor && !partListEntry.hasSuccessors()) {
            return null;
        }

        // Bereits besuchte Stücklisteneinträge merken um Endlosschleifen zu verhindern
        Set<iPartsDataPartListEntry> visitedEntries = new HashSet<>();

        Set<iPartsDataPartListEntry> result = new LinkedHashSet<>(); // LinkedHashSet für reproduzierbare Reihenfolge
        Stack<iPartsDataPartListEntry> replacementStack = new Stack<>();

        // Es müssen die Original "PEM ab/bis auswerten" Flags vom jeweiligen Stücklisteneintrag ausgewertet werden
        replacementStack.push(partListEntry);
        while (!replacementStack.isEmpty()) {
            iPartsDataPartListEntry currentNode = replacementStack.pop();
            if (visitedEntries.add(currentNode)) {
                Collection<iPartsReplacement> replacements;
                if (isPredecessor) {
                    replacements = currentNode.getPredecessors();
                } else {
                    replacements = currentNode.getSuccessors();
                }
                if ((replacements != null) && !replacements.isEmpty()) {
                    for (iPartsReplacement replacement : replacements) {
                        if (replacement.source == iPartsReplacement.Source.PRIMUS) {
                            if (!isPredecessor) { // PRIMUS-Vorgänger nicht berücksichtigen (können inzwischen eigentlich gar nicht mehr vorhanden sein)
                                result.add(currentNode); // möglicher letzter Nachfolger in der Kette
                            }
                            continue;
                        }

                        EtkDataPartListEntry replacementEntry = null;
                        if (isPredecessor) {
                            iPartsRFMEN rfmen = new iPartsRFMEN(replacement.rfmenFlags);
                            if (rfmen.isPredecessorReplaceable()) {
                                replacementEntry = replacement.predecessorEntry;
                            } else {
                                result.add(currentNode); // möglicher letzter Vorgänger in der Kette
                            }
                        } else {
                            iPartsRFMEA rfmea = new iPartsRFMEA(replacement.rfmeaFlags);
                            if (rfmea.isSuccessorReplaceable()) {
                                replacementEntry = replacement.successorEntry;
                            } else {
                                result.add(currentNode); // möglicher letzter Nachfolger in der Kette
                            }
                        }

                        if (replacementEntry instanceof iPartsDataPartListEntry) {
                            replacementStack.push((iPartsDataPartListEntry)replacementEntry);
                        }
                    }
                } else {
                    result.add(currentNode); // letzter Vorgänger bzw. Nachfolger in der Kette
                }
            }
        }

        // Den ursprünglichen Eintrag wieder aus der Liste entfernen falls er enthalten ist
        result.remove(partListEntry);

        if (!result.isEmpty()) {
            return result;
        } else {
            return null;
        }
    }

    /**
     * Wenn es mehrere Endpunkte in der Ersetzungskette gibt, dann wird pro Werk der Datensatz bestimmt, der das jüngste
     * bzw. älteste PEM Datum ab/bis hat (je nach <i>pemFrom</i>).
     * Die Ergebnisdaten enthalten pro Werk genau einen Eintrag
     *
     * @param entries Ermittelte Vorgänger bzw. Nachfolger
     * @param pemFrom Bei {@code true} wird das jüngste PEM ab Datum gesucht, bei {@code false} das älteste PEM bis Datum
     * @param project
     * @return Pro Werk die ermittelten kombinierten Werkseinsatzdaten
     */
    public static iPartsFactoryData findRelevantDataPerFactory(Collection<iPartsDataPartListEntry> entries, final boolean pemFrom,
                                                               final EtkProject project) {
        if ((entries == null) || entries.isEmpty()) {
            return null;
        }

        iPartsDataPartListEntry firstPartListEntry = entries.iterator().next();
        if (entries.size() == 1) {
            return firstPartListEntry.getFactoryDataForRetailWithoutReplacements();
        }

        // Eval PEM ab/bis Flags für die relevanten Werksdaten merken
        final Map<iPartsFactoryDataId, Boolean> evalPEMflags = new HashMap<>();

        // In allEntries werden die Werkseinsatzdaten für jeden PartListEntry aus entries pro Werk zusammengetragen
        Map<String, List<iPartsFactoryData.DataForFactory>> allEntries = new HashMap<>();
        for (iPartsDataPartListEntry entry : entries) {
            iPartsFactoryData factoryDataForRetail = entry.getFactoryDataForRetailWithoutReplacements();
            if ((factoryDataForRetail != null) && (factoryDataForRetail.getFactoryDataMap() != null)) {

                boolean pemFlag;
                if (pemFrom) {
                    pemFlag = factoryDataForRetail.isEvalPemFrom();
                } else {
                    pemFlag = factoryDataForRetail.isEvalPemTo();
                }

                for (Map.Entry<String, List<iPartsFactoryData.DataForFactory>> factoryDataMapContent : factoryDataForRetail.getFactoryDataMap().entrySet()) {
                    String factory = factoryDataMapContent.getKey();
                    List<iPartsFactoryData.DataForFactory> list = allEntries.get(factory);
                    if (list == null) {
                        list = new DwList<>();
                        allEntries.put(factory, list);
                    }

                    // DAIMLER-6825: Werkseinsatzdaten ignorieren, wenn das entsprechende "PEM auswerten"-Flag nicht gesetzt ist
                    if (pemFlag) {
                        list.addAll(factoryDataMapContent.getValue());
                    }

                    for (iPartsFactoryData.DataForFactory dataForFactory : factoryDataMapContent.getValue()) {
                        if (dataForFactory.factoryDataId != null) {
                            evalPEMflags.put(dataForFactory.factoryDataId, pemFlag);
                        }
                    }
                }
            }
        }

        // Aggregatetyp über den ersten Stücklisteneintrag bestimmen (ist ja sowieso für alle identisch)
        final String aggregateType = firstPartListEntry.getAggregateType();
        boolean isAggregateTypeCar = aggregateType.equals(iPartsConst.AGGREGATE_TYPE_CAR);

        // Unterscheidung zwischen Fahrzeug und Aggregaten
        final int endNumberLength;
        if (isAggregateTypeCar) {
            endNumberLength = FinId.IDENT_NO_LENGTH;
        } else {
            // Beim Millionenüberlauf spielt auch die Länge der Endnummer eine Rolle. Diese hängt vom Aggregate-Ident ab, den wir hier nicht haben.
            // Wir gehen daher den Umweg über eine virtuelle Aggregatedatenkarte mit Dummy Ident
            endNumberLength = AggregateDataCard.getEndNumberLengthForAggregateType(project, aggregateType);
        }

        final iPartsFactoryModel factoryModelCache = iPartsFactoryModel.getInstance(project);
        iPartsFactoryData resultFactoryData = new iPartsFactoryData();
        for (final Map.Entry<String, List<iPartsFactoryData.DataForFactory>> factoryEntry : allEntries.entrySet()) {
            String factory = factoryEntry.getKey();

            // Suche nach den Einträgen mit kleinstem oder größtem PEM Datum ab/bis
            DwList<iPartsFactoryData.DataForFactory> sortedList = new DwList<>(factoryEntry.getValue());
            if (sortedList.size() > 1) {
                Collections.sort(sortedList, new Comparator<iPartsFactoryData.DataForFactory>() {
                    //a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second
                    @Override
                    public int compare(iPartsFactoryData.DataForFactory o1, iPartsFactoryData.DataForFactory o2) {
                        // Berücksichtigung von ungültigen Datumswerten sowie unendlich bei PEM bis
                        if (pemFrom) {
                            // DAIMLER-6825: Zunächst die Idents vergleichen
                            int o1MinEndNumberFrom = getMinEndnumberFrom(o1);
                            int o2MinEndNumberFrom = getMinEndnumberFrom(o2);

                            // Hat o1 oder o2 gültige Idents?
                            if ((o1MinEndNumberFrom != Integer.MAX_VALUE) || (o2MinEndNumberFrom != Integer.MAX_VALUE)) {
                                int identCompare = ((Integer)o1MinEndNumberFrom).compareTo(o2MinEndNumberFrom);
                                if (identCompare != 0) { // Gleiche Idents liefern 0 -> PEM-Termine vergleichen
                                    return identCompare;
                                }
                            }

                            // Bei gleichen (oder nicht vorhandenen) Idents die PEM-Termine vergleichen
                            long o1DateFrom = o1.dateFrom;
                            long o2DateFrom = o2.dateFrom;

                            // Bei ungültigem Datum Long.MAX_VALUE setzen, damit dieses ganz hinten in der Liste landet
                            if (o1DateFrom == iPartsFactoryData.INVALID_DATE) {
                                o1DateFrom = Long.MAX_VALUE;
                            }
                            if (o2DateFrom == iPartsFactoryData.INVALID_DATE) {
                                o2DateFrom = Long.MAX_VALUE;
                            }

                            return ((Long)o1DateFrom).compareTo(o2DateFrom);
                        } else {
                            // DAIMLER-6825: Zunächst die Idents vergleichen
                            int o1MaxEndNumberTo = getMaxEndnumberTo(o1);
                            int o2MaxEndNumberTo = getMaxEndnumberTo(o2);

                            // Hat o1 oder o2 gültige Idents?
                            if ((o1MaxEndNumberTo != Integer.MIN_VALUE) || (o2MaxEndNumberTo != Integer.MIN_VALUE)) {
                                int identCompare = ((Integer)o2MaxEndNumberTo).compareTo(o1MaxEndNumberTo);
                                if (identCompare != 0) { // Gleiche Idents liefern 0 -> PEM-Termine vergleichen
                                    return identCompare;
                                }
                            }

                            // Bei gleichen (oder nicht vorhandenen) Idents die PEM-Termine vergleichen
                            long o1DateTo = o1.dateTo;
                            long o2DateTo = o2.dateTo;

                            // Bei ungültigem Datum Long.MIN_VALUE setzen, damit dieses ganz hinten in der Liste landet
                            if (o1DateTo == iPartsFactoryData.INVALID_DATE) {
                                o1DateTo = Long.MIN_VALUE;
                            } else if (o1DateTo == 0) { // unendlich
                                o1DateTo = Long.MAX_VALUE;
                            }
                            if (o2DateTo == iPartsFactoryData.INVALID_DATE) {
                                o2DateTo = Long.MIN_VALUE;
                            } else if (o2DateTo == 0) { // unendlich
                                o2DateTo = Long.MAX_VALUE;
                            }

                            return ((Long)o2DateTo).compareTo(o1DateTo);
                        }
                    }

                    private int getMinEndnumberFrom(iPartsFactoryData.DataForFactory dataForFactory) {
                        int minEndNumberFrom = Integer.MAX_VALUE;
                        List<iPartsFactoryData.Ident> validIdentsFrom = dataForFactory.getIdentsFrom(endNumberLength);
                        if (validIdentsFrom != null) {
                            iPartsModelId modelId = new iPartsModelId(dataForFactory.seriesNumber);
                            for (iPartsFactoryData.Ident validIdentFrom : validIdentsFrom) {
                                int endNumberFrom = factoryModelCache.getSerialNumberWithOverflowForFactoryNumber(factoryEntry.getKey(),
                                                                                                                  validIdentFrom.factorySign,
                                                                                                                  modelId,
                                                                                                                  aggregateType,
                                                                                                                  validIdentFrom.endNumber,
                                                                                                                  endNumberLength);
                                if (endNumberFrom != FinId.INVALID_SERIAL_NUMBER) {
                                    minEndNumberFrom = Math.min(minEndNumberFrom, endNumberFrom);
                                }
                            }
                        }
                        return minEndNumberFrom;
                    }

                    private int getMaxEndnumberTo(iPartsFactoryData.DataForFactory dataForFactory) {
                        int maxEndNumberTo = Integer.MIN_VALUE;
                        List<iPartsFactoryData.Ident> validIdentsTo = dataForFactory.getIdentsTo(endNumberLength);
                        if (validIdentsTo != null) {
                            iPartsModelId modelId = new iPartsModelId(dataForFactory.seriesNumber);
                            for (iPartsFactoryData.Ident validIdentTo : validIdentsTo) {
                                int endNumberTo = factoryModelCache.getSerialNumberWithOverflowForFactoryNumber(factoryEntry.getKey(),
                                                                                                                validIdentTo.factorySign,
                                                                                                                modelId,
                                                                                                                aggregateType,
                                                                                                                validIdentTo.endNumber,
                                                                                                                endNumberLength);
                                if (endNumberTo != FinId.INVALID_SERIAL_NUMBER) {
                                    maxEndNumberTo = Math.max(maxEndNumberTo, endNumberTo);
                                }
                            }
                        }
                        return maxEndNumberTo;
                    }
                });
            }

            if (!sortedList.isEmpty()) {
                iPartsFactoryData.DataForFactory dataForFactory = sortedList.get(0);
                DwList<iPartsFactoryData.DataForFactory> resultList = new DwList<>(1);
                resultList.add(dataForFactory);
                resultFactoryData.setDataForFactory(factory, resultList);
            }
        }

        if (resultFactoryData.getFactoryDataMap() == null) {
            return null;
        }

        // Eval PEM ab/bis Flags die zu den ermittelten Werksdaten gehören setzen/vererben
        if (pemFrom) {
            resultFactoryData.setEvalPemFrom(false);
        } else {
            resultFactoryData.setEvalPemTo(false);
        }
        for (List<iPartsFactoryData.DataForFactory> dataForFactories : resultFactoryData.getFactoryDataMap().values()) {
            for (iPartsFactoryData.DataForFactory dataForFactory : dataForFactories) {
                if (dataForFactory.factoryDataId != null) {
                    Boolean pemFlag = evalPEMflags.get(dataForFactory.factoryDataId);
                    if ((pemFlag != null) && pemFlag) {
                        if (pemFrom) {
                            resultFactoryData.setEvalPemFrom(true);
                        } else {
                            resultFactoryData.setEvalPemTo(true);
                        }
                        return resultFactoryData;
                    }
                }
            }
        }

        return resultFactoryData;
    }

    /**
     * Liefert den Datensatz mit dem höchsten ADAT und optional Sequenznummer aus der übergebenen Liste.
     *
     * @param dataObjectList
     * @param adatField      Feld für das ADAT
     * @param seqNoField     Optionales Feld für die Sequenznummer
     * @return
     */
    public static EtkDataObject getDataObjectWithNewestAdat(List<? extends EtkDataObject> dataObjectList, final String adatField,
                                                            final String seqNoField) {
        if ((dataObjectList == null) || dataObjectList.isEmpty()) {
            return null;
        }
        if (dataObjectList.size() > 1) {
            Collections.sort(dataObjectList, new Comparator<EtkDataObject>() {
                @Override
                public int compare(EtkDataObject o1, EtkDataObject o2) {
                    // Zuerst nach ADAT sortieren
                    int result = o1.getFieldValue(adatField).compareTo(o2.getFieldValue(adatField));
                    if ((result != 0) || StrUtils.isEmpty(seqNoField)) {
                        return result;
                    }

                    // Danach optional nach Sequenznummer sortieren
                    return o1.getFieldValue(seqNoField).compareTo(o2.getFieldValue(seqNoField));
                }
            });
        }
        return dataObjectList.get(dataObjectList.size() - 1);
    }

    /**
     * Füllt die übergebene Liste mit Werkseinsatzdaten, die bezogen auf ihren Status gefiltert wurden. Bei nicht aktiven
     * Retailfilter werden alle Datensätze angezeigt. Ist der Retailfilter gesetzt, dürfen nur freigegebene Datensätze angezeigt
     * werden. Sind mehrere freigegebene Datensätze pro Liste enthalten, darf nur der jüngste angezeigt werden.
     *
     * @param dataList
     * @param dataForFactory
     * @param retailFilter
     * @param releaseField
     * @param adatField
     */
    public static void fillListWithFilteredFactoryData(EtkDataObjectList dataList,
                                                       Collection<? extends List<? extends EtkDataObject>> dataForFactory,
                                                       boolean retailFilter, String releaseField, String adatField) {

        for (List<? extends EtkDataObject> datasetsForId : dataForFactory) {
            List<EtkDataObject> allDataList = new ArrayList<>();
            List<EtkDataObject> releasedDataList = new ArrayList<>();
            for (EtkDataObject data : datasetsForId) {
                allDataList.add(data);

                // Freigegebene Datensätze sollen immer ausgegeben werden.
                if (iPartsDataReleaseState.isReleased(data.getFieldValue(releaseField))) {
                    releasedDataList.add(data);
                }
            }
            if (retailFilter) {
                // Bei mehreren freigegebenen Datensätzen muss der aktuellste ausgegeben werden.
                EtkDataObject dataObjectWithNewestAdat = getDataObjectWithNewestAdat(releasedDataList, adatField, null);
                if (dataObjectWithNewestAdat != null) {
                    dataList.add(dataObjectWithNewestAdat, DBActionOrigin.FROM_DB);
                }
            } else {
                dataList.addAll(allDataList, DBActionOrigin.FROM_DB);
            }
        }
    }

    /**
     * Sortiert die Werksdaten nach ID (ohne Adat) und filtert die Datensätze nach neustem Adat (pro ID)
     *
     * @param dataList
     * @param adatField  Feld für das ADAT
     * @param seqNoField Optionales Feld für die Sequenznummer
     */
    public static void filterListByNewestFactoryData(iPartsDataFactoryDataList dataList, String adatField, String seqNoField) {
        Map<iPartsFactoryDataId, List<iPartsDataFactoryData>> factoryDataForId = new HashMap<>();
        // Datensätze werden nach ihrer ID gruppiert, weil die ADAT- und Statusprüfung auf Basis der "gleichen" Datensätze
        // geschehen muss. Weil das ADAT aber ein Bestandteil des Schlüssels ist, wird hier ein künstlicher Schlüssel
        // - ohne ADAT - erzeugt und als Gruppierungsschlüssel für die einzelnen Datensätze verwendet.
        // Bei gleichem ADAT gewinnt die höchste Sequenznummer
        for (iPartsDataFactoryData factoryData : dataList) {
            iPartsFactoryDataId existingId = factoryData.getAsId();
            iPartsFactoryDataId idWithoutAdat = new iPartsFactoryDataId(existingId.getGuid(), existingId.getFactory(),
                                                                        existingId.getSplitAttribute(), "", existingId.getDataId());
            List<iPartsDataFactoryData> dataFactoriesForIdWithoutAdat = factoryDataForId.get(idWithoutAdat);
            if (dataFactoriesForIdWithoutAdat == null) {
                dataFactoriesForIdWithoutAdat = new ArrayList<>();
                factoryDataForId.put(idWithoutAdat, dataFactoriesForIdWithoutAdat);
            }
            dataFactoriesForIdWithoutAdat.add(factoryData);
        }
        dataList.clear(DBActionOrigin.FROM_DB);
        for (List<? extends EtkDataObject> datasetsForId : factoryDataForId.values()) {
            iPartsDataFactoryData factoryDataWithNewestAdat = (iPartsDataFactoryData)getDataObjectWithNewestAdat(datasetsForId,
                                                                                                                 adatField,
                                                                                                                 seqNoField);
            if (factoryDataWithNewestAdat != null) {
                dataList.add(factoryDataWithNewestAdat, DBActionOrigin.FROM_DB);
            }
        }
    }

    /**
     * Filtert abhängig vom Retailfilter alle Ausreißer heraus, deren Status nicht "freigegeben" ist.
     *
     * @param responseSpikeList
     * @param retailFilter
     */
    public static iPartsDataResponseSpikeList getFilteredResponseSpikes(iPartsDataResponseSpikeList responseSpikeList, boolean retailFilter) {
        if (!retailFilter) { // Ohne Retailfilter muss nichts gemacht werden
            return responseSpikeList;
        }

        if (responseSpikeList == null) {
            return null;
        }

        // Filterung auf Status "freigegeben"
        iPartsDataResponseSpikeList filteredResponseSpikeList = new iPartsDataResponseSpikeList();
        for (iPartsDataResponseSpike dataResponseSpike : responseSpikeList) {
            if (dataResponseSpike.isReleased()) {
                filteredResponseSpikeList.add(dataResponseSpike, DBActionOrigin.FROM_DB);
            }
        }

        return filteredResponseSpikeList;
    }

    /***
     * Füllt Attribute eines Werkseinsatzdatensatzes mit den Daten aus dem BCTE Schlüssel und dem Stücklisteneintrag
     * @param factoryData
     * @param partListEntry
     * @param dialogBCTEPrimaryKey
     */
    public static void newFactoryDataFromPartListEntry(iPartsDataFactoryData factoryData, EtkDataPartListEntry partListEntry, iPartsDialogBCTEPrimaryKey dialogBCTEPrimaryKey) {

        // Produktgruppe und BCTE-Schlüssel in den neuen Werkseinsatzdaten vom Stücklisteneintrag übernehmen
        factoryData.setFieldValue(iPartsConst.FIELD_DFD_PRODUCT_GRP, partListEntry.getFieldValue(iPartsConst.FIELD_K_PRODUCT_GRP),
                                  DBActionOrigin.FROM_EDIT);
        HmMSmId hmMSmId = dialogBCTEPrimaryKey.getHmMSmId();
        factoryData.setFieldValue(iPartsConst.FIELD_DFD_SERIES_NO, hmMSmId.getSeries(), DBActionOrigin.FROM_EDIT);
        factoryData.setFieldValue(iPartsConst.FIELD_DFD_HM, hmMSmId.getHm(), DBActionOrigin.FROM_EDIT);
        factoryData.setFieldValue(iPartsConst.FIELD_DFD_M, hmMSmId.getM(), DBActionOrigin.FROM_EDIT);
        factoryData.setFieldValue(iPartsConst.FIELD_DFD_SM, hmMSmId.getSm(), DBActionOrigin.FROM_EDIT);
        factoryData.setFieldValue(iPartsConst.FIELD_DFD_POSE, dialogBCTEPrimaryKey.getPosE(), DBActionOrigin.FROM_EDIT);
        factoryData.setFieldValue(iPartsConst.FIELD_DFD_POSV, dialogBCTEPrimaryKey.getPosV(), DBActionOrigin.FROM_EDIT);
        factoryData.setFieldValue(iPartsConst.FIELD_DFD_WW, dialogBCTEPrimaryKey.getWW(), DBActionOrigin.FROM_EDIT);
        factoryData.setFieldValue(iPartsConst.FIELD_DFD_ET, dialogBCTEPrimaryKey.getET(), DBActionOrigin.FROM_EDIT);
        factoryData.setFieldValue(iPartsConst.FIELD_DFD_AA, dialogBCTEPrimaryKey.getAA(), DBActionOrigin.FROM_EDIT);
        factoryData.setFieldValue(iPartsConst.FIELD_DFD_SDATA, dialogBCTEPrimaryKey.getSData(), DBActionOrigin.FROM_EDIT);
    }

    /**
     * Es sollen nur Daten kopiert werden, die freigegeben sind, ausßerdem sind die AS Werkseinsatzdaten den Produktionsdaten
     * vorzuziehen
     *
     * @param allFactoryData
     * @return
     */
    public static iPartsDataFactoryDataList getDataForCopy(iPartsDataFactoryDataList allFactoryData) {

        List<iPartsDataFactoryData> input = allFactoryData.getAsList();
        List<? extends iPartsEtkDataObjectFactoryDataInterface> output = getDataForCopy(input);


        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();

        for (iPartsEtkDataObjectFactoryDataInterface outObj : output) {
            list.add(((iPartsDataFactoryData)outObj), DBActionOrigin.FROM_EDIT);
        }

        return list;
    }

    public static List<? extends iPartsEtkDataObjectFactoryDataInterface> getDataForCopy(List<? extends iPartsEtkDataObjectFactoryDataInterface> allFactoryData) {

        List<iPartsEtkDataObjectFactoryDataInterface> list = new ArrayList<>();
        List<iPartsEtkDataObjectFactoryDataInterface> listAS = new ArrayList<>();
        List<iPartsEtkDataObjectFactoryDataInterface> listBCTP = new ArrayList<>();

        // Nur feigegebene Werkseinsatzdaten
        // Sortieren nach Produktion und AS
        if ((allFactoryData != null) && !allFactoryData.isEmpty()) {
            for (iPartsEtkDataObjectFactoryDataInterface dataForFactory : allFactoryData) {
                if (dataForFactory.getReleaseState().equals(iPartsDataReleaseState.RELEASED)) {
                    if (dataForFactory.getDataId().equals(iPartsFactoryDataTypes.FACTORY_DATA_CONSTRUCTION.getDbValue())) {
                        listBCTP.add(dataForFactory);
                    } else {
                        listAS.add(dataForFactory);
                    }
                }
            }

            List<iPartsEtkDataObjectFactoryDataInterface> copy = new ArrayList<>(listBCTP);
            for (iPartsEtkDataObjectFactoryDataInterface fd : copy) {
                for (iPartsEtkDataObjectFactoryDataInterface as : listAS) {
                    if (fd.getFactory().equals(as.getFactory())) {
                        // Nur den AS Datensatz kopieren bei gleichem Werk
                        listBCTP.remove(fd);
                    }
                }
            }

            list.addAll(listAS);
            list.addAll(listBCTP);
        }
        return list;
    }

    /**
     * Sortiert die Werkseinsatzdaten zu Farbtabellen aus der Produktion (WX10, WX9 und WY9) absteigend. Diese müssen zum
     * gleichen SDA Wert nach ihrem Original-SDA Wert absteigend sortiert werden. Falls beide den gleichen Original-SDA Wert
     * haben, müssen sie nach ihrem ADAT Wert absteigend sortiert werden.
     *
     * @param factoryList
     */
    public static void sortProductionColorTableFactoryData(List<? extends EtkDataObject> factoryList) {
        Collections.sort(factoryList, new Comparator<EtkDataObject>() {
            @Override
            public int compare(EtkDataObject o1, EtkDataObject o2) {
                if ((o1 instanceof iPartsDataColorTableFactory) && (o2 instanceof iPartsDataColorTableFactory)) {
                    iPartsDataColorTableFactory o1FactoryData = (iPartsDataColorTableFactory)o1;
                    iPartsDataColorTableFactory o2FactoryData = (iPartsDataColorTableFactory)o2;
                    // Erst schauen, ob beide den gleichen SDA Wert im Schlüssel haben. Falls nicht, werden sie direkt
                    // nach dem SDA Wert im Sclüssel sortiert.
                    int result = o2FactoryData.getAsId().getSdata().compareTo(o1FactoryData.getAsId().getSdata());
                    if (result == 0) {
                        // SDA im Schlüssel ist gleich -> Sortieren nach Original-SDA
                        result = o2FactoryData.getOriginalSdata().compareTo(o1FactoryData.getOriginalSdata());
                        if (result == 0) {
                            // Original-SDA ist gleich -> Sortieren nach ADAT
                            result = o2FactoryData.getAsId().getAdat().compareTo(o1FactoryData.getAsId().getAdat());
                        }
                    }
                    return result;
                }
                return 0;
            }
        });
    }

    /**
     * Liefert zurück, ob es sich um relevante Werksdaten handelt. Relevant sind Werksdaten, wenn der Status ungleich
     * "nicht relevant" ist und wenn die Werke der Werksdaten gültig für das Produkt des Moduls (bzw. Produkte der SA) sind.
     *
     * @param ownerAssembly
     * @param factoryData
     * @param releaseStateField
     * @param factoryField
     * @return
     */
    public static boolean isRelevantFactoryData(iPartsDataAssembly ownerAssembly, EtkDataObject factoryData,
                                                String releaseStateField, String factoryField) {
        // Status Check
        iPartsDataReleaseState releaseState = iPartsDataReleaseState.getTypeByDBValue(factoryData.getFieldValue(releaseStateField));
        if ((releaseState == iPartsDataReleaseState.NOT_RELEVANT) || (releaseState == iPartsDataReleaseState.NOT_RELEVANT_READONLY)) {
            return false;
        }

        String factory = factoryData.getFieldValue(factoryField);
        // Ist die Werksangabe leer, soll nur die Statusprüfung erfolgen
        if (StrUtils.isEmpty(factory)) {
            return true;
        }

        Set<String> factories = new HashSet<>();
        // Bei einer SA Stückliste müssen die Werke zu allen Produkten berücksichtigt werden
        if (ownerAssembly.isSAAssembly()) {
            fillFactoriesForSaAssembly(factoryData.getEtkProject(), ownerAssembly, factories);
        } else {
            // Werke zum Produkt des Moduls bestimmen
            fillFactoriesForProductAssembly(factoryData.getEtkProject(), ownerAssembly, factories);
        }

        // Existieren keine Werke zum Produkt der Stückliste, soll die Prüfung entfallen
        if (factories.isEmpty()) {
            return true;
        }

        return factories.contains(factory);
    }

    /**
     * Befüllt das übergebene <code>productFactories</code> Set mit allen Werken zum Produkt, in dem die Stückliste
     * vorkommt
     *
     * @param project
     * @param ownerAssembly
     * @param productFactories
     */
    public static void fillFactoriesForProductAssembly(EtkProject project, iPartsDataAssembly ownerAssembly,
                                                       Set<String> productFactories) {
        iPartsProductId productIdFromModuleUsage = ownerAssembly.getProductIdFromModuleUsage();
        if (productIdFromModuleUsage != null) {
            iPartsProduct product = iPartsProduct.getInstance(project, productIdFromModuleUsage);
            productFactories.addAll(product.getProductFactories(project));
        }
    }

    /**
     * Befüllt das übergebene <code>productFactories</code> Set mit allen Werken zu allen Produkten zur übergebenen
     * SA Stückliste
     *
     * @param project
     * @param ownerAssembly
     * @param productFactories
     */
    public static void fillFactoriesForSaAssembly(EtkProject project, iPartsDataAssembly ownerAssembly,
                                                  Set<String> productFactories) {
        if (ownerAssembly.isSAAssembly()) {
            iPartsDataProductSAsList productSAsList = iPartsDataProductSAsList.loadProductSasForSaAssembly(project, ownerAssembly);
            if (!productSAsList.isEmpty()) {
                Set<iPartsProductId> productIds = productSAsList.getAsList().stream()
                        .map(productSA -> new iPartsProductId(productSA.getAsId().getProductNumber()))
                        .collect(Collectors.toSet());
                productIds.forEach(productId -> productFactories.addAll(iPartsProduct.getInstance(project, productId).getProductFactories(project)));
            }
        }
    }

    /**
     * Dialog Changes vom Typ Werkseinsatzdaten aus dem EtkDataObject erstellen
     *
     * @param project           EtkProject
     * @param dataObject        Das Datenobjekt aus dem DIALOG Changes Datensatz erzeugt werden soll
     * @param seriesNoFieldName Feldname für die Baureihe
     * @return Dialog Changes Datensatz
     */
    public static iPartsDataDIALOGChange getFactoryDataDIALOGChanges(EtkProject project, EtkDataObject dataObject, String seriesNoFieldName) {
        if (dataObject instanceof iPartsDataFactoryData) {
            iPartsDataFactoryData factoryData = (iPartsDataFactoryData)dataObject;
            iPartsDialogChangesId dialogChangesId = new iPartsDialogChangesId(iPartsDataDIALOGChange.ChangeType.FACTORY_DATA,
                                                                              factoryData.getAsId(), factoryData.getFieldValue(seriesNoFieldName),
                                                                              factoryData.getGUID(), "", "");
            return new iPartsDataDIALOGChange(project, dialogChangesId);
        }

        return null;
    }
}
