/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketSaaStatesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_WB_SAA_STATES.
 */
public class iPartsDataWorkBasketSaaStates extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_WBS_MODEL_NO, FIELD_WBS_PRODUCT_NO, FIELD_WBS_SAA, FIELD_WBS_SOURCE };

    public iPartsDataWorkBasketSaaStates(EtkProject project, iPartsWorkBasketSaaStatesId id) {
        super(KEYS);
        tableName = TABLE_DA_WB_SAA_STATES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsWorkBasketSaaStatesId createId(String... idValues) {
        return new iPartsWorkBasketSaaStatesId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsWorkBasketSaaStatesId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsWorkBasketSaaStatesId)id;
    }

    public iPartsSaaId getSAAId() {
        return new iPartsSaaId(getAsId().getSAANo());
    }

    public iPartsProductId getProductId() {
        return new iPartsProductId(getAsId().getProductNo());
    }

    public iPartsModelId getModelId() {
        return new iPartsModelId(getAsId().getModelNo());
    }
}
