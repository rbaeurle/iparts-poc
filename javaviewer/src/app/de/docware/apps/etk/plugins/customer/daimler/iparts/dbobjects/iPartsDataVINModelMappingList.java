/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataVINModelMapping}.
 */
public class iPartsDataVINModelMappingList extends EtkDataObjectList<iPartsDataVINModelMapping> implements iPartsConst {

    public iPartsDataVINModelMappingList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataVINModelMapping}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataVINModelMappingList loadCompleteMapping(EtkProject project) {
        iPartsDataVINModelMappingList list = new iPartsDataVINModelMappingList();
        list.loadCompleteVINModelMappingFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataVINModelMapping}s.
     *
     * @param project
     * @param origin
     */
    private void loadCompleteVINModelMappingFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_VIN_MODEL_MAPPING, null, null, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataVINModelMapping getNewDataObject(EtkProject project) {
        return new iPartsDataVINModelMapping(project, null);
    }
}
