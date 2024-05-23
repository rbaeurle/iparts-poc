package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.framework.modules.config.common.Language;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;

import java.util.Calendar;

/**
 * Repräsentiert eine Dictionary-Trans-Job-History-ID (Daten aus Tabelle DA_DICT_TRANS_JOB_HISTORY).
 */
public class iPartsDictTransJobHistoryId extends IdWithType {

    public static String TYPE = "DA_iPartsDictTransJobHistoryId";

    protected enum INDEX {TEXT_ID, SOURCE_LANG, DEST_LANG, JOB_ID, LAST_MODIFIED}

    /**
     * Der normale Konstruktor
     *
     * @param textId
     * @param sourceLang
     * @param destLang
     * @param jobId
     * @param lastModfified
     */
    public iPartsDictTransJobHistoryId(String textId, String sourceLang, String destLang, String jobId, String lastModfified) {
        super(TYPE, new String[]{ textId, sourceLang, destLang, jobId, lastModfified });
    }

    /**
     * Der normale Konstruktor
     *
     * @param textId
     * @param sourceLang
     * @param destLang
     * @param jobId
     * @param lastModfified
     */
    public iPartsDictTransJobHistoryId(String textId, Language sourceLang, Language destLang, String jobId, String lastModfified) {
        this(textId, sourceLang.getCode(), destLang.getCode(), jobId, lastModfified);
    }

    /**
     * Konstruktor aus {@link iPartsDictTransJobId} und lastModified
     *
     * @param dictTransJobId
     * @param lastModfified
     */
    public iPartsDictTransJobHistoryId(iPartsDictTransJobId dictTransJobId, String lastModfified) {
        this(dictTransJobId.getTextId(), dictTransJobId.getSourceLang(), dictTransJobId.getDestLang(),
             dictTransJobId.getJobId(), lastModfified);
    }

    /**
     * Konstruktor aus {@link iPartsDictTransJobId} und selbst berechnetem lastModified
     *
     * @param dictTransJobId
     */
    public iPartsDictTransJobHistoryId(iPartsDictTransJobId dictTransJobId) {
        this(dictTransJobId, SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance()));
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDictTransJobHistoryId() {
        this("", "", "", "", "");
    }

    public String getTextId() {
        return id[iPartsDictTransJobHistoryId.INDEX.TEXT_ID.ordinal()];
    }

    public String getSourceLang() {
        return id[iPartsDictTransJobHistoryId.INDEX.SOURCE_LANG.ordinal()];
    }

    public String getDestLang() {
        return id[iPartsDictTransJobHistoryId.INDEX.DEST_LANG.ordinal()];
    }

    public String getJobId() {
        return id[iPartsDictTransJobHistoryId.INDEX.JOB_ID.ordinal()];
    }

    public String getLastModified() {
        return id[iPartsDictTransJobHistoryId.INDEX.LAST_MODIFIED.ordinal()];
    }

}
