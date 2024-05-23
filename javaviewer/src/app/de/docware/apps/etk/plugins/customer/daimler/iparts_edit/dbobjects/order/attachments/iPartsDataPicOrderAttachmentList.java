/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.attachments;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.sql.TableAndFieldName;

/**
 * Liste von {@link iPartsDataPicOrderAttachment}s.
 */
public class iPartsDataPicOrderAttachmentList extends EtkDataObjectList<iPartsDataPicOrderAttachment> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrderAttachment}s für die übergebene PicOrder-GUID.
     *
     * @param project
     * @param picOrderId
     * @return
     */
    public static iPartsDataPicOrderAttachmentList loadAttachmentsForPicOrder(EtkProject project, iPartsPicOrderId picOrderId) {
        iPartsDataPicOrderAttachmentList list = new iPartsDataPicOrderAttachmentList();
        list.loadAttachmentsForPicOrderFromDB(project, picOrderId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataPicOrderAttachment} für die übergebene PicOrder-GUID. Die Verwaltung Bildauftrag
     * zu Anhang wird in DA_PIC_TO_ATTACHMENT gehalten, daher werden die Anhänge über einen Join auf diese Tabelle geladen.
     *
     * @param project
     * @param picOrderId
     * @param origin
     */
    public void loadAttachmentsForPicOrderFromDB(EtkProject project, iPartsPicOrderId picOrderId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_PIC_TO_ATTACHMENT, FIELD_DA_PTA_PICORDER) };
        String[] whereValues = new String[]{ picOrderId.getOrderGuid() };


        searchSortAndFillWithJoin(project, project.getDBLanguage(), null,
                                  new String[]{ FIELD_DPA_GUID },
                                  TABLE_DA_PIC_TO_ATTACHMENT,
                                  new String[]{ FIELD_DA_PTA_ATTACHMENT },
                                  false, false,
                                  whereFields,
                                  whereValues,
                                  false, null, false);
    }

    @Override
    protected iPartsDataPicOrderAttachment getNewDataObject(EtkProject project) {
        return new iPartsDataPicOrderAttachment(project, null);
    }
}
