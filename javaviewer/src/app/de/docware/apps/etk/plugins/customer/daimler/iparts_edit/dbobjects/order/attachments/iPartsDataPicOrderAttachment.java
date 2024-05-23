/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.attachments;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsAttachmentStatus;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_PICORDER_ATTACHMENTS.
 */
public class iPartsDataPicOrderAttachment extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DPA_GUID };

    public iPartsDataPicOrderAttachment(EtkProject project, iPartsPicOrderAttachmentId id) {
        super(KEYS);
        tableName = TABLE_DA_PICORDER_ATTACHMENTS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsPicOrderAttachmentId createId(String... idValues) {
        return new iPartsPicOrderAttachmentId(idValues[0]);
    }

    @Override
    public iPartsPicOrderAttachmentId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsPicOrderAttachmentId)id;
    }

    public String getName() {
        return getFieldValue(FIELD_DPA_NAME);
    }

    public String getFileType() {
        return getFieldValue(FIELD_DPA_FILETYPE);
    }

    public iPartsAttachmentStatus getStatus() {
        return iPartsAttachmentStatus.getFromDBAlias(getFieldValue(FIELD_DPA_STATUS));
    }

    public void setStatus(iPartsAttachmentStatus status) {
        if (status != null) {
            setFieldValue(FIELD_DPA_STATUS, status.getDBStatus(), DBActionOrigin.FROM_EDIT);
        }
    }

    public String getSize() {
        return getFieldValue(FIELD_DPA_SIZE);
    }

    public boolean hasErrors() {
        return !StrUtils.isEmpty(getErrorCode());
    }

    public String getErrorCode() {
        return getFieldValue(FIELD_DPA_ERRORCODE);
    }

    public String getErrorText() {
        return getFieldValue(FIELD_DPA_ERRORTEXT);
    }

    public String getDesc() {
        return getFieldValue(FIELD_DPA_DESC);
    }

    public byte[] getContent() {
        return getFieldValueAsBlob(FIELD_DPA_CONTENT);
    }

    @Override
    public iPartsDataPicOrderAttachment cloneMe(EtkProject project) {
        iPartsDataPicOrderAttachment clone = new iPartsDataPicOrderAttachment(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }
}
