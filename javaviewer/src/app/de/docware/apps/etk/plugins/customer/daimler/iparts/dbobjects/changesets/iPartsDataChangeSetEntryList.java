/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

/**
 * Liste von {@link iPartsDataChangeSetEntry}s.
 */
public class iPartsDataChangeSetEntryList extends EtkDataObjectList<iPartsDataChangeSetEntry> implements iPartsConst {

    public iPartsDataChangeSetEntryList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt alle Änderungsseteinträge (Änderungsstand von einem {@link de.docware.framework.modules.db.DBDataObject}) mit
     * der übergebenen Änderungsset-ID und für die übergebene {@link de.docware.framework.modules.db.DBDataObject}-ID,
     * wobei beide jeweils auch {@code null} sein können, wenn diese egal sind.
     *
     * @param project
     * @param changeSetId
     * @param dataObjectIdWithType
     * @return
     */
    public static iPartsDataChangeSetEntryList loadChangeSetEntriesForChangeSetAndDataObjectIdWithType(EtkProject project,
                                                                                                       iPartsChangeSetId changeSetId,
                                                                                                       IdWithType dataObjectIdWithType) {
        iPartsDataChangeSetEntryList list = new iPartsDataChangeSetEntryList();
        list.loadForChangeSetAndDataObjectIdWithType(project, changeSetId, dataObjectIdWithType, false);
        return list;
    }

    public static iPartsDataChangeSetEntryList loadChangeSetEntriesWithLikeForChangeSetAndDataObjectIdWithType(EtkProject project,
                                                                                                               iPartsChangeSetId changeSetId,
                                                                                                               IdWithType dataObjectIdWithType) {
        iPartsDataChangeSetEntryList list = new iPartsDataChangeSetEntryList();
        list.loadForChangeSetAndDataObjectIdWithType(project, changeSetId, dataObjectIdWithType, true);
        return list;
    }

    /**
     * Lädt alle Änderungsseteinträge (Änderungsstand von einem {@link de.docware.framework.modules.db.DBDataObject}) für
     * die übergebene {@code sourceGUID} mit der optional übergebenen Änderungsset-ID und für den optional übergebenen
     * {@code dataObjectType}, wobei beide jeweils auch {@code null} sein können, wenn diese egal sind.
     *
     * @param project
     * @param sourceGUID
     * @param changeSetId
     * @param dataObjectType
     * @return
     */
    public static iPartsDataChangeSetEntryList loadChangeSetEntriesForChangeSetAndTypeBySourceGUID(EtkProject project,
                                                                                                   String sourceGUID, iPartsChangeSetId changeSetId,
                                                                                                   String dataObjectType) {
        iPartsDataChangeSetEntryList list = new iPartsDataChangeSetEntryList();
        list.loadForChangeSetAndTypeBySourceGUID(project, sourceGUID, changeSetId, dataObjectType);
        return list;
    }

    public static iPartsDataChangeSetEntryList loadChangeSetEntriesForChangeSetAndIdType(EtkProject project, iPartsChangeSetId changeSetId,
                                                                                         iPartsChangeSetStatus changeSetStatus,
                                                                                         String idType) {
        iPartsDataChangeSetEntryList list = new iPartsDataChangeSetEntryList();
        list.loadForChangeSetAndIdType(project, changeSetId, changeSetStatus, idType);
        return list;
    }

    public void loadForChangeSetAndIdType(EtkProject project, iPartsChangeSetId changeSetId, iPartsChangeSetStatus changeSetStatus,
                                          String idType) {
        clear(DBActionOrigin.FROM_DB);
        String[] whereFields;
        String[] whereValues;
        if (changeSetId != null) {
            whereFields = new String[]{ FIELD_DCE_GUID, FIELD_DCE_DO_TYPE, };
            whereValues = new String[]{ changeSetId.getGUID(), idType };
        } else {
            whereFields = new String[]{ FIELD_DCE_DO_TYPE, };
            whereValues = new String[]{ idType };
        }
        if (changeSetStatus == null) {
            searchAndFill(project, TABLE_DA_CHANGE_SET_ENTRY, whereFields, whereValues, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        } else {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_DCS_STATUS });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ changeSetStatus.name() });
            searchSortAndFillWithJoin(project, null, null, new String[]{ FIELD_DCE_GUID }, TABLE_DA_CHANGE_SET, new String[]{ FIELD_DCS_GUID },
                                      false, false, whereFields, whereValues, false, null, false);
        }
    }

    private void loadForChangeSetAndDataObjectIdWithType(EtkProject project, iPartsChangeSetId changeSetId, IdWithType dataObjectIdWithType, boolean doLikeQuery) {
        clear(DBActionOrigin.FROM_DB);
        String[] whereFields = null;
        String[] whereValues = null;
        if (changeSetId != null) {
            if (dataObjectIdWithType != null) {
                iPartsChangeSetEntryId changeSetEntryId = new iPartsChangeSetEntryId(changeSetId, dataObjectIdWithType);
                whereFields = new String[]{ FIELD_DCE_GUID, FIELD_DCE_DO_TYPE, FIELD_DCE_DO_ID };
                whereValues = new String[]{ changeSetEntryId.getGUID(), changeSetEntryId.getDataObjectType(), changeSetEntryId.getDataObjectId() };
            } else {
                iPartsChangeSetEntryId changeSetEntryId = new iPartsChangeSetEntryId(changeSetId, new iPartsChangeSetEntryId());
                whereFields = new String[]{ FIELD_DCE_GUID };
                whereValues = new String[]{ changeSetEntryId.getGUID() };
            }
        } else if (dataObjectIdWithType != null) {
            iPartsChangeSetEntryId changeSetEntryId = new iPartsChangeSetEntryId(new iPartsChangeSetId(), dataObjectIdWithType);
            whereFields = new String[]{ FIELD_DCE_DO_TYPE, FIELD_DCE_DO_ID };
            whereValues = new String[]{ changeSetEntryId.getDataObjectType(), changeSetEntryId.getDataObjectId() };
        }
        if (doLikeQuery) {
            searchAndFillWithLike(project, TABLE_DA_CHANGE_SET_ENTRY, null, whereFields, whereValues, LoadType.COMPLETE, false, DBActionOrigin.FROM_DB);
        } else {
            searchAndFill(project, TABLE_DA_CHANGE_SET_ENTRY, whereFields, whereValues, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        }
    }

    private void loadForChangeSetAndTypeBySourceGUID(EtkProject project, String sourceGUID, iPartsChangeSetId changeSetId,
                                                     String dataObjectType) {
        clear(DBActionOrigin.FROM_DB);
        String[] whereFields;
        String[] whereValues;
        if (changeSetId != null) {
            if (dataObjectType != null) {
                whereFields = new String[]{ FIELD_DCE_GUID, FIELD_DCE_DO_TYPE, FIELD_DCE_DO_SOURCE_GUID };
                whereValues = new String[]{ changeSetId.getGUID(), dataObjectType, sourceGUID };
            } else {
                whereFields = new String[]{ FIELD_DCE_GUID, FIELD_DCE_DO_SOURCE_GUID };
                whereValues = new String[]{ changeSetId.getGUID(), sourceGUID };
            }
        } else if (dataObjectType != null) {
            whereFields = new String[]{ FIELD_DCE_DO_TYPE, FIELD_DCE_DO_SOURCE_GUID };
            whereValues = new String[]{ dataObjectType, sourceGUID };
        } else {
            whereFields = new String[]{ FIELD_DCE_DO_SOURCE_GUID };
            whereValues = new String[]{ sourceGUID };
        }

        searchAndFill(project, TABLE_DA_CHANGE_SET_ENTRY, whereFields, whereValues, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    public void loadForDataObjectIdWithTypeAndChangeSetStatus(EtkProject project, IdWithType dataObjectIdWithType, iPartsChangeSetStatus changeSetStatus) {
        loadForDataObjectIdWithTypeAndChangeSetStatus(project, dataObjectIdWithType, changeSetStatus, null);
    }

    /**
     * Lädt alle Änderungsseteinträge (Änderungsstand von einem {@link de.docware.framework.modules.db.DBDataObject}) für
     * die übergebene (teilweise) {@link de.docware.framework.modules.db.DBDataObject}-ID mit optionaler Einschränkung auf
     * einen {@link iPartsChangeSetStatus} inkl. der ID vom dazugehörigen Autoren-Auftrag (falls vorhanden).
     *
     * @param project
     * @param dataObjectIdWithType
     * @param changeSetStatus
     * @param foundAttributesCallback
     */
    public void loadForDataObjectIdWithTypeAndChangeSetStatus(EtkProject project, IdWithType dataObjectIdWithType,
                                                              iPartsChangeSetStatus changeSetStatus, FoundAttributesCallback foundAttributesCallback) {
        clear(DBActionOrigin.FROM_DB);

        EtkDisplayFields selectFields = project.getAllDisplayFieldsForTable(TABLE_DA_CHANGE_SET_ENTRY);
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET, FIELD_DCS_GUID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET, FIELD_DCS_COMMIT_DATE, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET, FIELD_DCS_SOURCE, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_GUID, false, false));

        iPartsChangeSetEntryId changeSetEntryId = new iPartsChangeSetEntryId(new iPartsChangeSetId(), dataObjectIdWithType);
        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_TYPE),
                                             TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_ID) };
        String[] whereValues = new String[]{ changeSetEntryId.getDataObjectType(), changeSetEntryId.getDataObjectId() };
        if (changeSetStatus != null) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET, FIELD_DCS_STATUS) });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ changeSetStatus.name() });
        }
        searchSortAndFillWithJoin(project, null, selectFields, whereFields, whereValues, false, null,
                                  false, true, foundAttributesCallback,
                                  new JoinData(TABLE_DA_CHANGE_SET, new String[]{ FIELD_DCE_GUID }, new String[]{ FIELD_DCS_GUID },
                                               false, false),
                                  new JoinData(TABLE_DA_AUTHOR_ORDER, new String[]{ FIELD_DCE_GUID }, new String[]{ FIELD_DAO_CHANGE_SET_ID },
                                               true, false));
    }

    /**
     * Analog zu {@link #loadForDataObjectIdWithTypeAndChangeSetStatus(EtkProject, IdWithType, iPartsChangeSetStatus)}.
     * Jedoch werden nur die ChangeSet-IDs sowie die Objekt-Kennung und -ID gesucht und geladen.
     * Hier kann man einen LRU-Cache dazwischenschalten (Objekt-Kennung und -ID).
     *
     * @param project
     * @param dataObjectIdWithType
     * @param changeSetStatus
     * @return
     */
    public static boolean hasDataObjectIdWithTypeAndChangeSetStatus(EtkProject project, IdWithType dataObjectIdWithType, iPartsChangeSetStatus changeSetStatus) {
        iPartsDataChangeSetEntryList changeSetEntryList = new iPartsDataChangeSetEntryList();
        changeSetEntryList.clear(DBActionOrigin.FROM_DB);
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET, FIELD_DCS_GUID, false, false));

        iPartsChangeSetEntryId changeSetEntryId = new iPartsChangeSetEntryId(new iPartsChangeSetId(), dataObjectIdWithType);
        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_TYPE),
                                             TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_ID) };
        String[] whereValues = new String[]{ changeSetEntryId.getDataObjectType(), changeSetEntryId.getDataObjectId() };
        if (changeSetStatus != null) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET, FIELD_DCS_STATUS) });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ changeSetStatus.name() });
        }
        changeSetEntryList.searchSortAndFillWithJoin(project, null, selectFields, whereFields, whereValues, false, null, false, true, null,
                                                     new JoinData(TABLE_DA_CHANGE_SET, new String[]{ FIELD_DCE_GUID }, new String[]{ FIELD_DCS_GUID },
                                                                  false, false));

        return !changeSetEntryList.isEmpty();
    }

    @Override
    protected iPartsDataChangeSetEntry getNewDataObject(EtkProject project) {
        return new iPartsDataChangeSetEntry(project, null);
    }
}