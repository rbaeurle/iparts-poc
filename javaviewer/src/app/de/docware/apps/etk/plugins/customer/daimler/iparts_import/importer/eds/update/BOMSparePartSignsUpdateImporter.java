/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.BOMSparePartSignsImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;

import java.util.Map;

/**
 * Änderungsdienstimporter für die Teilestammdaten Ersatzteilkennzeichnung aus der BOM-DB
 */
public class BOMSparePartSignsUpdateImporter extends AbstractBOMXMLDataImporter {

    public static final String IMPORT_TABLENAME = "getT43RTEID";

    private static final String TEID_PART_NUMBER = "PartNumber";
    private static final String TEID_SPARE_PART_IDENTIFIERS = "SparePartIdentifiers";
    private static final String TEID_SPARE_PART_IDENTIFIER = "SparePartIdentifier";
    private static final String TEID_ATT_TYPE = "type";
    private static final String TEID_ATT_ID = "id";

    private BOMSparePartSignsImportHelper helper;// Der Importer hat kein Mapping, somit reicht eine Helper Instanz für den kompletten Import

    public BOMSparePartSignsUpdateImporter(EtkProject project) {
        super(project, "!!BOM Ersatzteilkennzeichnung (TEID)", TABLE_MAT, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_MAT, BOM_SPARE_PART_SIGNS_UPDATE, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }));
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ TEID_PART_NUMBER };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void preImportTask() {
        helper = new BOMSparePartSignsImportHelper(getProject(), getDestinationTable());
        super.preImportTask();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        String partNo = EDSImportHelper.getTrimmedValueFromRecord(importRec, TEID_PART_NUMBER);
        if (StrUtils.isValid(partNo)) {
            // Unvollständige Datensätze (enthalten nur die Teilenummer) einfach überspringen.
            String sparePartIdentifiers = importRec.get(TEID_SPARE_PART_IDENTIFIERS);
            if (sparePartIdentifiers == null) {
                getMessageLog().fireMessage(translateForLog("!!Fehlender Tag \"%1\" bei Teilenummer \"%2\" in Zeile \"%3\", Datensatz wird übersprungen.",
                                                            TEID_SPARE_PART_IDENTIFIERS, String.valueOf(recordNo), partNo), MessageLogType.tmlWarning,
                                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return;
            }

            String signsAndMarkets = getSparePartIdentifierString(importRec, recordNo, partNo);
            if (importToDB && (helper != null)) {
                helper.handleSparePartsImport(this, partNo, signsAndMarkets);
            }
        }
    }

    /**
     * Holt die Liste der Ersatzteilkennzeichen pro Markt aus den Attributen und schreibt sie hintereinander in den
     * Ergebnis-String. Das Attribut "id" muss immer gesetzt sein, das Attribut "type" kann fehlen.
     * Der Datensatz mit dem fehlenden "type" wird übersprungen, ergibt aber ein gültiges Ergebnis.
     * Sollte der Datensatz mit dem fehlenden "type" der einzige Datensatz sein, wird ein gültiger, leerer String
     * zurückgegeben, der letztendlich zum leeren des M_MARKET_ETKZ-Blobs führt.
     * <p>
     * Der Ausgabe-String ist so aufgebaut, wie er im [BOMSparePartSignsImporter] aus dem Import-Feld: [TEID_ET_KENNER]
     * gelesen wird.
     * Es sind immer drei Zeichen:
     * - 2 Zeichen Markt
     * - plus 1 Zeichen Ersatzteilkennzeichen, die dann hintereinander weg in den Ergebnis-String geschrieben werden.
     * <p>
     * Beispiele für den Rückgabewert:
     * - "04E"
     * - "01K02K03K04K10K11K15K"
     *
     * @param importRec
     * @param recordNo
     * @param partNo
     * @return
     */
    private String getSparePartIdentifierString(Map<String, String> importRec, int recordNo, String partNo) {
        StringBuilder sb = new StringBuilder();
        Map<String, EDSImportHelper.ImportRecMultiLevelDataset> multiLevelValues = helper.getMultiLevelValues(TEID_SPARE_PART_IDENTIFIERS, TEID_SPARE_PART_IDENTIFIER, importRec, true);
        if ((multiLevelValues != null) && !multiLevelValues.isEmpty()) {
            for (EDSImportHelper.ImportRecMultiLevelDataset multilevelData : multiLevelValues.values()) {
                String market = multilevelData.getAttributes().get(TEID_ATT_ID);
                String sparePartSign = multilevelData.getAttributes().get(TEID_ATT_TYPE);
                if (StrUtils.isValid(market, sparePartSign)) {
                    sb.append(market);         // id
                    sb.append(sparePartSign);  // type
                } else {
                    // Fehlermeldung ins Logfile schreiben
                    if (market == null) {
                        getMessageLog().fireMessage(translateForLog("!!Fehlendes Attribut \"%1\" Teilenummer \"%2\" in Zeile \"%3\"",
                                                                    TEID_ATT_ID, String.valueOf(recordNo), partNo), MessageLogType.tmlWarning,
                                                    MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                    }
                }
            }
        }
        return sb.toString();
    }
}
