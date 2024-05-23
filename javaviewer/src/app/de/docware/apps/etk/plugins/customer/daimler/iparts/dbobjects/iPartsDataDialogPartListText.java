/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogPartListTextId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_DIALOG_PARTLIST_TEXT.
 */
public class iPartsDataDialogPartListText extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DD_PLT_BR, FIELD_DD_PLT_HM, FIELD_DD_PLT_M, FIELD_DD_PLT_SM,
                                                       FIELD_DD_PLT_POSE, FIELD_DD_PLT_POSV, FIELD_DD_PLT_WW, FIELD_DD_PLT_ETZ,
                                                       FIELD_DD_PLT_TEXTKIND, FIELD_DD_PLT_SDATA };

    public iPartsDataDialogPartListText(EtkProject project, iPartsDialogPartListTextId id) {
        super(KEYS);
        tableName = TABLE_DA_DIALOG_PARTLIST_TEXT;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDialogPartListTextId createId(String... idValues) {
        return new iPartsDialogPartListTextId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5], idValues[6], idValues[7], idValues[8], idValues[9]);
    }

    @Override
    public iPartsDialogPartListTextId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsDialogPartListTextId)id;
    }

}
