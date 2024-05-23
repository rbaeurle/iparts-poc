/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.images;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.drawing.ImageVariant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPicReferenceState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReference;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReferenceList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsPicReferenceId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImageImportSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.images.iPartsReferenceImagesImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Implementiert die Funktionalität DASTi-Bilder in einer Verzeichnisstruktur abzulegen
 * und passende Pfad/Dateinamen wieder auszulesen.
 */

public class DASTiPictureHelper implements iPartsConst, EtkDbConst {

    private static final int COMPARELENGTH = 6;
    private static final String DEFAULTRESULTDIR = "__0__";
    private static final String DASTI_FILE_DATE_FORMAT = "yyyy.MM.dd";
    private static final String DASTI_PICTURE_EXTENSION = ".png";
    private static final String DASTI_THUMBNAIL_FILE_INDICATOR = "_thumbnail";
    private static final String DASTI_THUMBNAIL_APPENDIX = DASTI_THUMBNAIL_FILE_INDICATOR + DASTI_PICTURE_EXTENSION;
    private static final String DASTI_HOTSPOT_FILE_EXTENSION = ".sen";

    private EtkProject project;
    private boolean dastiDirStructurePictureImportEnabled;
    private DWFile configuredPictureImportRootDir;

    /**
     * @param project
     */
    public DASTiPictureHelper(EtkProject project) {
        this.project = project;
        // Die Werte aus der Konfiguration einmalig setzen.
        ImageImportSource configuredImportSource = iPartsImportPlugin.getSelectedImageImportSource();
        String unzipRootDirString = iPartsImportPlugin.getPluginConfig().getConfigValueAsRawString(iPartsImportPlugin.CONFIG_IMPORT_RFTSX_IMAGES_UNZIP_ROOT_DIR, "");
        if (!unzipRootDirString.isEmpty()) {
            configuredPictureImportRootDir = iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_IMPORT_RFTSX_IMAGES_UNZIP_ROOT_DIR);
        }

        // Es muss ein Verzeichnis konfiguriert sein und dieses darf nicht leer sein, denn sonst können ja keine Bilder gefunden werden
        this.dastiDirStructurePictureImportEnabled = configuredImportSource.equals(ImageImportSource.RFTSX) && !unzipRootDirString.isEmpty()
                                                     && !configuredPictureImportRootDir.isEmpty();
    }

    /**
     * Gibt den Pfad/Dateinamen des eingestellten Root-Verzeichnisses zurück.
     */
    public String getConfiguredPictureImportRootDir() {
        if (configuredPictureImportRootDir == null) {
            return "";
        }
        return configuredPictureImportRootDir.getPath();
    }

    /**
     * @return false, wenn das Ermitteln der Bilder über ASPLM EINgeschaltet ist
     */
    public boolean isDastiDirStructurePictureImportEnabled() {
        return dastiDirStructurePictureImportEnabled;
    }

    /**
     * Macht aus dem übergebenen Dateinamen den passenden Sammel-Verzeichnisnamen.
     *
     * @param compareFileName reiner Dateiname OHNE PFAD!
     * @return
     */
    private String getDestinationDirectoryName(String compareFileName) {
        if ((StrUtils.isEmpty(compareFileName)) || (compareFileName.length() < COMPARELENGTH)) {
            return DEFAULTRESULTDIR;
        }
        return compareFileName.substring(0, COMPARELENGTH);
    }

    /**
     * @param fileList
     * @return
     */
    public boolean putPictures(List<DWFile> fileList) throws IOException {
        // DASTi-Verzeichnis muss korrekt konfiguriert sein
        if (configuredPictureImportRootDir == null) {
            return false;
        }

        for (DWFile srcFile : fileList) {
            // Der reine Dateiname ohne Pfad, aber mit Erweiterung.
            String pureFileName = srcFile.extractFileName(true);

            // Der aus dem Dateinamen eingedampfte Zielverzeichnisname ohne Pfad
            // Aus [B21050000159_2014.07.25.png] wird [B21050]
            String subDirName = getDestinationDirectoryName(pureFileName);

            DWFile destDir = configuredPictureImportRootDir.getChild(subDirName);
            if (destDir.exists() && destDir.isDirectory()) {
                // NOOP
            } else {
                destDir.mkDirsWithRepeat();
            }

            // Jetzt noch die Datei hinein verschieben.
            boolean couldMove = srcFile.move(destDir);
            if (!couldMove) {
                throw new IOException("Could not move file \"" + srcFile.getAbsolutePath() + "\" to \"" + destDir.getAbsolutePath() + "\"");
            }

        }
        return true;
    }

    /**
     * Sortiert die übergebene Liste aus DWFile(s) absteigend.
     * ==> Der "größere" String soll vor dem "kleineren" kommen.
     * Erwartetes Ergebnis: B21230000091_2014.07.29.png soll größer sein als B21230000091_2014.07.28.png
     *
     * @param dwFileListToSort
     */
    private void sortDWFileList(List<DWFile> dwFileListToSort) {

        Collections.sort(dwFileListToSort, new Comparator<DWFile>() {
            @Override
            public int compare(DWFile o1, DWFile o2) {
//                return o1.extractFileName(false).compareTo(o2.extractFileName(false));
                String filename1 = o1.extractFileName(false);
                String filename2 = o2.extractFileName(false);
                int res = filename2.compareTo(filename1);
                return res;
            }
        });
    }


    /**
     * Extrahiert das Datum aus einem möglichen Dateinamen (die 10 Zeichen nach dem Unterstrich = yyyy.MM.dd)
     * Zulässige Dateinamen sind:
     * [B21050000132_2014.10.30.png]
     * [B21050000132_2014.10.30.sen]
     * [B21050000132_2014.10.30_thumbnail.png]
     *
     * @param dastiFilePath
     * @return
     */
    private Date extractDateFromFilename(String dastiFilePath) {
        if (StrUtils.isEmpty(dastiFilePath)) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "DASTi import file path must not be null or empty!");
            return null;
        }
        String filename = DWFile.extractFileName(dastiFilePath, false);

        int idx = filename.indexOf("_");
        if (idx >= 0) {
            String dateStr = filename.substring(idx + 1, idx + 1 + 10);
            SimpleDateFormat formatter = new SimpleDateFormat(DASTI_FILE_DATE_FORMAT);
            try {
                return formatter.parse(dateStr);
            } catch (ParseException e) {
                Logger.getLogger().handleRuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Sucht die Dateien passend zum übergebenen Bildnamen und Datum aus der Verzeichnisstruktur heraus. Ist das Datum leer
     * oder <code>null</code>, dann wird das aktuelle Datum für die Suche herangezogen.
     *
     * @param pictureName
     * @param dataSetDateStr
     * @return
     */
    public DASTiImageFiles getPictures(final String pictureName, String dataSetDateStr) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date dataSetDate;
        if (StrUtils.isValid(dataSetDateStr)) {
            dataSetDate = formatter.parse(dataSetDateStr);
        } else {
            dataSetDate = Calendar.getInstance().getTime();
        }
        return getPictures(pictureName, dataSetDate);
    }

    /**
     * Über diese Funktion werden die zum Bildnamen und Datum passenden Dateien aus der Verzeichnisstruktur ermittelt.
     *
     * @param pictureName
     * @param dataSetDate
     * @return
     */
    public DASTiImageFiles getPictures(String pictureName, Date dataSetDate) {
        // Nur suchen, wenn das DASTi-Verzeichnis korrekt konfiguriert ist
        if (configuredPictureImportRootDir != null) { // isDastiDirStructurePictureImportEnabled() wertet auch die Admin-Einstellung der Mediensuche aus
            // Der aus dem Dateinamen eingedampfte Zielverzeichnisname ohne Pfad
            // Aus [B21050000159_2014.07.25.png] wird [B21050]
            String subDirName = getDestinationDirectoryName(pictureName);

            // Wenn es das Sammelverzeichnis gibt, darin nach passenden Dateien suchen.
            DWFile pictureDir = configuredPictureImportRootDir.getChild(subDirName);
            if (pictureDir.isDirectory()) {
                // Das eigentliche Bild, Beispiel: [B21230000091_2014.07.29.png]
                DWFile pictureFile = getMatchingDASTiPicture(pictureDir, pictureName, dataSetDate, DASTI_PICTURE_EXTENSION);
                if ((pictureFile != null) && pictureFile.isFile()) {
                    DASTiImageFiles pictureFiles = new DASTiImageFiles(pictureFile);
                    String fileName = pictureFile.extractFileName(false);

                    // Thumbnail: [B21230000091_2014.07.29_thumbnail.png]
                    DWFile thumbnailFile = pictureDir.getChild(fileName + DASTI_THUMBNAIL_APPENDIX);
                    if (thumbnailFile.isFile()) {
                        pictureFiles.setPreviewFile(thumbnailFile);
                    }

                    // Die Hotspots: [B21230000091_2014.07.29.sen]
                    DWFile hotspotFile = pictureDir.getChild(fileName + DASTI_HOTSPOT_FILE_EXTENSION);
                    if (hotspotFile.isFile()) {
                        pictureFiles.setHotspotFile(hotspotFile);
                    }
                    return pictureFiles;
                }
            }
        }

        return new DASTiImageFiles(null);
    }

    /**
     * Funktion, die aus dem Verzeichnisbaum der gesammelten DASTi-Bilddateien die passende heraussucht.
     *
     * @param searchDir   In diesem Verzeichnis wird die Bilddatei erwartet.
     * @param pictureName Der Name des Bildes.
     * @param dataSetDate Das Datum zu dem die Datei gesucht werden muss. Wird nicht die exakte gefunden, wird die neueste zurückgegeben.
     * @param extension   Nicht nur die Erweiterung, sondern das Ende des Dateinamens.
     *                    Es gibt die Thumbnail: [B21230000091_2014.07.29_thumbnail.png].
     *                    Bei der ist das Dateiende = "_thumbnail.png"
     */
    private DWFile getMatchingDASTiPicture(DWFile searchDir, final String pictureName, Date dataSetDate, final String extension) {
        DWFile pictureFile = null;
        if (dataSetDate != null) {
            // Zum Anhängen des Datums an den Bildnamen
            SimpleDateFormat formatter = new SimpleDateFormat(DASTI_FILE_DATE_FORMAT);
            // Das eigentliche Bild, Beispiel: [B21230000091_2014.07.29.png]
            String searchFileName = pictureName + "_" + formatter.format(dataSetDate) + extension;
            pictureFile = searchDir.getChild(searchFileName);
        }
        if ((pictureFile != null) && pictureFile.isFile()) {
            return pictureFile;
        } else {
            // Die Sternsuche ohne Datum
            List<DWFile> tmpFileList = searchDir.listDWFiles(new FilenameFilter() {
                @Override
                public boolean accept(File destDir, String name) {
                    if ((name.startsWith(pictureName)) && (name.endsWith(extension))) {
                        // Blöderweise werden hier auch die "thumbnail"-Dateien gefunden, wenn man nach den Bildern "<Bildname>*.png" sucht:
                        // - B21230000091_2014.07.29.png UND B21230000091_2014.07.29_thumbnail.png
                        // Daher müssen diese explizit entfernt werden, wenn NICHT nach Thumbnails gesucht wird.
                        if ((name.contains(DASTI_THUMBNAIL_FILE_INDICATOR)) && (!extension.contains(DASTI_THUMBNAIL_FILE_INDICATOR))) {
                            return false;
                        } else {
                            return true;
                        }
                    } else {
                        return false;
                    }
                }
            });

            // Wenn Bilddateien zum Bildnamen gefunden wurden.
            if (tmpFileList.size() > 0) {
                // Absteigend nach Datum sortieren.
                sortDWFileList(tmpFileList);

//                Date foundFileDate = extractDateFromFilename(tmpFileList.get(0).extractFileName(true));
//                if ((dataSetDate != null) && (foundFileDate != null)) {
//                    if (foundFileDate.after(dataSetDate)) {
//                        // Was soll passieren, wenn die datumlose Dateisuche eine Datei gefunden hat, deren Datum größer als das Suchdatum ist?
//                        // Aktuell wird die gefundene Datei genommen.
//                        // Kann man sich den Vergleich sparen?
//                    }
//                }

                Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "The algorithm has found the nearest DASTi Picture: " + tmpFileList.get(0));
                return tmpFileList.get(0);

            } else {
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "No picture file found for picture: " + pictureName);
            }
        }
        return null;
    }

    /**
     * Das Suchen der Bilder aus der Verzeichnisstruktur passend zur den Referenzen und Speichern in der Datenbank.
     *
     * @param storedReferences
     * @param messageLog
     * @param importOnlyNewerOnes Sollen nur Bilder mit neuerem Referenzdatum importiert werden?
     * @return
     */
    public iPartsDataPicReferenceList handleReferencesFromMigration(List<iPartsDataPicReference> storedReferences,
                                                                    EtkMessageLog messageLog, boolean importOnlyNewerOnes) {
        iPartsDataPicReferenceList result = new iPartsDataPicReferenceList();
        if (storedReferences == null) {
            return result;
        }
        // Die Bilder über einen bestehenden Importer in die Datenbank schreiben
        iPartsReferenceImagesImporter referenceImagesImporter = new iPartsReferenceImagesImporter(project, messageLog);
        referenceImagesImporter.disableBufferedSave();
        try {
            String fileNotFoundText = referenceImagesImporter.translateForLog("!!nicht gefunden");
            for (iPartsDataPicReference storedReference : storedReferences) {
                if (Thread.currentThread().isInterrupted()) {
                    referenceImagesImporter.cancelImport("!!Import-Thread wurde frühzeitig beendet");
                    break;
                }
                iPartsPicReferenceId picRefId = storedReference.getAsId();
                // Absicherung nach DAIMLER-6124. Sollte nicht passieren, weil die übergebenen Referenzen vorher alle angelegt
                // wurden
                if (!storedReference.existsInDB()) {
                    writeMessage(messageLog, referenceImagesImporter.translateForLog("!!Bildreferenz \"%1\" mit dem Datum \"%2\" aus der Migration " +
                                                                                     "existiert nicht in der DB obwohl sie existieren sollte",
                                                                                     picRefId.getPicReferenceNumber(), picRefId.getPicReferenceDate()),
                                 MessageLogType.tmlError);
                    continue;
                }
                // Dann müssen die Bildreferenz(en) und die Bilddatei(en) "nur" noch in die Datenbank transferiert werden.
                writeMessage(messageLog, referenceImagesImporter.translateForLog("!!Suche Bilder für Bildreferenz \"%1\" mit dem Datum \"%2\"",
                                                                                 picRefId.getPicReferenceNumber(), picRefId.getPicReferenceDate()),
                             MessageLogType.tmlMessage);

                // Holt eine Liste der passenden Dateien zur gespeicherten Referenz aus der Verzeichnisstruktur.
                DASTiImageFiles imageFiles = getPictures(picRefId.getPicReferenceNumber(), picRefId.getPicReferenceDate());
                if (!imageFiles.isValid()) {
                    writeMessage(messageLog, referenceImagesImporter.translateForLog("!!Für die Bildreferenz \"%1\" mit dem Datum \"%2\" wurden keine Bilder gefunden",
                                                                                     picRefId.getPicReferenceNumber(), picRefId.getPicReferenceDate()),
                                 MessageLogType.tmlMessage);
                    storedReference.setStatus(iPartsPicReferenceState.NOT_FOUND);
                } else {
                    // Bei einer Nachforderung mit importOnlyNewerOnes = true sollen nur Bilder importiert werden, die ein
                    // neueres Datum besitzen als in der DB steht
                    boolean foundPictureHasNewerDate = checkIfNewerDate(imageFiles, storedReference.getAsId());

                    // Beim Nachfordern der Bilder ist das Datum des Bildes nicht neuer als das Datum der Referenz ABER
                    // die Referenz hatte noch kein Bild. Ein Bild kann nur dranhängen, wenn der Status auf "DONE" steht.
                    boolean olderDateButNoPicture = !foundPictureHasNewerDate && (storedReference.getStatus() != iPartsPicReferenceState.DONE);

                    if (!importOnlyNewerOnes || foundPictureHasNewerDate || olderDateButNoPicture) {
                        writeMessage(messageLog, referenceImagesImporter.translateForLog("!!Für die Bildreferenz \"%1\" mit dem Datum \"%2\" wurden folgende Dateien gefunden: Bild \"%3\", Vorschaubild \"%4\" und Hotspotdatei \"%5\"",
                                                                                         picRefId.getPicReferenceNumber(), picRefId.getPicReferenceDate(),
                                                                                         imageFiles.getImageFile() != null ? imageFiles.getImageFile().getName() : fileNotFoundText,
                                                                                         imageFiles.getPreviewFile() != null ? imageFiles.getPreviewFile().getName() : fileNotFoundText,
                                                                                         imageFiles.getHotspotFile() != null ? imageFiles.getHotspotFile().getName() : fileNotFoundText),
                                     MessageLogType.tmlMessage);

                        // Die Bilder in die Datenbank schreiben
                        project.getDbLayer().startTransaction();
                        project.getDbLayer().startBatchStatement();
                        try {
                            referenceImagesImporter.importImageFromFile(imageFiles.getImageFile(), imageFiles.getHotspotFile(),
                                                                        imageFiles.getPreviewFile(), picRefId.getPicReferenceNumber(),
                                                                        "", "", ImageVariant.iv2D, false, true, false);
                            project.getDbLayer().endBatchStatement();
                            project.getDbLayer().commit();
                        } catch (Exception e) {
                            project.getDbLayer().cancelBatchStatement();
                            project.getDbLayer().rollback();
                            throw e;
                        }

                        // Für diejenigen PicReferences den Status auf DONE setzen, für die tatsächlich Bilder importiert wurden.
                        storedReference.setStatus(iPartsPicReferenceState.DONE);
                    }
                }
                result.add(storedReference, DBActionOrigin.FROM_EDIT);
            }
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            referenceImagesImporter.cancelImport(e.getMessage());
            Logger.getLogger().throwRuntimeException(e);
        }

        return result;
    }

    /**
     * Schreibt die übergebene Nachricht in den übergebenen {@link EtkMessageLog} (bei allen Typen außer {@link MessageLogType#tmlError}
     * allerdings nur in die Log-Datei).
     *
     * @param messageLog
     * @param text
     * @param messageLogType
     */
    private void writeMessage(EtkMessageLog messageLog, String text, MessageLogType messageLogType) {
        if (messageLog != null) {
            if (messageLogType == MessageLogType.tmlError) {
                messageLog.fireMessage(text, messageLogType, MessageLogOption.TIME_STAMP);
            } else {
                messageLog.fireMessage(text, messageLogType, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
        }
    }

    private boolean checkIfNewerDate(DASTiImageFiles imageFiles, iPartsPicReferenceId picReferenceId) {
        String currentDate = picReferenceId.getPicReferenceDate();
        if (StrUtils.isValid(currentDate, imageFiles.getDateAsString())) {
            return imageFiles.getDateAsString().compareTo(currentDate) >= 0;
        }
        return true;
    }
}


