/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;

/**
 * Liste von {@link iPartsDataBranchProductClass}.
 */
public class iPartsDataBranchProductClassList extends EtkDataObjectList<iPartsDataBranchProductClass> implements iPartsConst {

    public iPartsDataBranchProductClassList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataBranchProductClass}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataBranchProductClassList loadCompleteBranchProductClassMapping(EtkProject project) {
        iPartsDataBranchProductClassList list = new iPartsDataBranchProductClassList();
        list.loadCompleteBranchProductClassMappingFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataBranchProductClass}s.
     *
     * @param project
     * @param origin
     */
    private void loadCompleteBranchProductClassMappingFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_BRANCH_PC_MAPPING, null, null, DBDataObjectList.LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataBranchProductClass getNewDataObject(EtkProject project) {
        return new iPartsDataBranchProductClass(project, null);
    }
}
