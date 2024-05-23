/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

/**
 * Liste von {@link iPartsDataPicOrderPicture}s.
 */
public class iPartsDataPicOrderPicturesList extends EtkDataObjectList<iPartsDataPicOrderPicture> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrderPicture}s für die übergebene Auftrags-GUID.
     *
     * @param project
     * @param orderGuid
     * @return
     */
    public static iPartsDataPicOrderPicturesList loadPicOrderPicturesList(EtkProject project, String orderGuid) {
        iPartsDataPicOrderPicturesList list = new iPartsDataPicOrderPicturesList();
        list.loadPicOrderPicturesFromDB(project, orderGuid, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrderPicture}s für das übergebene {@link EtkDataImage}.
     * Gesucht wird nach der Bildnummer und nach der Bildversion (sofern vorhanden). Sortiert wird absteiged nach der
     * Bildversion.
     *
     * @param project
     * @param picture
     * @return
     */
    public static iPartsDataPicOrderPicturesList loadPicturesListForPicture(EtkProject project, EtkDataImage picture) {
        iPartsDataPicOrderPicturesList list = new iPartsDataPicOrderPicturesList();
        list.loadPicturesListForPictureFromDB(project, picture, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadPicturesListForPictureFromDB(EtkProject project, EtkDataImage picture, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_POP_PIC_ITEMID };
        String[] whereValues = new String[]{ picture.getFieldValue(FIELD_I_IMAGES) };
        String picVersion = picture.getFieldValue(FIELD_I_PVER);
        if (StrUtils.isValid(picVersion)) {
            whereFields = StrUtils.mergeArrays(whereFields, FIELD_DA_POP_PIC_ITEMREVID);
            whereValues = StrUtils.mergeArrays(whereValues, picVersion);
        }
        searchSortAndFill(project, TABLE_DA_PICORDER_PICTURES, whereFields, whereValues, new String[]{ FIELD_DA_POP_PIC_ITEMREVID },
                          LoadType.COMPLETE, true, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataPicOrderPicture} für die übergebene Auftrags-GUID.
     *
     * @param project
     * @param orderGuid
     * @param origin
     */
    public void loadPicOrderPicturesFromDB(EtkProject project, String orderGuid, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_POP_ORDER_GUID };
        String[] whereValues = new String[]{ orderGuid };
        searchAndFill(project, TABLE_DA_PICORDER_PICTURES, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataPicOrderPicture getNewDataObject(EtkProject project) {
        return new iPartsDataPicOrderPicture(project, null);
    }
}
