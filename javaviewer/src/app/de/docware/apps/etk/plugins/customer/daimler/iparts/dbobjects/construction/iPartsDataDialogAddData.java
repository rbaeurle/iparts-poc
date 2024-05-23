/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogAddDataId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_DIALOG_ADD_DATA.
 */
public class iPartsDataDialogAddData extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DAD_GUID, FIELD_DAD_ADAT };

    public iPartsDataDialogAddData(EtkProject project, iPartsDialogAddDataId id) {
        super(KEYS);
        tableName = TABLE_DA_DIALOG_ADD_DATA;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDialogAddDataId createId(String... idValues) {
        return new iPartsDialogAddDataId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsDialogAddDataId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsDialogAddDataId)id;
    }

    @Override
    public iPartsDataDialogAddData cloneMe(EtkProject project) {
        iPartsDataDialogAddData clone = new iPartsDataDialogAddData(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }
}
