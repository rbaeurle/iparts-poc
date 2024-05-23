/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;


import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class iPartsDataInternalTextList extends EtkDataObjectList<iPartsDataInternalText> implements iPartsConst {

    /**
     * Liefert alle internen Texte zu einem DIALOG-Konstruktions-Stücklisteneintrag sortiert nach {@link #FIELD_DIT_CHANGE_DATE}.
     *
     * @param project
     * @param dialogId
     * @return
     */
    public static iPartsDataInternalTextList getAllInternalTextForPartListEntry(EtkProject project, iPartsDialogId dialogId) {
        iPartsDataInternalTextList list = new iPartsDataInternalTextList();
        list.loadInternalTextsForPartPosFromDB(project, dialogId, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Liefert alle internen Texte zu einem Stücklisteneintrag sortiert nach {@link #FIELD_DIT_CHANGE_DATE}.
     *
     * @param project
     * @param partListEntryId
     * @return
     */
    public static iPartsDataInternalTextList getAllInternalTextForPartListEntry(EtkProject project, PartListEntryId partListEntryId) {
        iPartsDataInternalTextList list = new iPartsDataInternalTextList();
        list.loadInternalTextsForPartPosFromDB(project, partListEntryId, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Liefert den aktuellsten {@link iPartsDataInternalText} zu einem Stücklisteneintrag {@link PartListEntryId}.
     * Besitzt der Stücklisteneintrag keinen internen Text, so wird {@code null} zurückgegegeben.
     *
     * @param project
     * @param partListEntryId
     * @return
     */
    public static iPartsDataInternalText getYoungestDataInternalText(EtkProject project, PartListEntryId partListEntryId) {
        iPartsDataInternalTextList list = getAllInternalTextForPartListEntry(project, partListEntryId);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Liefert eine Map aller internen Texte pro Stücklisteneintrag für eine Assembly sortiert nach {@link #FIELD_DIT_CHANGE_DATE}.
     *
     * @param project
     * @param assemblyId
     * @return
     */
    public static Map<PartListEntryId, List<iPartsDataInternalText>> loadInternalTextsForAssembly(EtkProject project, AssemblyId assemblyId) {
        Map<PartListEntryId, List<iPartsDataInternalText>> resultMap = new HashMap<>();
        iPartsDataInternalTextList list = new iPartsDataInternalTextList();
        list.loadInternalTextsForAssemblyFromDB(project, assemblyId, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        for (iPartsDataInternalText dataInternalText : list) {
            IdWithType id = IdWithType.fromDBString(PartListEntryId.TYPE, dataInternalText.getDataObjectId());
            PartListEntryId partListEntryId = new PartListEntryId(id.toStringArrayWithoutType());
            List<iPartsDataInternalText> internalTextList = resultMap.get(partListEntryId);
            if (internalTextList == null) {
                internalTextList = new DwList<>();
                resultMap.put(partListEntryId, internalTextList);
            }
            internalTextList.add(dataInternalText);
        }
        return resultMap;
    }

    /**
     * Liefert eine Map aller internen Texte pro Stücklisteneintrag für eine DIALOG-Konstruktions-Assembly sortiert nach
     * {@link #FIELD_DIT_CHANGE_DATE}.
     *
     * @param project
     * @param hmMSmId
     * @return
     */
    public static Map<iPartsDialogBCTEPrimaryKey, List<iPartsDataInternalText>> loadInternalTextsForAssembly(EtkProject project, HmMSmId hmMSmId) {
        Map<iPartsDialogBCTEPrimaryKey, List<iPartsDataInternalText>> resultMap = new HashMap<>();
        iPartsDataInternalTextList list = new iPartsDataInternalTextList();
        list.loadInternalTextsForAssemblyFromDB(project, hmMSmId, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        for (iPartsDataInternalText dataInternalText : list) {
            IdWithType id = IdWithType.fromDBString(iPartsDialogId.TYPE, dataInternalText.getDataObjectId());
            iPartsDialogId dialogId = new iPartsDialogId(id.getValue(1)); // Typ ist an Index 0
            iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dialogId.getDialogGuid());
            if (bcteKey != null) {
                List<iPartsDataInternalText> internalTextList = resultMap.get(bcteKey);
                if (internalTextList == null) {
                    internalTextList = new DwList<>();
                    resultMap.put(bcteKey, internalTextList);
                }
                internalTextList.add(dataInternalText);
            }
        }
        return resultMap;
    }

    /**
     * überprüft, ob ein Stücklisteneintrag interne Texte besitzt.
     *
     * @param project
     * @param partListEntryId
     * @return
     */
    public static boolean hasPartListEntryInternalTexts(EtkProject project, PartListEntryId partListEntryId) {
        iPartsDataInternalTextList list = new iPartsDataInternalTextList();
        list.loadInternalTextsForPartPosFromDB(project, partListEntryId, LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
        return !list.isEmpty();
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataInternalText} für die übergebene TeilePos(=Data Object ID)
     *
     * @param project
     * @param idWithType
     * @param origin
     */
    private void loadInternalTextsForPartPosFromDB(EtkProject project, IdWithType idWithType, LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DIT_DO_TYPE, FIELD_DIT_DO_ID };
        String[] whereValues = new String[]{ idWithType.getType(), idWithType.toDBString() };

        String[] sortFields = new String[]{ FIELD_DIT_CHANGE_DATE };

        // Absteigend nach Zeitstempel sortieren, der jüngste soll ganz oben in der Liste dargestellt werden.
        searchSortAndFill(project, TABLE_DA_INTERNAL_TEXT, whereFields, whereValues, sortFields, loadType, true, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataInternalText} für das übergebene Modul
     *
     * @param project
     * @param searchValue entweder {@link HmMSmId} oder {@link AssemblyId}
     * @param loadType
     * @param origin
     */
    private void loadInternalTextsForAssemblyFromDB(EtkProject project, IdWithType searchValue, LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        String dataObjectType;
        String dataObjectLikeId;
        if (searchValue.getType().equals(HmMSmId.TYPE)) {
            // Suche nach Konstruktions-Einträgen
            dataObjectType = iPartsDialogId.TYPE;
            iPartsDialogId searchDialogId = new iPartsDialogId(searchValue.toString(iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER)
                                                               + iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER);
            dataObjectLikeId = searchDialogId.toDBString();
        } else {
            // Suche nach Retail-Einträgen
            dataObjectType = PartListEntryId.TYPE;
            dataObjectLikeId = searchValue.toDBString() + IdWithType.DB_ID_DELIMITER;
        }
        String[] whereFields = new String[]{ FIELD_DIT_DO_TYPE, FIELD_DIT_DO_ID };
        String[] whereValues = new String[]{ dataObjectType, dataObjectLikeId + "*" };

        String[] sortFields = new String[]{ FIELD_DIT_CHANGE_DATE };

        // Absteigend nach Zeitstempel sortieren, der jüngste soll ganz oben in der Liste dargestellt werden.
        searchWithWildCardsSortAndFill(project, whereFields, whereValues, sortFields, true, loadType, origin);
    }

    @Override
    protected iPartsDataInternalText getNewDataObject(EtkProject project) {
        return new iPartsDataInternalText(project, null);
    }
}
