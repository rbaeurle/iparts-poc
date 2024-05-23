/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.multithreadexporthelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Helfer um MultiThread Berechnungen für den Export zu steuern
 */
public class MultiThreadExportExecutor {

    public static ExecutorService createExecutor(int threadCount) {
        return Executors.newFixedThreadPool(threadCount);
    }

    public static boolean executorAwaitTermination(ExecutorService executorService) {
        if (executorService == null) {
            return true;
        }
        executorService.shutdown();
        try {
            return executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

}
