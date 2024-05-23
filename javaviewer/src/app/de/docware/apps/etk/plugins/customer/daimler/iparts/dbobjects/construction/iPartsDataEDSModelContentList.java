/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

public class iPartsDataEDSModelContentList extends EtkDataObjectList<iPartsDataEDSModelContent> implements iPartsConst {

    public iPartsDataEDSModelContentList() {
        setSearchWithoutActiveChangeSets(true);
    }

    public static iPartsDataEDSModelContentList loadAllModelEntries(EtkProject project, String modelNo) {
        return loadAllMatchingEntries(project, modelNo, null, null, null, new String[]{ FIELD_EDS_MODEL_MSAAKEY });
    }

    public static iPartsDataEDSModelContentList loadAllSaaBkEntries(EtkProject project, String saaBkNo) {
        return loadAllMatchingEntries(project, null, null, null, saaBkNo, null);
    }

    public static iPartsDataEDSModelContentList loadAllModelSaaEntries(EtkProject project, String modelNo, String saaNo) {
        return loadAllMatchingEntries(project, modelNo, null, null, saaNo, new String[]{ FIELD_EDS_MODEL_MSAAKEY });
    }

    public static iPartsDataEDSModelContentList loadAllMatchingEntries(EtkProject project, String modelNo, String group,
                                                                       String scope, String saaBkNo, String[] sortFields) {
        iPartsDataEDSModelContentList list = new iPartsDataEDSModelContentList();
        list.loadEntriesFromDB(project, modelNo, group, scope, null, null, null, saaBkNo, sortFields,
                               false, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataEDSModelContentList loadAllMatchingEntriesForPOS(EtkProject project, String modelNo, String group,
                                                                             String scope, String pos, String[] sortFields, boolean descending) {
        iPartsDataEDSModelContentList list = new iPartsDataEDSModelContentList();
        list.loadEntriesFromDB(project, modelNo, group, scope, pos, null, null, null, sortFields,
                               descending, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataEDSModelContentList loadAllRevFromVariants(EtkProject project, String modelNo, String group,
                                                                       String scope, String pos, String aa,
                                                                       String[] sortFields, boolean descending) {
        iPartsDataEDSModelContentList list = new iPartsDataEDSModelContentList();
        list.loadEntriesFromDB(project, modelNo, group, scope, pos, aa, null, null, sortFields,
                               descending, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadEntriesFromDB(EtkProject project, String modelNo, String group, String scope, String pos,
                                   String aa, String steeringType, String saaBkNo, String[] sortFields,
                                   boolean descending, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields;
        String[] whereValues;
        if (StrUtils.isValid(modelNo)) {
            whereFields = new String[]{ FIELD_EDS_MODEL_MODELNO };
            whereValues = new String[]{ modelNo };
        } else {
            whereFields = new String[0];
            whereValues = new String[0];
        }

        if (StrUtils.isValid(group)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_EDS_GROUP });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ group });
        }

        if (StrUtils.isValid(scope)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_EDS_SCOPE });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ scope });
        }

        if (StrUtils.isValid(pos)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_EDS_POS });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ pos });
        }

        if (StrUtils.isValid(aa)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_EDS_MODEL_AA });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ aa });
        }

        if (StrUtils.isValid(steeringType)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_EDS_MODEL_STEERING });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ steeringType });
        }
        if (StrUtils.isValid(saaBkNo)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_EDS_MODEL_MSAAKEY });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ saaBkNo });
            sortFields = new String[]{ FIELD_EDS_MODEL_MODELNO };
        }

        searchSortAndFill(project, TABLE_DA_EDS_MODEL, whereFields, whereValues, sortFields, LoadType.COMPLETE, descending, origin);
    }

    @Override
    protected iPartsDataEDSModelContent getNewDataObject(EtkProject project) {
        return new iPartsDataEDSModelContent(project, null);
    }
}
