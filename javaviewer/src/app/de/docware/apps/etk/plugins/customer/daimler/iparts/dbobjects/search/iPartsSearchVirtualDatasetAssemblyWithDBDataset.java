/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.search.model.EtkSearchHelper;
import de.docware.util.CanceledException;
import de.docware.util.collections.weak.WeakKeysMap;

/**
 * Virtueller Datensatz für die Assembly Suche in iParts mit einem {@link de.docware.framework.modules.db.DBDataSet} als Ergebnis.
 */
public abstract class iPartsSearchVirtualDatasetAssemblyWithDBDataset extends iPartsSearchVirtualDatasetWithDBDataset {

    public iPartsSearchVirtualDatasetAssemblyWithDBDataset(EtkDisplayFields dsSelectFields, EtkProject project, WeakKeysMap<String, String> multiLanguageCache) {
        super(dsSelectFields, project, multiLanguageCache);
    }

    @Override
    public EtkDataPartListEntry[] get() {
        EtkDataPartListEntry partListEntry = EtkSearchHelper.getNextPartListEntryForAssemblyDataSet(ds, dsSelectFields, project, multiLanguageCache, "");
        // DAIMLER-7521: Cache nicht mehr verwenden, weil die Gesamt-Performance v.a. bei SearchPartsWOContext dadurch
        // je nach Suchtext in der Regel leider erheblich schlechter ist als mit Cache
//        partListEntry = getPartListEntryFromCache(partListEntry, false);
        return new EtkDataPartListEntry[]{ partListEntry };
    }

    @Override
    public boolean next() throws CanceledException {
        return EtkSearchHelper.hasNextPartListEntryForAssemblyDataSet(ds, dsSelectFields, project, multiLanguageCache);
    }
}
