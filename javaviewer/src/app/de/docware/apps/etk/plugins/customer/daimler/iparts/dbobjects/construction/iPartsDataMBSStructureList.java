package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataMBSStructure}.
 */
public class iPartsDataMBSStructureList extends EtkDataObjectList<iPartsDataMBSStructure> implements iPartsConst {

    public iPartsDataMBSStructureList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Sucht in der Tabelle DA_STRUCTURE_MBS nach allen Datensätzen zu Baumuster absteigend sortiert nach DSM_RELEASE_FROM
     *
     * @param project
     * @param modelNo
     * @return
     */
    public static iPartsDataMBSStructureList searchLastReleaseDateFrom(EtkProject project, String modelNo) {
        iPartsDataMBSStructureList list = new iPartsDataMBSStructureList();
        list.loadAllModelEntriesSortedByReleaseDateFrom(project, modelNo, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Sucht in der Tabelle DA_STRUCTURE_MBS nach allen Datensätzen zu Baumuster und SAAs
     * in den Spalten DSM_SNR (=Baumuster) und DSM_SUB_SNR (=SAA)
     * sortiert nach den PK-Werten
     *
     * @param project
     * @param modelNo
     * @param saaNo
     * @return
     */
    public static iPartsDataMBSStructureList loadAllModelSaaEntries(EtkProject project, String modelNo, String saaNo) {
        iPartsDataMBSStructureList list = new iPartsDataMBSStructureList();
        list.loadAllModelSaaEntries(project, modelNo, saaNo, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllModelEntriesSortedByReleaseDateFrom(EtkProject project, String modelNo, DBActionOrigin origin) {
        clear(DBActionOrigin.FROM_DB);

        String[] whereFields = new String[]{ FIELD_DSM_SNR };
        String[] whereValues = new String[]{ modelNo };
        String[] sortFields = new String[]{ FIELD_DSM_RELEASE_FROM };

        searchSortAndFill(project, TABLE_DA_STRUCTURE_MBS, whereFields, whereValues, sortFields, LoadType.COMPLETE, true, origin);
    }

    private void loadAllModelSaaEntries(EtkProject project, String modelNo, String saaNo, DBActionOrigin origin) {
        clear(DBActionOrigin.FROM_DB);

        String[] whereFields = new String[]{ FIELD_DSM_SNR, FIELD_DSM_SUB_SNR };
        String[] whereValues = new String[]{ modelNo, saaNo };
        String[] sortFields = new String[]{ FIELD_DSM_SNR, FIELD_DSM_SNR_SUFFIX, FIELD_DSM_POS, FIELD_DSM_SORT, FIELD_DSM_KEM_FROM };

        searchSortAndFill(project, TABLE_DA_STRUCTURE_MBS, whereFields, whereValues, sortFields,
                          LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataMBSStructure getNewDataObject(EtkProject project) {
        return new iPartsDataMBSStructure(project, null);
    }
}
