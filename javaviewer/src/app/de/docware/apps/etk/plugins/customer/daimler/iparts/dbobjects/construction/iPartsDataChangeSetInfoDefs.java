/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für die Tabelle DA_CHANGE_SET_INFO_DEFS
 */
public class iPartsDataChangeSetInfoDefs extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DCID_DO_TYPE, FIELD_DCID_FELD, FIELD_DCID_AS_RELEVANT };

    protected iPartsDataChangeSetInfoDefs(EtkProject project, iPartsChangeSetInfoDefsId id) {
        super(KEYS);
        tableName = TABLE_DA_CHANGE_SET_INFO_DEFS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsChangeSetInfoDefsId createId(String... idValues) {
        return new iPartsChangeSetInfoDefsId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsChangeSetInfoDefsId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsChangeSetInfoDefsId)id;
    }
}
