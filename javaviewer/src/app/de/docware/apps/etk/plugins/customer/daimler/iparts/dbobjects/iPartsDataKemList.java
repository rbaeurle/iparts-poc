package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

public class iPartsDataKemList extends EtkDataObjectList<iPartsDataKem> implements iPartsConst {

    // Der normale Constructor.
    public iPartsDataKemList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt alle {@link iPartsDataKem} aus der DB für die angegebene KEM
     *
     * @param project
     * @param kem
     * @param origin
     */
    public iPartsDataKemList loadDataKemListForKemFromDB(EtkProject project, String kem, DBActionOrigin origin) {
        iPartsDataKemList list = new iPartsDataKemList();
        list.loadDataKemFromDB(project, kem, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataKem} aus der Datenbank
     *
     * @param project
     * @param loadType
     * @return
     */
    public static iPartsDataKemList loadDataKemList(EtkProject project, LoadType loadType) {
        iPartsDataKemList list = new iPartsDataKemList();
        list.loadDataKemFromDB(project, null, loadType, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataKem} aus der DB
     *
     * @param project
     * @param kem
     * @param loadType
     * @param origin
     */
    private void loadDataKemFromDB(EtkProject project, String kem, LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = null;
        String[] whereValues = null;
        String[] sortFields = new String[]{ FIELD_DKM_KEM, FIELD_DKM_SDA };
        if (StrUtils.isValid(kem)) {
            whereFields = new String[]{ FIELD_DKM_KEM };
            whereValues = new String[]{ kem };
        }

        searchSortAndFill(project, TABLE_DA_KEM_MASTERDATA, whereFields, whereValues, sortFields, loadType, origin);
    }

    /**
     * Neues {@link EtkDataObject} erzeugen für den Aufbau der {@link EtkDataObjectList}
     *
     * @param project
     * @return
     */
    @Override
    protected iPartsDataKem getNewDataObject(EtkProject project) {
        return new iPartsDataKem(project, null);
    }
}
