/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogPartListTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataDialogPartListText}.
 */
public class iPartsDataDialogPartListTextList extends EtkDataObjectList<iPartsDataDialogPartListText> implements iPartsConst {

    /**
     * Lädt alle Texte für eine DIALOG Konstruktionsstückliste inkl. der mehrspachigen Texte für die aktuelle DB-Sprache.
     *
     * @param project
     * @param hmMSmId
     * @return
     */
    public static iPartsDataDialogPartListTextList loadAllTextForHmMSmId(EtkProject project, HmMSmId hmMSmId) {
        iPartsDataDialogPartListTextList list = new iPartsDataDialogPartListTextList();
        list.loadAllText(project, hmMSmId, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllText(EtkProject project, HmMSmId hmMSmId, DBActionOrigin origin) {

        clear(origin);
        String[] whereFields = new String[]{ FIELD_DD_PLT_BR, FIELD_DD_PLT_HM, FIELD_DD_PLT_M, FIELD_DD_PLT_SM };
        String[] whereValues = new String[]{ hmMSmId.getSeries(), hmMSmId.getHm(), hmMSmId.getM(), hmMSmId.getSm() };

        searchSortAndFillWithMultiLangValues(project, project.getDBLanguage(), null, whereFields, whereValues, false, null, false);
    }

    @Override
    protected iPartsDataDialogPartListText getNewDataObject(EtkProject project) {
        return new iPartsDataDialogPartListText(project, null);
    }

    public static iPartsDataDialogPartListTextList loadAllRelatedDataForPartListTextId(EtkProject project, iPartsDialogPartListTextId partListTextId) {
        iPartsDataDialogPartListTextList list = new iPartsDataDialogPartListTextList();
        list.loadAllTextForId(project, partListTextId, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllTextForId(EtkProject project, iPartsDialogPartListTextId partListTextId, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DD_PLT_BR, FIELD_DD_PLT_HM, FIELD_DD_PLT_M, FIELD_DD_PLT_SM, FIELD_DD_PLT_POSE,
                                             FIELD_DD_PLT_POSV, FIELD_DD_PLT_WW, FIELD_DD_PLT_ETZ, FIELD_DD_PLT_TEXTKIND };
        String[] whereValues = new String[]{ partListTextId.getHmMSmId().getSeries(), partListTextId.getHmMSmId().getHm(),
                                             partListTextId.getHmMSmId().getM(), partListTextId.getHmMSmId().getSm(),
                                             partListTextId.getPos(), partListTextId.getPV(), partListTextId.getWW(),
                                             partListTextId.getEtz(), partListTextId.getTextArt() };

        searchSortAndFill(project, TABLE_DA_DIALOG_PARTLIST_TEXT, whereFields, whereValues, null, LoadType.COMPLETE, origin);
    }
}
