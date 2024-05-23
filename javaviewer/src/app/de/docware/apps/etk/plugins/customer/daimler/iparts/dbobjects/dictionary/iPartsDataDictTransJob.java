/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.sql.SQLStringConvert;

import java.util.Calendar;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_DICT_TRANS_JOB.
 */
public class iPartsDataDictTransJob extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DTJ_TEXTID, FIELD_DTJ_SOURCE_LANG,
                                                       FIELD_DTJ_DEST_LANG, FIELD_DTJ_JOBID };

    /**
     * normaler Konstruktor
     *
     * @param project
     * @param id
     */
    public iPartsDataDictTransJob(EtkProject project, iPartsDictTransJobId id) {
        super(KEYS);
        tableName = TABLE_DA_DICT_TRANS_JOB;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDictTransJobId createId(String... idValues) {
        return new iPartsDictTransJobId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsDictTransJobId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsDictTransJobId)id;
    }

    public String getLastModified() {
        return getFieldValue(FIELD_DTJ_LAST_MODIFIED);
    }

    public iPartsDictTransJobStates getTranslationState() {
        return iPartsDictTransJobStates.getValue(getFieldValue(FIELD_DTJ_TRANSLATION_STATE));
    }

    public boolean hasSameLanguages(Language sourceLang, Language targetLang) {
        iPartsDictTransJobId id = getAsId();
        // Abfrage umgedreht, da sourveLang immer DE
        return id.getDestLang().equals(targetLang.getCode()) && id.getSourceLang().equals(sourceLang.getCode());
    }

    public void updateTranslationJob(iPartsDictTransJobStates translationState) {
        updateTranslationJob(translationState, null);
    }

    public void updateTranslationJob(iPartsDictTransJobStates translationState, String errorMessage) {
        iPartsDataDictTransJobHistory historyJob = iPartsDataDictTransJobHistory.createHistoryDataFromTransJob(getEtkProject(), this);
        historyJob.saveToDB();
        setFieldValue(FIELD_DTJ_TRANSLATION_STATE, translationState.getDbValue(), DBActionOrigin.FROM_EDIT);
        setFieldValue(FIELD_DTJ_LAST_MODIFIED, SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance()), DBActionOrigin.FROM_EDIT);
        if (StrUtils.isValid(errorMessage)) {
            setFieldValue(FIELD_DTJ_ERROR_CODE, errorMessage, DBActionOrigin.FROM_EDIT);
        } else {
            setFieldValue(FIELD_DTJ_ERROR_CODE, " ", DBActionOrigin.FROM_EDIT);
        }
    }
}
