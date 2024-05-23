/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeriesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler für die automatische Berechnung offener Stände von allen Konstruktions-Stücklisten einer Baureihe und deren Export
 */
public class iPartsSeriesAutoCalcAndExport extends AbstractDayOfWeekHandler {

    public iPartsSeriesAutoCalcAndExport(EtkProject project, Session session) {
        super(project, session, iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, "auto series calc and export");
    }

    private void calcAndExportSeries() {
        iPartsDataSeriesList seriesList = iPartsDataSeriesList.loadAllAutoCalcAndExportSeries(getProject());
        if (!seriesList.isEmpty()) {
            long startTime = System.currentTimeMillis();
            int threadCount = iPartsEditPlugin.getPluginConfig().getConfigValueAsInteger(iPartsEditPlugin.CONFIG_AUTO_CALC_AND_EXPORT_THREAD_COUNT);
            Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.DEBUG, "Starting scheduled calculation and export for "
                                                                                           + seriesList.size() + " marked series with "
                                                                                           + threadCount + " threads...");
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            try {
                Session session = Session.get();

                // Baureihen sortieren für deterministische Reihenfolge
                Set<iPartsSeriesId> seriesIds = new TreeSet<>();
                for (iPartsDataSeries dataSeries : seriesList) {
                    seriesIds.add(dataSeries.getAsId());
                }

                for (iPartsSeriesId seriesId : seriesIds) {
                    executorService.execute(() -> {
                        Runnable calculationRunnable = () -> {
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }

                            String virtualKeyString = iPartsVirtualNode.getVirtualIdString(new iPartsVirtualNode(iPartsNodeType.DIALOG_HMMSM, seriesId));
                            iPartsAssemblyId assemblyId = iPartsDataVirtualFieldsHelper.getAssemblyIdFromVirtualKey(virtualKeyString);
                            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId);
                            if (assembly instanceof iPartsDataAssembly) {
                                iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                                if (assembly.existsInDB()) {
                                    AssemblyId parentAssemblyId = iPartsAssembly.getFirstParentAssemblyIdFromParentEntries();
                                    if (parentAssemblyId != null) {
                                        FrameworkThread threadForSeries = ReportForConstructionNodesHelper.doReporting(getProject(), assembly,
                                                                                                                       parentAssemblyId,
                                                                                                                       true, true);
                                        if (threadForSeries != null) {
                                            threadForSeries.waitFinished();
                                        }
                                    } else {
                                        Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.ERROR, "Scheduled series calculation for \""
                                                                                                                       + seriesId.getSeriesNumber()
                                                                                                                       + "\" not possible because of missing structure node for series type \""
                                                                                                                       + StrUtils.copySubString(seriesId.getSeriesNumber(), 0, 1) + "\"");
                                    }
                                }
                            }
                        };

                        if (session != null) {
                            session.runInSession(calculationRunnable);
                        } else {
                            calculationRunnable.run();
                        }
                    });
                }
            } finally {
                // Alle gewünschten Baureihen wurden zum ExecutorService hinzugefügt -> Shutdown aufrufen und warten bis alle
                // Tasks abgearbeitet wurden
                executorService.shutdown();
            }
            boolean finished;
            try {
                finished = executorService.awaitTermination(1, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                finished = false;
            }

            String durationString = DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, false, false,
                                                                       Language.EN.getCode());
            if (finished) {
                Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.DEBUG, "Finished scheduled calculation and export for all "
                                                                                               + seriesList.size() + " marked series in "
                                                                                               + durationString);
            } else {
                Logger.log(iPartsEditPlugin.LOG_CHANNEL_SERIES_CALC_AND_EXPORT, LogType.ERROR, "Scheduled calculation and export for "
                                                                                               + seriesList.size() + " marked series cancelled after "
                                                                                               + durationString);
            }
        }
    }

    @Override
    protected void executeLogic() {
        calcAndExportSeries();
    }
}