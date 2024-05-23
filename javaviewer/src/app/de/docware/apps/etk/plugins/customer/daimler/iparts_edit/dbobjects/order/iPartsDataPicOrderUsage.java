/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link de.docware.apps.etk.base.project.base.EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_PICORDER_USAGE.
 */
public class iPartsDataPicOrderUsage extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_POU_ORDER_GUID, FIELD_DA_POU_PRODUCT_NO, FIELD_DA_POU_EINPAS_HG, FIELD_DA_POU_EINPAS_G, FIELD_DA_POU_EINPAS_TU, FIELD_DA_POU_KG, FIELD_DA_POU_TU };

    public iPartsDataPicOrderUsage(EtkProject project, iPartsPicOrderUsageId id) {
        super(KEYS);
        tableName = TABLE_DA_PICORDER_USAGE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsPicOrderUsageId createId(String... idValues) {
        return new iPartsPicOrderUsageId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5], idValues[6]);
    }

    @Override
    public iPartsPicOrderUsageId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsPicOrderUsageId)id;
    }

    // Convenience Method
    public EinPasId getEinPASId() {
        if (!getAsId().getEinPasHg().isEmpty()) {
            return new EinPasId(getAsId().getEinPasHg(), getAsId().getEinPasG(), getAsId().getEinPasTu());
        }
        return null;
    }

    public KgTuId getKgTuId() {
        if (!getAsId().getKg().isEmpty()) {
            return new KgTuId(getAsId().getKg(), getAsId().getTu());
        }
        return null;
    }

    @Override
    public iPartsDataPicOrderUsage cloneMe(EtkProject project) {
        iPartsDataPicOrderUsage clone = new iPartsDataPicOrderUsage(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    /**
     * Liefert den {@link de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst.PRODUCT_STRUCTURING_TYPE}
     * basierend auf den Werten der ID.
     *
     * @return
     */
    public PRODUCT_STRUCTURING_TYPE getProductStructuringType() {
        if (getKgTuId() != null) {
            return PRODUCT_STRUCTURING_TYPE.KG_TU;
        } else if (getEinPASId() != null) {
            return PRODUCT_STRUCTURING_TYPE.EINPAS;
        } else {
            return null;
        }
    }
}
