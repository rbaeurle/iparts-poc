/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste mit {@link iPartsDataDialogAddData} Objekten
 */
public class iPartsDataDialogAddDataList extends EtkDataObjectList<iPartsDataDialogAddData> implements iPartsConst {

    public static iPartsDataDialogAddDataList loadAllDataForBCTEKey(EtkProject project, String bcteGUID) {
        iPartsDataDialogAddDataList result = new iPartsDataDialogAddDataList();
        result.loadForBCTEKey(project, bcteGUID, DBActionOrigin.FROM_DB);
        return result;
    }

    private void loadForBCTEKey(EtkProject project, String bcteGUID, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_DIALOG_ADD_DATA,
                          new String[]{ FIELD_DAD_GUID }, new String[]{ bcteGUID },
                          new String[]{ FIELD_DAD_ADAT },
                          LoadType.COMPLETE, origin);
    }

    /**
     * Lädt die neuesten (SDATB leer) Zusatzdaten für den übergebenen HMMSM Knoten
     *
     * @param project
     * @param hmMSmId
     * @return
     */
    public static iPartsDataDialogAddDataList loadNewestDataForHmMSmNode(EtkProject project, HmMSmId hmMSmId) {
        iPartsDataDialogAddDataList result = new iPartsDataDialogAddDataList();
        result.loadForHmMSm(project, hmMSmId, DBActionOrigin.FROM_DB);
        return result;
    }

    private void loadForHmMSm(EtkProject project, HmMSmId hmMSmId, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_DIALOG_ADD_DATA,
                          new String[]{ FIELD_DAD_SERIES_NO, FIELD_DAD_HM, FIELD_DAD_M, FIELD_DAD_SM, FIELD_DAD_SDATB },
                          new String[]{ hmMSmId.getSeries(), hmMSmId.getHm(), hmMSmId.getM(), hmMSmId.getSm(), "" },
                          new String[]{ FIELD_DAD_ADAT },
                          LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataDialogAddData getNewDataObject(EtkProject project) {
        return new iPartsDataDialogAddData(project, null);
    }
}
