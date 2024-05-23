/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataEPCFootNoteContent}s.
 */
public class iPartsDataEPCFootNoteContentList extends EtkDataObjectList<iPartsDataEPCFootNoteContent> implements iPartsConst {

    /**
     * Lädt eine Fußnote zu einem EPC Fußnotentyp und einer EPC Fußnoten-ID(sortiert nach Zeilennummer)
     *
     * @param project
     * @param footnoteType
     * @param epcFNTextId
     * @return
     */
    public static iPartsDataEPCFootNoteContentList loadFootNote(EtkProject project, EPCFootnoteType footnoteType, String epcFNTextId) {
        iPartsDataEPCFootNoteContentList list = new iPartsDataEPCFootNoteContentList();
        list.loadEPCFootNoteFromDB(project, footnoteType, epcFNTextId, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadEPCFootNoteFromDB(EtkProject project, EPCFootnoteType footnoteType, String epcFNTextId, LoadType loadType, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DEFC_TYPE, FIELD_DEFC_TEXT_ID };
        String[] whereValues = new String[]{ footnoteType.getDBValue(), epcFNTextId };
        String[] sortFields = new String[]{ FIELD_DFNC_LINE_NO };
        searchSortAndFill(project, TABLE_DA_EPC_FN_CONTENT, whereFields, whereValues, sortFields,
                          loadType, origin);
    }

    @Override
    protected iPartsDataEPCFootNoteContent getNewDataObject(EtkProject project) {
        return new iPartsDataEPCFootNoteContent(project, null);
    }
}
