/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.List;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_BRANCH_PC_MAPPING.
 */
public class iPartsDataBranchProductClass extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DBM_BRANCH };

    public iPartsDataBranchProductClass(EtkProject project, iPartsBranchProductClassId id) {
        super(KEYS);
        tableName = TABLE_DA_BRANCH_PC_MAPPING;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsBranchProductClassId createId(String... idValues) {
        return new iPartsBranchProductClassId(idValues[0]);
    }

    @Override
    public iPartsBranchProductClassId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsBranchProductClassId)id;
    }

    public List<String> getProductClasses() {
        return getFieldValueAsSetOfEnum(FIELD_DBM_AS_PRODUCT_CLASSES);
    }
}
