/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Repräsentiert ein Objekt aus der Tabelle DA_TOP_TUS im iParts Plug-in.
 */
public class iPartsDataTopTU extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DTT_PRODUCT_NO, FIELD_DTT_COUNTRY_CODE, FIELD_DTT_KG, FIELD_DTT_TU };

    public iPartsDataTopTU(EtkProject project, iPartsTopTUId id) {
        super(KEYS);
        tableName = TABLE_DA_TOP_TUS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsTopTUId createId(String... idValues) {
        return new iPartsTopTUId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsTopTUId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsTopTUId)id;
    }

    public void setRank(String rank, DBActionOrigin origin) {
        setFieldValue(FIELD_DTT_RANK, rank, origin);
    }

    public String getRank() {
        return getFieldValue(FIELD_DTT_RANK);
    }

    public int getIntRank() {
        return getFieldValueAsInteger(FIELD_DTT_RANK);
    }
}
