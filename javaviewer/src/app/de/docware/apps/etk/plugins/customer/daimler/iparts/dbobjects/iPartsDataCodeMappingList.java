/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataCodeMapping}.
 */
public class iPartsDataCodeMappingList extends EtkDataObjectList<iPartsDataCodeMapping> implements iPartsConst {

    public iPartsDataCodeMappingList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataCodeMapping}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataCodeMappingList loadAllCodeMappings(EtkProject project) {
        iPartsDataCodeMappingList list = new iPartsDataCodeMappingList();
        list.loadAllCodeMappingsFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataCodeMappingList loadCodeMappingListForModelType(EtkProject project, String modelType) {
        iPartsDataCodeMappingList list = new iPartsDataCodeMappingList();
        list.loadCodeMapListForModelType(project, modelType, DBActionOrigin.FROM_DB);
        return list;
    }


    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataAggCodeMapping}s.
     *
     * @param project
     * @param origin
     */
    private void loadAllCodeMappingsFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_CODE_MAPPING, null, null, LoadType.COMPLETE, origin);
    }

    private void loadCodeMapListForModelType(EtkProject project, String modelType, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_CODE_MAPPING, new String[]{ FIELD_DCM_MODEL_TYPE_ID }, new String[]{ modelType },
                      LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataCodeMapping getNewDataObject(EtkProject project) {
        return new iPartsDataCodeMapping(project, null);
    }
}
