/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;

/**
 * Connector-Interface für Edit-Forms in iParts.
 */
public interface EditFormIConnector extends AbstractJavaViewerFormIConnector {

    // Methoden aus AssemblyListFormIConnector
    EtkDataAssembly getCurrentAssembly();

    void setCurrentAssembly(EtkDataAssembly value);

    /**
     * Ist ein Autoren-Auftrag aktiv für das Editieren und der eingeloggte Benutzer hat auch die entsprechenden Rechte?
     *
     * @return
     */
    boolean isAuthorOrderValid();

    void clearFilteredEditPartListEntries();
}
