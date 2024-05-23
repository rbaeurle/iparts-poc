/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.truckworkbasket;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_WB_SAA_CALCULATION.
 */
public class iPartsDataWorkBasketCalc extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_WSC_SOURCE, FIELD_WSC_MODEL_NO, FIELD_WSC_SAA };

    public iPartsDataWorkBasketCalc(EtkProject project, iPartsWorkBasketSaaCalcId id) {
        super(KEYS);
        tableName = TABLE_DA_WB_SAA_CALCULATION;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsWorkBasketSaaCalcId createId(String... idValues) {
        return new iPartsWorkBasketSaaCalcId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsWorkBasketSaaCalcId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsWorkBasketSaaCalcId)id;
    }

    public String getSource() {
        return getAsId().getSource();
    }

    public iPartsSaaId getSAAId() {
        return new iPartsSaaId(getAsId().getSaa());
    }

    public iPartsModelId getModelId() {
        return new iPartsModelId(getAsId().getModelNo());
    }

    public String getMinReleasedFrom() {
        return getFieldValue(FIELD_WSC_MIN_RELEASE_FROM);
    }

    public String getMaxReleasedTo() {
        return getFieldValue(FIELD_WSC_MAX_RELEASE_TO);
    }

    public void setMinReleasedFrom(String value, DBActionOrigin origin) {
        setFieldValue(FIELD_WSC_MIN_RELEASE_FROM, value, origin);
    }

    public void setMaxReleasedTo(String value, DBActionOrigin origin) {
        setFieldValue(FIELD_WSC_MAX_RELEASE_TO, value, origin);
    }

    public void setCode(String value, DBActionOrigin origin) {
        setFieldValue(FIELD_WSC_CODE, value, origin);
    }

    public void setFactories(String value, DBActionOrigin origin) {
        setFieldValue(FIELD_WSC_FACTORIES, value, origin);
    }
}
