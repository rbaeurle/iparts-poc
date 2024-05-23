/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.devel;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MadTal40AImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MadTal4XABaseImporter;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.utils.Custom2DStringArray;
import de.docware.framework.utils.Custom2DStringArrayExportToExcel;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Converter ELDAS -> EPC nach Excel, der die Mechanik der normalen Importer verwendet
 * Nur im Developementmodus sichtbar.
 * Da die Importdateien sehr groß sind läuft das nur unter Swing (maximal 100MByte download)
 * Das Erstellen der Exceldateien ist sehr speicheraufwendig, deshalb sollte mit einer 64Bit JVM und 4GByte Speicher gearbeitet werden
 */
public class MadDevelTalXToXlsBaseConverter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    Map<String, Custom2DStringArray> values = new HashMap<String, Custom2DStringArray>();

    private MadTal4XABaseImporter.Tal4XAType talType;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MadDevelTalXToXlsBaseConverter(EtkProject project, String title, MadTal4XABaseImporter.Tal4XAType talType) {
        super(project, title,
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

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {

        if (!importRec.isEmpty()) {
            int index = 0;

            // Merke die Records in mehreren 2DStringArrays und speichere diese später als Excel
            // Der Code hier ist nicht produktiv und deshalb recht schlampig, so dass es geht
            // Sollte später echt importiert werden, dann muss das anders werden
            for (String key : importRec.keySet()) {

                //ginge auch mit TableAndFieldName.getFirstPart(key); und TableAndFieldName.getSecondPart(key);
                String[] aliasAndField = key.split("\\.");
                String alias = aliasAndField[0];
                String field = aliasAndField[1];

                // Das KATALOG-Feld ist immer da. Nutze dieses, um die Exceldateien zu splitten
                // und für jeden Katalog eine Exceldatei auszugeben

                String katalogOrSaa;

                if (talType == MadTal4XABaseImporter.Tal4XAType.TAL40A) {
                    katalogOrSaa = importRec.get(alias + "." + MadTal40AImporter.TAL40A_KATALOG);
                } else {
                    katalogOrSaa = importRec.get(alias + "." + MadTal40AImporter.TAL46A_SA_RUMPF);
                }

                String recordTypeAliasNameAndKatalog = katalogOrSaa + "-" + alias;

                Custom2DStringArray rows = values.get(recordTypeAliasNameAndKatalog);
                if (null == rows) {
                    //neuen Katalog anlegen
                    rows = new Custom2DStringArray();
                    rows.setRowCount(1);
                    values.put(recordTypeAliasNameAndKatalog, rows);
                }

                if (rows.getColCount() <= index + 1) {
                    rows.setColCount(index + 2);
                    rows.setCells(index + 1, 0, field);
                    // Die erste Spalte wird dazubeschummelt
                    rows.setCells(0, 0, "RecordNumber");
                }

                if (index == 0) {
                    rows.setRowCount(rows.getRowCount() + 1);
                }
                String value = importRec.get(key);
                rows.setCells(index + 1, rows.getRowCount() - 1, value.intern());
                rows.setCells(0, rows.getRowCount() - 1, Long.toString(recordNo).intern());
                index++;

            }
        }


    }


    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }


    @Override
    public boolean finishImport() {
        DWFile TempDir = DWFile.createTempDirectory("daim");
        for (String key : values.keySet()) {
            DWFile file = DWFile.get(TempDir, key + ".xlsx");
            getMessageLog().fireMessage(translateForLog("!!Speichere") + " " + file.getPath(), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            Custom2DStringArray custom2DStringArray = values.get(key);
            Custom2DStringArrayExportToExcel exporter = new Custom2DStringArrayExportToExcel(custom2DStringArray);
            exporter.writeExcel(file);
        }
        return super.finishImport();
    }


    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        return importMasterData(MadTal4XABaseImporter.prepareImporterFixedLength(importFile, talType));
    }

}
