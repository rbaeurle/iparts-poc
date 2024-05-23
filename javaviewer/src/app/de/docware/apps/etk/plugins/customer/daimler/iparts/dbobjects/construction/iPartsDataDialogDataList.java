/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Liste mit {@link iPartsDataDialogData} Objekten
 */
public class iPartsDataDialogDataList extends EtkDataObjectList<iPartsDataDialogData> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDialogData}s, die dem übergebenen {@link iPartsDialogBCTEPrimaryKey} zugeordnet sind.
     *
     * @param project
     * @param primaryKey
     * @return
     */
    public static iPartsDataDialogDataList loadDataWithDialogPKsFromDB(EtkProject project, iPartsDialogBCTEPrimaryKey primaryKey) {
        iPartsDataDialogDataList list = new iPartsDataDialogDataList();
        list.loadDataWithDialogPKsFromDB(project, primaryKey, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDialogData}s, die mit der übergebenen Baureihe und Materialnummer
     * übereinstimmen, wobei nur die BCTE-Schlüssel (GUIDs) geladen werden.
     *
     * @param project
     * @param seriesNr
     * @param matNr
     * @return
     */
    public static iPartsDataDialogDataList loadBCTEKeysForSeriesAndMatNr(EtkProject project, String seriesNr, String matNr) {
        iPartsDataDialogDataList list = new iPartsDataDialogDataList();
        list.loadBCTEKeysForSeriesAndMatNr(project, seriesNr, matNr, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataDialogDataList loadBCTEKeyForLinkedFactoryDataGuid(EtkProject project, String linkedFactoryDataGuid) {
        iPartsDataDialogDataList list = new iPartsDataDialogDataList();
        list.loadBCTEKeyForLinkedFactoryDataGuid(project, linkedFactoryDataGuid, DBActionOrigin.FROM_DB);
        return list;
    }

    public static Map<String, List<String>> loadAllBCTEKeysForLinkedFactoryDataGuid(EtkProject project) {
        Map<String, List<String>> result = new HashMap<>();
        iPartsDataDialogDataList list = new iPartsDataDialogDataList();
        // Ziel-Datensätze laden
        list.loadAllBCTEKeysForLinkedFactoryDataGuid(project, DBActionOrigin.FROM_DB);
        for (iPartsDataDialogData dialogData : list) {
            // In DD_LINKED_FACTORY_DATA_GUID steht die Quell Guid
            List<String> linkedGuid = result.get(dialogData.getFieldValue(FIELD_DD_LINKED_FACTORY_DATA_GUID));
            if (linkedGuid == null) {
                linkedGuid = new ArrayList<>();
                result.put(dialogData.getFieldValue(FIELD_DD_LINKED_FACTORY_DATA_GUID), linkedGuid);
            }
            linkedGuid.add(dialogData.getAsId().getDialogGuid());
        }
        return result;
    }


    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDialogData}s, die die übergebene Materialnummer und das
     * übergebene SDATB haben. Wichtig zur bestimmen von Vorgängern bei Konstruktionsersetzungen
     *
     * @param project
     * @param matNr
     * @param sdatb
     * @return
     */
    public static iPartsDataDialogDataList loadDialogDataForMatNrAndSDATB(EtkProject project, String matNr, String sdatb) {
        iPartsDataDialogDataList list = new iPartsDataDialogDataList();
        list.loadDialogDataForMatNrAndSDATBFromDB(project, matNr, sdatb, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDialogData}s, die die übergebene Materialnummer und das
     * übergebene SDATA haben. Wichtig zur bestimmen von Nachfolgern bei Konstruktionsersetzungen
     *
     * @param project
     * @param matNr
     * @param sdata
     * @return
     */
    public static iPartsDataDialogDataList loadDialogDataForMatNrAndSDATA(EtkProject project, String matNr, String sdata) {
        iPartsDataDialogDataList list = new iPartsDataDialogDataList();
        list.loadDialogDataForMatNrAndSDATAFromDB(project, matNr, sdata, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDialogData}s, die die übergebene Materialnummer haben.
     *
     * @param project
     * @param matNr
     * @return
     */
    public static iPartsDataDialogDataList loadDialogDataForMatNr(EtkProject project, String matNr) {
        iPartsDataDialogDataList list = new iPartsDataDialogDataList();
        list.loadDialogDataForMatNr(project, matNr, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Überprüft, ob der übergebene iPartsDialogBCTEPrimaryKey in der DB existiert
     *
     * @param project
     * @param primaryKey
     * @return
     */
    public static boolean existsInDB(EtkProject project, iPartsDialogBCTEPrimaryKey primaryKey) {
        iPartsDataDialogDataList list = loadDataWithDialogPKsFromDB(project, primaryKey);
        return list.size() == 1;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataDialogData}s, die dem übergebenen {@link iPartsDialogBCTEPrimaryKey} zugeordnet sind.
     *
     * @param project
     * @param primaryKey
     * @param origin
     * @return
     */
    public void loadDataWithDialogPKsFromDB(EtkProject project, iPartsDialogBCTEPrimaryKey primaryKey, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DD_SERIES_NO, FIELD_DD_HM, FIELD_DD_M, FIELD_DD_SM,
                                             FIELD_DD_POSE, FIELD_DD_POSV, FIELD_DD_WW, FIELD_DD_ETZ,
                                             FIELD_DD_AA, FIELD_DD_SDATA };
        String[] whereValues = new String[]{ primaryKey.seriesNo, primaryKey.hm, primaryKey.m, primaryKey.sm,
                                             primaryKey.posE, primaryKey.posV, primaryKey.ww, primaryKey.et,
                                             primaryKey.aa, primaryKey.sData };
        searchAndFill(project, TABLE_DA_DIALOG, whereFields, whereValues, LoadType.ONLY_IDS, origin);
    }

    public void loadBCTEKeyForLinkedFactoryDataGuid(EtkProject project, String linkedFactoryGuid, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DD_LINKED_FACTORY_DATA_GUID };
        String[] whereValues = new String[]{ linkedFactoryGuid };

        searchAndFill(project, TABLE_DA_DIALOG, whereFields, whereValues, LoadType.ONLY_IDS, origin);
    }

    private void loadAllBCTEKeysForLinkedFactoryDataGuid(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        searchSortAndFill(project, TABLE_DA_DIALOG,
                          null, null,
                          new String[]{ FIELD_DD_LINKED_FACTORY_DATA_GUID }, new String[]{ "" },
                          null,
                          LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDialogData}s, die mit der übergebenen Baureihe und Materialnummer
     * übereinstimmen, wobei nur die BCTE-Schlüssel (GUIDs) geladen werden.
     *
     * @param project
     * @param seriesNr
     * @param matNr
     * @param origin
     * @return
     */
    public void loadBCTEKeysForSeriesAndMatNr(EtkProject project, String seriesNr, String matNr, DBActionOrigin origin) {
        clear(origin);
        String[] selectFields = new String[]{ FIELD_DD_GUID };
        String[] whereFields = new String[]{ FIELD_DD_SERIES_NO, FIELD_DD_PARTNO };
        String[] whereValues = new String[]{ seriesNr, matNr };
        searchSortAndFill(project, TABLE_DA_DIALOG, selectFields, whereFields, whereValues, null, null, null, LoadType.COMPLETE,
                          false, origin);
    }

    public void loadDialogDataForMatNrAndSDATBFromDB(EtkProject project, String matNr, String sdatb, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DD_PARTNO, FIELD_DD_SDATB };
        String[] whereValues = new String[]{ matNr, sdatb };
        searchAndFill(project, TABLE_DA_DIALOG, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    public void loadDialogDataForMatNrAndSDATAFromDB(EtkProject project, String matNr, String sdata, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DD_PARTNO, FIELD_DD_SDATA };
        String[] whereValues = new String[]{ matNr, sdata };
        searchAndFill(project, TABLE_DA_DIALOG, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    public void loadDialogDataForMatNr(EtkProject project, String matNr, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DD_PARTNO };
        String[] whereValues = new String[]{ matNr };
        searchAndFill(project, TABLE_DA_DIALOG, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataDialogData getNewDataObject(EtkProject project) {
        return new iPartsDataDialogData(project, null);
    }
}
