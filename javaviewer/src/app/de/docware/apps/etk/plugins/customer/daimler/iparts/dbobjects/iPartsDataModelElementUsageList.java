/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Liste mit {@link iPartsDataModelElementUsage} Objekten
 */
public class iPartsDataModelElementUsageList extends EtkDataObjectList<iPartsDataModelElementUsage> implements iPartsConst {

    /**
     * Lädt alle Datensätze zum übergebenen Baumuster
     *
     * @param project
     * @param modelNo
     * @return
     */
    public static iPartsDataModelElementUsageList loadAllModelEntries(EtkProject project, String modelNo) {
        iPartsDataModelElementUsageList list = new iPartsDataModelElementUsageList();
        list.loadAllModelEntriesFromDB(project, modelNo);
        return list;
    }

    /**
     * Lädt alle Datensätze zu den Schlüsselattributen
     *
     * @param project
     * @param modelNo
     * @param module
     * @param subModule
     * @param pos
     * @param sortFields
     * @param descending
     * @return
     */
    public static EtkDataObjectList<? extends EtkDataObject> loadAllRevFromVariants(EtkProject project, String modelNo,
                                                                                    String module, String subModule,
                                                                                    String pos, String[] sortFields,
                                                                                    boolean descending) {
        iPartsDataModelElementUsageList list = new iPartsDataModelElementUsageList();
        list.loadAllRevFromVariantsFromDB(project, modelNo, module, subModule, pos, sortFields, descending);
        return list;

    }

    public static iPartsDataModelElementUsageList loadAllModelSaaEntries(EtkProject project, String modelNumber, String saaBkNo) {
        iPartsDataModelElementUsageList list = new iPartsDataModelElementUsageList();
        list.loadAllModelSaaEntriesFromDB(project, modelNumber, saaBkNo);
        return list;
    }

    public static iPartsDataModelElementUsageList loadAllModelSaaEntriesWithStructure(EtkProject project, String modelNumber,
                                                                                      String module, String subModule, String saaBkNo) {
        iPartsDataModelElementUsageList list = new iPartsDataModelElementUsageList();
        list.loadAllModelSaaEntriesWithStructureFromDB(project, modelNumber, module, subModule, saaBkNo);
        return list;
    }

    public static iPartsDataModelElementUsageList loadStructureEntriesForPOS(EtkProject project, String modelNo, String module,
                                                                             String subModule, String pos, boolean descending) {
        iPartsDataModelElementUsageList list = new iPartsDataModelElementUsageList();
        list.loadStructureEntriesForPOSFromDB(project, modelNo, module, subModule, pos, descending);
        return list;
    }

    private void loadStructureEntriesForPOSFromDB(EtkProject project, String modelNo, String module, String subModule, String pos,
                                                  boolean descending) {
        clear(DBActionOrigin.FROM_DB);
        String[] whereFields = new String[]{ FIELD_DMEU_MODELNO, FIELD_DMEU_MODULE, FIELD_DMEU_SUB_MODULE, FIELD_DMEU_POS };
        String[] whereValues = new String[]{ modelNo, module, subModule, pos };
        String[] sortFields = new String[]{ FIELD_DMEU_REVFROM };
        searchSortAndFill(project, TABLE_DA_MODEL_ELEMENT_USAGE, whereFields, whereValues, sortFields, LoadType.COMPLETE,
                          descending, DBActionOrigin.FROM_DB);
    }

    private void loadAllModelSaaEntriesWithStructureFromDB(EtkProject project, String modelNumber, String module, String subModule,
                                                           String saaBkNo) {
        clear(DBActionOrigin.FROM_DB);
        String[] whereFields = new String[]{ FIELD_DMEU_MODELNO, FIELD_DMEU_MODULE, FIELD_DMEU_SUB_MODULE, FIELD_DMEU_SUB_ELEMENT };
        String[] whereValues = new String[]{ modelNumber, module, subModule, saaBkNo };
        String[] sortFields = new String[]{ FIELD_DMEU_SUB_ELEMENT };
        searchSortAndFill(project, TABLE_DA_MODEL_ELEMENT_USAGE, whereFields, whereValues, sortFields, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    public static iPartsDataModelElementUsageList loadAllSaaBkEntries(EtkProject project, String saaBkNo) {
        iPartsDataModelElementUsageList list = new iPartsDataModelElementUsageList();
        list.loadAllSaaBkEntriesFromDB(project, saaBkNo);
        return list;
    }

    private void loadAllSaaBkEntriesFromDB(EtkProject project, String saaBkNo) {
        clear(DBActionOrigin.FROM_DB);
        String[] whereFields = new String[]{ FIELD_DMEU_SUB_ELEMENT };
        String[] whereValues = new String[]{ saaBkNo };
        searchSortAndFill(project, TABLE_DA_MODEL_ELEMENT_USAGE, whereFields, whereValues, null, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    private void loadAllModelSaaEntriesFromDB(EtkProject project, String modelNumber, String saaBkNo) {
        clear(DBActionOrigin.FROM_DB);
        String[] whereFields = new String[]{ FIELD_DMEU_MODELNO, FIELD_DMEU_SUB_ELEMENT };
        String[] whereValues = new String[]{ modelNumber, saaBkNo };
        String[] sortFields = new String[]{ FIELD_DMEU_SUB_ELEMENT };
        searchSortAndFill(project, TABLE_DA_MODEL_ELEMENT_USAGE, whereFields, whereValues, sortFields, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    private void loadAllRevFromVariantsFromDB(EtkProject project, String modelNo, String module, String subModule,
                                              String pos, String[] sortFields, boolean descending) {
        clear(DBActionOrigin.FROM_DB);
        List<String> whereFields = new ArrayList<>();
        List<String> whereValues = new ArrayList<>();

        addIfValid(whereFields, whereValues, FIELD_DMEU_MODELNO, modelNo);
        addIfValid(whereFields, whereValues, FIELD_DMEU_MODULE, module);
        addIfValid(whereFields, whereValues, FIELD_DMEU_SUB_MODULE, subModule);
        addIfValid(whereFields, whereValues, FIELD_DMEU_POS, pos);

        searchSortAndFill(project, TABLE_DA_MODEL_ELEMENT_USAGE, ArrayUtil.toArray(whereFields), ArrayUtil.toArray(whereValues), sortFields, LoadType.COMPLETE, descending, DBActionOrigin.FROM_DB);
    }

    private void addIfValid(List<String> whereFields, List<String> whereValues, String fieldName, String value) {
        if ((value != null) && StrUtils.isValid(fieldName)) {
            whereFields.add(fieldName);
            whereValues.add(value);
        }
    }

    private void loadAllModelEntriesFromDB(EtkProject project, String modelNo) {
        clear(DBActionOrigin.FROM_DB);
        String[] whereFields = new String[]{ FIELD_DMEU_MODELNO };
        String[] whereValues = new String[]{ modelNo };
        String[] sortFields = new String[]{ FIELD_DMEU_MODELNO, FIELD_DMEU_MODULE, FIELD_DMEU_SUB_MODULE,
                                            FIELD_DMEU_LEGACY_NUMBER, FIELD_DMEU_POS, FIELD_DMEU_REVFROM };
        searchSortAndFill(project, TABLE_DA_MODEL_ELEMENT_USAGE, whereFields, whereValues, sortFields, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataModelElementUsage getNewDataObject(EtkProject project) {
        return new iPartsDataModelElementUsage(project, null);
    }
}
