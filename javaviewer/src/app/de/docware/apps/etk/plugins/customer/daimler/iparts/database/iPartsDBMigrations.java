/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.database;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.base.config.db.EtkDatabaseDescription;
import de.docware.apps.etk.base.db.*;
import de.docware.apps.etk.base.misc.EtkEndpointHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.useradmin.EtkUserAdminDbActions;
import de.docware.apps.etk.plugins.customer.daimler.iparts.application.iPartsJavaViewerApplication;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.viewer.JavaViewerPathsManager;
import de.docware.apps.etk.viewer.webapp.deploytool.forms.BaseSettingsPanel;
import de.docware.framework.combimodules.useradmin.db.UserAdminDb;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.db.*;
import de.docware.framework.modules.config.defaultconfig.db.DBConnectionSetting;
import de.docware.framework.modules.config.defaultconfig.db.DBConnectionSettings;
import de.docware.framework.modules.db.DBDatabase;
import de.docware.framework.modules.db.DBDatabaseDomain;
import de.docware.framework.modules.db.DatabaseType;
import de.docware.framework.modules.db.SQLQueryWithUndo;
import de.docware.framework.modules.gui.app.DWApplicationPathsManager;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.startparameter.StartParameter;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.gui.session.SessionManager;
import de.docware.framework.modules.gui.session.SessionType;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.file.DWReader;
import de.docware.util.os.OsUtils;
import de.docware.util.sql.SQLParameterList;
import de.docware.util.sql.SQLStatement;
import de.docware.util.sql.dbobjects.DbInternalDbObject;
import de.docware.util.xml.dom.DomWriterJDom;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class iPartsDBMigrations implements iPartsConst {

    public static final int IPARTS_MIGRATION_ERROR_EXIT_CODE = 1337;
    private static final String DEFAULT_MIGRATIONS_PATH_CLASSPATH = "de/docware/apps/etk/plugins/customer/daimler/iparts/database/db/migration";
    private static final String DEFAULT_MIGRATIONS_PATH = "/src/app/" + DEFAULT_MIGRATIONS_PATH_CLASSPATH;
    private static final double CURRENT_DB_VERSION_FOR_IPARTS = 6.9;
    private static final String VM_PARAMETER_CREATE_SINGLE_FILES = "createsinglefiles";

    private final ConfigBase config;
    private Session dbMigrationSession;
    private EtkProject dbMigrationProject;
    private DBConnectionSetting setting;
    private Flyway flyway;
    private EtkDatabaseDescription createdDatabaseDescription;
    private boolean isDWKFileWritable;
    // TODO Indizes, die es nur in der DB gibt, sollten nicht gelöscht werden, weil es gerade bei PostgreSQL
    // einige Spezial-Indizes (GIN) für Like-Abfragen gibt, für die es zumindest aktuell noch kein Gegenstück
    // in den JAVA Index-Definitionen gibt
    public boolean deleteDeprecatedIndicesInDb = false;


    public iPartsDBMigrations(ConfigBase config) {
        this.config = config;
    }

    public void initDBMigrationData(boolean skipOracleCheck) throws IOException {
        // Creating session and project for the DB migrations
        createMigrationSession();
        createMigrationProject(false);
        // Retrieving the current db connection settings
        extractDBSetting();
        // Activating the current DB connection
        activateDBConnection();
        // An Oracle database need at least Flyway 10. Flyway 10 needs at least Java 17. Since iParts is compiled with
        // Java 11 there is currently no way to migrate or validate flyway migrations via code.
        boolean doFlywayActions = skipOracleCheck || (dbMigrationProject.getEtkDbs().getDatabaseType(DBDatabaseDomain.MAIN) != DatabaseType.ORACLE);
        if (doFlywayActions) {
            // Creating the Flyway object
            initFlyway();
            // Migrating the current schema version should only be possible in dev mode. In production flyway migration is
            // triggered by an outside mechanism.
            if (Constants.DEVELOPMENT) {
                Logger.log(LogChannels.DB_MIGRATION, LogType.INFO, "Executing \"flyway migrate\" on DB connection: " + setting.getDatasourceUrl());
                flyway.migrate();
            }
            Logger.log(LogChannels.DB_MIGRATION, LogType.INFO, "Executing \"flyway info\" on DB connection: " + setting.getDatasourceUrl());
            flyway.info();
            flyway.validate();
        }

        EtkDbs etkDbs = dbMigrationProject.getEtkDbs();
        if (etkDbs != null) {
            deleteDeprecatedIndicesInDb = etkDbs.getDatabaseType(DBDatabaseDomain.MAIN) == DatabaseType.ORACLE;
        }
    }

    private void activateDBConnection() {
        dbMigrationProject.setDBActive(setting, true, true, false);
        dbMigrationProject.getEtkDbs().getDBForDomain(DBDatabaseDomain.MAIN).setAdjustFieldLengthWithCompatibleInfo(false);
    }

    public void createStructureFromDefinition() throws IOException {
        JavaViewerPathsManager.createAndSetInstance(dbMigrationProject);
        // Creates the database definition based on the current java definition
        createDBDescriptionFromDefinition();
        // Applies the current java definition to the underlying DWK
        applyDefinitionChanges();
    }

    public void createFlywayMigrations() {
        if (flyway == null) {
            Logger.log(LogChannels.DB_MIGRATION, LogType.ERROR, "Flyway is not configured correctly! Could not create flyway migrations for " + setting.getDatasourceUrl());
        }
        Logger.log(LogChannels.DB_MIGRATION, LogType.INFO, "Checking if creation of Flyway migrations is needed for DB connection: " + setting.getDatasourceUrl());
        try {
            EtkDbs etkDbs = dbMigrationProject.getEtkDbs();
            DBDatabase database = etkDbs.getDBForDomain(DBDatabaseDomain.MAIN);
            if (database == null) {
                Logger.log(LogChannels.DB_MIGRATION, LogType.ERROR, "Database is not configured correctly");
                return;
            }

            DiffResult diffsForDBForVerification = EtkDatabaseMigrationHelper.findDiffForDB(etkDbs, createdDatabaseDescription, true);
            removeMetaTables(diffsForDBForVerification);
            if (diffsForDBForVerification.hasDiff(deleteDeprecatedIndicesInDb)) {
                Logger.log(LogChannels.DB_MIGRATION, LogType.INFO, "Creating scripts for DB connection: " + setting.getDatasourceUrl());
                DWFile directory = getCurrentMigrationPath();
                int schemaVersion = extractSchemaVersion();
                int currentVersion = schemaVersion + 1;
                boolean createSingleFiles = StartParameter.getSystemPropertyBoolean(VM_PARAMETER_CREATE_SINGLE_FILES, false);
                List<String> scripts = new ArrayList<>();
                // Removing schema elements
                // TABLES
                int counter = 0;
                for (EtkDatabaseTable deprecatedTable : diffsForDBForVerification.getDeprecatedTables()) {
                    EtkDatabaseMigrationHelper.addSQLForDeleteTable(deprecatedTable, scripts, etkDbs);
                    if (createSingleFiles) {
                        DWFile migrationFile = directory.getChild("V" + currentVersion + "__DROP_TABLE_" + deprecatedTable.getName() + ".sql");
                        if (writeData(migrationFile, scripts)) {
                            counter++;
                            currentVersion++;
                        }
                        scripts.clear();
                    }
                }

                // COLUMNS
                for (Map.Entry<String, List<EtkDatabaseField>> deprecatedFields : diffsForDBForVerification.getDeprecatedFieldsMap().entrySet()) {
                    String tableName = deprecatedFields.getKey();
                    for (EtkDatabaseField deprecatedField : deprecatedFields.getValue()) {
                        EtkDatabaseMigrationHelper.addSQLForDeleteField(deprecatedField, scripts, database);
                    }
                    if (createSingleFiles) {
                        DWFile migrationFile = directory.getChild("V" + currentVersion + "__REMOVING_FIELDS_FOR_TABLE_" + tableName + ".sql");
                        if (writeData(migrationFile, scripts)) {
                            counter++;
                            currentVersion++;
                        }
                        scripts.clear();
                    }
                }

                // INDICES
                if (deleteDeprecatedIndicesInDb) {
                    for (Map.Entry<String, List<DBDatabaseIndex>> deprecatedIndicesEntry : diffsForDBForVerification.getDeprecatedIndicesMap().entrySet()) {
                        String tableName = deprecatedIndicesEntry.getKey();
                        for (DBDatabaseIndex deprecatedIndex : deprecatedIndicesEntry.getValue()) {
                            EtkDatabaseMigrationHelper.addSQLForDeleteIndex(tableName, deprecatedIndex, scripts, database);
                        }
                        if (createSingleFiles) {
                            DWFile migrationFile = directory.getChild("V" + currentVersion + "__REMOVING_INDICES_FOR_TABLE_" + tableName + ".sql");
                            if (writeData(migrationFile, scripts)) {
                                counter++;
                                currentVersion++;
                            }
                            scripts.clear();
                        }
                    }
                }

                // Adding schema elements
                // TABLES
                for (EtkDatabaseTable missingTable : diffsForDBForVerification.getMissingTables()) {
                    EtkDatabaseMigrationHelper.addSQLForAddTable(missingTable, scripts, null, etkDbs);
                    if (createSingleFiles) {
                        DWFile migrationFile = directory.getChild("V" + currentVersion + "__NEW_TABLE_" + missingTable.getName() + ".sql");
                        if (writeData(migrationFile, scripts)) {
                            counter++;
                            currentVersion++;
                        }
                        scripts.clear();
                    }
                }

                // COLUMNS
                for (Map.Entry<String, List<EtkDatabaseField>> missingFields : diffsForDBForVerification.getMissingFieldsMap().entrySet()) {
                    String tableName = missingFields.getKey();
                    for (EtkDatabaseField missingField : missingFields.getValue()) {
                        EtkDatabaseMigrationHelper.addSQLForAddField(missingField, scripts, database);
                    }
                    if (createSingleFiles) {
                        DWFile migrationFile = directory.getChild("V" + currentVersion + "__NEW_FIELDS_FOR_TABLE_" + tableName + ".sql");
                        if (writeData(migrationFile, scripts)) {
                            counter++;
                            currentVersion++;
                        }
                        scripts.clear();
                    }
                }

                // INDICES
                for (Map.Entry<String, List<DBDatabaseIndex>> missingIndicesEntry : diffsForDBForVerification.getMissingIndicesMap().entrySet()) {
                    String tableName = missingIndicesEntry.getKey();
                    for (DBDatabaseIndex missingIndex : missingIndicesEntry.getValue()) {
                        EtkDatabaseMigrationHelper.addSQLForAddIndex(tableName, missingIndex, scripts, database);
                    }
                    if (createSingleFiles) {
                        DWFile migrationFile = directory.getChild("V" + currentVersion + "__NEW_INDICES_FOR_TABLE_" + tableName + ".sql");
                        if (writeData(migrationFile, scripts)) {
                            counter++;
                            currentVersion++;
                        }
                        scripts.clear();
                    }
                }

                // Changing modified fields
                long fieldsCount = diffsForDBForVerification.getModifiedFieldsMap().values().stream()
                        .flatMap(Collection::stream)
                        .filter(ModifiedField::isModifiedDDL) // Nur die relevanten veränderten Felder berücksichtigen
                        .count();
                if (fieldsCount > 0) {
                    for (Map.Entry<String, List<ModifiedField>> modifiedFields : diffsForDBForVerification.getModifiedFieldsMap().entrySet()) {
                        String tableName = modifiedFields.getKey();
                        for (ModifiedField modifiedField : modifiedFields.getValue()) {
                            if (!modifiedField.isModifiedDDL()) {
                                continue;
                            }
                            EtkDatabaseMigrationHelper.addSQLForModifyField(modifiedField, false, scripts, database);
                        }
                        if (createSingleFiles) {
                            DWFile migrationFile = directory.getChild("V" + currentVersion + "__MODIFIED_FIELDS_FOR_TABLE_" + tableName + ".sql");
                            if (writeData(migrationFile, scripts)) {
                                counter++;
                                currentVersion++;
                            }
                            scripts.clear();
                        }
                    }
                }

                List<SQLQueryWithUndo> initialMigrations = new DwList<>();
                if (schemaVersion == 0) {
                    // DB-Version vom Standardprodukt setzen
                    String newDatabaseVersionString = String.valueOf(CURRENT_DB_VERSION_FOR_IPARTS);
                    initialMigrations = EtkDatabaseMigrationHelper.insertOrUpdateVersionInDB(TABLE_KEYVALUE, new String[]{ FIELD_KV_KEY }, FIELD_KV_VALUE,
                                                                                             new String[]{ EtkCheckDWKDBVersion.VERSION_KEY }, newDatabaseVersionString,
                                                                                             createdDatabaseDescription, initialMigrations,
                                                                                             etkDbs, database);
                    // Schema-Version für die Benutzerverwaltung setzen
                    initialMigrations = EtkDatabaseMigrationHelper.insertOrUpdateVersionInDB(DbInternalDbObject.TABLE, new String[]{ DbInternalDbObject.FIELD_SCHEMA,
                                                                                                                                     DbInternalDbObject.FIELD_KEY },
                                                                                             DbInternalDbObject.FIELD_VALUE,
                                                                                             new String[]{ UserAdminDb.SCHEMA_NAME,
                                                                                                           DbInternalDbObject.SCHEMA_VERSION_KEY },
                                                                                             UserAdminDb.CURRENT_VERSION,
                                                                                             createdDatabaseDescription, initialMigrations,
                                                                                             etkDbs, database);
                }
                if (!initialMigrations.isEmpty()) {
                    int dataBaseType = etkDbs.getDatabaseType(DBDatabaseDomain.MAIN).getIndex();
                    for (SQLQueryWithUndo sqlQuery : initialMigrations) {
                        List<Object> parameterObjects = null;
                        SQLParameterList parameterListForExecution = sqlQuery.getSqlQuery().getParameterListForExecution();
                        if (parameterListForExecution != null) {
                            parameterObjects = parameterListForExecution.getConditionParametersOrderedWithCustom(sqlQuery.getSqlQuery(), true);
                        }
                        scripts.add(SQLStatement.getSQLQuery(dataBaseType, sqlQuery.getSqlQuery().toQueryString(), parameterObjects));
                    }
                    if (createSingleFiles) {
                        DWFile migrationFile = directory.getChild("V" + currentVersion + "__INITIAL_STRUCTURE_DATA.sql");
                        if (writeData(migrationFile, scripts)) {
                            counter++;
                            currentVersion++;
                        }
                        scripts.clear();
                    }
                }

                if (!createSingleFiles) {
                    if (!scripts.isEmpty()) {
                        DWFile migrationFile = directory.getChild("V" + currentVersion + "__" + DateUtils.getCurrentDateFormatted(DateUtils.simpleTimeFormatyyyyMMddHHmmss) + "_TITLE_PLACEHOLDER.sql");
                        writeData(migrationFile, scripts);
                        Logger.log(LogChannels.DB_MIGRATION, LogType.INFO, "Single script  \"" + migrationFile.getAbsolutePath()
                                                                           + "\" created for DB connection: " + setting.getDatasourceUrl());
                    }
                } else {
                    Logger.log(LogChannels.DB_MIGRATION, LogType.INFO, counter + " scripts created in folder \""
                                                                       + directory.getAbsolutePath() + "\" for DB connection: "
                                                                       + setting.getDatasourceUrl());
                }
            } else {
                Logger.log(LogChannels.DB_MIGRATION, LogType.INFO, "No migration scripts needed for DB connection: " + setting.getDatasourceUrl());
            }
        } catch (Exception e) {
            stopApplicationWithException(e);
        }
    }

    public void validateDefinition() {
        Logger.log(LogChannels.DB_MIGRATION, LogType.INFO, "Validating database definition for DB connection: " + setting.getDatasourceUrl());
        try {
            // Set the definition from java code as current definition in the flyway project for further comparison to the DB definition
            dbMigrationProject.getConfig().setDBDescription(createdDatabaseDescription);
            // Look for differences in java definition and DB
            DiffResult diffsForDBForVerification = EtkDatabaseMigrationHelper.findDiffForDB(dbMigrationProject.getEtkDbs(), createdDatabaseDescription, true);
            removeMetaTables(diffsForDBForVerification);
            if (diffsForDBForVerification.hasDiff(deleteDeprecatedIndicesInDb)) {
                String diffMessage = createDiffMessage(diffsForDBForVerification);
                throw new RuntimeException("Mismatch between the java database definition and the actual database:\n" + diffMessage);
            }
            Logger.log(LogChannels.DB_MIGRATION, LogType.INFO, "Validation of database definition successful for DB connection: " + setting.getDatasourceUrl());
        } catch (Exception e) {
            stopApplicationWithException(e);
        }
    }

    private String createDiffMessage(DiffResult diffsForDBForVerification) {
        StringBuilder builder = new StringBuilder();
        if (diffsForDBForVerification.hasMissingData()) {
            String data = diffsForDBForVerification.getMissingTables().stream().map(EtkDatabaseTable::getName).collect(Collectors.joining(", "));
            if (StrUtils.isValid(data)) {
                builder.append("Missing tables: ").append(data).append("\n");
            }
            diffsForDBForVerification.getMissingFieldsMap().forEach((table, fields) -> {
                String fieldNames = fields.stream().map(EtkDatabaseField::getName).collect(Collectors.joining(", "));
                if (StrUtils.isValid(fieldNames)) {
                    builder.append("Missing fields for table ").append(table).append(": ").append(fieldNames).append("\n");
                }
            });
            diffsForDBForVerification.getMissingIndicesMap().forEach((table, indices) -> {
                String indicesNames = indices.stream().map(DBDatabaseIndex::getIndexName).collect(Collectors.joining(", "));
                if (StrUtils.isValid(indicesNames)) {
                    builder.append("Missing indices for table ").append(table).append(": ").append(indicesNames).append("\n");
                }
            });
        }

        if (diffsForDBForVerification.hasDeprecatedData(deleteDeprecatedIndicesInDb)) {
            String tableNames = diffsForDBForVerification.getDeprecatedTables().stream().map(EtkDatabaseTable::getName).collect(Collectors.joining(", "));
            if (StrUtils.isValid(tableNames)) {
                builder.append("Deprecated tables: ").append(tableNames).append("\n");
            }
            diffsForDBForVerification.getDeprecatedFieldsMap().forEach((table, fields) -> {
                String fieldNames = fields.stream().map(EtkDatabaseField::getName).collect(Collectors.joining(", "));
                if (StrUtils.isValid(fieldNames)) {
                    builder.append("Deprecated fields for table ").append(table).append(": ").append(fieldNames).append("\n");
                }
            });
            if (deleteDeprecatedIndicesInDb || Constants.DEVELOPMENT) {
                diffsForDBForVerification.getDeprecatedIndicesMap().forEach((table, indices) -> {
                    String indicesNames = indices.stream().map(DBDatabaseIndex::getIndexName).collect(Collectors.joining(", "));
                    if (StrUtils.isValid(indicesNames)) {
                        if (Constants.DEVELOPMENT) {
                            builder.append("Deprecated indices but not allowed to drop (because of DB type) for table ")
                                    .append(table).append(": ").append(indicesNames).append("\n");
                        } else {
                            builder.append("Deprecated indices for table ").append(table).append(": ").append(indicesNames).append("\n");
                        }
                    }
                });
            }
        }

        if (diffsForDBForVerification.hasModifiedData()) {
            diffsForDBForVerification.getModifiedFieldsMap().forEach((table, fields) -> {
                String fieldNames = fields.stream().map(field -> field.getDatabaseField().getName()).collect(Collectors.joining(", "));
                if (StrUtils.isValid(fieldNames)) {
                    builder.append("Modified fields for table ").append(table).append(": ").append(fieldNames).append("\n");
                }
            });
        }

        return builder.toString();
    }

    private void removeMetaTables(DiffResult diffsForDBForVerification) {
        if (diffsForDBForVerification.hasDeprecatedData(false)) {
            List<EtkDatabaseTable> cleanedData = new ArrayList<>();
            for (EtkDatabaseTable deprecatedTable : diffsForDBForVerification.getDeprecatedTables()) {
                String tableName = deprecatedTable.getName();
                if (iPartsDatabaseFieldDescription.skipValidationForTable(tableName)
                    || ((flyway != null) && tableName.equalsIgnoreCase(flyway.getConfiguration().getTable()))) {
                    continue;
                }
                cleanedData.add(deprecatedTable);
            }
            diffsForDBForVerification.setDeprecatedTables(cleanedData);
        }
    }

    public void applyDefinitionChanges() throws IOException {
        // Diff auch bei ReadOnly DWK erstellen, weil dabei auch die Feldvisualisierungen und Texte kopiert werden
        DiffResult diffsForDWKForVerification = EtkDatabaseMigrationHelper.findDiffsForDWK(dbMigrationProject, createdDatabaseDescription, false, true); // Feldbenennungen nicht aktualisieren

        if (!isDWKFileWritable) {
            return;
        }
        EtkConfig dwkConfig = dbMigrationProject.getConfig();
        String storageInfo = dwkConfig.getStorageInfo();
        if (diffsForDWKForVerification.hasDiff(true)) {
            DWFile originalDwkFile = DWFile.get(storageInfo);

            // TODO Langer fast identischer Code-Block kopiert von EtkDatabaseMigrationHelper.executePluginDatabaseMigration() -> statische Mathode in EtkDatabaseMigrationHelper extrahieren?

            // Removed TABLES
            Set<String> deprecatedTableNames = collectDeprecatedTables(diffsForDWKForVerification);
            // Modified TABLES
            List<EtkDatabaseTable> modifiedTables = collectModifiedTables(diffsForDWKForVerification);

            boolean oldExpandEmptyElements = DomWriterJDom.EXPAND_EMPTY_ELEMENTS;
            try {
                DomWriterJDom.EXPAND_EMPTY_ELEMENTS = true; // Die Workbench expandiert leere XML-Tags
                TranslationHandler.getUiTranslationHandler().setFallbackLanguage(null);
                List<String> languages = dwkConfig.getViewerLanguages();
                dwkConfig.startWriting();
                try {
                    double currentDBVersion = dwkConfig.getDataBaseVersion();
                    if (currentDBVersion != CURRENT_DB_VERSION_FOR_IPARTS) {
                        dwkConfig.setDataBaseVersion(CURRENT_DB_VERSION_FOR_IPARTS);
                    }

                    // Remove deleted tables
                    if (deprecatedTableNames != null) {
                        for (String deprecatedTableName : deprecatedTableNames) {
                            dwkConfig.deleteKey(DBConst.DATABASETABLES + "/" + deprecatedTableName);
                        }
                    }

                    // Save new or modified tables
                    if (modifiedTables != null) {
                        EtkDatabaseDescription databaseDescription = dwkConfig.getDBDescription();
                        for (EtkDatabaseTable modifiedTable : modifiedTables) {
                            databaseDescription.saveTable(dwkConfig, modifiedTable, true,
                                                          true,
                                                          languages);
                        }
                    }

                    dwkConfig.commitWriting();

                    try (DWReader dwkConfigToSaveReader = originalDwkFile.getReader(DWFileCoding.UTF8)) {
                        String unprettyDwkContent = dwkConfigToSaveReader.lines()
                                .map(StrUtils::removeLeadingWhitespaces)
                                .collect(Collectors.joining(OsUtils.NEWLINE));
                        originalDwkFile.writeTextFile(unprettyDwkContent.getBytes(StandardCharsets.UTF_8));
                    }
                    Logger.log(LogChannels.DB_MIGRATION, LogType.INFO, "Applied found changes to the DWK definition file: "
                                                                       + DWFile.get(storageInfo).getAbsolutePath());
                } catch (Exception e) {
                    dwkConfig.rollbackWriting();
                    throw e;
                }
            } finally {
                DomWriterJDom.EXPAND_EMPTY_ELEMENTS = oldExpandEmptyElements;
            }

        } else {
            Logger.log(LogChannels.DB_MIGRATION, LogType.INFO, "No DWK <-> Java differences found. DWK definition " +
                                                               "and Java definition are identical. File: " + DWFile.get(storageInfo).getAbsolutePath());
        }
    }

    private void checkWritePermission() {
        DWFile originalDwkFile = (DWFile)dbMigrationSession.getAttribute(iPartsJavaViewerApplication.SESSION_KEY_DWK_FILE);
        isDWKFileWritable = originalDwkFile.canWriteToFile();
        if (!isDWKFileWritable) {
            Logger.log(LogChannels.DB_MIGRATION, LogType.INFO, "No permission to edit the DWK file \""
                                                               + originalDwkFile.getAbsolutePath()
                                                               + "\". Structure changes only in memory!");
        }
    }

    private List<EtkDatabaseTable> collectModifiedTables(DiffResult diffsForDWKForVerification) {
        Set<String> modifiedTableNames = new TreeSet<>();
        for (EtkDatabaseTable missingTable : diffsForDWKForVerification.getMissingTables()) {
            modifiedTableNames.add(missingTable.getName());
        }
        modifiedTableNames.addAll(diffsForDWKForVerification.getMissingFieldsMap().keySet());
        modifiedTableNames.addAll(diffsForDWKForVerification.getDeprecatedFieldsMap().keySet());
        modifiedTableNames.addAll(diffsForDWKForVerification.getModifiedFieldsMap().keySet());
        modifiedTableNames.addAll(diffsForDWKForVerification.getMissingIndicesMap().keySet());
        modifiedTableNames.addAll(diffsForDWKForVerification.getDeprecatedIndicesMap().keySet());

        List<EtkDatabaseTable> modifiedTables = null;
        if (!modifiedTableNames.isEmpty()) {
            modifiedTables = modifiedTableNames.stream()
                    .map(tableName -> createdDatabaseDescription.findTable(tableName))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return modifiedTables;
    }

    private Set<String> collectDeprecatedTables(DiffResult diffsForDWKForVerification) {
        Set<String> deprecatedTableNames = null;
        if (!diffsForDWKForVerification.getDeprecatedTables().isEmpty()) {
            deprecatedTableNames = new TreeSet<>();
            for (EtkDatabaseTable deprecatedTable : diffsForDWKForVerification.getDeprecatedTables()) {
                deprecatedTableNames.add(deprecatedTable.getName());
            }
        }
        return deprecatedTableNames;
    }

    private boolean writeData(DWFile migrationFile, List<String> scripts) throws IOException {
        if (!scripts.isEmpty()) {
            String sqlScripts = scripts.stream()
                    .map(script -> {
                        String oracleDBO = setting.getOracleDBO();
                        if (StrUtils.isValid(oracleDBO)) {
                            return EtkDatabaseMigrationHelper.getSQLLineWithSemicolon(StrUtils.replaceFirstSubstring(script, oracleDBO + ".", ""));
                        }
                        return EtkDatabaseMigrationHelper.getSQLLineWithSemicolon(script);
                    })
                    .collect(Collectors.joining(OsUtils.NEWLINE));
            migrationFile.writeTextFile(sqlScripts.getBytes(StandardCharsets.UTF_8));
            return true;
        }
        return false;
    }

    private int extractSchemaVersion() {
        MigrationInfoService infoResult = flyway.info();
        String version = infoResult.getInfoResult().schemaVersion;
        if (StrUtils.isValid(version)) {
            return Integer.parseInt(version);
        } else {
            return 0;
        }
    }

    public void stopApplicationWithException(Exception e) {
        Logger.log(LogChannels.DB_MIGRATION, LogType.ERROR, "Application halt due to exception: " + ExceptionUtils.getStackTrace(e));
        stopApplication();
    }

    public void stopApplication() {
        Runtime.getRuntime().halt(IPARTS_MIGRATION_ERROR_EXIT_CODE);
    }

    private void createDBDescriptionFromDefinition() {
        // Creating the description with the current DB version of the javaviewer application
        createdDatabaseDescription = new EtkDatabaseDescription();
        addStructureDefinitionToDatabaseDescription();
    }

    public void addStructureDefinitionToDatabaseDescription(EtkDatabaseDescription databaseDescription, boolean forceDWKWriteable) {
        // Creating session and project for the DB migrations
        createMigrationSession();
        createMigrationProject(true);
        activateDBConnection();
        this.createdDatabaseDescription = databaseDescription;
        addStructureDefinitionToDatabaseDescription();
    }

    private void addStructureDefinitionToDatabaseDescription() {
        if ((dbMigrationProject != null) && (createdDatabaseDescription != null)) {
            createdDatabaseDescription.createFixedFields(CURRENT_DB_VERSION_FOR_IPARTS);
            // Default user administration tables
            UserAdminDb.setUserAdminTablePrefixForCurrentApplication(EtkUserAdminDbActions.USER_ADMIN_TABLE_PREFIX);
            createdDatabaseDescription.addTablesFromDBSchema(new UserAdminDb());
            // Adding iParts specific DB Description
            iPartsDatabaseFieldDescription.addFieldDefinitions(createdDatabaseDescription);
            iPartsDatabaseIndexDescription.addIndexDefinitions(createdDatabaseDescription);
            // Clearing the cache since new tables may be added through the plugins
            createdDatabaseDescription.clearCaches();
            EtkDbs etkDbs = dbMigrationProject.getEtkDbs();
            DBDatabase database = etkDbs.getDBForDomain(DBDatabaseDomain.MAIN);
            if (database == null) {
                Logger.log(LogChannels.DB_MIGRATION, LogType.ERROR, "Database is not configured correctly");
                return;
            }

            for (EtkDatabaseTable table : createdDatabaseDescription.getTableList()) {
                // Ensure the field T_STAMP does exist in every table
                if (!table.fieldExists(FIELD_STAMP) && !table.getName().equalsIgnoreCase(TABLE_TREEID) && !table.getName().equalsIgnoreCase(TABLE_TREEMOD)) { // TREEID und TREEMOD haben kein Feld T_STAMP
                    createdDatabaseDescription.addField(table.getName(), FIELD_STAMP, EtkDatabaseDescription.STAMP_FIELD_TEXT,
                                                        EtkFieldType.feString, EnumSet.of(EtkFieldOption.NameFix, EtkFieldOption.TypFix,
                                                                                          EtkFieldOption.LaengeFix, EtkFieldOption.SystemFeld),
                                                        EtkFieldLengthType.flUserDefined, false, DEFAULT_STAMP_LEN);
                }

                // Creating UPPER indices for case-insensitive fields if necessary
                List<DBDatabaseIndex> upperIndexList = database.getUpperIndicesForCaseInsensitiveFields(table, table.getAllIndexe());
                if (Utils.isValid(upperIndexList)) {
                    for (DBDatabaseIndex upperIndex : upperIndexList) {
                        table.addUserIndex(upperIndex);
                    }
                }
            }
            // Setting the length information for every field
            EtkConfig dwkConfig = dbMigrationProject.getConfig();
            createdDatabaseDescription.loadDefaultLengthFromConfig(dwkConfig);
            createdDatabaseDescription.setLengthForTypen();
            iPartsDatabaseFieldDescription.modifyFieldDefinitions(createdDatabaseDescription);
        }
    }

    /**
     * Initializes the {@link Flyway} DB migration object with the current DB settings
     *
     * @throws IOException
     */
    private void initFlyway() throws IOException {
        FluentConfiguration configuration = Flyway.configure();
        configuration.dataSource(setting.getDatasourceUrl(), setting.getUser(), setting.getPassword());
        if (Constants.DEVELOPMENT) {
            String location;
            if (DWFile.get("javaviewer").exists()) { // Unittests werden im Hauptverzeichnis gestartet -> Pfad anpassen
                location = DWFile.get("javaviewer", DEFAULT_MIGRATIONS_PATH).getCanonicalPath();
            } else {
                location = getCurrentMigrationPath().getCanonicalPath();
            }
            configuration.locations("filesystem:" + location);
        } else {
            configuration.locations("classpath:" + DEFAULT_MIGRATIONS_PATH_CLASSPATH);
        }
        flyway = configuration.load();
    }

    private DWFile getCurrentMigrationPath() {
        DWFile pathToMigrations = DWFile.get(DEFAULT_MIGRATIONS_PATH);
        if (!pathToMigrations.exists(DWFile.DEFAULT_FILE_EXISTS_TIMEOUT)) {
            if (!pathToMigrations.mkDirsWithRepeat()) {
                throw new RuntimeException("Could not create directory for migrations!");
            }
        }
        return pathToMigrations;
    }

    private void createMigrationSession() {
        try {
            dbMigrationSession = EtkEndpointHelper.createSession(SessionType.CACHE);
        } catch (Exception e) {
            if (dbMigrationSession == null) {
                throw new RuntimeException(TranslationHandler.translate("!!DB migration session ist null"));
            } else {
                throw e;
            }
        }
    }

    private void createMigrationProject(boolean createFixedFields) {
        if (!createFixedFields) {
            EtkDatabaseDescription.CREATE_FIXED_FIELDS = false;
        }
        try {
            ConfigBase config = (ConfigBase)dbMigrationSession.getAttribute(Constants.SESSION_KEY_CONFIGURATION);
            if (config == null) {
                config = EtkEndpointHelper.createProjectConfig();
            }
            DWApplicationPathsManager.getInstance().setConfig(config);
            checkWritePermission();
            dbMigrationProject = EtkEndpointHelper.createProjectAndStoreItInEndpointSession(dbMigrationSession, isDWKFileWritable,
                                                                                            false, true);
        } finally {
            EtkDatabaseDescription.CREATE_FIXED_FIELDS = true;
        }
        if (dbMigrationProject == null) {
            throw new RuntimeException(TranslationHandler.translate("!!DB migration project ist null"));
        }
    }

    /**
     * Extracts the database connection for the current configuration variant (etk_viewer.config)
     */
    private void extractDBSetting() {
        DBConnectionSettings settings = new DBConnectionSettings();
        settings.read(config, DBConnectionSettings.XML_CONFIG_PATH_BASE);
        String dbAlias = config.getString(BaseSettingsPanel.XML_CONFIG_PATH_BASE + BaseSettingsPanel.XML_CONFIG_SUBPATH_DB_ALIAS, "");
        setting = settings.getSetting(dbAlias);
        if (setting == null) {
            throw new RuntimeException(TranslationHandler.translate("!!Ungültige Datenbank-Konfiguration für die Datenbank-Migration: %1",
                                                                    dbAlias));
        }
        if (setting.getDatasourceUrl() == null) {
            throw new RuntimeException("Datasource url could not be generated");
        }
    }

    public void clearReferences() {
        if (dbMigrationProject != null) {
            dbMigrationProject.setDBActive(false, false);
        }
        if (dbMigrationSession != null) {
            SessionManager.getInstance().destroySession(dbMigrationSession);
        }
        EtkDatabaseDescription.CREATE_FIXED_FIELDS = true;
    }
}
