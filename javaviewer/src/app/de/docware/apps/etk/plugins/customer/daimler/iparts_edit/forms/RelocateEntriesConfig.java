/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.utils.EtkDataArray;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper mit den Parameter zum Verlagern von Stücklistenpositionen
 */
public class RelocateEntriesConfig {

    private boolean assemblyIsNew;
    private boolean isPKWAssembly;
    private boolean setSourceReferenceAndTimeStamp;
    private boolean correctSAAValidities;
    private Map<PartListEntryId, EtkDataArray> sourcePLEIdToTargetVariantValidityMap;
    private Map<PartListEntryId, List<PartListEntryId>> sourcePLEIdToTargetPLEIdMap;
    private Set<PartListEntryId> sourcePLEIdToTargetOmitFlagSet;
    private Set<PartListEntryId> sourcePLEIdToTargetEditLockFlagSet;

    public static RelocateEntriesConfig createConfig(boolean assemblyIsNew, boolean isPKWAssembly) {
        return new RelocateEntriesConfig().setAssemblyIsNew(assemblyIsNew).setIsPKWAssembly(isPKWAssembly);
    }

    public boolean isAssemblyIsNew() {
        return assemblyIsNew;
    }

    public RelocateEntriesConfig setAssemblyIsNew(boolean assemblyIsNew) {
        this.assemblyIsNew = assemblyIsNew;
        return this;
    }

    public boolean isPKWAssembly() {
        return isPKWAssembly;
    }

    public RelocateEntriesConfig setIsPKWAssembly(boolean PKWAssembly) {
        isPKWAssembly = PKWAssembly;
        return this;
    }

    public boolean isSetSourceReferenceAndTimeStamp() {
        return setSourceReferenceAndTimeStamp;
    }

    /**
     * Sollen die Referenz zur Quelle und Zeitstempel gesetzt werden?
     *
     * @param setSourceReferenceAndTimeStamp
     * @return
     */
    public RelocateEntriesConfig setSourceReferenceAndTimeStamp(boolean setSourceReferenceAndTimeStamp) {
        this.setSourceReferenceAndTimeStamp = setSourceReferenceAndTimeStamp;
        return this;
    }

    public boolean isCorrectSAAValidities() {
        return correctSAAValidities;
    }

    /**
     * Sollen die SAA-Gültigkeiten bzgl. dem Ziel-Produkt korrigiert werden?
     *
     * @param correctSAAValidities
     * @return
     */
    public RelocateEntriesConfig setCorrectSAAValidities(boolean correctSAAValidities) {
        this.correctSAAValidities = correctSAAValidities;
        return this;
    }

    public Map<PartListEntryId, EtkDataArray> getSourcePLEIdToTargetVariantValidityMap() {
        return sourcePLEIdToTargetVariantValidityMap;
    }

    /**
     * Map zum Merken der PSK-Varianten-Gültigkeiten beim Abgleich von PSK-Modulen
     *
     * @param sourcePLEIdToTargetVariantValidityMap
     * @return
     */
    public RelocateEntriesConfig setSourcePLEIdToTargetVariantValidityMap(Map<PartListEntryId, EtkDataArray> sourcePLEIdToTargetVariantValidityMap) {
        this.sourcePLEIdToTargetVariantValidityMap = sourcePLEIdToTargetVariantValidityMap;
        return this;
    }

    public Set<PartListEntryId> getSourcePLEIdToTargetOmitFlagSet() {
        return sourcePLEIdToTargetOmitFlagSet;
    }

    /**
     * Set zum Merken des Unterdrückt-Flags {@link iPartsConst#FIELD_K_OMIT} beim Abgleich von PSK-Modulen
     *
     * @param sourcePLEIdToTargetOmitFlagSet
     * @return
     */
    public RelocateEntriesConfig setSourcePLEIdToTargetOmitFlagSet(Set<PartListEntryId> sourcePLEIdToTargetOmitFlagSet) {
        this.sourcePLEIdToTargetOmitFlagSet = sourcePLEIdToTargetOmitFlagSet;
        return this;
    }

    public Map<PartListEntryId, List<PartListEntryId>> getSourcePLEIdToTargetPLEIdMap() {
        return sourcePLEIdToTargetPLEIdMap;
    }

    /**
     * Map, um dieselben laufenden Nummern im PSK-Modul wieder zu verwenden beim Abgleich von PSK-Modulen
     *
     * @param sourcePLEIdToTargetPLEIdMap
     * @return
     */
    public RelocateEntriesConfig setSourcePLEIdToTargetPLEIdMap(Map<PartListEntryId, List<PartListEntryId>> sourcePLEIdToTargetPLEIdMap) {
        this.sourcePLEIdToTargetPLEIdMap = sourcePLEIdToTargetPLEIdMap;
        return this;
    }

    public Set<PartListEntryId> getSourcePLEIdToTargetEditLockFlagSet() {
        return sourcePLEIdToTargetEditLockFlagSet;
    }

    /**
     * Set zum Merken des zum Editieren gesperrt-Flags {@link iPartsConst#FIELD_K_ENTRY_LOCKED} beim Abgleich von PSK-Modulen
     *
     * @param sourcePLEIdToTargetEditLockFlagSet
     * @return
     */
    public RelocateEntriesConfig setSourcePLEIdToTargetEditLockFlagSet(Set<PartListEntryId> sourcePLEIdToTargetEditLockFlagSet) {
        this.sourcePLEIdToTargetEditLockFlagSet = sourcePLEIdToTargetEditLockFlagSet;
        return this;
    }

    public boolean hasSourcePLEIdToTargetPLEIdMap() {
        return getSourcePLEIdToTargetPLEIdMap() != null;
    }

    public boolean hasSourcePLEIdToTargetVariantValidityMap() {
        return getSourcePLEIdToTargetVariantValidityMap() != null;
    }

    public boolean hasSourcePLEIdToTargetOmitFlagSet() {
        return getSourcePLEIdToTargetOmitFlagSet() != null;
    }

    public boolean hasSourcePLEIdToTargetEditLockFlagSet() {
        return getSourcePLEIdToTargetEditLockFlagSet() != null;
    }
}
