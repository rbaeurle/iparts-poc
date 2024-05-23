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
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_EDS_SAA_MODELS.
 */
public class iPartsDataSAAModels extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_ESM_SAA_NO, FIELD_DA_ESM_MODEL_NO };

    public iPartsDataSAAModels(EtkProject project, iPartsSAAModelsId id) {
        super(KEYS);
        tableName = TABLE_DA_EDS_SAA_MODELS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsSAAModelsId createId(String... idValues) {
        return new iPartsSAAModelsId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsSAAModelsId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsSAAModelsId)id;
    }
}
