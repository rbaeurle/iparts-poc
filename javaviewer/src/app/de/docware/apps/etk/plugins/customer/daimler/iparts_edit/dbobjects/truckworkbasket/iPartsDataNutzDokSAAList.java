package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.config.iPartsNutzDokProcessingState;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataNutzDokSAAList extends EtkDataObjectList<iPartsDataNutzDokSAA> implements iPartsConst {

    public iPartsDataNutzDokSAAList() {
        setSearchWithoutActiveChangeSets(true);
    }

    public static iPartsDataNutzDokSAAList loadAllEntriesWithStatus(EtkProject project, iPartsNutzDokProcessingState statusValue) {
        iPartsDataNutzDokSAAList list = new iPartsDataNutzDokSAAList();
        list.loadAllEntriesWithStatusFromDB(project, statusValue, DBActionOrigin.FROM_DB);
        return list;
    }


    private void loadAllEntriesWithStatusFromDB(EtkProject project, iPartsNutzDokProcessingState statusValue, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DNS_PROCESSING_STATE };
        String[] whereValues = new String[]{ statusValue.getDBValue() };
        String[] sortFields = new String[]{ FIELD_DNS_SAA };

        searchSortAndFill(project, TABLE_DA_NUTZDOK_SAA, whereFields, whereValues, sortFields,
                          LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataNutzDokSAA getNewDataObject(EtkProject project) {
        return new iPartsDataNutzDokSAA(project, null);
    }
}
