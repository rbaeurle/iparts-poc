/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsRFMEA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsRFMEN;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Importer für DIALOG Migration RPos
 */
public class MigrationDialogRPosImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    // Beschreibung siehe {@link https://confluence.docware.de/confluence/pages/viewpage.action?pageId=17629822}
    // RPOS_NR, RPOS_KEY1, RPOS_POSV, RPOS_ETZ, RPOS_WW, RPOS_SDA bilden den DIALOG-Key (BCTE) vom Nachfolger
    // RPOS_NR, RPOS_VKEY1, RPOS_VPV, RPOS_VETZ, RPOS_VWW, RPOS_VSDA bilden den DIALOG-Key (BCTE) vom Vorgänger
    public static final String RPOS_NR = "RPOS_NR";       // Baureihe
    public static final String RPOS_KEY1 = "RPOS_KEY1";   // HM/M/SM-Schlüssel vom Nachfolger
    public static final String RPOS_KEY2 = "RPOS_KEY2";
    public static final String RPOS_KEY3 = "RPOS_KEY3";
    public static final String RPOS_POSV = "RPOS_POSV";   // Positionsvariante vom Nachfolger
    public static final String RPOS_ETZ = "RPOS_ETZ";     // ETZ vom Nachfolger
    public static final String RPOS_WW = "RPOS_WW";       // WW vom Nachfolger
    public static final String RPOS_SDA = "RPOS_SDA";     // SDA vom Nachfolger
    public static final String RPOS_POS = "RPOS_POS";     // Reihenfolgenummer der Ersetzung
    public static final String RPOS_SNR = "RPOS_SNR";     // Teilenummer vom Nachfolger
    public static final String RPOS_RFMEA = "RPOS_RFMEA"; // RFMEA-Flags
    public static final String RPOS_RFMEN = "RPOS_RFMEN"; // RFMEN-Flags
    public static final String RPOS_VSNR = "RPOS_VSNR";   // Teilenummer vom Vorgänger
    public static final String RPOS_VKEY1 = "RPOS_VKEY1"; // HM/M/SM-Schlüssel vom Vorgänger
    public static final String RPOS_VPV = "RPOS_VPV";     // Positionsvariante vom Vorgänger
    public static final String RPOS_VETZ = "RPOS_VETZ";   // ETZ vom Vorgänger
    public static final String RPOS_VWW = "RPOS_VWW";     // WW vom Vorgänger
    public static final String RPOS_VSDA = "RPOS_VSDA";   // SDA vom Vorgänger
    public static final String RPOS_VKEM = "RPOS_VKEM";
    public static final String RPOS_EDAT = "RPOS_EDAT";
    public static final String RPOS_ADAT = "RPOS_ADAT";

    private String tableName;
    private String[] headerNames;
    private String[] primaryKeysWithoutHeader;
    private HashMap<String, String> mapping;
    private boolean doBufferSave = true;

    public MigrationDialogRPosImporter(EtkProject project, boolean withHeader) {
        super(project, "DIALOG RPOS", withHeader,
              new FilesImporterFileListType("table", "!!DIALOG RPOS", true, false, false, new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_CSV }));
        initMapping();
    }

    private void initMapping() {
        this.tableName = "table";

        primaryKeysWithoutHeader = new String[]{};
        headerNames = new String[]{ RPOS_NR, RPOS_KEY1, RPOS_KEY2, RPOS_KEY3, RPOS_POSV, RPOS_ETZ, RPOS_WW, RPOS_SDA,
                                    RPOS_POS, RPOS_SNR, RPOS_RFMEA, RPOS_RFMEN, RPOS_VSNR, RPOS_VKEY1,
                                    RPOS_VPV, RPOS_VETZ, RPOS_VWW, RPOS_VSDA, RPOS_VKEM, RPOS_EDAT, RPOS_ADAT };

        mapping = new HashMap<>();
        // Alle Felder vom Primärschlüssel werden über diesen gesetzt und müssen nicht ins Mapping aufgenommen werden
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysWithoutHeader);

        String[] mustHaveData = new String[]{ RPOS_NR, RPOS_KEY1, RPOS_POSV, RPOS_POS, RPOS_SNR, RPOS_VKEY1, RPOS_VPV };
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
        //Alle notwendigen Werte im Record vorhanden und die Muss-Felder auch gefüllt?
        List<String> warnings = new DwList<>();
        if (!importer.isRecordValid(importRec, warnings)) {
            // An dieser Stelle ist explizit keine Warnung erwünscht.
            // Erzeugt zu viele Log-Ausgaben.
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
        RPosImportHelper importHelper = new RPosImportHelper(getProject(), mapping, tableName);

        // Wenn es sich um eine Pseudo-Ersetzung handelt, ist die Vorgänger-Teilenummer nicht gesetzt.
        String predecessor = importHelper.handleValueOfSpecialField(RPOS_VSNR, importRec).trim(); // Teilenummer vom Vorgänger
        String successor = importHelper.handleValueOfSpecialField(RPOS_SNR, importRec).trim();    // Teilenummer vom Nachfolger

        String rfmeaValue = importHelper.handleValueOfSpecialField(RPOS_RFMEA, importRec);
        iPartsRFMEA rfmea = new iPartsRFMEA(rfmeaValue);
        String rfmenValue = importHelper.handleValueOfSpecialField(RPOS_RFMEN, importRec);
        iPartsRFMEN rfmen = new iPartsRFMEN(rfmenValue);

        // Wenn es sich um eine <ECHTE> Ersetzung handelt
        if (!predecessor.isEmpty()) {
            iPartsCatalogImportWorker.Replace_Elem replaceElem = importHelper.buildReplaceElem(this, importRec, recordNo);
            if (!replaceElem.isValid()) {
                reduceRecordCount();
                return;
            }
            replaceElem.entryPos = importHelper.handleValueOfSpecialField(RPOS_POS, importRec);
            replaceElem.entryRFMEA = rfmeaValue;
            replaceElem.entryRFMEN = rfmenValue;

            // "PEM bis auswerten" am Vorgänger gültig?
            if (rfmea.isEvalPEMToForRealReplacement()) {
                iPartsCatalogImportWorker.PEMEvaluationEntry predecessorItem = new iPartsCatalogImportWorker.PEMEvaluationEntry();
                predecessorItem.setEvalPEMTo(true);
                predecessorItem.setMatNo(predecessor); // Vorgänger-Materialnummer

                // Vorgänger-BCTE-Schlüssel ist hier irrelevant, weil dieser ja bereits der Schlüssel beim Aufruf von addPEMEvaluationEntry()
                // ist und beim Setzen vom Flag "PEM BIS auswerten" am Vorgänger nur geprüft werden muss, ob der Vorgänger
                // nicht identisch zum Nachfolger ist (inkl. SDA, was bei replaceElem.replaceEntry NICHT vorhanden ist)
                predecessorItem.setSuccessorBCTEKey(importHelper.getBCTEKeyReplacePart(this, importRec, recordNo, true)); // Nachfolger-BCTE-Schlüssel (inkl. SDA)
                predecessorItem.setWithSDA(replaceElem.withSDA);

                addPEMEvaluationEntry(replaceElem.originalEntry.dialogBCTEKey, predecessorItem);
            }

            // "PEM ab auswerten" am Nachfolger gültig?
            if (iPartsReplacementHelper.isEvalPEMFrom(rfmea, rfmen)) {
                iPartsCatalogImportWorker.PEMEvaluationEntry successorItem = new iPartsCatalogImportWorker.PEMEvaluationEntry();
                successorItem.setEvalPEMFrom(true);
                successorItem.setMatNo(successor); // Nachfolger-Materialnummer

                // Nachfolger-BCTE-Schlüssel ist hier irrelevant, weil dieser ja bereits der Schlüssel beim Aufruf von addPEMEvaluationEntry()
                // ist und beim Setzen vom Flag "PEM AB auswerten" am Nachfolger nur geprüft werden muss, ob der Vorgänger
                // nicht identisch zum Nachfolger ist (inkl. SDA)
                successorItem.setPredecessorBCTEKey(replaceElem.originalEntry.dialogBCTEKey); // Vorgänger-BCTE-Schlüssel (immer inkl. SDA)
                successorItem.setWithSDA(replaceElem.withSDA);

                addPEMEvaluationEntry(replaceElem.replaceEntry.dialogBCTEKey, successorItem);
            }

            List<iPartsCatalogImportWorker.Replace_Elem> replaceList = getCatalogImportWorker().getBCTE_ReplaceMap().get(replaceElem.originalEntry.dialogBCTEKey);
            if (replaceList == null) {
                replaceList = new DwList<>();
                getCatalogImportWorker().getBCTE_ReplaceMap().put(replaceElem.originalEntry.dialogBCTEKey, replaceList);
            }
            replaceList.add(replaceElem);
            Collections.sort(replaceList, new Comparator<iPartsCatalogImportWorker.Replace_Elem>() {
                // Sortierung nach entryPos (RPOS_POS)
                @Override
                public int compare(iPartsCatalogImportWorker.Replace_Elem o1, iPartsCatalogImportWorker.Replace_Elem o2) {
                    return o1.entryPos.compareTo(o2.entryPos);
                }
            });
        } else { // <PSEUDO> Ersetzung!
            iPartsCatalogImportWorker.PEMEvaluationEntry evaluationItem = new iPartsCatalogImportWorker.PEMEvaluationEntry();
            evaluationItem.setMatNo(successor);

            // PEM BIS am aktuellen Teil gültig
            if (rfmea.isEvalPEMToForPseudoReplacement()) {
                evaluationItem.setEvalPEMTo(true);
            }

            // PEM AB am aktuellen Teil gültig
            if (rfmen.isNotReplaceable()) {
                evaluationItem.setEvalPEMFrom(true);
            }

            iPartsDialogBCTEPrimaryKey bcteKeyReplacePart = importHelper.getBCTEKeyReplacePart(this, importRec, recordNo, true);
            evaluationItem.setWithSDA(true); // Für Pseudo-Ersetzungen eigentlich irrelevant, ist aber so sauberer
            addPEMEvaluationEntry(bcteKeyReplacePart, evaluationItem);
        }
    }

    private void addPEMEvaluationEntry(iPartsDialogBCTEPrimaryKey bcteSearchKey, iPartsCatalogImportWorker.PEMEvaluationEntry pemEvaluationEntry) {
        // Liste der PEMEvaluationEntries zum BCTE-Schlüssel suchen
        List<iPartsCatalogImportWorker.PEMEvaluationEntry> evalList = getCatalogImportWorker().getBCTE_PEMEvaluationList().get(bcteSearchKey);
        // Gibt's noch nicht? ==> neu anlegen...
        if (evalList == null) {
            evalList = new DwList<>();
            // ... und einhängen
            getCatalogImportWorker().getBCTE_PEMEvaluationList().put(bcteSearchKey, evalList);
        }
        // Den PEMEvaluationEntry zur Liste hinzufügen
        evalList.add(pemEvaluationEntry);
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

    private class RPosImportHelper extends MADImportHelper {

        public RPosImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
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
            if (sourceField.equals(RPOS_ADAT) || sourceField.equals(RPOS_EDAT) || sourceField.equals(RPOS_SDA) || sourceField.equals(RPOS_VSDA)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(RPOS_ETZ) || sourceField.equals(RPOS_VETZ)) {
                value = StrUtils.prefixStringWithCharsUpToLength(value, '0', 3); // in CSV-Datei z.B. 1 -> 001
            } else if (sourceField.equals(RPOS_SNR) || sourceField.equals(RPOS_VSNR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            } else if (sourceField.equals(RPOS_RFMEA) || sourceField.equals(RPOS_RFMEN)) {
                return value; // bei RFMEA und RFMEN nicht trimmen, weil diese Felder mehrere Attribute enthalten, die auch leer sein können
            }
            return value.trim();
        }

        public iPartsDialogBCTEPrimaryKey getBCTEKeyReplacePart(AbstractDataImporter importer, Map<String, String> importRec,
                                                                int recordNo, boolean withSDA) {
            // DAIMLER-3449 AK1: Die Prüfung der Ersetzung erfolgt für alle Änderungsstände der Stückliste (ohne SDA-Abgleich)
            // DAIMLER-6354: Bei Pseudo-Ersetzungen muss SDA allerdings berücksichtigt werden
            return getPartListPrimaryBCTEKey(importer, recordNo, handleValueOfSpecialField(RPOS_NR, importRec),
                                             handleValueOfSpecialField(RPOS_KEY1, importRec), handleValueOfSpecialField(RPOS_POSV, importRec),
                                             handleValueOfSpecialField(RPOS_WW, importRec), handleValueOfSpecialField(RPOS_ETZ, importRec),
                                             "", withSDA ? handleValueOfSpecialField(RPOS_SDA, importRec) : "");
        }

        public iPartsDialogBCTEPrimaryKey getBCTEKeyOriginalPart(AbstractDataImporter importer, Map<String, String> importRec, int recordNo) {
            // DAIMLER-3449 AK1: Die Prüfung der Ersetzung erfolgt für alle Änderungsstände der Stückliste (ohne SDA-Abgleich)
            return getPartListPrimaryBCTEKey(importer, recordNo, handleValueOfSpecialField(RPOS_NR, importRec),
                                             handleValueOfSpecialField(RPOS_VKEY1, importRec), handleValueOfSpecialField(RPOS_VPV, importRec),
                                             handleValueOfSpecialField(RPOS_VWW, importRec), handleValueOfSpecialField(RPOS_VETZ, importRec),
                                             "", handleValueOfSpecialField(RPOS_VSDA, importRec));
        }

        public iPartsCatalogImportWorker.Replace_Elem buildReplaceElem(AbstractDataImporter importer, Map<String, String> importRec, int recordNo) {
            String originalMatNo = handleValueOfSpecialField(RPOS_VSNR, importRec);
            iPartsDialogBCTEPrimaryKey originalBCTE = getBCTEKeyOriginalPart(importer, importRec, recordNo);
            String replaceMatNo = handleValueOfSpecialField(RPOS_SNR, importRec);
            // Erst einmal den BCTE Schlüssel mit allen Elementen erzeugen (für die SDA Prüfung)
            iPartsDialogBCTEPrimaryKey replaceBCTEWithSDA = getBCTEKeyReplacePart(importer, importRec, recordNo, true);
            boolean withSDA = !handleValueOfSpecialField(RPOS_VSDA, importRec).trim().isEmpty() || (replaceMatNo.equals(originalMatNo) && originalBCTE.equals(replaceBCTEWithSDA));
            // Hier den Schlüssel des Nachfolgers abhängig von "withSDA" erzeugen
            return new iPartsCatalogImportWorker.Replace_Elem(withSDA ? replaceBCTEWithSDA : getBCTEKeyReplacePart(importer, importRec, recordNo, false),
                                                              replaceMatNo,
                                                              originalBCTE,
                                                              originalMatNo,
                                                              withSDA);
        }
    }
}