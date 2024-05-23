/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.sql.TableAndFieldName;

/**
 * Liste mit {@link iPartsDataBOMPartHistory} Objekten
 */
public class iPartsDataBOMPartHistoryList extends EtkDataObjectList<iPartsDataBOMPartHistory> implements iPartsConst {

    public iPartsDataBOMPartHistoryList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataBOMPartHistory}s zu einer Teilenummer
     *
     * @param project
     * @param partNumber
     * @param partVer
     * @return
     */
    public static iPartsDataBOMPartHistoryList loadBOMHistoryDataForPartNumber(EtkProject project, String partNumber, String partVer) {
        iPartsDataBOMPartHistoryList list = new iPartsDataBOMPartHistoryList();
        list.loadBOMPartHistory(project, partNumber, partVer, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataColorTableContent}s
     *
     * @param project
     * @param partNumber
     * @param partVer
     * @param origin
     */
    public void loadBOMPartHistory(EtkProject project, String partNumber, String partVer, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_BOM_MAT_HISTORY, new String[]{ FIELD_DBMH_PART_NO, FIELD_DBMH_PART_VER }, new String[]{ partNumber, partVer },
                          new String[]{ FIELD_DBMH_PART_NO, FIELD_DBMH_PART_VER, FIELD_DBMH_REV_FROM }, LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataBOMConstKitContent}s, die der übergebenen Teilenummer zugeordnet sind.
     *
     * @param project
     * @param partNumber
     * @return
     */
    public static iPartsDataBOMPartHistoryList loadConstKitHistoryDataForPartNumber(EtkProject project, String partNumber) {
        iPartsDataBOMPartHistoryList list = new iPartsDataBOMPartHistoryList();
        list.loadConstKitHistoryDataForPartNumberFromDB(project, partNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataBOMConstKitContent}s, die der übergebenen Teilenummer zugeordnet sind.
     *
     * @param project
     * @param partNumber
     * @param origin
     * @return
     */
    public void loadConstKitHistoryDataForPartNumberFromDB(EtkProject project, String partNumber, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_BOM_MAT_HISTORY, FIELD_DBMH_PART_NO) };
        String[] whereValues = new String[]{ partNumber };
        String[] sortFields = new String[]{ FIELD_DBMH_PART_NO, FIELD_DBMH_REV_FROM };
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_BOM_MAT_HISTORY, FIELD_DBMH_REV_FROM, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_BOM_MAT_HISTORY, FIELD_DBMH_RELEASE_FROM, false, false));
        searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields, new String[]{ FIELD_DBMH_PART_NO },
                                  TABLE_DA_EDS_CONST_KIT, new String[]{ FIELD_DCK_SNR },
                                  false, false, whereFields, whereValues,
                                  false, sortFields, true);
    }

    @Override
    protected iPartsDataBOMPartHistory getNewDataObject(EtkProject project) {
        return new iPartsDataBOMPartHistory(project, null);
    }
}
