/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.messaging.text;

/**
 * Funktion um DIALOG Direct Werte zu konvertieren
 */
public interface TextToDIALOGValueConverter {

    String convertValue(String value);
}
