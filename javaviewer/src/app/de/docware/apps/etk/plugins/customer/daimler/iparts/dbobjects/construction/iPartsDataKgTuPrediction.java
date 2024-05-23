/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle TABLE_DA_HMMSM_KGTU.
 */
public class iPartsDataKgTuPrediction extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DHK_BCTE };

    public iPartsDataKgTuPrediction(EtkProject project, iPartsKgTuPredictionId id) {
        super(KEYS);
        tableName = TABLE_DA_HMMSM_KGTU;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsKgTuPredictionId createId(String... idValues) {
        return new iPartsKgTuPredictionId(idValues[0]);
    }

    @Override
    public iPartsKgTuPredictionId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsKgTuPredictionId)id;
    }

    public String getDialogId() {
        return getAsId().getDialogId();
    }

    public String getKG() {
        return getFieldValue(FIELD_DHK_KG_PREDICTION);
    }

    public String getTU() {
        return getFieldValue(FIELD_DHK_TU_PREDICTION);
    }

    public KgTuId getKgTuId() {
        return new KgTuId(getKG(), getTU());
    }
}
