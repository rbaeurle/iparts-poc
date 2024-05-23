/*
 * Copyright (c) 2020 Docware GmbH
 */

/**
 * Erweiterung von {@link de.docware.apps.etk.base.project.base.EtkDataObject}
 * um iParts-spezifische Methoden und Daten für Tabelle DA_KEM_WORK_BASKET_MBS.
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsKEMWorkBasketMBSId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevantTruck;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataKEMWorkBasketMBS extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DKWM_KEM, FIELD_DKWM_SAA,
                                                       FIELD_DKWM_GROUP, FIELD_DKWM_PRODUCT_NO,
                                                       FIELD_DKWM_KG, FIELD_DKWM_MODULE_NO };

    public iPartsDataKEMWorkBasketMBS(EtkProject project, iPartsKEMWorkBasketMBSId id) {
        super(KEYS);
        tableName = TABLE_DA_KEM_WORK_BASKET_MBS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsKEMWorkBasketMBSId createId(String... idValues) {
        return new iPartsKEMWorkBasketMBSId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4], idValues[5]);
    }

    @Override
    public iPartsKEMWorkBasketMBSId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsKEMWorkBasketMBSId)id;
    }

    /**
     * Initialisiert diesen Datensatz mit default-Werten.
     *
     * @param origin
     */
    @Override
    public void initAttributesWithDefaultValues(DBActionOrigin origin) {
        super.initAttributesWithDefaultValues(origin);
        setFieldValue(FIELD_DKWM_DOCU_RELEVANT, iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_SPECIFIED.getDbValue(), origin);
    }

    public iPartsDocuRelevantTruck getDocuRelevant() {
        return iPartsDocuRelevantTruck.getFromDBValue(getFieldValue(FIELD_DKWM_DOCU_RELEVANT));
    }

    public void setDocuRelevant(iPartsDocuRelevantTruck docuRelevant, DBActionOrigin origin) {
        setFieldValue(FIELD_DKWM_DOCU_RELEVANT, docuRelevant.getDbValue(), origin);
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
