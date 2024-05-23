/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.Calendar;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_CONFIRM_CHANGES.
 */
public class iPartsDataConfirmChanges extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DCC_CHANGE_SET_ID, FIELD_DCC_DO_TYPE, FIELD_DCC_DO_ID, FIELD_DCC_PARTLIST_ENTRY_ID };

    public iPartsDataConfirmChanges(EtkProject project, iPartsConfirmChangesId id) {
        super(KEYS);
        tableName = TABLE_DA_CONFIRM_CHANGES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsConfirmChangesId createId(String... idValues) {
        return new iPartsConfirmChangesId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsConfirmChangesId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsConfirmChangesId)id;
    }

    /**
     * Akzeptiert die Änderungen dieses Datensatzes, indem der eingeloggte Benutzer sowie das aktuelle Datum in den
     * Datensatz geschrieben werden.
     */
    public void confirmChanges() {
        setFieldValue(FIELD_DCC_CONFIRMATION_USER, iPartsUserAdminDb.getLoginUserName(), DBActionOrigin.FROM_EDIT);
        setFieldValueAsDateTime(FIELD_DCC_CONFIRMATION_DATE, Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
    }
}