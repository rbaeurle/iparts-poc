/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.iPartsDataCortexImportList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok.AbstractWSConstructionKitsHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok.WSConstructionKitsHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok.WSConstructionKitsRemarksHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.nutzdok.WSConstructionKitsSaaRemarkHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;

/**
 * Scheduler für den automatischen Import von NutzDok Cortex Daten
 */
public class iPartsNutzDokCortexImportScheduler extends AbstractDayOfWeekHandler {

    /**
     * Test-Routine für den Cortex-Scheduler
     * Ist nur aus dem DEVELOPMENT-Menu "Cortex Scheduler starten..." aufrufbar
     * Setzt zusätzlich die KitsHandler Variable IS_TEST_MODE. Damit werden die Cortex-Elemente nicht
     * gelöscht, sondern der Status auf leer gesetzt.
     *
     * @param project
     * @param session
     */
    public static void doTestCortexScheduler(EtkProject project, Session session) {
        iPartsNutzDokCortexImportScheduler scheduler = new iPartsNutzDokCortexImportScheduler(project, session);
        if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            AbstractWSConstructionKitsHandler.IS_TEST_MODE = true; // true: abgearbeitete Cortex-Recs werden NICHT gelöscht,
            // sondern der Status auf leer gesetzt
        }
        scheduler.doExecuteLogic();
        scheduler.stopThread();
        if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            AbstractWSConstructionKitsHandler.IS_TEST_MODE = false;
        }
    }

    public iPartsNutzDokCortexImportScheduler(EtkProject project, Session session) {
        super(project, session, iPartsEditPlugin.LOG_CHANNEL_NUTZDOK_CORTEX_SCHEDULER, "nutzdok cortex import");
    }

    public void doExecuteLogic() {
        executeLogic();
    }

    @Override
    protected void executeLogic() {
        int warningCount = 0;
        int errorCount = 0;
        // Gemeinsamer LogHelper für alle Handler
        ImportExportLogHelper logHelper = ImportExportLogHelper.createLogHelperWithRunningJob("Import constructionKits");
        logHelper.addLogMsgWithTranslation("!!Cortex Import gestartet.");
        logHelper.addNewLine();

        // Zuerst die CONSTRUCTION_KITS
        AbstractWSConstructionKitsHandler kitsHandler = new WSConstructionKitsHandler(getProject(), logHelper);
        // Aufruf des Handlers für CONSTRUCTION_KITS
        boolean noErrors = handleOneImport(kitsHandler);
        // Warnings- und Error-Anzahl des Handlers sammeln
        warningCount += kitsHandler.getLogWarnings();
        errorCount += kitsHandler.getLogErrors();
        if (noErrors) {
            // Nur weitermachen, wenn der vorherige Handler fehlerfrei war
            // KEM_REMARKS
            kitsHandler = new WSConstructionKitsRemarksHandler(getProject(), logHelper);
            // Aufruf des Handlers für KEM_REMARKS
            noErrors = handleOneImport(kitsHandler);
            // Warnings- und Error-Anzahl des Handlers sammeln
            warningCount += kitsHandler.getLogWarnings();
            errorCount += kitsHandler.getLogErrors();
        }
        if (noErrors) {
            // Nur weitermachen, wenn der vorherige Handler fehlerfrei war
            // SAA_REMARKS
            kitsHandler = new WSConstructionKitsSaaRemarkHandler(getProject(), logHelper);
            // Aufruf des Handlers für SAA_REMARKS
            noErrors = handleOneImport(kitsHandler);
            // Warnings- und Error-Anzahl des Handlers sammeln
            warningCount += kitsHandler.getLogWarnings();
            errorCount += kitsHandler.getLogErrors();
        }

        logHelper.addNewLine();
        // Ende Meldung ausgeben
        if (noErrors) {
            logHelper.addLogMsgWithTranslation(buildEndMsg(logHelper, "!!Cortex Import beendet.", warningCount, errorCount));
        } else {
            logHelper.addLogMsgWithTranslation(buildEndMsg(logHelper, "!!Cortex Import abgebrochen.", warningCount, errorCount));
        }
        logHelper.addNewLine();

        // LogHelper schließen
        if (noErrors) {
            AbstractWSConstructionKitsHandler.closeJobFile(logHelper);
        } else {
            AbstractWSConstructionKitsHandler.closeJobFileWithError(logHelper);
        }
    }

    /**
     * Ende-Meldung des Cortex-Importers zusammenbauen
     * (incl Anzahl der Warnungen/Erros über alle Handler)
     *
     * @param logHelper
     * @param endMsg
     * @param warningCount
     * @param errorCount
     * @return
     */
    private String buildEndMsg(ImportExportLogHelper logHelper, String endMsg, int warningCount, int errorCount) {
        String additional = "";
        if ((warningCount + errorCount) > 0) {
            if (errorCount > 0) {
                additional = logHelper.translateForLog("!!mit %1 Fehlern", String.valueOf(errorCount));
                if (warningCount > 0) {
                    additional += " " + logHelper.translateForLog("!!und %1 Warnungen", String.valueOf(warningCount));
                }
            } else {
                additional = logHelper.translateForLog("!!mit %1 Warnungen", String.valueOf(warningCount));
            }
        }
        if (StrUtils.isValid(additional)) {
            endMsg = logHelper.translateForLog(endMsg) + " (" + additional + ")";
        }
        return endMsg;
    }

    /**
     * Ausführung eines Kits-Handlers
     *
     * @param kitsHandler
     * @return
     */
    private boolean handleOneImport(AbstractWSConstructionKitsHandler kitsHandler) {
        // Liste aus der Cortex-Table holen
        iPartsDataCortexImportList kitList = kitsHandler.loadHandleList();
        // und abarbeiten
        return kitsHandler.executeLogic(kitList);
    }
}
