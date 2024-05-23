/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashSet;
import java.util.Set;

/**
 * Liste von {@link iPartsDataConstKitContent}.
 */
public class iPartsDataConstKitContentList extends EtkDataObjectList<iPartsDataConstKitContent> implements iPartsConst {

    public iPartsDataConstKitContentList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt alle Teilenummern von Baukästen aus {@code DA_CONST_KIT_CONTENT} unsortiert
     *
     * @param project
     */
    public static Set<String> loadAllConstKitPartNumbers(EtkProject project) {
        Set<String> constKitPartNumbers = new HashSet<>();

        FoundAttributesCallback foundAttributesCallback = new FoundAttributesCallback() {

            @Override
            public boolean createDataObjects() {
                return false;
            }

            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                constKitPartNumbers.add(attributes.getFieldValue(FIELD_DCKC_PART_NO));
                return false;
            }
        };

        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_PART_NO, false, false));

        // Wir brauchen zwar keinen Join, aber eine distinct-Abfrage und einen FoundAttributesCallback -> searchSortAndFillWithJoin() verwenden
        iPartsDataConstKitContentList dataConstKitContentList = new iPartsDataConstKitContentList();
        dataConstKitContentList.searchSortAndFillWithJoin(project, null, selectFields,
                                                          new String[]{ TableAndFieldName.make(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_SDB) },
                                                          new String[]{ "" }, false, null, false, false, true, foundAttributesCallback);

        return constKitPartNumbers;
    }

    /**
     * Lädt alle Einträge für eine PartNo
     * sortiert nach FIELD_DCKC_DCKC_POSE
     *
     * @param project
     * @param partNo
     * @return
     */
    public static iPartsDataConstKitContentList loadForPartNo(EtkProject project, String partNo, boolean allVersions) {
        iPartsDataConstKitContentList dataConstKitContentList = new iPartsDataConstKitContentList();
        dataConstKitContentList.loadForPartNoFromDB(project, partNo, allVersions, DBActionOrigin.FROM_DB);
        return dataConstKitContentList;
    }

    public static iPartsDataConstKitContentList loadForPartWithJoin(EtkProject project, String partNo,
                                                                    EtkDisplayFields selectFields, boolean allVersions) {
        iPartsDataConstKitContentList dataConstKitContentList = new iPartsDataConstKitContentList();
        dataConstKitContentList.loadForPartNoFromDBWithJoin(project, partNo, selectFields, allVersions, DBActionOrigin.FROM_DB);
        return dataConstKitContentList;
    }


    private void loadForPartNoFromDB(EtkProject project, String partNo, boolean allVersions, DBActionOrigin origin) {
        clear(DBActionOrigin.FROM_DB);
        String[] whereFields;
        String[] whereValues;
        if (allVersions) {
            whereFields = new String[]{ FIELD_DCKC_PART_NO };
            whereValues = new String[]{ partNo };
        } else {
            whereFields = new String[]{ FIELD_DCKC_PART_NO, FIELD_DCKC_SDB };
            whereValues = new String[]{ partNo, "" };
        }

        searchSortAndFill(project, TABLE_DA_CONST_KIT_CONTENT, whereFields, whereValues, new String[]{ FIELD_DCKC_DCKC_POSE },
                          LoadType.COMPLETE, origin);
    }

    private void loadForPartNoFromDBWithJoin(EtkProject project, String partNo, EtkDisplayFields selectFields,
                                             boolean allVersions, DBActionOrigin origin) {
        clear(DBActionOrigin.FROM_DB);
        if (selectFields == null) {
            selectFields = new EtkDisplayFields();
            EtkDisplayField selectField = new EtkDisplayField(TABLE_MAT, FIELD_M_MATNR, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(TABLE_MAT, FIELD_M_QUANTUNIT, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(TABLE_MAT, FIELD_M_ETKZ, false, false);
            selectFields.addFeld(selectField);

            selectField = new EtkDisplayField(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_SUB_PART_NO, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, iPartsConst.FIELD_DCKC_QUANTITY, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, iPartsConst.FIELD_DCKC_KEM_FROM, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, iPartsConst.FIELD_DCKC_SDA, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, iPartsConst.FIELD_DCKC_KEM_TO, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(iPartsConst.TABLE_DA_CONST_KIT_CONTENT, iPartsConst.FIELD_DCKC_SDB, false, false);
            selectFields.addFeld(selectField);
            selectFields.loadStandards(project.getConfig());
        }
        String[] whereTableAndFields;
        String[] whereValues;
        if (allVersions) {
            whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_PART_NO) };
            whereValues = new String[]{ partNo };
        } else {
            whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_PART_NO),
                                                TableAndFieldName.make(TABLE_DA_CONST_KIT_CONTENT, FIELD_DCKC_SDB) };
            whereValues = new String[]{ partNo, "" };
        }
        searchSortAndFillWithJoin(project, project.getDBLanguage(),
                                  selectFields,
                                  new String[]{ FIELD_DCKC_SUB_PART_NO },
                                  TABLE_MAT,
                                  new String[]{ FIELD_M_MATNR },
                                  true,
                                  false,
                                  whereTableAndFields,
                                  whereValues,
                                  false,
                                  new String[]{ FIELD_DCKC_DCKC_POSE },
                                  false);
    }

    @Override
    protected iPartsDataConstKitContent getNewDataObject(EtkProject project) {
        return new iPartsDataConstKitContent(project, null);
    }
}
