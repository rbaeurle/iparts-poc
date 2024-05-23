/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.hierarchycalcualtion;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.util.Utils;

import java.util.*;

/**
 * Hilfsklasse die den "nackten" Baum der DIALOG Positionen der Konstruktionsstückliste enthält. Zusätzlich werden
 * die DIALOG Positionen der zu übernehmenden Positionen gehalten. Weil die Berechnung der Strukturstufen von der
 * jeweiligen Zielstückliste abhängig ist, werden die DIALOG Positionen der AS Stücklisten nach ihrer Zielstückliste
 * gruppiert.
 */
public class HierarchyStructuresForConstPartsList {

    private final Map<String, List<EtkDataPartListEntry>> posValuesFromSelectedEntries;
    private final Map<AssemblyId, Map<String, List<EtkDataPartListEntry>>> posValuesFromDestAssemblies;
    private final Map<AssemblyId, Map<String, Integer>> calculatedHierarchyValuesForAssembly;
    private final Map<AssemblyId, List<HierarchyNode>> calculatedBranchesForAssembly;
    private final Map<AssemblyId, Map<String, HierarchyNode>> calculatedBranchesWithPosForAssembly;
    private final Map<AssemblyId, Map<String, String>> allHotSpotsAndPreviousHotSpotsInDestAssemblyMap; // HotSpots aus der Ziel-Stücklisten inkl. vorherigen HotSpots
    private final Map<AssemblyId, Map<String, String>> allHotSpotsAndSeqNoForDestAssembliesMap; // HotSpots und die dazugehörigen Sequenznummer aus den Ziel-Stücklisten

    public HierarchyStructuresForConstPartsList() {
        this.posValuesFromSelectedEntries = new TreeMap<>();
        this.posValuesFromDestAssemblies = new HashMap<>();
        this.calculatedHierarchyValuesForAssembly = new HashMap<>();
        this.calculatedBranchesForAssembly = new HashMap<>();
        this.calculatedBranchesWithPosForAssembly = new HashMap<>();
        this.allHotSpotsAndSeqNoForDestAssembliesMap = new HashMap<>();
        this.allHotSpotsAndPreviousHotSpotsInDestAssemblyMap = new HashMap<>();
    }

    /**
     * Berechnet die Map mit allen HotSpots und ihre vorherigen HotSpots für die übergeben Stückliste
     */
    private void calcHotSpotMap(AssemblyId destAssemblyId) {
        Map<String, String> allHotSpotsAndSeqNoInDestAssembly = allHotSpotsAndSeqNoForDestAssembliesMap.get(destAssemblyId);
        if (allHotSpotsAndSeqNoInDestAssembly != null) {
            Map<String, String> result = new TreeMap<>(Comparator.comparing(Utils::toSortString));
            Set<String> hotSpots = allHotSpotsAndSeqNoInDestAssembly.keySet();
            String previousHotSpot = "0";
            for (String hotSpot : hotSpots) {
                result.put(hotSpot, previousHotSpot);
                previousHotSpot = hotSpot;
            }
            this.allHotSpotsAndPreviousHotSpotsInDestAssemblyMap.put(destAssemblyId, result);
        }
    }

    public Map<String, String> getAllHotSpotsAndPreviousHotSpotsInDestAssembly(AssemblyId destAssemblyId) {
        return allHotSpotsAndPreviousHotSpotsInDestAssemblyMap.get(destAssemblyId);
    }

    public Map<String, String> getAllHotSpotsAndSeqNoInDestAssembly(AssemblyId destAssemblyId) {
        return allHotSpotsAndSeqNoForDestAssembliesMap.get(destAssemblyId);
    }

    /**
     * Legt die DIALOG Positionen der zu übernehmenden Konstruktionspositionen als "gültige" DIALOG Position ab.
     * Diese DIALOG Positionen werden für die Berechnung der Strukturstufen benötigt.
     *
     * @param posValue
     * @param selectedEntry
     */
    public void addSelectedDIALOGPos(String posValue, EtkDataPartListEntry selectedEntry) {
        List<EtkDataPartListEntry> partListEntries = posValuesFromSelectedEntries.computeIfAbsent(posValue, k -> new ArrayList<>());
        partListEntries.add(selectedEntry);
    }

    /**
     * Legt die DIALOG Positionen der gefundenen AS Positionen als "gültige" DIALOG Position ab.
     * Diese DIALOG Positionen werden für die Berechnung der Strukturstufen für die Stückliste aus der sie kommen
     * benötigt.
     *
     * @param posValue
     * @param destEntry
     * @param destAssemblyId
     */
    public void addExistingDIALOGPos(String posValue, EtkDataPartListEntry destEntry, AssemblyId destAssemblyId) {
        Map<String, List<EtkDataPartListEntry>> posValuesForAssembly = posValuesFromDestAssemblies.computeIfAbsent(destAssemblyId, k -> new TreeMap<>());
        List<EtkDataPartListEntry> partListEntries = posValuesForAssembly.computeIfAbsent(posValue, k -> new ArrayList<>());
        partListEntries.add(destEntry);
    }

    /**
     * Liefert die berechnete Strukturstufe für die übergebene DIALOG Position aus der übergebenen AS Stückliste zurück
     *
     * @param posValue
     * @param posToHierarchy
     * @param destAssemblyId
     * @param hotSpotsAndSeqNoFromDestAssembly
     * @return
     */
    public String getCalculatedValueForDestAssembly(String posValue, Map<String, Integer> posToHierarchy,
                                                    AssemblyId destAssemblyId,
                                                    Map<String, String> hotSpotsAndSeqNoFromDestAssembly) {
        this.allHotSpotsAndSeqNoForDestAssembliesMap.put(destAssemblyId, hotSpotsAndSeqNoFromDestAssembly);
        calcHotSpotMap(destAssemblyId);
        Map<String, Integer> hierarchyValues = iPartsEditPlugin.isDIALOGHotSpotAndHierarchyCorrectionActive()
                                               ? loadHierarchyStructureChainIfNeeded(destAssemblyId, posToHierarchy)
                                               : loadHierarchyStructureIfNeeded(destAssemblyId, posToHierarchy);
        Integer result = hierarchyValues.get(posValue);
        if (result != null) {
            return String.valueOf(result);
        }
        return null;
    }

    /**
     * Berechnet die AS Strukturstufe auf Basis des "nackten" DIALOG Positionsbaums und den "gültigen" Positionen
     * aus der Konstruktionsstückliste und der Zielstückliste
     *
     * @param destAssemblyId
     * @param posToHierarchy
     * @return
     */
    private Map<String, Integer> loadHierarchyStructureIfNeeded(AssemblyId destAssemblyId, Map<String, Integer> posToHierarchy) {
        Map<String, Integer> hierarchyValues = calculatedHierarchyValuesForAssembly.get(destAssemblyId);
        if (hierarchyValues == null) {
            hierarchyValues = new TreeMap<>();
            Map<String, List<EtkDataPartListEntry>> posValuesForDestAssemblyMap = posValuesFromDestAssemblies.get(destAssemblyId);
            calculatedHierarchyValuesForAssembly.put(destAssemblyId, hierarchyValues);
            int currentNavigationHierarchy = -1;
            int currentCalculatedHierarchy = 0;
            boolean hierarchyIncreased = false;
            // Durchlaufe alle Positionen von "oben nach unten" ab. Beginnend mit der kleinsten bis zur größten.
            for (Map.Entry<String, Integer> posHierarchyEntry : posToHierarchy.entrySet()) {
                String currentPos = posHierarchyEntry.getKey();
                Integer navigationHierarchy = posHierarchyEntry.getValue();
                if ((currentNavigationHierarchy == -1) || (navigationHierarchy > currentNavigationHierarchy)) {
                    currentNavigationHierarchy = navigationHierarchy;
                    hierarchyIncreased = true;
                } else if (navigationHierarchy < currentNavigationHierarchy) {
                    currentCalculatedHierarchy = 0;
                    currentNavigationHierarchy = navigationHierarchy;
                }
                boolean newOrExistingPos = posValuesFromSelectedEntries.containsKey(currentPos)
                                           || ((posValuesForDestAssemblyMap != null) && posValuesForDestAssemblyMap.containsKey(currentPos));
                if (newOrExistingPos) {
                    if (hierarchyIncreased) {
                        currentCalculatedHierarchy++;
                        hierarchyIncreased = false;
                    }
                    if (currentCalculatedHierarchy == 0) {
                        currentCalculatedHierarchy++;
                    }
                    hierarchyValues.put(currentPos, currentCalculatedHierarchy);
                } else if ((currentCalculatedHierarchy == 1) && (navigationHierarchy == 1)) {
                    // Wurde auf der ersten Stufe (Entwicklung) die berechnete Strukturstufe auf 1 gesetzt und dann
                    // auf der gleichen Stufe wieder kein Treffer detektiert, dann müssen wir die Berechnung wieder
                    // zurücksetzen
                    currentCalculatedHierarchy = 0;
                }
            }
        }
        return hierarchyValues;
    }

    /**
     * Berechnet die AS Strukturstufe auf Basis des "nackten" DIALOG Positionsbaums und den "gültigen" Positionen
     * aus der Konstruktionsstückliste und der Zielstückliste
     *
     * @param destAssemblyId
     * @param posToHierarchy
     * @return
     */
    private Map<String, Integer> loadHierarchyStructureChainIfNeeded(AssemblyId destAssemblyId, Map<String, Integer> posToHierarchy) {
        Map<String, Integer> hierarchyValues = calculatedHierarchyValuesForAssembly.get(destAssemblyId);
        if (hierarchyValues == null) {
            hierarchyValues = new TreeMap<>();
            Map<String, List<EtkDataPartListEntry>> posValuesForDestAssembly = posValuesFromDestAssemblies.get(destAssemblyId);
            calculatedHierarchyValuesForAssembly.put(destAssemblyId, hierarchyValues);
            List<HierarchyNode> branches = calculatedBranchesForAssembly.computeIfAbsent(destAssemblyId, k -> new ArrayList<>());
            Map<String, HierarchyNode> branchesWithPos = calculatedBranchesWithPosForAssembly.computeIfAbsent(destAssemblyId, k -> new HashMap<>());

            HierarchyNode currentData = null;
            int lowestNavValue = -1;
            boolean branchIsCalculated = false;
            Map<String, String> currentPreviousHotSpotMap = allHotSpotsAndPreviousHotSpotsInDestAssemblyMap.get(destAssemblyId);
            Map<String, String> hotSpotAndSeqNoMap = allHotSpotsAndSeqNoForDestAssembliesMap.get(destAssemblyId);
            // Durchlaufe alle Positionen von "oben nach unten" ab. Beginnend mit der kleinsten bis zur größten.
            for (Map.Entry<String, Integer> posHierarchyEntry : posToHierarchy.entrySet()) {
                branchIsCalculated = false;
                String currentPos = posHierarchyEntry.getKey();
                Integer navigationHierarchy = posHierarchyEntry.getValue();

                List<EtkDataPartListEntry> entriesFromDestAssembly = (posValuesForDestAssembly == null) ? null : posValuesForDestAssembly.get(currentPos);
                List<EtkDataPartListEntry> entriesFromSourceAssembly = posValuesFromSelectedEntries.get(currentPos);
                boolean newOrExistingPos = (entriesFromSourceAssembly != null) || (entriesFromDestAssembly != null);

                if (currentData == null) {
                    currentData = createHierarchyData(currentPos, navigationHierarchy, newOrExistingPos,
                                                      entriesFromDestAssembly, entriesFromSourceAssembly,
                                                      branchesWithPos, currentPreviousHotSpotMap, hotSpotAndSeqNoMap);
                    lowestNavValue = navigationHierarchy;
                } else if (navigationHierarchy == currentData.getConstHierarchy()) {
                    HierarchyNode newData = createHierarchyData(currentPos, navigationHierarchy, newOrExistingPos,
                                                                entriesFromDestAssembly, entriesFromSourceAssembly,
                                                                branchesWithPos, currentPreviousHotSpotMap, hotSpotAndSeqNoMap);
                    HierarchyNode previousData = currentData.getPreviousData();
                    if (previousData == null) {
                        calcASHierarchyForBranch(branches, currentData);
                        branchIsCalculated = true;
                        currentData = newData;
                    } else {
                        previousData.addNextData(newData);
                        newData.setPreviousData(previousData);
                        currentData = newData;
                    }
                } else if (navigationHierarchy > currentData.getConstHierarchy()) {
                    HierarchyNode newData = createHierarchyData(currentPos, navigationHierarchy, newOrExistingPos,
                                                                entriesFromDestAssembly, entriesFromSourceAssembly,
                                                                branchesWithPos, currentPreviousHotSpotMap, hotSpotAndSeqNoMap);
                    newData.setPreviousData(currentData);
                    currentData.addNextData(newData);
                    currentData = newData;
                } else {
                    HierarchyNode newData = createHierarchyData(currentPos, navigationHierarchy, newOrExistingPos,
                                                                entriesFromDestAssembly, entriesFromSourceAssembly,
                                                                branchesWithPos, currentPreviousHotSpotMap, hotSpotAndSeqNoMap);
                    if (navigationHierarchy <= lowestNavValue) {
                        calcASHierarchyForBranch(branches, currentData);
                        branchIsCalculated = true;
                        currentData = newData;
                        lowestNavValue = navigationHierarchy;
                    } else {
                        HierarchyNode previousData = currentData.getPreviousData();
                        while ((previousData != null) && (previousData.getConstHierarchy() >= newData.getConstHierarchy())) {
                            previousData = previousData.getPreviousData();
                        }
                        if (previousData == null) {
                            calcASHierarchyForBranch(branches, currentData);
                            branchIsCalculated = true;
                            currentData = newData;
                        } else {
                            previousData.addNextData(newData);
                            newData.setPreviousData(previousData);
                            currentData = newData;
                        }
                    }
                }
            }
            if (!branchIsCalculated) {
                calcASHierarchyForBranch(branches, currentData);
            }
            collectPosToHierarchyValues(branches, hierarchyValues);
        }
        return hierarchyValues;
    }

    private HierarchyNode createHierarchyData(String currentPos, Integer navigationHierarchy, boolean newOrExistingPos,
                                              List<EtkDataPartListEntry> entriesFromDestAssembly,
                                              List<EtkDataPartListEntry> entriesFromSourceAssembly,
                                              Map<String, HierarchyNode> branchesWithPos,
                                              Map<String, String> currentPreviousHotSpot,
                                              Map<String, String> hotSpotAndSeqNo) {
        HierarchyNode hierarchyNode = new HierarchyNode(currentPos, navigationHierarchy, newOrExistingPos,
                                                        entriesFromDestAssembly, entriesFromSourceAssembly,
                                                        currentPreviousHotSpot, hotSpotAndSeqNo);
        branchesWithPos.put(currentPos, hierarchyNode);
        return hierarchyNode;
    }

    private void collectPosToHierarchyValues(List<HierarchyNode> branches, Map<String, Integer> hierarchyValues) {
        branches.forEach(startData -> {
            addHierarchyForPos(startData, hierarchyValues);
        });
    }

    private void addHierarchyForPos(HierarchyNode startData, Map<String, Integer> hierarchyValues) {
        if (startData.isNewOrExistingMatch()) {
            hierarchyValues.put(startData.getDialogPos(), startData.getAsHierarchy());
        }
        if (startData.getNextData() != null) {
            startData.getNextData().forEach(child -> {
                addHierarchyForPos(child, hierarchyValues);
            });
        }
    }

    private void calcASHierarchyForBranch(List<HierarchyNode> branches, HierarchyNode currentData) {
        HierarchyNode startData = currentData;
        while (startData.getPreviousData() != null) {
            startData = startData.getPreviousData();
        }
        branches.add(startData);
        calcASHierarchyForSingleData(startData, 1);
    }

    private void calcASHierarchyForSingleData(HierarchyNode currentData, int currentASHierarchy) {
        int tempCurrentASHierarchy = currentASHierarchy;
        if (currentData.isNewOrExistingMatch()) {
            currentData.setAsHierarchy(tempCurrentASHierarchy);
            tempCurrentASHierarchy++;
        }
        if ((currentData.getNextData() != null) && !currentData.getNextData().isEmpty()) {
            for (HierarchyNode child : currentData.getNextData()) {
                calcASHierarchyForSingleData(child, tempCurrentASHierarchy);
            }
        }
    }

    public HierarchyNode getBranchFroDIALOGPos(String dialogPos, AssemblyId ownerAssemblyId) {
        Map<String, HierarchyNode> posToBranch = calculatedBranchesWithPosForAssembly.get(ownerAssemblyId);
        if (posToBranch != null) {
            HierarchyNode branch = posToBranch.get(dialogPos);
            if (branch != null) {
                return branch;
            }
        }
        return null;
    }

}
