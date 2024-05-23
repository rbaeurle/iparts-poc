/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsKEMWorkBasketEDSId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevantTruck;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_KEM_WORK_BASKET.
 */
public class iPartsDataKEMWorkBasketEDS extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DKWB_KEM, FIELD_DKWB_SAA, FIELD_DKWB_PRODUCT_NO,
                                                       FIELD_DKWB_KG, FIELD_DKWB_MODULE_NO };

    public iPartsDataKEMWorkBasketEDS(EtkProject project, iPartsKEMWorkBasketEDSId id) {
        super(KEYS);
        tableName = TABLE_DA_KEM_WORK_BASKET;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsKEMWorkBasketEDSId createId(String... idValues) {
        return new iPartsKEMWorkBasketEDSId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4]);
    }

    @Override
    public iPartsKEMWorkBasketEDSId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsKEMWorkBasketEDSId)id;
    }

    /**
     * Initialisiert diesen Datensatz mit Default Werten soweit vorhanden, sonst leere Werte durch Aufruf von
     * {@link #initAttributesWithDefaultValues}
     *
     * @param origin
     */
    @Override
    public void initAttributesWithDefaultValues(DBActionOrigin origin) {
        super.initAttributesWithDefaultValues(origin);
        setFieldValue(FIELD_DKWB_DOCU_RELEVANT, iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_SPECIFIED.getDbValue(), origin);
    }

    // Convenience Methods
    public iPartsDocuRelevantTruck getDocuRelevant() {
        return iPartsDocuRelevantTruck.getFromDBValue(getFieldValue(FIELD_DKWB_DOCU_RELEVANT));
    }

    public void setDocuRelevant(iPartsDocuRelevantTruck docuRelevant, DBActionOrigin origin) {
        setFieldValue(FIELD_DKWB_DOCU_RELEVANT, docuRelevant.getDbValue(), origin);
    }

    public iPartsSaaId getSAAId() {
        return new iPartsSaaId(getAsId().getSAANo());
    }

    public iPartsProductId getProductId() {
        return new iPartsProductId(getAsId().getProductNo());
    }

    public AssemblyId getAssemblyId() {
        return new AssemblyId(getAsId().getModuleNo(), "");
    }
}
