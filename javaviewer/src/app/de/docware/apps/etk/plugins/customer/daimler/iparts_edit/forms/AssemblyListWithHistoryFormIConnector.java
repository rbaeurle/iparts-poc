/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;

import java.util.List;

/**
 * Erweiterung des {@link AssemblyListFormIConnector} um die Fähigkeit PartListEntries direkt übergeben zu können
 */
public interface AssemblyListWithHistoryFormIConnector extends AssemblyListFormIConnector {

    void setCurrentPartListEntries(List<EtkDataPartListEntry> entries);

    void setCurrentPartlistEntriesInvalid();

    String getPartNumber();

    void setPartNumber(String partNumber);
}
