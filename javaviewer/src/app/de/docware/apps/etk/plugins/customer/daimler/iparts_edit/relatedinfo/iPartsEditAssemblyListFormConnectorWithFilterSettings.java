/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation.iPartsEditAssemblyListValidationForm;

/**
 * Erweiterung vom {@link EditModuleFormConnector} für das {@link iPartsEditAssemblyListValidationForm}, um dort die Filterung
 * ausschalten zu können.
 */
public class iPartsEditAssemblyListFormConnectorWithFilterSettings extends EditModuleFormConnector implements iPartsAssemblyListFormFilterActiveIConnector {

    private boolean isFilterActive = true;

    /**
     * @param owner
     */
    public iPartsEditAssemblyListFormConnectorWithFilterSettings(AbstractJavaViewerFormIConnector owner) {
        super(owner);
    }

    public void setFilterActive(boolean filterActive) {
        isFilterActive = filterActive;
    }

    public boolean isFilterActive() {
        return isFilterActive;
    }
}
