/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.importer;

import de.docware.apps.etk.base.project.base.EtkMessageLog;

/**
 * Interface für einen generischen Importer.
 */
public interface GenericImporterInterface {

    /**
     * Liefert den übersetzten Namen für diesen Import zurück.
     *
     * @param language
     * @return
     */
    String getImportName(String language);

    /**
     * Initialisiert den Import mit dem übergebenen {@link EtkMessageLog}.
     *
     * @param messageLog
     * @return
     */
    boolean initImport(EtkMessageLog messageLog);

    /**
     * Schließt den Import ab.
     *
     * @return
     */
    boolean finishImport();

    /**
     * Ist der Import fertig (unabhängig davon, ob mit Fehlern oder ohne)?
     *
     * @return
     */
    boolean isFinished();

    /**
     * Bricht den Import mit der übergebenen Nachricht ab, erhöht den Fehlerzähler und führt dazu, dass {@link #isCancelled()}
     * {@code true} zurückliefert.
     *
     * @param message
     */
    void cancelImport(String message);

    /**
     * Wurde der Import z.B. aufgrund von Fehlern abgebrochen?
     *
     * @return
     */
    boolean isCancelled();
}
