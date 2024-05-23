/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.tests;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.DIALOGImportHelper;

/**
 * Test-Hilfsklasse um an die protected Methoden in {@link DIALOGImportHelper} zu kommen
 */
public class TestObjectForNumberConverterTest extends iPartsNumberHelper {

    public TestObjectForNumberConverterTest() {
        super();
    }

    public String getResultOfNumberConverterForGivenNumber(String number) {
        return checkNumberInputFormat(number, null);
    }
}
