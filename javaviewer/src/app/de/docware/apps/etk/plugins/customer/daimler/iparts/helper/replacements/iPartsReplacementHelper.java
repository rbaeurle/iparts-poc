/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReplaceConstMat;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReplaceConstPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsRFMEA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsRFMEN;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacementConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

public class iPartsReplacementHelper {

    public static Comparator<iPartsReplacement> seqNoComparator = Comparator.comparing(o -> o.replacementSeqNo);
    public static Comparator<iPartsReplacement> lfdnrOfPredComparator = Comparator.comparing(o -> o.predecessorEntry.getFieldValue(iPartsConst.FIELD_K_LFDNR));

    public static iPartsReplacement addToMapIfNotExists(Map<String, List<iPartsReplacement>> map, String mapKey, iPartsReplacement replacement) {
        List<iPartsReplacement> existingReplacements = getReplacementsFromMap(map, mapKey);

        iPartsReplacement existingReplacement = listContainsReplacementOrVirtualClone(existingReplacements, replacement);
        if (existingReplacement == null) {
            existingReplacements.add(replacement);
            existingReplacement = replacement;
        }
        return existingReplacement;
    }

    static List<iPartsReplacement> getReplacementsFromMap(Map<String, List<iPartsReplacement>> map, String mapKey) {
        List<iPartsReplacement> existingReplacements = map.get(mapKey);
        if (existingReplacements == null) {
            existingReplacements = new DwList<>();
            map.put(mapKey, existingReplacements);
        }
        return existingReplacements;
    }

    /**
     * Prüft, ob es in <code>list</code> bereits einen Eintrag gibt, der <code>replacement</code> entspricht.
     * Dabei werden alle Werte bis auf die Mitlieferteile und {@link iPartsReplacement#isVirtual()} verglichen.
     *
     * @param list
     * @param replacement
     * @return die gefundene identische Ersetzung falls es eine gibt, sonst <code>null</code>
     */
    private static iPartsReplacement listContainsReplacementOrVirtualClone(List<iPartsReplacement> list, iPartsReplacement replacement) {
        for (iPartsReplacement listContent : list) {
            boolean isValuesEqual = replacement.isValuesEqual(listContent) && replacement.replacementSeqNo.equals(listContent.replacementSeqNo);
            if (isValuesEqual && replacement.isSuccessorEqual(listContent)) {
                return listContent;
            }
        }
        return null;
    }

    /**
     * Berechnet die nächste SeqNo für Replacement
     *
     * @param project
     * @param partListEntryId
     * @return
     */
    public static String getNextReplacementSeqNo(EtkProject project, PartListEntryId partListEntryId) {
        String[] pkFields = new String[]{ iPartsConst.FIELD_DRP_VARI, iPartsConst.FIELD_DRP_VER, iPartsConst.FIELD_DRP_LFDNR };
        String[] pkValues = new String[]{ partListEntryId.getKVari(), partListEntryId.getKVer(), partListEntryId.getKLfdnr() };

        // Die Laufende Nummer ist Teil der ID und hängt vom Typ ab
        // ==> man kann diese Schleife leider nicht weiter unten in der Methode zum Reservieren unterbringen.
        int minLfdNr = 1;
        while (minLfdNr > 0) { // Normaler Abbruch durch return
            String lfdNr = project.getEtkDbs().getNextLfdNr(iPartsConst.TABLE_DA_REPLACE_PART, iPartsConst.FIELD_DRP_SEQNO, minLfdNr,
                                                            pkFields, pkValues);
            if (iPartsDataReservedPKList.reservePrimaryKey(project, new iPartsReplacePartId(partListEntryId, lfdNr))) {
                return lfdNr;
            } else {
                minLfdNr = StrUtils.strToIntDef(lfdNr, -1) + 1;
            }
        }

        // Hier dürften wir eigentlich nie hinkommen...
        String msg = TranslationHandler.translate("!!Keine freie laufende Nummer für Feld %s gefunden",
                                                  TableAndFieldName.make(iPartsConst.TABLE_DA_REPLACE_PART, iPartsConst.FIELD_DRP_SEQNO));
        throw new RuntimeException(msg);
    }

    /**
     * Berechnet die nächste höchste SeqNo für die Ersetzung anhand der geladenen ({@link iPartsDataReplacePartList})
     * und reserviert den Primärschlüssel dafür.
     *
     * @param project
     * @param predecessorId
     * @param replacementsRetail
     * @return
     */
    public static String getNextReplacementSeqNo(EtkProject project, PartListEntryId predecessorId, iPartsDataReplacePartList replacementsRetail) {
        int maxSeqNo = 0;
        for (iPartsDataReplacePart replacePart : replacementsRetail) {
            if (replacePart.getAsId().getPredecessorPartListEntryId().equals(predecessorId)) {
                // Gleicher Primärschlüssel bis auf die Sequenznummer, die wir gerade bestimmen
                int seqNo = StrUtils.strToIntDef(replacePart.getAsId().getSeqNo(), -1);
                if (seqNo > maxSeqNo) {
                    maxSeqNo = seqNo;
                }
            }
        }

        maxSeqNo++;
        while (true) { // Abbruch durch return
            if (iPartsDataReservedPKList.reservePrimaryKey(project, new iPartsReplacePartId(predecessorId, maxSeqNo))) {
                return EtkDbsHelper.formatLfdNr(maxSeqNo);
            } else {
                maxSeqNo++;
            }
        }
    }

    /**
     * DAIMLER-5661: Auswerteflags für echte Ersetzungen analog der Migration setzen, anhand der RFME-Flags einer Ersetzung, siehe
     * {@link de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog.MigrationDialogRPosImporter}
     * <p>
     * ACHTUNG! Die Funktion nur verwenden, wenn nur eine zusätzliche Ersetzung betrachtet wird, denn
     * es wird nur aktiv auf true gesetzt, wenn die RFME-Flags entsprechend gesetzt sind, aber nicht aktiv auf false,
     * was aber gemacht werden muss, falls Ersetzungen geändert oder gelöscht werden. Dafür gibt es {@link #calculateEvalPEMToAndPEMFrom}.
     *
     * @param predecessorEntryRetail    Der Vorgänger der Ersetzung, dessen PEM-bis-Auswerten-Flag gegebenenfalls auf true gesetzt wird
     * @param successorEntryRetail      Der Nachfolger der Ersetzung, dessen PEM-ab-Auswerten-Flag gegebenenfalls auf true gesetzt wird
     * @param replacementRetail         Die Ersetzung deren RFMEA/RFMEN-Flags ausgewertet werden
     * @param seriesIsRelevantForImport Handelt es sich um eine versorgungsrelevante Baureihe?
     */
    public static void setEvalPEMToAndPEMFrom(EtkDataPartListEntry predecessorEntryRetail, EtkDataPartListEntry successorEntryRetail,
                                              iPartsDataReplacePart replacementRetail, boolean seriesIsRelevantForImport) {
        if (seriesIsRelevantForImport) {
            return;
        }

        String rfmeaFlags = replacementRetail.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEA);
        String rfmenFlags = replacementRetail.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEN);

        iPartsRFMEA rfmea = new iPartsRFMEA(rfmeaFlags);
        iPartsRFMEN rfmen = new iPartsRFMEN(rfmenFlags);

        // Ist PEM BIS am Vorgänger gültig?
        if (rfmea.isEvalPEMToForRealReplacement()) {
            predecessorEntryRetail.setFieldValueAsBoolean(iPartsConst.FIELD_K_EVAL_PEM_TO, true, DBActionOrigin.FROM_EDIT);
        }
        // Ist PEM AB am Nachfolger gültig?
        if (isEvalPEMFrom(rfmea, rfmen)) {
            successorEntryRetail.setFieldValueAsBoolean(iPartsConst.FIELD_K_EVAL_PEM_FROM, true, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * PEM-ab-Auswerten-Flag neu durchrechnen. Logik Analog zu {@link #calculateEvalPEMToAndPEMFrom}, aber hier anhand der
     * Vorgänger-Ersetzungs-Datenobjekte des Nachfolgers für den das PEM-ab-Auswerten-Flag bestimmt werden soll.
     *
     * @param predecessorReplacementsOfSuccessor
     * @return
     */
    public static boolean calculateEvalPEMFrom(List<iPartsDataReplacePart> predecessorReplacementsOfSuccessor) {
        // PEM-ab-Auswerten am Nachfolger setzen, wenn es mindestens eine Vorgänger-Ersetzung gibt, bei dem das RFMEN-Flag das aussagt
        for (iPartsDataReplacePart predecessorReplacement : predecessorReplacementsOfSuccessor) {
            iPartsRFMEA rfmea = new iPartsRFMEA(predecessorReplacement.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEA));
            iPartsRFMEN rfmen = new iPartsRFMEN(predecessorReplacement.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEN));
            // Ist PEM AB am Nachfolger gültig?
            if (isEvalPEMFrom(rfmea, rfmen)) {
                return true;
            }
        }
        return false;
    }

    /**
     * PEM-bis-Auswerten-Flag neu durchrechnen. Logik Analog zu {@link #calculateEvalPEMToAndPEMFrom}, aber hier anhand der
     * Nachfolger-Ersetzungs-Datenobjekte des Vorgängers für den das PEM-bis-Auswerten-Flag bestimmt werden soll.
     *
     * @param successorReplacementsOfPredecessor
     * @return
     */
    public static boolean calculateEvalPEMTo(List<iPartsDataReplacePart> successorReplacementsOfPredecessor) {
        // PEM-bis-Auswerten am Voränger setzen, wenn es mindestens eine Nachfolger-Ersetzung gibt, bei dem das RFMEA-Flag das aussagt
        for (iPartsDataReplacePart successorReplacement : successorReplacementsOfPredecessor) {
            iPartsRFMEA rfmea = new iPartsRFMEA(successorReplacement.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEA));
            if (rfmea.isEvalPEMToForRealReplacement()) {
                return true;
            }
        }
        return false;
    }

    /**
     * DAIMLER-5661: Auswerteflags für echte Ersetzungen neu durchrechnen, wenn die Ersetzung, die diesen Vorgänger und
     * Nachfolger enthält, verändert oder gelöscht wurde, da neu geschaut werden muss, ob es nach dem Löschen immer noch
     * mindestens ein RFMEA/RFMEM-Flag gibt, durch welches die PEM-Auswerten Flags gesetzt werden
     *
     * @param predecessorEntryRetail    Der Vorgänger der Ersetzung, dessen PEM-bis-Auswerten-Flag gegebenenfalls auf true gesetzt wird
     * @param successorEntryRetail      Der Nachfolger der Ersetzung, dessen PEM-ab-Auswerten-Flag gegebenenfalls auf true gesetzt wird
     * @param replacementRetail         Die Ersetzung, die verändert oder gelöscht wurde.
     * @param replacementIsDeleted      Flag, ob die Ersetzung gelöscht wurde (bei {@code false} wurde sie verändert und muss
     *                                  bei der Berechnung berücksichtigt werden)
     * @param seriesIsRelevantForImport Handelt es sich um eine versorgungsrelevante Baureihe?
     */
    public static void calculateEvalPEMToAndPEMFrom(iPartsDataPartListEntry predecessorEntryRetail, iPartsDataPartListEntry successorEntryRetail,
                                                    iPartsDataReplacePart replacementRetail, boolean replacementIsDeleted,
                                                    boolean seriesIsRelevantForImport) {
        // PEM-bis-Auswerten am Voränger setzen, wenn es mindestens eine Nachfolger-Ersetzung gibt, bei dem das RFMEA-Flag das aussagt
        boolean evalPemTo = false;
        if (predecessorEntryRetail.hasSuccessors()) {
            for (iPartsReplacement successorReplacement : predecessorEntryRetail.getSuccessors()) {
                if (successorReplacement.getAsReplacePartId().equals(replacementRetail.getAsId()) || (successorReplacement.source == iPartsReplacement.Source.PRIMUS)) {
                    continue;
                }
                iPartsRFMEA rfmea = new iPartsRFMEA(successorReplacement.rfmeaFlags);
                if (rfmea.isEvalPEMToForRealReplacement()) {
                    evalPemTo = true;
                    break;
                }
            }
        }

        // PEM-ab-Auswerten am Nachfolger setzen, wenn es mindestens eine Vorgänger-Ersetzung gibt, bei dem das RFMEN-Flag das aussagt
        boolean evalPemFrom = false;
        if (successorEntryRetail.hasPredecessors()) {
            for (iPartsReplacement predecessorReplacement : successorEntryRetail.getPredecessors()) {
                if (predecessorReplacement.getAsReplacePartId().equals(replacementRetail.getAsId()) || (predecessorReplacement.source == iPartsReplacement.Source.PRIMUS)) {
                    continue;
                }
                iPartsRFMEA rfmea = new iPartsRFMEA(predecessorReplacement.rfmeaFlags);
                iPartsRFMEN rfmen = new iPartsRFMEN(predecessorReplacement.rfmenFlags);
                // Ist PEM AB am Nachfolger gültig?
                if (isEvalPEMFrom(rfmea, rfmen)) {
                    evalPemFrom = true;
                    break;
                }
            }
        }

        // Berücksichtigung von der veränderten Ersetzung
        // Nur Ersetzungen mit für den Retail relevanten Statuswerten berücksichtigen
        if (!replacementIsDeleted && isStatusValid(replacementRetail, iPartsDataReleaseState.getReplacementRelevantStatesDBValues())) {
            // Logik analog zu setEvalPEMToAndPEMFrom() -> darf aber nicht aufgerufen werden, weil ansonsten die Felder
            // K_EVAL_PEM_FROM bzw. K_EVAL_PEM_TO zunächst von true auf false und danach auf true gesetzt werden könnten,
            // was eine unnötige Änderung im ChangeSet ergeben würde
            iPartsRFMEA rfmea = new iPartsRFMEA(replacementRetail.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEA));
            iPartsRFMEN rfmen = new iPartsRFMEN(replacementRetail.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEN));
            // Ist PEM BIS am Vorgänger gültig?
            if (rfmea.isEvalPEMToForRealReplacement()) {
                evalPemTo = true;
            }
            // Ist PEM AB am Nachfolger gültig?
            if (isEvalPEMFrom(rfmea, rfmen)) {
                evalPemFrom = true;
            }
        }

        // Bei versorgungsrelevanten Baureihen werden die Flags durch die Vererbung der Ersetzungen virtuell beim Laden
        // der Stückliste gesetzt und nicht in die DB gespeichert (hier überhaupt nur notwendig für die Konvertierung
        // der Ersetzungen bei Umstellung in eine versorgungsrelevante Baureihe)
        DBActionOrigin actionOrigin = seriesIsRelevantForImport ? DBActionOrigin.FROM_DB : DBActionOrigin.FROM_EDIT;

        predecessorEntryRetail.setFieldValueAsBoolean(iPartsConst.FIELD_K_EVAL_PEM_TO, evalPemTo, actionOrigin);
        successorEntryRetail.setFieldValueAsBoolean(iPartsConst.FIELD_K_EVAL_PEM_FROM, evalPemFrom, actionOrigin);
    }

    /**
     * Liefert alle Ersetzungen, inklusive geladenen Mitlieferteilen für das übergebene {@link iPartsDataPartListEntry}.
     * Der Stücklisteneintrag wird dabei NICHT verändert, d.h Die Ersetzungen werden NICHT an diesen gehängt.
     * Wichtig für die Anzeige, die die Ersetzungen nur laden, aber den Stücklisteneintrag nicht verändern soll.
     *
     * @param partListEntry
     * @param predecessors          Liste, die mit den Vorgänger-Ersetzungen zum übergebenen Stücklisteeintrag befüllt wird
     * @param successors            Liste, die mit den Nachfolger-Ersetzungen zum übergebenen Stücklisteeintrag befüllt wird
     * @param addPrimusReplacements Flag, ob auch virtuelle PRIMUS-Ersetzungen hinzugefügt werden sollen
     */
    public static void loadReplacementsForPartListEntry(Set<String> validStates, List<iPartsReplacement> predecessors,
                                                        List<iPartsReplacement> successors, iPartsDataPartListEntry partListEntry,
                                                        boolean addPrimusReplacements) {
        EtkProject project = partListEntry.getEtkProject();
        DBDataObjectList<EtkDataPartListEntry> partListEntries = partListEntry.getOwnerAssembly().getPartListUnfiltered(null);
        boolean seriesRelevantForImport = partListEntry.isSeriesRelevantForImport();
        iPartsReplacementKEMHelper replacementHelper = null;
        if (seriesRelevantForImport) {
            replacementHelper = new iPartsReplacementKEMHelper(partListEntries);
        }
        Map<String, EtkDataPartListEntry> lfdNrToPartlistEntryMap = createLfdNrToPartlistEntryMap(partListEntries);

        List<iPartsDataReplacePart> allReplacementsInDB = new DwList<>();
        if (seriesRelevantForImport) {
            // vererbte Ersetzungen für Vorgänger- und Nachfolgerstände nur bei versorgungsrelevanten Baureihe generieren
            // Alle Vorgängerstände zum aktuellen Stücklisteneintrag ermitteln inkl. Stücklisteneintrag selbst
            List<iPartsDataPartListEntry> previousVersionsOfCurrentEntry =
                    replacementHelper.getEntriesForAllKEMs(partListEntry, true, false);
            // Alle Nachfolgerstände zum aktuellen Stücklisteneintrag ermitteln inkl. Stücklisteneintrag selbst
            List<iPartsDataPartListEntry> nextVersionsOfCurrentEntry =
                    replacementHelper.getEntriesForAllKEMs(partListEntry, false, false);
            for (iPartsDataPartListEntry previousVersionOfCurrentEntry : previousVersionsOfCurrentEntry) {
                // zu jedem Vorgängerstand schauen ob es eine echte Vorgänger-Ersetzung in der Datenbank gibt
                iPartsDataReplacePartList predecessorsCandidates = iPartsDataReplacePartList.
                        loadPredecessorsForPartListEntry(project, previousVersionOfCurrentEntry.getAsId());
                allReplacementsInDB.addAll(predecessorsCandidates.getAsList());
            }
            for (iPartsDataPartListEntry nextVersionOfCurrentEntry : nextVersionsOfCurrentEntry) {
                // zu jedem Nachfolgerstand schauen ob es eine echte Nachfolger-Ersetzung in der Datenbank gibt
                iPartsDataReplacePartList successorCandidates = iPartsDataReplacePartList.
                        loadSuccessorsForPartListEntry(project, nextVersionOfCurrentEntry.getAsId());
                allReplacementsInDB.addAll(successorCandidates.getAsList());
            }
        } else {
            iPartsDataReplacePartList predecessorsReplacements = iPartsDataReplacePartList.
                    loadPredecessorsForPartListEntry(project, partListEntry.getAsId());
            iPartsDataReplacePartList successorReplacements = iPartsDataReplacePartList.
                    loadSuccessorsForPartListEntry(project, partListEntry.getAsId());
            allReplacementsInDB.addAll(predecessorsReplacements.getAsList());
            allReplacementsInDB.addAll(successorReplacements.getAsList());
        }

        final Map<String, List<iPartsReplacement>> allPredecessors = new HashMap<>();
        final Map<String, List<iPartsReplacement>> allSuccessors = new HashMap<>();
        for (iPartsDataReplacePart replacementDataObjectInDB : allReplacementsInDB) {
            if (isStatusValid(replacementDataObjectInDB, validStates)) {
                iPartsDataIncludePartList includePartsForReplacement;
                includePartsForReplacement = iPartsDataIncludePartList.loadIncludePartsForReplacement(project, replacementDataObjectInDB);

                // Falls es keine Mitlieferteile gibt, die leeren Attribute eines iPartsDataIncludePart erzeugen
                // und mit den Attributen der Ersetzung kombinieren ...
                List<DBDataObjectAttributes> replacementWithIncludeParts = new DwList<>();
                if (includePartsForReplacement.isEmpty()) {
                    iPartsDataIncludePart emptyIncludePart = new iPartsDataIncludePart(project, new iPartsIncludePartId());
                    emptyIncludePart.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    includePartsForReplacement.add(emptyIncludePart, DBActionOrigin.FROM_DB);
                }
                // ... Ansonsten für jedes Mitlieferteil dessen Attribute mit den Attributen der Ersetzung kombinieren ...
                for (iPartsDataIncludePart includePart : includePartsForReplacement) {
                    DBDataObjectAttributes replacementWithIncludePart = new DBDataObjectAttributes();
                    replacementWithIncludePart.addFields(replacementDataObjectInDB.getAttributes(), DBActionOrigin.FROM_DB);
                    replacementWithIncludePart.addFields(includePart.getAttributes(), DBActionOrigin.FROM_DB);
                    replacementWithIncludeParts.add(replacementWithIncludePart);
                }

                // ... Damit diese dann dem Callback zum Laden der Ersetzungen übergeben werden können, der daraus die
                // Ersetzungen für die Stückliste erzeugt. Hier werden nur die Ersetzungen des aktuellen Stücklisteneintrags
                // erzeugt und in die Maps gelegt, da vorher nur diese aus der DB geladen wurden.
                for (DBDataObjectAttributes replacementWithIncludePart : replacementWithIncludeParts) {
                    partListEntry.getOwnerAssembly().foundReplacementsCallback(replacementWithIncludePart, null,
                                                                               lfdNrToPartlistEntryMap, allPredecessors, allSuccessors);
                }
            }
        }

        if (seriesRelevantForImport) {
            // vererbte Ersetzungen für Vorgänger- und Nachfolgerstände nur bei versorgungsrelevanten Baureihe generieren
            replacementHelper.createAndAddReplacementForAllKEMS(allSuccessors, allPredecessors);
        }

        if (addPrimusReplacements) {
            iPartsPRIMUSReplacementsLoader primusReplacementsLoader = new iPartsPRIMUSReplacementsLoader(project, partListEntries);
            primusReplacementsLoader.addPrimusReplacementsForPartList(allSuccessors, validStates == null);
        }

        if (predecessors != null) {
            sortAndAddToMap(partListEntry, allPredecessors, predecessors);
        }
        if (successors != null) {
            sortAndAddToMap(partListEntry, allSuccessors, successors);
        }
    }


    /**
     * Eine Ersetzungskette aufbauen, getrennt nach Vorgängerkette und Nachfolgerkette
     * Die Ersetzungen werden nach der laufender Nummer sortiert
     *
     * @param validStates
     * @param predecessorChain
     * @param successorChain
     * @param partListEntry
     * @param addPrimusReplacements
     */
    public static boolean findAllReplacementsInBothDirections(Set<String> validStates, List<iPartsReplacement> predecessorChain,
                                                              List<iPartsReplacement> successorChain, iPartsDataPartListEntry partListEntry,
                                                              boolean addPrimusReplacements) {
        List<iPartsReplacement> predecessors = new ArrayList<>();
        List<iPartsReplacement> successors = new ArrayList<>();
        // Erste Nachfolger/Vorgänger ermitteln und von da aus in beide Richtungen die Kette entlang gehen
        loadReplacementsForPartListEntry(validStates, predecessors, successors, partListEntry, addPrimusReplacements);
        if (!predecessors.isEmpty()) {
            findAllPredecessorReplacements(validStates, predecessors, addPrimusReplacements, predecessorChain);
        }
        // Die Vorgänger sind relevant zum Testen der Kette auf eine zyklische Ersetzung
        if (!successors.isEmpty()) {
            Set<iPartsReplacement> alreadyVisitedReplacement = new HashSet<>(predecessorChain);
            findAllSuccessorReplacements(validStates, successors, addPrimusReplacements, successorChain, alreadyVisitedReplacement);
        }

        predecessorChain.sort(lfdnrOfPredComparator);
        successorChain.sort(lfdnrOfPredComparator);

        return !predecessorChain.isEmpty() || !successorChain.isEmpty();
    }

    /**
     * Rekursives Zusammenstellen der Ersetzungskette in Vorgängerrichtung
     *
     * @param validStates
     * @param predecessors
     * @param addPrimusReplacements
     * @param result
     */
    private static void findAllPredecessorReplacements(Set<String> validStates, List<iPartsReplacement> predecessors,
                                                       boolean addPrimusReplacements, List<iPartsReplacement> result) {

        for (iPartsReplacement predecessor : predecessors) {
            // Ersetzung auf sich selber und zyklische Ersetzungen dürfen nicht weiter behandelt werden
            if (hasCycle(predecessor, result)) {
                continue;
            }

            List<iPartsReplacement> tempPredecessors = new ArrayList<>();
            iPartsDataPartListEntry predecessorIpartsDataPartlistEntry = null;
            if (predecessor.predecessorEntry instanceof iPartsDataPartListEntry) {
                predecessorIpartsDataPartlistEntry = (iPartsDataPartListEntry)predecessor.predecessorEntry;
            }
            if (predecessorIpartsDataPartlistEntry != null) {
                result.add(predecessor);
                loadReplacementsForPartListEntry(validStates, tempPredecessors, null, predecessorIpartsDataPartlistEntry, addPrimusReplacements);
                if (!tempPredecessors.isEmpty()) {
                    findAllPredecessorReplacements(validStates, tempPredecessors, addPrimusReplacements, result);
                }
            }
        }
    }


    /**
     * Rekursives Zusammenstellen der Ersetzungskette in Nachfolgerrichtung
     *
     * @param validStates
     * @param successors
     * @param addPrimusReplacements
     * @param result
     */
    private static void findAllSuccessorReplacements(Set<String> validStates, List<iPartsReplacement> successors,
                                                     boolean addPrimusReplacements, List<iPartsReplacement> result,
                                                     Set<iPartsReplacement> alreadyVisitedReplacements) {
        for (iPartsReplacement successor : successors) {
            // Ersetzung auf sich selber und zyklische Ersetzungen dürfen nicht weiter behandelt werden
            if (hasCycle(successor, alreadyVisitedReplacements)) {
                continue;
            }

            List<iPartsReplacement> tempSuccessors = new ArrayList<>();
            iPartsDataPartListEntry successorIpartsDataPartlistEntry = null;
            if (successor.successorEntry instanceof iPartsDataPartListEntry) {
                successorIpartsDataPartlistEntry = (iPartsDataPartListEntry)successor.successorEntry;
            }
            if (successorIpartsDataPartlistEntry != null) {
                result.add(successor);
                alreadyVisitedReplacements.add(successor);
                loadReplacementsForPartListEntry(validStates, null, tempSuccessors, successorIpartsDataPartlistEntry, addPrimusReplacements);
                if (!tempSuccessors.isEmpty()) {
                    findAllSuccessorReplacements(validStates, tempSuccessors, addPrimusReplacements, result, alreadyVisitedReplacements);
                }
            }
        }
    }

    /**
     * Findet zyklische Ersetzungen und Ersetzungen auf sich selber
     *
     * @param replacement
     * @param alreadyVisitedReplacements
     * @return
     */
    private static boolean hasCycle(iPartsReplacement replacement, Collection<iPartsReplacement> alreadyVisitedReplacements) {
        // Ersetzung auf sich selber
        if (replacement.successorEntry != null) {
            if (replacement.predecessorEntry.getAsId().equals(replacement.successorEntry.getAsId())) {
                return true;
            }
        }

        // zyklische Ersetzung
        for (iPartsReplacement replacementForCycleTest : alreadyVisitedReplacements) {
            if (replacementForCycleTest.predecessorEntry.getAsId().equals(replacement.predecessorEntry.getAsId())
                && replacementForCycleTest.successorEntry.getAsId().equals(replacement.successorEntry.getAsId())) {
                return true;
            }
        }
        return false;
    }

    private static void sortAndAddToMap(iPartsDataPartListEntry partListEntry, Map<String, List<iPartsReplacement>> allReplacementsMap,
                                        List<iPartsReplacement> replacementsList) {
        if (!allReplacementsMap.isEmpty() && (replacementsList != null)) {
            List<iPartsReplacement> replacements = allReplacementsMap.get(partListEntry.getAsId().getKLfdnr());
            if (replacements != null) {
                replacementsList.addAll(replacements);
                Collections.sort(replacementsList, seqNoComparator);
            }
        }
    }

    /**
     * Überprüft den Status des <code>replacePart</code> auf Gültigkeit
     * Falls <code>validStates == null</code> wird keine Statusprüfung durchgeführt
     *
     * @param replacePart Das zu Prüfende DataObject
     * @param validStates Alle gültigen Statuswerte; Bei <code>null</code> gilt alles als gültig
     * @return
     */
    public static boolean isStatusValid(iPartsDataReplacePart replacePart, Set<String> validStates) {
        String status = replacePart.getFieldValue(iPartsConst.FIELD_DRP_STATUS);
        return !StrUtils.isValid(status) || (validStates == null) || validStates.contains(status);
    }

    /**
     * Muss die "PEM-ab" ausgewertet werden?
     *
     * @param rfmea
     * @param rfmen
     * @return
     */
    public static boolean isEvalPEMFrom(iPartsRFMEA rfmea, iPartsRFMEN rfmen) {
        return rfmen.isNotReplaceable() || (rfmen.isIncludePart() && rfmea.isNotReplaceable());
    }

    /**
     * Erzeugt eine Map um schneller über die laufende Nummer den Stücklisteneintrag zu finden
     *
     * @param partListEntries
     * @return
     */
    public static Map<String, EtkDataPartListEntry> createLfdNrToPartlistEntryMap(DBDataObjectList<EtkDataPartListEntry> partListEntries) {
        Map<String, EtkDataPartListEntry> partListEntriesMap = new HashMap<>(partListEntries.size());
        for (EtkDataPartListEntry partListEntryOfAssembly : partListEntries) {
            partListEntriesMap.put(partListEntryOfAssembly.getAsId().getKLfdnr(), partListEntryOfAssembly);
        }
        return partListEntriesMap;
    }

    /**
     * Erzeugt eine Map, in der jeder Stücklisteneintrag mit seiner Materialnummer in eine Liste mit Stücklisteneinträgen
     * mit identischer Materialnummer abgelegt wird.
     *
     * @param partListEntries
     * @return
     */
    public static Map<String, List<iPartsDataPartListEntry>> createPartNoToPartlistEntryMap(DBDataObjectList<EtkDataPartListEntry> partListEntries) {
        Map<String, List<iPartsDataPartListEntry>> partNrToPartListEntryMap = new HashMap<>();
        for (EtkDataPartListEntry entry : partListEntries) {
            addPartNoToPartlistEntryMap(entry, partNrToPartListEntryMap);
        }
        return partNrToPartListEntryMap;
    }

    public static void addPartNoToPartlistEntryMap(EtkDataPartListEntry entry, Map<String, List<iPartsDataPartListEntry>> partNrToPartListEntryMap) {
        String matNr = entry.getPart().getAsId().getMatNr();
        if ((entry instanceof iPartsDataPartListEntry) && !matNr.isEmpty()) {
            iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)entry;
            List<iPartsDataPartListEntry> partListEntriesWithEqualMatNr = partNrToPartListEntryMap.get(matNr);
            if (partListEntriesWithEqualMatNr == null) {
                partListEntriesWithEqualMatNr = new ArrayList<>();
                partNrToPartListEntryMap.put(matNr, partListEntriesWithEqualMatNr);
            }
            partListEntriesWithEqualMatNr.add(partListEntry);
        }
    }

    public static void updateStates(iPartsDataReplacePartList replacementsRetail, iPartsDataReplacePart newReplacePart,
                                    boolean isFinalReleaseState) {
        iPartsDataReleaseState stateOfNewReplacement = iPartsDataReleaseState.NEW;
        iPartsDataReleaseState stateOfOldReplacements = iPartsDataReleaseState.CHECK_NOT_RELEVANT;

        final Set<String> notUpdateStates = new HashSet<>(2);
        notUpdateStates.add(iPartsDataReleaseState.NEW.getDbValue());
        notUpdateStates.add(iPartsDataReleaseState.NOT_RELEVANT.getDbValue());

        if (isFinalReleaseState) {
            stateOfNewReplacement = iPartsDataReleaseState.RELEASED;
            stateOfOldReplacements = iPartsDataReleaseState.NOT_RELEVANT;
        }

        for (iPartsDataReplacePart existingReplacePart : replacementsRetail) {
            iPartsReplacement.Source existingReplacePartSource = iPartsReplacement.Source.getFromDBValue(existingReplacePart.getFieldValue(iPartsConst.FIELD_DRP_SOURCE));
            if ((existingReplacePartSource != iPartsReplacement.Source.PRIMUS) && existingReplacePart.isSameReplacement(newReplacePart)) {
                // die Ersetzung zwischen diesem Vorgänger und Nachfolger existiert bereits, aber es haben sich Daten geändert.
                // Neue und nicht relevante Ersetzungen dürfen nicht geupdated werden, außer es handelt sich um den finalen Status.
                if (notUpdateStates.contains(existingReplacePart.getFieldValue(iPartsConst.FIELD_DRP_STATUS)) && !isFinalReleaseState) {
                    continue;
                }

                existingReplacePart.setFieldValue(iPartsConst.FIELD_DRP_STATUS, stateOfOldReplacements.getDbValue(), DBActionOrigin.FROM_EDIT);
            }
        }

        newReplacePart.setFieldValue(iPartsConst.FIELD_DRP_STATUS, stateOfNewReplacement.getDbValue(), DBActionOrigin.FROM_EDIT);
    }

    /**
     * Liefert alle relevanten Konstruktions-Ersetzungen zurück für die übergebenen Ersetzungs-Daten in {@code
     * dataReplacement} und die potenziellen Konstruktions-Vorgänger und -Nachfolger und Berücksichtigung der Vorrangregel.
     *
     * @param dataReplacement
     * @param isReplaceConstMat
     * @param potentialPredecessors
     * @param potentialSuccessors
     * @return
     */
    public static List<iPartsReplacementConst> getConstReplacementsForPotentialPLEs(EtkDataObject dataReplacement, boolean isReplaceConstMat,
                                                                                    Collection<iPartsDataPartListEntry> potentialPredecessors,
                                                                                    Collection<iPartsDataPartListEntry> potentialSuccessors) {
        List<iPartsReplacementConst> allRelevantReplacements = new DwList<>();

        // Es müssen sowohl Vorgänger und Nachfolger der Ersetzung in der Stückliste sein.
        if ((potentialSuccessors == null) || (potentialPredecessors == null)) {
            // Wenn nicht, dann wirkt sich diese Ersetzung auf keinen Fall auf die Stückliste aus
            return allRelevantReplacements;
        }

        String replacementSDATA;
        if (isReplaceConstMat) {
            replacementSDATA = dataReplacement.getFieldValue(iPartsConst.FIELD_DRCM_SDATA);
        } else {
            replacementSDATA = dataReplacement.getFieldValue(iPartsConst.FIELD_DRCP_SDATA);
        }
        for (iPartsDataPartListEntry potentialPredecessor : potentialPredecessors) {
            // SDATB vom Vorgänger schon frühzeitig gegen das SDATA der Ersetzung prüfen, um die Schleife schnellstmöglich
            // verlassen zu können
            if (!replacementSDATA.equals(potentialPredecessor.getSDATB())) {
                continue;
            }

            List<iPartsReplacementConst> replacementsWithSamePV = new DwList<>();
            List<iPartsReplacementConst> replacementsWithSamePos = new DwList<>();
            List<iPartsReplacementConst> replacementsWithSameSM = new DwList<>();
            for (iPartsDataPartListEntry potentialSuccessor : potentialSuccessors) {
                // SDATA vom Nachfolger schon frühzeitig gegen das SDATA der Ersetzung prüfen, um die Schleife schnellstmöglich
                // verlassen zu können
                if (!replacementSDATA.equals(potentialSuccessor.getSDATA())) {
                    continue;
                }

                // Ausführungsart vom Vorgänger schon frühzeitig gegen die Ausführungsart vom Nachfolger prüfen, um die
                // Schleife schnellstmöglich verlassen zu können
                if (!potentialPredecessor.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA).equals(potentialSuccessor.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA))) {
                    continue;
                }

                iPartsReplacementConst replacement;
                if (isReplaceConstMat) {
                    replacement = new iPartsReplacementConst((iPartsDataReplaceConstMat)dataReplacement, potentialPredecessor,
                                                             potentialSuccessor);
                } else {
                    replacement = new iPartsReplacementConst((iPartsDataReplaceConstPart)dataReplacement, potentialPredecessor,
                                                             potentialSuccessor);
                }
                if (replacement.isValid()) {
                    if (potentialPredecessor.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSE).equals(potentialSuccessor.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSE))) {
                        if (potentialPredecessor.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSV).equals(potentialSuccessor.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSV))) {
                            replacementsWithSamePV.add(replacement);
                        } else {
                            replacementsWithSamePos.add(replacement);
                        }
                    } else {
                        replacementsWithSameSM.add(replacement);
                    }
                }
            }

            // DAIMLER-15525: Ersetzungen über Vorrangregel erzeugen -> gleiche POS und PV gewinnt vor gleicher POS mit
            // Fallback auf gesamtes Submodul
            List<iPartsReplacementConst> relevantReplacements;
            if (!replacementsWithSamePV.isEmpty()) {
                relevantReplacements = replacementsWithSamePV;
            } else if (!replacementsWithSamePos.isEmpty()) {
                relevantReplacements = replacementsWithSamePos;
            } else {
                relevantReplacements = replacementsWithSameSM;
            }

            allRelevantReplacements.addAll(relevantReplacements);
        }

        return allRelevantReplacements;
    }


    public boolean isHandlePrimusHints(EtkProject project, iPartsDataAssembly assembly) {
        // DAIMLER-10211: Primus-Hinweise sollen nur ausgegeben werden falls die Option aktiv ist.
        boolean outputPrimusHints = iPartsPlugin.isPrimusHintHandling();
        // DAIMLER-10859: Am Produkt kann explizit geregelt werden, ob Primus-Hinweise ausgegeben werden sollen.
        // Wenn dieses Kennzeichen nicht gesetzt ist, werden PRIMUS-Hinweise ausgegeben.
        // Falls generell PRIMUS-Hinweise ausgegeben werden sollen, prüfen ob das Gleiche für das Produkt gilt.
        if (outputPrimusHints) {
            iPartsProductId productId = assembly.getProductIdFromModuleUsage();
            if (productId != null) {
                iPartsProduct product = iPartsProduct.getInstance(project, productId);
                if (product != null) {
                    outputPrimusHints = !product.isNoPrimusHints();
                }
            }
        }
        return outputPrimusHints;
    }
}
