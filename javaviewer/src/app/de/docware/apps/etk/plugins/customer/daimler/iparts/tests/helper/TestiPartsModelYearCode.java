/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsModelYearCode;
import de.docware.util.test.AbstractTest;

/**
 * Tests für {@see iPartsModelYearCode}.
 */
public class TestiPartsModelYearCode extends AbstractTest {

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    public void testFindModelYearCode() {
        int index = iPartsModelYearCode.findModelYearCode(";");
        assertEquals(-1, index);

        index = iPartsModelYearCode.findModelYearCode("M01");
        assertEquals(-1, index);

        index = iPartsModelYearCode.findModelYearCode("M01+X801");
        assertEquals(-1, index);

        index = iPartsModelYearCode.findModelYearCode("M01+801");
        assertEquals(1, index);
    }

    public void testModelYearCodePredecessors() {
        String[] predecessors = iPartsModelYearCode.getPredecessors(8, 4, true);
        assertEquals(new String[]{ "808", "807", "806", "805", "804" }, predecessors);

        predecessors = iPartsModelYearCode.getPredecessors(8, 4, false);
        assertEquals(new String[]{ "807", "806", "805", "804" }, predecessors);

        // Modulo für Ringpuffer
        predecessors = iPartsModelYearCode.getPredecessors(1, 4, true);
        assertEquals(new String[]{ "801", "800", "809", "808", "807" }, predecessors);

        predecessors = iPartsModelYearCode.getPredecessors(1, 4, false);
        assertEquals(new String[]{ "800", "809", "808", "807" }, predecessors);


        // Ungültiger Index in die Liste der Modeljahrcodes.
        // ==> Exception wird erwartet.
        try {
            predecessors = new String[]{ "YES! The expected overflow exception has successfully happened!" };
            predecessors = iPartsModelYearCode.getPredecessors(9999, 3, true);
            assertEquals(new String[]{ "Ooops!" }, predecessors);
        } catch (RuntimeException e) {
            // Fehler wurde erwartet
            assertEquals(new String[]{ "YES! The expected overflow exception has successfully happened!" }, predecessors);
        }
    }

    public void testModelYearCodeSuccessors() {
        String[] successors = iPartsModelYearCode.getSuccessors(1, 4, true);
        assertEquals(new String[]{ "801", "802", "803", "804", "805" }, successors);

        successors = iPartsModelYearCode.getSuccessors(1, 4, false);
        assertEquals(new String[]{ "802", "803", "804", "805" }, successors);

        // Modulo für Ringpuffer
        successors = iPartsModelYearCode.getSuccessors(8, 4, true);
        assertEquals(new String[]{ "808", "809", "800", "801", "802" }, successors);

        successors = iPartsModelYearCode.getSuccessors(8, 4, false);
        assertEquals(new String[]{ "809", "800", "801", "802" }, successors);


        // Ungültiger Index in die Liste der Modeljahrcodes.
        // ==> Exception wird erwartet.
        try {
            successors = new String[]{ "YES! The expected negative underflow exception has successfully happened!" };
            successors = iPartsModelYearCode.getSuccessors(-9999, 3, true);
            assertEquals(new String[]{ "Ooops!" }, successors);
        } catch (RuntimeException e) {
            // Fehler wurde erwartet
            assertEquals(new String[]{ "YES! The expected negative underflow exception has successfully happened!" }, successors);
        }
    }
}
