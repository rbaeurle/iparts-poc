/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;


import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataKemResponseList extends EtkDataObjectList<iPartsDataKemResponse> implements iPartsConst {

    // Der normale Constructor.
    public iPartsDataKemResponseList() {
        setSearchWithoutActiveChangeSets(true);
    }


    /**
     * Lädt eine Liste von {@link iPartsDataKemResponse} aus der Datenbank,
     * passend zur KEM
     *
     * @param project
     * @param kemNo
     * @return
     */
    public static iPartsDataKemResponseList loadKemResponseListForKemFromDB(EtkProject project, String kemNo) {
        iPartsDataKemResponseList list = new iPartsDataKemResponseList();
        list.loadKemResponseListForKemFromDB(project, kemNo, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Überprüft, ob es zur KEM einen Eintrag in DA_KEM_RESPONSE_DATA gibt
     *
     * @param project
     * @param kemNo
     * @return
     */
    public static boolean existsEntryForKem(EtkProject project, String kemNo) {
        iPartsDataKemResponseList list = new iPartsDataKemResponseList();
        list.loadKemResponseListForKemFromDB(project, kemNo, LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
        return !list.isEmpty();
    }

    /**
     * Erzeugt und lädt eine Liste von {@link iPartsDataKemResponse} aus der Datenbank,
     * passend zur Factory-ID
     *
     * @param project
     * @param factoryId
     * @return
     */
    public static iPartsDataKemResponseList loadKemResponseListForFactoryFromDB(EtkProject project, String factoryId) {
        iPartsDataKemResponseList list = new iPartsDataKemResponseList();
        list.loadKemResponseListForFactoryFromDB(project, factoryId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataKemResponse} aus der Datenbank
     *
     * @param project
     * @return
     */
    public static iPartsDataKemResponseList loadAllEntriesFromDB(EtkProject project) {
        iPartsDataKemResponseList list = new iPartsDataKemResponseList();
        list.loadAllEntriesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste von {@link iPartsDataKemResponse} aus der Datenbank,
     * passend zur KEM
     *
     * @param project
     * @param kemNo
     * @param origin
     * @return
     */
    private void loadKemResponseListForKemFromDB(EtkProject project, String kemNo, LoadType type, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_KRD_KEM };
        String[] whereValues = new String[]{ kemNo };

        searchSortAndFill(project, TABLE_DA_KEM_RESPONSE_DATA, whereFields, whereValues, getSortFields(),
                          type, origin);
    }

    /**
     * Lädt eine Liste von {@link iPartsDataKemResponse} aus der Datenbank,
     * passend zur Factory-ID
     *
     * @param project
     * @param factoryId
     * @param origin
     * @return
     */
    private void loadKemResponseListForFactoryFromDB(EtkProject project, String factoryId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_KRD_FACTORY };
        String[] whereValues = new String[]{ factoryId };

        searchSortAndFill(project, TABLE_DA_KEM_RESPONSE_DATA, whereFields, whereValues, getSortFields(),
                          LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataKemResponse} aus der Datenbank
     *
     * @param project
     * @param origin
     * @return
     */
    private void loadAllEntriesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        searchSortAndFill(project, TABLE_DA_KEM_RESPONSE_DATA, null, null, getSortFields(),
                          LoadType.COMPLETE, origin);
    }

    /**
     * Liefert die Liste der Sortierfelder zum Lesen aus der Datebank für dieses Objekt.
     *
     * @return
     */
    private String[] getSortFields() {
        return new String[]{ FIELD_KRD_FACTORY, FIELD_KRD_KEM, FIELD_KRD_FIN, FIELD_KRD_FIN_DATE };
    }

    /**
     * Neues {@link EtkDataObject} erzeugen für den Aufbau der {@link EtkDataObjectList}
     *
     * @param project
     * @return
     */
    @Override
    protected iPartsDataKemResponse getNewDataObject(EtkProject project) {
        return new iPartsDataKemResponse(project, null);
    }

}
