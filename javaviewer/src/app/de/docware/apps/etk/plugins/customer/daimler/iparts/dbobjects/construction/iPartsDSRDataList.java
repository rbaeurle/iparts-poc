package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

/*
 * Copyright (c) 2017 Docware GmbH
 *
 * Tabelle [DA_DIALOG_DSR], Listenobjekt für sicherheits- und zertifizierungsrelevante Teile, DSR-Kenner
 */

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDSRDataList extends EtkDataObjectList<iPartsDSRData> implements iPartsConst {

    public iPartsDSRDataList() {
        setSearchWithoutActiveChangeSets(true);
    }

    @Override
    protected iPartsDSRData getNewDataObject(EtkProject project) {
        return new iPartsDSRData(project, null);
    }

    public static iPartsDSRDataList loadDSRDataForMatTypeAndNo(EtkProject project, String matNr, String dsrType, String dsrNo) {
        iPartsDSRDataList list = new iPartsDSRDataList();
        list.loadDSRDataForMatTypeAndNoFromDB(project, matNr, dsrType, dsrNo, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadDSRDataForMatTypeAndNoFromDB(EtkProject project, String matNr, String dsrType, String dsrNo, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DSR_MATNR, FIELD_DSR_TYPE, FIELD_DSR_NO };
        String[] whereValues = new String[]{ matNr, dsrType, dsrNo };
        String[] sortFields = new String[]{ FIELD_DSR_MATNR, FIELD_DSR_SDATA, FIELD_DSR_MK4, FIELD_DSR_MK5 };
        searchSortAndFill(project, TABLE_DA_FN_MAT_REF, whereFields, whereValues, sortFields,
                          LoadType.COMPLETE, origin);

    }
}
