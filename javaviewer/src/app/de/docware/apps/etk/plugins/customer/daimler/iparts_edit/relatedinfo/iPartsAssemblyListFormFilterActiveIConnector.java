/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;

/**
 * Interface zur Erweiterung vom {@link AssemblyListFormIConnector} um ein Flag zur Filterung.
 */
public interface iPartsAssemblyListFormFilterActiveIConnector extends AssemblyListFormIConnector {

    void setFilterActive(boolean filterActive);

    boolean isFilterActive();
}
