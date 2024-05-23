/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.truckworkbasket;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.misc.id.IdWithType;

/**
 * ID für einen Datensatz der Tabelle TABLE_DA_WB_SAA_CALCULATION im iPartsEdit Plug-in.
 */
public class iPartsWorkBasketSaaCalcId extends IdWithType {

    public static final String TYPE = "DA_iPartsWorkBasketSaaCalcId";

    protected enum INDEX {SOURCE, MODEL_NO, SAA}

    public iPartsWorkBasketSaaCalcId(String source, String modelNo, String saaNo) {
        super(TYPE, new String[]{ source, modelNo, saaNo });
    }

    public iPartsWorkBasketSaaCalcId(DBDataObjectAttributes attributes) {
        this(attributes.getFieldValue(iPartsConst.FIELD_WSC_SOURCE),
             attributes.getFieldValue(iPartsConst.FIELD_WSC_MODEL_NO),
             attributes.getFieldValue(iPartsConst.FIELD_WSC_SAA));
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsWorkBasketSaaCalcId() {
        this("", "", "");
    }

    public String getSource() {
        return id[INDEX.SOURCE.ordinal()];
    }

    public String getModelNo() {
        return id[INDEX.MODEL_NO.ordinal()];
    }

    public String getSaa() {
        return id[INDEX.SAA.ordinal()];
    }

}
