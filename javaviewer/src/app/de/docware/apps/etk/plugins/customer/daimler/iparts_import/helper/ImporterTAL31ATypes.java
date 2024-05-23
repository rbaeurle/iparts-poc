/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper;

import de.docware.util.file.DWFile;

/**
 * Enums für die einzelnen Dateien in der TAL31A-Datei (Stammdaten)
 */
public enum ImporterTAL31ATypes {

    BM_STAMM("bmstamm"),
    EVO_BAUKASTEN("evo_baukasten"),
    TEXTART("textart"),
    KURZTEXTE("kurztexte"),
    LANG_TEXTE("langtexte"),
    FGST_AGGR("fgst_aggr"),
    SAA_STAMM("saastamm"),
    VARIANTEN("varianten"),
    TEILESTAMM("teilestamm"),
    E_TEXTE("etxte_ref"),
    KGTU_TEXTE("kgtu_texte"),
    TEXT_FUSSNOTEN("text_fussnoten"),
    APPLICATION_LIST("kat_steuer_info"),
    FEDERMAPPING("federmappings"),
    UNKNOWN("");

    private String fileName;

    ImporterTAL31ATypes(String fileName) {
        this.fileName = fileName;
    }

    public static ImporterTAL31ATypes getImportType(String fileName) {
        for (ImporterTAL31ATypes iType : values()) {
            if (fileName.equalsIgnoreCase(iType.fileName)) {
                return iType;
            }
        }
        return UNKNOWN;
    }

    public static ImporterTAL31ATypes getImportType(DWFile dwFile) {
        String fileName = dwFile.extractFileName(false);
        while (fileName.contains(".")) {
            fileName = DWFile.get(fileName).extractFileName(false);
        }
        return getImportType(fileName);
    }

    public static ImporterTAL31ATypes[] getFileImportOrder() {
        return new ImporterTAL31ATypes[]{ TEXT_FUSSNOTEN, TEXTART, KURZTEXTE, LANG_TEXTE, E_TEXTE, TEILESTAMM, VARIANTEN,
                                          SAA_STAMM, EVO_BAUKASTEN, KGTU_TEXTE, BM_STAMM, FGST_AGGR, APPLICATION_LIST, FEDERMAPPING };
    }

    public String getFileName() {
        return fileName;
    }
}
