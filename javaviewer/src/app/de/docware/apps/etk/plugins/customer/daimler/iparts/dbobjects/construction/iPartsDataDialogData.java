/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_DIALOG.
 */
public class iPartsDataDialogData extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DD_GUID };

    public iPartsDataDialogData(EtkProject project, iPartsDialogId id) {
        super(KEYS);
        tableName = TABLE_DA_DIALOG;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDialogId createId(String... idValues) {
        return new iPartsDialogId(idValues[0]);
    }

    @Override
    public iPartsDialogId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsDialogId)id;
    }

    @Override
    public void initAttributesWithDefaultValues(DBActionOrigin origin) {
        initAttributesWithEmptyValues(origin);
        setFieldValue(FIELD_DD_DOCU_RELEVANT, iPartsDocuRelevant.DOCU_RELEVANT_NOT_SPECIFIED.getDbValue(), origin);
    }
}
