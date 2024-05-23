/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.cemat;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsModuleCematId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleCemat;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.cemat.CEMaT;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.cemat.CEMaTTu;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.cemat.CEMaTTuMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractJSONDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWInputStream;
import de.docware.util.misc.CompressionUtils;

import java.io.IOException;
import java.util.*;

/**
 * Importer für die EinPAS-Knoten aus CEMaT
 * <p>
 * Der Importer wird über den RFTS/x-Importmechanismus aufgerufen.
 * Die Importdatei wird im konfigurerten RFTS/x-Eingangsverzeichnis mit dem Dateinamen: [PartMappings_versioned*.zip] erwartet.
 * Die Importdatei ist eine ge-ZIP-te JSON-Datei: [PartMappings_versioned.zip] bzw. entpackt dann: [PartMappings_versioned.json]
 */

public class CematModuleDataImporter extends AbstractJSONDataImporter implements iPartsConst {

    // Anzahl erwarteter Teilstings zur Überprüfung einer "node" : "001.37.20.33"
    private static final int NODE_VALID_TOKEN_COUNT = 4;

    // Längenüberprüfung der Teilstrings einer "node" : "001.37.20.33"
    private static final int VERSION_LENGTH = 3;
    private static final int HG_LENGTH = 2;
    private static final int G_LENGTH = 2;
    private static final int TU_LENGTH = 2;

    // Wertebereichsüberprüfung
    private static final int VERSION_MIN = 1;
    private static final int VERSION_MAX = 999;
    private static final int HG_MIN = 10;
    private static final int HG_MAX = 99;
    private static final int G_MIN = 10;
    private static final int G_MAX = 99;
    private static final int TU_MIN = 1;
    private static final int TU_MAX = 99;

    // Der Trenner zwischen den einzelnen Versionen im CLOB Feld DMC_VERSIONS
    private static final String CEMAT_NODE_DELIMITER = ".";

    private enum NODE_ENUMS {
        VERSION_NODE("!!Version", VERSION_LENGTH, VERSION_MIN, VERSION_MAX),
        HG_NODE("!!HG", HG_LENGTH, HG_MIN, HG_MAX),
        G_NODE("!!G", G_LENGTH, G_MIN, G_MAX),
        TU_NODE("!!TU", TU_LENGTH, TU_MIN, TU_MAX);

        private final String desc;
        private final int length;
        private final int min;
        private final int max;

        NODE_ENUMS(String desc, int length, int min, int max) {
            this.desc = desc;
            this.length = length;
            this.min = min;
            this.max = max;
        }

        public String getDesc() {
            return desc;
        }

        public int getLength() {
            return length;
        }

        public int getMax() {
            return max;
        }

        public int getMin() {
            return min;
        }
    }

    private String currentImportFileName = "";
    private int importRecordCount;
    private int currentTUCounter = 0;
    private String currentModuleNo = "";
    private String currentLfdNr = "";
    private int currentNodeCounter = 0;

    // Die üblichen Steuerelemente
    private boolean importToDB = true;

    public CematModuleDataImporter(EtkProject project) {
        super(project, CEMAT_MODULE_IMPORT_NAME, new FilesImporterFileListType(TABLE_DA_MODULE_CEMAT, CEMAT_MODULE_IMPORT_NAME, true,
                                                                               false, true,
                                                                               new String[]{ MimeTypes.EXTENSION_ZIP }));
        setBufferedSave(true);
    }

    /**
     * Zieltabelle leeren.
     *
     * @param importFileType
     * @return
     */
    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        fireMessage("!!Leeren der Tabelle \"%1\"", TABLE_DA_MODULE_CEMAT);
        long startTime = System.currentTimeMillis();
        getProject().getDB().delete(TABLE_DA_MODULE_CEMAT);
        String timeDurationString = DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, true, false, Language.DE.getCode());
        fireMessage("!!Leeren der Tabelle \"%1\" abgeschlossen in %2", TABLE_DA_MODULE_CEMAT, timeDurationString);
        return true;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        // Caches nicht löschen
        setClearCachesAfterImport(false);
        String fileName = importFile.extractFileName(true);
        try {
            Genson genson = JSONUtils.createGenson(true);
            // Das gleiche TEMP-Directory verwenden wie bei den meisten Daimler-Importern
            DWFile destDir = DWFile.createTempDirectory("daim");
            if (destDir != null) {
                // Die ZIP-Datei entpacken
                if (CompressionUtils.unzipFile(destDir.getAbsolutePath(), importFile.getAbsolutePath(), "UTF-8")) {
                    // Für gewöhnlich wird hier nur eine einzige Datei gefunden, dennoch das rekursive Suchen und die Schleife mit der Verarbeitung wie im ProvalCodeImporter gelassen.
                    List<DWFile> fileList = destDir.listDWFilesRecursively((dir, name) -> StrUtils.stringEndsWith(name, MimeTypes.EXTENSION_JSON, true));
                    fireMessage("!!Starte Import der EinPAS-Knoten aus CEMaT für %1 Importdatei(en)", String.valueOf(fileList.size()));
                    importRecordCount = 0;
                    // Normalerweise ist hier nur eine JSON-Datei entpackt worden.
                    for (DWFile cematJsonFile : fileList) {
                        importCematModuleJSONFile(cematJsonFile, genson);
                    }

                    // Hier muss Post aufgerufen werden, da der Importer nicht über die normale importRecord() Methode
                    // läuft. Die letzten <100 Code werden sonst nicht importiert.
                    postImportTask();
                    if (getErrorCount() == 0) {
                        logImportRecordsFinished(importRecordCount);
                    }

                    // Temporäres Verzeichnis löschen
                    if (!destDir.deleteRecursivelyWithRepeat(5000)) {
                        fireError("!!Fehler beim Löschen des temporären Verzeichnis \"%1\"", destDir.getAbsolutePath());
                        return false;
                    }
                    return true;
                } else {
                    cancelImport(translateForLog("!!Fehler beim Entpacken der CEMaT Importdatei \"%1\"", fileName), MessageLogType.tmlError);
                }
            }
        } catch (IOException e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            fireError("!!Fehler beim Importieren der Datei \"%1\"", fileName);
        }
        return false;
    }

    /**
     * Liest die übergebene Datei ein.
     *
     * @param jsonFile
     * @param genson
     */
    private void importCematModuleJSONFile(DWFile jsonFile, Genson genson) {
        if (jsonFile == null) {
            return;
        }
        currentImportFileName = jsonFile.getName();

        try (DWInputStream inputStream = jsonFile.getInputStream()) {

            fireMessage("!!Starte Import EinPAS-Knoten aus CEMaT aus Datei \"%1\"", currentImportFileName);
            CEMaT cemat = deserializeFromInputStream(genson, inputStream, currentImportFileName, CEMaT.class);
            if (cemat == null) {
                // Fehlermeldung
                fireWarning("!!Die Importdatei \"%1\" enthält keine gültigen Werte und wird übersprungen.", currentImportFileName);
                return;
            }

            currentTUCounter = 0;
            List<CEMaTTu> tuList = getTUs(cemat);
            if (tuList == null) {
                return;
            }

            // Die enthaltenen TUs nacheinander abarbeiten
            int maxTUCount = tuList.size();
            for (CEMaTTu tu : tuList) {

                // Wenn der Anwender während des Imports auf [Abbrechen] gedrückt hat.
                if (isCancelled()) {
                    fireMessage("!!Import durch Benutzereingabe abgebrochen");
                    return;
                }

                // Fortschritt anzeigen
                if ((currentTUCounter % 1000) == 0) {
                    fireProgress(currentTUCounter, maxTUCount);
                }

                // Für Fehlermeldungen den Knoten mitzählen, eine Zeilennummer gibts beim JSON nicht wirklich.
                currentTUCounter++;
                importRecordCount++;

                // Die Werte übernehmen, die Funktionen geben Fehlermeldungen aus, falls etwas nicht gefunden wurde.
                currentModuleNo = getModuleNo(tu);
                currentLfdNr = getLfdNr(tu);
                String currentPartNo = getPartNo(tu);
                List<CEMaTTuMapping> tuMappings = getTuMappings(tu);

                // Nur weitermachen, wenn die Header-Informationen gefunden wurden.
                if ((StrUtils.isValid(currentModuleNo, currentLfdNr, currentPartNo)) && (tuMappings != null)) {

                    // Jetzt die "node"s des "mappings" abklappern und die Versionen aufsammeln
                    currentNodeCounter = 0;
                    Map<iPartsModuleCematId, Set<String>> resultMap = new LinkedHashMap<>();
                    for (CEMaTTuMapping mapping : tuMappings) {

                        // Für Fehlerausgaben die "node" mitzählen
                        currentNodeCounter++;
                        CematNodeHelper helper = new CematNodeHelper(mapping.getNode());
                        if (!helper.isValid()) {
                            reduceRecordCount();
                            continue;
                        }
                        // Jetzt die gefundene Version zur ID in einem Set sammeln
                        iPartsModuleCematId moduleCematId = new iPartsModuleCematId(currentModuleNo, currentLfdNr, helper.getEinPasId());
                        Set<String> versionSet = resultMap.computeIfAbsent(moduleCematId, k -> new TreeSet<>());
                        // Aufsteigend sortieren ==> TreeSet
                        versionSet.add(helper.getVersion());
                    }

                    // Nun die aufgebaute Ergebnisliste abarbeiten
                    for (Map.Entry<iPartsModuleCematId, Set<String>> entry : resultMap.entrySet()) {
                        iPartsModuleCematId moduleCematId = entry.getKey();
                        Set<String> versionSet = entry.getValue();

                        // Jetzt die Daten aus der Datenbank lesen, falls vorhanden.
                        iPartsDataModuleCemat moduleCemat = new iPartsDataModuleCemat(getProject(), moduleCematId);
                        if (!moduleCemat.existsInDB()) {
                            moduleCemat.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        }
                        moduleCemat.setFieldValue(FIELD_DMC_PARTNO, currentPartNo, DBActionOrigin.FROM_EDIT);
                        moduleCemat.setFieldValue(FIELD_DMC_VERSIONS, getSeperatedVersionString(versionSet), DBActionOrigin.FROM_EDIT);

                        if (importToDB) {
                            saveToDB(moduleCemat);
                        }
                    }
                } else {
                    reduceRecordCount();
                }
            }

            // Final den Fortschritt auf 100% setzen
            fireProgress(maxTUCount, maxTUCount);

        } catch (IOException e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            fireError("!!Fehler beim Importieren der EinPAS-Knoten aus CEMaT aus Datei \"%1\"", currentImportFileName);
        }
        getMessageLog().hideProgress();
    }

    /**
     * Hängt die einzelnen Versionen aus dem Set in einem Komma-separierten String aneinander
     *
     * @param versionSet
     * @return
     */
    private String getSeperatedVersionString(Set<String> versionSet) {
        if ((versionSet == null) || versionSet.isEmpty()) {
            return "";
        }
        return StrUtils.stringListToString(versionSet, CEMAT_VERSION_DB_DELIMITER);
    }

    /**
     * Liefert die Liste der TUs aus CEMaT
     *
     * @param cemat
     * @return
     */
    private List<CEMaTTu> getTUs(CEMaT cemat) {
        List<CEMaTTu> list = cemat.getTuList();
        if ((list == null) || list.isEmpty()) {
            fireWarning("!!Die Importdatei \"%1\" enthält keine gültigen Werte und  wird übersprungen.", currentImportFileName);
            return null;
        }
        return list;
    }

    /**
     * Liefert die ModuleNo (= TU-Id) oder im Fehlerfall null mit Ausgabe einer Meldung
     *
     * @param tu
     * @return
     */
    private String getModuleNo(CEMaTTu tu) {
        String res = tu.getiPartsTuId();
        if (res == null) {
            fireWarning("!!Aus der Datei \"%1\" konnte beim Datensatz Nr %2 " +
                        "keine gültige \"iPartsTuId\" ermittelt werden.",
                        currentImportFileName, Integer.toString(currentTUCounter));
            return null;
        }
        return res;
    }

    /**
     * Liefert die LfdNr oder im Fehlerfall null mit Ausgabe einer Meldung
     *
     * @param tu
     * @return
     */
    private String getLfdNr(CEMaTTu tu) {
        String res = tu.getiPartsSerialNbr();
        if (res == null) {
            fireWarning("!!Aus der Datei \"%1\" konnte beim Datensatz Nr %2 " +
                        "zum Modul \"%3\" keine gültige \"iPartsSerialNbr\" ermittelt werden.",
                        currentImportFileName, Integer.toString(currentTUCounter), currentModuleNo);
            return null;
        }
        return res;
    }

    /**
     * Liefert die PartNo oder im Fehlerfall null mit Ausgabe einer Meldung
     *
     * @param tu
     * @return
     */
    private String getPartNo(CEMaTTu tu) {
        String res = tu.getPartNumber();
        if (res == null) {
            fireWarning("!!Aus der Datei \"%1\" konnte beim Datensatz Nr %2 " +
                        "zum Modul \"%3\" keine gültige \"partNumber\" ermittelt werden.",
                        currentImportFileName, Integer.toString(currentTUCounter), currentModuleNo);
            return null;
        }
        return res;
    }

    /**
     * Liefert die Liste der gemappten Versionen eines TUs aus CEMaT oder null im Fehlerfall + Meldung
     *
     * @param tu
     * @return
     */
    private List<CEMaTTuMapping> getTuMappings(CEMaTTu tu) {
        List<CEMaTTuMapping> list = tu.getMappings();
        if ((list == null) || list.isEmpty()) {
            fireWarningLF("!!Aus der Datei \"%1\" konnten beim Datensatz Nr %2 " +
                          "zum Modul \"%3\" mit laufender Nummer \"%4\" keine gültigen \"mappings\" ermittelt werden.",
                          currentImportFileName, Integer.toString(currentTUCounter),
                          currentModuleNo, currentLfdNr);
            return null;
        }
        return list;
    }


    private class CematNodeHelper {

        private String version;
        private EinPasId einPasId;

        public CematNodeHelper() {
            version = null;
            einPasId = null;
        }

        public CematNodeHelper(String node) {
            this();
            analyzeNode(node);
        }

        public boolean isValid() {
            return (version != null) && (einPasId != null) && einPasId.isValidId();
        }

        public String getVersion() {
            return version;
        }

        public EinPasId getEinPasId() {
            return einPasId;
        }

        private void analyzeNode(String node) {
            if (!StrUtils.isValid(node)) {
                fireWarning("!!Beim Datensatz Nr %1 zum Modul \"%2\" und Node Nr \"%3\" ist kein gültiger Node vorhanden.",
                            Integer.toString(currentTUCounter), currentModuleNo, Integer.toString(currentNodeCounter));
                return;
            }
            List<String> tokens = StrUtils.toStringList(node, CEMAT_NODE_DELIMITER, true);
            if (tokens.size() < NODE_VALID_TOKEN_COUNT) {
                fireWarning("!!Beim Datensatz Nr %1 zum Modul \"%2\" und Node Nr \"%3\" ist der Node \"%4\" falsch aufgebaut.",
                            Integer.toString(currentTUCounter), currentModuleNo, Integer.toString(currentNodeCounter),
                            node);
                return;
            }
            version = checkNodeToken(tokens.get(0), NODE_ENUMS.VERSION_NODE);
            if (version == null) {
                return;
            }
            einPasId = checkEinPasId(tokens.get(1), tokens.get(2), tokens.get(3));
            if (einPasId == null) {
                version = null;
            }
        }

        private EinPasId checkEinPasId(String hg, String g, String tu) {
            String einPasHg = checkNodeToken(hg, NODE_ENUMS.HG_NODE);
            if (einPasHg == null) {
                return null;
            }
            String einPasG = checkNodeToken(g, NODE_ENUMS.G_NODE);
            if (einPasG == null) {
                return null;
            }
            String einPasTU = checkNodeToken(tu, NODE_ENUMS.TU_NODE);
            if (einPasTU == null) {
                return null;
            }
            return new EinPasId(einPasHg, einPasG, einPasTU);
        }

        /**
         * Überprüft ein Element (nodeEnum) aus der "node" des Mappings: "node" : "001.37.20.33" ==> Version, HG,G,TU
         * oder null im Fehlerfall mit Ausgabe einer Meldung.
         * <p>
         * Der Range wirtd ebenfalls überprüft
         *
         * @param token
         * @param nodeEnum
         * @return
         */
        private String checkNodeToken(String token, NODE_ENUMS nodeEnum) {
            if (!StrUtils.isValid(token)) {
                fireWarning("!!Beim Datensatz Nr %1 zum Modul \"%2\" und Node Nr \"%3\" kein gültiger %4.",
                            Integer.toString(currentTUCounter), currentModuleNo, Integer.toString(currentNodeCounter),
                            translateForLog(nodeEnum.getDesc()));
                return null;
            }
            if (token.length() != nodeEnum.getLength()) {
                fireWarning("!!Beim Datensatz Nr %1 zum Modul \"%2\" und Node Nr \"%3\" ist der %4 \"%5\" ungültig, zulässig sind nur genau %6 Ziffern.",
                            Integer.toString(currentTUCounter), currentModuleNo, Integer.toString(currentNodeCounter),
                            translateForLog(nodeEnum.getDesc()), token, Integer.toString(nodeEnum.getLength()));
                return null;
            }
            if (!StrUtils.isDigit(token)) {
                fireWarning("!!Beim Datensatz Nr %1 zum Modul \"%2\" und Node Nr \"%3\" ist der %4 \"%5\" ungültig, zulässig sind nur Ziffern (0 - 9).",
                            Integer.toString(currentTUCounter), currentModuleNo, Integer.toString(currentNodeCounter),
                            translateForLog(nodeEnum.getDesc()), token);
                return null;
            }
            int iVal = Integer.parseInt(token);
            if ((iVal < nodeEnum.getMin()) || (iVal > nodeEnum.getMax())) {
                fireWarning("!!Beim Datensatz Nr %1 zum Modul \"%2\" und Node Nr \"%3\" ist der %4 \"%5\" ungültig, zulässig sind nur Werte von %6 bis %7.",
                            Integer.toString(currentTUCounter), currentModuleNo, Integer.toString(currentNodeCounter),
                            translateForLog(nodeEnum.getDesc()), token,
                            Integer.toString(nodeEnum.getMin()), Integer.toString(nodeEnum.getMax()));
                return null;
            }
            return token;
        }
    }
}
