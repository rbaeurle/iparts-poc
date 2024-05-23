/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.mechanic.mainview.connector.MechanicFormConnector;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Spezieller Connector, der einzelne Entries zu einer Baugruppe anzeigt. PartListEntries, die angezeigt werden sollen können direkt übergeben werden
 */
public class AssemblyListWithHistoryFormConnector extends MechanicFormConnector implements AssemblyListWithHistoryFormIConnector {


    private List<EtkDataPartListEntry> currentPartListEntries = new ArrayList<>();
    private String partNumber;

    public AssemblyListWithHistoryFormConnector(AbstractJavaViewerFormIConnector owner, EtkDataAssembly assembly, NavigationPath currentAssemblyPath,
                                                String partNumber) {
        super(owner);
        setCurrentAssembly(assembly);
        setCurrentNavigationPath(currentAssemblyPath);
        setPartNumber(partNumber);
    }

    @Override
    public List<EtkDataPartListEntry> getUnfilteredPartListEntries() {
        return getCurrentPartListEntries();
    }

    @Override
    public void setCurrentPartListEntries(List<EtkDataPartListEntry> entries) {
        currentPartListEntries = entries;
        // setzt kein ChangeFlag, um Rekursionen zu vermeiden
    }

    @Override
    public List<EtkDataPartListEntry> getCurrentPartListEntries() {
        if (currentPartListEntries == null) {
            currentPartListEntries = new ArrayList<>();
        }
        return currentPartListEntries;
    }

    @Override
    public void setCurrentPartlistEntriesInvalid() {
        flagCurrentAssemblyChanged.setChanged();
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }
}
