/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.hierarchycalcualtion;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.sort.SortBetweenHelper;

import java.util.*;

/**
 * Klasse mit Daten zu einem Knoten im berechneten AS-Strukturen-Baum
 */
public class HierarchyNode {

    private final String dialogPos; // DIALOG Position
    private final int constHierarchy; // Strukturstufe aus der Entwicklung
    private final boolean isNewOrExistingMatch; // Info, ob es für diese DIALOG Position einen Treffer in der AS Stückliste oder in den zu übernehmenden Stücklistenpositionen gibt
    private final List<EtkDataPartListEntry> entriesWithDialogPosFromDestAssembly; // Die Stücklistenpositionen aus der AS Stückliste zu dieser DIALOG Position
    private final List<EtkDataPartListEntry> entriesFromSourceAssembly; // Die Stücklistenpositionen aus den zu übernehmenden Stücklistenpositionen zu dieser DIALOG Position
    private final Map<String, String> currentPreviousHotSpotMap; // HotSpots aus der Ziel-Stückliste inkl. vorherigen HotSpots
    private final Map<String, String> hotSpotAndSeqNoMap; // HotSpots und die dazugehörigen Sequenznummer aus der Ziel-Stückliste

    private int asHierarchy; // AS Strukturstufe, die dieser Knoten bekommt, wenn es auf der DIALOG Position einen Treffer gab
    private int correctedHotSpot; // Der Hotspot der auf Basis der AS Strukturstufenkette berechnet wurde
    private final List<String> calculatedSequenceNumbers = new ArrayList<>(); // Die Sequenznummer die auf Basis der AS Strukturstufenkette berechnet wurde
    private boolean isCorrected; // Wurde dieser Knoten bei der Korrektur der AS Stufen schon berücksichtigt
    private HierarchyNode previousData; // Der vorherige Knoten im Baum
    private final List<HierarchyNode> nextData = new ArrayList<>(); // Die nächsten Knoten im Baum

    public HierarchyNode(String currentPos, Integer navigationHierarchy, boolean isNewOrExistingMatch,
                         List<EtkDataPartListEntry> entriesFromDestAssembly,
                         List<EtkDataPartListEntry> entriesFromSourceAssembly, Map<String, String> currentPreviousHotSpotMap, Map<String, String> hotSpotAndSeqNoMap) {

        this.dialogPos = currentPos;
        this.constHierarchy = navigationHierarchy;
        this.isNewOrExistingMatch = isNewOrExistingMatch;
        this.entriesWithDialogPosFromDestAssembly = entriesFromDestAssembly;
        this.entriesFromSourceAssembly = entriesFromSourceAssembly;
        this.currentPreviousHotSpotMap = currentPreviousHotSpotMap;
        this.hotSpotAndSeqNoMap = hotSpotAndSeqNoMap;
    }

    public Map<String, String> getCurrentPreviousHotSpotMap() {
        return currentPreviousHotSpotMap;
    }

    public Map<String, String> getHotSpotAndSeqNoMap() {
        return hotSpotAndSeqNoMap;
    }

    public String getDialogPos() {
        return dialogPos;
    }

    public int getConstHierarchy() {
        return constHierarchy;
    }

    public void setAsHierarchy(int asHierarchy) {
        this.asHierarchy = asHierarchy;
    }

    public int getAsHierarchy() {
        return asHierarchy;
    }

    public boolean isNewOrExistingMatch() {
        return isNewOrExistingMatch;
    }

    public HierarchyNode getPreviousData() {
        return previousData;
    }

    public void setPreviousData(HierarchyNode previousData) {
        this.previousData = previousData;
    }

    public List<EtkDataPartListEntry> getEntriesWithDialogPosFromDestAssembly() {
        return entriesWithDialogPosFromDestAssembly;
    }

    public List<HierarchyNode> getNextData() {
        return nextData;
    }

    public void addNextData(HierarchyNode newData) {
        nextData.add(newData);
    }

    public int getCorrectedHotSpot() {
        // Wird ein korrigierter HotSpot angefragt, dann muss die Berechnung der HotSpots auf Basis der AS Strukturen gelaufen sein
        correctASHierarchiesInBranch(this);
        return correctedHotSpot;
    }

    public void setCalculatedHotSpot(int correctedHotSpot) {
        this.correctedHotSpot = correctedHotSpot;
    }

    public String getCalculatedSequenceNumbers() {
        // Wird eine korrigierte Sequenznummer angefragt, dann muss die Berechnung der HotSpots auf Basis der AS Strukturen gelaufen sein
        correctASHierarchiesInBranch(this);
        if (calculatedSequenceNumbers.isEmpty()) {
            return SortBetweenHelper.getFirstSortValue();
        }
        return calculatedSequenceNumbers.remove(0);
    }

    public void addCalculatedSequenceNumber(String calculatedSequenceNumber) {
        this.calculatedSequenceNumbers.add(calculatedSequenceNumber);
    }

    public List<EtkDataPartListEntry> getEntriesFromSourceAssembly() {
        return entriesFromSourceAssembly;
    }

    /**
     * Korrigiert die AS Strukturstufen im kompletten Ast zu dem dieser Knoten gehört
     *
     * @param currentData
     */
    private static void correctASHierarchiesInBranch(HierarchyNode currentData) {
        HierarchyNode startData = currentData;
        // Den Start-Knoten suchen
        while (startData.getPreviousData() != null) {
            startData = startData.getPreviousData();
        }
        List<HierarchyNode> dataWithoutASEntries = new ArrayList<>();
        Optional<EtkDataPartListEntry> highestHotSpotEntry = getEntryWithHighestValue(startData.getEntriesWithDialogPosFromDestAssembly(), iPartsConst.FIELD_K_POS);
        // Korrektur-Objekt anlegen und Korrektur starten
        String seqNo = "800";
        int initialHotSpot = 0;
        if (highestHotSpotEntry.isPresent()) {
            seqNo = highestHotSpotEntry.get().getFieldValue(iPartsConst.FIELD_K_SEQNR);
            initialHotSpot = StrUtils.strToIntDef(highestHotSpotEntry.get().getFieldValue(iPartsConst.FIELD_K_POS), 0);
        }
        HierarchyCorrectionData hierarchyCorrectionData = new HierarchyCorrectionData(startData, seqNo,
                                                                                      initialHotSpot, dataWithoutASEntries);
        correctData(hierarchyCorrectionData);
    }

    /**
     * Korrigiert die AS Stufe und setzt einen möglichen, berechneten HotSpot
     *
     * @param hierarchyCorrectionData
     */
    private static void correctData(HierarchyCorrectionData hierarchyCorrectionData) {
        // Der aktuelle Knoten
        HierarchyNode hierarchyNode = hierarchyCorrectionData.getHierarchyData();
        // Wurde der Knoten schon bearbeitet, brauchen wir hier nichts zu tun
        if (hierarchyNode.isCorrected()) {
            return;
        }
        // Alle Positionen zum Knoten bzw. DIALOG Position im aktuellen Knoten
        List<EtkDataPartListEntry> entriesWithDialogPosFromDestAssembly = hierarchyNode.getEntriesWithDialogPosFromDestAssembly();
        // AS Strukturstufen-Korrektur:
        // Wir haben einen Treffer in AS oder in der Übernahme -> AS Stufe direkt in der AS Stücklistenposition
        // setzen. Die AS Stufen wurden ja schon im eigentlichen Aufbau des Baumes berechnet, daher muss man nur den Wert
        // aus "hierarchyNode" auslesen. Die erzeugten Positionen aus der Konstruktion haben schon die richtigen Werte gesetzt,
        // a diese pro Position während der Erzeugung gemacht werden (setASHierarchyValues()). Diese Korrektur läuft auch
        // nur, wenn man keinen HotSpot für die Position berechnen kann, sonst nicht.
        if (hierarchyNode.isNewOrExistingMatch()) {
            if (entriesWithDialogPosFromDestAssembly != null) {
                int asHierarchy = hierarchyNode.getAsHierarchy();
                entriesWithDialogPosFromDestAssembly.forEach(entry -> {
                    entry.setFieldValue(iPartsConst.FIELD_K_HIERARCHY, String.valueOf(asHierarchy), DBActionOrigin.FROM_EDIT);
                });
            }
        }

        // HotSpot- und Sequenznummern-Berechnung:
        // Hat der aktuelle Knoten Stücklistenpositionen aus AS, dann ist der kleinste mögliche HotSpot die obere Grenze
        // für alle Positionen, die vorher aufgesammelt wurden.
        boolean hasASEntries = Utils.isValid(entriesWithDialogPosFromDestAssembly);
        if (hasASEntries) {
            // AS Position mit dem niedrigsten HotSpot bestimmen
            Optional<EtkDataPartListEntry> lowestHotSpotEntry = getEntryWithLowestValue(entriesWithDialogPosFromDestAssembly, iPartsConst.FIELD_K_POS);
            // AS Position mit dem höchsten HotSpot bestimmen
            Optional<EtkDataPartListEntry> highestHotSpotEntry = getEntryWithHighestValue(entriesWithDialogPosFromDestAssembly,
                                                                                          iPartsConst.FIELD_K_POS);
            // Es muss mind. eine AS Position existieren, um die HotSpots zu korrigieren. Falls nicht, wird die Position
            // aufgesammelt und beim nächsten Knoten geprüft, ob es dort AS Positionen gibt
            if (lowestHotSpotEntry.isPresent() && highestHotSpotEntry.isPresent()) {
                // Niedrigster HotSpot und Sequenznummer im AS
                int endHotSpot = Integer.parseInt(lowestHotSpotEntry.get().getFieldValue(iPartsConst.FIELD_K_POS));
                String endSeqNo = lowestHotSpotEntry.get().getFieldValue(iPartsConst.FIELD_K_SEQNR);
                calcHotSpotAndSeqNo(hierarchyCorrectionData, endHotSpot, endSeqNo);
                hierarchyCorrectionData.setAsHotSpotStart(Integer.parseInt(highestHotSpotEntry.get().getFieldValue(iPartsConst.FIELD_K_POS)));
            } else {
                hierarchyCorrectionData.addHierarchyDataForHotSpotCalculation(hierarchyNode);
            }
            // Sind AS Stücklistenposition vorhanden, wird sich hier gemerkt, was die höchste Sequenznummer ist, damit
            // neue AS Stücklisten erst nach diesen Positionen platziert werden
            Optional<EtkDataPartListEntry> highestSeqNoEntry = getEntryWithHighestValue(entriesWithDialogPosFromDestAssembly, iPartsConst.FIELD_K_SEQNR);
            highestSeqNoEntry.ifPresent(entry -> {
                String highestSeqNo = entry.getFieldValue(iPartsConst.FIELD_K_SEQNR);
                if (StrUtils.isValid(highestSeqNo)) {
                    hierarchyCorrectionData.setAsSeqNoStart(highestSeqNo);
                }
            });

        } else {
            hierarchyCorrectionData.addHierarchyDataForHotSpotCalculation(hierarchyNode);
        }
        // Knoten wurde komplett abgearbeitet -> Markieren, damit er nicht noch einmal berechnet wird
        hierarchyNode.setCorrected(true);

        // Verarbeitung der Kinder im Ast
        List<HierarchyNode> children = hierarchyNode.getNextData();
        if (children.isEmpty()) {
            // Wir haben keine Kinder -> Prüfen, ob Positionen ohne HotSpot existieren und versuchen diese noch zu setzen
            Set<String> allHotSpots = hierarchyCorrectionData.getHierarchyData().getCurrentPreviousHotSpotMap().keySet();
            Map<String, String> allSeqNoMap = hierarchyCorrectionData.getHierarchyData().getHotSpotAndSeqNoMap();
            if (!allHotSpots.isEmpty() && !allSeqNoMap.isEmpty()) {
                // Der letzte HotSpot, der in der im Ast benutzt wurde
                String lastUsedHotSpot = String.valueOf(hierarchyCorrectionData.getAsHotSpotStart());
                Iterator<String> iteratorHotSpots = allHotSpots.iterator();
                boolean currentHotSpotFound = false;
                String endHotSpot = null;
                // Alle HotSpots im AS durchlaufen und prüfen, ob nach dem zuletzt genutzten HotSpot noch welche existieren
                while (iteratorHotSpots.hasNext()) {
                    String tempHotSpot = iteratorHotSpots.next();
                    // Wir haben den zuletzt genutzten HotSpot gefunden -> schauen, ob es danach noch einen gibt. Falls
                    // ja, diesen als obere Grenze verwenden
                    if (tempHotSpot.equalsIgnoreCase(lastUsedHotSpot)) {
                        currentHotSpotFound = true;
                        continue;
                    }
                    if (currentHotSpotFound) {
                        // Die neue HotSpot Obergrenze setzen
                        endHotSpot = tempHotSpot;
                        break;
                    }
                }
                // Es gab einen HotSpot nach dem zuletzt genutzten HotSpot. Die noch nicht verarbeiteten Positionen innerhalb
                // dem gefundenen HotSpot Intervall berechnen
                if (endHotSpot != null) {
                    String endSeqNo = allSeqNoMap.get(endHotSpot);
                    if (StrUtils.isValid(endHotSpot, endSeqNo)) {
                        calcHotSpotAndSeqNo(hierarchyCorrectionData, Integer.parseInt(endHotSpot), endSeqNo);
                    }
                }
            }
        } else {
            // Jetzt die Kinder analog zum aktuellen Knoten durchlaufen
            for (HierarchyNode nextData : hierarchyNode.getNextData()) {
                HierarchyCorrectionData hierarchyCorrectionDataChild = hierarchyCorrectionData.createChildInformation(nextData);
                correctData(hierarchyCorrectionDataChild);
                hierarchyCorrectionData.setAsSeqNoStart(hierarchyCorrectionDataChild.getAsSeqNoStart());
                int hotSpotFromChildren = hierarchyCorrectionDataChild.getAsHotSpotStart();
                if (hotSpotFromChildren > hierarchyCorrectionData.getAsHotSpotStart()) {
                    hierarchyCorrectionData.setAsHotSpotStart(hotSpotFromChildren);
                }
            }
        }
    }

    /**
     * Berechnet den HotSpot und die Sequenznummer für alle bisher aufgesammelten Positionen
     *
     * @param hierarchyCorrectionData
     * @param endHotSpot
     * @param endSeqNo
     */
    private static void calcHotSpotAndSeqNo(HierarchyCorrectionData hierarchyCorrectionData, int endHotSpot, String endSeqNo) {
        List<HierarchyNode> dataWithoutASEntries = hierarchyCorrectionData.getDataWithoutASEntries();
        if (!dataWithoutASEntries.isEmpty()) {
            // HotSpot der vorherigen Position
            int startHotSpot = getStartHotSpot(hierarchyCorrectionData, endHotSpot);
            // Intervall in dem der neue HotSpot liegt
            int hotSpotInterval = endHotSpot - startHotSpot;
            // Je nach Anzahl Positionen ohne HotSpot muss ein Abstand zwischen den vergebenen HotSpots bestimmt werden
            int hotSpotThreshold = Math.max((hotSpotInterval / (dataWithoutASEntries.size() + 1)), 1);
            // Analog zum HotSpot: Sequenznummer bestimmen
            String startSeqNo = getStartSeqNo(hierarchyCorrectionData, startHotSpot);
            // Alle Positionen ohne HotSpot durchlaufen und HotSpots und Sequenznummer vergeben
            boolean moreNodesThanHotSpots = dataWithoutASEntries.size() >= hotSpotThreshold;
            int previousHotSpot = startHotSpot;
            Map<String, String> currentAndPreviousHotSpotMap = hierarchyCorrectionData.getHierarchyData().getCurrentPreviousHotSpotMap();
            Map<String, String> hotSpotAndSeqNoMap = hierarchyCorrectionData.getHierarchyData().getHotSpotAndSeqNoMap();
            for (int i = 0; i < dataWithoutASEntries.size(); i++) {
                HierarchyNode dataWithoutHotSpot = dataWithoutASEntries.get(i);
                if (!dataWithoutHotSpot.hasCorrectedHotSpot()) {
                    int calculatedHotSpot;
                    if (moreNodesThanHotSpots) {
                        // Ist das Intervall an möglichen Hotspots kleiner als die Anzahl DIALOG Positionen,
                        // die einen Hotspot benötigen, dann bekommen alle den gleichen HotSpot
                        calculatedHotSpot = startHotSpot + (hotSpotInterval / 2);
                    } else {
                        calculatedHotSpot = startHotSpot + (hotSpotThreshold * (i + 1));
                    }
                    dataWithoutHotSpot.setCalculatedHotSpot(calculatedHotSpot);
                    // Hier den vergebenen Hotspot setzen, damit der nächste Knoten im Baum diesen als Start nimmt
                    hierarchyCorrectionData.setAsHotSpotStart(calculatedHotSpot);
                    // Für die Berechnung der richtigen HotSpots wird die Map mit allen HotSpots in AS und deren
                    // Vorgänger HotSpot mit dem neuen HotSpot aktualisiert
                    currentAndPreviousHotSpotMap.put(String.valueOf(calculatedHotSpot), String.valueOf(previousHotSpot));
                    currentAndPreviousHotSpotMap.put(String.valueOf(endHotSpot), String.valueOf(calculatedHotSpot));
                    previousHotSpot = calculatedHotSpot;
                    int entryCount = (dataWithoutHotSpot.getEntriesFromSourceAssembly() != null)
                                     ? dataWithoutHotSpot.getEntriesFromSourceAssembly().size()
                                     : 0;
                    String currentSortValue = null;
                    while (entryCount > 0) {
                        if (startSeqNo.equals(endSeqNo)) {
                            currentSortValue = SortBetweenHelper.getPreviousSortValue(startSeqNo);
                        } else {
                            currentSortValue = SortBetweenHelper.getSortBetween(startSeqNo, endSeqNo);
                        }
                        dataWithoutHotSpot.addCalculatedSequenceNumber(currentSortValue);
                        startSeqNo = currentSortValue;
                        entryCount--;
                    }
                    // Damit die Sortierreihenfolge für nachfolgende Positionen stimmt, wird hier die Map mit
                    // Hotspots und ihren Sequenznummern aktualisiert
                    if (StrUtils.isValid(currentSortValue)) {
                        hierarchyCorrectionData.setAsSeqNoStart(currentSortValue);
                        hotSpotAndSeqNoMap.put(String.valueOf(calculatedHotSpot), currentSortValue);
                    }
                }
            }
            // Alle Positionen wurden abgearbeitet -> Liste leeren und neue Suchen
            dataWithoutASEntries.clear();
        }
    }

    /**
     * Liefert den Start-Hotspot für die Berechnung auf Basis der AS Hotspots und dem zuletzt genutzten HotSpot im Ast
     *
     * @param hierarchyCorrectionData
     * @param endHotSpot
     * @return
     */
    private static int getStartHotSpot(HierarchyCorrectionData hierarchyCorrectionData, int endHotSpot) {
        int value = hierarchyCorrectionData.getAsHotSpotStart();
        Map<String, String> allHotSpots = hierarchyCorrectionData.getHierarchyData().getCurrentPreviousHotSpotMap();
        if (allHotSpots != null) {
            String previousHotSpot = allHotSpots.get(String.valueOf(endHotSpot));
            if (StrUtils.isValid(previousHotSpot)) {
                int existingHotSpot = StrUtils.strToIntDef(previousHotSpot, -1);
                if ((existingHotSpot != -1) && (existingHotSpot > value)) {
                    value = existingHotSpot;
                }
            }
        }
        return value;
    }

    /**
     * Liefert die Sequenznummer für den übergebenen HotSpot
     *
     * @param hierarchyCorrectionData
     * @param startHotSpot
     * @return
     */
    private static String getStartSeqNo(HierarchyCorrectionData hierarchyCorrectionData, int startHotSpot) {
        String value = hierarchyCorrectionData.getAsSeqNoStart();
        Map<String, String> allSeqNo = hierarchyCorrectionData.getHierarchyData().getHotSpotAndSeqNoMap();
        if (allSeqNo != null) {
            String seqNoForFoundHotSpot = allSeqNo.get(String.valueOf(startHotSpot));
            if (StrUtils.isValid(seqNoForFoundHotSpot)) {
                value = seqNoForFoundHotSpot;
            }
        }
        return value;
    }

    private static Optional<EtkDataPartListEntry> getEntryWithHighestValue(List<EtkDataPartListEntry> entries, String fieldName) {
        if (!Utils.isValid(entries)) {
            return Optional.empty();
        }
        return entries.stream()
                .filter(entry -> !entry.getFieldValue(fieldName).isEmpty())
                .max(Comparator.comparing(o -> Utils.toSortString(o.getFieldValue(fieldName))));
    }

    private static Optional<EtkDataPartListEntry> getEntryWithLowestValue(List<EtkDataPartListEntry> entries, String fieldName) {
        if (!Utils.isValid(entries)) {
            return Optional.empty();
        }
        return entries.stream()
                .filter(entry -> !entry.getFieldValue(fieldName).isEmpty())
                .min(Comparator.comparing(o -> Utils.toSortString(o.getFieldValue(fieldName))));
    }

    public boolean hasCorrectedHotSpot() {
        // Wird ein korrigierter HotSpot angefragt, dann muss die Berechnung der HotSpots auf Basis der AS Strukturen gelaufen sein
        correctASHierarchiesInBranch(this);
        return correctedHotSpot > 0;
    }

    private boolean isCorrected() {
        return isCorrected;
    }

    public void setCorrected(boolean corrected) {
        isCorrected = corrected;
    }

    public boolean hasSourceEntries() {
        return (entriesFromSourceAssembly != null) && !entriesFromSourceAssembly.isEmpty();
    }

}
