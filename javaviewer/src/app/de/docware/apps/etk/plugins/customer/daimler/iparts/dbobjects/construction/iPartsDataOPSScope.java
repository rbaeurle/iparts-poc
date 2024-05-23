/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.utils.EtkMultiSprache;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_OPS_SCOPE.
 */
public class iPartsDataOPSScope extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DOS_SCOPE };

    public iPartsDataOPSScope(EtkProject project, iPartsOPSScopeId id) {
        super(KEYS);
        tableName = TABLE_DA_OPS_SCOPE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsOPSScopeId createId(String... idValues) {
        return new iPartsOPSScopeId(idValues[0]);
    }

    @Override
    public iPartsOPSScopeId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsOPSScopeId)id;
    }

    public EtkMultiSprache getDescription() {
        return getFieldValueAsMultiLanguage(FIELD_DOS_DESC);
    }

    public String getPictureName() {
        return getFieldValue(FIELD_DOS_PICTURE);
    }

}
