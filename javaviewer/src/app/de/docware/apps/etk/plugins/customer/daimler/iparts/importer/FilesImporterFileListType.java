/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.importer;

import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

/**
 * Datenklasse für den Typ/Attribute einer Dateiliste, die vom {@link FilesImporterInterface} für Imports verwendet wird.
 */
public class FilesImporterFileListType {

    private String fileListType;
    private String fileListName;
    private boolean serverMode;
    private boolean removeExistingDataDefaultValue;
    private boolean removeExistingDataSelectable;
    private String[] validFileExtensions;
    private boolean mustContainValue;

    /**
     * Erzeugt einen neuen Import-Dateilisten-Typ.
     *
     * @param fileListType                   (Interner) Typname dieser Import-Dateiliste
     * @param fileListName                   Name dieser Import-Dateiliste
     * @param serverMode                     Steuerflag, ob die Datei hochgeladen wird, oder separat hochgeladen wurde
     *                                       und für den Import direkt auf dem Server liegt (für große Dateien).
     * @param removeExistingDataDefaultValue Standardwert für das Löschen aller vorhandenen Daten vor dem Import
     * @param removeExistingDataSelectable   Flag, ob das Löschen aller vorhandenen Daten vor dem Import erlaubt sein soll
     * @param validFileExtensions            Alle gültigen Dateiendungen für diese Import-Dateiliste;
     *                                       bei {@code null} sind alle Dateiendungen zulässig
     */
    public FilesImporterFileListType(String fileListType, String fileListName, boolean serverMode, boolean removeExistingDataDefaultValue,
                                     boolean removeExistingDataSelectable, String[] validFileExtensions) {
        // Dieser ältere Konstruktor erzwingt die Auswahl einer Datei über den letzten Parameter:
        this(fileListType, fileListName, serverMode, removeExistingDataDefaultValue, removeExistingDataSelectable, validFileExtensions, true);
    }

    /**
     * Erzeugt einen neuen Import-Dateilisten-Typ.
     *
     * @param fileListType                   (Interner) Typname dieser Import-Dateiliste
     * @param fileListName                   Name dieser Import-Dateiliste
     * @param removeExistingDataDefaultValue Standardwert für das Löschen aller vorhandenen Daten vor dem Import
     * @param removeExistingDataSelectable   Flag, ob das Löschen aller vorhandenen Daten vor dem Import erlaubt sein soll
     * @param validFileExtensions            Alle gültigen Dateiendungen für diese Import-Dateiliste;
     *                                       bei {@code null} sind alle Dateiendungen zulässig
     * @param mustContainValue               Es gibt FileImport Dialoge, bei denen nicht alle Dateien zum Import ausgewählt sein müssen.
     *                                       Die, die nicht zwingend erforderlich sind, können als [mustContainValue=false] markiert werden.
     */
    public FilesImporterFileListType(String fileListType, String fileListName, boolean serverMode, boolean removeExistingDataDefaultValue,
                                     boolean removeExistingDataSelectable, String[] validFileExtensions, boolean mustContainValue) {
        this.fileListType = fileListType;
        this.fileListName = fileListName;
        this.serverMode = serverMode;
        this.removeExistingDataDefaultValue = removeExistingDataDefaultValue;
        this.removeExistingDataSelectable = removeExistingDataSelectable;
        this.validFileExtensions = validFileExtensions;
        this.mustContainValue = mustContainValue;
    }

    /**
     * Liefert den (internen) Typnamen dieser Import-Dateiliste zurück.
     *
     * @return
     */
    public String getFileListType() {
        return fileListType;
    }

    /**
     * Liefert den übersetzten Namen dieser Import-Dateiliste zurück.
     *
     * @param language
     * @return
     */
    public String getFileListName(String language) {
        return TranslationHandler.translateForLanguage(fileListName, language);
    }

    /**
     * Liefert die Information, ob die Datei erst hochgeladen wird, oder über ein Fileshare auf dem Server gelesen werden kann.
     *
     * @return
     */
    public boolean isServerMode() {
        return serverMode;
    }

    /**
     * Liefert den Standardwert für das Löschen aller vorhandenen Daten vor dem Import.
     *
     * @return
     */
    public boolean isRemoveExistingDataDefaultValue() {
        return removeExistingDataDefaultValue;
    }

    /**
     * Flag, ob das Löschen aller vorhandenen Daten vor dem Import erlaubt sein soll.
     *
     * @return
     */
    public boolean isRemoveExistingDataSelectable() {
        return removeExistingDataSelectable;
    }

    /**
     * Liefert alle gültigen Dateiendungen für diese Import-Dateiliste zurück.
     *
     * @return Bei {@code null} sind alle Dateiendungen zulässig.
     */
    public String[] getValidFileExtensions() {
        return validFileExtensions;
    }

    /**
     * Liefert die Information, ob für diesen File-Importer zwingend eine Datei ausgewählt sein muss.
     *
     * @return
     */
    public boolean getMustContainValue() {
        return mustContainValue;
    }
}
