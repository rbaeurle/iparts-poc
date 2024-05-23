package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataES1;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataES1List;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsES1Id;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsPartFootnotesCache;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.*;

/**
 * ES1 Codes zu Typen (DA_ES1)
 */
public class iPartsES1 implements iPartsConst {

    public static final String VALID_MAT_STATE_FOR_ES1 = "30";

    private static ObjectInstanceStrongLRUList<Object, iPartsES1> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    // Die Liste der FootNotes zu einer ES1-ID
    protected Map<String, Set<String>> es1FootNotes = new HashMap<>();
    // Die Liste der Typen zu einer ES1-ID
    protected Map<String, String> es1TypePerFootNote = new HashMap<>();

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized iPartsES1 getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsES1.class, "ES1", false);
        iPartsES1 result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsES1();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private void load(EtkProject project) {
        iPartsDataES1List dataES1List = new iPartsDataES1List();
        dataES1List.load(project);
        for (iPartsDataES1 dataES1 : dataES1List) {
            // Die ID nur einmal holen, wird öfter gebraucht.
            iPartsES1Id es1Id = dataES1.getAsId();

            // Fußnote vorhanden?
            String footNoteNumber = StrUtils.removeLeadingCharsFromString(dataES1.getFootNote(), '0');
            if (!footNoteNumber.isEmpty()) {
                // Die Liste der Fußnoten zu einem ES1-Code füllen
                Set<String> footNotes = es1FootNotes.computeIfAbsent(es1Id.getES1Code(), key -> new HashSet<>());

                // Falls die Fußnote mit führenden Nullen in Excel-Datei für den benutzerdefinierten Import vorhanden war,
                // beim Einlesen die führenden Nullen gleich abschneiden.
                // Beim späteren Vergleich in ipartsDataES1.footnoteCheck() wird ohne führende Nullen verglichen.
                footNotes.add(footNoteNumber);
            }

            // Die Liste der Typen zu einer ES1-ID füllen
            if (!es1TypePerFootNote.containsKey(es1Id.getES1Code())) {
                es1TypePerFootNote.put(es1Id.getES1Code(), dataES1.getES1Type());
            }
        }
    }

    /**
     * Liefert ES1 Fußnoten für einen ES1 Code
     *
     * @param es1 ES1 Code als String
     * @return
     */
    public Set<String> getFootNotes(String es1) {
        return es1FootNotes.get(es1);
    }

    /**
     * Liefert den ES1 Typ für einen ES1 Code
     *
     * @param es1 ES1 Code als String
     * @return ES1 Typ (Enum-Wert)
     */
    public String getType(String es1) {
        return es1TypePerFootNote.get(es1);
    }

    /**
     * Die spezielle ES1-Fußnotennummernprüfung, DAIMLER-9557:
     * <p>
     * Alle Fußnoten aus DA_ES1 passend zum ES1-Schlüssel holen ==> ES1-Fußnoten-Liste
     * Dann gucken, ob es für die M_BASE_MATNR des Alternativteils eine Fußnote in DA_FN_MAT_REF gibt, die auch in der ES1-Fußnoten-Liste
     * enthalten ist
     * ja ==> ES2-Codes M_AS_ES2 ausgeben, nein ==> ES2-Code unterdrücken
     *
     * @param es1
     * @param alternativePartBaseMatNr
     * @param project
     */
    public boolean checkFootnoteValidity(String es1, String alternativePartBaseMatNr, EtkProject project) {
        // Gibt es überhaupt Fußnoten zum ES1?
        Set<String> footNotesForES1 = getFootNotes(es1);
        if ((footNotesForES1 == null) || footNotesForES1.isEmpty()) {
            return false;
        }

        // Basis-Materialnummer bestimmen (darf nicht leer sein)
        if (alternativePartBaseMatNr.isEmpty()) {
            return false;
        }

        // Die Fußnotennummern vom Alternativ-Teil über den Cache bestimmen
        List<iPartsFootNote> footnotesForAlternativePart = iPartsPartFootnotesCache.getInstance(project).getFootnotesForPart(project,
                                                                                                                             new PartId(alternativePartBaseMatNr, ""));
        if (footnotesForAlternativePart != null) {
            for (iPartsFootNote footNote : footnotesForAlternativePart) {
                // Die Fußnote kann auch mit führenden Nullen vorkommen '000697' statt nur '697'
                String footNoteNumber = StrUtils.removeLeadingCharsFromString(footNote.getFootNoteId().getFootNoteId(), '0');
                if (footNotesForES1.contains(footNoteNumber)) {
                    return true;
                }
            }
        }
        return false;
    }
}


