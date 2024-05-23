package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataErrorLocationList extends EtkDataObjectList<iPartsDataErrorLocation> implements iPartsConst {

    public iPartsDataErrorLocationList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@Link iPartsDataErrorLocationData}s, die dem abgespeckten, übergebenen {@link iPartsDialogBCTEPrimaryKey}
     * und der Materialnummer zugordnet sind
     *
     * @param project
     * @param primaryKey
     * @param partNo
     * @return
     */

    public static iPartsDataErrorLocationList loadErrorLocationDataListForPKsFromDB(EtkProject project, iPartsDialogBCTEPrimaryKey primaryKey, String partNo) {
        iPartsDataErrorLocationList list = new iPartsDataErrorLocationList();
        list.loadDataWithDialogPKsAndPartNoFromDB(project, primaryKey, partNo, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@Link iPartsDataErrorLocationData}s, die an dem PartListEntry hängen
     *
     * @param project
     * @param partListEntry
     * @return
     */
    public static iPartsDataErrorLocationList loadErrorLocationDataListForPartListEntryFromDB(EtkProject project, iPartsDataPartListEntry partListEntry) {
        iPartsDataErrorLocationList list = new iPartsDataErrorLocationList();
        String guid = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
        iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(guid);
        if (bctePrimaryKey != null) {
            list.loadDataWithDialogPKsAndPartNoFromDB(project, bctePrimaryKey, partListEntry.getPart().getFieldValue(iPartsConst.FIELD_K_MATNR),
                                                      DBActionOrigin.FROM_DB);
        }
        return list;
    }

    /**
     * Lädt eine Liste aller {@Link iPartsDataErrorLocationData}s, die dem abgespeckten, übergebenen {@link iPartsDialogBCTEPrimaryKey}
     * und der Materialnummer zugordnet sind
     *
     * @param project
     * @param primaryKey
     * @param partNo
     * @param origin
     */
    public void loadDataWithDialogPKsAndPartNoFromDB(EtkProject project, iPartsDialogBCTEPrimaryKey primaryKey, String partNo, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DEL_SERIES_NO, FIELD_DEL_HM, FIELD_DEL_M, FIELD_DEL_SM,
                                             FIELD_DEL_POSE, FIELD_DEL_PARTNO, FIELD_DEL_SDB };
        String[] whereValues = new String[]{ primaryKey.seriesNo, primaryKey.hm, primaryKey.m, primaryKey.sm,
                                             primaryKey.posE, partNo, "" };
        searchAndFill(project, TABLE_DA_ERROR_LOCATION, whereFields, whereValues, LoadType.ONLY_IDS, origin);
    }

    /**
     * Lädt eine Liste aller {@Link iPartsDataErrorLocationData}s, freigegebenen Datensätze zur Baureihe
     *
     * @param project
     * @param origin
     */
    public static iPartsDataErrorLocationList loadReleasedDataForSeriesNoFromDB(EtkProject project, String seriesNo, DBActionOrigin origin) {
        iPartsDataErrorLocationList list = new iPartsDataErrorLocationList();
        String[] whereFields = new String[]{ FIELD_DEL_SERIES_NO, FIELD_DEL_SDB };
        String[] whereValues = new String[]{ seriesNo, "" };
        list.searchAndFill(project, TABLE_DA_ERROR_LOCATION, whereFields, whereValues, LoadType.COMPLETE, origin);
        return list;
    }

    @Override
    protected iPartsDataErrorLocation getNewDataObject(EtkProject project) {
        return new iPartsDataErrorLocation(project, null);
    }
}
