/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.HashMap;
import java.util.Map;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_DICT_TRANS_JOB_HISTORY.
 */
public class iPartsDataDictTransJobHistory extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DTJH_TEXTID, FIELD_DTJH_SOURCE_LANG,
                                                       FIELD_DTJH_DEST_LANG, FIELD_DTJH_JOBID, FIELD_DTJH_LAST_MODIFIED };
    private static Map<String, String> JOB_TO_HISTORY_MAPPING = new HashMap<String, String>();

    static {
        JOB_TO_HISTORY_MAPPING.put(FIELD_DTJ_TRANSLATION_DATE, FIELD_DTJH_TRANSLATION_DATE);
        JOB_TO_HISTORY_MAPPING.put(FIELD_DTJ_BUNDLE_NAME, FIELD_DTJH_BUNDLE_NAME);
        JOB_TO_HISTORY_MAPPING.put(FIELD_DTJ_TRANSLATION_STATE, FIELD_DTJH_TRANSLATION_STATE);
        JOB_TO_HISTORY_MAPPING.put(FIELD_DTJ_STATE_CHANGE, FIELD_DTJH_STATE_CHANGE);
        //String FIELD_DTJ_LAST_MODIFIED = "DTJ_LAST_MODIFIED";        // Datum letzte Änderung: Zeitstempel wird bei jeder Statusänderung aktualisiert
        JOB_TO_HISTORY_MAPPING.put(FIELD_DTJ_JOB_TYPE, FIELD_DTJH_JOB_TYPE);
        JOB_TO_HISTORY_MAPPING.put(FIELD_DTJ_TEXTKIND, FIELD_DTJH_TEXTKIND);
        JOB_TO_HISTORY_MAPPING.put(FIELD_DTJ_USER_ID, FIELD_DTJH_USER_ID);
        JOB_TO_HISTORY_MAPPING.put(FIELD_DTJ_ERROR_CODE, FIELD_DTJH_ERROR_CODE);           // Error-Code
    }

    /**
     * normaler Konstruktor
     *
     * @param project
     * @param id
     */
    public iPartsDataDictTransJobHistory(EtkProject project, iPartsDictTransJobHistoryId id) {
        super(KEYS);
        tableName = TABLE_DA_DICT_TRANS_JOB_HISTORY;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDictTransJobHistoryId createId(String... idValues) {
        return new iPartsDictTransJobHistoryId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4]);
    }

    @Override
    public iPartsDictTransJobHistoryId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsDictTransJobHistoryId)id;
    }

    /**
     * erzeugt aus geladenen {@link iPartsDataDictTransJob} ein neues {@link iPartsDataDictTransJobHistory}
     *
     * @param project
     * @param dataDictTransJob
     * @return
     */
    public static iPartsDataDictTransJobHistory createHistoryDataFromTransJob(EtkProject project, iPartsDataDictTransJob dataDictTransJob) {
        String lastModified = dataDictTransJob.getLastModified();
        iPartsDataDictTransJobHistory result = new iPartsDataDictTransJobHistory(project, new iPartsDictTransJobHistoryId(dataDictTransJob.getAsId(), lastModified));
        result.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        for (Map.Entry<String, String> sourceToDestFieldMapping : JOB_TO_HISTORY_MAPPING.entrySet()) {
            String destFieldValue = dataDictTransJob.getFieldValue(sourceToDestFieldMapping.getKey());
            result.setFieldValue(sourceToDestFieldMapping.getValue(), destFieldValue, DBActionOrigin.FROM_EDIT);
        }
        return result;
    }
}
