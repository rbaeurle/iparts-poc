/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste mit {@link iPartsDataChangeSetInfoDefs} Objekten
 */
public class iPartsDataChangeSetInfoDefsList extends EtkDataObjectList<iPartsDataChangeSetInfoDefs> implements iPartsConst {

    public iPartsDataChangeSetInfoDefsList() {
        setSearchWithoutActiveChangeSets(true);
    }

    public static iPartsDataChangeSetInfoDefsList loadAllInfoDefs(EtkProject project) {
        iPartsDataChangeSetInfoDefsList list = new iPartsDataChangeSetInfoDefsList();
        list.loadAllInfoDefsFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllInfoDefsFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_CHANGE_SET_INFO_DEFS, null, null, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataChangeSetInfoDefs getNewDataObject(EtkProject project) {
        return new iPartsDataChangeSetInfoDefs(project, null);
    }
}
