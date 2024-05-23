/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;

/**
 * Liste mit {@link iPartsDataBOMConstKitText} Objekten
 */
public class iPartsDataBOMConstKitTextList extends EtkDataObjectList<iPartsDataBOMConstKitText> implements iPartsConst {

    public iPartsDataBOMConstKitTextList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataBOMConstKitText}s, die der übergebenen Teilenummer zugeordnet sind.
     *
     * @param project
     * @param partNumber
     * @return
     */
    public static iPartsDataBOMConstKitTextList loadConstKitHistoryDataForPartNumber(EtkProject project, String partNumber) {
        iPartsDataBOMConstKitTextList list = new iPartsDataBOMConstKitTextList();
        list.loadConstKitHistoryDataForPartNumberFromDB(project, partNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle Revisionen zu einer Teilenummer und der EDS Position (sortiert nach Revision)
     * Als Texttyp wird immer "V" verwendet
     *
     * @param project
     * @param upperPartNumber
     * @param position
     * @return
     */
    public static iPartsDataBOMConstKitTextList loadAllDataForUpperNumberAndPositionAndTextType(EtkProject project,
                                                                                                String upperPartNumber,
                                                                                                String textType,
                                                                                                String position) {
        iPartsDataBOMConstKitTextList list = new iPartsDataBOMConstKitTextList();
        list.loadAllDataForUpperNumberAndPositionFromDB(project, upperPartNumber, textType, position, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllDataForUpperNumberAndPositionFromDB(EtkProject project, String upperPartNumber, String textType,
                                                            String position, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DCP_SNR, FIELD_DCP_PARTPOS, FIELD_DCP_BTX_FLAG };
        String[] whereValues = new String[]{ upperPartNumber, position, textType };
        String[] sortFields = new String[]{ FIELD_DCP_REVFROM };
        searchSortAndFill(project, TABLE_DA_EDS_CONST_PROPS, whereFields, whereValues, sortFields, DBDataObjectList.LoadType.COMPLETE, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataBOMConstKitText}s, die der übergebenen Teilenummer zugeordnet sind.
     *
     * @param project
     * @param partNumber
     * @param origin
     * @return
     */
    public void loadConstKitHistoryDataForPartNumberFromDB(EtkProject project, String partNumber, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DCP_SNR };
        String[] whereValues = new String[]{ partNumber };
        String[] sortFields = new String[]{ FIELD_DCP_SNR, FIELD_DCP_REVFROM };
        searchSortAndFill(project, TABLE_DA_EDS_CONST_PROPS, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataBOMConstKitText getNewDataObject(EtkProject project) {
        return new iPartsDataBOMConstKitText(project, null);
    }
}
