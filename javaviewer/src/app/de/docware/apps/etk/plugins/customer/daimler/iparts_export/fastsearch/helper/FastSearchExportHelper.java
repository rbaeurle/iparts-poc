 /*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

 package de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.helper;

 import com.owlike.genson.Genson;
 import de.docware.apps.etk.base.project.EtkProject;
 import de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects.ElasticSearchIndex;
 import de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects.ElasticSearchIndexMetaData;
 import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.iPartsModelPartsListsFastSearchS3Helper;
 import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.multithreadexporthelper.MultiThreadExportMessageFileHelper;
 import de.docware.apps.etk.plugins.customer.daimler.iparts_export.iPartsExportPlugin;
 import de.docware.framework.modules.gui.misc.MimeTypes;
 import de.docware.framework.utils.JSONUtils;
 import de.docware.util.StrUtils;
 import de.docware.util.file.DWFile;
 import de.docware.util.file.DWFileCoding;
 import de.docware.util.file.DWWriter;
 import de.docware.util.os.OsUtils;

 import java.io.IOException;
 import java.nio.charset.StandardCharsets;
 import java.util.*;

 /**
  * Helfer zum Exportieren der ElasticSearch Index Daten pro Sprache
  */
 public class FastSearchExportHelper {

     public static final String TEXT_DELIMITER_FOR_HASH = "_";

     private final FastSearchTextHelper fastSearchTextHelper;
     private Map<String, StringBuilder> builderForLanguage = null;
     private final Map<String, Set<String>> exportedLinesForLanguage;
     private final Genson genson;
     private final List<String> fallbackLanguages;
     private final List<String> exportLanguages;
     private final MultiThreadExportMessageFileHelper messageHelper;
     private final Map<String, String> metaDataForLanguage;
     private boolean isTestModeActive;

     public FastSearchExportHelper(EtkProject project, List<String> exportLanguages, MultiThreadExportMessageFileHelper messageHelper) {
         this.fastSearchTextHelper = new FastSearchTextHelper(project);
         // Genson zum serialisieren des default DTOs mit den aktuellen Werten pro Sprache
         this.genson = JSONUtils.createGensonWithOmittedFields(true);
         this.fallbackLanguages = project.getDataBaseFallbackLanguages();
         this.exportLanguages = exportLanguages;
         this.messageHelper = messageHelper;
         this.metaDataForLanguage = new HashMap<>();
         this.exportedLinesForLanguage = new HashMap<>();
         this.isTestModeActive = iPartsExportPlugin.isFastSearchTestModeActive();
     }

     public FastSearchTextHelper getFastSearchTextHelper() {
         return fastSearchTextHelper;
     }

     public Map<String, Set<String>> getExportedLinesForLanguage() {
         return exportedLinesForLanguage;
     }

     public List<String> getFallbackLanguages() {
         return fallbackLanguages;
     }

     public List<String> getExportLanguages() {
         return exportLanguages;
     }

     public Map<String, StringBuilder> initBuilder() {
         builderForLanguage = new HashMap<>();
         if (exportLanguages != null) {
             for (String language : exportLanguages) {
                 builderForLanguage.put(language, new StringBuilder());
             }
         }
         return builderForLanguage;
     }

     /**
      * Check, ob die erzeugte Zeile schon existiert
      *
      * @param language
      * @param indexLine
      * @return
      */
     private boolean checkIndexData(String language, String indexLine) {
         Set<String> lines = exportedLinesForLanguage.computeIfAbsent(language, k -> new HashSet<>());
         return lines.add(indexLine);
     }

     /**
      * Schreibt das DTO für die übergebene Sprache raus
      *
      * @param defaultPartIndex
      * @param language
      */
     public void writeDataForLanguage(ElasticSearchIndex defaultPartIndex, String language) {
         String indexLineData = genson.serialize(defaultPartIndex);
         // Check, ob der Datensatz schon erzeugt wurde
         if (!checkIndexData(language, indexLineData)) {
             return;
         }

         // StringBuilder für aktuelle Sprache holen und DTO rausschreiben
         StringBuilder builder = builderForLanguage.get(language);
         if (builder != null) {
             String metaData = getMetaData(language, iPartsExportPlugin.useFastSearchReindexMethod() ? defaultPartIndex.getProd().toLowerCase() : null);
             if (metaData != null) {
                 builder.append(metaData);
                 builder.append("\n");
                 builder.append(indexLineData);
                 builder.append("\n");
             }
         }
     }

     /**
      * Liefert die Metadaten Zeile für die übergebene Sprache
      *
      * @param language
      * @return
      */
     private String getMetaData(String language, String product) {
         String metaData = metaDataForLanguage.get(language);
         if (metaData == null) {
             ElasticSearchIndexMetaData meta = ElasticSearchIndexMetaData.getMetaForLanguage(language, isTestModeActive);
             if (meta == null) {
                 metaData = "";
             } else {
                 if (StrUtils.isValid(product)) {
                     meta.getIndex().set_index(meta.getIndex().get_index() + "_" + product);
                 }
                 metaData = genson.serialize(meta);
             }
             metaDataForLanguage.put(language, metaData);
         }
         if (metaData.isEmpty()) {
             return null;
         }
         return metaData;
     }

     /**
      * Legt das produktspezifische Verzeichnis für die übergebenen Parameter an, z.B. "root/zeistempel/de/MBAG/C01"
      *
      * @param rootDir
      * @param productNumber
      * @param companyName
      * @return
      */
     public boolean createProductDir(DWFile rootDir, String productNumber, String companyName) {
         for (String language : exportLanguages) {
             String path = getProductPath(companyName, productNumber, language, OsUtils.FILESEPARATOR);
             if (StrUtils.isEmpty(path) || (messageHelper.createSubDir(rootDir, path, "in product directory") == null)) {
                 messageHelper.logError("Could not create sub directory for \"" + companyName + "\", \"" + productNumber
                                        + "\" and \"" + language + "\"");
                 return false;
             }
         }
         return true;
     }

     /**
      * Erzeugt das produktspezifische Verzeichnis für die übergebenen Parameter, z.B. "root/zeistempel/de/MBAG/C01"
      *
      * @param companyName
      * @param productNumber
      * @param language
      * @param delimiter
      * @return
      */
     private String getProductPath(String companyName, String productNumber, String language, String delimiter) {
         if (StrUtils.isValid(companyName, productNumber, language)) {
             return language.toLowerCase() + delimiter + companyName + delimiter + productNumber;
         }
         if (messageHelper != null) {
             messageHelper.logError("Could not create product path: company name: " + companyName + "; product number: "
                                    + productNumber + "; language: " + language);
         }
         return "";
     }


     /**
      * Schreibt die JSON-Dateien für das Baumuster pro Sprache in eine Datei
      *
      * @param cleanModelId
      * @param productNumber
      * @param companyName
      * @param rootDir
      * @return
      */
     public void exportJSONToFile(String cleanModelId, String productNumber, String companyName, DWFile rootDir) {
         if (!StrUtils.isValid(cleanModelId, productNumber, companyName)) {
             return;
         }
         if ((rootDir == null) || !rootDir.isDirectory()) {
             return;
         }
         // Prefix für den Dateinamen erzeugen: BM_Produkt
         String fileName = cleanModelId + TEXT_DELIMITER_FOR_HASH + productNumber + "." + MimeTypes.EXTENSION_JSON;
         if (builderForLanguage != null) {
             for (Map.Entry<String, StringBuilder> langAndJSON : builderForLanguage.entrySet()) {
                 DWWriter writer = null;
                 DWFile jsonFile = null;
                 String language = langAndJSON.getKey();
                 String path = getProductPath(companyName, productNumber, language, OsUtils.FILESEPARATOR);
                 if (StrUtils.isValid(path)) {
                     DWFile productDir = rootDir.getChild(path);
                     // Dateiname BM_PRODUKT.json
                     jsonFile = productDir.getChild(fileName);
                     writer = jsonFile.getWriter(DWFileCoding.UTF8, false);
                 }
                 if ((writer == null) || StrUtils.isEmpty(path)) {
                     if (messageHelper != null) {
                         messageHelper.logError("Could not create file writer for language " + language + ": "
                                                + ((jsonFile == null) ? "invalid path!" : jsonFile.getAbsolutePath()));
                     }
                     Thread.currentThread().interrupt();
                 } else {
                     try {
                         writer.write(langAndJSON.getValue().toString());
                     } catch (IOException e) {
                         writeException(e);
                     } finally {
                         try {
                             writer.close();
                         } catch (Throwable e) {
                             writeException(e);
                             Thread.currentThread().interrupt();
                         }
                     }
                 }
             }
         }
     }

     /**
      * Überträgt die JSON-Dateien für das Baumuster pro Sprache an den S3 Bucket
      *
      * @param cleanModelId
      * @param productNumber
      * @param companyName
      * @param s3ObjectStoreDir
      * @param s3Helper
      * @return
      */
     public void exportJSONToS3Bucket(String cleanModelId, String productNumber, String companyName, String s3ObjectStoreDir,
                                      iPartsModelPartsListsFastSearchS3Helper s3Helper) {
         if (!StrUtils.isValid(cleanModelId, productNumber, companyName)) {
             return;
         }
         // Dateinamen erzeugen: BM_Produkt.json
         String fileName = cleanModelId + TEXT_DELIMITER_FOR_HASH + productNumber + "." + MimeTypes.EXTENSION_JSON;
         if (builderForLanguage != null) {
             for (Map.Entry<String, StringBuilder> langAndJSON : builderForLanguage.entrySet()) {
                 String language = langAndJSON.getKey();
                 String objectStoreRelativeDir = getProductPath(companyName, productNumber, language, "/");
                 if (StrUtils.isEmpty(objectStoreRelativeDir)) {
                     // Fehlermeldung ist bereits in getProductPath()
                     Thread.currentThread().interrupt();
                     return;
                 }

                 // Kompletter Pfad im S3 Bucket
                 String objectStoreDirAndFileName = s3ObjectStoreDir + "/" + objectStoreRelativeDir + "/" + fileName;

                 if (!s3Helper.uploadData(objectStoreDirAndFileName, langAndJSON.getValue().toString().getBytes(StandardCharsets.UTF_8))) {
                     messageHelper.logError("Upload of exported fast search index to S3 bucket for model \"" + cleanModelId + "\" failed.");
                     Thread.currentThread().interrupt();
                     return;
                 }
             }
         }
     }

     private void writeException(Throwable e) {
         if (messageHelper != null) {
             messageHelper.logExceptionWithoutThrowing(e);
         }
     }
 }
