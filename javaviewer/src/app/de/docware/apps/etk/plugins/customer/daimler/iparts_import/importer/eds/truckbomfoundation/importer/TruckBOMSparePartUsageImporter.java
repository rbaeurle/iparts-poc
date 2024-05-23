/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getsparepartusage.TruckBOMSingleSparePartUsage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getsparepartusage.TruckBOMSparePartUsageData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getsparepartusage.TruckBOMSparePartUsageVersion;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.BOMSparePartSignsImportHelper;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.StrUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TruckBOMSparePartUsageImporter extends AbstractTruckBOMFoundationJSONImporter {

    public TruckBOMSparePartUsageImporter(EtkProject project) {
        super(project, TRUCK_BOM_FOUNDATION_SPARE_PART_USAGE_IMPORT_NAME, TABLE_MAT);
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected Map<String, AbstractBOMXMLDataImporter> createSubImporter() {
        return new HashMap<>();
    }

    /**
     * Es handelt sich um einen eigenen Importer mit eigener Logik. Hier werden also keine schon bestehenden Importer als Sub-Importer
     * aufgerufen
     *
     * @param response Empfangene response
     * @return war der Import ohne Fehler
     */
    @Override
    protected boolean importJSONResponse(String response) {
        String fileName = getImportName(getProject().getDBLanguage());
        Genson genson = JSONUtils.createGenson(true);
        try {
            TruckBOMSparePartUsageData truckBOMSparePartUsageData = deserializeFromString(genson, response, fileName, TruckBOMSparePartUsageData.class);
            if (truckBOMSparePartUsageData == null) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine gültigen Werte.", fileName);
                return false;
            }
            if (!checkObjectCount(truckBOMSparePartUsageData)) {
                return true;
            }

            // Alle Ersatzteilkennzeichen-Knoten holen
            List<TruckBOMSingleSparePartUsage> singleSparePartUsages = truckBOMSparePartUsageData.getSparePartUsage();
            if ((singleSparePartUsages == null) || (singleSparePartUsages.isEmpty())) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine Daten für Ersatzteilkennzeichen.", fileName);
                return false;
            }

            // Daten aus der TB.f Response zusammensammeln
            Map<String, StringBuilder> matNrToSparePartsMap = new HashMap<>();
            for (TruckBOMSingleSparePartUsage singleSparePartUsage : singleSparePartUsages) {
                String matNr = singleSparePartUsage.getSparePartIdentifier();
                List<TruckBOMSparePartUsageVersion> sparePartUsageVersions = singleSparePartUsage.getSparePartUsageVersion();
                sparePartUsageVersions.sort(Comparator.comparing(TruckBOMSparePartUsageVersion::getVersion));
                // Den Datensatz mit der höchsten Version nehmen
                TruckBOMSparePartUsageVersion sparePartUsageHighestVersion = sparePartUsageVersions.get(sparePartUsageVersions.size() - 1);
                String market = singleSparePartUsage.getSparePartDomainIdentifier();
                // Die Märkte sind zweistellig
                market = StrUtils.leftFill(market, 2, '0');
                String etkzSign = sparePartUsageHighestVersion.getSparePartUsageType();
                // Die Methode, mit der die Ersatzteilkenner in das DB-Format umgewandelt werden, braucht folgendesFormat:
                // MarktETKZMarktETKZ -> z.B 01E02K
                // Pro Teilenummer Markt mit ETKZ in diesem Format speichern
                StringBuilder marketAndSparePartString = matNrToSparePartsMap.computeIfAbsent(matNr, k -> new StringBuilder());
                marketAndSparePartString.append(market);
                marketAndSparePartString.append(etkzSign);
            }
            fireMessage("!!%1 Teilenummern mit %2 Ersatzteilkennzeichen werden verarbeitet",
                        String.valueOf(matNrToSparePartsMap.size()), String.valueOf(singleSparePartUsages.size()));
            BOMSparePartSignsImportHelper sparePartSignsImportHelper = new BOMSparePartSignsImportHelper(getProject(), iPartsConst.TABLE_MAT);
            int importRecordCount = 0;
            for (String matNr : matNrToSparePartsMap.keySet()) {
                StringBuilder marketAndSparePartString = matNrToSparePartsMap.get(matNr);
                // Falls Stamm nicht existiert -> anlegen -> Markt anlegen -> ETKZ anlegen
                // Falls Stamm existiert, aber der Markt nicht -> mit Markt erweitern
                // Falls Stamm existiert und der Markt existiert -> Markt mit ETKZ aktualisieren
                EtkDataPart part = sparePartSignsImportHelper.handleSpareParts(this, matNr, marketAndSparePartString.toString(), false);
                if (saveToDB(part)) {
                    importRecordCount++;
                }
            }
            logImportRecordsFinished(importRecordCount);
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            fireErrorLF("!!Fehler beim Importieren der Ersatzteilkennzeichen aus TruckBOM.foundation");
            return false;
        }
        return true;
    }
}
