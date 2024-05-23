/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataModuleCategory}.
 */
public class iPartsDataModuleCategoryList extends EtkDataObjectList<iPartsDataModuleCategory> implements iPartsConst {

    public iPartsDataModuleCategoryList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt alle {@link iPartsDataModuleCategory}s (sortiert nach Gruppe)
     *
     * @param project
     * @return
     */
    public static iPartsDataModuleCategoryList loadAllModuleCategories(EtkProject project) {
        iPartsDataModuleCategoryList list = new iPartsDataModuleCategoryList();
        list.loadAllModuleCategoriesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllModuleCategoriesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        String[] sortFields = new String[]{ FIELD_DMC_MODULE };
        searchSortAndFillWithMultiLangValueForAllLanguages(project, null, FIELD_DMC_DESC,
                                                           null, null, false,
                                                           sortFields, false);
    }

    @Override
    protected iPartsDataModuleCategory getNewDataObject(EtkProject project) {
        return new iPartsDataModuleCategory(project, null);
    }
}
