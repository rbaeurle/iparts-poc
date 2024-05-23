/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataMaterialWWFlag}.
 */
public class iPartsDataMaterialWWFlagList extends EtkDataObjectList<iPartsDataMaterialWWFlag> implements iPartsConst {

    public iPartsDataMaterialWWFlagList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt alle Daten aus TABLE_DA_EDS_MAT_WW_FLAGS sortiert
     *
     * @param project Das Projekt
     */
    public static iPartsDataMaterialWWFlagList load(EtkProject project) {
        iPartsDataMaterialWWFlagList list = new iPartsDataMaterialWWFlagList();
        list.loadFromDB(project);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataMaterialWWFlag} für eine Materialnummer und Revision aus DA_EDS_MAT_WW_FLAGS
     *
     * @param project Das Projekt
     * @param partNo  Teilenummer
     * @param revFrom Änderungsstand
     */
    public static iPartsDataMaterialWWFlagList loadAllWWFlagsForMaterialAndRevisionFromDB(EtkProject project, String partNo, String revFrom) {
        iPartsDataMaterialWWFlagList list = new iPartsDataMaterialWWFlagList();
        list.loadAllWWFlagsForMaterialAndRevisionFromDB(project, partNo, revFrom, DBActionOrigin.FROM_DB);
        return list;
    }


    /**
     * Funktion, die die Ergebnisfelder für alle Select-Statements zusammenstellt.
     *
     * @param project Das Projekt
     * @return Die Suchfelder
     */
    private EtkDisplayFields getSelectFields(EtkProject project) {
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_EDS_MAT_WW_FLAGS));
        return selectFields;
    }

    /**
     * Sortierfelder:
     * Prinzipiell sollen alle in dieser Klasse zusammengestellten Ergebnislisten gleich sortiert sein,
     * daher gibt es diese zentrale Methode, die die Sortierfelder zurückliefert.
     *
     * @return Die Sortierfelder
     */
    private String[] getSortFields() {
        return new String[]{ FIELD_DEMW_PART_NO, FIELD_DEMW_REV_FROM, FIELD_DEMW_FLAG };
    }

    /**
     * Lädt alle Daten aus TABLE_DA_EDS_MAT_WW_FLAGS sortiert
     *
     * @param project Das Projekt
     */
    public void loadFromDB(EtkProject project) {
        searchSortAndFill(project, TABLE_DA_EDS_MAT_WW_FLAGS, null, null, getSortFields(), LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataMaterialWWFlag} für eine Materialnummer und Revision aus DA_EDS_MAT_WW_FLAGS
     *
     * @param project Das Projekt
     * @param partNo  Teilenummer
     * @param revFrom Änderungsstand
     * @param origin  Die Herkunft
     */
    private void loadAllWWFlagsForMaterialAndRevisionFromDB(EtkProject project, String partNo, String revFrom, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_EDS_MAT_WW_FLAGS,
                          new String[]{ FIELD_DEMW_PART_NO, FIELD_DEMW_REV_FROM },
                          new String[]{ partNo, revFrom },
                          getSortFields(), LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataMaterialWWFlag getNewDataObject(EtkProject project) {
        return new iPartsDataMaterialWWFlag(project, null);
    }
}
