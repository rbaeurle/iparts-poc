/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataSubModuleCategory}.
 */
public class iPartsDataSubModuleCategoryList extends EtkDataObjectList<iPartsDataSubModuleCategory> implements iPartsConst {

    public iPartsDataSubModuleCategoryList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt alle {@link iPartsDataSubModuleCategory}s inkl. der Benennung für alle Sprachen
     *
     * @param project
     * @return
     */
    public static iPartsDataSubModuleCategoryList loadAllSubModuleCategories(EtkProject project) {
        iPartsDataSubModuleCategoryList list = new iPartsDataSubModuleCategoryList();
        list.loadAllSubModuleCategoriesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllSubModuleCategoriesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        String[] sortFields = new String[]{ FIELD_DSMC_SUB_MODULE };
        searchSortAndFillWithMultiLangValueForAllLanguages(project, null, FIELD_DSMC_DESC,
                                                           null, null,
                                                           false, sortFields, false);
    }

    @Override
    protected iPartsDataSubModuleCategory getNewDataObject(EtkProject project) {
        return new iPartsDataSubModuleCategory(project, null);
    }
}
