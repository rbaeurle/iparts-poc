/*
 * Copyright (c) 2019 Docware GmbH
 *
 * Klasse für eine weitere Liste bm-bildende Codes aus der Tabelle [DA_MODEL_BUILDING_CODE].
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

/**
 * Liste von {@link iPartsDataModelBuildingCode}.
 */
public class iPartsDataModelBuildingCodeList extends EtkDataObjectList<iPartsDataModelBuildingCode> implements iPartsConst {

    public iPartsDataModelBuildingCodeList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Die Liste komplett aus der Datenbank laden, also, die komplette Tabelle einlesen.
     *
     * @param project
     * @return
     */
    public static iPartsDataModelBuildingCodeList load(EtkProject project) {
        iPartsDataModelBuildingCodeList list = new iPartsDataModelBuildingCodeList();
        list.loadAllModelBuildingCodesForSeries(project, null, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Die Liste für eine bestimmte Baureihe aus der Datenbank laden.
     *
     * @param project
     * @param seriesNumber
     * @return
     */
    public static iPartsDataModelBuildingCodeList loadCodesForSeries(EtkProject project, String seriesNumber) {
        iPartsDataModelBuildingCodeList list = new iPartsDataModelBuildingCodeList();
        list.loadAllModelBuildingCodesForSeries(project, seriesNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Die eigentliche Datenbankzugriffsfunktion.
     *
     * @param project
     * @param seriesNumber
     * @param origin
     */
    private void loadAllModelBuildingCodesForSeries(EtkProject project, String seriesNumber, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = null;
        String[] whereValues = null;

        // Die Where-Fields+Values setzen, falls die Baureihe gesetzt ist.
        // Ansonsten wird automatisch die komplette Tabelle eingelesen.
        if (StrUtils.isValid(seriesNumber)) {
            whereFields = new String[]{ FIELD_DMBC_SERIES_NO };
            whereValues = new String[]{ seriesNumber };
        }
        searchSortAndFill(project, TABLE_DA_MODEL_BUILDING_CODE, whereFields, whereValues,
                          new String[]{ FIELD_DMBC_SERIES_NO, FIELD_DMBC_AA, FIELD_DMBC_CODE },
                          LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataModelBuildingCode getNewDataObject(EtkProject project) {
        return new iPartsDataModelBuildingCode(project, null);
    }
}
