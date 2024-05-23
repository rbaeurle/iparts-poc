/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.importer;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.output.swing.SwingHandler;
import de.docware.util.ArrayUtil;
import de.docware.util.Utils;
import de.docware.util.file.DWFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstrakte Superklasse zum Importieren von mehreren Dateilisten.
 */
public abstract class AbstractFilesImporter extends AbstractGenericImporter implements FilesImporterInterface {

    protected Map<FilesImporterFileListType, List<DWFile>> importFilesMap = new LinkedHashMap<FilesImporterFileListType, List<DWFile>>();
    protected FilesImporterFileListType[] importFileTypes;
    protected boolean copyImportFile = true;
    private boolean removeAllExistingData;

    public AbstractFilesImporter(EtkProject project, String importName, FilesImporterFileListType... importFileTypes) {
        super(project, importName);
        this.importFileTypes = importFileTypes;
    }

    @Override
    public FilesImporterFileListType[] getImportFileTypes() {
        return importFileTypes;
    }

    @Override
    public boolean importFiles(FilesImporterFileListType importFileType, List<DWFile> importFiles, boolean removeAllExistingData) {
        this.removeAllExistingData = removeAllExistingData;
        try {
            importFilesMap.put(importFileType, importFiles);

            if (errorInStartTransaction) { // Ohne DB-Transaktion macht ein Import keinen Sinn
                return false;
            }

            String filesListName = importFileType.getFileListName(getLogLanguage());
            if (removeAllExistingData) {
                getMessageLog().fireMessage(translateForLog("!!%1 werden gelöscht", filesListName), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                if (!removeAllExistingData(importFileType)) {
                    cancelImport(translateForLog("!!Fehler beim Löschen von '%1'", filesListName));
                    return false;
                }
            }

            if (!importFiles.isEmpty()) {
                for (DWFile importFile : importFiles) {
                    String importFileName = SwingHandler.isSwing() ? importFile.getAbsolutePath() : importFile.getName();
                    getMessageLog().fireMessage(translateForLog("!!Datei: %1 wird eingelesen", importFileName), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    copyImportFile = !importFileType.isServerMode();
                    if (!importFile(importFileType, importFile)) {
                        cancelImport(translateForLog("!!Fehler beim Importieren von '%1'", importFileName));
                        return false;
                    }
                }
            }

            return true;
        } catch (Throwable t) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, t);
            cancelImport(translateForLog("!!Exception beim Importieren:") + " " + Utils.exceptionToString(t, true));
            return false;
        }
    }


    @Override
    public boolean finishImport() {
        if (J2EEHandler.isJ2EE()) { // unter JEE alle temporären Importdateien gleich wieder löschen
            for (Map.Entry<FilesImporterFileListType, List<DWFile>> importEntry : importFilesMap.entrySet()) {
                if (!importEntry.getKey().isServerMode()) {
                    for (DWFile importFile : importEntry.getValue()) {
                        importFile.deleteRecursivelyWithRepeat();
                    }
                }
            }
        }
        return super.finishImport();
    }

    /**
     * Liefert alle Importdateien für den übergebenen Import-Dateilisten-Typ zurück.
     *
     * @param importFileType
     * @return Kann auch {@code null} sein
     */
    public List<DWFile> getImportFiles(FilesImporterFileListType importFileType) {
        return importFilesMap.get(importFileType);
    }

    /**
     * Löscht alle Spracheinträge für alle MultiLanguage-Felder einer Tabelle
     * Zusatz: Einträge mit gültiger TextID werden NICHT gelöscht
     *
     * @param tableName
     */
    public void deleteLanguageEntriesOfTable(String tableName) {
        deleteLanguageEntriesOfTable(tableName, (List<String>)null);
    }

    /**
     * Löscht alle Spracheinträge für alle MultiLanguage-Felder einer Tabelle
     * Ausnahme: die in exceptFields angegebenen Felder
     * Zusatz: Einträge mit gültiger TextID werden NICHT gelöscht
     *
     * @param tableName
     * @param exceptFields
     */
    public void deleteLanguageEntriesOfTable(String tableName, List<String> exceptFields) {
        List<String> fieldNameList = getProject().getConfig().getDBDescription().getTable(tableName).getMultiLangFields();
        for (String fieldName : fieldNameList) {
            boolean doDelete = exceptFields == null;
            if (!doDelete) {
                doDelete = !exceptFields.contains(fieldName);
            }
            if (doDelete) {
                getProject().getDbLayer().delete(EtkDbConst.TABLE_SPRACHE,
                                                 new String[]{
                                                         EtkDbConst.FIELD_S_FELD,
                                                         EtkDbConst.FIELD_S_TEXTID },
                                                 new String[]{
                                                         tableName + "." + fieldName,
                                                         "" });
            }
        }
    }

    /**
     * Löscht alle Spracheinträge für alle MultiLanguage-Felder einer Tabelle
     * Ausnahme: die in exceptFields angegebenen Felder
     * Zusatz: Einträge mit gültiger TextID werden NICHT gelöscht
     *
     * @param tableName
     * @param exceptFields
     */
    public void deleteLanguageEntriesOfTable(String tableName, String[] exceptFields) {
        if (exceptFields != null) {
            deleteLanguageEntriesOfTable(tableName, ArrayUtil.toArrayList(exceptFields));
        } else {
            deleteLanguageEntriesOfTable(tableName);
        }
    }

    /**
     * Löscht den kompletten Inhalt der übergebenen Tabelle, sofern sie in den <code>importFileType</code> vorkommt.
     *
     * @param importFileType
     * @param tableName
     * @return
     */
    protected boolean removeAllExistingDataForTable(FilesImporterFileListType importFileType, String tableName) {
        if (importFileType.getFileListType().equals(tableName)) {
            deleteLanguageEntriesOfTable(tableName);
            getProject().getDB().delete(tableName);
            return true;
        } else {
            return false;
        }
    }

    public boolean isRemoveAllExistingData() {
        return removeAllExistingData;
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
    protected abstract boolean removeAllExistingData(FilesImporterFileListType importFileType);

    /**
     * Importiert die Datei für den übergebenen Import-Dateilisten-Typ.
     *
     * @param importFileType
     * @param importFile
     * @return {@code true} falls der Import erfolgreich war
     */
    protected abstract boolean importFile(FilesImporterFileListType importFileType, DWFile importFile);
}
