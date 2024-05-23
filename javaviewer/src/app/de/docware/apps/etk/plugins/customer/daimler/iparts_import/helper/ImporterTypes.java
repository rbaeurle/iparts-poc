/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWTarGzHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Enums für die verschiedenen Import-Dateien
 */
public enum ImporterTypes {

    TAL40A("TAL40A"),
    TAL46A("TAL46A"),
    TAL83A("TAL83A"),
    TAL95M("TAL95M"),
    TAL31A("TAL31A"),
    TAL30A("TAL30A"),
    TAL47S("TAL47S"),
    RSK("SRM2ALLG"),
    DASTI("DASTI"),
    KI_MAPPING("Ergebnis_KI"),
    EPEP("ePEP_GSP_KEM_FIN_ANTWORT"),
    MBS_MASTER("IPARTS_MBS_STAMMDATEN"),
    MBS_STRUCT("IPARTS_MBS_STRUKTURDATEN"),
    CEMAT_MAPPING("PartMappings_versioned"),
    TOP_TUS("TOP_TUs"),
    CONNECT_WIRE_HARNESS("Connect_WH"),
    SNR2SUPPSNR_SRM("SRM2IPARTS"), // Import Sachnummer zu Lieferantennummer aus SRM
    UNKNOWN("");

    private String fileNamePrefix;

    ImporterTypes(String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public static String[] getAllEnumNames() {
        String[] enumNames = new String[values().length - 1];
        int index = 0;
        for (ImporterTypes iType : values()) {
            if (iType != UNKNOWN) {
                enumNames[index] = iType.name();
                index++;
            }
        }
        return enumNames;
    }

    public String getFileDate(String fileName) {
        if (fileName.startsWith(fileNamePrefix)) {
            return StrUtils.copySubString(fileName, fileNamePrefix.length(), fileName.length());
        }
        return "";
    }

    public String getFileDate(DWFile dwFile) {
        return getFileDate(dwFile.extractFileName(false));
    }

    public static ImporterTypes getImportType(String fileName) {
        for (ImporterTypes iType : values()) {
            if (fileName.startsWith(iType.fileNamePrefix)) {
                return iType;
            }
        }
        return UNKNOWN;
    }

    public static ImporterTypes getImportType(DWFile dwFile) {
        return getImportType(dwFile.extractFileName(false));
    }

    /**
     * Sonderbehandlung für TAL30, 31 und 95M: Datei wird entpackt und neu verpackt
     *
     * @param importType
     * @param importFile
     * @param destUnpackDir
     * @return
     */
    public static List<DWFile> splitImportFile(ImporterTypes importType, DWFile importFile, DWFile destUnpackDir) {
        List<DWFile> result = new ArrayList<>();
        switch (importType) {
            case TAL30A:
            case TAL95M:
            case TAL47S:
                try {
                    result = DWTarGzHelper.unpackAllTarEntries(importFile, destUnpackDir, true);
                } catch (IOException e) {
                    Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.ERROR, e);
                }
                break;
            case DASTI:
                try {
                    result = DWTarGzHelper.unpackAllTarEntries(importFile, destUnpackDir, false);
                } catch (IOException e) {
                    Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.ERROR, e);
                }
                break;
            case UNKNOWN:
                break;
            default:
                result.add(importFile);
        }
        return result;
    }

    /**
     * Überprüft, ob der übergeben Importtyp ein Sonderfall der Importer ist. Sonderfälle sind aktuell XML Importer
     * die Schema und XML Dateien gleichzeitig enthalten.
     *
     * @param importType
     * @return
     */
    public static boolean isImporterWithDifferentFiles(ImporterTypes importType) {
        switch (importType) {
            case TAL47S:
            case TAL95M:
                return true;
        }
        return false;
    }
}
