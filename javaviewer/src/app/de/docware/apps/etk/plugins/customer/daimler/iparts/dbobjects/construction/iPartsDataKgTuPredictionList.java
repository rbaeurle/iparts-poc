/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributesList;

public class iPartsDataKgTuPredictionList extends EtkDataObjectList<iPartsDataKgTuPrediction> implements iPartsConst {

    public iPartsDataKgTuPredictionList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt die komplette Tabelle in eine ObjectList.
     *
     * @param project
     */
    public void load(EtkProject project) {
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_HMMSM_KGTU);
        fillAndAddDataObjectsFromAttributesList(project, attributesList, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Liefert alle Mappingschlüssel zu einer Baureihe mit HM+M+SM zurück.
     *
     * @param project
     * @param hmmsmWithSeries
     * @return
     */
    public static iPartsDataKgTuPredictionList loadListForHmMSmWithSeries(EtkProject project, String hmmsmWithSeries) {
        iPartsDataKgTuPredictionList list = new iPartsDataKgTuPredictionList();
        list.loadListForHmMSmWithSeriesFromDB(project, hmmsmWithSeries, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Liest alle Mappingschlüssel zu einer Baureihe mit HM+M+SM sortiert nach dem BCTE-Schlüssel aus der Datenbank.
     *
     * @param project
     * @param hmmsmWithSeries
     * @param origin
     */
    private void loadListForHmMSmWithSeriesFromDB(EtkProject project, String hmmsmWithSeries, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DHK_BR_HMMSM };
        String[] whereValues = new String[]{ hmmsmWithSeries };
        String[] sortFields = new String[]{ FIELD_DHK_BCTE };

        searchSortAndFill(project, TABLE_DA_HMMSM_KGTU, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataKgTuPrediction getNewDataObject(EtkProject project) {
        return new iPartsDataKgTuPrediction(project, null);
    }
}
