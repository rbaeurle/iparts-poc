/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEntrySourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseSpike;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseDataWithSpikes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsResponseSpikes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Hilfsklasse für Rückmeldedaten und Ausreißer in iParts.
 */
public class ResponseDataHelper {

    /**
     * Rückmeldedaten zur PEM bestimmen
     * bei aktivem Retailfilter werden anhand der Idents vom Stücklisteneintrag die Rückmeldedaten aus dem Cache
     * ermitttelt und angezeigt. Es findet keine zusätzliche Filterung statt weil die Idents bereits gefiltert sind
     *
     * @param pem
     * @param partlistEntry
     * @param retailFilter  Flag, ob der Retail-Filter gesetzt ist.
     * @param project
     * @return
     */
    public static List<iPartsResponseDataWithSpikes> getResponseDataForPEM(String pem, EtkDataPartListEntry partlistEntry,
                                                                           boolean retailFilter, EtkProject project, boolean isPemFrom) {
        List<iPartsResponseDataWithSpikes> responseDataList = new ArrayList<>();
        if (retailFilter && (partlistEntry instanceof iPartsDataPartListEntry)) {
            iPartsDataPartListEntry iPartsEntry = (iPartsDataPartListEntry)partlistEntry;
            List<iPartsDataResponseData> responseDataForPem = iPartsResponseData.getInstance(project).getResponseData(pem);
            if ((responseDataForPem != null) && !responseDataForPem.isEmpty()) {
                // ab hier mit geklonten Rückmeldedaten arbeiten, sonst würden die Daten im Cache manipuliert werden
                List<iPartsDataResponseData> clonedResponseData = new DwList<>(responseDataList.size());
                for (iPartsDataResponseData responseData : responseDataForPem) {
                    clonedResponseData.add(responseData.cloneMe(project));
                }
                responseDataForPem = clonedResponseData;

                filterResponseData(responseDataForPem, iPartsEntry); // Rückmeldedaten nach AA und BR filtern
                // prüfen ob Konstruktion oder Retail
                iPartsModuleTypes moduleType = iPartsModuleTypes.getType(iPartsEntry.getOwnerAssembly().getEbeneName());
                iPartsFactoryData factoryDataForPartlist;
                if (moduleType.isConstructionRelevant()) {
                    factoryDataForPartlist = iPartsEntry.getFactoryDataForConstruction();
                } else {
                    factoryDataForPartlist = iPartsEntry.getFactoryDataForRetail();
                }

                if ((factoryDataForPartlist != null) && (factoryDataForPartlist.getFactoryDataMap() != null)) {
                    for (List<iPartsFactoryData.DataForFactory> factoryData : factoryDataForPartlist.getFactoryDataMap().values()) {
                        for (iPartsFactoryData.DataForFactory dataForFactory : factoryData) {
                            Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> idents;
                            if (isPemFrom) {
                                idents = dataForFactory.identsFrom;
                            } else {
                                idents = dataForFactory.identsTo;
                            }

                            if (idents != null) {
                                for (Map.Entry<iPartsFactoryData.IdentWithModelNumber, Set<String>> ident : idents.entrySet()) {
                                    iPartsFactoryData.IdentWithModelNumber identWithModel = ident.getKey();
                                    Set<String> spikes = ident.getValue();

                                    // Ausreißer aus dem Cache hinzufügen
                                    responseDataList.addAll(getSpikesForResponseData(responseDataForPem, dataForFactory.factoryDataId.getFactory(),
                                                                                     identWithModel, spikes, iPartsEntry, project));
                                }
                            }
                        }
                    }
                }
            }
        } else {
            return getResponseDataForPEM(pem, project);
        }
        return responseDataList;
    }

    public static List<iPartsResponseDataWithSpikes> getSpikesForResponseData(List<iPartsDataResponseData> responseDataForPem,
                                                                              String factoryNumber, iPartsFactoryData.IdentWithModelNumber identWithModel,
                                                                              Set<String> spikes, EtkDataPartListEntry partListEntry,
                                                                              EtkProject project) {
        List<iPartsResponseDataWithSpikes> responseDataList = new DwList<>();
        for (iPartsDataResponseData responseData : responseDataForPem) {
            // Primärschlüssel (außer ADAT und beim Baumuster mit Korrektur) sowie Lenkung vergleichen; Baureihe und Ausführungsart
            // muss extern bereits gefiltert werden
            if (factoryNumber.equals(responseData.getAsId().getFactory()) && identWithModel.ident.equals(responseData.getAsId().getIdent())
                && identWithModel.model.equals(responseData.getCorrectedModelNumber()) && identWithModel.steering.equals(responseData.getFieldValue(iPartsConst.FIELD_DRD_STEERING))) {
                iPartsResponseDataWithSpikes responseDataWithSpikes = new iPartsResponseDataWithSpikes(responseData);
                responseDataWithSpikes.addResponseSpikes(spikes, partListEntry, project);
                responseDataList.add(responseDataWithSpikes);
            }
        }
        return responseDataList;
    }

    /**
     * Rückmeldedaten nach Baureihe und Ausführungsart filtern.
     *
     * @param responseDataList
     * @param partlistEntry
     */
    public static void filterResponseData(List<iPartsDataResponseData> responseDataList, EtkDataPartListEntry partlistEntry) {
        filterResponseDataOrSpikes(responseDataList, iPartsConst.FIELD_DRD_SERIES_NO, iPartsConst.FIELD_DRD_AA, partlistEntry);
    }

    /**
     * Ausreißer nach Baureihe und Ausführungsart filtern.
     *
     * @param responseSpikesList
     * @param partlistEntry
     */
    public static void filterResponseSpikes(Collection<iPartsDataResponseSpike> responseSpikesList, EtkDataPartListEntry partlistEntry) {
        filterResponseDataOrSpikes(responseSpikesList, iPartsConst.FIELD_DRS_SERIES_NO, iPartsConst.FIELD_DRS_AA, partlistEntry);
    }

    /**
     * Daten nach Baureihe und Ausführungsart filtern.
     *
     * @param dataList
     * @param fieldNameSeriesNo
     * @param fieldNameAA
     * @param partlistEntry
     */
    private static void filterResponseDataOrSpikes(Collection<? extends EtkDataObject> dataList, String fieldNameSeriesNo, String fieldNameAA,
                                                   EtkDataPartListEntry partlistEntry) {
        /**
         * Bestimmung von Baureihe und Ausführungsart (AA).
         *
         * Die Baureihe wird aus dem Original-BCTE-Schlüssel einer DIALOG-Stückliste ausgelesen.
         * Für mich wirkte das erst wie eine Art Notlösung da der Fahrzeugkontext oder Navigationszustand im Related Info
         * nicht zur Verfügung steht (warum eigentlich nicht?).
         * Nachtrag: dass der NavigationContext fehlte war ein Fehler. Mittlerweile könnten wir also auch über diesen Weg.
         * Hinsichtlich der Beimischung von Aggregaten wäre es aber tatsächlich falsch hier das Produkt aus der Navigation zu
         * nehmen, da dies für Aggregatestücklisten falsch wäre. Hier muss die Info am Stücklisteneintrag gespeichert werden.
         *
         * Die AA würde ebenfalls im BCTE-Schlüssel stecken. Die wird bei der Übernahme von Stücklisteneinträgen aber auch
         * am Stücklisteneintrag (K_AA) gespeichert. Das ist also redundant.
         *
         * In den Rückmeldedaten gibt es dann auch noch Lenkung und Aggregatetyp. Hierüber wird in den AKs aber nichts gefordert.
         * Das sind normale Felder (also nicht OK). Das heißt hier können wir einen Datensatz in der Gültigkeit einschränken.
         * An beides kommen wir wohl über das Produkt (Achtung bei zugemischten Aggregaten! Dort müsste das Aggregate-Produkt
         * zur Filterung verwendet werden). Der Weg, um an das Produkt zu kommen wäre:
         * - EtkRelatedInfoData -> NavigationPath
         * - iPartsProductId productId = assembly.getProductIdFromNavPath(navigationPath);
         */

        String entrySeriesNo = "";
        iPartsEntrySourceType sourceType = iPartsEntrySourceType.getFromDbValue(partlistEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_TYPE));
        if (sourceType == iPartsEntrySourceType.DIALOG) {
            String sourceContext = partlistEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_CONTEXT);
            HmMSmId hmMSmId = HmMSmId.getHmMSmIdFromDIALOGSourceContext(sourceContext);
            if (hmMSmId != null) {
                entrySeriesNo = hmMSmId.getSeries();
            }
        }

        String entryAA = partlistEntry.getAttribute(iPartsConst.FIELD_K_AA).getAsString();

        // filtern
        for (Iterator<? extends EtkDataObject> iter = dataList.iterator(); iter.hasNext(); ) {
            EtkDataObject dataObject = iter.next();
            String seriesNo = dataObject.getFieldValue(fieldNameSeriesNo);
            String aa = dataObject.getFieldValue(fieldNameAA);
            if (!filterResponseDataOrSpikesforDIALOGEntry(seriesNo, aa, entrySeriesNo, entryAA)) {
                iter.remove();
            }
        }
    }


    public static List<iPartsResponseDataWithSpikes> getResponseDataForPEM(String pem, EtkProject project) {
        List<iPartsResponseDataWithSpikes> responseDataList = new ArrayList<>();
        if (iPartsResponseData.getInstance(project).getResponseData(pem) != null) {
            for (iPartsDataResponseData iPartsDataResponseData : iPartsResponseData.getInstance(project).getResponseData(pem)) {
                iPartsResponseDataWithSpikes responseDataWithSpikes =
                        new iPartsResponseDataWithSpikes(iPartsDataResponseData);
                String ident = iPartsDataResponseData.getAsId().getIdent();
                Set<iPartsDataResponseSpike> responseSpikes = iPartsResponseSpikes.getInstance(project).getResponseSpikes(pem, ident);
                responseDataWithSpikes.addResponseSpikes(responseSpikes);
                responseDataList.add(responseDataWithSpikes);
            }

        }
        return responseDataList;
    }

    public static iPartsResponseDataWithSpikes findResponseDataForPem(
            List<iPartsResponseDataWithSpikes> responseDataList, String pem) {
        if ((responseDataList != null) && StrUtils.isValid(pem)) {
            for (iPartsResponseDataWithSpikes responseDataWithSpikes : responseDataList) {
                iPartsDataResponseData responseData = responseDataWithSpikes.getResponseData();
                if (responseData.getAsId().getPem().equals(pem)) {
                    return responseDataWithSpikes;
                }
            }
        }
        return null;
    }

    public static iPartsResponseDataWithSpikes findResponseDataForPemAndIdent(
            List<iPartsResponseDataWithSpikes> responseDataList, String pem, String ident) {
        if ((responseDataList != null) && StrUtils.isValid(pem, ident)) {
            for (iPartsResponseDataWithSpikes responseDataWithSpikes : responseDataList) {
                iPartsDataResponseData responseData = responseDataWithSpikes.getResponseData();
                if (responseData.getAsId().getPem().equals(pem) && responseData.getAsId().getIdent().equals(ident)) {
                    return responseDataWithSpikes;
                }
            }
        }
        return null;
    }

    public static List<iPartsDataResponseSpike> getResponseSpikesForPemAndIdent(List<iPartsResponseDataWithSpikes> responseDataList,
                                                                                String pem, String ident) {
        iPartsResponseDataWithSpikes responseDataForPem = findResponseDataForPemAndIdent(responseDataList, pem, ident);
        if (responseDataForPem != null) {
            return responseDataForPem.getResponseSpikes();
        }
        return null;
    }

    public static boolean isResponseDataAvailableForPEM(String pem, Map<String, List<iPartsResponseDataWithSpikes>> responseDataMap) {
        if (responseDataMap != null) {
            List<iPartsResponseDataWithSpikes> responseDataForPem = responseDataMap.get(pem);
            return (responseDataForPem != null) && !responseDataForPem.isEmpty();
        }
        return false;
    }

    public static boolean filterResponseDataOrSpikesforDIALOGEntry(String responseSeriesNo, String responseAA, String entrySeriesNo,
                                                                   String entryAA) {
        if (!StrUtils.isEmpty(responseSeriesNo) && !responseSeriesNo.equals(entrySeriesNo)) {
            return false;
        }

        // bei AA muss auch Baureihe übereinstimmen
        if (!StrUtils.isEmpty(responseAA) && !(responseSeriesNo.equals(entrySeriesNo) && responseAA.equals(entryAA))) {
            return false;
        }
        return true;
    }
}
