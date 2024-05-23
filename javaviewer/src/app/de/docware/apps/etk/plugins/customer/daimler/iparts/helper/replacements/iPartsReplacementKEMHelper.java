/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditConstructionToRetailHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDIALOGPositionsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacementConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

public class iPartsReplacementKEMHelper implements iPartsConst {

    public static final String KEM_CHAIN_MATCH_ALL_HOTSPOTS = "@@*@@";

    private iPartsDIALOGPositionsHelper dialogPositionsHelper;

    public iPartsReplacementKEMHelper(DBDataObjectList<EtkDataPartListEntry> destPartList) {
        this.dialogPositionsHelper = new iPartsDIALOGPositionsHelper(destPartList);
    }

    /**
     * Für alle Vorgängerstände des Vorgängers und alle Nachfolgerstände des Nachfolgers eine virtuelle Ersetzung anlegen und
     * am Stücklisteneintrag hinterlegen. Diese Ersetzungen wurden bisher bei der Übernahme von Konstruktion in AS erzeugt.
     * Sie werden für die Filterung verwendet.
     *
     * @param successorsMap
     * @param predecessorsMap
     */
    public void createAndAddReplacementForAllKEMS(Map<String, List<iPartsReplacement>> successorsMap,
                                                  Map<String, List<iPartsReplacement>> predecessorsMap) {

        List<iPartsReplacement> workList = new DwList<>();
        for (List<iPartsReplacement> replacementsForCurrentEntry : successorsMap.values()) {
            workList.addAll(replacementsForCurrentEntry);
        }

        for (iPartsReplacement realReplacement : workList) {
            if ((realReplacement.successorEntry instanceof iPartsDataPartListEntry) &&
                (realReplacement.predecessorEntry instanceof iPartsDataPartListEntry)) {

                // alle Nachfolgerstände des Nachfolgers und Vorgängerstände des Vorgängers ermitteln inkl. des Stücklisteneintrags selbst
                List<iPartsDataPartListEntry> entriesWithCoherentKEMsSucc =
                        getEntriesForAllKEMs((iPartsDataPartListEntry)realReplacement.successorEntry, false, false);
                List<iPartsDataPartListEntry> entriesWithCoherentKEMsPre =
                        getEntriesForAllKEMs((iPartsDataPartListEntry)realReplacement.predecessorEntry, true, false);

                // für jeden dieser Einträge auch eine Ersetzung anlegen
                String realPredecessorKLfdnr = realReplacement.predecessorEntry.getAsId().getKLfdnr();
                String realSuccessorKLfdnr = realReplacement.successorEntry.getAsId().getKLfdnr();
                for (iPartsDataPartListEntry entryWithCoherentKEMSucc : entriesWithCoherentKEMsSucc) {
                    for (iPartsDataPartListEntry entryWithCoherentKemPre : entriesWithCoherentKEMsPre) {

                        String lfdNrPre = entryWithCoherentKemPre.getAsId().getKLfdnr();
                        String lfdNrSucc = entryWithCoherentKEMSucc.getAsId().getKLfdnr();

                        // die echte Ersetzung nicht zusätzlich als virtuellen Klon hinzufügen
                        if (!lfdNrPre.equals(realPredecessorKLfdnr) || !lfdNrSucc.equals(realSuccessorKLfdnr)) {
                            iPartsReplacement virtualReplacement = iPartsReplacement.createVirtualClone(
                                    realReplacement, entryWithCoherentKemPre, entryWithCoherentKEMSucc);

                            // zu den Nachfolgern hinzufügen
                            List<iPartsReplacement> successors = iPartsReplacementHelper.getReplacementsFromMap(successorsMap, lfdNrPre);
                            successors.add(virtualReplacement);

                            // und zu den Vorgängern
                            List<iPartsReplacement> predecessors = iPartsReplacementHelper.getReplacementsFromMap(predecessorsMap, lfdNrSucc);
                            predecessors.add(virtualReplacement);
                        }
                    }
                }
            }
        }
    }

    /**
     * Liefert die Vorgänger/Nachfolgerstände zum übergebenen After-Sales-{@link iPartsDataPartListEntry} zurück,
     * die dieselbe Materialnummer und denselben Hotspot haben wie dieser.
     * Da diese im After-Sales auch Lücken haben können, falls nicht alle Stände aus der Konstrukion übernommen wurden,
     * wird bei der Übernahme in AS immer in {@link EditConstructionToRetailHelper#calculateMinMaxKEMDatesWithoutCache}
     * das minimale und maximale Datum der zusammenhängenden Stände der Konstruktion am AS-Stücklisteneintrag hinterlegt.
     * Damit weiß man auch im AS, welche Stände Teil der Kette sind.
     *
     * @param partListEntry
     * @param isNewToOld     Wird die KEM-Kette vom übergebenen Stücklisteneintrag aus von neu nach alt (Vorgängerstände)
     *                       durchlaufen oder andersrum (Nachfolgerstände)?
     * @param removeOwnEntry {@code true} wenn der Stücklisteneintrag selbst nicht enthalten sein soll
     * @return
     */
    public List<iPartsDataPartListEntry> getEntriesForAllKEMs(iPartsDataPartListEntry partListEntry,
                                                              boolean isNewToOld, boolean removeOwnEntry) {
        List<iPartsDataPartListEntry> result = new DwList<>();
        if (!removeOwnEntry) {
            result.add(partListEntry);
        }

        if (dialogPositionsHelper == null) {
            return result;
        }

        // After-Sales
        List<EtkDataPartListEntry> partListEntriesForAllKEMs = dialogPositionsHelper.getPartListEntriesForAllKEMs(partListEntry, true);
        if (partListEntriesForAllKEMs == null) {
            return result;
        }

        for (EtkDataPartListEntry positionVariant : partListEntriesForAllKEMs) {
            if (positionVariant instanceof iPartsDataPartListEntry) {
                if (isPositionVariantInKEMChain(partListEntry, positionVariant, isNewToOld)) {
                    result.add((iPartsDataPartListEntry)positionVariant);
                }
            }
        }

        return result;
    }

    /**
     * Da es in der Konstrukion keine Hotspots gibt, kann es für einen Konstruktionsstücklisteneintrag im After-Sales mehrere
     * KEM-Vorgänger/Nachfolgerstände-Ketten geben. Die Funktion fiefert also für den übergebenen Konstruktionsstücklisteneintrag
     * alle Vorgänger/Nachfolgerstände aus dem After-Sales pro Hotspot zurück.
     *
     * @param partListEntryConstruction
     * @param isNewToOld
     * @param removeOwnEntry
     * @return
     */
    private Map<String, List<iPartsDataPartListEntry>> getASEntriesForAllKEMsPerHotspot(iPartsDataPartListEntry partListEntryConstruction,
                                                                                        boolean isNewToOld, boolean removeOwnEntry) {
        // replacementHelper.getEntriesForAllKEMs() kann hier mit einem geklonten Konstruktions-Stücklisteneintrag
        // aufgerufen werden, bei dem nur die für den Vergleich von der KEM-Kette relevanten Daten als KATALOG-Daten gesetzt
        // werden müssen
        iPartsDataPartListEntry dummyRetailPartListEntry = createDummyPartListEntryRetailForKemChain(partListEntryConstruction);
        List<iPartsDataPartListEntry> partListEntriesForAllKEMs = getEntriesForAllKEMs(dummyRetailPartListEntry,
                                                                                       isNewToOld, removeOwnEntry);
        Map<String, List<iPartsDataPartListEntry>> kemChainPerHotspotMap = new HashMap<>();
        for (iPartsDataPartListEntry otherVersionOfPartListEntry : partListEntriesForAllKEMs) {
            String hotspot = otherVersionOfPartListEntry.getFieldValue(FIELD_K_POS);
            List<iPartsDataPartListEntry> kemChain = kemChainPerHotspotMap.get(hotspot);
            if (kemChain == null) {
                kemChain = new DwList<>();
            }
            kemChain.add(otherVersionOfPartListEntry);
            kemChainPerHotspotMap.put(hotspot, kemChain);
        }
        return kemChainPerHotspotMap;
    }

    private boolean isPositionVariantInKEMChain(EtkDataPartListEntry partListEntry, EtkDataPartListEntry positionVariant,
                                                boolean isNewToOld) {
        // Die Materialnummer muss übereinstimmen
        if (!partListEntry.getFieldValue(FIELD_K_MATNR).equals(positionVariant.getFieldValue(FIELD_K_MATNR))) {
            return false;
        }

        // Der Hotspot muss übereinstimmen (außer bei KEM_CHAIN_MATCH_ALL_HOTSPOTS bei der Übernahme in die AS-Stückliste)
        String hotspot = partListEntry.getFieldValue(FIELD_K_POS);
        if (!hotspot.equals(positionVariant.getFieldValue(FIELD_K_POS)) && !hotspot.equals(KEM_CHAIN_MATCH_ALL_HOTSPOTS)) {
            return false;
        }

        String posVariantMinKemDateFrom = positionVariant.getFieldValue(FIELD_K_MIN_KEM_DATE_FROM);
        if (posVariantMinKemDateFrom.isEmpty()) { // KEM-Kette wurde noch nicht berechnet -> Fallback auf false
            return false;
        } else {
            // Minimales KEM-Datum-ab muss immer übereinstimmen (wird durch die KEM-Kette der Konstruktionsstückliste gesetzt)
            if (!partListEntry.getFieldValue(FIELD_K_MIN_KEM_DATE_FROM).equals(posVariantMinKemDateFrom)) {
                return false;
            }

            if (isNewToOld) { // Vorgängerstände
                return partListEntry.getFieldValue(FIELD_K_DATEFROM).compareTo(positionVariant.getFieldValue(FIELD_K_DATEFROM)) > 0;
            } else { // Nachfolgerstände
                return partListEntry.getFieldValue(FIELD_K_DATEFROM).compareTo(positionVariant.getFieldValue(FIELD_K_DATEFROM)) < 0;
            }
        }
    }

    /**
     * Suche den besten Vorgängerstand pro Hotspot in der Retail-Stückliste übergeben in der Map {@code partListSourceGUIDMap}
     * anhand vom maximalen KEM-Datum ab für den nähesten Vorgängerstand bezogen auf das KEM-Datum ab vom echten Vorgänger
     * der Ersetzung.
     *
     * @param replacementConst
     * @param partListSourceGUIDMap
     * @return Map von Hotspot auf besten Vorgängerstand
     */
    public Map<String, EtkDataPartListEntry> getBestPredecessorForHotspotMap(iPartsReplacementConst replacementConst,
                                                                             Map<String, List<EtkDataPartListEntry>> partListSourceGUIDMap) {
        // Zunächst die echten Vorgänger pro Hotspot suchen
        String predecessorGUID = replacementConst.predecessorEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
        List<EtkDataPartListEntry> realPredecessorsRetail = partListSourceGUIDMap.get(predecessorGUID);
        Map<String, EtkDataPartListEntry> hotspotToBestPredecessorRetailMap = new TreeMap<>();
        if (realPredecessorsRetail != null) {
            for (EtkDataPartListEntry realPredecessor : realPredecessorsRetail) {
                String hotspot = realPredecessor.getFieldValue(FIELD_K_POS);
                hotspotToBestPredecessorRetailMap.put(hotspot, realPredecessor);
            }
        }

        Map<String, List<iPartsDataPartListEntry>> kemChainPerHotspotMap = getASEntriesForAllKEMsPerHotspot(replacementConst.predecessorEntry,
                                                                                                            true, true);
        // Jetzt die besten Vorgängerstände pro Hotspot suchen (sofern es keinen echten Vorgänger für diesen Hotspot gibt)
        String realPredecessorKemDateFrom = replacementConst.predecessorEntry.getSDATA();
        for (Map.Entry<String, List<iPartsDataPartListEntry>> retailPredecessorEntryForKemChain : kemChainPerHotspotMap.entrySet()) {
            String hotspot = retailPredecessorEntryForKemChain.getKey();
            if (hotspotToBestPredecessorRetailMap.containsKey(hotspot)) {
                continue; // Es gibt bereits einen echten Vorgänger für diesen Hotspot
            }

            iPartsDataPartListEntry bestPredecessor = getBestPredecessor(realPredecessorKemDateFrom, retailPredecessorEntryForKemChain.getValue(), null);
            hotspotToBestPredecessorRetailMap.put(hotspot, bestPredecessor);
        }
        return hotspotToBestPredecessorRetailMap;
    }

    /**
     * @param ownKemDateFrom
     * @param previousVersionsOfCurrentEntry müssen alle den GLEICHEN Hotspot haben
     * @param otherPleIDsToBeDeleted         Optionales Set anderer {@link PartListEntryId}s, die ebenfalls gelöscht werden,
     *                                       was beim Verschieben von Ersetzungen relevant ist.
     * @return
     */
    public static iPartsDataPartListEntry getBestPredecessor(String ownKemDateFrom, List<iPartsDataPartListEntry> previousVersionsOfCurrentEntry,
                                                             Set<PartListEntryId> otherPleIDsToBeDeleted) {
        iPartsDataPartListEntry bestNewPredecessor = null;
        String bestMaxKemDateFrom = null;
        for (iPartsDataPartListEntry previousVersionOfCurrentEntry : previousVersionsOfCurrentEntry) {
            String dateFrom = previousVersionOfCurrentEntry.getFieldValue(FIELD_K_DATEFROM);
            if ((dateFrom.compareTo(ownKemDateFrom) <= 0) && ((otherPleIDsToBeDeleted == null) || !otherPleIDsToBeDeleted.contains(previousVersionOfCurrentEntry.getAsId()))) {
                int compareToBestMaxKemDateFrom = (bestMaxKemDateFrom == null) ? 1 : dateFrom.compareTo(bestMaxKemDateFrom);
                if (compareToBestMaxKemDateFrom > 0) { // Neuen besten Nachfolgerstand gefunden
                    bestNewPredecessor = previousVersionOfCurrentEntry;
                    bestMaxKemDateFrom = dateFrom;
                } else if (compareToBestMaxKemDateFrom == 0) { // Weiteren besten Nachfolgerstand gefunden, was in einem Hotspot nicht vorkommen darf
                    Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "More than one best predecessor in KEM chain found for hotspot "
                                                                              + bestNewPredecessor.getFieldValue(FIELD_K_POS) + ". Best predecessor is "
                                                                              + bestNewPredecessor.getAsId().toStringForLogMessages());
                }
            }
        }
        return bestNewPredecessor;
    }

    /**
     * Suche den besten Nachfolgerstand pro Hotspot in der Retail-Stückliste übergeben in der Map {@code partListSourceGUIDMap}
     * anhand vom minimalen KEM-Datum ab für den nähesten Nachfolgerstand bezogen auf das KEM-Datum ab vom echten Nachfolger
     * der Ersetzung.
     *
     * @param replacementConst
     * @param partListSourceGUIDMap
     * @return Map von Hotspot auf besten Vorgängerstand
     */
    public Map<String, EtkDataPartListEntry> getBestSuccessorForHotspotMap(iPartsReplacementConst replacementConst,
                                                                           Map<String, List<EtkDataPartListEntry>> partListSourceGUIDMap) {
        // Jetzt zunächst die echten Nachfolger pro Hotspot suchen
        String successorGUID = replacementConst.successorEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
        List<EtkDataPartListEntry> realSuccessorsRetail = partListSourceGUIDMap.get(successorGUID);
        Map<String, EtkDataPartListEntry> hotspotToBestSuccessorRetailMap = new TreeMap<>();
        if (realSuccessorsRetail != null) {
            for (EtkDataPartListEntry realSuccessor : realSuccessorsRetail) {
                String hotspot = realSuccessor.getFieldValue(FIELD_K_POS);
                hotspotToBestSuccessorRetailMap.put(hotspot, realSuccessor);
            }
        }

        Map<String, List<iPartsDataPartListEntry>> kemChainPerHotspotMap = getASEntriesForAllKEMsPerHotspot(replacementConst.successorEntry,
                                                                                                            false, true);
        // Jetzt die besten Nachfolgerstände pro Hotspot suchen (sofern es keinen echten Nachfolger für diesen Hotspot gibt)
        String realSuccessorKemDateFrom = replacementConst.successorEntry.getSDATA();
        for (Map.Entry<String, List<iPartsDataPartListEntry>> retailSuccessorEntryForKemChain : kemChainPerHotspotMap.entrySet()) {
            String hotspot = retailSuccessorEntryForKemChain.getKey();
            if (hotspotToBestSuccessorRetailMap.containsKey(hotspot)) {
                continue; // Es gibt bereits einen echten Nachfolger für diesen Hotspot
            }

            iPartsDataPartListEntry bestSuccessor = getBestSuccessor(realSuccessorKemDateFrom, retailSuccessorEntryForKemChain.getValue(), null);
            hotspotToBestSuccessorRetailMap.put(hotspot, bestSuccessor);
        }
        return hotspotToBestSuccessorRetailMap;
    }

    /**
     * @param ownKemDateFrom
     * @param nextVersionsOfCurrentEntry müssen alle den GLEICHEN Hotspot haben
     * @param otherPleIDsToBeDeleted     Optionales Set anderer {@link PartListEntryId}s, die ebenfalls gelöscht werden,
     *                                   was beim Verschieben von Ersetzungen relevant ist.
     * @return
     */
    public static iPartsDataPartListEntry getBestSuccessor(String ownKemDateFrom, List<iPartsDataPartListEntry> nextVersionsOfCurrentEntry,
                                                           Set<PartListEntryId> otherPleIDsToBeDeleted) {
        iPartsDataPartListEntry bestNewSuccessor = null;
        String bestMinKemDateFrom = null;
        for (iPartsDataPartListEntry nextVersionOfCurrentEntry : nextVersionsOfCurrentEntry) {
            String dateFrom = nextVersionOfCurrentEntry.getFieldValue(FIELD_K_DATEFROM);
            if ((dateFrom.compareTo(ownKemDateFrom) >= 0) && ((otherPleIDsToBeDeleted == null) || !otherPleIDsToBeDeleted.contains(nextVersionOfCurrentEntry.getAsId()))) {
                int compareToBestMinKemDateFrom = (bestMinKemDateFrom == null) ? -1 : dateFrom.compareTo(bestMinKemDateFrom);
                if (compareToBestMinKemDateFrom < 0) { // Neuen besten Nachfolgerstand gefunden
                    bestNewSuccessor = nextVersionOfCurrentEntry;
                    bestMinKemDateFrom = dateFrom;
                } else if ((compareToBestMinKemDateFrom == 0) && (bestNewSuccessor != null)) { // Weiteren besten Nachfolgerstand gefunden, was in einem Hotspot nicht vorkommen darf
                    Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "More than one best successor in KEM chain found for hotspot "
                                                                              + bestNewSuccessor.getFieldValue(FIELD_K_POS) + ". Best successor is "
                                                                              + bestNewSuccessor.getAsId().toStringForLogMessages());
                }
            }
        }
        return bestNewSuccessor;
    }

    private iPartsDataPartListEntry createDummyPartListEntryRetailForKemChain(iPartsDataPartListEntry partListEntryConstruction) {
        iPartsDataPartListEntry dummyPartListEntryRetail = (iPartsDataPartListEntry)partListEntryConstruction.cloneMe(partListEntryConstruction.getEtkProject());
        dummyPartListEntryRetail.setOwnerAssembly(partListEntryConstruction.getOwnerAssembly());

        // Hotspot auf KEM_CHAIN_MATCH_ALL_HOTSPOTS setzen, damit er alle Hotspots matcht (relevant ist hier zunächst nur
        // die KEM-Kette, da es keinen konkreten Hotspot für den Vergleich gibt)
        dummyPartListEntryRetail.setFieldValue(FIELD_K_POS, iPartsReplacementKEMHelper.KEM_CHAIN_MATCH_ALL_HOTSPOTS, DBActionOrigin.FROM_DB);

        dummyPartListEntryRetail.setFieldValue(FIELD_K_DATEFROM, dummyPartListEntryRetail.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA),
                                               DBActionOrigin.FROM_DB);

        // Setzt voraus, dass vorher bereits die KEM-Kette für partListEntryConstruction berechnet wurde, was aber in
        // createAndTransferPartListEntries() bereits gemacht wird
        dummyPartListEntryRetail.setFieldValue(FIELD_K_MIN_KEM_DATE_FROM, dummyPartListEntryRetail.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MIN_KEM_DATE_FROM),
                                               DBActionOrigin.FROM_DB);
        dummyPartListEntryRetail.setFieldValue(FIELD_K_MAX_KEM_DATE_TO, dummyPartListEntryRetail.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MAX_KEM_DATE_TO),
                                               DBActionOrigin.FROM_DB);

        return dummyPartListEntryRetail;
    }
}
