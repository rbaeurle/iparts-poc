/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_MODEL_OIL_QUANTITY.
 */
public class iPartsDataModelOilQuantity extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DMOQ_MODEL_NO, FIELD_DMOQ_CODE_VALIDITY, FIELD_DMOQ_SPEC_TYPE,
                                                       FIELD_DMOQ_IDENT_TO, FIELD_DMOQ_IDENT_FROM };

    public iPartsDataModelOilQuantity(EtkProject project, iPartsModelOilQuantityId id) {
        super(KEYS);
        tableName = TABLE_DA_MODEL_OIL_QUANTITY;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsModelOilQuantityId createId(String... idValues) {
        return new iPartsModelOilQuantityId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4]);
    }

    @Override
    public iPartsModelOilQuantityId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsModelOilQuantityId)id;
    }
}