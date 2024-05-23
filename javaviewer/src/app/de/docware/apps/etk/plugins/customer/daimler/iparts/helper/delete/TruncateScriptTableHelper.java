/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.delete;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.util.StrUtils;

public class TruncateScriptTableHelper extends TruncateTableHelper {

    public TruncateScriptTableHelper(EtkProject project) {
        super(project);
    }

    public void setSqlString(String sqlString) {
        this.sqlString = sqlString;
    }

    @Override
    protected boolean beforeDeleteTable(String tableName) {
        if (StrUtils.isValid(sqlString)) {
            addMessage("!!Tabelle %1: Lösche partiell alle relevanten Datensätze...", tableName);
            return true;
        } else {
            addError("!!SQL-Statement ist nicht gesetzt");
        }
        return false;
    }

    @Override
    protected boolean afterDeleteTable(String tableName) {
        int allRows = rowsToDelete;
        rowsToDelete = allRows - getRecordsCount(tableName);
        return super.afterDeleteTable(tableName);
    }
}
