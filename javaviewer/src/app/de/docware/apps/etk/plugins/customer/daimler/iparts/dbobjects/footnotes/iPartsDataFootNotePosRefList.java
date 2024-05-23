package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataFootNotePosRef}s.
 */
public class iPartsDataFootNotePosRefList extends EtkDataObjectList<iPartsDataFootNotePosRef> implements iPartsConst {

    public iPartsDataFootNotePosRefList() {
        setSearchWithoutActiveChangeSets(true);
    }

    public static iPartsDataFootNotePosRefList loadAllFootNotesForIDWithoutSDA(EtkProject project, iPartsFootNotePosRefId footNotePosRefId) {
        iPartsDataFootNotePosRefList list = new iPartsDataFootNotePosRefList();
        list.loadEntriesForIDWithoutSDA(project, footNotePosRefId);
        return list;
    }

    private void loadEntriesForIDWithoutSDA(EtkProject project, iPartsFootNotePosRefId footNotePosRefId) {
        clear(DBActionOrigin.FROM_DB);
        iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(footNotePosRefId.getBCTEKey());
        if (bctePrimaryKey != null) {
            bctePrimaryKey.sData = "*";
            String[] whereFields = new String[]{ FIELD_DFNP_GUID, FIELD_DFNP_SESI, FIELD_DFNP_POSP, FIELD_DFNP_FN_NO };
            String[] whereValues = new String[]{ bctePrimaryKey.createDialogGUID(), footNotePosRefId.getSesi(),
                                                 footNotePosRefId.getPosP(), footNotePosRefId.getFnNo() };
            searchAndFillWithLike(project, TABLE_DA_FN_POS, null, whereFields, whereValues, LoadType.COMPLETE,
                                  false, DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Lädt eine komplette Liste aller {@link iPartsDataFootNotePosRef}s der Tabelle.
     *
     * @param project
     * @return
     */
    public void loadFootNotePosRefListFromDB(EtkProject project) {
        clear(DBActionOrigin.FROM_DB);
        searchSortAndFill(project, TABLE_DA_FN_POS, null, null,
                          new String[]{ FIELD_DFNP_GUID, FIELD_DFNP_SESI, FIELD_DFNP_POSP, FIELD_DFNP_FN_NO },
                          LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataFootNotePosRef getNewDataObject(EtkProject project) {
        return new iPartsDataFootNotePosRef(project, null);
    }
}
