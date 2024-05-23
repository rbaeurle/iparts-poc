/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.misc.id.IdWithType;

import java.util.Calendar;


/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_INTERNAL_TEXT.
 */

public class iPartsDataInternalText extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DIT_U_ID, FIELD_DIT_CREATION_DATE, FIELD_DIT_DO_TYPE, FIELD_DIT_DO_ID };

    public iPartsDataInternalText(EtkProject project, iPartsDataInternalTextId id) {
        super(KEYS);
        tableName = TABLE_DA_INTERNAL_TEXT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    public iPartsDataInternalText(String[] pkKeys) {
        super(pkKeys);
    }

    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsDataInternalTextId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsDataInternalTextId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsDataInternalTextId)id;
    }

    public String getUserId() {
        return getAsId().getUserId();
    }

    public Calendar getCreationTimeStamp() {
        return getFieldValueAsDateTime(FIELD_DIT_CREATION_DATE);
    }

    public Calendar getChangeTimeStamp() {
        return getFieldValueAsDateTime(FIELD_DIT_CHANGE_DATE);
    }

    public void setChangeTimeStamp(Calendar timeStampDate, DBActionOrigin origin) {
        setFieldValueAsDateTime(FIELD_DIT_CHANGE_DATE, timeStampDate, origin);
    }

    public String getObjectType() {
        return getAsId().getDataObjectType();
    }

    public String getDataObjectId() {
        return getAsId().getDataObjectId();
    }

    public String getText() {
        return getFieldValue(FIELD_DIT_TEXT);
    }

    public void setText(String text) {
        setFieldValue(FIELD_DIT_TEXT, text, DBActionOrigin.FROM_EDIT);
    }

    public byte[] getAttachment() {
        return getFieldValueAsBlob(FIELD_DIT_ATTACHMENT);
    }

    public void setAttachment(byte[] blob) {
        setFieldValueAsBlob(FIELD_DIT_ATTACHMENT, blob, DBActionOrigin.FROM_EDIT);
    }

    public String getTitle() {
        return getFieldValue(FIELD_DIT_TITEL);
    }

    public void setTitle(String dbValue, DBActionOrigin origin) {
        setFieldValue(FIELD_DIT_TITEL, dbValue, origin);
    }

    public String getFollowUpDateAsDB() {
        return getTitle();
    }

    public void setFollowUpDateAsDB(String dbValue, DBActionOrigin origin) {
        setTitle(dbValue, origin);
    }

}
