/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractSearchQuery;
import de.docware.apps.etk.base.db.EtkDbsSearchQuery;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlCommonDbSelect;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.ArrayList;
import java.util.List;

/**
 * Liste von {@link iPartsDataDictMeta}s.
 */
public class iPartsDataDictMetaList extends EtkDataObjectList<iPartsDataDictMeta> implements iPartsConst {

    public iPartsDataDictMetaList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine begrenzte Liste <i>maxResults</i>der {@link iPartsDataDictMeta}s, die zu <i>textKindId</i> gehören.
     *
     * @param project
     * @param textKindId
     * @param maxResults
     * @return
     */
    public static iPartsDataDictMetaList loadMetaFromTextKindListLimited(EtkProject project, iPartsDictTextKindId textKindId, int maxResults) {
        iPartsDataDictMetaList list = new iPartsDataDictMetaList();
        list.loadMetaFromTextKindFromDBLimited(project, textKindId, maxResults, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictMeta}s, die zu <i>textKindId</i> gehören.
     *
     * @param project
     * @param textKindId
     * @return
     */
    public static iPartsDataDictMetaList loadMetaFromTextKindList(EtkProject project, iPartsDictTextKindId textKindId) {
        iPartsDataDictMetaList list = new iPartsDataDictMetaList();
        list.loadMetaFromTextKindFromDB(project, textKindId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictMeta}s, die zu <i>dictTextId</i> gehören.
     *
     * @param project
     * @param dictTextId
     * @return
     */
    public static iPartsDataDictMetaList loadMetaFromTextIdList(EtkProject project, String dictTextId) {
        iPartsDataDictMetaList list = new iPartsDataDictMetaList();
        list.loadMetaFromTextIdFromDB(project, dictTextId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Überprüft, ob es noch verwendete Einträge aus DA_DICT_SPRACHE (dictMetaId.getTextId())
     * in DA_DICT_META gibt, außer dem übergebenen DictMeta (dictMetaId.getTextKindId())
     *
     * @param project
     * @param dictMetaId
     * @return
     */
    public static boolean isMetaSpracheUsedInDictMeta(EtkProject project, iPartsDictMetaId dictMetaId) {
        iPartsDataDictMetaList usedList = loadMetaFromTextIdExcludeList(project, dictMetaId);
        return !usedList.isEmpty();
    }

    /**
     * Liefert die Liste der DictMetas, die die Text-Id (dictMetaId.getTextId()) verwenden,
     * außer dem übergebenen DoictMeta (dictMetaId.getTextKindId())
     *
     * @param project
     * @param dictMetaId
     * @return
     */
    public static iPartsDataDictMetaList loadMetaFromTextIdExcludeList(EtkProject project, iPartsDictMetaId dictMetaId) {
        iPartsDataDictMetaList list = new iPartsDataDictMetaList();
        list.loadMetaFromTextIdExcludeFromDB(project, dictMetaId.getTextId(), dictMetaId.getTextKindId(), DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataDictMetaList loadMetaFromTextIdWithChangeSetList(EtkProject project, String dictTextKindId,
                                                                             String dicTextId, boolean withoutChangeSets) {
        iPartsDataDictMetaList list = new iPartsDataDictMetaList();
        list.setSearchWithoutActiveChangeSets(withoutChangeSets);
        list.loadMetaFromTextKindAndTextIdFromDB(project, dictTextKindId, dicTextId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictMeta}s, die zur externen <i>foreignTextId</i> gehören.
     *
     * @param project
     * @param foreignTextId
     * @return
     */
    public static iPartsDataDictMetaList loadMetaFromForeignTextId(EtkProject project, String foreignTextId) {
        iPartsDataDictMetaList list = new iPartsDataDictMetaList();
        list.loadMetaFromForeignTextIdAndTextKindFromDB(project, foreignTextId, null, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictMeta}s, die zur externen <i>foreignTextId</i> und der
     * übergebenen Textart gehören.
     *
     * @param project
     * @param foreignTextId
     * @return
     */
    public static iPartsDataDictMetaList loadMetaFromForeignTextIdAndTextKind(EtkProject project, String foreignTextId,
                                                                              iPartsDictTextKindId textKindId) {
        iPartsDataDictMetaList list = new iPartsDataDictMetaList();
        list.loadMetaFromForeignTextIdAndTextKindFromDB(project, foreignTextId, textKindId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictMeta}s, die zu <i>dialogId</i> gehören.
     *
     * @param project
     * @param textKindId
     * @param dialogId
     * @return
     */
    public static iPartsDataDictMetaList loadMetaFromDialogIdList(EtkProject project, iPartsDictTextKindId textKindId, String dialogId) {
        iPartsDataDictMetaList list = new iPartsDataDictMetaList();
        list.loadMetaFromDialogIdFromDB(project, textKindId, dialogId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictMeta}s, die zu <i>eldasId</i> gehören.
     *
     * @param project
     * @param textKindId
     * @param eldasId
     * @return
     */
    public static iPartsDataDictMetaList loadMetaFromEldasIdList(EtkProject project, iPartsDictTextKindId textKindId, String eldasId) {
        iPartsDataDictMetaList list = new iPartsDataDictMetaList();
        list.loadMetaFromEldasIdFromDB(project, textKindId, eldasId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictMeta}s, die zu <i>dictTextKindId</i> gehören.
     * Hier werden nur die IDs geladen.
     *
     * @param project
     * @param dictTextKindId
     * @return
     */
    public static iPartsDataDictMetaList loadMetaIdsFromTextKindList(EtkProject project, String dictTextKindId) {
        iPartsDataDictMetaList list = new iPartsDataDictMetaList();
        list.loadMetaIdsFromTextKindFromDB(project, dictTextKindId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictMeta}s, die zu <i>dictTextId</i> gehören.
     *
     * @param project
     * @param foreignTextId
     * @param origin
     */
    private void loadMetaFromForeignTextIdAndTextKindFromDB(EtkProject project, String foreignTextId,
                                                            iPartsDictTextKindId textKindId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_DICT_META_FOREIGNID };
        String[] whereValues = new String[]{ foreignTextId };
        if (textKindId != null) {
            whereFields = StrUtils.mergeArrays(whereFields, FIELD_DA_DICT_META_TXTKIND_ID);
            whereValues = StrUtils.mergeArrays(whereValues, textKindId.getTextKindId());
        }

        loadMetaFromDB(project, whereFields, whereValues, origin);
    }

    /**
     * Erzeugt und lädt eine begrenzte Liste <i>maxResults</i>der {@link iPartsDataDictMeta}s, die zu <i>textKindId</i> gehören.
     *
     * @param project
     * @param textKindId
     * @param maxResults
     * @param origin
     */
    private void loadMetaFromTextKindFromDBLimited(EtkProject project, iPartsDictTextKindId textKindId, int maxResults, DBActionOrigin origin) {
        clear(origin);

        String tableName = TABLE_DA_DICT_META;
        String searchFieldName = FIELD_DA_DICT_META_TXTKIND_ID;

        EtkDatabaseTable tableDef = project.getConfig().getDBDescription().findTable(tableName);
        if (tableDef != null) {
            EtkDisplayFields searchFields = new EtkDisplayFields();
            List<String> searchValues = new ArrayList<String>();

            EtkDisplayField searchField = new EtkDisplayField(tableName, searchFieldName, tableDef.getField(searchFieldName).isMultiLanguage(), tableDef.getField(searchFieldName).isArray());
            searchField.setSprache(project.getDBLanguage());
            searchFields.addFeld(searchField);
            searchValues.add(textKindId.getTextKindId());

            EtkDisplayFields displayFields = new EtkDisplayFields();
            for (EtkDatabaseField dbField : tableDef.getFieldList()) {
                if (!dbField.getName().equals(DBConst.FIELD_STAMP)) {
                    EtkDisplayField field = new EtkDisplayField(TableAndFieldName.make(tableName, dbField.getName()), dbField.isMultiLanguage(), dbField.isArray());
                    displayFields.addFeld(field);
                }
            }
            displayFields.loadStandards(project.getConfig());

            EtkSqlCommonDbSelect sqlSelect = new EtkSqlCommonDbSelect(project, tableName);
            sqlSelect.buildSelect(tableName, searchFields, searchValues, displayFields, null); // keine Wildcards
            if (maxResults > 0) {
                sqlSelect.setHitLimit(maxResults + 1); // + 1, damit angezeigt wird, dass es mehr Treffer gäbe
            }
            AbstractSearchQuery sQuery = null;
            try {
                sQuery = new EtkDbsSearchQuery(sqlSelect.createAbfrage());
                List<String> fieldNames = new DwList<String>();
                for (EtkDisplayField field : displayFields.getFields()) {
                    fieldNames.add(field.getKey().getFieldName());
                }
                while (sQuery.next()) {
                    DBDataObjectAttributes attributes = sQuery.loadAttributes(project, fieldNames);
                    iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(project, null);
                    dataDictMeta.assignAttributes(project, attributes, true, DBActionOrigin.FROM_DB);
                    add(dataDictMeta, origin);
                }
            } finally {
                if (sQuery != null) {
                    sQuery.closeQuery();
                }
            }
        }
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictMeta}s, die zu <i>textKindId</i> gehören.
     *
     * @param project
     * @param textKindId
     * @param origin
     */
    private void loadMetaFromTextKindFromDB(EtkProject project, iPartsDictTextKindId textKindId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_DICT_META_TXTKIND_ID };
        String[] whereValues = new String[]{ textKindId.getTextKindId() };

        loadMetaFromDB(project, whereFields, whereValues, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictMeta}s, die zu <i>dictTextId</i> gehören.
     *
     * @param project
     * @param dictTextId
     * @param origin
     */
    private void loadMetaFromTextIdFromDB(EtkProject project, String dictTextId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_DICT_META_TEXTID };
        String[] whereValues = new String[]{ dictTextId };

        loadMetaFromDB(project, whereFields, whereValues, origin);
    }

    private void loadMetaFromTextIdExcludeFromDB(EtkProject project, String dictTextId, String excludeTextKindId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_DICT_META_TEXTID };
        String[] whereValues = new String[]{ dictTextId };
        String[] whereNotFields = new String[]{ FIELD_DA_DICT_META_TXTKIND_ID };
        String[] whereNotValues = new String[]{ excludeTextKindId };

        searchSortAndFill(project, TABLE_DA_DICT_META, whereFields, whereValues,
                          whereNotFields, whereNotValues, null, LoadType.ONLY_IDS, origin);
    }

    private void loadMetaFromTextKindAndTextIdFromDB(EtkProject project, String dictTextKindId, String dictTextId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_DICT_META_TXTKIND_ID, FIELD_DA_DICT_META_TEXTID };
        String[] whereValues = new String[]{ dictTextKindId, dictTextId };

        loadMetaFromDB(project, whereFields, whereValues, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictMeta}s, die zu <i>dialogId</i> gehören.
     *
     * @param project
     * @param dialogId
     * @param textKindId
     * @param origin
     */
    private void loadMetaFromDialogIdFromDB(EtkProject project, iPartsDictTextKindId textKindId, String dialogId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_DICT_META_TXTKIND_ID, FIELD_DA_DICT_META_DIALOGID };
        String[] whereValues = new String[]{ textKindId.getTextKindId(), "*" + dialogId + "*" }; // in DA_DICT_META_DIALOGID können mehrere DIALOG-IDs enthalten sein

        searchWithWildCardsSortAndFill(project, whereFields, whereValues, null, LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictMeta}s, die zu <i>eldasId</i> gehören.
     *
     * @param project
     * @param textKindId
     * @param eldasId
     * @param origin
     */
    private void loadMetaFromEldasIdFromDB(EtkProject project, iPartsDictTextKindId textKindId, String eldasId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_DICT_META_TXTKIND_ID, FIELD_DA_DICT_META_ELDASID };
        String[] whereValues = new String[]{ textKindId.getTextKindId(), "*" + eldasId + "*" }; // in DA_DICT_META_ELDASID können mehrere ELDAS-IDs enthalten sein

        searchWithWildCardsSortAndFill(project, whereFields, whereValues, null, LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictMeta}s, die zu <i>dictTextKindId</i> gehören.
     * Hier werden nur die IDs geladen.
     *
     * @param project
     * @param dictTextKindId
     * @param origin
     */
    private void loadMetaIdsFromTextKindFromDB(EtkProject project, String dictTextKindId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_DICT_META_TXTKIND_ID };
        String[] whereValues = new String[]{ dictTextKindId };

        searchAndFill(project, TABLE_DA_DICT_META, whereFields, whereValues, LoadType.ONLY_IDS, origin);
    }

    /**
     * Übernimmt die Suche und das Laden
     *
     * @param project
     * @param whereFields
     * @param whereValues
     * @param origin
     */
    private void loadMetaFromDB(EtkProject project, String[] whereFields, String[] whereValues, DBActionOrigin origin) {
        searchAndFill(project, TABLE_DA_DICT_META, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataDictMeta getNewDataObject(EtkProject project) {
        return new iPartsDataDictMeta(project, null);
    }

    /**
     * Ermöglicht das Befüllen dieser Liste über komplett geladene Attribute, die von außen gesetzt werden
     *
     * @param project
     * @param attributes
     * @return
     */
    public iPartsDataDictMeta addAndFillCompleteDictMetaFromAttributes(EtkProject project, DBDataObjectAttributes attributes) {
        return fillAndAddDataObjectFromAttributes(project, attributes, LoadType.COMPLETE, true, DBActionOrigin.FROM_DB);
    }
}