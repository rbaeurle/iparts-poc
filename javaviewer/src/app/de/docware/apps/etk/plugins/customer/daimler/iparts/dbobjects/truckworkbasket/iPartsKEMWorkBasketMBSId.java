/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

/**
 * ID für einen Datensatz der TABLE_DA_KEM_WORK_BASKET_MBS im iPartsEdit Plug-in.
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

public class iPartsKEMWorkBasketMBSId extends IdWithType {

    public static final String TYPE = "DA_iPartsKEMWorkBasketMBSId";

    protected enum INDEX {KEM, SAA_NO, GROUP, PRODUCT_NO, KG_NO, MODULE_NO}

    /**
     * Der normale Konstruktor
     *
     * @param kemNo
     * @param saaNo
     * @param productNo
     * @param kgNo
     * @param moduleNo
     */
    public iPartsKEMWorkBasketMBSId(String kemNo, String saaNo, String group, String productNo, String kgNo, String moduleNo) {
        super(TYPE, new String[]{ kemNo, saaNo, group, productNo, kgNo, moduleNo });
    }

    public iPartsKEMWorkBasketMBSId(String kemNo, String saaNo, String group) {
        this(kemNo, saaNo, group, "", "", "");
    }

    public iPartsKEMWorkBasketMBSId(String kemNo, String saaNo) {
        this(kemNo, saaNo, "", "", "", "");
    }

    public iPartsKEMWorkBasketMBSId(String kemNo) {
        this(kemNo, "", "", "", "", "");
    }

    public iPartsKEMWorkBasketMBSId(DBDataObjectAttributes attrib) {
        this(attrib.getFieldValue(iPartsConst.FIELD_DKWM_KEM), attrib.getFieldValue(iPartsConst.FIELD_DKWM_SAA),
             attrib.getFieldValue(iPartsConst.FIELD_DKWM_GROUP), attrib.getFieldValue(iPartsConst.FIELD_DKWM_PRODUCT_NO),
             attrib.getFieldValue(iPartsConst.FIELD_DKWM_KG), attrib.getFieldValue(iPartsConst.FIELD_DKWM_MODULE_NO));
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsKEMWorkBasketMBSId() {
        this("");
    }

    /**
     * Für iPartsKEMWorkBasketMBSId basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsKEMWorkBasketMBSId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsKEMWorkBasketMBSId doesn't have length "
                                               + INDEX.values().length);
        }
    }


    public String getKEMNo() {
        return id[iPartsKEMWorkBasketMBSId.INDEX.KEM.ordinal()];
    }

    public String getSAANo() {
        return id[iPartsKEMWorkBasketMBSId.INDEX.SAA_NO.ordinal()];
    }

    public String getGroup() {
        return id[iPartsKEMWorkBasketMBSId.INDEX.GROUP.ordinal()];
    }

    public String getProductNo() {
        return id[iPartsKEMWorkBasketMBSId.INDEX.PRODUCT_NO.ordinal()];
    }

    public String getKgNo() {
        return id[iPartsKEMWorkBasketMBSId.INDEX.KG_NO.ordinal()];
    }

    public String getModuleNo() {
        return id[iPartsKEMWorkBasketMBSId.INDEX.MODULE_NO.ordinal()];
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

    /**
     * Ermittelt die in der Group enthaltene KG Nummer falls vorhanden
     *
     * @return "" falls keine KG enthalten
     */
    public String extractKgFromGroup() {
        return StrUtils.stringAfterCharacter(getGroup(), "KG");
    }
}
