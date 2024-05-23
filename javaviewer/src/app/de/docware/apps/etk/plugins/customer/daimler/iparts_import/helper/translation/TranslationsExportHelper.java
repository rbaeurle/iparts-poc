/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit.DictTextKindTransitTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit.iPartsTransitMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlFile;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;
import de.docware.util.StringTokenizer;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.file.DWOutputStream;
import de.docware.util.misc.CompressionUtils;
import de.docware.util.sql.SQLStringConvert;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class TranslationsExportHelper extends ImportExportLogHelper implements iPartsConst {

    // XML Elemente für die Übersetzungsdatei
    private static final String ELEMENT_XPRT_TRANSLATION = "XPRT-translation";
    private static final String ELEMENT_TRANSLATIONS = "translations";
    private static final String ELEMENT_TRANSLATION = "translation";
    private static final String ELEMENT_TEXT = "text";
    private static final String ELEMENT_META_INFORMATION = "metaInformation";
    private static final String ELEMENT_CONTENT_TRANSLATE = "ContentTranslate";
    private static final String ELEMENT_SOURCE_TEXT = "sourceText";
    private static final String ELEMENT_CONTENT_NO_TRANSLATE = "ContentNoTranslate";
    private static final String ELEMENT_COMMENT = "comment";
    private static final String ELEMENT_CONTENT_READ_ONLY = "ContentReadOnly";
    private static final String ELEMENT_NEWLINE = "br";

    // XML Elemente für die TOC Datei
    private static final String ELEMENT_LANGUAGE_PACKAGE = "LanguagePackage";
    private static final String ELEMENT_FILES = "files";
    private static final String ELEMENT_FILE = "file";
    private static final String ELEMENT_FOLDERS = "folders";
    private static final String ELEMENT_FOLDER = "folder";
    private static final String ELEMENT_FOLDER_NAME = "foldername";


    // XML Attribute
    private static final String ATT_XMLNS = "xmlns:xsi";
    private static final String ATT_TASK = "task";
    private static final String ATT_SYSTEM = "system";
    private static final String ATT_SUBJECT = "subject";
    private static final String ATT_JOB_ID = "jobid";
    private static final String ATT_CUSTOMER_USER_ID = "CustomerUserID";
    private static final String ATT_CONTACT_PERSON_USER_ID = "ContactpersonUserID";
    private static final String ATT_ADDITIONAL_CONTACT_EMAIL = "AdditionalContactEmail";
    private static final String ATT_EXPORTED = "exported";
    private static final String ATT_CREATE_DATE_TIME = "createddatetime";
    private static final String ATT_TARGET_LANG = "TargetLang";
    private static final String ATT_SOURCE_LANG = "SourceLang";
    private static final String ATT_SCHEMA_LOCATION = "xsi:noNamespaceSchemaLocation";
    private static final String ATT_LANGUAGE_ID = "languageId";
    private static final String ATT_TRANSLATE_VERSION = "textTranslateVersionID";
    private static final String ATT_OBJECT_ID = "textObjectID";
    private static final String ATT_OBJECT_VERSION = "textObjectVersionID";
    private static final String ATT_MAX_LENGTH = "maxLength";
    private static final String ATT_TYPE = "type";


    private static final String FUNCTIONAL_ID = "EGT";
    private static final String SYSTEM_ID = "XPRT";
    private static final String SCHEMA_LOCATION = "XPRT-TRANSLATION-BASE-V2.xsd";
    private static final String XML_NS = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String DEFAULT_ID_VALUE = "1";
    private static final String DEFAULT_MAX_LENGTH_TRANSLATION = "64000";
    private static final String FILE_NAME_DELIMITER = "_";
    private static final String LANGUAGE_VARIANT = "XXX";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private static final String TOC_FILE_NAME = "_TOC.xml";
    private static final String FOLDER_NAME_INFO = "Info";
    private static final String FOLDER_NAME_PICTURES = "Pictures";
    private static final String FOLDER_NAME_SCHEMA = "Schema_DTD";
    private static final String FOLDER_NAME_STYLE_SHEET = "Stylesheet";
    private static final String FOLDER_NAME_TRANSLATION_FILES = "TranslationFiles";
    private static final String EMPTY_FOLDER_FILE_NAME = "emptyFolder.txt";
    private static final int JOBID_LENGTH = 6;

    private static TranslationsExportHelper instance;

    public static TranslationsExportHelper createInstance(EtkProject project) {
        instance = new TranslationsExportHelper(project);
        return instance;
    }

    public static TranslationsExportHelper getInstance() {
        return instance;
    }

    private final EtkProject project;
    private DWFile tempDirectoryExport;
    private DWFile translationFilesDirectory;
    private DWFile archiveDirectory;
    private DWFile archiveDirectoryForTruck;
    private String currentJobId;

    public TranslationsExportHelper(EtkProject project) {
        this.project = project;
    }

    /**
     * Liefert den Dateinamen nach den Vorgaben aus dem technischen Dokument
     *
     * @param textKind
     * @param jobId
     * @return
     */
    private String getFileName(iPartsDataDictTextKind textKind, String jobId) {
        if (StrUtils.isValid(jobId)) {
            DictTextKindTransitTypes transitTextKind = DictTxtKindIdByMADId.getInstance(project).getFirstTransitTextKindsForiPartsTextKind(textKind.getAsId());
            if (transitTextKind != DictTextKindTransitTypes.UNKNOWN) {
                return SYSTEM_ID + FILE_NAME_DELIMITER + transitTextKind.getTextKindFileName() + FILE_NAME_DELIMITER + jobId + "." + MimeTypes.EXTENSION_XML;
            }
        }
        return null;
    }

    public EtkProject getProject() {
        return project;
    }

    private String getCustomerUserIdCar() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsString(iPartsImportPlugin.CONFIG_TRANSIT_CUSTOM_USER_ID_CAR);
    }

    private String getContactPersonUserIdCar() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsString(iPartsImportPlugin.CONFIG_TRANSIT_CONTACT_PERSON_USER_ID_CAR);
    }

    private String getAdditionalContactEmailCar() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsString(iPartsImportPlugin.CONFIG_TRANSIT_ADDITIONAL_CONTACT_EMAIL_CAR);
    }

    private String getCustomerUserIdTruck() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsString(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_CUSTOM_USER_ID);
    }

    private String getContactPersonUserIdTruck() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsString(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_CONTACT_PERSON_USER_ID);
    }

    private String getAdditionalContactEmailTruck() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsString(iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_ADDITIONAL_CONTACT_EMAIL);
    }

    /**
     * Verarbeitet ein {@link TranslationJobContainer} Objekt. Hierbei wird ein Übersetzungsarchiv samt Inhalt erzeugt
     * sowie pro Eintrag in der XML ein Datensatz in der DB angelegt.
     *
     * @param transJobContainer
     * @param messageLog
     * @return
     */
    public int handleTranslationPackage(TranslationJobContainer transJobContainer, EtkMessageLog messageLog) {
        DWOutputStream outputStream = null;
        XMLStreamWriter xmlWriter = null;
        // Unterordner, damit nicht für jede Datei ein neuer temporärer Ordner angelegt wird
        String subFolder = transJobContainer.getSourceLang().getCode() + FILE_NAME_DELIMITER +
                           transJobContainer.getTextKind().getName(transJobContainer.getSourceLang().getCode()) + FILE_NAME_DELIMITER +
                           transJobContainer.getDestLang().getCode();
        int storedEntries = 0;
        boolean isTruckObjectStoreTranslations = transJobContainer.isTruckObjectStoreTranslations();
        // Erst wenn alle iPartsDataDictMeta Objekte für einen Transit-Job abgearbeitet wurden, ist der Export vorbei.
        while (!transJobContainer.getDataDictMetas().isEmpty()) {
            DWFile exportFile;
            String bundleDate = DateUtils.getCurrentDateFormatted(DateUtils.simpleTimeFormatyyyyMMddHHmmss);
            transJobContainer.setBundleDate(bundleDate);
            bundleDate = bundleDate.substring(0, bundleDate.length() - 2); // Sekunden abschneiden
            // Bundlename nach vorgegebenen Muster erzeugen
            String bundleName = createBundleName(transJobContainer, bundleDate);
            transJobContainer.setBundleName(bundleName);
            // Listen für alle DictMetas und TransJob Objekte, die nach der Erzeugung gespeichert werden sollen
            iPartsDataDictMetaList metasToStore = new iPartsDataDictMetaList();
            iPartsDataDictTransJobList transJobList = new iPartsDataDictTransJobList();
            try {
                // Dateinamen bestimmen und XMLWriter initialisieren
                String filename = getFileName(transJobContainer.getTextKind(), transJobContainer.getJobId());
                exportFile = getTempSubDirectory(subFolder).getChild(filename);
                XMLOutputFactory factory = XMLOutputFactory.newInstance();
                outputStream = exportFile.getOutputStream();
                // Keinen Indenting- sondern einen normalen XMLStreamWriter verwenden, weil es sonst zu ungewollten Leerzeichen
                // im Text kommt, wenn mehrzeilige Texte mit <br/> Tags verwendet werden
                xmlWriter = factory.createXMLStreamWriter(outputStream, "UTF-8");
                byte[] fileBOM = DWFileCoding.UTF8_BOM.getBom();
                if (fileBOM.length > 0) {
                    outputStream.write(fileBOM);
                }
                // Initialisierung des Writers ist fertig -> Beginne mit dem Schreiben der Informationen
                writeAndCreateContentEntries(transJobContainer, xmlWriter, metasToStore, transJobList, messageLog, isTruckObjectStoreTranslations);
                // Mögliche gecachte Informationen ausspülen
                xmlWriter.flush();
                outputStream.flush();
            } catch (Exception e) {
                Logger.getLogger().handleRuntimeException(e);
                break;
            } finally {
                try {
                    if (xmlWriter != null) {
                        xmlWriter.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }

                } catch (Exception e) {
                    Logger.getLogger().handleRuntimeException(e);
                    break;
                }

            }
            // Nachdem die Exportdatei erzeugt wurde muss nun das dazugehörige Archiv erzeugt werden
            if (exportFile.exists(1000) && createAndPushArchive(exportFile, transJobContainer, messageLog, isTruckObjectStoreTranslations)) {
                // Bundlename muss geleert werden, da bei jedem Durchlauf ein neue Name pro XML Datei erzeugt wird
                transJobContainer.setBundleName("");
                // Unterordner mit der XML Datei löschen
                deleteFileOrDirectory(exportFile.getParentDWFile());

                // Das Erzeugen der XML und der Archivdatei war erfolgreich -> jetzt erst dürfen die DB Objekte gespeichert werden
                storeObjectList(metasToStore, transJobList);
                TranslationsHelper.writeLogMessage(messageLog, translateForLog("!!Für die Ausgangssprache \"%1\", die Textart \"%2\" und Zielsprache" +
                                                                               " \"%3\" wurde ein Übersetzungsauftrag mit %4 Einträgen erzeugt",
                                                                               transJobContainer.getSourceLang().getCode(),
                                                                               transJobContainer.getTextKind().getName(project.getDBLanguage()),
                                                                               transJobContainer.getDestLang().getCode(),
                                                                               String.valueOf(transJobList.size())));
                storedEntries += transJobList.size();
            }
        }
        return storedEntries;

    }


    /**
     * Liefert den Archivnamen nach einem vorgegebenen Muster
     *
     * @param transJobContainer
     * @param bundleDate
     * @return
     */
    private String createBundleName(TranslationJobContainer transJobContainer, String bundleDate) {
        return SYSTEM_ID
               + FILE_NAME_DELIMITER
               + FUNCTIONAL_ID
               + FILE_NAME_DELIMITER
               + TranslationJobContainer.JobTypes.TRANSLATION.getType()
               + FILE_NAME_DELIMITER
               + transJobContainer.getJobId()
               + FILE_NAME_DELIMITER + FILE_NAME_DELIMITER
               + iPartsTransitMappingCache.getInstance(getProject()).getTransitLangForIsoLang(transJobContainer.getSourceLang())
               + FILE_NAME_DELIMITER
               + LANGUAGE_VARIANT
               + FILE_NAME_DELIMITER + FILE_NAME_DELIMITER
               + iPartsTransitMappingCache.getInstance(getProject()).getTransitLangForIsoLang(transJobContainer.getDestLang())
               + FILE_NAME_DELIMITER
               + LANGUAGE_VARIANT
               + FILE_NAME_DELIMITER + FILE_NAME_DELIMITER
               + bundleDate;
    }


    /**
     * Erzeugt ein Übersetzungsarchiv und legt es in dem vorgesehenen Verzeichnis ab
     *
     * @param exportFile
     * @param transJobContainer
     * @param messageLog
     * @param isTruckObjectStoreTranslations
     * @return
     */
    private boolean createAndPushArchive(DWFile exportFile, TranslationJobContainer transJobContainer, EtkMessageLog messageLog,
                                         boolean isTruckObjectStoreTranslations) {
        // Erst die Substruktur des Archivs erzeugen
        DWFile tempArchiveSubStructure = getArchiveSubstructure(isTruckObjectStoreTranslations);
        if (tempArchiveSubStructure == null) {
            TranslationsHelper.writeLogMessageError(messageLog, "!!Template Substruktur für das Archiv konnte nicht erzeugt werden");
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not create template substructure for archive");
            return false;
        }
        // Nun die TOC.xml Datei mit den Infos zum Übersetzungsarchiv
        DWFile tocFile = tempArchiveSubStructure.getChild(TOC_FILE_NAME);
        try {
            createTOCFile(tocFile, exportFile, transJobContainer, isTruckObjectStoreTranslations);
            TranslationsHelper.writeLogMessage(messageLog, translateForLog("!!\"_TOC.xml\" Datei für das Archiv \"%1\" erzeugt", transJobContainer.getBundleName()));
            // Wurde eine gültige TOC Datei erzeugt, dann wird die Übersetzungsdatei in den vorgesehenen Ordner in der
            // Substruktur abgelegt
            if (!tocFile.exists(DWFile.DEFAULT_FILE_EXISTS_TIMEOUT) || !exportFile.copy(translationFilesDirectory, true)) {
                TranslationsHelper.writeLogMessageError(messageLog, translateForLog("!!TOC Datei für das Archiv \"%1\" konnte nicht erstellt werden!", transJobContainer.getBundleName()));
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not create TOC file for archive");
                return false;
            }

            // Fehlermeldungen, falls es Probleme mit der Exportdatei oder dem Verzeichnis gibt
            if ((translationFilesDirectory != null) && (!translationFilesDirectory.exists(DWFile.DEFAULT_FILE_EXISTS_TIMEOUT) || !translationFilesDirectory.isDirectory())) {
                TranslationsHelper.writeLogMessageError(messageLog, translateForLog("!!Übersetzungsverzeichnis \"%1\" " +
                                                                                    "für das Archiv \"%2\" existiert nicht!",
                                                                                    translationFilesDirectory.getAbsolutePath(),
                                                                                    transJobContainer.getBundleName()));
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Translation directory for archive "
                                                                                + transJobContainer.getBundleName() + " does not exist!");
                return false;
            }

            if (!exportFile.exists(DWFile.DEFAULT_FILE_EXISTS_TIMEOUT)) {
                TranslationsHelper.writeLogMessageError(messageLog, translateForLog("!!Übersetzungsdatei \"%1\" " +
                                                                                    "für das Archiv \"%2\" konnte nicht erstellt werden!",
                                                                                    exportFile.getAbsolutePath(),
                                                                                    transJobContainer.getBundleName()));
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not create translation file "
                                                                                + exportFile.getAbsolutePath()
                                                                                + " for archive "
                                                                                + transJobContainer.getBundleName());
                return false;
            }

            // War bisher alles erfolgreich, dann hole das Ausgangsverzeichnis
            DWFile exportDirectory = iPartsImportPlugin.getTranslationDirectory(iPartsImportPlugin.CONFIG_TRANSIT_DIR_NAME_TEXT_OUTGOING);
            if (exportDirectory == null) {
                TranslationsHelper.writeLogMessageError(messageLog, translateForLog("!!Auf das Übersetzungsverzeichnis für ausgehende Dateien kann nicht zugegriffen werden!"));
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not access translation outgoing directory");
                return false;
            }
            // Bei einem Übersetzungslauf über den Object Store wird keine Datei auf der lokalen Platte erzeugt. Der
            // Inhalt wird direkt als byte Stream hochgeladen
            if (isTruckObjectStoreTranslations) {
                TranslationsObjectStoreHelper.getInstance().exportTranslations(tempArchiveSubStructure.getPath(), transJobContainer.getBundleName(), StandardCharsets.UTF_8);
            } else {
                // Und erzeuge mit dem Inhalt in der Substruktur ein ZIP Archiv im Ausgangsverzeichnis
                // Die Datei erst mit einer ungültigen Endung erzeugen und dann via atomarer Operation in ".zip" umbenennen.
                DWFile zipFile = exportDirectory.getChild(transJobContainer.getBundleName() + ".iparts_temp");
                CompressionUtils.zipDir(zipFile.getAbsolutePath(), tempArchiveSubStructure.getPath(), null, StandardCharsets.UTF_8);
                if (zipFile.renameTo(DWFile.get(zipFile.getParentDWFile(), zipFile.extractFileName(false) + "." + MimeTypes.EXTENSION_ZIP))) {
                    TranslationsHelper.writeLogMessage(messageLog, translateForLog("!!Archiv \"%1\" erzeugt und abgelegt", transJobContainer.getBundleName()));
                }
            }
        } catch (IOException e) {
            String logMessage = "Could not create TOC file and archive:";
            if (exportFile != null) {
                logMessage += " file name \"" + exportFile.getPath() + "\";";
            }
            if (transJobContainer != null) {
                logMessage += " bundle name: \"" + transJobContainer.getBundleName() + "\"";
            }
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, logMessage);
            Logger.getLogger().handleRuntimeException(e);
            return false;
        } finally {
            // Lösche die erzeugte TOC Datei und die XML Übersetzungsdatei (die einzigen zwei spezifischen Dateien)
            deleteFileOrDirectory(tocFile);
            if ((translationFilesDirectory != null) && translationFilesDirectory.exists(1000)) {
                translationFilesDirectory.deleteDirContentRecursively();
            }
        }
        return true;
    }

    /**
     * Erzeugt die _TOC.xml Datei für das Übersetzungsarchiv. Diese Datei enthält die Informationen zum Aufbau des
     * Archivs sowie die Eigenschaften der darin erzeugten XML Datei.
     *
     * @param tocFile
     * @param exportFile
     * @param transJobContainer
     * @param isTruckObjectStoreTranslations
     * @throws IOException
     */
    private void createTOCFile(DWFile tocFile, DWFile exportFile, TranslationJobContainer transJobContainer, boolean isTruckObjectStoreTranslations) throws IOException {
        DwXmlNode languagePackageNode = new DwXmlNode(ELEMENT_LANGUAGE_PACKAGE);
        languagePackageNode.setAttribute(ATT_SYSTEM, SYSTEM_ID);
        languagePackageNode.setAttribute(ATT_SUBJECT, FUNCTIONAL_ID);
        languagePackageNode.setAttribute(ATT_TASK, transJobContainer.getJobType().getType());
        languagePackageNode.setAttribute(ATT_SOURCE_LANG, getLangValueForXML(transJobContainer.getSourceLang()));
        languagePackageNode.setAttribute(ATT_TARGET_LANG, getLangValueForXML(transJobContainer.getDestLang()));
        languagePackageNode.setAttribute(ATT_JOB_ID, transJobContainer.getJobId());
        languagePackageNode.setAttribute(ATT_CREATE_DATE_TIME, getCurrentISODate());
        switch (transJobContainer.getJobId().substring(0, 1)) {
            case iPartsDictTransJobHelper.JOB_ID_PREFIX_CAR_AND_VAN:
                setOptionalTOCAttribute(ATT_CUSTOMER_USER_ID, getCustomerUserIdCar(), languagePackageNode);
                setOptionalTOCAttribute(ATT_CONTACT_PERSON_USER_ID, getContactPersonUserIdCar(), languagePackageNode);
                setOptionalTOCAttribute(ATT_ADDITIONAL_CONTACT_EMAIL, getAdditionalContactEmailCar(), languagePackageNode);
                break;
            case iPartsDictTransJobHelper.JOB_ID_PREFIX_TRUCK_AND_BUS:
                setOptionalTOCAttribute(ATT_CUSTOMER_USER_ID, getCustomerUserIdTruck(), languagePackageNode);
                setOptionalTOCAttribute(ATT_CONTACT_PERSON_USER_ID, getContactPersonUserIdTruck(), languagePackageNode);
                setOptionalTOCAttribute(ATT_ADDITIONAL_CONTACT_EMAIL, getAdditionalContactEmailTruck(), languagePackageNode);
                break;
        }
        DwXmlNode filesNode = new DwXmlNode(ELEMENT_FILES);
        DwXmlNode fileNode = new DwXmlNode(ELEMENT_FILE);
        fileNode.setTextContent(TOC_FILE_NAME);
        filesNode.appendChild(fileNode);
        languagePackageNode.appendChild(filesNode);

        DwXmlNode foldersNode = new DwXmlNode(ELEMENT_FOLDERS);
        foldersNode.appendChild(makeFolderNode(FOLDER_NAME_INFO, EMPTY_FOLDER_FILE_NAME));
        foldersNode.appendChild(makeFolderNode(FOLDER_NAME_PICTURES, EMPTY_FOLDER_FILE_NAME));
        DWFile schemaFile = TranslationsHelper.getSchemaFile(isTruckObjectStoreTranslations);
        if (schemaFile != null) {
            foldersNode.appendChild(makeFolderNode(FOLDER_NAME_SCHEMA, schemaFile.extractFileName(true)));
        }
        DWFile stylesheetFile = TranslationsHelper.getStyleSheetFile(isTruckObjectStoreTranslations);
        if (stylesheetFile != null) {
            foldersNode.appendChild(makeFolderNode(FOLDER_NAME_STYLE_SHEET, stylesheetFile.extractFileName(true)));
        }
        foldersNode.appendChild(makeFolderNode(FOLDER_NAME_TRANSLATION_FILES, exportFile.extractFileName(true)));

        languagePackageNode.appendChild(foldersNode);

        DwXmlFile xmlFile = new DwXmlFile(languagePackageNode);
        xmlFile.write(tocFile, true);
    }

    /**
     * Setzt das optionale Attribut für den übergebenen XML Knoten
     *
     * @param attName
     * @param value
     * @param xmlNode
     */
    private void setOptionalTOCAttribute(String attName, String value, DwXmlNode xmlNode) {
        if (StrUtils.isValid(value)) {
            xmlNode.setAttribute(attName, value);
        }
    }

    private String getCurrentISODate() {
        return DateUtils.getCurrentDateFormatted(DATE_TIME_PATTERN);
    }

    /**
     * Erzeugt ein <folder> Element samt allen Hinweisen auf die darin enthaltenen "leeren" Dateien.
     *
     * @param foldernameContent
     * @param fileContent
     * @return
     */
    private DwXmlNode makeFolderNode(String foldernameContent, String fileContent) {
        DwXmlNode folderNode = new DwXmlNode(ELEMENT_FOLDER);
        DwXmlNode folderNameNode = new DwXmlNode(ELEMENT_FOLDER_NAME);
        folderNameNode.setTextContent(foldernameContent);
        folderNode.appendChild(folderNameNode);
        DwXmlNode filesNode = new DwXmlNode(ELEMENT_FILES);
        DwXmlNode fileNode = new DwXmlNode(ELEMENT_FILE);
        fileNode.setTextContent(fileContent);
        filesNode.appendChild(fileNode);
        folderNode.appendChild(filesNode);
        return folderNode;

    }

    /**
     * Liefert den Sprachen-Wert für die XML Datei, z.B. DEU_XXX
     *
     * @param language
     * @return
     */
    private String getLangValueForXML(Language language) {
        return iPartsTransitMappingCache.getInstance(getProject()).getTransitLangForIsoLang(language) + FILE_NAME_DELIMITER + LANGUAGE_VARIANT;
    }

    /**
     * Liefert das Verzeichnis mit der Substruktur für ein Übersetzungsarchiv
     *
     * @param isTruckObjectStoreTranslations
     * @return
     */
    private DWFile getArchiveSubstructure(boolean isTruckObjectStoreTranslations) {
        if (isTruckObjectStoreTranslations) {
            if ((archiveDirectoryForTruck == null) || !archiveDirectoryForTruck.exists(1000)) {
                archiveDirectoryForTruck = createArchiveDirectory(isTruckObjectStoreTranslations);
            }
            return archiveDirectoryForTruck;
        } else {
            if ((archiveDirectory == null) || !archiveDirectory.exists(1000)) {
                archiveDirectory = createArchiveDirectory(isTruckObjectStoreTranslations);
            }
            return archiveDirectory;
        }
    }

    /**
     * Erzeugt ein temporäres Archiv für die Struktur einer Übersetzungs-ZIP-Datei
     *
     * @param isTruckObjectStoreTranslations
     * @return
     */
    private DWFile createArchiveDirectory(boolean isTruckObjectStoreTranslations) {
        DWFile directory = DWFile.createTempDirectory(isTruckObjectStoreTranslations ? "daim_truck" : "daim");
        if (directory == null) {
            return null;
        }
        if (!createSingleDirectories(directory, isTruckObjectStoreTranslations)) {
            if (!directory.deleteRecursively()) {
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not delete temporary directories for translations");
            }
            return null;
        }
        return directory;

    }

    /**
     * Erzeugt die einzelnen Subverzeichnisse für die Substruktur in einem Übersetzungsarchiv
     *
     * @param tempDirectory
     * @param isTruckObjectStoreTranslations
     * @return
     */
    private boolean createSingleDirectories(DWFile tempDirectory, boolean isTruckObjectStoreTranslations) {
        // Info Verzeichnis
        makeFolderWithEmptyFile(tempDirectory, FOLDER_NAME_INFO);
        // Pictures Verzeichnis
        makeFolderWithEmptyFile(tempDirectory, FOLDER_NAME_PICTURES);
        // Schema-DTD Verzeichnis
        DWFile schemaDir = tempDirectory.getChild(FOLDER_NAME_SCHEMA);
        if (!schemaDir.mkDirsWithRepeat()) {
            return false;
        }
        DWFile schemaFile = TranslationsHelper.getSchemaFile(isTruckObjectStoreTranslations);
        if ((schemaFile == null) || !schemaFile.copy(schemaDir, true)) {
            return false;
        }
        // Stylesheet Verzeichnis
        DWFile styleSheetDir = tempDirectory.getChild(FOLDER_NAME_STYLE_SHEET);
        if (!styleSheetDir.mkDirsWithRepeat()) {
            return false;
        }
        DWFile styleSheetFile = TranslationsHelper.getStyleSheetFile(isTruckObjectStoreTranslations);
        if ((styleSheetFile == null) || !styleSheetFile.copy(styleSheetDir, true)) {
            return false;
        }
        // TranslationFiles Verzeichnis
        translationFilesDirectory = tempDirectory.getChild(FOLDER_NAME_TRANSLATION_FILES);
        if (!translationFilesDirectory.mkDirsWithRepeat()) {
            return false;
        }
        return true;
    }

    /**
     * Erzeugt ein Verzeichnis mit einer leeren Datei. Die Bezeichnung der Datei ist fest vorgegeben und lautet
     * "emptyFolder.txt"
     *
     * @param tempDirectory
     * @param folderName
     * @return
     */
    private boolean makeFolderWithEmptyFile(DWFile tempDirectory, String folderName) {
        DWFile folder = tempDirectory.getChild(folderName);
        if (!folder.mkDirsWithRepeat()) {
            return false;
        }
        DWFile emptyFile = folder.getChild(EMPTY_FOLDER_FILE_NAME);
        try {
            emptyFile.writeTextFile(new byte[0]);
        } catch (IOException e) {
            Logger.getLogger().handleRuntimeException(e);
            return false;
        }
        return emptyFile.exists(1000);
    }


    /**
     * Liefert ein temporäres Unterverzeichnis
     *
     * @param subFolder
     * @return
     */
    private DWFile getTempSubDirectory(String subFolder) {
        DWFile directory = getExportTempDirectory();
        directory = directory.getChild(subFolder);
        if (!directory.exists(1000)) {
            directory.mkDirsWithRepeat();
        }
        return directory;
    }

    /**
     * Schreibt den gesamten Inhalt einer Übersetzungs-XML-Datei
     *
     * @param transJobContainer
     * @param xmlWriter
     * @param metasToStore
     * @param transJobList
     * @param messageLog
     * @param isTruckObjectStoreTranslations
     * @throws XMLStreamException
     */
    private void writeAndCreateContentEntries(TranslationJobContainer transJobContainer, XMLStreamWriter xmlWriter,
                                              iPartsDataDictMetaList metasToStore, iPartsDataDictTransJobList transJobList,
                                              EtkMessageLog messageLog, boolean isTruckObjectStoreTranslations) throws XMLStreamException {
        TranslationsHelper.writeLogMessage(messageLog, translateForLog("!!Erzeuge XML Datei für das Archiv \"%1\"", transJobContainer.getBundleName()));
        // XML Dokument öffnen
        openXMLDocument(transJobContainer, xmlWriter);
        // XML Inhalt schreiben und DB Einträge erzeugen
        writeAndStoreSingleEntries(xmlWriter, transJobContainer, metasToStore, transJobList, messageLog, isTruckObjectStoreTranslations);
        // XML Dokument schließen
        closeXMLDocument(xmlWriter);
    }

    /**
     * Schließt das komplette XML Dokument
     *
     * @param xmlWriter
     * @throws XMLStreamException
     */
    private void closeXMLDocument(XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeEndElement();
        xmlWriter.writeEndDocument();
    }

    /**
     * Öffnet das komplette XML Dokument
     *
     * @param transJobContainer
     * @param xmlWriter
     * @throws XMLStreamException
     */
    private void openXMLDocument(TranslationJobContainer transJobContainer, XMLStreamWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartDocument("UTF-8", "1.0");
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put(ATT_XMLNS, XML_NS);
        attributes.put(ATT_TASK, transJobContainer.getJobType().getType());
        attributes.put(ATT_SYSTEM, SYSTEM_ID);
        attributes.put(ATT_SUBJECT, FUNCTIONAL_ID);
        attributes.put(ATT_JOB_ID, transJobContainer.getJobId());
        transJobContainer.setTranslationDate(SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance()));
        attributes.put(ATT_EXPORTED, getCurrentISODate());
        attributes.put(ATT_TARGET_LANG, getLangValueForXML(transJobContainer.getDestLang()));
        attributes.put(ATT_SOURCE_LANG, getLangValueForXML(transJobContainer.getSourceLang()));
        attributes.put(ATT_SCHEMA_LOCATION, SCHEMA_LOCATION);
        startElement(xmlWriter, ELEMENT_XPRT_TRANSLATION, attributes);
    }

    /**
     * Schreibt die einzelnen "translation" Elemente in einer Übersetzungsdatei und speichert die dazugehörigen DataObjects
     *
     * @param xmlWriter
     * @param transJobContainer
     * @param messageLog
     * @param isTruckObjectStoreTranslations
     * @throws XMLStreamException
     */
    private void writeAndStoreSingleEntries(XMLStreamWriter xmlWriter, TranslationJobContainer transJobContainer,
                                            iPartsDataDictMetaList metasToStore, iPartsDataDictTransJobList transJobList,
                                            EtkMessageLog messageLog, boolean isTruckObjectStoreTranslations) throws XMLStreamException {
        Map<String, String> attributes = new LinkedHashMap<>();
        Language sourceLang = transJobContainer.getSourceLang();
        Language destLang = transJobContainer.getDestLang();
        // Attribute müssen nur einmal bestimmt werden
        DBDataObjectAttributes attributesForSet = calculateAttributes(transJobContainer);
        // <translations>
        startElement(xmlWriter, ELEMENT_TRANSLATIONS, null);
        Iterator<iPartsDataDictMeta> iter = transJobContainer.getDataDictMetas().iterator();
        // Durchlaufe alle DictMetas und erzeuge daraus einen Eintrag für die XML und einen Eintrag in der DB
        int maxEntriesPerFile = TranslationsHelper.getMaxEntriesPerFile(isTruckObjectStoreTranslations);
        while (iter.hasNext()) {
            if (transJobList.size() >= maxEntriesPerFile) {
                TranslationsHelper.writeLogMessage(messageLog, translateForLog("!!XML Datei für das Archiv \"%1\" hat das Maximum" +
                                                                               " von %2 Einträgen pro Datei erreicht. Es wird ein weiteres Archiv erstellt!",
                                                                               transJobContainer.getBundleName(), String.valueOf(maxEntriesPerFile)));
                break;
            }
            iPartsDataDictMeta dictMeta = iter.next();
            // Eintrag in der XML Datei erzeugen
            handleXMLFileEntry(xmlWriter, dictMeta, transJobContainer, attributes);
            // Eintrag in der DB erzeugen
            handleDataObjects(dictMeta, messageLog, transJobList, sourceLang, destLang, transJobContainer.getJobId(), attributesForSet);
            // Nachdem eine Ausgangssprache-Textart-Zielsprache Kombination für ein DictMeta Objekt erzeugt wurde, muss
            // das DictMeta Objekt entfernt werden
            iter.remove();
            // zur Sicherheit, falls dataDictMeta aus der Anzeige kommt
            dictMeta.removeForeignTablesAttributes();
            // Translation Status für jede existierende Sprache setzen
            // => wird bei der nächsten Suche nach neuen Texten nicht mehr gefunden
            dictMeta.setTranslationStateForAllExistingLanguages(iPartsDictSpracheTransState.IN_TRANSLATION_WORKFLOW, DBActionOrigin.FROM_EDIT);
            metasToStore.add(dictMeta, DBActionOrigin.FROM_EDIT);
        }
        // </translations>
        xmlWriter.writeEndElement();
    }

    /**
     * Speichert die übergebenen {@link EtkDataObjectList}s
     *
     * @param metasToStore
     * @param transJobList
     * @return
     */
    private boolean storeObjectList(EtkDataObjectList metasToStore, EtkDataObjectList transJobList) {
        if (!metasToStore.isEmpty() || !transJobList.isEmpty()) {
            project.getDbLayer().startBatchStatement();
            project.getDbLayer().startTransaction();
            try {
                if (!metasToStore.isEmpty()) {
                    metasToStore.saveToDB(project);
                }
                if (!transJobList.isEmpty()) {
                    transJobList.saveToDB(project);
                }
                project.getDbLayer().endBatchStatement();
                project.getDbLayer().commit();
            } catch (Exception e) {
                project.getDbLayer().cancelBatchStatement();
                project.getEtkDbs().rollback();
                Logger.getLogger().handleRuntimeException(e);
                return false;
            }
        }
        return true;
    }

    /**
     * Erzeugt die {@link iPartsDataDictTransJob} Objekte für den übergebenen {@link TranslationJobContainer}. Der Container
     * enthält alle gewünschten Attribute für einen neuen Übersetzungsauftrag.
     *
     * @param dataDictMeta
     * @param messageLog
     * @param transJobList
     * @param sourceLang
     * @param destLang
     * @param jobId
     * @param attributesForSet
     */
    private void handleDataObjects(iPartsDataDictMeta dataDictMeta, EtkMessageLog messageLog, iPartsDataDictTransJobList transJobList,
                                   Language sourceLang, Language destLang, String jobId, DBDataObjectAttributes attributesForSet) {

        String textId = dataDictMeta.getTextId();
        // für noch nicht übersetzte Sprachen: TransJob anlegen
        iPartsDictTransJobId id = new iPartsDictTransJobId(textId, sourceLang, destLang, jobId);
        iPartsDataDictTransJob dictTransJob = new iPartsDataDictTransJob(project, id);
        if (!dictTransJob.existsInDB()) {
            dictTransJob.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        }
        // Attribute besetzen
        setAttributes(dictTransJob, attributesForSet);
        TranslationsHelper.writeLogMessage(messageLog, translateForLog("!!Für Text-Id \"%1\" wurde ein Eintrag mit der JobId \"%2\" angelegt. " +
                                                                       "Ausgangssprache: %3, Zielsprache %4", textId, jobId, sourceLang.getCode(), destLang.getCode()));
        transJobList.add(dictTransJob, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Berechnet die Attribute für alle {@link iPartsDataDictTransJob} Objekte mit der gleichen Ausgangs- und Zielsprache
     * sowie der gleichen Textart.
     *
     * @return
     */
    private DBDataObjectAttributes calculateAttributes(TranslationJobContainer transJobContainer) {
        DBDataObjectAttributes attributesForSet = new DBDataObjectAttributes();
        attributesForSet.addField(FIELD_DTJ_TRANSLATION_DATE, transJobContainer.getTranslationDate(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        attributesForSet.addField(FIELD_DTJ_BUNDLE_NAME, transJobContainer.getBundleName(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);          // Paketname (Dateiname des Archivs)
        attributesForSet.addField(FIELD_DTJ_TRANSLATION_STATE, iPartsDictTransJobStates.TRANS_EXPORTED.getDbValue(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);    // Gesamtstatus
        attributesForSet.addField(FIELD_DTJ_STATE_CHANGE, "", DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);         // Attribut für jeden Statuswechsel mit Zeitstempel
        attributesForSet.addField(FIELD_DTJ_LAST_MODIFIED, transJobContainer.getBundleDate(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);        // Datum letzte Änderung: Zeitstempel wird bei jeder Statusänderung aktualisiert
        attributesForSet.addField(FIELD_DTJ_JOB_TYPE, transJobContainer.getJobType().getType(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);             // Auftragstyp: Ueb (Übersetzung) (später gibt es evtl. einen Kor=Korrekturauftrag)
        attributesForSet.addField(FIELD_DTJ_TEXTKIND, transJobContainer.getTextKind().getAsId().getTextKindId(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);             // Textart: Hat ein Text mehrere Textarten soll die erste genommen werden.
        attributesForSet.addField(FIELD_DTJ_USER_ID, transJobContainer.getUserId(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);              // User: System/UserID der den Übersetzungslauf gestartet hat
        attributesForSet.addField(FIELD_DTJ_ERROR_CODE, "", DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);           // Error-Code
        return attributesForSet;
    }

    /**
     * zuvor festgelegte Attribute vererben an dictTransJob
     *
     * @param dictTransJob
     * @param attributesForSet
     */
    private void setAttributes(iPartsDataDictTransJob dictTransJob, DBDataObjectAttributes attributesForSet) {
        for (DBDataObjectAttribute attrib : attributesForSet.getFields()) {
            dictTransJob.setFieldValue(attrib.getName(), attrib.getAsString(), DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Erzeugt einen Eintrag im XML Dokument
     *
     * @param xmlWriter
     * @param dictMeta
     * @param transJobContainer
     * @param attributes
     * @throws XMLStreamException
     */
    private void handleXMLFileEntry(XMLStreamWriter xmlWriter, iPartsDataDictMeta dictMeta,
                                    TranslationJobContainer transJobContainer, Map<String, String> attributes) throws XMLStreamException {
        // <translation>
        startElement(xmlWriter, ELEMENT_TRANSLATION, null);
        String languageId = iPartsTransitMappingCache.getInstance(project).getLanguageId(transJobContainer.getSourceLang());
        attributes.put(ATT_LANGUAGE_ID, languageId);
        attributes.put(ATT_TRANSLATE_VERSION, DEFAULT_ID_VALUE);
        attributes.put(ATT_OBJECT_ID, dictMeta.getTextId());
        attributes.put(ATT_OBJECT_VERSION, DEFAULT_ID_VALUE);
        // <text>
        startElement(xmlWriter, ELEMENT_TEXT, attributes);
        attributes.clear();
        attributes.put(ATT_MAX_LENGTH, DEFAULT_MAX_LENGTH_TRANSLATION);
        String text = dictMeta.getMultiLang().getText(transJobContainer.getSourceLang().getCode());
        // <ContentTranslate>
        startElement(xmlWriter, ELEMENT_CONTENT_TRANSLATE, text, attributes, true);
        // </ContentTranslate>
        xmlWriter.writeEndElement();
        // </text>
        xmlWriter.writeEndElement();
        attributes.clear();
        DictTextKindTransitTypes textkind = DictTxtKindIdByMADId.getInstance(project).getFirstTransitTextKindsForiPartsTextKind(transJobContainer.getTextKind().getAsId());
        attributes.put(ATT_TYPE, textkind.getXmlMetaInformationType());
        // <metaInformation>
        startElement(xmlWriter, ELEMENT_META_INFORMATION, attributes);
        attributes.clear();
        // </metaInformation>
        xmlWriter.writeEndElement();
        // <sourceText>
        startElement(xmlWriter, ELEMENT_SOURCE_TEXT, null);
        // <ContentNoTranslate>
        startElement(xmlWriter, ELEMENT_CONTENT_NO_TRANSLATE, text, null, true);
        // </ContentNoTranslate>
        xmlWriter.writeEndElement();
        // </sourceText>
        xmlWriter.writeEndElement();
        // </translation>
        xmlWriter.writeEndElement();
    }

    /**
     * Öffnet ein XML Element
     *
     * @param xmlWriter
     * @param element
     * @param attributes
     * @throws XMLStreamException
     */
    private void startElement(XMLStreamWriter xmlWriter, String element, Map<String, String> attributes) throws XMLStreamException {
        startElement(xmlWriter, element, null, attributes, false);
    }

    /**
     * Öffnet ein XML Element und setzt den optionalen Text {@param content}
     *
     * @param xmlWriter
     * @param element
     * @param content
     * @param attributes
     * @param convertNewlines falls <code>true</code> werden Newlines zu <code><br/></code> konvertiert
     * @throws XMLStreamException
     */
    private void startElement(XMLStreamWriter xmlWriter, String element, String content, Map<String, String> attributes,
                              boolean convertNewlines) throws XMLStreamException {
        if ((xmlWriter != null) && StrUtils.isValid(element)) {
            xmlWriter.writeStartElement(element);
            if (attributes != null) {
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    xmlWriter.writeAttribute(entry.getKey(), entry.getValue());
                }
            }
            if (StrUtils.isValid(content)) {
                writeText(xmlWriter, content, convertNewlines);
            }
        }
    }

    /**
     * Schreibt den Text {@param content}
     *
     * @param xmlWriter
     * @param content
     * @param convertNewlines falls <code>true</code> werden Newlines zu <code><br/></code> konvertiert
     * @throws XMLStreamException
     */
    private void writeText(XMLStreamWriter xmlWriter, String content, boolean convertNewlines) throws XMLStreamException {
        boolean textProcessed = false;
        if (convertNewlines && StrUtils.isValid(content)) {
            // zuerst alle newlines durch <br> ersetzen und danach damit tokenizen
            String brContent = StrUtils.replaceNewlinesWithBR(content);
            String br = "<br>";
            if (brContent.contains(br)) {
                StringTokenizer tokenizer = new StringTokenizer(brContent, br, true);
                while (tokenizer.hasMoreTokens()) {
                    String line = tokenizer.nextToken();
                    if (line.equals(br)) {
                        xmlWriter.writeEmptyElement(ELEMENT_NEWLINE);
                    } else {
                        xmlWriter.writeCharacters(line);
                    }
                }
                textProcessed = true;
            }
        }
        if (!textProcessed) { // falls keine newlines gefunden wurden, oder sie nicht umgewandelt werden sollten
            xmlWriter.writeCharacters(content);
        }
    }

    /**
     * Neue JobId auf Basis der bisherigen IDs in der DB liefern. Optional kann ein Präfix übergeben werden, den die
     * Job-Ids haben sollten.
     *
     * @param prefix
     * @return
     */
    private String retrieveJobIdFromDB(String prefix) {
        // Den höchsten Wert aus der DB holen
        String result;
        if (StrUtils.isValid(prefix)) {
            // Nur Job-Ids holen, die den übergebenen Präfix haben
            result = iPartsNumberHelper.getHighestOrderValueFromDBField(project, TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_JOBID,
                                                                        new String[]{ FIELD_DTJ_JOBID }, new String[]{ prefix + "*" });
        } else {
            // Nur Job-Ids holen, die keinen Präfix haben
            result = iPartsNumberHelper.getHighestOrderValueFromDBField(project, TABLE_DA_DICT_TRANS_JOB, FIELD_DTJ_JOBID,
                                                                        new String[]{ FIELD_DTJ_JOBID, FIELD_DTJ_JOBID },
                                                                        new String[]{ iPartsDictTransJobHelper.JOB_ID_PREFIX_CAR_AND_VAN + "*",
                                                                                      iPartsDictTransJobHelper.JOB_ID_PREFIX_TRUCK_AND_BUS + "*" },
                                                                        true);
        }
        if (result == null) {
            result = String.valueOf(0);
        }
        return result;
    }


    /**
     * Liefert die nächste Job-Id mit optionalen Präfix
     *
     * @param prefix
     * @return
     */
    public String getNextJobId(String prefix) {
        boolean validPrefix = StrUtils.isValid(prefix);
        if (validPrefix) {
            currentJobId = getNextJobIdWithPrefix(prefix);
        } else {
            currentJobId = getNextJobIdWithoutPrefix();
        }
        return currentJobId;
    }

    /**
     * Liefert die nächste Job-Id ohne Präfix
     *
     * @return
     */
    private String getNextJobIdWithoutPrefix() {
        String jobId = currentJobId;
        if (StrUtils.isEmpty(jobId)) {
            jobId = retrieveJobIdFromDB(null);
        }
        return increaseJobId(jobId);
    }

    /**
     * Liefert die nächste Job-Id mit dem übergebenen Präfix
     *
     * @param prefix
     * @return
     */
    private String getNextJobIdWithPrefix(String prefix) {
        String jobIdWithPrefix = currentJobId;
        if (StrUtils.isEmpty(jobIdWithPrefix) || !jobIdWithPrefix.startsWith(prefix)) {
            jobIdWithPrefix = retrieveJobIdFromDB(prefix);
        }
        // Bevor die Zahl hochgezählt werden kann, muss der Präfix entfernt werden
        if (jobIdWithPrefix.startsWith(prefix)) {
            jobIdWithPrefix = StrUtils.replaceFirstSubstring(jobIdWithPrefix, prefix, "");
        }
        jobIdWithPrefix = increaseJobId(jobIdWithPrefix);
        return prefix + jobIdWithPrefix;

    }

    private String increaseJobId(String jobId) {
        // Aktuelle ID um eins erhöhen
        int highestNumberAsInteger = StrUtils.strToIntDef(jobId, -1);
        highestNumberAsInteger++;
        return StrUtils.leftFill(String.valueOf(highestNumberAsInteger), JOBID_LENGTH, '0');
    }

    public void clearJobId() {
        currentJobId = null;
    }

    private DWFile getExportTempDirectory() {
        if ((tempDirectoryExport == null) || !tempDirectoryExport.exists(1000)) {
            tempDirectoryExport = DWFile.createTempDirectory("daim");
        }
        return tempDirectoryExport;
    }

    public void deleteTempDirectories() {
        deleteFileOrDirectory(tempDirectoryExport);
        deleteFileOrDirectory(translationFilesDirectory);
        deleteFileOrDirectory(archiveDirectory);
        deleteFileOrDirectory(archiveDirectoryForTruck);
    }

    private void deleteFileOrDirectory(DWFile directory) {
        if ((directory != null) && directory.exists(1000)) {
            directory.deleteRecursively();
        }
    }
}
