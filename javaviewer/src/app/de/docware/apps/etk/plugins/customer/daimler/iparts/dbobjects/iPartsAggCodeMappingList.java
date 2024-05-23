/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataAggCodeMapping}.
 */
public class iPartsAggCodeMappingList extends EtkDataObjectList<iPartsDataAggCodeMapping> implements iPartsConst {

    public iPartsAggCodeMappingList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataAggCodeMapping}s.
     *
     * @param project
     * @return
     */
    public static iPartsAggCodeMappingList loadAllCodesForPartNoList(EtkProject project) {
        iPartsAggCodeMappingList list = new iPartsAggCodeMappingList();
        list.loadAllCodesForPartNoFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsAggCodeMappingList loadCodesForZBAggPartNo(EtkProject project, String aggPartNo) {
        iPartsAggCodeMappingList list = new iPartsAggCodeMappingList();
        list.loadCodesForPartNo(project, aggPartNo, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadCodesForPartNo(EtkProject project, String partNo, DBActionOrigin origin) {
        clear(origin);

        searchAndFill(project, TABLE_DA_AGG_PART_CODES, new String[]{ FIELD_DAPC_PART_NO }, new String[]{ partNo },
                      LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataAggCodeMapping}s.
     *
     * @param project
     * @param origin
     */
    private void loadAllCodesForPartNoFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_AGG_PART_CODES, null, null, LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataAggCodeMapping getNewDataObject(EtkProject project) {
        return new iPartsDataAggCodeMapping(project, null);
    }
}
