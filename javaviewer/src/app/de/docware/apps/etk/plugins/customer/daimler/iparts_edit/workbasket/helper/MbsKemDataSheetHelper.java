package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.io.File;

/**
 * Helper für die Verwaltung und das Holen eines MBS-KEM-Blattes
 */
public class MbsKemDataSheetHelper {

    public static final String KEM_SHEET_YEAR_PREFIX = "20";
    public static final String KEM_SHEET_FILE_SEPARATOR = "_";
    public static final String KEM_SHEET_FILE_EXTENSION = ".xlsx";
    public static final String KEM_SHEET_FILE_DESCRIPTION = "!!KEM-Blatt (*.xlsx)";

    public static boolean isKemDataSheetEnabled() {
        return iPartsEditPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsEditPlugin.CONFIG_KEM_DATA_SHEET);
    }

    public static DWFile getKemDataSheetRootDir() {
        return iPartsEditPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsEditPlugin.CONFIG_KEM_DATA_SHEET_CONTENT_DIR);
    }

    public static boolean isKemDataSheetDirValid(boolean withMsg) {
        DWFile saveDir = getKemDataSheetRootDir();
        if (!saveDir.isDirectory()) {
            if (withMsg) {
                MessageDialog.showError(TranslationHandler.translate("!!Das Verzeichnis existiert nicht:") + " " + saveDir.getPath());
            }
            return false;
        }
        return true;
    }

    private String kemNo;
    private boolean isInit;
    private String kemArt;
    private String kemYear;
    private String kemNumber;
    private String kemSupplement;
    private int kemNumberLength;

    public MbsKemDataSheetHelper(String kemNo, int kemNumberLength) {
        this.kemNo = kemNo;
        this.kemArt = "";
        this.kemYear = "";
        this.kemNumber = "";
        this.kemSupplement = "";
        this.kemNumberLength = kemNumberLength;
        this.isInit = checkKemNo();
    }

    public MbsKemDataSheetHelper(String kemNo) {
        this(kemNo, 3);
    }

    public boolean isInit() {
        return isInit;
    }

    public String getKemFileName() {
        if (isInit()) {
//             ..\Änderungsaufträge 20<Jahr>\<KEM-Art>_<Jahr>\<lfd_Nr>_<Jahr><Nachtrag>.xlsx
            String kemYearPrefix = iPartsEditPlugin.getPluginConfig().getConfigValueAsString(iPartsEditPlugin.CONFIG_KEM_DATA_SHEET_YEAR_PREFIX);
            StringBuilder str = new StringBuilder();
            str.append(kemYearPrefix);
            str.append(kemYear);
            str.append(File.separatorChar);
            str.append(kemArt);
            str.append(KEM_SHEET_FILE_SEPARATOR);
            str.append(kemYear);
            str.append(File.separatorChar);
            str.append(kemNumber);
            str.append(KEM_SHEET_FILE_SEPARATOR);
            str.append(kemYear);
            str.append(kemSupplement);
            str.append(KEM_SHEET_FILE_EXTENSION);
            return str.toString();
        }
        return "";
    }

    private String getKemArt() {
        return kemArt;
    }

    private String getKemYear() {
        return kemYear;
    }

    private String getKemNumber() {
        return kemNumber;
    }

    private String getKemSupplement() {
        return kemSupplement;
    }

    private boolean checkKemNo() {
/*
Dabei ist die KEM-Nummer zu zerlegen:

    KEM Art: Stellen 1-3

    Wenn 3. letztes Zeichen =N dann

    Jahr (2-stellig): 5. und 4. letzte Ziffer
    Nachtrag (3-stellig): letzten drei Stellen
    laufende Nummer: ab der 4. Stelle bis die 5. letzte Stelle beginnt

    Wenn 3. letztes Zeichen <>N

    Jahr (2-stellig): vorletzte und letzte Ziffer
    laufende Nummer: ab der 4. Stelle bis die vorletzte Stelle beginnt

Bei der laufenden Nummer sind in den MBS-Stücklistendaten keine "führenden 0" enthalten. Allerdings bei den Files auf dem Share gibt es führende "0".
Daher müssen wir das File entweder mit Wildcard suchen oder "führende 0" beim Zugriff auf das Laufwerk links hinzufügen bis es 3 Stellen sind.

             ..\Änderungsaufträge 20<Jahr>\<KEM-Art>_<Jahr>\<lfd_Nr>_<Jahr><Nachtrag>.xlsx
UAN220N01:   ..\Änderungsaufträge 2020\UAN_20\002_20N01.xlsx oder ..\Änderungsaufträge 2020\UAN_20\*2_20N01.xlsx
UAN320:      ..\Änderungsaufträge 2020\UAN_20\003_20.xlsx oder ..\Änderungsaufträge 2020\UAN_20\*3_20.xlsx
UFS12111N02: ..\Änderungsaufträge 2011\UFS_11\121_11N02.xlsx oder ..\Änderungsaufträge 2011\UFS_11\*121_11N02.xlsx
 */
        boolean result = StrUtils.isValid(kemNo);
        if (result) {
            result = kemNo.length() >= 6;
        }
        if (result) {
            kemArt = StrUtils.copySubString(kemNo, 0, 3);
            if (StrUtils.isValid(kemArt)) {
                String workStr = StrUtils.copySubString(kemNo, 3, kemNo.length());
                int pos = -1;
                for (int lfdNr = 0; lfdNr < workStr.length(); lfdNr++) {
                    if (!Character.isDigit(workStr.charAt(lfdNr))) {
                        break;
                    }
                    pos = lfdNr;
                }
                if (pos >= 0) {
                    kemYear = StrUtils.copySubString(workStr, pos - 1, 2);
                    kemNumber = StrUtils.copySubString(workStr, 0, pos - 1);
                    if (StrUtils.isValid(kemNumber)) {
                        kemNumber = StrUtils.leftFill(kemNumber, kemNumberLength, '0');
                    }
                    kemSupplement = StrUtils.copySubString(workStr, pos + 1, workStr.length());
                }
            }
            result = StrUtils.isValid(kemArt, kemYear, kemNumber);
        }
        return result;
    }

    public String getSAPFormat() {
        if (isInit()) {
            StringBuilder str = new StringBuilder();
            str.append(kemArt);
            str.append(kemNumber);
            str.append(kemYear);
            if (kemSupplement.length() > 2) {
                str.append(kemSupplement.substring(1));
            }
            return str.toString();
        }
        return "";
    }
}

