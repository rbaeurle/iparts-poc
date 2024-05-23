/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.util.CanceledException;
import de.docware.util.collections.weak.WeakKeysMap;

import java.util.List;

/**
 * Virtueller Datensatz für die Suche in iParts mit einzelnen {@link EtkDataPartListEntry}s als Ergebnisliste.
 */
public abstract class iPartsSearchVirtualDatasetWithEntries extends iPartsSearchVirtualDataset {

    protected List<EtkDataPartListEntry> resultPartListEntries;
    protected int resultIndex = -1;

    public iPartsSearchVirtualDatasetWithEntries(EtkDisplayFields dsSelectFields, EtkProject project, WeakKeysMap<String, String> multiLanguageCache) {
        super(dsSelectFields, project, multiLanguageCache);
    }

    @Override
    public void create() throws CanceledException {
        resultPartListEntries = createResultPartListEntries();
    }

    @Override
    public EtkDataPartListEntry[] get() {
        if ((resultPartListEntries != null) && (resultIndex >= 0) && (resultIndex < resultPartListEntries.size())) {
            return new EtkDataPartListEntry[]{ resultPartListEntries.get(resultIndex) };
        }
        return null;
    }

    @Override
    public boolean next() {
        if (resultPartListEntries != null) {
            resultIndex++;
            if (resultIndex < resultPartListEntries.size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() {
        resultPartListEntries = null;
        resultIndex = -1;
    }

    /**
     * Wird aufgerufen, um die {@link EtkDataPartListEntry}-Ergebnisse zu erzeugen.
     *
     * @return Kann auch {@code null} sein.
     */
    protected abstract List<EtkDataPartListEntry> createResultPartListEntries() throws CanceledException;
}