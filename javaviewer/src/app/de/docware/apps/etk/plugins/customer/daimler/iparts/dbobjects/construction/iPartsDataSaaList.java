/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;

/**
 * Liste mit {@link iPartsDataSaa} Objekten
 */
public class iPartsDataSaaList extends EtkDataObjectList<iPartsDataSaa> implements iPartsConst {

    public static iPartsDataSaaList loadAllSaasForSource(EtkProject project, iPartsImportDataOrigin source) {
        iPartsDataSaaList list = new iPartsDataSaaList();
        list.loadAllSaasForSourceFromDB(project, source.getOrigin(), DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataSaaList loadAllSaasForSa(EtkProject project, String saNumber) {
        iPartsDataSaaList list = new iPartsDataSaaList();
        list.loadAllSaasForSaFromDB(project, saNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllSaasForSaFromDB(EtkProject project, String saNumber, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFillWithMultiLangValueForAllLanguagesAndJoin(project, null, FIELD_DS_DESC, new String[]{ FIELD_DS_SAA },
                                                                  new String[]{ saNumber + "*" }, false, null, false, true);
    }

    private void loadAllSaasForSourceFromDB(EtkProject project, String source, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_SAA, new String[]{ FIELD_DS_SOURCE }, new String[]{ source }, new String[]{ FIELD_DS_SAA },
                          DBDataObjectList.LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataSaa getNewDataObject(EtkProject project) {
        return new iPartsDataSaa(project, null);
    }
}
