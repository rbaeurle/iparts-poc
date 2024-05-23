/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.versioninfo;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsPublishingHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.date.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;

/**
 * Response Data Transfer Object für den VersionInfo-Webservice
 *
 * Beispiel-Response (kein Request-Objekt nötig)
 *
 * {
 * "contentLanguages": [
 * "de",
 * "en",
 * "fr"
 * ],
 * "lastPublishedDate": "2015-11-12T12:17:30+0100",
 * "version": "Development"
 * }
 */
public class iPartsWSVersionInfoResponse implements RESTfulTransferObjectInterface {

    private String version;
    private String lastPublishedDate;
    private List<String> contentLanguages;

    public iPartsWSVersionInfoResponse(EtkProject project) {
        this(project, iPartsPlugin.getPublishingHelper());
    }

    public iPartsWSVersionInfoResponse(EtkProject project, iPartsPublishingHelper publishingHelper) {
        init(project, publishingHelper);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLastPublishedDate() {
        return lastPublishedDate;
    }

    public void setLastPublishedDate(String lastPublishedDate) {
        this.lastPublishedDate = lastPublishedDate;
    }

    public List<String> getContentLanguages() {
        return contentLanguages;
    }

    public void setContentLanguages(List<String> contentLanguages) {
        this.contentLanguages = contentLanguages;
    }

    private void init(EtkProject project, iPartsPublishingHelper publishingHelper) {
        this.version = Constants.APP_VERSION;

        // Letztes Publikations-Datum bestimmen
        Date date = null;
        if (publishingHelper != null) {
            try {
                SimpleDateFormat publishingDateFormat = new SimpleDateFormat(DateUtils.simpleDateFormatyyyyMMdd + DateUtils.simpleTimeFormatHHmmss);
                publishingDateFormat.setLenient(false);
                date = publishingDateFormat.parse(publishingHelper.getLastPublishingDate());
            } catch (ParseException e) {
                date = null;
            }
        }

        // Fallback auf aktuelles Datum + Uhrzeit
        if (date == null) {
            date = GregorianCalendar.getInstance().getTime();
        }

        SimpleDateFormat formatter = new SimpleDateFormat(DateUtils.simpleDateTimeZoneFormatIso);
        this.lastPublishedDate = formatter.format(date);

        List<String> languageCodes = project.getConfig().getDatabaseLanguages();
        toLowerCaseList(languageCodes);
        this.contentLanguages = languageCodes;
    }

    private void toLowerCaseList(List<String> values) {
        ListIterator<String> iter = values.listIterator();
        while (iter.hasNext()) {
            iter.set(iter.next().toLowerCase());
        }
    }

}
