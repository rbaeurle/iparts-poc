/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Zentrale Stelle für das Laden und Verarbeiten von PRIMUS-Hinweisen, sodass falls deren Verarbeitung in der Konfiguration
 * deaktiviert ist, diese hier überhaupt nicht geladen oder verarbeitet werden.
 */
public class iPartsPRIMUSReplacementsLoader {

    public static final String PRIMUS_CODE_HAS_INCLUDE_PARTS = "23";

    private final iPartsReplacementHelper replacementHelper;
    private iPartsPRIMUSReplacementsCache primusReplacementsCache;
    private DBDataObjectList<EtkDataPartListEntry> partList;
    private iPartsDataPartListEntry partListEntry;
    private final EtkProject project;

    // Da die Map nur nötig ist, wenn tatsächlich Primus-Ersetzungen für die Stückliste erzeugt werden müssen,
    // wird diese nur per Lazy-Loading geladen.
    private Map<String, List<iPartsDataPartListEntry>> partNoToPartlistEntryMap;

    public iPartsPRIMUSReplacementsLoader(EtkProject project, DBDataObjectList<EtkDataPartListEntry> partList) {
        this.project = project;
        this.replacementHelper = new iPartsReplacementHelper();
        if (Utils.isValid(partList) && replacementHelper.isHandlePrimusHints(project, (iPartsDataAssembly)partList.get(0).getOwnerAssembly())) {
            this.primusReplacementsCache = iPartsPRIMUSReplacementsCache.getInstance(project);
            this.partList = partList;
        }
    }

    public iPartsPRIMUSReplacementsLoader(EtkProject project, iPartsDataPartListEntry partListEntry) {
        this.project = project;
        this.replacementHelper = new iPartsReplacementHelper();
        if ((partListEntry != null) && replacementHelper.isHandlePrimusHints(project, partListEntry.getOwnerAssembly())) {
            this.primusReplacementsCache = iPartsPRIMUSReplacementsCache.getInstance(project);
            this.partListEntry = partListEntry;
        }
    }

    public iPartsReplacement loadReplacementWithoutSuccessor(iPartsDataPartListEntry partListEntry) {
        if (!replacementHelper.isHandlePrimusHints(project, partListEntry.getOwnerAssembly())) {
            return null;
        }
        String matNr = partListEntry.getPart().getFieldValue(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR);
        iPartsPRIMUSReplacementCacheObject replacementCacheObject = primusReplacementsCache.getReplacementCacheObjectForMatNr(matNr);
        if (replacementCacheObject == null) {
            return null;
        }
        if (replacementCacheObject.getSuccessorPartNo().isEmpty()) {
            return new iPartsReplacement(replacementCacheObject, partListEntry, null);
        }
        return null;
    }

    /**
     * Falls in der Konfiguration aktiviert, wird eine evtl. vorhandene PRIMUS-Nachfolger-Ersetzung für alle (Vorgänger-)Stücklisteneinträge
     * zu den Maps aller Ersetzungen hinzugefügt, aber nur falls der jeweilige Stücklisteneintrag noch keine Nachfolger-Ersetzung hat.
     *
     * @param allSuccessorsMap
     * @param editMode
     */
    public void addPrimusReplacementsForPartList(Map<String, List<iPartsReplacement>> allSuccessorsMap, boolean editMode) {
        if (!Utils.isValid(partList)) {
            return;
        }
        for (EtkDataPartListEntry partListEntry : partList) {
            if (partListEntry instanceof iPartsDataPartListEntry) {
                String kLfdnr = partListEntry.getAsId().getKLfdnr();
                List<iPartsReplacement> successorsOfPartListEntry = allSuccessorsMap.get(kLfdnr);
                List<iPartsReplacement> successorsWithPRIMUS = internalAddPrimusReplacement((iPartsDataPartListEntry)partListEntry,
                                                                                            successorsOfPartListEntry, editMode);
                if (successorsWithPRIMUS != null) {
                    allSuccessorsMap.put(kLfdnr, successorsWithPRIMUS);
                }
            }
        }
    }

    /**
     * Falls in der Konfiguration aktiviert, wird eine evtl. vorhandene PRIMUS-Nachfolger-Ersetzung für den (Vorgänger-)Stücklisteneintrag
     * zur Liste aller Ersetzungen hinzugefügt, aber nur falls der Stücklisteneintrag noch keine Nachfolger-Ersetzung hat.
     *
     * @param successors
     * @param editMode
     * @return Ersetzungen inkl. PRIMUS-Nachfolger
     */
    public List<iPartsReplacement> addPrimusReplacementsForPartListEntry(List<iPartsReplacement> successors, boolean editMode) {
        if (partListEntry == null) {
            return successors;
        }
        return internalAddPrimusReplacement(partListEntry, successors, editMode);
    }

    /**
     * Liefert die PRIMUS-Ersetzung für den übergebenen {@link iPartsDataPartListEntry}, welche als Vorgänger
     * den der ersten Ersetzung in der Kette hat und alle restlichen Daten von der letzten Ersetzung in der Kette.
     * Z.B. partlistEntry hat MatNr A1: die Ersetzungen "A1 -> A2" und "A2 -> A3" werden zu EINER Ersetzung "A1 -> A3".
     *
     * @param predecessorPartListEntry
     * @return {@code null}, falls keine Ersetzungen mit der Materialnummer des Stücklisteneintrags gefunden wurden.
     */
    private iPartsReplacement getReplacementChainAsSingleReplacement(iPartsDataPartListEntry predecessorPartListEntry) {
        String initialMatNr = predecessorPartListEntry.getPart().getFieldValue(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR);
        List<iPartsPRIMUSReplacementCacheObject> primusChain = getReplacementChain(initialMatNr);

        if (primusChain == null || primusChain.isEmpty()) {
            return null;
        }

        return iPartsReplacement.createPrimusReplacement(primusChain.get(primusChain.size() - 1), predecessorPartListEntry, getPartNoToPartlistEntryMap());
    }

    public List<iPartsPRIMUSReplacementCacheObject> getReplacementChain(String initialMatNr) {
        iPartsPRIMUSReplacementCacheObject initialReplacementCacheObject = primusReplacementsCache.getReplacementCacheObjectForMatNr(initialMatNr);
        if ((initialReplacementCacheObject == null) || initialReplacementCacheObject.getSuccessorPartNo().isEmpty()) {
            return null;
        }

        List<iPartsPRIMUSReplacementCacheObject> primusChain = new ArrayList<>();
        iPartsPRIMUSReplacementCacheObject replacementCacheObject = initialReplacementCacheObject;
        primusChain.add(replacementCacheObject);
        iPartsPRIMUSReplacementCacheObject nextReplacementCacheObject = primusReplacementsCache.getReplacementCacheObjectForMatNr(replacementCacheObject.getSuccessorPartNo());
        while (nextReplacementCacheObject != null) {
            if (nextReplacementCacheObject.getPredecessorPartNo().equals(initialMatNr)) {
                // Das Nächste ist wieder der Anfang... Diese zirkuläre Ersetzungskette muss unterbrochen werden.
                break;
            }
            if (nextReplacementCacheObject.getSuccessorPartNo().isEmpty() || replacementCacheObject.getCodeForward().equals(PRIMUS_CODE_HAS_INCLUDE_PARTS)) {
                // DAIMLER-10211: Hinweise ohne Nachfolger werden ignoriert und die Kette ist damit auch zu Ende bzw.
                // bei Code C23 wird die Ersetzung mit Mitlieferteilen sofort ausgegeben ohne weitere rekursive Prüfung.
                break;
            }
            replacementCacheObject = nextReplacementCacheObject;
            primusChain.add(replacementCacheObject);
            nextReplacementCacheObject = primusReplacementsCache.getReplacementCacheObjectForMatNr(replacementCacheObject.getSuccessorPartNo());
        }
        // nextReplacementCacheObject ist null bzw. Kette unterbrochen, also ist das Ende der Kette erreicht
        return primusChain;
    }

    private List<iPartsReplacement> internalAddPrimusReplacement(iPartsDataPartListEntry partListEntry, List<iPartsReplacement> successorsOfPartListEntry,
                                                                 boolean editMode) {
        // PRIMUS-Ersetzung nur dann hinzufügen, wenn es keinen echten Nachfolger gibt
        boolean showPrimusReplacement = (successorsOfPartListEntry == null) || successorsOfPartListEntry.isEmpty();
        if (!showPrimusReplacement && editMode) { // Nur im Edit-Modus kann es nicht freigegebene Ersetzungen geben
            boolean validReplacementFound = false;
            for (iPartsReplacement replacement : successorsOfPartListEntry) {
                if (iPartsDataReleaseState.replacementRelevantStates.contains(replacement.releaseState)) {
                    validReplacementFound = true;
                    break;
                }
            }
            showPrimusReplacement = !validReplacementFound;
        }
        if (showPrimusReplacement) {
            iPartsReplacement primusReplacement = getReplacementChainAsSingleReplacement(partListEntry);
            if (primusReplacement != null) {
                if (successorsOfPartListEntry == null) {
                    successorsOfPartListEntry = new DwList<>(1);
                }
                successorsOfPartListEntry.add(primusReplacement);
            }
            // Bei PRIMUS-Ersetzungen keine Vorgänger-Ersetzungen an den Nachfolgern hinzufügen
        }
        return successorsOfPartListEntry;
    }

    public Map<String, List<iPartsDataPartListEntry>> getPartNoToPartlistEntryMap() {
        if (partNoToPartlistEntryMap == null) {
            if (partList != null) {
                partNoToPartlistEntryMap = iPartsReplacementHelper.createPartNoToPartlistEntryMap(partList);
            } else {
                partNoToPartlistEntryMap = new HashMap<>();
                if (partListEntry != null) {
                    iPartsReplacementHelper.addPartNoToPartlistEntryMap(partListEntry, partNoToPartlistEntryMap);
                }
            }
        }
        return partNoToPartlistEntryMap;
    }
}
