/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.events.GotoPartWithPartialPathEvent;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;

/**
 * Eventklasse um zu einem bestimmten Path zu springen
 * wird benötigt, da sonst immer der GotoPartWithPartialPathEvent im Katalog ausgeführt wird
 */
public class GotoEditPartWithPartialPathEvent extends GotoPartWithPartialPathEvent {

    public GotoEditPartWithPartialPathEvent(NavigationPath path, AssemblyId id, String kLfdNr, boolean markEntryAsSearchResult,
                                            boolean navigateToSubAssembly, AbstractJavaViewerForm sender) {
        super(path, id, kLfdNr, markEntryAsSearchResult, navigateToSubAssembly, sender);
    }
}
