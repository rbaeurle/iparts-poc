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
 * Liste von {@link iPartsDataSpringMapping}.
 */
public class iPartsDataSpringMappingList extends EtkDataObjectList<iPartsDataSpringMapping> implements iPartsConst {

    public iPartsDataSpringMappingList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt das komplette Feder Mapping
     *
     * @param project
     * @return
     */
    public static iPartsDataSpringMappingList loadEntireMapping(EtkProject project) {
        iPartsDataSpringMappingList dataSpringMappingList = new iPartsDataSpringMappingList();
        dataSpringMappingList.loadEntireMappingFromDB(project);
        return dataSpringMappingList;
    }

    /**
     * Lädt das komplette Feder Mapping
     *
     * @param project
     */
    private void loadEntireMappingFromDB(EtkProject project) {
        super.searchAndFill(project, TABLE_DA_SPRING_MAPPING, null, null, DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataSpringMapping getNewDataObject(EtkProject project) {
        return new iPartsDataSpringMapping(project, null);
    }
}