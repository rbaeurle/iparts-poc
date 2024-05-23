/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.util.CanceledException;
import de.docware.util.collections.weak.WeakKeysMap;

/**
 * iPartsDataset besteht aus mehreren einzelnen virtuellen Dataset. Um in mehreren Strukturen suchen zu können, benötigt
 * man mehrere verschiedene Abfragen. Eine dieser mehreren Abfragen ist jeweils so ein iPartsSearchVirtualDataset.
 */
public abstract class iPartsSearchVirtualDataset {

    protected EtkDisplayFields dsSelectFields;
    protected EtkProject project;
    protected WeakKeysMap<String, String> multiLanguageCache;

    public iPartsSearchVirtualDataset(EtkDisplayFields dsSelectFields, EtkProject project, WeakKeysMap<String, String> multiLanguageCache) {
        this.dsSelectFields = dsSelectFields;
        this.project = project;
        this.multiLanguageCache = multiLanguageCache;
    }

    /**
     * In abgeleiteten Klassen muss die Abfrage initialisiert werden.
     */
    public abstract void create() throws CanceledException;

    /**
     * Liefert die {@link EtkDataPartListEntry}-Ergebnisse vom aktuellen virtuellen Datensatz.
     *
     * @return Kann auch {@code null} sein.
     */
    public abstract EtkDataPartListEntry[] get();

    /**
     * Gibt es einen weiteren Datensatz?
     *
     * @return
     */
    public abstract boolean next() throws CanceledException;

    /**
     * Abfrage schließen.
     */
    public abstract void close();
}