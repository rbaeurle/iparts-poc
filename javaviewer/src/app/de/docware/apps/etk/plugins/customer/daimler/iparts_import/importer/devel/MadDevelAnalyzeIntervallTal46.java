/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.devel;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MadTal40AImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MadTal4XABaseImporter;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Converter ELDAS -> EPC nach Excel, der die Mechanik der normalen Importer verwendet
 * Nur im Developementmodus sichtbar.
 * Da die Importdateien sehr groß sind läuft das nur unter Swing (maximal 100MByte download)
 * Das erstellen der Exceldateien ist sehr speicheraufwendig, deshalb sollte mit einer 64Bit JVM und 4GByte Speicher gearbeitet werden
 */
public class MadDevelAnalyzeIntervallTal46 extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    enum Status {NO_INTERVALL, NO_DIFF, HAS_DIFF}

    class ImportRecordAndStatus {

        Map<String, String> importRec;
        Status status = Status.NO_INTERVALL;
    }

    Map<String, ImportRecordAndStatus> values = new HashMap<String, ImportRecordAndStatus>();

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MadDevelAnalyzeIntervallTal46(EtkProject project) {
        super(project, "TAL46 Analyse",
              new FilesImporterFileListType("MAD-Rohdaten", "!!MAD-Rohdaten", true, false, false, new String[]{ "*" }));
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return true;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {

        if (!importRec.isEmpty()) {
            // Merke die Records in eine Map für die SAA + Hotspot + Teilenummer nur Datensatzart 'D'
            String key = importRec.keySet().iterator().next();

            //ginge auch mit TableAndFieldName.getFirstPart(key); und TableAndFieldName.getSecondPart(key);
            String[] aliasAndField = key.split("\\.");
            String alias = aliasAndField[0];

            // Das KATALOG-Feld ist immer da. Nutze dieses, um die Exceldateien zu splitten
            // und für jeden Katalog eine Exceldatei auszugeben

            String katalogOrSaa;

            katalogOrSaa = importRec.get(alias + "." + MadTal40AImporter.TAL46A_SA_RUMPF);

            if (alias.equals(MadTal40AImporter.SATZART_D)) {
                String mapKey = katalogOrSaa + "|" + importRec.get(alias + "." + MadTal40AImporter.TAL4XA_BT_POS) + "|" + importRec.get(alias + "." + MadTal40AImporter.TAL4XA_TEILENUMMER);

                //String mapKey = katalogOrSaa + "|" + importRec.get(alias + "." + MadTal40AImporter.TAL4XA_LFDNR);

                ImportRecordAndStatus mapValue = values.get(mapKey);
                if (mapValue != null) {
                    if (mapValue.importRec.get(alias + "." + MadTal40AImporter.TAL46A_SA_INTERVALL)
                            .equals(importRec.get(alias + "." + MadTal40AImporter.TAL46A_SA_INTERVALL))) {
                        // Zweimal gleiches Material an gleichem Hotspot -> überspringe diesen Fall, der verfälscht nur das Ergebnis -> Muss man später sehen
                    } else {
                        if (hasDiff(importRec, mapValue.importRec)) {
                            mapValue.status = Status.HAS_DIFF;
                        } else {
                            if (mapValue.status == Status.NO_INTERVALL) {
                                mapValue.status = Status.NO_DIFF;
                            }
                        }
                    }
                } else {
                    mapValue = new ImportRecordAndStatus();
                    mapValue.importRec = importRec;
                    values.put(mapKey, mapValue);
                }
            }
        }


    }

    static String[] ignoreFields = new String[]{ MadTal40AImporter.TAL4XA_MENGE_JE_BAUMUSTER, MadTal40AImporter.TAL46A_SA_INTERVALL, MadTal40AImporter.TAL4XA_LFDNR };

    private boolean hasDiff(Map<String, String> importRec1, Map<String, String> importRec2) {
        boolean result = false;
        for (String key : importRec1.keySet()) {
            boolean ignoreFound = false;
            for (String ignoreString : ignoreFields) {
                if (key.contains(ignoreString)) {
                    ignoreFound = true;
                }
            }

            if (!ignoreFound) {

                String s1 = importRec1.get(key);
                String s2 = importRec2.get(key);
                if (!StrUtils.stringEquals(s1, s2)) {
                    result = true;
                    s1 = StrUtils.replaceSubstring(s1, "\n", " ");
                    s2 = StrUtils.replaceSubstring(s2, "\n", " ");
                    messages.add("Unterschied: " + key + " Record: " + importRec2.get(MadTal40AImporter.SATZART_D + "." + MadTal40AImporter.TAL4XA_LFDNR) + " \"" + s1 + "\" \"" + s2 + "\"");
                }
            }
        }
        return result;
    }


    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    static int allDiff = 0;
    static int allEqual = 0;
    static List<String> messages = new ArrayList<String>();

    @Override
    public boolean finishImport() {
        for (String key : values.keySet()) {
            if (values.get(key).status == Status.NO_DIFF) {
                allEqual++;
            } else if (values.get(key).status == Status.HAS_DIFF) {
                allDiff++;
            }
        }

        getMessageLog().fireMessage("Ergebnis für alle Test seit Neustart:");


        getMessageLog().fireMessage("Alles gleich: " + Integer.toString(allEqual) +
                                    " Unterschiede: " + Integer.toString(allDiff));

        getMessageLog().fireMessage("Detailunterschiede:");

        for (String s : messages) {
            getMessageLog().fireMessage(s);
        }


        return super.finishImport();
    }


    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        return importMasterData(MadTal4XABaseImporter.prepareImporterFixedLength(importFile, MadTal4XABaseImporter.Tal4XAType.TAL46A));
    }

}
