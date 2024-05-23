/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.hierarchycalcualtion;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Hilfsklasse zum Halten aller Strukturstufenberechnungen während einer Übernahme von Positionen in AS
 */
public class HierarchyStructuresForProduct {

    private final Map<String, Map<String, Integer>> posToHierarchyForAAs;
    private final Map<HmMSmId, HierarchyStructuresForConstPartsList> hierarchiesInAssembly;
    private final Map<AssemblyId, Map<String, String>> hotSpotsAndSeqNoFromDestAssemblies;

    public HierarchyStructuresForProduct(Map<String, Map<String, Integer>> posToHierarchyForAAs) {
        this.posToHierarchyForAAs = posToHierarchyForAAs;
        this.hierarchiesInAssembly = new HashMap<>();
        this.hotSpotsAndSeqNoFromDestAssemblies = new HashMap<>();
    }

    /**
     * Liefert die berechnete Strukturstufe für den übergebenen BCTE Schlüssel in der übergebenen AS Stückliste
     *
     * @param bctePrimaryKey
     * @param destAssemblyId
     * @return
     */
    public String getHierarchyForDIALOGSourceEntry(iPartsDialogBCTEPrimaryKey bctePrimaryKey, AssemblyId destAssemblyId) {
        // Die berechneten Daten werden pro Konstruktionsstückliste gehalten. HMMSM der Konstruktionsstückliste bestimmen
        HmMSmId hmMSmId = bctePrimaryKey.getHmMSmId();
        if (hmMSmId != null) {
            HierarchyStructuresForConstPartsList hierarchies = hierarchiesInAssembly.get(hmMSmId);
            if (hierarchies != null) {
                Map<String, Integer> posToHierarchy = posToHierarchyForAAs.get(bctePrimaryKey.getAA());
                if (posToHierarchy != null) {
                    return hierarchies.getCalculatedValueForDestAssembly(bctePrimaryKey.getPosE(), posToHierarchy,
                                                                         destAssemblyId,
                                                                         hotSpotsAndSeqNoFromDestAssemblies.get(destAssemblyId));
                }
            }
        }
        return null;
    }

    /**
     * Fügt die DIALOG Position der selektierten Konstruktionsposition als "gültige" DIALOG Position für die Berechnung
     * hinzu
     *
     * @param bctePrimaryKey
     * @param partListEntry
     */
    public void addDIALOGPosOfSelectedEntry(iPartsDialogBCTEPrimaryKey bctePrimaryKey, EtkDataPartListEntry partListEntry) {
        // Die berechneten Daten werden pro Konstruktionsstückliste gehalten. HMMSM der Konstruktionsstückliste bestimmen
        HmMSmId hmMSmId = bctePrimaryKey.getHmMSmId();
        if (hmMSmId != null) {
            HierarchyStructuresForConstPartsList hierarchyStructure = hierarchiesInAssembly.get(hmMSmId);
            if (hierarchyStructure == null) {
                hierarchyStructure = new HierarchyStructuresForConstPartsList();
                hierarchiesInAssembly.put(hmMSmId, hierarchyStructure);
            }
            hierarchyStructure.addSelectedDIALOGPos(bctePrimaryKey.getPosE(), partListEntry);
        }

    }

    /**
     * Fügt die DIALOG Position der AS Stücklistenposition als "gültige" DIALOG Position für die Berechnung der
     * Strukturstufen hinzu
     *
     * @param bctePrimaryKeyExistingEntry
     * @param destEntry
     * @param destAssemblyId
     */
    public void addDIALOGPosOfExistingEntry(iPartsDialogBCTEPrimaryKey bctePrimaryKeyExistingEntry, EtkDataPartListEntry destEntry, AssemblyId destAssemblyId) {
        // Die berechneten Daten werden pro Konstruktionsstückliste gehalten. HMMSM der Konstruktionsstückliste bestimmen
        HmMSmId hmMSmId = bctePrimaryKeyExistingEntry.getHmMSmId();
        if (hmMSmId != null) {
            HierarchyStructuresForConstPartsList hierarchyStructure = hierarchiesInAssembly.get(hmMSmId);
            if (hierarchyStructure != null) {
                hierarchyStructure.addExistingDIALOGPos(bctePrimaryKeyExistingEntry.getPosE(), destEntry, destAssemblyId);
            }
        }
        if (destEntry != null) {
            // HotSpot und Sequenznummer der AS Position abspeichern
            String hotSpot = destEntry.getFieldValue(iPartsConst.FIELD_K_POS);
            String seqNo = destEntry.getFieldValue(iPartsConst.FIELD_K_SEQNR);
            Map<String, String> hotSpotsAndSeqNo = hotSpotsAndSeqNoFromDestAssemblies.computeIfAbsent(destAssemblyId, k -> new TreeMap<>(Comparator.comparing(Utils::toSortString)));
            hotSpotsAndSeqNo.put(hotSpot, seqNo);
        }
    }

    /**
     * Setzt den HotSpot und die Sequenznummer zur übergebenen Position auf Basis des berechneten AS-Stufen-Baums aus der
     * Konstruktion
     *
     * @param entry
     */
    public void setHotSpotAndSeqNo(EtkDataPartListEntry entry) {
        // BCTE Schlüssel zu jeder Position bestimmen
        iPartsDialogBCTEPrimaryKey primaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(entry);
        HmMSmId hmMSmId = primaryKey.getHmMSmId();
        if (hmMSmId != null) {
            HierarchyStructuresForConstPartsList hierarchyStructure = hierarchiesInAssembly.get(hmMSmId);
            if (hierarchyStructure != null) {
                HierarchyNode branch = hierarchyStructure.getBranchFroDIALOGPos(primaryKey.getPosE(), entry.getOwnerAssemblyId());
                // In der Struktur nach einem möglichen Hotspot suchen aber nur, wenn einer berechnet wurd
                if (branch.hasCorrectedHotSpot()) {
                    int correctedHotSpot = branch.getCorrectedHotSpot();
                    if (correctedHotSpot > 0) {
                        // Konnte einer bestimmt werden, wird dieser gesetzt
                        entry.setFieldValue(iPartsConst.FIELD_K_POS, String.valueOf(correctedHotSpot), DBActionOrigin.FROM_EDIT);
                    }

                    String seqNoForHotSpot = branch.getCalculatedSequenceNumbers();
                    if (StrUtils.isValid(seqNoForHotSpot)) {
                        // Konnte einer bestimmt werden, wird dieser gesetzt
                        entry.setFieldValue(iPartsConst.FIELD_K_SEQNR, seqNoForHotSpot, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
        }
    }
}
