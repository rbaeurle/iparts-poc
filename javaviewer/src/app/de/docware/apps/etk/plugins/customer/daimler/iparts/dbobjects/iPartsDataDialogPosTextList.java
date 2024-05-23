/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataDialogPosText}.
 */
public class iPartsDataDialogPosTextList extends EtkDataObjectList<iPartsDataDialogPosText> implements iPartsConst {

    /**
     * Lädt alle Positionstexte für eine DIALOG Konstruktionsstückliste inkl. der mehrspachigen Texte für die aktuelle
     * DB-Sprache.
     *
     * @param project
     * @param hmMSmId
     * @return
     */
    public static iPartsDataDialogPosTextList loadAllTextForHmMSmId(EtkProject project, HmMSmId hmMSmId) {
        iPartsDataDialogPosTextList list = new iPartsDataDialogPosTextList();
        list.loadAllText(project, hmMSmId, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllText(EtkProject project, HmMSmId hmMSmId, DBActionOrigin origin) {

        clear(origin);
        String[] whereFields = new String[]{ FIELD_DD_POS_BR, FIELD_DD_POS_HM, FIELD_DD_POS_M, FIELD_DD_POS_SM };
        String[] whereValues = new String[]{ hmMSmId.getSeries(), hmMSmId.getHm(), hmMSmId.getM(), hmMSmId.getSm() };

        searchSortAndFillWithMultiLangValues(project, project.getDBLanguage(), null, whereFields, whereValues, false, null, false);
    }

    @Override
    protected iPartsDataDialogPosText getNewDataObject(EtkProject project) {
        return new iPartsDataDialogPosText(project, null);
    }
}
