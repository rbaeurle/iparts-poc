/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_MODELS_AGGS.
 */
public class iPartsDataModelsAggs extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DMA_MODEL_NO, FIELD_DMA_AGGREGATE_NO };

    public iPartsDataModelsAggs(EtkProject project, iPartsModelsAggsId id) {
        super(KEYS);
        tableName = TABLE_DA_MODELS_AGGS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsModelsAggsId createId(String... idValues) {
        return new iPartsModelsAggsId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsModelsAggsId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsModelsAggsId)id;
    }

    public String getSource() {
        return getFieldValue(iPartsConst.FIELD_DMA_SOURCE);
    }
}
