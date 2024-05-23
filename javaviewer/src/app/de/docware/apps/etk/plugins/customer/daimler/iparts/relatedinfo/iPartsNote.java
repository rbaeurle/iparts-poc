/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Datenklasse für eine Notiz aus der Related Info in iParts ({@link de.docware.apps.etk.base.project.common.EtkNote ist
 * zu umfangreich und komplex} inkl. Cache für alle Notizen.
 */
public class iPartsNote implements iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, Map<PartListEntryId, List<iPartsNote>>> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    private String language;
    private String type;
    private String kVari;
    private String kVer;
    private String kLfdnr;
    private String seqNo;
    private EtkMultiSprache multiLangTitle;
    private String category;
    private EtkMultiSprache multiLangText;

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized Map<PartListEntryId, List<iPartsNote>> getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsNote.class, "Notes", false);
        Map<PartListEntryId, List<iPartsNote>> result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private static Map<PartListEntryId, List<iPartsNote>> load(EtkProject project) {
        // Alle Notizen aus der Notiz-Tabelle laden und in die Map aufnehmen
        Map<PartListEntryId, List<iPartsNote>> result = new HashMap<PartListEntryId, List<iPartsNote>>();
        String[] fields = new String[]{ FIELD_N_SPRACH, FIELD_N_TYP, FIELD_N_KVARI, FIELD_N_KVER, FIELD_N_KLFDNR, FIELD_N_LFDNR,
                                        FIELD_N_TITEL, FIELD_N_TEXT, FIELD_N_CATEGORY };
        String[] sortFields = new String[]{ FIELD_N_KVARI, FIELD_N_KVER, FIELD_N_KLFDNR, FIELD_N_TYP, FIELD_N_SPRACH, FIELD_N_LFDNR };

        // ExtendedDataTypeLoadType.NONE, weil es keine erweiterten Datentypen in der Notiz-Tabelle gibt
        DBDataObjectAttributesList notesAttributesList = project.getDbLayer().getAttributesListSorted(TABLE_NOTIZ, fields, null,
                                                                                                      null, sortFields);
        for (DBDataObjectAttributes noteAttributes : notesAttributesList) {
            PartListEntryId partListEntryId = new PartListEntryId(noteAttributes.getFieldValue(FIELD_N_KVARI),
                                                                  noteAttributes.getFieldValue(FIELD_N_KVER),
                                                                  noteAttributes.getFieldValue(FIELD_N_KLFDNR));

            List<iPartsNote> notesList = result.get(partListEntryId);
            boolean addNewNote = true;
            if (notesList != null) {
                // Alle schon vorhandenen Notizen durchgehen und prüfen, ob es schon eine mit der gleichen N_LFDNR
                // und gleichem Typ gibt. Wenn ja, dann einfach die neue Sprache mit anhängen
                for (iPartsNote existingNote : notesList) {
                    if ((existingNote.getSeqNo().equals(noteAttributes.getFieldValue(FIELD_N_LFDNR)))
                        && (existingNote.getType().equals(noteAttributes.getFieldValue(FIELD_N_TYP)))) {
                        existingNote.setMultiLangTextAndTitle(noteAttributes.getFieldValue(FIELD_N_SPRACH),
                                                              noteAttributes.getFieldValue(FIELD_N_TEXT),
                                                              noteAttributes.getFieldValue(FIELD_N_TITEL));
                        addNewNote = false;
                        break;
                    }
                }
            }

            if (addNewNote) {
                iPartsNote note = new iPartsNote(noteAttributes.getFieldValue(FIELD_N_SPRACH), noteAttributes.getFieldValue(FIELD_N_TYP),
                                                 partListEntryId.getKVari(), partListEntryId.getKVer(), partListEntryId.getKLfdnr(),
                                                 noteAttributes.getFieldValue(FIELD_N_LFDNR), noteAttributes.getFieldValue(FIELD_N_TITEL),
                                                 noteAttributes.getFieldValue(FIELD_N_TEXT), noteAttributes.getFieldValue(FIELD_N_CATEGORY));
                if (notesList == null) {
                    notesList = new DwList<iPartsNote>(1);
                    result.put(partListEntryId, notesList);
                }
                notesList.add(note);
            }
        }

        return result;
    }

    /**
     * Gibt es Notizen für die übergebene Stücklistenposition (OHNE Notizen am Material!)?
     *
     * @param partListEntryId
     * @param project
     * @return
     */
    public static boolean hasNote(PartListEntryId partListEntryId, EtkProject project) {
        return getInstance(project).containsKey(partListEntryId);
    }

    /**
     * Liefert alle Notizen für die übergebene Stücklistenposition (OHNE Notizen am Material!) zurück.
     *
     * @param partListEntryId
     * @param project
     * @return {@code null} falls es keine Notizen für die übergebene Stücklistenposition gibt
     */
    public static List<iPartsNote> getNotes(PartListEntryId partListEntryId, EtkProject project) {
        List<iPartsNote> notes = getInstance(project).get(partListEntryId);
        if (notes != null) {
            return Collections.unmodifiableList(notes);
        } else {
            return null;
        }
    }

    /**
     * Gibt es Notizen für das übergebene Material?
     *
     * @param partId
     * @param project
     * @return
     */
    public static boolean hasNote(PartId partId, EtkProject project) {
        return getInstance(project).containsKey(new PartListEntryId(partId.getMatNr(), partId.getMVer(), ""));
    }

    /**
     * Liefert alle Notizen für das übergebene Material zurück.
     *
     * @param partId
     * @param project
     * @return {@code null} falls es keine Notizen für das übergebene Material gibt
     */
    public static List<iPartsNote> getNotes(PartId partId, EtkProject project) {
        List<iPartsNote> notes = getInstance(project).get(new PartListEntryId(partId.getMatNr(), partId.getMVer(), ""));
        if (notes != null) {
            return Collections.unmodifiableList(notes);
        } else {
            return null;
        }
    }

    /**
     * Liefert alle Notizen für den übergebenen Stücklisteneintrag inkl. Notizen am Material zurück.
     *
     * @param partListEntry
     * @param project
     * @return {@code null} falls es keine Notizen für den übergebenen Stücklisteneintrag gibt
     */
    public static List<iPartsNote> getNotes(EtkDataPartListEntry partListEntry, EtkProject project) {
        List<iPartsNote> notesForPartListEntry = getNotes(partListEntry.getAsId(), project);
        List<iPartsNote> notesForPart = getNotes(partListEntry.getPart().getAsId(), project);
        if (notesForPartListEntry != null) {
            if (notesForPart != null) {
                // Notizen vom Stücklisteneintrag und Material zusammenführen
                List<iPartsNote> notes = new DwList<iPartsNote>(notesForPartListEntry.size() + notesForPart.size());
                notes.addAll(notesForPartListEntry);
                notes.addAll(notesForPart);
                return Collections.unmodifiableList(notes);
            } else {
                return notesForPartListEntry;
            }
        } else {
            return notesForPart; // kann auch null sein
        }
    }

    public iPartsNote(String language, String type, String kVari, String kVer, String kLfdnr, String seqNo, String title,
                      String text, String category) {
        this.language = language;
        this.type = type;
        this.kVari = kVari;
        this.kVer = kVer;
        this.kLfdnr = kLfdnr;
        this.seqNo = seqNo;
        this.category = category;
        this.multiLangText = new EtkMultiSprache(new String[]{ language }, new String[]{ text });
        this.multiLangTitle = new EtkMultiSprache(new String[]{ language }, new String[]{ title });
    }


    private void setMultiLangTextAndTitle(String noteLanguage, String text, String title) {
        this.multiLangText.setText(noteLanguage, text);
        this.multiLangTitle.setText(noteLanguage, title);
    }

    public String getLanguage() {
        return language;
    }

    public String getType() {
        return type;
    }

    public String getkVari() {
        return kVari;
    }

    public String getkVer() {
        return kVer;
    }

    public String getkLfdnr() {
        return kLfdnr;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public EtkMultiSprache getMultiLangTitle() {
        return multiLangTitle;
    }

    public String getCategory() {
        return category;
    }

    public EtkMultiSprache getMultiLangText() {
        return multiLangText;
    }
}