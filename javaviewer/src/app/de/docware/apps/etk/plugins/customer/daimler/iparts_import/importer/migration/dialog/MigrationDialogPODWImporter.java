/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsFactoryDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.collections.diskmappedlist.DiskMappedKeyValueEntry;
import de.docware.util.collections.diskmappedlist.compare.DiskMappedKeyValueListCompare;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Importer für die Werkseinsatzdaten einer Baureihe (Teil des Baureihenimporters)
 */
public class MigrationDialogPODWImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {
//    public static final String TABLE_DA_MAD_FACTORY_ADOPTION = "DA_FACT_ADOPT";

    // Feldnamen in CSV-Header
    // Bedeutung und Verarbeitungsregeln siehe  https://confluence.docware.de/confluence/pages/viewpage.action?pageId=15008172
    public static final String PODW_HERK = "PODW_HERK";  // nicht verarbeiten
    public static final String PODW_NR = "PODW_NR";
    public static final String PODW_KEY1 = "PODW_KEY1";
    public static final String PODW_KEY2 = "PODW_KEY2";
    public static final String PODW_KEY3 = "PODW_KEY3";
    public static final String PODW_POSV = "PODW_POSV";
    public static final String PODW_ETZ = "PODW_ETZ";
    public static final String PODW_WW = "PODW_WW";
    public static final String PODW_AA = "PODW_AA";
    public static final String PODW_SDA = "PODW_SDA";
    public static final String PODW_WERK = "PODW_WERK";
    public static final String PODW_ADAT1 = "PODW_ADAT1";
    public static final String PODW_PTAB = "PODW_PTAB";
    public static final String PODW_PEMA = "PODW_PEMA";
    public static final String PODW_PTBI = "PODW_PTBI";
    public static final String PODW_PEMB = "PODW_PEMB";
    public static final String PODW_SPKZ1 = "PODW_SPKZ1";
    public static final String PODW_SPKZ2 = "PODW_SPKZ2";   // keine Entsprechung in DA_FACTORY_DATA gefunden
    public static final String PODW_STCA = "PODW_STCA";
    public static final String PODW_STCB = "PODW_STCB";
    public static final String PODW_BZA = "PODW_BZA";   // keine Entsprechung in DA_FACTORY_DATA gefunden
    public static final String PODW_LKG = "PODW_LKG";   // keine Entsprechung in DA_FACTORY_DATA gefunden
    public static final String PODW_ENRA = "PODW_ENRA"; // nicht verarbeiten
    public static final String PODW_ENRB = "PODW_ENRB"; // nicht verarbeiten
    public static final String PODW_CR = "PODW_CR";
    public static final String PODW_PG = "PODW_PG";
    public static final String PODW_EDAT = "PODW_EDAT";  // nicht verarbeiten
    public static final String PODW_ADAT = "PODW_ADAT";  // nicht verarbeiten

    private String[] headerNames = new String[]{
            PODW_NR,
            PODW_KEY1,
            PODW_KEY2,
            PODW_KEY3,
            PODW_POSV,
            PODW_ETZ,
            PODW_WW,
            PODW_AA,
            PODW_SDA,
            PODW_WERK,
            PODW_ADAT1,
            PODW_PTAB,
            PODW_PEMA,
            PODW_PTBI,
            PODW_PEMB,
            PODW_SPKZ1,
            PODW_SPKZ2,
            PODW_STCA,
            PODW_STCB,
            PODW_BZA,
            PODW_LKG,
            PODW_ENRA,
            PODW_ENRB,
            PODW_CR,
            PODW_PG,
            PODW_EDAT,
            PODW_ADAT };


    private boolean isSingleCall = false;
    private boolean importToDB = true; //sollen die Daten abgespeichert werden?
    private boolean doBufferSave = true;
    private String[] primaryKeysWithHeader, primaryKeysWithoutHeader;
    private HashMap<String, String> mapping;  // Feldname DataObject -> CSV Feldname
    private String tableName;
    private Set<String> seriesFactoryDataHandled = new HashSet<String>();
    private DiskMappedKeyValueListCompare listComp = null;
    private List<iPartsDataFactoryData> totalFactoryDataList = new ArrayList<iPartsDataFactoryData>();

    public MigrationDialogPODWImporter(EtkProject project, boolean withHeader) {
        super(project, "DIALOG PODW", withHeader,
              new FilesImporterFileListType(TABLE_DA_FACTORY_DATA, "!!DIALOG PODW", true, false, false, new String[]{ MimeTypes.EXTENSION_CSV, FILE_EXTENSION_NO_HEADER }));
        initMapping();
    }

    private void initMapping() {
        this.tableName = TABLE_DA_FACTORY_DATA;

        primaryKeysWithHeader = new String[]{ PODW_HERK, PODW_NR, PODW_KEY1, PODW_KEY2, PODW_KEY3, PODW_POSV, PODW_ETZ, PODW_WW, PODW_AA, PODW_SDA, PODW_WERK };  // Schlüsselfelder aus CSV wie in Confluence beschrieben
        primaryKeysWithoutHeader = new String[]{ PODW_NR, PODW_KEY1, PODW_KEY2, PODW_KEY3, PODW_POSV, PODW_ETZ, PODW_WW, PODW_AA, PODW_SDA, PODW_WERK };  // aktuelle Schlüsselfelder bei fehlendem Header ohne PODW_HERK

        mapping = new HashMap<String, String>();
        // Alle Felder vom Primärschlüssel werden über diesen gesetzt und müssen nicht ins Mapping aufgenommen werden
        //FIELD_DFD_SESI (WBCT_SESI) existiert nicht (PODW_KEY2) // brauchen wir laut Herrn Müller aber auch nicht
        //FIELD_DFD_POSP (WBCT_POSP) existiert nicht (PODW_KEY3) // brauchen wir laut Herrn Müller aber auch nicht
        mapping.put(FIELD_DFD_PEMTA, PODW_PTAB);  // (WBCT_PEMTA);
        mapping.put(FIELD_DFD_PEMA, PODW_PEMA);  // (WBCT_PEMA);
        mapping.put(FIELD_DFD_PEMTB, PODW_PTBI);  // (WBCT_PEMTB);
        mapping.put(FIELD_DFD_PEMB, PODW_PEMB);  // (WBCT_PEMB);
        //mapping.put(FIELD_DFD_SPKZ2, PODW_SPKZ2); // (WBCT_SPKZ1); // brauchen wir laut Herrn Müller aber auch nicht
        mapping.put(FIELD_DFD_STCA, PODW_STCA);  // (WBCT_STCA);
        mapping.put(FIELD_DFD_STCB, PODW_STCB);  // (WBCT_STCB);
        //mapping.put(FIELD_DFD_BZA, PODW_BZA);  // (WBCT_BZA); // brauchen wir laut Herrn Müller aber auch nicht
        //mapping.put(FIELD_DFD_STEERING, PODW_LKG);  // (WBCT_L); // brauchen wir laut Herrn Müller aber auch nicht
        mapping.put(FIELD_DFD_CRN, PODW_CR);  // (WBCT_CRN);
        mapping.put(FIELD_DFD_PRODUCT_GRP, PODW_PG);  // (WBCT_PG);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysWithHeader);

        String[] mustHaveData = new String[]{ PODW_NR, PODW_KEY1, PODW_POSV, PODW_AA, PODW_SDA, PODW_WERK };
        importer.setMustHaveData(mustHaveData);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //nur für den EinzelTest (Menu im Plugin)
        if (getCatalogImportWorker() == null) {
            setCatalogImportWorker(new iPartsCatalogImportWorker(getProject(), getDatasetDate()));
            isSingleCall = true;
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (!importer.isRecordValid(importRec, errors)) {
            return false;
        }
        return true;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferSave);
        seriesFactoryDataHandled.clear();
    }

    /**
     * Import eines Records
     * Die Methode müsste eigentlich processRecord() heißen weil hier ein Record nicht importiert sondern nur verarbeitet wird.
     *
     * Wir bekommen mit dem Import alle Datensätze einer BR bzw. mehrerer BR. Gleichzeitig können wir in der DB alle Datensätze
     * einer BR identifizieren. Damit können wir auch Datensätze identifizieren, die nicht mehr in der neuen BR enthalten sind und
     * daher gelöscht werden müssen. In iParts Sprechweise wird das als Differenzupdate bezeichnet.
     * Zu diesem Zweck verwenden wir die Hilfsklasse DiskMappedKeyValueListCompare mit der wir Differenzen zwischen zwei Listen ermitteln können.
     *
     * Vorgehensweise:
     * - alle DB-Datensätze der DB für die zu importierende BR in die erste Liste und in eine Gesamtliste laden.
     * - zu importierenden Record in erster Liste suchen; gefunden?
     * ja: Eintrag in die zweite Liste aufnehmen
     * nein: neuen Eintrag in zweite Liste und Gesamtliste aufnehmen
     * - nachdem alle Records verarbeitet wurden enthält die zweite Liste die aktuelle Liste und die Gesamtliste die aktuelle Liste plus der zu löschenden Einträge
     * - in postImportTask() können die zu löschenden Einträge nun über DiskMappedKeyValueListCompare ermittelt werden und aus der DB gelöscht werden.
     * Die gelöschten Datensätze werden in der Gesamtliste gekennzeichnet.
     * - anschließend wird über die Gesamtliste iteriert und saveToDB() auf die DataObjects gemacht. Dabei wird Update, Insert oder nicht gemacht,
     * je nachdem ob die Datensätze geändert neu oder unverändert sind.
     *
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        FactoryDataImportHelper importHelper = new FactoryDataImportHelper(getProject(), mapping, tableName, withHeader);

        iPartsDialogBCTEPrimaryKey primaryBCTEKey = importHelper.getPrimaryBCTEKey(this, importRec, recordNo);
        if (primaryBCTEKey == null) {
            importHelper.cancelImporterDueToIncorrectBCTEKey(this, recordNo);
            return;
        }
        // Baue den AS-PEM zu Werk Cache für die Rückmeldedaten auf
        handleASPemFactoryCache(importHelper, importRec);

        iPartsFactoryDataId factoryDataASId = importHelper.getFactoryDataId(importRec, primaryBCTEKey);
        String seriesNumber = primaryBCTEKey.seriesNo;
        if (!seriesFactoryDataHandled.contains(seriesNumber)) {  // die Importdaten sind aktuell für eine konkrete BR; falls das evtl. in Zukunft mal
            // anders sein sollte, wäre man hier gerüstet
            // alle Werkseinsatzdaten für die Baureihe laden, die aus der Migration (MAD) importiert wurden
            getMessageLog().fireMessage(translateForLog("!!Es werden die vorhandenen Datensätze für \"%1\" gelesen", seriesNumber),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            iPartsDataFactoryDataList factoryDataList = iPartsDataFactoryDataList.loadFactoryDataListForImport(getProject(), seriesNumber);
            if (listComp == null) {
                // todo kommentieren welche Überlegung hinter diesen Zahlen steckt
                int maxMemoryRows = Math.max(factoryDataList.size() + 100, 20000);
                maxMemoryRows = Math.min(maxMemoryRows, 300000);

                listComp = new DiskMappedKeyValueListCompare(200, 20, maxMemoryRows, true, false, true);
            }
            for (iPartsDataFactoryData factoryData : factoryDataList) {
                int index = totalFactoryDataList.size();
                totalFactoryDataList.add(factoryData);
                listComp.putFirst(factoryData.getAsId().toString("\t"), String.valueOf(index));
            }
            seriesFactoryDataHandled.add(seriesNumber);
            getMessageLog().fireMessage(translateForLog("!!Import wird fortgesetzt"),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
        iPartsDataFactoryData factoryData;
        String index = listComp.getOnlyInFirstItems().get(factoryDataASId.toString("\t"));
        if (StrUtils.isEmpty(index)) {
            factoryData = new iPartsDataFactoryData(getProject(), factoryDataASId);
            factoryData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            int index2 = totalFactoryDataList.size();
            totalFactoryDataList.add(factoryData);
            listComp.putSecond(factoryData.getAsId().toString("\t"), String.valueOf(index2));
        } else {
            factoryData = totalFactoryDataList.get(Integer.parseInt(index));
            listComp.putSecond(factoryData.getAsId().toString("\t"), index);
        }
        importHelper.fillOverrideCompleteDataForMADReverse(factoryData, importRec, iPartsMADLanguageDefs.MAD_DE);
        // Setzen der separaten HM/M/SM Felder
        importHelper.setExtraFields(factoryData, importRec, primaryBCTEKey);

        // Die Importdatenquelle auf MAD setzen
        factoryData.setFieldValue(iPartsConst.FIELD_DFD_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);

        factoryData.setFieldValue(iPartsConst.FIELD_DFD_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);

        // Redundante BCTE-Felder mit den Werten aus der GUID aktualisieren
        factoryData.updateDialogBCTEFields(DBActionOrigin.FROM_EDIT);

    }

    /**
     * Überprüft, ob es sich bei der PEM und dem Werk um gültige AS-PEM Werte handelt. Fals ja, wird die AS-PEM samt
     * dazugehörigen Werk an den {@link iPartsCatalogImportWorker} übergeben.
     *
     * @param importHelper
     * @param importRec
     */
    private void handleASPemFactoryCache(FactoryDataImportHelper importHelper, Map<String, String> importRec) {
        if (getCatalogImportWorker() != null) {
            // mögliche AS-PEM im PEMA Feld
            String pem = importHelper.handleValueOfSpecialField(PODW_PEMA, importRec);
            handleSingleASPemFactoryCacheEntry(importHelper, importRec, pem);
            // mögliche AS-PEM im PEMB Feld
            pem = importHelper.handleValueOfSpecialField(PODW_PEMB, importRec);
            handleSingleASPemFactoryCacheEntry(importHelper, importRec, pem);

        }
    }

    private void handleSingleASPemFactoryCacheEntry(FactoryDataImportHelper importHelper, Map<String, String> importRec, String pem) {
        // Check, ob es sich um eine AS-PEM handelt
        if (importHelper.isASPem(pem)) {
            String factory = importHelper.handleValueOfSpecialField(PODW_WERK, importRec);
            if (StrUtils.isValid(factory)) {
                String br = importHelper.handleValueOfSpecialField(PODW_NR, importRec);
                String aa = importHelper.handleValueOfSpecialField(PODW_AA, importRec);
                importHelper.addASPemToFactoriesCacheEntry(getCatalogImportWorker().getAsPemToFactoriesMap(), pem, br, aa, factory);
                // falls AA vorhanden benötigen wir auch noch einen Cache-Eintrag ohne AA
                if (StrUtils.isValid(aa)) {
                    importHelper.addASPemToFactoriesCacheEntry(getCatalogImportWorker().getAsPemToFactoriesMap(), pem, br, "", factory);
                }
            }
        }
    }

    /**
     * Siehe Kommentar zu importRecord()
     */
    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (isSingleCall) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
            if (importToDB) {
                int counter = 0;

                // Alle Werkseinsatzdaten aus der Vergleichsliste entfernen, die nur in der DB vorhanden sind, aber NICHT
                // von MAD importiert wurden -> diese dürfen auch nicht gelöscht werden
                Iterator<DiskMappedKeyValueEntry> iterator = listComp.getOnlyInFirstItems().getIterator();
                while (iterator.hasNext()) {
                    String index = iterator.next().getValue();
                    iPartsDataFactoryData factoryData = totalFactoryDataList.get(Integer.parseInt(index));
                    if (factoryData.getSource() != iPartsImportDataOrigin.MAD) {
                        iterator.remove();
                    }
                }

                int count = listComp.getOnlyInFirstItems().size();
                if (count > 0) {
                    getMessageLog().fireMessage(translateForLog("!!Es werden %1 Datensätze gelöscht", String.valueOf(count)),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    getMessageLog().fireProgress(0, count, "", true, false);
                    //Lösche Datensätze
                    Iterator<DiskMappedKeyValueEntry> iter = listComp.getOnlyInFirstItems().getIterator();
                    while (iter.hasNext()) {
                        if (Thread.currentThread().isInterrupted()) {
                            cancelImport("!!Import-Thread wurde frühzeitig beendet");
                            return;
                        }
                        DiskMappedKeyValueEntry entry = iter.next();
                        String index = entry.getValue();
                        iPartsDataFactoryData factoryData = totalFactoryDataList.get(Integer.parseInt(index));
                        factoryData.deleteFromDB(true);
                        factoryData.clear(DBActionOrigin.FROM_EDIT);
                        counter++;
                        getMessageLog().fireProgress(counter, count, "", true, true);
                    }
                    getMessageLog().hideProgress();
                }
                listComp.cleanup();

                counter = 0;
                count = totalFactoryDataList.size();
                getMessageLog().fireMessage(translateForLog("!!Speichern der Datensätze"),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                getMessageLog().fireProgress(0, count, "", true, false);
                for (iPartsDataFactoryData factoryData : totalFactoryDataList) {
                    if (Thread.currentThread().isInterrupted()) {
                        cancelImport("!!Import-Thread wurde frühzeitig beendet");
                        return;
                    }
                    //die vorher gelöschten müssen nicht importiert werden
                    if (!factoryData.getAsId().isEmpty()) {
                        saveToDB(factoryData);
                    }
                    counter++;
                    getMessageLog().fireProgress(counter, count, "", true, true);
                }
                getMessageLog().hideProgress();
            }
        }
        listComp.cleanup();
        super.postImportTask();
    }

    /**
     * Löscht alle vorhandenen Daten für den übergebenen Import-Dateilisten-Typ.
     * Diese Methode betrifft nur den Import per Dialog, nicht per MQ.
     * Wenn das Löschen der vorhandenen Daten nicht erlaubt sein sein, gibt man false zurück.
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


    private class FactoryDataImportHelper extends MADImportHelper {

        private boolean withHeader;

        public FactoryDataImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName, boolean withHeader) {
            super(project, mapping, tableName);
            this.withHeader = withHeader;
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            // todo es ist inkonsequent einerseits die Substring-Ersetzung zumachen und dann andererseits NICHT mehr weiterzumachen
            // ich für meinen Teil kann mir nicht vorstellen dass es Werte geben soll wo NULL drinsteht und man trotzdem noch sinnvoll weitermachen soll
            // d.h. aus meiner Sicht würde ich die solche Werte durch Leerstring ersetzen
            if (StrUtils.stringContains(value, MAD_NULL_VALUE)) {
                // die folgende Ersetzung war schon an anderer Stelle so zu finden
                value = StrUtils.replaceSubstring(value, MAD_NULL_VALUE, "").trim();
            } else if (sourceField.equals(PODW_ADAT1) || sourceField.equals(PODW_SDA) || sourceField.equals(PODW_PTAB) || sourceField.equals(PODW_PTBI)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(PODW_ETZ)) {
                value = StrUtils.prefixStringWithCharsUpToLength(value, '0', 3); // in CSV-Datei z.B. 1 -> 001
            }

            return value.trim();
        }


        public void setExtraFields(EtkDataObject dataObject, Map<String, String> importRec, iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
            //i.A. nichts zu tun
        }

        public iPartsDialogBCTEPrimaryKey getPrimaryBCTEKey(AbstractDataImporter importer, Map<String, String> importRec, int recordNo) {
            return getPartListPrimaryBCTEKey(importer, recordNo, handleValueOfSpecialField(PODW_NR, importRec),
                                             handleValueOfSpecialField(PODW_KEY1, importRec), handleValueOfSpecialField(PODW_POSV, importRec),
                                             handleValueOfSpecialField(PODW_WW, importRec), handleValueOfSpecialField(PODW_ETZ, importRec),
                                             handleValueOfSpecialField(PODW_AA, importRec), handleValueOfSpecialField(PODW_SDA, importRec));
        }

        public iPartsFactoryDataId getFactoryDataId(Map<String, String> importRec, iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
            // Bei leerem ADAT handelt es sich um AS-Werkseinsatzdaten und sonst um Produktions-Einsatzdaten
            String adat = handleValueOfSpecialField(PODW_ADAT1, importRec);
            return new iPartsFactoryDataId(primaryBCTEKey.createDialogGUID(),
                                           handleValueOfSpecialField(PODW_WERK, importRec),
                                           handleValueOfSpecialField(PODW_SPKZ1, importRec),
                                           adat,
                                           adat.isEmpty() ? iPartsFactoryDataTypes.FACTORY_DATA_AS.getDbValue() : iPartsFactoryDataTypes.FACTORY_DATA_CONSTRUCTION.getDbValue());
        }
    }

}
