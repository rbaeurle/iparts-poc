/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events;

/**
 * Interface für das Aktivieren eines Autoren-Auftrags außerhalb von {@link de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditWorkMainForm}
 */
public interface OnActivateAuthorOrderEvent {

    boolean onActivateAuthorOrder();
}
