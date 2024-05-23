/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.delete;

import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.framework.modules.db.DBBase;
import de.docware.util.date.DateUtils;

public abstract class AbstractDeleteTableHelper extends AbstractDeleteDataHelper {

    protected int rowsToDelete;

    public AbstractDeleteTableHelper(EtkProject project) {
        super(project);
    }

    protected void clear() {
        super.clear();
        rowsToDelete = -1;
    }

    public boolean doDeleteTable(String tableName) {
        clear();
        showTestModi();
        if (project.getEtkDbs().tableExists(tableName)) {
            if (getRecordsCount(tableName) > 0) {
                return deleteTable(tableName);
            } else {
                addWarning("!!Tabelle %1 ist bereits leer.", tableName);
            }
        } else {
            addError("!!Tabelle %1 existiert nicht.", tableName);
        }
        return false;
    }

    protected boolean deleteTable(String tableName) {
        boolean result;
        EtkDbs db = project.getEtkDbs();
        long startTime = System.currentTimeMillis();
        db.startTransaction();
        try {
            result = beforeDeleteTable(tableName);
            if (result) {
                // deleteTable
                result = deleteTableNow(db, tableName);
                if (result) {
                    afterDeleteTable(tableName);
                }
            }
            if (TEST_SYNTAX) {
                db.rollback();
            } else {
                db.commit();
            }
            deleteDuration = System.currentTimeMillis() - startTime;
        } catch (Exception e) {
            // exception als errors
            addException(e);
            result = catchExceptionDeletingTable(db, tableName);
            db.rollback();
        } finally {
            finallyDeleteTable(tableName);
        }
        return result;
    }

    protected int getRecordsCount(String tableName) {
        DBBase db = project.getDB();
        rowsToDelete = db.getRecordCount(tableName);
        return rowsToDelete;
    }

    protected abstract boolean beforeDeleteTable(String tableName);

    protected abstract boolean deleteTableNow(EtkDbs db, String tableName);

    protected boolean afterDeleteTable(String tableName) {
        addMessage("!!Tabelle %1: %2 Datensätze gelöscht", tableName, String.valueOf(rowsToDelete));
        return true;
    }

    protected abstract boolean catchExceptionDeletingTable(EtkDbs db, String tableName);

    protected void finallyDeleteTable(String tableName) {
        String timeDurationString = DateUtils.formatTimeDurationString(deleteDuration, true, false, getLogLanguage());
        addMessage("!!Laufzeit: %1", timeDurationString);
    }
}
