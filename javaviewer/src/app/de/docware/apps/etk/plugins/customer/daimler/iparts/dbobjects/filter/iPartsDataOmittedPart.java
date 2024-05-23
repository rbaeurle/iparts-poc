/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_OMITTED_PARTS.
 */
public class iPartsDataOmittedPart extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_OP_PARTNO };

    public iPartsDataOmittedPart(EtkProject project, iPartsOmittedPartId id) {
        super(KEYS);
        tableName = TABLE_DA_OMITTED_PARTS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsOmittedPartId createId(String... idValues) {
        return new iPartsOmittedPartId(idValues[0]);
    }

    @Override
    public iPartsOmittedPartId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsOmittedPartId)id;
    }
}
