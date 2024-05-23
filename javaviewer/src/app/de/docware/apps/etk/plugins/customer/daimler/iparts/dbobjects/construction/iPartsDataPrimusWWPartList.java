/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von Primus-Wahlweise-Teilen {@link iPartsDataPrimusWWPart}.
 */
public class iPartsDataPrimusWWPartList extends EtkDataObjectList<iPartsDataPrimusWWPart> implements iPartsConst {

    /**
     * Primus-Wahlweise-Teile können nicht vom Autor geändert werden, also ChangeSets nicht berücksichtigen.
     */
    public iPartsDataPrimusWWPartList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * lädt alle Primus-Wahlweise-Teile.
     *
     * @param project
     * @return
     */
    public static iPartsDataPrimusWWPartList loadAllPrimusWWParts(EtkProject project) {
        iPartsDataPrimusWWPartList list = new iPartsDataPrimusWWPartList();
        list.loadAllPrimusWWPartsFromDB(project);
        return list;
    }

    /**
     * lädt alle Primus-Wahlweise-Teile für die übergebene führende Teilenummer.
     *
     * @param leadingPartNo
     * @param project
     * @return
     */
    public static iPartsDataPrimusWWPartList loadPrimusWWPartsForLeadingPart(String leadingPartNo, EtkProject project) {
        iPartsDataPrimusWWPartList list = new iPartsDataPrimusWWPartList();
        list.loadPrimusWWPartsForLeadingPartFromDB(leadingPartNo, project);
        return list;
    }

    /**
     * lädt alle Primus-Wahlweise-Teile für die übergebene führende Teilenummer.
     *
     * @param leadingPartNo
     * @param wwID
     * @param project
     * @return
     */
    public static iPartsDataPrimusWWPartList loadPrimusWWPartsForLeadingPartAndWWID(String leadingPartNo, String wwID, EtkProject project) {
        iPartsDataPrimusWWPartList list = new iPartsDataPrimusWWPartList();
        list.loadPrimusWWPartsForLeadingPartAndWWIDFromDB(leadingPartNo, wwID, project);
        return list;
    }

    private void loadAllPrimusWWPartsFromDB(EtkProject project) {
        clear(DBActionOrigin.FROM_DB);

        searchSortAndFill(project, TABLE_DA_PRIMUS_WW_PART, null, null, new String[]{ FIELD_PWP_PART_NO, FIELD_PWP_WW_PART_NO },
                          LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    private void loadPrimusWWPartsForLeadingPartFromDB(String leadingPartNo, EtkProject project) {
        clear(DBActionOrigin.FROM_DB);

        searchSortAndFill(project, TABLE_DA_PRIMUS_WW_PART, new String[]{ FIELD_PWP_PART_NO }, new String[]{ leadingPartNo },
                          new String[]{ FIELD_PWP_WW_PART_NO }, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    private void loadPrimusWWPartsForLeadingPartAndWWIDFromDB(String leadingPartNo, String wwID, EtkProject project) {
        clear(DBActionOrigin.FROM_DB);

        searchSortAndFill(project, TABLE_DA_PRIMUS_WW_PART, new String[]{ FIELD_PWP_PART_NO, FIELD_PWP_ID }, new String[]{ leadingPartNo, wwID },
                          new String[]{ FIELD_PWP_WW_PART_NO }, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataPrimusWWPart getNewDataObject(EtkProject project) {
        return new iPartsDataPrimusWWPart(project, null);
    }
}
