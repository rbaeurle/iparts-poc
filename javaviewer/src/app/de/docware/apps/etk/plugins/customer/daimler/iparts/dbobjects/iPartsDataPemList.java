/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

public class iPartsDataPemList extends EtkDataObjectList<iPartsDataPem> implements iPartsConst {

    // Der normale Constructor.
    public iPartsDataPemList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt alle {@link iPartsDataPem} aus der DB für die angegebene PEM
     *
     * @param project
     * @param pem
     * @param origin
     */
    public iPartsDataPemList loadDataPemListForPemFromDB(EtkProject project, String pem, DBActionOrigin origin) {
        iPartsDataPemList list = new iPartsDataPemList();
        list.loadDataPemFromDB(project, pem, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPem} aus der Datenbank
     *
     * @param project
     * @param loadType
     * @return
     */
    public static iPartsDataPemList loadDataPemList(EtkProject project, LoadType loadType) {
        iPartsDataPemList list = new iPartsDataPemList();
        list.loadDataPemFromDB(project, null, loadType, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataPem} aus der DB
     *
     * @param project
     * @param pem
     * @param loadType
     * @param origin
     */
    private void loadDataPemFromDB(EtkProject project, String pem, LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = null;
        String[] whereValues = null;
        String[] sortFields = new String[]{ FIELD_DPM_PEM, FIELD_DPM_FACTORY_NO };
        if (StrUtils.isValid(pem)) {
            whereFields = new String[]{ FIELD_DPM_PEM };
            whereValues = new String[]{ pem };
        }

        searchSortAndFill(project, TABLE_DA_PEM_MASTERDATA, whereFields, whereValues, sortFields, loadType, origin);
    }

    /**
     * Lädt alle {@link iPartsDataPem} Objekte zum übergebenen PEM Ursprung
     *
     * @param project
     * @param factory
     * @param pemOrigin
     * @return
     */
    public static iPartsDataPemList loadDataPemListWithPEMOrigin(EtkProject project, String factory, String pemOrigin) {
        iPartsDataPemList list = new iPartsDataPemList();
        list.loadDataForFactory(project, factory, pemOrigin, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataPem} Objekte, die keinen PEM Ursprung besitzen
     *
     * @param project
     * @param factory
     * @return
     */
    public static iPartsDataPemList loadDataPemListWithoutPEMOrigin(EtkProject project, String factory) {
        iPartsDataPemList list = new iPartsDataPemList();
        list.loadDataForFactory(project, factory, null, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadDataForFactory(EtkProject project, String factory, String pemOrigin, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DPM_FACTORY_NO, FIELD_DPM_PRODUCT_NO };
        String[] whereValues = new String[]{ factory, (pemOrigin != null) ? pemOrigin : "" };
        String[] sortFields = new String[]{ FIELD_DPM_FACTORY_NO };

        searchSortAndFill(project, TABLE_DA_PEM_MASTERDATA, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataPem} Objekte zum übergebenen Werk
     *
     * @param project
     * @param factory
     * @return
     */
    public static iPartsDataPemList loadDataPemListForEldas(EtkProject project, String factory) {
        iPartsDataPemList list = new iPartsDataPemList();
        list.loadDataForFactoryForEldas(project, factory, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadDataForFactoryForEldas(EtkProject project, String factory, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DPM_FACTORY_NO };
        String[] whereValues = new String[]{ factory };
        String[] whereNotFields = new String[]{ FIELD_DPM_PRODUCT_NO };
        String[] whereNotValues = new String[]{ "" };
        String[] sortFields = new String[]{ FIELD_DPM_FACTORY_NO };

        searchSortAndFill(project, TABLE_DA_PEM_MASTERDATA, whereFields, whereValues,
                          whereNotFields, whereNotValues, sortFields, LoadType.COMPLETE, origin);
    }


    /**
     * Neues {@link iPartsDataPem} erzeugen für den Aufbau der {@link iPartsDataPemList}
     *
     * @param project
     * @return
     */
    @Override
    protected iPartsDataPem getNewDataObject(EtkProject project) {
        return new iPartsDataPem(project, null);
    }
}
