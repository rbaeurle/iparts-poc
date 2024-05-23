/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.tests;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsVS2USDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.ModuleRelationImporter;
import de.docware.util.test.AbstractTest;

/**
 * Testet den {@link ModuleRelationImporter} samt {@link iPartsVS2USDataId}
 */
public class TestModuleRelationImporter extends AbstractTest {

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    public void testModuleRelationImporter() {

        iPartsVS2USDataId id = new iPartsVS2USDataId("1", "2", "3", "4", "5", "6");
        String tmp = id.toString();
        assertEquals(tmp, "DA_iPartsVS2USDataId, \"1\", \"2\", \"3\", \"4\", \"5\", \"6\"");

        // vehicleSeries, posBez, posVar, ausfuehrungsArt, aggregatBaureihe, kemFrom
        id = new iPartsVS2USDataId("VehicleSeries", "Position", "Positionsvariante", "Ausfuehrungsart", "Aggregatbaureihe", "KEM-from");
        assertEquals(id.getVehicleSeries(), "VehicleSeries");
        assertEquals(id.getPosBez(), "Position");
        assertEquals(id.getPosVar(), "Positionsvariante");
        assertEquals(id.getAusfuehrungsArt(), "Ausfuehrungsart");
        assertEquals(id.getAggregatBaureihe(), "Aggregatbaureihe");
        assertEquals(id.getDateFrom(), "KEM-from");


        ModuleRelationImporter importer = new ModuleRelationImporter(null, ModuleRelationImporter.IMPORT_TABLENAME_X6E);
        assertNotNull("Failed to create object [importer]", importer);
    }

}
