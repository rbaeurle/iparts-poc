/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.attachments;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataPicOrderAttachmentReference}s.
 */
public class iPartsDataPicOrderAttachmentReferenceList extends EtkDataObjectList<iPartsDataPicOrderAttachmentReference> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrderAttachmentReference}s für die übergebene {@link iPartsPicOrderId}.
     *
     * @param project
     * @param picOrderId
     * @return
     */
    public static iPartsDataPicOrderAttachmentReferenceList loadAttachmentsForPicOrder(EtkProject project, iPartsPicOrderId picOrderId) {
        iPartsDataPicOrderAttachmentReferenceList list = new iPartsDataPicOrderAttachmentReferenceList();
        list.loadAttachmentsForPicOrderFromDB(project, picOrderId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataPicOrderAttachmentReference} für die übergebene {@link iPartsPicOrderId}.
     *
     * @param project
     * @param picOrderId
     * @param origin
     */
    public void loadAttachmentsForPicOrderFromDB(EtkProject project, iPartsPicOrderId picOrderId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_PTA_PICORDER };
        String[] whereValues = new String[]{ picOrderId.getOrderGuid() };

        searchAndFill(project, TABLE_DA_PIC_TO_ATTACHMENT, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataPicOrderAttachmentReference getNewDataObject(EtkProject project) {
        return new iPartsDataPicOrderAttachmentReference(project, null);
    }
}
