/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Liste mit {@link iPartsDataReplacePart} Objekten
 */
public class iPartsDataReplacePartList extends EtkDataObjectList<iPartsDataReplacePart> implements iPartsConst {

    /**
     * Lädt alle Ersetzungen zu einer Assembly (sortiert nach kVari, kVer, laufender Nummer der Ersetzung)
     *
     * @param project
     * @param assemblyId
     * @return
     */
    public static iPartsDataReplacePartList loadReplacementsForAssembly(EtkProject project, AssemblyId assemblyId) {
        iPartsDataReplacePartList list = new iPartsDataReplacePartList();
        list.loadDataForAssembly(project, assemblyId, null, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Löscht alle Ersetzungen für eine Assembly
     *
     * @param project
     * @param assemblyId
     */
    public static void deleteReplacementsForAssembly(EtkProject project, AssemblyId assemblyId) {
        project.getDbLayer().delete(TABLE_DA_REPLACE_PART, new String[]{ FIELD_DRP_VARI, FIELD_DRP_VER },
                                    new String[]{ assemblyId.getKVari(), assemblyId.getKVer() });

    }

    /**
     * Lädt alle Vorgänger zu einem PartListEntry (sortiert nach laufender Nummer der Ersetzung)
     *
     * @param project
     * @param partListEntryId
     * @return
     */
    public static iPartsDataReplacePartList loadPredecessorsForPartListEntry(EtkProject project, PartListEntryId partListEntryId) {
        iPartsDataReplacePartList list = new iPartsDataReplacePartList();
        list.loadPredecessors(project, partListEntryId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle Nachfolger zu einem PartListEntry (sortiert nach laufender Nummer der Ersetzung)
     *
     * @param project
     * @param partListEntryId
     * @return
     */
    public static iPartsDataReplacePartList loadSuccessorsForPartListEntry(EtkProject project, PartListEntryId partListEntryId) {
        iPartsDataReplacePartList list = new iPartsDataReplacePartList();
        list.loadSuccessors(project, partListEntryId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt Ersetzungen mit angegegebenen GUIDS (AA unabh.) als Vorgänger und Nachfolger
     *
     * @param project
     * @param predecessorGUID
     * @param successorGUID
     * @return
     */
    public static iPartsDataReplacePartList loadReplacementsWithSuccessorAndPredecessorGUID(EtkProject project, String predecessorGUID, String successorGUID) {
        iPartsDataReplacePartList list = new iPartsDataReplacePartList();
        list.loadReplacementsForDialogGUIDsWithoutAA(project, predecessorGUID, successorGUID);
        return list;
    }

    /**
     * Lädt alle Ersetzungen zu einer Assembly (sortiert nach kVari, kVer, laufender Nummer der Ersetzung)
     *
     * @param project
     * @param assemblyId
     * @param status     Optionaler Status
     * @param origin
     */
    public void loadDataForAssembly(EtkProject project, AssemblyId assemblyId, iPartsDataReleaseState status, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields;
        String[] whereValues;
        String[] sortFields = new String[]{ FIELD_DRP_VARI, FIELD_DRP_VER, FIELD_DRP_SEQNO };
        if (status != null) {
            whereFields = new String[]{ FIELD_DRP_VARI, FIELD_DRP_VER, FIELD_DRP_STATUS };
            whereValues = new String[]{ assemblyId.getKVari(), assemblyId.getKVer(), status.getDbValue() };

        } else {
            whereFields = new String[]{ FIELD_DRP_VARI, FIELD_DRP_VER };
            whereValues = new String[]{ assemblyId.getKVari(), assemblyId.getKVer() };
        }

        searchSortAndFill(project, TABLE_DA_REPLACE_PART, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }


    /**
     * Lädt alle Vorgänger zu einem PartListEntry (sortiert nach laufender Nummer der Ersetzung)
     *
     * @param project
     * @param partListEntryId
     * @param origin
     */
    private void loadPredecessors(EtkProject project, PartListEntryId partListEntryId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DRP_VARI, FIELD_DRP_VER, FIELD_DRP_REPLACE_LFDNR };
        String[] whereValues = new String[]{ partListEntryId.getKVari(), partListEntryId.getKVer(), partListEntryId.getKLfdnr() };
        String[] sortFields = new String[]{ FIELD_DRP_SEQNO };

        searchSortAndFill(project, TABLE_DA_REPLACE_PART, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle Nachfolger zu einem PartListEntry (sortiert nach laufender Nummer der Ersetzung)
     *
     * @param project
     * @param partListEntryId
     * @param origin
     */
    private void loadSuccessors(EtkProject project, PartListEntryId partListEntryId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DRP_VARI, FIELD_DRP_VER, FIELD_DRP_LFDNR };
        String[] whereValues = new String[]{ partListEntryId.getKVari(), partListEntryId.getKVer(), partListEntryId.getKLfdnr() };
        String[] sortFields = new String[]{ FIELD_DRP_SEQNO };

        searchSortAndFill(project, TABLE_DA_REPLACE_PART, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }

    /**
     * liefert alle Vorgänger zum übergebenen PartListEntry aus der bereits geladenen Liste zurück.
     *
     * @param partListEntryId
     * @return
     */
    public List<iPartsDataReplacePart> getPredecessorsFromList(PartListEntryId partListEntryId) {
        String[] whereFields = new String[]{ iPartsConst.FIELD_DRP_VARI, iPartsConst.FIELD_DRP_VER, iPartsConst.FIELD_DRP_REPLACE_LFDNR };
        String[] whereValues = new String[]{ partListEntryId.getKVari(), partListEntryId.getKVer(), partListEntryId.getKLfdnr() };

        return getSubSetFromList(whereFields, whereValues);
    }

    /**
     * liefert alle Nachfolger zum übergebenen PartListEntry aus der bereits geladenen Liste zurück.
     *
     * @param partListEntryId
     * @return
     */
    public List<iPartsDataReplacePart> getSuccessorsFromList(PartListEntryId partListEntryId) {
        String[] whereFields = new String[]{ iPartsConst.FIELD_DRP_VARI, iPartsConst.FIELD_DRP_VER, iPartsConst.FIELD_DRP_LFDNR };
        String[] whereValues = new String[]{ partListEntryId.getKVari(), partListEntryId.getKVer(), partListEntryId.getKLfdnr() };

        return getSubSetFromList(whereFields, whereValues);
    }

    /**
     * liefert alle Einträge aus der bereits geladenen Liste zurück, die die übergebenen Feldwerte haben. Das heißt es findet
     * kein Zugriff auf die Datenbank statt. Die Methode macht also nur Sinn, wenn vorher bereits eine Lademethode aufgerufen wurde.
     *
     * @param whereFields
     * @param whereValues
     * @return
     */
    public List<iPartsDataReplacePart> getSubSetFromList(String[] whereFields, String[] whereValues) {
        List<iPartsDataReplacePart> result = new DwList<>();
        for (iPartsDataReplacePart replacePart : list) {
            boolean wasFound = true;
            for (int i = 0; i < whereFields.length; i++) {
                if (!replacePart.getFieldValue(whereFields[i]).equals(whereValues[i])) {
                    wasFound = false;
                }
            }
            if (wasFound) {
                result.add(replacePart);
            }
        }
        return result;
    }

    /**
     * Liefert alle Ersetzungen für die übergebenen DIALOG-GUIDs von echtem Vorgänger und echtem Nachfolger.
     * Mit variabler AA
     *
     * @param project
     * @param predecessorGUID
     * @param successorGUID
     */
    public void loadReplacementsForDialogGUIDsWithoutAA(EtkProject project, String predecessorGUID, String successorGUID) {
        clear(DBActionOrigin.FROM_DB);
        // BCTE Schlüssel als Suchkriterium für alle möglichen AAs
        String searchValuePredecessor = getDialogGuidForAllPossibleAA(predecessorGUID);
        String searchValueSucessor = getDialogGuidForAllPossibleAA(successorGUID);

        String[] whereFields = new String[]{ FIELD_DRP_SOURCE_GUID, FIELD_DRP_REPLACE_SOURCE_GUID };
        String[] whereValues = new String[]{ searchValuePredecessor, searchValueSucessor };
        String[] sortFields = new String[]{ FIELD_DRP_VARI, FIELD_DRP_VER, FIELD_DRP_LFDNR, FIELD_DRP_SEQNO };

        searchSortAndFillWithLike(project, TABLE_DA_REPLACE_PART, null, whereFields, whereValues, sortFields, false, LoadType.COMPLETE, false, DBActionOrigin.FROM_DB);
    }

    /**
     * Liefert den übergebenen BCTE Schlüssel als String in dem der AA Wert durch die Wildcard * ersetzt wurde
     *
     * @param dialogGuid
     * @return
     */
    private String getDialogGuidForAllPossibleAA(String dialogGuid) {
        iPartsDialogBCTEPrimaryKey primaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dialogGuid);
        if (primaryKey != null) {
            primaryKey.aa = "*";
            return primaryKey.createGUID();
        }
        return dialogGuid;
    }

    @Override
    protected iPartsDataReplacePart getNewDataObject(EtkProject project) {
        return new iPartsDataReplacePart(project, null);
    }
}
