/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer;

import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMSingleKEM;
import de.docware.util.file.DWFile;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

/**
 * Abstrakter KeyValue-Reader für die TruckBOM.foundation Sub-Importer, die auf KEM Daten basieren
 */
public abstract class AbstractTruckBOMKeyValueJSONReaderWithKEMData extends AbstractTruckBOMKeyValueJSONReader {

    private final Map<String, TruckBOMSingleKEM> kemFromForParts; // Map mit Id auf "KEM ab" Daten
    private final Map<String, TruckBOMSingleKEM> kemToForParts; // Map mit Id auf "KEM bis" Daten

    public AbstractTruckBOMKeyValueJSONReaderWithKEMData(DWFile savedJSONFile, Map<String, TruckBOMSingleKEM> kemFromForParts,
                                                         Map<String, TruckBOMSingleKEM> kemToForParts,
                                                         int recordCount, String tableName) {
        super(savedJSONFile, recordCount, tableName);
        this.kemFromForParts = kemFromForParts;
        this.kemToForParts = kemToForParts;
    }

    public Map<String, TruckBOMSingleKEM> getKemFromForParts() {
        return kemFromForParts;
    }

    public Map<String, TruckBOMSingleKEM> getKemToForParts() {
        return kemToForParts;
    }

    @Override
    public boolean open() throws IOException, SAXException {
        return super.open() && (getKemFromForParts() != null) && !getKemFromForParts().isEmpty();
    }
}
