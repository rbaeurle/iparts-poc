package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

/**
 * Liste von {@link iPartsDataSeriesExpireDate}.
 */
public class iPartsDataSeriesExpireDateList extends EtkDataObjectList<iPartsDataSeriesExpireDate> implements iPartsConst {

    public iPartsDataSeriesExpireDateList() {
        setSearchWithoutActiveChangeSets(true);
    }

    public static iPartsDataSeriesExpireDateList getAllSeriesExpireDate(EtkProject project, iPartsSeriesId seriesId) {
        iPartsDataSeriesExpireDateList list = new iPartsDataSeriesExpireDateList();
        if (project.getEtkDbs().tableExists(TABLE_DA_SERIES_EXPDATE)) {
            list.loadAllSeriesFromDB(project, seriesId, null, DBActionOrigin.FROM_DB);
        }
        return list;
    }

    public static iPartsDataSeriesExpireDateList getSeriesAAExpireDate(EtkProject project, iPartsSeriesId seriesId, String seriesAA) {
        iPartsDataSeriesExpireDateList list = new iPartsDataSeriesExpireDateList();
        if (project.getEtkDbs().tableExists(TABLE_DA_SERIES_EXPDATE)) {
            list.loadAllSeriesFromDB(project, seriesId, seriesAA, DBActionOrigin.FROM_DB);
        }
        return list;
    }


    private void loadAllSeriesFromDB(EtkProject project, iPartsSeriesId seriesId, String seriesAA, DBActionOrigin origin) {
        clear(origin);
        String whereFields[] = new String[]{ FIELD_DSED_SERIES_NO };
        String whereValues[] = new String[]{ seriesId.getSeriesNumber() };

        if (StrUtils.isValid(seriesAA)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_DSED_AA });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ seriesAA });
        }
        searchSortAndFill(project, TABLE_DA_SERIES_EXPDATE, whereFields, whereValues,
                          new String[]{ FIELD_DSED_SERIES_NO, FIELD_DSED_AA, FIELD_DSED_FACTORY_NO }, LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataSeriesExpireDate getNewDataObject(EtkProject project) {
        return new iPartsDataSeriesExpireDate(project, null);
    }
}
