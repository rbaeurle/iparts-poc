/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Liste mit {@link iPartsDataGenericPart} Objekten
 */
public class iPartsDataGenericPartList extends EtkDataObjectList<iPartsDataGenericPart> implements iPartsConst {

    /**
     * Filtert die übergebene {@link iPartsDataGenericPartList} basierend auf dem SDatA und SDatB eines Stücklisteneintrags
     * und sortiert diese absteigend nach {@link #FIELD_DGP_SDATA}.
     *
     * @param genericPartList
     * @param partListSDatA
     * @param partListSDatB
     * @return
     */
    public static List<iPartsDataGenericPart> getSortedValidGenericPartData(List<iPartsDataGenericPart> genericPartList, String partListSDatA,
                                                                            String partListSDatB) {
        List<iPartsDataGenericPart> validGenericParts = null;
        if ((genericPartList != null) && !genericPartList.isEmpty()) {
            validGenericParts = genericPartList.stream()
                    .filter(dataGenericPart -> {
                        long partListEntrySDatADate = iPartsFactoryData.getFactoryDateFromDateString(partListSDatA, "iPartsDataGenericPartList.partListSDatA");
                        long partListEntrySDatBDate = iPartsFactoryData.getFactoryDateFromDateString(partListSDatB, "iPartsDataGenericPartList.partListSDatB");
                        if (partListEntrySDatBDate == 0) { // unendlich
                            partListEntrySDatBDate = Long.MAX_VALUE;
                        }

                        long genericPartSDatADate = iPartsFactoryData.getFactoryDateFromDateString(dataGenericPart.getFieldValue(FIELD_DGP_SDATA),
                                                                                                   "iPartsDataGenericPartList.DGP_SDATA");
                        long genericPartSDatBDate = iPartsFactoryData.getFactoryDateFromDateString(dataGenericPart.getFieldValue(FIELD_DGP_SDATB),
                                                                                                   "iPartsDataGenericPartList.DGP_SDATB");
                        if (genericPartSDatBDate == 0) { // unendlich
                            genericPartSDatBDate = Long.MAX_VALUE;
                        }

                        if (((genericPartSDatADate <= partListEntrySDatADate) && (partListEntrySDatADate < genericPartSDatBDate)) ||
                            ((partListEntrySDatADate < genericPartSDatADate) && (genericPartSDatADate < partListEntrySDatBDate))) {
                            return true;
                        }
                        return false;
                    })
                    .sorted(Comparator.comparing((iPartsDataGenericPart dataGenericPart) -> dataGenericPart.getFieldValue(FIELD_DGP_SDATA)).reversed())
                    .collect(Collectors.toList());
        }

        return validGenericParts;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataGenericPart}s, die der übergebenen {@link HmMSmId} zugeordnet sind.
     *
     * @param project
     * @param hmMSmId
     * @return
     */
    public static iPartsDataGenericPartList loadGenericPartDataForHmMSmId(EtkProject project, HmMSmId hmMSmId) {
        iPartsDataGenericPartList list = new iPartsDataGenericPartList();
        list.loadGenericPartDataForBrHmMSmFromDB(project, hmMSmId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und Lädt eine Liste aller {@link iPartsDataGenericPart}s, die dem übergebenen {@link iPartsDialogBCTEPrimaryKey}
     * zugeordnet sind absteigend sortiert nach {@link #FIELD_DGP_SDATA}.
     *
     * @param project
     * @param bcteKey
     * @param partListSDatA
     * @param partListSDatB
     * @return
     */
    public static List<iPartsDataGenericPart> loadFilterAndSortGenericPartDataForBCTEKeyFromDB(EtkProject project, iPartsDialogBCTEPrimaryKey bcteKey,
                                                                                               String partListSDatA, String partListSDatB) {
        iPartsDataGenericPartList list = new iPartsDataGenericPartList();
        list.loadGenericPartDataForBCTEKeyFromDB(project, bcteKey, DBActionOrigin.FROM_DB);
        return getSortedValidGenericPartData(list.getAsList(), partListSDatA, partListSDatB);
    }

    public iPartsDataGenericPartList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataGenericPart}s, die der übergebenen {@link HmMSmId} zugeordnet sind.
     *
     * @param project
     * @param hmMSmId
     * @param origin
     * @return
     */
    public void loadGenericPartDataForBrHmMSmFromDB(EtkProject project, HmMSmId hmMSmId, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DGP_SERIES_NO, FIELD_DGP_HM, FIELD_DGP_M, FIELD_DGP_SM };
        String[] whereValues = new String[]{ hmMSmId.getSeries(), hmMSmId.getHm(), hmMSmId.getM(), hmMSmId.getSm() };
        searchAndFill(project, TABLE_DA_GENERIC_PART, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataGenericPart}s, die dem übergebenen {@link iPartsDialogBCTEPrimaryKey} ohne SDatA
     * zugeordnet sind.
     *
     * @param project
     * @param bcteKey
     * @param origin
     * @return
     */
    public void loadGenericPartDataForBCTEKeyFromDB(EtkProject project, iPartsDialogBCTEPrimaryKey bcteKey, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DGP_SERIES_NO, FIELD_DGP_HM, FIELD_DGP_M, FIELD_DGP_SM, FIELD_DGP_POSE,
                                             FIELD_DGP_POSV, FIELD_DGP_WW, FIELD_DGP_ETZ, FIELD_DGP_AA };
        String[] whereValues = new String[]{ bcteKey.seriesNo, bcteKey.hm, bcteKey.m, bcteKey.sm, bcteKey.posE, bcteKey.posV,
                                             bcteKey.ww, bcteKey.et, bcteKey.aa };
        searchAndFill(project, TABLE_DA_GENERIC_PART, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataGenericPart getNewDataObject(EtkProject project) {
        return new iPartsDataGenericPart(project, null);
    }
}
