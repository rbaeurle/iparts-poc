/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.misc.id.IdWithType;

/**
 * ID für einen Datensatz der TABLE_DA_KEM_WORK_BASKET im iPartsEdit Plug-in.
 */
public class iPartsKEMWorkBasketEDSId extends IdWithType {


    public static final String TYPE = "DA_iPartsKEMWorkBasketId";

    protected enum INDEX {KEM, SAA_NO, PRODUCT_NO, KG_NO, MODULE_NO}

    /**
     * Der normale Konstruktor
     *
     * @param kemNo
     * @param saaNo
     * @param productNo
     * @param kgNo
     * @param moduleNo
     */
    public iPartsKEMWorkBasketEDSId(String kemNo, String saaNo, String productNo, String kgNo, String moduleNo) {
        super(TYPE, new String[]{ kemNo, saaNo, productNo, kgNo, moduleNo });
    }

    public iPartsKEMWorkBasketEDSId(String kemNo, String saaNo) {
        this(kemNo, saaNo, "", "", "");
    }

    public iPartsKEMWorkBasketEDSId(String kemNo) {
        this(kemNo, "", "", "", "");
    }

    public iPartsKEMWorkBasketEDSId(DBDataObjectAttributes attributes) {
        this(attributes.getFieldValue(iPartsConst.FIELD_DKWB_KEM), attributes.getFieldValue(iPartsConst.FIELD_DKWB_SAA),
             attributes.getFieldValue(iPartsConst.FIELD_DKWB_PRODUCT_NO), attributes.getFieldValue(iPartsConst.FIELD_DKWB_KG),
             attributes.getFieldValue(iPartsConst.FIELD_DKWB_MODULE_NO));
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsKEMWorkBasketEDSId() {
        this("");
    }

    /**
     * Für iPartsKEMWorkBasketEDSId basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsKEMWorkBasketEDSId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsKEMWorkBasketEDSId doesn't have length "
                                               + INDEX.values().length);
        }
    }


    public String getKEMNo() {
        return id[INDEX.KEM.ordinal()];
    }

    public String getSAANo() {
        return id[INDEX.SAA_NO.ordinal()];
    }

    public String getProductNo() {
        return id[INDEX.PRODUCT_NO.ordinal()];
    }

    public String getKgNo() {
        return id[INDEX.KG_NO.ordinal()];
    }

    public String getModuleNo() {
        return id[INDEX.MODULE_NO.ordinal()];
    }

    public boolean isOnlyKemNoSet() {
        if (!getKEMNo().isEmpty()) {
            for (INDEX index : INDEX.values()) {
                if (index == INDEX.KEM) {
                    continue;
                }
                if (!id[index.ordinal()].isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
