/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.truckworkbasket;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

/**
 * Liste von {@link iPartsDataWorkBasketCalc} für die Tabelle DA_WB_SAA_CALCULATION.
 */
public class iPartsDataWorkBasketCalcList extends EtkDataObjectList<iPartsDataWorkBasketCalc> implements iPartsConst {

    public iPartsDataWorkBasketCalcList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt den kompletten Tabelleninhalt aus DA_WB_SAA_CALCULATION
     *
     * @param project
     * @return
     */
    public static iPartsDataWorkBasketCalcList loadAllEntries(EtkProject project) {
        iPartsDataWorkBasketCalcList list = new iPartsDataWorkBasketCalcList();
        list.loadAllEntriesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt die Liste zur übergebenen Datenquelle
     *
     * @param project
     * @param source
     * @return
     */
    public static iPartsDataWorkBasketCalcList loadWorkBasketBySource(EtkProject project, iPartsImportDataOrigin source) {
        iPartsDataWorkBasketCalcList list = new iPartsDataWorkBasketCalcList();
        list.loadDataWorkBasketBySourceAndModelAndSaaFromDB(project, source, null, null, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt die Liste zu einer Datenquelle und Baumuster
     *
     * @param project
     * @param source
     * @param modelNo
     * @return
     */
    public static iPartsDataWorkBasketCalcList loadWorkBasketBySourceAndModel(EtkProject project, iPartsImportDataOrigin source, String modelNo) {
        iPartsDataWorkBasketCalcList list = new iPartsDataWorkBasketCalcList();
        list.loadDataWorkBasketBySourceAndModelAndSaaFromDB(project, source, modelNo, null, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt die Liste zur Quelle für eine SAA
     *
     * @param project
     * @param source
     * @param saaNo
     * @return
     */
    public static iPartsDataWorkBasketCalcList loadWorkBasketBySourceAndSaa(EtkProject project, iPartsImportDataOrigin source, String saaNo) {
        iPartsDataWorkBasketCalcList list = new iPartsDataWorkBasketCalcList();
        list.loadDataWorkBasketBySourceAndModelAndSaaFromDB(project, source, null, saaNo, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt die Liste für eine Quelle, Baureihe und SAA
     *
     * @param project
     * @param source
     * @param modelNo
     * @param saaNo
     * @return
     */
    public static iPartsDataWorkBasketCalcList loadWorkBasketBySourceAndModelAndSaa(EtkProject project, iPartsImportDataOrigin source, String modelNo, String saaNo) {
        iPartsDataWorkBasketCalcList list = new iPartsDataWorkBasketCalcList();
        list.loadDataWorkBasketBySourceAndModelAndSaaFromDB(project, source, modelNo, saaNo, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Das eigentliche Laden der Liste.
     * Parameter für die <null> übergeben wurde, werden nicht in die WHERE-Klausel aufgenommen.
     *
     * @param project
     * @param source
     * @param modelNo
     * @param saaNo
     * @param origin
     */
    private void loadDataWorkBasketBySourceAndModelAndSaaFromDB(EtkProject project, iPartsImportDataOrigin source, String modelNo, String saaNo, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{};
        String[] whereValues = new String[]{};

        if (source != null) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_WSC_SOURCE });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ source.getOrigin() });
        }

        if (StrUtils.isValid(modelNo)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_WSC_MODEL_NO });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ modelNo });
        }

        if (StrUtils.isValid(saaNo)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_WSC_SAA });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ saaNo });
        }

        searchSortAndFill(project, TABLE_DA_WB_SAA_CALCULATION, whereFields, whereValues, getSortFields(),
                          LoadType.COMPLETE, origin);
    }

    /**
     * Zentrale Methode, die die Sortierfelder zurückliefert.
     * Prinzipiell sollen alle in dieser Klasse zusammengestellten Ergebnislisten gleich sortiert sein.
     *
     * @return Die Sortierfelder
     */
    private String[] getSortFields() {
        return new String[]{ FIELD_WSC_SOURCE, FIELD_WSC_MODEL_NO, FIELD_WSC_SAA };
    }

    /**
     * Erzeugt und lädt eine Liste ALLER {@link iPartsDataWorkBasketCalc} aus DA_WB_SAA_CALCULATION.
     *
     * @param project Das Projekt
     */
    private void loadAllEntriesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_WB_SAA_CALCULATION, null, null, getSortFields(),
                          LoadType.COMPLETE, origin);
    }

    /**
     * Ein leeres Objekt erzeugen.
     *
     * @param project
     * @return
     */
    @Override
    protected iPartsDataWorkBasketCalc getNewDataObject(EtkProject project) {
        return new iPartsDataWorkBasketCalc(project, null);
    }
}
