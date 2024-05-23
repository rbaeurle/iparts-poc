/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste mit {@link iPartsDataSaaWWFlags} Objekten
 */
public class iPartsDataSaaWWFlagsList extends EtkDataObjectList<iPartsDataSaaWWFlags> implements iPartsConst {

    public iPartsDataSaaWWFlagsList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataSaaWWFlags}s, die der übergebenen SAA und Änderungsstand zugeordnet sind.
     *
     * @param project
     * @param saaNumber
     * @param revFrom
     * @return
     */
    public static iPartsDataSaaWWFlagsList loadAllWWFlagsForSAARevision(EtkProject project, String saaNumber, String revFrom) {
        iPartsDataSaaWWFlagsList list = new iPartsDataSaaWWFlagsList();
        list.loadWWFlagsForSaaRevFromDB(project, saaNumber, revFrom, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataSaaWWFlags}s, die der übergebenen SAA und Änderungsstand zugeordnet sind.
     *
     * @param project
     * @param saaNumber
     * @param revFrom
     * @param origin
     * @return
     */
    public void loadWWFlagsForSaaRevFromDB(EtkProject project, String saaNumber, String revFrom, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DESW_SAA, FIELD_DESW_REV_FROM };
        String[] whereValues = new String[]{ saaNumber, revFrom };
        String[] sortFields = new String[]{ FIELD_DESW_FLAG };
        searchSortAndFill(project, TABLE_DA_EDS_SAA_WW_FLAGS, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataSaaWWFlags getNewDataObject(EtkProject project) {
        return new iPartsDataSaaWWFlags(project, null);
    }

}
