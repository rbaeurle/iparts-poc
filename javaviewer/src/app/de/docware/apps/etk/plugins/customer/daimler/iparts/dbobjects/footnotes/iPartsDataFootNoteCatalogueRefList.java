/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.LinkedList;
import java.util.List;

/**
 * Liste von {@link iPartsDataFootNoteCatalogueRef}s.
 */
public class iPartsDataFootNoteCatalogueRefList extends EtkDataObjectList<iPartsDataFootNoteCatalogueRef> implements iPartsConst {

    /**
     * löscht alle Fußnoten zu einem Assembly, ohne die Fussnote selbst zu löschen
     * !!Achtung: gilt nur für DAIMLER, da hier nur TextIds verwendet werden
     *
     * @param project
     * @param assemblyId
     */
    public static void deleteFootNotesForAssembly(EtkProject project, AssemblyId assemblyId) {
        project.getDbLayer().delete(TABLE_DA_FN_KATALOG_REF,
                                    new String[]{ FIELD_DFNK_MODULE, FIELD_DFNK_MODVER },
                                    new String[]{ assemblyId.getKVari(), assemblyId.getKVer() });
    }

    /**
     * löscht alle Fußnoten zu einem PartListEntry, ohne die Fussnote selbst zu löschen
     * !!Achtung: gilt nur für DAIMLER, da hier nur TextIds verwendet werden
     *
     * @param project
     * @param partListEntryId
     */
    public static void deleteFootNotesForPartListEntry(EtkProject project, PartListEntryId partListEntryId) {
        project.getDbLayer().delete(TABLE_DA_FN_KATALOG_REF,
                                    new String[]{ FIELD_DFNK_MODULE, FIELD_DFNK_MODVER, FIELD_DFNK_SEQNO },
                                    new String[]{ partListEntryId.getKVari(), partListEntryId.getKVer(), partListEntryId.getKLfdnr() });
    }

    /**
     * Liefert alle Fußnoten zu einem Assembly (sortiert nach K_LfdNr, FNId, FN_SEQNO)
     *
     * @param project
     * @param assemblyId
     * @return
     */
    public static iPartsDataFootNoteCatalogueRefList loadFootNotesForAssembly(EtkProject project, AssemblyId assemblyId) {
        iPartsDataFootNoteCatalogueRefList list = new iPartsDataFootNoteCatalogueRefList();
        list.loadFootNotesForAssemblyFromDB(project, assemblyId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Liefert alle Fußnoten zu einem PartListEntry (sortiert nach FNId, FN_SEQNO)
     *
     * @param project
     * @param partListEntryId
     * @return
     */
    public static iPartsDataFootNoteCatalogueRefList loadFootNotesForPartListEntryWithJoin(EtkProject project,
                                                                                           PartListEntryId partListEntryId) {
        iPartsDataFootNoteCatalogueRefList list = new iPartsDataFootNoteCatalogueRefList();
        list.loadFootNotesForPartListEntryFromDB(project, partListEntryId, true, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Liefert alle Fußnoten zu einem PartListEntry (sortiert nach FNId, FN_SEQNO)
     *
     * @param project
     * @param partListEntryId
     * @return
     */
    public static iPartsDataFootNoteCatalogueRefList loadFootNotesForPartListEntry(EtkProject project,
                                                                                   PartListEntryId partListEntryId) {
        iPartsDataFootNoteCatalogueRefList list = new iPartsDataFootNoteCatalogueRefList();
        list.loadFootNotesForPartListEntryFromDB(project, partListEntryId, false, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Liefert alle Fußnoten-IDs {@link iPartsFootNoteId} zu einem PartListEntry (sortiert nach FNId, FN_SEQNO)
     *
     * @param project
     * @param partListEntryId
     * @return
     */
    public static List<iPartsFootNoteId> getFootNoteListForPartListEntry(EtkProject project, PartListEntryId partListEntryId) {
        List<iPartsFootNoteId> footNoteIdList = new LinkedList<iPartsFootNoteId>();
        iPartsDataFootNoteCatalogueRefList entries = loadFootNotesForPartListEntryWithJoin(project, partListEntryId);
        for (iPartsDataFootNoteCatalogueRef dataFootNoteCatalogueRef : entries) {
            footNoteIdList.add(new iPartsFootNoteId(dataFootNoteCatalogueRef.getAsId().getFootNoteId()));
        }
        return footNoteIdList;
    }

    /**
     * Liefert alle Fußnoten zu einem Assembly (sortiert nach K_LfdNr, FNId, FN_SEQNO)
     *
     * @param project
     * @param assemblyId
     * @param origin
     */
    private void loadFootNotesForAssemblyFromDB(EtkProject project, AssemblyId assemblyId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DFNK_MODULE, FIELD_DFNK_MODVER };
        String[] whereValues = new String[]{ assemblyId.getKVari(), assemblyId.getKVer() };
        String[] sortFields = new String[]{ FIELD_DFNK_SEQNO, FIELD_DFNK_FN_SEQNO };
        searchSortAndFill(project, TABLE_DA_FN_KATALOG_REF, whereFields, whereValues, sortFields,
                          LoadType.COMPLETE, origin);
/*
    String FIELD_DFNK_MODULE             = "DFNK_MODULE";
    String FIELD_DFNK_MODVER             = "DFNK_MODVER";
    String FIELD_DFNK_SEQNO              = "DFNK_SEQNO";
    String FIELD_DFNK_FNID               = "DFNK_FNID";

 */
    }

    /**
     * Liefert alle Fußnoten zu einem PartListEntry (sortiert nach FNId, FN_SEQNO)
     *
     * @param project
     * @param partListEntryId
     * @param origin
     */
    private void loadFootNotesForPartListEntryFromDB(EtkProject project, PartListEntryId partListEntryId, boolean withJoin,
                                                     DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DFNK_MODULE, FIELD_DFNK_MODVER, FIELD_DFNK_SEQNO };
        String[] whereValues = new String[]{ partListEntryId.getKVari(), partListEntryId.getKVer(), partListEntryId.getKLfdnr() };
        String[] sortFields = new String[]{ FIELD_DFNK_FN_SEQNO };
        if (withJoin) {
            EtkDisplayFields selectFields = project.getAllDisplayFieldsForTable(TABLE_DA_FN_KATALOG_REF);
            selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_FN));
            searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields, whereFields, whereValues, false, sortFields,
                                      false, null,
                                      new JoinData(TABLE_DA_FN,
                                                   new String[]{ FIELD_DFNK_FNID },
                                                   new String[]{ FIELD_DFN_ID },
                                                   false, false));
        } else {
            searchSortAndFill(project, TABLE_DA_FN_KATALOG_REF, whereFields, whereValues, sortFields,
                              LoadType.COMPLETE, origin);
        }
    }

    @Override
    protected iPartsDataFootNoteCatalogueRef getNewDataObject(EtkProject project) {
        return new iPartsDataFootNoteCatalogueRef(project, null);
    }
}
