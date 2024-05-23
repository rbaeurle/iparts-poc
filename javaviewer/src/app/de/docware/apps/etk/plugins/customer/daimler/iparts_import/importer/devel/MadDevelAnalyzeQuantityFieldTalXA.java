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

import java.util.List;
import java.util.Map;


/**
 * Converter ELDAS -> EPC nach Excel, der die Mechanik der normalen Importer verwendet
 * Nur im Developementmodus sichtbar.
 * Da die Importdateien sehr groß sind läuft das nur unter Swing (maximal 100MByte download)
 * Das erstellen der Exceldateien ist sehr speicheraufwendig, deshalb sollte mit einer 64Bit JVM und 4GByte Speicher gearbeitet werden
 */
public class MadDevelAnalyzeQuantityFieldTalXA extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    private MadTal4XABaseImporter.Tal4XAType talType;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MadDevelAnalyzeQuantityFieldTalXA(EtkProject project, MadTal4XABaseImporter.Tal4XAType talType) {
        super(project, talType.toString(),
              new FilesImporterFileListType("MAD-Rohdaten", "!!MAD-Rohdaten", true, false, false, new String[]{ "*" }));

        this.talType = talType;
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

    static int allEqual = 0;
    static int allEqualButSomeNot = 0;
    static int allDiff = 0;

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {

        if (!importRec.isEmpty()) {

            for (String key : importRec.keySet()) {

                //ginge auch mit TableAndFieldName.getFirstPart(key); und TableAndFieldName.getSecondPart(key);
                String[] aliasAndField = key.split("\\.");
                String field = aliasAndField[1];

                if (field.equals(MadTal40AImporter.TAL4XA_MENGE_JE_BAUMUSTER)) {
                    String Menge = importRec.get(key);
                    if (Menge != null) {
                        checkMenge(Menge);
                    }
                }

            }
        }


    }

    private void checkMenge(String menge) {
        List<String> values = StrUtils.toStringList(menge, "\n", true);
        while (values.size() > 0 && values.get(values.size() - 1).isEmpty()) {
            values.remove(values.size() - 1);
        }
        String testValue = "";
        for (String s : values) {
            if (!s.isEmpty()) {
                testValue = s;
                break;
            }
        }

        boolean isDiffAll = false;
        boolean isDiff0 = false;

        for (String s : values) {
            if (s.isEmpty() || s.equals("000")) {
                isDiff0 = true;
            } else {
                if (!s.equals(testValue)) {
                    isDiffAll = true;
                }
            }
        }

        if (isDiffAll) {
            allDiff++;
        } else if (isDiff0) {
            allEqualButSomeNot++;

        } else {
            allEqual++;
        }
    }


    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }


    @Override
    public boolean finishImport() {

        getMessageLog().fireMessage("Ergebnis für alle Test seit Neustart:");


        getMessageLog().fireMessage("Alles gleich: " + Integer.toString(allEqual) +
                                    " Menge gleich: " + Integer.toString(allEqualButSomeNot) +
                                    " Mengen unterschiedlich: " + Integer.toString(allDiff));


        return super.finishImport();
    }


    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        return importMasterData(MadTal4XABaseImporter.prepareImporterFixedLength(importFile, talType));
    }

}
