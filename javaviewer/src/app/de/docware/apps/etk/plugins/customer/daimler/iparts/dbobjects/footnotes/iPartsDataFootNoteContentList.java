/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashSet;
import java.util.Set;

/**
 * Liste von {@link iPartsDataFootNoteContent}s.
 */
public class iPartsDataFootNoteContentList extends EtkDataObjectList<iPartsDataFootNoteContent> implements iPartsConst {

    /**
     * Lädt eine Fußnote oder Tabellen-Fußnoten zu einer Fußnoten-Id (sortiert nach Zeilennummer)
     *
     * @param project
     * @param footNoteId
     * @return
     */
    public static iPartsDataFootNoteContentList loadFootNote(EtkProject project, String footNoteId) {
        iPartsDataFootNoteContentList list = new iPartsDataFootNoteContentList();
        list.loadFootNoteFromDB(project, footNoteId, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }


    /**
     * Lädt eine Fußnote oder Tabellen-Fußnoten zu einer Fußnoten-Id (sortiert nach Zeilennummer)
     *
     * @param project
     * @param footNoteId
     * @return
     */
    public static iPartsDataFootNoteContentList loadFootNote(EtkProject project, iPartsFootNoteId footNoteId) {
        return loadFootNote(project, footNoteId.getFootNoteId());
    }

    /**
     * Lädt eine Fußnote oder Tabellen-Fußnoten zu einer Fußnoten-Id (sortiert nach Zeilennummer). Zusätzlich wird
     * der Fußnotentext in jeder Sprache geladen.
     *
     * @param project
     * @param footNoteId
     * @return
     */
    public static iPartsDataFootNoteContentList loadFootNoteWithAllLanguages(EtkProject project, String footNoteId) {
        iPartsDataFootNoteContentList dataFootNoteContents = new iPartsDataFootNoteContentList();
        dataFootNoteContents.loadFootNoteForAllLanguagesFromDB(project, footNoteId, DBActionOrigin.FROM_DB);
        return dataFootNoteContents;
    }

    /**
     * Lädt alle Fußnotentexte in allen Sprachen zu allen übergebenenen Fußnoten-IDs.
     *
     * @param project
     * @param footNoteIds
     * @return
     */
    public static iPartsDataFootNoteContentList loadFootNoteContentsForIds(EtkProject project, Set<String> footNoteIds) {
        iPartsDataFootNoteContentList dataFootNoteContents = new iPartsDataFootNoteContentList();
        dataFootNoteContents.loadAllFootNotesForIds(project, footNoteIds, DBActionOrigin.FROM_DB);
        return dataFootNoteContents;
    }

    private void loadAllFootNotesForIds(EtkProject project, Set<String> footNoteIds, DBActionOrigin origin) {
        clear(origin);
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_FN_CONTENT, FIELD_DFNC_FNID, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_FN_CONTENT, FIELD_DFNC_LINE_NO, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT, true, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT_NEUTRAL, false, false);
        displayFields.addFeld(displayField);

        String[] whereFields = new String[footNoteIds.size()];
        String[] whereValues = new String[footNoteIds.size()];
        String footNoteIdFieldName = TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_FNID);
        int index = 0;
        for (String footnoteId : footNoteIds) {
            whereFields[index] = footNoteIdFieldName;
            whereValues[index] = footnoteId;
            index++;
        }
        String[] sortFields = new String[]{ FIELD_DFNC_FNID, FIELD_DFNC_LINE_NO };

        searchSortAndFillWithMultiLangValueForAllLanguages(project, displayFields, FIELD_DFNC_TEXT, whereFields, whereValues,
                                                           true, sortFields, false);
    }

    private void loadFootNoteForAllLanguagesFromDB(EtkProject project, String footNoteId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_FNID) };
        String[] whereValues = new String[]{ footNoteId };
        String[] sortFields = new String[]{ FIELD_DFNC_LINE_NO };
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT_NEUTRAL, false, false);
        displayFields.addFeld(displayField);
        searchSortAndFillWithMultiLangValueForAllLanguages(project, new EtkDisplayFields(), TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT),
                                                           whereFields, whereValues, false, sortFields, false);
    }

    /**
     * Überprüft, ob es sich um eine Tabellen-Fußnote handelt
     *
     * @param project
     * @param footNoteId
     * @return
     */
    public static boolean isTableFootNote(EtkProject project, String footNoteId) {
        iPartsDataFootNoteContentList list = new iPartsDataFootNoteContentList();
        list.loadFootNoteFromDB(project, footNoteId, LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
        return list.size() > 1;
    }

    /**
     * Liefert alle Fußnoten mit der dictId
     *
     * @param project
     * @param dictId
     * @return
     */
    public static iPartsDataFootNoteContentList loadFootNoteByDictId(EtkProject project, String dictId) {
        iPartsDataFootNoteContentList list = new iPartsDataFootNoteContentList();
        list.loadFootNoteByDictIdFromDB(project, dictId, LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Liefert alle Fußnoten mit der dictId (komplett geladen)
     *
     * @param project
     * @param dictId
     * @return
     */
    public static iPartsDataFootNoteContentList loadFootNoteByDictIdComplete(EtkProject project, String dictId) {
        iPartsDataFootNoteContentList list = new iPartsDataFootNoteContentList();
        list.loadFootNoteByDictIdFromDB(project, dictId, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    public static boolean isDictIdUsedInTableFootNotes(EtkProject project, String dictId) {
        iPartsDataFootNoteContentList list = new iPartsDataFootNoteContentList();
        list.loadFootNoteByDictIdFromDB(project, dictId, LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
        if (!list.isEmpty()) {
            for (iPartsDataFootNoteContent dataFootNoteContent : list) {
                iPartsFootNoteContentId footNoteContentId = dataFootNoteContent.getAsId();
                if (footNoteContentId.getFootNoteId().startsWith("T")) {
                    return true;
                } else if (footNoteContentId.getFootNoteId().startsWith(iPartsDataFootNote.FOOTNOTE_PREFIX_ELDAS + iPartsDataFootNote.FOOTNOTE_ID_DELIMITER)) {
                    if (StrUtils.isInteger(footNoteContentId.getFootNoteLineNo())) {
                        int lfdNr = Integer.valueOf(footNoteContentId.getFootNoteLineNo());
                        if (lfdNr > 1) {
                            return true;
                        }
                    }
                    //ELDAS Fußnote => überprüfen, ob Table-Footnote
                    footNoteContentId = new iPartsFootNoteContentId(dataFootNoteContent.getAsId().getFootNoteId(), EtkDbsHelper.formatLfdNr(2));
                    iPartsDataFootNoteContent helperDataFootNoteContent = new iPartsDataFootNoteContent(project, footNoteContentId);
                    if (helperDataFootNoteContent.existsInDB()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Lädt alle DIALOG Tabellenfußnoten (sortiert nach Id und Line_No)
     *
     * @param project
     * @return
     */
    public static iPartsDataFootNoteContentList loadAllDIALOGTableFootNotes(EtkProject project) {
        iPartsDataFootNoteContentList list = new iPartsDataFootNoteContentList();
        list.loadAllDIALOGTableFootNotesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle ELDAS Tabellenfußnoten-Ids (sortiert nach Id)
     *
     * @param project
     * @return
     */
    public static iPartsDataFootNoteContentList loadAllELDASTableFootNoteIds(EtkProject project) {
        iPartsDataFootNoteContentList list = new iPartsDataFootNoteContentList();
        list.loadAllELDASTableFootNoteIdsFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Liefert alle TextIds der Tabellenfußnoten
     *
     * @param project
     * @return
     */
    public static Set<String> loadAllDIALOGTableFootNoteTextIds(EtkProject project) {
        Set<String> textIdSet = new HashSet<String>();
        iPartsDataFootNoteContentList tableFootNoteList = loadAllDIALOGTableFootNotes(project);
        for (iPartsDataFootNoteContent dataFootNoteContent : tableFootNoteList) {
            //nachladen überspringen
            String textId = dataFootNoteContent.getAttribute(FIELD_DFNC_TEXT, false).getAsString();
            if (!StrUtils.isEmpty(textId)) {
                textIdSet.add(textId);
            }
        }
        return textIdSet;
    }

    /**
     * Lädt eine Fußnote oder Tabellen-Fußnoten zu einer Fußnoten-Id (sortiert nach Zeilennummer)
     *
     * @param project
     * @param footNoteId
     * @param origin
     */
    private void loadFootNoteFromDB(EtkProject project, String footNoteId, LoadType loadType, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DFNC_FNID };
        String[] whereValues = new String[]{ footNoteId };
        String[] sortFields = new String[]{ FIELD_DFNC_LINE_NO };
        searchSortAndFill(project, TABLE_DA_FN_CONTENT, whereFields, whereValues, sortFields,
                          loadType, origin);

/*
        searchSortAndFillWithJoin(EtkProject project, String dbLanguage, EtkDisplayFields selectFields, String[] fieldsForJoin,
                String joinTable, String[] joinTableFieldsForJoin, boolean isLeftOuterJoin,
        boolean isOrForJoinFields, String[] whereTableAndFields, String[] whereValues,
        boolean isOrForWhereFields, String[] sortFields, boolean searchCaseInsensitive);
*/
/*
    String FIELD_DFNC_FNID               = "DFNC_FNID";
    String FIELD_DFNC_LINIENO            = "DFNC_LINIENO";
    String FIELD_DFNC_TEXTID             = "DFNC_TEXTID";
    String FIELD_DFNC_TEXT_NEUTRAL       = "DFNC_TEXT_NEUTRAL";

 */
    }

    private void loadFootNoteByDictIdFromDB(EtkProject project, String dictId, LoadType loadType, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DFNC_TEXT };
        String[] whereValues = new String[]{ dictId };
        String[] sortFields = new String[]{ FIELD_DFNC_FNID };
        searchSortAndFill(project, TABLE_DA_FN_CONTENT, whereFields, whereValues, sortFields,
                          loadType, origin);
    }

    /**
     * Lädt alle DIALOG-Tabellenfußnoten (sortiert nach Id und Line_No)
     *
     * @param project
     * @param origin
     */
    private void loadAllDIALOGTableFootNotesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DFNC_FNID };
        String[] whereValues = new String[]{ "T*" };
        String[] sortFields = new String[]{ FIELD_DFNC_FNID, FIELD_DFNC_LINE_NO };

        searchWithWildCardsSortAndFill(project, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle ELDAS-Tabellenfußnoten-Ids (sortiert nach Id)
     *
     * @param project
     * @param origin
     */
    private void loadAllELDASTableFootNoteIdsFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DFNC_FNID, FIELD_DFNC_LINE_NO };
        String[] whereValues = new String[]{ iPartsDataFootNote.FOOTNOTE_PREFIX_ELDAS + iPartsDataFootNote.FOOTNOTE_ID_DELIMITER + "*",
                                             EtkDbsHelper.formatLfdNr(2) };
        String[] sortFields = new String[]{ FIELD_DFNC_FNID };

        searchWithWildCardsSortAndFill(project, whereFields, whereValues, sortFields, LoadType.ONLY_IDS, origin);
    }

    @Override
    protected iPartsDataFootNoteContent getNewDataObject(EtkProject project) {
        return new iPartsDataFootNoteContent(project, null);
    }
}
