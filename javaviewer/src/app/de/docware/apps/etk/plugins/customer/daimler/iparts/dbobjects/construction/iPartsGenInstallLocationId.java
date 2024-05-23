/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Generischer Verbauort-ID (Tabelle DA_GENERIC_INSTALL_LOCATION) im iParts Plug-in.
 */
public class iPartsGenInstallLocationId extends IdWithType {

    public static final String TYPE = "DA_DialogGenInstallLocationId";

    /**
     * Bildet den Key für die genInstallationMap aus einem extern übergebenen {@param bcteKey}
     *
     * @param bcteKey
     * @return
     */
    public static String calcGenInstallMappingKeyFromBCTEKey(iPartsDialogBCTEPrimaryKey bcteKey) {
        iPartsGenInstallLocationId id = new iPartsGenInstallLocationId();
        return id.getGenInstallMappingKeyFromBCTEKey(bcteKey);
    }


    private enum INDEX {SERIES, HM, M, SM, POSE, DATE_FROM}

    public iPartsGenInstallLocationId(String series, String hm, String m, String sm, String posE, String kemFrom) {
        super(TYPE, new String[]{ series, hm, m, sm, posE, kemFrom });
    }

    public iPartsGenInstallLocationId(HmMSmId hmMSm, String posE, String kemFrom) {
        this(hmMSm.getSeries(), hmMSm.getHm(), hmMSm.getM(), hmMSm.getSm(), posE, kemFrom);
    }

    public iPartsGenInstallLocationId() {
        this("", "", "", "", "", "");
    }

    public String getSeries() {
        return id[INDEX.SERIES.ordinal()];
    }

    public String getHM() {
        return id[INDEX.HM.ordinal()];
    }

    public String getM() {
        return id[INDEX.M.ordinal()];
    }

    public String getSM() {
        return id[INDEX.SM.ordinal()];
    }

    public String getPosE() {
        return id[INDEX.POSE.ordinal()];
    }

    public String getDateFrom() {
        return id[INDEX.DATE_FROM.ordinal()];
    }

    public HmMSmId getHmMSmId() {
        return new HmMSmId(getSeries(), getHM(), getM(), getSM());
    }

    /**
     * Bildet den Key für die genInstallationMap aus einer iPartsGenInstallLocationId (this)
     *
     * @return
     */
    public String getGenInstallMappingKey() {
        return calcGenInstallMappingKey(getHmMSmId(), getPosE());
    }

    /**
     * Bildet den Key für die genInstallationMap aus einer {@param dialogGUID}
     *
     * @param dialogGUID
     * @return
     */
    public String getGenInstallMappingKeyFromBCTEKey(String dialogGUID) {
        return getGenInstallMappingKeyFromBCTEKey(iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dialogGUID));
    }

    /**
     * Bildet den Key für die genInstallationMap aus einem {@param bcteKey}
     *
     * @param bcteKey
     * @return
     */
    public String getGenInstallMappingKeyFromBCTEKey(iPartsDialogBCTEPrimaryKey bcteKey) {
        if (bcteKey != null) {
            return calcGenInstallMappingKey(bcteKey.getHmMSmId(), bcteKey.getPosE());
        }
        return "";
    }

    /**
     * Bildet den Key für die genInstallationMap aus {@param hmMSmId} und {@param posE}
     *
     * @param hmMSmId
     * @param posE
     * @return
     */
    private String calcGenInstallMappingKey(HmMSmId hmMSmId, String posE) {
        if (hmMSmId != null) {
            return hmMSmId.toString(iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER) +
                   iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER + posE;
        }
        return "";
    }

    @Override
    public String toString() {
        return "(" + getSeries() + "," + getHM() + "," + getM() + "," + getSM() + "," + getPosE() + "," + getDateFrom() + ")";
    }


}
