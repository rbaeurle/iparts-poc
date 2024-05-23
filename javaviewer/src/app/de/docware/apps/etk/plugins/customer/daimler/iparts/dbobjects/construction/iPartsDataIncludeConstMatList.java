/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsReplaceConstMatId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataIncludeConstMat}.
 */
public class iPartsDataIncludeConstMatList extends EtkDataObjectList<iPartsDataIncludeConstMat> implements iPartsConst {

    public iPartsDataIncludeConstMatList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Liefert alle Mitlieferteile (Include) zu einem Replace-Teil
     * sortiert nach LfdNr (FIELD_DICM_INCLUDE_PART_NO)
     *
     * @param project
     * @param replaceConstMatId
     * @return
     */
    public static iPartsDataIncludeConstMatList loadIncludeList(EtkProject project, iPartsReplaceConstMatId replaceConstMatId) {
        iPartsDataIncludeConstMatList list = new iPartsDataIncludeConstMatList();
        list.loadIncludeListFromDB(project, replaceConstMatId, DBActionOrigin.FROM_DB);
        return list;
    }


    /**
     * Liefert alle Mitlieferteile (Include) zu einem Replace-Teil
     * sortiert nach LfdNr (FIELD_DICM_INCLUDE_PART_NO)
     *
     * @param project
     * @param replaceConstMatId
     * @param origin
     */
    private void loadIncludeListFromDB(EtkProject project, iPartsReplaceConstMatId replaceConstMatId, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DICM_PART_NO, FIELD_DICM_SDATA };
        String[] whereValues = new String[]{ replaceConstMatId.getPartNo(), replaceConstMatId.getsDatA() };

        searchSortAndFill(project, TABLE_DA_INCLUDE_CONST_MAT, whereFields, whereValues, new String[]{ FIELD_DICM_INCLUDE_PART_NO },
                          LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataIncludeConstMat getNewDataObject(EtkProject project) {
        return new iPartsDataIncludeConstMat(project, null);
    }
}
