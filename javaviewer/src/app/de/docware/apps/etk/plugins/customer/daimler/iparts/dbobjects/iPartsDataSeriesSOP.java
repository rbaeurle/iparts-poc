/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.Calendar;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_SERIES_SOP
 */
public class iPartsDataSeriesSOP extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DSP_SERIES_NO, FIELD_DSP_AA };

    public iPartsDataSeriesSOP(EtkProject project, iPartsSeriesSOPId id) {
        super(KEYS);
        tableName = TABLE_DA_SERIES_SOP;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsSeriesSOPId createId(String... idValues) {
        return new iPartsSeriesSOPId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsSeriesSOPId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsSeriesSOPId)id;
    }

    /**
     * Initialisiert diesen Datensatz mit Default Werten soweit vorhanden, sonst leere Werte durch Aufruf von
     * {@link #initAttributesWithDefaultValues}
     *
     * @param origin
     */
    @Override
    public void initAttributesWithDefaultValues(DBActionOrigin origin) {
        initAttributesWithEmptyValues(origin);
        Calendar calendar = Calendar.getInstance();
        setFieldValueAsDateTime(FIELD_DSP_START_OF_PROD, calendar, origin);
        calendar.add(Calendar.MONTH, -12);
        setFieldValueAsDateTime(FIELD_DSP_KEM_TO, calendar, origin);
        setFieldValueAsBoolean(FIELD_DSP_ACTIVE, false, origin);
    }

}
