/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataOPSScope}.
 */
public class iPartsDataOPSScopeList extends EtkDataObjectList<iPartsDataOPSScope> implements iPartsConst {

    public iPartsDataOPSScopeList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt alle {@link iPartsDataOPSScope}s inkl. der Benennung für alle Sprachen
     *
     * @param project
     * @return
     */
    public static iPartsDataOPSScopeList loadAllOPSScopes(EtkProject project) {
        iPartsDataOPSScopeList list = new iPartsDataOPSScopeList();
        list.loadAllScopesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllScopesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        String[] sortFields = new String[]{ FIELD_DOS_SCOPE };
        searchSortAndFillWithMultiLangValueForAllLanguages(project, null, FIELD_DOS_DESC, null, null, false, sortFields, false);
    }

    @Override
    protected iPartsDataOPSScope getNewDataObject(EtkProject project) {
        return new iPartsDataOPSScope(project, null);
    }

}
