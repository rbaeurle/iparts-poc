/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

public class iPartsDataWorkBasketSaaStatesList extends EtkDataObjectList<iPartsDataWorkBasketSaaStates> implements iPartsConst {

    public iPartsDataWorkBasketSaaStatesList() {
        setSearchWithoutActiveChangeSets(true);
    }

    public static iPartsDataWorkBasketSaaStatesList loadAllEntries(EtkProject project) {
        iPartsDataWorkBasketSaaStatesList list = new iPartsDataWorkBasketSaaStatesList();
        list.loadAllEntriesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataWorkBasketSaaStatesList loadSaaStatesByModel(EtkProject project, String modelNo, iPartsImportDataOrigin source) {
        iPartsDataWorkBasketSaaStatesList list = new iPartsDataWorkBasketSaaStatesList();
        list.loadSaaStatesByModelAndProductAndSaaFromDB(project, modelNo, null, null, source, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadSaaStatesByModelAndProductAndSaaFromDB(EtkProject project, String modelNo, String productNo, String saaNo, iPartsImportDataOrigin source, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{};
        String[] whereValues = new String[]{};
        if (StrUtils.isValid(modelNo)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_WBS_MODEL_NO });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ modelNo });
        }
        if (StrUtils.isValid(productNo)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_WBS_PRODUCT_NO });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ productNo });
        }
        if (StrUtils.isValid(saaNo)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_WBS_SAA });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ saaNo });
        }
        if (source != null) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_WBS_SOURCE });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ source.getOrigin() });
        }
        String[] sortFields = new String[]{ FIELD_WBS_PRODUCT_NO, FIELD_WBS_SAA };

        searchSortAndFill(project, TABLE_DA_WB_SAA_STATES, whereFields, whereValues, sortFields,
                          LoadType.COMPLETE, origin);
    }

    private void loadAllEntriesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        String[] sortFields = new String[]{ FIELD_WBS_MODEL_NO, FIELD_WBS_PRODUCT_NO, FIELD_WBS_SAA };

        searchSortAndFill(project, TABLE_DA_WB_SAA_STATES, null, null, sortFields,
                          LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataWorkBasketSaaStates getNewDataObject(EtkProject project) {
        return new iPartsDataWorkBasketSaaStates(project, null);
    }

}
