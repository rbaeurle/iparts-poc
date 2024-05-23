/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.sql.TableAndFieldName;

/**
 * Liste von {@link iPartsDataASPLMGroup}.
 */
public class iPartsDataASPLMGroupList extends EtkDataObjectList<iPartsDataASPLMGroup> implements iPartsConst {

    public static iPartsDataASPLMGroupList loadGroupListFilteredWithCompany(EtkProject project, String[] companies) {
        iPartsDataASPLMGroupList list = new iPartsDataASPLMGroupList();
        list.loadGroupListFilteredWithCompany(project, companies, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataASPLMGroupList loadGroupListFilteredWithSupplierNo(EtkProject project, String supplierNo) {
        iPartsDataASPLMGroupList list = new iPartsDataASPLMGroupList();
        list.loadGroupListFilteredWithSupplierNo(project, supplierNo, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt die Liste aller {@link iPartsDataASPLMGroup}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataASPLMGroupList loadCompleteGroupList(EtkProject project) {
        iPartsDataASPLMGroupList list = new iPartsDataASPLMGroupList();
        list.loadCompleteGroupsFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle Gruppen, sortiert nach Gruppen-Id
     *
     * @param project
     * @param origin
     */
    public void loadCompleteGroupsFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_UM_GROUPS, null, null, new String[]{ FIELD_DA_G_ID }, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle Gruppen gefiltert nach der Unternehmenszugehörigkeit
     * Gibt es mehrere Unternehmen, dann wird mit OR gesucht
     *
     * @param project
     * @param companies
     * @param origin
     */
    public void loadGroupListFilteredWithCompany(EtkProject project, String[] companies, DBActionOrigin origin) {
        clear(origin);
        if (companies.length == 1) {
            searchSortAndFill(project, TABLE_DA_UM_GROUPS, new String[]{ FIELD_DA_G_BRANCH }, companies, new String[]{ FIELD_DA_G_ID },
                              LoadType.COMPLETE, origin);
        } else {
            String[] whereFields = new String[companies.length];
            String whereField = TableAndFieldName.make(TABLE_DA_UM_GROUPS, FIELD_DA_G_BRANCH);
            for (int i = 0; i < companies.length; i++) {
                whereFields[i] = whereField;
            }
            searchSortAndFillWithJoin(project, null, null, null, null, null, false, false, whereFields,
                                      companies, true, new String[]{ FIELD_DA_G_ID }, false);
        }
    }

    /**
     * Lädt alle Gruppen gefiltert nach Lieferantennummer
     *
     * @param project
     * @param supplierNo
     * @param origin
     */
    public void loadGroupListFilteredWithSupplierNo(EtkProject project, String supplierNo, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_UM_GROUPS, new String[]{ FIELD_DA_G_SUPPLIER_NO }, new String[]{ supplierNo },
                          new String[]{ FIELD_DA_G_ID }, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataASPLMGroup getNewDataObject(EtkProject project) {
        return new iPartsDataASPLMGroup(project, null);
    }
}
