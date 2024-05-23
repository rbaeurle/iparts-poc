/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataIncludeConstMat;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataIncludeConstMatList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReplaceConstMat;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReplaceConstPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsReplaceConstMatId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsReplaceConstPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.collections.dwlist.DwList;

import java.util.Collection;
import java.util.Map;

/**
 * Datenklasse für eine Ersetzung in der Konstruktion (Quelle: TS7 oder VTNV)´in iParts mit Vorgänger, Nachfolger und Mitlieferteilen
 */
public class iPartsReplacementConst {

    public final static String LOCK_FLAG_S = "S";

    public enum Source {VTNV, TS7}

    public iPartsDataPartListEntry predecessorEntry;
    public iPartsDataPartListEntry successorEntry;
    public String sDatA;
    public String preRFMEFlags;
    public String RFMEFlags;
    public String lockFlag;
    public Source source;
    private boolean includePartsLoaded;
    private Collection<IncludePartConstMat> includeParts;

    private iPartsReplacementConst() {

    }

    public iPartsReplacementConst(iPartsDataReplaceConstMat replacementVTNV, iPartsDataPartListEntry predecessorEntry,
                                  iPartsDataPartListEntry successorEntry) {
        this.predecessorEntry = predecessorEntry;
        this.successorEntry = successorEntry;
        this.sDatA = replacementVTNV.getFieldValue(iPartsConst.FIELD_DRCM_SDATA);
        this.preRFMEFlags = replacementVTNV.getFieldValue(iPartsConst.FIELD_DRCM_PRE_RFME);
        this.RFMEFlags = replacementVTNV.getFieldValue(iPartsConst.FIELD_DRCM_RFME);
        this.lockFlag = replacementVTNV.getFieldValue(iPartsConst.FIELD_DRCM_LOCK_FLAG);
        this.source = Source.VTNV;
        this.includePartsLoaded = false;
    }

    public iPartsReplacementConst(iPartsDataReplaceConstPart replacementTS7, iPartsDataPartListEntry predecessorEntry,
                                  iPartsDataPartListEntry successorEntry) {
        this.predecessorEntry = predecessorEntry;
        this.successorEntry = successorEntry;
        this.sDatA = replacementTS7.getFieldValue(iPartsConst.FIELD_DRCP_SDATA);
        this.preRFMEFlags = replacementTS7.getFieldValue(iPartsConst.FIELD_DRCP_RFME);
        this.RFMEFlags = replacementTS7.getFieldValue(iPartsConst.FIELD_DRCP_RFME);
        this.lockFlag = "";
        this.source = Source.TS7;
        this.includePartsLoaded = false;
    }

    public iPartsReplaceConstPartId getAsReplaceConstPartId() {
        return new iPartsReplaceConstPartId(getSuccessorPartNo(), sDatA);
    }

    public iPartsReplaceConstMatId getAsReplaceConstMatId() {
        return new iPartsReplaceConstMatId(getSuccessorPartNo(), sDatA);
    }

    public boolean isSourceTS7() {
        return source == Source.TS7;
    }

    public boolean isSourceVTNV() {
        return source == Source.VTNV;
    }

    /**
     * Liefert {@code true}, wenn die Ersetzung für ihre beiden Stücklisteneinträge gültig ist. Ersetzungen sind in DIALOG
     * nur bei gleicher Ausführungsart und innerhalb der gleichen HmMSm Stückliste möglich. Außerdem muss Datum-ab
     * der Ersetzung gleich dem Datum-bis des Vorgängers und gleich dem Datum-ab des Nachfolgers sein.
     */
    public boolean isValid() {
        if ((predecessorEntry == null) || (successorEntry == null)) {
            return false;
        }

        // Ausführungsart von Vorgänger und Nachfolger muss übereinstimmen und das SDATA der Ersetzung mit dem SDATB vom
        // Vorgänger sowie dem SDATA vom Nachfolger
        if (!predecessorEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA).equals(successorEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA))
            || !sDatA.equals(predecessorEntry.getSDATB()) || !sDatA.equals(successorEntry.getSDATA())) {
            return false;
        }

        iPartsDialogBCTEPrimaryKey predecessorGUID = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(predecessorEntry);
        iPartsDialogBCTEPrimaryKey successorGUID = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(successorEntry);
        if ((predecessorGUID == null) || (successorGUID == null)) {
            return false;
        }
        HmMSmId predecessorHmMSmId = predecessorGUID.getHmMSmId();
        HmMSmId successorHmMSmId = successorGUID.getHmMSmId();
        if (!predecessorHmMSmId.equals(successorHmMSmId)) {
            return false;
        }

        return true;
    }

    public String getSuccessorPartNo() {
        if ((successorEntry != null) && (successorEntry.getPart() != null)) {
            return successorEntry.getPart().getAsId().getMatNr();
        }
        return "";
    }

    public String getPredecessorPartNo() {
        if ((predecessorEntry != null) && (predecessorEntry.getPart() != null)) {
            return predecessorEntry.getPart().getAsId().getMatNr();
        }
        return "";
    }

    /**
     * Klont diese Ersetzung, wobei der Vorgänger und Nachfolger durch die korrespondierenden Stücklisteneinträge der übergebenen
     * {@code destPartListEntriesMap} ersetzt werden.
     *
     * @param destPartListEntriesMap
     */
    public iPartsReplacementConst cloneMe(Map<String, iPartsDataPartListEntry> destPartListEntriesMap) {
        iPartsReplacementConst destReplacement = new iPartsReplacementConst();
        if (predecessorEntry != null) {
            destReplacement.predecessorEntry = destPartListEntriesMap.get(predecessorEntry.getAsId().getKLfdnr());
        }

        if (successorEntry != null) {
            destReplacement.successorEntry = destPartListEntriesMap.get(successorEntry.getAsId().getKLfdnr());
        }

        destReplacement.sDatA = sDatA;
        destReplacement.preRFMEFlags = preRFMEFlags;
        destReplacement.RFMEFlags = RFMEFlags;
        destReplacement.lockFlag = lockFlag;
        destReplacement.source = source;

        destReplacement.includePartsLoaded = includePartsLoaded;
        if (includeParts != null) {
            destReplacement.includeParts = new DwList<>(includeParts.size());
            for (IncludePartConstMat includePart : includeParts) {
                destReplacement.includeParts.add(includePart.cloneMe());
            }
        }

        return destReplacement;
    }

    public Collection<IncludePartConstMat> getIncludeParts(EtkProject project) {
        if (!includePartsLoaded && (project != null) && !getSuccessorPartNo().isEmpty()) {
            // Mitlieferteile laden
            iPartsReplaceConstMatId replaceConstMatId = new iPartsReplaceConstMatId(getSuccessorPartNo(), sDatA);
            iPartsDataReplaceConstMat dataReplaceConst = new iPartsDataReplaceConstMat(project, replaceConstMatId);
            dataReplaceConst.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);

            iPartsDataIncludeConstMatList includeConstList = dataReplaceConst.getIncludeConstMats();

            if (!includeConstList.isEmpty()) {
                // Mitlieferteile erzeugen
                includeParts = new DwList<>(includeConstList.size());
                for (iPartsDataIncludeConstMat dataIncludeConst : includeConstList) {
                    IncludePartConstMat includePartConstMat = new IncludePartConstMat(dataIncludeConst.getAttributes());
                    includeParts.add(includePartConstMat);
                }
            }
            includePartsLoaded = true;
        }
        return includeParts;
    }

    /**
     * Fügt das übergebene Mitlieferteil dieser Ersetzung hinzu und ergänzt die laufende Nummer des Mitlieferteils
     *
     * @param includePart
     */
    public void addIncludePart(IncludePartConstMat includePart) {
        if (includePart == null) {
            return;
        }
        if (includeParts == null) {
            includeParts = new DwList<>();
        }
        includeParts.add(includePart);
        includePartsLoaded = true;
    }

    /**
     * Gibt an, ob es für diese Ersetzung geladene Mitlieferteile gibt.
     *
     * @param project
     * @return
     */
    public boolean hasIncludeParts(EtkProject project) {
        Collection<IncludePartConstMat> loadedIncludeParts = getIncludeParts(project);
        return (loadedIncludeParts != null) && !loadedIncludeParts.isEmpty();
    }

    public void setIncludePartsLoaded(boolean includePartsLoaded) {
        this.includePartsLoaded = includePartsLoaded;
    }

    /**
     * Datenklasse für ein Mitlieferteil mit Menge
     */
    public static class IncludePartConstMat {

        public IncludePartConstMat() {
        }

        public IncludePartConstMat(DBDataObjectAttributes attributes) {
            partNumber = attributes.getFieldValue(iPartsConst.FIELD_DICM_INCLUDE_PART_NO);
            quantity = attributes.getFieldValue(iPartsConst.FIELD_DICM_INCLUDE_PART_QUANTITY);
        }

        public IncludePartConstMat cloneMe() {
            IncludePartConstMat destIncludePart = new IncludePartConstMat();
            destIncludePart.partNumber = partNumber;
            destIncludePart.quantity = quantity;
            return destIncludePart;
        }

        public String partNumber;
        public String quantity;
    }

}