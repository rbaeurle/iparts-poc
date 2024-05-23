/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListSelectFormConnector;

/**
 * Eigene Implementierung des {@link AssemblyListSelectFormConnector} für iParts.
 * Wird benötigt für Unterdialoge des {@link iPartsRelatedInfoFilterReasonDataForm}, um dort die Filterung auszuschalten.
 */
public class iPartsAssemblyListSelectFormConnectorWithFilterSettings extends AssemblyListSelectFormConnector implements iPartsAssemblyListFormFilterActiveIConnector {

    private boolean isFilterActive = true;

    /**
     * Liefert zurück, ob die Filterung aktiv ist für den übergebenen {@link AbstractJavaViewerFormIConnector}, wobei das
     * Flag {@link #isFilterActive} ausgewertet wird, wenn es sich um eine Instanz von dieser Klasse handelt.
     *
     * @param connector
     * @return
     */
    public static boolean isFilterActive(AbstractJavaViewerFormIConnector connector) {
        if (connector instanceof iPartsAssemblyListFormFilterActiveIConnector) {
            return ((iPartsAssemblyListFormFilterActiveIConnector)connector).isFilterActive();
        } else {
            return true;
        }
    }

    /**
     * @param owner
     */
    public iPartsAssemblyListSelectFormConnectorWithFilterSettings(AbstractJavaViewerFormIConnector owner) {
        super(owner);
    }

    public void setFilterActive(boolean filterActive) {
        isFilterActive = filterActive;
    }

    public boolean isFilterActive() {
        return isFilterActive;
    }
}
