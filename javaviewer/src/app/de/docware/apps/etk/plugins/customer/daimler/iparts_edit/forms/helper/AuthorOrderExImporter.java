package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserDialog;
import de.docware.framework.modules.gui.controls.filechooser.filefilter.DWFileFilterEnum;
import de.docware.framework.modules.gui.misc.ContentTypes;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.misc.CompressionUtils;
import de.docware.util.misc.id.IdWithType;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

/**
 * Export und Import von Autoren-Aufträgen
 */
public class AuthorOrderExImporter {

    private EtkProject project;
    private iPartsDataAuthorOrder dataAuthorOrder;
    private iPartsDataChangeSet dataChangeSet;
    private iPartsDataChangeSetEntryList dataChangeSetEntryList;
    private iPartsDataReservedPKList dataReservedPKList;
    private String errorMessages;

    public AuthorOrderExImporter(EtkProject project) {
        this.project = project;
    }

    public boolean exportCompleteAuthorOrder(iPartsDataAuthorOrder dataAuthorOrder) {
        errorMessages = "";
        if (dataAuthorOrder == null) {
            return false;
        }

        // DWFile mit Zip-Stream öffnen
        DWFile tempDir = createTempDir();
        if (tempDir == null) {
            return false;
        }

        boolean result;
        try {
            // XML Datei, in die alle Informationen geschrieben werden
            String filename = getExportFileName(dataAuthorOrder);
            DWFile exportFile = tempDir.getChild(filename);
            exportFile.mkDirsWithRepeat();
            result = exportAuthorOrder(dataAuthorOrder, exportFile);
            if (result) {
                GuiFileChooserDialog fileChooserDialog = new GuiFileChooserDialog(FileChooserPurpose.SAVE, GuiFileChooserDialog.FILE_MODE_FILES,
                                                                                  null, false);
                fileChooserDialog.setServerMode(false);
                try {
                    String fileName = exportFile.extractFileName(false);
                    final DWFile zipFile = exportFile.getParentDWFile().getChild(fileName + "." + MimeTypes.EXTENSION_ZIP);
                    CompressionUtils.zipDir(zipFile.getAbsolutePath(), exportFile.extractDirectory());
                    fileChooserDialog.setVisible(zipFile);
                } catch (Exception e) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    errorMessages = TranslationHandler.translate("!!Fehler beim Speichern.") + "\n" + e.getMessage();
                    return false;
                }
            }
        } finally {
            if (tempDir != null) {
                tempDir.deleteRecursivelyWithRepeat();
            }
        }
        return result;
    }


    private boolean exportAuthorOrder(iPartsDataAuthorOrder dataAuthorOrder, DWFile destDir) {
        if (dataAuthorOrder == null) {
            return false;
        }
        this.dataAuthorOrder = dataAuthorOrder;
        if (!exportOneDataObject(destDir, dataAuthorOrder)) {
            errorMessages = TranslationHandler.translate("!!Fehler beim Export des Autoren-Auftrags!");
            return false;
        }

        iPartsChangeSetId changeSetId = dataAuthorOrder.getChangeSetId();
        if (changeSetId.isValidId()) {
            dataChangeSet = new iPartsDataChangeSet(project, changeSetId);
            if (!dataChangeSet.existsInDB()) {
                errorMessages = TranslationHandler.translate("!!Autoren-Auftrag besitzt kein Änderungsset!");
                return false;
            }
            dataChangeSetEntryList = iPartsDataChangeSetEntryList.loadChangeSetEntriesForChangeSetAndDataObjectIdWithType(project,
                                                                                                                          dataChangeSet.getAsId(),
                                                                                                                          null);

            if (!exportOneDataObject(destDir, dataChangeSet)) {
                errorMessages = TranslationHandler.translate("!!Fehler beim Export des Änderungssets!");
                return false;
            }

            // Blobs nachladen
            for (iPartsDataChangeSetEntry dataChangeSetEntry : dataChangeSetEntryList) {
                dataChangeSetEntry.getCurrentData();
                dataChangeSetEntry.getHistoryData();
            }

            if (!exportOneDataObjectList(destDir, dataChangeSetEntryList)) {
                errorMessages = TranslationHandler.translate("!!Fehler beim Export der Änderungsset-Einträge!");
                return false;
            }

            // Primärschlüssel-Reservierungen exportieren
            dataReservedPKList = iPartsDataReservedPKList.loadPrimaryKeysForChangeSet(project, dataChangeSet.getAsId());
            if (!exportOneDataObjectList(destDir, dataReservedPKList)) {
                errorMessages = TranslationHandler.translate("!!Fehler beim Export der Primärschlüssel-Reservierungen!");
                return false;
            }
        }

        return true;
    }

    public boolean importCompleteAuthorOrder(DWFile zipFile) {
        errorMessages = "";
        if ((zipFile == null) || !zipFile.isFile()) {
            zipFile = getImportFile();
            if (zipFile == null) {
                return false;
            }
        }

        // DWFile mit Zip-Stream öffnen
        DWFile tempDir = createTempDir();
        if (tempDir == null) {
            return false;
        }

        try {
            CompressionUtils.unzipFile(tempDir.getAbsolutePath(), zipFile.getAbsolutePath(), ContentTypes.Charset.UTF8.getCharsetName());

            // Die ZIP-Datei enthält genau ein Unterverzeichnis -> in diesem liegen die XML-Dateien
            List<DWFile> subDirs = tempDir.listDWFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });

            if (subDirs.size() != 1) {
                errorMessages = TranslationHandler.translate("!!Ungültige Import-Datei.");
                return false;
            }

            DWFile importFile = subDirs.get(0); // Erstes (und einziges) Verzeichnis für den Import nehmen
            return importAuthorOrder(importFile);
        } catch (IOException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            errorMessages = TranslationHandler.translate("!!Fehler beim Import.") + "\n" + e.getMessage();
            return false;
        } finally {
            if (tempDir != null) {
                tempDir.deleteRecursivelyWithRepeat();
            }
        }
    }

    private boolean importAuthorOrder(DWFile sourceDir) {
        dataAuthorOrder = new iPartsDataAuthorOrder(project, null);
        dataAuthorOrder.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        boolean result = importOneDataObject(sourceDir, dataAuthorOrder);
        if (result) {
            if (dataAuthorOrder.getChangeSetId().isEmpty()) {
                return true;
            }

            dataChangeSet = new iPartsDataChangeSet(project, null);
            dataChangeSet.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            result = importOneDataObject(sourceDir, dataChangeSet);
            if (result) {
                GetNewEtkDataObjectEvent onNewChangeSetEntry = project -> new iPartsDataChangeSetEntry(project, null);
                dataChangeSetEntryList = new iPartsDataChangeSetEntryList();
                result = importDataObjectList(sourceDir, dataChangeSetEntryList, onNewChangeSetEntry);
                if (!result) {
                    errorMessages = TranslationHandler.translate("!!Fehler beim Import der Änderungsset-Einträge!");
                    return false;
                }

                // Primärschlüssel-Reservierungen importieren
                GetNewEtkDataObjectEvent onNewReservedPK = project -> new iPartsDataReservedPK(project, null);
                dataReservedPKList = new iPartsDataReservedPKList();
                result = importDataObjectList(sourceDir, dataReservedPKList, onNewReservedPK);
                if (!result) {
                    errorMessages = TranslationHandler.translate("!!Fehler beim Import der Primärschlüssel-Reservierungen!");
                    return false;
                }
            } else {
                errorMessages = TranslationHandler.translate("!!Fehler beim Import des Änderungssets!");
            }
        } else {
            errorMessages = TranslationHandler.translate("!!Fehler beim Import des Autoren-Auftrags!");
        }
        return result;
    }

    private boolean changeGuids() {
        if (dataAuthorOrder == null) {
            errorMessages = TranslationHandler.translate("!!Kein Autoren-Auftrag eingelesen!");
            return false;
        }
        String authorGuid = StrUtils.makeGUID();
        String changeSetGuid = StrUtils.makeGUID();
        String oldAuthorGuid = dataAuthorOrder.getAsId().getAuthorGuid();
        String oldChangeSetGuid = "";
        if (dataChangeSet != null) {
            oldChangeSetGuid = dataChangeSet.getAsId().getGUID();
        }

        DBDataObjectAttributes attributes = dataAuthorOrder.getAttributes();
        attributes.addField(iPartsConst.FIELD_DAO_GUID, authorGuid, DBActionOrigin.FROM_DB);
        if (!oldChangeSetGuid.isEmpty()) {
            attributes.addField(iPartsConst.FIELD_DAO_CHANGE_SET_ID, changeSetGuid, DBActionOrigin.FROM_DB);
        }

        // Hier keine Übersetzung möglich, da der Autoren-Auftrags-Name nur einsprachig ist
        attributes.addField(iPartsConst.FIELD_DAO_NAME, TranslationHandler.translate("!!Importiert:") + " " + attributes.getFieldValue(iPartsConst.FIELD_DAO_NAME),
                            DBActionOrigin.FROM_DB);

        // Hier keine Übersetzung möglich, da die Autoren-Auftrags-Beschreibung nur einsprachig ist
        String extraText = TranslationHandler.translate("!!Importierter Autoren-Auftrag:") + "\n"
                           + TranslationHandler.translate("!!- Ursprüngliche Autoren-Auftrags-GUID:") + " " + oldAuthorGuid;
        if (!oldChangeSetGuid.isEmpty()) {
            extraText += "\n" + TranslationHandler.translate("!!- Ursprüngliche Änderungsset-GUID:") + " " + oldChangeSetGuid;
        }

        String text = attributes.getFieldValue(iPartsConst.FIELD_DAO_DESC);
        if (!text.isEmpty()) {
            text = extraText + "\n" + text;
        } else {
            text = extraText;
        }
        attributes.addField(iPartsConst.FIELD_DAO_DESC, text, DBActionOrigin.FROM_DB);
        attributes.addField(iPartsConst.FIELD_DAO_CURRENT_USER_ID, iPartsDataAuthorOrder.getLoginAcronym(), DBActionOrigin.FROM_DB);

        dataAuthorOrder = new iPartsDataAuthorOrder(project, null);
        dataAuthorOrder.setAttributes(attributes, DBActionOrigin.FROM_EDIT);

        if (!oldChangeSetGuid.isEmpty() && (dataChangeSet != null)) {
            // ChangeSet-ID im ChangeSet korrigieren
            attributes = dataChangeSet.getAttributes();
            attributes.addField(iPartsConst.FIELD_DCS_GUID, changeSetGuid, DBActionOrigin.FROM_DB);
            dataChangeSet = new iPartsDataChangeSet(project, null);
            dataChangeSet.setAttributes(attributes, DBActionOrigin.FROM_EDIT);

            // ChangeSet-ID in den Primärschlüssel-Reservierungen korrigieren
            List<iPartsDataReservedPK> reservedPKList = dataReservedPKList.getAsList();
            dataReservedPKList.clear(DBActionOrigin.FROM_DB);
            for (iPartsDataReservedPK dataReservedPK : reservedPKList) {
                // Prüfung auf bereits vorhandene Primärschlüssel-Reservierung
                iPartsDataReservedPK existingDataReservedPK = new iPartsDataReservedPK(project, dataReservedPK.getAsId());
                if (existingDataReservedPK.existsInDB()) {
                    String dataObjectType = existingDataReservedPK.getAsId().getDataObjectType();
                    String dataObjectIdString = IdWithType.fromDBString(dataObjectType, existingDataReservedPK.getAsId().getDataObjectId()).toStringForLogMessages();
                    errorMessages = TranslationHandler.translate("!!Primärschlüssel-Reservierung für Datenobjekt vom Typ \"%1\" mit ID %2 bereits vorhanden im Änderungsset \"%3\"!",
                                                                 dataObjectType, dataObjectIdString, existingDataReservedPK.getFieldValue(iPartsConst.FIELD_DRP_CHANGE_SET_ID));
                    return false;
                }

                attributes = dataReservedPK.getAttributes();
                attributes.addField(iPartsConst.FIELD_DRP_CHANGE_SET_ID, changeSetGuid, DBActionOrigin.FROM_DB);
                dataReservedPK = new iPartsDataReservedPK(project, null);
                dataReservedPK.setAttributes(attributes, DBActionOrigin.FROM_EDIT);
                dataReservedPKList.add(dataReservedPK, DBActionOrigin.FROM_EDIT);
            }

            // ChangeSet-ID in den ChangeSetEntries-Reservierungen korrigieren
            List<iPartsDataChangeSetEntry> changeSetEntryList = dataChangeSetEntryList.getAsList();
            dataChangeSetEntryList.clear(DBActionOrigin.FROM_DB);
            for (iPartsDataChangeSetEntry dataChangeSetEntry : changeSetEntryList) {
                attributes = dataChangeSetEntry.getAttributes();
                attributes.addField(iPartsConst.FIELD_DCE_GUID, changeSetGuid, DBActionOrigin.FROM_DB);
                dataChangeSetEntry = new iPartsDataChangeSetEntry(project, null);
                dataChangeSetEntry.setAttributes(attributes, DBActionOrigin.FROM_EDIT);
                dataChangeSetEntryList.add(dataChangeSetEntry, DBActionOrigin.FROM_EDIT);
            }
        } else {
            dataChangeSet = null;
            dataChangeSetEntryList = null;
            dataReservedPKList = null;
        }

        return true;
    }

    public boolean saveCompleteAuthorOrder() {
        if (!changeGuids()) {
            return false;
        }

        // Aktive Änderungssets temporär deaktivieren
        final VarParam<Boolean> result = new VarParam<>(true);
        project.executeWithoutActiveChangeSets(new Runnable() {
            @Override
            public void run() {
                project.getDbLayer().startTransaction();
                project.getDbLayer().startBatchStatement();
                try {
                    dataAuthorOrder.saveToDB();
                    if (dataChangeSet != null) {
                        dataChangeSet.saveToDB();
                    }
                    if (Utils.isValid(dataChangeSetEntryList)) {
                        dataChangeSetEntryList.saveToDB(project);
                    }
                    if (Utils.isValid(dataReservedPKList)) {
                        dataReservedPKList.saveToDB(project);
                    }
                    project.getDbLayer().endBatchStatement();
                    project.getDbLayer().commit();
                } catch (Exception e) {
                    project.getDbLayer().cancelBatchStatement();
                    project.getDbLayer().rollback();
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    errorMessages = TranslationHandler.translate("!!Fehler beim Speichern vom importierten Autoren-Auftrag.")
                                    + "\n" + e.getMessage();
                    result.setValue(false);
                }
            }
        }, true);
        return result.getValue();
    }

    public iPartsDataAuthorOrder getDataAuthorOrder() {
        return dataAuthorOrder;
    }

    public iPartsDataChangeSet getDataChangeSet() {
        return dataChangeSet;
    }

    public iPartsDataChangeSetEntryList getDataChangeSetEntryList() {
        return dataChangeSetEntryList;
    }

    public iPartsDataReservedPKList getDataReservedPKList() {
        return dataReservedPKList;
    }

    public String getErrorMessage() {
        return errorMessages;
    }

    private DWFile createTempDir() {
        DWFile tempDir = DWFile.createTempDirectory("daim");
        if (tempDir == null) {
            errorMessages = TranslationHandler.translate("!!Temporäres Verzeichnis kann nicht angelegt werden!");
        }
        return tempDir;
    }

    private String getExportFileName(iPartsDataAuthorOrder dataAuthorOrder) {
        return DWFile.convertToValidFileName("AuthorOrder_" + dataAuthorOrder.getAuthorOrderName() + "_" + dataAuthorOrder.getAsId().getAuthorGuid()
                                             + "_" + DateUtils.getCurrentDateFormatted(DateUtils.simpleTimeFormatyyyyMMddHHmmss));
    }

    private DWFile getImportFile() {
        GuiFileChooserDialog box = new GuiFileChooserDialog(FileChooserPurpose.OPEN, GuiFileChooserDialog.FILE_MODE_FILES,
                                                            null, false);

        DWFileFilterEnum activeFileFilter = DWFileFilterEnum.ZIP_FILES;
        box.addChoosableFileFilter(activeFileFilter.getDescription(), activeFileFilter.getExtensions());
        box.setActiveFileFilter(activeFileFilter.getDescription());
        box.setServerMode(false);
        box.setVisible(true);

        return box.getSelectedFile();
    }

    private boolean exportOneDataObject(DWFile destDir, EtkDataObject dataObject) {
        iPartsXmlDbExporter exporter = new iPartsXmlDbExporter(project);
        return exporter.exportOneDataObject(destDir, dataObject);
    }

    private boolean exportOneDataObjectList(DWFile destDir, EtkDataObjectList dataObjectList) {
        if (!dataObjectList.isEmpty()) {
            iPartsXmlDbExporter exporter = new iPartsXmlDbExporter(project);
            return exporter.exportOneDataObjectList(destDir, dataObjectList);
        }
        return true;
    }

    private boolean importOneDataObject(DWFile sourceDir, EtkDataObject dataObject) {
        iPartsXmlDbImporter importer = new iPartsXmlDbImporter(project);
        return importer.importOneDataObject(sourceDir, dataObject);
    }

    private boolean importDataObjectList(DWFile sourceDir, EtkDataObjectList dataObjectList, GetNewEtkDataObjectEvent onNewDataObject) {
        iPartsXmlDbImporter importer = new iPartsXmlDbImporter(project);
        return importer.importDataObjectList(sourceDir, dataObjectList, onNewDataObject);
    }
}
