/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataPrimusIncludePart}.
 */
public class iPartsDataPrimusIncludePartList extends EtkDataObjectList<iPartsDataPrimusIncludePart> implements iPartsConst {

    /**
     * Primus-Ersetzungs-Hinweise und deren Mitlieferteile können nicht vom Autor geändert werden, also ChangeSets nicht
     * berücksichtigen.
     */
    public iPartsDataPrimusIncludePartList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Liefert alle Mitlieferteile zu der übergebenen Ersetzung.
     *
     * @param project
     * @param replacementId
     * @return
     */
    public static iPartsDataPrimusIncludePartList loadIncludePartsForReplacement(EtkProject project, iPartsPrimusReplacePartId replacementId) {
        iPartsDataPrimusIncludePartList list = new iPartsDataPrimusIncludePartList();
        list.loadIncludePartsForReplacementFromDB(project, replacementId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle Mitlieferteile zu der übergebenen Ersetzung aus der DB.
     *
     * @param project
     * @param replacePartId
     * @param origin
     */
    private void loadIncludePartsForReplacementFromDB(EtkProject project, iPartsPrimusReplacePartId replacePartId, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_PIP_PART_NO };
        String[] whereValues = new String[]{ replacePartId.getPartNo() };

        searchAndFill(project, TABLE_DA_PRIMUS_INCLUDE_PART, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataPrimusIncludePart getNewDataObject(EtkProject project) {
        return new iPartsDataPrimusIncludePart(project, null);
    }

}
