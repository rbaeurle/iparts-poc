/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.application;

import de.docware.apps.etk.plugins.customer.daimler.iparts.database.iPartsDBMigrations;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.viewer.FrameworkMain;
import de.docware.apps.etk.viewer.webapp.deploytool.forms.BaseSettingsPanel;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.startparameter.CommandLine;
import de.docware.framework.modules.gui.misc.startparameter.StartParameter;
import de.docware.framework.modules.gui.output.swing.SwingHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.gui.session.SessionException;

import java.io.File;

/**
 * Main Klasse für die iParts Anwendung
 */
public class iPartsFrameworkMain extends FrameworkMain {

    public static final String VM_PARAMETER_SKIP_DB_VALIDATION = "skipDBValidation";
    private static final Object LOCK_OBJECT = new Object();
    private static boolean skipDBValidation;

    /**
     * main() für das automatische Erzeugen von FlyWay Migrationen
     *
     * @param args
     * @throws SessionException
     */
    public static void main(final String[] args) throws SessionException {
        StartParameter startParameters = new CommandLine(new String[]{ Constants.VM_PARAMETER_FORCE_WEB_APP + "=false" });
        AbstractApplication.__internal_swingStartup(iPartsFrameworkMain.class, startParameters.getParameterAsList());
    }

    public iPartsFrameworkMain(File baseDir) {
        super(init(baseDir));
    }

    private static File init(File baseDir) {
        skipDBValidation = StartParameter.getSystemPropertyBoolean(iPartsFrameworkMain.VM_PARAMETER_SKIP_DB_VALIDATION, false);
        iPartsPlugin.initDatabaseOptions();
        deactivateExceptionHandlingWithGUI();
        return baseDir;
    }

    public static boolean isSkipDBValidation() {
        return skipDBValidation;
    }

    private static void deactivateExceptionHandlingWithGUI() {
        if (Constants.MODE.equals(SwingHandler.TYPE)) {
            Logger logger = Logger.getLogger();
            logger.setExceptionHandlingWithGUI(false);
        }
    }

    public iPartsFrameworkMain(File baseDir, Boolean onlyConfig) {
        super(init(baseDir), onlyConfig);
    }

    private void checkDBStatus() {
        // Differentiate between Migrations-Creating-Mode and plain iParts
        boolean isDBMigrateCall = Constants.MODE.equals(SwingHandler.TYPE);
        iPartsDBMigrations migrationsHelper = new iPartsDBMigrations(configuration);
        try {
            migrationsHelper.initDBMigrationData(isDBMigrateCall);
            migrationsHelper.createStructureFromDefinition();
            if (isDBMigrateCall) {
                migrationsHelper.createFlywayMigrations();
                Runtime.getRuntime().halt(0);
            } else {
                migrationsHelper.validateDefinition();
            }
        } catch (Exception e) {
            // FlyWay liefert einen Fehler -> Anwendung beenden
            migrationsHelper.stopApplicationWithException(e);
        } finally {
            migrationsHelper.clearReferences();
        }
    }

    @Override
    protected void __internal_initConfig() {
        super.__internal_initConfig();

        synchronized (LOCK_OBJECT) {
            // Die normale DBMigration auf jeden Fall deaktivieren
            if (configuration.getBoolean(BaseSettingsPanel.XML_CONFIG_PATH_BASE + BaseSettingsPanel.XML_CONFIG_SUBPATH_DB_MIGRATE_EXECUTE_ON_START, false)) {
                configuration.startWriting();
                try {
                    configuration.setBoolean(BaseSettingsPanel.XML_CONFIG_PATH_BASE + BaseSettingsPanel.XML_CONFIG_SUBPATH_DB_MIGRATE_EXECUTE_ON_START, false);

                    // Damit keine Log-Datei angelegt wird, werden hier die Konfigurationen überschrieben, die dafür sorgen, dass sonst eine Datei erzeugt wird
                    //                configuration.setString(LoggingConfiguration.XML_CONFIG_PATH_BASE + LoggingConfiguration.XML_CONFIG_SUBPATH_LOGGING_FILE, "");
                    //                configuration.setBoolean(LoggingConfiguration.XML_CONFIG_PATH_BASE + LoggingConfiguration.XML_CONFIG_SUBPATH_LOGGING_STD_OUT, true);
                    //                configuration.setString(LoggingConfiguration.XML_CONFIG_PATH_BASE + LoggingConfiguration.XML_CONFIG_SUBPATH_LOGGING_FILE +
                    //                                        LoggingConfiguration.XML_CONFIG_ATTRIB_LOGGING_FILE_DATE_PATTERN, "");
                    configuration.commitWriting();
                } catch (Exception e) {
                    configuration.rollbackWriting();
                    Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e.getMessage());
                }
            }
        }
    }

    @Override
    protected void checkLibsAndClassPath() {
        Logger.log(LogChannels.APPLICATION_START, LogType.INFO, "Using iParts main class");
        if (!skipDBValidation) {
            checkDBStatus();
        } else {
            Logger.log(LogChannels.DB_MIGRATION, LogType.ERROR, "Warning! Database validation is skipped!");
        }
    }

    protected void initJavaViewerApplication() {
        iPartsJavaViewerApplication.createInstance(getConfiguration());
    }

    @Override
    public void startSession(Session session, StartParameter startParameter) {
        if (skipDBValidation) {
            if (session.canHandleGui()) {
                if (!startParameter.getParameterBoolean(Constants.FRAMEWORK_PARAMETER_ADMIN, false)) {
                    throw new RuntimeException("VM parameter " + iPartsFrameworkMain.VM_PARAMETER_SKIP_DB_VALIDATION
                                               + " is only temporarily valid to access the administrator mode!");
                }
            } else {
                // Keine Sessions ohne GUI starten bei skipDBValidation
                Logger.log(LogChannels.DB_MIGRATION, LogType.ERROR, "Session of type " + session.getSessionType().name()
                                                                    + " is not started because VM parameter " + iPartsFrameworkMain.VM_PARAMETER_SKIP_DB_VALIDATION
                                                                    + " is active which is only temporarily valid to access the administrator mode!");
                return;
            }
        }

        super.startSession(session, startParameter);
    }
}
