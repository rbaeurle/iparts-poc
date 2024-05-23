/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;

/**
 * DataObjectList für {@link iPartsDataFactoryModel}
 */
public class iPartsDataFactoryModelList extends EtkDataObjectList<iPartsDataFactoryModel> implements iPartsConst {

    public iPartsDataFactoryModelList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt die Daten aus DA_FACTORY_MODEL zu DFM_AGG_TYPE (sortiert nach DFM_FACTORY)
     *
     * @param project
     * @param aggType
     */
    public void loadByAggType(EtkProject project, String aggType) {
        String[] whereFields = new String[]{ FIELD_DFM_AGG_TYPE };
        String[] whereValues = new String[]{ aggType };
        super.searchSortAndFill(project, TABLE_DA_FACTORY_MODEL, whereFields, whereValues, new String[]{ FIELD_DFM_FACTORY },
                                DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Lädt die Daten aus DA_FACTORY_MODEL zum Werk sortiert nach Werk
     *
     * @param project
     * @param factory
     */
    public void loadByFactory(EtkProject project, String factory) {
        String[] whereFields = new String[]{ FIELD_DFM_FACTORY };
        String[] whereValues = new String[]{ factory };
        searchSortAndFill(project, TABLE_DA_FACTORY_MODEL, whereFields, whereValues,
                          new String[]{ FIELD_DFM_FACTORY }, LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
    }

    /**
     * Lädt alle Daten aus DA_FACTORY_MODEL unsortiert
     *
     * @param project
     */
    public void load(EtkProject project) {
        super.searchAndFill(project, TABLE_DA_FACTORY_MODEL, null, null, DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataFactoryModel getNewDataObject(EtkProject project) {
        return new iPartsDataFactoryModel(project, null);
    }
}
