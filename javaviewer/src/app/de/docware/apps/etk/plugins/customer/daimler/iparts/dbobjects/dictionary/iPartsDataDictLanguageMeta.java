/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.Calendar;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_DICT_SPRACHE.
 */
public class iPartsDataDictLanguageMeta extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_DICT_SPRACHE_TEXTID, FIELD_DA_DICT_SPRACHE_SPRACH };

    public iPartsDataDictLanguageMeta(EtkProject project, iPartsDictLanguageMetaId id) {
        super(KEYS);
        tableName = TABLE_DA_DICT_SPRACHE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDictLanguageMetaId createId(String... idValues) {
        return new iPartsDictLanguageMetaId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsDictLanguageMetaId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsDictLanguageMetaId)id;
    }

    // Convenience Method
    public String getState() {
        return getFieldValue(FIELD_DA_DICT_SPRACHE_STATUS);
    }

    public void setState(String state, DBActionOrigin origin) {
        setFieldValue(FIELD_DA_DICT_SPRACHE_STATUS, state, origin);
    }

    public String getCreationDate() {
        return getFieldValue(FIELD_DA_DICT_SPRACHE_CREATE);
    }

    public void setActCreationDate(DBActionOrigin origin) {
        setFieldValueAsDateTime(FIELD_DA_DICT_SPRACHE_CREATE, Calendar.getInstance(), origin);
    }

    public String getChangeDate() {
        return getFieldValue(FIELD_DA_DICT_SPRACHE_CHANGE);
    }

    public void setActChangeDate(DBActionOrigin origin) {
        setFieldValueAsDateTime(FIELD_DA_DICT_SPRACHE_CHANGE, Calendar.getInstance(), origin);
    }

    public iPartsDictSpracheTransState getTranslationState() {
        return iPartsDictSpracheTransState.getValue(getFieldValue(FIELD_DA_DICT_SPRACHE_TRANS_STATE));
    }

    public void setTranslationState(iPartsDictSpracheTransState translationState, DBActionOrigin origin) {
        setFieldValue(FIELD_DA_DICT_SPRACHE_TRANS_STATE, translationState.getDbValue(), origin);
    }

    @Override
    public iPartsDataDictLanguageMeta cloneMe(EtkProject project) {
        iPartsDataDictLanguageMeta clone = new iPartsDataDictLanguageMeta(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

}
