/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

public interface OnFilterDateChangeEvent {

    public void onChangeFromDate(EditFilterDateObject filterDateObject);

    public void onChangeToDate(EditFilterDateObject filterDateObject);

}
