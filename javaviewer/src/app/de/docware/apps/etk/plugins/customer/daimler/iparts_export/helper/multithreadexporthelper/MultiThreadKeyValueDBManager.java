/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.multithreadexporthelper;

import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.util.sql.SQLStringConvert;

/**
 * Hilfsklasse um die Synchronität eines MultiThread Exports über die Datenbank zu verwalten
 */
public class MultiThreadKeyValueDBManager {

    private final String keyRunning;
    private final String keyCanceled;
    private final String keyTotalCount;
    private final String keyDoneCount;
    private final EtkDbs etkDbs;

    public MultiThreadKeyValueDBManager(EtkProject project, String keyRunning, String keyCanceled, String keyTotalCount,
                                        String keyDoneCount) {
        this.keyRunning = keyRunning;
        this.keyCanceled = keyCanceled;
        this.keyTotalCount = keyTotalCount;
        this.keyDoneCount = keyDoneCount;
        if (project != null) {
            this.etkDbs = project.getEtkDbs();
        } else {
            this.etkDbs = null;
        }
    }

    public boolean isRunning() {
        return getKeyValueBool(keyRunning);
    }

    public void setRunning(boolean value) {
        setKeyValueBool(keyRunning, value);
        boolean resetCountAndCanceled = true;
        if (!value) {
            if (isCanceled()) {
                // beim Zurücksetzen des Run-Flags bedingt durch Cancel-Flag bleiben die Counts + Canceled stehen
                // (für die Fortschritts-Anzeige)
                resetCountAndCanceled = false;
            }
        }
        if (resetCountAndCanceled) {
            reset();
        }
    }

    public void reset() {
        setKeyDataTotalCount(-1);
        setKeyExportedDataCount(0);
        setCanceled(false);
    }

    public boolean isCanceled() {
        return Thread.currentThread().isInterrupted() || getKeyValueBool(keyCanceled);
    }

    public void setCanceled(boolean value) {
        setKeyValueBool(keyCanceled, value);
    }

    public int getKeyDataTotalCount() {
        return getKeyValueInt(keyTotalCount);
    }

    public void setKeyDataTotalCount(int totalCount) {
        setKeyValueInt(keyTotalCount, totalCount);
    }

    public int getKeyExportedDataCount() {
        return getKeyValueInt(keyDoneCount);
    }

    public void setKeyExportedDataCount(int exportedDataCount) {
        setKeyValueInt(keyDoneCount, exportedDataCount);
    }

    private boolean getKeyValueBool(String key) {
        return SQLStringConvert.ppStringToBoolean(etkDbs.getKeyValue(key));
    }

    private void setKeyValueBool(String key, boolean value) {
        etkDbs.setKeyValue(key, SQLStringConvert.booleanToPPString(value));
    }

    private int getKeyValueInt(String key) {
        return SQLStringConvert.ppStringToInt(etkDbs.getKeyValue(key));
    }

    private void setKeyValueInt(String key, int value) {
        etkDbs.setKeyValue(key, SQLStringConvert.intToPPString(value));
    }
}
