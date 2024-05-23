/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket;

public interface WorkbasketSearchCallback {

    boolean searchWasCanceled();

    void showProgress(int progress);
}
