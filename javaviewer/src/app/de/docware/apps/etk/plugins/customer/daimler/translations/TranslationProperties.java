package de.docware.apps.etk.plugins.customer.daimler.translations;

import de.docware.util.StrUtils;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class TranslationProperties {

    /**
     * Reads all generated ProGuard dump files and writes a properties file for each plugin containing all strings to be translated.
     */
    public static void main(String[] args) {
        Path projectRoot = Paths.get("").toAbsolutePath();
        System.out.println("Project root: " + projectRoot);
        Path dumpDir = Paths.get(projectRoot + "/build/translations/proguard/");
        System.out.println("Dump dir: " + dumpDir);
        Path propertiesDir = Paths.get(projectRoot + "/build/translations/properties/");
        System.out.println("Properties dir: " + propertiesDir);
        if(!Files.isDirectory(propertiesDir)) {
            System.out.println("Missing properties dir. Creating properties directory.");
            try {
                Path newPropertiesDir = Files.createDirectory(propertiesDir);
                System.out.println("Created properties dir: " + newPropertiesDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dumpDir, "*.{dump_out}")) {
            for (Path dumpFile: stream) {
                String pluginName = dumpFile.getFileName()
                        .toString()
                        .replace(".dump_out", "")
                        .replace("ProGuardDump", "")
                        .replace("Plugin", "")
                        .toLowerCase()
                        .trim();
                System.out.println("Calculating plugin: " + pluginName);
                Properties translationProperties = new Properties();

                try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(dumpFile)))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.replace("\u0001", "");  // Steuerzeichen SOH entfernen
                        if (StrUtils.stringContains(line, "- String [!!", false) && (!StrUtils.stringContains(line, "]", false))) {
                            line = StrUtils.stringAfterCharacter(line, "- String [!!");
                            throw new RuntimeException("Zeilenumbrüche in Übersetzungstexten sind nicht erlaubt. Text: '" + line + "'");
                        }
                        if (StrUtils.stringContains(line, "- String [!!", false) && StrUtils.stringContains(line, "]", false)) {
                            line = StrUtils.stringAfterCharacter(line, "- String [!!");
                            line = StrUtils.stringUpToLastCharacter(line, "]");
                            validateAndPersistTranslation(line, translationProperties);
                        }
                        if (StrUtils.stringContains(line, "- Utf8 [!!", false) && StrUtils.stringContains(line, "]", false)) {
                            line = StrUtils.stringAfterCharacter(line, "- Utf8 [!!");
                            line = StrUtils.stringUpToLastCharacter(line, "]");
                            validateAndPersistTranslation(line, translationProperties);
                        }
                    }

                    String propertiesFile = "";
                    if(pluginName.equals("main")) {
                        propertiesFile = propertiesDir.toAbsolutePath() + "/docware_plugin_javaviewer_daimler_iparts_translations_de.properties";
                    } else {
                        propertiesFile = propertiesDir.toAbsolutePath() + "/docware_plugin_javaviewer_daimler_iparts_" + pluginName + "_translations_de.properties";
                    }
                    System.out.println("Writing translation properties to: " + propertiesFile);
                    translationProperties.store(new FileWriter(propertiesFile), "");

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Überprüft den String auf Gültigkeit und speichert eineindeutige Übersetzungen in einem Properties-Objekt ab
     *
     * @param line
     * @param translationProperties
     */
    private static void validateAndPersistTranslation(String line, Properties translationProperties) {
        // Leere und bereits vorhandene Strings ignorieren
        if ((!line.isEmpty()) && (!translationProperties.containsKey(line))) {
            if (!line.trim().equals(line)) {
                throw new RuntimeException("Vorne und hinten darf bei der Übersetzung kein Leerzeichen sein, Text: '" + line + "'");
            }
            translationProperties.put(line, line);
        }
    }
}
