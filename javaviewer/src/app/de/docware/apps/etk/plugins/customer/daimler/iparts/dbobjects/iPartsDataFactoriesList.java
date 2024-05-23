/*
 * Copyright (c) 2016 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;

/**
 * Liste von {@link iPartsDataFactories}.
 */
public class iPartsDataFactoriesList extends EtkDataObjectList<iPartsDataFactories> implements iPartsConst {

    public iPartsDataFactoriesList() {
        setSearchWithoutActiveChangeSets(true);
    }

    @Override
    protected iPartsDataFactories getNewDataObject(EtkProject project) {
        return new iPartsDataFactories(project, null);
    }

    /**
     * Lädt alle Daten aus TABLE_DA_FACTORIES unsortiert
     *
     * @param project
     */
    public void load(EtkProject project) {
        super.searchAndFill(project, TABLE_DA_FACTORIES, null, null, DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Lädt alle Daten aus TABLE_DA_FACTORIES sortiert nach DA_FACTORY_NO
     *
     * @param project
     */
    public void loadSortedByFactoryNumber(EtkProject project) {
        super.searchSortAndFill(project, TABLE_DA_FACTORIES, null, null, new String[]{ FIELD_DF_FACTORY_NO },
                                DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    public iPartsDataFactories get(iPartsFactoriesId factoriesId) {
        for (iPartsDataFactories dataFactories : getAsList()) {
            if (dataFactories.getAsId().equals(factoriesId)) {
                return dataFactories;
            }
        }
        return null;
    }
}
