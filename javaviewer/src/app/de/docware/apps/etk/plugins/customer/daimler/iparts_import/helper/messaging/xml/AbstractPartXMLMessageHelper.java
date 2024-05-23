/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.messaging.xml;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.AbstractXMLMessageListener;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsMQMessageManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.AbstractXMLPartImporter;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOption;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.os.OsUtils;

import java.io.IOException;

/**
 * Abstrakter Helfer zum Verabeiten von empfangenen XML Teilestammdateien (SRM und PRIMUS)
 */
public abstract class AbstractPartXMLMessageHelper {

    private static final int IS_FILE_TIMEOUT = 5000;

    // Felder für die gemeinsame Log-Datei
    private AbstractXMLMessageListener importListener;
    private DWFile sharedLogFile;
    private long collectedImportDuration;
    private long collectedMessagesCount;
    private AbstractXMLPartImporter lastImporter;
    private FrameworkThread collectMessagesThread;
    private String type;


    public AbstractPartXMLMessageHelper(String datasetType) {
        this.type = datasetType;
    }

    public void registerListener(EtkProject project, Session session) {
        importListener = new AbstractXMLMessageListener(session) {
            private final Object syncObject = new Object();

            @Override
            public boolean messageReceived(AbstractMQMessage xmlMQMessage) {
                if (xmlMQMessage.isOfType(type)) {
                    long importStartTime = System.currentTimeMillis();
                    AbstractXMLPartImporter importer = getImporter(project);
                    boolean closeAndCreateSharedLogFile;
                    synchronized (syncObject) {
                        closeAndCreateSharedLogFile = (sharedLogFile == null) || !sharedLogFile.isFile(IS_FILE_TIMEOUT);
                    }
                    if (closeAndCreateSharedLogFile) {
                        if (collectMessagesThread != null) {
                            collectMessagesThread.cancel(); // ruft beim Beenden closeSharedLogFile() auf
                        } else {
                            closeSharedLogFile(syncObject);
                        }
                        createSharedLogFile(importer, session, syncObject);
                    }
                    synchronized (syncObject) {
                        lastImporter = importer;
                        boolean result = importer.startImportFromMQMessage(xmlMQMessage, sharedLogFile);
                        collectedImportDuration += System.currentTimeMillis() - importStartTime;
                        collectedMessagesCount++;
                        return result;
                    }
                }

                xmlMQMessage.moveSavedMQXmlFile(iPartsConst.SUBDIR_UNKNOWN);
                return false;
            }
        };
        iPartsMQMessageManager.getInstance(iPartsImportPlugin.XML_MESSAGE_MANAGER_NAME_IMPORT_DATA).addXMLMessageListenerForChannelTypes(importListener, getMQChannelName());
    }

    private void createSharedLogFile(AbstractXMLPartImporter importer, Session session, Object syncObject) {
        synchronized (syncObject) {
            collectedImportDuration = 0;
            collectedMessagesCount = 0;
            sharedLogFile = importer.importJobRunning();
        }

        // Das Starten des Singleton-Threads (inkl. Beenden vom vorherigen) darf nicht innerhalb von synchronized
        // stattfinden, weil es ansonsten zu einem Deadlock kommen würde
        collectMessagesThread = session.startSingletonChildThread(getChildThreadName(), new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                int mqCollectTimeMs = iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(getCollectTimeConfig()) * 1000;
                Java1_1_Utils.sleep(mqCollectTimeMs);

                // Gemeinsame Log-Datei schließen (auch bei einem abgebrochenen Thread, wo Java1_1_Utils.sleep() true zurückliefert)
                closeSharedLogFile(syncObject);
            }
        });
    }

    private void closeSharedLogFile(Object syncObject) {
        // Bisherige gemeinsame Log-Datei abschließen und ins PROCESSED-Verzeichnis verschieben
        synchronized (syncObject) {
            if (lastImporter == null) {
                sharedLogFile = null;
                return;
            }

            // Nicht sharedLogFile verwenden, weil bei einem Fehler die Log-Datei vom Importer bereits ins
            // ERROR-Verzeichnis verschoben wurde
            DWFile logFile = lastImporter.getLogFile();
            if ((logFile != null) && logFile.isFile(IS_FILE_TIMEOUT)) {
                if (collectedMessagesCount > 0) {
                    try {
                        String durationPerMessageString = DateUtils.formatTimeDurationString(collectedImportDuration / collectedMessagesCount,
                                                                                             true, false, lastImporter.getLogLanguage());
                        if (lastImporter.getErrorCount() == 0) { // keine Fehler -> Zeitraum mit angeben
                            int mqCollectTimeMs = iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(getCollectTimeConfig()) * 1000;
                            String importDurationString = DateUtils.formatTimeDurationString(mqCollectTimeMs,
                                                                                             false, false, lastImporter.getLogLanguage());
                            logFile.appendTextFile((OsUtils.NEWLINE + OsUtils.NEWLINE
                                                    + TranslationHandler.translateForLanguage("!!Es wurden %1 MQ XML-Nachrichten im Zeitraum von %2 importiert mit durchschnittlich %3 Importdauer.",
                                                                                              lastImporter.getLogLanguage(),
                                                                                              String.valueOf(collectedMessagesCount),
                                                                                              importDurationString,
                                                                                              durationPerMessageString)
                                                    + OsUtils.NEWLINE).getBytes(DWFileCoding.UTF8.getJavaCharsetName()));
                        } else { // Fehler -> Zeitraum ist kürzer als eingestellt
                            logFile.appendTextFile((OsUtils.NEWLINE + OsUtils.NEWLINE
                                                    + TranslationHandler.translateForLanguage("!!Es wurden %1 MQ XML-Nachrichten importiert mit durchschnittlich %2 Importdauer, wobei der letzte Import mit Fehlern beendet wurde.",
                                                                                              lastImporter.getLogLanguage(),
                                                                                              String.valueOf(collectedMessagesCount),
                                                                                              durationPerMessageString)
                                                    + OsUtils.NEWLINE).getBytes(DWFileCoding.UTF8.getJavaCharsetName()));
                        }
                    } catch (IOException e) {
                        Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, e);
                    }
                }

                // Log-Datei nur dann ins PROCESSED-Verzeichnis verschieben, wenn der letzte Import fehlerfrei
                // durchgelaufen ist; bei Fehlern wird die Log-Datei vom Importer selbst sofort ins ERROR-Verzeichnis
                // verschoben
                if (lastImporter.getErrorCount() == 0) {
                    iPartsJobsManager.getInstance().jobProcessed(logFile);
                }
            }
            sharedLogFile = null;
            lastImporter = null;
        }
    }

    public void deregisterListener() {
        iPartsMQMessageManager.getInstance(iPartsImportPlugin.XML_MESSAGE_MANAGER_NAME_IMPORT_DATA).removeXMLMessageListenerForChannelTypes(importListener, getMQChannelName());
        importListener = null;
    }

    protected abstract AbstractXMLPartImporter getImporter(EtkProject project);

    protected abstract String getChildThreadName();

    protected abstract UniversalConfigOption getCollectTimeConfig();

    protected abstract MQChannelType getMQChannelName();
}
