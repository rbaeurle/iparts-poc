/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.singlepicparts;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.mediaservice.partnumbers.MediaServicePartNumbersService;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;

import java.util.Collection;
import java.util.TreeSet;

/**
 * Scheduler für den automatischen Import von Einzelteilbilder-Infos.
 */
public class SinglePicPartsImportScheduler extends AbstractDayOfWeekHandler {

    public SinglePicPartsImportScheduler(EtkProject project, Session session) {
        super(project, session, iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, "single picture for part import");
    }

    @Override
    protected void executeLogic() {
        String webserviceUri = iPartsPlugin.getWebservicesSinglePicPartsBaseURI();
        if (webserviceUri.isEmpty()) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, LogType.ERROR, "No URI configured for Web service of single picture for part import");
            return;
        }

        // Webservice aufrufen für die Teilenummern mit Einzelteilbildern
        if (!webserviceUri.endsWith('/' + MediaServicePartNumbersService.WEBSERVICE_NAME)) {
            webserviceUri = StrUtils.addCharacterIfLastCharacterIsNot(webserviceUri, '/');
            webserviceUri += MediaServicePartNumbersService.WEBSERVICE_NAME;
        }
        long startTime = System.currentTimeMillis();
        Logger.log(iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, LogType.DEBUG, "Starting single picture for part import by calling Web service \""
                                                                             + webserviceUri + "\"...");
        Collection<String> partNumbers;
        try {
            partNumbers = MediaServicePartNumbersService.getPartNumbersFromMediaService(getProject());
            if (partNumbers == null) {
                throw new RuntimeException("No result from Web service \"" + webserviceUri + "\"");
            }
            partNumbers = new TreeSet<>(partNumbers); // Sortieren für schönere Logausgaben
        } catch (Exception e) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, LogType.DEBUG, "Error while executing the Web service of single picture for part import");
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, LogType.ERROR, e);
            return;
        }
        String durationString = DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, false, false,
                                                                   Language.EN.getCode());
        Logger.log(iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, LogType.DEBUG, "Web service of single picture for part import returned "
                                                                             + partNumbers.size() + " part numbers in "
                                                                             + durationString);

        // Import durchführen
        Logger.log(iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, LogType.DEBUG, "Executing single picture for part import...");
        SinglePicPartsImporter singlePicPartsImporter = new SinglePicPartsImporter(getProject());
        singlePicPartsImporter.doImport(partNumbers);
        durationString = DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, false, false,
                                                            Language.EN.getCode());
        if (singlePicPartsImporter.isCancelled()) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, LogType.DEBUG, "Cancelled single picture for part import after "
                                                                                 + durationString);
        } else if (singlePicPartsImporter.getErrorCount() == 0) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, LogType.DEBUG, "Finished single picture for part import in "
                                                                                 + durationString);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, LogType.ERROR, "Error while executing single picture for part import after "
                                                                                 + durationString);
        }
    }
}
