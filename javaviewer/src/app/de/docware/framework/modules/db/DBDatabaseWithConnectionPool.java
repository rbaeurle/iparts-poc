/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.framework.modules.db;

import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.defaultconfig.db.DBConnectionSetting;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.PerformanceLogger;
import de.docware.util.CanceledException;
import de.docware.util.sql.*;
import de.docware.util.sql.pool.ConnectionPool;
import de.docware.util.sql.pool.ConnectionPoolType;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 * Basisklasse für Datenbanken, die mit dem Connectionpool arbeiten. Eigentlich sind das alle außer H2, H2 verwendet den Connectionpool intern mit vielen Spezialitäten
 */
public abstract class DBDatabaseWithConnectionPool extends DBDatabase {

    protected volatile ConnectionPool connectionPool = null;
    protected ConnectionPoolType connectionPoolType = ConnectionPoolType.TOMCAT;  // diesen Default brauchen wir wenn die DB-Konfig. aus der DWK kommt da in diesem Fall keine Zuweisung gibt
    protected String datasourceName;
    protected int abandonedConnectionTimeout = DBConnectionSetting.ABANDONED_CONNECTION_TIMEOUT_DEFAULT; // Nach dieser Zeit werden noch aktive Verbindungen zurückgesetzt

    public DBDatabaseWithConnectionPool(ConfigBase configBase, boolean withSecurity, DBConnectionSetting dbConnectionSetting) {
        super(configBase, withSecurity, dbConnectionSetting);
    }


    /**
     * Wird NICHT verwendet wenn die DB-Konfig. aus der DWK kommt
     *
     * @param dbConnectionSetting
     */
    @Override
    public void setDbConnectionSetting(DBConnectionSetting dbConnectionSetting) {
        this.dbConnectionSetting = dbConnectionSetting;
        this.connectionPoolType = dbConnectionSetting.getConnectionPoolType();
        this.datasourceName = dbConnectionSetting.getDatasourceName();
    }

    /**
     * Ist DB-Verbindung aktiv?
     *
     * @return true, wenn aktiv
     */
    @Override
    public boolean getActive() {
        return (connectionPool != null);
    }

    @Override
    protected ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    protected DBDataSet executeSimpleQuery(String sql) {
//        Logger.log(LogChannels.APPLICATION, LogType.INFO, "executeSimpleQuery: " + sql);
        SQLQuery query = getNewQuery();
        query.initAsSimpleSQL(sql);
        return executeQuery(query, null);
    }

    protected void executeSimple(String sql) {
        Logger.log(LogChannels.APPLICATION, LogType.INFO, "executeSimple: " + sql);
        SQLStatement statement = null;
        try {
            statement = getNewStatement();
            int statementIndex = statement.prepareStatement(sql);
            statement.executeUpdate(statementIndex, null);
        } catch (Throwable e) {
            Logger.getLogger().throwRuntimeException(e);
        } finally {
            if (statement != null) {
                statement.release();
            }
        }
    }


    protected void doExecSQL(String value) {
        Logger.log(LogChannels.APPLICATION, LogType.INFO, "doExecSQL: " + value);
        assertActive();

        SQLStatement statement = null;
        try {
            statement = connectionPool.getNewStatement();
            int idx = statement.prepareStatement(value);
            statement.executeUpdate(idx, null);
        } catch (Throwable e) {
            Logger.getLogger().throwRuntimeException(e);
        } finally {
            if (statement != null) {
                statement.release();
            }
        }
    }


    private static boolean skip = false;

    /**
     * Ausführen einer Query und Rückgabe der DataSet -> entspricht Resultset
     *
     * @param query
     * @param paramList
     * @return
     */
    @Override
    protected DBDataSetBase internalExecuteQuery(SQLQuery query, SQLParameterList paramList, boolean cancelable) throws CanceledException {


        // vor einer SQL Query muss das aktuelle Batch-Statement mit entsprechendem Hinweis ausgeführt werden
        executeBatchStatement(true);

        FrameworkThread thread = null;
        Session session = Session.get();
        if (session != null) {
            thread = session.currentChildThread();
        }

        SQLStatement statement = null;
        try {
            paramList = query.convertStringConditionsToParamConditions(paramList);
            statement = getNewStatement();
            int statementIndex = statement.prepareStatement(query);
            SQLPreparedStatement preparedStatement = statement.getPreparedStatement(statementIndex);
            if (cancelable) {
                if ((thread != null)) {
                    thread.setCurrentAppCanceable(new CancelableStatement(preparedStatement));
                }
            }
            SQLResultSet result;
            if (skip) {
                try (ResultSet jdbcResultSet = new EmptyResultSet()) {
                    long durationNs = 10;
                    result =
                            new SQLResultSet(statement, preparedStatement, jdbcResultSet, SQLStatement.POSTGRES, durationNs, resultSet -> {
                                //
                            });
                }
                if (cancelable) {
                    return new DBDataSetCancelableImpl(statement, result, spaceInsteadOfEmtyString);
                } else {
                    return new DBDataSetImpl(statement, result, spaceInsteadOfEmtyString);
                }
            }
            StringBuilder param = new StringBuilder(" ");
            if (paramList != null) {
                for (int i = 0; i < paramList.size(); i++) {
                    param.append(paramList.getObject(i)).append(", ");
                }
            }
            Logger.log(LogChannels.APPLICATION, LogType.INFO, "internalExecuteQuery: " + query.toString() + ", " + param);
            PerformanceLogger.get().startTime("executeQuery");
            if (cancelable) {
                result = statement.executeQueryCancelable(statementIndex, paramList, null);
            } else {
                result = statement.executeQuery(statementIndex, paramList, null);
            }
            PerformanceLogger.get().endTime("executeQuery");

            if (cancelable) {
                // Wegen diverser Multithreadingprobleme in Oracle. Das Cancelable-Object jetzt wieder rausnehmen.
                // Das Canceln soll ein lang laufendes executeQuery abbrechbar machen und hier ist das ja schon durch.
                // Später beim durchgehen des Resultsets wird im Next der Cancel abgefragt. Theoretisch könnte ein canceln der Query
                // ein lang laufendes next() unterbrechen. Das ist aber eher theoretisch und nicht sicher, ob es überhaupt funktioniert
                if (thread != null) {
                    thread.setCurrentAppCanceable(null);
                }
            }

            if (cancelable) {
                return new DBDataSetCancelableImpl(statement, result, spaceInsteadOfEmtyString);
            } else {
                return new DBDataSetImpl(statement, result, spaceInsteadOfEmtyString);
            }
        } catch (SQLException e) {
            // Dass hier muss vor dem Release stehen! Nicht als finally!
            if (thread != null) {
                thread.setCurrentAppCanceable(null);
            }
            if (statement != null) {
                statement.release();
            }
            Logger.getLogger().throwRuntimeException(e);
            return null;
        } catch (CanceledException e) {
            // Dass hier muss vor dem Release stehen! Nicht als finally!
            if (thread != null) {
                thread.setCurrentAppCanceable(null);
            }
            if (statement != null) {
                statement.release();
            }
            throw e;
        }
    }

    @Override
    public String getDBConnectionInfoString() {
        if (connectionPool == null) {
            return "";
        }
        return connectionPool.getConnectString();
    }

    /**
     * Setzt den Timeout, nach welcher Zeit eine Verbindung, die noch in Gebrauch (busy) ist, vom Tomcat getrennt wird.
     * Sollte also höher sein als die am längsten laufenden Statements.
     * Vorsicht z.B. bei PPSync ist dies die komplette Dauer eines Tabellenimports (vermutlich wegen Transaktion)
     *
     * @param abandonedConnectionTimeout
     */
    public void setAbandonedConnectionTimeout(int abandonedConnectionTimeout) {
        this.abandonedConnectionTimeout = abandonedConnectionTimeout;
    }

    private static class EmptyResultSet implements ResultSet {

        @Override
        public boolean next() throws SQLException {
            return false;
        }

        @Override
        public void close() throws SQLException {

        }

        @Override
        public boolean wasNull() throws SQLException {
            return false;
        }

        @Override
        public String getString(int columnIndex) throws SQLException {
            return "";
        }

        @Override
        public boolean getBoolean(int columnIndex) throws SQLException {
            return false;
        }

        @Override
        public byte getByte(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public short getShort(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public int getInt(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public long getLong(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public float getFloat(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public double getDouble(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
            return null;
        }

        @Override
        public byte[] getBytes(int columnIndex) throws SQLException {
            return new byte[0];
        }

        @Override
        public Date getDate(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public InputStream getAsciiStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public InputStream getUnicodeStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public InputStream getBinaryStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            return "";
        }

        @Override
        public boolean getBoolean(String columnLabel) throws SQLException {
            return false;
        }

        @Override
        public byte getByte(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public short getShort(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public int getInt(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public long getLong(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public float getFloat(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public double getDouble(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
            return null;
        }

        @Override
        public byte[] getBytes(String columnLabel) throws SQLException {
            return new byte[0];
        }

        @Override
        public Date getDate(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public InputStream getAsciiStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public InputStream getUnicodeStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public InputStream getBinaryStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return null;
        }

        @Override
        public void clearWarnings() throws SQLException {

        }

        @Override
        public String getCursorName() throws SQLException {
            return "";
        }

        @Override
        public ResultSetMetaData getMetaData() throws SQLException {
            return null;
        }

        @Override
        public Object getObject(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Object getObject(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public int findColumn(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public Reader getCharacterStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Reader getCharacterStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public boolean isBeforeFirst() throws SQLException {
            return false;
        }

        @Override
        public boolean isAfterLast() throws SQLException {
            return false;
        }

        @Override
        public boolean isFirst() throws SQLException {
            return false;
        }

        @Override
        public boolean isLast() throws SQLException {
            return false;
        }

        @Override
        public void beforeFirst() throws SQLException {

        }

        @Override
        public void afterLast() throws SQLException {

        }

        @Override
        public boolean first() throws SQLException {
            return false;
        }

        @Override
        public boolean last() throws SQLException {
            return false;
        }

        @Override
        public int getRow() throws SQLException {
            return 0;
        }

        @Override
        public boolean absolute(int row) throws SQLException {
            return false;
        }

        @Override
        public boolean relative(int rows) throws SQLException {
            return false;
        }

        @Override
        public boolean previous() throws SQLException {
            return false;
        }

        @Override
        public void setFetchDirection(int direction) throws SQLException {

        }

        @Override
        public int getFetchDirection() throws SQLException {
            return 0;
        }

        @Override
        public void setFetchSize(int rows) throws SQLException {

        }

        @Override
        public int getFetchSize() throws SQLException {
            return 0;
        }

        @Override
        public int getType() throws SQLException {
            return 0;
        }

        @Override
        public int getConcurrency() throws SQLException {
            return 0;
        }

        @Override
        public boolean rowUpdated() throws SQLException {
            return false;
        }

        @Override
        public boolean rowInserted() throws SQLException {
            return false;
        }

        @Override
        public boolean rowDeleted() throws SQLException {
            return false;
        }

        @Override
        public void updateNull(int columnIndex) throws SQLException {

        }

        @Override
        public void updateBoolean(int columnIndex, boolean x) throws SQLException {

        }

        @Override
        public void updateByte(int columnIndex, byte x) throws SQLException {

        }

        @Override
        public void updateShort(int columnIndex, short x) throws SQLException {

        }

        @Override
        public void updateInt(int columnIndex, int x) throws SQLException {

        }

        @Override
        public void updateLong(int columnIndex, long x) throws SQLException {

        }

        @Override
        public void updateFloat(int columnIndex, float x) throws SQLException {

        }

        @Override
        public void updateDouble(int columnIndex, double x) throws SQLException {

        }

        @Override
        public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

        }

        @Override
        public void updateString(int columnIndex, String x) throws SQLException {

        }

        @Override
        public void updateBytes(int columnIndex, byte[] x) throws SQLException {

        }

        @Override
        public void updateDate(int columnIndex, Date x) throws SQLException {

        }

        @Override
        public void updateTime(int columnIndex, Time x) throws SQLException {

        }

        @Override
        public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

        }

        @Override
        public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {

        }

        @Override
        public void updateObject(int columnIndex, Object x) throws SQLException {

        }

        @Override
        public void updateNull(String columnLabel) throws SQLException {

        }

        @Override
        public void updateBoolean(String columnLabel, boolean x) throws SQLException {

        }

        @Override
        public void updateByte(String columnLabel, byte x) throws SQLException {

        }

        @Override
        public void updateShort(String columnLabel, short x) throws SQLException {

        }

        @Override
        public void updateInt(String columnLabel, int x) throws SQLException {

        }

        @Override
        public void updateLong(String columnLabel, long x) throws SQLException {

        }

        @Override
        public void updateFloat(String columnLabel, float x) throws SQLException {

        }

        @Override
        public void updateDouble(String columnLabel, double x) throws SQLException {

        }

        @Override
        public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {

        }

        @Override
        public void updateString(String columnLabel, String x) throws SQLException {

        }

        @Override
        public void updateBytes(String columnLabel, byte[] x) throws SQLException {

        }

        @Override
        public void updateDate(String columnLabel, Date x) throws SQLException {

        }

        @Override
        public void updateTime(String columnLabel, Time x) throws SQLException {

        }

        @Override
        public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {

        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {

        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {

        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {

        }

        @Override
        public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {

        }

        @Override
        public void updateObject(String columnLabel, Object x) throws SQLException {

        }

        @Override
        public void insertRow() throws SQLException {

        }

        @Override
        public void updateRow() throws SQLException {

        }

        @Override
        public void deleteRow() throws SQLException {

        }

        @Override
        public void refreshRow() throws SQLException {

        }

        @Override
        public void cancelRowUpdates() throws SQLException {

        }

        @Override
        public void moveToInsertRow() throws SQLException {

        }

        @Override
        public void moveToCurrentRow() throws SQLException {

        }

        @Override
        public Statement getStatement() throws SQLException {
            return null;
        }

        @Override
        public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
            return null;
        }

        @Override
        public Ref getRef(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Blob getBlob(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Clob getClob(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Array getArray(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
            return null;
        }

        @Override
        public Ref getRef(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Blob getBlob(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Clob getClob(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Array getArray(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Date getDate(int columnIndex, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Date getDate(String columnLabel, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(int columnIndex, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(String columnLabel, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public URL getURL(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public URL getURL(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public void updateRef(int columnIndex, Ref x) throws SQLException {

        }

        @Override
        public void updateRef(String columnLabel, Ref x) throws SQLException {

        }

        @Override
        public void updateBlob(int columnIndex, Blob x) throws SQLException {

        }

        @Override
        public void updateBlob(String columnLabel, Blob x) throws SQLException {

        }

        @Override
        public void updateClob(int columnIndex, Clob x) throws SQLException {

        }

        @Override
        public void updateClob(String columnLabel, Clob x) throws SQLException {

        }

        @Override
        public void updateArray(int columnIndex, Array x) throws SQLException {

        }

        @Override
        public void updateArray(String columnLabel, Array x) throws SQLException {

        }

        @Override
        public RowId getRowId(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public RowId getRowId(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public void updateRowId(int columnIndex, RowId x) throws SQLException {

        }

        @Override
        public void updateRowId(String columnLabel, RowId x) throws SQLException {

        }

        @Override
        public int getHoldability() throws SQLException {
            return 0;
        }

        @Override
        public boolean isClosed() throws SQLException {
            return false;
        }

        @Override
        public void updateNString(int columnIndex, String nString) throws SQLException {

        }

        @Override
        public void updateNString(String columnLabel, String nString) throws SQLException {

        }

        @Override
        public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

        }

        @Override
        public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

        }

        @Override
        public NClob getNClob(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public NClob getNClob(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public SQLXML getSQLXML(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public SQLXML getSQLXML(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

        }

        @Override
        public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

        }

        @Override
        public String getNString(int columnIndex) throws SQLException {
            return "";
        }

        @Override
        public String getNString(String columnLabel) throws SQLException {
            return "";
        }

        @Override
        public Reader getNCharacterStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Reader getNCharacterStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

        }

        @Override
        public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

        }

        @Override
        public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

        }

        @Override
        public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

        }

        @Override
        public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

        }

        @Override
        public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

        }

        @Override
        public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

        }

        @Override
        public void updateClob(int columnIndex, Reader reader) throws SQLException {

        }

        @Override
        public void updateClob(String columnLabel, Reader reader) throws SQLException {

        }

        @Override
        public void updateNClob(int columnIndex, Reader reader) throws SQLException {

        }

        @Override
        public void updateNClob(String columnLabel, Reader reader) throws SQLException {

        }

        @Override
        public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
            return null;
        }

        @Override
        public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
}