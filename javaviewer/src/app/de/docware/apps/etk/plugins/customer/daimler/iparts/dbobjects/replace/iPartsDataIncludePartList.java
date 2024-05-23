/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste mit {@link iPartsDataIncludePart} Objekten
 */
public class iPartsDataIncludePartList extends EtkDataObjectList<iPartsDataIncludePart> implements iPartsConst {

    /**
     * Lädt alle Mitlieferteile zu einer Assembly
     *
     * @param project
     * @param assemblyId
     * @return
     */
    public static iPartsDataIncludePartList loadIncludePartsForAssembly(EtkProject project, AssemblyId assemblyId) {
        iPartsDataIncludePartList list = new iPartsDataIncludePartList();
        list.loadAllData(project, assemblyId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle Mitlieferteile zu einem Ersetzungsvorgang über die zugehörige Ersetzung, egal ob diese einen echten Nachfolger
     * oder nur eine Nachfolgermaterialnummer hat.
     *
     * @param project
     * @param replacement
     * @return
     */
    public static iPartsDataIncludePartList loadIncludePartsForReplacement(EtkProject project, iPartsDataReplacePart replacement) {
        if (replacement.getSuccessorPartListEntryId() != null) {
            return loadIncludePartsForReplacement(project, replacement.getPredecessorPartListEntryId(), replacement.getSuccessorPartListEntryId());
        } else {
            return loadIncludePartsForReplacement(project, replacement.getPredecessorPartListEntryId(), replacement.getFieldValue(FIELD_DRP_REPLACE_MATNR));
        }
    }

    /**
     * Lädt alle Mitlieferteile zu einem Ersetzungsvorgang über die Materialnummer des Nachfolgers.
     *
     * @param project
     * @param predecessorPartListEntryId
     * @param successorMatNr
     * @return
     */
    public static iPartsDataIncludePartList loadIncludePartsForReplacement(EtkProject project, PartListEntryId predecessorPartListEntryId,
                                                                           String successorMatNr) {
        iPartsDataIncludePartList list = new iPartsDataIncludePartList();
        list.loadDataForReplacement(project, predecessorPartListEntryId, successorMatNr, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle Mitlieferteile zu einem Ersetzungsvorgang über die {@link PartListEntryId} des Nachfolgers.
     * des Nachfolgers.
     *
     * @param project
     * @param predecessorPartListEntryId
     * @param successorPartListEntryId
     */
    public static iPartsDataIncludePartList loadIncludePartsForReplacement(EtkProject project, PartListEntryId predecessorPartListEntryId,
                                                                           PartListEntryId successorPartListEntryId) {
        iPartsDataIncludePartList list = new iPartsDataIncludePartList();
        list.loadDataForReplacement(project, predecessorPartListEntryId, successorPartListEntryId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Löscht alle Mitlieferteile für eine Assembly
     *
     * @param project
     * @param assemblyId
     */
    public static void deleteIncludePartsForAssembly(EtkProject project, AssemblyId assemblyId) {
        project.getDbLayer().delete(TABLE_DA_INCLUDE_PART, new String[]{ FIELD_DIP_VARI, FIELD_DIP_VER },
                                    new String[]{ assemblyId.getKVari(), assemblyId.getKVer() });

    }

    /**
     * Löscht alle Mitlieferteile für einen PartListEntry
     *
     * @param project
     * @param partListEntryId
     */
    public static void deleteIncludePartsForPartListEntry(EtkProject project, PartListEntryId partListEntryId) {
        project.getDbLayer().delete(TABLE_DA_INCLUDE_PART, new String[]{ FIELD_DIP_VARI, FIELD_DIP_VER, FIELD_DIP_REPLACE_LFDNR },
                                    new String[]{ partListEntryId.getKVari(), partListEntryId.getKVer(), partListEntryId.getKLfdnr() });

    }

    /**
     * Lädt alle Mitlieferteile zu einem Ersetzungsvorgang über die Materialnummer des Nachfolgers.
     *
     * @param project
     * @param predecessorPartListEntryId
     * @param successorMatNr
     * @param origin
     */
    private void loadDataForReplacement(EtkProject project, PartListEntryId predecessorPartListEntryId,
                                        String successorMatNr, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DIP_VARI, FIELD_DIP_VER, FIELD_DIP_LFDNR, FIELD_DIP_REPLACE_MATNR };
        String[] whereValues = new String[]{ predecessorPartListEntryId.getKVari(), predecessorPartListEntryId.getKVer(),
                                             predecessorPartListEntryId.getKLfdnr(), successorMatNr };
        String[] sortFields = new String[]{ FIELD_DIP_SEQNO };

        searchSortAndFill(project, TABLE_DA_INCLUDE_PART, whereFields, whereValues, sortFields, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Lädt alle Mitlieferteile zu einem Ersetzungsvorgang über die {@link PartListEntryId} des Nachfolgers.
     *
     * @param project
     * @param predecessorPartListEntryId
     * @param successorPartListEntryId
     * @param origin
     */
    private void loadDataForReplacement(EtkProject project, PartListEntryId predecessorPartListEntryId, PartListEntryId successorPartListEntryId,
                                        DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DIP_VARI, FIELD_DIP_VER, FIELD_DIP_LFDNR, FIELD_DIP_REPLACE_LFDNR };
        String[] whereValues = new String[]{ predecessorPartListEntryId.getKVari(), predecessorPartListEntryId.getKVer(),
                                             predecessorPartListEntryId.getKLfdnr(), successorPartListEntryId.getKLfdnr() };
        String[] sortFields = new String[]{ FIELD_DIP_SEQNO };

        searchSortAndFill(project, TABLE_DA_INCLUDE_PART, whereFields, whereValues, sortFields, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Lädt alle Mitlieferteile zu einer Assembly
     *
     * @param project
     * @param assemblyId
     * @param origin
     */
    private void loadAllData(EtkProject project, AssemblyId assemblyId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DIP_VARI, FIELD_DIP_VER };
        String[] whereValues = new String[]{ assemblyId.getKVari(), assemblyId.getKVer() };
        String[] sortFields = new String[]{ FIELD_DIP_VARI, FIELD_DIP_VER, FIELD_DIP_LFDNR, FIELD_DIP_SEQNO };

        searchSortAndFill(project, TABLE_DA_INCLUDE_PART, whereFields, whereValues, sortFields, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataIncludePart getNewDataObject(EtkProject project) {
        return new iPartsDataIncludePart(project, null);
    }
}
