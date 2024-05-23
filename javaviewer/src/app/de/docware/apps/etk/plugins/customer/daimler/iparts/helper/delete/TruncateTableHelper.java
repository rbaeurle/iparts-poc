/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.delete;

import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.util.StrUtils;
import de.docware.util.sql.SQLStatement;

import java.sql.SQLException;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

public class TruncateTableHelper extends AbstractDeleteTableHelper {

    protected String sqlString;

    public TruncateTableHelper(EtkProject project) {
        super(project);
    }

    protected boolean doSQL(EtkDbs db) {
        boolean result;
        SQLStatement statement = null;

        try {
            statement = db.getDBForDomain(MAIN).getNewStatement();
            result = doSQL(statement, sqlString);
        } catch (Exception exception) {
            addException(exception);
            result = false;
        } finally {
            if (statement != null) {
                statement.release();
            }
        }
        return result;
    }

    private boolean doSQL(SQLStatement statement, String sqlStatement) throws SQLException {
        // Syntax des SQL-Statements überprüfen und anpassen
        sqlStatement = modifySqlStatement(sqlStatement);
        addMessage("!!SQL-Statement: \"%1\"", sqlStatement);
        if (!TEST_MODE) {
            int statementIndex = statement.prepareStatement(sqlStatement);
            statement.executeUpdate(statementIndex, null);
        }
        return true;
    }

    private String modifySqlStatement(String sqlStatement) {
        // Strichpunkt am Ende entfernen
        sqlStatement = StrUtils.removeLastCharacterIfCharacterIs(sqlStatement, ";");
        return sqlStatement;
    }

    @Override
    protected boolean beforeDeleteTable(String tableName) {
        addMessage("!!Tabelle %1: Lösche alle Datensätze...", tableName);
        sqlString = "truncate table " + tableName;
        return true;
    }

    @Override
    protected boolean deleteTableNow(EtkDbs db, String tableName) {
        return doSQL(db);
    }

    @Override
    protected boolean catchExceptionDeletingTable(EtkDbs db, String tableName) {
        return false;
    }
}
