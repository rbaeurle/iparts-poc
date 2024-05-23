/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataAggsMapping}.
 */
public class iPartsDataAggsMappingList extends EtkDataObjectList<iPartsDataAggsMapping> implements iPartsConst {

    public void iPartsDataAggsMappingList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt eine Liste mit {@link iPartsDataAggsMapping}s, welche zuvor aus der DB geladen wurden
     * und gibt diese zurück.
     *
     * @param project
     * @return
     */
    public static iPartsDataAggsMappingList loadAllAggTypeMappings(EtkProject project) {
        iPartsDataAggsMappingList list = new iPartsDataAggsMappingList();
        list.loadAllAggTypeMappingsFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataAggsMapping} Datensätze aus der DB.
     *
     * @param project
     * @param origin
     */
    private void loadAllAggTypeMappingsFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_AGGS_MAPPING, null, null, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataAggsMapping getNewDataObject(EtkProject project) {
        return new iPartsDataAggsMapping(project, null);
    }
}
