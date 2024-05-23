/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.attachments;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_PIC_TO_ATTACHMENT.
 */
public class iPartsDataPicOrderAttachmentReference extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_PTA_PICORDER, FIELD_DA_PTA_ATTACHMENT };

    public iPartsDataPicOrderAttachmentReference(EtkProject project, iPartsPicOrderAttachmentReferenceId id) {
        super(KEYS);
        tableName = TABLE_DA_PIC_TO_ATTACHMENT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsPicOrderAttachmentReferenceId createId(String... idValues) {
        return new iPartsPicOrderAttachmentReferenceId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsPicOrderAttachmentReferenceId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsPicOrderAttachmentReferenceId)id;
    }
}
