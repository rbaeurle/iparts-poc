/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets;

import de.docware.apps.etk.base.db.ChangeSetId;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataConfirmChanges}s.
 */
public class iPartsDataConfirmChangesList extends EtkDataObjectList<iPartsDataConfirmChanges> implements iPartsConst {

    public iPartsDataConfirmChangesList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt alle Änderungs-Bestätigungen für die übergebene ChangeSet-ID (optional nur unbestätigte Datensätze).
     *
     * @param project
     * @param changeSetId
     * @param onlyOpenConfirmations Sollen nur unbestätigte Datensätze geladen werden?
     * @return
     */
    public static iPartsDataConfirmChangesList loadConfirmChangesForChangeSet(EtkProject project, ChangeSetId changeSetId,
                                                                              boolean onlyOpenConfirmations) {
        iPartsDataConfirmChangesList list = new iPartsDataConfirmChangesList();
        list.loadForConfirmChangesForChangeSet(project, changeSetId, onlyOpenConfirmations);
        return list;
    }

    /**
     * Lädt alle Änderungs-Bestätigungen für die übergebene ChangeSet-ID und {@link AssemblyId} (optional nur unbestätigte
     * Datensätze).
     *
     * @param project
     * @param changeSetId
     * @param assemblyId
     * @param onlyOpenConfirmations Sollen nur unbestätigte Datensätze geladen werden?
     * @return
     */
    public static iPartsDataConfirmChangesList loadConfirmChangesForChangeSetAndAssemblyId(EtkProject project, ChangeSetId changeSetId,
                                                                                           AssemblyId assemblyId, boolean onlyOpenConfirmations) {
        iPartsDataConfirmChangesList list = new iPartsDataConfirmChangesList();
        list.loadForConfirmChangesForChangeSetAndAssemblyId(project, changeSetId, assemblyId, onlyOpenConfirmations);
        return list;
    }

    private void loadForConfirmChangesForChangeSet(EtkProject project, ChangeSetId changeSetId, boolean onlyOpenConfirmations) {
        clear(DBActionOrigin.FROM_DB);
        String[] whereFields;
        String[] whereValues;
        if (onlyOpenConfirmations) {
            whereFields = new String[]{ FIELD_DCC_CHANGE_SET_ID, FIELD_DCC_CONFIRMATION_USER };
            whereValues = new String[]{ changeSetId.getGUID(), "" };
        } else {
            whereFields = new String[]{ FIELD_DCC_CHANGE_SET_ID };
            whereValues = new String[]{ changeSetId.getGUID() };
        }
        searchSortAndFill(project, TABLE_DA_CONFIRM_CHANGES, whereFields, whereValues,
                          new String[]{ FIELD_DCC_DO_TYPE, FIELD_DCC_PARTLIST_ENTRY_ID, FIELD_DCC_DO_ID },
                          LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    private void loadForConfirmChangesForChangeSetAndAssemblyId(EtkProject project, ChangeSetId changeSetId, AssemblyId assemblyId,
                                                                boolean onlyOpenConfirmations) {
        clear(DBActionOrigin.FROM_DB);
        String[] whereFields;
        String[] whereValues;
        if (onlyOpenConfirmations) {
            whereFields = new String[]{ FIELD_DCC_CHANGE_SET_ID, FIELD_DCC_PARTLIST_ENTRY_ID, FIELD_DCC_CONFIRMATION_USER };
            whereValues = new String[]{ changeSetId.getGUID(), assemblyId.toDBString() + "*", "" };
        } else {
            whereFields = new String[]{ FIELD_DCC_CHANGE_SET_ID, FIELD_DCC_PARTLIST_ENTRY_ID };
            whereValues = new String[]{ changeSetId.getGUID(), assemblyId.toDBString() + "*" };
        }
        searchSortAndFillWithLike(project, TABLE_DA_CONFIRM_CHANGES, null, whereFields, whereValues,
                                  new String[]{ FIELD_DCC_DO_TYPE, FIELD_DCC_PARTLIST_ENTRY_ID, FIELD_DCC_DO_ID }, false,
                                  LoadType.COMPLETE, false, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataConfirmChanges getNewDataObject(EtkProject project) {
        return new iPartsDataConfirmChanges(project, null);
    }
}