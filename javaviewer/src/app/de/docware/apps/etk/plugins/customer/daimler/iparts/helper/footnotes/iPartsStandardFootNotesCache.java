package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Cache für Standardfußnoten, der auf jeden Fall auch die Standardfußnoten aus {@link iPartsDefaultStandardFootNote} enthält.
 * Diese werden mit Basiswerten befüllt falls sie nicht in der Datenbank exisiteren (was eigentlich nicht vorkommen sollte).
 */
public class iPartsStandardFootNotesCache {

    private static ObjectInstanceStrongLRUList<Object, iPartsStandardFootNotesCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    // Sortiert nach Fußnotennummer
    private final TreeMap<String, iPartsDataFootNote> fnIdToStandardFootNoteMap = new TreeMap<>();

    public static synchronized iPartsStandardFootNotesCache getInstance(EtkProject project) {
        // Standardfußnoten sind unabhängig von ChangeSets immer die Gleichen.
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsStandardFootNotesCache.class,
                                                             "StandardFootNotesCache", false);
        iPartsStandardFootNotesCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsStandardFootNotesCache();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }


    /**
     * Holt eine Kopie des {@link iPartsDataFootNote}-Objekts zur übergebenen Standardfußnote aus dem Cache.
     * Diese enthält auch alle Felder aus {@link iPartsDataFootNoteContent}.
     *
     * @param standardFootNote
     * @param project
     * @return
     */
    public iPartsDataFootNote getStandardFootNoteWithContent(iPartsDefaultStandardFootNote standardFootNote, EtkProject project) {
        iPartsDataFootNote dataFootNote = fnIdToStandardFootNoteMap.get(standardFootNote.getFootNoteId().getFootNoteId());
        if (dataFootNote != null) {
            return dataFootNote.cloneMe(project);
        } else {
            return null;
        }
    }

    /**
     * Holt alle Standardfußnoten aus dem Cache, analog zu {@link #getStandardFootNoteWithContent(iPartsDefaultStandardFootNote, EtkProject)}.
     *
     * @param project
     * @return
     */
    public Collection<iPartsDataFootNote> getAllStandardFootNotesWithContent(EtkProject project) {
        List<iPartsDataFootNote> clonedStandardFootNotes = new ArrayList<>(fnIdToStandardFootNoteMap.size());
        for (iPartsDataFootNote dataFootNote : fnIdToStandardFootNoteMap.values()) {
            clonedStandardFootNotes.add(dataFootNote.cloneMe(project));
        }
        return Collections.unmodifiableCollection(clonedStandardFootNotes);
    }

    /**
     * Liefert das vollständig befüllte {@link iPartsFootNote}-Objekt (mit Typ {@link iPartsFootnoteType#NO_TYPE})
     * für die übergebene {@link iPartsDefaultStandardFootNote} aus dem Cache zurück.
     *
     * @param standardFootNote
     * @param project
     * @return
     */
    public iPartsFootNote getStandardFootNote(iPartsDefaultStandardFootNote standardFootNote, EtkProject project) {
        String footNoteId = standardFootNote.getFootNoteId().getFootNoteId();
        iPartsDataFootNote dataFootNoteWithContent = fnIdToStandardFootNoteMap.get(footNoteId);
        String footNoteName = dataFootNoteWithContent.getFieldValue(iPartsConst.FIELD_DFN_NAME);
        boolean isStandardFootNote = dataFootNoteWithContent.getFieldValueAsBoolean(iPartsConst.FIELD_DFN_STANDARD);

        // Eigentlichen Fußnotentext hinzufügen, also die erste (und da Standardfußnote einzige) Zeile
        iPartsFootNoteContentId footNoteContentId = new iPartsFootNoteContentId(dataFootNoteWithContent.getAsId().getFootNoteId(),
                                                                                dataFootNoteWithContent.getFieldValue(iPartsConst.FIELD_DFNC_LINE_NO));
        iPartsDataFootNoteContent dataFootNoteContent = new iPartsDataFootNoteContent(project, footNoteContentId);
        dataFootNoteContent.setAttributes(dataFootNoteWithContent.getAttributes(), DBActionOrigin.FROM_DB);
        List<String> footNoteTexts = new DwList<>(1);
        footNoteTexts.add(dataFootNoteContent.getText(project.getDBLanguage(), project.getDataBaseFallbackLanguages()));
        return new iPartsFootNote(standardFootNote.getFootNoteId(), footNoteName, footNoteTexts, isStandardFootNote,
                                  iPartsFootnoteType.NO_TYPE);
    }

    private void load(EtkProject project) {
        iPartsDataFootNoteList dataStandardFootNotes = iPartsDataFootNoteList.loadStandardFootNoteListWithContent(project);
        for (iPartsDataFootNote dataFootNoteWithContent : dataStandardFootNotes) {
            fnIdToStandardFootNoteMap.put(dataFootNoteWithContent.getAsId().getFootNoteId(), dataFootNoteWithContent);
        }

        // Besondere Standardfußnoten immer hinzufügen
        for (iPartsDefaultStandardFootNote standardFootNoteWithDefaults : iPartsDefaultStandardFootNote.values()) {
            iPartsFootNoteId footNoteId = standardFootNoteWithDefaults.getFootNoteId();
            String footNoteIdString = footNoteId.getFootNoteId();
            iPartsDataFootNote dataStandardFootNote = fnIdToStandardFootNoteMap.get(footNoteIdString);
            if (dataStandardFootNote == null) {
                // Dummy anlegen, da die Standardfußnote nicht in der DB existiert
                iPartsDataFootNote dataFootNoteWithContent = new iPartsDataFootNote(project, footNoteId);
                dataFootNoteWithContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                dataFootNoteWithContent.setFieldValue(iPartsConst.FIELD_DFN_NAME, footNoteIdString, DBActionOrigin.FROM_DB);
                dataFootNoteWithContent.setFieldValueAsBoolean(iPartsConst.FIELD_DFN_STANDARD, true, DBActionOrigin.FROM_DB);

                // Die Attribute von DA_FN_CONTENT setzen. Standardfußnoten haben nur eine Zeile.
                String dummyLineNumber = EtkDbsHelper.formatLfdNr(1);
                iPartsFootNoteContentId dummyDataFootNoteContentId = new iPartsFootNoteContentId(footNoteIdString, dummyLineNumber);
                iPartsDataFootNoteContent dummyDataFootNoteContent = new iPartsDataFootNoteContent(project, dummyDataFootNoteContentId);
                dummyDataFootNoteContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);

                // nur DE und EN Text hinzufügen und das mehrsprachige Attribut als "fertig geladen" markieren
                DBDataObjectAttribute fnContentTextAttribute = new DBDataObjectAttribute(iPartsConst.FIELD_DFNC_TEXT, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE,
                                                                                         false);
                fnContentTextAttribute.setMultiLanguageCompleteLoaded(true);
                fnContentTextAttribute.setPreloadValueForMultiLanguage(Language.DE.getCode(), standardFootNoteWithDefaults.getDefaultGermanText());
                fnContentTextAttribute.setPreloadValueForMultiLanguage(Language.EN.getCode(), standardFootNoteWithDefaults.getDefaultEnglishText());
                dummyDataFootNoteContent.getAttributes().addField(fnContentTextAttribute, DBActionOrigin.FROM_DB);

                dataFootNoteWithContent.getAttributes().addFields(dummyDataFootNoteContent.getAttributes(), DBActionOrigin.FROM_DB);
                fnIdToStandardFootNoteMap.put(footNoteIdString, dataFootNoteWithContent);
            }
        }
    }
}