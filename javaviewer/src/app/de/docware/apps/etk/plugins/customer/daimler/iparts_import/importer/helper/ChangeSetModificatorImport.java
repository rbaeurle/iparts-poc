package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper;

import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ChangeSetModificator;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogChannels;

/**
 * Hilfsklasse zum Verändern von ChangeSets (speziell von nicht freigegebenen Autoren-Aufträgen) in Importern.
 */
public class ChangeSetModificatorImport extends ChangeSetModificator {

    private final AbstractDataImporter importer;
    private final boolean isCancellable;

    public ChangeSetModificatorImport(LogChannels logChannel, AbstractDataImporter importer, boolean isCancellable) {
        super(logChannel);
        this.importer = importer;
        this.isCancellable = isCancellable;
    }

    @Override
    public void logMessage(String message, String... placeHolderTexts) {
        importer.getMessageLog().fireMessage(importer.translateForLog(message, placeHolderTexts),
                                             MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
    }

    @Override
    public void onProgressChanged(int progressCounter, int maxProgress) {
        importer.getMessageLog().fireProgress(progressCounter, maxProgress, "", true, false);
    }

    @Override
    public void onCriticalError(String message, String... placeHolderTexts) {
        importer.cancelImport(importer.translateForLog(message, placeHolderTexts));
    }

    @Override
    public boolean isCancelled() {
        return isCancellable && (importer.isCancelled() || importer.cancelImportIfInterrupted());
    }
}
