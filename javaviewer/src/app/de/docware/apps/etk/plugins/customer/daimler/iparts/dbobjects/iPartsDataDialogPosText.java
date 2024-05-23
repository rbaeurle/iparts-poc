/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogPosTextId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_DIALOG_POS_TEXT.
 */
public class iPartsDataDialogPosText extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DD_POS_BR, FIELD_DD_POS_HM, FIELD_DD_POS_M, FIELD_DD_POS_SM, FIELD_DD_POS_POS, FIELD_DD_POS_SDATA };

    public iPartsDataDialogPosText(EtkProject project, iPartsDialogPosTextId id) {
        super(KEYS);
        tableName = TABLE_DA_DIALOG_POS_TEXT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDialogPosTextId createId(String... idValues) {
        return new iPartsDialogPosTextId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5]);
    }

    @Override
    public iPartsDialogPosTextId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsDialogPosTextId)id;
    }

}
