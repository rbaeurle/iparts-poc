/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_AGG_CODE_MAPPING
 */
public class iPartsDataAggCodeMapping extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DAPC_PART_NO, FIELD_DAPC_CODE, FIELD_DAPC_SERIES_NO, FIELD_DAPC_FACTORY,
                                                       FIELD_DAPC_FACTORY_SIGN, FIELD_DAPC_DATE_FROM, FIELD_DAPC_DATE_TO };

    public iPartsDataAggCodeMapping(EtkProject project, iPartsAggCodeMappingId id) {
        super(KEYS);
        tableName = TABLE_DA_AGG_PART_CODES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsAggCodeMappingId createId(String... idValues) {
        return new iPartsAggCodeMappingId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5], idValues[6]);
    }

    @Override
    public iPartsAggCodeMappingId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsAggCodeMappingId)id;
    }

    /**
     * Liefert die Baureihe zu Sachnummer und Code
     *
     * @return
     */
    public String getSeriesNo() {
        return getFieldValue(FIELD_DAPC_SERIES_NO);
    }
}
