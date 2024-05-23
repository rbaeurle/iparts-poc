package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.config.iPartsNutzDokProcessingState;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataNutzDokKEMList extends EtkDataObjectList<iPartsDataNutzDokKEM> implements iPartsConst {

    public iPartsDataNutzDokKEMList() {
        setSearchWithoutActiveChangeSets(true);
    }

    public static iPartsDataNutzDokKEMList loadAllEntriesWithStatus(EtkProject project, iPartsNutzDokProcessingState statusValue) {
        iPartsDataNutzDokKEMList list = new iPartsDataNutzDokKEMList();
        list.loadAllEntriesWithStatusFromDB(project, statusValue, DBActionOrigin.FROM_DB);
        return list;
    }


    private void loadAllEntriesWithStatusFromDB(EtkProject project, iPartsNutzDokProcessingState statusValue, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DNK_PROCESSING_STATE };
        String[] whereValues = new String[]{ statusValue.getDBValue() };
        String[] sortFields = new String[]{ FIELD_DNK_KEM };

        searchSortAndFill(project, TABLE_DA_NUTZDOK_KEM, whereFields, whereValues, sortFields,
                          LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataNutzDokKEM getNewDataObject(EtkProject project) {
        return new iPartsDataNutzDokKEM(project, null);
    }
}
