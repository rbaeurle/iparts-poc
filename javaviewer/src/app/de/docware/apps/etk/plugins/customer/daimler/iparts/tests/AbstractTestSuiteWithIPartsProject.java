/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests;

import de.docware.apps.etk.base.AbstractTestEtkProject;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.application.iPartsFrameworkMain;
import de.docware.apps.etk.plugins.customer.daimler.iparts.application.iPartsJavaViewerApplication;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.viewer.FrameworkMain;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.os.OsUtils;
import de.docware.util.security.PasswordString;

import java.io.File;

/**
 * Abstrakte Testklasse für Tests, die eine echte iParts DB verwenden und daher eine {@link junit.framework.TestSuite}
 * verwenden sollten.
 * <br/><b>Siehe Kommentar in {@link de.docware.util.test.AbstractTest} bzgl. der Punkte, die in abgeleiteten Testklassen
 * berücksichtigt werden MÜSSEN.</b>
 */
public abstract class AbstractTestSuiteWithIPartsProject extends AbstractTestEtkProject {

    public static final String POSTGRES_CLONE_SERVER = "iparts-devtest.qsx.quanos.io";
    public static final int POSTGRES_CLONE_API_PORT = 2345;
    public static final String POSTGRES_CLONE_DB_USER = "unittest";
    public static final String POSTGRES_CLONE_DB_PW = "ipartsP0st#MB"; // Kann hier beliebig gewählt werden
    public static final String POSTGRES_CLONE_SOURCE_DB_WEBSERVICE_UNITTESTS = "iparts_ws";
    public static final String POSTGRES_VERIFICATION_TOKEN = "02pB5K2ICOHqKrb1rUdi0hTi4wORRJBDFQIKLtHPLFSKV217CkkzseKM70cQCWgh";

    /**
     * Erzeugt die Parameter für leichtgewichtige PostgreSQL-Klone von der iParts Webservice Unittest DB.
     *
     * @return
     */
    public static PostgresCloneParams createPostgresCloneParamsForIPartsWebserviceUnittestDB(String postgresCloneAlias) {
        return new PostgresCloneParams(POSTGRES_CLONE_SERVER, POSTGRES_CLONE_API_PORT, POSTGRES_CLONE_SOURCE_DB_WEBSERVICE_UNITTESTS,
                                       POSTGRES_CLONE_DB_USER, new PasswordString(POSTGRES_CLONE_DB_PW), postgresCloneAlias,
                                       new PasswordString(POSTGRES_VERIFICATION_TOKEN));
    }

    // Diese statische Methode MUSS in jeder Ableitung hinzugefügt werden, wenn die Testklasse eine TestSuite verwendet, damit
    // die Tests korrekt gestartet werden können
    /**
     * Eine manuelle {@link junit.framework.TestSuite} und {@link de.docware.util.test.FrameworkTestSetup} erzeugen, um
     * komplette Tests mit einem globalen SetUp und TearDown zu ermöglichen inkl. Nachtests.
     *
     * @return
     */
//    public static Test suite() {
//        return createTestSuite(TestClass.class);
//    }

    /**
     * Standardkonstruktor
     */
    public AbstractTestSuiteWithIPartsProject() {
        setUseGlobalTest(true);
        addExcludedDirs();
    }

    /**
     * Konstruktor für den kompletten Test über eine manuell erzeugte {@link junit.framework.TestSuite}.
     *
     * @param globalTest
     * @param methodName
     */
    public AbstractTestSuiteWithIPartsProject(AbstractTestSuiteWithIPartsProject globalTest, String methodName) {
        super(globalTest, methodName);
        addExcludedDirs();
    }

    @Override
    protected FrameworkMain createApplicationFrameworkMain() {
        return new iPartsFrameworkMain(new File(OsUtils.getBaseDirPath()));
    }

    @Override
    protected void createApplicationInstance(ConfigBase configuration) {
        iPartsJavaViewerApplication.createInstance(configuration);
    }

    @Override
    public void globalSetUp() throws Exception {
        super.globalSetUp();

        // Interessante Logkanäle aktivieren
//        Logger.getLogger().addChannel(LogChannels.SQL);
        Logger.getLogger().addChannel(LogChannels.SQL_LONG_DURATION);
//        Logger.getLogger().addChannel(LogChannels.SQL_RESULT_SET);
//        Logger.getLogger().addChannel(LogChannels.SQL_BATCH);
        Logger.getLogger().addChannel(LogChannels.DB_PERFORMANCE);
    }

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    @Override
    protected boolean isFrameworkApplicationInstanceNeeded() {
        return true;
    }

    @Override
    protected String getConfigFileName() {
        return "etk_viewer.config";
    }

    @Override
    protected String getDwkFileName() {
        return null; // DWK aus Konfiguration übernehmen
    }

    @Override
    public PostgresCloneParams getPostgresCloneParams() {
        return AbstractTestSuiteWithIPartsProject.createPostgresCloneParamsForIPartsWebserviceUnittestDB(getDefaultCloneAlias());
    }

    protected void addExcludedDirs() {
        addExcludedFileOrDirForCopyToTmpDir("importLogs");
        addExcludedFileOrDirForCopyToTmpDir("logs");
        if (isUsingClone()) {
            addExcludedFileOrDirForCopyToTmpDir("Data"); // H2 nicht kopieren, wenn mit Oracle-Klonen gearbeitet wird
        }
    }

    /**
     * Erzeugt einen Stücklisteneintrag für die Tests
     *
     * @param lfdNo
     * @param code
     * @param pos
     * @return
     */
    protected iPartsDataPartListEntry createTestPartListEntry(String lfdNo, String code, String pos) {
        iPartsDataPartListEntry entry = new iPartsDataPartListEntry(getProject(), new PartListEntryId("0", "0", lfdNo));
        entry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        entry.setFieldValue(iPartsConst.FIELD_K_CODES, code, DBActionOrigin.FROM_EDIT);
        entry.setFieldValue(iPartsConst.FIELD_K_POS, pos, DBActionOrigin.FROM_EDIT);
        return entry;
    }
}