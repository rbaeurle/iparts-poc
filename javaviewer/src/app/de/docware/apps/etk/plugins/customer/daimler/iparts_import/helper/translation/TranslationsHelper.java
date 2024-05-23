/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.translation;

import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOption;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

public class TranslationsHelper extends ImportExportLogHelper {

    public static DWFile getStyleSheetFile(boolean isTruckObjectStoreTranslations) {
        return getConfigFile(isTruckObjectStoreTranslations ? iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_STYLE_SHEET_FILE
                                                            : iPartsImportPlugin.CONFIG_TRANSIT_STYLE_SHEET_FILE, "xsl");
    }

    public static DWFile getSchemaFile(boolean isTruckObjectStoreTranslations) {
        return getConfigFile(isTruckObjectStoreTranslations ? iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_SCHEMA_FILE
                                                            : iPartsImportPlugin.CONFIG_TRANSIT_SCHEMA_FILE, "xsd");

    }

    private static DWFile getConfigFile(UniversalConfigOption configOption, String extension) {
        if (!iPartsImportPlugin.getPluginConfig().getConfigValueAsRawString(configOption, "").isEmpty()) {
            DWFile configFile = iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(configOption);
            if (configFile.isFile(DWFile.DEFAULT_FILE_EXISTS_TIMEOUT) && configFile.extractExtension(false).equals(extension)) {
                return configFile;
            }
        }
        return null;
    }

    /**
     * Liefert die maximale Anzahl von Einträgen pro XML Datei.
     *
     * @return
     */
    public static int getMaxEntriesPerFile(boolean isTruckObjectStoreTranslations) {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(isTruckObjectStoreTranslations ? iPartsImportPlugin.CONFIG_TRANSLATION_TRUCK_MAX_ENTRIES : iPartsImportPlugin.CONFIG_TRANSIT_MAX_ENTRIES);
    }

    protected static void writeMessageAndLog(String applicationLogFileMessage, EtkMessageLog messageLog, String translatedLogMessage) {
        Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "Translation process: " + applicationLogFileMessage);
        if ((messageLog != null) && StrUtils.isValid(translatedLogMessage)) {
            messageLog.fireMessage(translatedLogMessage, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
    }

    protected static void writeLogMessageError(EtkMessageLog messageLog, String translatedLogMessage) {
        writeLogMessage(messageLog, MessageLogType.tmlError, translatedLogMessage);
    }

    protected static void writeLogMessage(EtkMessageLog messageLog, String translatedLogMessage) {
        writeLogMessage(messageLog, MessageLogType.tmlMessage, translatedLogMessage);
    }

    protected static void writeLogMessage(EtkMessageLog messageLog, MessageLogType messageLogType, String translatedLogMessage) {
        if (messageLog == null) {
            return;
        }
        messageLog.fireMessage(translatedLogMessage, messageLogType,
                               MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
    }

}
