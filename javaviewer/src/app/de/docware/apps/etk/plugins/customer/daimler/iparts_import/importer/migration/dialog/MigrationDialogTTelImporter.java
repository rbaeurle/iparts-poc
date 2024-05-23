/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importer für DIALOG Migration TTel
 */
public class MigrationDialogTTelImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    // Beschreibung siehe {@link https://confluence.docware.de/confluence/pages/viewpage.action?pageId=17629822}
    // TTEL_NR, TTEL_KEY1, TTEL_POSV, TTEL_ETZ, TTEL_WW, TTEL_SDA bilden den DIALOG-Key (BCTE) vom Nachfolger
    public static final String TTEL_NR = "TTEL_NR"; // Baureihe
    public static final String TTEL_KEY1 = "TTEL_KEY1"; // HM/M/SM-Schlüssel vom Nachfolger
    public static final String TTEL_KEY2 = "TTEL_KEY2";
    public static final String TTEL_KEY3 = "TTEL_KEY3";
    public static final String TTEL_POSV = "TTEL_POSV"; // Positionsvariante vom Nachfolger
    public static final String TTEL_ETZ = "TTEL_ETZ"; // ETZ vom Nachfolger
    public static final String TTEL_WW = "TTEL_WW"; // WW vom Nachfolger
    public static final String TTEL_SDA = "TTEL_SDA"; // SDA vom Nachfolger
    public static final String TTEL_TNR2 = "TTEL_TNR2"; // Teilenummer vom Mitleiferteil
    public static final String TTEL_KZ = "TTEL_KZ";
    public static final String TTEL_MITLPOS = "TTEL_MITLPOS"; // Reihenfolgenummer vom Mitlieferteil
    public static final String TTEL_ME = "TTEL_ME"; // Menge vom Mitlieferteil
    public static final String TTEL_MGKZ = "TTEL_MGKZ";
    public static final String TTEL_DIASDA = "TTEL_DIASDA";
    public static final String TTEL_EDAT = "TTEL_EDAT";
    public static final String TTEL_ADAT = "TTEL_ADAT";
    public static final String TTEL_VSNR = "TTEL_VSNR"; // Teilenummer vom Vorgänger (entspricht RPOS_VSNR)

    private String tableName;
    private String[] headerNames;
    private String[] primaryKeysWithoutHeader;
    private HashMap<String, String> mapping;
    private boolean doBufferSave = true;

    public MigrationDialogTTelImporter(EtkProject project, boolean withHeader) {
        super(project, "DIALOG TTEL", withHeader,
              new FilesImporterFileListType("table", "!!DIALOG TTEL", true, false, false, new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_CSV }));
        initMapping();
    }

    private void initMapping() {
        this.tableName = "table";

        primaryKeysWithoutHeader = new String[]{};
        headerNames = new String[]{ TTEL_NR, TTEL_KEY1, TTEL_KEY2, TTEL_KEY3, TTEL_POSV, TTEL_ETZ, TTEL_WW, TTEL_SDA,
                                    TTEL_TNR2, TTEL_KZ, TTEL_MITLPOS, TTEL_ME, TTEL_MGKZ, TTEL_DIASDA, TTEL_EDAT, TTEL_ADAT, TTEL_VSNR };

        mapping = new HashMap<String, String>();
        // Alle Felder vom Primärschlüssel werden über diesen gesetzt und müssen nicht ins Mapping aufgenommen werden
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysWithoutHeader);

        String[] mustHaveData = new String[]{ TTEL_NR, TTEL_KEY1, TTEL_POSV, TTEL_TNR2, TTEL_MITLPOS, TTEL_VSNR };
        importer.setMustHaveData(mustHaveData);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        // nur für den Einzeltest (Menu im Plugin)
        if (getCatalogImportWorker() == null) {
            setCatalogImportWorker(new iPartsCatalogImportWorker(getProject(), getDatasetDate()));
            isSingleCall = true;
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return true; // ungültige Datensätze sollen nur Warnung und keinen Fehler erzeugen
    }

    @Override
    protected boolean skipRecord(AbstractKeyValueRecordReader importer, Map<String, String> importRec) {
        //Alle notwendigen Werte im Record vorhanden und die Muss-Felder auch gefüllt? Falls nicht: Warnung erzeugen
        List<String> warnings = new ArrayList<String>();
        if (!importer.isRecordValid(importRec, warnings)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 enthält kein gültiges Mitlieferteil und wird übersprungen: %2",
                                                        String.valueOf(importer.getRecordNo()), StrUtils.stringListToString(warnings, "\n")),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return true;
        }
        return false;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        TTelImportHelper importHelper = new TTelImportHelper(getProject(), mapping, tableName);
        iPartsCatalogImportWorker.Include_Elem newIncludeElem = importHelper.buildIncludeElem(this, importRec, recordNo);
        if (!newIncludeElem.isValid()) {
            reduceRecordCount();
            return;
        }

        iPartsCatalogImportWorker.Include_Elem includeElem = getCatalogImportWorker().getBCTE_IncludeMap().get(newIncludeElem.includeElemKey);
        if (includeElem == null) { // Mitlieferteileliste für BCTE-Schlüssel noch nicht vorhanden -> erzeugen
            includeElem = newIncludeElem;
            getCatalogImportWorker().getBCTE_IncludeMap().put(includeElem.includeElemKey, newIncludeElem);
        }

        // Mitlieferteil hinzufügen
        includeElem.addIncludeMatEntry(importHelper.handleValueOfSpecialField(TTEL_TNR2, importRec),
                                       importHelper.handleValueOfSpecialField(TTEL_MITLPOS, importRec),
                                       importHelper.handleValueOfSpecialField(TTEL_ME, importRec));
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (isSingleCall) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf")); // gilt für die ganze Transaktion also auch für die folgende Löschaktion
            }
        }
        super.postImportTask();
    }

    /**
     * Löscht alle vorhandenen Daten für den übergebenen Import-Dateilisten-Typ.
     * Diese Methode betrifft nur den Import per Dialog, nicht per MQ.
     * Wenn das Löschen der vorhandenen Daten nicht erlaubt sein soll, gibt man false zurück.
     * Für Testdaten sollte die Methode implementiert werden.
     *
     * @param importFileType
     * @return {@code true} falls das Entfernen erfolgreich war (allerdings werden wir bei Fehler in eine Exception laufen und nicht nach false; so ist jedenfalls überall implementiert)
     */
    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    /**
     * Importiert die Datei für den übergebenen Import-Dateilisten-Typ.
     *
     * @param importFileType
     * @param importFile
     * @return {@code true} falls der Import erfolgreich war
     */
    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', withHeader, headerNames));
        }
        return false;
    }

    private class TTelImportHelper extends MADImportHelper {

        public TTelImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        /**
         * Spezielle Behandlung für bestimmte Felder (Modifikation des Wertes aus dem Feld im <i>importRecord</i>).
         * <br/>Diese Methode sollte von Importern beim Import aufgerufen werden.
         *
         * @param sourceField
         * @param value
         * @return
         */
        @Override
        public String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(TTEL_ADAT) || sourceField.equals(TTEL_EDAT) || sourceField.equals(TTEL_SDA)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(TTEL_ETZ)) {
                value = StrUtils.prefixStringWithCharsUpToLength(value, '0', 3); // in CSV-Datei z.B. 1 -> 001
            } else if (sourceField.equals(TTEL_TNR2) || sourceField.equals(TTEL_VSNR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            } else if (sourceField.equals(TTEL_ME)) {
                value = StrUtils.removeLeadingCharsFromString(value, '0'); // führende Nullen bei der Menge entfernen
            }
            return value.trim();
        }

        public iPartsDialogBCTEPrimaryKey getBCTEKey(AbstractDataImporter importer, Map<String, String> importRec, int recordNo) {
            return getPartListPrimaryBCTEKey(importer, recordNo, handleValueOfSpecialField(TTEL_NR, importRec),
                                             handleValueOfSpecialField(TTEL_KEY1, importRec), handleValueOfSpecialField(TTEL_POSV, importRec),
                                             handleValueOfSpecialField(TTEL_WW, importRec), handleValueOfSpecialField(TTEL_ETZ, importRec),
                                             "", ""); // ohne handleValueOfSpecialField(TTEL_SDA, importRec), da SDA bei Ersetzungen nicht mehr berücksichtigt wird
        }

        public iPartsCatalogImportWorker.Include_Elem buildIncludeElem(AbstractDataImporter importer, Map<String, String> importRec, int recordNo) {
            String predecessorMatNo = handleValueOfSpecialField(TTEL_VSNR, importRec);
            iPartsCatalogImportWorker.Include_Elem_Key includeElemKey = new iPartsCatalogImportWorker.Include_Elem_Key(predecessorMatNo,
                                                                                                                       getBCTEKey(importer, importRec, recordNo));
            iPartsCatalogImportWorker.Include_Elem include_elem = new iPartsCatalogImportWorker.Include_Elem(includeElemKey);
            return include_elem;
        }
    }
}