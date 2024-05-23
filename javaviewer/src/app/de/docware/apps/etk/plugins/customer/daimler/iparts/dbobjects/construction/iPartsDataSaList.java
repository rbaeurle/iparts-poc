/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste mit {@link iPartsDataSa} Objekten
 */
public class iPartsDataSaList extends EtkDataObjectList<iPartsDataSa> implements iPartsConst {

    /**
     * Lädt eine Liste aller {@link iPartsDataSa}s (nur die SA-Nummern), deren SA-Nummer mit dem übergebenen String anfängt.
     *
     * @param project
     * @return
     */
    public static iPartsDataSaList loadAllSAsStartingWith(EtkProject project, String searchString) {
        iPartsDataSaList list = new iPartsDataSaList();
        list.loadAllSAsStartingWithFromDB(project, searchString, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllSAsStartingWithFromDB(EtkProject project, String saNumber, DBActionOrigin origin) {
        clear(origin);
        searchWithWildCardsSortAndFill(project, new String[]{ FIELD_DS_SA },
                                       new String[]{ saNumber + "*" },
                                       new String[]{ FIELD_DS_SA },
                                       LoadType.ONLY_IDS,
                                       origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataSa}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataSaList loadAllSAs(EtkProject project) {
        iPartsDataSaList list = new iPartsDataSaList();
        list.loadAllSAsFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllSAsFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFillWithMultiLangValueForAllLanguages(project, null, FIELD_DS_DESC, null, null,
                                                           false, null, false);
    }


    @Override
    protected iPartsDataSa getNewDataObject(EtkProject project) {
        return new iPartsDataSa(project, null);
    }
}
