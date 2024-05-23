/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataCustomProperty}s.
 */
public class iPartsDataCustomPropertyList extends EtkDataObjectList<iPartsDataCustomProperty> implements iPartsConst {

    public iPartsDataCustomPropertyList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataCustomProperty}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataCustomPropertyList loadAllCustomProperties(EtkProject project) {
        iPartsDataCustomPropertyList list = new iPartsDataCustomPropertyList();
        list.loadAllCustomPropertiesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataCustomProperty}s aus der DB.
     *
     * @param project
     * @param origin
     */
    private void loadAllCustomPropertiesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_CUSTPROP, null, null, LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataCustomProperty getNewDataObject(EtkProject project) {
        return new iPartsDataCustomProperty(project, null);
    }
}
