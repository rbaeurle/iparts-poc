/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.events.CacheForGetCacheDataEvent;
import de.docware.apps.etk.base.project.events.SetCacheDataEvent;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFootNote;
import de.docware.apps.etk.util.AbstractCacheWithChangeSets;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.ArrayUtil;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Cache Klasse für Fußnoten zu Teilenummern
 */
public class iPartsPartFootnotesCache implements CacheForGetCacheDataEvent<iPartsPartFootnotesCache>, iPartsConst {

    private static AbstractCacheWithChangeSets<ObjectInstanceStrongLRUList<Object, iPartsPartFootnotesCache>> cacheWithChangeSets =
            new AbstractCacheWithChangeSets<ObjectInstanceStrongLRUList<Object, iPartsPartFootnotesCache>>(MAX_CACHE_SIZE_CHANGE_SETS, iPartsPlugin.getCachesLifeTime()) {
                @Override
                protected ObjectInstanceStrongLRUList<Object, iPartsPartFootnotesCache> createNewCache() {
                    return new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_PARTS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);
                }
            };

    private static final EtkDisplayFields SELECT_FIELDS_FOR_FOOT_NOTES = new EtkDisplayFields();

    @JsonProperty
    private Map<String, Map<String, FootnoteWithTextForCache>> partToFootNotes; // Map von Teilenummer auf Map von Fußnotennummer auf Fußnote
    protected iPartsPartFootnotesCache sourceCache;

    static {
        // DA_FN
        SELECT_FIELDS_FOR_FOOT_NOTES.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_ID, false, false));
        SELECT_FIELDS_FOR_FOOT_NOTES.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_NAME, false, false));
        SELECT_FIELDS_FOR_FOOT_NOTES.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_STANDARD, false, false));
        SELECT_FIELDS_FOR_FOOT_NOTES.addFeld(new EtkDisplayField(TABLE_DA_FN, FIELD_DFN_TYPE, false, false));

        // DA_FN_CONTENT
        SELECT_FIELDS_FOR_FOOT_NOTES.addFeld(new EtkDisplayField(TABLE_DA_FN_CONTENT, FIELD_DFNC_LINE_NO, false, false));
        SELECT_FIELDS_FOR_FOOT_NOTES.addFeld(new EtkDisplayField(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT, true, false));
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        cacheWithChangeSets.clearCache();
    }

    /**
     * Entfernt den Cache-Eintrag für die gerade aktiven {@link AbstractRevisionChangeSet}s.
     *
     * @param project
     */
    public static synchronized void removeCacheForActiveChangeSets(EtkProject project) {
        cacheWithChangeSets.removeCacheForActiveChangeSets(project);
    }

    private static String getInstanceHashObject(EtkProject project) {
        // Hier nun keine ChangeSets mehr verwenden für das hashObject, weil dies ja bereits über cacheWithChangeSets
        // gelöst wurde
        return CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsPartFootnotesCache.class, "PartFootnotesCache", false);
    }

    public static synchronized iPartsPartFootnotesCache getInstance(final EtkProject project) {
        ObjectInstanceStrongLRUList<Object, iPartsPartFootnotesCache> cache = cacheWithChangeSets.getCacheInstance(project);
        Object hashObject = getInstanceHashObject(project);
        iPartsPartFootnotesCache result = cache.get(hashObject);

        if (result == null) {
            if (cache == cacheWithChangeSets.getNormalCache()) {
                result = iPartsPlugin.createCacheInstanceWithCachesProvider(new iPartsPartFootnotesCache(), null, cache, hashObject);
                if (result != null) {
                    return result;
                }
            }

            result = new iPartsPartFootnotesCache();

            if (project.getEtkDbs().isRevisionChangeSetActive()) {
                // Den normalen Cache zunächst laden falls notwendig
                iPartsPartFootnotesCache normalCacheData = cacheWithChangeSets.getNormalCache().get(hashObject);
                if (normalCacheData == null) {
                    normalCacheData = new iPartsPartFootnotesCache();
                    final iPartsPartFootnotesCache normalCacheDataFinal = normalCacheData;

                    // Der normale Cache muss ohne aktive ChangeSets geladen werden
                    project.getRevisionsHelper().executeWithoutActiveChangeSets(new Runnable() {
                        @Override
                        public void run() {
                            normalCacheDataFinal.load(project);
                        }
                    }, false, project);

                    cacheWithChangeSets.getNormalCache().put(hashObject, normalCacheData);
                }

                // Zunächst nur den normalen Cache referenzieren
                referenceCache(normalCacheData, result);

                // Danach alle Änderungen an den Materialstamm-Fußnoten aus den aktiven ChangeSets simulieren
                String[] pkFields = null;
                for (AbstractRevisionChangeSet changeSet : project.getEtkDbs().getActiveRevisionChangeSets()) {
                    Collection<SerializedDBDataObject> serializedFootNoteMatRefList = changeSet.getSerializedObjectsByTable(TABLE_DA_FN_MAT_REF);
                    if (serializedFootNoteMatRefList != null) {
                        if (pkFields == null) { // Primärschlüsselfelder nur einmal bestimmen
                            EtkDatabaseTable dbTable = project.getConfig().getDBDescription().findTable(TABLE_DA_FN_MAT_REF);
                            if (dbTable != null) {
                                pkFields = ArrayUtil.toStringArray(dbTable.getPrimaryKeyFields());
                            }
                        }
                        if (pkFields != null) {
                            for (SerializedDBDataObject serializedFootNoteMatRef : serializedFootNoteMatRefList) {
                                iPartsFootNoteMatRefId footNoteMatRefId = new iPartsFootNoteMatRefId(serializedFootNoteMatRef.getPkValuesForPkFields(pkFields));
                                iPartsDataFootNoteMatRef footNoteMatRef = new iPartsDataFootNoteMatRef(project, footNoteMatRefId);

                                switch (serializedFootNoteMatRef.getState()) {
                                    case NEW: // Fallthrough beabsichtigt
                                    case REPLACED: // Fallthrough beabsichtigt
                                    case MODIFIED:
                                        footNoteMatRef.__internal_setNew(serializedFootNoteMatRef.getState() == SerializedDBDataObjectState.NEW);
                                        if (footNoteMatRef.existsInDB()) {
                                            result.addCacheForFootNoteMatRef(footNoteMatRef, project);
                                        }
                                        break;
                                    case DELETED:
                                        result.deleteCacheForFootNoteMatRef(footNoteMatRef);
                                        break;
                                }
                            }
                        }
                    }
                }
            } else {
                // Noch nicht geladen -> lade aus der Datenbank
                result.load(project);
            }
            cache.put(hashObject, result);
        }

        return result;
    }

    /**
     * Verwendet den Cache {@code sourceData} auch als Cache {@code destData} (also dieselben Cache-Daten).
     *
     * @param sourceData
     * @param destData
     * @see #reuseCache(iPartsPartFootnotesCache, iPartsPartFootnotesCache)
     */
    private static synchronized void referenceCache(iPartsPartFootnotesCache sourceData, iPartsPartFootnotesCache destData) {
        // Cache-Daten von der Quelle verwenden
        destData.partToFootNotes = sourceData.partToFootNotes;
        destData.sourceCache = sourceData;
    }

    /**
     * Verwendet den Inhalt vom Cache {@code sourceData} im Cache {@code destData} in einer eigenen Datenstruktur (ohne
     * die einzelnen Materialstamm-Fußnoten zu klonen).
     *
     * @param sourceData
     * @param destData
     * @see #referenceCache(iPartsPartFootnotesCache, iPartsPartFootnotesCache)
     */
    private static synchronized void reuseCache(iPartsPartFootnotesCache sourceData, iPartsPartFootnotesCache destData) {
        // Cache-Daten von der Quelle in einer eigenen Map ablegen
        destData.partToFootNotes = new HashMap<>();
        for (Map.Entry<String, Map<String, FootnoteWithTextForCache>> partToFootnoteEntry : sourceData.partToFootNotes.entrySet()) {
            Map<String, FootnoteWithTextForCache> sourceFootNotesMap = partToFootnoteEntry.getValue();
            Map<String, FootnoteWithTextForCache> destFootNotesMap = new HashMap<>(sourceFootNotesMap);
            destData.partToFootNotes.put(partToFootnoteEntry.getKey(), destFootNotesMap);
        }
    }

    /**
     * Verwendet den Cache der aktuell aktiven {@link AbstractRevisionChangeSet}s auch als Cache, der mit {@code destinationCacheKey}
     * referenziert wird.
     *
     * @param project
     * @param destinationCacheKey
     */
    public static synchronized void referenceActiveChangeSetCache(EtkProject project, String destinationCacheKey) {
        // Ziel-Cache bestimmen
        ObjectInstanceStrongLRUList<Object, iPartsPartFootnotesCache> destCache = cacheWithChangeSets.getCacheForChangeSetsInstance(destinationCacheKey);
        Object hashObject = getInstanceHashObject(project);
        iPartsPartFootnotesCache destData = destCache.get(hashObject);
        if (destData == null) {
            destData = new iPartsPartFootnotesCache();
            destCache.put(hashObject, destData);
        }

        referenceCache(getInstance(project), destData);
    }

    /**
     * Siehe {@link AbstractCacheWithChangeSets#moveActiveChangeSetCache(EtkProject, String)}.
     *
     * @param project
     * @param destinationCacheKey
     */
    public static synchronized void moveActiveChangeSetCache(EtkProject project, String destinationCacheKey) {
        cacheWithChangeSets.moveActiveChangeSetCache(project, destinationCacheKey);
    }


    @Override
    public iPartsPartFootnotesCache createInstance(EtkProject project, SetCacheDataEvent setCacheDataEvent) {
        return createInstance(setCacheDataEvent, cacheWithChangeSets.getNormalCache(), getInstanceHashObject(project));
    }

    /**
     * Lädt alle Fußnoten zu Teilenummern
     *
     * @param project
     */
    private void load(EtkProject project) {
        partToFootNotes = new HashMap<>();
        // Join zum Laden aller Teil zu Fußnote Beziehungen samt Fußnotentexte in allen Sprachen
        iPartsDataFootNoteMatRefList matRefsWithFootNotes = new iPartsDataFootNoteMatRefList();
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFelder(SELECT_FIELDS_FOR_FOOT_NOTES);
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_FN_MAT_REF, FIELD_DFNM_MATNR, false, false));

        matRefsWithFootNotes.searchSortAndFillWithMultiLangValueForAllLanguagesAndJoin(project, selectFields, TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT),
                                                                                       null, null, false,
                                                                                       new String[]{ TableAndFieldName.make(TABLE_DA_FN_MAT_REF, FIELD_DFNM_MATNR),
                                                                                                     TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_FNID),
                                                                                                     TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_LINE_NO) },
                                                                                       false, false,
                                                                                       new EtkDataObjectList.JoinData(TABLE_DA_FN_CONTENT,
                                                                                                                      new String[]{ FIELD_DFNM_FNID },
                                                                                                                      new String[]{ FIELD_DFNC_FNID },
                                                                                                                      false, false),
                                                                                       new EtkDataObjectList.JoinData(TABLE_DA_FN,
                                                                                                                      new String[]{ FIELD_DFNM_FNID },
                                                                                                                      new String[]{ FIELD_DFN_ID },
                                                                                                                      false, false));
        addMatRefsWithFootNotes(matRefsWithFootNotes, project);
    }

    private void addMatRefsWithFootNotes(iPartsDataFootNoteMatRefList matRefsWithFootNotes, EtkProject project) {
        if (!matRefsWithFootNotes.isEmpty()) {
            Map<iPartsFootNoteId, FootnoteWithTextForCache> tempCache = new HashMap<>();
            for (iPartsDataFootNoteMatRef matRefWithFootNote : matRefsWithFootNotes) {
                iPartsFootNoteId footNoteId = matRefWithFootNote.getAsId().getAsFootNoteId();
                FootnoteWithTextForCache footnoteWithText = tempCache.get(footNoteId);

                // Check, ob die Fußnote schon einmal vorkam (versch. Teile können ja die gleich Fußnote haben)
                if (footnoteWithText == null) {
                    footnoteWithText = new FootnoteWithTextForCache(footNoteId);
                    tempCache.put(footNoteId, footnoteWithText);
                }

                String fnName = matRefWithFootNote.getFieldValue(FIELD_DFN_NAME);
                String lineNo = matRefWithFootNote.getFieldValue(FIELD_DFNC_LINE_NO);
                boolean isStandard = matRefWithFootNote.getFieldValueAsBoolean(FIELD_DFN_STANDARD);
                EtkMultiSprache text = matRefWithFootNote.getFieldValueAsMultiLanguage(FIELD_DFNC_TEXT);

                footnoteWithText.addText(project, fnName, iPartsFootnoteType.PART, lineNo, isStandard, text);

                // Beziehung zwischen Fußnote und Teil aufbauen
                String matNr = matRefWithFootNote.getAsId().getAsPartId().getMatNr();
                Map<String, FootnoteWithTextForCache> footnotesForPart = partToFootNotes.get(matNr);
                if (footnotesForPart == null) {
                    footnotesForPart = new TreeMap<>();
                    partToFootNotes.put(matNr, footnotesForPart);
                }
                if (!footnotesForPart.containsKey(footNoteId.getFootNoteId())) {
                    footnotesForPart.put(footNoteId.getFootNoteId(), footnoteWithText);
                }
            }
        }
    }

    /**
     * Verwendet den Inhalt vom gemerkten Quell-Cache in diesem Cache in einer eigenen Datenstruktur (ohne die einzelnen
     * Materialstamm-Fußnoten zu klonen).
     *
     * @see #reuseCache(iPartsPartFootnotesCache, iPartsPartFootnotesCache)
     */
    private synchronized void reuseSourceCache() {
        if (sourceCache != null) {
            reuseCache(sourceCache, this);
            sourceCache = null; // Verwendung vom sourceCache darf nur einmal stattfinden
        }
    }

    public List<iPartsFootNote> getFootnotesForPart(EtkProject project, PartId partId, Language language) {
        return getFootnotesForPart(project, partId, language.getCode());
    }

    public List<iPartsFootNote> getFootnotesForPart(EtkProject project, PartId partId, String language) {
        Map<String, FootnoteWithTextForCache> footnotes = partToFootNotes.get(partId.getMatNr());
        if ((footnotes != null) && !footnotes.isEmpty()) {
            List<iPartsFootNote> footnotesForPart = new DwList<>();
            for (FootnoteWithTextForCache footnote : footnotes.values()) {
                iPartsFootNote footnoteForLanguage = footnote.getFootnoteForLanguage(project, language);
                if (footnoteForLanguage != null) {
                    footnotesForPart.add(footnoteForLanguage);
                }
            }
            if (!footnotesForPart.isEmpty()) {
                return footnotesForPart;
            }
        }
        return null;
    }

    public List<iPartsFootNote> getFootnotesForPart(EtkProject project, PartId partId) {
        return getFootnotesForPart(project, partId, project.getDBLanguage());
    }

    /**
     * Fügt einen Cache-Eintrag für das Material und die Fußnoten-ID aus {@code footNoteMatRef} hinzu falls dieser nicht
     * schon existiert.
     *
     * @param footNoteMatRef Neue oder zu aktualisierende Materialstamm-Fußnote
     */
    public synchronized void addCacheForFootNoteMatRef(iPartsDataFootNoteMatRef footNoteMatRef, EtkProject project) {
        reuseSourceCache(); // Bei Veränderungen muss eine eigene Datenstruktur verwendet werden

        iPartsFootNoteId footNoteId = footNoteMatRef.getAsId().getAsFootNoteId();
        boolean footNoteIdExists = false;
        Map<String, FootnoteWithTextForCache> cacheData = partToFootNotes.get(footNoteMatRef.getAsId().getAsPartId().getMatNr());
        if (cacheData != null) {
            footNoteIdExists = cacheData.containsKey(footNoteId.getFootNoteId());
        }

        if (!footNoteIdExists) {


            // Die Daten aus DA_FN und DA_FN_CONTENT für die Fußnoten-ID über einen simplen Join laden (analog zu loadAllFootNotesForPartList(),
            // wobei searchSortAndFillWithMultiLangValueForAllLanguagesAndJoin() nicht verwendet werden kann, weil dies
            // zwei Joins wären -> searchSortAndFillWithJoin ohne dbLanguage verwenden und danach das mehrsprachige Feld
            // DFNC_TEXT explizit nachladen)
            iPartsDataFootNoteContentList singleFootNoteContentList = new iPartsDataFootNoteContentList();

            String fnId = footNoteId.getFootNoteId();
            singleFootNoteContentList.searchSortAndFillWithJoin(project, null, SELECT_FIELDS_FOR_FOOT_NOTES,
                                                                new String[]{ TableAndFieldName.make(TABLE_DA_FN_CONTENT,
                                                                                                     FIELD_DFNC_FNID),
                                                                              // getRecords()-Simulation von Joins unterstützen durch diese redundanten where-Felder
                                                                              TableAndFieldName.make(TABLE_DA_FN,
                                                                                                     FIELD_DFN_ID) },
                                                                new String[]{ fnId, fnId }, false,
                                                                new String[]{ TableAndFieldName.make(TABLE_DA_FN_CONTENT,
                                                                                                     FIELD_DFNC_LINE_NO) },
                                                                false, null,
                                                                new EtkDataObjectList.JoinData(TABLE_DA_FN,
                                                                                               new String[]{ FIELD_DFNC_FNID },
                                                                                               new String[]{ FIELD_DFN_ID },
                                                                                               false, false));
            if (!singleFootNoteContentList.isEmpty()) {
                // Alle Daten in der matRefsWithFootNotes zusammenführen
                iPartsDataFootNoteMatRefList matRefsWithFootNotes = new iPartsDataFootNoteMatRefList();
                for (iPartsDataFootNoteContent dataFootNoteContent : singleFootNoteContentList) {
                    iPartsDataFootNoteMatRef mergedFootNoteMatRef = footNoteMatRef.cloneMe(project);

                    // Feld DFNC_TEXT als mehrsprachiges Feld mit der geladenen Text-Nummer befüllen, damit es beim Aufruf
                    // von getFieldValueAsMultiLanguage() dann komplett für alle Sprachen aus der DB geladen wird
                    dataFootNoteContent.setTextNrForMultiLanguage(FIELD_DFNC_TEXT, dataFootNoteContent.getFieldValue(FIELD_DFNC_TEXT),
                                                                  DBActionOrigin.FROM_EDIT);
                    dataFootNoteContent.getFieldValueAsMultiLanguage(FIELD_DFNC_TEXT); // Laden vom Text in allen Sprachen erzwingen

                    mergedFootNoteMatRef.getAttributes().addFields(dataFootNoteContent.getAttributes(), DBActionOrigin.FROM_DB);

                    // Zusammengeführte Daten in matRefsWithFootNotes ablegen für die weitere Verarbeitung in addMatRefsWithFootNotes()
                    matRefsWithFootNotes.add(mergedFootNoteMatRef, DBActionOrigin.FROM_DB);
                }
                addMatRefsWithFootNotes(matRefsWithFootNotes, project);
            }
        }
    }

    /**
     * Den übergebenen Eintrag im Cache suchen und falls gefunden löschen.
     *
     * @param footNoteMatRef
     */
    public synchronized void deleteCacheForFootNoteMatRef(iPartsDataFootNoteMatRef footNoteMatRef) {
        reuseSourceCache(); // Bei Veränderungen muss eine eigene Datenstruktur verwendet werden

        Map<String, FootnoteWithTextForCache> cacheData = partToFootNotes.get(footNoteMatRef.getAsId().getAsPartId().getMatNr());
        if (cacheData != null) {
            cacheData.remove(footNoteMatRef.getAsId().getAsFootNoteId().getFootNoteId());
        }
    }
}