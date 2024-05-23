package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteCatalogueRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Hilfsklasse für die Vererbung von Fußnoten vor dem Speichern in Edit
 * Es sind Hilfsmethoden für PartList-Edit als auch für Vereinheitlichen im Edit vorhanden.
 * Zusätzlich wird auch die Vererbung bei der Übernahme nach AS abgehandelt (inverses Vorgehen)
 *
 * Vorgehensweise:
 * In {@link EditEqualizeFieldsHelper} wird die Fußnote als spezielles Attribut verwaltet
 */
public class EditEqualizeFootNoteHelper {

    private static final String FN_DONT_USE_OLD_PART = "414";

    // Liste von Fußnoten-IDs, die NICHT vererbt werden sollen
    public static final Set<String> DONT_INHERIT_FOOTNOTE_NUMBERS = new HashSet<>();

    private static final String KEY_DELIMTER = "&";

    static {
        DONT_INHERIT_FOOTNOTE_NUMBERS.add(FN_DONT_USE_OLD_PART);
    }

    private EtkProject project;
    private boolean explicitMapKey = false;  // wenn true: mehrere gleiche Fußnoten erlaubt

    /**
     * Für Vererbung nach dem Fußnoten-Editor
     * Überprüft, ob sourceCatalogueRefs neue oder gelöschte Fußnoten enthält und vererbt sie an den destPartListEntry
     * (unter Berücksichtigung der 414-Fußnote - wird nicht übernommen)
     * Die neu erzeugten {@link iPartsDataFootNoteCatalogueRef} stehen im Rückgabewert.
     *
     * @param project
     * @param sourceCatalogueRefs
     * @param destPartListEntry
     * @return
     */
    public static iPartsDataFootNoteCatalogueRefList equalizeOnePartListEntryFootNote(EtkProject project,
                                                                                      iPartsDataFootNoteCatalogueRefList sourceCatalogueRefs,
                                                                                      EtkDataPartListEntry destPartListEntry) {
        iPartsDataFootNoteCatalogueRefList fnCatalogueRefList = new iPartsDataFootNoteCatalogueRefList();
        if ((sourceCatalogueRefs != null) && (!sourceCatalogueRefs.isEmpty() || !sourceCatalogueRefs.getDeletedList().isEmpty())) {
            EditEqualizeFootNoteHelper equalizeFootNoteHelper = new EditEqualizeFootNoteHelper(project);
            iPartsDataFootNoteCatalogueRefList inheritedFootnotes = equalizeFootNoteHelper.inheritFootNotes(sourceCatalogueRefs,
                                                                                                            destPartListEntry,
                                                                                                            true);

            // erst jetzt zur DB-Liste hinzufügen
            fnCatalogueRefList.addAll(inheritedFootnotes, DBActionOrigin.FROM_EDIT);
        }
        return fnCatalogueRefList;
    }

    /**
     * Für Übernahme nach AS
     * Überprüft, ob sourcePartListEntry Fußnoten besitzt und vererbt sie an den destPartListEntry
     * (unter Berücksichtigung der 414-Fußnote - wird nicht übernommen)
     * Die neu erzeugten {@link iPartsDataFootNoteCatalogueRef} stehen in fnCatalogueRefList
     *
     * @param project
     * @param fnCatalogueRefList
     * @param sourcePartListEntry
     * @param destPartListEntry
     */
    public static void inheritOnePartListEntryFootNote(EtkProject project, iPartsDataFootNoteCatalogueRefList fnCatalogueRefList,
                                                       EtkDataPartListEntry sourcePartListEntry,
                                                       EtkDataPartListEntry destPartListEntry) {
        EditEqualizeFootNoteHelper equalizeFootNoteHelper = new EditEqualizeFootNoteHelper(project);
        equalizeFootNoteHelper.inheritOnePartListEntryFootNote(fnCatalogueRefList, sourcePartListEntry, destPartListEntry);
    }


    public EditEqualizeFootNoteHelper(EtkProject project) {
        this.project = project;
    }

    /**
     * Überprüft, ob sourcePartListEntry Fußnoten besitzt und vererbt sie an den destPartListEntry
     * (unter Berücksichtigung der 414-Fußnote - wird nicht übernommen)
     * Die neu erzeugten {@link iPartsDataFootNoteCatalogueRef} stehen in fnCatalogueRefList
     *
     * @param fnCatalogueRefList
     * @param sourcePartListEntry
     * @param destPartListEntry
     */
    public void inheritOnePartListEntryFootNote(iPartsDataFootNoteCatalogueRefList fnCatalogueRefList,
                                                EtkDataPartListEntry sourcePartListEntry,
                                                EtkDataPartListEntry destPartListEntry) {

        Collection<iPartsFootNote> fnList = ((iPartsDataPartListEntry)sourcePartListEntry).getFootNotes();
        if ((fnList != null) && !fnList.isEmpty()) {
            // Fußnoten Referenzen kopieren
            iPartsDataFootNoteCatalogueRefList fnRefList = iPartsDataFootNoteCatalogueRefList.loadFootNotesForPartListEntry(project,
                                                                                                                            sourcePartListEntry.getAsId());
            // Fußnoten werden erst in einer Liste aufgesammelt, da später evtl. die Sequenznummern angepasst werden müssen
            iPartsDataFootNoteCatalogueRefList inheritedFootnotes = inheritFootNotes(fnRefList, destPartListEntry, false);

            // erst jetzt zur DB-Liste hinzufügen
            fnCatalogueRefList.addAll(inheritedFootnotes, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Vererbt die Fußnoten {@link iPartsDataFootNoteCatalogueRefList} in sourceCatalogueRefs an den destPartListEntry
     * (unter Berücksichtigung der 414-Fußnote - wird nicht übernommen)
     * Das Ergebnis {@link List<iPartsDataFootNoteCatalogueRef>} wird als Rückgabewert zur Verfügung gestellt.
     *
     * @param sourceCatalogueRefs
     * @param destPartListEntry
     * @param checkDestForNoDoubles
     * @return
     */
    public iPartsDataFootNoteCatalogueRefList inheritFootNotes(iPartsDataFootNoteCatalogueRefList sourceCatalogueRefs,
                                                               EtkDataPartListEntry destPartListEntry,
                                                               boolean checkDestForNoDoubles) {
        return inheritFootNotes(sourceCatalogueRefs, destPartListEntry, null, checkDestForNoDoubles);
    }

    /**
     * Vererbt die Fußnoten {@link iPartsDataFootNoteCatalogueRefList} in sourceCatalogueRefs an den destPartListEntry
     * (unter Berücksichtigung der 414-Fußnote - wird nicht übernommen)
     * Das Ergebnis {@link List<iPartsDataFootNoteCatalogueRef>} wird als Rückgabewert zur Verfügung gestellt.
     *
     * @param sourceCatalogueRefs
     * @param destPartListEntry
     * @param destRefList
     * @param checkDestForNoDoubles
     * @return
     */
    public iPartsDataFootNoteCatalogueRefList inheritFootNotes(iPartsDataFootNoteCatalogueRefList sourceCatalogueRefs,
                                                               EtkDataPartListEntry destPartListEntry,
                                                               iPartsDataFootNoteCatalogueRefList destRefList,
                                                               boolean checkDestForNoDoubles) {
        // Vererbte Fußnoten werden in einer Liste aufgesammelt
        iPartsDataFootNoteCatalogueRefList inheritedFootnotes = new iPartsDataFootNoteCatalogueRefList();

        // Diese Map enthält die nach Sequenznummer sortierten neuen Fußnoten und auch die bereits im Ziel-Stücklisteneintrag
        // vorhandenen Fußnoten, was für die evtl. spätere Anpassung der Sequenznummern notwendig ist
        Map<String, iPartsDataFootNoteCatalogueRef> allFootnotesMap = new TreeMap<>();

        // Diese Liste enthält alle Fußnoten in der gewünschten Reihenfolge inkl. der nicht vererbten Fußnoten (414 er)
        Collection<iPartsDataFootNoteCatalogueRef> allFootnotesWithDontInherit;

        boolean rearrangeSeqNrNecessary = false;
        if ((sourceCatalogueRefs != null) && (!sourceCatalogueRefs.isEmpty() || !sourceCatalogueRefs.getDeletedList().isEmpty())) {
            if (checkDestForNoDoubles) {
                // Map um im Destination vorhandene iPartsDataFootNoteCatalogueRef wieder zu verwenden
                Map<String, iPartsDataFootNoteCatalogueRef> destFootNoteCatalogueRefMap;
                if (destRefList == null) {
                    destFootNoteCatalogueRefMap = fillCatalogueRefMap(destPartListEntry);
                } else {
                    destFootNoteCatalogueRefMap = fillCatalogueRefMap(destPartListEntry, destRefList);
                }

                // Über alle neuen Fußnoten iterieren
                for (iPartsDataFootNoteCatalogueRef fnCatalogueRef : sourceCatalogueRefs) {
                    String footNoteId = fnCatalogueRef.getAsId().getFootNoteId();
                    if (!DONT_INHERIT_FOOTNOTE_NUMBERS.contains(footNoteId)) {
                        iPartsDataFootNoteCatalogueRef destFnCatalogueRef = getCatalogueRefFromMap(destFootNoteCatalogueRefMap,
                                                                                                   fnCatalogueRef);
                        if (destFnCatalogueRef != null) {
                            String key = buildMapKey(destFnCatalogueRef);
                            String destSeqNo = destFnCatalogueRef.getSequenceNumber();
                            String newSeqNo = fnCatalogueRef.getSequenceNumber();
                            if (!destSeqNo.equals(newSeqNo)) {
                                // Alle Werte (speziell Fußnoten-ID) außer der SeqNo passen -> nur die SeqNo korrigieren
                                destFnCatalogueRef.setFieldValue(iPartsConst.FIELD_DFNK_FN_SEQNO, newSeqNo,
                                                                 DBActionOrigin.FROM_EDIT);
                                inheritedFootnotes.add(destFnCatalogueRef, DBActionOrigin.FROM_EDIT);

                                // Position in allFootnotesMap korrigieren
                                removeFnCatalogueRefFromMap(allFootnotesMap, destFnCatalogueRef);
                            }
                            allFootnotesMap.put(newSeqNo, destFnCatalogueRef);
                            destFootNoteCatalogueRefMap.remove(key);
                        } else {
                            iPartsDataFootNoteCatalogueRef newFnCatalogueRef = createFootNote(project, destPartListEntry,
                                                                                              fnCatalogueRef);
                            inheritedFootnotes.add(newFnCatalogueRef, DBActionOrigin.FROM_EDIT);
                            allFootnotesMap.put(newFnCatalogueRef.getSequenceNumber(), newFnCatalogueRef);
                        }
                    } else {
                        // wenn eine 414 gefunden wurde, dann müssen die Sequenznummern korrigiert werden, da sonst eine Lücke entsteht
                        rearrangeSeqNrNecessary = true;
                    }
                }

                // Über alle in sourcePartListEntry gelöschten Fußnoten iterieren
                if (!sourceCatalogueRefs.getDeletedList().isEmpty()) {
                    for (iPartsDataFootNoteCatalogueRef fnCatalogueRef : sourceCatalogueRefs.getDeletedList()) {
                        String footNoteId = fnCatalogueRef.getAsId().getFootNoteId();
                        if (!DONT_INHERIT_FOOTNOTE_NUMBERS.contains(footNoteId)) {
                            iPartsDataFootNoteCatalogueRef destFnCatalogueRef = getCatalogueRefFromMap(destFootNoteCatalogueRefMap,
                                                                                                       fnCatalogueRef);
                            if (destFnCatalogueRef != null) {
                                inheritedFootnotes.delete(destFnCatalogueRef, true, DBActionOrigin.FROM_EDIT);
                                removeFnCatalogueRefFromMap(allFootnotesMap, destFnCatalogueRef);
                                destFootNoteCatalogueRefMap.remove(buildMapKey(destFnCatalogueRef));
                                rearrangeSeqNrNecessary = true;
                            }
                        }
                    }
                }

                allFootnotesWithDontInherit = new DwList<>(allFootnotesMap.values());

                // fnRefs, die im destPartListEntry übrig sind löschen (außer 414 er, die müssen hinten hinzugefügt werden)
                if (!destFootNoteCatalogueRefMap.isEmpty()) {
                    for (iPartsDataFootNoteCatalogueRef destFnCatalogueRef : destFootNoteCatalogueRefMap.values()) {
                        String footNoteId = destFnCatalogueRef.getAsId().getFootNoteId();
                        allFootnotesWithDontInherit.remove(destFnCatalogueRef);
                        rearrangeSeqNrNecessary = true;

                        // 414 er hinten hinzufügen
                        if (DONT_INHERIT_FOOTNOTE_NUMBERS.contains(footNoteId)) {
                            allFootnotesWithDontInherit.add(destFnCatalogueRef);
                        } else { // Ansonsten löschen
                            inheritedFootnotes.delete(destFnCatalogueRef, true, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
            } else {
                // Routine für die Übernahme nach AS (inverses Vorgehen)
                for (iPartsDataFootNoteCatalogueRef fnCatalogueRef : sourceCatalogueRefs) {
                    // 414 er Standardfußnoten dürfen nicht vererbt werden, da sie durch bestimmte Ersetzungen erzeugt werden
                    // und durch Vererbung auch am Nachfolger landen könnten, was aber fachlich falsch ist (DAIMLER-8571)
                    String footNoteId = fnCatalogueRef.getAsId().getFootNoteId();
                    if (!DONT_INHERIT_FOOTNOTE_NUMBERS.contains(footNoteId)) {
                        iPartsDataFootNoteCatalogueRef newFnCatalogueRef = createFootNote(project, destPartListEntry, fnCatalogueRef);
                        inheritedFootnotes.add(newFnCatalogueRef, DBActionOrigin.FROM_EDIT);
                        allFootnotesMap.put(newFnCatalogueRef.getSequenceNumber(), newFnCatalogueRef);
                    } else {
                        // wenn eine 414 gefunden wurde, dann müssen die Sequenznummern korrigiert werden, da sonst eine Lücke entsteht
                        rearrangeSeqNrNecessary = true;
                    }
                }

                allFootnotesWithDontInherit = allFootnotesMap.values();
            }

            // Sequenznummern ggf. neu verteilen
            inheritedFootnotes = rearrangeSeqNr(rearrangeSeqNrNecessary, inheritedFootnotes, allFootnotesWithDontInherit);
        }
        return inheritedFootnotes;
    }

    private iPartsDataFootNoteCatalogueRefList rearrangeSeqNr(boolean rearrangeSeqNrNecessary,
                                                              iPartsDataFootNoteCatalogueRefList inheritedFootnotes,
                                                              Collection<iPartsDataFootNoteCatalogueRef> allFootnotesWithDontInherit) {
        // Sequenznummern ggf. neu verteilen
        if (rearrangeSeqNrNecessary) {
            rearrangeFNSeqNr(allFootnotesWithDontInherit, 1);

            // inheritedFootnotes neu bestimmen, weil durch die Neuberechnung der Sequenznummern im Worst Case auch
            // bisher nicht veränderte Fußnoten-Referenzen verändert worden sein können
            List<iPartsDataFootNoteCatalogueRef> deletedList = new DwList<>(inheritedFootnotes.getDeletedList());
            inheritedFootnotes.clear(DBActionOrigin.FROM_DB);
            for (iPartsDataFootNoteCatalogueRef deletedDataFootNoteCatalogueRef : deletedList) {
                inheritedFootnotes.delete(deletedDataFootNoteCatalogueRef, true, DBActionOrigin.FROM_EDIT);
            }
            for (iPartsDataFootNoteCatalogueRef dataFootNoteCatalogueRef : allFootnotesWithDontInherit) {
                if (dataFootNoteCatalogueRef.isModified()) {
                    inheritedFootnotes.add(dataFootNoteCatalogueRef, DBActionOrigin.FROM_EDIT);
                }
            }
        }
        return inheritedFootnotes;
    }

    private void removeFnCatalogueRefFromMap(Map<String, iPartsDataFootNoteCatalogueRef> footnotesMap, iPartsDataFootNoteCatalogueRef fnCatalogueRef) {
        Iterator<iPartsDataFootNoteCatalogueRef> footnotesIterator = footnotesMap.values().iterator();
        while (footnotesIterator.hasNext()) {
            iPartsDataFootNoteCatalogueRef footnoteRef = footnotesIterator.next();
            if (footnoteRef == fnCatalogueRef) {
                footnotesIterator.remove();
                break;
            }
        }
    }

    private String buildMapKey(iPartsDataFootNoteCatalogueRef fnCatalogueRef) {
        if (explicitMapKey) {
            return fnCatalogueRef.getAsId().getFootNoteId() + KEY_DELIMTER + fnCatalogueRef.getSequenceNumber();
        } else {
            return fnCatalogueRef.getAsId().getFootNoteId();
        }
    }

    private Map<String, iPartsDataFootNoteCatalogueRef> fillCatalogueRefMap(EtkDataPartListEntry destPartListEntry) {
        iPartsDataFootNoteCatalogueRefList destRefList = iPartsDataFootNoteCatalogueRefList.loadFootNotesForPartListEntry(project,
                                                                                                                          destPartListEntry.getAsId());
        return fillCatalogueRefMap(destPartListEntry, destRefList);
    }

    private Map<String, iPartsDataFootNoteCatalogueRef> fillCatalogueRefMap(EtkDataPartListEntry destPartListEntry,
                                                                            iPartsDataFootNoteCatalogueRefList destRefList) {
        Map<String, iPartsDataFootNoteCatalogueRef> destFootNoteCatalogueRefMap = new LinkedHashMap<>();
        if (destRefList != null) {
            // Mehrere gleiche FootNote-IDs in der CatalogueRef evtl. zusammenfassen
            for (iPartsDataFootNoteCatalogueRef destFnCatalogueRef : destRefList) {
                destFootNoteCatalogueRefMap.put(buildMapKey(destFnCatalogueRef), destFnCatalogueRef);
            }
        }
        return destFootNoteCatalogueRefMap;
    }

    private iPartsDataFootNoteCatalogueRef getCatalogueRefFromMap(Map<String, iPartsDataFootNoteCatalogueRef> destFootNoteCatalogueRefMap,
                                                                  iPartsDataFootNoteCatalogueRef fnCatalogueRef) {
        iPartsDataFootNoteCatalogueRef catalogueRef = destFootNoteCatalogueRefMap.get(buildMapKey(fnCatalogueRef));
        if (explicitMapKey && (catalogueRef == null)) {
            String searchKey = fnCatalogueRef.getAsId().getFootNoteId() + KEY_DELIMTER;
            for (String key : destFootNoteCatalogueRefMap.keySet()) {
                if (key.startsWith(searchKey)) {
                    return destFootNoteCatalogueRefMap.get(key);
                }
            }
        }
        return catalogueRef;
    }

    // TODO Nach dem Review entfernen
//    private void removeCatalogueRefFromMap(Map<String, List<iPartsDataFootNoteCatalogueRef>> destFootNoteCatalogueRefMap,
//                                           String footNoteId, String seqNo) {
//        List<iPartsDataFootNoteCatalogueRef> refList = destFootNoteCatalogueRefMap.get(footNoteId);
//        if (refList != null) {
//            Iterator<iPartsDataFootNoteCatalogueRef> iterator = refList.iterator();
//            while (iterator.hasNext()) {
//                iPartsDataFootNoteCatalogueRef fnCatalogueRef = iterator.next();
//                if (fnCatalogueRef.getSequenceNumber().equals(seqNo)) {
//                    iterator.remove();
//                    break;
//                }
//            }
//            if (refList.isEmpty()) {
//                destFootNoteCatalogueRefMap.remove(footNoteId);
//            }
//        }
//    }
//
//    /**
//     * Kopiert alle Attribute-Werte (ohne PKValues und TimeStamp) von source nach Dest
//     *
//     * @param project
//     * @param sourceFnCatalogueRef
//     * @param destFnCatalogueRef
//     */
//    private void copyAttributeValuesWithoutPK(EtkProject project, iPartsDataFootNoteCatalogueRef sourceFnCatalogueRef,
//                                              iPartsDataFootNoteCatalogueRef destFnCatalogueRef) {
//        EtkDatabaseTable baseTable = project.getConfig().getDBDescription().findTable(sourceFnCatalogueRef.getTableName());
//        if (baseTable != null) {
//            Set<String> pkFields = new HashSet<>();
//            for (String pkField : sourceFnCatalogueRef.getPKFields()) {
//                pkFields.add(pkField);
//            }
//            pkFields.add(DBConst.FIELD_STAMP);
//            for (EtkDatabaseField baseField : baseTable.getFieldList()) {
//                if (!pkFields.contains(baseField.getName())) {
//                    EditEqualizeFieldsHelper.copyAttributeValue(baseField.getName(), sourceFnCatalogueRef.getAttributes(),
//                                                                destFnCatalogueRef.getAttributes(), sourceFnCatalogueRef);
//                }
//            }
//        }
//    }

    /**
     * Neue Fußnoten-Referenz für den destPartListEntry anlegen
     *
     * @param project
     * @param destPartListEntry
     * @param fnCatalogueRef
     * @return
     */
    private iPartsDataFootNoteCatalogueRef createFootNote(EtkProject project, EtkDataPartListEntry destPartListEntry,
                                                          iPartsDataFootNoteCatalogueRef fnCatalogueRef) {
        iPartsFootNoteCatalogueRefId id = new iPartsFootNoteCatalogueRefId(destPartListEntry.getAsId(),
                                                                           fnCatalogueRef.getAsId().getFootNoteId());
        iPartsDataFootNoteCatalogueRef newFnCatalogueRef = new iPartsDataFootNoteCatalogueRef(project, id);
        newFnCatalogueRef.assignAttributes(project, fnCatalogueRef.getAttributes(), false, DBActionOrigin.FROM_EDIT);

        // ID wird durch assignAttributes() auf die ID von fnCatalogueRef geändert
        newFnCatalogueRef.setId(id, DBActionOrigin.FROM_EDIT);
        newFnCatalogueRef.updateOldId();

        return newFnCatalogueRef;
    }

    /**
     * Verteilt die Sequenznummern der Fußnoten neu.
     *
     * @param footnotes
     * @param startSeqNr
     */
    private void rearrangeFNSeqNr(Collection<iPartsDataFootNoteCatalogueRef> footnotes, int startSeqNr) {
        int seqNr = startSeqNr;
        for (iPartsDataFootNoteCatalogueRef inheritedFootnote : footnotes) {
            inheritedFootnote.setSequenceNumber(seqNr);
            seqNr++;
        }
    }
}
