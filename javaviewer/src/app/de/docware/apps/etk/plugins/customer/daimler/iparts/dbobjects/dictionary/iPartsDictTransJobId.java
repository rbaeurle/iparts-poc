package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.framework.modules.config.common.Language;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Dictionary-Trans-Job-ID (Daten aus Tabelle DA_DICT_TRANS_JOB).
 */
public class iPartsDictTransJobId extends IdWithType {

    public static String TYPE = "DA_iPartsDictTransJobId";

    protected enum INDEX {TEXT_ID, SOURCE_LANG, DEST_LANG, JOB_ID}

    /**
     * Der normale Konstruktor
     *
     * @param textId
     * @param sourceLang
     * @param destLang
     * @param jobId
     */
    public iPartsDictTransJobId(String textId, String sourceLang, String destLang, String jobId) {
        super(TYPE, new String[]{ textId, sourceLang, destLang, jobId });
    }

    /**
     * Der normale Konstruktor
     *
     * @param textId
     * @param sourceLang
     * @param destLang
     * @param jobId
     */
    public iPartsDictTransJobId(String textId, Language sourceLang, Language destLang, String jobId) {
        this(textId, sourceLang.getCode(), destLang.getCode(), jobId);
    }

    /**
     * Der normale Konstruktor
     *
     * @param dictTransJobHistoryId
     */
    public iPartsDictTransJobId(iPartsDictTransJobHistoryId dictTransJobHistoryId) {
        this(dictTransJobHistoryId.getTextId(), dictTransJobHistoryId.getSourceLang(),
             dictTransJobHistoryId.getDestLang(), dictTransJobHistoryId.getJobId());
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDictTransJobId() {
        this("", "", "", "");
    }

    public String getTextId() {
        return id[INDEX.TEXT_ID.ordinal()];
    }

    public String getSourceLang() {
        return id[INDEX.SOURCE_LANG.ordinal()];
    }

    public String getDestLang() {
        return id[INDEX.DEST_LANG.ordinal()];
    }

    public String getJobId() {
        return id[INDEX.JOB_ID.ordinal()];
    }

}
